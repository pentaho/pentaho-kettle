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
package org.pentaho.di.repository.pur.provider;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PluginRepositoryProviderTest {

  private RepositoryProvider pluginRepositoryProvider;
  private final String repoPassword = "password";
  private final String repoUsername = "username";

  @Before
  public void setUp() {
    pluginRepositoryProvider = spy( new RepositoryProvider() );
  }

  @Test
  public void testGetRepoUserName() {
    pluginRepositoryProvider.setUsername( repoUsername );

    String instanceRepoUsername = pluginRepositoryProvider.getUsername();

    assertEquals( repoUsername, instanceRepoUsername );
  }

  @Test
  public void testGetPassword() {
    String repoPassword = "password";
    pluginRepositoryProvider.setPassword( repoPassword );

    String instanceRepoPassword = pluginRepositoryProvider.getPassword();

    assertEquals( repoPassword, instanceRepoPassword );
  }

  @Test
  public void testGetRepository() {
    Repository repository = null;
    Repository mockRepository = mock( Repository.class );

    pluginRepositoryProvider.setRepository( mockRepository );
    repository = pluginRepositoryProvider.getRepository();

    assertNotNull( repository );
  }

  @Test
  public void testGetRepositoryShouldReconnectRepositoryOnChangePassword() {
    Repository repository = null;
    Repository mockRepository = mock( Repository.class );
    pluginRepositoryProvider.setPassword( "SomePassword" );

    pluginRepositoryProvider.setRepository( mockRepository );
    repository = pluginRepositoryProvider.getRepository();

    assertNotNull( repository );
    verify( pluginRepositoryProvider, times( 1 ) ).reconnectToRepository();
  }

  @Test
  public void testGetRepositoryShouldReconnectRepositoryOnChangeUsername() {
    Repository repository = null;
    Repository mockRepository = mock( Repository.class );
    pluginRepositoryProvider.setUsername( "SomeUsername" );

    pluginRepositoryProvider.setRepository( mockRepository );
    repository = pluginRepositoryProvider.getRepository();

    assertNotNull( repository );
    verify( pluginRepositoryProvider, times( 1 ) ).reconnectToRepository();
  }

  @Test
  public void testReconnectRepository() {
    Repository mockRepository = mock( Repository.class );
    pluginRepositoryProvider.setRepository( mockRepository );
    when( mockRepository.isConnected() ).thenReturn( true );
    pluginRepositoryProvider.setUsername( repoUsername );
    pluginRepositoryProvider.setPassword( repoPassword );

    pluginRepositoryProvider.reconnectToRepository();

    verify( mockRepository, times( 1 ) ).disconnect();
    try {
      verify( mockRepository, times( 1 ) ).connect( anyString(), anyString() );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }
}
