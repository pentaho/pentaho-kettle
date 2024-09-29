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
