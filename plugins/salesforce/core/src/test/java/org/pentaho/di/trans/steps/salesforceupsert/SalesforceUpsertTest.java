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

package org.pentaho.di.trans.steps.salesforceupsert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.wsdl.Constants;

import javax.xml.namespace.QName;

public class SalesforceUpsertTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String EXT_ID_ACCOUNT_ID_C = "ExtID_AccountId__c";
  private static final String ACCOUNT_EXT_ID_ACCOUNT_ID_C_ACCOUNT = "Account:" + EXT_ID_ACCOUNT_ID_C + "/Account";
  private static final String ACCOUNT_ID = "AccountId";
  private StepMockHelper<SalesforceUpsertMeta, SalesforceUpsertData> smh;

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
        new StepMockHelper<SalesforceUpsertMeta, SalesforceUpsertData>( "SalesforceUpsert", SalesforceUpsertMeta.class,
            SalesforceUpsertData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  @Test
  public void testWriteToSalesForceForNullExtIdField_WithExtIdNO() throws Exception {
    SalesforceUpsert sfInputStep =
        new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpsertMeta meta = generateSalesforceUpsertMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpsertData data = generateSalesforceUpsertData();
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
    SalesforceUpsert sfInputStep =
        new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpsertMeta meta =
        generateSalesforceUpsertMeta( new String[] { ACCOUNT_EXT_ID_ACCOUNT_ID_C_ACCOUNT }, new Boolean[] { true } );
    SalesforceUpsertData data = generateSalesforceUpsertData();
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
    SalesforceUpsert sfInputStep =
        new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpsertMeta meta = generateSalesforceUpsertMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpsertData data = generateSalesforceUpsertData();
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
    SalesforceUpsert sfInputStep =
        new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpsertMeta meta =
        generateSalesforceUpsertMeta( new String[] { ACCOUNT_EXT_ID_ACCOUNT_ID_C_ACCOUNT }, new Boolean[] { true } );
    SalesforceUpsertData data = generateSalesforceUpsertData();
    sfInputStep.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaString( "AccExtId" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    String extIdValue = "tkas88";
    sfInputStep.writeToSalesForce( new Object[] { extIdValue } );
    assertEquals( 0, data.sfBuffer[0].getFieldsToNull().length );
    assertEquals( 1, SalesforceConnection.getChildren( data.sfBuffer[0] ).length );
    assertEquals( Constants.PARTNER_SOBJECT_NS,
      SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getName().getNamespaceURI() );
    assertEquals( "Account", SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getName().getLocalPart() );
    assertNull( SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getValue() );
    assertEquals( extIdValue, SalesforceConnection.getChildren( data.sfBuffer[0] )[0].getChild( EXT_ID_ACCOUNT_ID_C ).getValue() );
  }

  @Test
  public void testLogMessageInDetailedModeFotWriteToSalesForce() throws KettleException {
    SalesforceUpsert sfInputStep =
        new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpsertMeta meta = generateSalesforceUpsertMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpsertData data = generateSalesforceUpsertData();
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

  private SalesforceUpsertData generateSalesforceUpsertData() {
    SalesforceUpsertData data = smh.initStepDataInterface;
    data.nrfields = 1;
    data.fieldnrs = new int[] { 0 };
    data.sfBuffer = new SObject[] { null };
    data.outputBuffer = new Object[][] { null };
    return data;
  }

  private SalesforceUpsertMeta generateSalesforceUpsertMeta( String[] updateLookup, Boolean[] useExternalId ) {
    SalesforceUpsertMeta meta = smh.initStepMetaInterface;
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
  public void testWriteToSalesForcePentahoIntegerValue() throws Exception {
    SalesforceUpsert sfInputStep =
      new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    SalesforceUpsertMeta meta =
      generateSalesforceUpsertMeta( new String[] { ACCOUNT_ID }, new Boolean[] { false } );
    SalesforceUpsertData data = generateSalesforceUpsertData();
    sfInputStep.init( meta, data );

    RowMeta rowMeta = new RowMeta();
    ValueMetaBase valueMeta = new ValueMetaInteger( "IntValue" );
    rowMeta.addValueMeta( valueMeta );
    smh.initStepDataInterface.inputRowMeta = rowMeta;

    sfInputStep.writeToSalesForce( new Object[] { 1L } );
    XmlObject sObject = data.sfBuffer[ 0 ].getChild( ACCOUNT_ID );
    Assert.assertEquals( sObject.getValue(), 1 );
  }

  @Test
  public void testSetFieldInSObjectForeignKey() throws Exception {
    SalesforceUpsert salesforceUpsert =
      new SalesforceUpsert( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );

    SObject sobjPass = new SObject();
    XmlObject parentObject = new XmlObject();
    String parentParam = "parentParam";
    String parentValue = "parentValue";
    parentObject.setName( new QName( parentParam ) );
    parentObject.setValue( parentValue );

    String child = "child";
    String childParam = "childParam";
    String childValue = "childValue";

    XmlObject childObject = new XmlObject();
    childObject.setName( new QName( child ) );
    childObject.setField( childParam, childValue );

    salesforceUpsert.setFieldInSObject( sobjPass, parentObject );
    salesforceUpsert.setFieldInSObject( sobjPass, childObject );

    Assert.assertEquals( parentValue, sobjPass.getField( parentParam ) );
    Assert.assertEquals( childValue, ( (SObject) sobjPass.getField( child ) ).getField( childParam ) );
  }
}
