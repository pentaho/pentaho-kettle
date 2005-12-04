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
        
        data.mappingInput.putRow(r);     // copy row to possible alternate rowset(s) in the mapping.
        
        if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);

        //
        // The problem now is to get a row back from the mapping...
        // This transformed row we need to send to the next step.
        // Well actually this is not done here but in the mapping.  See init()
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
                // TODO: see how we can to do this in the GUI.
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
            
            // Close the running transformation
            if (data.trans!=null)
            {
                // Wait until the child transformation has finished.
                data.trans.waitUntilFinished();
                
                // store some logging, close shop.
                data.trans.endProcessing("end");
            }
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
}
