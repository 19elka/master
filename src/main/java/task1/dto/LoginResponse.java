package task1.dto;

public record LoginResponse(
        String token,
        String type,
        String username,
        String roles
) {
    public static LoginResponse of(String token, String username, String roles) {
        return new LoginResponse(token, "Bearer", username, roles);
    }
}
