/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
 * @author rmansoor
 *
 */
public interface IRepositoryExplorerUISupport {
   /**
   * Adds and apply the overlays and the event handlers to the xul dom container 
   * @param xul dom container
   * @throws XulException
   */
  public void apply(XulDomContainer container) throws XulException ;
  /**
   * Get the list of event handlers added to the list of event handlers
   * @return list of event handlers
   */
  public List<XulEventHandler> getEventHandlers();
  /**
   * Get the list of overlays for the UI Support
   * @return
   */
  public List<XulOverlay> getOverlays();
  /**
   * Initialize the controller
   * @throws ControllerInitializationException
   */
  public void initControllers(Repository rep) throws ControllerInitializationException;
}
