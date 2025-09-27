package com.ccms.portal.util;

public final class MaskUtil {

    private MaskUtil() {}

    /** Mask a card number, leaving only last 4 visible. */
    public static String maskCardNumber(String number) {
        if (number == null || number.isBlank()) return null;
        String digits = number.replaceAll("\\s+", "");
        if (digits.length() <= 4) return digits;
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    /** Get only the last 4 digits of a card number. */
    public static String last4(String number) {
        if (number == null || number.isBlank()) return null;
        String digits = number.replaceAll("\\s+", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }
}
