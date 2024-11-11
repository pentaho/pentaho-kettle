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


package org.pentaho.di.trans.steps.regexeval;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class RegexEval_EmptyStringVsNull_Test {
  private StepMockHelper<RegexEvalMeta, StepDataInterface> helper;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    helper = StepMockUtil.getStepMockHelper( RegexEvalMeta.class, "RegexEval_EmptyStringVsNull_Test" );
  }

  @After
  public void cleanUp() {
    helper.cleanUp();
  }

  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" );
    List<Object[]> expected = Arrays.asList(
      new Object[] { false, "" },
      new Object[] { false, "" },
      new Object[] { false, null }
    );
    executeAndAssertResults( expected );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    List<Object[]> expected = Arrays.asList(
      new Object[] { false, "" },
      new Object[] { false, "" },
      new Object[] { false, null }
    );
    executeAndAssertResults( expected );
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

  private RegexEval createAndInitStep( RegexEvalMeta meta, RegexEvalData data ) {
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    RegexEval step = new RegexEval( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }
}
