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
package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.ael.websocket.TransWebSocketEngineAdapter;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TransSupplier implements Supplier<Trans> {

  private static final Class<?> PKG = TransSupplier.class;
  private static final String MSG_KETTLE_ENGINE = "TransSupplier.SelectedEngine.Kettle";
  private static final String MSG_SPARK_ENGINE = "TransSupplier.SelectedEngine.Spark";

  private final TransMeta transMeta;
  private final LogChannelInterface log;
  private final Supplier<Trans> fallbackSupplier;

  public TransSupplier( TransMeta transMeta, LogChannelInterface log, Supplier<Trans> fallbackSupplier ) {
    this.transMeta = transMeta;
    this.log = log;
    this.fallbackSupplier = fallbackSupplier;
  }

  /**
   * Creates the appropriate trans.  Either 1)  A {@link TransWebSocketEngineAdapter} wrapping an Engine if an alternate
   * execution engine has been selected 2)  A legacy {@link Trans} otherwise.
   */
  public Trans get() {
    if ( Utils.isEmpty( transMeta.getVariable( "engine" ) ) ) {
      log.logBasic( BaseMessages.getString( PKG, MSG_KETTLE_ENGINE ) );
      return fallbackSupplier.get();
    }

    Variables variables = new Variables();
    variables.initializeVariablesFrom( null );
    String protocol = transMeta.environmentSubstitute( transMeta.getVariable( "engine.scheme" ) );
    String url = transMeta.environmentSubstitute( transMeta.getVariable( "engine.url" ) );

    URI uri = URI.create( protocol + url );

    //default value for ssl for now false
    boolean ssl = "https".equalsIgnoreCase( protocol ) || "wss".equalsIgnoreCase( protocol );
    log.logBasic( BaseMessages.getString( PKG, MSG_SPARK_ENGINE, protocol, url ) );
    return new TransWebSocketEngineAdapter( transMeta, uri.getHost(), uri.getPort(), ssl );
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
