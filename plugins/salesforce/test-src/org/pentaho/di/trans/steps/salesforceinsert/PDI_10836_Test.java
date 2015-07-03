/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforceinsert;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import com.sforce.soap.partner.sobject.SObject;

/**
 * Tests for SalesforceInsert step
 *
 * @author Pavel Sakun
 * @see SalesforceInsert
 */
public class PDI_10836_Test {
  private StepMockHelper<SalesforceInsertMeta, SalesforceInsertData> smh;

  @Before
  public void init() {
    smh =
        new StepMockHelper<SalesforceInsertMeta, SalesforceInsertData>( "SalesforceInsert", SalesforceInsertMeta.class,
            SalesforceInsertData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
  }

  @Test
  public void testDateInsert() throws Exception {
    SalesforceInsert step = new SalesforceInsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceInsertMeta meta = smh.initStepMetaInterface;
    doReturn( 2 ).when( meta ).getBatchSizeInt();
    doReturn( new String[] { "Date" } ).when( meta ).getUpdateLookup();
    doReturn( new Boolean[] {false}  ).when( meta ).getUseExternalId();

    SalesforceInsertData data = smh.initStepDataInterface;
    data.nrfields = 1;
    data.fieldnrs = new int[] { 0 };
    data.sfBuffer = new SObject[]{ null };
    data.outputBuffer = new Object[][]{ null };

    step.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMeta valueMeta = new ValueMeta( "date", ValueMetaInterface.TYPE_DATE );
    valueMeta.setDateFormatTimeZone( TimeZone.getTimeZone( "Europe/Minsk" ) );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    Calendar minskTime = Calendar.getInstance( valueMeta.getDateFormatTimeZone() );
    minskTime.clear();
    minskTime.set( 2013, Calendar.OCTOBER, 16 );

    Object[] args = new Object[] { minskTime.getTime() };

    Method m = SalesforceInsert.class.getDeclaredMethod( "writeToSalesForce", Object[].class );
    m.setAccessible( true );
    m.invoke( step, new Object[] { args } );

    DateFormat utc = new SimpleDateFormat( "yyyy-MM-dd" );
    utc.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

    Assert.assertEquals( "2013-10-16", utc.format( data.sfBuffer[0].get_any()[0].getObjectValue() ) );
  }
}
