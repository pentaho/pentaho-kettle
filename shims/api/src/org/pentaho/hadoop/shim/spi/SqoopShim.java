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

import org.pentaho.hadoop.shim.api.Configuration;

/**
 * Provides a simple abstraction for executing a Sqoop tool.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface SqoopShim {
  /**
   * Execute Sqoop with the provided arguments and configuration. This entry-point 
   * parses the correct SqoopTool to use from the args.
   * 
   * @see org.apache.sqoop.Sqoop#runTool(String[], org.apache.hadoop.conf.Configuration)
   */
  int runTool(String[] args, Configuration c);
}
