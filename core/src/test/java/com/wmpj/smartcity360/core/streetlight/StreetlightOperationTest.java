package com.wmpj.smartcity360.core.streetlight;

import com.wmpj.smartcity360.core.entity.StreetlightOperationalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreetlightOperationTest {

    @Test
    void validatesSupportedBrightnessAndStatusChanges() {
        StreetlightStatusChange valid = new StreetlightStatusChange(
                true,
                (short) 75,
                StreetlightOperationalStatus.ACTIVE
        );

        assertEquals(75, valid.brightness());
        assertThrows(
                IllegalArgumentException.class,
                () -> new StreetlightStatusChange(
                        true,
                        (short) 101,
                        StreetlightOperationalStatus.ACTIVE
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> new StreetlightStatusChange(true, (short) 50, null)
        );
    }
}
