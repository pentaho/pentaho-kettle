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

package org.pentaho.di.core;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;

public class ConditionTest extends TestCase {

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
    assertFalse( condition.evaluate( rowMeta1, new Object[] { null, "test" } ) );

    condition = new Condition( left, Condition.FUNC_SMALLER_EQUAL, null, right_exact );
    assertFalse( condition.evaluate( rowMeta1, new Object[] { null, "test" } ) );
  }
}
