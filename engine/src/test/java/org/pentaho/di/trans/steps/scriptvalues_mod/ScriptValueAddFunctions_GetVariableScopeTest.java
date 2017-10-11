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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.junit.Test;
import org.mozilla.javascript.EvaluatorException;

import static org.junit.Assert.assertEquals;

public class ScriptValueAddFunctions_GetVariableScopeTest {

  @Test
  public void getSystemVariableScope() {
    assertEquals( ScriptValuesAddedFunctions.getVariableScope( "s" ), ScriptValuesAddedFunctions.VariableScope.SYSTEM );
  }

  @Test
  public void getRootVariableScope() {
    assertEquals( ScriptValuesAddedFunctions.getVariableScope( "r" ), ScriptValuesAddedFunctions.VariableScope.ROOT );
  }

  @Test
  public void getParentVariableScope() {
    assertEquals( ScriptValuesAddedFunctions.getVariableScope( "p" ), ScriptValuesAddedFunctions.VariableScope.PARENT );
  }

  @Test
  public void getGrandParentVariableScope() {
    assertEquals( ScriptValuesAddedFunctions.getVariableScope( "g" ),
      ScriptValuesAddedFunctions.VariableScope.GRAND_PARENT );
  }

  @Test( expected = EvaluatorException.class )
  public void getNonExistingVariableScope() {
    ScriptValuesAddedFunctions.getVariableScope( "dummy" );
  }
}
