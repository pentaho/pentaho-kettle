/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RepositoryDialog extends ThinDialog {

  private LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryDialog.class );

  private static Class<?> PKG = RepositoryDialog.class;

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM;
  private static final String CREATION_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.NewRepo.Title" );
  private static final String CREATION_WEB_CLIENT_PATH = "#!/add";
  private static final String MANAGER_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Manager.Title" );
  private static final String LOGIN_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Login.Title" );
  private static final String LOGIN_WEB_CLIENT_PATH = "#!/connect";
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private static final String THIN_CLIENT_HOST = "THIN_CLIENT_HOST";
  private static final String THIN_CLIENT_PORT = "THIN_CLIENT_PORT";
  private static final String LOCALHOST = "127.0.0.1";
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();


  private RepositoryConnectController controller;
  private Shell shell;
  private boolean result = false;

  public RepositoryDialog( Shell shell, RepositoryConnectController controller ) {
    super( shell, WIDTH, HEIGHT );
    this.controller = controller;
    this.shell = shell;
  }

  private boolean open() {
    return open( null );
  }

  private boolean open( RepositoryMeta repositoryMeta ) {
    return open( repositoryMeta, false, null );
  }

  private boolean open( RepositoryMeta repositoryMeta, boolean relogin, String errorMessage ) {

    new BrowserFunction( browser, "closeWindow" ) {
      @Override public Object function( Object[] arguments ) {
        browser.dispose();
        dialog.close();
        dialog.dispose();
        return true;
      }
    };

    controller.setCurrentRepository( repositoryMeta );
    controller.setRelogin( relogin );
    controller.setParentShell( dialog );

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return result;
  }

  public void openManager() {
    super.createDialog( MANAGER_TITLE, getRepoURL( "" ), OPTIONS, LOGO );
    open();
  }

  public void openCreation() {
    super.createDialog( CREATION_TITLE, getRepoURL( CREATION_WEB_CLIENT_PATH ), OPTIONS, LOGO );
    open();
  }

  public boolean openRelogin( RepositoryMeta repositoryMeta, String errorMessage ) {
    super.createDialog( LOGIN_TITLE, getRepoURL( LOGIN_WEB_CLIENT_PATH ) + "/" + repositoryMeta.getName(), OPTIONS, LOGO );
    return open( repositoryMeta, true, errorMessage );
  }

  public boolean openLogin( RepositoryMeta repositoryMeta ) {
    super.createDialog( LOGIN_TITLE, getRepoURL( LOGIN_WEB_CLIENT_PATH ) + "/" + repositoryMeta.getName(), OPTIONS, LOGO );
    return open( repositoryMeta );
  }

  private void setResult( boolean result ) {
    this.result = result;
  }

  private static Integer getOsgiServicePort() {
    // if no service port is specified try getting it from
    ServerPort osgiServicePort = ServerPortRegistry.getPort( OSGI_SERVICE_PORT );
    if ( osgiServicePort != null ) {
      return osgiServicePort.getAssignedPort();
    }
    return null;
  }

  private static String getClientPath() {
    Properties properties = new Properties();
    try {
      InputStream inputStream =
        RepositoryDialog.class.getClassLoader().getResourceAsStream( "project.properties" );
      properties.load( inputStream );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return properties.getProperty( "CLIENT_PATH" );
  }

  private static String getRepoURL( String path ) {
    String host;
    Integer port;
    try {
      host = getKettleProperty( THIN_CLIENT_HOST );
      port = Integer.valueOf( getKettleProperty( THIN_CLIENT_PORT ) );
    } catch ( Exception e ) {
      host = LOCALHOST;
      port = getOsgiServicePort();
    }
    return "http://" + host + ":" + port + getClientPath() + path;
  }

  private static String getKettleProperty( String propertyName ) throws KettleException {
    // loaded in system properties at startup
    return System.getProperty( propertyName );
  }

}
