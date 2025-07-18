package com.study.shortlink.util;

public class Base62 {
    private static final String BASE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE_CHARS.length();

    public static String fromBase10(long n) {
        if (n == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.insert(0, BASE_CHARS.charAt((int) (n % BASE)));
            n /= BASE;
        }
        return sb.toString();
    }
}
