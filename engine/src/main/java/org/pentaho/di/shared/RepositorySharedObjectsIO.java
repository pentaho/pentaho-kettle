/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.shared;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface.SlaveServersSupplier;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryExtended;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.w3c.dom.Node;

/**
 * An implementation of SharedObjectsIO that backs to a Repository.
 * <p>
 * This class does not cache anything. Note that PurRepository does its own caching, but only through the
 * RepositoryExtended interface, which this class makes use of.
 *
 */
public class RepositorySharedObjectsIO implements SharedObjectsIO {

  private final Repository repository;
  private final SlaveServersSupplier slaveServerSupplier;

  public RepositorySharedObjectsIO( Repository repository, SlaveServersSupplier slaveServerSupplier ) {
    this.repository = Objects.requireNonNull( repository );
    this.slaveServerSupplier = slaveServerSupplier;
  }

  @Override
  public Map<String, Node> getSharedObjects( String type ) throws KettleException {
    SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );
    List<? extends SharedObjectInterface> objects = null;
    if ( repository instanceof RepositoryExtended ) {
      // use the methods that support caching
      RepositoryExtended extended = (RepositoryExtended) repository;
      switch ( objectType ) {
          case CONNECTION:
            objects = extended.getConnections( true );
            break;
          case SLAVESERVER:
            objects = extended.getSlaveServers( true );
            break;
          case PARTITIONSCHEMA:
            objects = extended.getPartitions( true );
            break;
          case CLUSTERSCHEMA:
            objects = extended.getClusters( true );
            break;
      }
    } else {
      switch ( objectType ) {
          case CONNECTION:
            objects = repository.readDatabases();
            break;
          case SLAVESERVER:
            objects = repository.getSlaveServers();
            break;
          case PARTITIONSCHEMA:
            ObjectId[] psids = repository.getPartitionSchemaIDs( false );
            List<PartitionSchema> pss = new ArrayList<PartitionSchema>();
            if ( psids != null ) {
              for ( ObjectId id : psids ) {
                pss.add( repository.loadPartitionSchema( id, null ) );
              }
            }
            objects = pss;
            break;
          case CLUSTERSCHEMA:
            ObjectId[] csids = repository.getClusterIDs( false );
            List<ClusterSchema> css = new ArrayList<ClusterSchema>();
            if ( csids != null ) {
              List<SlaveServer> sss = slaveServerSupplier.get();
              for ( ObjectId id : csids ) {
                css.add( repository.loadClusterSchema( id, sss, null ) );
              }
            }
            objects = css;
            break;
      }
    }
    if ( objects != null ) {
      Map<String, Node> result = new HashMap<>();
      for ( SharedObjectInterface object : objects ) {
        result.put( object.getName(), object.toNode() );
      }
      return result;
    }
    return Collections.emptyMap();
  }

  @Override
  public Node getSharedObject( String type, String name ) throws KettleException {
    SharedObjectInterface object = null;
    ObjectId id = null;
    SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );
    switch ( objectType ) {
        case CONNECTION:
          id = repository.getDatabaseID( name );
          if ( id != null ) {
            object = repository.loadDatabaseMeta( id, null );
          }
          break;
        case SLAVESERVER:
          id = repository.getSlaveID( name );
          if ( id != null ) {
            object = repository.loadSlaveServer( id, null );
          }
          break;
        case PARTITIONSCHEMA:
          id = repository.getPartitionSchemaID( name );
          if ( id != null ) {
            object = repository.loadPartitionSchema( id, null );
          }
          break;
        case CLUSTERSCHEMA:
          id = repository.getClusterID( name );
          if ( id != null ) {
            object = repository.loadClusterSchema( id, slaveServerSupplier.get(), null );
          }
          break;
    }
    if ( object != null ) {
      return object.toNode();
    }
    return null;
  }

  @Override
  public void clear( String type ) throws KettleException {
    SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );
    ObjectId[] ids = null;
    switch ( objectType ) {
        case CONNECTION:
          String[] names = repository.getDatabaseNames( false );
          for ( String name : names ) {
            repository.deleteDatabaseMeta( name );
          }
          break;
        case SLAVESERVER:
          ids = repository.getSlaveIDs( false );
          for ( ObjectId id : ids ) {
            repository.deleteSlave( id );
          }
          break;
        case PARTITIONSCHEMA:
          ids = repository.getPartitionSchemaIDs( false );
          for ( ObjectId id : ids ) {
            repository.deletePartitionSchema( id );
          }
          break;
        case CLUSTERSCHEMA:
          ids = repository.getClusterIDs( false );
          for ( ObjectId id : ids ) {
            repository.deleteClusterSchema( id );
          }
          break;
    }
  }

  @Override
  public void delete( String type, String name ) throws KettleException {
    SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );
    ObjectId id;
    switch ( objectType ) {
        case CONNECTION:
          repository.deleteDatabaseMeta( name );
          break;
        case SLAVESERVER:
          id = repository.getSlaveID( name );
          if ( id != null ) {
            repository.deleteSlave( id );
          }
          break;
        case PARTITIONSCHEMA:
          id = repository.getPartitionSchemaID( name );
          if ( id != null ) {
            repository.deletePartitionSchema( id );
          }
          break;
        case CLUSTERSCHEMA:
          id = repository.getClusterID( name );
          if ( id != null ) {
            repository.deleteClusterSchema( id );
          }
          break;
    }
  }

  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    RepositoryElementInterface repoElement = null;
    SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );
    switch ( objectType ) {
        case CONNECTION:
          repoElement = new DatabaseMeta( node );
          break;
        case SLAVESERVER:
          repoElement = new SlaveServer( node );
          break;
        case PARTITIONSCHEMA:
          repoElement = new PartitionSchema( node );
          break;
        case CLUSTERSCHEMA:
          repoElement = new ClusterSchema( node, slaveServerSupplier.get() );
          break;
    }
    if ( repoElement != null ) {
      repository.save( repoElement, Const.VERSION_COMMENT_EDIT_VERSION, null );
    }
  }
}

