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


package org.pentaho.di.trans.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

public class StepErrorMetaTest {

  @Test
  public void testGetErrorRowMeta() {
    VariableSpace vars = new Variables();
    vars.setVariable( "VarNumberErrors", "nbrErrors" );
    vars.setVariable( "VarErrorDescription", "errorDescription" );
    vars.setVariable( "VarErrorFields", "errorFields" );
    vars.setVariable( "VarErrorCodes", "errorCodes" );
    StepErrorMeta testObject = new StepErrorMeta( vars, new StepMeta(), new StepMeta(),
      "${VarNumberErrors}", "${VarErrorDescription}", "${VarErrorFields}", "${VarErrorCodes}" );
    RowMetaInterface result = testObject.getErrorRowMeta( 10, "some data was bad", "factId", "BAD131" );

    assertNotNull( result );
    assertEquals( 4, result.size() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, result.getValueMeta( 0 ).getType() );
    assertEquals( "nbrErrors", result.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta( 1 ).getType() );
    assertEquals( "errorDescription", result.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta( 2 ).getType() );
    assertEquals( "errorFields", result.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta( 3 ).getType() );
    assertEquals( "errorCodes", result.getValueMeta( 3 ).getName() );
  }
}
