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

package org.pentaho.hadoop.shim.common.mapred;

import java.io.IOException;

import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.hadoop.mapred.TaskCompletionEvent;

public class MockRunningJob implements RunningJob {

  @Override
  public float cleanupProgress() throws IOException {
    return 0;
  }

  @Override
  public Counters getCounters() throws IOException {
    return null;
  }

  @Override
  public JobID getID() {
    return null;
  }

  @Override
  public String getJobFile() {
    return null;
  }

  @Override
  @Deprecated
  public String getJobID() {
    return null;
  }

  @Override
  public String getJobName() {
    return null;
  }

  @Override
  public int getJobState() throws IOException {
    return 0;
  }

  @Override
  public TaskCompletionEvent[] getTaskCompletionEvents(int arg0) throws IOException {
    return null;
  }

  @Override
  public String[] getTaskDiagnostics(TaskAttemptID arg0) throws IOException {
    return null;
  }

  @Override
  public String getTrackingURL() {
    return null;
  }

  @Override
  public boolean isComplete() throws IOException {
    return false;
  }

  @Override
  public boolean isSuccessful() throws IOException {
    return false;
  }

  @Override
  public void killJob() throws IOException {
  }

  @Override
  public void killTask(TaskAttemptID arg0, boolean arg1) throws IOException {
  }

  @Override
  @Deprecated
  public void killTask(String arg0, boolean arg1) throws IOException {
  }

  @Override
  public float mapProgress() throws IOException {
    return 0;
  }

  @Override
  public float reduceProgress() throws IOException {
    return 0;
  }

  @Override
  public void setJobPriority(String arg0) throws IOException {
  }

  @Override
  public float setupProgress() throws IOException {
    return 0;
  }

  @Override
  public void waitForCompletion() throws IOException {
  }

  // Omit @Override since not all Hadoop versions define this method 
  public String getFailureInfo() throws IOException {
    return null;
  }
}
