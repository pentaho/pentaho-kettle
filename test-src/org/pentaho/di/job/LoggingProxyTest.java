/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.job;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.pentaho.di.job.LoggingProxy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link org.pentaho.di.job.LoggingProxy}.
 */
public class LoggingProxyTest {

  /**
   * Make sure the logging proxy can wrap a {@link java.io.PrintStream}, e.g. {@link System#out}, and log all
   * messages passed to {@link java.io.PrintStream#print(String)} get logged to the provided {@link Logger}
   */
  @Test
  public void wrapSysOut() {
    Logger logger = Logger.getLogger(getClass());
    final List<LoggingEvent> receivedEvents = new ArrayList<LoggingEvent>();
    logger.addAppender(new AppenderSkeleton() {
      @Override
      protected void append(LoggingEvent event) {
        receivedEvents.add(event);
      }

      @Override
      public boolean requiresLayout() {
        return false;
      }

      @Override
      public void close() {
      }
    });
    logger.setLevel(Level.TRACE);

    LoggingProxy proxy = new LoggingProxy(System.out, logger, Level.INFO);

    assertEquals(System.out, proxy.getWrappedStream());

    String expectedString = "testing";
    proxy.print(expectedString);
    assertEquals(1, receivedEvents.size());
    assertEquals(expectedString, receivedEvents.get(0).getMessage());
    assertEquals(Level.INFO, receivedEvents.get(0).getLevel());
  }
}
