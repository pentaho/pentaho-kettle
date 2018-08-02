/*
 * Copyright 2018 Hitachi Vantara. All rights reserved.
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

package org.pentaho.di.ui.trans.steps.jsoninput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ddiroma on 7/25/2018.
 */
public class JsonInputGetFieldsDialog extends ThinDialog {
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String TITLE = "JSON Input";
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private static final String THIN_CLIENT_HOST = "THIN_CLIENT_HOST";
  private static final String THIN_CLIENT_PORT = "THIN_CLIENT_PORT";
  private static final String LOCALHOST = "localhost";

  public JsonInputGetFieldsDialog( Shell shell, int width, int height ) {
    super( shell, width, height );
  }

  public void open() {
    StringBuilder clientPath = new StringBuilder();
    clientPath.append( getClientPath() );
    super.createDialog( TITLE, getRepoURL( clientPath.toString() ), OPTIONS, LOGO );
    super.dialog.setMinimumSize( 435, 580 );
    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private static String getClientPath() {
    Properties properties = new Properties();
    try ( InputStream inputStream =
           JsonInputGetFieldsDialog.class.getClassLoader().getResourceAsStream( "project.properties" ) ) {
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
}
