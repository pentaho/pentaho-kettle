/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.pgbulkloader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Matchers;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;



public class PGBulkLoaderTest {

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testCreateCommandLine() throws Exception {
    PGBulkLoaderMeta meta = Mockito.mock( PGBulkLoaderMeta.class );
    Mockito.doReturn( new DatabaseMeta() ).when( meta ).getDatabaseMeta();
    Mockito.doReturn( new String[0] ).when( meta ).getFieldStream();
    PGBulkLoaderData data = Mockito.mock( PGBulkLoaderData.class );
    TransMeta transMeta = Mockito.mock( TransMeta.class );
    Trans trans = Mockito.mock( Trans.class );
    StepMockHelper<PGBulkLoaderMeta, PGBulkLoaderData> stepMockHelper = new StepMockHelper<PGBulkLoaderMeta,
      PGBulkLoaderData>( "TEST", PGBulkLoaderMeta.class, PGBulkLoaderData.class );
    Mockito.when( stepMockHelper.logChannelInterfaceFactory.create( Matchers.any(), Matchers.any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    Mockito.when( stepMockHelper.trans.isRunning() ).thenReturn( true );

    PGBulkLoader spy =
      Mockito.spy( new PGBulkLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );
    Mockito.doReturn( new Object[0] ).when( spy ).getRow();
    Mockito.doReturn( "" ).when( spy ).getCopyCommand();
    Mockito.doNothing( ).when( spy ).connect();
    Mockito.doNothing( ).when( spy ).processTruncate();
    spy.processRow( meta, data );
    Mockito.verify( spy ).processTruncate();
  }
}
