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

package org.pentaho.di.ui.repository;

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.repository.controllers.RepositoriesController;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class RepositoriesDialog {
  private static final Class<?> CLZ = RepositoriesDialog.class;
  private static Log log = LogFactory.getLog( RepositoriesDialog.class );
  private RepositoriesController repositoriesController = new RepositoriesController();
  private XulDomContainer container;
  private ILoginCallback callback;
  private ResourceBundle resourceBundle = new XulSpoonResourceBundle( CLZ );

  public RepositoriesDialog( Shell shell, String preferredRepositoryName, ILoginCallback callback ) {
    try {
      this.callback = callback;
      KettleXulLoader xulLoader = new KettleXulLoader();
      xulLoader.setOuterContext( shell );
      xulLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      container = xulLoader.loadXul( "org/pentaho/di/ui/repository/xul/repositories.xul", resourceBundle );
      final XulRunner runner = new SwtXulRunner();
      runner.addContainer( container );

      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument( container.getDocumentRoot() );
      repositoriesController.setBindingFactory( bf );
      repositoriesController.setPreferredRepositoryName( preferredRepositoryName );
      repositoriesController.setMessages( resourceBundle );
      repositoriesController.setCallback( callback );
      repositoriesController.setShell( getShell() );
      container.addEventHandler( repositoriesController );

      try {
        runner.initialize();
      } catch ( XulException e ) {
        SpoonFactory.getInstance().messageBox(
          e.getLocalizedMessage(), "Service Initialization Failed", false, Const.ERROR );
        log.error( resourceBundle.getString( "RepositoryLoginDialog.ErrorStartingXulApplication" ), e );
      }
    } catch ( XulException e ) {
      log.error( resourceBundle.getString( "RepositoryLoginDialog.ErrorLoadingXulApplication" ), e );
    }
  }

  public Composite getDialogArea() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById( "repository-login-dialog" );
    return (Composite) dialog.getManagedObject();
  }

  public void show() {
    repositoriesController.show();
  }

  public ILoginCallback getCallback() {
    return callback;
  }

  public Shell getShell() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById( "repository-login-dialog" );
    return (Shell) dialog.getRootObject();
  }
}
