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

import org.apache.commons.logging.Log;

/**
 * Below is a sample of how to use this class to generate a log file.
 * 
 * <ol>
 * <li>Initiate the job my calling the factoty method <code>PurgeUtilityLogger.createNewInstance(OutputStream,
 * purgePath)</code> to instantiate the log and supply it with an output stream to hold the log and the root folder of
 * the purge.</li>
 * <li>Call setCurrentFilePath() each time you start processing a new import file. The log shows the file being imported
 * so it must be registered.</li>
 * <li>Call endJob() when the import is done to log the finish and release resources. If the the import terminates
 * abnormally this call should be in the finally block.</li>
 * </ol>
 * 
 * To get a logger from any method use PurgeUtilitylogger.getLogger() which will return the IPurgeUtilityLog defined
 * above, or create a new phsuedo instance, if the code that's running is not part of the purgeUtility run.
 * 
 * @author TKafalas
 * 
 */
public interface IPurgeUtilityLogger extends Log {

  /**
   * Registers the file being worked on. Each log entry will list the path to the file being processed. Call this method
   * just before processing the next file. It will automatically post a "Start File Import" entry in the log.
   * 
   * @param currentFilePath
   *          path to file being imported
   */
  void setCurrentFilePath( String currentFilePath );

  /**
   * Makes an "End Import Job" log entry and releases memory associated with this log.
   */
  void endJob();

  /**
   * Log informational data. Should be called when the starting a new file and when finishing that file.
   * 
   * @param s
   *          The information message to be logged.
   */
  void info( String s );

  /**
   * Log an error.
   * 
   * @param s
   *          The Error message to be logged.
   */
  void error( String s );

  /**
   * Log debug information
   * 
   * @param s
   *          The debug message to be logged
   */
  void debug( String s );

  /**
   * Log error information
   * 
   * @param e
   *          The exception to be logged.
   */
  void error( Exception e );

  /**
   * Allows a class to check if an ImportLogger has been instantiated for the current thread.
   * 
   * @return true if the logger is present.
   */
  boolean hasLogger();
}
