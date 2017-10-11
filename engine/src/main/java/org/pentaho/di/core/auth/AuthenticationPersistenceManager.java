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

package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationFactoryException;
import org.pentaho.di.core.auth.core.AuthenticationManager;
import org.pentaho.di.core.auth.core.AuthenticationProvider;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;

public class AuthenticationPersistenceManager {
  private static final Class<?> PKG = AuthenticationPersistenceManager.class;
  private static final LogChannelInterface log = new LogChannel( AuthenticationPersistenceManager.class.getName() );

  public static AuthenticationManager getAuthenticationManager() {
    AuthenticationManager manager = new AuthenticationManager();
    manager.registerAuthenticationProvider( new NoAuthenticationAuthenticationProvider() );

    // TODO: Register providers from metastore

    for ( PluginInterface plugin : PluginRegistry.getInstance().getPlugins( AuthenticationConsumerPluginType.class ) ) {
      try {
        Object pluginMain = PluginRegistry.getInstance().loadClass( plugin );
        if ( pluginMain instanceof AuthenticationConsumerType ) {
          Class<? extends AuthenticationConsumer<?, ?>> consumerClass =
              ( (AuthenticationConsumerType) pluginMain ).getConsumerClass();
          manager.registerConsumerClass( consumerClass );
        } else {
          throw new KettlePluginException( BaseMessages.getString( PKG,
              "AuthenticationPersistenceManager.NotConsumerType", pluginMain, AuthenticationConsumerType.class ) );
        }
      } catch ( KettlePluginException e ) {
        log.logError( e.getMessage(), e );
      } catch ( AuthenticationFactoryException e ) {
        log.logError( e.getMessage(), e );
      }
    }
    return manager;
  }

  public static void persistAuthenticationProvider( AuthenticationProvider authenticationProvider ) {
    // TODO: Persist to metastore
  }
}
