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
