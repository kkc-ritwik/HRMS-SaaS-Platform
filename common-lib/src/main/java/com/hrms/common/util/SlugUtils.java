package com.hrms.common.util;

import java.text.Normalizer;

public final class SlugUtils {
    private SlugUtils() {}
    public static String toSlug(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\w\\s-]", "").replaceAll("\\s+", "-").toLowerCase();
    }
}
