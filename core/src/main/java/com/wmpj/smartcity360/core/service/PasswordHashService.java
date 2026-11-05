package com.wmpj.smartcity360.core.service;

public interface PasswordHashService {

    String hash(char[] password);

    boolean verify(char[] password, String storedHash);
}
