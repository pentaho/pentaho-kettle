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

import java.util.Date;

import com.sforce.soap.partner.sobject.SObject;

/**
 * Store a record from Salesforce extraction.
 *
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceRecordValue {

  private int recordIndex;
  private SObject record;
  private boolean recordIndexChanged;
  private boolean allRecordsProcessed;
  private Date deletionDate;

  public SalesforceRecordValue( int index ) {
    this.recordIndex = index;
    this.record = null;
    this.recordIndexChanged = false;
    this.allRecordsProcessed = false;
    this.deletionDate = null;
  }

  public boolean isAllRecordsProcessed() {
    return this.allRecordsProcessed;
  }

  public void setAllRecordsProcessed( boolean value ) {
    this.allRecordsProcessed = value;
  }

  public boolean isRecordIndexChanges() {
    return this.recordIndexChanged;
  }

  public void setRecordIndexChanges( boolean value ) {
    this.recordIndexChanged = value;
  }

  public int getRecordIndex() {
    return this.recordIndex;
  }

  public void setRecordIndex( int index ) {
    this.recordIndex = index;
  }

  public SObject getRecordValue() {
    return this.record;
  }

  public void setRecordValue( SObject value ) {
    this.record = value;
  }

  public void setDeletionDate( Date value ) {
    this.deletionDate = value;
  }

  public Date getDeletionDate() {
    return this.deletionDate;
  }
}
