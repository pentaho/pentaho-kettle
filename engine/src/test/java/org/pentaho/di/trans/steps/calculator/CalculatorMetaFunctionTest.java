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

package org.pentaho.di.trans.steps.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;

public class CalculatorMetaFunctionTest {

  @Test
  public void testEquals() {
    CalculatorMetaFunction meta1 = new CalculatorMetaFunction();
    CalculatorMetaFunction meta2 = (CalculatorMetaFunction) meta1.clone();
    assertNotSame( meta1, meta2 );

    assertFalse( meta1.equals( null ) );
    assertFalse( meta1.equals( new Object() ) );
    assertTrue( meta1.equals( meta2 ) );

    meta2.setCalcType( CalculatorMetaFunction.CALC_ADD_DAYS );
    assertFalse( meta1.equals( meta2 ) );
  }

  @Test
  public void testGetCalcFunctionLongDesc() {
    assertNull( CalculatorMetaFunction.getCalcFunctionLongDesc( Integer.MIN_VALUE ) );
    assertNull( CalculatorMetaFunction.getCalcFunctionLongDesc( Integer.MAX_VALUE ) );
    assertNull( CalculatorMetaFunction.getCalcFunctionLongDesc( CalculatorMetaFunction.calcLongDesc.length ) );
  }

  @Test
  public void testGetCalcFunctionDefaultResultType() {
    assertEquals( ValueMetaInterface.TYPE_NONE,
      CalculatorMetaFunction.getCalcFunctionDefaultResultType( Integer.MIN_VALUE ) );
    assertEquals( ValueMetaInterface.TYPE_NONE,
      CalculatorMetaFunction.getCalcFunctionDefaultResultType( Integer.MAX_VALUE ) );
    assertEquals( ValueMetaInterface.TYPE_NONE,
      CalculatorMetaFunction.getCalcFunctionDefaultResultType( -1 ) );
    assertEquals( ValueMetaInterface.TYPE_STRING,
      CalculatorMetaFunction.getCalcFunctionDefaultResultType( CalculatorMetaFunction.CALC_CONSTANT ) );
    assertEquals( ValueMetaInterface.TYPE_NUMBER,
      CalculatorMetaFunction.getCalcFunctionDefaultResultType( CalculatorMetaFunction.CALC_ADD ) );
  }
}
