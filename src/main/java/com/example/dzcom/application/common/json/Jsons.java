package com.example.dzcom.application.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Jackson JSON helper shared by application and interface layers. */
public final class Jsons {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final TypeReference<Map<String, Object>> OBJECT_MAP_TYPE = new TypeReference<>() {
    };

    private Jsons() {
    }

    /** Serialize a value to compact JSON. */
    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON序列化失败", exception);
        }
    }

    /** Parse arbitrary JSON. */
    public static JsonNode readTree(String value) {
        try {
            return MAPPER.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON解析失败", exception);
        }
    }

    /** Parse JSON object, returning an empty object for blank text. */
    public static ObjectNode readObjectOrEmpty(String value) {
        if (value == null || value.isBlank()) {
            return MAPPER.createObjectNode();
        }
        JsonNode node = readTree(value);
        if (node instanceof ObjectNode objectNode) {
            return objectNode;
        }
        throw new IllegalArgumentException("JSON必须是对象");
    }

    /** Parse a JSON object as a mutable map. */
    public static Map<String, Object> readObjectMapOrEmpty(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return MAPPER.readValue(value, OBJECT_MAP_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON对象解析失败", exception);
        }
    }

    /** Validate optional JSON text. */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            MAPPER.readTree(value);
            return true;
        } catch (JsonProcessingException exception) {
            return false;
        }
    }

    /** Return object field, or an empty object when missing/not object. */
    public static ObjectNode object(JsonNode node, String fieldName) {
        JsonNode child = node == null ? null : node.get(fieldName);
        return child instanceof ObjectNode objectNode ? objectNode : MAPPER.createObjectNode();
    }

    /** Return array field, or an empty array when missing/not array. */
    public static ArrayNode array(JsonNode node, String fieldName) {
        JsonNode child = node == null ? null : node.get(fieldName);
        return child instanceof ArrayNode arrayNode ? arrayNode : MAPPER.createArrayNode();
    }

    /** Read nullable text field. */
    public static String text(JsonNode node, String fieldName) {
        JsonNode child = node == null ? null : node.get(fieldName);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.isTextual() ? child.asText() : child.toString();
    }

    /** Read nullable boolean field. */
    public static Boolean bool(JsonNode node, String fieldName) {
        JsonNode child = node == null ? null : node.get(fieldName);
        return child == null || child.isNull() ? null : child.asBoolean();
    }

    /** Read boolean field with default value. */
    public static boolean bool(JsonNode node, String fieldName, boolean defaultValue) {
        Boolean value = bool(node, fieldName);
        return value == null ? defaultValue : value;
    }

    /** Read integer field with default value. */
    public static int integer(JsonNode node, String fieldName, int defaultValue) {
        JsonNode child = node == null ? null : node.get(fieldName);
        return child == null || child.isNull() ? defaultValue : child.asInt(defaultValue);
    }

    /** Read nullable decimal field. */
    public static BigDecimal decimal(JsonNode node, String fieldName) {
        JsonNode child = node == null ? null : node.get(fieldName);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isNumber()) {
            return child.decimalValue();
        }
        String text = child.asText();
        if (text == null || text.isBlank()) {
            return null;
        }
        return new BigDecimal(text.trim());
    }

    /** Convert scalar value to text, preserving objects/arrays as compact JSON. */
    public static String valueText(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        return node.toString();
    }
}
