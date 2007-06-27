package org.pentaho.di.trans.steps.abort;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;


/**
 * Step that will abort after having seen 'x' number of rows on its input.
 * 
 * @author Sven Boden
 */
public class Abort extends BaseStep implements StepInterface {

    private AbortMeta meta;
    private AbortData data;
    private int nrInputRows;
    private int nrThresholdRows;
    
    public Abort(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AbortMeta)smi;
		data=(AbortData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			nrInputRows = 0;
			String threshold = environmentSubstitute(meta.getRowThreshold());
			nrThresholdRows = Const.toInt(threshold, -1);
			if ( nrThresholdRows < 0 )
			{
			    logError(Messages.getString("Abort.Log.ThresholdInvalid", threshold));
			}
			
		    return true;
		}
		return false;
	} 
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta=(AbortMeta)smi;
		data=(AbortData)sdi;
		
        Object[] r=getRow();  // Get row from input rowset & set row busy!
        if (r==null)          // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        else
        {
        	putRow(getInputRowMeta(), r);
        	nrInputRows++;
        	if ( nrInputRows > nrThresholdRows)
        	{
        	   //
        	   // Here we abort!!
        	   //
        	   logMinimal(Messages.getString("Abort.Log.Wrote.AbortRow", Long.toString(nrInputRows), r.toString()) );
        		
        	   String message = environmentSubstitute(meta.getMessage());
        	   if ( message == null || message.length() == 0 )
        	   {
        		   logMinimal(Messages.getString("Abort.Log.DefaultAbortMessage", "" + nrInputRows));
        	   }
        	   else
        	   {
        		   logMinimal(message);
        	   }
               setErrors(1);
               stopAll();        	   
        	}
        	else 
        	{
        		// seen a row but not yet reached the threshold
        		if ( meta.isAlwaysLogRows() )
        		{
        			logMinimal(Messages.getString("Abort.Log.Wrote.Row", Long.toString(nrInputRows), r.toString()) );
        		}
        		else
        		{
        	        if (log.isRowLevel())
        	        {        	        	
        	            logRowlevel(Messages.getString("Abort.Log.Wrote.Row", Long.toString(nrInputRows), r.toString()) );
        	        }
        		}
        	}
        }
        
        return true;
    }

    //
    // Run is were the action happens!
    public void run()
    {
        try
        {
        	logBasic(Messages.getString("Abort.Log.StartingToRun")); //$NON-NLS-1$ 
            while (processRow(meta, data) && !isStopped());
        }
        catch(Exception e)
        {
        	logError(Messages.getString("Abort.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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