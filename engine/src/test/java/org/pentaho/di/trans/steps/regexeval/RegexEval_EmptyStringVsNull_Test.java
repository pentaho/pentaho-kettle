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

package org.pentaho.di.trans.steps.regexeval;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.test.util.FieldAccessor;

import java.util.List;
import java.util.regex.Pattern;

import java.util.Arrays;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class RegexEval_EmptyStringVsNull_Test {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    List<Object[]> expected = Arrays.asList(
      new Object[] { false, "" },
      new Object[] { false, "" },
      new Object[] { false, null }
    );
    doTestEmptyStringVsNull( false, expected );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    List<Object[]> expected = Arrays.asList(
      new Object[] { false, "" },
      new Object[] { false, "" },
      new Object[] { false, null }
    );
    doTestEmptyStringVsNull( true, expected );
  }


  private void doTestEmptyStringVsNull( boolean diffProperty, List<Object[]> expected ) throws Exception {
    FieldAccessor.ensureEmptyStringIsNotNull( diffProperty );
    try {
      executeAndAssertResults( expected );
    } finally {
      FieldAccessor.resetEmptyStringIsNotNull();
    }
  }

  private void executeAndAssertResults( List<Object[]> expected ) throws Exception {
    RegexEvalMeta meta = new RegexEvalMeta();
    meta.allocate( 2 );
    meta.getFieldName()[ 0 ] = "string";
    meta.getFieldName()[ 1 ] = "matcher";
    meta.setFieldType( new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING } );
    meta.setResultFieldName( "string" );
    meta.setReplacefields( true );
    meta.setMatcher( "matcher" );
    meta.setAllowCaptureGroupsFlag( true );

    RegexEvalData data = new RegexEvalData();
    RegexEval step = createAndInitStep( meta, data );

    RowMeta input = new RowMeta();
    input.addValueMeta( new ValueMetaString( "string" ) );
    input.addValueMeta( new ValueMetaString( "matcher" ) );
    step.setInputRowMeta( input );

    step = spy( step );
    doReturn( new String[] { " ", " " } )
      .doReturn( new String[] { "", "" } )
      .doReturn( new String[] { null, null } )
      .when( step ).getRow();

    // dummy pattern, just to contain two groups
    // needed to activate a branch with conversion from string
    data.pattern = Pattern.compile( "(a)(a)" );

    List<Object[]> actual = TransTestingUtil.execute( step, meta, data, 3, false );
    TransTestingUtil.assertResult( expected, actual );
  }

  private RegexEval createAndInitStep( RegexEvalMeta meta, RegexEvalData data ) throws Exception {
    StepMockHelper<RegexEvalMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( RegexEvalMeta.class, "RegexEval_EmptyStringVsNull_Test" );
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    RegexEval step = new RegexEval( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }
}
