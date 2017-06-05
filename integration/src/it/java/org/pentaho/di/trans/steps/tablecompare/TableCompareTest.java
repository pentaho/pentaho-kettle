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

package org.pentaho.di.trans.steps.tablecompare;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class TableCompareTest {

  public static String PKG = "blackbox/tests/trans/steps/tablecompare/";
  static LoggingObjectInterface log;
  static DatabaseMeta databaseMeta;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
    log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );
    databaseMeta =
        new DatabaseMeta( "TableCompare", "Hypersonic", "JDBC", null, "mem:HSQLDB-JUNIT-LOGJOB", null, null, null );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    InputStream input = TableCompareTest.class.getClassLoader().getResourceAsStream( PKG + "PDI-7255.sql" );
    String sql = TestUtilities.getStringFromInput( input );
    Database db = new Database( log, databaseMeta );
    db.connect();
    db.execStatements( sql );
    db.commit( true );
    db.disconnect();
  }

  static final String schema = "public";

  static String ehlkd = "ErrorHandlingKeyDescIn";
  static String ehlrvi = "ErrorHandlingRefValueIn";
  static String ehlcvif = "ErrorHandlingCompareValueInF";
  static String schemaName = "schema";
  static String reference = "reference";
  static String compare = "compare";
  static String key = "key";
  static String exclude = "exclude";

  private RowMetaInterface getRowMeta() {
    RowMetaInterface rm = new RowMeta();
    ValueMetaInterface[] valuesMeta =
    {
      // fields to handle error information
      new ValueMetaString( ehlkd ),
      new ValueMetaString( ehlrvi ),
      new ValueMetaString( ehlcvif ),
      // fields to handle connection properties
      new ValueMetaString( schemaName ), new ValueMetaString( reference ),
      new ValueMetaString( compare ), new ValueMetaString( key ),
      new ValueMetaString( exclude ) };
    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }
    return rm;
  }

  // used to test PDI-7255
  private Object[] getData1() {
    Object[] r1 = new Object[] { "", "", "", schema, reference, compare, "key1", "" };
    return r1;
  }

  // used to test complex keys comparison
  private Object[] getData2() {
    Object[] r2 = new Object[] { "", "", "", schema, reference, compare, "key1, key2", "" };
    return r2;
  }

  // used to test value comparison results
  private Object[] getData3() {
    Object[] r2 = new Object[] { "", "", "", schema, reference, compare, "key1", "" };
    return r2;
  }

  // used to test value comparison results, but fields with values that does not match are excluded:
  private Object[] getData4() {
    Object[] r2 = new Object[] { "", "", "", schema, reference, compare, "key1", "key2" };
    return r2;
  }

  /**
   * PDI-7255 - The table compare step reports incorrect record as missing
   *
   * @throws KettleException
   * @throws IOException
   */
  @Test
  public void testMissedReferenceLinesAreRepored() throws KettleException, IOException {
    // prepare database
    executeSqlPrecondition( "PDI-7255.sql" );

    TableCompareMeta meta = getTableCompareMeta();

    // prepare input date
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( getRowMeta(), getData1() ) );

    // execute transformations
    TransMeta trMeta = TransTestFactory.generateTestTransformationError( null, meta, "junit" );
    Map<String, RowStepCollector> result = TransTestFactory
        .executeTestTransformationError( trMeta, "junit", inputData );

    // check the results
    List<RowMetaAndData> read = result.get( TransTestFactory.DUMMY_STEPNAME ).getRowsRead();
    Assert.assertTrue( "Step achieve comparsion data", read.size() == 1 );
    RowMetaAndData row = read.get( 0 );

    Assert.assertEquals( "Number of errors", 1, row.getInteger( 8 ).intValue() );
    Assert.assertEquals( "Reference table row count", 3, row.getInteger( 9 ).intValue() );
    Assert.assertEquals( "Compare table row count", 2, row.getInteger( 10 ).intValue() );

    Assert.assertEquals( "Number of left joins errors", 0, row.getInteger( 11 ).intValue() );
    Assert.assertEquals( "Number of inner joins errors", 0, row.getInteger( 12 ).intValue() );
    Assert.assertEquals( "Number of right joins errors", 1, row.getInteger( 13 ).intValue() );

    List<RowMetaAndData> errors = result.get( TransTestFactory.ERROR_STEPNAME ).getRowsRead();
    Assert.assertTrue( "One error output to negative step", errors.size() == 1 );
    row = errors.get( 0 );

    Assert.assertEquals( "Reported one missing key", "KEY1 = '2'", row.getString( ehlkd, null ) );
  }

  /**
   * Test table compare test can handle complex keys comparison
   *
   * @throws IOException
   * @throws KettleException
   */
  @Test
  public void testComplexKeysComparsion() throws IOException, KettleException {
    executeSqlPrecondition( "complex_key_test.sql" );

    TableCompareMeta meta = getTableCompareMeta();
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( getRowMeta(), getData2() ) );

    TransMeta trMeta = TransTestFactory.generateTestTransformationError( null, meta, "junit" );
    Map<String, RowStepCollector> result = TransTestFactory
        .executeTestTransformationError( trMeta, "junit", inputData );

    List<RowMetaAndData> read = result.get( TransTestFactory.DUMMY_STEPNAME ).getRowsRead();
    List<RowMetaAndData> errors = result.get( TransTestFactory.ERROR_STEPNAME ).getRowsRead();

    Assert.assertEquals( "One row passed to positive step", 1, read.size() );
    Assert.assertEquals( "Two rows passed to negative step", 2, errors.size() );

    // check positive step output
    RowMetaAndData row = read.get( 0 );

    Assert.assertEquals( "Number errors", 2, row.getInteger( 8 ).intValue() );
    Assert.assertEquals( "Reference table row count", 4, row.getInteger( 9 ).intValue() );
    Assert.assertEquals( "Compare table row count", 4, row.getInteger( 10 ).intValue() );

    Assert.assertEquals( "Number of left joins errors", 1, row.getInteger( 11 ).intValue() );
    Assert.assertEquals( "Number of inner joins errors", 0, row.getInteger( 12 ).intValue() );
    Assert.assertEquals( "Number of right joins errors", 1, row.getInteger( 13 ).intValue() );

    // check error step output
    row = errors.get( 0 );
    Assert.assertEquals( "error composite key is mentioned", "KEY1 = '2' and KEY2 = '1'", row.getString( 0, null ) );
    row = errors.get( 1 );
    Assert.assertEquals( "error composite key is mentioned", "KEY1 = '2' and KEY2 = '2'", row.getString( 0, null ) );
  }

  /**
   * Test compare table if reference table is empty
   *
   * @throws IOException
   * @throws KettleException
   */
  @Test
  public void testValueNotExistsReference() throws IOException, KettleException {
    executeSqlPrecondition( "compare_only.sql" );

    TableCompareMeta meta = getTableCompareMeta();
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( getRowMeta(), getData3() ) );

    TransMeta trMeta = TransTestFactory.generateTestTransformationError( null, meta, "junit" );
    Map<String, RowStepCollector> result = TransTestFactory
        .executeTestTransformationError( trMeta, "junit", inputData );

    List<RowMetaAndData> read = result.get( TransTestFactory.DUMMY_STEPNAME ).getRowsRead();
    List<RowMetaAndData> errors = result.get( TransTestFactory.ERROR_STEPNAME ).getRowsRead();

    Assert.assertEquals( "One row passed to positive output", 1, read.size() );

    RowMetaAndData row = read.get( 0 );
    Assert.assertEquals( "Errors reported", 4, row.getInteger( 8 ).intValue() );
    Assert.assertEquals( "Reference table row count", 0, row.getInteger( 9 ).intValue() );
    Assert.assertEquals( "Compare table row count", 4, row.getInteger( 10 ).intValue() );
    Assert.assertEquals( "Number of left joins errors", 0, row.getInteger( 11 ).intValue() );
    Assert.assertEquals( "Number of inner joins errors", 0, row.getInteger( 12 ).intValue() );
    Assert.assertEquals( "Number of right joins errors", 4, row.getInteger( 13 ).intValue() );

    Assert.assertEquals( "4 error rows passed to error output", 4, errors.size() );
  }

  /**
   * Test compare table reference table is empty
   *
   * @throws IOException
   * @throws KettleException
   */
  @Test
  public void testValueNotExistedInCompare() throws IOException, KettleException {
    executeSqlPrecondition( "reference_only.sql" );

    TableCompareMeta meta = getTableCompareMeta();
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( getRowMeta(), getData3() ) );

    TransMeta trMeta = TransTestFactory.generateTestTransformationError( null, meta, "junit" );
    Map<String, RowStepCollector> result = TransTestFactory
        .executeTestTransformationError( trMeta, "junit", inputData );

    List<RowMetaAndData> read = result.get( TransTestFactory.DUMMY_STEPNAME ).getRowsRead();
    List<RowMetaAndData> errors = result.get( TransTestFactory.ERROR_STEPNAME ).getRowsRead();

    RowMetaAndData row = read.get( 0 );
    Assert.assertEquals( "Errors reported", 4, row.getInteger( 8 ).intValue() );
    Assert.assertEquals( "Reference table row count", 4, row.getInteger( 9 ).intValue() );
    Assert.assertEquals( "Compare table row count", 0, row.getInteger( 10 ).intValue() );
    Assert.assertEquals( "Number of left joins errors", 4, row.getInteger( 11 ).intValue() );
    Assert.assertEquals( "Number of inner joins errors", 0, row.getInteger( 12 ).intValue() );
    Assert.assertEquals( "Number of right joins errors", 0, row.getInteger( 13 ).intValue() );

    Assert.assertEquals( "4 error rows passed to error output", 4, errors.size() );
  }

  /**
   * Test that step reports value comparison errors
   *
   * @throws IOException
   * @throws KettleException
   */
  @Test
  public void testValueComparsion() throws IOException, KettleException {
    executeSqlPrecondition( "complex_key_test.sql" );

    TableCompareMeta meta = getTableCompareMeta();
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( getRowMeta(), getData3() ) );

    TransMeta trMeta = TransTestFactory.generateTestTransformationError( null, meta, "junit" );
    Map<String, RowStepCollector> result = TransTestFactory
        .executeTestTransformationError( trMeta, "junit", inputData );

    List<RowMetaAndData> read = result.get( TransTestFactory.DUMMY_STEPNAME ).getRowsRead();
    List<RowMetaAndData> errors = result.get( TransTestFactory.ERROR_STEPNAME ).getRowsRead();

    Assert.assertEquals( "One row passed to positive output", 1, read.size() );
    Assert.assertEquals( "One row passed to negative output", 1, errors.size() );

    // check error is properly reported:
    RowMetaAndData row = read.get( 0 );

    Assert.assertEquals( "One errors reported", 1, row.getInteger( 8 ).intValue() );
    Assert.assertEquals( "Reference table row count", 4, row.getInteger( 9 ).intValue() );
    Assert.assertEquals( "Compare table row count", 4, row.getInteger( 10 ).intValue() );
    Assert.assertEquals( "Number of left joins errors", 0, row.getInteger( 11 ).intValue() );
    Assert.assertEquals( "Number of inner joins errors", 1, row.getInteger( 12 ).intValue() );
    Assert.assertEquals( "Number of right joins errors", 0, row.getInteger( 13 ).intValue() );

    row = errors.get( 0 );
    Assert.assertEquals( "Reported key for not match value", "KEY1 = '2'", row.getString( ehlkd, null ) );
    Assert.assertEquals( "Reported reference table value", "2", row.getString( ehlrvi, null ) );
    Assert.assertEquals( "Reported compare table value", "1", row.getString( ehlcvif, null ) );
  }

  /**
   * Test that step can ignore excluded values during comparison
   *
   * @throws IOException
   * @throws KettleException
   */
  @Test
  public void testValueExcludeComparsion() throws IOException, KettleException {
    executeSqlPrecondition( "complex_key_test.sql" );

    TableCompareMeta meta = getTableCompareMeta();
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( getRowMeta(), getData4() ) );

    TransMeta trMeta = TransTestFactory.generateTestTransformationError( null, meta, "junit" );
    Map<String, RowStepCollector> result = TransTestFactory
        .executeTestTransformationError( trMeta, "junit", inputData );

    List<RowMetaAndData> read = result.get( TransTestFactory.DUMMY_STEPNAME ).getRowsRead();
    List<RowMetaAndData> errors = result.get( TransTestFactory.ERROR_STEPNAME ).getRowsRead();

    Assert.assertEquals( "There is no errors reported", 0, errors.size() );

    RowMetaAndData row = read.get( 0 );

    Assert.assertEquals( "No errors reported", 0, row.getInteger( 8 ).intValue() );
    Assert.assertEquals( "Reference table row count", 4, row.getInteger( 9 ).intValue() );
    Assert.assertEquals( "Compare table row count", 4, row.getInteger( 10 ).intValue() );

    Assert.assertEquals( "Number of left joins errors", 0, row.getInteger( 11 ).intValue() );
    Assert.assertEquals( "Number of inner joins errors", 0, row.getInteger( 12 ).intValue() );
    Assert.assertEquals( "Number of right joins errors", 0, row.getInteger( 13 ).intValue() );
  }

  private TableCompareMeta getTableCompareMeta() {
    TableCompareMeta meta = new TableCompareMeta();
    meta.setDefault();
    // configure step
    meta.setReferenceConnection( databaseMeta );
    meta.setCompareConnection( databaseMeta );
    meta.setReferenceSchemaField( schemaName );
    meta.setCompareSchemaField( schemaName );
    // set table names
    meta.setReferenceTableField( reference );
    meta.setCompareTableField( compare );

    meta.setKeyFieldsField( key );
    meta.setExcludeFieldsField( exclude );

    meta.setKeyDescriptionField( ehlkd );
    meta.setValueReferenceField( ehlrvi );
    meta.setValueCompareField( ehlcvif );

    return meta;
  }

  private void executeSqlPrecondition( String sqlFile ) throws IOException, KettleDatabaseException {
    String path = PKG + sqlFile;
    InputStream input = TableCompareTest.class.getClassLoader().getResourceAsStream( PKG + sqlFile );
    if ( input == null ) {
      throw new IOException( "Resource not found in classpath: " + path );
    }
    String sql = TestUtilities.getStringFromInput( input );
    Database db = new Database( log, databaseMeta );
    db.connect();
    db.execStatements( sql );
    db.commit( true );
    db.disconnect();
  }
}
