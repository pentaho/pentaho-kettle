/*
 * Copyright 2018-2021 Hitachi Vantara. All rights reserved.
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
 */

package org.pentaho.getfields.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by ddiroma on 7/25/2018.
 */
public class GetFieldsDialog extends ThinDialog {
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private static final String THIN_CLIENT_HOST = "THIN_CLIENT_HOST";
  private static final String THIN_CLIENT_PORT = "THIN_CLIENT_PORT";
  private static final String LOCALHOST = "127.0.0.1";
  private List<String> paths = new ArrayList<>();

  private String title = "";
  private String file;
  private String type = "";

  public GetFieldsDialog( Shell shell, int width, int height, String file, List<String> paths ) {
    super( shell, width, height );
    this.file = file;
    this.paths = paths;
  }

  public List<String> getPaths() {
    return paths;
  }

  public void setPaths( List<String> paths ) {
    this.paths = paths;
  }

  private void disposeBrowser() {
    browser.dispose();
    dialog.close();
    dialog.dispose();
  }

  public void open() {
    StringBuilder clientPath = new StringBuilder();
    clientPath.append( getClientPath() );
    clientPath.append( "#!?path=" );
    clientPath.append( file );
    clientPath.append( "&type=" );
    clientPath.append( type );
    if ( this.paths.size() > 0 ) {
      clientPath.append( "&paths=" );
      clientPath.append( String.join( ",", this.paths ) );
    }
    createDialog( title, getRepoURL( clientPath.toString() ), OPTIONS, LOGO );
    dialog.setMinimumSize( 470, 580 );

    GetFieldsDialog currentDialog = this;
    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        paths = new ArrayList<>();
        if ( Const.isRunningOnWebspoonMode() ) {
          Runnable execute = currentDialog::disposeBrowser;
          display.asyncExec( execute );
        } else {
          disposeBrowser();
        }
        return true;
      }
    };

    new BrowserFunction( browser, "ok" ) {
      @Override public Object function( Object[] arguments ) {
        paths = new ArrayList<>();
        for ( Object path : (Object[]) arguments[0] ) {
          paths.add( (String) path );
        }
        if ( Const.isRunningOnWebspoonMode() ) {
          Runnable execute = currentDialog::disposeBrowser;
          display.asyncExec( execute );
        } else {
          disposeBrowser();
        }
        return true;
      }
    };

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private static String getClientPath() {
    Properties properties = new Properties();
    try ( InputStream inputStream =
            GetFieldsDialog.class.getClassLoader().getResourceAsStream( "project.properties" ) ) {
      properties.load( inputStream );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return properties.getProperty( "CLIENT_PATH" );
  }

  private static String getRepoURL( String path ) {
    if ( Const.isRunningOnWebspoonMode() ) {
      return System.getProperty( "KETTLE_CONTEXT_PATH", "" ) + "/osgi" + path;
    }
    String host;
    Integer port;
    try {
      host = getKettleProperty( THIN_CLIENT_HOST );
      port = Integer.valueOf( getKettleProperty( THIN_CLIENT_PORT ) );
    } catch ( Exception e ) {
      host = LOCALHOST;
      port = getOsgiServicePort();
    }
    return "http://" + host + ":" + port + path;
  }

  private static String getKettleProperty( String propertyName ) throws KettleException {
    // loaded in system properties at startup
    return System.getProperty( propertyName );
  }

  private static Integer getOsgiServicePort() {
    // if no service port is specified try getting it from
    ServerPort osgiServicePort = ServerPortRegistry.getPort( OSGI_SERVICE_PORT );
    if ( osgiServicePort != null ) {
      return osgiServicePort.getAssignedPort();
    }
    return null;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }
}
