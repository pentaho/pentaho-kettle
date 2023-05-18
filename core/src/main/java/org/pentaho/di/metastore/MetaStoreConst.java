/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.File;
import java.util.Collection;
import java.util.function.Supplier;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.Utils;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.stores.xml.XmlUtil;

public class MetaStoreConst {

  public static final String DB_ATTR_ID_DESCRIPTION = "description";
  public static final String DB_ATTR_ID_PLUGIN_ID = "plugin_id";
  public static final String DB_ATTR_ID_ACCESS_TYPE = "access_type";
  public static final String DB_ATTR_ID_HOSTNAME = "host_name";
  public static final String DB_ATTR_ID_PORT = "port";
  public static final String DB_ATTR_ID_DATABASE_NAME = "database_name";
  public static final String DB_ATTR_ID_USERNAME = "username";
  @SuppressWarnings( "squid:S2068" ) public static final String DB_ATTR_ID_PASSWORD = "password";
  public static final String DB_ATTR_ID_SERVERNAME = "server_name";
  public static final String DB_ATTR_ID_DATA_TABLESPACE = "data_tablespace";
  public static final String DB_ATTR_ID_INDEX_TABLESPACE = "index_tablespace";
  public static boolean disableMetaStore; // Used for testing only

  // Extra information for 3rd party tools, not used by Kettle
  //
  public static final String DB_ATTR_DRIVER_CLASS = "driver_class";
  public static final String DB_ATTR_JDBC_URL = "jdbc_url";

  public static final String DB_ATTR_ID_ATTRIBUTES = "attributes";

  private static final Supplier<IMetaStore> metaStoreSupplier = new Supplier<IMetaStore>() {
    private volatile MetastoreLocator metastoreLocator;

    public IMetaStore get() {
      if ( metastoreLocator == null ) {
        synchronized ( this ) {
          try {
            if ( metastoreLocator == null ) {
              Collection<MetastoreLocator> metastoreLocators =
                  PluginServiceLoader.loadServices( MetastoreLocator.class );
              metastoreLocator = metastoreLocators.stream().findFirst().orElse( null );
            }
          } catch ( KettlePluginException e ) {
            LogChannel.GENERAL.logError( "Error getting metastore locator", e );
          }
        }
      }
      return metastoreLocator == null ? null : metastoreLocator.getMetastore();
    }
  };

  public static final String getDefaultPentahoMetaStoreLocation() {
    return System.getProperty( "user.home" ) + File.separator + ".pentaho";
  }

  public static IMetaStore openLocalPentahoMetaStore() throws MetaStoreException {
    return MetaStoreConst.openLocalPentahoMetaStore( true );
  }

  public static IMetaStore openLocalPentahoMetaStore( boolean allowCreate ) throws MetaStoreException {
    if ( disableMetaStore ) {
      return null;
    }
    String rootFolder = System.getProperty( Const.PENTAHO_METASTORE_FOLDER );
    if ( Utils.isEmpty( rootFolder ) ) {
      rootFolder = getDefaultPentahoMetaStoreLocation();
    }
    File rootFolderFile = new File( rootFolder );
    File metaFolder = new File( rootFolder + File.separator + XmlUtil.META_FOLDER_NAME );
    if ( !allowCreate && !metaFolder.exists() ) {
      return null;
    }
    if ( !rootFolderFile.exists() ) {
      try {
        rootFolderFile.mkdirs();
      } catch ( SecurityException e ) {
        throw new MetaStoreException( e.getMessage() );
      }
    }

    XmlMetaStore metaStore = new XmlMetaStore( rootFolder );
    if ( allowCreate ) {
      metaStore.setName( Const.PENTAHO_METASTORE_NAME );
    }
    return metaStore;
  }

  public static Supplier<IMetaStore> getDefaultMetastoreSupplier() {
    return metaStoreSupplier;
  }
}
