package task1.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String name,
        String roles
) {
}
