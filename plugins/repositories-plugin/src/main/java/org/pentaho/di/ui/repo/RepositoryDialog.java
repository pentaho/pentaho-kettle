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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.thin.ThinDialog;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.util.HashMap;

/**
 * Created by bmorrise on 2/21/16.
 */
public class RepositoryDialog extends ThinDialog {

  private static Log log = LogFactory.getLog( RepositoryDialog.class );

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static final String CREATION_TITLE = "New Repository Connection";
  private static final String CREATION_WEB_CLIENT_PATH = "/repositories/web/index.html";
  private static final String MANAGER_TITLE = "Repository Manager";
  private static final String MANAGER_WEB_CLIENT_PATH = "/repositories/web/index.html#repository-manager";
  private static final String LOGIN_TITLE = "Login to Repository";
  private static final String LOGIN_WEB_CLIENT_PATH = "/repositories/web/index.html#pentaho-repository-connect";
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private RepositoryConnectController controller;
  private Shell shell;

  public RepositoryDialog( Shell shell, RepositoryConnectController controller ) {
    super( shell, WIDTH, HEIGHT );
    this.controller = controller;
    this.shell = shell;
  }

  private void open() {
    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        dialog.dispose();
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
          log.error( "Unable to load repository json object", e );
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
        controller.connectToRepository( username, password );
        return true;
      }
    };

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  public void openManager() {
    super.createDialog( MANAGER_TITLE, getRepoURL( MANAGER_WEB_CLIENT_PATH ) );
    open();
  }

  public void openCreation() {
    super.createDialog( CREATION_TITLE, getRepoURL( CREATION_WEB_CLIENT_PATH ) );
    open();
  }

  public void openLogin( RepositoryMeta repositoryMeta ) {
    super.createDialog( LOGIN_TITLE, getRepoURL( LOGIN_WEB_CLIENT_PATH ) );
    controller.setCurrentRepository( repositoryMeta );
    open();
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
