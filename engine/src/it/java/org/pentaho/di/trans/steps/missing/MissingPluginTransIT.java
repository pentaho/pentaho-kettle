/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.missing;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
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
      verify( log, times( 1 ) ).logError( "Step [JSON Input.0] failed to initialize!" );
    }
  }
}
