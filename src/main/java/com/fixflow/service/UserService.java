package com.fixflow.service;

import com.fixflow.dao.UserDAO;
import com.fixflow.dto.CreateUserRequest;
import com.fixflow.dto.PatchUserRequest;
import com.fixflow.dto.UpdateUserRequest;
import com.fixflow.dto.UserResponse;
import com.fixflow.exception.ConflictException;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<UserResponse> getAllUsers() {
        return userDAO.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        return mapToResponse(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        validateCreateUser(request);
        if (userDAO.findByEmail(request.getEmail()) != null) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        return mapToResponse(userDAO.create(user));
    }

    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        User existing = userDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        validateUpdateUser(request);

        User emailCheck = userDAO.findByEmail(request.getEmail());
        if (emailCheck != null && !emailCheck.getId().equals(id)) {
            throw new ConflictException("Email is already in use");
        }

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setRole(request.getRole());
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existing.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        }

        userDAO.update(existing);
        return mapToResponse(userDAO.findById(id));
    }

    public UserResponse patchUser(Integer id, PatchUserRequest request) {
        User existing = userDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        if (request.getEmail() != null) {
            User emailCheck = userDAO.findByEmail(request.getEmail());
            if (emailCheck != null && !emailCheck.getId().equals(id)) {
                throw new ConflictException("Email is already in use");
            }
            existing.setEmail(request.getEmail());
        }

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getPhone() != null) existing.setPhone(request.getPhone());
        if (request.getRole() != null) existing.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existing.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        }

        userDAO.update(existing);
        return mapToResponse(userDAO.findById(id));
    }

    public void deleteUser(Integer id) {
        if (userDAO.findById(id) == null) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userDAO.delete(id);
    }

    private void validateCreateUser(CreateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) throw new ValidationException("Name is required");
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) throw new ValidationException("Email is required");
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) throw new ValidationException("Password is required");
        if (request.getRole() == null) throw new ValidationException("Role is required");
    }

    private void validateUpdateUser(UpdateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) throw new ValidationException("Name is required");
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) throw new ValidationException("Email is required");
        if (request.getRole() == null) throw new ValidationException("Role is required");
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setRole(user.getRole());
        res.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        res.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        return res;
    }
}
