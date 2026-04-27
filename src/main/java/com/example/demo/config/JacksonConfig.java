package com.example.demo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 时间反序列化配置
 * 无时区时间按 UTC+8 直接解析，带时区时间统一换算为 UTC+8 后存储。
 */
@Configuration
public class JacksonConfig {

    private static final ZoneId UTC_PLUS_8 = ZoneId.of("Asia/Shanghai");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                    String rawValue = parser.getText();
                    if (rawValue == null || rawValue.trim().isEmpty()) {
                        return null;
                    }

                    String normalizedValue = normalizeDateTime(rawValue.trim());

                    try {
                        if (containsTimezone(normalizedValue)) {
                            return OffsetDateTime.parse(normalizedValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                    .atZoneSameInstant(UTC_PLUS_8)
                                    .toLocalDateTime();
                        }

                        return LocalDateTime.parse(normalizedValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (DateTimeParseException ex) {
                        throw new DateTimeParseException("无法解析时间", rawValue, ex.getErrorIndex(), ex);
                    }
                }
            });
            builder.modules(javaTimeModule);
        };
    }

    private static boolean containsTimezone(String value) {
        return value.endsWith("Z") || value.matches(".*[+-]\\d{2}:\\d{2}$");
    }

    private static String normalizeDateTime(String value) {
        String normalized = value;
        if (normalized.contains(" ") && !normalized.contains("T")) {
            normalized = normalized.replaceFirst(" ", "T");
        }

        int separatorIndex = normalized.indexOf('T');
        if (separatorIndex < 0) {
            return normalized;
        }

        String[] dateParts = normalized.substring(0, separatorIndex).split("-");
        if (dateParts.length != 3) {
            return normalized;
        }

        String normalizedDate = String.format("%s-%02d-%02d",
                dateParts[0],
                Integer.parseInt(dateParts[1]),
                Integer.parseInt(dateParts[2]));

        return normalizedDate + normalized.substring(separatorIndex);
    }
}
