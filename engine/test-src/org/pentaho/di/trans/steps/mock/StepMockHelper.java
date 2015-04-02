/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class StepMockHelper<Meta extends StepMetaInterface, Data extends StepDataInterface> {
  public final StepMeta stepMeta;
  public final Data stepDataInterface;
  public final TransMeta transMeta;
  public final Trans trans;
  public final Meta initStepMetaInterface;
  public final Data initStepDataInterface;
  public final Meta processRowsStepMetaInterface;
  public final Data processRowsStepDataInterface;
  public final LogChannelInterface logChannelInterface;
  public final LogChannelInterfaceFactory logChannelInterfaceFactory;
  public final LogChannelInterfaceFactory originalLogChannelInterfaceFactory;

  public StepMockHelper( String stepName, Class<Meta> stepMetaClass, Class<Data> stepDataClass ) {
    originalLogChannelInterfaceFactory = KettleLogStore.getLogChannelInterfaceFactory();
    logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    stepMeta = mock( StepMeta.class );
    when( stepMeta.getName() ).thenReturn( stepName );
    stepDataInterface = (Data) mock( stepDataClass );
    transMeta = mock( TransMeta.class );
    when( transMeta.findStep( stepName ) ).thenReturn( stepMeta );
    trans = mock( Trans.class );
    initStepMetaInterface = (Meta) mock( stepMetaClass );
    initStepDataInterface = (Data) mock( stepDataClass );
    processRowsStepDataInterface = (Data) mock( stepDataClass );
    processRowsStepMetaInterface = (Meta) mock( stepMetaClass );
  }

  public RowSet getMockInputRowSet( Object[]... rows ) {
    return getMockInputRowSet( asList( rows ) );
  }

  public RowSet getMockInputRowSet( final List<Object[]> rows ) {
    final AtomicInteger index = new AtomicInteger( 0 );
    RowSet rowSet = mock( RowSet.class, Mockito.RETURNS_MOCKS );
    when( rowSet.getRowWait( anyLong(), any( TimeUnit.class ) ) ).thenAnswer( new Answer<Object[]>() {
      @Override
      public Object[] answer( InvocationOnMock invocation ) throws Throwable {
        int i = index.getAndIncrement();
        return i < rows.size() ? rows.get( i ) : null;
      }

    } );
    when( rowSet.isDone() ).thenAnswer( new Answer<Boolean>() {

      @Override
      public Boolean answer( InvocationOnMock invocation ) throws Throwable {
        return index.get() >= rows.size();
      }
    } );
    return rowSet;
  }

  public static List<Object[]> asList( Object[]... objects ) {
    List<Object[]> result = new ArrayList<Object[]>();
    for ( Object[] object : objects ) {
      result.add( object );
    }
    return result;
  }

  public void cleanUp() {
    KettleLogStore.setLogChannelInterfaceFactory( originalLogChannelInterfaceFactory );
  }
}
