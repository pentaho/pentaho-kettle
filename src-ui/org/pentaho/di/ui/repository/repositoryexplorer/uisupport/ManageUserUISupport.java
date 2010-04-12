package org.pentaho.di.ui.repository.repositoryexplorer.uisupport;

import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SecurityController;

public class ManageUserUISupport extends AbstractRepositoryExplorerUISupport{

  @Override
  protected void setup() {
    SecurityController securityController = new SecurityController();
    controllerNames.add(securityController.getName());
    handlers.add(securityController);
    overlays.add(new RepositoryExplorerDefaultXulOverlay("org/pentaho/di/ui/repository/repositoryexplorer/xul/security-enabled-layout-overlay.xul", RepositoryExplorer.class)); //$NON-NLS-1$
  }
}
