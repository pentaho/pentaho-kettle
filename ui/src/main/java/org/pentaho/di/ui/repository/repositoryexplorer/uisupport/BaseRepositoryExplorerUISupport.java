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

import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ClustersController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ConnectionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PartitionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SlavesController;

public class BaseRepositoryExplorerUISupport extends AbstractRepositoryExplorerUISupport {

  @Override
  protected void setup() {
    BrowseController browseController = new BrowseController();
    ConnectionsController connectionsController = new ConnectionsController();
    PartitionsController partitionsController = new PartitionsController();
    SlavesController slavesController = new SlavesController();
    ClustersController clustersController = new ClustersController();

    handlers.add( browseController );
    controllerNames.add( browseController.getName() );
    handlers.add( connectionsController );
    controllerNames.add( connectionsController.getName() );
    handlers.add( partitionsController );
    controllerNames.add( partitionsController.getName() );
    handlers.add( slavesController );
    controllerNames.add( slavesController.getName() );
    handlers.add( clustersController );
    controllerNames.add( clustersController.getName() );
  }

}
