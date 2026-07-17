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

package org.pentaho.di.cli.auth.store;

import org.pentaho.di.cli.config.CliConfig;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.cli.auth.BrokerAuthClient;

import java.io.File;

/**
 * Creates the most-secure {@link TokenStore} that the current environment
 * supports.
 *
 * <h2>Selection order</h2>
 * <ol>
 * <li>{@link EncryptedFileTokenStore} — AES-256-GCM with a key
 * derived from {@code user.name + hostname} via PBKDF2. Zero external
 * dependencies — relies only on {@code javax.crypto} bundled in every JRE.
 * Works consistently across all OS environments including Docker/CI.</li>
 * <li>{@link FileTokenStore} — plaintext fallback, only used when the caller
 * explicitly sets {@code token.store.backend=file} in Pan config. Never
 * selected automatically.</li>
 * </ol>
 *
 * <p>
 * The factory result is not cached globally so that configuration changes
 * are picked up on the next process start (normal CLI lifecycle).
 */
public final class TokenStoreFactory {

  private static final LogChannelInterface log = new LogChannel( "TokenStoreFactory" );

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( BrokerAuthClient.class, key, tokens );
  }

  private TokenStoreFactory() {
    // utility class
  }

  /**
   * Returns the highest-priority {@link TokenStore} available in the current
   * environment.
   *
   * <p>
   * Selection logic:
   * <ol>
   * <li>If {@code token.store.backend=file} is set in Pan config, returns
   * {@link FileTokenStore} immediately — encryption is bypassed.
   * Use this only if the encrypted store is failing or for debugging.</li>
   * <li>Otherwise, returns {@link EncryptedFileTokenStore} — AES-256-GCM with a
   * machine-derived key. Works in all environments with zero external
   * tooling.</li>
   * </ol>
   *
   * <p>
   * {@link FileTokenStore} (plaintext) is never selected automatically.
   *
   * @return never {@code null}
   */
  public static TokenStore create() {
    String backend = CliConfig.getInstance().getTokenStoreBackend();
    File kettleDir = new File( Const.getKettleDirectory() );

    if ( CliConfig.TOKEN_STORE_BACKEND_FILE.equals( backend ) ) {
      log.logDebug( message( "TokenStoreFactory.UsePlainFile" ) );
      return new FileTokenStore( new File( kettleDir, FileTokenStore.SESSION_FILE_NAME ) );
    }

    log.logDebug( message( "TokenStoreFactory.UseEncryptedFile" ) );
    return new EncryptedFileTokenStore( new File( kettleDir, EncryptedFileTokenStore.ENC_FILE_NAME ) );
  }
}
