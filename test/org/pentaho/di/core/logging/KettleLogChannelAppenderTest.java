/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KettleLogChannelAppenderTest {
  /**
   * Test class to encapsulate log message information we might want to retrieve for comparison.
   */
  private class MessageObject {
    private String message;
    private Object[] args;
    private Throwable throwable;

    private MessageObject(String message, Object[] args, Throwable throwable) {
      this.message = message;
      this.args = args;
      this.throwable = throwable;
    }

    public String getMessage() {
      return message;
    }

    public Object[] getArgs() {
      return args;
    }

    public Throwable getThrowable() {
      return throwable;
    }
  }

  /**
   * Simple logging channel that keeps a running list of all messages received with any provided arguments.
   */
  private class MockLoggingChannel implements LogChannelInterface {
    private List<MessageObject> minimal = new ArrayList<MessageObject>();
    private List<MessageObject> basic = new ArrayList<MessageObject>();
    private List<MessageObject> detailed = new ArrayList<MessageObject>();
    private List<MessageObject> debug = new ArrayList<MessageObject>();
    private List<MessageObject> rowLevel = new ArrayList<MessageObject>();
    private List<MessageObject> error = new ArrayList<MessageObject>();

    @Override
    public String getLogChannelId() {
      return null;
    }

    @Override
    public LogLevel getLogLevel() {
      return null;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
    }

    @Override
    public String getContainerObjectId() {
      return null;
    }

    @Override
    public void setContainerObjectId(String containerObjectId) {
    }

    @Override
    public boolean isBasic() {
      return false;
    }

    @Override
    public boolean isDetailed() {
      return false;
    }

    @Override
    public boolean isDebug() {
      return false;
    }

    @Override
    public boolean isRowLevel() {
      return false;
    }

    @Override
    public boolean isError() {
      return false;
    }

    @Override
    public void logMinimal(String message) {
      minimal.add(new MessageObject(message, null, null));
    }

    @Override
    public void logMinimal(String message, Object... arguments) {
      minimal.add(new MessageObject(message, arguments, null));
    }

    @Override
    public void logBasic(String message) {
      basic.add(new MessageObject(message, null, null));
    }

    @Override
    public void logBasic(String message, Object... arguments) {
      basic.add(new MessageObject(message, arguments, null));
    }

    @Override
    public void logDetailed(String message) {
      detailed.add(new MessageObject(message, null, null));
    }

    @Override
    public void logDetailed(String message, Object... arguments) {
      detailed.add(new MessageObject(message, arguments, null));
    }

    @Override
    public void logDebug(String message) {
      debug.add(new MessageObject(message, null, null));
    }

    @Override
    public void logDebug(String message, Object... arguments) {
      debug.add(new MessageObject(message, arguments, null));
    }

    @Override
    public void logRowlevel(String message) {
      rowLevel.add(new MessageObject(message, null, null));
    }

    @Override
    public void logRowlevel(String message, Object... arguments) {
      rowLevel.add(new MessageObject(message, arguments, null));
    }

    @Override
    public void logError(String message) {
      error.add(new MessageObject(message, null, null));
    }

    @Override
    public void logError(String message, Throwable e) {
      error.add(new MessageObject(message, null, e));
    }

    @Override
    public void logError(String message, Object... arguments) {
      error.add(new MessageObject(message, arguments, null));
    }

    public List<MessageObject> getMinimalMessages() {
      return minimal;
    }

    public List<MessageObject> getBasicMessages() {
      return basic;
    }

    public List<MessageObject> getDetailedMessages() {
      return detailed;
    }

    public List<MessageObject> getDebugMessages() {
      return debug;
    }

    public List<MessageObject> getRowLevelMessages() {
      return rowLevel;
    }

    public List<MessageObject> getErrorMessages() {
      return error;
    }
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_logging_channel() {
    new KettleLogChannelAppender(null, new Log4jKettleLayout());
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_layout() {
    new KettleLogChannelAppender(new MockLoggingChannel(), null);
  }

  @Test
  public void requiresLayout() {
    Appender appender = new KettleLogChannelAppender(new MockLoggingChannel());
    assertTrue(appender.requiresLayout());
  }

  @Test
  public void append_basic() {
    MockLoggingChannel log = new MockLoggingChannel();
    KettleLogChannelAppender appender = new KettleLogChannelAppender(log);
    Logger testLogger = Logger.getLogger(getClass());
    testLogger.setLevel(Level.ALL);
    Layout layout = new Log4jKettleLayout();

    // ALL, INFO, and WARN messages should be interpreted as "basic" messages
    @SuppressWarnings("deprecation")
    LoggingEvent infoEvent = new LoggingEvent("org.test", testLogger, Priority.INFO, "Testing Info", null);
    @SuppressWarnings("deprecation")
    LoggingEvent warnEvent = new LoggingEvent("org.test", testLogger, Priority.WARN, "Testing Warning", null);
    @SuppressWarnings("deprecation")
    LoggingEvent allEvent = new LoggingEvent("org.test", testLogger, Priority.toPriority(Priority.ALL_INT), "Testing All", null);
    appender.doAppend(infoEvent);
    appender.doAppend(warnEvent);
    appender.doAppend(allEvent);

    assertEquals(0, log.getDebugMessages().size());
    assertEquals(0, log.getDetailedMessages().size());
    assertEquals(0, log.getErrorMessages().size());
    assertEquals(0, log.getMinimalMessages().size());
    assertEquals(0, log.getRowLevelMessages().size());

    assertEquals(3, log.getBasicMessages().size());
    assertEquals(layout.format(infoEvent), log.getBasicMessages().get(0).getMessage());
    assertEquals(layout.format(warnEvent), log.getBasicMessages().get(1).getMessage());
    assertEquals(layout.format(allEvent), log.getBasicMessages().get(2).getMessage());
  }

  @Test
  public void append_debug() {
    MockLoggingChannel log = new MockLoggingChannel();
    KettleLogChannelAppender appender = new KettleLogChannelAppender(log);
    Logger testLogger = Logger.getLogger(getClass());
    testLogger.setLevel(Level.ALL);

    // DEBUG messages should be interpreted as "debug" messages
    Layout layout = new Log4jKettleLayout();
    @SuppressWarnings("deprecation")
    LoggingEvent event = new LoggingEvent("org.test", testLogger, Priority.DEBUG, "debug test!", null);

    appender.doAppend(event);
    assertEquals(0, log.getBasicMessages().size());
    assertEquals(0, log.getDetailedMessages().size());
    assertEquals(0, log.getErrorMessages().size());
    assertEquals(0, log.getMinimalMessages().size());
    assertEquals(0, log.getRowLevelMessages().size());

    assertEquals(1, log.getDebugMessages().size());
    assertEquals(layout.format(event), log.getDebugMessages().get(0).getMessage());
  }

  @Test
  public void append_trace() {
    MockLoggingChannel log = new MockLoggingChannel();
    KettleLogChannelAppender appender = new KettleLogChannelAppender(log);
    Logger testLogger = Logger.getLogger(getClass());
    testLogger.setLevel(Level.ALL);

    // TRACE logging events should be interpreted as "row level" messages
    Layout layout = new Log4jKettleLayout();
    @SuppressWarnings("deprecation")
    LoggingEvent event = new LoggingEvent("org.test", testLogger, Priority.toPriority(Level.TRACE_INT), "trace test!", null);

    appender.doAppend(event);
    assertEquals(0, log.getBasicMessages().size());
    assertEquals(0, log.getDetailedMessages().size());
    assertEquals(0, log.getErrorMessages().size());
    assertEquals(0, log.getMinimalMessages().size());
    assertEquals(0, log.getDebugMessages().size());

    assertEquals(1, log.getRowLevelMessages().size());
    assertEquals(layout.format(event), log.getRowLevelMessages().get(0).getMessage());
  }

  @Test
  public void append_off() {
    MockLoggingChannel log = new MockLoggingChannel();
    KettleLogChannelAppender appender = new KettleLogChannelAppender(log);
    Logger testLogger = Logger.getLogger(getClass());
    testLogger.setLevel(Level.ALL);

    // OFF logging events should be interpreted as "minimal" messages
    Layout layout = new Log4jKettleLayout();
    @SuppressWarnings("deprecation")
    LoggingEvent event = new LoggingEvent("org.test", testLogger, Priority.toPriority(Level.OFF_INT), "off test!", null);

    appender.doAppend(event);
    assertEquals(0, log.getBasicMessages().size());
    assertEquals(0, log.getDetailedMessages().size());
    assertEquals(0, log.getErrorMessages().size());
    assertEquals(0, log.getRowLevelMessages().size());
    assertEquals(0, log.getDebugMessages().size());

    assertEquals(1, log.getMinimalMessages().size());
    assertEquals(layout.format(event), log.getMinimalMessages().get(0).getMessage());
  }

  @Test
  public void append_error() {
    MockLoggingChannel log = new MockLoggingChannel();
    KettleLogChannelAppender appender = new KettleLogChannelAppender(log);
    Logger testLogger = Logger.getLogger(getClass());
    testLogger.setLevel(Level.ALL);

    // ERROR and FATAL map to "error" messages
    Layout layout = new Log4jKettleLayout();
    @SuppressWarnings("deprecation")
    LoggingEvent errorEvent1 = new LoggingEvent("org.test", testLogger, Priority.ERROR, "Testing", null);
    Exception errorException = new Exception("something went wrong!");
    @SuppressWarnings("deprecation")
    LoggingEvent errorEvent2 = new LoggingEvent("org.test", testLogger, Priority.ERROR, "Testing", errorException);
    @SuppressWarnings("deprecation")
    LoggingEvent fatalEvent1 = new LoggingEvent("org.test", testLogger, Priority.FATAL, "Testing", null);
    Exception fatalException = new Exception("something went fatally wrong!");
    @SuppressWarnings("deprecation")
    LoggingEvent fatalEvent2 = new LoggingEvent("org.test", testLogger, Priority.FATAL, "Testing", fatalException);

    appender.doAppend(errorEvent1);
    appender.doAppend(errorEvent2);
    appender.doAppend(fatalEvent1);
    appender.doAppend(fatalEvent2);

    assertEquals(0, log.getDebugMessages().size());
    assertEquals(0, log.getDetailedMessages().size());
    assertEquals(0, log.getBasicMessages().size());
    assertEquals(0, log.getMinimalMessages().size());
    assertEquals(0, log.getRowLevelMessages().size());

    assertEquals(4, log.getErrorMessages().size());
    assertEquals(layout.format(errorEvent1), log.getErrorMessages().get(0).getMessage());
    assertEquals(layout.format(errorEvent1), log.getErrorMessages().get(1).getMessage());
    assertEquals(errorException, log.getErrorMessages().get(1).getThrowable());
    assertEquals(layout.format(fatalEvent1), log.getErrorMessages().get(2).getMessage());
    assertEquals(layout.format(fatalEvent2), log.getErrorMessages().get(3).getMessage());
    assertEquals(fatalException, log.getErrorMessages().get(3).getThrowable());
  }
}
