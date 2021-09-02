/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.kitchen;

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
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( BaseMessages.class )
public class KitchenCommandExecutorTest {

  private KitchenCommandExecutor mockedKitchenCommandExecutor;
  private Result result;
  private LogChannelInterface logChannelInterface;

  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }
  
  @Before
  public void setUp() throws Exception {
    KettleLogStore.init();
    mockedKitchenCommandExecutor = mock( KitchenCommandExecutor.class );
    result = mock( Result.class );
    logChannelInterface = mock( LogChannelInterface.class );

    // call real methods for loadTransFromFilesystem(), loadTransFromRepository();
    when( mockedKitchenCommandExecutor.loadJobFromFilesystem( anyString(), anyString(), anyObject() ) ).thenCallRealMethod();
    when( mockedKitchenCommandExecutor.loadJobFromRepository( anyObject(), anyString(), anyString() ) ).thenCallRealMethod();
    when( mockedKitchenCommandExecutor.decodeBase64ToZipFile( anyObject(), anyBoolean() ) ).thenCallRealMethod();
    when( mockedKitchenCommandExecutor.decodeBase64ToZipFile( anyObject(), anyString() ) ).thenCallRealMethod();
    when( mockedKitchenCommandExecutor.getReturnCode() ).thenCallRealMethod();
  }

  @After
  public void tearDown() {
    mockedKitchenCommandExecutor = null;
    result = null;
    logChannelInterface = null;
  }

  @Test
  public void testFilesystemBase64Zip() throws Exception {
    String fileName = "hello-world.kjb";
    File zipFile = new File( getClass().getResource( "testKjbArchive.zip" ).toURI() );
    String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );
    Job job = mockedKitchenCommandExecutor.loadJobFromFilesystem( null, fileName, base64Zip );
    assertNotNull( job );
  }

  @Test
  public void testReturnCodeSuccess() {
    when( mockedKitchenCommandExecutor.getResult() ).thenReturn( result );
    when( result.getResult() ).thenReturn( true );
    assertEquals( mockedKitchenCommandExecutor.getReturnCode(), CommandExecutorCodes.Kitchen.SUCCESS.getCode() );
  }

  @Test
  public void testReturnCodeWithErrors() {
    mockStatic( BaseMessages.class );
    when( result.getNrErrors() ).thenReturn( new Long( 1 ) );
    when( mockedKitchenCommandExecutor.getResult() ).thenReturn( result );
    when( mockedKitchenCommandExecutor.getLog() ).thenReturn( logChannelInterface );
    when( BaseMessages.getString( any(), anyString() ) ).thenReturn( "NoMessage" );
    assertEquals( mockedKitchenCommandExecutor.getReturnCode(), CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() );
  }

  @Test
  public void testReturnCodeFailWithNoErrors() {
    when( mockedKitchenCommandExecutor.getResult() ).thenReturn( result );
    assertEquals( mockedKitchenCommandExecutor.getReturnCode(), CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() );
  }

  @Test
  public void testExecuteWithInvalidRepository() {
    // Create Mock Objects
    Params params = mock( Params.class );
    KitchenCommandExecutor kitchenCommandExecutor = new KitchenCommandExecutor( Kitchen.class );
    PowerMockito.mockStatic( BaseMessages.class );

    // Mock returns
    when( params.getRepoName() ).thenReturn( "NoExistingRepository" );
    when( BaseMessages.getString( any( Class.class ), anyString(), anyVararg() ) ).thenReturn( "" );

    try {
      Result result = kitchenCommandExecutor.execute( params, null );
      Assert.assertEquals( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), result.getExitStatus() );
    } catch ( Throwable throwable ) {
      Assert.fail();
    }
  }

  @Test
  public void testJobFailOnInitializationExtensionPointCall() throws Throwable {
    boolean kettleXMLExceptionThrown = false;
    Job job = null;

    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.JobFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    String fullPath = getClass().getResource( "brokenjob.kjb" ).getPath();
    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( fullPath );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( "" );
    try {
      job = mockedKitchenCommandExecutor.loadJobFromFilesystem( "", fullPath, "" );
    } catch ( KettleXMLException e ) {
      kettleXMLExceptionThrown = true;
    }

    KitchenCommandExecutor kitchenCommandExecutor = new KitchenCommandExecutor( KitchenCommandExecutor.class );
    Result result = kitchenCommandExecutor.execute( params );

    Assert.assertTrue( kettleXMLExceptionThrown );

    verify( extensionPoint, times( 1 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( job ) );
  }
}
