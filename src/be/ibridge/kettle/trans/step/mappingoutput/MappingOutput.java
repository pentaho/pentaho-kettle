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
 
package be.ibridge.kettle.trans.step.mappingoutput;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.mapping.Mapping;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class MappingOutput extends BaseStep implements StepInterface
{
	private MappingOutputMeta meta;
	private MappingOutputData data;


	public MappingOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MappingOutputMeta)smi;
		data=(MappingOutputData)sdi;

		Row r=getRow();    // get row, set busy!
		if (r==null) 
		{
            // No more input to be expected...
            // Tell the next steps.
            //
			data.mapping.setOutputDone();
			return false;
		}
		
        if (first)
        {
            first=false;
            // 
            // Wait until we know were to store the row...
            // 
            while (!isStopped() && data.mapping==null)
            {
                try { Thread.sleep(10); } catch(InterruptedException e) { stopAll(); }
            }
        }
        
        System.out.println("r = "+r);

        // Change the output fields that are specified...
        // TODO: use indexes to speed up the lookups, no time for this at the moment...
        for (int i=0;i<data.outputMapping.length;i++)
        {
            Value v = r.searchValue(data.outputMapping[i]);
            if (v!=null)
            {
                v.setName(data.outputField[i]);
            }
            else
            {
                throw new KettleStepException("Mapping output field specified couldn't be found: "+data.outputMapping[i]);
            }
        }
        

        
        
		data.mapping.putRow(r);     // copy row to possible alternate rowset(s).

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
			
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingOutputMeta)smi;
		data=(MappingOutputData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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

    public synchronized void setConnectorStep(Mapping mapping)
    {
        data.mapping = mapping;
    }

    public void setOutputField(String[] outputField)
    {
        data.outputField = outputField;
        
    }

    public void setOutputMapping(String[] outputMapping)
    {
        data.outputMapping = outputMapping;
        
    }
}
