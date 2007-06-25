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
 
package org.pentaho.di.trans.steps.nullif;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Get information from the System or the supervising transformation.
 * 
 * @author Matt 
 * @since 4-aug-2003
 */
public class NullIf extends BaseStep implements StepInterface
{
	private NullIfMeta meta;
	private NullIfData data;
	
	public NullIf(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(NullIfMeta)smi;
		data=(NullIfData)sdi;
		
	    // Get one row from one of the rowsets...
		Object[] r = getRow();
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (log.isRowLevel()) logRowlevel(Messages.getString("NullIf.Log.ConvertFieldValuesToNullForRow")+r); //$NON-NLS-1$
		
		if (first)
		{
		    first=false;
            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone(); 
		    data.keynr     = new int[meta.getFieldValue().length];
		    data.nullValue = new Object[meta.getFieldValue().length];
		    data.nullValueMeta = new ValueMeta[meta.getFieldValue().length];
		    for (int i=0;i<meta.getFieldValue().length;i++)
		    {
		        data.keynr[i] = data.outputRowMeta.indexOfValue(meta.getFieldName()[i]);
				if (data.keynr[i]<0)
				{
					logError(Messages.getString("NullIf.Log.CouldNotFindFieldInRow",meta.getFieldName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
				data.nullValueMeta[i]=data.outputRowMeta.getValueMeta(data.keynr[i]);
				//convert from input string entered by the user
		        data.nullValue[i] = data.nullValueMeta[i].convertData(new ValueMeta(null, ValueMetaInterface.TYPE_STRING), meta.getFieldValue()[i]);
		    }
		}
		
		for (int i=0;i<meta.getFieldValue().length;i++)
		{
		    Object field = r[data.keynr[i]]; 
		    if (field!=null && data.nullValueMeta[i].compare(field, data.nullValue[i])==0)
		    {
		        // OK, this value needs to be set to NULL
		    	r[data.keynr[i]]=null;
		    }
		}
		
		putRow(data.outputRowMeta,r);     // Just one row!

		return true;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(NullIfMeta)smi;
		data=(NullIfData)sdi;

		super.dispose(smi, sdi);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(NullIfMeta)smi;
		data=(NullIfData)sdi;
		
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
			logBasic(Messages.getString("NullIf.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("NullIf.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
}
