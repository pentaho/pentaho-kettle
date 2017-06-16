/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

public class ClusterDelegate extends AbstractDelegate implements ITransformer, SharedObjectAssembler<ClusterSchema>,
    java.io.Serializable {

  private static final long serialVersionUID = -4350522899150054287L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final String NODE_ROOT = "Slave"; //$NON-NLS-1$

  private static final String PROP_BASE_PORT = "BASE_PORT"; //$NON-NLS-1$

  private static final String PROP_SOCKETS_BUFFER_SIZE = "SOCKETS_BUFFER_SIZE"; //$NON-NLS-1$

  private static final String PROP_SOCKETS_FLUSH_INTERVAL = "SOCKETS_FLUSH_INTERVAL"; //$NON-NLS-1$

  private static final String PROP_SOCKETS_COMPRESSED = "SOCKETS_COMPRESSED"; //$NON-NLS-1$

  private static final String PROP_DYNAMIC = "DYNAMIC"; //$NON-NLS-1$

  private static final String NODE_ATTRIBUTES = "attributes"; //$NON-NLS-1$

  private static final String PROP_NB_SLAVE_SERVERS = "NB_SLAVE_SERVERS"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private PurRepository repo;

  // ~ Constructors ====================================================================================================

  public ClusterDelegate( final PurRepository repo ) {
    super();
    this.repo = repo;
  }

  public RepositoryElementInterface dataNodeToElement( DataNode rootNode ) throws KettleException {
    ClusterSchema clusterSchema = new ClusterSchema();
    dataNodeToElement( rootNode, clusterSchema );
    return clusterSchema;
  }

  public void dataNodeToElement( DataNode rootNode, RepositoryElementInterface element ) throws KettleException {
    ClusterSchema clusterSchema = (ClusterSchema) element;
    // The metadata...
    clusterSchema.setBasePort( getString( rootNode, PROP_BASE_PORT ) );
    clusterSchema.setSocketsBufferSize( getString( rootNode, PROP_SOCKETS_BUFFER_SIZE ) );
    clusterSchema.setSocketsFlushInterval( getString( rootNode, PROP_SOCKETS_FLUSH_INTERVAL ) );
    clusterSchema.setSocketsCompressed( rootNode.getProperty( PROP_SOCKETS_COMPRESSED ).getBoolean() );
    clusterSchema.setDynamic( rootNode.getProperty( PROP_DYNAMIC ).getBoolean() );
    DataNode attrNode = rootNode.getNode( NODE_ATTRIBUTES );
    // The slaves...
    long nrSlaves = attrNode.getProperty( PROP_NB_SLAVE_SERVERS ).getLong();
    for ( int i = 0; i < nrSlaves; i++ ) {
      if ( attrNode.hasProperty( String.valueOf( i ) ) ) {
        DataNodeRef slaveNodeRef = attrNode.getProperty( String.valueOf( i ) ).getRef();
        clusterSchema.getSlaveServers().add( findSlaveServer( new StringObjectId( slaveNodeRef.toString() ) ) );
      }
    }
  }

  public DataNode elementToDataNode( RepositoryElementInterface element ) throws KettleException {
    ClusterSchema clusterSchema = (ClusterSchema) element;
    DataNode rootNode = new DataNode( NODE_ROOT );
    // save the properties...
    rootNode.setProperty( PROP_BASE_PORT, clusterSchema.getBasePort() );
    rootNode.setProperty( PROP_SOCKETS_BUFFER_SIZE, clusterSchema.getSocketsBufferSize() );
    rootNode.setProperty( PROP_SOCKETS_FLUSH_INTERVAL, clusterSchema.getSocketsFlushInterval() );
    rootNode.setProperty( PROP_SOCKETS_COMPRESSED, clusterSchema.isSocketsCompressed() );
    rootNode.setProperty( PROP_DYNAMIC, clusterSchema.isDynamic() );

    DataNode attrNode = rootNode.addNode( NODE_ATTRIBUTES );

    // Also save the used slave server references.

    attrNode.setProperty( PROP_NB_SLAVE_SERVERS, clusterSchema.getSlaveServers().size() );
    for ( int i = 0; i < clusterSchema.getSlaveServers().size(); i++ ) {
      SlaveServer slaveServer = clusterSchema.getSlaveServers().get( i );
      DataNodeRef slaveNodeRef = new DataNodeRef( slaveServer.getObjectId().getId() );
      // Save the slave server by reference, this way it becomes impossible to delete the slave by accident when still
      // in use.
      attrNode.setProperty( String.valueOf( i ), slaveNodeRef );
    }
    return rootNode;
  }

  private SlaveServer findSlaveServer( ObjectId slaveServerId ) {
    List<SlaveServer> slaveServers;
    try {
      slaveServers = repo.getSlaveServers();
      for ( SlaveServer slaveServer : slaveServers ) {
        if ( slaveServer.getObjectId().equals( slaveServerId ) ) {
          return slaveServer;
        }
      }
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  protected Repository getRepository() {
    return repo;
  }

  public ClusterSchema assemble( RepositoryFile file, NodeRepositoryFileData data, VersionSummary version )
    throws KettleException {
    ClusterSchema clusterSchema = (ClusterSchema) dataNodeToElement( data.getNode() );
    clusterSchema.setName( file.getTitle() );
    clusterSchema.setObjectId( new StringObjectId( file.getId().toString() ) );
    clusterSchema.setObjectRevision( repo.createObjectRevision( version ) );
    clusterSchema.clearChanged();
    return clusterSchema;
  }
}
