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

public class KettleLoggingEvent {

  private Object message;

  public long timeStamp;

  private LogLevel level;

  public KettleLoggingEvent() {
    this( null, System.currentTimeMillis(), LogLevel.BASIC );
  }

  public KettleLoggingEvent( Object message, long timeStamp, LogLevel level ) {
    super();
    this.message = message;
    this.timeStamp = timeStamp;
    this.level = level;
  }

  public Object getMessage() {
    return message;
  }

  public void setMessage( Object message ) {
    this.message = message;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp( long timeStamp ) {
    this.timeStamp = timeStamp;
  }

  public LogLevel getLevel() {
    return level;
  }

  public void setLevel( LogLevel level ) {
    this.level = level;
  }

}
