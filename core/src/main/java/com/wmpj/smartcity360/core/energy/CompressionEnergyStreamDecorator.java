package com.wmpj.smartcity360.core.energy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class CompressionEnergyStreamDecorator extends AbstractEnergyStreamDecorator {

    public static final int MAX_DECOMPRESSED_BYTES = 262_144;

    public CompressionEnergyStreamDecorator(EnergyStreamComponent wrappedComponent) {
        super(wrappedComponent);
    }

    @Override
    public byte[] encode(byte[] payload) {
        byte[] innerPayload = wrappedComponent.encode(payload);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(innerPayload);
            }

            return output.toByteArray();
        } catch (IOException exception) {
            throw new EnergyStreamProcessingException("Energy payload compression failed", exception);
        }
    }

    @Override
    public byte[] decode(byte[] payload) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(payload))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4_096];
            int totalBytes = 0;
            int bytesRead;

            while ((bytesRead = gzip.read(buffer)) != -1) {
                totalBytes += bytesRead;

                if (totalBytes > MAX_DECOMPRESSED_BYTES) {
                    throw new EnergyStreamProcessingException(
                            "Decoded energy payload exceeds the maximum size"
                    );
                }

                output.write(buffer, 0, bytesRead);
            }

            return wrappedComponent.decode(output.toByteArray());
        } catch (EnergyStreamProcessingException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new EnergyStreamProcessingException("Energy payload is not valid GZIP data");
        }
    }
}
