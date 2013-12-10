/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.sql;

import junit.framework.TestCase;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class IifFunctionTest extends TestCase {
  public void testIifFunction01() throws Exception {

    RowMetaInterface serviceFields = generateTestRowMeta();

    String conditionClause = "B>5000";
    String trueValueString = "'Big'";
    String falseValueString = "'Small'";

    IifFunction function =
      new IifFunction( "Service", conditionClause, trueValueString, falseValueString, serviceFields );
    assertNotNull( function.getSqlCondition() );
    Condition condition = function.getSqlCondition().getCondition();
    assertNotNull( condition );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">", condition.getFunctionDesc() );
    assertEquals( "5000", condition.getRightExactString() );

    // test the value data type determination
    //
    assertNotNull( function.getTrueValue() );
    assertEquals( ValueMetaInterface.TYPE_STRING, function.getTrueValue().getValueMeta().getType() );
    assertEquals( "Big", function.getTrueValue().getValueData() );
    assertNotNull( function.getFalseValue() );
    assertEquals( ValueMetaInterface.TYPE_STRING, function.getFalseValue().getValueMeta().getType() );
    assertEquals( "Small", function.getFalseValue().getValueData() );
  }

  private RowMetaInterface generateTestRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "A", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "B", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    return rowMeta;
  }
}
