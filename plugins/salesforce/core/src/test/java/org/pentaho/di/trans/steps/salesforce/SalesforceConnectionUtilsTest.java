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
