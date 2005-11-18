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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XBase;
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

		debug="Get row from DBF file";
		Row row=data.xbi.getRow(data.fields);
		if (row==null) 
		{
			debug="No more rows.";
			if (data.xbi.hasError())
			{
				logError("Unable to continue because of errors");
				setErrors(1);
				stopAll();
			}
			setOutputDone();  // signal end to receiver(s)
			return false; // end of data or error.
		}
		
		linesInput++;
		
		debug="Do we need to add a rownr?";
		// Add a rownr???
		if (meta.isRowNrAdded() && meta.getRowNrField()!=null && meta.getRowNrField().length()>0)
		{
			Value rownr = new Value(meta.getRowNrField(), Value.VALUE_TYPE_INTEGER);
			rownr.setLength(9,0);
			rownr.setValue(linesInput);
			row.addValue(rownr);
		}
				
		debug="Send the row to the next step.";
		putRow(row);        // fill the rowset(s). (wait for empty)

		if ((linesInput>0) && (linesInput%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesInput);

		debug="End of readRowOfData.";
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XBaseInputMeta)smi;
		data=(XBaseInputData)sdi;

	    if (super.init(smi, sdi))
	    {
			// Replace possible environment variables...
			String file_dbf = Const.replEnv( meta.getDbfFileName() ); 
					
			data.xbi=new XBase(file_dbf);
            try
            {
                data.xbi.open();
				// Add memo-file to structure!
				// xbi.setMemo("D:\\Projects\\Kettle\\testsuite\\CTS\\i-brid\\CTSLAY.DBT");
	
				logBasic("Opened XBase database file... ");
				data.fields = data.xbi.getFields();
				return true;
            }
            catch(KettleException e)
			{
				logError("Couldn't open or read the XBase database ("+file_dbf+") because of an error: "+e.getMessage());
			}
	    }
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		logBasic("Finished reading records, closing file(s).");
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
			logBasic("Starting to run...");		
			while (!isStopped() && processRow(meta, data) );
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
			markStop();
		    logSummary();
		}
	}
}
