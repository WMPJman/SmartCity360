package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.energy.EnergyKeyUnavailableException;
import com.wmpj.smartcity360.core.energy.EnergyStreamKeys;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@ApplicationScoped
public class EnergyStreamKeyProvider {

    private static final String HMAC_KEY_VARIABLE = "SMARTCITY360_ENERGY_HMAC_KEY";
    private static final String AES_KEY_VARIABLE = "SMARTCITY360_ENERGY_AES_KEY";

    public EnergyStreamKeys loadKeys(
            boolean authenticationRequired,
            boolean encryptionRequired
    ) {
        SecretKey authenticationKey = null;
        SecretKey encryptionKey = null;

        if (authenticationRequired) {
            byte[] keyBytes = decodeKey(HMAC_KEY_VARIABLE);

            if (keyBytes.length < 32) {
                throw new EnergyKeyUnavailableException(
                        "Configured energy authentication key is invalid"
                );
            }

            authenticationKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        }

        if (encryptionRequired) {
            byte[] keyBytes = decodeKey(AES_KEY_VARIABLE);

            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new EnergyKeyUnavailableException(
                        "Configured energy encryption key is invalid"
                );
            }

            encryptionKey = new SecretKeySpec(keyBytes, "AES");
        }

        return new EnergyStreamKeys(authenticationKey, encryptionKey);
    }

    private static byte[] decodeKey(String variableName) {
        String encodedKey = System.getenv(variableName);

        if (encodedKey == null || encodedKey.isBlank()) {
            throw new EnergyKeyUnavailableException("Required energy key is unavailable");
        }

        try {
            return Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new EnergyKeyUnavailableException("Configured energy key is invalid");
        }
    }
}
