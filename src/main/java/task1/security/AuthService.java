package task1.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task1.dto.LoginRequest;
import task1.dto.LoginResponse;
import task1.dto.UserCreateDto;
import task1.dto.UserResponseDto;
import task1.entity.Role;
import task1.entity.User;
import task1.repository.UserRepository;

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

        User user = User.builder() //билдер
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .name(dto.name())
                .roles(Role.USER) //энам
                .build();

        userRepository.save(user);

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRoles().name()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new LoginResponse(token, "Bearer", user.getUsername(), user.getRoles().name());
    }
}
