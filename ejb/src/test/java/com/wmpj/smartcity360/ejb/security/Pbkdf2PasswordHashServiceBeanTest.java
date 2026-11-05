package com.wmpj.smartcity360.ejb.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pbkdf2PasswordHashServiceBeanTest {

    @Test
    void hashesAndVerifiesTheCorrectPasswordWithoutStoringPlaintext() {
        Pbkdf2PasswordHashServiceBean service = new Pbkdf2PasswordHashServiceBean();
        char[] password = "Correct Horse Battery Staple".toCharArray();

        String storedHash = service.hash(password);

        assertTrue(storedHash.startsWith("$pbkdf2-sha256$i=600000,l=32$"));
        assertFalse(storedHash.contains(new String(password)));
        assertTrue(service.verify(password, storedHash));
        assertFalse(service.verify("incorrect password".toCharArray(), storedHash));
        Arrays.fill(password, '\0');
    }

    @Test
    void generatesAUniqueSaltForEveryPasswordHash() {
        Pbkdf2PasswordHashServiceBean service = new Pbkdf2PasswordHashServiceBean();
        char[] password = "same password".toCharArray();

        String firstHash = service.hash(password);
        String secondHash = service.hash(password);

        assertNotEquals(firstHash, secondHash);
        assertTrue(service.verify(password, firstHash));
        assertTrue(service.verify(password, secondHash));
        Arrays.fill(password, '\0');
    }

    @Test
    void rejectsMalformedAndUnsupportedStoredHashesWithoutThrowing() {
        Pbkdf2PasswordHashServiceBean service = new Pbkdf2PasswordHashServiceBean();
        char[] password = "irrelevant".toCharArray();

        assertFalse(service.verify(password, null));
        assertFalse(service.verify(password, ""));
        assertFalse(service.verify(password, "plaintext"));
        assertFalse(service.verify(password, "$argon2id$v=19$unsupported"));
        assertFalse(service.verify(password, "$pbkdf2-sha256$i=not-a-number,l=32$salt$hash"));
        assertFalse(service.verify(password, "$pbkdf2-sha256$i=600000,l=32$%%%$%%%"));
        Arrays.fill(password, '\0');
    }
}
