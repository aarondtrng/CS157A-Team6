package service;

import dao.DataAccessException;
import dao.ItemDao;
import dao.RedditItemDao;
import dao.UserDao;
import dto.RedditPost;
import model.Category;
import model.PendingRedditItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.ImageSniffer;
import util.ImageStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The "Scraping Mechanism": reads recent lost-and-found posts from r/SJSU and saves each new one as an
 * item post that stays hidden until an administrator approves it.
 *
 * Posts come from FetchLayer (a Reddit data API) when SPARTANFINDS_FETCHLAYER_KEY is set, because Reddit
 * now blocks unauthenticated requests. Without a key it tries Reddit's public JSON directly.
 */
public class RedditScrapeService {
    private static final Logger LOG = Logger.getLogger(RedditScrapeService.class.getName());

    static final String SEARCH_URL = "https://www.reddit.com/r/SJSU/search.json"
            + "?q=" + URLEncoder.encode("lost OR found OR missing", StandardCharsets.UTF_8)
            + "&restrict_sr=1&sort=new&t=month&limit=50";
    static final String FETCHLAYER_URL = "https://api.fetchlayer.dev/reddit/search";
    /** Environment variable (or JVM system property) holding the FetchLayer key ("ss-..."). */
    static final String FETCHLAYER_KEY_SETTING = "SPARTANFINDS_FETCHLAYER_KEY";
    /** Reddit asks API clients to identify themselves; generic clients get blocked more often. */
    static final String USER_AGENT = "java:spartanfinds-cs157a:0.1 (school project)";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    /** The review queue on the admin dashboard shows at most this many posts. */
    public static final int MAX_QUEUE_ROWS = 50;

    /** Error keys passed in the redirect URL; the dashboard only shows messages for these. */
    public static final Map<String, String> ERROR_MESSAGES = Map.of(
            "rate_limited", "Too many requests right now. Try again in a few minutes.",
            "blocked", "Reddit refused the request. Set SPARTANFINDS_FETCHLAYER_KEY to import through FetchLayer.",
            "bad_key", "FetchLayer rejected the API key. Check SPARTANFINDS_FETCHLAYER_KEY in setenv.",
            "out_of_credits", "The FetchLayer account is out of credits.",
            "unreachable", "Could not reach Reddit. Check the server's internet connection.",
            "bad_response", "Reddit sent a response we could not read.",
            "no_categories", "There are no categories yet. Run Backend/db/seed.sql first.",
            "database", "The posts could not be saved. Check the server log.");

    /** What one Fetch did. errorKey is null when the fetch itself worked. */
    public record ScrapeResult(int fetched, int added, int duplicates, int failed, String errorKey) {
        static ScrapeResult error(String key) {
            return new ScrapeResult(0, 0, 0, 0, key);
        }
    }

    private final RedditItemDao redditItemDao;
    private final ItemDao itemDao;
    private final UserDao userDao;
    private final HttpClient http;

    public RedditScrapeService(RedditItemDao redditItemDao, ItemDao itemDao, UserDao userDao) {
        this.redditItemDao = redditItemDao;
        this.itemDao = itemDao;
        this.userDao = userDao;
        // Redirects are not followed, so an image download can never leave the allowed Reddit hosts.
        this.http = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /** Downloads the listing and imports every post that was not imported before. */
    public ScrapeResult scrape() {
        List<Category> categories;
        int botUserId;
        try {
            categories = itemDao.findAllCategories();
            botUserId = userDao.ensureRedditBotUser();
        } catch (SQLException | DataAccessException | IllegalStateException e) {
            LOG.log(Level.WARNING, "Reddit import: database not ready", e);
            return ScrapeResult.error("database");
        }
        if (categories.isEmpty()) {
            return ScrapeResult.error("no_categories");
        }

        List<RedditPost> posts;
        try {
            posts = fetchPosts();
        } catch (FetchFailed e) {
            return ScrapeResult.error(e.key);
        }

        int added = 0;
        int duplicates = 0;
        int failed = 0;
        for (RedditPost post : posts) {
            try {
                if (redditItemDao.countByPostUrl(post.postUrl()) > 0) {
                    duplicates++;
                    continue;
                }
                importPost(post, botUserId, categoryId(categories, post.categoryName()));
                added++;
            } catch (DataAccessException e) {
                LOG.log(Level.WARNING, "Reddit import: could not save " + post.postUrl(), e);
                failed++;
            }
        }
        return new ScrapeResult(posts.size(), added, duplicates, failed, null);
    }

    /** A listing could not be read; key is one of ERROR_MESSAGES. */
    private static final class FetchFailed extends Exception {
        final String key;

        FetchFailed(String key) {
            super(key, null, false, false);
            this.key = key;
        }
    }

    /** One search of r/SJSU, through FetchLayer when a key is set (1 credit), else Reddit's public JSON. */
    private List<RedditPost> fetchPosts() throws FetchFailed {
        String key = fetchLayerKey();
        try {
            HttpResponse<String> response;
            if (key != null) {
                String body = new JSONObject()
                        .put("query", "lost OR found OR missing")
                        .put("subreddit", "SJSU")
                        .put("sort", "new")
                        .put("time", "month")
                        .put("limit", 25)
                        .put("include", new JSONArray().put("body").put("media"))
                        .toString();
                response = http.send(HttpRequest.newBuilder(URI.create(FETCHLAYER_URL))
                                .timeout(Duration.ofSeconds(60))
                                .header("Authorization", "Bearer " + key)
                                .header("Content-Type", "application/json")
                                .header("User-Agent", USER_AGENT)
                                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                                .build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            } else {
                response = http.send(
                        request(SEARCH_URL).header("Accept", "application/json").build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            }

            int status = response.statusCode();
            if (status != 200) {
                LOG.warning("Reddit import: " + (key != null ? "FetchLayer" : "Reddit") + " returned HTTP " + status);
                if (status == 429) {
                    throw new FetchFailed("rate_limited");
                }
                if (key != null && status == 401) {
                    throw new FetchFailed("bad_key");
                }
                if (key != null && status == 402) {
                    throw new FetchFailed("out_of_credits");
                }
                throw new FetchFailed(key != null && status >= 500 ? "unreachable" : "blocked");
            }
            return key != null
                    ? RedditPostMapper.parseFetchLayer(response.body())
                    : RedditPostMapper.parseListing(response.body());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Reddit import: listing request failed", e);
            throw new FetchFailed("unreachable");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FetchFailed("unreachable");
        } catch (JSONException e) {
            LOG.log(Level.WARNING, "Reddit import: listing was not valid JSON", e);
            throw new FetchFailed("bad_response");
        }
    }

    /** The FetchLayer key from the environment or a JVM system property, or null when not set. */
    static String fetchLayerKey() {
        String value = System.getenv(FETCHLAYER_KEY_SETTING);
        if (value == null || value.isBlank()) {
            value = System.getProperty(FETCHLAYER_KEY_SETTING);
        }
        return value == null || value.isBlank() ? null : value.strip();
    }

    /** Pending posts first in the dashboard queue. */
    public List<PendingRedditItem> getPending() {
        return redditItemDao.findByStatus(RedditItemDao.PENDING, MAX_QUEUE_ROWS);
    }

    public Map<String, Integer> getCounts() {
        return redditItemDao.countByStatus();
    }

    /** Approves (true) or rejects (false) a pending import. False when it was not pending. */
    public boolean decide(int itemId, boolean approve) {
        return redditItemDao.setApproval(itemId, approve ? RedditItemDao.APPROVED : RedditItemDao.REJECTED);
    }

    /** The original Reddit link of an imported post, or empty for posts made on SpartanFinds. */
    public Optional<String> findPostUrl(int itemId) {
        return redditItemDao.findPostUrl(itemId);
    }

    private void importPost(RedditPost post, int botUserId, int categoryId) {
        List<String> savedUrls = new ArrayList<>();
        for (String imageUrl : post.imageUrls()) {
            String saved = downloadImage(imageUrl);
            if (saved != null) {
                savedUrls.add(saved);
            }
        }
        try {
            redditItemDao.createPendingItem(botUserId, post, categoryId, savedUrls);
        } catch (RuntimeException e) {
            savedUrls.forEach(ImageStore::deleteQuietly);
            throw e;
        }
    }

    /**
     * Saves one Reddit image to the upload folder like a normal upload, or returns null when it cannot be
     * used (wrong host, too big, not a JPEG/PNG, or any network error). A missing image never stops the import.
     */
    private String downloadImage(String imageUrl) {
        if (!RedditPostMapper.isAllowedImageUrl(imageUrl)) {
            return null;
        }
        try {
            HttpResponse<InputStream> response = http.send(
                    request(imageUrl).build(), HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    return null;
                }
                byte[] data = body.readNBytes(PostService.MAX_IMAGE_BYTES + 1);
                if (data.length > PostService.MAX_IMAGE_BYTES) {
                    return null;
                }
                String extension = ImageSniffer.extensionFor(data);
                return extension == null ? null : ImageStore.save(data, extension);
            }
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.FINE, "Reddit import: skipped image " + imageUrl, e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static HttpRequest.Builder request(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .GET();
    }

    /** The id of the named category, else "Other", else the first category. */
    static int categoryId(List<Category> categories, String name) {
        Category other = null;
        for (Category category : categories) {
            if (category.getCategoryName().equals(name)) {
                return category.getCategoryId();
            }
            if (RedditPostMapper.OTHER_CATEGORY.equals(category.getCategoryName())) {
                other = category;
            }
        }
        return other != null ? other.getCategoryId() : categories.get(0).getCategoryId();
    }
}
