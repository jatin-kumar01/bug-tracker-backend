package com.bugtracker.user.service;

import com.bugtracker.exception.DuplicateResourceException;
import com.bugtracker.exception.ForbiddenOperationException;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.user.dto.CreateUserRequest;
import com.bugtracker.user.dto.UpdateUserRequest;
import com.bugtracker.user.dto.UpdateUserRoleRequest;
import com.bugtracker.user.dto.UserResponse;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import com.bugtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(request.getRole())
                .active(true)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findEntityById(id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(id)) {
            throw new ForbiddenOperationException("You can only update your own profile.");
        }
        User user = findEntityById(id);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request) {
        User user = findEntityById(id);
        user.setRole(request.getRole());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(Long id, User currentUser) {
        if (currentUser.getId().equals(id)) {
            throw new ForbiddenOperationException("Admins cannot deactivate their own account.");
        }
        User user = findEntityById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
