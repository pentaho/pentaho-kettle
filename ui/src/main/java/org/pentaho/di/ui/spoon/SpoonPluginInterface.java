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


package org.pentaho.di.ui.spoon;

import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

public interface SpoonPluginInterface {

  public void applyToContainer( String category, XulDomContainer container ) throws XulException;

  /**
   * Provides an optional SpoonLifecycleListener to be notified of Spoon startup and shutdown.
   *
   * @return optional SpoonLifecycleListener
   */
  public SpoonLifecycleListener getLifecycleListener();

  /**
   * Provides an optional SpoonPerspective.
   *
   * @return optional SpoonPerspective
   */
  public SpoonPerspective getPerspective();
}
