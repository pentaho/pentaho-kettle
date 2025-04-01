/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.salesforceupdate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.After;
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
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.wsdl.Constants;

public class SalesforceUpdateTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String ACCOUNT_EXT_ID_ACCOUNT_ID_C_ACCOUNT = "Account:ExtID_AccountId__c/Account";
  private static final String ACCOUNT_ID = "AccountId";
  private StepMockHelper<SalesforceUpdateMeta, SalesforceUpdateData> smh;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void setUp() throws Exception {
    smh =
      new StepMockHelper<SalesforceUpdateMeta, SalesforceUpdateData>( "SalesforceUpsert", SalesforceUpdateMeta.class,
        SalesforceUpdateData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      smh.logChannelInterface );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  @Test
  public void testWriteToSalesForceForNullExtIdField_WithExtIdNO() throws Exception {
    SalesforceUpdate sfInputStep =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpdateMeta meta = generateSalesforceUpdateMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpdateData data = generateSalesforceUpdateData();
    sfInputStep.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaString( "AccNoExtId" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    sfInputStep.writeToSalesForce( new Object[] { null } );
    assertEquals( 1, data.sfBuffer[0].getFieldsToNull().length );
    assertEquals( ACCOUNT_ID, data.sfBuffer[0].getFieldsToNull()[0] );
    assertNull( SalesforceConnection.getChildren( data.sfBuffer[0] ) );
  }

  @Test
  public void testWriteToSalesForceForNullExtIdField_WithExtIdYES() throws Exception {
    SalesforceUpdate sfInputStep =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpdateMeta meta =
      generateSalesforceUpdateMeta( new String[] { ACCOUNT_EXT_ID_ACCOUNT_ID_C_ACCOUNT }, new Boolean[] { true } );
    SalesforceUpdateData data = generateSalesforceUpdateData();
    sfInputStep.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaString( "AccExtId" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    sfInputStep.writeToSalesForce( new Object[] { null } );
    assertEquals( 1, data.sfBuffer[0].getFieldsToNull().length );
    assertEquals( ACCOUNT_ID, data.sfBuffer[0].getFieldsToNull()[0] );
    assertNull( SalesforceConnection.getChildren( data.sfBuffer[0] ) );
  }

  @Test
  public void testWriteToSalesForceForNotNullExtIdField_WithExtIdNO() throws Exception {
    SalesforceUpdate sfInputStep =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpdateMeta meta = generateSalesforceUpdateMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpdateData data = generateSalesforceUpdateData();
    sfInputStep.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaString( "AccNoExtId" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    sfInputStep.writeToSalesForce( new Object[] { "001i000001c5Nv9AAE" } );
    assertEquals( 0, data.sfBuffer[0].getFieldsToNull().length );
    assertEquals( 1, SalesforceConnection.getChildren( data.sfBuffer[0] ).length );
    assertEquals( Constants.PARTNER_SOBJECT_NS,
      SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getName().getNamespaceURI() );
    assertEquals( ACCOUNT_ID, SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getName().getLocalPart() );
    assertEquals( "001i000001c5Nv9AAE", SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getValue() );
    assertFalse( SalesforceConnection.getChildren( data.sfBuffer[0] )[0].hasChildren() );
  }

  @Test
  public void testWriteToSalesForceForNotNullExtIdField_WithExtIdYES() throws Exception {
    SalesforceUpdate sfInputStep =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpdateMeta meta =
      generateSalesforceUpdateMeta( new String[] { ACCOUNT_EXT_ID_ACCOUNT_ID_C_ACCOUNT }, new Boolean[] { true } );
    SalesforceUpdateData data = generateSalesforceUpdateData();
    sfInputStep.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaString( "AccExtId" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    sfInputStep.writeToSalesForce( new Object[] { "tkas88" } );
    assertEquals( 0, data.sfBuffer[0].getFieldsToNull().length );
    assertEquals( 1, SalesforceConnection.getChildren( data.sfBuffer[0] ).length );
    assertEquals( Constants.PARTNER_SOBJECT_NS,
      SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getName().getNamespaceURI() );
    assertEquals( "Account", SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getName().getLocalPart() );
    assertNull( SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getValue() );
    assertFalse( SalesforceConnection.getChildren( data.sfBuffer[0] )[0].hasChildren() );
  }

  @Test
  public void testLogMessageInDetailedModeFotWriteToSalesForce() throws KettleException {
    SalesforceUpdate sfInputStep =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpdateMeta meta = generateSalesforceUpdateMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpdateData data = generateSalesforceUpdateData();
    sfInputStep.init( meta, data );
    when( sfInputStep.getLogChannel().isDetailed() ).thenReturn( true );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaString( "AccNoExtId" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    verify( sfInputStep.getLogChannel(), never() ).logDetailed( anyString() );
    sfInputStep.writeToSalesForce( new Object[] { "001i000001c5Nv9AAE" } );
    verify( sfInputStep.getLogChannel() ).logDetailed( "Called writeToSalesForce with 0 out of 2" );
  }

  private SalesforceUpdateData generateSalesforceUpdateData() {
    SalesforceUpdateData data = smh.initStepDataInterface;
    data.nrfields = 1;
    data.fieldnrs = new int[] { 0 };
    data.sfBuffer = new SObject[] { null };
    data.outputBuffer = new Object[][] { null };
    return data;
  }

  private SalesforceUpdateMeta generateSalesforceUpdateMeta( String[] updateLookup, Boolean[] useExternalId ) {
    SalesforceUpdateMeta meta = smh.initStepMetaInterface;
    doReturn( UUID.randomUUID().toString() ).when( meta ).getTargetURL();
    doReturn( UUID.randomUUID().toString() ).when( meta ).getUsername();
    doReturn( UUID.randomUUID().toString() ).when( meta ).getPassword();
    doReturn( UUID.randomUUID().toString() ).when( meta ).getModule();
    doReturn( 2 ).when( meta ).getBatchSizeInt();
    doReturn( updateLookup ).when( meta ).getUpdateLookup();
    doReturn( useExternalId ).when( meta ).getUseExternalId();
    return meta;
  }

  @Test
  public void testModulesAction() {
    SalesforceUpdate salesforceUpdate =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceUpdate.modulesAction( queryParams );
    assert ( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void test_testButtonAction() {
    SalesforceUpdate salesforceUpdate =
      new SalesforceUpdate( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    Map<String, String> queryParams = new HashMap<>();

    JSONObject response = salesforceUpdate.testButtonAction( queryParams );
    assert ( response.containsKey( "connectionStatus" ) );
  }

}
