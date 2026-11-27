package service;

import dto.RedditPost;
import model.Category;

import java.util.List;

/** Run main after mvn test-compile; no database, network or test framework required. */
public class RedditPostMapperTest {
    private static int checks;

    private static final String LISTING = """
            {"kind": "Listing", "data": {"children": [
              {"kind": "t3", "data": {
                "title": "Found AirPods case near MLK library", "selftext": "Black case &amp; one pod. DM me.",
                "author": "spartan123", "permalink": "/r/SJSU/comments/abc/found_airpods/",
                "created_utc": 1732400000, "url": "https://i.redd.it/photo1.jpg",
                "stickied": false, "over_18": false, "removed_by_category": null}},
              {"kind": "t3", "data": {
                "title": "Lost my blue Hydro Flask in ENG 189", "selftext": "",
                "author": "[deleted]", "permalink": "/r/SJSU/comments/def/lost_hydro/",
                "created_utc": 1732300000, "url": "https://www.reddit.com/gallery/def", "is_gallery": true,
                "gallery_data": {"items": [{"media_id": "m2"}, {"media_id": "m1"}]},
                "media_metadata": {
                  "m1": {"s": {"u": "https://preview.redd.it/one.jpg?width=640&amp;s=abc"}},
                  "m2": {"s": {"u": "https://preview.redd.it/two.jpg?width=640&amp;s=def"}}}}},
              {"kind": "t3", "data": {
                "title": "Weekly megathread: lost and found", "selftext": "Post here",
                "permalink": "/r/SJSU/comments/ghi/megathread/", "stickied": true}},
              {"kind": "t3", "data": {
                "title": "Lost wallet", "selftext": "[removed]",
                "permalink": "/r/SJSU/comments/jkl/lost_wallet/", "removed_by_category": "moderator"}},
              {"kind": "t3", "data": {
                "title": "Best tacos near campus?", "selftext": "Looking for recommendations",
                "permalink": "/r/SJSU/comments/mno/tacos/"}},
              {"kind": "t3", "data": {
                "title": "Missing keys", "selftext": "Silver keys on a lanyard",
                "permalink": "/r/SJSU/comments/pqr/keys/", "url": "https://evil.example.com/x.jpg",
                "preview": {"images": [{"source": {"url": "http://preview.redd.it/insecure.jpg"}}]}}}
            ]}}
            """;


    /** Shaped like a real FetchLayer /reddit/search response (names and text made up). */
    private static final String FETCHLAYER = """
            {"listingType": "search", "itemCount": 6, "nextCursor": null, "transport": "reddit", "items": [
              {"id": "a1", "author": "example_user", "title": "Lost Student ID",
               "permalink": "https://www.reddit.com/r/SJSU/comments/a1/lost_student_id/",
               "createdAt": "2026-11-19T18:00:00.000Z", "bodyText": "Left it in the SU. Tower card, name on it.",
               "removed": false, "removedByCategory": null, "nsfw": false, "stickied": false,
               "media": [
                 {"kind": "image", "url": "https://i.redd.it/card.jpg"},
                 {"kind": "image", "url": "https://i.redd.it/card.jpg"},
                 {"kind": "video", "url": "https://v.redd.it/clip"},
                 {"kind": "link_preview", "url": "https://evil.example.com/x.png"}]},
              {"id": "a2", "author": "someone", "title": "Missed a quiz",
               "permalink": "https://www.reddit.com/r/SJSU/comments/a2/missed_a_quiz/",
               "createdAt": "2026-11-26T01:28:27.176Z", "bodyText": "Do teachers allow a retake?",
               "removed": false, "removedByCategory": null, "nsfw": false, "stickied": false, "media": null},
              {"id": "a3", "title": "Found keys at the rec center", "author": "x",
               "permalink": "https://www.reddit.com/r/SJSU/comments/a3/keys/", "createdAt": "2026-11-20T00:00:00Z",
               "bodyText": null, "stickied": true, "nsfw": false, "removed": false, "removedByCategory": null},
              {"id": "a4", "title": "Found wallet", "author": "x",
               "permalink": "https://www.reddit.com/r/SJSU/comments/a4/wallet/", "createdAt": "2026-11-20T00:00:00Z",
               "nsfw": false, "stickied": false, "removed": null, "removedByCategory": "moderator"},
              {"id": "a5", "title": "Lost hoodie", "author": "x",
               "permalink": "https://evil.example.com/r/SJSU/comments/a5/", "createdAt": "2026-11-20T00:00:00Z",
               "nsfw": false, "stickied": false, "removed": false, "removedByCategory": null},
              {"id": "a6", "title": "Found a black umbrella", "author": "",
               "permalink": "https://www.reddit.com/r/SJSU/comments/a6/umbrella/", "createdAt": "not a date",
               "nsfw": false, "stickied": false, "removed": false, "removedByCategory": null}
            ]}
            """;

    public static void main(String[] args) {
        List<RedditPost> posts = RedditPostMapper.parseListing(LISTING);
        check(posts.size() == 3, "stickied, removed and unrelated posts are skipped");

        RedditPost airpods = posts.get(0);
        check(airpods.postUrl().equals("https://www.reddit.com/r/SJSU/comments/abc/found_airpods/"), "post URL");
        check(airpods.type().equals("FOUND"), "found in title means FOUND");
        check(airpods.categoryName().equals("Electronics"), "AirPods are electronics");
        check(airpods.description().startsWith("Black case & one pod."), "HTML entities unescaped");
        check(airpods.description().endsWith("Posted by u/spartan123 on r/SJSU."), "author footer");
        check(airpods.imageUrls().equals(List.of("https://i.redd.it/photo1.jpg")), "direct i.redd.it image");
        check(airpods.createdAt() != null, "created time read");

        RedditPost flask = posts.get(1);
        check(flask.type().equals("LOST"), "lost post is LOST");
        check(flask.categoryName().equals("Water Bottles & Drinkware"), "Hydro Flask category");
        check(flask.description().startsWith("See the original thread"), "empty body placeholder");
        check(flask.description().endsWith("Posted by a Reddit user on r/SJSU."), "deleted author");
        check(flask.imageUrls().equals(List.of(
                "https://preview.redd.it/two.jpg?width=640&s=def",
                "https://preview.redd.it/one.jpg?width=640&s=abc")), "gallery order and &amp; unescaped");

        RedditPost keys = posts.get(2);
        check(keys.categoryName().equals("Keys"), "keys category");
        check(keys.imageUrls().isEmpty(), "other hosts and plain http are never downloaded");

        check(RedditPostMapper.isAllowedImageUrl("https://i.redd.it/a.png"), "allowed host");
        for (String bad : new String[] {"http://i.redd.it/a.png", "https://i.redd.it.evil.com/a.png",
                "https://user@i.redd.it/a.png", "https://i.redd.it:8443/a.png", "file:///etc/passwd", "not a url"}) {
            check(!RedditPostMapper.isAllowedImageUrl(bad), "rejected image URL " + bad);
        }

        check(RedditPostMapper.categoryFor("found a student ID card").equals("ID & Cards"), "ID card");
        check(RedditPostMapper.categoryFor("lost wallet with my ID").equals("Wallets & Money"), "wallet first");
        check(RedditPostMapper.categoryFor("lost something").equals("Other"), "fallback category");

        String longBody = "x".repeat(6000);
        String description = RedditPostMapper.buildDescription(longBody, "a");
        check(description.length() <= PostService.MAX_DESCRIPTION_LENGTH, "description fits the column");
        check(description.endsWith("Posted by u/a on r/SJSU."), "footer kept when truncated");
        check(RedditPostMapper.truncate("y".repeat(300), PostService.MAX_TITLE_LENGTH).length()
                == PostService.MAX_TITLE_LENGTH, "title fits the column");

        List<Category> categories = List.of(new Category(1, "Keys"), new Category(9, "Other"));
        check(RedditScrapeService.categoryId(categories, "Keys") == 1, "category id by name");
        check(RedditScrapeService.categoryId(categories, "Electronics") == 9, "missing category uses Other");


        List<RedditPost> fl = RedditPostMapper.parseFetchLayer(FETCHLAYER);
        check(fl.size() == 2, "FetchLayer: unrelated, sticky, removed and non-Reddit permalinks skipped");
        RedditPost id = fl.get(0);
        check(id.postUrl().equals("https://www.reddit.com/r/SJSU/comments/a1/lost_student_id/"), "FetchLayer post URL");
        check(id.type().equals("LOST") && id.categoryName().equals("ID & Cards"), "FetchLayer type and category");
        check(id.description().startsWith("Left it in the SU.")
                && id.description().endsWith("Posted by u/example_user on r/SJSU."), "FetchLayer description");
        check(id.imageUrls().equals(List.of("https://i.redd.it/card.jpg")), "FetchLayer images: deduped, video and other hosts dropped");
        check(id.createdAt().getYear() == 2026, "FetchLayer createdAt parsed");
        RedditPost umbrella = fl.get(1);
        check(umbrella.type().equals("FOUND") && umbrella.categoryName().equals("Other"), "FetchLayer FOUND/Other");
        check(umbrella.description().startsWith("See the original thread")
                && umbrella.description().endsWith("Posted by a Reddit user on r/SJSU."), "FetchLayer empty body and author");
        check(umbrella.createdAt() != null, "bad date falls back to now");

        System.out.println("RedditPostMapperTest: " + checks + " checks passed");
    }

    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) {
            throw new AssertionError("Failed: " + name);
        }
    }
}
