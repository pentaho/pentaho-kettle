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
package org.pentaho.metastore.locator.impl.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.repository.Repository;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryLocator;
import org.pentaho.metastore.api.IMetaStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/18/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class RepositoryMetastoreProviderTest {
  @Mock KettleRepositoryLocator kettleRepositoryLocator;
  private RepositoryMetastoreProvider repositoryMetastoreProvider;

  @Before
  public void setup() {
    repositoryMetastoreProvider = new RepositoryMetastoreProvider( kettleRepositoryLocator );
  }

  @Test
  public void testGetMetastoreNullRepository() {
    when( kettleRepositoryLocator.getRepository() ).thenReturn( null );
    assertNull( repositoryMetastoreProvider.getMetastore() );
  }

  @Test
  public void testGetMetastoreSuccess() {
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    when( repository.getRepositoryMetaStore() ).thenReturn( metaStore );
    when( kettleRepositoryLocator.getRepository() ).thenReturn( repository );
    assertEquals( metaStore, repositoryMetastoreProvider.getMetastore() );
  }
}
