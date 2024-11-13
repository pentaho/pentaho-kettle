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


package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.ui.core.PropsUI;

import java.lang.reflect.Field;
import java.util.Collections;


public class PreviewRowsDialogTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void getDataForRow() throws Exception {

    RowMetaInterface rowMetaInterface = Mockito.mock( RowMetaInterface.class );
    Mockito.when( rowMetaInterface.size() ).thenReturn( 3 );
    Mockito.when( rowMetaInterface.getValueMeta( Mockito.anyInt() ) ).thenReturn( Mockito.mock( ValueMetaInterface.class ) );

    Field propsField = Props.class.getDeclaredField( "props" );
    propsField.setAccessible( true );
    propsField.set( PropsUI.class, Mockito.mock( PropsUI.class ) );

    PreviewRowsDialog previewRowsDialog = new PreviewRowsDialog( Mockito.mock( Shell.class ), Mockito.mock( VariableSpace.class ), SWT.None, "test",
            rowMetaInterface, Collections.emptyList() );

    //run without NPE
    int actualResult = previewRowsDialog.getDataForRow( Mockito.mock( TableItem.class ), null );
    Assert.assertEquals( 0, actualResult );
  }
}
