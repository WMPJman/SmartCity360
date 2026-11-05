package com.wmpj.smartcity360.ejb.repository;

final class QueryBounds {
    private static final int MAX_LIMIT = 100;

    private QueryBounds() {
    }

    static void requirePage(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be zero or greater");
        }
        requireLimit(limit);
    }

    static void requireLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    }
}
