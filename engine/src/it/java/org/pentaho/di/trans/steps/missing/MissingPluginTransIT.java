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

package org.pentaho.di.trans.steps.missing;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MissingPluginTransIT {

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
  }

  /**
   * Given a transformation having a step which's plugin is missing in current Kettle installation.
   * When this transformation is executed, then execution should fail.
   */
  @Test
  public void testForPluginMissingStep() throws Exception {
    InputStream is = new FileInputStream(
        new File( this.getClass().getResource( "missing_plugin_trans.ktr" ).getFile() ) );
    TransMeta transMeta = new TransMeta( is, null, false, null, null );
    Trans trans = new Trans( transMeta );
    LogChannelInterface log = mock( LogChannelInterface.class );
    trans.setLog( log );

    try {
      trans.prepareExecution( null );
      fail();
    } catch ( KettleException e ) {
      verify( log, times( 1 ) ).logError(
        BaseMessages.getString( Trans.class, "Trans.Log.StepFailedToInit", "JSON Input.0" ) );
    }
  }
}
