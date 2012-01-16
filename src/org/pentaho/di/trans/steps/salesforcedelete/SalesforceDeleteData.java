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