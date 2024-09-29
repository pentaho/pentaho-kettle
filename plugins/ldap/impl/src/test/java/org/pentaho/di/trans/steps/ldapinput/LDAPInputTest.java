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


package org.pentaho.di.trans.steps.ldapinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * Tests LDAP Input Step
 *
 * @author nhudak
 */
public class LDAPInputTest {
  private static StepMockHelper<LDAPInputMeta, LDAPInputData> stepMockHelper;

  @BeforeClass
  public static void setup() {
    stepMockHelper =
      new StepMockHelper<LDAPInputMeta, LDAPInputData>(
        "LDAP INPUT TEST", LDAPInputMeta.class, LDAPInputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  private LDAPInputMeta mockMeta() {
    LDAPInputMeta meta = mock( LDAPInputMeta.class );
    return meta;
  }

  @AfterClass
  public static void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testRowProcessing() throws Exception {
    //Setup step
    LDAPInput ldapInput = new LDAPInput(
      stepMockHelper.stepMeta, stepMockHelper.stepDataInterface,
      0, stepMockHelper.transMeta, stepMockHelper.trans );
    LDAPInputData data = new LDAPInputData();
    LDAPInputMeta meta = mockMeta();

    //Mock fields
    LDAPInputField[] fields = new LDAPInputField[] {
      new LDAPInputField( "dn" ),
      new LDAPInputField( "cn" ),
      new LDAPInputField( "role" )
    };
    int sortedField = 1;
    fields[sortedField].setSortedKey( true );
    when( meta.getInputFields() ).thenReturn( fields );

    //Mock LDAP Connection
    when( meta.getProtocol() ).thenReturn( LdapMockProtocol.getName() );
    when( meta.getHost() ).thenReturn( "host.mock" );
    when( meta.getDerefAliases() ).thenReturn( "never" );
    when( meta.getReferrals() ).thenReturn( "ignore" );
    LdapMockProtocol.setup();

    try {
      //Run Initialization
      assertTrue( "Input Initialization Failed", ldapInput.init( meta, data ) );

      //Verify
      assertEquals( "Field not marked as sorted", 1, data.connection.getSortingAttributes().size() );
      assertEquals( "Field not marked as sorted", data.attrReturned[sortedField], data.connection.getSortingAttributes().get( 0 ) );
      assertNotNull( data.attrReturned[sortedField] );
    } finally {
      LdapMockProtocol.cleanup();
    }
  }
}
