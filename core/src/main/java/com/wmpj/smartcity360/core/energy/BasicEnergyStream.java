package com.wmpj.smartcity360.core.energy;

import java.util.Arrays;
import java.util.Objects;

public final class BasicEnergyStream implements EnergyStreamComponent {

    public static final int MAX_ENCODED_PAYLOAD_BYTES = 65_536;

    @Override
    public byte[] encode(byte[] payload) {
        return copyBounded(payload);
    }

    @Override
    public byte[] decode(byte[] payload) {
        return copyBounded(payload);
    }

    private static byte[] copyBounded(byte[] payload) {
        Objects.requireNonNull(payload, "payload");

        if (payload.length > MAX_ENCODED_PAYLOAD_BYTES) {
            throw new EnergyStreamProcessingException("Energy payload is too large");
        }

        return Arrays.copyOf(payload, payload.length);
    }
}
