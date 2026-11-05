package com.wmpj.smartcity360.core.energy;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

public final class EncryptionEnergyStreamDecorator extends AbstractEnergyStreamDecorator {

    private static final byte[] MAGIC = {'A', 'G', 'C', 'M'};
    private static final byte VERSION = 1;
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey encryptionKey;
    private final byte[] additionalData;
    private final SecureRandom secureRandom;

    public EncryptionEnergyStreamDecorator(
            EnergyStreamComponent wrappedComponent,
            SecretKey encryptionKey,
            String meterCode
    ) {
        this(wrappedComponent, encryptionKey, meterCode, new SecureRandom());
    }

    EncryptionEnergyStreamDecorator(
            EnergyStreamComponent wrappedComponent,
            SecretKey encryptionKey,
            String meterCode,
            SecureRandom secureRandom
    ) {
        super(wrappedComponent);
        this.encryptionKey = Objects.requireNonNull(encryptionKey, "encryptionKey");
        this.additionalData = protocolBinding(meterCode);
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
    }

    @Override
    public byte[] encode(byte[] payload) {
        byte[] innerPayload = wrappedComponent.encode(payload);
        byte[] nonce = new byte[NONCE_LENGTH];
        secureRandom.nextBytes(nonce);

        try {
            Cipher cipher = cipher(Cipher.ENCRYPT_MODE, nonce);
            byte[] ciphertext = cipher.doFinal(innerPayload);

            return ByteBuffer.allocate(MAGIC.length + 1 + NONCE_LENGTH + ciphertext.length)
                    .put(MAGIC)
                    .put(VERSION)
                    .put(nonce)
                    .put(ciphertext)
                    .array();
        } catch (GeneralSecurityException exception) {
            throw new EnergyStreamProcessingException("Energy payload encryption failed", exception);
        }
    }

    @Override
    public byte[] decode(byte[] payload) {
        int minimumLength = MAGIC.length + 1 + NONCE_LENGTH + 16;

        if (payload == null || payload.length < minimumLength) {
            throw new EnergyStreamProcessingException("Encrypted energy payload is malformed");
        }

        ByteBuffer envelope = ByteBuffer.wrap(payload);
        byte[] magic = new byte[MAGIC.length];
        envelope.get(magic);
        byte version = envelope.get();

        if (!Arrays.equals(MAGIC, magic) || version != VERSION) {
            throw new EnergyStreamProcessingException("Encrypted energy payload is malformed");
        }

        byte[] nonce = new byte[NONCE_LENGTH];
        envelope.get(nonce);
        byte[] ciphertext = new byte[envelope.remaining()];
        envelope.get(ciphertext);

        try {
            Cipher cipher = cipher(Cipher.DECRYPT_MODE, nonce);
            return wrappedComponent.decode(cipher.doFinal(ciphertext));
        } catch (AEADBadTagException exception) {
            throw new EnergyStreamProcessingException("Encrypted energy payload failed validation");
        } catch (GeneralSecurityException exception) {
            throw new EnergyStreamProcessingException("Energy payload decryption failed", exception);
        }
    }

    private Cipher cipher(int mode, byte[] nonce) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, encryptionKey, new GCMParameterSpec(TAG_BITS, nonce));
        cipher.updateAAD(additionalData);
        return cipher;
    }

    private static byte[] protocolBinding(String meterCode) {
        Objects.requireNonNull(meterCode, "meterCode");
        return ("SmartCity360-Energy-v1:" + meterCode).getBytes(StandardCharsets.UTF_8);
    }
}
