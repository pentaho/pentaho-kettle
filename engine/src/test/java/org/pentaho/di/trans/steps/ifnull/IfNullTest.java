/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.ifnull;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.ifnull.IfNullMeta.Fields;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

import junit.framework.Assert;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests for IfNull step
 *
 * @author Ivan Pogodin
 * @see IfNull
 */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class IfNullTest {
  StepMockHelper<IfNullMeta, IfNullData> smh;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void beforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    smh = new StepMockHelper<IfNullMeta, IfNullData>( "Field IfNull processor", IfNullMeta.class, IfNullData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );

  }

  @After
  public void clean() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    smh.cleanUp();
  }

  private RowSet buildInputRowSet( Object... row ) {
    return smh.getMockInputRowSet( new Object[][] { row } );
  }

  private IfNullMeta mockProcessRowMeta() throws KettleStepException {
    IfNullMeta processRowMeta = smh.processRowsStepMetaInterface;
    doReturn( createFields( "null-field", "empty-field", "space-field" ) ).when( processRowMeta ).getFields();
    doReturn( "replace-value" ).when( processRowMeta ).getReplaceAllByValue();
    doCallRealMethod().when( processRowMeta ).getFields( any( RowMetaInterface.class ), anyString(), any(
        RowMetaInterface[].class ), any( StepMeta.class ), any( VariableSpace.class ), any( Repository.class ), any(
            IMetaStore.class ) );
    return processRowMeta;
  }

  private static Fields[] createFields( String... fieldNames ) {
    Fields[] fields = new Fields[fieldNames.length];
    for ( int i = 0; i < fields.length; i++ ) {
      Fields currentField = new Fields();
      currentField.setFieldName( fieldNames[i] );
      fields[i] = currentField;
    }
    return fields;
  }

  private RowMeta buildInputRowMeta( ValueMetaInterface... valueMetaInterface ) {
    RowMeta inputRowMeta = new RowMeta();
    for ( ValueMetaInterface iValuMetaInterface : valueMetaInterface ) {
      inputRowMeta.addValueMeta( iValuMetaInterface );
    }
    return inputRowMeta;
  }

  @Test
  public void testString_emptyIsNull() throws KettleException {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" );
    IfNull step = new IfNull( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.init( smh.initStepMetaInterface, smh.stepDataInterface );
    final RowMeta inputRowMeta = buildInputRowMeta( //
        new ValueMetaString( "some-field" ), //
        new ValueMetaString( "null-field" ), //
        new ValueMetaString( "empty-field" ), //
        new ValueMetaString( "space-field" ), //
        new ValueMetaString( "another-field" ) //
    );
    step.setInputRowMeta( inputRowMeta );

    final Object[] inputRow = new Object[] { "value1", null, "", "    ", "value5" };
    final Object[] expectedRow = new Object[] { "value1", "replace-value", "replace-value", "    ", "value5" };

    step.addRowSetToInputRowSets( buildInputRowSet( inputRow ) );
    step.addRowSetToOutputRowSets( new QueueRowSet() );

    boolean hasMoreRows;
    do {
      hasMoreRows = step.processRow( mockProcessRowMeta(), smh.processRowsStepDataInterface );
    } while ( hasMoreRows );

    RowSet outputRowSet = step.getOutputRowSets().get( 0 );

    assertRowSetMatches( "", expectedRow, outputRowSet );

  }

  @Test
  public void testString_emptyIsNotNull() throws KettleException {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    IfNull step = new IfNull( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.init( smh.initStepMetaInterface, smh.stepDataInterface );
    final RowMeta inputRowMeta = buildInputRowMeta( //
        new ValueMetaString( "some-field" ), //
        new ValueMetaString( "null-field" ), //
        new ValueMetaString( "empty-field" ), //
        new ValueMetaString( "space-field" ), //
        new ValueMetaString( "another-field" ) //
    );
    step.setInputRowMeta( inputRowMeta );

    final Object[] inputRow = new Object[] { "value1", null, "", "    ", "value5" };
    final Object[] expectedRow = new Object[] { "value1", "replace-value", "", "    ", "value5" };

    step.addRowSetToInputRowSets( buildInputRowSet( inputRow ) );
    step.addRowSetToOutputRowSets( new QueueRowSet() );

    boolean hasMoreRows;
    do {
      hasMoreRows = step.processRow( mockProcessRowMeta(), smh.processRowsStepDataInterface );
    } while ( hasMoreRows );

    RowSet outputRowSet = step.getOutputRowSets().get( 0 );

    assertRowSetMatches( "", expectedRow, outputRowSet );

  }

  private void assertRowSetMatches( String msg, Object[] expectedRow, RowSet outputRowSet ) {
    Object[] actualRow = outputRowSet.getRow();
    Assert.assertEquals( msg + ". Output row is of an unexpected length", expectedRow.length, outputRowSet.getRowMeta()
        .size() );

    for ( int i = 0; i < expectedRow.length; i++ ) {
      Assert.assertEquals( msg + ". Unexpected output value at index " + i, expectedRow[i], actualRow[i] );
    }
  }
}
