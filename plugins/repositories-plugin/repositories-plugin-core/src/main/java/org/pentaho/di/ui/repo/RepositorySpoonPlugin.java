/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repo;

import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulToolbar;

@org.pentaho.di.ui.spoon.SpoonPlugin( id = "repositories-plugin", image = "" )
@SpoonPluginCategories( { "spoon" } )
public class RepositorySpoonPlugin implements SpoonPluginInterface {

  private static final String SPOON_CATEGORY = "spoon";

  private RepositoryConnectController repositoryConnectController;

  public RepositorySpoonPlugin( RepositoryConnectController repositoryConnectController ) {
    this.repositoryConnectController = repositoryConnectController;
  }

  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    if ( category.equals( SPOON_CATEGORY ) ) {
      XulToolbar toolbar = (XulToolbar) container.getDocumentRoot().getElementById( "main-toolbar" );
      RepositoryConnectMenu repoConnectMenu =
        new RepositoryConnectMenu( Spoon.getInstance(), (ToolBar) toolbar.getManagedObject(),
          repositoryConnectController );
      repoConnectMenu.render();
    }
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {

    return new SpoonLifecycleListener() {
      @Override
      public void onEvent( SpoonLifeCycleEvent evt ) {
        switch ( evt ) {
          case REPOSITORY_DISCONNECTED:
            repositoryConnectController.fireListeners();
            break;
          default:
        }
      }

    };
  }

  @Override
  public SpoonPerspective getPerspective() {
    // no perspective
    return null;
  }

  // destroy-method in blueprint xml
  public void removeFromContainer() throws XulException {
    // create removal code
  }
}
