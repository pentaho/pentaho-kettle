/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.debug;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class TransDebugMetaTest {
  @Mock private RowMetaInterface rowMetaInterface;
  @Mock private StepDebugMeta stepDebugMeta;

  Trans trans;
  TransDebugMeta transDebugMeta;
  TransMeta meta;


  @BeforeClass
  public static void beforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void beforeTest() throws KettleException {
    meta = new TransMeta();
    transDebugMeta = new TransDebugMeta( meta );
    trans = spy( new Trans( meta ) );
    trans.setLog( Mockito.mock( LogChannelInterface.class ) );
    trans.prepareExecution( null );
    trans.startThreads();
  }

  @Test
  public void testIfTransIsPaused() throws Exception {
    List<Object[]> list = Arrays.asList( new Object[]{}, new Object[]{} );
    when( stepDebugMeta.getRowCount() ).thenReturn( 1 );
    when( stepDebugMeta.isReadingFirstRows() ).thenReturn( true );
    when( stepDebugMeta.getRowBuffer() ).thenReturn( list );

    assertFalse( trans.isPaused() );
    transDebugMeta.rowWrittenEventHandler( rowMetaInterface, new Object[]{}, stepDebugMeta, trans, transDebugMeta );
    verify( trans, times( 1 ) ).pauseRunning();
    assertTrue( trans.isPaused() );

    transDebugMeta.rowWrittenEventHandler( rowMetaInterface, new Object[]{}, stepDebugMeta, trans, transDebugMeta );
    assertTrue( trans.isPaused() );
    verify( trans, times( 1 ) ).pauseRunning();
  }
}
