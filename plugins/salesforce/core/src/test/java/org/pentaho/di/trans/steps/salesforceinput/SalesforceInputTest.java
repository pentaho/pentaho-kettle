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


package org.pentaho.di.trans.steps.salesforceinput;

import com.rometools.rome.io.impl.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.lang.reflect.Field;


public class SalesforceInputTest {

  @Test
  public void doConversions() throws Exception {
    StepMeta stepMeta = new StepMeta();
    String name = "test";
    stepMeta.setName( name );
    StepDataInterface stepDataInterface = Mockito.mock( StepDataInterface.class );
    int copyNr = 0;
    TransMeta transMeta = Mockito.mock( TransMeta.class );
    Trans trans = Mockito.mock( Trans.class );
    Mockito.when( transMeta.findStep( Mockito.eq( name ) ) ).thenReturn( stepMeta );
    SalesforceInput salesforceInput = new SalesforceInput( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    SalesforceInputMeta meta = new SalesforceInputMeta();
    SalesforceInputData data = new SalesforceInputData();

    data.outputRowMeta = Mockito.mock( RowMeta.class );
    Mockito.when( data.outputRowMeta.getValueMeta( Mockito.eq( 0 ) ) ).thenReturn( new ValueMetaBinary() );

    data.convertRowMeta = Mockito.mock( RowMeta.class );
    Mockito.when( data.convertRowMeta.getValueMeta( Mockito.eq( 0 ) ) ).thenReturn( new ValueMetaString() );

    Field metaField = salesforceInput.getClass().getDeclaredField( "meta" );
    metaField.setAccessible( true );
    metaField.set( salesforceInput, meta );

    Field dataField = salesforceInput.getClass().getDeclaredField( "data" );
    dataField.setAccessible( true );
    dataField.set( salesforceInput, data );

    Object[] outputRowData = new Object[ 1 ];
    byte[] binary = { 0, 1, 0, 1, 1, 1 };
    salesforceInput.doConversions( outputRowData, 0, new String( Base64.encode( binary ) ) );
    Assert.assertArrayEquals( binary, (byte[]) outputRowData[ 0 ] );

    binary = new byte[ 0 ];
    salesforceInput.doConversions( outputRowData, 0, new String( Base64.encode( binary ) ) );
    Assert.assertArrayEquals( binary, (byte[]) outputRowData[ 0 ] );
  }

}
