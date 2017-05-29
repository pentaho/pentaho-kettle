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

package org.pentaho.di.ui.trans.steps.excelinput;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;

import java.lang.reflect.Method;


public class ExcelInputDialogTest {

  @Test
  /**
   * http://jira.pentaho.com/browse/PDI-13930
   */
  public void getFieldsTest() throws Exception {
    ExcelInputDialog dialog = Mockito.mock( ExcelInputDialog.class );
    RowMeta fields = new RowMeta();

    ExcelInputMeta info = Mockito.mock( ExcelInputMeta.class );
    Mockito.doReturn( true ).when( info ).readAllSheets();
    int[] startColumn = {0};
    Mockito.doReturn( startColumn ).when( info ).getStartColumn();
    int[] startRow = {0};
    Mockito.doReturn( startRow ).when( info ).getStartRow();


    KWorkbook workbook =  Mockito.mock( KWorkbook.class );
    Mockito.doReturn( 1 ).when( workbook ).getNumberOfSheets();
    KSheet sheet = Mockito.mock( KSheet.class );
    Mockito.doReturn( sheet ).when( workbook ).getSheet( 0 );
    KCell cell  = Mockito.mock( KCell.class );
    int fieldCount = 400;
    for ( int i = 0; i <= fieldCount - 1; i++ ) {
      Mockito.doReturn( cell ).when( sheet ).getCell( i, 0 );
      Mockito.doReturn( cell ).when( sheet ).getCell( i, 1 );
    }
    Mockito.doReturn( "testValue" ).when( cell ).getContents();
    Mockito.doReturn( KCellType.NUMBER ).when( cell ).getType();

    PluginRegistry pluginRegistry = Mockito.mock( PluginRegistry.class );
    PluginInterface stringPlugin = Mockito.mock( PluginInterface.class );
    Mockito.doReturn( stringPlugin ).when( pluginRegistry ).getPlugin( ValueMetaPluginType.class, "1" );
    Mockito.doReturn( Mockito.mock( ValueMetaInterface.class ) ).when( pluginRegistry ).
            loadClass( stringPlugin, ValueMetaInterface.class );
    ValueMetaFactory.pluginRegistry = pluginRegistry;


    Method processingWorkbookMethod = ExcelInputDialog.class.getDeclaredMethod( "processingWorkbook", RowMetaInterface.class,
            ExcelInputMeta.class, KWorkbook.class );
    processingWorkbookMethod.setAccessible( true );
    processingWorkbookMethod.invoke( dialog, fields, info, workbook );

    Assert.assertEquals( fieldCount, fields.size() );
  }

}
