package com.wmpj.smartcity360.ejb.security;

import com.wmpj.smartcity360.core.service.PasswordHashService;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Stateless
@Local(PasswordHashService.class)
public class Pbkdf2PasswordHashServiceBean implements PasswordHashService {

    static final int ITERATIONS = 600_000;
    static final int SALT_LENGTH_BYTES = 16;
    static final int HASH_LENGTH_BYTES = 32;

    private static final String ALGORITHM_ID = "pbkdf2-sha256";
    private static final String JCA_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int MINIMUM_ACCEPTED_ITERATIONS = 100_000;
    private static final int MAXIMUM_ACCEPTED_ITERATIONS = 2_000_000;
    private static final int MINIMUM_SALT_LENGTH_BYTES = 16;
    private static final int MAXIMUM_SALT_LENGTH_BYTES = 64;
    private static final int MINIMUM_HASH_LENGTH_BYTES = 16;
    private static final int MAXIMUM_HASH_LENGTH_BYTES = 64;

    private final SecureRandom secureRandom;

    public Pbkdf2PasswordHashServiceBean() {
        secureRandom = new SecureRandom();
    }

    Pbkdf2PasswordHashServiceBean(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
    }

    @Override
    public String hash(char[] password) {
        requirePassword(password);

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derivedHash = derive(password, salt, ITERATIONS, HASH_LENGTH_BYTES);

        try {
            return encode(ITERATIONS, HASH_LENGTH_BYTES, salt, derivedHash);
        } finally {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(derivedHash, (byte) 0);
        }
    }

    @Override
    public boolean verify(char[] password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        ParsedHash parsedHash = parse(storedHash);

        if (parsedHash == null) {
            return false;
        }

        byte[] candidateHash = derive(
                password,
                parsedHash.salt(),
                parsedHash.iterations(),
                parsedHash.hashLengthBytes()
        );

        try {
            return MessageDigest.isEqual(parsedHash.hash(), candidateHash);
        } finally {
            parsedHash.clear();
            Arrays.fill(candidateHash, (byte) 0);
        }
    }

    private static void requirePassword(char[] password) {
        Objects.requireNonNull(password, "password");

        if (password.length == 0) {
            throw new IllegalArgumentException("password must not be empty");
        }
    }

    private static byte[] derive(
            char[] password,
            byte[] salt,
            int iterations,
            int hashLengthBytes
    ) {
        PBEKeySpec keySpec = new PBEKeySpec(
                password,
                salt,
                iterations,
                Math.multiplyExact(hashLengthBytes, Byte.SIZE)
        );

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(JCA_ALGORITHM);
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("PBKDF2 password hashing is unavailable", exception);
        } finally {
            keySpec.clearPassword();
        }
    }

    private static String encode(
            int iterations,
            int hashLengthBytes,
            byte[] salt,
            byte[] hash
    ) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        return "$" + ALGORITHM_ID
                + "$i=" + iterations + ",l=" + hashLengthBytes
                + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(hash);
    }

    private static ParsedHash parse(String storedHash) {
        try {
            String[] sections = storedHash.split("\\$", -1);

            if (sections.length != 5 || !sections[0].isEmpty()) {
                return null;
            }

            if (!ALGORITHM_ID.equals(sections[1])) {
                return null;
            }

            String[] parameters = sections[2].split(",", -1);

            if (parameters.length != 2
                    || !parameters[0].startsWith("i=")
                    || !parameters[1].startsWith("l=")) {
                return null;
            }

            int iterations = Integer.parseInt(parameters[0].substring(2));
            int hashLengthBytes = Integer.parseInt(parameters[1].substring(2));

            if (iterations < MINIMUM_ACCEPTED_ITERATIONS
                    || iterations > MAXIMUM_ACCEPTED_ITERATIONS
                    || hashLengthBytes < MINIMUM_HASH_LENGTH_BYTES
                    || hashLengthBytes > MAXIMUM_HASH_LENGTH_BYTES) {
                return null;
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] salt = decoder.decode(sections[3]);
            byte[] hash = decoder.decode(sections[4]);

            if (salt.length < MINIMUM_SALT_LENGTH_BYTES
                    || salt.length > MAXIMUM_SALT_LENGTH_BYTES
                    || hash.length != hashLengthBytes) {
                Arrays.fill(salt, (byte) 0);
                Arrays.fill(hash, (byte) 0);
                return null;
            }

            return new ParsedHash(iterations, hashLengthBytes, salt, hash);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private record ParsedHash(
            int iterations,
            int hashLengthBytes,
            byte[] salt,
            byte[] hash
    ) {

        private void clear() {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(hash, (byte) 0);
        }
    }
}
