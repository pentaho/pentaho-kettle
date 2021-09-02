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
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
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
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( BaseMessages.class )
public class PanCommandExecutorTest {

  private static final String FS_METASTORE_NAME = "FS_METASTORE";
  private static final String REPO_METASTORE_NAME = "REPO_METASTORE";

  private static final String SAMPLE_KTR = "hello-world.ktr";
  private static final String BASE64_FAIL_ON_INIT_KTR = "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";
  private static final String FAIL_ON_INIT_KTR = "fail_on_exec.ktr";

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

  @Test
  public void testTransformationInitializationErrorExtensionPointCalled() throws Throwable {
    boolean kettleXMLExceptionThrown = false;
    Trans trans = null;
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransformationFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( FAIL_ON_INIT_KTR );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( BASE64_FAIL_ON_INIT_KTR );
    try {
      trans =
        mockedPanCommandExecutor.loadTransFromFilesystem( "", FAIL_ON_INIT_KTR, "", BASE64_FAIL_ON_INIT_KTR );
    } catch ( KettleXMLException e ) {
      kettleXMLExceptionThrown = true;
    }
    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( PanCommandExecutor.class );
    panCommandExecutor.execute( params );

    Assert.assertTrue( kettleXMLExceptionThrown );

    verify( extensionPoint, times( 1 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( trans ) );
  }
}
