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

package org.pentaho.metastore.locator.api.impl;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tkafalas on 7/25/2017.
 */
public class MetastoreLocatorExtensionPointTest {

  @Test
  public void testCallExtensionPointWithTransMeta() throws Exception {
    MetastoreLocatorOsgi metastoreLocator = new MetastoreLocatorImpl();
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    Collection<MetastoreLocator> metastoreLocators = new ArrayList<>();
    metastoreLocators.add( (MetastoreLocator) metastoreLocator );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( MetastoreLocator.class ) )
        .thenReturn( metastoreLocators );
      MetastoreLocatorExtensionPoint metastoreLocatorExtensionPoint =
        new MetastoreLocatorExtensionPoint();

      metastoreLocatorExtensionPoint.callExtensionPoint( logChannelInterface, mockTransMeta );
      verify( mockTransMeta ).setMetastoreLocatorOsgi( eq( metastoreLocator ) );
    }
  }

  @Test
  public void testCallExtensionPointWithTrans() throws Exception {
    MetastoreLocatorOsgi mockMetastoreLocator = new MetastoreLocatorImpl();
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    Trans mockTrans = mock( Trans.class );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    Collection<MetastoreLocator> metastoreLocators = new ArrayList<>();
    metastoreLocators.add( (MetastoreLocator) mockMetastoreLocator );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( MetastoreLocator.class ) )
        .thenReturn( metastoreLocators );
      MetastoreLocatorExtensionPoint metastoreLocatorExtensionPoint =
        new MetastoreLocatorExtensionPoint();

      metastoreLocatorExtensionPoint.callExtensionPoint( logChannelInterface, mockTrans );
      verify( mockTransMeta ).setMetastoreLocatorOsgi( eq( mockMetastoreLocator ) );
    }
  }

}
