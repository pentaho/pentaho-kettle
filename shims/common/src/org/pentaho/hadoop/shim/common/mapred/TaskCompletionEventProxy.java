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

import org.pentaho.hadoop.shim.api.mapred.TaskCompletionEvent;

public class TaskCompletionEventProxy implements TaskCompletionEvent {
  private org.apache.hadoop.mapred.TaskCompletionEvent delegate;

  public TaskCompletionEventProxy(org.apache.hadoop.mapred.TaskCompletionEvent delegate) {
    if (delegate == null) {
      throw new NullPointerException();
    }
    this.delegate = delegate;
  }

  @Override
  public Object getTaskAttemptId() {
    return delegate.getTaskAttemptId();
  }
  
  @Override
  public Status getTaskStatus() {
    org.apache.hadoop.mapred.TaskCompletionEvent.Status s = delegate.getTaskStatus();
    switch(s) {
      case FAILED:
        return Status.FAILED;
      case KILLED:
        return Status.KILLED;
      case OBSOLETE:
        return Status.OBSOLETE;
      case SUCCEEDED:
        return Status.SUCCEEDED;
      case TIPFAILED:
        return Status.TIPFAILED;
      default:
        throw new IllegalStateException("unknown status: " + s);
    }
  }

  @Override
  public int getEventId() {
    return delegate.getEventId();
  }
}
