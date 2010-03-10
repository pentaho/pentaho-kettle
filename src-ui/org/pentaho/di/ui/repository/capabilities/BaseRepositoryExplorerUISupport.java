package org.pentaho.di.ui.repository.capabilities;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ClustersController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ConnectionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PartitionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PermissionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SlavesController;
import org.pentaho.ui.xul.XulException;

public class BaseRepositoryExplorerUISupport extends AbstractRepositoryExplorerUISupport{

  
  @Override
  public void initControllers(Repository rep) throws ControllerInitializationException {
   
    // Browser Controller needs a reference of main controller
    try {
      MainController mainController = (MainController) container.getEventHandler("mainController"); //$NON-NLS-1$
      BrowseController browseController = (BrowseController) container.getEventHandler(
          "browseController");//$NON-NLS-1$
      browseController.setMainController(mainController);
      super.initControllers(rep);
    } catch (XulException e) {
      throw new ControllerInitializationException(e);
    }
  }

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
