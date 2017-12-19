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

package org.pentaho.di.trans.steps.fieldsplitter;

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

import java.util.Arrays;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class FieldSplitter_EmptyStringVsNull_Test {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    List<Object[]> expected = Arrays.asList(
      new Object[] { "a", "", "a" },
      new Object[] { "b", null, "b" },
      new Object[] { null }
    );
    doTestEmptyStringVsNull( false, expected );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    List<Object[]> expected = Arrays.asList(
      new Object[] { "a", "", "a" },
      new Object[] { "b", "", "b" },
      new Object[] { null }
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
    FieldSplitterMeta meta = new FieldSplitterMeta();
    meta.allocate( 3 );
    meta.setFieldName( new String[] { "s1", "s2", "s3" } );
    meta.setFieldType( new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING } );
    meta.setSplitField( "string" );
    meta.setDelimiter( "," );

    FieldSplitterData data = new FieldSplitterData();

    FieldSplitter step = createAndInitStep( meta, data );

    RowMeta input = new RowMeta();
    input.addValueMeta( new ValueMetaString( "string" ) );
    step.setInputRowMeta( input );

    step = spy( step );
    doReturn( new String[] { "a, ,a" } )
      .doReturn( new String[] { "b,,b" } )
      .doReturn( new String[] { null } )
      .when( step ).getRow();

    List<Object[]> actual = TransTestingUtil.execute( step, meta, data, 3, false );
    TransTestingUtil.assertResult( expected, actual );
  }

  private FieldSplitter createAndInitStep( FieldSplitterMeta meta, FieldSplitterData data ) throws Exception {
    StepMockHelper<FieldSplitterMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( FieldSplitterMeta.class, "FieldSplitter_EmptyStringVsNull_Test" );
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    FieldSplitter step = new FieldSplitter( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }
}
