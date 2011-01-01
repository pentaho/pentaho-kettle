
/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
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
