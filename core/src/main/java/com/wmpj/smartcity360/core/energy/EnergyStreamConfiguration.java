package com.wmpj.smartcity360.core.energy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public record EnergyStreamConfiguration(
        boolean authenticationEnabled,
        boolean encryptionEnabled,
        boolean compressionEnabled,
        List<EnergyStreamLayer> effectiveOrder
) {

    private static final List<EnergyStreamLayer> SECURE_ORDER = List.of(
            EnergyStreamLayer.COMPRESSION,
            EnergyStreamLayer.ENCRYPTION,
            EnergyStreamLayer.AUTHENTICATION
    );

    public EnergyStreamConfiguration {
        effectiveOrder = List.copyOf(effectiveOrder);
        List<EnergyStreamLayer> expectedOrder = enabledLayers(
                authenticationEnabled,
                encryptionEnabled,
                compressionEnabled
        );

        if (!effectiveOrder.equals(expectedOrder)) {
            throw new EnergyStreamProcessingException(
                    "The configured stream layer order is invalid"
            );
        }
    }

    public static EnergyStreamConfiguration fromJson(
            boolean authenticationEnabled,
            boolean encryptionEnabled,
            boolean compressionEnabled,
            String layerOrderJson
    ) {
        List<EnergyStreamLayer> configuredOrder = parseLayerOrder(layerOrderJson);

        return new EnergyStreamConfiguration(
                authenticationEnabled,
                encryptionEnabled,
                compressionEnabled,
                configuredOrder
        );
    }

    private static List<EnergyStreamLayer> enabledLayers(
            boolean authenticationEnabled,
            boolean encryptionEnabled,
            boolean compressionEnabled
    ) {
        List<EnergyStreamLayer> enabledLayers = new ArrayList<>();

        for (EnergyStreamLayer layer : SECURE_ORDER) {
            if (layer == EnergyStreamLayer.COMPRESSION && compressionEnabled) {
                enabledLayers.add(layer);
            }

            if (layer == EnergyStreamLayer.ENCRYPTION && encryptionEnabled) {
                enabledLayers.add(layer);
            }

            if (layer == EnergyStreamLayer.AUTHENTICATION && authenticationEnabled) {
                enabledLayers.add(layer);
            }
        }

        return enabledLayers;
    }

    private static List<EnergyStreamLayer> parseLayerOrder(String json) {
        if (json == null || json.isBlank()) {
            throw new EnergyStreamProcessingException("Stream layer order is missing");
        }

        String trimmed = json.trim();

        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new EnergyStreamProcessingException("Stream layer order is malformed");
        }

        String content = trimmed.substring(1, trimmed.length() - 1).trim();

        if (content.isEmpty()) {
            return List.of();
        }

        List<EnergyStreamLayer> layers = new ArrayList<>();
        Set<EnergyStreamLayer> uniqueLayers = new HashSet<>();

        for (String token : content.split(",")) {
            String value = token.trim();

            if (value.length() < 2 || !value.startsWith("\"") || !value.endsWith("\"")) {
                throw new EnergyStreamProcessingException("Stream layer order is malformed");
            }

            String layerName = value.substring(1, value.length() - 1)
                    .toUpperCase(Locale.ROOT);
            EnergyStreamLayer layer;

            try {
                layer = EnergyStreamLayer.valueOf(layerName);
            } catch (IllegalArgumentException exception) {
                throw new EnergyStreamProcessingException(
                        "Stream layer order contains an unknown layer"
                );
            }

            if (!uniqueLayers.add(layer)) {
                throw new EnergyStreamProcessingException(
                        "Stream layer order contains a duplicate layer"
                );
            }

            layers.add(layer);
        }

        return layers;
    }
}
