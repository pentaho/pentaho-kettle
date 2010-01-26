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
 

package org.pentaho.di.trans.steps.closure;

import java.util.HashMap;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads information from a database table by using freehand SQL
 * 
 * @author Matt
 * @since 8-apr-2003
 */
public class ClosureGenerator extends BaseStep implements StepInterface
{
	private static Class<?> PKG = ClosureGeneratorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private ClosureGeneratorMeta meta;
	private ClosureGeneratorData data;
	
	public ClosureGenerator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (data.reading) {
			Object[] rowData = getRow();
			
			if (rowData==null) {
				data.reading=false;
			}
			else {
				if (first) {
					first=false;
					
					// Create the output row metadata
					//
					data.outputRowMeta = getInputRowMeta().clone();
					meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
					
					// Get indexes of parent and child field
					//
					data.parentIndex = getInputRowMeta().indexOfValue(meta.getParentIdFieldName());
					if (data.parentIndex<0) {
						throw new KettleException(BaseMessages.getString(PKG, "ClosureGenerator.Exception.ParentFieldNotFound"));
					}
					data.childIndex = getInputRowMeta().indexOfValue(meta.getChildIdFieldName());
					if (data.childIndex<0) {
						throw new KettleException(BaseMessages.getString(PKG, "ClosureGenerator.Exception.ChildFieldNotFound"));
					}
					
					data.parentValueMeta = getInputRowMeta().getValueMeta(data.parentIndex);
					data.childValueMeta = getInputRowMeta().getValueMeta(data.childIndex);
				}
				
				// add values to the buffer...
				//
				Object parentId = rowData[data.parentIndex];
				Object childId = rowData[data.childIndex];
				data.map.put(childId, parentId);
			}
		}
		else {
			// Writing the rows back...
			//
			for (Object current : data.map.keySet()) {
		        data.parents = new HashMap<Object,Long>();
		        
		        //add self as distance 0
		        //
		        data.parents.put(current, 0L);
		        
		        recurseParents(current,1);
		        for (Object parent : data.parents.keySet()) {
		        	Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		        	outputRow[0] = parent;
		        	outputRow[1] = current;
		        	outputRow[2] = data.parents.get(parent);
		        	putRow(data.outputRowMeta, outputRow);
				}
		    }
			
			setOutputDone();
			return false;
		}
		
		return true;
	}
	
    private void recurseParents(Object key, long distance) {
		// catch infinite loop - change at will
		if (distance > 20) {
			throw new RuntimeException("infinite loop detected:" + key);
		}
		Object parent = data.map.get(key);

		if (parent == null || parent == data.topLevel || parent.equals(data.topLevel)) {
			return;
		} else {
			data.parents.put(parent, distance);
			recurseParents(parent, distance + 1);
			return;
		}
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ClosureGeneratorMeta)smi;
		data=(ClosureGeneratorData)sdi;

		if (super.init(smi, sdi))
		{
			data.reading = true;
			data.map = new HashMap<Object, Object>();
			
			data.topLevel = null;
			if (meta.isRootIdZero()) {
				data.topLevel = new Long(0);
			}
			
			return true;
		}
		
		return false;
	}

}