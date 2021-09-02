/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.spoon.Spoon;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
  * @author Luis Martins (16-Feb-2018)
  */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { Spoon.class, ExtensionPointHandler.class, RunConfigurationDelegate.class } )
public class RunConfigurationDelegateTest {

  private Spoon spoon;
  private RunConfigurationService service;
  private RunConfigurationDelegate delegate;

  @Before
  public void setup() {
    spoon = mock( Spoon.class );
    doReturn( mock( Shell.class ) ).when( spoon ).getShell();

    PowerMockito.mockStatic( Spoon.class );
    when( Spoon.getInstance() ).thenReturn( spoon );

    service = mock( RunConfigurationService.class );
    delegate = spy( new RunConfigurationDelegate( service ) );
  }

  @Test
  public void testNew() {
    assertSame( service, Whitebox.getInternalState( delegate, "configurationManager" ) );
  }

  @Test
  public void testCreate() throws Exception {
    RunConfigurationDialog dialog = mock( RunConfigurationDialog.class );
    whenNew( RunConfigurationDialog.class ).withAnyArguments().thenReturn( dialog );

    List<String> list = new ArrayList<>();
    list.add( "Configuration 1" );

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    doReturn( list ).when( service ).getNames();
    doReturn( config ).when( dialog ).open();

    delegate.create();

    verify( service, times( 1 ) ).save( config );
    verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
  }

  @Test
  public void testDelete() throws Exception {
    RunConfigurationDeleteDialog dialog = mock( RunConfigurationDeleteDialog.class );
    whenNew( RunConfigurationDeleteDialog.class ).withAnyArguments().thenReturn( dialog );

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    doReturn( SWT.YES ).when( dialog ).open();

    delegate.delete( config );

    verify( service, times( 1 ) ).delete( "Test" );
    verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
  }

  @Test
  public void testEdit() throws Exception {
    RunConfigurationDialog dialog = mock( RunConfigurationDialog.class );
    whenNew( RunConfigurationDialog.class ).withAnyArguments().thenReturn( dialog );

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    doReturn( config ).when( dialog ).open();
    doNothing().when( delegate ).updateLoadedJobs( "Test", config );

    delegate.edit( config );

    verify( delegate, times( 1 ) ).updateLoadedJobs( "Test", config );
    verify( service, times( 1 ) ).delete( "Test" );
    verify( service, times( 1 ) ).save( config );
    verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
  }

  @Test
  public void testLoad() {
    delegate.load();
    verify( service, times( 1 ) ).load();
  }

  @Test
  public void testUpdateLoadedJobs_Spark() {
    JobEntryTrans trans = new JobEntryTrans();
    trans.setRunConfiguration( "key" );

    JobMeta meta = new JobMeta();
    meta.addJobEntry( new JobEntryCopy( trans ) );

    JobMeta[] jobs = new JobMeta[] { meta };
    doReturn( jobs ).when( spoon ).getLoadedJobs();

    SparkRunConfiguration config = new SparkRunConfiguration();
    config.setName( "Test" );

    delegate.updateLoadedJobs( "key", config );

    assertEquals( "Test", trans.getRunConfiguration() );
  }

  @Test
  public void testUpdateLoadedJobs_PDI16777() {
    JobEntryTrans trans = new JobEntryTrans();
    trans.setRunConfiguration( "key" );

    JobMeta meta = new JobMeta();
    meta.addJobEntry( new JobEntryCopy( trans ) );

    JobMeta[] jobs = new JobMeta[] { meta };
    doReturn( jobs ).when( spoon ).getLoadedJobs();

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    delegate.updateLoadedJobs( "key", config );

    assertEquals( "Test", trans.getRunConfiguration() );
    assertEquals( "localhost", trans.getRemoteSlaveServerName() );
  }

  @Test
  public void testUpdateLoadedJobs_Exception() throws Exception {
    JobEntryTrans trans = new JobEntryTrans();
    trans.setRunConfiguration( "key" );

    JobMeta meta = new JobMeta();
    meta.addJobEntry( new JobEntryCopy( trans ) );

    JobMeta[] jobs = new JobMeta[] { meta };
    doReturn( jobs ).when( spoon ).getLoadedJobs();

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    LogChannelInterface log = mock( LogChannelInterface.class );
    doReturn( log ).when( spoon ).getLog();

    PowerMockito.mockStatic( ExtensionPointHandler.class );
    PowerMockito.when( ExtensionPointHandler.class, "callExtensionPoint", any(), any(), any() ).thenThrow( KettleException.class );

    delegate.updateLoadedJobs( "key", config );

    verify( log, times( 1 ) ).logBasic( any() );
  }
}
