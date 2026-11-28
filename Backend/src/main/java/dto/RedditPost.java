package dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One r/SJSU post, already turned into item post fields by RedditPostMapper.
 *
 * @param postUrl     canonical link to the Reddit thread (stored in RedditItem.post_url)
 * @param title       item title, at most 255 characters
 * @param description item description, at most 5000 characters, ending with "Posted by u/... on r/SJSU"
 * @param type        LOST or FOUND
 * @param categoryName guessed category name from seed.sql (falls back to "Other")
 * @param createdAt   when the Reddit post was made (stored in Item.created_at)
 * @param imageUrls   https image links on i.redd.it / preview.redd.it, at most PostService.MAX_IMAGES
 */
public record RedditPost(
        String postUrl,
        String title,
        String description,
        String type,
        String categoryName,
        LocalDateTime createdAt,
        List<String> imageUrls) {

    public RedditPost {
        imageUrls = List.copyOf(imageUrls);
    }
}
