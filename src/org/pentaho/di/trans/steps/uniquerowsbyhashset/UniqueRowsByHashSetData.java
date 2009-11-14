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

package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import java.util.HashSet;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



public class UniqueRowsByHashSetData extends BaseStepData implements StepDataInterface
{
    public RowMetaInterface outputRowMeta;
    public RowMetaInterface inputRowMeta;
    public boolean storeValues;
	public int  fieldnrs[];
	public String compareFields;
	public String realErrorDescription;
	boolean sendDuplicateRows;
	
	public HashSet<RowKey> seen = new HashSet<RowKey>();
	
	public UniqueRowsByHashSetData()
	{
		super();
	}
	
	public void clearHashSet()
	{
		sendDuplicateRows=false;
	    compareFields=null;
	    realErrorDescription=null;
	}
}
