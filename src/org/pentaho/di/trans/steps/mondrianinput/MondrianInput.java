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
 

package org.pentaho.di.trans.steps.mondrianinput;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads information from a database table by using freehand SQL
 * 
 * @author Matt
 * @since 8-apr-2003
 */
public class MondrianInput extends BaseStep implements StepInterface
{
	private MondrianInputMeta meta;
	private MondrianData data;
	
	public MondrianInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (first) // we just got started
		{
			first=false;

			data.mondrianHelper = new MondrianHelper(meta.getDatabaseMeta(), meta.getCatalog(), meta.getSQL());
			data.mondrianHelper.openQuery();
			data.mondrianHelper.createRectangularOutput();
			
			data.outputRowMeta = data.mondrianHelper.getOutputRowMeta().clone(); //
			
			data.rowNumber = 0;
		}

        if (data.rowNumber>=data.mondrianHelper.getRows().size())
        {
            setOutputDone(); // signal end to receiver(s)
            return false; // end of data or error.
        }
        
        List<Object> row = data.mondrianHelper.getRows().get(data.rowNumber++);
        Object[] outputRowData = RowDataUtil.allocateRowData(row.size());
        for (int i=0;i<row.size();i++) {
        	outputRowData[i] = row.get(i);
        }
        
        putRow(data.outputRowMeta, outputRowData);
        
		return true;
	}
    
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		logBasic("Finished reading query, closing connection.");
		
	    data.mondrianHelper.close();

	    super.dispose(smi, sdi);
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MondrianInputMeta)smi;
		data=(MondrianData)sdi;

		if (super.init(smi, sdi))
		{
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
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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