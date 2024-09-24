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

package org.pentaho.di.trans.steps.salesforce;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

public class SalesforceConnectionUtilsTest {

  @Test
  public void testLookups() {
    assertEquals( SalesforceConnectionUtils.recordsFilterCode.length, SalesforceConnectionUtils.recordsFilterDesc.length );
    assertEquals( SalesforceConnectionUtils.recordsFilterCode[0], SalesforceConnectionUtils.getRecordsFilterCode( -1 ) );
    assertEquals( SalesforceConnectionUtils.recordsFilterCode[0],
      SalesforceConnectionUtils.getRecordsFilterCode( SalesforceConnectionUtils.recordsFilterDesc.length + 1 ) );
    assertEquals( SalesforceConnectionUtils.recordsFilterDesc[0], SalesforceConnectionUtils.getRecordsFilterDesc( -1 ) );
    assertEquals( SalesforceConnectionUtils.recordsFilterDesc[0],
      SalesforceConnectionUtils.getRecordsFilterDesc( SalesforceConnectionUtils.recordsFilterDesc.length + 1 ) );

    assertEquals( 0, SalesforceConnectionUtils.getRecordsFilterByCode( null ) );
    assertEquals( 1, SalesforceConnectionUtils.getRecordsFilterByCode( SalesforceConnectionUtils.recordsFilterCode[1] ) );
    assertEquals( 0, SalesforceConnectionUtils.getRecordsFilterByCode( UUID.randomUUID().toString() ) );
    assertEquals( 0, SalesforceConnectionUtils.getRecordsFilterByDesc( null ) );
    assertEquals( 1, SalesforceConnectionUtils.getRecordsFilterByDesc( SalesforceConnectionUtils.recordsFilterCode[1] ) );
    assertEquals( 0, SalesforceConnectionUtils.getRecordsFilterByDesc( UUID.randomUUID().toString() ) );
  }
}
