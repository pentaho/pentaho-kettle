/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2021 by Hitachi Vantara : http://www.pentaho.com
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.eclipse.swt.browser.Browser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by bmorrise on 5/23/17.
 */
public class FileOpenSaveDialog extends ThinDialog implements FileDetails {
  private static final Class<?> PKG = FileOpenSaveDialog.class;

  public static final String STATE_SAVE = "save";
  public static final String STATE_OPEN = "open";
  public static final String SELECT_FOLDER = "selectFolder";
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String THIN_CLIENT_HOST = "THIN_CLIENT_HOST";
  private static final String THIN_CLIENT_PORT = "THIN_CLIENT_PORT";
  private static final String LOCALHOST = "127.0.0.1";
  private static final String HELP_URL =
    Const.getDocUrl( "Products/Work_with_transformations#Open_a_transformation" );

  public static final String PATH_PARAM = "path";
  public static final String USE_SCHEMA_PARAM = "useSchema";
  public static final String CONNECTION_PARAM = "connection";
  public static final String PROVIDER_PARAM = "provider";
  public static final String PROVIDER_FILTER_PARAM = "providerFilter";
  public static final String FILTER_PARAM = "filter";
  public static final String DEFAULT_FILTER_PARAM = "defaultFilter";
  public static final String CONNECTION_FILTER_PARAM = "connectionTypes";
  public static final String ORIGIN_PARAM = "origin";
  public static final String FILENAME_PARAM = "filename";
  public static final String FILE_TYPE_PARM = "fileType";
  public static final String OBJECT_ID_PARAM = "objectId";
  public static final String NAME_PARAM = "name";
  public static final String PARENT_PARAM = "parent";
  public static final String TYPE_PARAM = "type";

  private String objectId;
  private String name;
  private String path;
  private String parentPath;
  private String type;
  private String connection;
  private String provider;

  private LogChannelInterface log;

  public FileOpenSaveDialog( Shell shell, int width, int height, LogChannelInterface logger ) {
    super( shell, width, height );
    this.log = logger;
  }

  private void addParameter( List<NameValuePair> parameters, String name, String value ) {
    if ( !Utils.isEmpty( value ) ) {
      parameters.add( new BasicNameValuePair( name, value ) );
    }
  }

  public void open( FileDialogOperation fileDialogOperation ) {

    String dialogPath = fileDialogOperation.getPath() != null
      ? fileDialogOperation.getPath()
      : fileDialogOperation.getStartDir();

    try {
      dialogPath = URLEncoder.encode( dialogPath, "UTF-8" );
    } catch ( Exception e ) {
      // ignore if fails
    }

    StringBuilder clientPath = new StringBuilder();
    String cmd = fileDialogOperation.getCommand();

    clientPath.append( getClientPath() );
    clientPath.append( !Utils.isEmpty( cmd ) ? "#!/" + cmd : "" );

    List<NameValuePair> parameters = new ArrayList<>();
    addParameter( parameters, PATH_PARAM, dialogPath );
    addParameter( parameters, CONNECTION_PARAM, fileDialogOperation.getConnection() );
    addParameter( parameters, PROVIDER_PARAM, fileDialogOperation.getProvider() );
    addParameter( parameters, PROVIDER_FILTER_PARAM, fileDialogOperation.getProviderFilter() );
    addParameter( parameters, FILTER_PARAM, fileDialogOperation.getFilter() );
    addParameter( parameters, DEFAULT_FILTER_PARAM, fileDialogOperation.getDefaultFilter() );
    addParameter( parameters, ORIGIN_PARAM, fileDialogOperation.getOrigin() );
    addParameter( parameters, FILENAME_PARAM, fileDialogOperation.getFilename() );
    addParameter( parameters, FILE_TYPE_PARM, fileDialogOperation.getFileType() );
    addParameter( parameters, CONNECTION_FILTER_PARAM, fileDialogOperation.getConnectionTypeFilter()  );

    if ( fileDialogOperation.getUseSchemaPath() ) {
      addParameter( parameters, USE_SCHEMA_PARAM, "true" );
    }

    String queryParams = URLEncodedUtils.format( parameters, "UTF-8" );
    clientPath.append( "?" ).append( queryParams );

    String title = Utils.isEmpty( cmd ) ? "" : BaseMessages.getString( PKG,
      ( "FileOpenSaveDialog.dialog." + cmd + ".title" ) );

    super.createDialog( fileDialogOperation.getTitle() != null ? fileDialogOperation.getTitle()
        : StringUtils.capitalize( title ), getRepoURL( clientPath.toString() ), OPTIONS, LOGO );
    super.dialog.setMinimumSize( 545, 458 );

    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        if ( Const.isRunningOnWebspoonMode() ) {
          Runnable execute = () -> closeBrowser( browser );
          display.asyncExec( execute );
        } else {
          closeBrowser( browser );
        }
        return true;
      }
    };

    new BrowserFunction( browser, "select" ) {
      @Override public Object function( Object[] arguments ) {
        if ( Const.isRunningOnWebspoonMode() ) {
          Runnable execute = () -> closeBrowserWithParameters( arguments );
          display.asyncExec( execute );
        } else {
          closeBrowserWithParameters( arguments );
        }
        return true;
      }
    };

    new BrowserFunction( browser, "help" ) {
      @Override public Object function( Object[]  arguments ) {
        openHelpDialog();
        return true;
      }
    };

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private void closeBrowserWithParameters( Object[] arguments ) {
    try {
      setProperties( arguments );
      closeBrowser( browser );
    } catch ( Exception e ) {
      log.logError( "Error in processing select() from file-open-save app: ", e );
    }
  }

  private void closeBrowser( Browser browser ) {
    browser.dispose();
    dialog.close();
    dialog.dispose();
  }

  private void openHelpDialog() {
    HelpUtils.openHelpDialog( this.dialog, "", HELP_URL );
  }

  private void setProperties( Object[] arguments ) throws ParseException {
    if ( arguments.length == 1 ) {
      String jsonString = (String) arguments[ 0 ];
      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse( jsonString );
      objectId = (String) jsonObject.get( OBJECT_ID_PARAM );
      name = (String) jsonObject.get( NAME_PARAM );
      path = (String) jsonObject.get( PATH_PARAM );
      parentPath = (String) jsonObject.get( PARENT_PARAM );
      connection = (String) jsonObject.get( CONNECTION_PARAM );
      provider = (String) jsonObject.get( PROVIDER_PARAM );
      type = (String) jsonObject.get( TYPE_PARAM );
    }
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

  private static String getKettleProperty( String propertyName ) {
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
