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


package org.pentaho.di.ui.repo.plugin;

import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.menu.RepositoryConnectMenu;
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

  static RepositoryConnectController getRepoControllerInstance(){
    return RepositoryConnectController.getInstance();
  }

  public RepositorySpoonPlugin() {
    this.repositoryConnectController = getRepoControllerInstance();
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
