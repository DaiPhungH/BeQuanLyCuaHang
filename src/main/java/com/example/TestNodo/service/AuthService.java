package com.example.TestNodo.service;

import com.example.TestNodo.dto.RegisterRequest;
import com.example.TestNodo.entity.User;
import com.example.TestNodo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // ❌ Không mã hóa
        user.setRole(request.getRole().toUpperCase());

        userRepository.save(user);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        userRepository.delete(user);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUser(String username, RegisterRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(request.getRole().toUpperCase());
        }

        userRepository.save(user);
    }
}
