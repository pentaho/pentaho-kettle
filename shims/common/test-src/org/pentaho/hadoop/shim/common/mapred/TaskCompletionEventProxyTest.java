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

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.hadoop.mapred.TaskID;
import org.junit.Test;
import org.pentaho.hadoop.shim.api.mapred.TaskCompletionEvent;

@SuppressWarnings("deprecation")
public class TaskCompletionEventProxyTest {

  @Test(expected = NullPointerException.class)
  public void instantiation_null() {
    new TaskCompletionEventProxy(null);
  }

  @Test
  public void getTaskAttemptId() {
    final TaskAttemptID id = new TaskAttemptID(new TaskID(), 0);
    org.apache.hadoop.mapred.TaskCompletionEvent delegate = new org.apache.hadoop.mapred.TaskCompletionEvent() {
      public org.apache.hadoop.mapred.TaskAttemptID getTaskAttemptId() {
        return id;
      };
    };
    TaskCompletionEventProxy proxy = new TaskCompletionEventProxy(delegate);

    assertEquals(id, proxy.getTaskAttemptId());
  }

  @Test
  public void getTaskStatus() {
    final AtomicReference<org.apache.hadoop.mapred.TaskCompletionEvent.Status> status = new AtomicReference<org.apache.hadoop.mapred.TaskCompletionEvent.Status>();

    org.apache.hadoop.mapred.TaskCompletionEvent delegate = new org.apache.hadoop.mapred.TaskCompletionEvent() {
      public Status getTaskStatus() {
        return status.get();
      };
    };
    TaskCompletionEventProxy proxy = new TaskCompletionEventProxy(delegate);

    status.set(org.apache.hadoop.mapred.TaskCompletionEvent.Status.FAILED);
    assertEquals(TaskCompletionEvent.Status.FAILED, proxy.getTaskStatus());
    status.set(org.apache.hadoop.mapred.TaskCompletionEvent.Status.KILLED);
    assertEquals(TaskCompletionEvent.Status.KILLED, proxy.getTaskStatus());
    status.set(org.apache.hadoop.mapred.TaskCompletionEvent.Status.OBSOLETE);
    assertEquals(TaskCompletionEvent.Status.OBSOLETE, proxy.getTaskStatus());
    status.set(org.apache.hadoop.mapred.TaskCompletionEvent.Status.SUCCEEDED);
    assertEquals(TaskCompletionEvent.Status.SUCCEEDED, proxy.getTaskStatus());
    status.set(org.apache.hadoop.mapred.TaskCompletionEvent.Status.TIPFAILED);
    assertEquals(TaskCompletionEvent.Status.TIPFAILED, proxy.getTaskStatus());
  }

  @Test
  public void getEventId() {
    final int id = 12332;
    org.apache.hadoop.mapred.TaskCompletionEvent delegate = new org.apache.hadoop.mapred.TaskCompletionEvent() {
      public int getEventId() {
        return id;
      }
    };
    TaskCompletionEventProxy proxy = new TaskCompletionEventProxy(delegate);

    assertEquals(id, proxy.getEventId());
  }

}
