package be.ibridge.kettle.trans.step.abort;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

public class AbortMeta  extends BaseStepMeta implements StepMetaInterface {
		
	/**
	 * Threshold to abort.
	 */
	private String rowThreshold;
	
	/**
	 * Message to put in log when aborting.
	 */
	private String message;
	
	/**
	 * Always log rows.
	 */
	private boolean alwaysLogRows;
	
    public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String[] input, String[] output, Row info) {
        // See if we have input streams leading to this step!
        if (input.length>0) {
            CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FilesFromResultMeta.CheckResult.StepExpectingNoReadingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        } else {
            CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FilesFromResultMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String stepname) {
        return new AbortDialog(shell, info, transMeta, stepname);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new Abort(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public StepDataInterface getStepData() {
        return new AbortData();
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException {
        readData(stepnode);        
    }

    public void setDefault() 
    {
    	rowThreshold  = "0";
    	message       = "";    	
    	alwaysLogRows = true;
    }
     
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

        retval.append("      ").append(XMLHandler.addTagValue("row_threshold", rowThreshold)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("message", message)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("always_log_rows", alwaysLogRows)); //$NON-NLS-1$ //$NON-NLS-2$
	    
	    return retval.toString();
	}

    private void readData(Node stepnode)
	    throws KettleXMLException
    {
    	try 
    	{
    	    rowThreshold  = XMLHandler.getTagValue(stepnode, "row_threshold"); //$NON-NLS-1$
    	    message       = XMLHandler.getTagValue(stepnode, "message"); //$NON-NLS-1$
    		alwaysLogRows = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "always_log_rows")); //$NON-NLS-1$ //$NON-NLS-2$
    	}
	    catch(Exception e)
	    {
 		    throw new KettleXMLException(Messages.getString("AbortMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
	    }    	
    }

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
 	    throws KettleException
    {
	    try
	    {
	    	rowThreshold  = rep.getStepAttributeString(id_step, "row_threshold"); //$NON-NLS-1$
	    	message       = rep.getStepAttributeString(id_step, "message"); //$NON-NLS-1$
		    alwaysLogRows = rep.getStepAttributeBoolean(id_step, "always_log_rows"); //$NON-NLS-1$
	    }
	    catch(Exception e)
	    {
 		    throw new KettleException(Messages.getString("Abort.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
	    }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step)
	    throws KettleException
    {
	    try
	    {
	    	rep.saveStepAttribute(id_transformation, id_step, "row_threshold",   rowThreshold); //$NON-NLS-1$
	    	rep.saveStepAttribute(id_transformation, id_step, "message",         message); //$NON-NLS-1$
  	 	    rep.saveStepAttribute(id_transformation, id_step, "always_log_rows", alwaysLogRows); //$NON-NLS-1$
	    }
	    catch(Exception e)
	    {
		    throw new KettleException(Messages.getString("Abort.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
	    }
    }
    
	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	public String getRowThreshold() 
	{
		return rowThreshold;
	}

	public void setRowThreshold(String rowThreshold) 
	{
		this.rowThreshold = rowThreshold;
	}

	public boolean isAlwaysLogRows() 
	{
		return alwaysLogRows;
	}

	public void setAlwaysLogRows(boolean alwaysLogRows) 
	{
		this.alwaysLogRows = alwaysLogRows;
	}
}