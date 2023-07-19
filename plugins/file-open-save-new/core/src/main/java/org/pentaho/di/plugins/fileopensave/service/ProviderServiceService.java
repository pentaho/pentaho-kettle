/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
