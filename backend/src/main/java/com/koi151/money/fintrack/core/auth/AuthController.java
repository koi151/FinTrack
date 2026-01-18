package com.koi151.money.fintrack.core.auth;

import com.koi151.money.fintrack.common.AppResponse;
import com.koi151.money.fintrack.core.user.UserService;
import com.koi151.money.fintrack.core.user.payload.UserRegisterRequest;
import com.koi151.money.fintrack.core.user.payload.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user registration and authentication")
public class AuthController {

    private final UserService userService;

    @Operation(
        summary = "Register new user",
        description = "Create a new local user account. Requires a unique username and email."
    )
    @ApiResponse(
        responseCode = "201",
        description = "User registered successfully",
        content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input parameters (e.g., weak password, invalid email format)",
        content = @Content
    )
    @ApiResponse(
        responseCode = "409",
        description = "Conflict: Email or Username already exists",
        content = @Content
    )
    @PostMapping("/register")
    public ResponseEntity<AppResponse<UserResponse>> register(
        @Valid @RequestBody UserRegisterRequest request
    ) {
        UserResponse newUser = userService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED) // 201 Created
            .body(AppResponse.success(newUser, "User registered successfully"));
    }
}