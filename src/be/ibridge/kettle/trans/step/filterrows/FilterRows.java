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

package be.ibridge.kettle.trans.step.filterrows;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Filters input rows base on conditions.
 * 
 * @author Matt
 * @since 16-apr-2003, 07-nov-2004 (rewrite)
 */

public class FilterRows extends BaseStep implements StepInterface
{
	private FilterRowsMeta meta;
	private FilterRowsData data;
	
	public FilterRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized boolean keepRow(Row row)
	{
		debug="before evaluate condition";
		boolean ret=meta.getCondition().evaluate(row);
        debug="after evaluate condition";
		return ret;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(FilterRowsMeta)smi;
		data=(FilterRowsData)sdi;

		Row r=null;
		boolean keep;
		
		r=getRow();       // Get next useable row from input rowset(s)!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		keep=keepRow(r); // Keep this row?
		if (!meta.chosesTargetSteps())
		{
			if (keep)
			{
				putRow(r);       // copy row to output rowset(s);
			}
		}
		else
		{
		    if (keep)
		    {
		        //System.out.println("Sending row to true  :"+info.getSendTrueStepname()+" : "+r);
		        putRowTo(r, meta.getSendTrueStepname());
		    }
		    else
		    {
		        //System.out.println("Sending row to false :"+info.getSendTrueStepname()+" : "+r);
		        putRowTo(r, meta.getSendFalseStepname());
		    }
		}
		
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
			
		return true;
	}

	/**
     * @see StepInterface#init( be.ibridge.kettle.trans.step.StepMetaInterface , be.ibridge.kettle.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(FilterRowsMeta)smi;
		data=(FilterRowsData)sdi;

        if (super.init(smi, sdi))
        {
            if (meta.getSendTrueStepname()!=null ^ meta.getSendFalseStepname()!=null)
            {
                logError("Both the 'true' and the 'false' steps need to be supplied, or neither");
            }
            else
            {
                return true;
            }            
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
