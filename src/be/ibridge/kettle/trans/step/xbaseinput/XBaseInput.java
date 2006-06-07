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
 
package be.ibridge.kettle.trans.step.xbaseinput;

import java.io.File;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XBase;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Reads data from an XBase (dBase, foxpro, ...) file.
 * 
 * @author Matt
 * @since 8-sep-2004
 */
public class XBaseInput extends BaseStep implements StepInterface
{
	private XBaseInputMeta meta;
	private XBaseInputData data;
		
	public XBaseInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XBaseInputMeta)smi;
		data=(XBaseInputData)sdi;

		debug="Get row from DBF file"; //$NON-NLS-1$
		Row row=data.xbi.getRow(data.fields);
		if (row==null) 
		{
			debug="No more rows."; //$NON-NLS-1$
			if (data.xbi.hasError())
			{
				logError(Messages.getString("XBaseInput.Log.Error.UnexpectedErrorCanNotContinue")); //$NON-NLS-1$
				setErrors(1);
				stopAll();
			}
			setOutputDone();  // signal end to receiver(s)
			return false; // end of data or error.
		}
		
		linesInput++;
		
		debug="Do we need to add a rownr?"; //$NON-NLS-1$
		// Add a rownr???
		if (meta.isRowNrAdded() && meta.getRowNrField()!=null && meta.getRowNrField().length()>0)
		{
			Value rownr = new Value(meta.getRowNrField(), Value.VALUE_TYPE_INTEGER);
			rownr.setLength(9,0);
			rownr.setValue(linesInput);
			row.addValue(rownr);
		}
				
		debug="Send the row to the next step."; //$NON-NLS-1$
		putRow(row);        // fill the rowset(s). (wait for empty)

		if ((linesInput>0) && (linesInput%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("XBaseInput.Log.LineNr")+linesInput); //$NON-NLS-1$

		debug="End of readRowOfData."; //$NON-NLS-1$
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XBaseInputMeta)smi;
		data=(XBaseInputData)sdi;

	    if (super.init(smi, sdi))
	    {
			// Replace possible environment variables...
			String file_dbf = StringUtil.environmentSubstitute( meta.getDbfFileName() ); 
					
			data.xbi=new XBase(file_dbf);
            try
            {
                data.xbi.open();
				// Add memo-file to structure!
				// xbi.setMemo("D:\\Projects\\Kettle\\testsuite\\CTS\\i-brid\\CTSLAY.DBT");
	
				logBasic(Messages.getString("XBaseInput.Log.OpenedXBaseFile")); //$NON-NLS-1$
				data.fields = data.xbi.getFields();
				
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, new File(meta.getDbfFileName()), getTransMeta().getName(), getStepname());
				resultFile.setComment("File was read by an XBase input step");
                addResultFile(resultFile);

				return true;
            }
            catch(KettleException e)
			{
				logError(Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile1")+file_dbf+Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}
	    }
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		logBasic(Messages.getString("XBaseInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		data.xbi.close();
		
		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("XBaseInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e)
		{
			logError(Messages.getString("XBaseInput.Log.Error.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
		    dispose(meta, data);
			markStop();
		    logSummary();
		}
	}
}
