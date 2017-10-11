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

package org.pentaho.di.trans.cluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMetaFactory;
import org.pentaho.di.trans.TransMetaFactoryImpl;
import org.w3c.dom.Node;

public class TransSplitterTest {
  private LogChannelInterfaceFactory oldLogChannelInterfaceFactory;

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
    TransMetaFactory factory = mock( TransMetaFactory.class );
    when( factory.create( any( Node.class ), any( Repository.class ) ) ).thenReturn( meta2 );
    when( meta.getXML() ).thenReturn( "<transformation></transformation>" );
    try {
      new TransSplitter( meta, factory );
    } catch ( Exception e ) {
      // ignore
    }
    verify( rep, times( 1 ) ).readTransSharedObjects( meta2 );
  }

  @Test
   public void testTransSplitterRowsetSize() throws KettleException {
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
