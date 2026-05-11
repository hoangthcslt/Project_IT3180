package com.bluemoon.utils;
import com.bluemoon.models.User; // Class User team sẽ tự tạo sau

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser() { return currentUser; }
    public void clearSession() { this.currentUser = null; }
}