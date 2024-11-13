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

package org.pentaho.metastore.locator.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.locator.api.MetastoreProvider;
import org.pentaho.metastore.locator.api.impl.MetastoreLocatorImpl;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNotNull;

/**
 * Created by tkafalas 7/26/2017.
 */
public class MetastoreLocatorImplTest {
  private MetastoreLocatorImpl metastoreLocator;

  @Before
  public void setup() {
    metastoreLocator = new MetastoreLocatorImpl();
  }

  @Test
  public void testgetMetastoreNone() {
    assertNull( metastoreLocator.getExplicitMetastore( "" ) );
  }

  @Test
  public void testGetMetastoreSingleNull() {
    // Test a null metastore provider that delivers a null metastore
    MetastoreProvider provider = mock( MetastoreProvider.class );
    when( provider.getProviderType() ).thenReturn( MetastoreLocator.LOCAL_PROVIDER_KEY );
    Collection<MetastoreProvider> providerCollection = new ArrayList<>();
    providerCollection.add( provider );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( MetastoreProvider.class ) )
        .thenReturn( providerCollection );

      assertNull( metastoreLocator.getExplicitMetastore( MetastoreLocator.LOCAL_PROVIDER_KEY ) );
      verify( provider ).getMetastore();
    }
  }

  @Test
  public void testGetMetastoreTest() {
    //Test that repository metastore gets returned if both local and repository metastore providers exist.
    //Also test that both providers can be accessed directly.
    MetastoreProvider localProvider = mock( MetastoreProvider.class );
    IMetaStore localMeta = mock( IMetaStore.class );
    when( localProvider.getMetastore() ).thenReturn( localMeta );
    when( localProvider.getProviderType() ).thenReturn( MetastoreLocator.LOCAL_PROVIDER_KEY );
    MetastoreProvider repoProvider = mock( MetastoreProvider.class );
    IMetaStore repoMeta = mock( IMetaStore.class );
    when( repoProvider.getMetastore() ).thenReturn( repoMeta );
    when( repoProvider.getProviderType() ).thenReturn( MetastoreLocator.REPOSITORY_PROVIDER_KEY );
    Collection<MetastoreProvider> providerCollection = new ArrayList<>();
    providerCollection.add( localProvider );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( MetastoreProvider.class ) )
        .thenReturn( providerCollection );

      // only local provider exists
      assertEquals( localMeta, metastoreLocator.getMetastore() );
      providerCollection.clear();
      providerCollection.add( repoProvider );
      // only repo provider exists
      assertEquals( repoMeta, metastoreLocator.getMetastore() );
      providerCollection.add( localProvider );

      // both providers exist
      assertEquals( localMeta, metastoreLocator.getExplicitMetastore( MetastoreLocator.LOCAL_PROVIDER_KEY ) );
      assertEquals( repoMeta, metastoreLocator.getExplicitMetastore( MetastoreLocator.REPOSITORY_PROVIDER_KEY ) );
    }
  }

  @Test
  public void testSetAndDisposeEmbeddedMetastore() throws MetaStoreException {
    IMetaStore embeddedMeta = mock( IMetaStore.class );
    when( embeddedMeta.getName() ).thenReturn( "MetastoreUniqueName" );
    String key = metastoreLocator.setEmbeddedMetastore( embeddedMeta );
    assertEquals( "MetastoreUniqueName", key );
    assertNotNull( key, "Embedded key value not returned" );
    assertEquals( embeddedMeta, metastoreLocator.getExplicitMetastore( key ) );
    assertEquals( embeddedMeta, metastoreLocator.getMetastore( key ) );

    metastoreLocator.disposeMetastoreProvider( key );
    assertNull( metastoreLocator.getExplicitMetastore( key ) );
  }
}
