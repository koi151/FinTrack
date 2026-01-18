package com.koi151.money.fintrack.core.user;

import com.koi151.money.fintrack.common.exception.DbExceptionHelper;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.user.domain.User;
import com.koi151.money.fintrack.core.user.domain.UserFactory;
import com.koi151.money.fintrack.core.user.payload.UserRegisterRequest;
import com.koi151.money.fintrack.core.user.payload.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserFactory userFactory;

    /**
     * Config for mapping database constraint violations to error codes.
     */
    private static final Map<String, ErrorCode> CONSTRAINT_ERROR_MAP = Map.of(
        User.UK_USER_EMAIL, ErrorCode.USER_EMAIL_EXISTED,
        User.UK_USER_USERNAME, ErrorCode.USERNAME_EXISTED,
        User.UK_USER_PROVIDER_IDENTITY, ErrorCode.OAUTH_ACCOUNT_ALREADY_LINKED
    );

    /**
     * Register a new user
     * @return UserResponse DTO of the newly registered user
     */
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());

        User newUser = userFactory.createLocalUser(
            request.username(),
            request.email(),
            encodedPassword
        );

        User savedUser = saveUserSafely(newUser);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    /**
     * Safely attempts to save the user to the database.
     * @param user the user entity to save
     * @return the persisted user entity
     */
    private User saveUserSafely(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw DbExceptionHelper.handleDataIntegrityViolation(e, CONSTRAINT_ERROR_MAP);
        }
    }
}