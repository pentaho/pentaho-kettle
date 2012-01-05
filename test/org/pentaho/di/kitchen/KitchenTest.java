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

package org.pentaho.di.kitchen;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.pan.CommandLineOption;

/**
 * Kitchen Tests
 * 
 * @author jganoff
 */
public class KitchenTest extends TestCase {

  public void testArgumentMaxLogLines_valid() throws KettleException {
    final String maxLogLinesArg = "50"; //$NON-NLS-1$
    int expected = 50;
    CommandLineOption opt = new CommandLineOption("maxloglines", null, new StringBuffer(maxLogLinesArg)); //$NON-NLS-1$
    int maxLogLines = Kitchen.parseIntArgument(opt, 0);
    assertEquals(expected, maxLogLines);
  }

  public void testArgumentMaxLogLines_invalid() {
    final String maxLogLinesArg = "fifty"; //$NON-NLS-1$
    CommandLineOption opt = new CommandLineOption("maxloglines", null, new StringBuffer(maxLogLinesArg)); //$NON-NLS-1$
    try {
      Kitchen.parseIntArgument(opt, 0);
      fail("Argument should not be parsable"); //$NON-NLS-1$
    } catch (KettleException expected) {
      assertTrue(
          "Error is not as expected: " + expected.getMessage(), expected.getMessage().contains("ERROR: maxloglines")); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void testArgumentMaxLogTimeout_valid() throws KettleException {
    final String maxLogTimeoutArg = "658"; //$NON-NLS-1$
    int expected = 658;
    CommandLineOption opt = new CommandLineOption("maxlogtimeout", null, new StringBuffer(maxLogTimeoutArg)); //$NON-NLS-1$
    int maxLogLines = Kitchen.parseIntArgument(opt, 0);
    assertEquals(expected, maxLogLines);
  }

  public void testArgumentMaxLogTimeout_invalid() {
    final String maxLogTimeoutArg = "sixfiftyeight"; //$NON-NLS-1$
    CommandLineOption opt = new CommandLineOption("maxlogtimeout", null, new StringBuffer(maxLogTimeoutArg)); //$NON-NLS-1$
    try {
      Kitchen.parseIntArgument(opt, 0);
      fail("Argument should not be parsable"); //$NON-NLS-1$
    } catch (KettleException expected) {
      assertTrue(
          "Error is not as expected: " + expected.getMessage(), expected.getMessage().contains("ERROR: maxlogtimeout")); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void testConfigureLogging() throws KettleException {
    final String maxLogTimeoutArg = "600"; //$NON-NLS-1$
    // Init Kettle Environment so the default CentralLogStore is initialized
    KettleEnvironment.init();
    // Change the max nr of lines
    final int maxNrLines = CentralLogStore.getAppender().getMaxNrLines() + 50;
    final String maxLogLinesArg = String.valueOf(maxNrLines);
    CommandLineOption maxLogLinesOption = new CommandLineOption("maxloglines", null, new StringBuffer(maxLogLinesArg)); //$NON-NLS-1$
    CommandLineOption maxLogTimeoutOption = new CommandLineOption(
        "maxlogtimeout", null, new StringBuffer(maxLogTimeoutArg)); //$NON-NLS-1$
    // Configure logging with the new options
    Kitchen.configureLogging(maxLogLinesOption, maxLogTimeoutOption);
    assertEquals(maxNrLines, CentralLogStore.getAppender().getMaxNrLines());
  }
}
