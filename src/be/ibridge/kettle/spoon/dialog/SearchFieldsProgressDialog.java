/*
 *
 *
 */

package be.ibridge.kettle.spoon.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;


/**
 * 
 * 
 * @author Matt
 * @since  10-mrt-2005
 */
public class SearchFieldsProgressDialog implements IRunnableWithProgress
{
	private StepMeta  stepInfo;
	private boolean   before;
	private TransMeta transMeta;
	private Row       fields;
	
	public SearchFieldsProgressDialog(TransMeta transMeta, StepMeta stepMeta, boolean before)
	{
		this.transMeta = transMeta;
		this.stepInfo  = stepMeta;
		this.before    = before;
		this.fields    = null;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		int size = transMeta.findNrPrevSteps(stepInfo);

		try
		{
			if (before)
			{
				monitor.beginTask("Searching for input fields...", size);
				fields = transMeta.getPrevStepFields(stepInfo, monitor);
			}
			else
			{
				monitor.beginTask("Searching for output fields...", size);
				fields = transMeta.getStepFields(stepInfo, monitor);
			}
		}
		catch(KettleStepException kse)
		{
			LogWriter.getInstance().logError("Search fields progress dialog", "Unable to get fields for step ["+stepInfo+"] : "+kse.getMessage());
			throw new InvocationTargetException(kse, "Unable to get fields for step ["+stepInfo+"] : "+kse.getMessage());
		}

		monitor.done();
	}
	
	

	/**
	 * @return Returns the before.
	 */
	public boolean isBefore()
	{
		return before;
	}
	
	/**
	 * @param before The before to set.
	 */
	public void setBefore(boolean before)
	{
		this.before = before;
	}
	
	/**
	 * @return Returns the fields.
	 */
	public Row getFields()
	{
		return fields;
	}
	
	/**
	 * @param fields The fields to set.
	 */
	public void setFields(Row fields)
	{
		this.fields = fields;
	}
	
	/**
	 * @return Returns the stepInfo.
	 */
	public StepMeta getStepInfo()
	{
		return stepInfo;
	}
	
	/**
	 * @param stepInfo The stepInfo to set.
	 */
	public void setStepInfo(StepMeta stepInfo)
	{
		this.stepInfo = stepInfo;
	}
	
	/**
	 * @return Returns the transMeta.
	 */
	public TransMeta getTransMeta()
	{
		return transMeta;
	}
	
	/**
	 * @param transMeta The transMeta to set.
	 */
	public void setTransMeta(TransMeta transMeta)
	{
		this.transMeta = transMeta;
	}
}
