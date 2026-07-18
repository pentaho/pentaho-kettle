/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.ui.spoon;

/**
 * Registered implementations will be notified of Spoon startup and shutdown. This class will most likely be registered
 * as part of a SpoonPlugin.
 *
 * @author nbaker
 *
 */
public interface SpoonLifecycleListener {
  public enum SpoonLifeCycleEvent {
    STARTUP, SHUTDOWN, REPOSITORY_CONNECTED, REPOSITORY_CHANGED, REPOSITORY_DISCONNECTED, MENUS_REFRESHED
  }

  void onEvent( SpoonLifeCycleEvent evt );
}
