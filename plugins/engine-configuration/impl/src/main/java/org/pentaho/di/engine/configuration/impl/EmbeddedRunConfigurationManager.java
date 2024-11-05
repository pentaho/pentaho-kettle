/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.configuration.impl;

import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfigurationProvider;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 5/4/17.
 */
public class EmbeddedRunConfigurationManager {
  public static RunConfigurationManager build( EmbeddedMetaStore embeddedMetaStore ) {
    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( () -> embeddedMetaStore );
    SparkRunConfigurationProvider sparkRunConfigurationProvider =
    new SparkRunConfigurationProvider( createMetastoreLocator( embeddedMetaStore ) );
    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.add( defaultRunConfigurationProvider );
    runConfigurationProviders.add( sparkRunConfigurationProvider );

    return new RunConfigurationManager( runConfigurationProviders );
  }


  private static MetastoreLocator createMetastoreLocator(IMetaStore embeddedMetaStore ) {
    return new MetastoreLocator() {

      @Override
      public IMetaStore getMetastore( String providerKey ) {
        return embeddedMetaStore;
      }

      @Override
      public IMetaStore getMetastore() {
        return embeddedMetaStore;
      }

      @Override public String setEmbeddedMetastore( IMetaStore metastore ) {
        return null;
      }

      @Override public void disposeMetastoreProvider( String providerKey ) {

      }
      @Override public IMetaStore getExplicitMetastore( String providerKey ) {
        return null;
      }
    };
  }

}

