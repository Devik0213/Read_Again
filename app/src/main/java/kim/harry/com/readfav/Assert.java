/*
 * @(#)Assert.class $version 2014. 4. 16.
 *
 * Copyright 2014 Naver Corp. All rights Reserved.
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package kim.harry.com.readfav;

/**
 *
 * @author answer
 */
public class Assert {
    private static final String MESSAGE_FORMAT = "\"%s\" argument must be not null.";
    private static final String MESSAGE_FORMAT_NOT_ZERO = "\"%s\" argument must be zero.";

    public static void notNull(Object object, String argName) {
        if (object == null) {
            // When a parameter is null,
            // it throws a NullPointerException instead of an IllegalArgumentException.
            // This rule is recommended by the item 62 on Effective Java 2nd edition as follows.
            // "If a caller passes null in some parameter for which null values are prohibited,
            // convention dictates that NullPointerException be thrown rather than IllegalArgumentException."
            throw new NullPointerException(String.format(MESSAGE_FORMAT, argName));
        }
    }

    public static void notZero(int value, String argName) {
        if (value == 0) {
            throw new IllegalArgumentException(String.format(MESSAGE_FORMAT_NOT_ZERO, argName));
        }
    }
}