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

package org.pentaho.di.trans;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.EnginePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.engine.api.Engine;
import org.pentaho.di.trans.ael.adapters.TransEngineAdapter;
import org.pentaho.di.trans.ael.websocket.TransWebSocketEngineAdapter;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TransSupplier implements Supplier<Trans> {

  private final TransMeta transMeta;
  private final LogChannelInterface log;
  private final Supplier<Trans> fallbackSupplier;

  public TransSupplier( TransMeta transMeta, LogChannelInterface log, Supplier<Trans> fallbackSupplier ) {
    this.transMeta = transMeta;
    this.log = log;
    this.fallbackSupplier = fallbackSupplier;
  }

  /**
   * Creates the appropriate trans.  Either
   * 1)  A {@link TransEngineAdapter} wrapping an {@link Engine}
   * 2)  A {@link TransWebSocketEngineAdapter} wrapping an {@link Engine}
   * if an alternate execution engine has been selected
   * 3)  A legacy {@link Trans} otherwise.
   */
  public Trans get() {
    if ( Utils.isEmpty( transMeta.getVariable( "engine" ) ) ) {
      log.logBasic( "Using legacy execution engine" );
      return fallbackSupplier.get();
    }

    Variables variables = new Variables();
    variables.initializeVariablesFrom( null );
    //default is the websocket daemon
    String version = variables.getVariable( "KETTLE_AEL_PDI_DAEMON_VERSION", "2.0" );
    if ( Const.toDouble( version, 1 ) >= 2 ) {
      String protocol = transMeta.getVariable( "engine.protocol" );
      String host = transMeta.getVariable( "engine.host" );
      String port = transMeta.getVariable( "engine.port" );
      //default value for ssl for now false
      boolean ssl = "https".equalsIgnoreCase( protocol ) || "wss".equalsIgnoreCase( protocol );
      return new TransWebSocketEngineAdapter( transMeta, host, port, ssl );
    } else {
      try {
        return PluginRegistry.getInstance().getPlugins( EnginePluginType.class ).stream()
          .filter( useThisEngine() )
          .findFirst()
          .map( plugin -> (Engine) loadPlugin( plugin ) )
          .map( engine -> {
            log.logBasic( "Using execution engine " + engine.getClass().getCanonicalName() );
            return (Trans) new TransEngineAdapter( engine, transMeta );
          } )
          .orElseThrow(
            () -> new KettleException( "Unable to find engine [" + transMeta.getVariable( "engine" ) + "]" ) );
      } catch ( KettleException e ) {
        log.logError( "Failed to load engine", e );
        throw new RuntimeException( e );
      }
    }
  }

  /**
   * Uses a trans variable called "engine" to determine which engine to use.
   */
  private Predicate<PluginInterface> useThisEngine() {
    return plugin -> Arrays.stream( plugin.getIds() )
      .filter( id -> id.equals( ( transMeta.getVariable( "engine" ) ) ) )
      .findAny()
      .isPresent();
  }


  private Object loadPlugin( PluginInterface plugin ) {
    try {
      return PluginRegistry.getInstance().loadClass( plugin );
    } catch ( KettlePluginException e ) {
      throw new RuntimeException( e );
    }
  }
}
