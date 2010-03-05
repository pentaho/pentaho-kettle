package org.pentaho.di.ui.repository.capabilities;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PermissionsController;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

public class AclUISupport extends AbstractRepositoryExplorerUISupport {


  @Override
  public void initControllers(Repository rep) throws ControllerInitializationException {

    // If we have Support ACL capability then we need to get the controller and set
    // browser controller's reference and repository directory
    try {
      BrowseController browseController = (BrowseController) container.getEventHandler("browseController"); //$NON-NLS-1$
      PermissionsController permissionsController = (PermissionsController) container.getEventHandler(
          "permissionsController");//$NON-NLS-1$
      permissionsController.setBrowseController(browseController);
      super.initControllers(rep);
    } catch (XulException e) {
      throw new ControllerInitializationException(e);
    }
  }

  @Override
  protected void setup() {
    overlays.add(new DefaultXulOverlay(
    "org/pentaho/di/ui/repository/repositoryexplorer/xul/acl-enabled-layout-overlay.xul")); //$NON-NLS-1$
    PermissionsController permissionsController = new PermissionsController();
    controllerNames.add(permissionsController.getName());
    handlers.add(permissionsController);
  }

}
