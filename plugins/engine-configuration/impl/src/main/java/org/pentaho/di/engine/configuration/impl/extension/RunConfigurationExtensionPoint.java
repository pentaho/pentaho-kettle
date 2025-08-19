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


package org.pentaho.di.engine.configuration.impl.extension;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by bmorrise on 3/16/17.
 */
@ExtensionPoint( id = "RunConfigurationExtensionPoint", extensionPointId = "RunConfiguration",
  description = "" )
public class RunConfigurationExtensionPoint implements ExtensionPointInterface {

  private final Function<Bowl, RunConfigurationManager> rcmProvider = bowl ->
    RunConfigurationManager.getInstance( () -> bowl != null ? bowl.getMetastore()
      : DefaultBowl.getInstance().getMetastore() );

  @SuppressWarnings( "unchecked" )
  @Override
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    List<String> runConfigurations = (ArrayList<String>) ( (Object[]) o )[ 0 ];
    String type = (String) ( (Object[]) o )[ 1 ];
    Bowl bowl = (Bowl) ( (Object[]) o )[ 2 ];

    RunConfigurationManager runConfigurationManager =
      rcmProvider.apply( bowl );

    runConfigurations.addAll( runConfigurationManager.getNames( type ) );
  }
}
