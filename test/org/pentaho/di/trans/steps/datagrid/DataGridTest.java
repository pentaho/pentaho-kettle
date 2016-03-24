/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.datagrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.test.util.FieldAccessorUtl;

public class DataGridTest {

  private static final String FIELD_NAME_NUM_1 = "f_num1";

  private static final String FIELD_TYPE_NUMBER = "Number";

  private static final String STEP_NAME = "testDataGridStep";

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init();
  }

  @After
  public void after() throws KettleException, NoSuchFieldException, IllegalAccessException {
    FieldAccessorUtl.resetEmptyStringIsNotNull();
  }

  @Test
  public void test_invalid() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    final DataGridMeta meta = new DataGridMeta();

    String[] fieldNames = new String[] { FIELD_NAME_NUM_1 };
    final String[] fieldTypes = new String[] { FIELD_TYPE_NUMBER };
    String[] fieldFormats = new String[] { null };
    String[] currencies = new String[] { null };
    String[] decimals = new String[] { null };
    String[] groups = new String[] { null };
    int[] fieldLengths = new int[] { -1 };
    int[] fieldPrecisions = new int[] { -1 };
    boolean[] setEmptyStrings = new boolean[] { false };

    final int fieldsCount = fieldNames.length;
    Assert.assertEquals( "(test data) fieldTypes.length", fieldsCount, fieldTypes.length );
    Assert.assertEquals( "(test data) fieldFormats.length", fieldsCount, fieldFormats.length );
    Assert.assertEquals( "(test data) currencies.length", fieldsCount, currencies.length );
    Assert.assertEquals( "(test data) decimals.length", fieldsCount, decimals.length );
    Assert.assertEquals( "(test data) groups.length", fieldsCount, groups.length );
    Assert.assertEquals( "(test data) fieldLengths.length", fieldsCount, fieldLengths.length );
    Assert.assertEquals( "(test data) fieldPrecisions.length", fieldsCount, fieldPrecisions.length );
    Assert.assertEquals( "(test data) setEmptyStrings.length", fieldsCount, setEmptyStrings.length );
    final String[][] dataRows = new String[][] { //
      new String[] { "a" }, //
      new String[] { "1" } //
    };
    final int rowCount = dataRows.length;
    assertSize( "(test data) dataRows", rowCount, fieldsCount, dataRows );

    meta.setFieldName( fieldNames );
    meta.setFieldType( fieldTypes );
    meta.setFieldFormat( fieldFormats );
    meta.setCurrency( currencies );
    meta.setDecimal( decimals );
    meta.setGroup( groups );
    meta.setFieldLength( fieldLengths );
    meta.setFieldPrecision( fieldPrecisions );
    meta.setEmptyString( setEmptyStrings );

    meta.setDataLines( buildListListString( dataRows ) );
    {
      final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
      final List<RowMetaAndData> inputList =
        java.util.Collections.singletonList( new RowMetaAndData( new RowMeta(), new Object[ 0 ] ) );

      try {
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
          TransTestFactory.DUMMY_STEPNAME, inputList );
        Assert.fail( "empty input data. KettleException expected" );
      } catch ( KettleException e ) {
        // NOP: Ok
      }
    }
    {
      final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
      final RowMetaInterface inputRowMeta = buildRowMeta( new ValueMetaString( "ff" ) );
      final Object[] inputRowData = new Object[] { "asdf" };
      final List<RowMetaAndData> inputList =
        java.util.Collections.singletonList( new RowMetaAndData( inputRowMeta, inputRowData ) );

      try {
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
          TransTestFactory.DUMMY_STEPNAME, inputList );
        Assert.fail( "not empty input data. KettleException expected" );
      } catch ( KettleException e ) {
        // NOP: Ok
      }

    }
  }

  private void assertSize( String msg, int expectedRowCount, int expectedFieldsCount, Object[][] rows ) {
    Assert.assertNotNull( msg + ". rows", rows );
    Assert.assertEquals( msg + ". rows.length", expectedRowCount, rows.length );
    for ( int i = 0, n = rows.length; i < n; i++ ) {
      final Object[] row = rows[ i ];
      Assert.assertNotNull( msg + ". row[" + i + "]", row );
      Assert.assertEquals( msg + ". row[" + i + "].length", expectedFieldsCount, row.length );
    }
  }

  private static List<List<String>> buildListListString( String[][] gridvalues ) {
    if ( gridvalues == null ) {
      return null;
    }
    final int rowCount = gridvalues.length;
    List<List<String>> list = new ArrayList<List<String>>( rowCount );
    for ( String[] rowValues : gridvalues ) {
      final int colCount = rowValues.length;
      List<String> row = new ArrayList<String>( colCount );
      Collections.addAll( row, rowValues );
      list.add( row );
    }
    return list;
  }

  /**
   * @param valueMetaInterfaces
   * @return
   */
  private static RowMetaInterface buildRowMeta( ValueMetaInterface... valueMetaInterfaces ) {
    RowMetaInterface rm = new RowMeta();
    for ( int i = 0; i < valueMetaInterfaces.length; i++ ) {
      rm.addValueMeta( valueMetaInterfaces[ i ] );
    }
    return rm;
  }

}
