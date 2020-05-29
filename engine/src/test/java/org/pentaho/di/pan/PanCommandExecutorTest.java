/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.pan;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.kitchen.Kitchen;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;

import java.io.File;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.same;

@RunWith( PowerMockRunner.class )
@PrepareForTest( BaseMessages.class )
public class PanCommandExecutorTest {

  private static final String FS_METASTORE_NAME = "FS_METASTORE";
  private static final String REPO_METASTORE_NAME = "REPO_METASTORE";

  private static final String SAMPLE_KTR = "hello-world.ktr";

  private DelegatingMetaStore metastore = new DelegatingMetaStore();

  private Repository repository;
  private IMetaStore fsMetaStore;
  private IMetaStore repoMetaStore;
  private RepositoryDirectoryInterface directoryInterface;
  private PanCommandExecutor mockedPanCommandExecutor;
  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }

  @Before
  public void setUp() throws Exception {
    KettleLogStore.init();
    repository = mock( Repository.class );
    fsMetaStore = mock( IMetaStore.class );
    repoMetaStore = mock( IMetaStore.class );
    directoryInterface = mock( RepositoryDirectoryInterface.class );
    mockedPanCommandExecutor = mock( PanCommandExecutor.class );

    // mock actions from Metastore
    when( fsMetaStore.getName() ).thenReturn( FS_METASTORE_NAME );
    when( repoMetaStore.getName() ).thenReturn( REPO_METASTORE_NAME );
    metastore.addMetaStore( fsMetaStore );

    // mock actions from Repository
    when( repository.getMetaStore() ).thenReturn( repoMetaStore );

    // mock actions from PanCommandExecutor
    when( mockedPanCommandExecutor.getMetaStore() ).thenReturn( metastore );
    when( mockedPanCommandExecutor.loadRepositoryDirectory( anyObject(), anyString(), anyString(), anyString(), anyString() ) )
      .thenReturn( directoryInterface );

    // call real methods for loadTransFromFilesystem(), loadTransFromRepository();
    when( mockedPanCommandExecutor.loadTransFromFilesystem( anyString(), anyString(), anyString(), anyObject() ) ).thenCallRealMethod();
    when( mockedPanCommandExecutor.loadTransFromRepository( anyObject(), anyString(), anyString() ) ).thenCallRealMethod();
    when( mockedPanCommandExecutor.decodeBase64ToZipFile( anyObject(), anyBoolean() ) ).thenCallRealMethod();
    when( mockedPanCommandExecutor.decodeBase64ToZipFile( anyObject(), anyString() ) ).thenCallRealMethod();

  }

  @After
  public void tearDown() {
    metastore = null;
    repository = null;
    fsMetaStore = null;
    repoMetaStore = null;
    directoryInterface = null;
    mockedPanCommandExecutor = null;
  }

  @Test
  public void testMetastoreFromRepoAddedIn() throws Exception {

    // mock Trans loading from repo
    TransMeta t = new TransMeta( getClass().getResource( SAMPLE_KTR ).getPath() );
    when( repository.loadTransformation( anyString(), anyObject(), anyObject(), anyBoolean(), anyString() ) ).thenReturn( t );

    // test
    Trans trans = mockedPanCommandExecutor.loadTransFromRepository( repository, "", SAMPLE_KTR );
    assertNotNull( trans );
    assertNotNull( trans.getMetaStore() );
    assertTrue( trans.getMetaStore() instanceof DelegatingMetaStore );
    assertNotNull( ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList() );

    assertEquals( 2, ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList().size() );
    assertTrue( ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList().stream()
      .anyMatch( m -> {
        try {
          return REPO_METASTORE_NAME.equals( m.getName() );
        } catch ( Exception e ) {
          return false;
        }
      } ) );
  }

  @Test
  public void testMetastoreFromFilesystemAddedIn() throws Exception {

    String fullPath = getClass().getResource( SAMPLE_KTR ).getPath();

    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", fullPath, "", "" );
    assertNotNull( trans );
    assertNotNull( trans.getMetaStore() );
    assertTrue( trans.getMetaStore() instanceof DelegatingMetaStore );
    assertNotNull( ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList() );

    assertEquals( 1, ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList().size() );

    assertTrue( ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList().stream()
      .anyMatch( m -> {
        try {
          return FS_METASTORE_NAME.equals( m.getName() );
        } catch ( Exception e ) {
          return false;
        }
      } ) );
  }

  @Test
  public void testFilesystemBase64Zip() throws Exception {
    String fileName = "test.ktr";
    File zipFile = new File( getClass().getResource( "testKtrArchive.zip" ).toURI() );
    String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );
    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( null, fileName, null, base64Zip );
    assertNotNull( trans );
  }


  @Test
  public void testExecuteWithInvalidRepository() {
    // Create Mock Objects
    Params params = mock( Params.class );
    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( Kitchen.class );
    PowerMockito.mockStatic( BaseMessages.class );

    // Mock returns
    when( params.getRepoName() ).thenReturn( "NoExistingRepository" );
    when( BaseMessages.getString( any( Class.class ), anyString(), anyVararg() ) ).thenReturn( "" );

    try {
      Result result = panCommandExecutor.execute( params, null );
      Assert.assertEquals( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode(), result.getExitStatus() );
    } catch ( Throwable throwable ) {
      Assert.fail();
    }

  }

  /**
   * This method test a valid ktr and make sure the callExtensionPoint is never called, as this method is called
   * if the ktr fails in preparation step
   * @throws Throwable
   */
  @Test
  public void testNoTransformationFinishExtensionPointCalled() throws Throwable {
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransformationFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    // Execute a sample KTR
    String fullPath = getClass().getResource( SAMPLE_KTR ).getPath();
    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( fullPath );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( "" );
    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", fullPath, "", "" );

    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( PanCommandExecutor.class );
    Result result = panCommandExecutor.execute( params );
    verify( extensionPoint, times( 0 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( trans ) );
  }

  /**
   * This method test a ktr that fails in preparation step and and checks to make sure the callExtensionPoint is
   * called once.
   * @throws Throwable
   */
  @Test
  public void testTransformationFinishExtensionPointCalled() throws Throwable {
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransformationFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    // Execute a sample KTR
    String fullPath = getClass().getResource( "fail_on_prep_hello_world.ktr" ).getPath();
    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( fullPath );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( "" );
    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", fullPath, "", "" );

    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( PanCommandExecutor.class );
    Result result = panCommandExecutor.execute( params );
    verify( extensionPoint, times( 1 ) ).callExtensionPoint(  any( LogChannelInterface.class ), any( Trans.class ) );
  }

}
