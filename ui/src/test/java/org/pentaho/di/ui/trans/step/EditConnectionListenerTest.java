/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.ui.trans.step;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog.EditConnectionListener;

import java.util.function.Supplier;


import org.eclipse.swt.custom.CCombo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditConnectionListenerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static String TEST_NAME = "TEST_NAME";

  private static String TEST_HOST = "TEST_HOST";

  private BaseStepDialog dialog;

  private EditConnectionListener editConnectionListener;

  private DatabaseManagementInterface managementDbMgr;

  private Spoon spoon;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void init() throws Exception {
    dialog = mock( BaseStepDialog.class );
    when( dialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenAnswer(
      new PropsSettingAnswer(
        TEST_NAME, TEST_HOST ) );
    dialog.transMeta = spy( new TransMeta() );

    Supplier<Spoon> spoonSupplier = mock( Supplier.class );
    spoon = mock( Spoon.class );
    Bowl managementBowl = mock( Bowl.class );
    Bowl globalBowl = mock( Bowl.class );
    managementDbMgr = mock( DatabaseManagementInterface.class );
    DatabaseManagementInterface globalDbMgr = mock( DatabaseManagementInterface.class );

    Whitebox.setInternalState( dialog, "spoonSupplier", spoonSupplier );
    when( spoonSupplier.get() ).thenReturn( spoon );
    when( spoon.getManagementBowl() ).thenReturn( managementBowl );
    when( spoon.getGlobalManagementBowl() ).thenReturn( globalBowl );
    when( managementBowl.getManager( DatabaseManagementInterface.class ) ).thenReturn( managementDbMgr );
    when( globalBowl.getManager( DatabaseManagementInterface.class ) ).thenReturn( globalDbMgr );

    CCombo combo = mock( CCombo.class );
    when( combo.getText() ).thenReturn( TEST_NAME );

    editConnectionListener = spy( dialog.new EditConnectionListener( combo ) );
    doNothing().when( editConnectionListener ).showErrorDialog( any( Exception.class ) );
  }

  @Test
  public void widgetSelected_refreshesEditedConnectionAndRemovesWhenRenamed() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setName( TEST_NAME );
    ObjectId objectId = mock( ObjectId.class );
    databaseMeta.setObjectId( objectId );
    dialog.transMeta.addDatabase( databaseMeta );

    when( managementDbMgr.get( TEST_NAME ) ).thenReturn( databaseMeta );
    when( dialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenAnswer( invocation -> {
      DatabaseMeta clonedMeta = (DatabaseMeta) invocation.getArguments()[ 0 ];
      clonedMeta.setName( "RENAMED" );
      return "RENAMED";
    } );

    editConnectionListener.widgetSelected( null );

    verify( managementDbMgr, times( 1 ) ).remove( databaseMeta );
    ArgumentCaptor<DatabaseMeta> addedDatabaseCaptor = ArgumentCaptor.forClass( DatabaseMeta.class );
    verify( managementDbMgr, times( 1 ) ).add( addedDatabaseCaptor.capture() );
    assertNull( addedDatabaseCaptor.getValue().getObjectId() );
    verify( spoon, times( 1 ) ).refreshDbConnection( "RENAMED" );
  }

  @Test
  public void widgetSelected_refreshesEditedConnectionAndSkipsRemoveWhenRenameIsCaseOnly() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setName( TEST_NAME );
    ObjectId objectId = mock( ObjectId.class );
    databaseMeta.setObjectId( objectId );
    dialog.transMeta.addDatabase( databaseMeta );

    String editedConnectionName = TEST_NAME.toUpperCase();
    when( managementDbMgr.get( TEST_NAME ) ).thenReturn( databaseMeta );
    when( dialog.showDbDialogUnlessCancelledOrValid( anyDbMeta(), anyDbMeta(), anyDbMgr() ) ).thenAnswer( invocation -> {
      DatabaseMeta clonedMeta = (DatabaseMeta) invocation.getArguments()[ 0 ];
      clonedMeta.setName( editedConnectionName );
      return editedConnectionName;
    } );

    editConnectionListener.widgetSelected( null );

    verify( managementDbMgr, never() ).remove( databaseMeta );
    ArgumentCaptor<DatabaseMeta> addedDatabaseCaptor = ArgumentCaptor.forClass( DatabaseMeta.class );
    verify( managementDbMgr, times( 1 ) ).add( addedDatabaseCaptor.capture() );
    assertNotNull( addedDatabaseCaptor.getValue().getObjectId() );
    verify( spoon, times( 1 ) ).refreshDbConnection( editedConnectionName );
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
      DatabaseMeta meta = (DatabaseMeta) invocation.getArguments()[0];
      meta.setName( name );
      meta.setHostname( host );
      return name;
    }
  }

  private static DatabaseMeta anyDbMeta() {
    return any( DatabaseMeta.class );
  }

  private static DatabaseManagementInterface anyDbMgr() {
    return any( DatabaseManagementInterface.class );
  }


}
