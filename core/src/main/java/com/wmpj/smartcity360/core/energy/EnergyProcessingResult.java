package com.wmpj.smartcity360.core.energy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record EnergyProcessingResult(
        byte[] payload,
        List<EnergyStreamLayer> effectiveOrder
) {

    public EnergyProcessingResult {
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(effectiveOrder, "effectiveOrder");
        payload = Arrays.copyOf(payload, payload.length);
        effectiveOrder = List.copyOf(effectiveOrder);
    }

    @Override
    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }
}
