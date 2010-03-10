package org.pentaho.di.ui.repository.capabilities;

import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PermissionsController;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

public class AclUISupport extends AbstractRepositoryExplorerUISupport {

  @Override
  protected void setup() {
    overlays.add(new DefaultXulOverlay(
    "org/pentaho/di/ui/repository/repositoryexplorer/xul/acl-enabled-layout-overlay.xul")); //$NON-NLS-1$
    PermissionsController permissionsController = new PermissionsController();
    controllerNames.add(permissionsController.getName());
    handlers.add(permissionsController);
  }

}
