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
