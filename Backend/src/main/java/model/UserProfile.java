package model;

public class UserProfile {
    private final int userId;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String profileDescription;
    private final String profileImage;
    private final String role;

    public UserProfile(
            int userId,
            String name,
            String email,
            String phoneNumber,
            String profileDescription,
            String profileImage,
            String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileDescription = profileDescription;
        this.profileImage = profileImage;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getRole() {
        return role;
    }

    /** First letter of the name, upper case, for the default avatar; "?" when the name is empty. */
    public String getInitial() {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return new String(Character.toChars(name.strip().codePointAt(0))).toUpperCase();
    }
}