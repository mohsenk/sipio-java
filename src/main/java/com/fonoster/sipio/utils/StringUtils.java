package com.fonoster.sipio.utils;

public class StringUtils {

    public static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

}
