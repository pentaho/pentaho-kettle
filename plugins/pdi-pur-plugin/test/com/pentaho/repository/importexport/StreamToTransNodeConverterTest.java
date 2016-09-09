/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.repository.importexport;

import org.junit.Test;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamToTransNodeConverterTest {
  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }

  @Test
  public void convertPostRepoSave() throws Exception {
    StreamToTransNodeConverter converter = mock( StreamToTransNodeConverter.class );
    doCallRealMethod().when( converter ).convertPostRepoSave( any( RepositoryFile.class ) );
    Repository repository = mock( Repository.class );
    when( converter.connectToRepository() ).thenReturn( repository );

    TransMeta transMeta = mock( TransMeta.class );
    when( repository.loadTransformation( any(), anyString() ) ).thenReturn( transMeta );
    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getId() ).thenReturn( "fileId" );

    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransImportAfterSaveToRepo.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] {"extensionpointId"} );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    converter.convertPostRepoSave( file );

    verify( extensionPoint, times( 1 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( transMeta ) );
  }
}
