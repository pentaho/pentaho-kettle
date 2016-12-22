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

package org.pentaho.di.core.row;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RowMetaTest {

  RowMetaInterface rowMeta = new RowMeta();
  ValueMetaInterface string;
  ValueMetaInterface integer;
  ValueMetaInterface date;

  ValueMetaInterface charly;
  ValueMetaInterface dup;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    string = ValueMetaFactory.createValueMeta( "string", ValueMetaInterface.TYPE_STRING );
    rowMeta.addValueMeta( string );
    integer = ValueMetaFactory.createValueMeta( "integer", ValueMetaInterface.TYPE_INTEGER );
    rowMeta.addValueMeta( integer );
    date = ValueMetaFactory.createValueMeta( "date", ValueMetaInterface.TYPE_DATE );
    rowMeta.addValueMeta( date );

    charly = ValueMetaFactory.createValueMeta( "charly", ValueMetaInterface.TYPE_SERIALIZABLE );

    dup = ValueMetaFactory.createValueMeta( "dup", ValueMetaInterface.TYPE_SERIALIZABLE );
  }

  private List<ValueMetaInterface> generateVList( String[] names, int[] types ) throws KettlePluginException {
    List<ValueMetaInterface> list = new ArrayList<ValueMetaInterface>();
    for ( int i = 0; i < names.length; i++ ) {
      ValueMetaInterface vm = ValueMetaFactory.createValueMeta( names[i], types[i] );
      vm.setOrigin( "originStep" );
      list.add( vm );
    }
    return list;
  }

  @Test
  public void testGetValueMetaList() {
    List<ValueMetaInterface> list = rowMeta.getValueMetaList();
    Assert.assertTrue( list.contains( string ) );
    Assert.assertTrue( list.contains( integer ) );
    Assert.assertTrue( list.contains( date ) );
  }

  @Test
  public void testSetValueMetaList() throws KettlePluginException {
    List<ValueMetaInterface> setList = this.generateVList( new String[]{ "alpha", "bravo" }, new int[]{ 2, 2 } );
    rowMeta.setValueMetaList( setList );
    Assert.assertTrue( setList.contains( rowMeta.searchValueMeta( "alpha" ) ) );
    Assert.assertTrue( setList.contains( rowMeta.searchValueMeta( "bravo" ) ) );

    // check that it is avalable by index:
    Assert.assertEquals( 0, rowMeta.indexOfValue( "alpha" ) );
    Assert.assertEquals( 1, rowMeta.indexOfValue( "bravo" ) );
  }

  @Test
  public void testSetValueMetaListNullName() throws KettlePluginException {
    List<ValueMetaInterface> setList = this.generateVList( new String[]{ "alpha", null }, new int[]{ 2, 2 } );
    rowMeta.setValueMetaList( setList );
    Assert.assertTrue( setList.contains( rowMeta.searchValueMeta( "alpha" ) ) );
    Assert.assertFalse( setList.contains( rowMeta.searchValueMeta( null ) ) );

    // check that it is avalable by index:
    Assert.assertEquals( 0, rowMeta.indexOfValue( "alpha" ) );
    Assert.assertEquals( -1, rowMeta.indexOfValue( null ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testDeSynchronizationModifyingOriginalList() {
    // remember 0-based arrays
    int size = rowMeta.size();
    // should be added at the end
    rowMeta.getValueMetaList().add( charly );
    Assert.assertEquals( size, rowMeta.indexOfValue( "charly" ) );
  }

  @Test
  public void testExists() {
    Assert.assertTrue( rowMeta.exists( string ) );
    Assert.assertTrue( rowMeta.exists( date ) );
    Assert.assertTrue( rowMeta.exists( integer ) );
  }

  @Test
  public void testAddValueMetaValueMetaInterface() throws KettlePluginException {
    rowMeta.addValueMeta( charly );
    Assert.assertTrue( rowMeta.getValueMetaList().contains( charly ) );
  }

  @Test
  public void testAddValueMetaNullName() throws KettlePluginException {
    ValueMetaInterface vmi = new ValueMetaBase();
    rowMeta.addValueMeta( vmi );
    Assert.assertTrue( rowMeta.getValueMetaList().contains( vmi ) );
  }

  @Test
  public void testAddValueMetaIntValueMetaInterface() throws KettlePluginException {
    rowMeta.addValueMeta( 1, charly );
    Assert.assertTrue( rowMeta.getValueMetaList().indexOf( charly ) == 1 );
  }

  @Test
  public void testGetValueMeta() {
    // see before method insertion order.
    Assert.assertTrue( rowMeta.getValueMeta( 1 ).equals( integer ) );
  }

  @Test
  public void testSetValueMeta() throws KettlePluginException {
    rowMeta.setValueMeta( 1, charly );
    Assert.assertEquals( 1, rowMeta.getValueMetaList().indexOf( charly ) );
    Assert.assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    Assert.assertEquals( -1, rowMeta.indexOfValue( "integer" ) );
  }

  @Test
  public void testSetValueMetaDup() throws KettlePluginException {
    rowMeta.setValueMeta( 1, dup );
    Assert.assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    Assert.assertEquals( -1, rowMeta.indexOfValue( "integer" ) );

    rowMeta.setValueMeta( 1, dup );
    Assert.assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    Assert.assertEquals( -1, rowMeta.indexOfValue( "integer" ) );

    rowMeta.setValueMeta( 2, dup );
    Assert.assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    Assert.assertEquals( "Original is still the same (object)", 1, rowMeta.getValueMetaList().indexOf( dup ) );
    Assert.assertEquals( "Original is still the same (name)", 1, rowMeta.indexOfValue( "dup" ) );
    Assert.assertEquals( "Renaming happened", 2, rowMeta.indexOfValue( "dup_1" ) );
  }

  @Test
  public void testSetValueMetaNullName() throws KettlePluginException {
    ValueMetaInterface vmi = new ValueMetaBase();
    rowMeta.setValueMeta( 1, vmi );
    Assert.assertEquals( 1, rowMeta.getValueMetaList().indexOf( vmi ) );
    Assert.assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
  }

  @Test
  public void testIndexOfValue() {
    List<ValueMetaInterface> list = rowMeta.getValueMetaList();
    Assert.assertEquals( 0, list.indexOf( string ) );
    Assert.assertEquals( 1, list.indexOf( integer ) );
    Assert.assertEquals( 2, list.indexOf( date ) );
  }

  @Test
  public void testIndexOfNullValue() {
    Assert.assertEquals( -1, rowMeta.indexOfValue( null ) );
  }

  @Test
  public void testSearchValueMeta() {
    ValueMetaInterface vmi = rowMeta.searchValueMeta( "integer" );
    Assert.assertEquals( integer, vmi );
    vmi = rowMeta.searchValueMeta( "string" );
    Assert.assertEquals( string, vmi );
    vmi = rowMeta.searchValueMeta( "date" );
    Assert.assertEquals( date, vmi );
  }

  @Test
  public void testAddRowMeta() throws KettlePluginException {
    List<ValueMetaInterface> list =
      this.generateVList( new String[]{ "alfa", "bravo", "charly", "delta" }, new int[]{ 2, 2, 3, 4 } );
    RowMeta added = new RowMeta();
    added.setValueMetaList( list );
    rowMeta.addRowMeta( added );

    Assert.assertEquals( 7, rowMeta.getValueMetaList().size() );
    Assert.assertEquals( 5, rowMeta.indexOfValue( "charly" ) );
  }

  @Test
  public void testMergeRowMeta() throws KettlePluginException {
    List<ValueMetaInterface> list =
      this.generateVList( new String[]{ "phobos", "demos", "mars" }, new int[]{ 6, 6, 6 } );
    list.add( 1, integer );
    RowMeta toMerge = new RowMeta();
    toMerge.setValueMetaList( list );

    rowMeta.mergeRowMeta( toMerge );
    Assert.assertEquals( 7, rowMeta.size() );

    list = rowMeta.getValueMetaList();
    Assert.assertTrue( list.contains( integer ) );
    ValueMetaInterface found = null;
    for ( ValueMetaInterface vm : list ) {
      if ( vm.getName().equals( "integer_1" ) ) {
        found = vm;
        break;
      }
    }
    Assert.assertNotNull( found );
  }

  @Test
  public void testRemoveValueMetaString() throws KettleValueException {
    rowMeta.removeValueMeta( "string" );
    Assert.assertEquals( 2, rowMeta.size() );
    Assert.assertNotNull( rowMeta.searchValueMeta( "integer" ) );
    Assert.assertTrue( rowMeta.searchValueMeta( "integer" ).getName().equals( "integer" ) );
    Assert.assertNull( rowMeta.searchValueMeta( "string" ) );
  }

  @Test
  public void testRemoveValueMetaInt() {
    rowMeta.removeValueMeta( 1 );
    Assert.assertEquals( 2, rowMeta.size() );
    Assert.assertNotNull( rowMeta.searchValueMeta( "date" ) );
    Assert.assertNotNull( rowMeta.searchValueMeta( "string" ) );
    Assert.assertNull( rowMeta.searchValueMeta( "notExists" ) );
    Assert.assertTrue( rowMeta.searchValueMeta( "date" ).getName().equals( "date" ) );
    Assert.assertNull( rowMeta.searchValueMeta( "integer" ) );
  }

  @Test
  public void testLowerCaseNamesSearch() {
    Assert.assertNotNull( rowMeta.searchValueMeta( "Integer" ) );
    Assert.assertNotNull( rowMeta.searchValueMeta( "string".toUpperCase() ) );
  }

  @Test
  public void testMultipleSameNameInserts() {
    for ( int i = 0; i < 13; i++ ) {
      rowMeta.addValueMeta( integer );
    }
    String resultName = "integer_13";
    Assert.assertTrue( rowMeta.searchValueMeta( resultName ).getName().equals( resultName ) );
  }

  @Test
  public void testExternalValueMetaModification() {
    ValueMetaInterface vmi = rowMeta.searchValueMeta( "string" );
    vmi.setName( "string2" );
    Assert.assertNotNull( rowMeta.searchValueMeta( vmi.getName() ) );
  }

  @Test
  public void testSwapNames() throws KettlePluginException {
    ValueMetaInterface string2 = ValueMetaFactory.createValueMeta( "string2", ValueMetaInterface.TYPE_STRING );
    rowMeta.addValueMeta( string2 );
    Assert.assertSame( string, rowMeta.searchValueMeta( "string" ) );
    Assert.assertSame( string2, rowMeta.searchValueMeta( "string2" ) );
    string.setName( "string2" );
    string2.setName( "string" );
    Assert.assertSame( string2, rowMeta.searchValueMeta( "string" ) );
    Assert.assertSame( string, rowMeta.searchValueMeta( "string2" ) );
  }

  @Test
  public void testCopyRowMetaCacheConstructor() {
    Map<String, Integer> mapping = new HashMap<>();
    mapping.put( "a", 1 );
    List<Integer> needRealClone = new ArrayList<>();
    needRealClone.add( 2 );
    RowMeta.RowMetaCache rowMetaCache = new RowMeta.RowMetaCache( mapping, needRealClone );
    RowMeta.RowMetaCache rowMetaCache2 = new RowMeta.RowMetaCache( rowMetaCache );
    Assert.assertEquals( rowMetaCache.mapping, rowMetaCache2.mapping );
    Assert.assertEquals( rowMetaCache.needRealClone, rowMetaCache2.needRealClone );
    rowMetaCache = new RowMeta.RowMetaCache( mapping, null );
    rowMetaCache2 = new RowMeta.RowMetaCache( rowMetaCache );
    Assert.assertEquals( rowMetaCache.mapping, rowMetaCache2.mapping );
    Assert.assertNull( rowMetaCache2.needRealClone );
  }

  // @Test
  public void hasedRowMetaListFasterWhenSearchByName() throws KettlePluginException {
    rowMeta.clear();

    ValueMetaInterface searchFor = null;
    for ( int i = 0; i < 100000; i++ ) {
      ValueMetaInterface vm =
        ValueMetaFactory.createValueMeta( UUID.randomUUID().toString(), ValueMetaInterface.TYPE_STRING );
      rowMeta.addValueMeta( vm );
      if ( i == 50000 ) {
        searchFor = vm;
      }
    }
    List<ValueMetaInterface> vmList = rowMeta.getValueMetaList();

    // now see how fast we are.
    long start, stop, time1, time2;
    start = System.nanoTime();
    vmList.indexOf( searchFor );
    stop = System.nanoTime();
    time1 = stop - start;

    start = System.nanoTime();
    ValueMetaInterface found = rowMeta.searchValueMeta( searchFor.getName() );
    stop = System.nanoTime();
    Assert.assertEquals( searchFor, found );
    time2 = stop - start;

    // System.out.println( time1 + ", " + time2 );

    Assert.assertTrue( "array search is slower then current implementation : " + "for array list: " + time1
      + ", for hashed rowMeta: " + time2, time1 > time2 );
  }

  // @Test
  public void hashedRowMetaListNotMuchSlowerThenIndexedAccess() throws KettlePluginException {
    rowMeta = new RowMeta();

    // create pre-existed rom meta list
    List<ValueMetaInterface> pre = new ArrayList<ValueMetaInterface>( 100000 );
    for ( int i = 0; i < 100000; i++ ) {
      ValueMetaInterface vm =
        ValueMetaFactory.createValueMeta( UUID.randomUUID().toString(), ValueMetaInterface.TYPE_STRING );
      pre.add( vm );
    }

    // now see how fast we are.

    long start, stop, time1, time2;
    start = System.nanoTime();
    // this is when filling regular array like in prev implementation
    List<ValueMetaInterface> prev = new ArrayList<ValueMetaInterface>();
    for ( ValueMetaInterface item : pre ) {
      prev.add( item );
    }
    stop = System.nanoTime();
    time1 = stop - start;

    start = System.nanoTime();
    for ( ValueMetaInterface item : pre ) {
      rowMeta.addValueMeta( item );
    }
    stop = System.nanoTime();
    time2 = stop - start;

    // ~6 time slower that for original implementation
    System.out.println( time1 + ", " + time2 );

    // let say finally it is not 10 times slower :(
    Assert.assertTrue( "it is not 10 times slower than for original arrayList", time1 * 10 > time2 );
  }

  @Test
  public void testMergeRowMetaWithOriginStep() throws Exception {

    List<ValueMetaInterface> list =
      this.generateVList( new String[]{ "phobos", "demos", "mars" }, new int[]{ 6, 6, 6 } );
    list.add( 1, integer );
    RowMeta toMerge = new RowMeta();
    toMerge.setValueMetaList( list );

    rowMeta.mergeRowMeta( toMerge, "newOriginStep" );
    Assert.assertEquals( 7, rowMeta.size() );

    list = rowMeta.getValueMetaList();
    Assert.assertTrue( list.contains( integer ) );
    ValueMetaInterface found = null;
    ValueMetaInterface other = null;
    for ( ValueMetaInterface vm : list ) {
      if ( vm.getName().equals( "integer_1" ) ) {
        found = vm;
        break;
      } else {
        other = vm;
      }
    }
    Assert.assertNotNull( found );
    Assert.assertEquals( found.getOrigin(), "newOriginStep" );
    Assert.assertNotNull( other );
    Assert.assertEquals( other.getOrigin(), "originStep" );

  }

  @Test
  public void testGetFieldNames() {
    rowMeta.clear();
    fillRowMeta();
    String[] names = rowMeta.getFieldNames();
    Assert.assertEquals( 10, names.length );
    Assert.assertEquals( "sample", names[0] );
    for ( int i = 1; i < names.length; i++ ) {
      Assert.assertEquals( "", names[i] );
    }
  }

  private void fillRowMeta() {
    rowMeta.addValueMeta( 0, new ValueMetaString( "sample" ) );
    for ( int i = 1; i < 10; i++ ) {
      rowMeta.addValueMeta( i, new ValueMetaInteger( null ) );
    }
  }
}
