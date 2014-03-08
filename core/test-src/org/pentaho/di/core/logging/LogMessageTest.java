package org.pentaho.di.core.logging;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests for {@link LogMessage}.
 */
public class LogMessageTest extends TestCase {
  public void testToString() throws Exception {
    LogMessage msg = new LogMessage("Log message",
        "Channel 01",
        LogLevel.DEBUG);
    msg.setSubject("Simple");

    Assert.assertEquals("Simple - Log message", msg.toString());
  }

  public void testToString_withOneArgument() throws Exception {
    LogMessage msg = new LogMessage(
        "Log message for {0}",
        "Channel 01",
        new String[]{"Test"},
        LogLevel.DEBUG);
    msg.setSubject("Subject");

    Assert.assertEquals("Subject - Log message for Test", msg.toString());
  }
}
