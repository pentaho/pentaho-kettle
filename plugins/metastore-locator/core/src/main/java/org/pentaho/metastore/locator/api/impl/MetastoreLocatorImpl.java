/*!
 * Copyright 2010 - 2023 Hitachi Vantara.  All rights reserved.
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
 * limitations under the License.
 *
 */
package org.pentaho.metastore.locator.api.impl;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.locator.api.MetastoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tkafalas on 6/19/2017
 */
@ServiceProvider( id = "MetastoreLocator", description = "Provides access to the metastore", provides = MetastoreLocator.class )
public class MetastoreLocatorImpl implements MetastoreLocator, MetastoreLocatorOsgi, ServiceProviderInterface<MetastoreLocator> {

  private static Map<String, MetastoreProvider> providerMap = new ConcurrentHashMap<>();
  private Logger logger = LoggerFactory.getLogger( MetastoreLocatorImpl.class );

  // Returns the exact metastore defined by the key
  @Override
  public IMetaStore getExplicitMetastore( String providerKey ) {

    try {
      for ( MetastoreProvider provider : PluginServiceLoader.loadServices( MetastoreProvider.class ) ) {
        if ( provider.getProviderType().equals( providerKey ) ) {
          return provider.getMetastore();
        }
      }
    } catch ( KettlePluginException e ) {
      logger.error( "Exception loading MetastoreProvider services", e );
    }
    MetastoreProvider embeddedProvider = providerMap.get( providerKey );
    if ( null != embeddedProvider ) {
      return embeddedProvider.getMetastore();
    }
    return null;
  }

  @Override
  public IMetaStore getMetastore() {
    return getMetastore( null );
  }

  @Override
  public IMetaStore getMetastore( String providerKey ) {
    //Look for VFS metastore.
    IMetaStore metaStore = getExplicitMetastore( MetastoreLocator.VFS_PROVIDER_KEY );
    if ( metaStore == null ) {
      metaStore = getExplicitMetastore( MetastoreLocator.REPOSITORY_PROVIDER_KEY );
    }
    if ( metaStore == null ) {
      metaStore = getExplicitMetastore( MetastoreLocator.LOCAL_PROVIDER_KEY );
    }
    if ( metaStore == null && providerKey != null ) {
      metaStore = getExplicitMetastore( providerKey );
    }
    if ( metaStore == null ) {
      try {
        metaStore = MetaStoreConst.openLocalPentahoMetaStore( false );
      } catch ( MetaStoreException e ) {
        LogChannel.GENERAL.logError( "Could not load Metastore.", e );
        return null;
      }
    }

    return metaStore;
  }

  @Override
  public String setEmbeddedMetastore( final IMetaStore metastore ) {
    MetastoreProvider metastoreProvider = new MetastoreProvider() {
      @Override public IMetaStore getMetastore() {
        return metastore;
      }

      @Override public String getProviderType() {
        return null;
      }
    };
    try {
      String providerKey = metastore.getName() == null ? UUID.randomUUID().toString() : metastore.getName();
      if ( getExplicitMetastore( providerKey ) == null ) {
        providerMap.put( providerKey, metastoreProvider );
      }
      return providerKey;
    } catch ( MetaStoreException e ) {
      throw new IllegalStateException( e );
    }

  }

  @Override
  public void disposeMetastoreProvider( String providerKey ) {
    providerMap.remove( providerKey );
  }

}
