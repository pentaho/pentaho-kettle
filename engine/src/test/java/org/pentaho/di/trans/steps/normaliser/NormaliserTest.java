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
package org.pentaho.di.trans.steps.normaliser;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NormaliserTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init( false );
  }

  private NormaliserMeta.NormaliserField[] getTestNormaliserFieldsWiki() {
    NormaliserMeta.NormaliserField[] rtn = new NormaliserMeta.NormaliserField[6];
    rtn[0] = new NormaliserMeta.NormaliserField();
    rtn[0].setName( "pr_sl" );
    rtn[0].setNorm( "Product Sales" );
    rtn[0].setValue( "Product1" ); // Type

    rtn[1] = new NormaliserMeta.NormaliserField();
    rtn[1].setName( "pr1_nr" );
    rtn[1].setNorm( "Product Number" );
    rtn[1].setValue( "Product1" );

    rtn[2] = new NormaliserMeta.NormaliserField();
    rtn[2].setName( "pr2_sl" );
    rtn[2].setNorm( "Product Sales" );
    rtn[2].setValue( "Product2" );

    rtn[3] = new NormaliserMeta.NormaliserField();
    rtn[3].setName( "pr2_nr" );
    rtn[3].setNorm( "Product Number" );
    rtn[3].setValue( "Product2" );

    rtn[4] = new NormaliserMeta.NormaliserField();
    rtn[4].setName( "pr3_sl" );
    rtn[4].setNorm( "Product Sales" );
    rtn[4].setValue( "Product3" );

    rtn[5] = new NormaliserMeta.NormaliserField();
    rtn[5].setName( "pr3_nr" );
    rtn[5].setNorm( "Product Number" );
    rtn[5].setValue( "Product3" );

    return rtn;
  }

  private List<RowMetaAndData> getExpectedWikiOutputRowMetaAndData() {
    final Date theDate = new Date( 103, 01, 01 );
    List<RowMetaAndData> list = new ArrayList<>();
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaDate( "DATE" ) );
    rm.addValueMeta( new ValueMetaString( "Type" ) );
    rm.addValueMeta( new ValueMetaInteger( "Product Sales" ) );
    rm.addValueMeta( new ValueMetaInteger( "Product Number" ) );
    Object[] row = new Object[4];
    row[0] = theDate;
    row[1] = "Product1";
    row[2] = 100;
    row[3] = 5;
    list.add( new RowMetaAndData( rm, row ) );

    row = new Object[4];
    row[0] = theDate;
    row[1] = "Product2";
    row[2] = 250;
    row[3] = 10;
    list.add( new RowMetaAndData( rm, row ) );

    row = new Object[4];
    row[0] = theDate;
    row[1] = "Product3";
    row[2] = 150;
    row[3] = 4;
    list.add( new RowMetaAndData( rm, row ) );
    return list;
  }


  private List<RowMetaAndData> getWikiInputRowMetaAndData() {
    List<RowMetaAndData> list = new ArrayList<>();
    Object[] row = new Object[7];
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaDate( "DATE" ) );
    row[0] = new Date( 103, 01, 01 );
    rm.addValueMeta( new ValueMetaInteger( "PR1_NR" ) );
    row[1] = 5;
    rm.addValueMeta( new ValueMetaInteger( "PR_SL" ) );
    row[2] = 100;
    rm.addValueMeta( new ValueMetaInteger( "PR2_NR" ) );
    row[3] = 10;
    rm.addValueMeta( new ValueMetaInteger( "PR2_SL" ) );
    row[4] = 250;
    rm.addValueMeta( new ValueMetaInteger( "PR3_NR" ) );
    row[5] = 4;
    rm.addValueMeta( new ValueMetaInteger( "PR3_SL" ) );
    row[6] = 150;
    list.add( new RowMetaAndData( rm, row ) );
    return list;
  }

  private void checkResults( List<RowMetaAndData> expectedOutput, List<RowMetaAndData> outputList ) {
    assertEquals( expectedOutput.size(), outputList.size() );
    for ( int i = 0; i < outputList.size(); i++ ) {
      RowMetaAndData aRowMetaAndData = outputList.get( i );
      RowMetaAndData expectedRowMetaAndData = expectedOutput.get( i );
      RowMetaInterface rowMeta = aRowMetaAndData.getRowMeta();
      RowMetaInterface expectedRowMeta = expectedRowMetaAndData.getRowMeta();
      String[] fields = rowMeta.getFieldNames();
      String[] expectedFields = expectedRowMeta.getFieldNames();
      assertEquals( expectedFields.length, fields.length );
      assertArrayEquals( expectedFields, fields );
      Object[] aRow = aRowMetaAndData.getData();
      Object[] expectedRow = expectedRowMetaAndData.getData();
      assertEquals( expectedRow.length, aRow.length );
      assertArrayEquals( expectedRow, aRow );
    }
  }

  @Test
  public void testNormaliserProcessRowsWikiData() throws Exception {
    // We should have 1 row as input to the normaliser and 3 rows as output to the normaliser with the data
    // shown on the Wiki page: http://wiki.pentaho.com/display/EAI/Row+Normaliser
    //
    //
    // Data input looks like this:
    //
    // DATE     PR1_NR  PR_SL PR2_NR  PR2_SL  PR3_NR  PR3_SL
    // 2003010  5       100   10      250     4       150
    //
    // Data output looks like this:
    //
    // DATE     Type      Product Sales Product Number
    // 2003010  Product1  100           5
    // 2003010  Product2  250           10
    // 2003010  Product3  150           4
    //


    final String stepName = "Row Normaliser";
    NormaliserMeta stepMeta = new NormaliserMeta();
    stepMeta.setDefault();
    stepMeta.setNormaliserFields( getTestNormaliserFieldsWiki() );
    stepMeta.setTypeField( "Type" );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, stepName );
    List<RowMetaAndData> inputList = getWikiInputRowMetaAndData();
    List<RowMetaAndData> outputList = TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName, TransTestFactory.DUMMY_STEPNAME, inputList );
    List<RowMetaAndData> expectedOutput = this.getExpectedWikiOutputRowMetaAndData();
    checkResults( expectedOutput, outputList );
  }

}
