package com.fixflow.service;

import com.fixflow.dao.UserDAO;
import com.fixflow.dto.AuthResponse;
import com.fixflow.dto.LoginRequest;
import com.fixflow.dto.RegisterRequest;
import com.fixflow.dto.UserResponse;
import com.fixflow.exception.ConflictException;
import com.fixflow.exception.UnauthorizedException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Role;
import com.fixflow.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class AuthService {
    
    private final UserDAO userDAO = new UserDAO();
    private static final String SECRET_STRING = System.getenv("JWT_SECRET");
    private static final SecretKey SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    static {
        if (SECRET_STRING != null && !SECRET_STRING.isEmpty()) {
            SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
        } else {
            SECRET_KEY = Keys.hmacShaKeyFor("a-very-long-fallback-secret-key-that-should-never-be-used-in-production".getBytes(StandardCharsets.UTF_8));
        }
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        
        if (userDAO.findByEmail(request.getEmail()) != null) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setPhone(request.getPhone());
        user.setRole(Role.USER); // Always USER for public registration

        User created = userDAO.create(user);
        return new AuthResponse("Registration successful", null, mapToResponse(created));
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new UnauthorizedException("Email and password are required");
        }

        User user = userDAO.findByEmail(request.getEmail());
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();

        return new AuthResponse("Login successful", token, mapToResponse(user));
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        return response;
    }
}
