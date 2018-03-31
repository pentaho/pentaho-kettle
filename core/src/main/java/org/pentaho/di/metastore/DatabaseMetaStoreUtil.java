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

package org.pentaho.di.metastore;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.metastore.util.PentahoDefaults;

public class DatabaseMetaStoreUtil extends MetaStoreUtil {

  public static List<DatabaseMeta> getDatabaseElements( IMetaStore metaStore ) throws MetaStoreException {
    List<DatabaseMeta> databases = new ArrayList<DatabaseMeta>();

    // If the data type doesn't exist, it's an empty list...
    //
    IMetaStoreElementType elementType =
      metaStore.getElementTypeByName(
        PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    if ( elementType == null ) {
      return databases;
    }

    List<IMetaStoreElement> elements = metaStore.getElements( PentahoDefaults.NAMESPACE, elementType );
    for ( IMetaStoreElement element : elements ) {
      try {
        DatabaseMeta databaseMeta = loadDatabaseMetaFromDatabaseElement( metaStore, element );
        databases.add( databaseMeta );
      } catch ( Exception e ) {
        throw new MetaStoreException( "Unable to load database from element with name '"
          + element.getName() + "' and type '" + elementType.getName() + "'", e );
      }
    }

    return databases;
  }

  public static void createDatabaseElement( IMetaStore metaStore, DatabaseMeta databaseMeta ) throws MetaStoreException {

    // If the Pentaho namespace doesn't exist, create it!
    //
    if ( !metaStore.namespaceExists( PentahoDefaults.NAMESPACE ) ) {
      metaStore.createNamespace( PentahoDefaults.NAMESPACE );
    }

    // If the database connection element type doesn't exist, create it
    //
    IMetaStoreElementType elementType =
      metaStore.getElementTypeByName(
        PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    if ( elementType == null ) {
      elementType = populateDatabaseElementType( metaStore );
      metaStore.createElementType( PentahoDefaults.NAMESPACE, elementType );
    }

    // populate an element, store it.
    //
    IMetaStoreElement databaseElement = populateDatabaseElement( metaStore, databaseMeta );

    // Store the element physically
    //
    metaStore.createElement( PentahoDefaults.NAMESPACE, databaseElement.getElementType(), databaseElement );
  }

  public static IMetaStoreElementType populateDatabaseElementType( IMetaStore metaStore ) throws MetaStoreException {

    // The new type will typically have an ID so all we need to do is give the type a name and a description.
    //
    IMetaStoreElementType elementType = metaStore.newElementType( PentahoDefaults.NAMESPACE );

    // Name and description...
    //
    elementType.setName( PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    elementType.setDescription( PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_DESCRIPTION );
    return elementType;
  }

  public static IMetaStoreElement populateDatabaseElement( IMetaStore metaStore, DatabaseMeta databaseMeta ) throws MetaStoreException {

    if ( !metaStore.namespaceExists( PentahoDefaults.NAMESPACE ) ) {
      throw new MetaStoreException( "Namespace '" + PentahoDefaults.NAMESPACE + "' doesn't exist." );
    }

    // If the data type doesn't exist, error out...
    //
    IMetaStoreElementType elementType =
      metaStore.getElementTypeByName(
        PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    if ( elementType == null ) {
      throw new MetaStoreException( "Unable to find the database connection type" );
    }

    elementType = populateDatabaseElementType( metaStore );

    // generate a new database element and populate it with metadata
    //
    IMetaStoreElement element = metaStore.newElement( elementType, databaseMeta.getName(), null );

    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_PLUGIN_ID, databaseMeta.getPluginId() ) );

    element.setName( databaseMeta.getName() );

    element.addChild( metaStore
      .newAttribute( MetaStoreConst.DB_ATTR_ID_DESCRIPTION, databaseMeta.getDescription() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_ACCESS_TYPE, databaseMeta
      .getAccessTypeDesc() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_HOSTNAME, databaseMeta.getHostname() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_PORT, databaseMeta
      .getDatabasePortNumberString() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_DATABASE_NAME, databaseMeta
      .getDatabaseName() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_USERNAME, databaseMeta.getUsername() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_PASSWORD, metaStore
      .getTwoWayPasswordEncoder().encode( databaseMeta.getPassword() ) ) );
    element
      .addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_SERVERNAME, databaseMeta.getServername() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_DATA_TABLESPACE, databaseMeta
      .getDataTablespace() ) );
    element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_INDEX_TABLESPACE, databaseMeta
      .getIndexTablespace() ) );

    IMetaStoreAttribute attributesChild = metaStore.newAttribute( MetaStoreConst.DB_ATTR_ID_ATTRIBUTES, null );
    element.addChild( attributesChild );

    // Now add a list of all the attributes set on the database connection...
    //
    Properties attributes = databaseMeta.getAttributes();
    Enumeration<Object> keys = databaseMeta.getAttributes().keys();
    while ( keys.hasMoreElements() ) {
      String code = (String) keys.nextElement();
      String attribute = (String) attributes.get( code );
      // Add it to the attributes child
      //
      attributesChild.addChild( metaStore.newAttribute( code, attribute ) );
    }

    // Extra information for 3rd-party tools:
    //
    // The driver class
    //
    element
      .addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_DRIVER_CLASS, databaseMeta.getDriverClass() ) );

    // The URL
    //
    try {
      element.addChild( metaStore.newAttribute( MetaStoreConst.DB_ATTR_JDBC_URL, databaseMeta.getURL() ) );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to assemble URL from database '" + databaseMeta.getName() + "'", e );
    }

    return element;
  }

  public static DatabaseMeta loadDatabaseMetaFromDatabaseElement( IMetaStore metaStore, IMetaStoreElement element ) throws KettlePluginException {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    PluginRegistry pluginRegistry = PluginRegistry.getInstance();

    // Load the appropriate database plugin (database interface)
    //
    String pluginId = getChildString( element, MetaStoreConst.DB_ATTR_ID_PLUGIN_ID );
    String driverClassName = getChildString( element, MetaStoreConst.DB_ATTR_DRIVER_CLASS );
    if ( Utils.isEmpty( pluginId ) && Utils.isEmpty( driverClassName ) ) {
      throw new KettlePluginException( "The attributes 'plugin_id' and 'driver_class' can't be both empty" );
    }
    if ( Utils.isEmpty( pluginId ) ) {
      // Determine pluginId using the plugin registry.
      //
      List<PluginInterface> plugins = pluginRegistry.getPlugins( DatabasePluginType.class );
      for ( PluginInterface plugin : plugins ) {
        DatabaseInterface databaseInterface = (DatabaseInterface) pluginRegistry.loadClass( plugin );
        if ( driverClassName.equalsIgnoreCase( databaseInterface.getDriverClass() ) ) {
          pluginId = plugin.getIds()[0];
        }
      }
    }
    if ( Utils.isEmpty( pluginId ) ) {
      throw new KettlePluginException(
        "The 'plugin_id' attribute could not be determined using 'driver_class' value '" + driverClassName + "'" );
    }

    // Look for the plugin
    //
    PluginInterface plugin = PluginRegistry.getInstance().getPlugin( DatabasePluginType.class, pluginId );
    DatabaseInterface databaseInterface = (DatabaseInterface) PluginRegistry.getInstance().loadClass( plugin );
    databaseInterface.setPluginId( pluginId );
    databaseMeta.setDatabaseInterface( databaseInterface );

    databaseMeta.setObjectId( new StringObjectId( element.getId() ) );
    databaseMeta.setName( element.getName() );
    databaseMeta.setDescription( getChildString( element, MetaStoreConst.DB_ATTR_ID_DESCRIPTION ) );

    String accessTypeString = getChildString( element, MetaStoreConst.DB_ATTR_ID_ACCESS_TYPE );
    if ( Utils.isEmpty( accessTypeString ) ) {
      accessTypeString = DatabaseMeta.getAccessTypeDesc( DatabaseMeta.TYPE_ACCESS_NATIVE );
    }
    databaseMeta.setAccessType( DatabaseMeta.getAccessType( accessTypeString ) );

    databaseMeta.setHostname( getChildString( element, MetaStoreConst.DB_ATTR_ID_HOSTNAME ) );
    databaseMeta.setDBPort( getChildString( element, MetaStoreConst.DB_ATTR_ID_PORT ) );
    databaseMeta.setDBName( getChildString( element, MetaStoreConst.DB_ATTR_ID_DATABASE_NAME ) );
    databaseMeta.setUsername( getChildString( element, MetaStoreConst.DB_ATTR_ID_USERNAME ) );
    databaseMeta.setPassword( metaStore.getTwoWayPasswordEncoder().decode(
      getChildString( element, MetaStoreConst.DB_ATTR_ID_PASSWORD ) ) );
    databaseMeta.setServername( getChildString( element, MetaStoreConst.DB_ATTR_ID_SERVERNAME ) );
    databaseMeta.setDataTablespace( getChildString( element, MetaStoreConst.DB_ATTR_ID_DATA_TABLESPACE ) );
    databaseMeta.setIndexTablespace( getChildString( element, MetaStoreConst.DB_ATTR_ID_INDEX_TABLESPACE ) );

    IMetaStoreAttribute attributesChild = element.getChild( MetaStoreConst.DB_ATTR_ID_ATTRIBUTES );
    if ( attributesChild != null ) {
      // Now add a list of all the attributes set on the database connection...
      //
      Properties attributes = databaseMeta.getAttributes();
      for ( IMetaStoreAttribute attr : attributesChild.getChildren() ) {
        String code = attr.getId();
        String value = getAttributeString( attr );
        attributes.put( code, Const.NVL( value, "" ) );

      }
    }

    return databaseMeta;
  }

}
