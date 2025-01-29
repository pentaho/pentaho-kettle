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

package com.pentaho.di.purge;

import org.apache.logging.log4j.core.LogEvent;

/**
 * Provides a common interface for implementation of the Purge Utility Log File. The implementation should extend
 * <code>org.apache.logging.log4j.core.Layout</code>
 * 
 * @author tkafalas
 * 
 */
public interface IPurgeUtilityLayout {
  /**
   * Set the title of the log file
   */
  void setTitle( String title );

  /**
   * Returns the current value of the <b>Title</b> option.
   */
  String getTitle();

  /**
   * Returns the content type output by this layout, i.e "text/html".
   */
  String getContentType();

  /**
   * Format the log line for output
   */
  String format( LogEvent event );

  /**
   * return <code>false</code> if the layout processes a throwable in the logging event, returns <code>true</code>
   * otherwise.
   */
  boolean ignoresThrowable();
}
