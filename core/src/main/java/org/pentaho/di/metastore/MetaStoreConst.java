/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.Utils;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreDirectoryProvider;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.stores.xml.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger logger = LoggerFactory.getLogger( MetaStoreConst.class );

  // whether to default to the local metastore. This is not wanted most of the time because it second-guesses the
  // logic in MetastoreLocator. Should only be enabled in tests that don't load plugins.
  private static volatile boolean defaultToLocalXml = false;

  // NOTE: Suppliers are needed for a couple reasons: First, things that need metastores can be initialized before
  // plugins (such as MetastoreLocatorImpl) are fully loaded, and the Supplier defers initialization until usage time.
  // Nothing actually uses the metastore before plugins finish loading.
  // Second, things like connecting to Repositories can change the metastore at runtime, so it's better to hold onto
  // a Supplier than a specific instance.

  private static final Supplier<MetastoreLocator> metastoreLocatorSupplier = new Supplier<MetastoreLocator> () {
    private volatile MetastoreLocator metastoreLocator;

    public MetastoreLocator get() {
      // double-checked idiom because this can fail until the plugin is loaded.
      MetastoreLocator result = metastoreLocator;
      if ( result != null ) {
        return result;
      }
      synchronized ( this ) {
        try {
          if ( metastoreLocator == null ) {
            Collection<MetastoreLocator> metastoreLocators =
              PluginServiceLoader.loadServices( MetastoreLocator.class );
            metastoreLocator = result = metastoreLocators.stream().findFirst().orElse( null );
          }
        } catch ( KettlePluginException e ) {
          logger.error( "Error getting metastore locator", e );
        }
        return metastoreLocator;
      }
    }
  };

  private static final MetastoreDirectoryProvider NULL_METASTORE_DIRECTORY_PROVIDER =
    new MetastoreDirectoryProvider() {
      @Override
      public IMetaStore getMetastoreForDirectory(String rootFolder) {
        return null;
      }
    };

  private static final Supplier<MetastoreDirectoryProvider> metastoreDirectoryProviderSupplier =
    new Supplier<MetastoreDirectoryProvider> () {
    private volatile MetastoreDirectoryProvider metastoreDirectoryProvider;

    public MetastoreDirectoryProvider get() {
      // double-checked idiom because this can fail until the plugin is loaded.
      MetastoreDirectoryProvider result = metastoreDirectoryProvider;
      if ( result != null ) {
        return result;
      }
      synchronized ( this ) {
        try {
          if ( metastoreDirectoryProvider == null ) {
            Collection<MetastoreDirectoryProvider> metastoreDirectoryProviders =
              PluginServiceLoader.loadServices( MetastoreDirectoryProvider.class );
            metastoreDirectoryProvider = result = metastoreDirectoryProviders.stream().findFirst().orElse( null );
            if ( result == null ) {
              // cache the null so we don't constantly try to look this up.
              metastoreDirectoryProvider = result = NULL_METASTORE_DIRECTORY_PROVIDER;
            }
          }
        } catch ( KettlePluginException e ) {
          logger.error( "Error getting metastore directory provider", e );
        }
        return metastoreDirectoryProvider;
      }
    }
  };


  private static final Supplier<IMetaStore> metaStoreSupplier = new Supplier<IMetaStore> () {
    private volatile IMetaStore metaStore;

    public IMetaStore get() {
      // double-checked idiom because this can fail until the plugin is loaded.
      IMetaStore result = metaStore;
      if ( result != null ) {
        return result;
      }
      synchronized( this ) {
        if ( metaStore == null ) {
          MetastoreLocator locator = metastoreLocatorSupplier.get();
          if ( locator != null ) {
            metaStore = result = new SuppliedMetaStore( () -> locator.getMetastore() );
          } else if ( defaultToLocalXml ) {
            try {
              // Do not store as metaStore
              return openLocalPentahoMetaStore();
            } catch ( MetaStoreException e ) {
              logger.error( "Error opening local XML metastore", e );
            }
          }
        }
        return metaStore;
      }
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

  public static IMetaStore getDefaultMetastore() {
    return metaStoreSupplier.get();
  }

  /**
   * Returns a metastore implementation at the given path. Different implementations may support different types of
   * paths (e.g. local, vfs)
   *
   * NOTE that this supplier can do some non-trivial work and the result should never change, so the Supplier should
   * only be used for deffered initialization, and the result should generally be held onto.
   *
   *
   * @param rootFolder path to the metastore parent directory. The ".metastore" directory will be created under this
   *                  path.
   *
   * @return IMetaStore a metastore implementation at the given path, or null
   *
   */
  public static Supplier<IMetaStore> getMetastoreForDirectorySupplier( String rootFolder ) {
    return () -> {
      MetastoreDirectoryProvider provider = metastoreDirectoryProviderSupplier.get();
      if ( provider == null ) {
        return null;
      }
      return provider.getMetastoreForDirectory( rootFolder );
    };
  }

  /**
   * When this is enabled, if the MetastoreLocator returns null, a local xml metastore will be returned. This should
   * only happen when plugins are not loaded, which should only apply in some unit tests. Runtime code should all
   * load the metastore on demand, ideally using the metastoreSupplier above, and should not initialize a metastore at
   * construction time.
   *
   */
  public static void enableDefaultToLocalXml() {
    defaultToLocalXml = true;
  }

}

