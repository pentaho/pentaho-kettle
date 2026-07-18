/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.ui.repository.repositoryexplorer.uisupport;

import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SecurityController;

public class ManageUserUISupport extends AbstractRepositoryExplorerUISupport {

  @Override
  protected void setup() {
    SecurityController securityController = new SecurityController();
    controllerNames.add( securityController.getName() );
    handlers.add( securityController );
    overlays.add( new RepositoryExplorerDefaultXulOverlay(
      "org/pentaho/di/ui/repository/repositoryexplorer/xul/security-enabled-layout-overlay.xul",
      RepositoryExplorer.class ) );
  }
}
