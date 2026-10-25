package dto;

import java.util.Map;

/** Outcome of submitting a post: the new item's id, or field-name -> message errors. */
public record PostResult(Integer itemId, Map<String, String> errors) {
    public static PostResult success(int itemId) {
        return new PostResult(itemId, Map.of());
    }

    public static PostResult failure(Map<String, String> errors) {
        return new PostResult(null, errors);
    }

    public boolean isSuccess() {
        return itemId != null;
    }
}
