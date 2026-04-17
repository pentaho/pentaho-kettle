/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.exception.RepositoryExceptionUtils;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
  * @author Luis Martins (16-Feb-2018)
  */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
@Ignore
public class RunConfigurationDelegateTest {

  private Spoon spoon;
  private RunConfigurationService service;
  private RunConfigurationDelegate delegate;
  private MockedStatic<Spoon> mockedSpoon;

  @Before
  public void setup() throws KettleException {
    spoon = mock( Spoon.class );
    doReturn( mock( Shell.class ) ).when( spoon ).getShell();

    mockedSpoon = mockStatic( Spoon.class );
    when( Spoon.getInstance() ).thenReturn( spoon );

    delegate = spy( RunConfigurationDelegate.getInstance( DefaultBowl.getInstance() ) );
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

  @Test
  public void testEdit_OperationSucceeds_NoRetry() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    doNothing().when( delegate ).updateLoadedJobs( "Test", config );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( config ) );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      delegate.edit( config );

      // Operation succeeded — no ErrorDialog should have been created
      assertEquals( 0, mockedError.constructed().size() );
      verify( service, times( 1 ) ).save( config );
    }
  }

  @Test
  public void testCreate_NonSessionError_ShowsErrorDialog() {
    RuntimeException nonSessionError = new RuntimeException( "Some generic error" );
    doThrow( nonSessionError ).when( service ).getNames();

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( nonSessionError ) ).thenReturn( false );

      delegate.create();

      // ErrorDialog should have been shown
      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void testCreate_SessionExpired_ReconnectSucceeds_RetrySucceeds() {
    RuntimeException sessionError = new RuntimeException( "Session expired" );

    // First call throws, second call succeeds
    List<String> emptyList = new ArrayList<>();
    when( service.getNames() )
      .thenThrow( sessionError )
      .thenReturn( emptyList );

    when( spoon.handleSessionExpiryWithRelogin() ).thenReturn( true );

    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( config ) );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );

      delegate.create();

      // Reconnect should have been called
      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();

      // Retry succeeded — config should have been saved
      verify( service, times( 1 ) ).save( config );

      // No error dialog
      assertEquals( 0, mockedError.constructed().size() );
    }
  }

  @Test
  public void testCreate_SessionExpired_ReconnectSucceeds_RetryFails() {
    RuntimeException sessionError = new RuntimeException( "Session expired" );
    RuntimeException retryError = new RuntimeException( "Retry also failed" );

    when( service.getNames() )
      .thenThrow( sessionError )
      .thenThrow( retryError );

    when( spoon.handleSessionExpiryWithRelogin() ).thenReturn( true );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );

      delegate.create();

      // Reconnect was attempted
      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();

      // Retry failed — ErrorDialog should have been shown
      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void testCreate_SessionExpired_ReconnectFails_NoRetry() {
    RuntimeException sessionError = new RuntimeException( "Session expired" );

    when( service.getNames() ).thenThrow( sessionError );
    when( spoon.handleSessionExpiryWithRelogin() ).thenReturn( false );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );

      delegate.create();

      // Reconnect was attempted but returned false
      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();

      // getNames() should only have been called once (no retry)
      verify( service, times( 1 ) ).getNames();

      // No error dialog from executeWithSessionRetry when reconnection returns false
      // (handleSessionExpiryWithRelogin returns false, no retry occurs)
    }
  }

  @Test
  public void testDelete_SessionExpired_HandleSessionExpiryThrows() {
    RuntimeException sessionError = new RuntimeException( "Session expired" );
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    try ( MockedConstruction<RunConfigurationDeleteDialog> mockedDelDialog = mockConstruction(
            RunConfigurationDeleteDialog.class,
            ( mock, context ) -> when( mock.open() ).thenThrow( sessionError ) );
          MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );
      when( spoon.handleSessionExpiryWithRelogin() ).thenThrow( new RuntimeException( "Spoon unavailable" ) );

      delegate.delete( config );

      // executeWithSessionRetry caught the exception — should have shown an ErrorDialog
      // (one from the relogin catch block, no retry occurs)
      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();
    }
  }

  @Test
  public void testEdit_SessionExpired_ReconnectsAndRetries() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RuntimeException sessionError = new RuntimeException( "Session expired" );

    // First call to getNames (inside editInternal) throws, second succeeds
    when( service.getNames() )
      .thenThrow( sessionError )
      .thenReturn( new ArrayList<>() );

    when( spoon.handleSessionExpiryWithRelogin() ).thenReturn( true );
    doNothing().when( delegate ).updateLoadedJobs( anyString(), any() );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( config ) );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );

      delegate.edit( config );

      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();
      // On successful retry, save should be called
      verify( service, times( 1 ) ).save( config );
      assertEquals( 0, mockedError.constructed().size() );
    }
  }

  @Test
  public void testDelete_SessionExpired_ReconnectsAndRetries() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );

    RuntimeException sessionError = new RuntimeException( "Session expired" );

    // Use a counter to make delete dialog throw first time, succeed second time
    final int[] callCount = { 0 };

    when( spoon.handleSessionExpiryWithRelogin() ).thenReturn( true );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<RunConfigurationDeleteDialog> mockedDelDialog = mockConstruction(
            RunConfigurationDeleteDialog.class,
            ( mock, context ) -> {
              callCount[0]++;
              if ( callCount[0] == 1 ) {
                when( mock.open() ).thenThrow( sessionError );
              } else {
                when( mock.open() ).thenReturn( SWT.YES );
              }
            } );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );

      delegate.delete( config );

      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();
      // On successful retry, delete should be called
      verify( service, times( 1 ) ).delete( "TestConfig" );
      assertEquals( 0, mockedError.constructed().size() );
    }
  }

  @Test
  public void loadAndEdit_LoadsConfigByNameAndEdits() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "MyRunConfig" );
    when( service.load( "MyRunConfig" ) ).thenReturn( config );
    doNothing().when( delegate ).updateLoadedJobs( anyString(), any() );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( config ) ) ) {
      delegate.loadAndEdit( "MyRunConfig" );

      verify( service, times( 1 ) ).load( "MyRunConfig" );
      verify( service, times( 1 ) ).delete( "MyRunConfig" );
      verify( service, times( 1 ) ).save( config );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoadAndEdit_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndEdit( "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void testLoadAndDelete_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndDelete( "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void testLoadAndDuplicate_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndDuplicate( "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  // ================================================================
  // executeWithSessionRetry wraps duplicate()
  // ================================================================

  @Test
  public void testDuplicate_NonSessionError_ShowsError() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RuntimeException error = new RuntimeException( "getNames failed" );
    when( service.getNames() ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {

      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.duplicate( config );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void testLoadByName() {
    delegate.load( "TestConfig" );
    verify( service, times( 1 ) ).load( "TestConfig" );
  }

  @Test
  public void testLoadAndEdit_Success() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );
    when( service.load( "TestConfig" ) ).thenReturn( config );
    doNothing().when( delegate ).updateLoadedJobs( anyString(), any() );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( config ) ) ) {
      delegate.loadAndEdit( "TestConfig" );

      verify( service, times( 1 ) ).save( config );
    }
  }

  @Test
  public void testLoadAndDelete_Success() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );
    when( service.load( "TestConfig" ) ).thenReturn( config );

    try ( MockedConstruction<RunConfigurationDeleteDialog> mockedDelDialog = mockConstruction(
            RunConfigurationDeleteDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( SWT.YES ) ) ) {
      delegate.loadAndDelete( "TestConfig" );

      verify( service, times( 1 ) ).delete( "TestConfig" );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoadAndDuplicate_Success() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );

    DefaultRunConfiguration duplicatedConfig = new DefaultRunConfiguration();
    duplicatedConfig.setName( "TestConfig 2" );

    List<String> names = new ArrayList<>();
    names.add( "TestConfig" );
    when( service.load( "TestConfig" ) ).thenReturn( config );
    when( service.getNames() ).thenReturn( names );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( duplicatedConfig ) ) ) {
      delegate.loadAndDuplicate( "TestConfig" );

      verify( service, times( 1 ) ).save( duplicatedConfig );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoadAndCopyToGlobal_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {
      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndCopyToGlobal( mock( RunConfigurationManager.class ), "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void loadAndCopyToProject_LoadsConfigAndCopiesToProjectTarget() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );

    when( service.load( "TestConfig" ) ).thenReturn( config );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl projectBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getManagementBowl() ).thenReturn( projectBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.loadAndCopyToProject( srcManager, "TestConfig" );

      verify( service, times( 1 ) ).load( "TestConfig" );
      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, never() ).delete( any() );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoadAndCopyToProject_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {
      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndCopyToProject( mock( RunConfigurationManager.class ), "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void loadAndMoveToGlobal_LoadsConfigAndMovesToGlobalTarget() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );

    when( service.load( "TestConfig" ) ).thenReturn( config );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.loadAndMoveToGlobal( srcManager, "TestConfig" );

      verify( service, times( 1 ) ).load( "TestConfig" );
      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, times( 1 ) ).delete( "TestConfig" );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoadAndMoveToGlobal_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {
      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndMoveToGlobal( mock( RunConfigurationManager.class ), "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void loadAndMoveToProject_LoadsConfigAndMovesToProjectTarget() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );

    when( service.load( "TestConfig" ) ).thenReturn( config );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl projectBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getManagementBowl() ).thenReturn( projectBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.loadAndMoveToProject( srcManager, "TestConfig" );

      verify( service, times( 1 ) ).load( "TestConfig" );
      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, times( 1 ) ).delete( "TestConfig" );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testLoadAndMoveToProject_NonSessionError_ShowsError() {
    RuntimeException error = new RuntimeException( "Load failed" );
    when( service.load( "myConfig" ) ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {
      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.loadAndMoveToProject( mock( RunConfigurationManager.class ), "myConfig" );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void testDuplicate_Success_NoNameConflict() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    DefaultRunConfiguration duplicatedConfig = new DefaultRunConfiguration();
    duplicatedConfig.setName( "Test copy" );

    List<String> names = new ArrayList<>();
    names.add( "Test" );
    when( service.getNames() ).thenReturn( names );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( duplicatedConfig ) ) ) {
      delegate.duplicate( config );

      verify( service, times( 1 ) ).save( duplicatedConfig );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testDuplicate_UserCancels_NoSave() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    List<String> names = new ArrayList<>();
    names.add( "Test" );
    when( service.getNames() ).thenReturn( names );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( null ) ) ) {
      delegate.duplicate( config );

      verify( service, never() ).save( any() );
      verify( spoon, never() ).refreshTree( anyString() );
    }
  }

  @Test
  public void testDuplicate_NameConflict_ShowsExistsDialog() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    // Dialog returns a name that was already in existingNames
    DefaultRunConfiguration conflictConfig = new DefaultRunConfiguration();
    conflictConfig.setName( "Test" );

    List<String> names = new ArrayList<>();
    names.add( "Test" );
    when( service.getNames() ).thenReturn( names );

    try ( MockedConstruction<RunConfigurationDialog> mockedDialog = mockConstruction( RunConfigurationDialog.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( conflictConfig ) );
          MockedConstruction<MessageDialog> mockedMsgDialog = mockConstruction( MessageDialog.class ) ) {
      delegate.duplicate( config );

      assertEquals( 1, mockedMsgDialog.constructed().size() );
      verify( service, never() ).save( any() );
    }
  }

  @Test
  public void testShouldOverwrite_UserSelectsYes_ReturnsTrue() {
    try ( MockedConstruction<MessageBox> mockedMB = mockConstruction( MessageBox.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( SWT.YES ) ) ) {
      boolean result = delegate.shouldOverwrite( "Overwrite?" );

      assertTrue( result );
    }
  }

  @Test
  public void testShouldOverwrite_UserSelectsNo_ReturnsFalse() {
    try ( MockedConstruction<MessageBox> mockedMB = mockConstruction( MessageBox.class,
            ( mock, context ) -> when( mock.open() ).thenReturn( SWT.NO ) ) ) {
      boolean result = delegate.shouldOverwrite( "Overwrite?" );

      assertFalse( result );
    }
  }

  @Test
  public void testCopyToGlobal_NoConflict_SavesOnly() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.copyToGlobal( srcManager, config );

      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, never() ).delete( any() );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testMoveToGlobal_NoConflict_SavesAndDeletesSource() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.moveToGlobal( srcManager, config );

      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, times( 1 ) ).delete( "Test" );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testCopyToGlobal_NameConflict_UserOverwrites_DeletesAndSaves() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    List<String> existingNames = new ArrayList<>();
    existingNames.add( "Test" );
    when( targetManager.getNames() ).thenReturn( existingNames );
    doReturn( true ).when( delegate ).shouldOverwrite( any() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.copyToGlobal( srcManager, config );

      verify( targetManager, times( 1 ) ).delete( "Test" );
      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, never() ).delete( any() );
    }
  }

  @Test
  public void testCopyToGlobal_NameConflict_UserDeclines_DoesNotSave() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    List<String> existingNames = new ArrayList<>();
    existingNames.add( "Test" );
    when( targetManager.getNames() ).thenReturn( existingNames );
    doReturn( false ).when( delegate ).shouldOverwrite( any() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.copyToGlobal( srcManager, config );

      verify( targetManager, never() ).save( any() );
      verify( srcManager, never() ).delete( any() );
      verify( spoon, never() ).refreshTree( anyString() );
    }
  }

  @Test
  public void testCopyToProject_NoConflict_SavesOnly() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl projectBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getManagementBowl() ).thenReturn( projectBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.copyToProject( srcManager, config );

      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, never() ).delete( any() );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testMoveToProject_NoConflict_SavesAndDeletesSource() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl projectBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getManagementBowl() ).thenReturn( projectBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.moveToProject( srcManager, config );

      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, times( 1 ) ).delete( "Test" );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testCopyToGlobal_NonSessionError_ShowsError() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    RuntimeException error = new RuntimeException( "Unexpected error" );
    when( spoon.getGlobalManagementBowl() ).thenThrow( error );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {
      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( error ) ).thenReturn( false );

      delegate.copyToGlobal( srcManager, config );

      assertEquals( 1, mockedError.constructed().size() );
    }
  }

  @Test
  public void loadAndCopyToGlobal_LoadsConfigAndCopiesToGlobalTarget() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "TestConfig" );

    when( service.load( "TestConfig" ) ).thenReturn( config );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );

    try ( MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class ) ) {
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.loadAndCopyToGlobal( srcManager, "TestConfig" );

      verify( service, times( 1 ) ).load( "TestConfig" );
      verify( targetManager, times( 1 ) ).save( config );
      verify( srcManager, never() ).delete( any() );
      verify( spoon, times( 1 ) ).refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
    }
  }

  @Test
  public void testCopyToGlobal_SessionExpired_ReconnectsAndRetries() {
    DefaultRunConfiguration config = new DefaultRunConfiguration();
    config.setName( "Test" );

    RunConfigurationManager srcManager = mock( RunConfigurationManager.class );
    Bowl globalBowl = mock( Bowl.class );
    RunConfigurationManager targetManager = mock( RunConfigurationManager.class );

    RuntimeException sessionError = new RuntimeException( "Session expired" );
    when( spoon.getGlobalManagementBowl() )
      .thenThrow( sessionError )
      .thenReturn( globalBowl );
    when( targetManager.getNames() ).thenReturn( new ArrayList<>() );
    when( spoon.handleSessionExpiryWithRelogin() ).thenReturn( true );

    try ( MockedStatic<RepositoryExceptionUtils> repoUtils = mockStatic( RepositoryExceptionUtils.class );
          MockedStatic<RunConfigurationManager> mockedRCM = mockStatic( RunConfigurationManager.class );
          MockedConstruction<ErrorDialog> mockedError = mockConstruction( ErrorDialog.class ) ) {
      repoUtils.when( () -> RepositoryExceptionUtils.isSessionExpired( sessionError ) ).thenReturn( true );
      mockedRCM.when( () -> RunConfigurationManager.getInstance( any( Bowl.class ) ) ).thenReturn( targetManager );

      delegate.copyToGlobal( srcManager, config );

      verify( spoon, times( 1 ) ).handleSessionExpiryWithRelogin();
      verify( targetManager, times( 1 ) ).save( config );
      assertEquals( 0, mockedError.constructed().size() );
    }
  }

}
