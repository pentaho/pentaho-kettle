/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.IRepositoryExplorerUISupport;
import org.pentaho.di.ui.spoon.SharedObjectSyncUtil;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

/**
 *
 */
public class RepositoryExplorer {

  private static Log log = LogFactory.getLog( RepositoryExplorer.class );

  private static final Class<?> CLZ = RepositoryExplorer.class;

  private MainController mainController = new MainController();

  private XulDomContainer container;

  private boolean initialized = false;

  private ResourceBundle resourceBundle = new XulSpoonResourceBundle( CLZ );

  public RepositoryExplorer( Shell shell, final Repository rep, RepositoryExplorerCallback callback,
    VariableSpace variableSpace ) throws XulException {
    KettleXulLoader xulLoader = new KettleXulLoader();
    xulLoader.setIconsSize( 24, 24 );
    xulLoader.setOuterContext( shell );
    xulLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
    container =
      xulLoader.loadXul(
        "org/pentaho/di/ui/repository/repositoryexplorer/xul/explorer-layout.xul", resourceBundle );

    SpoonPluginManager.getInstance().applyPluginsForContainer( "repository-explorer", container );

    final XulRunner runner = new SwtXulRunner();
    runner.addContainer( container );

    mainController.setRepository( rep );
    mainController.setCallback( callback );

    container.addEventHandler( mainController );

    List<IRepositoryExplorerUISupport> uiSupportList = new ArrayList<IRepositoryExplorerUISupport>();
    try {
      for ( Class<? extends IRepositoryService> sevice : rep.getServiceInterfaces() ) {
        IRepositoryExplorerUISupport uiSupport = UISupportRegistery.getInstance().createUISupport( sevice );
        if ( uiSupport != null ) {
          uiSupportList.add( uiSupport );
          uiSupport.apply( container );
        }
      }
    } catch ( Exception e ) {
      log.error( resourceBundle.getString( "RepositoryExplorer.ErrorStartingXulApplication" ), e );
      new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(), BaseMessages.getString(
        Spoon.class, "Spoon.Error" ), e.getMessage(), e );
    }
    // Call the init method for all the Active UISupportController
    KettleRepositoryLostException krle = null;
    for ( IRepositoryExplorerUISupport uiSupport : uiSupportList ) {
      try {
        uiSupport.initControllers( rep );
      } catch ( ControllerInitializationException e ) {
        log.error( resourceBundle.getString( "RepositoryExplorer.ErrorStartingXulApplication" ), e );
        krle = KettleRepositoryLostException.lookupStackStrace( e );
        if ( krle == null ) {
          new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(), BaseMessages.getString(
            Spoon.class, "Spoon.Error" ), e.getMessage(), e );
        } else {
          break;
        }
      }
    }

    if ( krle != null ) {
      dispose();
      throw krle;
    }

    try {
      runner.initialize();
    } catch ( XulException e ) {
      log.error( resourceBundle.getString( "RepositoryExplorer.ErrorStartingXulApplication" ), e );
      new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(), BaseMessages.getString(
        Spoon.class, "Spoon.Error" ), e.getMessage(), e );
    }

    initialized = true;
  }

  public RepositoryExplorer( Shell shell, final Repository rep, RepositoryExplorerCallback callback,
    VariableSpace variableSpace, SharedObjectSyncUtil syncUtil ) throws XulException {
    this( shell, rep, callback, variableSpace );
    mainController.setSharedObjectSyncUtil( syncUtil );
  }

  public void show() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById( "repository-explorer-dialog" );
    dialog.show();

  }

  public void dispose() {
    SwtDialog dialog = (SwtDialog) container.getDocumentRoot().getElementById( "repository-explorer-dialog" );
    dialog.dispose();
    initialized = false;
  }

  public boolean isInitialized() {
    return initialized;
  }

}
