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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.pentaho.di.core.logging.KettleLogChannelAppender;
import org.pentaho.di.core.logging.LogLevel;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

public class SqoopUtils {
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

  public static void attachAppenderTo(Appender appender, LogLevel logLevel, Map<String, Level> logLevelCache, String... logNames) {
    for (String logName : logNames) {
      Logger logger = findLogger(logName);
      logger.addAppender(appender);
      // Update logger level to match our logging level
      Level level = KettleLogChannelAppender.LOG_LEVEL_MAP.get(logLevel);
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
   * Configure a {@link SqoopConfig}'s Namenode and Jobtracker connection information based off a Hadoop Configuration's
   * settings. These properties are parsed from {@code fs.default.name} and {@code mapred.job.tracker} properties.
   *
   * @param config Sqoop configuration to update
   * @param c      Hadoop configuration to parse connection information from
   */
  public static void configureConnectionInformation(SqoopConfig config, Configuration c) {
    URI namenode = FileSystem.getDefaultUri(c);
    if (namenode != null) {
      config.setNamenodeHost(namenode.getHost());
      if (namenode.getPort() != -1) {
        config.setNamenodePort(String.valueOf(namenode.getPort()));
      }
    }

    if (!"local".equals(c.get("mapred.job.tracker", "local"))) {
      InetSocketAddress jobtracker = JobTracker.getAddress(c);
      config.setJobtrackerHost(jobtracker.getHostName());
      config.setJobtrackerPort(String.valueOf(jobtracker.getPort()));
    }
  }
}
