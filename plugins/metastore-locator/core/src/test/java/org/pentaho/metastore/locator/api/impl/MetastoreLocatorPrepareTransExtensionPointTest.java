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

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tkafalas on 7/25/2017.
 */
public class MetastoreLocatorPrepareTransExtensionPointTest {

  @Test
  public void testCallExtensionPoint() throws Exception {
    MetastoreLocatorOsgi metastoreLocator = new MetastoreLocatorImpl();
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    Collection<MetastoreLocator> metastoreLocators = new ArrayList<>();
    metastoreLocators.add( (MetastoreLocator) metastoreLocator );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( MetastoreLocator.class ) )
        .thenReturn( metastoreLocators );
      MetastoreLocatorPrepareTransExtensionPoint metastoreLocatorPrepareTransExtensionPoint =
        new MetastoreLocatorPrepareTransExtensionPoint();

      metastoreLocatorPrepareTransExtensionPoint.callExtensionPoint( logChannelInterface, mockTransMeta );
      verify( mockTransMeta ).setMetastoreLocatorOsgi( eq( metastoreLocator ) );
    }
  }

}
