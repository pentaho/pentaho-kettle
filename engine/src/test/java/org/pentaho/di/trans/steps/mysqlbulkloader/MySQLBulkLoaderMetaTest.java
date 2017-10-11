/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.mysqlbulkloader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class MySQLBulkLoaderMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( /*"connection",*/ "schema", "table", "encoding", "delimiter", "enclosure",
        "escape_char", "replace", "ignore", "local", "fifo_file_name",  "bulk_size",
        "stream_name", "field_name", "field_format_ok" );

    Map<String, String> getterMap = new HashMap<String, String>();
    //getterMap.put( "connection", "" );
    getterMap.put( "schema", "getSchemaName" );
    getterMap.put( "table", "getTableName" );
    getterMap.put( "encoding", "getEncoding" );
    getterMap.put( "delimiter", "getDelimiter" );
    getterMap.put( "enclosure", "getEnclosure" );
    getterMap.put( "escape_char", "getEscapeChar" );
    getterMap.put( "replace", "isReplacingData" );
    getterMap.put( "ignore", "isIgnoringErrors" );
    getterMap.put( "local", "isLocalFile" );
    getterMap.put( "fifo_file_name", "getFifoFileName" );
    getterMap.put( "bulk_size", "getBulkSize" );
    getterMap.put( "stream_name", "getFieldTable" );
    getterMap.put( "field_name", "getFieldStream" );
    getterMap.put( "field_format_ok", "getFieldFormatType" );

    Map<String, String> setterMap = new HashMap<String, String>();
    //setterMap.put( "connection", "" );
    setterMap.put( "schema", "setSchemaName" );
    setterMap.put( "table", "setTableName" );
    setterMap.put( "encoding", "setEncoding" );
    setterMap.put( "delimiter", "setDelimiter" );
    setterMap.put( "enclosure", "setEnclosure" );
    setterMap.put( "escape_char", "setEscapeChar" );
    setterMap.put( "replace", "setReplacingData" );
    setterMap.put( "ignore", "setIgnoringErrors" );
    setterMap.put( "local", "setLocalFile" );
    setterMap.put( "fifo_file_name", "setFifoFileName" );
    setterMap.put( "bulk_size", "setBulkSize" );
    setterMap.put( "stream_name", "setFieldTable" );
    setterMap.put( "field_name", "setFieldStream" );
    setterMap.put( "field_format_ok", "setFieldFormatType" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<int[]> fieldFormatTypeArrayLoadSaveValidator =
      new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator( MySQLBulkLoaderMeta.getFieldFormatTypeCodes().length ), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "stream_name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "field_name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "field_format_ok", fieldFormatTypeArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( MySQLBulkLoaderMeta.class, attributes, getterMap, setterMap,
        fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<MySQLBulkLoaderMeta, MySQLBulkLoaderData> mockHelper =
            new StepMockHelper<MySQLBulkLoaderMeta, MySQLBulkLoaderData>( "mySQLBulkLoader", MySQLBulkLoaderMeta.class, MySQLBulkLoaderData.class );

    MySQLBulkLoaderMeta mySQLBulkLoader = new MySQLBulkLoaderMeta();
    mySQLBulkLoader.setFieldTable( new String[] { "table1", "table2", "table3" } );
    mySQLBulkLoader.setFieldStream( new String[] { "stream1" } );
    mySQLBulkLoader.setFieldFormatType( new int[] { 0, 1 } );
    mySQLBulkLoader.setSchemaName( "test_schema" );

    try {
      String badXml = mySQLBulkLoader.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    mySQLBulkLoader.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = mySQLBulkLoader.getXML();

    int targetSz = mySQLBulkLoader.getFieldTable().length;
    Assert.assertEquals( targetSz, mySQLBulkLoader.getFieldStream().length );
    Assert.assertEquals( targetSz, mySQLBulkLoader.getFieldFormatType().length );

  }

}
