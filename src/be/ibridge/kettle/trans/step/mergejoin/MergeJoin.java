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

package be.ibridge.kettle.trans.step.mergejoin;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
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
 * Merge rows from 2 sorted streams and output joined rows with matched key fields.
 * Use this instead of hash join is both your input streams are too big to fit in
 * memory. Note that both the inputs must be sorted on the join key.
 * 
 * This is a first prototype implementation that only handles two streams and
 * inner join. It also always outputs all values from both streams. Ideally, we should:
 *   1) Support any number of incoming streams
 *   2) Allow user to choose the join type (inner, outer) for each stream
 *   3) Allow user to choose which fields to push to next step
 *   4) Have multiple output ports as follows:
 *      a) Containing matched records
 *      b) Unmatched records for each input port
 *   5) Support incoming rows to be sorted either on ascending or descending order.
 *      The currently implementation only supports ascending
 * @author Biswapesh
 * @since 24-nov-2006
 */

public class MergeJoin extends BaseStep implements StepInterface
{
	private MergeJoinMeta meta;
	private MergeJoinData data;
	
	public MergeJoin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MergeJoinMeta)smi;
		data=(MergeJoinData)sdi;

        if (first)
        {
            first = false;
            
    		data.one=getRowFrom(meta.getStepName1());
            data.two=getRowFrom(meta.getStepName2());
            
            if (!isInputLayoutValid(data.one, data.two))
            {
            	throw new KettleException(Messages.getString("MergeJoin.Exception.InvalidLayoutDetected"));
            }            

            if (data.one!=null)
            {
                // Find the key indexes:
                data.keyNrs1 = new int[meta.getKeyFields1().length];
                for (int i=0;i<data.keyNrs1.length;i++)
                {
                    data.keyNrs1[i] = data.one.searchValueIndex(meta.getKeyFields1()[i]);
                    if (data.keyNrs1[i]<0)
                    {
                        String message = Messages.getString("MergeJoin.Exception.UnableToFindFieldInReferenceStream",meta.getKeyFields1()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                        logError(message);
                        throw new KettleStepException(message);
                    }
                }
            }

            if (data.two!=null)
            {
                // Find the key indexes:
                data.keyNrs2 = new int[meta.getKeyFields2().length];
                for (int i=0;i<data.keyNrs2.length;i++)
                {
                    data.keyNrs2[i] = data.two.searchValueIndex(meta.getKeyFields2()[i]);
                    if (data.keyNrs2[i]<0)
                    {
                        String message = Messages.getString("MergeJoin.Exception.UnableToFindFieldInReferenceStream",meta.getKeyFields2()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                        logError(message);
                        throw new KettleStepException(message);
                    }
                }
            }

        }

        if (log.isRowLevel()) logRowlevel(Messages.getString("MergeJoin.Log.DataInfo",data.one+"")+data.two); //$NON-NLS-1$ //$NON-NLS-2$

        /*
         * For the time being, implement simple inner join. So, if any input stream is
         * finished, we can stop processing the remaining rows from the other streams
         * @todo Add options for left, right and full outer joins
         */
        if (data.one==null || data.two==null)
        {
            setOutputDone();
            return false;
        }

        int compare = data.one.compare(data.two, data.keyNrs1, data.keyNrs2, null, null);
        switch (compare)
        {
        case 0:
        	// Got a match - so put the result to the output stream
        	data.one.addRow(data.two);
        	putRow(data.one);
        	/*
        	 * @todo This actually is more complex than this. For cases when there are
        	 * dupicate keys, we need to output N1 * N2 rows where:
        	 *    N1 = number of rows with identical keys in stream 1
        	 *    N2 = number of rows with identical keys in stream 2
        	 */
        	data.one = getRowFrom(meta.getStepName1());
        	data.two = getRowFrom(meta.getStepName2());
        	break;
        case 1:
        	// First stream is greater. So read next row from second stream
        	data.two = getRowFrom(meta.getStepName2());
        	break;
        case -1:
        	// Second stream is greater. So read next row from first stream
        	data.one = getRowFrom(meta.getStepName1());
        	break;
        default:
        	// How on earth did we get here? Make sure we do not go into an infinite
        	// loop by continuing to read data
        	data.one = getRowFrom(meta.getStepName1());
    	    data.two = getRowFrom(meta.getStepName2());
    	    break;
        }
        if (checkFeedback(linesRead)) logBasic(Messages.getString("MergeJoin.LineNumber")+linesRead); //$NON-NLS-1$
		return true;
	}

	/**
     * @see StepInterface#init( be.ibridge.kettle.trans.step.StepMetaInterface , be.ibridge.kettle.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(MergeJoinMeta)smi;
		data=(MergeJoinData)sdi;

        if (super.init(smi, sdi))
        {
            if (meta.getStepName1()!=null ^ meta.getStepName2()!=null)
            {
                logError(Messages.getString("MergeJoin.Log.BothTrueAndFalseNeeded")); //$NON-NLS-1$
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether incoming rows are join compatible. This essentially
     * means that the keys being compared should be of the same datatype
     * and both rows should have the same number of keys specified
     * @param row1 Reference row
     * @param row2 Row to compare to
     * 
     * @return true when templates are compatible.
     */
    protected boolean isInputLayoutValid(Row row1, Row row2)
    {
        if (row1!=null && row2!=null)
        {
            // Compare the key types
        	String keyFields1[] = meta.getKeyFields1();
            int nrKeyFields1 = keyFields1.length;
        	String keyFields2[] = meta.getKeyFields2();
            int nrKeyFields2 = keyFields2.length;

            if (nrKeyFields1 != nrKeyFields2)
            {
            	logError("Number of keys do not match " + nrKeyFields1 + " vs " + nrKeyFields2);
            	return false;
            }

            for (int i=0;i<nrKeyFields1;i++)
            {
            	Value v1 = row1.searchValue(keyFields1[i]);
                if (v1 == null)
                {
                	return false;
                }
            	Value v2 = row2.searchValue(keyFields2[i]);
                if (v2 == null)
                {
                	return false;
                }          
                if ( ! v1.equalValueType(v2, true) )
                {
                	return false;
                }
            }
        }
        // we got here, all seems to be ok.
        return true;
    }

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("MergeJoin.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("MergeJoin.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
