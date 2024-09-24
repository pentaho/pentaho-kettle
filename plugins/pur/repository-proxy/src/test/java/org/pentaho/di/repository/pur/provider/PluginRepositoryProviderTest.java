/*!
 * Copyright 2018 - 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
