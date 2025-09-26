package org.pentaho.di.core.bowl;

import org.pentaho.di.core.exception.KettleException;

import java.util.WeakHashMap;

/**
 * Wraps a base factory to apply cache invalidation for updates from managers in parent bowls.
 *
 *
 * @param <T>
 */
public class CachingManagerFactory<T extends CachingManager> implements ManagerFactory<T> {

  // need to hang onto the subscriber as long as the Bowl exists
  private final WeakHashMap<Bowl, UpdateSubscriber> subscribers = new WeakHashMap<>();

  private final Class<T> clazz;
  private final ManagerFactory<T> baseFactory;

  public CachingManagerFactory( Class<T> clazz, ManagerFactory<T> baseFactory ) {
    this.clazz = clazz;
    this.baseFactory = baseFactory;
  }

  public T apply( Bowl bowl ) throws KettleException {
    T manager = baseFactory.apply( bowl );
    if ( !bowl.getParentBowls().isEmpty() ) {
      UpdateSubscriber subscriber = manager::notifyChanged;
      for ( Bowl parentBowl : bowl.getParentBowls() ) {
        parentBowl.getManager( clazz ).addSubscriber( subscriber );
      }
      subscribers.put( bowl, subscriber );
    }
    return manager;
  }
}
