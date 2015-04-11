/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * 
 * Copyright 2015 De Bortoli Wines Pty Limited (Australia)
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

package org.pentaho.ui.database.event;

import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.ui.database.Messages;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

/**
 * Fragment handler deals with the logistics of replacing a portion of the dialog from a XUL fragment when the
 * combination of database connection type and database access method calls for a replacement.
 *
 * @author gmoran
 * @created Mar 19, 2008
 */
public class FragmentHandler extends AbstractXulEventHandler {

  private XulListbox connectionBox;
  private XulListbox accessBox;

  private String packagePath = "org/pentaho/ui/database/";
  private String packageName = "org.pentaho.ui.database.";

  public FragmentHandler() {
  }

  private void loadDatabaseOptionsFragment( String fragmentUri, ResourceBundle bundle ) throws XulException {

    XulComponent groupElement = document.getElementById( "database-options-box" );
    XulComponent parentElement = groupElement.getParent();

    XulDomContainer fragmentContainer = null;

    try {

      // Get new group box fragment ...
      // This will effectively set up the SWT parent child relationship...

      fragmentContainer = this.xulDomContainer.loadFragment( fragmentUri, bundle );
      XulComponent newGroup = fragmentContainer.getDocumentRoot().getFirstChild();
      parentElement.replaceChild( groupElement, newGroup );

    } catch ( XulException e ) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * This method handles the resource-like loading of the XUL fragment definitions based on connection type and access
   * method. If there is a common definition, and no connection specific override definition, then the common definition
   * is used. Connection specific definition resources follow the naming pattern [connection type code]_[access
   * method].xul.
   */
  public void refreshOptions() {

    connectionBox = (XulListbox) document.getElementById( "connection-type-list" );
    accessBox = (XulListbox) document.getElementById( "access-type-list" );

    Object connectionKey = DataHandler.connectionNametoID.get( connectionBox.getSelectedItem() );
    String databaseName = null;
    try {
      databaseName =
        PluginRegistry.getInstance().getPlugin( DatabasePluginType.class, "" + connectionKey ).getIds()[0];
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    DatabaseInterface database = DataHandler.connectionMap.get( connectionBox.getSelectedItem() );

    Object accessKey = accessBox.getSelectedItem();
    int access = DatabaseMeta.getAccessType( (String) accessKey );

    String fragment = null;

    DataHandler dataHandler = null;
    try {
      dataHandler = (DataHandler) xulDomContainer.getEventHandler( "dataHandler" );
      dataHandler.pushCache();
    } catch ( XulException e ) {
      // TODO not a critical function, but should log a problem...
    }

    switch ( access ) {
      case DatabaseMeta.TYPE_ACCESS_JNDI:
        fragment = getFragment( database, databaseName, "_jndi.xul", "common_jndi.xul" );
        break;
      case DatabaseMeta.TYPE_ACCESS_NATIVE:
        fragment = getFragment( database, databaseName, "_native.xul", "common_native.xul" );
        break;
      case DatabaseMeta.TYPE_ACCESS_OCI:
        fragment = getFragment( database, databaseName, "_oci.xul", "common_native.xul" );
        break;
      case DatabaseMeta.TYPE_ACCESS_ODBC:
        fragment = getFragment( database, databaseName, "_odbc.xul", "common_odbc.xul" );
        break;
      case DatabaseMeta.TYPE_ACCESS_PLUGIN:
        fragment = getFragment( database, databaseName, "_plugin.xul", "common_native.xul" );
        break;
      default:
        break;
    }

    try {
      loadDatabaseOptionsFragment( fragment.toLowerCase(), getResourceBundle( database ) );
    } catch ( XulException e ) {
      // TODO should be reporting as an error dialog; need error dialog in XUL framework
      showMessage( Messages.getString( "FragmentHandler.USER.CANT_LOAD_OPTIONS", databaseName ) );
    }

    XulTextbox portBox = (XulTextbox) document.getElementById( "port-number-text" );
    if ( portBox != null ) {
      int port = database.getDefaultDatabasePort();
      if ( port > 0 ) {
        portBox.setValue( Integer.toString( port ) );
      }
    }

    if ( dataHandler != null ) {
      dataHandler.popCache();
    }

  }

  private String getFragment( DatabaseInterface database, String dbName, String extension, String defaultFragment ) {
    String fragment;
    InputStream in = null;
    if ( database.getXulOverlayFile() != null ) {
      // Handle custom XUL file
      fragment = packagePath.concat( database.getXulOverlayFile() ).concat( extension );

      ClassLoader loader = getPluginClassLoader( database );
      in = loader.getResourceAsStream( fragment.toLowerCase() );
      
      if ( in != null ) {
        xulDomContainer.registerClassLoader( loader );
      }

    } else {
      fragment = packagePath.concat( dbName ).concat( extension );
      in = getClass().getClassLoader().getResourceAsStream( fragment.toLowerCase() );
    }
    
    if ( in == null ) {
      fragment = packagePath.concat( defaultFragment );
    }
    
    return fragment;
  }
  
  private ResourceBundle getResourceBundle( DatabaseInterface database ) {
    // Handle custom XUL resource bundle
    if ( database.getXulOverlayFile() != null ) {
      String bundleName = packageName.concat( database.getXulOverlayFile() ) ;
      ResourceBundle bundle = ResourceBundle.getBundle( bundleName, Locale.getDefault(), getPluginClassLoader( database ) ); 
      if ( bundle != null) {
        return bundle; 
      }
    }

    return Messages.getBundle();
  }
  
  private ClassLoader getPluginClassLoader( DatabaseInterface database ) {
    try {
      PluginRegistry registry = PluginRegistry.getInstance();
      PluginInterface plugin = registry.getPlugin( DatabasePluginType.class, database.getPluginId() );
      
      if ( plugin != null ) {
        return registry.getClassLoader( plugin );
      }
    } catch ( KettlePluginException e ) {
      System.out.println( "Error getting plugin class loader " + e.getMessage() );
    }

    return null;
  }

  public Object getData() {
    return null;
  }

  public void setData( Object arg0 ) {
  }

  private void showMessage( String message ) {
    try {
      XulMessageBox box = (XulMessageBox) document.createElement( "messagebox" );
      box.setMessage( message );
      box.open();
    } catch ( XulException e ) {
      System.out.println( "Error creating messagebox " + e.getMessage() );
    }
  }
}
