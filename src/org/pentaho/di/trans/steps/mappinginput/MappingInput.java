 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package org.pentaho.di.trans.steps.mappinginput;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
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
                    throw new KettleException(Messages.getString("MappingInput.Exception.UnableToConnectWithParentMapping", ""+(totalsleep/1000)));
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
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            
            // Now change the field names according to the mapping specification...
            // That means that all fields go through unchanged, unless specified.
            // 
            //
            for (MappingValueRename valueRename : data.valueRenames) {
            	ValueMetaInterface valueMeta = data.outputRowMeta.searchValueMeta(valueRename.getSourceValueName());
            	if (valueMeta==null) {
            		throw new KettleStepException(Messages.getString("MappingInput.Exception.UnableToFindMappedValue", valueRename.getSourceValueName()));
            	}
            	valueMeta.setName(valueRename.getTargetValueName());
            }
		}
		
		putRow(data.outputRowMeta, row);
		
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

	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("MappingInput.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("MappingInput.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}

	public void setConnectorSteps(StepInterface[] sourceSteps, List<MappingValueRename> valueRenames) {
		
        for (int i=0;i<sourceSteps.length;i++) {
        	
	        // OK, before we leave, make sure there is a rowset that covers the path to this target step.
	        // We need to create a new RowSet and add it to the Input RowSets of the target step
        	//
	        RowSet rowSet = new RowSet(getTransMeta().getSizeRowset());
	        
	        // This is always a single copy, both for source and target...
	        //
	        rowSet.setThreadNameFromToCopy(sourceSteps[i].getStepname(), 0, getStepname(), 0);
	        
	        // Make sure to connect it to both sides...
	        //
	        sourceSteps[i].getOutputRowSets().add(rowSet);
	        getInputRowSets().add(rowSet);
        }
        data.valueRenames = valueRenames;
        
		data.sourceSteps = sourceSteps;
	}
}
