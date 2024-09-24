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
