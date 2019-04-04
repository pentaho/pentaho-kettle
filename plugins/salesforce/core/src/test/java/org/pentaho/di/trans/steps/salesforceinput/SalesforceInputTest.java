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

package org.pentaho.di.trans.steps.salesforceinput;

import com.sun.syndication.io.impl.Base64;
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
