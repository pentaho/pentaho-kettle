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

import java.util.ArrayList;
import java.util.Iterator;

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
		int compare;

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

            Value v;
            
            // Calculate one_dummy...
            if (data.one!=null)
            {
                data.one_dummy = new Row(data.one);
            }
            else
            {
                // no input data found, go with the metadata only...
                //
                data.one_dummy = new Row(getTransMeta().getStepFields(meta.getStepMeta1()));
            }
            for (int i=0; i < data.one_dummy.size(); ++i)
            {
            	v = data.one_dummy.getValue(i);
            	v.setNull();
            	data.one_dummy.setValue(i, v);
            }
            
            // Calculate two_dummy...
            //
            if (data.two!=null)
            {
                data.two_dummy = new Row(data.two);
            }
            else
            {
                // no input data found, go with the metadata only...
                //
                data.two_dummy = new Row(getTransMeta().getStepFields(meta.getStepMeta2()));
            }
            for (int i=0; i < data.two_dummy.size(); ++i)
            {
            	v = data.two_dummy.getValue(i);
            	v.setNull();
            	data.two_dummy.setValue(i, v);
            }
            
            // Verify that the 2 rows (one_dummy and two_dummy) all have different field names...
            for (int i=0;i<data.one_dummy.size();i++)
            {
                Value dummy = data.one_dummy.getValue(i);
                int idx = data.two_dummy.searchValueIndex(dummy.getName());
                if (idx>=0)
                {
                    throw new KettleStepException(Messages.getString("MergeJoin.Exception.DuplicateFieldnamesInResult", dummy.getName()));
                }
            }
        }

        if (log.isRowLevel()) logRowlevel(Messages.getString("MergeJoin.Log.DataInfo",data.one+"")+data.two); //$NON-NLS-1$ //$NON-NLS-2$

        /*
         * We can stop processing if any of the following is true:
         *   a) Both streams are empty
         *   b) First stream is empty and join type is INNER or LEFT OUTER
         *   c) Second stream is empty and join type is INNER or RIGHT OUTER
         */
        if ((data.one == null && data.two == null) ||
        	(data.one == null && data.one_optional == false) ||
        	(data.two == null && data.two_optional == false))
        {
        	// Before we stop processing, we have to make sure that all rows from both input streams are depleted!
        	// If we don't do this, the transformation can stall.
        	//
        	while (data.one!=null && !isStopped()) data.one=getRowFrom(meta.getStepName1());
            while (data.two!=null && !isStopped()) data.two=getRowFrom(meta.getStepName2());
            
            setOutputDone();
            return false;
        }

        if (data.one == null)
        {
        	compare = -1;
        }
        else 
        {
            if (data.two == null)
            {
                compare = 1;
            }
            else
        	{
                int cmp = data.one.compare(data.two, data.keyNrs1, data.keyNrs2, null, null);
                compare = cmp>0?1 : cmp<0?-1 : 0;
            }
        }
        
        switch (compare)
        {
        case 0:
        	/*
        	 * We've got a match. This is what we do next (to handle duplicate keys correctly):
        	 *   Read the next record from both streams
        	 *   If any of the keys match, this means we have duplicates. We therefore
        	 *     Create an array of all rows that have the same keys
        	 *     Push a cartesian product of the two arrays to output
        	 *   Else
        	 *     Just push the combined rowset to output
        	 */ 
        	data.one_next = getRowFrom(meta.getStepName1());
        	data.two_next = getRowFrom(meta.getStepName2());
        	int compare1 = (data.one_next == null) ? -1 : data.one.compare(data.one_next, data.keyNrs1, data.keyNrs1, null, null);
        	int compare2 = (data.two_next == null) ? -1 : data.two.compare(data.two_next, data.keyNrs2, data.keyNrs2, null, null);
        	if (compare1 == 0 || compare2 == 0) // Duplicate keys
        	{
            	if (data.ones == null)
            		data.ones = new ArrayList();
            	else
            		data.ones.clear();
            	if (data.twos == null)
            		data.twos = new ArrayList();
            	else
            		data.twos.clear();
            	data.ones.add(data.one);
            	if (compare1 == 0) // First stream has duplicates
            	{
            		data.ones.add(data.one_next);
	            	for (;;)
	            	{
	                	data.one_next = getRowFrom(meta.getStepName1());
	                	if (0 != ((data.one_next == null) ? -1 : data.one.compare(data.one_next, data.keyNrs1, data.keyNrs1, null, null)))
	                		break;
	                	data.ones.add(data.one_next);
	            	}
            	}
            	data.twos.add(data.two);
            	if (compare2 == 0) // Second stream has duplicates
            	{
            		data.twos.add(data.two_next);
	            	for (;;)
	            	{
	                	data.two_next = getRowFrom(meta.getStepName2());
	                	if (0 != ((data.two_next == null) ? -1 : data.two.compare(data.two_next, data.keyNrs2, data.keyNrs2, null, null)))
	                		break;
	                	data.twos.add(data.two_next);
	            	}
            	}
            	Iterator one_iter = data.ones.iterator();
            	while (one_iter.hasNext())
            	{
            		Row one = (Row) one_iter.next();
            		Iterator two_iter = data.twos.iterator();
            		while (two_iter.hasNext())
            		{
            			Row combi = new Row(one);
            			combi.addRow((Row) two_iter.next());
            			putRow(combi);
            		}
            	}
            	data.ones.clear();
            	data.twos.clear();
        	}
        	else // No duplicates
        	{
	        	data.one.addRow(data.two);
	        	putRow(data.one);
        	}
        	data.one = data.one_next;
        	data.two = data.two_next;
        	break;
        case 1:
        	logDebug("First stream has missing key");
        	/*
        	 * First stream is greater than the second stream. This means:
        	 *   a) This key is missing in the first stream
        	 *   b) Second stream may have finished
        	 *  So, if full/right outer join is set and 2nd stream is not null,
        	 *  we push a record to output with only the values for the second
        	 *  row populated. Next, if 2nd stream is not finished, we get a row
        	 *  from it; otherwise signal that we are done
        	 */
        	if (data.one_optional == true)
        	{
        		if (data.two != null)
        		{
	        		Row combi = new Row(data.one_dummy);
	        		combi.addRow(data.two);
	        		putRow(combi);
	        		data.two = getRowFrom(meta.getStepName2());
        		}
        		else if (data.two_optional == false)
        		{
        			/*
        			 * If we are doing right outer join then we are done since
        			 * there are no more rows in the second set
        			 */
                	// Before we stop processing, we have to make sure that all rows from both input streams are depleted!
                	// If we don't do this, the transformation can stall.
                	//
                	while (data.one!=null && !isStopped()) data.one=getRowFrom(meta.getStepName1());
                    while (data.two!=null && !isStopped()) data.two=getRowFrom(meta.getStepName2());

        			setOutputDone();
        			return false;
        		}
        		else
        		{
        			/*
        			 * We are doing full outer join so print the 1st stream and
        			 * get the next row from 1st stream
        			 */
	        		Row combi = new Row(data.one);
	        		combi.addRow(data.two_dummy);
	        		putRow(combi);
	        		data.one = getRowFrom(meta.getStepName1());
        		}
        	}
        	else if (data.two == null && data.two_optional == true)
        	{
        		/**
        		 * We have reached the end of stream 2 and there are records
        		 * present in the first stream. Also, join is left or full outer.
        		 * So, create a row with just the values in the first stream
        		 * and push it forward
        		 */
        		Row combi = new Row(data.one);
        		combi.addRow(data.two_dummy);
        		putRow(combi);
        		data.one = getRowFrom(meta.getStepName1());
        	}
        	else if (data.two != null)
        	{
        		/*
        		 * We are doing an inner or left outer join, so throw this row away
        		 * from the 2nd stream
        		 */
        		data.two = getRowFrom(meta.getStepName2());
        	}
        	break;
        case -1:
        	logDebug("Second stream has missing key");
        	/*
        	 * Second stream is greater than the first stream. This means:
        	 *   a) This key is missing in the second stream
        	 *   b) First stream may have finished
        	 *  So, if full/left outer join is set and 1st stream is not null,
        	 *  we push a record to output with only the values for the first
        	 *  row populated. Next, if 1st stream is not finished, we get a row
        	 *  from it; otherwise signal that we are done
        	 */
        	if (data.two_optional == true)
        	{
        		if (data.one != null)
        		{
	        		Row combi = new Row(data.one);
	        		combi.addRow(data.two_dummy);
	        		putRow(combi);
	        		data.one = getRowFrom(meta.getStepName1());
        		}
        		else if (data.one_optional == false)
        		{
        			/*
        			 * We are doing a left outer join and there are no more rows
        			 * in the first stream; so we are done
        			 */
                	// Before we stop processing, we have to make sure that all rows from both input streams are depleted!
                	// If we don't do this, the transformation can stall.
                	//
                	while (data.one!=null && !isStopped()) data.one=getRowFrom(meta.getStepName1());
                    while (data.two!=null && !isStopped()) data.two=getRowFrom(meta.getStepName2());

        			setOutputDone();
        			return false;
        		}
        		else
        		{
        			/*
        			 * We are doing a full outer join so print the 2nd stream and
        			 * get the next row from the 2nd stream
        			 */
	        		Row combi = new Row(data.one_dummy);
	        		combi.addRow(data.two);
	        		putRow(combi);
	        		data.two = getRowFrom(meta.getStepName2());
        		}
        	}
        	else if (data.one == null && data.one_optional == true)
        	{
        		/*
        		 * We have reached the end of stream 1 and there are records
        		 * present in the second stream. Also, join is right or full outer.
        		 * So, create a row with just the values in the 2nd stream
        		 * and push it forward
        		 */
        		Row combi = new Row(data.one_dummy);
        		combi.addRow(data.two);
        		putRow(combi);
        		data.two = getRowFrom(meta.getStepName2());
        	}
        	else if (data.one != null)
        	{
        		/*
        		 * We are doing an inner or right outer join so a non-matching row
        		 * in the first stream is of no use to us - throw it away and get the
        		 * next row
        		 */
        		data.one = getRowFrom(meta.getStepName1());
        	}
        	break;
        default:
        	logDebug("We shouldn't be here!!");
        	// Make sure we do not go into an infinite loop by continuing to read data
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
            if (meta.getStepName1()==null || meta.getStepName2()==null)
            {
                logError(Messages.getString("MergeJoin.Log.BothTrueAndFalseNeeded")); //$NON-NLS-1$
                return false;
            }
            String joinType = meta.getJoinType();
            for (int i = 0; i < MergeJoinMeta.join_types.length; ++i)
            {
            	if (joinType.equalsIgnoreCase(MergeJoinMeta.join_types[i]))
            	{
            		data.one_optional = MergeJoinMeta.one_optionals[i];
            		data.two_optional = MergeJoinMeta.two_optionals[i];
            		return true;
            	}
            }
           	logError(Messages.getString("MergeJoin.Log.InvalidJoinType", meta.getJoinType())); //$NON-NLS-1$
               return false;
        }
        return true;
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
