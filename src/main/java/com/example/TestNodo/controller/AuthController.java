package com.example.TestNodo.controller;

import com.example.TestNodo.dto.LoginRequest;
import com.example.TestNodo.dto.RegisterRequest;
import com.example.TestNodo.entity.User;
import com.example.TestNodo.repository.UserRepository;
import com.example.TestNodo.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        return userRepository.findByUsernameAndPassword(request.getUsername(), request.getPassword())
                .map(user -> {
                    session.setAttribute("user", user);
                    response.put("message", "Login successful");
                    response.put("role", user.getRole());
                    return response;
                })
                .orElseGet(() -> {
                    response.put("message", "Invalid credentials");
                    return response;
                });
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out");
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            response.put("loggedIn", false);
        } else {
            response.put("loggedIn", true);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
        }
        return response;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Tạo tài khoản thành công"));
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        authService.deleteUser(username);
        return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công"));
    }

    @GetMapping("/users")
    public ResponseEntity getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/update/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody RegisterRequest request) {
        authService.updateUser(username, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật người dùng thành công"));
    }
}
