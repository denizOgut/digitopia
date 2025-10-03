package com.digitopia.common.dto.request;

public record SearchUserRequest(
    String normalizedName,
    String email,
    Integer page,
    Integer size
) {
    public SearchUserRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}
