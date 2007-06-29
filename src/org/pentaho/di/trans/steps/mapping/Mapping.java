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
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;




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
        
        // Start the mapping/transformation threads
        //
        // Start the transformation in the background!
        // Pass the arguments to THIS transformation...
        data.trans.execute(getTrans().getTransMeta().getArguments());
        
        List<Thread> connectorThreads = new ArrayList<Thread>();
        
        // OK, check the input mapping definitions and look up the steps to read from.
        for (MappingIODefinition inputDefinition : meta.getInputMappings()) {
        	// This is an input mapping, so it reads from this transformation and writes to the mapping...
        	// What step is it reading from?
        	StepInterface sourceStep = (StepInterface) getTrans().findRunThread(inputDefinition.getInputStepname());
        	
        	// What step is it writing to?
        	StepInterface targetStep = (StepInterface) getTrans().findRunThread(inputDefinition.getOutputStepname());
        	if (targetStep==null) {
        		// No target was specifically specified.
        		// That means we only expect one "mapping input" step in the mapping...
        		MappingInput[] mappingInputSteps = data.trans.findMappingInput();
        		
        		if (mappingInputSteps.length==0) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OneMappingInputStepRequired"));
        		}
        		if (mappingInputSteps.length>1) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OnlyOneMappingInputStepAllowed", ""+mappingInputSteps.length));
        		}
        		
        		targetStep = mappingInputSteps[0];
        	}
        	MappingThread mappingThread = new MappingThread(this, sourceStep, targetStep);
        	Thread connectorThread = new Thread(mappingThread);
        	connectorThreads.add(connectorThread);
        }
        
        // Now we have a List of connector threads.
        // If we start all these we'll be starting to pump data into the mapping
        // If we don't have any threads to start, nothings going in there...
        // However, before we send anything over, let's first explain to the mapping output steps where the data needs to go...
        //
        for (MappingIODefinition outputDefinition : meta.getOutputMappings()) {
        	// OK, what is the source (input) step in the mapping: it's the mapping output step...
        	// What step are we reading from here?
        	//
        	MappingOutput sourceStep = (MappingOutput) getTrans().findRunThread(outputDefinition.getInputStepname());
        	if (sourceStep==null) {
        		// No source step was specified: we're reading from a single Mapping Output step.
        		// We should verify this if this is really the case...
        		//
        		MappingOutput[] mappingOutputSteps = data.trans.findMappingOutput();
        		
        		if (mappingOutputSteps.length==0) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OneMappingOutputStepRequired"));
        		}
        		if (mappingOutputSteps.length>1) {
        			throw new KettleException(Messages.getString("MappingDialog.Exception.OnlyOneMappingOutputStepAllowed", ""+mappingOutputSteps.length));
        		}
        		
        		sourceStep = mappingOutputSteps[0];
        	}
        	
        	// To what step in this transformation are we writing to?
        	//
        	StepInterface targetStep;
        	if (!Const.isEmpty(outputDefinition.getOutputStepname())) {
        		// If we have a target step specification for the output of the mapping, we need to send it over there...
        		//
            	targetStep = (StepInterface) getTrans().findRunThread(outputDefinition.getOutputStepname());
            	if (targetStep==null) {
            		throw new KettleException(Messages.getString("MappingDialog.Exception.StepNameNotFound", outputDefinition.getOutputStepname()));
            	}
        	}
        	else {
        		targetStep=this; // Just send the data over here if no target step is specified.
        	}
        	
        	// Now tell the mapping output step where to look...
        	sourceStep.setConnectorStep(targetStep);
        }
        
        // OK, now's as good as any time to start the connector threads...
        // 
        for (Thread connectorThread : connectorThreads) {
        	connectorThread.start();
        }
        
        // In case any data comes our way, we should pass it along...
        //
        Object[] row = getRow();
        while (row!=null && !isStopped()) {
        	putRow(getInputRowMeta(), row); // pass it along to the next step...
        	row = getRow();
        }
        setOutputDone();
        
        // The transformation still runs in the background and might have some more work to do.
        // Since everything is running in the MappingThreads we don't have to do anything else here but wait...
        // Join all the threads and we should be done with it.
        //
        for (Thread connectorThread : connectorThreads) {
        	try {
				connectorThread.join();
			} catch (InterruptedException e) {
				// Ignore this
			}
        }
        
		return false;
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
