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

package org.pentaho.di.trans.steps.sort;

import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

/**
 * Test class for the Sort step.
 *
 * @author Sven Boden
 */
public class SortRowsIT {

  static String sortRowsStepname = "sort rows step";
  static Random RN = new Random( new Date().getTime() );

  public static int MAX_COUNT = 1000;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  RowMetaInterface createStringRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "KEY1" ),
      new ValueMetaString( "KEY2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  // field names
  static String KEY1 = "KEY1";
  static String KEY2 = "KEY2";
  static String TMS = "TMS";
  static String INT = "INT";
  static String STR = "STR";
  static String INTG1 = "INTG1";
  static String STRG2 = "STRG2";
  static String CONST = "CONST";

  /**
   * Generate rows for couple of possible combinations
   * <ul>
   * <li>String
   * <li>String (with dup)
   * <li>Timestamp
   * <li>Integer
   * <li>String (CaseSensitive)
   * <li>Integer (pre-sorted) 1 grp
   * <li>String (pre-sorted) 2 grp
   * </ul>
   *
   * @return
   */
  List<RowMetaAndData> createGlobalData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rm = new RowMeta();

    rm.addValueMeta( new ValueMetaString( KEY1 ) );
    rm.addValueMeta( new ValueMetaString( KEY2 ) );
    rm.addValueMeta( new ValueMetaDate( TMS ) );
    rm.addValueMeta( new ValueMetaInteger( INT ) );
    rm.addValueMeta( new ValueMetaString( STR ) );
    rm.addValueMeta( new ValueMetaInteger( INTG1 ) );
    rm.addValueMeta( new ValueMetaString( STRG2 ) );
    rm.addValueMeta( new ValueMetaString( CONST ) );

    long time = new Date().getTime();

    String prevKey = null;

    int counter = 0;
    int group = 0;

    for ( int idx = 0; idx < MAX_COUNT; idx++ ) {
      String key1 = UUID.randomUUID().toString();
      String key2 = null;
      if ( ( idx % 10 == 0 || idx == 11 ) && idx != 0 ) {
        key2 = prevKey;
      } else {
        key2 = UUID.randomUUID().toString();
        prevKey = key2;
      }

      int rand = Math.abs( RN.nextInt() % 10000 );

      Timestamp tms = new Timestamp( time + rand );
      Long igr = new Long( rand );
      String caseSen = TestUtilities.generateString( RN, 10 );

      char ch = (char) ( 65 + counter );
      String gr2 = String.valueOf( ch );

      Object[] row = new Object[] { key1, key2, tms, igr, caseSen, new Long( group ), gr2, "stable" };
      list.add( new RowMetaAndData( rm, row ) );

      if ( counter == 13 ) {
        counter = 0;
        group++;
      } else {
        counter++;
      }
    }
    return list;
  }

  List<RowMetaAndData> createStringData() {
    // Create
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    String old_key1 = null;

    RowMetaInterface rm = createStringRowMetaInterface();

    Random rand = new Random();
    for ( int idx = 0; idx < MAX_COUNT; idx++ ) {
      int key1 = Math.abs( rand.nextInt() % 1000000 );
      int key2 = Math.abs( rand.nextInt() % 1000000 );

      String key1_string = "" + key1 + "." + idx;
      String key2_string = "" + key2 + "." + idx;
      if ( ( ( idx % 100 ) == 0 ) && old_key1 != null ) {
        // have duplicate key1's sometimes
        key1_string = old_key1;
      }
      Object[] r1 = new Object[] { key1_string, key2_string };
      list.add( new RowMetaAndData( rm, r1 ) );
      old_key1 = key1_string;
    }
    return list;
  }

  List<RowMetaAndData> createTimestampData() {
    // Create
    long time = new Date().getTime();
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createStringRowMetaInterface();
    List<ValueMetaInterface> valueMetaList = new ArrayList<ValueMetaInterface>();
    valueMetaList.add( new ValueMetaTimestamp( "KEY1" ) );
    valueMetaList.add( new ValueMetaTimestamp( "KEY2" ) );
    rm.setValueMetaList( valueMetaList );
    Random rand = new Random();
    for ( int idx = 0; idx < MAX_COUNT; idx++ ) {
      int key1 = Math.abs( rand.nextInt() % 10000 );
      int key2 = Math.abs( rand.nextInt() % 10000 );

      Object[] r1 = new Object[] { new Timestamp( time + key1 ), new Timestamp( time + key2 ) };
      list.add( new RowMetaAndData( rm, r1 ) );
    }
    return list;
  }

  /**
   * Check the list, the list has to be sorted.
   */
  void checkStringRows( List<RowMetaAndData> rows, boolean ascending ) throws Exception {
    String prev_key1 = null, prev_key2 = null;
    int idx = 0;

    for ( RowMetaAndData rm : rows ) {
      Object[] r1 = rm.getData();
      RowMetaInterface rmi = rm.getRowMeta();

      String key1 = rmi.getString( r1, "KEY1", "" );
      String key2 = rmi.getString( r1, "KEY2", "" );

      if ( prev_key1 != null && prev_key2 != null ) {
        if ( ascending ) {
          if ( prev_key1.compareTo( key1 ) == 0 ) {
            if ( prev_key2.compareTo( key2 ) > 0 ) {
              fail( "error in sort" );
            }
          } else if ( prev_key1.compareTo( key1 ) > 0 ) {
            fail( "error in sort" );
          }
        } else {
          if ( prev_key1.compareTo( key1 ) == 0 ) {
            if ( prev_key2.compareTo( key2 ) < 0 ) {
              fail( "error in sort" );
            }
          } else if ( prev_key1.compareTo( key1 ) < 0 ) {
            fail( "error in sort" );
          }
        }
      }
      prev_key1 = key1;
      prev_key2 = key2;
      idx++;
    }
    Assert.assertEquals( "less rows returned than expected", MAX_COUNT, idx );
  }

  /**
   * Test case for sorting step .. ascending order on "numeric" data.
   */
  @Test
  public void testSortRows1() throws Exception {

    // Create a sort rows step
    //
    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 10 ) );
    String[] sortFields = { "KEY1", "KEY2" };
    boolean[] ascendingFields = { true, true };
    boolean[] caseSensitive = { true, true };
    boolean[] presortedFields = { false, false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = createStringData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    checkStringRows( ret, true );
  }

  /**
   * Test case for sorting step .. descending order on "numeric" data.
   */
  @Test
  public void testSortRows2() throws Exception {

    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 10 ) );
    String[] sortFields = { "KEY1", "KEY2" };
    boolean[] ascendingFields = { false, false };
    boolean[] caseSensitive = { true, true };
    boolean[] presortedFields = { false, false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = createStringData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    checkStringRows( ret, false );
  }

  /**
   * Test case for sorting step .. ascending order on "timestamp" data.
   */
  @Test
  public void testSortRows3() throws Exception {

    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 10 ) );
    String[] sortFields = { "KEY1", "KEY2" };
    boolean[] ascendingFields = { true, true };
    boolean[] caseSensitive = { true, true };
    boolean[] presortedFields = { false, false };

    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = createTimestampData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    checkStringRows( ret, true );
  }

  /**
   * Test for empty input step does not turn into infinity loop
   *
   * @throws Exception
   */
  @Test( timeout = 4000 )
  public void testSortRowsPresortedNullInput() throws Exception {
    //
    // Create a sort rows step
    //
    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 100 ) );
    String[] sortFields = { "KEY1", "KEY2" };
    boolean[] ascendingFields = { true, true };
    boolean[] caseSensitive = { true, true };
    boolean[] presortedFields = { true, false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = Collections.emptyList();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    Assert.assertTrue( ret.isEmpty() );
  }

  /**
   * Uses 2 fields as a group, sort descending
   *
   * @throws KettleException
   */
  @Test
  public void test2GrouppingSort() throws KettleException {
    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 100 ) );

    boolean asc = false;

    String[] sortFields = { INTG1, CONST, INT };
    boolean[] ascendingFields = { true, true, asc };
    boolean[] caseSensitive = { false, false, false };
    boolean[] presortedFields = { true, true, false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = this.createGlobalData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    Assert.assertEquals( "All rows is processed", MAX_COUNT, ret.size() );
    this.checkGrouppingFieldSort( ret, false );
  }

  /**
   * Test that rows can be sorted with one grouping field
   *
   * @throws KettleException
   */
  @Test
  public void test1GroupingSort() throws KettleException {
    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 100 ) );

    String[] sortFields = { INTG1, INT };
    boolean[] ascendingFields = { true, true };
    boolean[] caseSensitive = { false, false };
    boolean[] presortedFields = { true, false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = this.createGlobalData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    Assert.assertEquals( "All rows is processed", MAX_COUNT, ret.size() );
    this.checkGrouppingFieldSort( ret, true );
  }

  /**
   * Test rows are sorted case sensitive
   *
   * @throws KettleException
   */
  @Test
  public void testSortCaseSensitive() throws KettleException {

    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 100 ) );
    String[] sortFields = { STR };

    boolean caseSen = true;
    boolean asc = true;

    boolean[] ascendingFields = { asc };
    boolean[] caseSensitive = { caseSen };
    boolean[] presortedFields = { false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = this.createGlobalData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );
    Assert.assertEquals( "All rows is processed", MAX_COUNT, ret.size() );
    this.checkStringSortCorrect( ret, caseSen, asc );
  }

  /**
   * Check rows are sorted case insensitive descending.
   *
   * @throws KettleException
   */
  @Test
  public void testStringSortedCaseInsensitive() throws KettleException {
    SortRowsMeta srm = new SortRowsMeta();
    srm.setSortSize( Integer.toString( MAX_COUNT / 100 ) );
    String[] sortFields = { STR };

    boolean caseSen = false;
    boolean asc = false;

    boolean[] ascendingFields = { asc };
    boolean[] caseSensitive = { caseSen };
    boolean[] presortedFields = { false };
    srm.setFieldName( sortFields );
    srm.setAscending( ascendingFields );
    srm.setCaseSensitive( caseSensitive );
    srm.setPreSortedField( presortedFields );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, srm, sortRowsStepname );

    // add rows
    List<RowMetaAndData> inputList = this.createGlobalData();
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, sortRowsStepname,
            TransTestFactory.DUMMY_STEPNAME, inputList );
    Assert.assertEquals( "All rows is processed", MAX_COUNT, ret.size() );
    this.checkStringSortCorrect( ret, caseSen, asc );
  }

  private void checkStringSortCorrect( List<RowMetaAndData> list, boolean caseSensitive, boolean asc ) throws KettleValueException {
    List<String> actual = new ArrayList<String>();
    List<String> expected = new ArrayList<String>();

    String caseSen = caseSensitive ? "case sensitive" : "case unsensitive";

    for ( RowMetaAndData row : list ) {
      String value = row.getString( STR, null );
      if ( !caseSensitive ) {
        expected.add( value.toLowerCase() );
        actual.add( value.toLowerCase() );
      } else {
        expected.add( value );
        actual.add( value );
      }
    }
    if ( asc ) {
      Collections.sort( expected );
    } else {
      // avoid create custorm comparator
      Collections.sort( expected );
      Collections.reverse( expected );
    }

    Assert.assertEquals( "Data is sorted: " + caseSen, expected, actual );
  }

  private void checkGrouppingFieldSort( List<RowMetaAndData> list, boolean asc ) throws KettleValueException {
    Long prev = null;
    List<Long> actual = new ArrayList<Long>();
    List<Long> expected = new ArrayList<Long>();
    for ( RowMetaAndData row : list ) {
      Long group = row.getInteger( INTG1 );
      if ( prev == null ) {
        // first row
        prev = group;
      }
      if ( !prev.equals( group ) ) {
        // group has changed
        // do assertion
        if ( asc ) {
          Collections.sort( expected );
          Assert.assertEquals( "Values under one group properly sorted asc", expected, actual );
        } else {
          Collections.sort( expected );
          Collections.reverse( expected );
          Assert.assertEquals( "Values under one group properly sorted desc", expected, actual );
        }
        actual.clear();
        expected.clear();
      }
      prev = group;

      Long value = row.getInteger( INT );
      actual.add( value );
      expected.add( value );
    }
  }

}
