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

package org.pentaho.hadoop.shim.spi;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.pentaho.hadoop.shim.api.Configuration;

/**
 * Provides a simple interface for working with Pig.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface PigShim extends PentahoHadoopShim {
  /**
   * Possible execution modes for executing a Pig Script
   */
  static enum ExecutionMode {
    LOCAL,
    MAPREDUCE
  }
  
  /**
   * Allows a specific shim to indicate if local execution is supported.
   * @return {@code true} if a Pig script can be executed locally with this shim.
   */
  boolean isLocalExecutionSupported();

  /**
   * Configure the properties. If a configuration is provided merge those into properties as well.
   * 
   * @param properties Properties to configure
   * @param configuration Optional configuration properties to merge into properties
   */
  void configure(Properties properties, Configuration configuration);

  /**
   * Perform parameter substitution on the input stream for a Pig Script.
   * 
   * @param pigScript URL to the Pig Script to perform subsitution on
   * @param paramList Parameter key/value pairs (format: "key=value")
   * @return modified Pig Script contents
   * @throws Exception when an error is encountered reading, writing, or parsing the pig script
   */
  String substituteParameters(URL pigScript, List<String> paramList) throws Exception;
  
  /**
   * Execute a Pig Script in a specific mode with a set of properties.
   * 
   * @param pigScript Pig Script to execute
   * @param mode Execution mode
   * @param properties Properties to configure execution environment
   * @return Array of successful and failed jobs ([successful, failed])
   * @throws Exception when executing the script
   */
  int[] executeScript(String pigScript, ExecutionMode mode, Properties properties) throws Exception;
}
