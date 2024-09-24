/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import com.google.common.collect.Lists;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConditionTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void testNegatedTrueFuncEvaluatesAsFalse() throws Exception {
    String left = "test_filed";
    String right = "test_value";
    int func = Condition.FUNC_TRUE;
    boolean negate = true;

    Condition condition = new Condition( negate, left, func, right, null );
    assertFalse( condition.evaluate( new RowMeta(), new Object[]{ "test" } ) );
  }

  @Test
  public void testPdi13227() throws Exception {
    RowMetaInterface rowMeta1 = new RowMeta();
    rowMeta1.addValueMeta( new ValueMetaNumber( "name1" ) );
    rowMeta1.addValueMeta( new ValueMetaNumber( "name2" ) );
    rowMeta1.addValueMeta( new ValueMetaNumber( "name3" ) );

    RowMetaInterface rowMeta2 = new RowMeta();
    rowMeta2.addValueMeta( new ValueMetaNumber( "name2" ) );
    rowMeta2.addValueMeta( new ValueMetaNumber( "name1" ) );
    rowMeta2.addValueMeta( new ValueMetaNumber( "name3" ) );

    String left = "name1";
    String right = "name3";
    Condition condition = new Condition( left, Condition.FUNC_EQUAL, right, null );

    assertTrue( condition.evaluate( rowMeta1, new Object[] { 1.0, 2.0, 1.0} ) );
    assertTrue( condition.evaluate( rowMeta2, new Object[] { 2.0, 1.0, 1.0} ) );
  }

  @Test
  public void testNullLessThanNumberEvaluatesAsFalse() throws Exception {
    RowMetaInterface rowMeta1 = new RowMeta();
    rowMeta1.addValueMeta( new ValueMetaInteger( "name1" ) );

    String left = "name1";
    ValueMetaAndData right_exact = new ValueMetaAndData( new ValueMetaInteger( "name1" ), new Long( -10 ) );

    Condition condition = new Condition( left, Condition.FUNC_SMALLER, null, right_exact );
    assertTrue( condition.evaluate( rowMeta1, new Object[] { null, "test" } ) );

    condition = new Condition( left, Condition.FUNC_SMALLER_EQUAL, null, right_exact );
    assertTrue( condition.evaluate( rowMeta1, new Object[] { null, "test" } ) );
  }

  @Test
  public void testZeroLargerOrEqualsThanNull() {
    String left = "left";
    String right = "right";

    Long leftValue = 0L;
    Long rightValue = null;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_LARGER_EQUAL, right, null );
    assertTrue( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testZeroLargerThanNull() {
    String left = "left";
    String right = "right";

    Long leftValue = 0L;
    Long rightValue = null;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_LARGER, right, null );
    assertTrue( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testZeroSmallerOrEqualsThanNull() {
    String left = "left";
    String right = "right";

    Long leftValue = 0L;
    Long rightValue = null;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_SMALLER_EQUAL, right, null );
    assertFalse( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testZeroSmallerThanNull() {
    String left = "left";
    String right = "right";

    Long leftValue = 0L;
    Long rightValue = null;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_SMALLER, right, null );
    assertFalse( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testNullLargerOrEqualsThanZero() {
    String left = "left";
    String right = "right";

    Long leftValue = null;
    Long rightValue = 0L;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_LARGER_EQUAL, right, null );
    assertFalse( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testNullLargerThanZero() {
    String left = "left";
    String right = "right";

    Long leftValue = null;
    Long rightValue = 0L;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_LARGER, right, null );
    assertFalse( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testNullSmallerOrEqualsThanZero() {
    String left = "left";
    String right = "right";

    Long leftValue = null;
    Long rightValue = 0L;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_SMALLER_EQUAL, right, null );
    assertTrue( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }

  @Test
  public void testNullSmallerThanZero() {
    String left = "left";
    String right = "right";

    Long leftValue = null;
    Long rightValue = 0L;

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    Condition condition = new Condition( left, Condition.FUNC_SMALLER, right, null );
    assertTrue( condition.evaluate( rowMeta, new Object[] { leftValue, rightValue  } ) );
  }


  /**
   * This test should evaluate the following table result
   *
   * +------+------+-------+-------+-------+-------+
   * |  A   |  B   |  A<B  | A<=B  |  B>A  | B>=A  |
   * +------+------+-------+-------+-------+-------+
   * | null | 1    | true  | true  | true  | true  |
   * | 0    | 1    | true  | true  | true  | true  |
   * | 1    | null | false | false | false | false |
   * | 1    | 0    | false | false | false | false |
   * | null | 0    | true  | true  | true  | true  |
   * | 0    | null | false | false | false | false |
   * | null | -1   | true  | true  | true  | true  |
   * | 0    | -1   | false | false | false | false |
   * | -1   | null | false | false | false | false |
   * | -1   | 0    | true  | true  | true  | true  |
   * +------+------+-------+-------+-------+-------+
   *
   */
  @Test
  public void testFormula() {
    String left = "left";
    String right = "right";

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( left ) );
    rowMeta.addValueMeta( new ValueMetaInteger( right ) );

    List<Long> valuesA = Arrays.asList( null, 0L, 1L, 1L, null, 0L, null, 0L, -1L, -1L );
    List<Long> valuesB = Arrays.asList( 1L, 1L, null, 0L, 0L, null, -1L, -1L, null, 0L );

    List<Boolean> resultsSmallerFunction = Arrays.asList( true, true, false, false, true, false, true, false, false, true );
    List<Boolean> resultsSmallerOrEqualFunction = Arrays.asList( true, true, false, false, true, false, true, false, false, true );
    List<Boolean> resultsLargerFunction = Arrays.asList( true, true, false, false, true, false, true, false, false, true );
    List<Boolean> resultsLargerOrEqualFunction = Arrays.asList( true, true, false, false, true, false, true, false, false, true );

    for ( int i = 0; i < resultsSmallerFunction.size(); i++ ) {
      System.out.print( String.format( "FUNC: A<B; A: %d; B: %d; Expected: %b;", valuesA.get( i ), valuesB.get( i ), resultsSmallerFunction.get( i ) ) );
      Condition condition = new Condition( left, Condition.FUNC_SMALLER, right, null );
      assertEquals( resultsSmallerFunction.get( i ), condition.evaluate( rowMeta, new Object[] { valuesA.get( i ), valuesB.get( i )  } ) );
      System.out.println(" Result: " + condition.evaluate( rowMeta, new Object[] { valuesA.get( i ), valuesB.get( i )  } ) );
    }

    for ( int i = 0; i < resultsSmallerOrEqualFunction.size(); i++ ) {
      System.out.print( String.format( "FUNC: A<=B; A: %d; B: %d; Expected: %b;", valuesA.get( i ), valuesB.get( i ), resultsSmallerOrEqualFunction.get( i ) ) );
      Condition condition = new Condition( left, Condition.FUNC_SMALLER_EQUAL, right, null );
      assertEquals( resultsSmallerOrEqualFunction.get( i ), condition.evaluate( rowMeta, new Object[] { valuesA.get( i ), valuesB.get( i )  } ) );
      System.out.println(" Result: " + condition.evaluate( rowMeta, new Object[] { valuesA.get( i ), valuesB.get( i )  } ) );
    }

    for ( int i = 0; i < resultsLargerFunction.size(); i++ ) {
      System.out.print( String.format( "FUNC: B>A; A: %d; B: %d; Expected: %b;", valuesA.get( i ), valuesB.get( i ), resultsLargerFunction.get( i ) ) );
      Condition condition = new Condition( left, Condition.FUNC_LARGER, right, null );
      assertEquals( resultsLargerFunction.get( i ), condition.evaluate( rowMeta, new Object[] { valuesB.get( i ), valuesA.get( i )  } ) );
      System.out.println(" Result: " + condition.evaluate( rowMeta, new Object[] { valuesB.get( i ), valuesA.get( i )  } ) );
    }

    for ( int i = 0; i < resultsLargerOrEqualFunction.size(); i++ ) {
      System.out.print( String.format( "FUNC: B>=A; A: %d; B: %d; Expected: %b;", valuesA.get( i ), valuesB.get( i ), resultsLargerOrEqualFunction.get( i ) ) );
      Condition condition = new Condition( left, Condition.FUNC_LARGER_EQUAL, right, null );
      assertEquals( resultsLargerOrEqualFunction.get( i ), condition.evaluate( rowMeta, new Object[] { valuesB.get( i ), valuesA.get( i )  } ) );
      System.out.println(" Result: " + condition.evaluate( rowMeta, new Object[] { valuesB.get( i ), valuesA.get( i )  } ) );
    }


  }

}
