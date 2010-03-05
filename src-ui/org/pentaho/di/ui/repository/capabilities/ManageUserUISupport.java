package org.pentaho.di.ui.repository.capabilities;

import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SecurityController;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

public class ManageUserUISupport extends AbstractRepositoryExplorerUISupport{

  @Override
  protected void setup() {
    SecurityController securityController = new SecurityController();
    controllerNames.add(securityController.getName());
    handlers.add(securityController);
    overlays.add(new DefaultXulOverlay("org/pentaho/di/ui/repository/repositoryexplorer/xul/security-enabled-layout-overlay.xul")); //$NON-NLS-1$
  }
}
