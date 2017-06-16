/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.apache.log4j.Level;
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
