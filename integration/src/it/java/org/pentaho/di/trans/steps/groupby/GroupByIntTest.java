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

package org.pentaho.di.trans.steps.groupby;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

/**
 * 'Int' is added just to avoid name conflicts with engine/test_src existing class.
 * 
 */
public class GroupByIntTest {

  public static final String stepName = "GroupBY";
  public static final String KEY1 = "s1";
  public static final String KEY2 = "i2";
  public static final String KEY3 = "n3";
  public static final String KEY4 = "bn4";

  public static final String OUT1 = "out1";
  public static final String OUT2 = "out2";
  public static final String OUT3 = "out3";
  public static final String OUT4 = "out4";

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init();
  }

  List<RowMetaAndData> getTestRowMetaAndData( int count, Integer[] nulls ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaString( KEY1 ) );
    rm.addValueMeta( new ValueMetaInteger( KEY2 ) );
    rm.addValueMeta( new ValueMetaNumber( KEY3 ) );
    rm.addValueMeta( new ValueMetaBigNumber( KEY4 ) );

    Object[] row = new Object[4];
    List<Integer> nullsList = Arrays.asList( nulls );

    for ( int i = 0; i < count; i++ ) {
      if ( nullsList.contains( i ) ) {
        for ( int j = 0; j < row.length; j++ ) {
          row[j] = null;
        }
      } else {
        row[0] = "";
        row[1] = 1L;
        row[2] = 2.0;
        row[3] = new BigDecimal( 3 );
      }
      list.add( new RowMetaAndData( rm, row ) );
    }
    return list;
  }

  /**
   * Since of PDI-10250 fix we replace null value with 0 representation. But not all 0 is the same.
   * We must be correct if it is 0L, or it is double 0, or it is BigDecimal 0. 
   * Not keep attention to java class conversions we can get specific errors on UI :(   * 
   * 
   * @throws KettleException
   */
  @Test
  public void testGroupByNullAggregationsConversion() throws KettleException {
    // this to force null aggregations becomes nulls
    System.getProperties().setProperty( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "Y" );

    GroupByMeta meta = new GroupByMeta();
    meta.setSubjectField( new String[] { KEY1, KEY2, KEY3, KEY4 } );
    meta.setAggregateField( new String[] { OUT1, OUT2, OUT3, OUT4 } );
    meta.setGroupField( new String[] { KEY1 } );
    meta.setAggregateType( new int[] { GroupByMeta.TYPE_GROUP_CONCAT_COMMA, GroupByMeta.TYPE_GROUP_SUM,
      GroupByMeta.TYPE_GROUP_SUM, GroupByMeta.TYPE_GROUP_SUM } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData( 2, new Integer[] { 0, 1 } );
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    Assert.assertNotNull( "At least it is not null", ret );
    Assert.assertEquals( "Ouput is just one row", 1, ret.size() );
    RowMetaAndData rmd = ret.get( 0 );

    // now take a closer look at value meta types we do have at the end:
    ValueMetaInterface vmi = rmd.getValueMeta( 0 );
    Assert.assertEquals( "First is usually is grouping field", ValueMetaInterface.TYPE_STRING, vmi.getType() );
    Assert.assertEquals( "This is key1 field", KEY1, vmi.getName() );

    vmi = rmd.getValueMeta( 1 );
    Assert.assertEquals( "The next value is first aggregation", OUT1, vmi.getName() );
    Assert.assertEquals( "Since it was null String output will be empty string", "", rmd.getData()[1] );

    vmi = rmd.getValueMeta( 2 );
    Assert.assertEquals( "Third field is second output field", OUT2, vmi.getName() );
    Assert.assertEquals( "To keep correct conversion it must be Long object (Integer representation)", 0L, rmd
        .getData()[2] );

    vmi = rmd.getValueMeta( 3 );
    Assert.assertEquals( "4 is 3 output field", OUT3, vmi.getName() );
    Assert.assertEquals( "And since it was null it become correct 0", 0.0, rmd.getData()[3] );

    vmi = rmd.getValueMeta( 4 );
    Assert.assertEquals( "And the last one is null which becomes 0 BigNumber", new BigDecimal( 0 ), rmd.getData()[4] );

  }

}
