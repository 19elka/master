package task1.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Roles {
    USER,
    ADMIN;

    @JsonValue
    public String toValue() {
        return name();
    }
}
