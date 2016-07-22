/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Test;

import com.sforce.soap.partner.sobject.SObject;

public class SalesforceRecordValueTest {

  @Test
  public void testClass() {
    SalesforceRecordValue srv = new SalesforceRecordValue( 100 );
    assertEquals( 100, srv.getRecordIndex() );
    assertNull( srv.getRecordValue() );
    assertFalse( srv.isRecordIndexChanges() );
    assertFalse( srv.isAllRecordsProcessed() );
    assertNull( srv.getDeletionDate() );

    srv.setRecordIndex( 120 );
    assertEquals( 120, srv.getRecordIndex() );

    srv.setRecordValue( mock( SObject.class ) );
    assertNotNull( srv.getRecordValue() );

    srv.setAllRecordsProcessed( true );
    assertTrue( srv.isAllRecordsProcessed() );
    srv.setAllRecordsProcessed( false );
    assertFalse( srv.isRecordIndexChanges() );

    srv.setRecordIndexChanges( true );
    assertTrue( srv.isRecordIndexChanges() );
    srv.setRecordIndexChanges( false );
    assertFalse( srv.isRecordIndexChanges() );

    srv.setDeletionDate( new Date() );
    assertNotNull( srv.getDeletionDate() );
  }
}
