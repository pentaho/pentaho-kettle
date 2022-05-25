/*!
 * Copyright 2010 - 2022 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.kettle.repository.locator.impl.platform;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.repository.Repository;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/15/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoSessionHolderRepositoryProviderTest {
  public static final String SESSION_TEST_NAME = "sessionTestName";
  @Mock Supplier<IPentahoSession> pentahoSessionSupplier;
  @Mock IPentahoSession pentahoSession;
  @Mock Function<IPentahoSession, ICacheManager> cacheManagerFunction;
  @Mock ICacheManager cacheManager;

  private PentahoSessionHolderRepositoryProvider
    pentahoSessionHolderRepositoryProvider;

  @Before
  public void setup() {
    when( cacheManagerFunction.apply( pentahoSession ) ).thenReturn( cacheManager );
    when( pentahoSession.getName() ).thenReturn( SESSION_TEST_NAME );
    pentahoSessionHolderRepositoryProvider =
      new PentahoSessionHolderRepositoryProvider( pentahoSessionSupplier, cacheManagerFunction );
  }

  @Test
  public void testNoArgConstructor() {
    assertNotNull( new PentahoSessionHolderRepositoryProvider() );
  }

  @Test
  public void testGetRepositoryNullSession() {
    when( pentahoSessionSupplier.get() ).thenReturn(null );
    assertNull( pentahoSessionHolderRepositoryProvider.getRepository() );
  }

  @Test
  public void testGetRepositoryNull() {
    when( pentahoSessionSupplier.get() ).thenReturn( pentahoSession );
    when( cacheManager.getFromRegionCache( PentahoSessionHolderRepositoryProvider.REGION, SESSION_TEST_NAME ) ).thenReturn( null );
    assertNull( pentahoSessionHolderRepositoryProvider.getRepository() );
  }

  @Test
  public void testGetRepositorySuccess() {
    Repository repository = mock( Repository.class );
    when( pentahoSessionSupplier.get() ).thenReturn( pentahoSession );
    when( cacheManager.getFromRegionCache( PentahoSessionHolderRepositoryProvider.REGION, SESSION_TEST_NAME ) )
      .thenReturn( repository );
    assertEquals( repository, pentahoSessionHolderRepositoryProvider.getRepository() );
  }
}
