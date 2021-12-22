/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.di.purge;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class PurgeUtilityLog {

  private Logger logger;
  static final String FILE_KEY = "currentFile"; // Intentionally scoped as default
  private OutputStream outputStream;
  private String currentFilePath;
  private String logName;
  private String purgePath;
  private Level logLevel;
  private Appender writeAppender;
  static protected Class layoutClass = PurgeUtilityTextLayout.class;

  /**
   * Constructs this object when when no some method that uses the logger is executed but there is no formal log. When
   * this occurs the log returned is just a simple Log4j logger.
   */
  PurgeUtilityLog() {

  }

  /**
   * Constructs an object that keeps track of additional fields for Log4j logging and writes/formats an html file to the
   * output stream provided.
   * 
   * @param outputStream
   */
  PurgeUtilityLog( OutputStream outputStream, String purgePath, Level logLevel ) {
    this.outputStream = outputStream;
    this.purgePath = purgePath;
    this.logLevel = logLevel;
    init();
  }

  private void init() {
    logName = "PurgeUtilityLog." + getThreadName();
    logger = LogManager.getLogger( logName );

    IPurgeUtilityLayout layout;
    if ( layoutClass == PurgeUtilityHTMLLayout.class ) {
      layout = new PurgeUtilityHTMLLayout( logLevel );
    } else {
      layout = new PurgeUtilityTextLayout( logLevel );
    }
    writeAppender =
      makeAppender("PurgeUtilityLog", new OutputStreamWriter(outputStream), (Layout<Serializable>)layout);
    addAppender(writeAppender, getLogger(), logLevel);
  }

  public Logger getLogger() {
    if ( logger == null ) {
      return LogManager.getLogger( Thread.currentThread().getStackTrace()[4].getClassName() );
    } else {
      return logger;
    }
  }

  /**
   * @return the currentFilePath
   */
  public String getCurrentFilePath() {
    return currentFilePath;
  }

  /**
   * @param currentFilePath
   *          the currentFilePath to set
   */
  public void setCurrentFilePath( String currentFilePath ) {
    this.currentFilePath = currentFilePath;
    if ( currentFilePath != null ) {
      ThreadContext.put( FILE_KEY, currentFilePath );
    }
  }

  /**
   * @return the purgePath
   */
  public String getPurgePath() {
    return purgePath;
  }

  protected void endJob() {
    try {
      outputStream.write( writeAppender.getLayout().getFooter() );
    } catch ( Exception e ) {
      System.out.println( e );
      // Don't try logging a log error.
    }
    removeAppender(writeAppender, getLogger());
  }

  private String getThreadName() {
    return Thread.currentThread().getName();
  }

  public static void addAppender(Appender appender, Logger logger, Level level) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    appender.start();
    config.addAppender(appender);
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    loggerConfig.setLevel(level);
    loggerConfig.addAppender( appender, level, null );
    ctx.updateLoggers();
  }

  public static void removeAppender(Appender appender, Logger logger) {
    appender.stop();
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    loggerConfig.removeAppender( appender.getName() );
    ctx.updateLoggers();
  }

  public static Appender makeAppender(
      String name,
      OutputStreamWriter sw,
      Layout<Serializable> layout)
  {
    return WriterAppender.newBuilder()
        .setName(name)
        .setLayout(layout)
        .setTarget(sw)
        .build();
  }
}
