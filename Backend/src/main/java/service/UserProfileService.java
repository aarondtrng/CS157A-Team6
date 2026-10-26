package service;

import dao.UserDao;
import model.UserProfile;

import java.sql.SQLException;
import java.util.Optional;

public class UserProfileService {
    private final UserDao userDao;

    public UserProfileService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<UserProfile> getProfile(int userId) throws SQLException {
        return userDao.findProfileById(userId);
    }

    public boolean updateProfile(
        int userId,
        String name,
        String phoneNumber,
        String profileDescription,
        String profileImage) throws SQLException {

    return userDao.updateProfile(
            userId,
            name,
            phoneNumber,
            profileDescription,
            profileImage);
    }
}