/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entry;

import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;

import org.eclipse.swt.custom.CCombo;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryDialog_ConnectionLine_Test {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String INITIAL_NAME = "qwerty";
  private static final String INPUT_NAME = "asdfg";

  private static final String INITIAL_HOST = "1.2.3.4";
  private static final String INPUT_HOST = "5.6.7.8";

  private static JobEntryDialog mockDialog;
  private static Spoon mockSpoon;
  private static DatabaseManagementInterface dbMgr;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
    DefaultBowl.getInstance().setSharedObjectsIO( new MemorySharedObjectsIO() );
    DefaultBowl.getInstance().clearManagers();
    Supplier<Spoon> mockSupplier;

    mockSupplier = mock( Supplier.class );
    mockSpoon = mock( Spoon.class );
    mockDialog = mock( JobEntryDialog.class );

    Whitebox.setInternalState( mockDialog, "spoonSupplier", mockSupplier );
    when( mockSupplier.get() ).thenReturn( mockSpoon );
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getBowl();
    doReturn( DefaultBowl.getInstance() ).when( mockSpoon ).getGlobalManagementBowl();

    dbMgr = DefaultBowl.getInstance().getManager( DatabaseManagementInterface.class );
  }

  @After
  public void perTestTeardown() throws KettleException {
    clearInvocations( mockSpoon );
    clearInvocations( mockDialog );
    for ( DatabaseMeta db : dbMgr.getAll() ) {
      dbMgr.remove( db );
    }
  }

  @Test
  public void adds_WhenConnectionNameIsUnique() throws Exception {
    JobMeta jobMeta = new JobMeta();

    invokeAddConnectionListener( jobMeta, INPUT_NAME );

    assertOnlyOneActiveDb( jobMeta, INPUT_NAME, INPUT_HOST );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 0 );
  }

  @Test
  public void adds_WhenGlobalConnectionNameOverridesLocal() throws Exception {
    JobMeta jobMeta = new JobMeta();

    jobMeta.addDatabase( createDefaultDatabase() ); //local
    assertOnlyOneActiveDb( jobMeta, INITIAL_NAME, INITIAL_HOST );
    assertTotalDbs( jobMeta, 1 );

    invokeAddConnectionListener( jobMeta, INITIAL_NAME ); //global
    assertOnlyOneActiveDb( jobMeta, INITIAL_NAME, INPUT_HOST );
    assertTotalDbs( jobMeta, 2 );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 1 );
  }

  @Test
  public void ignoresAdd_WhenConnectionNameIsNull() throws Exception {
    JobMeta jobMeta = new JobMeta();
    dbMgr.add( createDefaultDatabase() );

    invokeAddConnectionListener( jobMeta, null );

    assertOnlyOneActiveDb( jobMeta, INITIAL_NAME, INITIAL_HOST );
    assertTotalDbs( jobMeta, 1 );
  }

  @Test
  public void edits_globalConnectionWhenNotRenamed() throws Exception {
    JobMeta jobMeta = new JobMeta();
    DatabaseMeta db = createDefaultDatabase();

    jobMeta.addDatabase( db );
    dbMgr.add( db );
    assertTotalDbs( jobMeta, 2 );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 1 );

    invokeEditConnectionListener( jobMeta, INITIAL_NAME );

    DatabaseMeta localDb = jobMeta.getDatabaseManagementInterface().get( INITIAL_NAME );
    DatabaseMeta globalDb =
      jobMeta.getDatabases().stream().filter( databaseMeta -> databaseMeta.getName().equals( INITIAL_NAME ) )
        .findFirst().get();

    assertEquals( INITIAL_HOST, localDb.getHostname() );
    assertEquals( INPUT_HOST, globalDb.getHostname() );
    assertTotalDbs( jobMeta, 2 );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 1 );
  }

  @Test
  public void edits_localConnectionWhenNotRenamed() throws Exception {
    JobMeta jobMeta = new JobMeta();
    jobMeta.addDatabase( createDefaultDatabase() );

    invokeEditConnectionListener( jobMeta, INITIAL_NAME );

    assertOnlyOneActiveDb( jobMeta, INITIAL_NAME, INPUT_HOST );
    assertTotalDbs( jobMeta, 1 );
    assertNumberOfGlobalDBs( 0 );
    assertNumberOfLocalDBs( jobMeta, 1 );
  }

  @Test
  public void edits_WhenNewNameIsUnique() throws Exception {
    JobMeta jobMeta = new JobMeta();
    dbMgr.add( createDefaultDatabase() );

    invokeEditConnectionListener( jobMeta, INPUT_NAME );

    assertOnlyOneActiveDb( jobMeta, INPUT_NAME, INPUT_HOST );
    assertTotalDbs( jobMeta, 1 );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 0 );
  }

  @Test
  public void ignores_EditToLocalConnectionWhenNewNameIsNull() throws Exception {
    JobMeta jobMeta = new JobMeta();
    jobMeta.addDatabase( createDefaultDatabase() );

    invokeEditConnectionListener( jobMeta, null );

    assertOnlyOneActiveDb( jobMeta, INITIAL_NAME, INITIAL_HOST );
    assertTotalDbs( jobMeta, 1 );
    assertNumberOfGlobalDBs( 0 );
    assertNumberOfLocalDBs( jobMeta, 1 );
  }

  @Test
  public void ignores_EditToGlobalConnectionWhenNewNameIsNull() throws Exception {
    JobMeta jobMeta = new JobMeta();
    dbMgr.add( createDefaultDatabase() );

    invokeEditConnectionListener( jobMeta, null );

    assertOnlyOneActiveDb( jobMeta, INITIAL_NAME, INITIAL_HOST );
    assertTotalDbs( jobMeta, 1 );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 0 );
  }

  @Test
  public void edit_showDbDialog_ReturnsNull_OnCancel_GlobalDb() throws Exception {
    // null as input emulates cancelling
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( null, null, "global" );
  }

  @Test
  public void edit_showDbDialog_ReturnsInputName_WhenItIsUnique_GlobalDb() throws Exception {
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( INPUT_NAME, INPUT_NAME, "global" );
  }

  @Test
  public void edit_showDbDialog_ReturnsInputName_WhenItIsUnique_WithSpaces_GlobalDb() throws Exception {
    String input = " " + INPUT_NAME + " ";
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( input, INPUT_NAME, "global" );
  }

  @Test
  public void edit_showDbDialog_ReturnsExistingName_WhenNameWasNotChanged_GlobalDb() throws Exception {
    // this is the case of editing when name was not changed (e.g., host was updated)
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( INITIAL_NAME, INITIAL_NAME, "global" );
  }

  @Test
  public void edit_showDbDialog_ReturnsNull_OnCancel_LocalDb() throws Exception {
    // null as input emulates cancelling
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( null, null, "local" );
  }

  @Test
  public void edit_showDbDialog_ReturnsInputName_WhenItIsUnique_LocalDb() throws Exception {
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( INPUT_NAME, INPUT_NAME, "local" );
  }

  @Test
  public void edit_showDbDialog_ReturnsInputName_WhenItIsUnique_WithSpaces_LocalDb() throws Exception {
    String input = " " + INPUT_NAME + " ";
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( input, INPUT_NAME, "local" );
  }

  @Test
  public void edit_showDbDialog_ReturnsExistingName_WhenNameWasNotChanged_LocalDb() throws Exception {
    // this is the case of editing when name was not changed (e.g., host was updated)
    edit_showDbDialogUnlessCancelledOrValid_ShownOnce( INITIAL_NAME, INITIAL_NAME, "local" );
  }

  @Test
  public void edit_showDbDialog_LoopsUntilUniqueValueIsInput_LocalDbs() throws Exception {
    edit_showDbDialog_LoopsUntilUniqueValueIsInput( "local" );
  }

  @Test
  public void edit_showDbDialog_LoopsUntilUniqueValueIsInput_GlobalDbs() throws Exception {
    edit_showDbDialog_LoopsUntilUniqueValueIsInput( "global" );
  }

  @Test
  public void edit_doNotShowDbExistsErrorDialogForDbsWithSharedNamesAtDifferentLevels() throws Exception {
    DatabaseMeta globalDb = createDefaultDatabase();

    DatabaseMeta localDb = createDefaultDatabase();
    localDb.setName( INPUT_NAME );

    JobMeta jobMeta = new JobMeta();
    dbMgr.add( globalDb );
    jobMeta.addDatabase( localDb );
    assertNumberOfGlobalDBs( 1 );
    assertNumberOfLocalDBs( jobMeta, 1 );

    DatabaseDialog databaseDialog = mock( DatabaseDialog.class );
    when( databaseDialog.open() ).thenReturn( INPUT_NAME );

    mockDialog.databaseDialog = databaseDialog;
    mockDialog.jobMeta = jobMeta;
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenCallRealMethod();

    // renaming global to have same name as local
    String result = mockDialog.showDbDialogUnlessCancelledOrValid( (DatabaseMeta) globalDb.clone(), globalDb, dbMgr );
    assertEquals( result, INPUT_NAME );

    verify( mockDialog, times( 0 ) ).showDbExistsDialog( anyDbMeta() );
  }

  @Test
  public void edit_shouldNotShowDbExistsErrorDialogWhenRenamingLocalConnectionWithDifferentCase() throws Exception {
    edit_shouldNotShowDbExistsErrorDialogWhenRenaming( "local", INITIAL_NAME.toUpperCase() );
  }

  @Test
  public void edit_shouldNotShowDbExistsErrorDialogWhenRenamingGlobalConnectionWithDifferentCase() throws Exception {
    edit_shouldNotShowDbExistsErrorDialogWhenRenaming( "global", INITIAL_NAME.toUpperCase() );
  }

  @Test
  public void edit_shouldNotShowDbExistsErrorDialogWhenRenamingLocalConnectionWithSpaces() throws Exception {
    edit_shouldNotShowDbExistsErrorDialogWhenRenaming( "local", INITIAL_NAME + " " );
  }

  @Test
  public void edit_shouldNotShowDbExistsErrorDialogWhenRenamingGlobalConnectionWithSpaces() throws Exception {
    edit_shouldNotShowDbExistsErrorDialogWhenRenaming( "global", INITIAL_NAME + " " );
  }

  @Test
  public void add_showDbDialog_LoopsUntilUniqueValueIsInput() throws Exception {
    DatabaseMeta db1 = createDefaultDatabase();
    DatabaseMeta db2 = new DatabaseMeta();
    db2.setName( INPUT_NAME );
    db2.setHostname( INITIAL_HOST );
    DatabaseMeta db3 = new DatabaseMeta();
    db3.setName( "QwErTy" );
    db3.setHostname( INITIAL_HOST );

    JobMeta jobMeta = new JobMeta();
    dbMgr.add( db1 );
    dbMgr.add( db2 );
    dbMgr.add( db3 );

    final String expectedResult = INITIAL_NAME + "2";

    DatabaseDialog databaseDialog = mock( DatabaseDialog.class );
    when( databaseDialog.open() )
      .thenReturn( INITIAL_NAME + " " )
      .thenReturn( INITIAL_NAME.toUpperCase() )
      .thenReturn( INPUT_NAME )
      .thenReturn( INPUT_NAME.toUpperCase() )
      .thenReturn( INPUT_NAME + " " )
      // unique value
      .thenReturn( expectedResult );

    mockDialog.databaseDialog = databaseDialog;
    mockDialog.jobMeta = jobMeta;
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), any(), anyDbMgr() ) ).thenCallRealMethod();

    // try to rename db1 (named "qwerty")
    String result = mockDialog.showDbDialogUnlessCancelledOrValid( (DatabaseMeta) db1.clone(), null, dbMgr );
    assertEquals( expectedResult, result );

    // error message should be shown once for each incorrect input
    verify( mockDialog, times( 5 ) ).showDbExistsDialog( anyDbMeta() );
    verify( databaseDialog, times( 6 ) ).open();
  }

  private void edit_shouldNotShowDbExistsErrorDialogWhenRenaming( String level, String newName )
    throws Exception {
    DatabaseMeta db = createDefaultDatabase();

    JobMeta jobMeta = new JobMeta();
    DatabaseManagementInterface testDbMgr = null;
    if ( level.equals( "global" ) ) {
      testDbMgr = dbMgr;
    } else if ( level.equals( "local" ) ) {
      testDbMgr = jobMeta.getDatabaseManagementInterface();
    }
    testDbMgr.add( db );

    DatabaseDialog databaseDialog = mock( DatabaseDialog.class );
    when( databaseDialog.open() )
      .thenReturn( newName )
      // The following thenReturn allows the test to fail gracefully, rather than getting stuck indefinitely because
      // the error dialog appeared when it wasn't expected
      .thenReturn( "shouldNotBeReachable" );

    mockDialog.databaseDialog = databaseDialog;
    mockDialog.jobMeta = jobMeta;
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenCallRealMethod();

    String result = mockDialog.showDbDialogUnlessCancelledOrValid( (DatabaseMeta) db.clone(), db, testDbMgr );
    assertEquals( result, newName.trim() );

    verify( mockDialog, times( 0 ) ).showDbExistsDialog( anyDbMeta() );
  }

  private void edit_showDbDialog_LoopsUntilUniqueValueIsInput( String level ) throws Exception {
    DatabaseMeta db1 = createDefaultDatabase();
    DatabaseMeta db2 = new DatabaseMeta();
    db2.setName( INPUT_NAME );
    db2.setHostname( INITIAL_HOST );

    JobMeta jobMeta = new JobMeta();
    DatabaseManagementInterface testDbMgr = null;
    if ( level.equals( "global" ) ) {
      testDbMgr = dbMgr;
    } else if ( level.equals( "local" ) ) {
      testDbMgr = jobMeta.getDatabaseManagementInterface();
    }
    testDbMgr.add( db1 );
    testDbMgr.add( db2 );

    final String expectedResult = INPUT_NAME + "2";

    DatabaseDialog databaseDialog = mock( DatabaseDialog.class );
    when( databaseDialog.open() )
      // duplicate
      .thenReturn( INPUT_NAME )
      // duplicate with spaces
      .thenReturn( INPUT_NAME + " " )
      // duplicate in other case
      .thenReturn( INPUT_NAME.toUpperCase() )
      // duplicate with other case and spaces
      .thenReturn( INPUT_NAME.toUpperCase() + " " )
      // unique value
      .thenReturn( expectedResult );

    mockDialog.databaseDialog = databaseDialog;
    mockDialog.jobMeta = jobMeta;
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenCallRealMethod();

    // try to rename db1 (named "qwerty")
    String result = mockDialog.showDbDialogUnlessCancelledOrValid( (DatabaseMeta) db1.clone(), db1, testDbMgr );
    assertEquals( expectedResult, result );

    // error message should be shown once for each incorrect input
    verify( mockDialog, times( 4 ) ).showDbExistsDialog( anyDbMeta() );
    verify( databaseDialog, times( 5 ) ).open();
  }

  private void edit_showDbDialogUnlessCancelledOrValid_ShownOnce( String inputName,
                                                                  String expectedResult, String level )
    throws Exception {
    JobMeta jobMeta = new JobMeta();
    DatabaseManagementInterface testDbMgr = null;
    if ( level.equals( "global" ) ) {
      testDbMgr = dbMgr;
    } else if ( level.equals( "local" ) ) {
      testDbMgr = jobMeta.getDatabaseManagementInterface();
    }

    DatabaseDialog databaseDialog = mock( DatabaseDialog.class );
    when( databaseDialog.open() ).thenReturn( inputName );
    when( databaseDialog.getDatabaseMeta() ).thenReturn( createDefaultDatabase() );

    DatabaseMeta db = createDefaultDatabase();
    testDbMgr.add( db );

    mockDialog.databaseDialog = databaseDialog;
    mockDialog.jobMeta = jobMeta;
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenCallRealMethod();

    String result = mockDialog.showDbDialogUnlessCancelledOrValid( (DatabaseMeta) db.clone(), db, testDbMgr );
    assertEquals( expectedResult, result );

    // database dialog should be shown only once
    verify( databaseDialog, times( 1 ) ).open();
  }

  private void assertTotalDbs( JobMeta jobMeta, int expected ) throws KettleException {
    assertEquals( expected,
      jobMeta.getDatabaseManagementInterface().getAll().size() + dbMgr.getAll().size() );
  }

  private void assertNumberOfGlobalDBs( int expected ) throws KettleException {
    assertEquals( expected, dbMgr.getAll().size() );
  }

  private void assertNumberOfLocalDBs( JobMeta jobMeta, int expected ) throws KettleException {
    assertEquals( expected, jobMeta.getDatabaseManagementInterface().getAll().size() );
  }

  private void invokeAddConnectionListener( JobMeta jobMeta, String answeredName ) {
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), any(), anyDbMgr() ) )
      .thenAnswer( new PropsSettingAnswer( answeredName, INPUT_HOST ) );

    mockDialog.jobMeta = jobMeta;
    mockDialog.new AddConnectionListener( mock( CCombo.class ) ).widgetSelected( null );
    if ( answeredName != null ) {
      verify( mockSpoon, times( 1 ) ).refreshTree( anyString() );
    }
  }

  private void invokeEditConnectionListener( JobMeta jobMeta, String answeredName ) {
    when( mockDialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) )
      .thenAnswer( new PropsSettingAnswer( answeredName, INPUT_HOST ) );

    CCombo combo = mock( CCombo.class );
    when( combo.getText() ).thenReturn( INITIAL_NAME );

    mockDialog.jobMeta = jobMeta;
    mockDialog.new EditConnectionListener( combo ).widgetSelected( null );
  }

  private DatabaseMeta createDefaultDatabase() {
    DatabaseMeta existing = new DatabaseMeta();
    existing.setName( INITIAL_NAME );
    existing.setHostname( INITIAL_HOST );
    return existing;
  }

  private void assertOnlyOneActiveDb( JobMeta jobMeta, String name, String host ) {
    assertEquals( 1, jobMeta.getDatabases().size() );
    assertEquals( name, jobMeta.getDatabase( 0 ).getName() );
    assertEquals( host, jobMeta.getDatabase( 0 ).getHostname() );
  }

  private static DatabaseMeta anyDbMeta() {
    return any( DatabaseMeta.class );
  }

  private static DatabaseManagementInterface anyDbMgr() {
    return any( DatabaseManagementInterface.class );
  }

  private static class PropsSettingAnswer implements Answer<String> {
    private final String name;
    private final String host;

    public PropsSettingAnswer( String name, String host ) {
      this.name = name;
      this.host = host;
    }

    @Override
    public String answer( InvocationOnMock invocation ) throws Throwable {
      DatabaseMeta meta = (DatabaseMeta) invocation.getArguments()[ 0 ];
      meta.setName( name );
      meta.setHostname( host );
      return name;
    }
  }
}
