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
