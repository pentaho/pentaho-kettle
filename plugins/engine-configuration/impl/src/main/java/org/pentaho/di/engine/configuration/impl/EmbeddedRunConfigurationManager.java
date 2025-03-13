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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 5/4/17.
 */
public class EmbeddedRunConfigurationManager {
  public static RunConfigurationManager build( EmbeddedMetaStore embeddedMetaStore ) {
    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( () -> embeddedMetaStore );
    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.add( defaultRunConfigurationProvider );

    return new RunConfigurationManager( runConfigurationProviders );
  }

}

