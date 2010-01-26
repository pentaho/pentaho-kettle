package org.pentaho.di.trans.step;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

public class RunThread implements Runnable {

	private static Class<?> PKG = BaseStep.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private StepInterface	step;
	private StepMetaInterface	meta;
	private StepDataInterface	data;
	private LogChannelInterface	log;

	public RunThread(StepMetaDataCombi combi) {
		this.step = combi.step;
		this.meta = combi.meta;
		this.data = combi.data;
		this.log = step.getLogChannel();
	}
	
	public void run() {
		try
		{
			step.setRunning(true);
			if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "System.Log.StartingToRun")); //$NON-NLS-1$

			while (step.processRow(meta, data) && !step.isStopped());
		}
		catch(Throwable t)
		{
		    try
		    {
		        //check for OOME
		        if(t instanceof OutOfMemoryError) {
		            // Handle this different with as less overhead as possible to get an error message in the log.
		            // Otherwise it crashes likely with another OOME in Me$$ages.getString() and does not log
		            // nor call the setErrors() and stopAll() below.
		        	log.logError("UnexpectedError: ", t); //$NON-NLS-1$
		        } else {
		        	log.logError(BaseMessages.getString(PKG, "System.Log.UnexpectedError")+" : ", t); //$NON-NLS-1$ //$NON-NLS-2$
		        }
		        // baseStep.logError(Const.getStackTracker(t));
		    }
		    catch(OutOfMemoryError e)
		    {
		        e.printStackTrace();
		    }
		    finally
		    {
		        step.setErrors(1);
		        step.stopAll();
		    }
		}
		finally
		{
			step.dispose(meta, data);
			try {
	            long li = step.getLinesInput();
	            long lo = step.getLinesOutput();
	            long lr = step.getLinesRead();
	            long lw = step.getLinesWritten();
	            long lu = step.getLinesUpdated();
	            long lj = step.getLinesRejected();
	            long e = step.getErrors();
	            if (li > 0 || lo > 0 || lr > 0 || lw > 0 || lu > 0 || lj > 0 || e > 0)
	            	log.logBasic(BaseMessages.getString(PKG, "BaseStep.Log.SummaryInfo", String.valueOf(li), String.valueOf(lo), String.valueOf(lr), String.valueOf(lw), String.valueOf(lu), String.valueOf(e+lj)));
	            else
	            	log.logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.SummaryInfo", String.valueOf(li), String.valueOf(lo), String.valueOf(lr), String.valueOf(lw), String.valueOf(lu), String.valueOf(e+lj)));
			} catch(Throwable t) {
				//
				// it's likely an OOME, so we don't want to introduce overhead by using BaseMessages.getString(), see above
				//
				log.logError("UnexpectedError: " + t.toString()); //$NON-NLS-1$
			} finally {
				step.markStop();
			}
		}
	}

}
