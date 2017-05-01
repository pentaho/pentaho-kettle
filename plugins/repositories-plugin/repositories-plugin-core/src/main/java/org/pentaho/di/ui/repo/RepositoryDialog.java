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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

/**
 * Created by bmorrise on 2/21/16.
 */
public class RepositoryDialog extends ThinDialog {

  public static final String HELP_URL = Const.getDocUrl( "0L0/0Y0/040" );
  private LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryDialog.class );

  private static Class<?> PKG = RepositoryConnectMenu.class;

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM;
  private static final String CREATION_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.NewRepo.Title" );
  private static final String CREATION_WEB_CLIENT_PATH = "/repositories/web/index.html";
  private static final String MANAGER_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Manager.Title" );
  private static final String MANAGER_WEB_CLIENT_PATH = "/repositories/web/index.html#repository-manager";
  private static final String LOGIN_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Login.Title" );
  private static final String LOGIN_WEB_CLIENT_PATH = "/repositories/web/index.html#repository-connect";
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
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

    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        browser.dispose();
        dialog.close();
        dialog.dispose();
        return true;
      }
    };

    new BrowserFunction( browser, "setResult" ) {
      @Override public Object function( Object[] arguments ) {
        setResult( (boolean) arguments[ 0 ] );
        return true;
      }
    };

    new BrowserFunction( browser, "getErrorMessage" ) {
      @Override public Object function( Object[] objects ) {
        return errorMessage == null ? "" : errorMessage;
      }
    };

    new BrowserFunction( browser, "help" ) {
      @Override public Object function( Object[] objects ) {
        HelpUtils.openHelpDialog( shell, BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Tile" ), HELP_URL,
          BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Header" ) );
        return true;
      }
    };

    new BrowserFunction( browser, "getRepositories" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getRepositories();
      }
    };

    new BrowserFunction( browser, "getRepositoryTypes" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getPlugins();
      }
    };

    new BrowserFunction( browser, "deleteRepository" ) {
      @Override public Object function( Object[] objects ) {
        return controller.deleteRepository( (String) objects[ 0 ] );
      }
    };

    new BrowserFunction( browser, "selectLocation" ) {
      @Override public Object function( Object[] objects ) {
        DirectoryDialog directoryDialog = new DirectoryDialog( shell );
        return directoryDialog.open();
      }
    };

    new BrowserFunction( browser, "connectToRepository" ) {
      @Override public Object function( Object[] objects ) {
        try {
          controller.connectToRepository();
          dialog.dispose();
        } catch ( KettleException e ) {
          return false;
        }
        return true;
      }
    };

    new BrowserFunction( browser, "setDefaultRepository" ) {
      @Override public Object function( Object[] objects ) {
        return controller.setDefaultRepository( (String) objects[ 0 ] );
      }
    };

    new BrowserFunction( browser, "getDatabases" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getDatabases();
      }
    };

    new BrowserFunction( browser, "createNewConnection" ) {
      @Override public Object function( Object[] objects ) {
        DatabaseDialog databaseDialog = new DatabaseDialog( shell, new DatabaseMeta() );
        databaseDialog.open();
        DatabaseMeta databaseMeta = databaseDialog.getDatabaseMeta();
        if ( databaseMeta != null ) {
          if ( !controller.isDatabaseWithNameExist( databaseMeta, true ) ) {
            controller.addDatabase( databaseMeta );
            return true;
          } else {
            DatabaseDialog.showDatabaseExistsDialog( shell, databaseMeta );
          }
        }
        return false;
      }
    };

    new BrowserFunction( browser, "editDatabaseConnection" ) {
      @Override public Object function( Object[] objects ) {
        DatabaseMeta databaseMeta = controller.getDatabase( (String) objects[ 0 ] );
        String originalName = databaseMeta.getName();
        DatabaseDialog databaseDialog = new DatabaseDialog( shell, databaseMeta );
        databaseDialog.open();
        if ( !controller.isDatabaseWithNameExist( databaseMeta, false ) ) {
          controller.save();
          return databaseMeta.getName();
        } else {
          DatabaseDialog.showDatabaseExistsDialog( shell, databaseMeta );
          databaseMeta.setName( originalName );
          databaseMeta.setDisplayName( originalName );
          return originalName;
        }
      }
    };

    new BrowserFunction( browser, "deleteDatabaseConnection" ) {
      @Override public Object function( Object[] objects ) {
        controller.removeDatabase( (String) objects[ 0 ] );
        return true;
      }
    };

    new BrowserFunction( browser, "reset" ) {
      @Override public Object function( Object[] objects ) {
        controller.setCurrentRepository( null );
        controller.setRelogin( false );
        return true;
      }
    };

    new BrowserFunction( browser, "getCurrentUser" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getCurrentUser();
      }
    };

    new BrowserFunction( browser, "getCurrentRepository" ) {
      @Override
      public Object function( Object[] objects ) {
        return controller.getCurrentRepository() != null ? controller.getCurrentRepository().getName() : "";
      }
    };

    new BrowserFunction( browser, "getDefaultUrl" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getDefaultUrl();
      }
    };

    new BrowserFunction( browser, "loadRepository" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getRepository( (String) objects[ 0 ] );
      }
    };

    new BrowserFunction( browser, "getConnectedRepositoryName" ) {
      @Override public Object function( Object[] objects ) {
        return controller.getConnectedRepository() != null ? controller.getConnectedRepository().getName() : "";
      }
    };

    controller.setCurrentRepository( repositoryMeta );
    controller.setRelogin( relogin );

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return result;
  }

  public void openManager() {
    super.createDialog( MANAGER_TITLE, getRepoURL( MANAGER_WEB_CLIENT_PATH ), OPTIONS, LOGO );
    open();
  }

  public void openCreation() {
    super.createDialog( CREATION_TITLE, getRepoURL( CREATION_WEB_CLIENT_PATH ), OPTIONS, LOGO );
    open();
  }

  public boolean openRelogin( RepositoryMeta repositoryMeta, String errorMessage ) {
    super.createDialog( LOGIN_TITLE, getRepoURL( LOGIN_WEB_CLIENT_PATH ), OPTIONS, LOGO );
    return open( repositoryMeta, true, errorMessage );
  }

  public boolean openLogin( RepositoryMeta repositoryMeta ) {
    super.createDialog( LOGIN_TITLE, getRepoURL( LOGIN_WEB_CLIENT_PATH ), OPTIONS, LOGO );
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

  private static String getRepoURL( String path ) {
    return "http://localhost:" + getOsgiServicePort() + path;
  }
}
