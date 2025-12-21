/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2025 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResultTest {

  private Result result;

  @Before
  public void setUp() {
    result = new Result();
  }

  @Test
  public void testGetMaxCharCount_DefaultValue() {
    System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    // When no system property is set, should return -1 (unlimited)
    int maxCharCount = result.getMaxCharCount();
    assertEquals(-1, maxCharCount); // Default: unlimited (no system property set)
  }

  @Test
  public void testGetMaxCharCount_WithSystemProperty() {
    // Test with system property set to 1MB
    System.setProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB", "1");
    try {
      Result testResult = new Result(); // New instance to pick up system property
      int maxCharCount = testResult.getMaxCharCount();
      assertEquals(262144, maxCharCount); // 1MB = 262144 chars (with UTF-8 maxBytesPerChar = 4.0)
    } finally {
      System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    }
  }

  @Test
  public void testGetMaxCharCount_NoLogging() {
    // Test with system property set to 0 (no logging)
    System.setProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB", "0");
    try {
      Result testResult = new Result(); // New instance to pick up system property
      int maxCharCount = testResult.getMaxCharCount();
      assertEquals(0, maxCharCount); // 0MB = no logging
    } finally {
      System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    }
  }

  @Test
  public void testGetMaxCharCount_CustomValue() {
    // Test with custom 2MB setting
    System.setProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB", "2");
    try {
      Result testResult = new Result(); // New instance to pick up system property
      int maxCharCount = testResult.getMaxCharCount();
      assertEquals(524288, maxCharCount); // 2MB = 524288 chars (with UTF-8 maxBytesPerChar = 4.0)
    } finally {
      System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    }
  }

  @Test
  public void testAppendLogText_WithinLimit() {
    // Test normal append within buffer limit - should always work
    String testText = "Short message";
    result.appendLogText(testText);
    String logText = result.getLogText();
    assertTrue("Log should contain the short message", logText.contains("Short message"));
  }

  @Test
  public void testAppendLogText_Basic() {
    // Test basic functionality - replacement for unlimited buffer test
    result.appendLogText("Hello");
    result.appendLogText(" World");
    String logText = result.getLogText();
    assertTrue("Log should contain messages", !logText.isEmpty());
    // Should contain the expected concatenated result
    assertTrue("Log should contain 'Hello World'", logText.contains("Hello World"));
    assertTrue("Log should contain 'Hello'", logText.contains("Hello"));
    assertTrue("Log should contain 'World'", logText.contains("World"));
  }

  @Test
  public void testAppendLogText_IncomingTextExceedsLimit() {
    // Test case where incoming text is larger than entire buffer limit
    System.setProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB", "1");
    try {
      Result testResult = new Result(); // New instance to pick up system property
      // Create text larger than 262144 chars (1MB limit)
      String massiveText = createStringOfLength(400000, "INITIAL_UNIQUE_PREFIX_");
      testResult.appendLogText(massiveText);
      
      String logText = testResult.getLogText();
      assertNotNull(logText);
      assertTrue("Log should not be empty", !logText.isEmpty());
      assertTrue("Log should be within limit", logText.length() <= testResult.getMaxCharCount());
      // Since the incoming text is larger than the buffer, it should be truncated but contain some of the prefix
      assertTrue("Log should contain some part of the message", 
          logText.contains("INITIAL_UNIQUE_PREFIX_") || logText.contains("ABCDEFG"));
    } finally {
      System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    }
  }

  @Test
  public void testAppendLogText_MultipleAppendsRespectLimit() {
    // Test multiple appends that eventually exceed the buffer limit
    System.setProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB", "1");
    try {
      Result testResult = new Result(); // New instance to pick up system property
      // Add initial text
      String initialText = createStringOfLength(50000, "INITIAL_UNIQUE_PREFIX_");
      testResult.appendLogText(initialText);
      
      // Add several chunks that will exceed the limit
      for (int i = 0; i < 6; i++) {
        String chunkText = createStringOfLength(80000, "CHUNK_" + i + "_");
        testResult.appendLogText(chunkText);
      }
      
      String logText = testResult.getLogText();
      assertNotNull(logText);
      assertTrue("Log should be within limit", logText.length() <= testResult.getMaxCharCount());
      // Should contain the most recent chunk (CHUNK_5_) when trimming occurs
      assertTrue("Log should contain the most recent chunk", logText.contains("CHUNK_5_"));
      // The initial text should likely be trimmed out due to buffer limits
      assertFalse("Initial text should be trimmed out", logText.contains("INITIAL_UNIQUE_PREFIX_"));
    } finally {
      System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    }
  }

  @Test
  public void testAppendLogText_TrimBehavior() {
    // Test that trimming keeps recent content and removes old content
    System.setProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB", "1");
    try {
      Result testResult = new Result(); // New instance to pick up system property
      // Fill buffer close to limit
      String oldText = createStringOfLength(300000, "OLD_TEXT_");
      testResult.appendLogText(oldText);
      
      // Add new text that should trigger trimming
      String newText = createStringOfLength(100000, "NEW_TEXT_");
      testResult.appendLogText(newText);
      
      String logText = testResult.getLogText();
      assertNotNull(logText);
      assertTrue("Log should be within limit", logText.length() <= testResult.getMaxCharCount());
      assertTrue("Log should contain new text", logText.contains("NEW_TEXT_"));
    } finally {
      System.clearProperty("KETTLE_MAX_LOG_BUFFER_SIZE_MB");
    }
  }

  @Test
  public void testAppendLogText_NullAndEmptyHandling() {
    // Test null and empty string handling
    result.appendLogText(null);
    assertEquals("", result.getLogText());
    
    result.appendLogText("");
    assertEquals("", result.getLogText());
    
    result.appendLogText("test");
    assertEquals("test", result.getLogText());
    
    result.appendLogText(null);
    assertEquals("test", result.getLogText());
  }

  /**
   * Helper method to create a string of specified length with repeating pattern
   */
  private String createStringOfLength(int length, String prefix) {
    StringBuilder sb = new StringBuilder(length);
    sb.append(prefix);
    
    // Fill remaining space with repeating A-Z pattern
    String pattern = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    int remaining = length - prefix.length();
    
    while (remaining > 0) {
      int chunkSize = Math.min(remaining, pattern.length());
      sb.append(pattern, 0, chunkSize);
      remaining -= chunkSize;
    }
    
    return sb.toString();
  }
}