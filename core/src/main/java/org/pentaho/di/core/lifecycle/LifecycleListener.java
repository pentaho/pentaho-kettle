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
