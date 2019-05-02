/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.dialog;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by bmorrise on 5/23/17.
 */
public class FileOpenSaveDialog extends ThinDialog {

  public static final String STATE_SAVE = "save";
  public static final String STATE_OPEN = "open";
  public static final String SELECT_FOLDER = "selectFolder";
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String THIN_CLIENT_HOST = "THIN_CLIENT_HOST";
  private static final String THIN_CLIENT_PORT = "THIN_CLIENT_PORT";
  private static final String LOCALHOST = "localhost";
  public static final String PATH = "path";
  public static final String CONNECTION = "connection";
  public static final String PROVIDER = "provider";
  public static final String FILTER = "filter";
  public static final String ORIGIN = "origin";
  public static final String FILENAME = "filename";
  public static final String FILE_TYPE = "fileType";

  private String objectId;
  private String name;
  private String path;
  private String parentPath;
  private String type;
  private String connection;
  private String provider;

  public FileOpenSaveDialog( Shell shell, int width, int height ) {
    super( shell, width, height );
  }

  public void open( String path, String connection, String provider, String state, String title, String filter,
                    String origin ) {
    open( path, state, title, filter, origin, null, "" );
  }

  private void addParameter( String path, List<NameValuePair> parameters, String name, String value ) {
    if ( !Utils.isEmpty( path ) && !Utils.isEmpty( value ) ) {
      parameters.add( new BasicNameValuePair( name, value ) );
    }
  }

  public void open( String path, String connection, String provider, String state, String title, String filter,
                    String origin, String filename, String fileType ) {
    try {
      path = URLEncoder.encode( path, "UTF-8" );
    } catch ( Exception e ) {
      // ignore if fails
    }

    StringBuilder clientPath = new StringBuilder();
    clientPath.append( getClientPath() );
    clientPath.append( !Utils.isEmpty( state ) ? "#/" + state : "" );

    List<NameValuePair> parameters = new ArrayList<>();
    addParameter( path, parameters, PATH, path );
    addParameter( path, parameters, CONNECTION, connection );
    addParameter( path, parameters, PROVIDER, provider );
    addParameter( path, parameters, FILTER, filter );
    addParameter( path, parameters, ORIGIN, origin );
    addParameter( path, parameters, FILENAME, filename );
    addParameter( path, parameters, FILE_TYPE, fileType );
    String queryParams = URLEncodedUtils.format( parameters, "UTF-8" );
    clientPath.append( "?" ).append( queryParams );

    super.createDialog( title != null ? title : StringUtils.capitalize( state ), getRepoURL( clientPath.toString() ),
      OPTIONS, LOGO );
    super.dialog.setMinimumSize( 545, 458 );

    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        browser.dispose();
        dialog.close();
        dialog.dispose();
        return true;
      }
    };

    new BrowserFunction( browser, "select" ) {
      @Override public Object function( Object[] arguments ) {
        setProperties( arguments );

        browser.dispose();
        dialog.close();
        dialog.dispose();
        return true;
      }
    };

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private void setProperties( Object[] arguments ) {
    objectId = (String) arguments[ 0 ];
    name = (String) arguments[ 1 ];
    path = (String) arguments[ 2 ];
    parentPath = (String) arguments[ 3 ];
    connection = (String) arguments[ 4 ];
    provider = (String) arguments[ 5 ];
    type = (String) arguments[ 6 ];
  }

  private static String getClientPath() {
    Properties properties = new Properties();
    try ( InputStream inputStream = FileOpenSaveDialog.class.getClassLoader()
      .getResourceAsStream( "project.properties" ) ) {
      properties.load( inputStream );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return properties.getProperty( "CLIENT_PATH" );
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

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId( String objectId ) {
    this.objectId = objectId;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection( String connection ) {
    this.connection = connection;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath( String parentPath ) {
    this.parentPath = parentPath;
  }
}
