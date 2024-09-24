/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
