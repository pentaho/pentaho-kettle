package org.pentaho.di.core.logging;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test for {@link KettleLogLayout}.
 */
public class KettleLogLayoutTest extends TestCase {
  public void testFormat() throws Exception {
    LogMessage mcg = new LogMessage("Log message for {0}",
        "Channel 01",
        new String[]{"Test"},
        LogLevel.DEBUG);
    mcg.setSubject("Subject");

    KettleLoggingEvent event = new KettleLoggingEvent(mcg, 0, LogLevel.BASIC);
    KettleLogLayout layout = new KettleLogLayout();

    final String formattedMsg = layout.format(event);

    Assert.assertEquals("The log message must be formatted and not contain placeholders.",
        "Subject - Log message for Test",
        formattedMsg.substring(formattedMsg.indexOf('-') + 2));
  }
}
