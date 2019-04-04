/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.uisupport;

import java.io.Serializable;

import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.controller.ConnectionPermissionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.AbstractRepositoryExplorerUISupport;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.RepositoryExplorerDefaultXulOverlay;

/**
 * This support class registered the new ConnectionPermissionsController and xul overlay for managing Database
 * Connection Permissions in the UI.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
public class ConnectionAclUISupport extends AbstractRepositoryExplorerUISupport implements Serializable {

  private static final long serialVersionUID = -1381250312418398039L; /* EESOURCE: UPDATE SERIALVERUID */

  @Override
  protected void setup() {
    overlays.add( new RepositoryExplorerDefaultXulOverlay(
        "org/pentaho/di/ui/repository/pur/repositoryexplorer/xul/connection-acl-overlay.xul", IUIEEUser.class ) ); //$NON-NLS-1$
    ConnectionPermissionsController connAclController = new ConnectionPermissionsController();
    controllerNames.add( connAclController.getName() );
    handlers.add( connAclController );
  }

}
