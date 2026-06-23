package com.aimr.notify.consumer.util;

import java.util.Map;

public final class TemplateRenderer {

    public static String render(String templateBody, Map<String, String> variables) {
        if (templateBody == null) return "";
        if (variables == null || variables.isEmpty()) return templateBody;

        String result = templateBody;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                // Regex replaces {{key}}, {{ key }}, {{  key  }} case-insensitively
                String regex = "(?i)\\{\\{\\s*" + java.util.regex.Pattern.quote(entry.getKey()) + "\\s*\\}\\}";
                result = result.replaceAll(regex, java.util.regex.Matcher.quoteReplacement(entry.getValue()));
            }
        }
        return result;
    }
}