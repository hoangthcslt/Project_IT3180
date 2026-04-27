package com.bluemoon.services;

import com.bluemoon.models.User;
import com.bluemoon.repositories.UserRepository;
import com.bluemoon.utils.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public boolean authenticate(String username, String plainPassword) {
        User user = userRepository.getUserByUsername(username);
        
        if (user != null) {
            boolean isPasswordMatch = false;
            try {
                // Kiểm tra bằng jBCrypt
                isPasswordMatch = BCrypt.checkpw(plainPassword, user.getPassword());
            } catch (IllegalArgumentException e) {
                // Hỗ trợ trường hợp dev test với dữ liệu mẫu (mật khẩu chưa hash: '123456')
                if (plainPassword.equals(user.getPassword())) {
                    isPasswordMatch = true;
                }
            }
            
            if (isPasswordMatch) {
                SessionManager.getInstance().setCurrentUser(user);
                return true;
            }
        }
        return false;
    }
}
