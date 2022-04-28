package org.pentaho.di.plugins.fileopensave.service;

import java.util.Arrays;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.recents.RecentFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

public enum ProviderServiceService {

    INSTANCE;

    private ProviderService providerService;

    private ProviderServiceService() {

        LocalFileProvider localProvider = new LocalFileProvider();
        RepositoryFileProvider repoProvider = new RepositoryFileProvider();
        RecentFileProvider recentProvider = new RecentFileProvider();
        VFSFileProvider vfsProvider = new VFSFileProvider();

        ConnectionManager.getInstance().setMetastoreSupplier( () -> {

            try {
                return MetaStoreConst.openLocalPentahoMetaStore();
            } catch ( Exception e ) {
                // Error getting metastore
                throw new RuntimeException( e );
            }
        } );

        this.providerService = new ProviderService( Arrays.asList( recentProvider, localProvider, repoProvider, vfsProvider ) );

    }

    public ProviderService get() {
        return providerService;
    }

}
