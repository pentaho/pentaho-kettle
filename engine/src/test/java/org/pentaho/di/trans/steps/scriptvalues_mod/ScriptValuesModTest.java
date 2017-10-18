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

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.steps.StepMockUtil;

import java.math.BigDecimal;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Andrey Khayrutdinov
 */
public class ScriptValuesModTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void bigNumberAreNotTrimmedToInt() throws Exception {
    ScriptValuesMod step = StepMockUtil.getStep( ScriptValuesMod.class, ScriptValuesMetaMod.class, "test" );

    RowMeta input = new RowMeta();
    input.addValueMeta( new ValueMetaBigNumber( "value_int" ) );
    input.addValueMeta( new ValueMetaBigNumber( "value_double" ) );
    step.setInputRowMeta( input );

    step = spy( step );
    doReturn( new Object[] { BigDecimal.ONE, BigDecimal.ONE } ).when( step ).getRow();

    ScriptValuesMetaMod meta = new ScriptValuesMetaMod();
    meta.setCompatible( false );
    meta.allocate( 2 );
    meta.setFieldname( new String[] { "value_int", "value_double" } );
    meta.setType( new int[] { ValueMetaInterface.TYPE_BIGNUMBER, ValueMetaInterface.TYPE_BIGNUMBER } );
    meta.setReplace( new boolean[] { true, true } );

    meta.setJSScripts( new ScriptValuesScript[] {
      new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "script",
        "value_int = 10.00;\nvalue_double = 10.50" )
    } );

    ScriptValuesModData data = new ScriptValuesModData();
    step.init( meta, data );

    Object[] expectedRow = { BigDecimal.TEN, new BigDecimal( "10.5" ) };
    Object[] row = TransTestingUtil.execute( step, meta, data, 1, false ).get( 0 );
    TransTestingUtil.assertResult( expectedRow, row );
  }

  @Test
  public void variableIsSetInScopeOfStep() throws Exception {
    ScriptValuesMod step = StepMockUtil.getStep( ScriptValuesMod.class, ScriptValuesMetaMod.class, "test" );

    RowMeta input = new RowMeta();
    input.addValueMeta( new ValueMetaString( "str" ) );
    step.setInputRowMeta( input );

    step = spy( step );
    doReturn( new Object[] { "" } ).when( step ).getRow();

    ScriptValuesMetaMod meta = new ScriptValuesMetaMod();
    meta.setCompatible( false );
    meta.allocate( 1 );
    meta.setFieldname( new String[] { "str" } );
    meta.setType( new int[] { ValueMetaInterface.TYPE_STRING } );
    meta.setReplace( new boolean[] { true } );

    meta.setJSScripts( new ScriptValuesScript[] {
      new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "script",
        "setVariable('temp', 'pass', 'r');\nstr = getVariable('temp', 'fail');" )
    } );

    ScriptValuesModData data = new ScriptValuesModData();
    step.init( meta, data );

    Object[] expectedRow = { "pass" };
    Object[] row = TransTestingUtil.execute( step, meta, data, 1, false ).get( 0 );
    TransTestingUtil.assertResult( expectedRow, row );
  }
}
