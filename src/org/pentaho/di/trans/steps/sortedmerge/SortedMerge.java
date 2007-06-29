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
 
package org.pentaho.di.trans.steps.sortedmerge;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class SortedMerge extends BaseStep implements StepInterface
{
	private SortedMergeMeta meta;
	private SortedMergeData data;
	
	public SortedMerge(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
    
    /**
     * We read from all streams in the partition mergin mode
     * For that we need at least one row on all input rowsets...
     * If we don't have a row, we wait for one.
     * 
     * TODO: keep the inputRowSets() list sorted and go from there. That should dramatically improve speed as you only need half as many comparissons.
     * 
     * @return the next row
     */
    private synchronized Object[] getRowSorted() throws KettleException
    {
        int smallestId = 0;
        Object[] smallestRow = null;
        
        if (inputRowSets.size()==0) return null;

        // Sort & binary search...
        /*
        Collections.sort(inputRowSets, data.rowComparator);
        int idx = Collections.binarySearch(inputRowSets, new Row(), data.rowComparator);
        */
        
        for (int i=0;i<inputRowSets.size();i++)
        {
            RowSet rowSet = (RowSet)inputRowSets.get(i);

            // First see if the input rowset is empty & done...
            while (rowSet!=null && rowSet.isEmpty() && rowSet.isDone()) // nothing more here: remove it from input
            {
                inputRowSets.remove(i); // inputRowSets.size() became smaller!
                if (inputRowSets.size()==0) // All done, no more rows to be found! 
                {
                    return null;
                }
                
                if (i<inputRowSets.size())
                {
                    rowSet = (RowSet)inputRowSets.get(i);
                }
                else
                {
                    rowSet = null;
                }
            }

            if (rowSet!=null)
            {
                // If it's empty : wait
                int sleeptime=getTransMeta().getSleepTimeEmpty();
                while (rowSet.isEmpty() && !stopped)
                {
                    try { if (sleeptime>0) sleep(0, sleeptime); else super.notifyAll(); } 
                    catch(Exception e) 
                    { 
                        logError(Messages.getString("BaseStep.Log.SleepInterupted")+e.toString()); //$NON-NLS-1$
                        setErrors(1); 
                        stopAll(); 
                        return null; 
                    }
                    if (sleeptime<100) sleeptime = ((int)(sleeptime*1.2))+1; else sleeptime=100; 
                    setNrGetSleeps(getNrGetSleeps()+sleeptime);
                }
                
                if (stopped) return null;
                
                // OK, now get the row and compare with smallest
                Object[] row = rowSet.lookAtFirst();

                if (data.rowMeta==null) {
                	//get the RowMeta 
                	data.rowMeta=rowSet.getRowMeta();
                	// Set the sorted properties: ascending/descending
                	meta.getFields(data.rowMeta, getStepname(), null, null);                	
                }
                
                if (smallestRow==null)
                {
                    smallestRow = row;
                    smallestId = i;
                }
                else
                {
                    // What fields do we compare on and in what order?
    
                    // Better cache the location of the partitioning column
                    // First time operation only
                    //
                    if (data.fieldIndices==null)
                    {
                        // Get the indexes of the specified sort fields...
                        data.fieldIndices = new int[meta.getFieldName().length];
                        for (int f=0;f<data.fieldIndices.length;f++)
                        {
                            data.fieldIndices[f] = data.rowMeta.indexOfValue(meta.getFieldName()[f]);
                            if (data.fieldIndices[f]<0)
                            {
                                throw new KettleStepException("Unable to find fieldname ["+meta.getFieldName()[f]+"] in row : "+row);
                            }
                        }
                    }
    
                    // Do the compare.
                    if (data.rowMeta.compare(row, smallestRow, data.fieldIndices)<0)
                    {
                        smallestRow = row;
                        smallestId = i;
                    }
                }
            }
        }
        
        // OK then, take one row from the inputrow with the smallest record...
        Object[] row = null;
        if (!isSafeModeEnabled())
        {
        	row = ((RowSet)inputRowSets.get(smallestId)).getRow();
        } 
        else // OK, before we return the row, let's see if we need to check on mixing row compositions...
        {
        	// for checking we need to get data and meta
        	RowSet rowSet=((RowSet)inputRowSets.get(smallestId));
        	row = rowSet.getRow();
            safeModeChecking(rowSet.getRowMeta());            
        }
        
        // Notify all rowlisteners...
        for (int i=0;i<getRowListeners().size();i++)
        {
            RowListener rowListener = (RowListener)getRowListeners().get(i);
            rowListener.rowReadEvent(data.rowMeta, row);
        }
        
        return row;
    }
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SortedMergeMeta)smi;
		data=(SortedMergeData)sdi;

		Object[] row=getRowSorted();    // get row, sorted
		if (row==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
		putRow(data.rowMeta,row);     // copy row to possible alternate rowset(s).

        if (checkFeedback(linesRead)) logBasic(Messages.getString("SortedMerge.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SortedMergeMeta)smi;
		data=(SortedMergeData)sdi;
		
		if (super.init(smi, sdi))
		{
            //data.rowComparator = new RowComparator();
            
		    // Add init code here.
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
			logBasic(Messages.getString("SortedMerge.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("SortedMerge.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
