package com.wmpj.smartcity360.core.energy;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

public final class AuthenticationEnergyStreamDecorator extends AbstractEnergyStreamDecorator {

    private static final byte[] MAGIC = {'H', 'M', 'A', 'C'};
    private static final byte VERSION = 1;
    private static final int TAG_LENGTH = 32;

    private final SecretKey authenticationKey;
    private final byte[] protocolBinding;

    public AuthenticationEnergyStreamDecorator(
            EnergyStreamComponent wrappedComponent,
            SecretKey authenticationKey,
            String meterCode
    ) {
        super(wrappedComponent);
        this.authenticationKey = Objects.requireNonNull(authenticationKey, "authenticationKey");
        Objects.requireNonNull(meterCode, "meterCode");
        this.protocolBinding = ("SmartCity360-Energy-v1:" + meterCode)
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] encode(byte[] payload) {
        byte[] innerPayload = wrappedComponent.encode(payload);
        byte[] tag = createTag(innerPayload);

        return ByteBuffer.allocate(MAGIC.length + 1 + TAG_LENGTH + innerPayload.length)
                .put(MAGIC)
                .put(VERSION)
                .put(tag)
                .put(innerPayload)
                .array();
    }

    @Override
    public byte[] decode(byte[] payload) {
        int headerLength = MAGIC.length + 1 + TAG_LENGTH;

        if (payload == null || payload.length < headerLength) {
            throw new EnergyStreamProcessingException("Authenticated energy payload is malformed");
        }

        ByteBuffer envelope = ByteBuffer.wrap(payload);
        byte[] magic = new byte[MAGIC.length];
        envelope.get(magic);
        byte version = envelope.get();

        if (!Arrays.equals(MAGIC, magic) || version != VERSION) {
            throw new EnergyStreamProcessingException("Authenticated energy payload is malformed");
        }

        byte[] suppliedTag = new byte[TAG_LENGTH];
        envelope.get(suppliedTag);
        byte[] innerPayload = new byte[envelope.remaining()];
        envelope.get(innerPayload);
        byte[] expectedTag = createTag(innerPayload);

        if (!MessageDigest.isEqual(suppliedTag, expectedTag)) {
            throw new EnergyStreamProcessingException(
                    "Authenticated energy payload failed validation"
            );
        }

        return wrappedComponent.decode(innerPayload);
    }

    private byte[] createTag(byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(authenticationKey);
            mac.update(protocolBinding);
            mac.update(payload);
            return mac.doFinal();
        } catch (GeneralSecurityException exception) {
            throw new EnergyStreamProcessingException(
                    "Energy payload authentication failed",
                    exception
            );
        }
    }
}
