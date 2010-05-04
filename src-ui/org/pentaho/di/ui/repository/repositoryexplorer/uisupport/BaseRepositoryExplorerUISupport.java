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

import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ClustersController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ConnectionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PartitionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SlavesController;

public class BaseRepositoryExplorerUISupport extends AbstractRepositoryExplorerUISupport{

  @Override
  protected void setup() {
    BrowseController browseController = new BrowseController();
    ConnectionsController connectionsController = new ConnectionsController();
    PartitionsController partitionsController = new PartitionsController();
    SlavesController slavesController = new SlavesController();
    ClustersController clustersController = new ClustersController();
    
    handlers.add(browseController);
    controllerNames.add(browseController.getName());
    handlers.add(connectionsController);
    controllerNames.add(connectionsController.getName());
    handlers.add(partitionsController);
    controllerNames.add(partitionsController.getName());
    handlers.add(slavesController);
    controllerNames.add(slavesController.getName());
    handlers.add(clustersController);
    controllerNames.add(clustersController.getName());
  }

}
