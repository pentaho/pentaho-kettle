/*
 * ! ******************************************************************************
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
import java.util.concurrent.locks.ReentrantLock;
import org.w3c.dom.Node;

/**
 * An implementation of SharedObjectsIO that backs to a Repository with a write-through cache.
 * <p>
 * Reads are served from the in-memory cache after the first load for each type; writes go to both the
 * backing repository and the cache. Call {@link #clearCache()} to evict all locally cached entries so
 * that the next read fetches fresh data from the backing repository.
 * <p>
 * Note that PurRepository does its own caching through the RepositoryExtended interface, which this
 * class also makes use of when available.
 */
public class RepositorySharedObjectsIO implements SharedObjectsIO {

  private final Repository repository;
  private final SlaveServersSupplier slaveServerSupplier;
  private final Map<String, Map<String, Node>> cache = new HashMap<>();
  // Guards cache map reads/writes and repository cache invalidation calls.
  private final ReentrantLock cacheLock = new ReentrantLock();
  // Implements SharedObjectsIO Node-access lock contract.
  private final ReentrantLock nodeLock = new ReentrantLock();

  public RepositorySharedObjectsIO( Repository repository, SlaveServersSupplier slaveServerSupplier ) {
    this.repository = Objects.requireNonNull( repository );
    this.slaveServerSupplier = slaveServerSupplier;
  }

  @SuppressWarnings( "deprecation" ) @Override
  public Map<String, Node> getSharedObjects( String type ) throws KettleException {
    try {
      cacheLock.lock();
      SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );
      Map<String, Node> cached = cache.get( objectType.getName() );
      if ( cached != null ) {
        return new HashMap<>( cached );
      }
      List<? extends SharedObjectInterface<?>> objects = repository instanceof RepositoryExtended
        ? loadFromExtendedRepository( (RepositoryExtended) repository, objectType )
        : loadFromRepository( objectType );
      Map<String, Node> result = new HashMap<>();
      if ( objects != null ) {
        for ( SharedObjectInterface<?> object : objects ) {
          result.put( object.getName(), object.toNode() );
        }
      }
      cache.put( objectType.getName(), result );
      return new HashMap<>( result );
    } finally {
      cacheLock.unlock();
    }
  }

  private List<? extends SharedObjectInterface<?>> loadFromExtendedRepository(
      @SuppressWarnings( "deprecation" ) RepositoryExtended extended, SharedObjectType objectType ) throws KettleException {
    switch ( objectType ) {
      case CONNECTION:
        return extended.getConnections( true );
      case SLAVESERVER:
        return extended.getSlaveServers( true );
      case PARTITIONSCHEMA:
        return extended.getPartitions( true );
      case CLUSTERSCHEMA:
        return extended.getClusters( true );
      default:
        return Collections.emptyList();
    }
  }

  private List<? extends SharedObjectInterface<?>> loadFromRepository( SharedObjectType objectType )
      throws KettleException {
    switch ( objectType ) {
      case CONNECTION:
        return repository.readDatabases();
      case SLAVESERVER:
        return repository.getSlaveServers();
      case PARTITIONSCHEMA:
        return loadPartitionSchemas();
      case CLUSTERSCHEMA:
        return loadClusterSchemas();
      default:
        return Collections.emptyList();
    }
  }

  private List<PartitionSchema> loadPartitionSchemas() throws KettleException {
    ObjectId[] psids = repository.getPartitionSchemaIDs( false );
    List<PartitionSchema> pss = new ArrayList<>();
    if ( psids != null ) {
      for ( ObjectId id : psids ) {
        pss.add( repository.loadPartitionSchema( id, null ) );
      }
    }
    return pss;
  }

  private List<ClusterSchema> loadClusterSchemas() throws KettleException {
    ObjectId[] csids = repository.getClusterIDs( false );
    List<ClusterSchema> css = new ArrayList<>();
    if ( csids != null ) {
      List<SlaveServer> sss = slaveServerSupplier.get();
      for ( ObjectId id : csids ) {
        css.add( repository.loadClusterSchema( id, sss, null ) );
      }
    }
    return css;
  }

  @Override
  public Node getSharedObject( String type, String name ) throws KettleException {
    try {
      cacheLock.lock();
      Map<String, Node> nodeMap = getSharedObjects( type );
      return nodeMap.get( SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap.keySet() ) );
    } finally {
      cacheLock.unlock();
    }
  }

  @Override
  public void clear( String type ) throws KettleException {
    try {
      cacheLock.lock();
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
      cache.remove( objectType.getName() );
    } finally {
      cacheLock.unlock();
    }
  }

  @Override
  public void delete( String type, String name ) throws KettleException {
    try {
      cacheLock.lock();
      SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );

      Map<String, Node> nodeMap = getSharedObjects( type );
      String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap.keySet() );
      if ( existingName == null ) {
        existingName = name;
      }

      deleteInner( objectType, existingName );
    } finally {
      cacheLock.unlock();
    }
  }

  // Must be called with cacheLock held.
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
    Map<String, Node> typeCache = cache.get( objectType.getName() );
    if ( typeCache != null ) {
      typeCache.remove( existingName );
    }
  }

  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    try {
      cacheLock.lock();
      SharedObjectType objectType = SharedObjectType.valueOf( type.toUpperCase() );

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
        updateCache( objectType.getName(), name, (SharedObjectInterface<?>) repoElement );
      }
    } finally {
      cacheLock.unlock();
    }
  }

  // Must be called with cacheLock held.
  private void updateCache( String type, String oldName, SharedObjectInterface<?> element ) throws KettleException {
    Map<String, Node> typeCache = cache.get( type );
    if ( typeCache == null ) {
      return;
    }
    String existingKey = SharedObjectsIO.findSharedObjectIgnoreCase( oldName, typeCache.keySet() );
    if ( existingKey != null ) {
      typeCache.remove( existingKey );
    }
    typeCache.put( element.getName(), element.toNode() );
  }

  @Override
  public void clearCache() {
    try {
      cacheLock.lock();
      cache.clear();
      repository.clearSharedObjectCache();
    } finally {
      cacheLock.unlock();
    }
  }

  @Override
  public void lock() {
    nodeLock.lock();
  }

  @Override
  public void unlock() {
    nodeLock.unlock();
  }
}

