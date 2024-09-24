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

package org.pentaho.di.trans.steps.denormaliser;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PDI4910_DenormaliserTest {

  private StepMockHelper<DenormaliserMeta, DenormaliserData> mockHelper;

  @Before
  public void init() {
    mockHelper = new StepMockHelper<>( "Denormalizer", DenormaliserMeta.class, DenormaliserData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );
  }

  @After
  public void cleanUp() {
    mockHelper.cleanUp();
  }

  @Test
  public void testDeNormalise() throws Exception {

    // init step data
    DenormaliserData stepData = new DenormaliserData();
    stepData.keyFieldNr = 0;
    stepData.keyValue = new HashMap<>();
    stepData.keyValue.put( "1", Arrays.asList( 0, 1 ) );
    stepData.fieldNameIndex = new int[] { 1, 2 };
    stepData.inputRowMeta = new RowMeta();
    ValueMetaDate outDateField1 = new ValueMetaDate( "date_field[yyyy-MM-dd]" );
    ValueMetaDate outDateField2 = new ValueMetaDate( "date_field[yyyy/MM/dd]" );
    stepData.outputRowMeta = new RowMeta();
    stepData.outputRowMeta.addValueMeta( 0, outDateField1 );
    stepData.outputRowMeta.addValueMeta( 1, outDateField2 );
    stepData.removeNrs = new int[] { };
    stepData.targetResult = new Object[] { null, null };

    // init step meta
    DenormaliserMeta stepMeta = new DenormaliserMeta();
    DenormaliserTargetField[] denormaliserTargetFields = new DenormaliserTargetField[ 2 ];
    DenormaliserTargetField targetField1 = new DenormaliserTargetField();
    DenormaliserTargetField targetField2 = new DenormaliserTargetField();
    targetField1.setTargetFormat( "yyyy-MM-dd" );
    targetField2.setTargetFormat( "yyyy/MM/dd" );
    denormaliserTargetFields[ 0 ] = targetField1;
    denormaliserTargetFields[ 1 ] = targetField2;
    stepMeta.setDenormaliserTargetField( denormaliserTargetFields );

    // init row meta
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( 0, new ValueMetaInteger( "key" ) );
    rowMeta.addValueMeta( 1, new ValueMetaString( "stringDate1" ) );
    rowMeta.addValueMeta( 2, new ValueMetaString( "stringDate2" ) );

    // init row data
    Object[] rowData = new Object[] { 1L, "2000-10-20", "2000/10/20" };

    // init step
    Denormaliser denormaliser = new Denormaliser( mockHelper.stepMeta, stepData,
      0, mockHelper.transMeta, mockHelper.trans );

    // inject step meta
    Field metaField = denormaliser.getClass().getDeclaredField( "meta" );
    Assert.assertNotNull( "Can't find a field 'meta' in class Denormalizer", metaField );
    metaField.setAccessible( true );
    metaField.set( denormaliser, stepMeta );

    // call tested method
    Method deNormalise =
      denormaliser.getClass().getDeclaredMethod( "deNormalise", RowMetaInterface.class, Object[].class );
    Assert.assertNotNull( "Can't find a method 'deNormalise' in class Denormalizer", deNormalise );
    deNormalise.setAccessible( true );
    deNormalise.invoke( denormaliser, rowMeta, rowData );

    // vefiry
    for ( Object res : stepData.targetResult ) {
      Assert.assertNotNull( "Date is null", res );
    }
  }

}
