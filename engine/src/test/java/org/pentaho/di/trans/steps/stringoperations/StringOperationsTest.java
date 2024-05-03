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

package org.pentaho.di.trans.steps.stringoperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * Tests for StringOperations step
 *
 * @author Pavel Sakun
 * @see StringOperations
 */
public class StringOperationsTest {
  private static StepMockHelper<StringOperationsMeta, StringOperationsData> smh;

  @Before
  public void setup() {
    smh =
      new StepMockHelper<>( "StringOperations", StringOperationsMeta.class,
        StringOperationsData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  private RowSet mockInputRowSet() {
    ValueMetaString valueMeta = new ValueMetaString( "Value" );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    valueMeta.setStorageMetadata( new ValueMetaString( "Value" ) );

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( valueMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[][] { { " Value ".getBytes() } } );
    doReturn( inputRowMeta ).when( inputRowSet ).getRowMeta();

    return inputRowSet;
  }

  private StringOperationsMeta mockStepMeta() {
    StringOperationsMeta meta = mock( StringOperationsMeta.class );
    doReturn( new String[] { "Value" } ).when( meta ).getFieldInStream();
    doReturn( new String[] { "" } ).when( meta ).getFieldOutStream();
    doReturn( new String[] { StringOperationsMeta.trimTypeCode[StringOperationsMeta.TRIM_BOTH] } ).when( meta ).getTrimType();
    doReturn( new String[] { StringOperationsMeta.lowerUpperCode[StringOperationsMeta.LOWER_UPPER_NONE] } ).when( meta ).getLowerUpper();
    doReturn( new String[] { StringOperationsMeta.paddingCode[StringOperationsMeta.PADDING_NONE] } ).when( meta ).getPaddingType();
    doReturn( new String[] { "" } ).when( meta ).getPadChar();
    doReturn( new String[] { "" } ).when( meta ).getPadLen();
    doReturn( new String[] { StringOperationsMeta.initCapCode[StringOperationsMeta.INIT_CAP_NO] } ).when( meta ).getInitCap();
    doReturn( new String[] { StringOperationsMeta.maskXMLCode[StringOperationsMeta.MASK_NONE] } ).when( meta ).getMaskXML();
    doReturn( new String[] { StringOperationsMeta.digitsCode[StringOperationsMeta.DIGITS_NONE] } ).when( meta ).getDigits();
    doReturn( new String[] { StringOperationsMeta.removeSpecialCharactersCode[StringOperationsMeta.REMOVE_SPECIAL_CHARACTERS_NONE] } ).when( meta ).getRemoveSpecialCharacters();

    return meta;
  }

  private StringOperationsData mockStepData() {
    return mock( StringOperationsData.class );
  }

  private boolean verifyOutput( Object[][] expectedRows, RowSet outputRowSet ) throws KettleValueException {
    if ( expectedRows.length == outputRowSet.size() ) {
      for ( Object[] expectedRow : expectedRows ) {
        Object[] row = outputRowSet.getRow();
        if ( expectedRow.length == outputRowSet.getRowMeta().size() ) {
          for ( int j = 0; j < expectedRow.length; j++ ) {
            if ( !expectedRow[j].equals( outputRowSet.getRowMeta().getString( row, j ) ) ) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  @Test
  public void testProcessBinaryInput() throws KettleException {
    StringOperations step = new StringOperations( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.addRowSetToInputRowSets( mockInputRowSet() );

    RowSet outputRowSet = new QueueRowSet();
    step.addRowSetToOutputRowSets( outputRowSet );

    StringOperationsMeta meta = mockStepMeta();
    StringOperationsData data = mockStepData();

    step.init( meta, data );

    boolean processResult;

    do {
      processResult = step.processRow( meta, data );
    } while ( processResult );

    Assert.assertTrue( outputRowSet.isDone() );

    Assert.assertTrue( "Unexpected output", verifyOutput( new Object[][] { { "Value" } }, outputRowSet ) );
  }
}
