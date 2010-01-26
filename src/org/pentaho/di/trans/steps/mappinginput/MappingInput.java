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
 
package org.pentaho.di.trans.steps.mappinginput;

import java.util.List;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class MappingInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MappingInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MappingInputMeta meta;
	private MappingInputData data;
	
	public MappingInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    // ProcessRow is not doing anything
    // It's a place holder for accepting rows from the parent transformation...
    // So, basically, this is a glorified Dummy with a little bit of meta-data
    // 
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MappingInputMeta)smi;
		data=(MappingInputData)sdi;
		
        if (!data.linked)
        {
            // 
            // Wait until we know were to read from the parent transformation...
            // However, don't wait forever, if we don't have a connection after 60 seconds: bail out! 
            //
            int totalsleep = 0;
            while (!isStopped() && data.sourceSteps==null)
            {
                try { totalsleep+=10; Thread.sleep(10); } catch(InterruptedException e) { stopAll(); }
                if (totalsleep>60000)
                {
                    throw new KettleException(BaseMessages.getString(PKG, "MappingInput.Exception.UnableToConnectWithParentMapping", ""+(totalsleep/1000)));
                }
            }
            
            // OK, now we're ready to read from the parent source steps.
            data.linked=true;
        }
        
		Object[] row = getRow();
		if (row==null) {
			setOutputDone();
			return false;
		}
		
		if (first) {
			first=false;
			
            // The Input RowMetadata is not the same as the output row meta-data.
            // The difference is described in the data interface 
			// 
            // String[] data.sourceFieldname 
            // String[] data.targetFieldname 
            //
            // --> getInputRowMeta() is not corresponding to what we're outputting.
			// In essence, we need to rename a couple of fields...
			//
            data.outputRowMeta = getInputRowMeta().clone();
            meta.setInputRowMeta(getInputRowMeta());

            if (meta.isSelectingAndSortingUnspecifiedFields()) {
            	//
            	// Create a list of the indexes to select to get the right order or fields on the output.
            	//
            	data.fieldNrs = new int[data.outputRowMeta.size()];
            	for (int i=0;i<data.outputRowMeta.size();i++) {
            		data.fieldNrs[i] = getInputRowMeta().indexOfValue(data.outputRowMeta.getValueMeta(i).getName());
            	}
            }

            // Now change the field names according to the mapping specification...
            // That means that all fields go through unchanged, unless specified.
            // 
            for (MappingValueRename valueRename : data.valueRenames) {
            	ValueMetaInterface valueMeta = data.outputRowMeta.searchValueMeta(valueRename.getSourceValueName());
            	if (valueMeta==null) {
            		throw new KettleStepException(BaseMessages.getString(PKG, "MappingInput.Exception.UnableToFindMappedValue", valueRename.getSourceValueName()));
            	}
            	valueMeta.setName(valueRename.getTargetValueName());
            }

            // meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}
		
		if (meta.isSelectingAndSortingUnspecifiedFields()) {
			Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			for (int i=0;i<data.fieldNrs.length;i++) {
				outputRowData[i] = row[data.fieldNrs[i]];
			}
			putRow(data.outputRowMeta, outputRowData);
		}
		else {
			putRow(data.outputRowMeta, row);
		}
		
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingInputMeta)smi;
		data=(MappingInputData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

	public void setConnectorSteps(StepInterface[] sourceSteps, List<MappingValueRename> valueRenames, String mappingStepname) {
		
        for (int i=0;i<sourceSteps.length;i++) {
        	
        	// We don't want to add the mapping-to-mapping rowset
        	//
        	if (!sourceSteps[i].isMapping()) {
		        // OK, before we leave, make sure there is a rowset that covers the path to this target step.
		        // We need to create a new RowSet and add it to the Input RowSets of the target step
	        	//
		        BlockingRowSet rowSet = new BlockingRowSet(getTransMeta().getSizeRowset());
		        
		        // This is always a single copy, both for source and target...
		        //
		        rowSet.setThreadNameFromToCopy(sourceSteps[i].getStepname(), 0, mappingStepname, 0);
		        
		        // Make sure to connect it to both sides...
		        //
		        sourceSteps[i].getOutputRowSets().add(rowSet);
		        getInputRowSets().add(rowSet);
        	}
        }
        data.valueRenames = valueRenames;
        
		data.sourceSteps = sourceSteps;
	}
	
}