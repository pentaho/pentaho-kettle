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


package org.pentaho.di.ui.trans.steps.tableoutput;

import org.eclipse.swt.custom.CCombo;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.TextVar;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

public class TableOutputDialogTest {

  private void isConnectionSupportedTest( boolean supported ) {
    TableOutputDialog dialog = mock( TableOutputDialog.class );

    TransMeta transMeta = mock( TransMeta.class );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    TextVar text = mock( TextVar.class );
    CCombo combo = mock( CCombo.class );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );

    setInternalState( dialog, "wTable", text );
    setInternalState( dialog, "wConnection", combo );
    setInternalState( dialog, "transMeta", transMeta );

    when( text.getText() ).thenReturn( "someTable" );
    when( combo.getText() ).thenReturn( "someConnection" );
    when( transMeta.findDatabase( anyString() ) ).thenReturn( dbMeta );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbInterface );

    doNothing().when( dialog ).showUnsupportedConnectionMessageBox( dbInterface );
    doCallRealMethod().when( dialog ).isConnectionSupported();

    //Check that if the db interface does not support standard output then showUnsupportedConnection is called
    when( dbInterface.supportsStandardTableOutput() ).thenReturn( supported );
    dialog.isConnectionSupported();
    verify( dialog, times( !supported ? 1 : 0 ) ).showUnsupportedConnectionMessageBox( dbInterface );
  }

  @Test
  public void isConnectionSupportedValidTest() {
    isConnectionSupportedTest( true );
  }

  @Test
  public void isConnectionSupportedInvalidTest() {
    isConnectionSupportedTest( false );
  }
}
