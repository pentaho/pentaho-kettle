/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforceinput;

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
	
	public SalesforceRecordValue(int index) {
		this.recordIndex=index;
		this.record=null;
		this.recordIndexChanged=false;
		this.allRecordsProcessed=false;
		this.deletionDate=null;
	}
	public boolean isAllRecordsProcessed() {
		return this.allRecordsProcessed;
	}
	public void setAllRecordsProcessed(boolean value) {
		this.allRecordsProcessed=value;
	}
	public boolean isRecordIndexChanges() {
		return this.recordIndexChanged;
	}
	public void setRecordIndexChanges(boolean value) {
		this.recordIndexChanged=value;
	}
	public int getRecordIndex() {
		return this.recordIndex;
	}
	public void setRecordIndex(int index) {
		this.recordIndex=index;
	}
	public SObject getRecordValue() {
		return this.record;
	}
	
	public void setRecordValue(SObject value) {
		this.record=value;
	}
	
	public void setDeletionDate(Date value) {
		this.deletionDate=value;
	}
	public Date getDeletionDate() {
		return this.deletionDate;
	}
}
