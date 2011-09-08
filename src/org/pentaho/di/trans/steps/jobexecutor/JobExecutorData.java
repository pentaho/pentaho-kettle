 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.jobexecutor;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class JobExecutorData extends BaseStepData implements StepDataInterface
{
  public Job executorJob;
  public JobMeta executorJobMeta;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface executionResultsOutputRowMeta;
  public RowMetaInterface resultRowsOutputRowMeta;
  public RowMetaInterface resultFilesOutputRowMeta;

  
  public List<RowMetaAndData> groupBuffer;
  public int groupSize;
  public int groupTime;
  public long groupTimeStart;
  public String groupField;
  public int groupFieldIndex;
  public ValueMetaInterface groupFieldMeta;
  public Object prevGroupFieldData;
  public RowSet resultRowsRowSet;
  public RowSet resultFilesRowSet;
  public RowSet executionResultRowSet;
  
	/**
	 * 
	 */
	public JobExecutorData()
	{
		super();
	}

}
