package com.wmpj.smartcity360.web.dto;

import com.wmpj.smartcity360.core.energy.EnergyStreamLayer;

import java.util.List;

public record EnergyProcessResponse(
        String payloadBase64,
        int payloadLength,
        List<EnergyStreamLayer> effectiveOrder
) {
}
