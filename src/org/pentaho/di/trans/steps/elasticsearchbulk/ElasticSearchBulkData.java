/*************************************************************************************** 
 * Copyright (C) 2011 webdetails.  All rights reserved. 
 * This software was developed by webdetails and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is webdetails.  
 * The Initial Developer is webdetails.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
 
 

package org.pentaho.di.trans.steps.elasticsearchbulk;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;




/*
 * @author webdetails
 * @since 16-02-2011
 */
public class ElasticSearchBulkData extends BaseStepData implements StepDataInterface 
{
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;

	public int nextBufferRowIdx;
	
	public Object[][] inputRowBuffer;
	
	/**
	 * 
	 */
	public ElasticSearchBulkData()
	{
		super();

		nextBufferRowIdx=0;
	}
	
	
}