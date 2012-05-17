/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.job.entries.sqoop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.pentaho.di.core.logging.LogLevel;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SqoopUtilsTest {
  private static final Appender MOCK_APPENDER = new AppenderSkeleton() {
    @Override
    protected void append(LoggingEvent event) {
    }

    @Override
    public boolean requiresLayout() {
      return false;
    }

    @Override
    public void close() {
    }
  };

  @Test
  public void findLogger() {
    String loggerName = "testLogger";
    Log log = LogFactory.getLog(loggerName);
    assertTrue(log instanceof org.apache.commons.logging.impl.Log4JLogger);
    Log4JLogger log4jLogger = (org.apache.commons.logging.impl.Log4JLogger) log;
    Logger logger = log4jLogger.getLogger();
    assertNotNull(logger);

    // This should find the logger we determined to exist above as "logger"
    assertNotNull(SqoopUtils.findLogger(loggerName));
  }

  @Test
  public void attachAppenderTo() {
    Map<String, Level> logLevelCache = new HashMap<String, Level>();
    String loggerName = "testLogger";
    Log log = LogFactory.getLog(loggerName);
    assertTrue(log instanceof org.apache.commons.logging.impl.Log4JLogger);
    Log4JLogger log4jLogger = (org.apache.commons.logging.impl.Log4JLogger) log;
    Logger logger = log4jLogger.getLogger();
    assertNotNull(logger);

    assertFalse(logger.getAllAppenders().hasMoreElements());
    try {
      SqoopUtils.attachAppenderTo(MOCK_APPENDER, LogLevel.DETAILED, logLevelCache, loggerName);
      assertTrue(logger.getAllAppenders().hasMoreElements());
      assertEquals(1, logLevelCache.size());
    } finally {
      logger.removeAllAppenders();
    }
  }

  @Test
  public void removeAppenderFrom() {
    Map<String, Level> logLevelCache = new HashMap<String, Level>();
    String loggerName = "testLogger";
    logLevelCache.put(loggerName, Level.ERROR);

    Log log = LogFactory.getLog(loggerName);
    assertTrue(log instanceof org.apache.commons.logging.impl.Log4JLogger);
    Log4JLogger log4jLogger = (org.apache.commons.logging.impl.Log4JLogger) log;
    Logger logger = log4jLogger.getLogger();
    logger.setLevel(Level.INFO);
    assertNotNull(logger);

    assertFalse(logger.getAllAppenders().hasMoreElements());
    logger.addAppender(MOCK_APPENDER);
    assertTrue(logger.getAllAppenders().hasMoreElements());
    try {
      SqoopUtils.removeAppenderFrom(MOCK_APPENDER, logLevelCache, loggerName);

      // Make sure the appender is gone and the logging level is restored
      assertFalse(logger.getAllAppenders().hasMoreElements());
      assertEquals(Level.ERROR, logger.getLevel());
      assertEquals(0, logLevelCache.size());
    } finally {
      logger.removeAllAppenders();
    }
  }

  @Test(timeout = 5000)
  public void configureConnectionInformation() {
    SqoopConfig config = new SqoopConfig() {
    };

    Configuration c = new Configuration();
    c.set("fs.default.name", "localhost:54310");
    c.set("mapred.job.tracker", "anotherhost:54311");
    SqoopUtils.configureConnectionInformation(config, c);

    assertEquals("localhost", config.getNamenodeHost());
    assertEquals("54310", config.getNamenodePort());

    assertEquals("anotherhost", config.getJobtrackerHost());
    assertEquals("54311", config.getJobtrackerPort());
  }

  @Test
  public void configureConnectionInformation_local() {
    SqoopConfig config = new SqoopConfig() {
    };

    Configuration c = new Configuration();
    c.set("fs.default.name", "local");
    c.set("mapred.job.tracker", "local");
    SqoopUtils.configureConnectionInformation(config, c);

    assertNull(config.getNamenodeHost());
    assertNull(config.getNamenodePort());

    assertNull(config.getJobtrackerHost());
    assertNull(config.getJobtrackerPort());
  }
}
