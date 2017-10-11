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
package org.pentaho.di.trans.steps.orabulkloader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 4/8/14 Time: 1:44 PM
 */
public class OraBulkLoaderTest {

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testCreateCommandLine() throws Exception {
    StepMockHelper<OraBulkLoaderMeta, OraBulkLoaderData> stepMockHelper = new StepMockHelper<OraBulkLoaderMeta,
      OraBulkLoaderData>( "TEST_CREATE_COMMANDLINE", OraBulkLoaderMeta.class, OraBulkLoaderData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    OraBulkLoader oraBulkLoader =
      new OraBulkLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    File tmp = File.createTempFile( "testCreateCOmmandLine", "tmp" );
    tmp.deleteOnExit();
    OraBulkLoaderMeta meta = new OraBulkLoaderMeta();
    meta.setSqlldr( tmp.getAbsolutePath() );
    meta.setControlFile( tmp.getAbsolutePath() );
    DatabaseMeta dm = mock( DatabaseMeta.class );
    when( dm.getUsername() ).thenReturn( "user" );
    when( dm.getPassword() ).thenReturn( "Encrypted 2be98afc86aa7f2e4cb298b5eeab387f5" );
    meta.setDatabaseMeta( dm );
    String cmd = oraBulkLoader.createCommandLine( meta, true );
    String expected = tmp.getAbsolutePath() + " control='" + tmp.getAbsolutePath() + "' userid=user/PENTAHO@";
    assertEquals( "Comandline for oracle bulkloader is not as expected", expected, cmd );
  }
}
