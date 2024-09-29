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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.apache.logging.log4j.Level;
import org.junit.Test;

public class PurgeUtilityLoggerTest {

  final static String PURGE_PATH = "purgePath";

  @SuppressWarnings( "null" )
  @Test
  public void textLoggerTest() {
    OutputStream out = new ByteArrayOutputStream();
    PurgeUtilityLog.layoutClass = PurgeUtilityTextLayout.class;
    PurgeUtilityLogger.createNewInstance( out, PURGE_PATH, Level.DEBUG );
    PurgeUtilityLogger logger = PurgeUtilityLogger.getPurgeUtilityLogger();
    logger.setCurrentFilePath( "file1" );
    logger.info( "info on 1st file" );
    logger.setCurrentFilePath( "file2" );
    logger.debug( "debug on file2" );
    String nullString = null;
    try {
      nullString.getBytes(); // Generate an error to log
    } catch ( Exception e ) {
      logger.error( e );
    }

    logger.endJob();

    String logOutput = out.toString();

    Assert.assertTrue( logOutput.contains( PURGE_PATH ) );
    Assert.assertTrue( logOutput.contains( "info on 1st file" ) );
    Assert.assertTrue( logOutput.contains( "debug on file2" ) );
    Assert.assertTrue( logOutput.contains( "java.lang.NullPointerException" ) );
  }

  @SuppressWarnings( "null" )
  @Test
  public void htmlLoggerTest() {
    OutputStream out = new ByteArrayOutputStream();
    PurgeUtilityLog.layoutClass = PurgeUtilityHTMLLayout.class;
    PurgeUtilityLogger.createNewInstance( out, PURGE_PATH, Level.DEBUG );
    PurgeUtilityLogger logger = PurgeUtilityLogger.getPurgeUtilityLogger();
    logger.setCurrentFilePath( "file1" );
    logger.info( "info on 1st file" );
    logger.setCurrentFilePath( "file2" );
    logger.debug( "debug on file2" );
    String nullString = null;
    try {
      nullString.getBytes(); // Generate an error to log
    } catch ( Exception e ) {
      logger.error( e );
    }

    logger.endJob();

    String logOutput = out.toString();

    Assert.assertTrue( logOutput.contains( PURGE_PATH ) );
    Assert.assertTrue( logOutput.contains( "info on 1st file" ) );
    Assert.assertTrue( logOutput.contains( "debug on file2" ) );
    Assert.assertTrue( logOutput.contains( "java.lang.NullPointerException" ) );
  }

}
