/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.test.util.FieldAccessorUtl;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * User: Dzmitry Stsiapanau Date: 5/3/14 Time: 11:47 AM
 */

public class DataGridTest {

  private static final String FIELD_NAME_NUM_1 = "f_num1";

  private static final String FIELD_TYPE_NUMBER = "Number";

  private static final String STEP_NAME = "testDataGridStep";

  private DocumentBuilderFactory dbf;
  private DocumentBuilder db;
  private PluginRegistry registry;
  private int[] TYPES = { 6, 8, 4, 3, 5, 10, 1, 2, 9 };


  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    dbf = DocumentBuilderFactory.newInstance();
    db = dbf.newDocumentBuilder();
    registry = PluginRegistry.getInstance();
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

  @Test
  public void testProcessRow() throws Exception {
    TransMeta tm = new TransMeta();

    StepMeta dm = load( "datagrid.xml", "Data Grid" );
    tm.addStep( dm );
    StepMeta dm3 = load( "datagrid3.xml", "Data Grid 3" );
    tm.addStep( dm3 );
    StepMeta dm4 = load( "datagrid4.xml", "Data Grid 4" );
    tm.addStep( dm4 );
    StepMeta dm5 = load( "datagrid5.xml", "Data Grid 5" );
    tm.addStep( dm5 );
    TransHopMeta hi = new TransHopMeta( dm3, dm );
    TransHopMeta hi2 = new TransHopMeta( dm4, dm );
    TransHopMeta hi3 = new TransHopMeta( dm5, dm );
    tm.addTransHop( hi );
    tm.addTransHop( hi2 );
    tm.addTransHop( hi3 );

    dm.getStepMetaInterface().searchInfoAndTargetSteps( tm.getSteps() );

    Trans trans = new Trans( tm );
    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( "Data Grid", 0 );
    //    StepInterface si = trans.getStepInterface( "Data Grid", 0 );
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener( rc );

    trans.startThreads();
    trans.waitUntilFinished();

    List<RowMetaAndData> checkList = createData();
    List<RowMetaAndData> resultRows = rc.getRowsWritten();
    checkRows( resultRows, checkList );
  }

  private StepMeta load( String xml, String name ) throws Exception {
    Document doc;
    IMetaStore iMetaStore = null;

    DataGridMeta dm = new DataGridMeta();
    doc = db.parse( this.getClass().getResourceAsStream( xml ) );
    dm.loadXML( doc.getDocumentElement(), Collections.EMPTY_LIST, iMetaStore );
    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    return new StepMeta( dummyPid, name, dm );
  }

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = {
      new ValueMeta( "BigNumber", ValueMeta.TYPE_BIGNUMBER ),
      new ValueMeta( "Binary", ValueMeta.TYPE_BINARY ),
      new ValueMeta( "Boolean", ValueMeta.TYPE_BOOLEAN ),
      new ValueMeta( "Date", ValueMeta.TYPE_DATE ),
      new ValueMeta( "Integer", ValueMeta.TYPE_INTEGER ),
      new ValueMeta( "Internet Address", ValueMeta.TYPE_INET ),
      new ValueMeta( "Number", ValueMeta.TYPE_NUMBER ),
      new ValueMeta( "String", ValueMeta.TYPE_STRING ),
      new ValueMeta( "Timestamp", ValueMeta.TYPE_TIMESTAMP ),
      new ValueMeta( "BigNumber", ValueMeta.TYPE_BIGNUMBER, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Binary", ValueMeta.TYPE_BINARY, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Boolean", ValueMeta.TYPE_BOOLEAN, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Date", ValueMeta.TYPE_DATE, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Integer", ValueMeta.TYPE_INTEGER, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Internet Address", ValueMeta.TYPE_INET, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Number", ValueMeta.TYPE_NUMBER, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "String", ValueMeta.TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "Timestamp", ValueMeta.TYPE_TIMESTAMP, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ),
      new ValueMeta( "BigNumber", ValueMeta.TYPE_BIGNUMBER, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Binary", ValueMeta.TYPE_BINARY, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Boolean", ValueMeta.TYPE_BOOLEAN, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Date", ValueMeta.TYPE_DATE, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Integer", ValueMeta.TYPE_INTEGER, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Internet Address", ValueMeta.TYPE_INET, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Number", ValueMeta.TYPE_NUMBER, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "String", ValueMeta.TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_INDEXED ),
      new ValueMeta( "Timestamp", ValueMeta.TYPE_TIMESTAMP, ValueMetaInterface.STORAGE_TYPE_INDEXED ) };

    for ( ValueMetaInterface aValuesMeta : valuesMeta ) {
      rm.addValueMeta( aValuesMeta );
    }

    return rm;
  }

  public List<RowMetaAndData> createData() throws Exception {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();
    int storageType = 0;
    Object[] data = {
      new BigDecimal( "123456789.987654321" ),
      new byte[] { 49, 97, 50, 115, 51, 100 }, //"1a2s3d".getBytes()
      false,
      new Date( 1355296210000L ),
      1L,
      InetAddress.getByName( "192.168.1.2" ),
      3.0,
      "string1",
      Timestamp.valueOf( "2012-12-12 10:10:10.12345678" ),
      new byte[] { 32, 49, 50, 51, 52, 53, 54, 55, 56, 57, 46, 57, 56, 55, 54, 53, 52, 51, 50, 49 },
      new byte[] { 49, 97, 50, 115, 51, 100 },
      new byte[] { 78 },
      new byte[] { 50, 48, 49, 50, 47, 49, 50, 47, 49, 50, 32, 49, 48, 58, 49, 48, 58, 49, 48, 46, 48, 48, 48 },
      new byte[] { 32, 49 },
      new byte[] { 49, 57, 50, 46, 49, 54, 56, 46, 49, 46, 50 },
      new byte[] { 32, 51, 46, 48 },
      new byte[] { 115, 116, 114, 105, 110, 103, 49 },
      new byte[] { 49, 50, 45, 49, 50, 45, 49, 50, 32, 49, 48, 58, 49, 48, 58, 49, 48, 46, 49, 50, 51, 52, 53 },
      2,
      1,
      1,
      0,
      0,
      1,
      2,
      0,
      1
    };

    for ( int i = 0; i < 3 * 9; i++ ) {
      //          for ( int k = 1; k <= 9; k++ ) {
      Object[] obj = new Object[ 9 * 3 ];
      obj[ i ] = data[ i ];
      list.add( new RowMetaAndData( rm, obj ) );
      //          }
    }

    return list;
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   */
  public void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2 ) {
    int idx = 1;
    if ( rows1.size() != rows2.size() ) {
      fail( "Number of rows is not the same: " + rows1.size() + " and " + rows2.size() );
    }
    Iterator<RowMetaAndData> it1 = rows1.iterator();
    Iterator<RowMetaAndData> it2 = rows2.iterator();

    while ( it1.hasNext() && it2.hasNext() ) {
      RowMetaAndData rm1 = it1.next();
      RowMetaAndData rm2 = it2.next();

      Object[] r1 = rm1.getData();
      Object[] r2 = rm2.getData();

      if ( rm1.size() != rm2.size() ) {
        fail( "row nr " + idx + " is not equal" );
      }
      int[] fields = new int[ rm1.size() ];
      for ( int ydx = 0; ydx < rm1.size(); ydx++ ) {
        fields[ ydx ] = ydx;
      }
      try {
        if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
          fail( "row nr " + idx + " is not equal" );
        }
      } catch ( KettleValueException e ) {
        fail( "row nr " + idx + " is not equal" );
      } catch ( ClassCastException e ) {
        fail( "row nr " + idx + " is not equal" );
      }

      idx++;
    }
  }
}
