/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
