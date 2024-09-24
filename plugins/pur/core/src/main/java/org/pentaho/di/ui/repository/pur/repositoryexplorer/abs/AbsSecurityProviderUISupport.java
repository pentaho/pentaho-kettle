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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs;

import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsClustersController;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsConnectionsController;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsContextMenuController;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsPartitionsController;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsSlavesController;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.AbstractRepositoryExplorerUISupport;

public class AbsSecurityProviderUISupport extends AbstractRepositoryExplorerUISupport implements java.io.Serializable {

  private static final long serialVersionUID = -5965263581796252745L; /* EESOURCE: UPDATE SERIALVERUID */

  @Override
  public void setup() {
    AbsConnectionsController absConnectionsController = new AbsConnectionsController();
    AbsPartitionsController absPartitionsController = new AbsPartitionsController();
    AbsSlavesController absSlavesController = new AbsSlavesController();
    AbsClustersController absClustersController = new AbsClustersController();
    AbsContextMenuController absContextMenuController = new AbsContextMenuController();
    handlers.add( absConnectionsController );
    controllerNames.add( absConnectionsController.getName() );
    handlers.add( absPartitionsController );
    controllerNames.add( absPartitionsController.getName() );
    handlers.add( absSlavesController );
    controllerNames.add( absSlavesController.getName() );
    handlers.add( absClustersController );
    controllerNames.add( absClustersController.getName() );
    handlers.add( absContextMenuController );
    controllerNames.add( absContextMenuController.getName() );
  }
}
