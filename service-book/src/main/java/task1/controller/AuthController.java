package task1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task1.dto.auth.LoginRequest;
import task1.dto.auth.LoginResponse;
import task1.dto.auth.UserCreateDto;
import task1.dto.auth.UserResponseDto;
import task1.security.AuthService;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Profile("book-service")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserCreateDto createDto
    ) {
        log.info("Registering new user: {}", createDto.username());
        UserResponseDto response = authService.register(createDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        log.info("Login request from user: {}", loginRequest.username());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
