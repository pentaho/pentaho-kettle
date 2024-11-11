/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package com.pentaho.di.purge;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.pentaho.platform.api.util.LogUtil;

import org.slf4j.MDC;

public class PurgeUtilityLog {

  private Logger logger;
  static final String FILE_KEY = "currentFile"; // Intentionally scoped as default
  private OutputStream outputStream;
  private String currentFilePath;
  private String logName;
  private String purgePath;
  private Level logLevel;
  private Appender appender;
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
    LogUtil.setLevel( logger, logLevel );
    IPurgeUtilityLayout layout;
    if ( layoutClass == PurgeUtilityHTMLLayout.class ) {
      layout = new PurgeUtilityHTMLLayout( logLevel );
    } else {
      layout = new PurgeUtilityTextLayout( logLevel );
    }
    layout.setTitle( "Purge Utility Log" );
    appender =
            LogUtil.makeAppender( logName, new OutputStreamWriter( outputStream, Charset.forName( "utf-8" ) ), (Layout) layout);
    LogUtil.addAppender( appender, logger, logLevel );
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
      outputStream.write( appender.getLayout().getFooter() );
    } catch ( Exception e ) {
      System.out.println( e );
      // Don't try logging a log error.
    }
    LogUtil.removeAppender(appender, logger);
  }

  private String getThreadName() {
    return Thread.currentThread().getName();
  }

}
