package com.wmpj.smartcity360.core.dto;

import java.util.List;
import java.util.Objects;

public record PageResponse<T>(List<T> items, int offset, int limit) {

    public PageResponse {
        Objects.requireNonNull(items, "items");
        items = List.copyOf(items);
    }
}
