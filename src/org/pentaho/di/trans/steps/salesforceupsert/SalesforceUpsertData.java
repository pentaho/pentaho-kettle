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
 
 

package org.pentaho.di.trans.steps.salesforceupsert;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnection;

import com.salesforce.soap.partner.UpsertResult;
import com.salesforce.soap.partner.sobject.SObject;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceUpsertData extends BaseStepData implements StepDataInterface 
{
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;

    public int nrfields;
    public String realURL;
    public String realModule;
    public int[] fieldnrs;

	public SalesforceConnection connection;
	public UpsertResult[] upsertResult;
	
	public SObject[] sfBuffer;
	public Object[][] outputBuffer; 
	public int iBufferPos;
	
	public String realSalesforceFieldName;
    
    
	/**
	 * 
	 */
	public SalesforceUpsertData()
	{
		super();

		nrfields=0;
		
		connection=null;
		realURL=null;
		upsertResult=null;
		realModule=null;
		realSalesforceFieldName=null;
		iBufferPos=0;
	}
}