package dto;

import java.util.List;

/** A validated new post, ready to save. Location is required; its name is optional (null when blank). */
public record NewPost(int userId, String type, String title, String description, int categoryId,
                      String locationName, double latitude, double longitude, List<String> imageUrls) {
}
