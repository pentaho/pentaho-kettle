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
 
package be.ibridge.kettle.trans.step.dbproc;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Retrieves values from a database by calling database stored procedures or functions
 *  
 * @author Matt
 * @since 26-apr-2003
 *
 */

public class DBProc extends BaseStep implements StepInterface
{
	private DBProcMeta meta;
	private DBProcData data;
	
	public DBProc(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void runProc(Row row)
		throws KettleException
	{
		int i;
		Row add;

        if (first)
		{
			first=false;
			data.argnrs=new int[meta.getArgument().length];
			
			for (i=0;i<meta.getArgument().length;i++)
			{
				if (!meta.getArgumentDirection()[i].equalsIgnoreCase("OUT")) // IN or INOUT
				{
					data.argnrs[i]=row.searchValueIndex(meta.getArgument()[i]);
					if (data.argnrs[i]<0)
					{
						logError("Error finding field: "+meta.getArgument()[i]+"]");
						throw new KettleStepException("Couldn't find field '"+meta.getArgument()[i]+"' in row!");
					}
				}
				else
				{
					data.argnrs[i]=-1;
				}
			}
			data.db.setProcLookup(meta.getProcedure(), meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), 
			                      meta.getResultName(), meta.getResultType());
		}

		data.db.setProcValues(row, data.argnrs, meta.getArgumentDirection(), meta.getResultName()!=null); 

		add=data.db.callProcedure(meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), meta.getResultName(), meta.getResultType());
		for (i=0;i<add.size();i++)
		{
			row.addValue( add.getValue(i) );
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DBProcMeta)smi;
		data=(DBProcData)sdi;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		    
		try
		{
			runProc(r); // add new values to the row in rowset[0].
			putRow(r);  // copy row to output rowset(s);
				
			if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
		}
		catch(KettleException e)
		{
			logError("Because of an error, this step can't continue: "+e.getMessage());
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DBProcMeta)smi;
		data=(DBProcData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabase());
			try
			{
				data.db.connect();
				
				logBasic("Connected to database...");
				
				return true;
			}
			catch(KettleException e)
			{
				logError("An error occurred, processing will be stopped: "+e.getMessage());
				data.db.disconnect();
			}
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DBProcMeta)smi;
	    data = (DBProcData)sdi;
	    
	    data.db.disconnect();
	    
	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		logBasic("Starting to run...");
		
		try
		{
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
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
