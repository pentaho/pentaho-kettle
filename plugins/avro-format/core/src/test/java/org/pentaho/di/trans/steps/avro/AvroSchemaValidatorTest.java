/*! ****************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.avro;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for AvroSchemaValidator to ensure it protects against CVE-2025-33042.
 */
public class AvroSchemaValidatorTest {

  @Test
  public void testValidSchemaIsAccepted() {
    String validSchema = "{\n"
        + "  \"type\": \"record\",\n"
        + "  \"name\": \"User\",\n"
        + "  \"fields\": [\n"
        + "    {\"name\": \"name\", \"type\": \"string\", \"doc\": \"The user's name\"},\n"
        + "    {\"name\": \"age\", \"type\": \"int\", \"doc\": \"The user's age in years\"}\n"
        + "  ]\n"
        + "}";
    
    try {
      AvroSchemaValidator.validateSchema(validSchema);
      // If we get here, validation passed
      assertTrue(true);
    } catch (IllegalArgumentException e) {
      fail("Valid schema should not be rejected: " + e.getMessage());
    }
  }

  @Test
  public void testMaliciousSchemaWithCodeInjectionIsRejected() {
    String maliciousSchema = "{\n"
        + "  \"type\": \"record\",\n"
        + "  \"name\": \"Exploit\",\n"
        + "  \"fields\": [\n"
        + "    {\n"
        + "      \"name\": \"data\",\n"
        + "      \"type\": \"string\",\n"
        + "      \"doc\": \"*/ static { Runtime.getRuntime().exec(\\\"touch /tmp/pwned\\\"); } /*\"\n"
        + "    }\n"
        + "  ]\n"
        + "}";
    
    try {
      AvroSchemaValidator.validateSchema(maliciousSchema);
      fail("Malicious schema should be rejected");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate code injection detection",
        e.getMessage().contains("code injection") || e.getMessage().contains("suspicious"));
    }
  }

  @Test
  public void testSchemaWithRuntimeGetRuntimeIsRejected() {
    String maliciousSchema = "{\n"
        + "  \"type\": \"record\",\n"
        + "  \"name\": \"Evil\",\n"
        + "  \"fields\": [\n"
        + "    {\"name\": \"data\", \"type\": \"string\", \"doc\": \"Description with Runtime.getRuntime injection\"}\n"
        + "  ]\n"
        + "}";
    
    try {
      AvroSchemaValidator.validateSchema(maliciousSchema);
      fail("Schema with Runtime.getRuntime should be rejected");
    } catch (IllegalArgumentException e) {
      assertTrue("Should detect Runtime.getRuntime pattern", e.getMessage().contains("injection"));
    }
  }

  @Test
  public void testSchemaWithExecIsRejected() {
    String maliciousSchema = "{\n"
        + "  \"type\": \"record\",\n"
        + "  \"name\": \"Exploit\",\n"
        + "  \"fields\": [\n"
        + "    {\"name\": \"cmd\", \"type\": \"string\", \"doc\": \"exec(\\\"rm -rf /\\\")\"}\n"
        + "  ]\n"
        + "}";
    
    try {
      AvroSchemaValidator.validateSchema(maliciousSchema);
      fail("Schema with exec() call should be rejected");
    } catch (IllegalArgumentException e) {
      assertTrue("Should detect exec pattern", e.getMessage().contains("injection"));
    }
  }

  @Test
  public void testSchemaWithCommentCharsIsRejected() {
    String maliciousSchema = "{\n"
        + "  \"type\": \"record\",\n"
        + "  \"name\": \"Exploit\",\n"
        + "  \"fields\": [\n"
        + "    {\"name\": \"data\", \"type\": \"string\", \"doc\": \"comment */ malicious code /* end\"}\n"
        + "  ]\n"
        + "}";
    
    try {
      AvroSchemaValidator.validateSchema(maliciousSchema);
      fail("Schema with comment escape sequences should be rejected");
    } catch (IllegalArgumentException e) {
      assertTrue("Should detect comment pattern", e.getMessage().contains("injection"));
    }
  }

  @Test
  public void testSanitizeSchemaRemovesMaliciousPatterns() {
    String maliciousSchema = "{\n"
        + "  \"type\": \"record\",\n"
        + "  \"name\": \"Test\",\n"
        + "  \"fields\": [\n"
        + "    {\"name\": \"data\", \"type\": \"string\", \"doc\": \"Safe Runtime.getRuntime text\"}\n"
        + "  ]\n"
        + "}";
    
    String sanitized = AvroSchemaValidator.sanitizeSchema(maliciousSchema);
    
    assertNotNull("Sanitized schema should not be null", sanitized);
    assertFalse("Sanitized schema should not contain Runtime.getRuntime",
      sanitized.toLowerCase().contains("runtime.getruntime"));
  }

  @Test
  public void testNullSchemaThrowsException() {
    try {
      AvroSchemaValidator.validateSchema(null);
      fail("Null schema should throw exception");
    } catch (IllegalArgumentException e) {
      assertTrue("Should indicate null schema", e.getMessage().contains("null"));
    }
  }

  @Test
  public void testEmptySchemaThrowsException() {
    try {
      AvroSchemaValidator.validateSchema("");
      fail("Empty schema should throw exception");
    } catch (IllegalArgumentException e) {
      assertTrue("Should indicate empty schema", e.getMessage().contains("empty"));
    }
  }
}
