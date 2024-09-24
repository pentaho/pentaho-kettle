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

package org.pentaho.di.trans.steps.insertupdate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for PDI-3499
 *
 * @author Pavel Sakun
 * @see InsertUpdate
 */
public class PDI3499Test {
  StepMockHelper<InsertUpdateMeta, InsertUpdateData> smh;

  @Before
  public void setUp() {
    smh =
      new StepMockHelper<>( "insertUpdate", InsertUpdateMeta.class,
        InsertUpdateData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  @Test
  public void testDateLazyConversion() throws KettleException {
    Database db = mock( Database.class );
    RowMeta returnRowMeta = new RowMeta();
    doReturn( new Object[] { new Timestamp( System.currentTimeMillis() ) } ).when( db ).getLookup(
        any( PreparedStatement.class ) );
    returnRowMeta.addValueMeta( new ValueMetaDate( "TimeStamp" ) );
    doReturn( returnRowMeta ).when( db ).getReturnRowMeta();

    ValueMetaString storageMetadata = new ValueMetaString( "Date" );
    storageMetadata.setConversionMask( "yyyy-MM-dd" );

    ValueMetaDate valueMeta = new ValueMetaDate( "Date" );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    valueMeta.setStorageMetadata( storageMetadata );

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( valueMeta );

    InsertUpdateMeta stepMeta = smh.processRowsStepMetaInterface;
    InsertUpdateMeta.UpdateField updateField = new InsertUpdateMeta.UpdateField();
    updateField.setUpdate( true );
    InsertUpdateMeta.UpdateField[] updateFields = new InsertUpdateMeta.UpdateField[1];
    updateFields[0] = updateField;
    doReturn(  updateFields  ).when( stepMeta ).getUpdateFields();

    InsertUpdateData stepData = smh.processRowsStepDataInterface;
    stepData.lookupParameterRowMeta = inputRowMeta;
    stepData.db = db;
    stepData.keynrs = stepData.valuenrs = new int[] { 0 };
    stepData.keynrs2 = new int[] { -1 };
    stepData.updateParameterRowMeta = when( mock( RowMeta.class ).size() ).thenReturn( 2 ).getMock();

    InsertUpdate step = new InsertUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    step.setInputRowMeta( inputRowMeta );
    step.addRowSetToInputRowSets( smh.getMockInputRowSet( new Object[] { "2013-12-20".getBytes() } ) );
    step.init( smh.initStepMetaInterface, smh.initStepDataInterface );
    step.first = false;
    step.processRow( stepMeta, stepData );
  }
}
