/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
  * @author Luis Martins (16-Feb-2018)
  */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationDelegateTest {

  private Spoon spoon;
  private RunConfigurationService service;
  private RunConfigurationDelegate delegate;
  private MockedStatic<Spoon> mockedSpoon;

  @Before
  public void setup() {
    spoon = mock( Spoon.class );
    doReturn( mock( Shell.class ) ).when( spoon ).getShell();

    mockedSpoon = mockStatic( Spoon.class );
    when( Spoon.getInstance() ).thenReturn( spoon );

    delegate = spy( new RunConfigurationDelegate() );
    service = mock( RunConfigurationManager.class );
    delegate.setRunConfigurationManager( service );
  }

  @After
  public void teardown() {
    mockedSpoon.close();
  }

  @Test
  public void testCreate() throws Exception {
    List<String> list = new ArrayList<>();
    list.add( "Configuration 1" );

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    doReturn( list ).when( service ).getNames();

    try ( MockedConstruction<RunConfigurationDialog> mockedConfDialog = mockConstruction( RunConfigurationDialog.class,
      (mock, context) -> when( mock.open() ).thenReturn( config ) ) ) {
      delegate.create();

      verify( service, times( 1 ) ).save( config );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testDelete() throws Exception {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    try ( MockedConstruction<RunConfigurationDeleteDialog> mockedConfDialog = mockConstruction( RunConfigurationDeleteDialog.class,
      (mock, context) -> when( mock.open() ).thenReturn( SWT.YES ) ) ) {
      delegate.delete( config );

      verify( service, times( 1 ) ).delete( "Test" );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testEdit() throws Exception {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );
    config.setServer( "localhost" );

    doNothing().when( delegate ).updateLoadedJobs( "Test", config );

    try ( MockedConstruction<RunConfigurationDialog> mockedConfDialog = mockConstruction( RunConfigurationDialog.class,
      (mock, context) -> when( mock.open() ).thenReturn( config ) ) ) {
      delegate.edit( config );

      verify( delegate, times( 1 ) ).updateLoadedJobs( "Test", config );
      verify( service, times( 1 ) ).delete( "Test" );
      verify( service, times( 1 ) ).save( config );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoad() {
    delegate.load();
    verify( service, times( 1 ) ).load();
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

    try ( MockedStatic<ExtensionPointHandler> mockedHandler = mockStatic( ExtensionPointHandler.class ) ) {
      mockedHandler.when( () -> ExtensionPointHandler.callExtensionPoint( any(), any(), any() ) ).thenThrow( KettleException.class );
      delegate.updateLoadedJobs( "key", config );

      verify( log, times( 1 ) ).logBasic( any() );
    }
  }
}
