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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.PrintStream;

/**
 * Redirect all String-based logging for a {@link PrintStream} to a Log4j logger at a specified logging level.
 */
public class LoggingProxy extends PrintStream {
  private PrintStream wrappedStream;
  private Logger logger;
  private Level level;

  /**
   * Create a new Logging proxy that will log all {@link String}s printed with {@link #print(String)} to the logger
   * using the level provided.
   *
   * @param stream Stream to redirect output for
   * @param logger Logger to log to
   * @param level  Level to log messages at
   */
  public LoggingProxy(PrintStream stream, Logger logger, Level level) {
    super(stream);
    wrappedStream = stream;
    this.logger = logger;
    this.level = level;
  }

  @Override
  public void print(String s) {
    wrappedStream.print(s);
    logger.log(level, s);
  }

  /**
   * @return the steam this proxy wraps
   */
  public PrintStream getWrappedStream() {
    return wrappedStream;
  }
}
