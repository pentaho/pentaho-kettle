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

package org.pentaho.kettle.repository.locator.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.repository.Repository;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 4/15/16.
 */
public class KettleRepositoryLocatorImplTest {
  private KettleRepositoryLocatorImpl kettleRepositoryLocator;

  @Before
  public void setup() {
    kettleRepositoryLocator = new KettleRepositoryLocatorImpl();
  }

  @Test
  public void testGetRepositoryNone() {
    assertNull( kettleRepositoryLocator.getRepository() );
  }

  @Test
  public void testGetRepositorySingleNull() {
    KettleRepositoryProvider provider = mock( KettleRepositoryProvider.class );
    Collection<KettleRepositoryProvider> providerCollection = new ArrayList<>();
    providerCollection.add( provider );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( any() ) ).thenReturn( providerCollection );
      assertNull( kettleRepositoryLocator.getRepository() );
      verify( provider ).getRepository();
    }
  }

  @Test
  public void testGetRepositoryMultiple() {
    KettleRepositoryProvider provider1 = mock( KettleRepositoryProvider.class );
    KettleRepositoryProvider provider2 = mock( KettleRepositoryProvider.class );
    KettleRepositoryProvider provider3 = mock( KettleRepositoryProvider.class );
    KettleRepositoryProvider provider4 = mock( KettleRepositoryProvider.class );
    Collection<KettleRepositoryProvider> providerCollection = new ArrayList<>();
    providerCollection.add( provider1 );
    providerCollection.add( provider2 );
    providerCollection.add( provider3 );
    providerCollection.add( provider4 );

    Repository repository = mock( Repository.class );
    when( repository.getName() ).thenReturn( "repo1" );
    Repository repository2 = mock( Repository.class );
    when( repository2.getName() ).thenReturn( "repo2" );
    when( provider1.getRepository() ).thenReturn( repository );
    when( provider2.getRepository() ).thenReturn( repository2 );
    Repository repository3 = mock( Repository.class );
    when( repository3.getName() ).thenReturn( "repo3" );
    when( provider3.getRepository() ).thenReturn( repository3 );
    Repository repository4 = mock( Repository.class );
    when( repository4.getName() ).thenReturn( "repo4" );
    when( provider4.getRepository() ).thenReturn( repository4 );

    // this test is a bit ugly and fairly dependent on the implementation of the collection being used here and
    // the Java streams implementation
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( any() ) ).thenReturn( providerCollection );
      Repository repoReturned = kettleRepositoryLocator.getRepository();
      assertEquals( "Expected repo1 got " + repoReturned.getName(), repository, repoReturned );
      verify( provider1 ).getRepository();
      verify( provider2, never() ).getRepository();
      verify( provider3, never() ).getRepository();
      verify( provider4, never() ).getRepository();
    }
  }
}
