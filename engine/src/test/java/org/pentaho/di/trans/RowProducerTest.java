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
package org.pentaho.di.trans;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepInterface;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by mburgess on 10/7/15.
 */
public class RowProducerTest {

  RowProducer rowProducer;
  StepInterface stepInterface;
  RowSet rowSet;
  RowMetaInterface rowMeta;
  Object[] rowData;

  @Before
  public void setUp() throws Exception {
    stepInterface = mock( StepInterface.class );
    rowSet = mock( RowSet.class );
    rowProducer = new RowProducer( stepInterface, rowSet );
    rowMeta = mock( RowMetaInterface.class );
    rowData = new Object[]{};
  }

  @Test
  public void testPutRow2Arg() throws Exception {
    when( rowSet.putRowWait( any( RowMetaInterface.class ), any( Object[].class ), anyLong(), any( TimeUnit.class ) ) )
      .thenReturn( true );
    rowProducer.putRow( rowMeta, rowData );
    verify( rowSet, times( 1 ) ).putRowWait( rowMeta, rowData, Long.MAX_VALUE, TimeUnit.DAYS );
    assertTrue( rowProducer.putRow( rowMeta, rowData, true ) );
  }

  @Test
  public void testPutRow3Arg() throws Exception {
    when( rowSet.putRowWait( any( RowMetaInterface.class ), any( Object[].class ), anyLong(), any( TimeUnit.class ) ) )
      .thenReturn( true );

    rowProducer.putRow( rowMeta, rowData, false );
    verify( rowSet, times( 1 ) ).putRow( rowMeta, rowData );
  }

  @Test
  public void testPutRowWait() throws Exception {
    rowProducer.putRowWait( rowMeta, rowData, 1, TimeUnit.MILLISECONDS );
    verify( rowSet, times( 1 ) ).putRowWait( rowMeta, rowData, 1, TimeUnit.MILLISECONDS );
  }

  @Test
  public void testFinished() throws Exception {
    rowProducer.finished();
    verify( rowSet, times( 1 ) ).setDone();
  }

  @Test
  public void testGetSetRowSet() throws Exception {
    assertEquals( rowSet, rowProducer.getRowSet() );
    rowProducer.setRowSet( null );
    assertNull( rowProducer.getRowSet() );
    RowSet newRowSet = mock( RowSet.class );
    rowProducer.setRowSet( newRowSet );
    assertEquals( newRowSet, rowProducer.getRowSet() );
  }

  @Test
  public void testGetSetStepInterface() throws Exception {
    assertEquals( stepInterface, rowProducer.getStepInterface() );
    rowProducer.setStepInterface( null );
    assertNull( rowProducer.getStepInterface() );
    StepInterface newStepInterface = mock( StepInterface.class );
    rowProducer.setStepInterface( newStepInterface );
    assertEquals( newStepInterface, rowProducer.getStepInterface() );
  }
}
