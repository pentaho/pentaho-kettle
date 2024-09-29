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


package org.pentaho.di.trans.steps.salesforceinsert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import com.sforce.ws.bind.XmlObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;

import com.sforce.soap.partner.sobject.SObject;

/**
 * Tests for SalesforceInsert step
 *
 * @author Pavel Sakun
 * @see SalesforceInsert
 */
public class PDI_10836_Test {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private StepMockHelper<SalesforceInsertMeta, SalesforceInsertData> smh;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void init() {
    smh =
        new StepMockHelper<SalesforceInsertMeta, SalesforceInsertData>( "SalesforceInsert", SalesforceInsertMeta.class,
            SalesforceInsertData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  @Test
  public void testDateInsert() throws Exception {
    SalesforceInsert step = new SalesforceInsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceInsertMeta meta = smh.initStepMetaInterface;
    doReturn( UUID.randomUUID().toString() ).when( meta ).getTargetURL();
    doReturn( UUID.randomUUID().toString() ).when( meta ).getUsername();
    doReturn( UUID.randomUUID().toString() ).when( meta ).getPassword();
    doReturn( UUID.randomUUID().toString() ).when( meta ).getModule();
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
    ValueMetaInterface valueMeta = new ValueMetaDate( "date" );
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

    XmlObject xmlObject = SalesforceConnection.getChildren( data.sfBuffer[ 0 ] )[ 0 ];
    Assert.assertEquals( "2013-10-16",
      utc.format( ( (Calendar) xmlObject.getValue() ).getTime() ) );
  }
}
