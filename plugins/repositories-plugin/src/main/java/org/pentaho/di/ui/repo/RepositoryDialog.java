/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.util.HashMap;

/**
 * Created by bmorrise on 2/21/16.
 */
public class RepositoryDialog extends ThinDialog {

  public static final String HELP_URL = "https://help.pentaho.com/Documentation/7.0/0L0/0Y0/040";
  private LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryDialog.class );

  private static Class<?> PKG = RepositoryConnectMenu.class;

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM;
  private static final String CREATION_TITLE = "New Repository Connection";
  private static final String CREATION_WEB_CLIENT_PATH = "/repositories/web/index.html";
  private static final String MANAGER_TITLE = "Repository Manager";
  private static final String MANAGER_WEB_CLIENT_PATH = "/repositories/web/index.html#repository-manager";
  private static final String LOGIN_TITLE = "Login to Repository";
  private static final String LOGIN_WEB_CLIENT_PATH = "/repositories/web/index.html#repository-connect";
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";


  private RepositoryConnectController controller;
  private Shell shell;

  public RepositoryDialog( Shell shell, RepositoryConnectController controller ) {
    super( shell, WIDTH, HEIGHT );
    this.controller = controller;
    this.shell = shell;
  }

  private void open() {
    open( null );
  }

  private void open( RepositoryMeta repositoryMeta ) {

    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        browser.dispose();
        dialog.close();
        dialog.dispose();
        return true;
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

    new BrowserFunction( browser, "createRepository" ) {
      @SuppressWarnings( "unchecked" )
      @Override public Object function( Object[] objects ) {
        try {
          return controller.createRepository( (String) objects[ 0 ],
            new ObjectMapper().readValue( (String) objects[ 1 ], HashMap.class ) );
        } catch ( Exception e ) {
          log.logError( "Unable to load repository json object", e );
        }
        return false;
      }
    };

    new BrowserFunction( browser, "connectToRepository" ) {
      @Override public Object function( Object[] objects ) {
        controller.connectToRepository();
        dialog.dispose();
        return true;
      }
    };

    new BrowserFunction( browser, "setDefaultRepository" ) {
      @Override public Object function( Object[] objects ) {
        return controller.setDefaultRepository( (String) objects[ 0 ] );
      }
    };

    new BrowserFunction( browser, "loginToRepository" ) {
      @Override public Object function( Object[] objects ) {
        String username = (String) objects[ 0 ];
        String password = (String) objects[ 1 ];
        return controller.connectToRepository( username, password );
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
          controller.addDatabase( databaseMeta );
        }
        return true;
      }
    };

    new BrowserFunction( browser, "editDatabaseConnection" ) {
      @Override public Object function( Object[] objects ) {
        DatabaseMeta databaseMeta = controller.getDatabase( (String) objects[ 0 ] );
        DatabaseDialog databaseDialog = new DatabaseDialog( shell, databaseMeta );
        databaseDialog.open();
        controller.save();
        return databaseMeta.getName();
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
        return controller.getCurrentRepository().getName();
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

    controller.setCurrentRepository( repositoryMeta );

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  public void openManager() {
    super.createDialog( MANAGER_TITLE, getRepoURL( MANAGER_WEB_CLIENT_PATH ), OPTIONS );
    open();
  }

  public void openCreation() {
    super.createDialog( CREATION_TITLE, getRepoURL( CREATION_WEB_CLIENT_PATH ), OPTIONS );
    open();
  }

  public void openLogin( RepositoryMeta repositoryMeta ) {
    super.createDialog( LOGIN_TITLE, getRepoURL( LOGIN_WEB_CLIENT_PATH ), OPTIONS );
    open( repositoryMeta );
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
