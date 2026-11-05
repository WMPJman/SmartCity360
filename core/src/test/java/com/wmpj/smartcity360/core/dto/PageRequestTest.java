package com.wmpj.smartcity360.core.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageRequestTest {

    @Test
    void acceptsBoundedPagesAndRejectsUnsafeBounds() {
        assertDoesNotThrow(() -> new PageRequest(0, PageRequest.DEFAULT_LIMIT));
        assertDoesNotThrow(() -> new PageRequest(10, PageRequest.MAX_LIMIT));
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(-1, 20));
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PageRequest(0, PageRequest.MAX_LIMIT + 1)
        );
    }
}
