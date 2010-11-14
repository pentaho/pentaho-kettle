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