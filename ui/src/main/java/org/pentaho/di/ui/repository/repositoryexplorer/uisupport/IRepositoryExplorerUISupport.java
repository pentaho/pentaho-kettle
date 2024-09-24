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

package org.pentaho.di.ui.repository.repositoryexplorer.uisupport;

import java.util.List;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * UI Support interface to provide list of event handlers and overlay for a given capability or service
 *
 * @author rmansoor
 *
 */
public interface IRepositoryExplorerUISupport {
  /**
   * Adds and apply the overlays and the event handlers to the xul dom container
   *
   * @param xul
   *          dom container
   * @throws XulException
   */
  public void apply( XulDomContainer container ) throws XulException;

  /**
   * Get the list of event handlers added to the list of event handlers
   *
   * @return list of event handlers
   */
  public List<XulEventHandler> getEventHandlers();

  /**
   * Get the list of overlays for the UI Support
   *
   * @return
   */
  public List<XulOverlay> getOverlays();

  /**
   * Initialize the controller
   *
   * @throws ControllerInitializationException
   */
  public void initControllers( Repository rep ) throws ControllerInitializationException;
}
