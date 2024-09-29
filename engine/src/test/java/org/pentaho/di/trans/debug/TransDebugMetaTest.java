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
