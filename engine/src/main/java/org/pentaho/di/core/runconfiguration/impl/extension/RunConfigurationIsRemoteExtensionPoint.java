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


package org.pentaho.di.core.runconfiguration.impl.extension;

import org.pentaho.di.core.runconfiguration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.runconfiguration.api.RunConfiguration;
import org.pentaho.di.core.runconfiguration.impl.RunConfigurationManager;

import java.util.List;

/**
 * Created by ppatricio on 04/01/19.
 */
@ExtensionPoint( id = "RunConfigurationIsRemoteExtensionPoint", extensionPointId = "RunConfigurationIsRemote",
  description = "" )
public class RunConfigurationIsRemoteExtensionPoint implements ExtensionPointInterface {

  private RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance();

  @SuppressWarnings( "unchecked" )
  @Override
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    List<Object> items = (List<Object>) o;
    String name = (String) items.get( 0 );
    RunConfiguration runConfiguration = runConfigurationManager.load( name );
    if ( runConfiguration != null && runConfiguration.getType().equals( DefaultRunConfiguration.TYPE ) ) {
      items.set( 1, ((DefaultRunConfiguration) runConfiguration).isRemote() );
    }
  }
}

