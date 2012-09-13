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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.hadoopjobexecutor.NoExitSecurityManager.NoExitSecurityException;

public class NoExitSecurityManagerTest {

  @Test
  public void checkExit_blocked_thread() {
    NoExitSecurityManager nesm = new NoExitSecurityManager(System.getSecurityManager());
    nesm.addBlockedThread(Thread.currentThread());
    int status = 1;
    try {
      nesm.checkExit(status);
      fail("expected exception");
    } catch (NoExitSecurityException ex) {
      assertEquals(status, ex.getStatus());
      assertEquals(BaseMessages.getString(NoExitSecurityManager.class, "NoSystemExit"), ex.getMessage());
    }
  }

  @Test
  public void checkExit_nonblocked_thread() {
    NoExitSecurityManager nesm = new NoExitSecurityManager(System.getSecurityManager());
    try {
      nesm.checkExit(1);      
    } catch (NoExitSecurityException ex) {
      fail("Should have been able to exit");
    }
  }
}
