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

package be.ibridge.kettle.trans.step.mergerows;

import be.ibridge.kettle.core.Const;
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
 * Filters input rows base on conditions.
 * 
 * @author Matt
 * @since 16-apr-2003, 07-nov-2004 (rewrite)
 */

public class MergeRows extends BaseStep implements StepInterface
{
    private static final Value VALUE_IDENTICAL  = new Value("flag", "identical");
    private static final Value VALUE_CHANGED    = new Value("flag", "changed");
    private static final Value VALUE_NEW        = new Value("flag", "new");
    private static final Value VALUE_DELETED    = new Value("flag", "deleted");
    
	private MergeRowsMeta meta;
	private MergeRowsData data;
	
	public MergeRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MergeRowsMeta)smi;
		data=(MergeRowsData)sdi;

        if (first)
        {
            first = false;
    		data.one=getRowFrom(meta.getReferenceStepName());
            data.two=getRowFrom(meta.getCompareStepName());
        }

        if (data.one==null && data.two==null)
        {
            setOutputDone();
            return false;
        }
        
        if (data.one==null && data.two!=null) // Record 2 is flagged as new!
        {
            data.two.addValue(VALUE_NEW);
            putRow(data.two);
            
            // Also get a next row from compare rowset...
            data.two=getRowFrom(meta.getCompareStepName());
        }
        else
        if (data.one!=null && data.two==null) // Record 1 is flagged as deleted!
        {
            data.one.addValue(VALUE_DELETED);
            putRow(data.one);
            
            // Also get a next row from reference rowset...
            data.one=getRowFrom(meta.getReferenceStepName());
        }
        else
        {
            int compare = data.one.compare(data.two); // TODO: add compare on keys, add keys to meta-data...
            if (compare==0)
            {
                data.one.addValue(VALUE_IDENTICAL);
                putRow(data.one);
                
                // Get a new row from both streams...
                data.one=getRowFrom(meta.getReferenceStepName());
                data.two=getRowFrom(meta.getCompareStepName());
            }
            else
            {
                if (compare<0) // one < two
                {
                    data.one.addValue(VALUE_CHANGED);
                    putRow(data.one); // This is not correct as we are going to lose track, we need a key to compare!
                    data.one=getRowFrom(meta.getReferenceStepName());
                }
                else
                {
                    
                }
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
		meta=(MergeRowsMeta)smi;
		data=(MergeRowsData)sdi;

        if (super.init(smi, sdi))
        {
            if (meta.getReferenceStepName()!=null ^ meta.getCompareStepName()!=null)
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
