/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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
    Map<String, Node> nodeMap = getSharedObjects( type );
    return nodeMap.get( SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap ) );
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

    Map<String, Node> nodeMap = getSharedObjects( type );
    String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap );
    if ( existingName == null ) {
      existingName = name;
    }

    deleteInner( objectType, existingName );
  }

  private void deleteInner( SharedObjectType objectType, String existingName ) throws KettleException {
    ObjectId id;
    switch ( objectType ) {
        case CONNECTION:
          repository.deleteDatabaseMeta( existingName );
          break;
        case SLAVESERVER:
          id = repository.getSlaveID( existingName );
          if ( id != null ) {
            repository.deleteSlave( id );
          }
          break;
        case PARTITIONSCHEMA:
          id = repository.getPartitionSchemaID( existingName );
          if ( id != null ) {
            repository.deletePartitionSchema( id );
          }
          break;
        case CLUSTERSCHEMA:
          id = repository.getClusterID( existingName );
          if ( id != null ) {
            repository.deleteClusterSchema( id );
          }
          break;
    }
  }

  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );

    Map<String, Node> nodeMap = getSharedObjects( type );
    String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap );
    if ( existingName != null ) {
      deleteInner( objectType, existingName );
    }

    RepositoryElementInterface repoElement = null;
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

