/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.core.bowl;


/**
 * This indicates that the implementing manager can both react to changes and can have others subscribe to changes,
 * primarily for cache invalidation.
 * <p>
 * See CachingManagerFactory for how to use this with BowlManagerFactoryRegistry
 */
public interface CachingManager {

  /**
   * Adds a subscriber that should be notified when the members of this manager are changed.
   *
   * @param subscriber an UpdateSubscriber (not necessarily another CachingManager) that should be notified
   */
  void addSubscriber( UpdateSubscriber subscriber );

  /**
   * Notify this manager that changes in another manager have happened.
   *
   */
  void notifyChanged();

  /**
   * clearCache is called to clear any cached data held by this manager.
   * This may have slightly different behavior than notifyChanged, so the APIs are separate.
   */
  void clearCache();
}
