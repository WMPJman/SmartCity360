package com.wmpj.smartcity360.core.dto;

public record PageRequest(int offset, int limit) {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public PageRequest {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be zero or greater");
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException(
                    "limit must be between 1 and " + MAX_LIMIT
            );
        }
    }
}
