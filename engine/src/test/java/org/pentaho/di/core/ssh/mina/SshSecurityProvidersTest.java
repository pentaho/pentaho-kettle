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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.security.Provider;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SshSecurityProvidersTest {

  private Provider originalProvider;

  /** Stands in for a third-party (e.g. Snowflake JDBC) unsigned provider registered under the BC name. */
  private static class ForeignBcProvider extends Provider {
    ForeignBcProvider() {
      super( "BC", "1.0", "foreign provider using the BC name" );
    }
  }

  @Before
  public void setUp() {
    originalProvider = Security.getProvider( "BC" );
    Security.removeProvider( "BC" );
  }

  @After
  public void tearDown() {
    Security.removeProvider( "BC" );
    if ( originalProvider != null ) {
      Security.addProvider( originalProvider );
    }
  }

  @Test
  public void registersSignedProviderWhenNoneIsPresent() {
    assertNull( Security.getProvider( "BC" ) );

    SshSecurityProviders.ensureSignedBouncyCastleProvider();

    assertTrue( Security.getProvider( "BC" ) instanceof BouncyCastleProvider );
  }

  @Test
  public void replacesForeignProviderOwningTheBcName() {
    Security.addProvider( new ForeignBcProvider() );
    assertTrue( Security.getProvider( "BC" ) instanceof ForeignBcProvider );

    SshSecurityProviders.ensureSignedBouncyCastleProvider();

    assertTrue( Security.getProvider( "BC" ) instanceof BouncyCastleProvider );
  }

  @Test
  public void keepsExistingSignedProviderAndIsIdempotent() {
    BouncyCastleProvider existing = new BouncyCastleProvider();
    Security.addProvider( existing );

    SshSecurityProviders.ensureSignedBouncyCastleProvider();
    SshSecurityProviders.ensureSignedBouncyCastleProvider();

    assertSame( existing, Security.getProvider( "BC" ) );
  }

  @Test
  public void waitsForTheSecurityMonitorSoProviderMutationsAreNotInterleaved() throws Exception {
    Security.addProvider( new ForeignBcProvider() );
    Thread worker = new Thread( SshSecurityProviders::ensureSignedBouncyCastleProvider );

    synchronized ( Security.class ) {
      worker.start();
      worker.join( 300 );
      // Registry untouched while another party holds the JCA provider-mutation monitor.
      assertTrue( worker.isAlive() );
      assertTrue( Security.getProvider( "BC" ) instanceof ForeignBcProvider );
    }

    worker.join( 10_000 );
    assertTrue( Security.getProvider( "BC" ) instanceof BouncyCastleProvider );
  }

  @Test
  public void leavesOnlyOneProviderNamedBc() {
    Security.addProvider( new ForeignBcProvider() );

    SshSecurityProviders.ensureSignedBouncyCastleProvider();

    int count = 0;
    for ( Provider provider : Security.getProviders() ) {
      if ( "BC".equals( provider.getName() ) ) {
        count++;
      }
    }
    assertEquals( 1, count );
    assertNotNull( Security.getProvider( "BC" ) );
  }
}
