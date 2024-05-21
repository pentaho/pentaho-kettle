/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.cluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMetaFactory;
import org.pentaho.di.trans.TransMetaFactoryImpl;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransSplitterTest {
  private LogChannelInterfaceFactory oldLogChannelInterfaceFactory;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUp() throws Exception {
    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    oldLogChannelInterfaceFactory = KettleLogStore.getLogChannelInterfaceFactory();
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        logChannelInterface );
  }

  @After
  public void tearDown() {
    KettleLogStore.setLogChannelInterfaceFactory( oldLogChannelInterfaceFactory );
  }

  @Test
  public void testTransSplitterReadsRep() throws KettleException {
    TransMeta meta = mock( TransMeta.class );
    Repository rep = mock( Repository.class );
    when( meta.getRepository() ).thenReturn( rep );
    TransMeta meta2 = mock( TransMeta.class );

    List<StepMeta> stepMetaList = new ArrayList<>();
    StepMeta stepMeta = mock( StepMeta.class );
    ClusterSchema schema = mock( ClusterSchema.class );
    when( schema.getName() ).thenReturn( "Test-Cluster" );
    when( schema.findMaster() ).thenReturn( new SlaveServer() );
    when( schema.getSlaveServersFromMasterOrLocal() ).thenReturn( Collections.singletonList( new SlaveServer() ) );
    when( stepMeta.getClusterSchema() ).thenReturn( schema );
    stepMetaList.add( stepMeta );
    when( meta2.getSteps() ).thenReturn( stepMetaList );
    ClusterSchema firstUsedSchema = mock( ClusterSchema.class );
    when( firstUsedSchema.isDynamic() ).thenReturn( false );
    when( meta2.findFirstUsedClusterSchema() ).thenReturn( firstUsedSchema );


    TransMetaFactory factory = mock( TransMetaFactory.class );
    when( factory.create( any(), any() ) ).thenReturn( meta2 );
    when( meta.getXML() ).thenReturn( "<transformation></transformation>" );
    new TransSplitter( meta, factory );
    verify( rep, times( 1 ) ).readTransSharedObjects( meta2 );
  }

  @Test
   public void testTransSplitterRowsetSize() {
    TransMeta originalMeta = new TransMeta();
    originalMeta.setSizeRowset( 0 );
    TransMetaFactory factory = new TransMetaFactoryImpl();

    try {
      TransSplitter transSplitter = new TransSplitter( originalMeta, factory );
      transSplitter.splitOriginalTransformation();
      assertEquals( originalMeta.getSizeRowset(), transSplitter.getMaster().getSizeRowset() );
    } catch ( Exception e ) {
      //ignore
    }

  }
}
