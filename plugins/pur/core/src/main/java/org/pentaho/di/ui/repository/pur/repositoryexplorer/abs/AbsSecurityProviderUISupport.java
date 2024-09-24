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
