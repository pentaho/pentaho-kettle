/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
