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

package org.pentaho.di.ui.core.database.dialog;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class GetDatabaseInfoProgressDialogTest {

  @Test
  public void databaseProgressDialogNotifyStateTest() throws Exception {
    DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
    Shell shell = mock( Shell.class );
    GetDatabaseInfoProgressDialog dialog = spy( new GetDatabaseInfoProgressDialog( shell, dbMetaMock ) );
    ProgressMonitorDialog progMonitorDialogMock = mock( ProgressMonitorDialog.class );
    doReturn( progMonitorDialogMock ).when( dialog ).newProgressMonitorDialog();

    boolean[] changeState = { false };
    //Add a progress listener to the dialog
    dialog.addDatabaseProgressListener( progressMonitor -> changeState[ 0 ] = true );
    //dialog will open and notify all listeners that the progress has finished
    dialog.open();
    //The listener has been notified and the changeState must now be true!
    assertTrue( changeState[0] );
  }

  @Test
  public void databaseProgressDialogNotifyStateNoListenersTest() throws Exception {
    DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
    Shell shell = mock( Shell.class );
    GetDatabaseInfoProgressDialog dialog = spy( new GetDatabaseInfoProgressDialog( shell, dbMetaMock ) );
    ProgressMonitorDialog progMonitorDialogMock = mock( ProgressMonitorDialog.class );
    doReturn( progMonitorDialogMock ).when( dialog ).newProgressMonitorDialog();

    boolean[] changeState = { false };
    DatabaseInfoProgressListener dbProgressListener = progressMonitor -> changeState[ 0 ] = true;
    //dialog will open and notify all listeners that the progress has finished.
    dialog.open();
    //There were no listeners so changeState must still be false!
    assertFalse( changeState[0] );
  }

  @Test
  public void databaseProgressDialogNotifyStateAddAndRemoveListenersTest() throws Exception {
    DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
    Shell shell = mock( Shell.class );
    GetDatabaseInfoProgressDialog dialog = spy( new GetDatabaseInfoProgressDialog( shell, dbMetaMock ) );
    ProgressMonitorDialog progMonitorDialogMock = mock( ProgressMonitorDialog.class );
    doReturn( progMonitorDialogMock ).when( dialog ).newProgressMonitorDialog();

    boolean[] changeState = { false };
    DatabaseInfoProgressListener dbProgressListener = progressMonitor -> changeState[ 0 ] = true;
    //Add a progress listener to the dialog
    dialog.addDatabaseProgressListener( dbProgressListener );
    //Remove the listener
    dialog.removeDatabaseProgressListener( dbProgressListener );
    //dialog will open and notify all listeners that the progress has finished.
    dialog.open();
    //The listener was added and then removed. There were no listeners so changeState must still be false!
    assertFalse( changeState[0] );
  }
}
