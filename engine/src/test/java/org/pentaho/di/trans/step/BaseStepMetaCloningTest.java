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

package org.pentaho.di.trans.step;

import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;

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
    meta.ioMeta = new StepIOMeta( true, false, false, false, false, false );
    meta.repository = repository;
    meta.parentStepMeta = stepMeta;

    BaseStepMeta clone = (BaseStepMeta) meta.clone();
    assertTrue( clone.hasChanged() );

    // is it OK ?
    assertTrue( clone.databases == meta.databases );
    assertArrayEquals( meta.databases, clone.databases );

    assertEquals( meta.repository, clone.repository );
    assertEquals( meta.parentStepMeta, clone.parentStepMeta );

    assertNotNull( clone.ioMeta );
    assertEquals( meta.ioMeta.isInputAcceptor(), clone.ioMeta.isInputAcceptor() );
    assertEquals( meta.ioMeta.isInputDynamic(), clone.ioMeta.isInputDynamic() );
    assertEquals( meta.ioMeta.isInputOptional(), clone.ioMeta.isInputOptional() );
    assertEquals( meta.ioMeta.isOutputDynamic(), clone.ioMeta.isOutputDynamic() );
    assertEquals( meta.ioMeta.isOutputProducer(), clone.ioMeta.isOutputProducer() );
    assertEquals( meta.ioMeta.isSortedDataRequired(), clone.ioMeta.isSortedDataRequired() );
    assertNotNull( clone.ioMeta.getInfoStreams() );
    assertEquals( 0, clone.ioMeta.getInfoStreams().size() );
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
    meta.ioMeta = new StepIOMeta( true, false, false, false, false, false );

    final String refStepName = "referenced step";
    final StepMeta refStepMeta = mock( StepMeta.class );
    doReturn( refStepName ).when( refStepMeta ).getName();
    StreamInterface stream = new Stream( StreamInterface.StreamType.INFO, refStepMeta, null, null, refStepName );
    meta.ioMeta.addStream( stream );
    meta.repository = repository;
    meta.parentStepMeta = stepMeta;

    BaseStepMeta clone = (BaseStepMeta) meta.clone();
    assertTrue( clone.hasChanged() );

    // is it OK ?
    assertTrue( clone.databases == meta.databases );
    assertArrayEquals( meta.databases, clone.databases );

    assertEquals( meta.repository, clone.repository );
    assertEquals( meta.parentStepMeta, clone.parentStepMeta );

    assertNotNull( clone.ioMeta );
    assertEquals( meta.ioMeta.isInputAcceptor(), clone.ioMeta.isInputAcceptor() );
    assertEquals( meta.ioMeta.isInputDynamic(), clone.ioMeta.isInputDynamic() );
    assertEquals( meta.ioMeta.isInputOptional(), clone.ioMeta.isInputOptional() );
    assertEquals( meta.ioMeta.isOutputDynamic(), clone.ioMeta.isOutputDynamic() );
    assertEquals( meta.ioMeta.isOutputProducer(), clone.ioMeta.isOutputProducer() );
    assertEquals( meta.ioMeta.isSortedDataRequired(), clone.ioMeta.isSortedDataRequired() );

    final List<StreamInterface> clonedInfoStreams = clone.ioMeta.getInfoStreams();
    assertNotNull( clonedInfoStreams );
    assertEquals( 1, clonedInfoStreams.size() );

    final StreamInterface clonedStream = clonedInfoStreams.get( 0 );
    assertNotSame( stream, clonedStream );
    assertEquals( stream.getStreamType(), clonedStream.getStreamType() );
    assertEquals( refStepName, clonedStream.getStepname() );

    assertSame( refStepMeta, clonedStream.getStepMeta() ); // PDI-15799
  }
}
