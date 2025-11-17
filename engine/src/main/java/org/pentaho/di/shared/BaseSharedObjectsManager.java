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

import org.pentaho.di.core.bowl.CachingManager;
import org.pentaho.di.core.bowl.UpdateSubscriber;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.WeakHashMap;

/**
 * This class uses the SharedObjectsIO to retrieve and save shared objects. This is used by the UI.
 * <p>
 * This class caches the state of the underlying SharedObjectsIO, and does not re-read from the source. Only changes
 * written through this interface will be reflected.
 */
public abstract class BaseSharedObjectsManager<T extends SharedObjectInterface<T> & RepositoryElementInterface>
  implements SharedObjectsManagementInterface<T>, CachingManager {

  protected SharedObjectsIO sharedObjectsIO;
  private final WeakHashMap<UpdateSubscriber, Void> changeSubscribers = new WeakHashMap<>();

  private Map<String, T> sharedObjectsMap = new HashMap<>();
  private volatile boolean initialized = false;
  String sharedObjectType;

  protected BaseSharedObjectsManager( String type, SharedObjectsIO sharedObjectsIO ) {
    this.sharedObjectType = type;
    this.sharedObjectsIO = sharedObjectsIO;
  }

  /**
   * Call the SharedObject type specific method to populate the internal map
   * 
   * @throws KettleException
   */
  private void populateSharedObjectMap() throws KettleException {
    if ( !initialized ) {
      synchronized ( this ) {
        if ( !initialized ) {
          try {
            sharedObjectsIO.lock();
            Map<String, Node> nodeMap = sharedObjectsIO.getSharedObjects( sharedObjectType );
            Map<String, T> localSharedObjectMap = new HashMap<>();
            for ( String name : nodeMap.keySet() ) {
              T sharedObject = createSharedObjectUsingNode( nodeMap.get( name ) );
              if ( !localSharedObjectMap.containsKey( name ) ) {
                localSharedObjectMap.put( name, sharedObject );
              }
            }
            sharedObjectsMap = localSharedObjectMap;
          } finally {
            sharedObjectsIO.unlock();
          }
        }
      }
      initialized = true;
    }
  }

  /**
   * This method is called while populating the sharedObjectMap to create concrete SharedObjectInterface implementation
   * class.
   * This will be implemented by subclasses.
   * 
   * @param node
   * @return
   * @throws KettleException
   */
  protected abstract T createSharedObjectUsingNode( Node node ) throws KettleException;


  /**
   * Save the SharedObjectInterface for a type using the persistence mechanism defined by SharedObjectIO and
   * also add to the local map
   * 
   * @param sharedObjectInterface
   * @throws KettleException
   */

  @Override
  public synchronized void add( T sharedObjectInterface ) throws KettleException {
    populateSharedObjectMap();
    String name = sharedObjectInterface.getName();
    Node node = sharedObjectInterface.toNode();

    try {
      sharedObjectsIO.lock();

      String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, sharedObjectsMap.keySet() );
      if ( existingName != null && !existingName.equals( name ) ) {
        // NOTE: we do *not* need to remove from the sharedObjectsIO because the contract for saveSharedObject()
        // requires it to handle renames, even including just case changes, and some SharedObjectsIO, particularly
        // for the Repository, don't allow deletions of in-use databases in particular.
        // We do need to remove it from this class's cache, though.
        sharedObjectsMap.remove( existingName );
      }

      sharedObjectsIO.saveSharedObject( sharedObjectType, name, node );
      Node readBackNode = sharedObjectsIO.getSharedObject( sharedObjectType, name );
      T readBack = createSharedObjectUsingNode( readBackNode );

      sharedObjectsMap.put( name, readBack.makeClone() );
    } finally {
      sharedObjectsIO.unlock();
    }
    notifySubscribers();
  }

  /**
   * Get the list of SharedObjectInterface for the type
   * 
   * @return
   * @throws KettleException
   */
  @Override
  public List<T> getAll() throws KettleException {
    populateSharedObjectMap();
    try {
      sharedObjectsIO.lock();
      return sharedObjectsMap.values().stream().map( SharedObjectInterface::makeClone ).collect( Collectors.toList() );
    } finally {
      sharedObjectsIO.unlock();
    }
  }

  /**
   * Get the SharedObjectInterface object for the type using the name
   * 
   * @param name name of the SharedObject
   * @return
   * @throws KettleException
   */
  @Override
  public T get( String name ) throws KettleException {
    populateSharedObjectMap();

    try {
      sharedObjectsIO.lock();
      T sharedObjectInterface =
        sharedObjectsMap.get( SharedObjectsIO.findSharedObjectIgnoreCase( name, sharedObjectsMap.keySet() ) );
      return sharedObjectInterface == null ? sharedObjectInterface : sharedObjectInterface.makeClone();
    } finally {
      sharedObjectsIO.unlock();
    }
  }

  /**
   * Remove the SharedObjectInterface object for a type
   * 
   * @param sharedObjectInterface SharedObject to remove
   * @throws KettleException
   */
  @Override
  public synchronized void remove( T sharedObjectInterface ) throws KettleException {
    remove( sharedObjectInterface.getName() );
  }


  @Override
  public synchronized void remove( String sharedObjectName ) throws KettleException {
    populateSharedObjectMap();

    String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( sharedObjectName, sharedObjectsMap.keySet() );
    if ( existingName != null ) {
      this.sharedObjectsIO.delete( sharedObjectType, existingName );
      sharedObjectsMap.remove( existingName );
    }
    notifySubscribers();
  }

  @Override
  public synchronized void clear() throws KettleException {
    this.sharedObjectsIO.clear( sharedObjectType );
    reset();
    notifySubscribers();
  }

  /**
   * resets the caches in this manager.
   */
  public synchronized void reset() {
    sharedObjectsMap.clear();
    initialized = false;
  }

  /**
   * Subscribe to changes made to this Manager instance.
   * <p>
   * Note that this implementation uses a WeakReference to retain the connection to the subscriber, so the caller should
   * hold onto this object as long as it needs to be called for changes.
   *
   * @param subscriber
   */
  @Override
  public synchronized void addSubscriber( UpdateSubscriber subscriber ) {
    changeSubscribers.put( subscriber, null );
  }

  private void notifySubscribers() {
    // operate on a copy
    Set<UpdateSubscriber> subs;
    synchronized ( this ) {
      subs = new HashSet<>( changeSubscribers.keySet() );
    }
    for ( UpdateSubscriber subscriber : subs ) {
      if ( subscriber != null ) {
        subscriber.notifyChanged();
      }
    }
  }

  @Override
  public void notifyChanged() {
    reset();
  }

  @Override
  public void clearCache() {
    reset();
  }

}
