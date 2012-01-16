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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

public abstract class AbstractRepositoryExplorerUISupport implements IRepositoryExplorerUISupport{
  protected List<XulEventHandler> handlers;
  protected List<XulOverlay> overlays;
  protected XulDomContainer container;
  protected List<String> controllerNames;

  public AbstractRepositoryExplorerUISupport() {
    super();
    handlers = new ArrayList<XulEventHandler>();
    overlays = new ArrayList<XulOverlay>();
    controllerNames = new ArrayList<String>();
    setup();
  }

  public void apply(XulDomContainer container) throws XulException {
    this.container = container;
    container.registerClassLoader(getClass().getClassLoader());
    for(XulEventHandler handler:handlers) {
      container.addEventHandler(handler);
    }
    for (XulOverlay overlay : overlays) {
      if(overlay instanceof RepositoryExplorerDefaultXulOverlay) {
        container.loadOverlay(overlay.getOverlayUri(), new XulSpoonResourceBundle(((RepositoryExplorerDefaultXulOverlay) overlay).getPackageClass()));
      } else {
        container.loadOverlay(overlay.getOverlayUri(), overlay.getResourceBundleUri());  
      }
    }
  }

  public List<XulEventHandler> getEventHandlers() {
    return handlers;
  }

  public List<XulOverlay> getOverlays() {
    return overlays;
  }

  public void initControllers(Repository rep) throws ControllerInitializationException {
    for(String name:controllerNames) {
      try {
        IUISupportController controller = (IUISupportController)
           container.getEventHandler(name);
        controller.init(rep);
      } catch (XulException e) {
        throw new ControllerInitializationException(e);
      }
    }
  }
  /**
   * Setup the event handlers and the overlays for a service or capability
   */
  protected abstract void setup();
}
