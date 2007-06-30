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

package org.pentaho.di.trans.steps.mergerows;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Merge rows from 2 sorted streams to detect changes.
 * Use this as feed for a dimension in case you have no time stamps in your source system.
 * 
 * @author Matt
 * @since 19-dec-2005
 */

public class MergeRows extends BaseStep implements StepInterface
{
    private static final String VALUE_IDENTICAL  = "identical"; //$NON-NLS-1$ 
    private static final String VALUE_CHANGED    = "changed"; //$NON-NLS-1$
    private static final String VALUE_NEW        = "new"; //$NON-NLS-1$
    private static final String VALUE_DELETED    = "deleted"; //$NON-NLS-1$ 
    
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
            
            try
            {
                checkInputLayoutValid(data.one, data.two);
            }
            catch(KettleRowException e)
            {
            	throw new KettleException(Messages.getString("MergeRows.Exception.InvalidLayoutDetected"), e);
            }            

            if (data.one!=null)
            {
                // Find the key indexes:
                data.keyNrs = new int[meta.getKeyFields().length];
                data.keyAsc = new boolean[meta.getKeyFields().length];
                for (int i=0;i<data.keyNrs.length;i++)
                {
                    data.keyNrs[i] = data.one.getRowMeta().indexOfValue(meta.getKeyFields()[i]);
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
                    data.valueNrs[i] = data.two.getRowMeta().indexOfValue(meta.getValueFields()[i]);
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
        
        if (data.outputRowMeta==null)
        {
            data.outputRowMeta = new RowMeta();
            if (data.one!=null)
            {
                meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.one.getRowMeta() }, null, this);
            }
            else
            {
                meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.two.getRowMeta() }, null, this);
            }
        }

        Object[] outputRow = null;
        String flagField = null;
        
        if (data.one==null && data.two!=null) // Record 2 is flagged as new!
        {
            outputRow = data.two.getData();
            flagField = VALUE_NEW;

            // Also get a next row from compare rowset...
            data.two=getRowFrom(meta.getCompareStepName());
        }
        else
        if (data.one!=null && data.two==null) // Record 1 is flagged as deleted!
        {
            outputRow = data.one.getData();
            flagField = VALUE_DELETED;
            
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
                    outputRow = data.one.getData();
                    flagField = VALUE_IDENTICAL;
                }
                else
                {
                    // Return the compare (most recent) row
                    //
                    outputRow = data.two.getData();
                    flagField = VALUE_CHANGED;
                }

                // Get a new row from both streams...
                data.one=getRowFrom(meta.getReferenceStepName());
                data.two=getRowFrom(meta.getCompareStepName());
            }
            else 
            {
                if (compare<0) // one < two
                {
                    outputRow = data.one.getData();
                    flagField = VALUE_DELETED;

                    data.one=getRowFrom(meta.getReferenceStepName());
                }
                else
                {
                    outputRow = data.two.getData();
                    flagField = VALUE_NEW;

                    data.two=getRowFrom(meta.getCompareStepName());
                }
            }
        }
        
        // send the row to the next steps...
        putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, flagField));

        if (checkFeedback(linesRead)) logBasic(Messages.getString("MergeRows.LineNumber")+linesRead); //$NON-NLS-1$

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

    /**
     * Checks whether 2 template rows are compatible for the mergestep. 
     * 
     * @param referenceRow Reference row
     * @param compareRow Row to compare to
     * 
     * @return true when templates are compatible.
     * @throws KettleRowException in case there is a compatibility error.
     */
    protected void checkInputLayoutValid(RowMetaAndData referenceRow, RowMetaAndData compareRow) throws KettleRowException
    {
        if (referenceRow!=null && compareRow!=null)
        {
            BaseStep.safeModeChecking(referenceRow.getRowMeta(), compareRow.getRowMeta());
        }
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
			logError(Messages.getString("MergeRows.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
