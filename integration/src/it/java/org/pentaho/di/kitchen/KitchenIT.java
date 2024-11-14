/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.kitchen;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.pan.CommandLineOption;

/**
 * Kitchen Tests
 *
 * @author jganoff
 */
public class KitchenIT extends TestCase {

  public void testArgumentMaxLogLines_valid() throws KettleException {
    final String maxLogLinesArg = "50";
    int expected = 50;
    CommandLineOption opt = new CommandLineOption( "maxloglines", null, new StringBuilder( maxLogLinesArg ) );
    int maxLogLines = Kitchen.parseIntArgument( opt, 0 );
    assertEquals( expected, maxLogLines );
  }

  public void testArgumentMaxLogLines_invalid() {
    final String maxLogLinesArg = "fifty";
    CommandLineOption opt = new CommandLineOption( "maxloglines", null, new StringBuilder( maxLogLinesArg ) );
    try {
      Kitchen.parseIntArgument( opt, 0 );
      fail( "Argument should not be parsable" );
    } catch ( KettleException expected ) {
      assertTrue( "Error is not as expected: " + expected.getMessage(), expected.getMessage().contains(
        "ERROR: maxloglines" ) );
    }
  }

  public void testArgumentMaxLogTimeout_valid() throws KettleException {
    final String maxLogTimeoutArg = "658";
    int expected = 658;
    CommandLineOption opt = new CommandLineOption( "maxlogtimeout", null, new StringBuilder( maxLogTimeoutArg ) );
    int maxLogLines = Kitchen.parseIntArgument( opt, 0 );
    assertEquals( expected, maxLogLines );
  }

  public void testArgumentMaxLogTimeout_invalid() {
    final String maxLogTimeoutArg = "sixfiftyeight";
    CommandLineOption opt = new CommandLineOption( "maxlogtimeout", null, new StringBuilder( maxLogTimeoutArg ) );
    try {
      Kitchen.parseIntArgument( opt, 0 );
      fail( "Argument should not be parsable" );
    } catch ( KettleException expected ) {
      assertTrue( "Error is not as expected: " + expected.getMessage(), expected.getMessage().contains(
        "ERROR: maxlogtimeout" ) );
    }
  }

  public void testConfigureLogging() throws KettleException {
    final String maxLogTimeoutArg = "600";
    // Init Kettle Environment so the default CentralLogStore is initialized
    KettleEnvironment.init();
    // Change the max nr of lines
    final int maxNrLines = KettleLogStore.getAppender().getMaxNrLines() + 50;
    final String maxLogLinesArg = String.valueOf( maxNrLines );
    CommandLineOption maxLogLinesOption =
      new CommandLineOption( "maxloglines", null, new StringBuilder( maxLogLinesArg ) );
    CommandLineOption maxLogTimeoutOption =
      new CommandLineOption( "maxlogtimeout", null, new StringBuilder( maxLogTimeoutArg ) );
    // Configure logging with the new options
    Kitchen.configureLogging( maxLogLinesOption, maxLogTimeoutOption );
    assertEquals( maxNrLines, KettleLogStore.getAppender().getMaxNrLines() );
  }
}
