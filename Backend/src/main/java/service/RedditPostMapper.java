package service;

import dto.RedditPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns Reddit's JSON listing into item post fields. Pure logic with no network or database, so
 * RedditPostMapperTest can check it with a fixed JSON string.
 */
public final class RedditPostMapper {
    public static final String SUBREDDIT_LABEL = "r/SJSU";
    static final String REDDIT_ORIGIN = "https://www.reddit.com";
    static final String OTHER_CATEGORY = "Other";

    /** Only these hosts are downloaded from, so a post can never make the server fetch an arbitrary URL. */
    static final Set<String> ALLOWED_IMAGE_HOSTS = Set.of("i.redd.it", "preview.redd.it");

    private static final Pattern LOST_ITEM_WORDS =
            Pattern.compile("\\b(lost|found|missing|left|dropped|misplaced)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FOUND_WORD = Pattern.compile("\\bfound\\b", Pattern.CASE_INSENSITIVE);

    /** Keyword pattern → category name from seed.sql. The first match wins, so more specific groups come first. */
    private static final Map<Pattern, String> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        category("airpods?|iphone|phone|laptop|macbook|charger|ipad|tablet|headphones?|earbuds?|apple watch|calculator",
                "Electronics");
        category("wallets?|cash|money|purse", "Wallets & Money");
        category("id|ids|student id|tower card|cards?|license|licence", "ID & Cards");
        category("keys?|keychain|key ring|lanyard", "Keys");
        category("backpacks?|bags?|tote|duffel|pouch", "Backpacks & Bags");
        category("bottles?|hydro ?flask|tumbler|thermos|cup|mug|stanley", "Water Bottles & Drinkware");
        category("jackets?|hoodie|sweater|sweatshirt|coat|hat|beanie|shoes?|scarf|gloves?", "Clothing");
        category("books?|notebooks?|textbooks?|binder|planner", "Books & Notebooks");
        category("rings?|necklace|bracelet|watch|earrings?|glasses|sunglasses|jewelry", "Jewelry & Accessories");
    }

    private static void category(String words, String name) {
        CATEGORY_KEYWORDS.put(Pattern.compile("\\b(" + words + ")\\b", Pattern.CASE_INSENSITIVE), name);
    }

    private RedditPostMapper() {
    }

    /**
     * Reads a Reddit listing (the response of /r/SJSU/search.json) and returns the posts that look like
     * lost-and-found posts. Stickied, NSFW, removed and deleted posts are left out.
     *
     * @throws JSONException if the text is not a Reddit listing
     */
    public static List<RedditPost> parseListing(String json) {
        JSONArray children = new JSONObject(json).getJSONObject("data").getJSONArray("children");
        List<RedditPost> posts = new ArrayList<>();
        for (int i = 0; i < children.length(); i++) {
            JSONObject data = children.getJSONObject(i).optJSONObject("data");
            if (data == null) {
                continue;
            }
            RedditPost post = toPost(data);
            if (post != null) {
                posts.add(post);
            }
        }
        return posts;
    }

    /** A post from Reddit's own JSON as item fields, or null when it should not be imported. */
    static RedditPost toPost(JSONObject data) {
        // isNull is true when the key is missing or JSON null, so this skips posts a moderator removed.
        if (data.optBoolean("stickied") || data.optBoolean("over_18") || !data.isNull("removed_by_category")) {
            return null;
        }
        String permalink = data.optString("permalink", "");
        if (!permalink.startsWith("/")) {
            return null;
        }
        long createdUtc = data.optLong("created_utc", 0L);
        LocalDateTime createdAt = createdUtc > 0
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(createdUtc), ZoneId.systemDefault())
                : LocalDateTime.now();
        return build(
                unescapeHtml(data.optString("title", "")),
                unescapeHtml(data.optString("selftext", "")),
                data.optString("author", ""),
                REDDIT_ORIGIN + permalink,
                createdAt,
                imageUrls(data));
    }

    /**
     * Reads a FetchLayer search response (POST https://api.fetchlayer.dev/reddit/search) and returns the
     * posts that look like lost-and-found posts, with the same rules as parseListing.
     *
     * @throws JSONException if the text is not a FetchLayer listing
     */
    public static List<RedditPost> parseFetchLayer(String json) {
        JSONArray items = new JSONObject(json).getJSONArray("items");
        List<RedditPost> posts = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            RedditPost post = item == null ? null : fromFetchLayer(item);
            if (post != null) {
                posts.add(post);
            }
        }
        return posts;
    }

    /** One FetchLayer item as item fields, or null when it should not be imported. */
    static RedditPost fromFetchLayer(JSONObject item) {
        if (item.optBoolean("stickied") || item.optBoolean("nsfw") || item.optBoolean("removed")
                || !item.isNull("removedByCategory")) {
            return null;
        }
        // FetchLayer gives an absolute permalink. Keep only Reddit threads, stored in the same form as
        // parseListing produces, so a post is recognised as a duplicate whichever source imported it.
        String permalink = item.optString("permalink", "");
        if (!permalink.startsWith(REDDIT_ORIGIN + "/r/")) {
            return null;
        }
        LocalDateTime createdAt;
        try {
            createdAt = LocalDateTime.ofInstant(Instant.parse(item.optString("createdAt", "")), ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            createdAt = LocalDateTime.now();
        }

        List<String> images = new ArrayList<>();
        JSONArray media = item.optJSONArray("media");
        if (media != null) {
            for (int i = 0; i < media.length(); i++) {
                JSONObject entry = media.optJSONObject(i);
                if (entry == null) {
                    continue;
                }
                String kind = entry.optString("kind", "");
                if ("image".equals(kind) || "gallery_item".equals(kind) || "link_preview".equals(kind)) {
                    images.add(unescapeHtml(entry.optString("url", "")));
                }
            }
        }

        return build(
                item.optString("title", ""),
                item.optString("bodyText", ""),
                item.optString("author", ""),
                permalink,
                createdAt,
                images);
    }

    /**
     * The rules both sources share: skip removed or unrelated posts, decide LOST/FOUND and the category, fit
     * title and description to the Item columns, and keep only allowed image links.
     */
    private static RedditPost build(String rawTitle, String rawBody, String author, String postUrl,
                                    LocalDateTime createdAt, List<String> imageCandidates) {
        String title = rawTitle.strip();
        String body = rawBody.strip();
        if (title.isEmpty() || "[removed]".equals(body) || "[deleted]".equals(body)) {
            return null;
        }
        if (!LOST_ITEM_WORDS.matcher(title + "\n" + body).find()) {
            return null;
        }
        List<String> images = new ArrayList<>();
        for (String candidate : imageCandidates) {
            if (images.size() == PostService.MAX_IMAGES) {
                break;
            }
            if (isAllowedImageUrl(candidate) && !images.contains(candidate)) {
                images.add(candidate);
            }
        }
        return new RedditPost(
                postUrl,
                truncate(title, PostService.MAX_TITLE_LENGTH),
                buildDescription(body, author),
                FOUND_WORD.matcher(title).find() ? "FOUND" : "LOST",
                categoryFor(title + "\n" + body),
                createdAt,
                images);
    }

    /** The category name whose keywords appear first in the list, or "Other". */
    static String categoryFor(String text) {
        for (Map.Entry<Pattern, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (entry.getKey().matcher(text).find()) {
                return entry.getValue();
            }
        }
        return OTHER_CATEGORY;
    }

    /** The post body followed by who posted it, cut to fit Item.description (5000 characters). */
    static String buildDescription(String selftext, String author) {
        String body = selftext.isEmpty() ? "See the original thread for details." : selftext;
        String footer = "\n\nPosted by "
                + (author.isEmpty() || "[deleted]".equals(author) ? "a Reddit user" : "u/" + author)
                + " on " + SUBREDDIT_LABEL + ".";
        int room = PostService.MAX_DESCRIPTION_LENGTH - footer.length();
        if (body.length() > room) {
            body = body.substring(0, room - 1).stripTrailing() + "…";
        }
        return body + footer;
    }

    /**
     * Image links in display order: the post's own image, else its gallery, else Reddit's preview image.
     * Only https links on the allowed hosts are kept, and at most PostService.MAX_IMAGES.
     */
    static List<String> imageUrls(JSONObject data) {
        List<String> candidates = new ArrayList<>();
        String url = unescapeHtml(data.optString("url", ""));
        if (isAllowedImageUrl(url) && hostOf(url).equals("i.redd.it")) {
            candidates.add(url);
        } else if (data.optBoolean("is_gallery") && data.optJSONObject("media_metadata") != null) {
            JSONObject media = data.getJSONObject("media_metadata");
            List<String> ids = new ArrayList<>();
            JSONObject gallery = data.optJSONObject("gallery_data");
            JSONArray items = gallery == null ? null : gallery.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    ids.add(items.getJSONObject(i).optString("media_id", ""));
                }
            } else {
                ids.addAll(media.keySet());
            }
            for (String id : ids) {
                JSONObject entry = media.optJSONObject(id);
                JSONObject source = entry == null ? null : entry.optJSONObject("s");
                if (source != null) {
                    candidates.add(unescapeHtml(source.optString("u", "")));
                }
            }
        } else {
            JSONObject preview = data.optJSONObject("preview");
            JSONArray images = preview == null ? null : preview.optJSONArray("images");
            if (images != null && !images.isEmpty()) {
                JSONObject source = images.getJSONObject(0).optJSONObject("source");
                if (source != null) {
                    candidates.add(unescapeHtml(source.optString("url", "")));
                }
            }
        }

        List<String> allowed = new ArrayList<>();
        for (String candidate : candidates) {
            if (allowed.size() == PostService.MAX_IMAGES) {
                break;
            }
            if (isAllowedImageUrl(candidate)) {
                allowed.add(candidate);
            }
        }
        return allowed;
    }

    /** True for an https link whose host is i.redd.it or preview.redd.it (no other port, no user info). */
    static boolean isAllowedImageUrl(String url) {
        try {
            URI uri = new URI(url);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getUserInfo() == null
                    && uri.getPort() == -1
                    && uri.getHost() != null
                    && ALLOWED_IMAGE_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static String hostOf(String url) {
        try {
            String host = new URI(url).getHost();
            return host == null ? "" : host.toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            return "";
        }
    }

    /** Reddit escapes &, < and > in its JSON strings. */
    static String unescapeHtml(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1).stripTrailing() + "…";
    }
}
