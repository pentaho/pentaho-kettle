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
 
package be.ibridge.kettle.trans.step.mapping;

import java.util.ArrayList;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Execute a mapping: a re-usuable transformation
 * 
 * @author Matt
 * @since 22-nov-2005
 */

public class Mapping extends BaseStep implements StepInterface
{
	private MappingMeta meta;
	private MappingData data;
	
	public Mapping(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    /**
     * Process a single row.  In our case, we send one row of data to a piece of transformation.
     * In the transformation, we look up the MappingInput step to send our rows to it.
     * As a consequence, for the time being, there can only be one MappingInput and one MappingOutput step in the Mapping.
     */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MappingMeta)smi;
		data=(MappingData)sdi;

		Row r=getRow();    // get row, set busy!
		if (r==null)       // no more input to be expected...
		{
            // Signal output done to the mapping input step in the mapping...
			data.mappingInput.setOutputDone();
            
			// The wait for mapping input is over...
            data.mappingInput.setFinished(); 
            
			return false;
		}
        
        //
        // OK, we have a row.
        // We need to "give" this row to the running mapping (sub-transformation).
        // We do this by looking up the MappingInput step in the transformation.
        // We give the row to this step ...
        //
        // Before we can send out this data, we need to convert field-names and actually do the field mapping
		// This means that we're renaming fields in case they are not the same.
		// They will stay renamed even after the mapping step.
		// 
		if (first)
		{
			first=false;
			data.renameFieldIndexes = new ArrayList();
			data.renameFieldNames   = new ArrayList();
			
			for (int i=0;i<meta.getInputField().length;i++)
			{
				if (meta.getInputField()[i]!=null && meta.getInputField()[i].length()>0)
				{
					if (meta.getInputMapping()[i]!=null && meta.getInputMapping()[i].length()>0)
					{
						if (!meta.getInputField()[i].equals(meta.getInputMapping()[i])) // rename these!
						{
							int idx = r.searchValueIndex(meta.getInputField()[i]);
							if (idx<0)
							{
								logError("Mapping target field ["+meta.getInputField()[i]+"] is not present in the input rows!");
								setErrors(1);
								stopAll();
								data.trans.stopAll();
								return false;
							}
							data.renameFieldIndexes.add(new Integer(idx));
							data.renameFieldNames.add(meta.getInputMapping()[i]);
							if (log.isRowLevel()) logRowlevel("#"+data.renameFieldIndexes.size()+" : index="+i+", new name="+meta.getInputMapping()[i]);
						}
					}
					else
					{
						logError("Mapping target field #"+i+" is not specified for input ["+meta.getInputField()[i]+"]!");
						setErrors(1);
						stopAll();
						data.trans.stopAll();
						return false;
					}
				}
				else
				{
					logError("Input field #"+i+" is not specified!");
					setErrors(1);
					stopAll();
					data.trans.stopAll();
					return false;
				}
			}
		} // end of first block
		
		for (int i=0;i<data.renameFieldIndexes.size();i++)
		{
			int idx = ((Integer)data.renameFieldIndexes.get(i)).intValue();
			String newName = (String)data.renameFieldNames.get(i);
			r.getValue(idx).setName(newName);
		}
		
        data.mappingInput.putRow(r);     // copy row to possible alternate rowset(s) in the mapping.
        
        if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);

        //
        // The problem now is to get a row back from the mapping...
        // This transformed row we need to send to the next step.
        // Well actually this is not done here but in the mapping output.  See init()
        //
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingMeta)smi;
		data=(MappingData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // First we need to load the mapping (transformation)
            if (meta.getMappingTransMeta()!=null) // Do we have a mapping at all?
            {
                // Create the transformation from meta-data...
                LogWriter log = LogWriter.getInstance();
                data.trans = new Trans(log, meta.getMappingTransMeta());
                
                // Start the transformation in the background!
                // Pass the arguments to THIS transformation...
                data.trans.execute(getTrans().getTransMeta().getArguments());
                
                // Now, the transformation runs in the background.
                // We pick it up and close shop when this step has finished processing.
                
                // Let's find out what the MappingInput step is...
                data.mappingInput = data.trans.findMappingInput();
                if (data.mappingInput==null)
                {
                    logError("Couldn't find MappingInput step in the mapping.");
                    return false;
                }
                
                // And the mapping output step?
                data.mappingOutput = data.trans.findMappingOutput();
                if (data.mappingOutput==null)
                {
                    logError("Couldn't find MappingOutput step in the mapping.");
                    return false;
                }
                
                // OK, now tell the MappingOutput step to send records over here!!
                data.mappingOutput.setConnectorStep(this);
                data.mappingOutput.setOutputField(meta.getOutputField());
                data.mappingOutput.setOutputMapping(meta.getOutputMapping());
            }
            
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
			data.trans.stopAll();
		}
		finally
		{
            // Close the running transformation
            if (data.trans!=null)
            {
                // Wait until the child transformation has finished.
                data.trans.waitUntilFinished();
                
                // store some logging, close shop.
                try
                {
	                data.trans.endProcessing("end");
                }
                catch(KettleException e)
                {
                	log.logError(toString(), "Unable to log end of transformation: "+e.toString());
                }
                
        		// See if there was an error in the sub-transformation, in that case, flag error etc.
        		if (data.trans.getErrors()>0)
        		{
        			logError("An error occurred in the sub-transformation, halting processing");
        			setErrors(1);
        		}
            }
            
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
