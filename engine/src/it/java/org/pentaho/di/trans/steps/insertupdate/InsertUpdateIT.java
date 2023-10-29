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

package org.pentaho.di.trans.steps.insertupdate;

import static org.junit.Assert.assertArrayEquals;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

import junit.framework.TestCase;

@Ignore( "This test has sections that were commented because they no longer compile." )
public class InsertUpdateIT extends TestCase {

  public static final String[] databasesXML = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<connection>" + "<name>db</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
      + "<access>Native</access>" + "<database>mem:db</database>" + "<port></port>" + "<username>sa</username>"
      + "<password></password>" + "</connection>", };

  public static final String TARGET_TABLE = "insertupdate_step_test_case_table";

  private static final String[] insertStatement = {
    // New rows for the source
    "INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " + "VALUES (NULL, NULL, 'null_id_code', 1)",

    "INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " + "VALUES (NULL, 1, 'null_id', 3)",

    "INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " + "VALUES (1, NULL, 'null_code', 5)",

    "INSERT INTO " + TARGET_TABLE + "(ID, CODE, VALUE, ROW_ORDER) " + "VALUES (2, 2, 'non_null_keys', 7)",

  };

  // this points to the transformation
  Trans trans;

  // this points to the update step being tested
  public InsertUpdateMeta insupd;

  // these are used to write and read rows in the test transformation
  public RowStepCollector rc;
  public RowProducer rp;

  // the database used for the transformation run
  public Database db;

  // returns the structure of the target table
  public RowMetaInterface getTargetTableRowMeta() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaInteger( "ID", 8, 0 ),
      new ValueMetaInteger( "CODE", 8, 0 ),
      new ValueMetaString( "VALUE", 255, 0 ),
      new ValueMetaInteger( "ROW_ORDER", 8, 0 ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }
    return rm;
  }

  // adds lookup key line definition to the update step
  // input is in format {key, condition, stream, stream2}
  public void addLookup( String[] def ) {
    //FIXME The following commented block does not compile!
    // For now, it's a huge job to make things work again!
/*
    if ( insupd.getKeyLookup() == null ) {
      insupd.setKeyLookup( new String[0] );
      insupd.setKeyCondition( new String[0] );
      insupd.setKeyStream( new String[0] );
      insupd.setKeyStream2( new String[0] );
    }

    int newLength = insupd.getKeyLookup().length + 1;

    ArrayList<String> newKeyLookup = new ArrayList<String>( newLength );
    newKeyLookup.addAll( Arrays.asList( insupd.getKeyLookup() ) );
    newKeyLookup.add( def[0] );
    insupd.setKeyLookup( newKeyLookup.toArray( new String[0] ) );

    ArrayList<String> newKeyCondition = new ArrayList<String>( newLength );
    newKeyCondition.addAll( Arrays.asList( insupd.getKeyCondition() ) );
    newKeyCondition.add( def[1] );
    insupd.setKeyCondition( newKeyCondition.toArray( new String[0] ) );

    ArrayList<String> newKeyStream = new ArrayList<String>( newLength );
    newKeyStream.addAll( Arrays.asList( insupd.getKeyStream() ) );
    newKeyStream.add( def[2] );
    insupd.setKeyStream( newKeyStream.toArray( new String[0] ) );

    ArrayList<String> newKeyStream2 = new ArrayList<String>( newLength );
    newKeyStream2.addAll( Arrays.asList( insupd.getKeyStream2() ) );
    newKeyStream2.add( def[3] );
    insupd.setKeyStream2( newKeyStream2.toArray( new String[0] ) );
*/
  }

  @Override
  @Before
  public void setUp() throws Exception {

    KettleEnvironment.init();

    /* SET UP TRANSFORMATION */

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "insert/update test" );

    // Add the database connections
    for ( int i = 0; i < databasesXML.length; i++ ) {
      DatabaseMeta databaseMeta = new DatabaseMeta( databasesXML[i] );
      transMeta.addDatabase( databaseMeta );
    }

    DatabaseMeta dbInfo = transMeta.findDatabase( "db" );

    /* SET UP DATABASE */
    // Create target table
    db = new Database( transMeta, dbInfo );
    db.connect();

    String source = db.getCreateTableStatement( TARGET_TABLE, getTargetTableRowMeta(), null, false, null, true );
    db.execStatement( source );

    // populate target table
    for ( String sql : insertStatement ) {
      db.execStatement( sql );
    }

    /* SET UP TRANSFORMATION STEPS */

    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step...
    String injectorStepName = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepName, im );
    transMeta.addStep( injectorStep );

    // create the update step...
    String updateStepName = "insert/update [" + TARGET_TABLE + "]";
    insupd = new InsertUpdateMeta();
    insupd.setDatabaseMeta( transMeta.findDatabase( "db" ) );
    insupd.setTableName( TARGET_TABLE );

    //FIXME The following commented block does not compile!
    // For now, it's a huge job to make things work again!
/*
    insupd.setUpdateLookup( new String[] { "VALUE", "ROW_ORDER" } );
    insupd.setUpdateStream( new String[] { "VALUE", "ROW_ORDER" } );
    insupd.setUpdate( new Boolean[] { true, false } );
*/
    String fromid = registry.getPluginId( StepPluginType.class, insupd );
    StepMeta updateStep = new StepMeta( fromid, updateStepName, insupd );
    updateStep.setDescription( "insert/update data in table [" + TARGET_TABLE + "] on database [" + dbInfo + "]" );
    transMeta.addStep( updateStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, updateStep );
    transMeta.addTransHop( hi );

    /* PREPARE TRANSFORMATION EXECUTION */

    trans = new Trans( transMeta );
    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( updateStepName, 0 );
    rc = new RowStepCollector();
    si.addRowListener( rc );

    rp = trans.addRowProducer( injectorStepName, 0 );

  }

  @Override
  @After
  public void tearDown() throws Exception {

    /* DROP THE TEST TABLE */

    if ( db != null ) {
      db.execStatement( "DROP TABLE " + TARGET_TABLE + ";" );
      db.disconnect();
    }

    db = null;
    insupd = null;
    trans = null;
    rc = null;
    rp = null;
  }

  public List<RowMetaAndData> createMatchingDataRows() {
    RowMetaInterface rm = getTargetTableRowMeta();
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    list.add( new RowMetaAndData( rm, new Object[] { null, null, "null_id_code_insupd", 2L } ) );
    list.add( new RowMetaAndData( rm, new Object[] { null, 1L, "null_id_insupd", 4L } ) );
    list.add( new RowMetaAndData( rm, new Object[] { 1L, null, "null_code_insupd", 6L } ) );
    list.add( new RowMetaAndData( rm, new Object[] { 2L, 2L, "updated", 8L } ) );

    return list;
  }

  // this method pumps rows to the update step;
  public void pumpMatchingRows() throws Exception {
    pumpRows( createMatchingDataRows() );
  }

  public void pumpRows( List<RowMetaAndData> inputList ) throws Exception {

    trans.startThreads();

    // add rows
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();
    if ( trans.getErrors() > 0 ) {
      fail( "test transformation failed, check logs!" );
    }

  }

  public String[] getDbRows() throws Exception {

    ResultSet rs = db.openQuery( "SELECT VALUE FROM " + TARGET_TABLE + " ORDER BY ROW_ORDER ASC;" );

    ArrayList<String> rows = new ArrayList<String>();

    while ( rs.next() ) {
      rows.add( rs.getString( "VALUE" ) );
    }

    return rows.toArray( new String[0] );

  }

  public void testUpdateEquals() throws Exception {

    addLookup( new String[] { "ID", "=", "ID", "" } );
    pumpMatchingRows();
    String[] rows = getDbRows();

    // now the 1,null and 2,2 record should have been updated
    // the others got inserted
    String[] expected =
    { "null_id_code", "null_id_code_insupd", "null_id", "null_id_insupd", "null_code_insupd", "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateEqualsTwoKeys() throws Exception {

    addLookup( new String[] { "ID", "=", "ID", "" } );
    addLookup( new String[] { "CODE", "=", "CODE", "" } );
    pumpMatchingRows();
    String[] rows = getDbRows();

    // now the 2,2 record should have been updated
    String[] expected =
    {
      "null_id_code", "null_id_code_insupd", "null_id", "null_id_insupd", "null_code", "null_code_insupd",
      "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateEqualsSupportsNull() throws Exception {

    addLookup( new String[] { "ID", "= ~NULL", "ID", "" } );
    pumpMatchingRows();
    String[] rows = getDbRows();

    // now all records should have been updated, the later matching records taking precedence
    String[] expected = { "null_id_insupd", "null_id_insupd", "null_code_insupd", "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateEqualsSupportsNullTwoKeys() throws Exception {

    addLookup( new String[] { "ID", "= ~NULL", "ID", "" } );
    addLookup( new String[] { "CODE", "= ~NULL", "CODE", "" } );

    pumpMatchingRows();
    String[] rows = getDbRows();

    // now all records should have been updated
    String[] expected = { "null_id_code_insupd", "null_id_insupd", "null_code_insupd", "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateEqualsSupportsNullTwoKeysMixed() throws Exception {

    addLookup( new String[] { "ID", "= ~NULL", "ID", "" } );
    addLookup( new String[] { "CODE", "=", "CODE", "" } );

    pumpMatchingRows();
    String[] rows = getDbRows();

    // now [null,1], [2,2] records should have been updated, rest inserted
    String[] expected =
    { "null_id_code", "null_id_code_insupd", "null_id_insupd", "null_code", "null_code_insupd", "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateIsNull() throws Exception {

    addLookup( new String[] { "CODE", "IS NULL", "", "" } );

    pumpMatchingRows();
    String[] rows = getDbRows();

    // now [null, null], [1,null] records should have been updated, last record taking precedence
    String[] expected = { "updated", "null_id", "updated", "non_null_keys" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateIsNotNull() throws Exception {

    addLookup( new String[] { "CODE", "IS NOT NULL", "", "" } );

    pumpMatchingRows();
    String[] rows = getDbRows();

    // now [null, 1], [2,2] records should have been updated, last record taking precedence
    String[] expected = { "null_id_code", "updated", "null_code", "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateBetween() throws Exception {
    addLookup( new String[] { "ID", "BETWEEN", "ID", "CODE" } );

    pumpMatchingRows();
    String[] rows = getDbRows();

    // now [2,2] record should have been updated, rest is inserted
    String[] expected =
    {
      "null_id_code", "null_id_code_insupd", "null_id", "null_id_insupd", "null_code", "null_code_insupd",
      "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

  public void testUpdateEqualsSupportsNullTwoKeysMixed2() throws Exception {

    addLookup( new String[] { "ID", "=", "ID", "" } );
    addLookup( new String[] { "CODE", "= ~NULL", "CODE", "" } );

    pumpMatchingRows();
    String[] rows = getDbRows();

    // now [1,null], [2,2] records should have been updated, rest inserted
    String[] expected =
    { "null_id_code", "null_id_code_insupd", "null_id", "null_id_insupd", "null_code_insupd", "updated" };
    assertArrayEquals( "Unexpected changes by insert/update step", expected, rows );

  }

}
