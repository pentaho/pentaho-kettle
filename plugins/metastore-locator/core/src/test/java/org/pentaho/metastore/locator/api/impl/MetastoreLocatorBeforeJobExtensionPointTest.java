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
import org.pentaho.di.job.JobMeta;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tkafalas on 8/11/2017.
 */
public class MetastoreLocatorBeforeJobExtensionPointTest {

  @Test
  public void testCallExtensionPoint() throws Exception {
    MetastoreLocatorOsgi metastoreLocator = new MetastoreLocatorImpl();
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    Collection<MetastoreLocator> metastoreLocators = new ArrayList<>();
    metastoreLocators.add( (MetastoreLocator) metastoreLocator );
    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic( PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when( () -> PluginServiceLoader.loadServices( MetastoreLocator.class ) )
        .thenReturn( metastoreLocators );
      MetastoreLocatorBeforeJobExtensionPoint metastoreLocatorBeforeJobExtensionPoint =
        new MetastoreLocatorBeforeJobExtensionPoint();

      metastoreLocatorBeforeJobExtensionPoint.callExtensionPoint( logChannelInterface, mockJobMeta );
      verify( mockJobMeta ).setMetastoreLocatorOsgi( eq( metastoreLocator ) );
    }
  }

}
