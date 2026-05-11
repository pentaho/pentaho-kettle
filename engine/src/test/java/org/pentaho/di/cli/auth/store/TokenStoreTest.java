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

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TokenStoreTest {

  @Test
  public void contractStubCanSaveLoadDeleteAndReportAvailability() {
    InMemoryTokenStore store = new InMemoryTokenStore();
    StoredCredential credential = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .oauthAccessToken( "access-token" )
      .build();

    assertTrue( store.isAvailable() );

    store.save( credential );
    assertSame( credential, store.load().orElseThrow() );

    store.delete();
    assertFalse( store.load().isPresent() );
  }

  private static final class InMemoryTokenStore implements TokenStore {
    private StoredCredential credential;

    @Override
    public void save( StoredCredential credential ) {
      this.credential = credential;
    }

    @Override
    public Optional<StoredCredential> load() {
      return Optional.ofNullable( credential );
    }

    @Override
    public void delete() {
      credential = null;
    }

    @Override
    public boolean isAvailable() {
      return true;
    }
  }
}
