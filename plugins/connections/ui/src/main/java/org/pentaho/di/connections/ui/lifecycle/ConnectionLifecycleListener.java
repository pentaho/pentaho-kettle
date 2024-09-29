/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui.lifecycle;

import org.pentaho.di.connections.ui.tree.ConnectionFolderProvider;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.providers.other.OtherConnectionDetailsProvider;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 7/6/18.
 */
@LifecyclePlugin( id = "VFSConnectionLifecycleListener" )
public class ConnectionLifecycleListener implements LifecycleListener {

  private static final String OTHER = "other";
  private static final String FTP_SCHEMA = "ftp";
  private static final String HTTP_SCHEMA = "http";
  private static final Logger logger = LoggerFactory.getLogger( ConnectionLifecycleListener.class );

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;
  private MetastoreLocator metastoreLocator;

  public ConnectionLifecycleListener() {
    try {
      Collection<MetastoreLocator> metastoreLocators = PluginServiceLoader.loadServices( MetastoreLocator.class );
      metastoreLocator = metastoreLocators.stream().findFirst().get();
    } catch ( Exception e ) {
      logger.error( "Error getting MetastoreLocator", e );
      throw new IllegalStateException( e );
    }
  }

  @Override
  public void onStart( LifeEventHandler handler ) throws LifecycleException {
    Spoon spoon = spoonSupplier.get();
    if ( spoon != null ) {
      spoon.getTreeManager()
        .addTreeProvider( Spoon.STRING_TRANSFORMATIONS, new ConnectionFolderProvider( metastoreLocator ) );
      spoon.getTreeManager().addTreeProvider( Spoon.STRING_JOBS, new ConnectionFolderProvider( metastoreLocator ) );
    }
    connectionManagerSupplier.get()
      .addConnectionProvider( OtherConnectionDetailsProvider.SCHEME, new OtherConnectionDetailsProvider() );
    VFSLookupFilter vfsLookupFilter = new VFSLookupFilter();
    vfsLookupFilter.addKeyLookup( FTP_SCHEMA, OTHER );
    vfsLookupFilter.addKeyLookup( HTTP_SCHEMA, OTHER );
    connectionManagerSupplier.get().addLookupFilter( vfsLookupFilter );
  }

  @Override
  public void onExit( LifeEventHandler handler ) throws LifecycleException {

  }
}
