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

package org.pentaho.di.trans.steps.memgroupby;

import java.util.HashMap;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MemoryGroupByData extends BaseStepData implements StepDataInterface
{
	public class HashEntry {
		private Object[] groupData;
		
		public HashEntry(Object[] groupData) {
			this.groupData = groupData;
		}
		
		public Object[] getGroupData() {
			return groupData;
		}
		
		public boolean equals(Object obj) {
			HashEntry entry = (HashEntry) obj;
			
			try {
				return groupMeta.compare(groupData, entry.groupData)==0;
			} catch(KettleValueException e) {
				throw new RuntimeException(e);
			}
		}
		
		public int hashCode() {
			try {
				return groupMeta.hashCode(groupData);
			} catch (KettleValueException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public HashMap<HashEntry, Aggregate> map;
	
	public RowMetaInterface aggMeta;
	public RowMetaInterface groupMeta;
	public RowMetaInterface entryMeta;
	
	public RowMetaInterface groupAggMeta; // for speed: groupMeta+aggMeta
	public int  groupnrs[];
	public int  subjectnrs[];
  

    public boolean firstRead;

    public Object groupResult[];

    public boolean hasOutput;
    
    
    public RowMetaInterface inputRowMeta;
    public RowMetaInterface outputRowMeta;


	public ValueMetaInterface valueMetaInteger;
	public ValueMetaInterface valueMetaNumber;

    
	/**
	 * 
	 */
	public MemoryGroupByData()
	{
		super();

	}
	
	public HashEntry getHashEntry(Object[] groupData) {
		return new HashEntry(groupData);
	}

}
