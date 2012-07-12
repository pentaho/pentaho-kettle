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

import org.apache.hadoop.mapred.TaskAttemptID;
import org.pentaho.hadoop.shim.api.mapred.RunningJob;
import org.pentaho.hadoop.shim.api.mapred.TaskCompletionEvent;

public class RunningJobProxy implements RunningJob {
  org.apache.hadoop.mapred.RunningJob delegate;
  
  public RunningJobProxy(org.apache.hadoop.mapred.RunningJob delegate) {
    if (delegate == null) {
      throw new NullPointerException();
    }
    this.delegate = delegate;
  }

  @Override
  public boolean isComplete() throws IOException {
    return delegate.isComplete();
  }
  
  @Override
  public void killJob() throws IOException {
    delegate.killJob(); 
  }
  
  @Override
  public boolean isSuccessful() throws IOException {
    return delegate.isSuccessful();
  }
  
  @Override
  public TaskCompletionEvent[] getTaskCompletionEvents(int startIndex) throws IOException {
    org.apache.hadoop.mapred.TaskCompletionEvent[] events = delegate.getTaskCompletionEvents(startIndex);
    TaskCompletionEvent[] wrapped = new TaskCompletionEvent[events.length];

    for (int i = 0; i < wrapped.length; i ++) {
      wrapped[i] = new TaskCompletionEventProxy(events[i]);
    }
    
    return wrapped;
  }
  
  @Override
  public String[] getTaskDiagnostics(Object taskAttemptId) throws IOException {
    @SuppressWarnings("deprecation")
    TaskAttemptID id = (TaskAttemptID) taskAttemptId;
    return delegate.getTaskDiagnostics(id);
  }
  
  @Override
  public float setupProgress() throws IOException {
    return delegate.setupProgress();
  }
  
  @Override
  public float mapProgress() throws IOException {
    return delegate.mapProgress();
  }
  
  @Override
  public float reduceProgress() throws IOException {
    return delegate.reduceProgress();
  }
}
