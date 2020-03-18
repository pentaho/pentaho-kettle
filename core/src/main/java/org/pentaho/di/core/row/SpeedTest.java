/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.StringUtil;

public class SpeedTest {
  private static final Log log = LogFactory.getLog( SpeedTest.class );
  private Object[] rowString10;
  private Object[] rowString100;
  private Object[] rowString1000;

  private Object[] rowMixed10;
  private Object[] rowMixed100;
  private Object[] rowMixed1000;

  private RowMetaInterface metaString10;
  private RowMetaInterface metaMixed10;

  private RowMetaInterface metaString100;
  private RowMetaInterface metaMixed100;

  private RowMetaInterface metaString1000;
  private RowMetaInterface metaMixed1000;

  public SpeedTest() {
    rowString10 = new Object[10];
    rowString100 = new Object[100];
    rowString1000 = new Object[1000];

    rowMixed10 = new Object[50];
    rowMixed100 = new Object[500];
    rowMixed1000 = new Object[5000];

    metaString10 = new RowMeta();
    metaMixed10 = new RowMeta();

    metaString100 = new RowMeta();
    metaMixed100 = new RowMeta();

    metaString1000 = new RowMeta();
    metaMixed1000 = new RowMeta();

    for ( int i = 0; i < 10; i++ ) {
      populateMetaAndData( i, rowString10, metaString10, rowMixed10, metaMixed10 );
    }

    for ( int i = 0; i < 100; i++ ) {
      populateMetaAndData( i, rowString100, metaString100, rowMixed100, metaMixed100 );
    }

    for ( int i = 0; i < 1000; i++ ) {
      populateMetaAndData( i, rowString1000, metaString1000, rowMixed1000, metaMixed1000 );
    }

  }

  private static void populateMetaAndData( int i, Object[] rowString10, RowMetaInterface metaString10,
                                           Object[] rowMixed10, RowMetaInterface metaMixed10 ) {
    String strNamePrefix = "String";
    rowString10[i] = StringUtil.generateRandomString( 20, "", "", false );
    ValueMetaInterface meta = new ValueMetaString( strNamePrefix + ( i + 1 ), 20, 0 );
    metaString10.addValueMeta( meta );

    rowMixed10[i * 5 + 0] = StringUtil.generateRandomString( 20, "", "", false );
    ValueMetaInterface meta0 = new ValueMetaString( strNamePrefix + ( i * 5 + 1 ), 20, 0 );
    metaMixed10.addValueMeta( meta0 );

    rowMixed10[i * 5 + 1] = new Date();
    ValueMetaInterface meta1 = new ValueMetaDate( strNamePrefix + ( i * 5 + 1 ) );
    metaMixed10.addValueMeta( meta1 );

    rowMixed10[i * 5 + 2] = Double.valueOf( Math.random() * 1000000 );
    ValueMetaInterface meta2 = new ValueMetaNumber( strNamePrefix + ( i * 5 + 1 ), 12, 4 );
    metaMixed10.addValueMeta( meta2 );

    rowMixed10[i * 5 + 3] = Long.valueOf( (long) ( Math.random() * 1000000 ) );
    ValueMetaInterface meta3 = new ValueMetaInteger( strNamePrefix + ( i * 5 + 1 ), 8, 0 );
    metaMixed10.addValueMeta( meta3 );

    rowMixed10[i * 5 + 4] = Boolean.valueOf( Math.random() > 0.5 );
    ValueMetaInterface meta4 = new ValueMetaBoolean( strNamePrefix + ( i * 5 + 1 ) );
    metaMixed10.addValueMeta( meta4 );
  }

  public long runTestStrings10( int iterations ) throws KettleValueException {
    long startTime = System.currentTimeMillis();

    for ( int i = 0; i < iterations; i++ ) {
      metaString10.cloneRow( rowString10 );
    }

    long stopTime = System.currentTimeMillis();

    return stopTime - startTime;
  }

  public long runTestMixed10( int iterations ) throws KettleValueException {
    long startTime = System.currentTimeMillis();

    for ( int i = 0; i < iterations; i++ ) {
      metaMixed10.cloneRow( rowMixed10 );
    }

    long stopTime = System.currentTimeMillis();

    return stopTime - startTime;
  }

  public long runTestStrings100( int iterations ) throws KettleValueException {
    long startTime = System.currentTimeMillis();

    for ( int i = 0; i < iterations; i++ ) {
      metaString100.cloneRow( rowString100 );
    }

    long stopTime = System.currentTimeMillis();

    return stopTime - startTime;
  }

  public long runTestMixed100( int iterations ) throws KettleValueException {
    long startTime = System.currentTimeMillis();

    for ( int i = 0; i < iterations; i++ ) {
      metaMixed100.cloneRow( rowMixed100 );
    }

    long stopTime = System.currentTimeMillis();

    return stopTime - startTime;
  }

  public long runTestStrings1000( int iterations ) throws KettleValueException {
    long startTime = System.currentTimeMillis();

    for ( int i = 0; i < iterations; i++ ) {
      metaString1000.cloneRow( rowString1000 );
    }

    long stopTime = System.currentTimeMillis();

    return stopTime - startTime;
  }

  public long runTestMixed1000( int iterations ) throws KettleValueException {
    long startTime = System.currentTimeMillis();

    for ( int i = 0; i < iterations; i++ ) {
      metaMixed1000.cloneRow( rowMixed1000 );
    }

    long stopTime = System.currentTimeMillis();

    return stopTime - startTime;
  }

  public static final int ITERATIONS = 1000000;

  public static void main( String[] args ) throws KettleValueException {
    SpeedTest speedTest = new SpeedTest();

    String strTimesPrefix = " times : ";
    String strMsPrefix = " ms (";
    String strRowPerSec = " r/s)\n";
    // Found a speed boost in adding to a string builder, then logging rather than logging each time.
    StringBuilder runtimeTestMessage = new StringBuilder();

    long timeString10 = speedTest.runTestStrings10( ITERATIONS );
    runtimeTestMessage.append( "\nTime to run 'String10' test "
      + ITERATIONS + strTimesPrefix + timeString10 + strMsPrefix + ( 1000 * ITERATIONS / timeString10 ) + strRowPerSec );
    long timeMixed10 = speedTest.runTestMixed10( ITERATIONS );
    runtimeTestMessage.append( "Time to run 'Mixed10' test "
      + ITERATIONS + strTimesPrefix + timeMixed10 + strMsPrefix + ( 1000 * ITERATIONS / timeMixed10 ) + strRowPerSec );

    long timeString100 = speedTest.runTestStrings100( ITERATIONS );
    runtimeTestMessage.append( "Time to run 'String100' test "
      + ITERATIONS + strTimesPrefix + timeString100 + strMsPrefix + ( 1000 * ITERATIONS / timeString100 ) + strRowPerSec );
    long timeMixed100 = speedTest.runTestMixed100( ITERATIONS );
    runtimeTestMessage.append( "Time to run 'Mixed100' test "
      + ITERATIONS + strTimesPrefix + timeMixed100 + strMsPrefix + ( 1000 * ITERATIONS / timeMixed100 ) + strRowPerSec );

    long timeString1000 = speedTest.runTestStrings1000( ITERATIONS );
    runtimeTestMessage.append( "Time to run 'String1000' test "
      + ITERATIONS + strTimesPrefix + timeString1000 + strMsPrefix + ( 1000 * ITERATIONS / timeString1000 ) + strRowPerSec );
    long timeMixed1000 = speedTest.runTestMixed1000( ITERATIONS );
    runtimeTestMessage.append( "Time to run 'Mixed1000' test "
      + ITERATIONS + strTimesPrefix + timeMixed1000 + strMsPrefix + ( 1000 * ITERATIONS / timeMixed1000 ) + strRowPerSec );

    log.info( runtimeTestMessage );
  }

}
