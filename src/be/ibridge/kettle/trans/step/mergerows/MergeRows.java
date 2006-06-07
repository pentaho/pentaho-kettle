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
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Merge rows from 2 sorted streams to detect changes.
 * Use this as feed for a dimension in case you have no time stamps in your source system.
 * 
 * @author Matt
 * @since 19-dec-2005
 */

public class MergeRows extends BaseStep implements StepInterface
{
    private static final Value VALUE_IDENTICAL  = new Value("flag", "identical"); //$NON-NLS-1$ //$NON-NLS-2$
    private static final Value VALUE_CHANGED    = new Value("flag", "changed"); //$NON-NLS-1$ //$NON-NLS-2$
    private static final Value VALUE_NEW        = new Value("flag", "new"); //$NON-NLS-1$ //$NON-NLS-2$
    private static final Value VALUE_DELETED    = new Value("flag", "deleted"); //$NON-NLS-1$ //$NON-NLS-2$
    
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
            
            if (data.one!=null)
            {
                // Find the key indexes:
                data.keyNrs = new int[meta.getKeyFields().length];
                data.keyAsc = new boolean[meta.getKeyFields().length];
                for (int i=0;i<data.keyNrs.length;i++)
                {
                    data.keyNrs[i] = data.one.searchValueIndex(meta.getKeyFields()[i]);
                    if (data.keyNrs[i]<0)
                    {
                        String message = Messages.getString("MergeRows.Exception.UnableToFindFieldInReferenceStream",meta.getKeyFields()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                        logError(message);
                        throw new KettleStepException(message);
                    }
                    data.keyAsc[i] = true;
                }
            }
            
            if (data.two!=null)
            {
                data.valueNrs = new int[meta.getValueFields().length];
                data.valueAsc = new boolean[meta.getValueFields().length];
                for (int i=0;i<data.valueNrs.length;i++)
                {
                    data.valueNrs[i] = data.one.searchValueIndex(meta.getValueFields()[i]);
                    if (data.valueNrs[i]<0)
                    {
                        String message = Messages.getString("MergeRows.Exception.UnableToFindFieldInReferenceStream",meta.getValueFields()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                        logError(message);
                        throw new KettleStepException(message);
                    }
                    data.valueAsc[i] = true;
                }
            }
        }

        if (log.isRowLevel()) logRowlevel(Messages.getString("MergeRows.Log.DataInfo",data.one+"")+data.two); //$NON-NLS-1$ //$NON-NLS-2$
        
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
        else  // OK, Here is the real start of the compare code!
        {
            int compare = data.one.compare(data.two, data.keyNrs, data.keyAsc);
            if (compare==0)  // The Key matches, we CAN compare the two rows...
            {
                int compareValues = data.one.compare(data.two, data.valueNrs, data.valueAsc);
                if (compareValues==0)
                {
                    data.one.addValue(VALUE_IDENTICAL);
                    putRow(data.one);
                }
                else
                {
                    data.two.addValue(VALUE_CHANGED);
                    putRow(data.two);
                }
                
                // Get a new row from both streams...
                data.one=getRowFrom(meta.getReferenceStepName());
                data.two=getRowFrom(meta.getCompareStepName());
            }
            else 
            {
                if (compare<0) // one < two
                {
                    data.one.addValue(VALUE_DELETED);
                    putRow(data.one);
                    data.one=getRowFrom(meta.getReferenceStepName());
                }
                else
                {
                    data.two.addValue(VALUE_NEW);
                    putRow(data.two);
                    data.two=getRowFrom(meta.getCompareStepName());
                }
            }
        }
        
        if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("MergeRows.LineNumber")+linesRead); //$NON-NLS-1$
			
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
                logError(Messages.getString("MergeRows.Log.BothTrueAndFalseNeeded")); //$NON-NLS-1$
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
			logBasic(Messages.getString("MergeRows.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("MergeRows.Log.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
