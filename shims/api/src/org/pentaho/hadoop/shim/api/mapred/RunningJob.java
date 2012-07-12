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

package org.pentaho.hadoop.shim.api.mapred;

import java.io.IOException;

/**
 * An abstraction for {@link org.apache.hadoop.mapred.RunningJob}. This is used
 * to poll for a MapReduce job's status.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface RunningJob {

  /**
   * Check if the job is completed.
   * 
   * @return {@code true} if the job has completed
   * @throws IOException
   */
  boolean isComplete() throws IOException;

  /**
   * Kill a running job. This blocks until all tasks of the job have been killed
   * as well.
   * 
   * @throws IOException
   */
  void killJob() throws IOException;

  /**
   * Check if a job completed successfully.
   * 
   * @return {@code true} if the job succeeded.
   * @throws IOException
   */
  boolean isSuccessful() throws IOException;

  /**
   * Get a list of events indicating success/failure of underlying tasks.
   * 
   * @param startIndex offset/index to start fetching events from
   * @return an array of events
   * @throws IOException
   */
  TaskCompletionEvent[] getTaskCompletionEvents(int startIndex) throws IOException;

  /**
   * Retrieve the diagnostic messages for a given task attempt.
   * 
   * @param taskAttemptId Identifier of the task
   * @return an array of diagnostic messages for the task attempt with the id provided.
   * @throws IOException
   */
  String[] getTaskDiagnostics(Object taskAttemptId) throws IOException;

  /**
   * The progress of the job's setup tasks.
   * 
   * @return progress percentage
   * @throws IOException
   */
  float setupProgress() throws IOException;

  /**
   * The progress of the job's map tasks.
   * 
   * @return progress percentage
   * @throws IOException
   */
  float mapProgress() throws IOException;

  /**
   * The progress of the job's reduce tasks.
   * 
   * @return progress percentage
   * @throws IOException
   */
  float reduceProgress() throws IOException;
}
