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

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileTokenStoreTest {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private Path tempDir;
  private Path sessionFile;

  @Before
  public void setUp() throws IOException {
    tempDir = Files.createTempDirectory( "file-token-store-test" );
    sessionFile = tempDir.resolve( ".kettle-sessions" );
  }

  @After
  public void tearDown() throws IOException {
    if ( tempDir != null && Files.exists( tempDir ) ) {
      Files.walk( tempDir )
        .sorted( java.util.Comparator.reverseOrder() )
        .forEach( path -> {
          try {
            Files.deleteIfExists( path );
          } catch ( IOException ignored ) {
            // best-effort temp cleanup
          }
        } );
    }
  }

  @Test
  public void saveAndLoadRoundTripCredential() {
    FileTokenStore store = new FileTokenStore( sessionFile.toFile() );
    StoredCredential credential = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionToken( "JSESSIONID" )
      .sessionCookie( "JSESSIONID=abc" )
      .username( "alice" )
      .sessionExpiry( 1234L )
      .oauthAccessToken( "access-token" )
      .oauthRefreshToken( "refresh-token" )
      .oauthTokenType( "Bearer" )
      .oauthIdpRegistrationId( "azure" )
      .oauthTokenExpiry( 5678L )
      .oauthRefreshHandle( "refresh-handle" )
      .oauthBrokerAuthHandle( "broker-auth-handle" )
      .build();

    store.save( credential );

    assertTrue( Files.exists( sessionFile ) );
    Optional<StoredCredential> loaded = store.load();
    assertTrue( loaded.isPresent() );
    assertEquals( credential, loaded.get() );
    assertTrue( store.isAvailable() );
  }

  @Test
  public void loadReturnsEmptyWhenFileIsMissingOrUnreadable() throws IOException {
    FileTokenStore store = new FileTokenStore( sessionFile.toFile() );

    assertFalse( store.load().isPresent() );

    Files.createDirectories( sessionFile );
    assertFalse( store.load().isPresent() );
  }

  @Test
  public void deleteRemovesSessionFileWhenPresent() throws IOException {
    FileTokenStore store = new FileTokenStore( sessionFile.toFile() );
    Files.writeString( sessionFile, "serverUrl=http://localhost:8080/pentaho", StandardCharsets.UTF_8 );

    store.delete();

    assertFalse( Files.exists( sessionFile ) );
  }
}
