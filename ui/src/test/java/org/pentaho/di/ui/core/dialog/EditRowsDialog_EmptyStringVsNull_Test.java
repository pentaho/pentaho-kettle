/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.widgets.TableItem;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransTestingUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class EditRowsDialog_EmptyStringVsNull_Test {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" );
    executeAndAssertResults( new String[]{ "", null, null } );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    executeAndAssertResults( new String[]{ "", "", "" } );
  }

  private void executeAndAssertResults( String[] expected ) throws Exception {
    EditRowsDialog dialog = mock( EditRowsDialog.class );

    when( dialog.getRowForData( any( TableItem.class ), anyInt() ) ).thenCallRealMethod();
    doCallRealMethod().when( dialog ).setRowMeta( any( RowMetaInterface.class ) );
    doCallRealMethod().when( dialog ).setStringRowMeta( any( RowMetaInterface.class ) );

    when( dialog.isDisplayingNullValue( any( TableItem.class ), anyInt() ) ).thenReturn( false );

    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "s1" ) );
    meta.addValueMeta( new ValueMetaString( "s2" ) );
    meta.addValueMeta( new ValueMetaString( "s3" ) );
    dialog.setRowMeta( meta );
    dialog.setStringRowMeta( meta );

    TableItem item = mock( TableItem.class );
    when( item.getText( 1 ) ).thenReturn( " " );
    when( item.getText( 2 ) ).thenReturn( "" );
    when( item.getText( 3 ) ).thenReturn( null );

    Object[] data = dialog.getRowForData( item, 0 );
    TransTestingUtil.assertResult( expected, data );
  }
}
