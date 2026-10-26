/*! ****************************************************************************
**
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.core.ssh.mina;

import java.security.Provider;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Makes sure the JVM-wide {@code BC} JCA provider is the signed Bouncy Castle shipped with Pentaho.
 * <p>
 * Apache MINA SSHD resolves its Bouncy Castle provider by name ({@code Security.getProvider("BC")}) and only creates
 * its own when none is registered. Some JDBC drivers (for example Snowflake) register their own <b>shaded and
 * unsigned</b> Bouncy Castle under the same name from a static initializer. {@code Security.addProvider} never replaces
 * an already registered provider, so SSHD then ends up using the unsigned one, and JDKs that enforce JCE provider
 * signature verification reject it ("The JCE Provider ... is not signed").
 * <p>
 * Whoever registers the {@code BC} name first wins, so this class is invoked at Kettle start-up (covers Spoon, Pan,
 * Kitchen, Carte and the Pentaho Server, CE and EE). That repairs a driver that was loaded earlier and prevents one
 * loaded later from taking the name. It is also invoked before each SSH client is created as a safeguard for JVMs
 * where Kettle start-up did not run or the provider was changed afterwards. Calls are cheap and idempotent once the
 * signed provider owns the {@code BC} name.
 */
public final class SshSecurityProviders {

  static final String BC_PROVIDER_NAME = "BC";

  // java.util.logging on purpose: this runs before the Kettle log store is initialized.
  private static final Logger LOGGER = Logger.getLogger( SshSecurityProviders.class.getName() );

  private SshSecurityProviders() {
  }

  /**
   * Ensures the provider registered as {@code BC} is the signed {@link BouncyCastleProvider}. A foreign provider that
   * owns the name is replaced; if none is registered the signed one is added.
   */
  public static void ensureSignedBouncyCastleProvider() {
    try {
      if ( Security.getProvider( BC_PROVIDER_NAME ) instanceof BouncyCastleProvider ) {
        return;
      }
      // Built before touching the registry so that a failure here cannot leave the BC name unregistered.
      Provider signed = new BouncyCastleProvider();
      Provider replaced = null;
      // Security.addProvider/removeProvider are static synchronized, i.e. they lock Security.class. Holding the same
      // monitor makes recheck + remove + add atomic: a driver initializing concurrently cannot register its own
      // provider under the BC name between the removal and the addition (addProvider would then silently return -1
      // and leave the unsigned provider installed).
      synchronized ( Security.class ) {
        Provider current = Security.getProvider( BC_PROVIDER_NAME );
        if ( current instanceof BouncyCastleProvider ) {
          return;
        }
        if ( current != null ) {
          Security.removeProvider( BC_PROVIDER_NAME );
          replaced = current;
        }
        Security.addProvider( signed );
      }
      if ( replaced != null ) {
        LOGGER.log( Level.INFO, "Replaced the ''{0}'' security provider {1} with the signed Bouncy Castle provider used "
          + "for SSH.", new Object[] { BC_PROVIDER_NAME, replaced.getClass().getName() } );
      }
    } catch ( RuntimeException e ) {
      // Never block start-up or a connection attempt because the provider could not be adjusted.
      LOGGER.log( Level.WARNING, "Unable to register the signed Bouncy Castle security provider", e );
    }
  }
}
