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
 
 

package org.pentaho.di.trans.steps.salesforcedelete;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnection;

import com.sforce.soap.partner.DeleteResult;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceDeleteData extends BaseStepData implements StepDataInterface 
{
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;

    public String realURL;
    public String realModule;

	public SalesforceConnection connection;
	public DeleteResult[] deleteResult;
	
	public String[] deleteId;
	public Object[][] outputBuffer; 
	public int iBufferPos;
	
	public int indexOfKeyField;

    
	/**
	 * 
	 */
	public SalesforceDeleteData()
	{
		super();

		connection=null;
		realURL=null;
		deleteResult=null;
		realModule=null;
		iBufferPos=0;
		indexOfKeyField=-1;
	}
}