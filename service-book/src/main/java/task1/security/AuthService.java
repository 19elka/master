package task1.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task1.dto.auth.LoginRequest;
import task1.dto.auth.LoginResponse;
import task1.dto.auth.UserCreateDto;
import task1.dto.auth.UserResponseDto;
import task1.entity.Roles;
import task1.entity.User;
import task1.exception.ServerErrorException;
import task1.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponseDto register(UserCreateDto dto) {

        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .name(dto.name())
                .roles(List.of(Roles.USER))
                .build();

        userRepository.save(user);

        String roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                roles
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ServerErrorException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getUsername());

        String roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        return new LoginResponse(token, "Bearer", user.getUsername(), roles);
    }
}
