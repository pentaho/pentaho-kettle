/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.Map;

/**
 * User: RFellows
 * Date: 6/7/12
 */
public class JobEntryUtils {

  public static Logger findLogger(String logName) {
    Log log = LogFactory.getLog(logName);
    if (log instanceof org.apache.commons.logging.impl.Log4JLogger) {
      Logger logger = ((org.apache.commons.logging.impl.Log4JLogger) log).getLogger();
      if (logger == null) {
        throw new IllegalArgumentException("Logger does not exist for log: " + logName);
      }
      return logger;
    } else if (log == null) {
      throw new IllegalArgumentException("Unknown log name: " + logName);
    } else {
      throw new IllegalArgumentException("Unsupported logging type: " + log.getClass());
    }
  }

  @SuppressWarnings("deprecation")
  public static void attachAppenderTo(Appender appender, LogLevel logLevel, Map<String, Level> logLevelCache, String... logNames) {
    for (String logName : logNames) {
      Logger logger = findLogger(logName);
      logger.addAppender(appender);
      // Update logger level to match our logging level
      Level level = org.pentaho.di.core.logging.KettleLogChannelAppender.LOG_LEVEL_MAP.get(logLevel);
      if (level != null) {
        // Cache the original level so we can reset it when we're done
        logLevelCache.put(logger.getName(), logger.getLevel());
        logger.setLevel(level);
      }
    }
  }

  public static void removeAppenderFrom(Appender appender, Map<String, Level> logLevelCache, String... logNames) {
    for (String logName : logNames) {
      Logger logger = findLogger(logName);
      logger.removeAppender(appender);
      // Reset logger level if it was changed
      if (logLevelCache.containsKey(logger.getName())) {
        logger.setLevel(logLevelCache.get(logger.getName()));
        logLevelCache.remove(logger.getName());
      }
    }
    appender.close();
  }

  /**
   * @return {@code true} if {@link Boolean#parseBoolean(String)} returns {@code true} for {@link #isBlockingExecution()}
   */
  /**
   * Determine if the string equates to {@link Boolean#TRUE} after performing a variable substitution.
   *
   * @param s             String-encoded boolean value or variable expression
   * @param variableSpace Context for variables so we can substitute {@code s}
   * @return the value returned by {@link Boolean#parseBoolean(String) Boolean.parseBoolean(s)} after substitution
   */
  public static boolean asBoolean(String s, VariableSpace variableSpace) {
    String value = variableSpace.environmentSubstitute(s);
    return Boolean.parseBoolean(value);
  }

  /**
   * Parse the string as a {@link Long} after variable substitution.
   *
   * @param s             String-encoded {@link Long} value or variable expression that should resolve to a {@link Long} value
   * @param variableSpace Context for variables so we can substitute {@code s}
   * @return the value returned by {@link Long#parseLong(String, int) Long.parseLong(s, 10)} after substitution
   */
  public static Long asLong(String s, VariableSpace variableSpace) {
    String value = variableSpace.environmentSubstitute(s);
    return value == null ? null : Long.valueOf(value, 10);
  }

}
