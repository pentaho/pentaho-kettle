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
 * A callback to be notified when the Kettle environment is initialized and shut down.
 */
public interface KettleLifecycleListener {
  /**
   * Called during KettleEnvironment initialization.
   *
   * @throws LifecycleException
   *           to indicate the listener did not complete successfully. Severe {@link LifecycleException}s will stop the
   *           initialization of the KettleEnvironment.
   */
  void onEnvironmentInit() throws org.pentaho.di.core.lifecycle.LifecycleException;

  /**
   * Called when the VM that initialized KettleEnvironment terminates.
   */
  void onEnvironmentShutdown();
}
