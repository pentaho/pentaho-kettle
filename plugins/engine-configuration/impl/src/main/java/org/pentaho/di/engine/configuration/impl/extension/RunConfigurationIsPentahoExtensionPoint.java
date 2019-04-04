/*
* *****************************************************************************
*
*  Pentaho Data Integration
*
*  Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
*
*  *******************************************************************************
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
*  this file except in compliance with the License. You may obtain a copy of the
*  License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
* *****************************************************************************
*
*/

package org.pentaho.di.engine.configuration.impl.extension;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;

import java.util.List;

/**
 * Created by bmorrise on 10/12/18.
 */
@ExtensionPoint( id = "RunConfigurationIsPentahoExtensionPoint", extensionPointId = "RunConfigurationSelection",
        description = "" )
public class RunConfigurationIsPentahoExtensionPoint implements ExtensionPointInterface {

  private RunConfigurationManager runConfigurationManager;

  public RunConfigurationIsPentahoExtensionPoint( RunConfigurationManager runConfigurationManager ) {
    this.runConfigurationManager = runConfigurationManager;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    List<Object> items = (List<Object>) o;
    String name = (String) items.get( 0 );
    RunConfiguration runConfiguration = runConfigurationManager.load( name );
    if ( runConfiguration != null && runConfiguration.getType().equals( DefaultRunConfiguration.TYPE ) ) {
      items.set( 1, ((DefaultRunConfiguration) runConfiguration).isPentaho() );
    }
  }
}
