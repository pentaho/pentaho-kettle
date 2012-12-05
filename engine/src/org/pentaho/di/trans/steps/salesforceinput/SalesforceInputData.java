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

import java.util.GregorianCalendar;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceInputData extends BaseStepData implements StepDataInterface 
{
	public int    nr_repeats;
    public long                rownr;
	public Object[] previousRow;
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public int recordcount;
    public int nrfields;
    public boolean limitReached;
    public long limit;
    public String Module;
	// available before we call query more if needed
	public int nrRecords;
	// We use this variable to query more
	// we initialize it each time we call query more
	public int recordIndex;;
	public SalesforceConnection connection;
	public GregorianCalendar startCal;
	public GregorianCalendar endCal;
	public boolean finishedRecord;

	/**
	 * 
	 */
	public SalesforceInputData()
	{
		super();

		nr_repeats=0;
		nrfields=0;
		recordcount=0;
		limitReached=false;
		limit=0;
		nrRecords=0;
		recordIndex=0;
		rownr = 0;	
		
		connection=null;
		startCal=null;
		endCal=null;
	}
}