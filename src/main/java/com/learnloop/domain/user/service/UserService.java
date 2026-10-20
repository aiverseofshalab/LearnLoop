package com.learnloop.domain.user.service;

import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.domain.user.dto.AdminUserStatusRequest;
import com.learnloop.domain.user.dto.UpdateProfileRequest;
import com.learnloop.domain.user.dto.UserProfileResponse;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.entity.UserStatus;
import com.learnloop.domain.user.repository.UserRepository;
import com.learnloop.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        user = userRepository.save(user);
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getAdminUsers(String query, UserRole role, UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(query, role, status, pageable);
        return users.map(UserProfileResponse::fromEntity);
    }

    @Transactional
    public UserProfileResponse updateUserStatusByAdmin(String userId, AdminUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        user = userRepository.save(user);
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntity(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
