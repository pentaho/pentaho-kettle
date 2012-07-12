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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class RunningJobProxyTest {

  @Test(expected = NullPointerException.class)
  public void instantiate_null_delegate() {
    new RunningJobProxy(null);
  }

  @Test
  public void isComplete() throws IOException {
    final AtomicBoolean called = new AtomicBoolean(false);
    RunningJobProxy proxy = new RunningJobProxy(new MockRunningJob() {
      @Override
      public boolean isComplete() throws IOException {
        called.set(true);
        return true;
      }
    });

    assertTrue(proxy.isComplete());
    assertTrue(called.get());
  }

  @Test
  public void killJob() throws IOException {
    final AtomicBoolean called = new AtomicBoolean(false);
    RunningJobProxy proxy = new RunningJobProxy(new MockRunningJob() {
      @Override
      public void killJob() throws IOException {
        called.set(true);
      }
    });

    proxy.killJob();

    assertTrue(called.get());
  }

}
