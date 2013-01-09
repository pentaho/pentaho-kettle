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
