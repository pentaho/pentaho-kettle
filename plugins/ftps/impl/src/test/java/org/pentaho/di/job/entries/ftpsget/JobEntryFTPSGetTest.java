/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ftpsget;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.KettleLogStore;

public class JobEntryFTPSGetTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  /**
   * PDI-6868, attempt to set binary mode is after the connection.connect() succeeded.
   * @throws Exception
   */
  @Test
  public void testBinaryModeSetAfterConnectionSuccess() throws Exception {
    JobEntryFTPSGet job = new JobEntryFTPSGetCustom();
    FTPSConnection connection = Mockito.mock( FTPSConnection.class );
    InOrder inOrder = Mockito.inOrder( connection );
    job.buildFTPSConnection( connection );
    inOrder.verify( connection ).connect();
    inOrder.verify( connection ).setBinaryMode( Mockito.anyBoolean() );
  }

  class JobEntryFTPSGetCustom extends JobEntryFTPSGet {
    @Override
    public boolean isBinaryMode() {
      return true;
    }
  }
}
