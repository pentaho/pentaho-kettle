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

package org.pentaho.di.ui.job.entries.columnsexist;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.columnsexist.JobEntryColumnsExistHelper;
import org.pentaho.di.ui.core.widget.TableView;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

@SuppressWarnings( "unchecked" )
public class JobEntryColumnsExistDialogTest {

  @Test
  public void testGetListColumns_EmptyTablename_DoesNothing() {
    JobEntryColumnsExistDialog dialog = mock( JobEntryColumnsExistDialog.class );
    doCallRealMethod().when( dialog ).getListColumns();

    TextVar wTablename = mock( TextVar.class );
    when( wTablename.getText() ).thenReturn( "" );
    setInternalState( dialog, "wTablename", wTablename );

    dialog.getListColumns();

    verify( dialog, never() ).createColumnsExistHelper();
  }

  @Test
  public void testGetListColumns_SuccessWithColumns() {
    JobEntryColumnsExistDialog dialog = mock( JobEntryColumnsExistDialog.class );
    doCallRealMethod().when( dialog ).getListColumns();
    doNothing().when( dialog ).showErrorDialog( anyString() );

    TextVar wTablename = mock( TextVar.class );
    when( wTablename.getText() ).thenReturn( "employees" );

    CCombo wConnection = mock( CCombo.class );
    when( wConnection.getText() ).thenReturn( "testConn" );

    TextVar wSchemaname = mock( TextVar.class );
    when( wSchemaname.getText() ).thenReturn( "testSchema" );

    TableView wFields = mock( TableView.class );

    JobMeta jobMeta = mock( JobMeta.class );

    JSONArray columns = new JSONArray();
    columns.add( "COL1" );
    columns.add( "COL2" );

    JSONObject successResponse = new JSONObject();
    successResponse.put( ACTION_STATUS, SUCCESS_RESPONSE );
    successResponse.put( "columns", columns );

    JobEntryColumnsExistHelper helper = mock( JobEntryColumnsExistHelper.class );
    when( helper.getTableColumns( any( JobMeta.class ), anyMap() ) )
      .thenReturn( successResponse );
    when( helper.isFailedResponse( successResponse ) ).thenReturn( false );
    when( dialog.createColumnsExistHelper() ).thenReturn( helper );

    setInternalState( dialog, "wTablename", wTablename );
    setInternalState( dialog, "wConnection", wConnection );
    setInternalState( dialog, "wSchemaname", wSchemaname );
    setInternalState( dialog, "wFields", wFields );
    setInternalState( dialog, "jobMeta", jobMeta );

    dialog.getListColumns();

    verify( wFields ).removeAll();
    verify( wFields ).add( "COL1" );
    verify( wFields ).add( "COL2" );
    verify( wFields ).removeEmptyRows();
    verify( wFields ).setRowNums();
    verify( dialog, never() ).showErrorDialog( anyString() );
  }

  @Test
  public void testGetListColumns_NullColumns_ShowsError() {
    JobEntryColumnsExistDialog dialog = mock( JobEntryColumnsExistDialog.class );
    doCallRealMethod().when( dialog ).getListColumns();
    doNothing().when( dialog ).showErrorDialog( anyString() );

    TextVar wTablename = mock( TextVar.class );
    when( wTablename.getText() ).thenReturn( "employees" );

    CCombo wConnection = mock( CCombo.class );
    when( wConnection.getText() ).thenReturn( "testConn" );

    TextVar wSchemaname = mock( TextVar.class );
    when( wSchemaname.getText() ).thenReturn( "" );

    TableView wFields = mock( TableView.class );
    JobMeta jobMeta = mock( JobMeta.class );

    JSONObject successResponse = new JSONObject();
    successResponse.put( ACTION_STATUS, SUCCESS_RESPONSE );
    // no "columns" key → null columns

    JobEntryColumnsExistHelper helper = mock( JobEntryColumnsExistHelper.class );
    when( helper.getTableColumns( any(), anyMap() ) )
      .thenReturn( successResponse );
    when( helper.isFailedResponse( successResponse ) ).thenReturn( false );
    when( dialog.createColumnsExistHelper() ).thenReturn( helper );

    setInternalState( dialog, "wTablename", wTablename );
    setInternalState( dialog, "wConnection", wConnection );
    setInternalState( dialog, "wSchemaname", wSchemaname );
    setInternalState( dialog, "wFields", wFields );
    setInternalState( dialog, "jobMeta", jobMeta );

    dialog.getListColumns();

    verify( dialog ).showErrorDialog( anyString() );
    verify( wFields, never() ).removeAll();
  }

  @Test
  public void testGetListColumns_HelperFailure_ShowsError() {
    JobEntryColumnsExistDialog dialog = mock( JobEntryColumnsExistDialog.class );
    doCallRealMethod().when( dialog ).getListColumns();
    doNothing().when( dialog ).showErrorDialog( anyString() );

    TextVar wTablename = mock( TextVar.class );
    when( wTablename.getText() ).thenReturn( "employees" );

    CCombo wConnection = mock( CCombo.class );
    when( wConnection.getText() ).thenReturn( "testConn" );

    TextVar wSchemaname = mock( TextVar.class );
    when( wSchemaname.getText() ).thenReturn( "" );

    TableView wFields = mock( TableView.class );
    JobMeta jobMeta = mock( JobMeta.class );

    JSONObject failureResponse = new JSONObject();
    failureResponse.put( ACTION_STATUS, FAILURE_RESPONSE );
    failureResponse.put( "message", "Connection failed" );

    JobEntryColumnsExistHelper helper = mock( JobEntryColumnsExistHelper.class );
    when( helper.getTableColumns( any(), anyMap() ) )
      .thenReturn( failureResponse );
    when( helper.isFailedResponse( failureResponse ) ).thenReturn( true );
    when( dialog.createColumnsExistHelper() ).thenReturn( helper );

    setInternalState( dialog, "wTablename", wTablename );
    setInternalState( dialog, "wConnection", wConnection );
    setInternalState( dialog, "wSchemaname", wSchemaname );
    setInternalState( dialog, "wFields", wFields );
    setInternalState( dialog, "jobMeta", jobMeta );

    dialog.getListColumns();

    verify( dialog ).showErrorDialog( "Connection failed" );
    verify( wFields, never() ).removeAll();
  }

  @Test
  public void testCreateColumnsExistHelper_ReturnsNewInstance() {
    JobEntryColumnsExistDialog dialog = mock( JobEntryColumnsExistDialog.class );
    doCallRealMethod().when( dialog ).createColumnsExistHelper();

    JobEntryColumnsExistHelper result = dialog.createColumnsExistHelper();

    assertNotNull( result );
    assertTrue( result instanceof JobEntryColumnsExistHelper );
  }

  @Test
  public void testShowErrorDialog_SetsTextAndMessageAndOpens() {
    JobEntryColumnsExistDialog dialog = mock( JobEntryColumnsExistDialog.class );
    doCallRealMethod().when( dialog ).showErrorDialog( anyString() );
    setInternalState( dialog, "shell", mock( Shell.class ) );

    try ( var mb = mockConstruction( MessageBox.class ) ) {
      dialog.showErrorDialog( "Something went wrong" );

      assertNotNull( mb.constructed() );
      MessageBox constructed = mb.constructed().get( 0 );
      verify( constructed ).setText( anyString() );
      verify( constructed ).setMessage( "Something went wrong" );
      verify( constructed ).open();
    }
  }
}
