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

package org.pentaho.di.core.logging;

/**
 * This enumeration describes the logging status in a logging table for transformations and jobs.
 *
 * @author matt
 *
 */
public enum LogStatus {

  START( "start" ), END( "end" ), STOP( "stop" ), ERROR( "error" ), RUNNING( "running" ), PAUSED( "paused" );

  private String status;

  private LogStatus( String status ) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return status;
  }

  public boolean equals( LogStatus logStatus ) {
    return status.equalsIgnoreCase( logStatus.status );
  }

  /**
   * Find the LogStatus based on the string description of the status.
   *
   * @param status
   *          the status string to search for
   * @return the LogStatus or null if none is found
   */
  public static LogStatus findStatus( String status ) {
    for ( LogStatus logStatus : values() ) {
      if ( logStatus.status.equalsIgnoreCase( status ) ) {
        return logStatus;
      }
    }
    return null;
  }
}
