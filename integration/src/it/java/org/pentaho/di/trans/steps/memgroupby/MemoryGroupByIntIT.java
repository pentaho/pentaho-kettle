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

package org.pentaho.di.trans.steps.memgroupby;

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
import org.pentaho.di.trans.steps.groupby.GroupByMeta;

public class MemoryGroupByIntIT {

  public static final String stepName = "MemoryGroupBY";
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
    KettleEnvironment.init( false );
  }

  RowMetaInterface getTestRowMeta() {
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaString( KEY1 ) );
    rm.addValueMeta( new ValueMetaInteger( KEY2 ) );
    rm.addValueMeta( new ValueMetaNumber( KEY3 ) );
    rm.addValueMeta( new ValueMetaBigNumber( KEY4 ) );
    return rm;
  }

  List<RowMetaAndData> getTestRowMetaAndData( int count, Integer[] nulls ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rm = getTestRowMeta();

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
   * This case tests when the MemoryGroupBy step receives 0 input rows, and also no row meta is in the previous step,
   * whether it throws an error and fails, or ends successfully without error, while passing no output rows.
   * See PDI-12501 for details
   */
  @Test
  public void testMemoryGroupByNoInputData() throws KettleException {
    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.setSubjectField( new String[]{ KEY2 } );
    meta.setAggregateField( new String[]{ OUT1 } );
    meta.setGroupField( new String[]{ KEY1 } );
    meta.setAggregateType( new int[] { GroupByMeta.TYPE_GROUP_CONCAT_COMMA } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName, getTestRowMeta() );
    List<RowMetaAndData> inputList = new ArrayList<RowMetaAndData>();
    List<RowMetaAndData> result = null;
    try {
      result =
        TransTestFactory.executeTestTransformation( transMeta, stepName, inputList );
    } catch ( KettleException e ) {
      Assert.fail();
    }
    Assert.assertNotNull( result );
    Assert.assertEquals( 0, result.size() );
  }

  /**
   * This case tests when the MemoryGroupBy step receives 0 input rows, and also no row meta is in the previous step,
   * whether it throws an error and fails, or ends successfully without error, while passing no output rows.
   * See PDI-15415 for details
   */
  @Test
  public void testMemoryGroupByAlwaysReturnARow() throws KettleException {
    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.setSubjectField( new String[]{ KEY2 } );
    meta.setAggregateField( new String[]{ OUT1 } );
    meta.setGroupField( new String[]{ KEY1 } );
    meta.setAggregateType( new int[] { GroupByMeta.TYPE_GROUP_CONCAT_COMMA } );
    meta.setAlwaysGivingBackOneRow( true );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName, getTestRowMeta() );
    List<RowMetaAndData> inputList = new ArrayList<RowMetaAndData>();
    List<RowMetaAndData> result = null;
    try {
      result =
        TransTestFactory.executeTestTransformation( transMeta, stepName, inputList );
    } catch ( KettleException e ) {
      Assert.fail();
    }
    Assert.assertNotNull( result );
    Assert.assertEquals( 1, result.size() );
    Assert.assertEquals( KEY1, result.get( 0 ).getValueMeta( 0 ).getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 0 ).getType() );
    Assert.assertNull( result.get( 0 ).getString( 0, null ) );
    Assert.assertEquals( OUT1, result.get( 0 ).getValueMeta( 1 ).getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 1 ).getType() );
    Assert.assertNull( result.get( 0 ).getString( 1, null ) );
  }

  /**
   * This case to test calculation value conversion for MemoryGroupBy, see PDI-11530
   * See PDI-11897 for additional details - this case has commented lines which can be real
   * bugs. Uncomment code when PDI-11897 will be fixed some day.
   */
  @Test
  public void testMemoryGroupByNullAggregationsConversion() throws KettleException {
    // this to force null aggregations becomes nulls
    System.getProperties().setProperty( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "Y" );

    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.setSubjectField( new String[] { KEY1, KEY2, KEY3, KEY4 } );
    meta.setAggregateField( new String[] { OUT1, OUT2, OUT3, OUT4 } );
    meta.setGroupField( new String[] { KEY1 } );
    meta.setAggregateType( new int[] { GroupByMeta.TYPE_GROUP_CONCAT_COMMA, GroupByMeta.TYPE_GROUP_SUM,
      GroupByMeta.TYPE_GROUP_SUM, GroupByMeta.TYPE_GROUP_SUM } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData( 2, new Integer[] { 0, 1 } );
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, stepName, inputList );

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
    // TODO fix it for GroupBy or MemoryGroupBy
    /*
     * Assert.assertEquals( "This is a bug of MemoryGroupBy - memory group by returns Double, while" +
     * "GroupBy returns Long. So this test will fail here", 0L, rmd .getData()[2] );
     */

    vmi = rmd.getValueMeta( 3 );
    Assert.assertEquals( "4 is 3 output field", OUT3, vmi.getName() );
    Assert.assertEquals( "And since it was null it become correct 0", 0.0, rmd.getData()[3] );

    // TODO fix Memory Group By. Waiting for clarification.
    /*vmi = rmd.getValueMeta( 4 );
    Assert.assertEquals( "For this case we have silent BigDecimal to Double conversion that can"
        + " lead to loss of accuracy. This is serious issue and we can't just ignore it.", BigDecimal.class, rmd
        .getData()[4].getClass() );
    Assert.assertEquals( "And the last one is null which becomes 0 BigNumber", new BigDecimal( 0 ),
        rmd.getData()[4] );*/
  }

}
