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
import org.mockito.MockedStatic;
import org.pentaho.di.cli.config.CliConfig;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class TokenStoreFactoryTest {

  @Test
  public void createReturnsPlainFileStoreWhenConfigured() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.getTokenStoreBackend() ).thenReturn( CliConfig.TOKEN_STORE_BACKEND_FILE );

    try ( MockedStatic<CliConfig> mockedConfig = mockStatic( CliConfig.class ) ) {
      mockedConfig.when( CliConfig::getInstance ).thenReturn( cliConfig );

      TokenStore tokenStore = TokenStoreFactory.create();

      assertTrue( tokenStore instanceof FileTokenStore );
    }
  }

  @Test
  public void createReturnsEncryptedStoreWhenConfiguredBackendIsEncrypted() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.getTokenStoreBackend() ).thenReturn( CliConfig.TOKEN_STORE_BACKEND_ENC );

    try ( MockedStatic<CliConfig> mockedConfig = mockStatic( CliConfig.class ) ) {
      mockedConfig.when( CliConfig::getInstance ).thenReturn( cliConfig );

      TokenStore tokenStore = TokenStoreFactory.create();

      assertTrue( tokenStore instanceof EncryptedFileTokenStore );
    }
  }
}
