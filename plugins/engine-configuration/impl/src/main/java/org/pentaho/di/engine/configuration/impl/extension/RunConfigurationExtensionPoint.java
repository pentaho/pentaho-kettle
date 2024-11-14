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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 3/16/17.
 */
@ExtensionPoint( id = "RunConfigurationExtensionPoint", extensionPointId = "SpoonRunConfiguration",
  description = "" )
public class RunConfigurationExtensionPoint implements ExtensionPointInterface {

  private RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance();

  @SuppressWarnings( "unchecked" )
  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    List<String> runConfigurations = (ArrayList) ( (Object[]) o )[ 0 ];
    String type = (String) ( (Object[]) o )[ 1 ];

    runConfigurations.addAll( runConfigurationManager.getNames( type ) );
  }
}
