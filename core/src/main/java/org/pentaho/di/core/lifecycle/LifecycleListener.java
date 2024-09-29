/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.lifecycle;

/**
 * A callback interface that listens to specific lifecycle events triggered when Spoon starts and stops.
 *
 * Listeners are loaded dynamically by PDI. In order to register a listener with Spoon, a class that implements this
 * interface must be placed in the "org.pentaho.di.core.listeners.pdi" package, and it will be loaded automatically when
 * Spoon starts.
 *
 * @author Alex Silva
 *
 */
public interface LifecycleListener {
  /**
   * Called when the application starts.
   *
   * @throws LifecycleException
   *           Whenever this listener is unable to start succesfully.
   */
  public void onStart( LifeEventHandler handler ) throws LifecycleException;

  /**
   * Called when the application ends
   *
   * @throws LifecycleException
   *           If a problem prevents this listener from shutting down.
   */
  public void onExit( LifeEventHandler handler ) throws LifecycleException;

}
