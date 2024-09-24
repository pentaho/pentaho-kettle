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

package org.pentaho.di.plugins.fileopensave.service;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.ProviderServiceInterface;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.recents.RecentFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ServiceProvider( id = "ProviderServiceService", description = "Allows external file providers to register themselves and access to providers", provides = ProviderServiceInterface.class )
public class ProviderServiceService implements ProviderServiceInterface, ServiceProviderInterface<ProviderServiceInterface> {

  private static ProviderService providerService;

  public ProviderServiceService() {
    initProviderService();
  }

  private static void initProviderService() {
    if ( null != providerService ) {
      return;
    }
    LocalFileProvider localProvider = new LocalFileProvider();
    RepositoryFileProvider repoProvider = new RepositoryFileProvider();
    RecentFileProvider recentProvider = new RecentFileProvider();
    VFSFileProvider vfsProvider = new VFSFileProvider();

    List<FileProvider> fileProviders = new ArrayList<>();
    fileProviders.addAll( Arrays.asList( recentProvider, localProvider, repoProvider, vfsProvider ) );
    providerService =
      new ProviderService( fileProviders );
  }

  public static ProviderService get() {
    initProviderService();
    return providerService;
  }

  @Override public void addProviderService( FileProvider fileProvider ) {
    initProviderService();
    providerService.add( fileProvider );
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
