/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.steps.avro;

import org.apache.avro.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for Avro schemas to protect against CVE-2025-33042 (code injection via schema fields).
 * This class sanitizes and validates Avro schemas before parsing to prevent malicious code injection.
 */
public class AvroSchemaValidator {

  private static final ObjectMapper mapper = new ObjectMapper();

  // Patterns that indicate potential code injection attempts
  private static final List<Pattern> INJECTION_PATTERNS = new ArrayList<>();

  static {
    // Java code patterns that shouldn't be in schema fields
    INJECTION_PATTERNS.add(Pattern.compile("Runtime\\.getRuntime", Pattern.CASE_INSENSITIVE));
    INJECTION_PATTERNS.add(Pattern.compile("exec\\s*\\(", Pattern.CASE_INSENSITIVE));
    INJECTION_PATTERNS.add(Pattern.compile("\\*\\s*/", Pattern.DOTALL)); // */ comment close
    INJECTION_PATTERNS.add(Pattern.compile("/\\s*\\*", Pattern.DOTALL)); // /* comment open
    INJECTION_PATTERNS.add(Pattern.compile("static\\s*\\{", Pattern.CASE_INSENSITIVE)); // static block
    INJECTION_PATTERNS.add(Pattern.compile("class\\s+\\w+", Pattern.CASE_INSENSITIVE)); // class definition
  }

  /**
   * Validates a schema string for potential code injection vulnerabilities.
   * 
   * @param schemaString the schema JSON string to validate
   * @throws IllegalArgumentException if the schema contains suspicious patterns
   */
  public static void validateSchema(String schemaString) throws IllegalArgumentException {
    if (schemaString == null || schemaString.isEmpty()) {
      throw new IllegalArgumentException("Schema string cannot be null or empty");
    }

    try {
      JsonNode schemaNode = mapper.readTree(schemaString);
      if (schemaNode.isObject()) {
        validateSchemaNode((ObjectNode) schemaNode);
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid schema JSON: " + e.getMessage(), e);
    }
  }

  /**
   * Validates a schema node and all its fields for injection patterns.
   * 
   * @param node the schema node to validate
   * @throws IllegalArgumentException if suspicious patterns are found
   */
  private static void validateSchemaNode(ObjectNode node) throws IllegalArgumentException {
    Iterator<String> fieldNames = node.fieldNames();
    
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode fieldValue = node.get(fieldName);

      // Check doc field specifically, as it's the vector for CVE-2025-33042
      if ("doc".equalsIgnoreCase(fieldName) && fieldValue != null) {
        if (fieldValue.isTextual()) {
          String docContent = fieldValue.asText();
          checkForInjectionPatterns(docContent, "doc field");
        }
      }

      // Recursively check nested objects
      if (fieldValue != null && fieldValue.isObject()) {
        validateSchemaNode((ObjectNode) fieldValue);
      } else if (fieldValue != null && fieldValue.isArray()) {
        fieldValue.forEach(item -> {
          if (item.isObject()) {
            validateSchemaNode((ObjectNode) item);
          }
        });
      }
    }
  }

  /**
   * Checks a string for injection patterns.
   * 
   * @param content the content to check
   * @param fieldName the name of the field being checked (for error messages)
   * @throws IllegalArgumentException if injection patterns are found
   */
  private static void checkForInjectionPatterns(String content, String fieldName) 
      throws IllegalArgumentException {
    if (content == null || content.isEmpty()) {
      return;
    }

    for (Pattern pattern : INJECTION_PATTERNS) {
      if (pattern.matcher(content).find()) {
        throw new IllegalArgumentException(
          String.format("Potential code injection detected in %s: %s", fieldName, 
            "schema contains suspicious code patterns"));
      }
    }
  }

  /**
   * Sanitizes a schema by removing or neutralizing suspicious patterns.
   * This is a more lenient approach than strict validation.
   * 
   * @param schemaString the schema JSON string to sanitize
   * @return the sanitized schema string
   */
  public static String sanitizeSchema(String schemaString) {
    if (schemaString == null || schemaString.isEmpty()) {
      return schemaString;
    }

    try {
      JsonNode schemaNode = mapper.readTree(schemaString);
      if (schemaNode.isObject()) {
        sanitizeSchemaNode((ObjectNode) schemaNode);
      }
      return mapper.writeValueAsString(schemaNode);
    } catch (Exception e) {
      // If sanitization fails, return original (validation will catch it later)
      return schemaString;
    }
  }

  /**
   * Sanitizes a schema node by removing injection patterns.
   * 
   * @param node the schema node to sanitize
   */
  private static void sanitizeSchemaNode(ObjectNode node) {
    Iterator<String> fieldNames = node.fieldNames();
    
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode fieldValue = node.get(fieldName);

      // Sanitize doc and similar text fields
      if (isTextField(fieldName) && fieldValue != null && fieldValue.isTextual()) {
        String sanitized = sanitizeText(fieldValue.asText());
        node.put(fieldName, sanitized);
      }

      // Recursively sanitize nested objects
      if (fieldValue != null && fieldValue.isObject()) {
        sanitizeSchemaNode((ObjectNode) fieldValue);
      } else if (fieldValue != null && fieldValue.isArray()) {
        fieldValue.forEach(item -> {
          if (item.isObject()) {
            sanitizeSchemaNode((ObjectNode) item);
          }
        });
      }
    }
  }

  /**
   * Checks if a field name typically contains user documentation.
   * 
   * @param fieldName the field name
   * @return true if the field is typically a text field
   */
  private static boolean isTextField(String fieldName) {
    return fieldName.equalsIgnoreCase("doc") 
        || fieldName.equalsIgnoreCase("description")
        || fieldName.equalsIgnoreCase("comment");
  }

  /**
   * Sanitizes text by removing code injection patterns.
   * 
   * @param text the text to sanitize
   * @return the sanitized text
   */
  private static String sanitizeText(String text) {
    String sanitized = text;
    
    for (Pattern pattern : INJECTION_PATTERNS) {
      // Remove or replace detected patterns
      sanitized = pattern.matcher(sanitized).replaceAll("");
    }
    
    return sanitized;
  }
}
