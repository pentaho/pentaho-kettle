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


package org.pentaho.di.trans.step;

import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BaseStepMetaCloningTest {

  @Test
  public void testClone() throws Exception {
    final Database db1 = mock( Database.class );
    final Database db2 = mock( Database.class );
    final Repository repository = mock( Repository.class );
    final StepMeta stepMeta = mock( StepMeta.class );

    BaseStepMeta meta = new BaseStepMeta();
    meta.setChanged( true );
    meta.databases = new Database[] { db1, db2 };
    StepIOMetaInterface ioMeta = new StepIOMeta( true, false, false, false, false, false );
    meta.setStepIOMeta( ioMeta ); 
    meta.repository = repository;
    meta.parentStepMeta = stepMeta;

    BaseStepMeta clone = (BaseStepMeta) meta.clone();
    assertTrue( clone.hasChanged() );

    // is it OK ?
    assertTrue( clone.databases == meta.databases );
    assertArrayEquals( meta.databases, clone.databases );

    assertEquals( meta.repository, clone.repository );
    assertEquals( meta.parentStepMeta, clone.parentStepMeta );

    StepIOMetaInterface cloneIOMeta = clone.getStepIOMeta();
    assertNotNull( cloneIOMeta );
    assertEquals( ioMeta.isInputAcceptor(), cloneIOMeta.isInputAcceptor() );
    assertEquals( ioMeta.isInputDynamic(), cloneIOMeta.isInputDynamic() );
    assertEquals( ioMeta.isInputOptional(), cloneIOMeta.isInputOptional() );
    assertEquals( ioMeta.isOutputDynamic(), cloneIOMeta.isOutputDynamic() );
    assertEquals( ioMeta.isOutputProducer(), cloneIOMeta.isOutputProducer() );
    assertEquals( ioMeta.isSortedDataRequired(), cloneIOMeta.isSortedDataRequired() );
    assertNotNull( cloneIOMeta.getInfoStreams() );
    assertEquals( 0, cloneIOMeta.getInfoStreams().size() );
  }

  @Test
  public void testCloneWithInfoSteps() throws Exception {
    final Database db1 = mock( Database.class );
    final Database db2 = mock( Database.class );
    final Repository repository = mock( Repository.class );
    final StepMeta stepMeta = mock( StepMeta.class );

    BaseStepMeta meta = new BaseStepMeta();
    meta.setChanged( true );
    meta.databases = new Database[] { db1, db2 };
    StepIOMetaInterface ioMeta = new StepIOMeta( true, false, false, false, false, false );
    meta.setStepIOMeta( ioMeta );

    final String refStepName = "referenced step";
    final StepMeta refStepMeta = mock( StepMeta.class );
    doReturn( refStepName ).when( refStepMeta ).getName();
    StreamInterface stream = new Stream( StreamInterface.StreamType.INFO, refStepMeta, null, null, refStepName );
    ioMeta.addStream( stream );
    meta.repository = repository;
    meta.parentStepMeta = stepMeta;

    BaseStepMeta clone = (BaseStepMeta) meta.clone();
    assertTrue( clone.hasChanged() );

    // is it OK ?
    assertTrue( clone.databases == meta.databases );
    assertArrayEquals( meta.databases, clone.databases );

    assertEquals( meta.repository, clone.repository );
    assertEquals( meta.parentStepMeta, clone.parentStepMeta );


    StepIOMetaInterface cloneIOMeta = clone.getStepIOMeta();
    assertNotNull( cloneIOMeta );
    assertEquals( ioMeta.isInputAcceptor(), cloneIOMeta.isInputAcceptor() );
    assertEquals( ioMeta.isInputDynamic(), cloneIOMeta.isInputDynamic() );
    assertEquals( ioMeta.isInputOptional(), cloneIOMeta.isInputOptional() );
    assertEquals( ioMeta.isOutputDynamic(), cloneIOMeta.isOutputDynamic() );
    assertEquals( ioMeta.isOutputProducer(), cloneIOMeta.isOutputProducer() );
    assertEquals( ioMeta.isSortedDataRequired(), cloneIOMeta.isSortedDataRequired() );

    final List<StreamInterface> clonedInfoStreams = cloneIOMeta.getInfoStreams();
    assertNotNull( clonedInfoStreams );
    assertEquals( 1, clonedInfoStreams.size() );

    final StreamInterface clonedStream = clonedInfoStreams.get( 0 );
    assertNotSame( stream, clonedStream );
    assertEquals( stream.getStreamType(), clonedStream.getStreamType() );
    assertEquals( refStepName, clonedStream.getStepname() );

    assertSame( refStepMeta, clonedStream.getStepMeta() ); // PDI-15799
  }
}
