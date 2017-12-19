/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import static org.junit.Assert.fail;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PurRepository_SharedObjects_Links_IT extends PurRepositoryIT {

  public PurRepository_SharedObjects_Links_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  private interface GenericMeta {
    public AbstractMeta createFilled() throws Exception;

    public void loadFromXml( Node xmlNode ) throws Exception;

    public AbstractMeta createEmpty();
  }

  @Test
  public void testReadSharedObjects_Trans() throws Exception {
    testReadSharedObjects( new GenericMeta() {

      private TransMeta meta;

      @Override
      public void loadFromXml( Node xmlNode ) throws Exception {
        meta.loadXML( xmlNode, repository, true, null );
      }

      @Override
      public AbstractMeta createFilled() throws Exception {
        meta = createTransMeta( EXP_DBMETA_NAME );
        return meta;
      }

      @Override
      public AbstractMeta createEmpty() {
        meta = new TransMeta();
        return meta;
      }
    } );
  }

  @Test
  public void testReadSharedObjects_Job() throws Exception {
    testReadSharedObjects( new GenericMeta() {

      private JobMeta meta;

      @Override
      public void loadFromXml( Node xmlNode ) throws Exception {
        meta.loadXML( xmlNode, repository, null );
      }

      @Override
      public AbstractMeta createFilled() throws Exception {
        meta = createJobMeta( EXP_DBMETA_NAME );
        return meta;
      }

      @Override
      public AbstractMeta createEmpty() {
        meta = new JobMeta();
        return meta;
      }
    } );
  }

  @SuppressWarnings( "unchecked" )
  private void testReadSharedObjects( GenericMeta gMeta ) throws Exception {
    PurRepository pur = (PurRepository) repository;

    RepositoryDirectoryInterface rootDir = initRepo();

    SlaveServer slave1 = createSlaveServer( "slave1" );
    SlaveServer slave2 = createSlaveServer( "slave2" );

    pur.save( slave1, VERSION_COMMENT_V1, null );
    pur.save( slave2, VERSION_COMMENT_V1, null );

    AbstractMeta meta = gMeta.createFilled();

    meta.getSlaveServers().add( slave1 );
    meta.getSlaveServers().add( slave2 );

    rootDir.findDirectory( DIR_TRANSFORMATIONS );
    pur.save( meta, VERSION_COMMENT_V1, null );
    String xmlText = meta.getXML();

    try {
      // import transformation from file
      meta = gMeta.createEmpty();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse( IOUtils.toInputStream( xmlText ) );
      gMeta.loadFromXml( doc.getParentNode() );

      List<SharedObjectInterface> sharedObjects =
          (List<SharedObjectInterface>) pur.loadAndCacheSharedObjects( false ).get( RepositoryObjectType.SLAVE_SERVER );

      for ( int i = 0; i < meta.getSlaveServers().size(); i++ ) {
        for ( int j = 0; j < sharedObjects.size(); j++ ) {
          SlaveServer s1 = meta.getSlaveServers().get( i );
          SlaveServer s2 = (SlaveServer) sharedObjects.get( j );
          if ( s1 == s2 ) {
            fail( "Trans/job has direct links on slave servers from cache" );
          }
        }
      }
    } finally {
      pur.deleteSlave( slave1.getObjectId() );
      pur.deleteSlave( slave2.getObjectId() );
      pur.clearSharedObjectCache();
    }
  }

  @Override
  protected void delete( ObjectId id ) {
    // do nothing
  }

}
