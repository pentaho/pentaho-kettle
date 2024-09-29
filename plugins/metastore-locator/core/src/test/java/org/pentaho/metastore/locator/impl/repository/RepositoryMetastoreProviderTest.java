/*!
 * Copyright 2010 - 2023 Hitachi Vantara.  All rights reserved.
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
