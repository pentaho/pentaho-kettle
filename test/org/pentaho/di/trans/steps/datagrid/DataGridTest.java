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
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.test.util.FieldAccessorUtl;

public class DataGridTest {

  private static final String FIELD_NAME_STR_1 = "f_str1";
  private static final String FIELD_NAME_NUM_2 = "f_str2";
  private static final String FIELD_NAME_NUM_1 = "f_num1";

  private static final String FIELD_TYPE_STRING = "String";
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
  public void test_emptyStringIsNotNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( true );

    final DataGridMeta meta = new DataGridMeta();

    String[] fieldNames = new String[] { FIELD_NAME_STR_1, FIELD_NAME_NUM_2, FIELD_NAME_NUM_1 };
    final String[] fieldTypes = new String[] { FIELD_TYPE_STRING, FIELD_TYPE_STRING, FIELD_TYPE_NUMBER };
    String[] fieldFormats = new String[] { null, null, null };
    String[] currencies = new String[] { null, null, null };
    String[] decimals = new String[] { null, null, null };
    String[] groups = new String[] { null, null, null };
    int[] fieldLengths = new int[] { -1, -1, -1 };
    int[] fieldPrecisions = new int[] { -1, -1, -1 };
    boolean[] setEmptyStrings = new boolean[] { false, true, false };

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
        new String[] { "1", "2", "3" }, //
          new String[] { "a", "b", "34" }, //
          new String[] { " ", "  ", " " }, //
          new String[] { "", "", "" }, //
          new String[] { null, null, null } //
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

    final String[] expectedFieldNames = fieldNames;

    final int[] expectedFieldTypes =
        new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_NUMBER };

    final int expectedRowCount = rowCount;
    final int expectedFieldsCount = fieldsCount;
    Assert.assertEquals( "(test data) expectedFieldNames.length", expectedFieldsCount, expectedFieldNames.length );
    Assert.assertEquals( "(test data) expectedFieldTypes.length", expectedFieldsCount, expectedFieldTypes.length );
    Assert.assertEquals( "(test data) expectedFieldTypes.length", expectedFieldsCount, expectedFieldTypes.length );
    Object[][] expectedRows = new Object[][] { //
        new Object[] { "1", "", 3.0 }, //
          new Object[] { "a", "", 34.0 }, //
          new Object[] { "", "", null }, //
          new Object[] { "", "", null }, //
          new Object[] { null, "", null } //
        };
    assertSize( "(test data) expectedRows", expectedRowCount, expectedFieldsCount, expectedRows );

    meta.setDataLines( buildListListString( dataRows ) );
    {
      final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
      final List<RowMetaAndData> inputList =
          java.util.Collections.singletonList( new RowMetaAndData( new RowMeta(), new Object[0] ) );

      List<RowMetaAndData> ret =
          TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
              TransTestFactory.DUMMY_STEPNAME, inputList );

      assertResultRows( "empty input data", expectedRows, expectedFieldNames, expectedFieldTypes, ret );
    }
    {
      final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
      final RowMetaInterface inputRowMeta = buildRowMeta( new ValueMetaString( "ff" ) );
      final Object[] inputRowData = new Object[] { "asdf" };
      final List<RowMetaAndData> inputList =
          java.util.Collections.singletonList( new RowMetaAndData( inputRowMeta, inputRowData ) );

      List<RowMetaAndData> ret =
          TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
              TransTestFactory.DUMMY_STEPNAME, inputList );

      assertResultRows( "not empty input data", expectedRows, expectedFieldNames, expectedFieldTypes, ret );
    }
  }

  @Test
  public void test_emptyStringIsNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    final DataGridMeta meta = new DataGridMeta();

    String[] fieldNames = new String[] { FIELD_NAME_STR_1, FIELD_NAME_NUM_2, FIELD_NAME_NUM_1 };
    final String[] fieldTypes = new String[] { FIELD_TYPE_STRING, FIELD_TYPE_STRING, FIELD_TYPE_NUMBER };
    String[] fieldFormats = new String[] { null, null, null };
    String[] currencies = new String[] { null, null, null };
    String[] decimals = new String[] { null, null, null };
    String[] groups = new String[] { null, null, null };
    int[] fieldLengths = new int[] { -1, -1, -1 };
    int[] fieldPrecisions = new int[] { -1, -1, -1 };
    boolean[] setEmptyStrings = new boolean[] { false, true, false };

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
        new String[] { "1", "2", "3" }, //
          new String[] { "a", "b", "34" }, //
          new String[] { " ", "  ", " " }, //
          new String[] { "", "", "" }, //
          new String[] { null, null, null } //
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

    final String[] expectedFieldNames = fieldNames;

    final int[] expectedFieldTypes =
        new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_NUMBER };

    final int expectedRowCount = rowCount;
    final int expectedFieldsCount = fieldsCount;
    Assert.assertEquals( "(test data) expectedFieldNames.length", expectedFieldsCount, expectedFieldNames.length );
    Assert.assertEquals( "(test data) expectedFieldTypes.length", expectedFieldsCount, expectedFieldTypes.length );
    Assert.assertEquals( "(test data) expectedFieldTypes.length", expectedFieldsCount, expectedFieldTypes.length );
    Object[][] expectedRows = new Object[][] { //
        new Object[] { "1", "", 3.0 }, //
          new Object[] { "a", "", 34.0 }, //
          new Object[] { "", "", null }, //
          new Object[] { "", "", null }, //
          new Object[] { null, "", null } //
        };
    assertSize( "(test data) expectedRows", expectedRowCount, expectedFieldsCount, expectedRows );

    meta.setDataLines( buildListListString( dataRows ) );
    {
      final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
      final List<RowMetaAndData> inputList =
          java.util.Collections.singletonList( new RowMetaAndData( new RowMeta(), new Object[0] ) );

      List<RowMetaAndData> ret =
          TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
              TransTestFactory.DUMMY_STEPNAME, inputList );

      assertResultRows( "empty input data", expectedRows, expectedFieldNames, expectedFieldTypes, ret );
    }
    {
      final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
      final RowMetaInterface inputRowMeta = buildRowMeta( new ValueMetaString( "ff" ) );
      final Object[] inputRowData = new Object[] { "asdf" };
      final List<RowMetaAndData> inputList =
          java.util.Collections.singletonList( new RowMetaAndData( inputRowMeta, inputRowData ) );

      List<RowMetaAndData> ret =
          TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
              TransTestFactory.DUMMY_STEPNAME, inputList );

      assertResultRows( "not empty input data", expectedRows, expectedFieldNames, expectedFieldTypes, ret );
    }
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
          java.util.Collections.singletonList( new RowMetaAndData( new RowMeta(), new Object[0] ) );

      try {
        List<RowMetaAndData> ret =
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
        List<RowMetaAndData> ret =
            TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
                TransTestFactory.DUMMY_STEPNAME, inputList );
        Assert.fail( "not empty input data. KettleException expected" );
      } catch ( KettleException e ) {
        // NOP: Ok
      }

    }
  }

  /**
   * 
   * @param expectedRows
   * @param expectedFieldNames
   * @param expectedFieldTypes
   * @param ret
   * @throws KettleValueException
   */
  private void assertResultRows( final String msg, final Object[][] expectedRows, final String[] expectedFieldNames,
      final int[] expectedFieldTypes, List<RowMetaAndData> ret ) throws KettleValueException {

    Assert.assertNotNull( msg + ". So we have some results", ret );

    final int expectedRowCount = expectedRows.length;
    Assert.assertEquals( msg + ". We have one result row", expectedRowCount, ret.size() );

    if ( expectedRowCount > 0 ) {
      Assert.assertNotNull( msg + ". expectedRows[0]", expectedRows[0] );
      final int expectedFieldsCount = expectedRows[0].length;
      for ( int iRow = 0; iRow < expectedRowCount; iRow++ ) {
        RowMetaAndData rmd = ret.get( iRow );

        Assert.assertEquals( msg + ". Result row includes input plus result columns[" + iRow + "]",
            expectedFieldsCount, rmd.size() );
        for ( int iField = 0, nFields = expectedFieldsCount; iField < nFields; iField++ ) {

          final Object expectedResult = expectedRows[iRow][iField];
          ValueMetaInterface resultValueMeta = rmd.getValueMeta( iField );

          Assert.assertNotNull( resultValueMeta );
          Assert.assertEquals( msg + ". resultName[" + iRow + "][" + iField + "]", expectedFieldNames[iField],
              resultValueMeta.getName() );
          Assert.assertEquals( msg + ". resultType[" + iRow + "][" + iField + "]", expectedFieldTypes[iField],
              resultValueMeta.getType() );

          switch ( expectedFieldTypes[iField] ) {
            case ValueMetaInterface.TYPE_STRING:
              Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].0", expectedResult, rmd
                  .getString( iField, null ) );
              Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].1", expectedResult, rmd
                  .getString( expectedFieldNames[iField], null ) );
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              if ( expectedResult != null ) {
                final double defaulValue =
                    ( expectedResult instanceof Number && ( (Number) expectedResult ).doubleValue() == 0.0 ) ? 1.0
                        : 0.0;

                Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].00", expectedResult, rmd
                    .getNumber( iField, defaulValue ) );
                Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].10", expectedResult, rmd
                    .getNumber( expectedFieldNames[iField], defaulValue ) );

              } else {
                Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].00", 0.0, rmd.getNumber(
                    iField, 0.0 ), 0.0 );
                Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].01", 1.0, rmd.getNumber(
                    iField, 1.0 ), 0.0 );

                Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].10", 0.0, rmd.getNumber(
                    expectedFieldNames[iField], 0.0 ), 0.0 );
                Assert.assertEquals( msg + ". expectedResult[" + iRow + "][" + iField + "].11", 1.0, rmd.getNumber(
                    expectedFieldNames[iField], 0.1 ), 1.0 );
              }
              break;
            default:
              Assert.fail( "unpredicted Field type: " + expectedFieldTypes[iField] );
          }
        }
      }
    }
  }

  private void assertSize( String msg, int expectedRowCount, int expectedFieldsCount, Object[][] rows ) {
    Assert.assertNotNull( msg + ". rows", rows );
    Assert.assertEquals( msg + ". rows.length", expectedRowCount, rows.length );
    for ( int i = 0, n = rows.length; i < n; i++ ) {
      final Object[] row = rows[i];
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
      if ( rowValues != null ) {
        for ( String colValue : rowValues ) {
          row.add( colValue );
        }
      }
      list.add( row );
    }
    return list;
  }

  /**
   * 
   * @param valueMetaInterfaces
   * @return
   */
  private static RowMetaInterface buildRowMeta( ValueMetaInterface... valueMetaInterfaces ) {
    RowMetaInterface rm = new RowMeta();
    for ( int i = 0; i < valueMetaInterfaces.length; i++ ) {
      rm.addValueMeta( valueMetaInterfaces[i] );
    }
    return rm;
  }

}
