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
