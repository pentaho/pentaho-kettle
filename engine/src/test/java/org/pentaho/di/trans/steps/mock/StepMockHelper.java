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


package org.pentaho.di.trans.steps.mock;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogMessageInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    stepDataInterface = mock( stepDataClass );
    transMeta = mock( TransMeta.class );
    when( transMeta.findStep( stepName ) ).thenReturn( stepMeta );
    trans = mock( Trans.class );
    initStepMetaInterface = mock( stepMetaClass );
    initStepDataInterface = mock( stepDataClass );
    processRowsStepDataInterface = mock( stepDataClass );
    processRowsStepMetaInterface = mock( stepMetaClass );
  }

  public RowSet getMockInputRowSet( Object[]... rows ) {
    return getMockInputRowSet( asList( rows ) );
  }

  public RowSet getMockInputRowSet( final List<Object[]> rows ) {
    final AtomicInteger index = new AtomicInteger( 0 );
    RowSet rowSet = mock( RowSet.class, Mockito.RETURNS_MOCKS );
    Answer<Object[]> answer = invocation -> {
      int i = index.getAndIncrement();
      return i < rows.size() ? rows.get( i ) : null;
    };
    lenient().when( rowSet.getRowWait( anyLong(), any( TimeUnit.class ) ) ).thenAnswer( answer );
    when( rowSet.getRow() ).thenAnswer( answer );
    when( rowSet.isDone() ).thenAnswer( (Answer<Boolean>) invocation -> index.get() >= rows.size() );
    return rowSet;
  }

  public static List<Object[]> asList( Object[]... objects ) {
    List<Object[]> result = new ArrayList<>();
    Collections.addAll( result, objects );
    return result;
  }

  public void cleanUp() {
    KettleLogStore.setLogChannelInterfaceFactory( originalLogChannelInterfaceFactory );
  }

  /**
   *  In case you need to use log methods during the tests
   *  use redirectLog method after creating new StepMockHelper object.
   *  Examples:
   *    stepMockHelper.redirectLog( System.out, LogLevel.ROWLEVEL );
   *    stepMockHelper.redirectLog( new FileOutputStream("log.txt"), LogLevel.BASIC );
   */
  public void redirectLog( final OutputStream out, LogLevel channelLogLevel ) {
    final LogChannel log = spy( new LogChannel( this.getClass().getName(), true ) );
    log.setLogLevel( channelLogLevel );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( log );
    doAnswer( (Answer<Object>) invocation -> {
      Object[] args = invocation.getArguments();

      LogLevel logLevel = (LogLevel) args[1];
      LogLevel channelLogLevel1 = log.getLogLevel();

      if ( !logLevel.isVisible( channelLogLevel1 ) ) {
        return null; // not for our eyes.
      }
      if ( channelLogLevel1.getLevel() >= logLevel.getLevel() ) {
        LogMessageInterface logMessage = (LogMessageInterface) args[0];
        out.write( logMessage.getMessage().getBytes() );
        out.write( '\n' );
        out.write( '\r' );
        out.flush();
        return true;
      }
      return false;
    } ).when( log ).println( any( LogMessageInterface.class ), any( LogLevel.class ) );
  }
}
