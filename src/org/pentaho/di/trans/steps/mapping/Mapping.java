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
 
package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.Arrays;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;




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

		Object[] r=getRow();    // get row, set busy!
		if (r==null)       // no more input to be expected...
		{
            // Signal output done to the mapping input step in the mapping...
            // But only if the mapping was started in the first place.
            // This only happens when 0 lines of data are processed.
            //
            if (data.wasStarted)
            {
    			data.mappingInput.setOutputDone();
                
    			// The wait for mapping input is over...
                data.mappingInput.setFinished();
            }
            else
            {
                // Zero rows were received, the mapping was never started: so just signal "END"...
                setOutputDone();
            }

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
            
            // Start the mapping/transformation threads
            //
            // Start the transformation in the background!
            // Pass the arguments to THIS transformation...
            data.trans.execute(getTrans().getTransMeta().getArguments());
            
            // Now, the transformation runs in the background.
            // We pick it up and close shop when this step has finished processing.
            
            // Let's find out what the MappingInput step is...
            data.mappingInput = data.trans.findMappingInput();
            if (data.mappingInput==null)
            {
                logError(Messages.getString("Mapping.Log.CouldNotFindMappingInputStep")); //$NON-NLS-1$
                return false;
            }
            
            // And the mapping output step?
            data.mappingOutput = data.trans.findMappingOutput();
            if (data.mappingOutput==null)
            {
                logError(Messages.getString("Mapping.Log.CouldNotFindMappingInputStep2")); //$NON-NLS-1$
                return false;
            }
            
            // OK, now tell the MappingOutput step to send records over here!!
            data.mappingOutput.setConnectorStep(this);
            data.mappingOutput.setOutputField(meta.getOutputField());
            data.mappingOutput.setOutputMapping(meta.getOutputMapping());
            
            data.wasStarted = true;
            
            // Now we continue with out regular program... 
			data.renameFieldIndexes = new ArrayList();
			data.renameFieldNames   = new ArrayList();
			
            // See if the same field get's mapped to a different target multiple times
            // To do this, we simply check if the inputFields contains doubles.
            String[] inputFields = new String[meta.getInputField().length];
            for (int i=0;i<inputFields.length;i++) inputFields[i]=meta.getInputField()[i];
            Arrays.sort(inputFields);
            for (int i=0;i<inputFields.length-1;i++)
            {
                if (inputFields[i].equalsIgnoreCase(inputFields[i+1]))
                {
                    throw new KettleException(Messages.getString("Mapping.Exception.SameFieldMappedTwice", inputFields[i]));
                }
            }
            
			for (int i=0;i<meta.getInputField().length;i++)
			{
				if (meta.getInputField()[i]!=null && meta.getInputField()[i].length()>0)
				{
					if (meta.getInputMapping()[i]!=null && meta.getInputMapping()[i].length()>0)
					{
						if (!meta.getInputField()[i].equals(meta.getInputMapping()[i])) // rename these!
						{
							int idx = getInputRowMeta().indexOfValue(meta.getInputField()[i]);
							if (idx<0)
							{
								logError(Messages.getString("Mapping.Log.TargetFieldNotPresent",meta.getInputField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
								setErrors(1);
								stopAll();
								data.trans.stopAll();
								return false;
							}
							data.renameFieldIndexes.add(new Integer(idx));
							data.renameFieldNames.add(meta.getInputMapping()[i]);
							if (log.isRowLevel()) logRowlevel(Messages.getString("Mapping.Log.RenameFieldInfo",data.renameFieldIndexes.size()+"",i+"",meta.getInputMapping()[i])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
					else
					{
						logError(Messages.getString("Mapping.Log.TargetFieldNotSpecified",i+"",meta.getInputField()[i])+"]!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setErrors(1);
						stopAll();
						data.trans.stopAll();
						return false;
					}
				}
				else
				{
					logError(Messages.getString("Mapping.Log.InputFieldNotSpecified",i+"")); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					data.trans.stopAll();
					return false;
				}
			}
            
            // Create the output row metadata for the mapping input step...
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            
            for (int i=0;i<data.renameFieldIndexes.size();i++)
            {
                int idx = ((Integer)data.renameFieldIndexes.get(i)).intValue();
                String newName = (String)data.renameFieldNames.get(i);
                data.outputRowMeta.getValueMeta(idx).setName(newName);
            }
                        
		} // end of first block
		
		data.mappingInput.putRow(data.outputRowMeta, r);     // copy row to possible alternate rowset(s) in the mapping.
        
        if (checkFeedback(linesRead)) logBasic(Messages.getString("Mapping.Log.LineNumber")+linesRead); //$NON-NLS-1$

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
            try
            {
                Repository repository = Repository.getCurrentRepository();
                data.mappingTransMeta = MappingMeta.loadMappingMeta(meta.getFileName(), meta.getTransName(), meta.getDirectoryPath(), repository);
                if (data.mappingTransMeta!=null) // Do we have a mapping at all?
                {
                    // Create the transformation from meta-data...
                    LogWriter log = LogWriter.getInstance();
                    data.trans = new Trans(log, data.mappingTransMeta);
                    
                    // We launch the transformation in the processRow when the first row is received.
                    // This will allow the correct variables to be passed.
                    // Otherwise the parent is the init() thread which will be gone once the init is done.
                    return true;
                }
                else
                {
                    logError("No valid mapping was specified!");
                    return false;
                }
            }
            catch(Exception e)
            {
                logError("Unable to load the mapping transformation because of an error : "+e.toString());
                logError(Const.getStackTracker(e));
            }
            
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // Close the running transformation
        if (data.wasStarted)
        {
            // Wait until the child transformation has finished.
            data.trans.waitUntilFinished();
            
            // store some logging, close shop.
            try
            {
                data.trans.endProcessing("end"); //$NON-NLS-1$
            }
            catch(KettleException e)
            {
                log.logError(toString(), Messages.getString("Mapping.Log.UnableToLogEndOfTransformation")+e.toString()); //$NON-NLS-1$
            }
            
            // See if there was an error in the sub-transformation, in that case, flag error etc.
            if (data.trans.getErrors()>0)
            {
                logError(Messages.getString("Mapping.Log.ErrorOccurredInSubTransformation")); //$NON-NLS-1$
                setErrors(1);
            }
        }
        super.dispose(smi, sdi);
    }
    
    public void stopAll()
    {
        // Stop this step
        super.stopAll();
        
        // Also stop the mapping step.
        if ( data.trans != null  )
        {
            data.trans.stopAll();
        }
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("Mapping.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("Mapping.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
			if (data.trans!=null) data.trans.stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
