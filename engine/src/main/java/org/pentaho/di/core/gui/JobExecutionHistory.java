/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.gui;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for keeping track of the execution of a job. It does this by keeping a Map in memory. This
 * map has the Unique job entry name has key. The value stored in there includes: - The execution state (waiting,
 * running, finished) - The
 *
 * @author matt
 *
 */
public class JobExecutionHistory {
  private Map<String, JobEntryExecutionResult> executionMap;

  public JobExecutionHistory() {
    this.executionMap = new HashMap<String, JobEntryExecutionResult>();
  }

  /**
   * @return the executionMap
   */
  public Map<String, JobEntryExecutionResult> getExecutionMap() {
    return executionMap;
  }

  /**
   * @param executionMap
   *          the executionMap to set
   */
  public void setExecutionMap( Map<String, JobEntryExecutionResult> executionMap ) {
    this.executionMap = executionMap;
  }

}
