/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;
import org.pentaho.di.shared.MemorySharedObjectsIO;

public class StreamToTransNodeConverterTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }

  @BeforeClass
  public static void setupClass() {
    DefaultBowl.getInstance().setSharedObjectsIO( new MemorySharedObjectsIO() );
  }

  @Test
  public void convertPostRepoSave() throws Exception {
    StreamToTransNodeConverter converter = mock( StreamToTransNodeConverter.class );
    doCallRealMethod().when( converter ).convertPostRepoSave( any( RepositoryFile.class ) );
    Repository repository = mock( Repository.class );
    when( converter.connectToRepository() ).thenReturn( repository );

    TransMeta transMeta = mock( TransMeta.class );
    when( repository.loadTransformation( any(), any() ) ).thenReturn( transMeta );
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

  // depends on non-stream mode
  //@Test
  public void testConvertTransWithMissingPlugins() throws IOException, KettleException {
    RepositoryFile repositoryFile = new RepositoryFile.Builder( "test file" ).build();
    IUnifiedRepository pur = mock( IUnifiedRepository.class );
    when( pur.getFileById( "MissingTrans.ktr" ) ).thenReturn( repositoryFile );

    TransMeta transMeta = new TransMeta();

    Repository repository = mock( Repository.class );
    when( repository.loadTransformation( any( StringObjectId.class ), anyString() ) ).thenReturn( transMeta );

    StreamToTransNodeConverter transNodeConverter = new StreamToTransNodeConverter( pur );
    transNodeConverter = spy( transNodeConverter );
    doReturn( repository ).when( transNodeConverter ).connectToRepository();

    try {
      transNodeConverter.convert( getClass().getResource( "MissingTrans.ktr" ).openStream(), "UTF-8", "application/vnd.pentaho.transformation" );
    } catch ( ConverterException e ) {
      assertTrue( e.getMessage().contains( "MissingPlugin" ) );
      return;
    }
    fail();
  }

  private List<DatabaseMeta> getDummyDatabases() {
    List<DatabaseMeta> databases = new ArrayList<>(  );
    databases.add( new DatabaseMeta( "database1", "Oracle", "Native", "", "", "", "", "" ) );
    databases.add( new DatabaseMeta( "database2", "Oracle", "Native", "", "", "", "", "" ) );
    databases.add( new DatabaseMeta( "database3", "Oracle", "Native", "", "", "", "", "" ) );
    databases.add( new DatabaseMeta( "database4", "Oracle", "Native", "", "", "", "", "" ) );
    return databases;
  }

  private void setDatabases( AbstractMeta meta, List<DatabaseMeta> databases ) throws Exception {
    meta.getDatabaseManagementInterface().clear();
    for ( DatabaseMeta db : databases ) {
      meta.getDatabaseManagementInterface().add( db );
    }
  }
}

