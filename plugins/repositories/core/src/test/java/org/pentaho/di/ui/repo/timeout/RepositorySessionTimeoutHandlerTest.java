/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repo.timeout;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.metastore.api.IMetaStore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositorySessionTimeoutHandlerTest {

  private ReconnectableRepository repository;

  private RepositoryConnectController repositoryConnectController;

  private RepositorySessionTimeoutHandler timeoutHandler;

  @Before
  public void before() {
    repository = mock( ReconnectableRepository.class );
    repositoryConnectController = mock( RepositoryConnectController.class );
    timeoutHandler = new RepositorySessionTimeoutHandler( repository, repositoryConnectController );
  }

  @Test
  public void connectedToRepository() {
    when( repository.isConnected() ).thenReturn( true );
    assertTrue( timeoutHandler.connectedToRepository() );
  }

  @Test
  public void connectedToRepositoryReturnsFalse() {
    when( repository.isConnected() ).thenReturn( false );
    assertFalse( timeoutHandler.connectedToRepository() );
  }

  @Test
  public void wrapMetastoreWithTimeoutHandler() throws Throwable {
    IMetaStore metaStore = mock( IMetaStore.class );
    doThrow( KettleRepositoryLostException.class ).when( metaStore ).createNamespace( any() );
    SessionTimeoutHandler sessionTimeoutHandler = mock( SessionTimeoutHandler.class );
    IMetaStore wrappedMetaStore =
        RepositorySessionTimeoutHandler.wrapMetastoreWithTimeoutHandler( metaStore, sessionTimeoutHandler );

    wrappedMetaStore.createNamespace( "TEST_NAMESPACE" );

    verify( sessionTimeoutHandler ).handle( any(), any(), any(), any() );
  }

}
