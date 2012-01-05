/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

/**
 * Registered implementations will be notified of Spoon startup and shutdown. This class will
 * most likely be registered as part of a SpoonPlugin.
 * 
 * @author nbaker
 *
 */
public interface SpoonLifecycleListener {
  public enum SpoonLifeCycleEvent{STARTUP, SHUTDOWN, REPOSITORY_CONNECTED, REPOSITORY_CHANGED, REPOSITORY_DISCONNECTED, MENUS_REFRESHED};
  void onEvent(SpoonLifeCycleEvent evt);
}
