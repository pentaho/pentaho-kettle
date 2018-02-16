/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.nullif;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.nullif.NullIfMeta.Field;
import org.pentaho.metastore.api.IMetaStore;

/**
 * Tests for NullIf step
 *
 * @author Ivan Pogodin
 * @see NullIf
 */
public class NullIfTest {
  StepMockHelper<NullIfMeta, NullIfData> smh;

  @Before
  public void setUp() {
    smh = new StepMockHelper<NullIfMeta, NullIfData>( "Field NullIf processor", NullIfMeta.class, NullIfData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  private RowSet mockInputRowSet() {
    return smh.getMockInputRowSet( new Object[][] { { "value1", "nullable-value", "value3" } } );
  }

  private NullIfMeta mockProcessRowMeta() throws KettleStepException {
    NullIfMeta processRowMeta = smh.processRowsStepMetaInterface;
    Field[] fields = createArrayWithOneField( "nullable-field", "nullable-value" );
    doReturn( fields ).when( processRowMeta ).getFields();
    doCallRealMethod().when( processRowMeta ).getFields( any( RowMetaInterface.class ), anyString(),
        any( RowMetaInterface[].class ), any( StepMeta.class ), any( VariableSpace.class ), any( Repository.class ),
        any( IMetaStore.class ) );

    return processRowMeta;
  }

  private RowMeta getInputRowMeta() {
    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaString( "some-field" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "nullable-field" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "another-field" ) );

    return inputRowMeta;
  }

  @Test
  public void test() throws KettleException {
    KettleEnvironment.init();

    NullIf step = new NullIf( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.init( smh.initStepMetaInterface, smh.stepDataInterface );
    step.setInputRowMeta( getInputRowMeta() );
    step.addRowSetToInputRowSets( mockInputRowSet() );
    step.addRowSetToOutputRowSets( new QueueRowSet() );

    boolean hasMoreRows;
    do {
      hasMoreRows = step.processRow( mockProcessRowMeta(), smh.processRowsStepDataInterface );
    } while ( hasMoreRows );

    RowSet outputRowSet = step.getOutputRowSets().get( 0 );
    Object[] actualRow = outputRowSet.getRow();
    Object[] expectedRow = new Object[] { "value1", null, "value3" };

    Assert.assertEquals( "Output row is of an unexpected length", expectedRow.length, outputRowSet.getRowMeta().size() );

    for ( int i = 0; i < expectedRow.length; i++ ) {
      Assert.assertEquals( "Unexpected output value at index " + i, expectedRow[i], actualRow[i] );
    }
  }

  private static Field[] createArrayWithOneField( String fieldName, String fieldValue ) {
    Field field = new Field();
    field.setFieldName( fieldName );
    field.setFieldValue( fieldValue );
    return new Field[] { field };
  }

  private RowMeta getInputRowMeta2() {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaDate vmd1 = new ValueMetaDate( "value1" );
    vmd1.setConversionMask( "yyyyMMdd" );
    inputRowMeta.addValueMeta( vmd1 );
    ValueMetaDate vmd2 = new ValueMetaDate( "value2" );
    vmd2.setConversionMask( "yyyy/MM/dd HH:mm:ss.SSS" );
    inputRowMeta.addValueMeta( vmd2 );
    ValueMetaDate vmd3 = new ValueMetaDate( "value3" );
    vmd3.setConversionMask( "yyyyMMdd" );
    inputRowMeta.addValueMeta( vmd3 );
    ValueMetaDate vmd4 = new ValueMetaDate( "value4" );
    vmd4.setConversionMask( "yyyy/MM/dd HH:mm:ss.SSS" );
    inputRowMeta.addValueMeta( vmd4 );

    return inputRowMeta;
  }

  private NullIfMeta mockProcessRowMeta2() throws KettleStepException {
    NullIfMeta processRowMeta = smh.processRowsStepMetaInterface;
    Field[] fields = new Field[4];
    fields[0] = createArrayWithOneField( "value1", "20150606" )[0];
    fields[1] = createArrayWithOneField( "value2", "2015/06/06 00:00:00.000" )[0];
    fields[2] = createArrayWithOneField( "value3", "20150606" )[0];
    fields[3] = createArrayWithOneField( "value4", "2015/06/06 00:00:00.000" )[0];
    doReturn( fields ).when( processRowMeta ).getFields();
    doCallRealMethod().when( processRowMeta ).getFields( any( RowMetaInterface.class ), anyString(),
        any( RowMetaInterface[].class ), any( StepMeta.class ), any( VariableSpace.class ), any( Repository.class ),
        any( IMetaStore.class ) );

    return processRowMeta;
  }

  @Test
  public void testDateWithFormat() throws KettleException {
    KettleEnvironment.init();

    NullIf step = new NullIf( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.init( smh.initStepMetaInterface, smh.stepDataInterface );
    step.setInputRowMeta( getInputRowMeta2() );
    Date d1 = null;
    Date d2 = null;
    Date d3 = null;
    Date d4 = null;
    try {
      DateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );
      d1 = formatter.parse( "20150606" );
      d3 = formatter.parse( "20150607" );
      formatter = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
      d2 = formatter.parse( "2015/06/06 00:00:00.000" );
      d4 = formatter.parse( "2015/07/06 00:00:00.000" );
    } catch ( ParseException e ) {
      e.printStackTrace();
    }
    step.addRowSetToInputRowSets( smh.getMockInputRowSet( new Object[][] { { d1, d2, d3, d4 } } ) );
    step.addRowSetToOutputRowSets( new QueueRowSet() );
    boolean hasMoreRows;
    do {
      hasMoreRows = step.processRow( mockProcessRowMeta2(), smh.processRowsStepDataInterface );
    } while ( hasMoreRows );

    RowSet outputRowSet = step.getOutputRowSets().get( 0 );
    Object[] actualRow = outputRowSet.getRow();
    Object[] expectedRow = new Object[] { null, null, d3, d4 };

    Assert.assertEquals( "Output row is of an unexpected length", expectedRow.length, outputRowSet.getRowMeta().size() );

    for ( int i = 0; i < expectedRow.length; i++ ) {
      Assert.assertEquals( "Unexpected output value at index " + i, expectedRow[i], actualRow[i] );
    }
  }
}
