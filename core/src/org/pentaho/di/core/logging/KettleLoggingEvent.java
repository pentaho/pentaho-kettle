/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.logging;

public class KettleLoggingEvent {

  private final Object subjectOfLog;
  private final LogLevel channelLogLevel;
  private Object message;

  public long timeStamp;

  private LogLevel level;

  public KettleLoggingEvent() {
    this( null, System.currentTimeMillis(), LogLevel.BASIC, LogLevel.BASIC, null);
  }

  public KettleLoggingEvent( Object message, long timeStamp, LogLevel level ) {
    this(message, timeStamp, level, LogLevel.BASIC, null);
  }

  public KettleLoggingEvent(Object message, long timeStamp, LogLevel level, LogLevel channelLogLevel, Object subjectOfLog) {
    this.message = message;
    this.timeStamp = timeStamp;
    this.level = level;
    this.channelLogLevel = channelLogLevel;
    this.subjectOfLog = subjectOfLog;
  }

  public LogLevel getChannelLogLevel() {
    return channelLogLevel;
  }

  public Object getSubjectOfLog() {
    return subjectOfLog;
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
