/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs;

import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsController;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.AbstractRepositoryExplorerUISupport;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.RepositoryExplorerDefaultXulOverlay;

public class AbsSecurityManagerUISupport extends AbstractRepositoryExplorerUISupport implements java.io.Serializable {

  private static final long serialVersionUID = -3161444723795411324L; /* EESOURCE: UPDATE SERIALVERUID */

  @Override
  public void setup() {
    AbsController absController = new AbsController();
    handlers.add( absController );
    controllerNames.add( absController.getName() );
    overlays
        .add( new RepositoryExplorerDefaultXulOverlay(
            "org/pentaho/di/ui/repository/pur/repositoryexplorer/abs/xul/abs-layout-overlay.xul", AbsSecurityManagerUISupport.class ) ); //$NON-NLS-1$
  }
}
