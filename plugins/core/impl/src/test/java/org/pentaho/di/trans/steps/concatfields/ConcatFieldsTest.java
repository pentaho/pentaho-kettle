/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.concatfields;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 2/11/14 Time: 11:00 AM
 */
public class ConcatFieldsTest {

  private class ConcatFieldsHandler extends ConcatFields {

    private Object[] row;
    private boolean useSuperHeader = false;

    public ConcatFieldsHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans, boolean useSuperHeader ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
      this.useSuperHeader = useSuperHeader;
    }

    /**
     * In case of getRow, we receive data from previous steps through the input rowset. In case we split the stream, we
     * have to copy the data to the alternate splits: rowsets 1 through n.
     */
    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    public void setRow( Object[] row ) {
      this.row = row;
    }

    @Override
    protected void initOutput() throws KettleException {

    }

    @Override
    protected Object[] putRowFastDataDump( Object[] r ) throws KettleStepException {
      return null;
    }

    @Override
    protected boolean writeHeader() {
      if ( useSuperHeader ) {
        return super.writeHeader();
      }
      return true;
    }

    @Override
    Object[] putRowFromStream( Object[] r ) throws KettleStepException {
      incrementLinesWritten();
      return prepareOutputRow( r );
    }
  }

  private StepMockHelper<ConcatFieldsMetaHandler, ConcatFieldsData> stepMockHelper;
  private TextFileField textFileField = new TextFileField( "Name", 2, "", 10, 20, "", "", "", "" );
  private TextFileField textFileField2 = new TextFileField( "Surname", 2, "", 10, 20, "", "", "", "" );
  private TextFileField[] textFileFields = new TextFileField[] { textFileField, textFileField2 };

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
        new StepMockHelper<ConcatFieldsMetaHandler, ConcatFieldsData>( "CONCAT FIELDS TEST", ConcatFieldsMetaHandler.class,
            ConcatFieldsData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testPrepareOutputRow() throws Exception {
    ConcatFieldsHandler concatFields =
        new ConcatFieldsHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
            stepMockHelper.transMeta, stepMockHelper.trans, false );
    Object[] row = new Object[] { "one", "two" };
    String[] fieldNames = new String[] { "one", "two" };
    concatFields.setRow( row );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( inputRowMeta.size() ).thenReturn( 2 );
    when( inputRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.isRemoveSelectedFields() ).thenReturn( Boolean.TRUE );

    stepMockHelper.processRowsStepMetaInterface.setDoNotOpenNewFileInit( true );
    concatFields.setInputRowMeta( inputRowMeta );
    try {
      concatFields.processRow( stepMockHelper.processRowsStepMetaInterface,
          stepMockHelper.processRowsStepDataInterface );
      concatFields.prepareOutputRow( row );
    } catch ( NullPointerException npe ) {
      fail( "NullPointerException issue PDI-8870 still reproduced " );
    }
  }

  @Test
  public void testWriteHeaderWithoutFields() throws Exception {
    // A test for PDI-17902
    ConcatFieldsHandler concatFields =
        new ConcatFieldsHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
            stepMockHelper.transMeta, stepMockHelper.trans, true );
    Object[] row = new Object[] { "one", "two" };
    concatFields.setRow( row );
    RowMetaInterface inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaString( "one" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "two" ) );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( new TextFileField[0] );
    when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.isRemoveSelectedFields() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.getSeparator() ).thenReturn( ";" );

    doCallRealMethod().when( stepMockHelper.processRowsStepMetaInterface ).setTargetFieldName( any( String.class ) );
    when( stepMockHelper.processRowsStepMetaInterface.getTargetFieldName() ).thenCallRealMethod();

    doCallRealMethod().when( stepMockHelper.processRowsStepMetaInterface ).getFields(
        any( RowMetaInterface.class ), any( String.class ), any(), nullable( StepMeta.class ),
      nullable( VariableSpace.class ), nullable( Repository.class ), nullable( IMetaStore.class ) );

    stepMockHelper.processRowsStepMetaInterface.setTargetFieldName( "target_result" );

    concatFields.setInputRowMeta( inputRowMeta );
    concatFields.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    concatFields.processRow( stepMockHelper.processRowsStepMetaInterface,
        stepMockHelper.processRowsStepDataInterface );
    concatFields.prepareOutputRow( row );

    String headerString =
        new String( ( (ConcatFieldsOutputStream) stepMockHelper.processRowsStepDataInterface.writer ).read() );

    Assert.assertEquals( "one;two", headerString );

  }

  @Test
  public void testPrepareOutputRowWithSplitRows() throws Exception {
    String expected = "Name;Surname1;DATA12;DATA2Name;Surname3;DATA34;DATA45;DATA5";
    processStep( false );

    String result =  new String( ( (ConcatFieldsOutputStream) stepMockHelper.processRowsStepDataInterface.writer ).read() );
    Assert.assertEquals( expected, result );
  }

  @Test
  public void testPrepareOutputRowWithSplitRowsAndHeaderOffsetForSplitRowsAllowed() throws Exception {
    String expected = "Name;Surname1;DATA12;DATA23;DATA3Name;Surname4;DATA45;DATA5";
    processStep( true );

    String result =  new String( ( (ConcatFieldsOutputStream) stepMockHelper.processRowsStepDataInterface.writer ).read() );
    Assert.assertEquals( expected, result );
  }

  private void processStep( boolean enableHeaderOffsetForSplitRows ) throws KettleException {
    List<Object[]> rows = createRowsData();
    ConcatFieldsHandler concatFields = createConcatFieldsHandler( rows );

    String headerOffsetFlag = enableHeaderOffsetForSplitRows ? "Y" : "N";
    concatFields.setVariable( Const.KETTLE_COMPATIBILITY_CONCAT_FIELDS_SPLIT_ROWS_HEADER_OFFSET, headerOffsetFlag );

    concatFields.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    for ( Object[] row : rows ) {
      concatFields.setRow( row );
      concatFields.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
      concatFields.prepareOutputRow( row );
    }
    concatFields.setRow( null );
  }

  private ConcatFieldsHandler createConcatFieldsHandler( List<Object[]> rows ) throws KettleStepException {
    ConcatFieldsHandler concatFields =
      new ConcatFieldsHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans, true );

    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( Boolean.FALSE );
    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.isRemoveSelectedFields() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.isFooterEnabled() ).thenReturn( Boolean.TRUE );
    when( stepMockHelper.processRowsStepMetaInterface.getSeparator() ).thenReturn( ";" );
    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( "-" );
    when( stepMockHelper.processRowsStepMetaInterface.getSplitEvery() ).thenReturn( 4 );

    ValueMetaInterface[] metaWithFieldOptions = getValueMetaInterfaces();
    when( stepMockHelper.processRowsStepMetaInterface.getMetaWithFieldOptions() ).thenReturn( metaWithFieldOptions );

    when( stepMockHelper.processRowsStepMetaInterface.getTargetFieldName() ).thenCallRealMethod();
    doCallRealMethod().when( stepMockHelper.processRowsStepMetaInterface ).setTargetFieldName( any( String.class ) );
    doCallRealMethod().when( stepMockHelper.processRowsStepMetaInterface ).getFields(
      any( RowMetaInterface.class ), any( String.class ), any(), any( StepMeta.class ),
      any( VariableSpace.class ), any( Repository.class ), any( IMetaStore.class ) );

    stepMockHelper.processRowsStepMetaInterface.setTargetFieldName( "target_result" );
    stepMockHelper.processRowsStepMetaInterface.setDoNotOpenNewFileInit( true );

    String[] fieldNames = new String[] { textFileField.getName(), textFileField2.getName() };
    RowSet rowSet = stepMockHelper.getMockInputRowSet( rows );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    concatFields.setInputRowMeta( inputRowMeta );

    when( rowSet.getRowWait( anyInt(), any( TimeUnit.class ) ) ).thenReturn(
      rows.isEmpty() ? null : rows.iterator().next() );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( inputRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( inputRowMeta.size() ).thenReturn( textFileFields.length );

    for ( int i = 0; i < textFileFields.length; i++ ) {
      String name = textFileFields[ i ].getName();
      ValueMetaString valueMetaString = new ValueMetaString( name );
      when( inputRowMeta.getValueMeta( i ) ).thenReturn( valueMetaString );
      when( inputRowMeta.indexOfValue( name ) ).thenReturn( i );
    }

    concatFields.addRowSetToInputRowSets( rowSet );
    concatFields.addRowSetToOutputRowSets( rowSet );
    return concatFields;
  }

  private List<Object[]> createRowsData() {
    List<Object[]> rows = new ArrayList<>();
    for ( long i = 1; i < 6; i++ ) {
      rows.add( new Object[] { i, "DATA" + i } );
    }
    return rows;
  }

  private ValueMetaInterface[] getValueMetaInterfaces() {
    ValueMetaInterface[] metaWithFieldOptions = new ValueMetaInterface[ textFileFields.length ];
    ValueMetaInterface valueMeta1 = new ValueMetaString();
    ValueMetaInterface valueMeta2 = new ValueMetaString();
    metaWithFieldOptions[ 0 ] = valueMeta1;
    metaWithFieldOptions[ 1 ] = valueMeta2;
    return metaWithFieldOptions;
  }

  public class ConcatFieldsMetaHandler extends ConcatFieldsMeta {
    @Override
    protected synchronized ValueMetaInterface[] getMetaWithFieldOptions() {
      return super.getMetaWithFieldOptions();
    }
  }
}
