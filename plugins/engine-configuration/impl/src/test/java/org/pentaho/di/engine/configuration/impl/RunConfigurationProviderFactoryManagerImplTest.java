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

package org.pentaho.di.engine.configuration.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.RunConfigurationProviderFactory;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RunConfigurationProviderFactoryManagerImplTest {


  ArrayList<RunConfigurationProviderFactory> savedFactories;

  String DEFAULT_PROVIDER_TYPE = new DefaultRunConfigurationProvider( null ).getType();

  /**
   * This will serve the double purpose of initializing the singleton in the first call
   * and in backup/restore of the default state
   */
  @Before
  public void saveDefaults() {
    savedFactories = RunConfigurationProviderFactoryManagerImpl.getInstance()
      .getFactories();
  }

  @After
  public void restoreDefaults() {
    RunConfigurationProviderFactoryManagerImpl.getInstance().setFactories( savedFactories );
  }

  @Test
  public void testDefault() {
    List<RunConfigurationProvider> providers =
      RunConfigurationProviderFactoryManagerImpl.getInstance().generateProviders();

    Assert.assertEquals( 1, providers.size() );
    Assert.assertEquals( DEFAULT_PROVIDER_TYPE, providers.get( 0 ).getType() );
  }

  @Test
  public void testAddFactories() {
    String PROVIDER1_TYPE = "ONE";
    String PROVIDER2_TYPE = "TWO";

    List<String> expectedProviderTypes =
      Arrays.asList( DEFAULT_PROVIDER_TYPE, PROVIDER1_TYPE, PROVIDER2_TYPE );

    CheckedMetaStoreSupplier metaStoreSupplier = mock( CheckedMetaStoreSupplier.class );

    ArrayList<RunConfigurationProviderFactory> factories = new ArrayList<>();

    RunConfigurationProviderFactory factory1 = mock( RunConfigurationProviderFactory.class );
    RunConfigurationProviderFactory factory2 = mock( RunConfigurationProviderFactory.class );

    RunConfigurationProvider provider1 = mock( RunConfigurationProvider.class );
    RunConfigurationProvider provider2 = mock( RunConfigurationProvider.class );

    when( provider1.getType() ).thenReturn( PROVIDER1_TYPE );
    when( provider2.getType() ).thenReturn( PROVIDER2_TYPE );

    when( factory1.getProvider( metaStoreSupplier ) ).thenReturn( provider1 );
    when( factory2.getProvider( metaStoreSupplier ) ).thenReturn( provider2 );

    factories.addAll( RunConfigurationProviderFactoryManagerImpl.getInstance().getFactories() );
    factories.add( factory1 );
    factories.add( factory2 );

    RunConfigurationProviderFactoryManagerImpl.getInstance().setFactories( factories );

    List<RunConfigurationProvider> providers =
      RunConfigurationProviderFactoryManagerImpl.getInstance().generateProviders( metaStoreSupplier );

    Assert.assertEquals( 3, providers.size() );

    List<String> providerTypes = new ArrayList<>();
    providers.stream().forEach( provider -> providerTypes.add( provider.getType() ) );

    Assert.assertTrue( providerTypes.containsAll( expectedProviderTypes ) );
  }
}
