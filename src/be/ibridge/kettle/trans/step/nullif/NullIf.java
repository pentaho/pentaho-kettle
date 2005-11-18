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
 
package be.ibridge.kettle.trans.step.nullif;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


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
		Row r = getRow();
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		logRowlevel("Convert field values to NULL for row: "+r);
		
		if (first)
		{
		    first=false;
		    
		    data.keynr     = new int[meta.getFieldValue().length];
		    data.nullValue = new Value[meta.getFieldValue().length];
		    for (int i=0;i<meta.getFieldValue().length;i++)
		    {
		        data.keynr[i] = r.searchValueIndex(meta.getFieldName()[i]);
				if (data.keynr[i]<0)
				{
					logError("Couldn't find field '"+meta.getFieldValue()[i]+"' in row!");
					setErrors(1);
					stopAll();
					return false;
				}
		        data.nullValue[i] = new Value(meta.getFieldName()[i], meta.getFieldValue()[i]);
		        data.nullValue[i].setType(r.getValue(data.keynr[i]).getType());
		    }
		}
		
		for (int i=0;i<meta.getFieldValue().length;i++)
		{
		    Value field = r.getValue(data.keynr[i]); 
		    if (field!=null && field.equals(data.nullValue[i]))
		    {
		        // OK, this value needs to be set to NULL
		        field.setNull();
		    }
		}
		
		putRow(r);     // Just one row!

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
}
