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

package org.pentaho.di.trans.steps.datagrid;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.test.util.FieldAccessor;

import java.util.List;

import java.util.Arrays;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class DataGrid_EmptyStringVsNull_Test {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    List<Object[]> expected = Arrays.asList(
      new Object[] { "", "", null },
      new Object[] { null, "", null },
      new Object[] { null, "", null }
    );
    doTestEmptyStringVsNull( false, expected );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    List<Object[]> expected = Arrays.asList(
      new Object[] { "", "", null },
      new Object[] { "", "", null },
      new Object[] { null, "", null }
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
    final String stringType = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING );
    final String numberType = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_NUMBER );

    DataGridMeta meta = new DataGridMeta();
    meta.allocate( 3 );
    meta.setFieldName( new String[] { "string", "string-setEmpty", "number" } );
    meta.setFieldType( new String[] { stringType, stringType, numberType } );
    meta.setEmptyString( new boolean[] { false, true, false } );

    List<List<String>> dataRows = Arrays.asList(
      Arrays.asList( " ", " ", " " ),
      Arrays.asList( "", "", "" ),
      Arrays.asList( (String) null, null, null )
    );
    meta.setDataLines( dataRows );

    DataGridData data = new DataGridData();
    DataGrid step = createAndInitStep( meta, data );

    List<Object[]> actual = TransTestingUtil.execute( step, meta, data, 3, true );
    TransTestingUtil.assertResult( expected, actual );
  }

  private DataGrid createAndInitStep( DataGridMeta meta, DataGridData data ) {
    StepMockHelper<DataGridMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( DataGridMeta.class, "DataGrid_EmptyStringVsNull_Test" );
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    DataGrid step = new DataGrid( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }
}
