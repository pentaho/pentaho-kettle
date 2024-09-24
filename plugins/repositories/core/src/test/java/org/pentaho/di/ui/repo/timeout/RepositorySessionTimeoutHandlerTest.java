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
