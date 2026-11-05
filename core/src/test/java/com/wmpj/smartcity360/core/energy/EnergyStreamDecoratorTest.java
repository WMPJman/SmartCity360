package com.wmpj.smartcity360.core.energy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnergyStreamDecoratorTest {

    private SecretKey authenticationKey;
    private SecretKey encryptionKey;
    private byte[] payload;

    @BeforeEach
    void createEphemeralKeys() throws GeneralSecurityException {
        KeyGenerator hmacGenerator = KeyGenerator.getInstance("HmacSHA256");
        hmacGenerator.init(256);
        authenticationKey = hmacGenerator.generateKey();

        KeyGenerator aesGenerator = KeyGenerator.getInstance("AES");
        aesGenerator.init(256);
        encryptionKey = aesGenerator.generateKey();
        payload = "meter reading: 14.2500 kWh".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void basicAndEachDecoratorOperateIndependently() {
        List<EnergyStreamComponent> components = List.of(
                new BasicEnergyStream(),
                new CompressionEnergyStreamDecorator(new BasicEnergyStream()),
                new EncryptionEnergyStreamDecorator(
                        new BasicEnergyStream(),
                        encryptionKey,
                        "METER-001"
                ),
                new AuthenticationEnergyStreamDecorator(
                        new BasicEnergyStream(),
                        authenticationKey,
                        "METER-001"
                )
        );

        for (EnergyStreamComponent component : components) {
            byte[] encoded = component.encode(payload);
            assertArrayEquals(payload, component.decode(encoded));
        }

        assertArrayEquals(payload, new BasicEnergyStream().encode(payload));
    }

    @Test
    void configuredPipelineUsesSecureOrderAndRoundTrips() {
        EnergyStreamConfiguration configuration = EnergyStreamConfiguration.fromJson(
                true,
                true,
                true,
                "[\"COMPRESSION\",\"ENCRYPTION\",\"AUTHENTICATION\"]"
        );
        EnergyStreamComponent pipeline = new EnergyStreamPipelineFactory().create(
                "METER-001",
                configuration,
                new EnergyStreamKeys(authenticationKey, encryptionKey)
        );

        byte[] encoded = pipeline.encode(payload);

        assertFalse(Arrays.equals(payload, encoded));
        assertArrayEquals(payload, pipeline.decode(encoded));
    }

    @Test
    void disabledLayersAreNotApplied() {
        EnergyStreamConfiguration configuration = EnergyStreamConfiguration.fromJson(
                false,
                false,
                false,
                "[]"
        );
        EnergyStreamComponent pipeline = new EnergyStreamPipelineFactory().create(
                "METER-001",
                configuration,
                new EnergyStreamKeys(null, null)
        );

        assertArrayEquals(payload, pipeline.encode(payload));
    }

    @Test
    void invalidDuplicateOrInsecureLayerOrdersAreRejected() {
        assertThrows(
                EnergyStreamProcessingException.class,
                () -> EnergyStreamConfiguration.fromJson(
                        true,
                        false,
                        false,
                        "[\"AUTHENTICATION\",\"AUTHENTICATION\"]"
                )
        );
        assertThrows(
                EnergyStreamProcessingException.class,
                () -> EnergyStreamConfiguration.fromJson(
                        true,
                        true,
                        true,
                        "[\"AUTHENTICATION\",\"ENCRYPTION\",\"COMPRESSION\"]"
                )
        );
        assertThrows(
                EnergyStreamProcessingException.class,
                () -> EnergyStreamConfiguration.fromJson(
                        false,
                        false,
                        true,
                        "[\"UNKNOWN\"]"
                )
        );
    }

    @Test
    void authenticationAndEncryptionRejectModifiedEnvelopes() {
        EnergyStreamComponent authentication = new AuthenticationEnergyStreamDecorator(
                new BasicEnergyStream(),
                authenticationKey,
                "METER-001"
        );
        byte[] authenticated = authentication.encode(payload);
        authenticated[authenticated.length - 1] ^= 1;

        assertThrows(
                EnergyStreamProcessingException.class,
                () -> authentication.decode(authenticated)
        );

        EnergyStreamComponent encryption = new EncryptionEnergyStreamDecorator(
                new BasicEnergyStream(),
                encryptionKey,
                "METER-001"
        );
        byte[] encrypted = encryption.encode(payload);
        encrypted[encrypted.length - 1] ^= 1;

        assertThrows(
                EnergyStreamProcessingException.class,
                () -> encryption.decode(encrypted)
        );
    }

    @Test
    void decompressionLimitAndMissingKeysFailClosed() throws Exception {
        byte[] oversizedCompressed = gzip(
                new byte[CompressionEnergyStreamDecorator.MAX_DECOMPRESSED_BYTES + 1]
        );
        EnergyStreamComponent compression = new CompressionEnergyStreamDecorator(
                new BasicEnergyStream()
        );

        assertThrows(
                EnergyStreamProcessingException.class,
                () -> compression.decode(oversizedCompressed)
        );

        EnergyStreamConfiguration encryptedConfiguration = EnergyStreamConfiguration.fromJson(
                false,
                true,
                false,
                "[\"ENCRYPTION\"]"
        );

        assertThrows(
                EnergyKeyUnavailableException.class,
                () -> new EnergyStreamPipelineFactory().create(
                        "METER-001",
                        encryptedConfiguration,
                        new EnergyStreamKeys(null, null)
                )
        );
    }

    private static byte[] gzip(byte[] value) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(value);
        }

        return output.toByteArray();
    }
}
