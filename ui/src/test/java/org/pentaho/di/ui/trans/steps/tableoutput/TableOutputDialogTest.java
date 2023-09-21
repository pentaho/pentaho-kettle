/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.tableoutput;

import org.eclipse.swt.custom.CCombo;
import org.junit.Before;
import org.junit.Test;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.TextVar;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

public class TableOutputDialogTest {

  private static RowMetaInterface filled;
  private static RowMetaInterface empty;
  private static String[] sample = { "1", "2", "3" };

  @Before
  public void setup() {
    filled = createRowMeta( sample, false );
    empty = createRowMeta( sample, true );
  }

  @Test
  public void validationRowMetaTest() throws Exception {
    Method m = TableOutputDialog.class.getDeclaredMethod( "isValidRowMeta", RowMetaInterface.class );
    m.setAccessible( true );
    Object result1 = m.invoke( null, filled );
    Object result2 = m.invoke( null, empty );
    assertTrue( Boolean.parseBoolean( result1 + "" ) );
    assertFalse( Boolean.parseBoolean( result2 + "" ) );
  }

  private RowMetaInterface createRowMeta( String[] args, boolean hasEmptyFields ) {
    RowMetaInterface result = new RowMeta();
    if ( hasEmptyFields ) {
      result.addValueMeta( new ValueMetaString( "" ) );
    }
    for ( String s : args ) {
      result.addValueMeta( new ValueMetaString( s ) );
    }
    return result;
  }

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
