/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
 package org.pentaho.di.trans.steps.denormaliser;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * Data structure used by Denormaliser during processing
 * @author Matt
 * @since 19-jan-2006
 *
 */
public class DenormaliserData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	
	public Object[] previous;
	
	public int     groupnrs[];
	public Integer fieldNrs[];
	
	public Object[]         targetResult;

    public int keyFieldNr;
    
    public Map<String, List<Integer>> keyValue;

    public int[] removeNrs;

    public int[] fieldNameIndex;

    public long[] counters;

    public Object[] sum;

	public RowMetaInterface inputRowMeta;

	/**
	 * 
	 */
	public DenormaliserData()
	{
		super();

		previous=null;
        keyValue = new Hashtable<String, List<Integer>>();
	}

}
