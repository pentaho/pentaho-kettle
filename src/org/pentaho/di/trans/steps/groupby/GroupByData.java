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

package org.pentaho.di.trans.steps.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class GroupByData extends BaseStepData implements StepDataInterface
{
	public Object previous[];
	
	public RowMetaInterface aggMeta;
	public Object  agg[];
	public RowMetaInterface groupMeta;
	public RowMetaInterface groupAggMeta; // for speed: groupMeta+aggMeta
	public int  groupnrs[];
	public int  subjectnrs[];
	public long counts[];
  
	public Set<Object> distinctObjs[];

    public ArrayList<Object[]> bufferList;

    public File tempFile;

    public FileOutputStream fos;

    public DataOutputStream dos;

    public int rowsOnFile;

    public boolean firstRead;

    public FileInputStream fis;
    public DataInputStream dis;

    public Object groupResult[];

    public boolean hasOutput;
    
    
    public RowMetaInterface inputRowMeta;
    public RowMetaInterface outputRowMeta;

	public List<Integer> cumulativeSumSourceIndexes;
	public List<Integer> cumulativeSumTargetIndexes;
	
	public List<Integer> cumulativeAvgSourceIndexes;
	public List<Integer> cumulativeAvgTargetIndexes;

	public Object[] previousSums;

	public Object[] previousAvgSum;

	public long[] previousAvgCount;

	public ValueMetaInterface valueMetaInteger;
	public ValueMetaInterface valueMetaNumber;

	public double[] mean;
	
	public boolean newBatch;
    
	/**
	 * 
	 */
	public GroupByData()
	{
		super();

		previous=null;
	}

}
