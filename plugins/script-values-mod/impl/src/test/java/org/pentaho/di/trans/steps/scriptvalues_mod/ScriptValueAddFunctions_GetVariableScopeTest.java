/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
