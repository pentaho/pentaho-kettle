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

package org.pentaho.di.core.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Logs Log4j log events to a Kettle log channel
 */
public class KettleLogChannelAppender extends AppenderSkeleton {
  private LogChannelInterface log;

  public KettleLogChannelAppender(LogChannelInterface log) {
    this(log, new Log4jKettleLayout());
  }

  /**
   * Create an appender that logs to the provided log channel
   *
   * @param log    Log channel to log to
   * @param layout layout to use
   * @throws NullPointerException If {@code log} is null
   */
  public KettleLogChannelAppender(LogChannelInterface log, Layout layout) {
    if (log == null || layout == null) {
      throw new NullPointerException();
    }
    setLayout(layout);
    this.log = log;
  }

  @Override
  protected void append(LoggingEvent event) {
    String s = layout.format(event);

    if (Level.DEBUG.equals(event.getLevel())) {
      log.logDebug(s);
    } else if (Level.ERROR.equals(event.getLevel())
      || Level.FATAL.equals(event.getLevel())) {
      Throwable t = event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable();
      if (t == null) {
        log.logError(s);
      } else {
        log.logError(s, t);
      }
    } else if (Level.TRACE.equals(event.getLevel())) {
      log.logRowlevel(s);
    } else if (Level.OFF.equals(event.getLevel())) {
      log.logMinimal(s);
    } else {
      // ALL, WARN, INFO, or others
      log.logBasic(s);
    }
  }

  @Override
  public boolean requiresLayout() {
    // We may or may not have a layout
    return true;
  }

  @Override
  public void close() {
    // no-op
  }
}
