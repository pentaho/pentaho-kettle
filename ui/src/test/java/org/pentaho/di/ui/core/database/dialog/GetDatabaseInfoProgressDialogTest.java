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
package org.pentaho.di.ui.core.database.dialog;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.database.DatabaseMeta;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { ProgressMonitorDialog.class, GetDatabaseInfoProgressDialog.class } )
public class GetDatabaseInfoProgressDialogTest {

  @Test
  public void databaseProgressDialogNotifyStateTest() throws Exception {
    DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
    Shell shell = mock( Shell.class );
    GetDatabaseInfoProgressDialog dialog = new GetDatabaseInfoProgressDialog( shell, dbMetaMock );
    ProgressMonitorDialog progMonitorDialogMock = mock( ProgressMonitorDialog.class );
    whenNew( ProgressMonitorDialog.class ).withArguments( shell ).thenReturn( progMonitorDialogMock );
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
    GetDatabaseInfoProgressDialog dialog = new GetDatabaseInfoProgressDialog( shell, dbMetaMock );
    ProgressMonitorDialog progMonitorDialogMock = mock( ProgressMonitorDialog.class );
    whenNew( ProgressMonitorDialog.class ).withArguments( shell ).thenReturn( progMonitorDialogMock );
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
    GetDatabaseInfoProgressDialog dialog = new GetDatabaseInfoProgressDialog( shell, dbMetaMock );
    ProgressMonitorDialog progMonitorDialogMock = mock( ProgressMonitorDialog.class );
    whenNew( ProgressMonitorDialog.class ).withArguments( shell ).thenReturn( progMonitorDialogMock );
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
