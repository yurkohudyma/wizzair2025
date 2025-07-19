package ua.hudyma.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.security.SecureRandom;

public class IdGenerator {
    private IdGenerator() {
    }

    protected static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public static String generateId(int length) {
        return NanoIdUtils.randomNanoId(new SecureRandom(), ALPHABET, length);
    }
}
