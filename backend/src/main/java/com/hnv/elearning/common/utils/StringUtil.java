package com.hnv.elearning.common.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class StringUtil {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Chuyển chuỗi tiếng Việt có dấu thành không dấu giữ nguyên khoảng trắng/ký tự.
     * Ví dụ: "Lập Trình Java Spring Boot Đỉnh Cao" -> "Lap Trinh Java Spring Boot Dinh Cao"
     */
    public static String removeAccents(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Tách các ký tự có dấu thành: ký tự gốc + ký tự dấu rời rạc
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Xóa các ký tự dấu rời rạc
        String withoutAccents = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");

        // Thay thế ký tự đ/Đ
        return withoutAccents.replace('đ', 'd').replace('Đ', 'D');
    }


    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String noAccents = removeAccents(input).toLowerCase(Locale.ROOT);

        // Bỏ ký tự đặc biệt chỉ giữ a-z, 0-9, space, gạch ngang
        String clean = SPECIAL_CHARS_PATTERN.matcher(noAccents).replaceAll("");

        // Thay khoảng trắng thành dấu gạch nối và trim gạch nối ở 2 đầu
        return WHITESPACE_PATTERN.matcher(clean.trim()).replaceAll("-")
                .replaceAll("^-+|-+$", "");
    }
}
