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
