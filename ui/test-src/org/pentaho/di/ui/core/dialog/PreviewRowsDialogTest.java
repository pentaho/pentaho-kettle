/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;

import java.lang.reflect.Field;
import java.util.Collections;


public class PreviewRowsDialogTest {

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
