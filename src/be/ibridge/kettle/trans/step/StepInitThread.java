package be.ibridge.kettle.trans.step;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.trans.Messages;

public class StepInitThread implements Runnable
{
    private static final LocalVariables localVariables = LocalVariables.getInstance();
    public boolean ok;
    public boolean finished;
    
    private StepMetaDataCombi combi;

    private LogWriter log;
    
    private Thread parentThread;

    public StepInitThread(StepMetaDataCombi combi, LogWriter log)
    {
        this.combi = combi;
        this.log = log;
        this.ok = false;
        this.finished=false;
        
        this.parentThread = Thread.currentThread();
    }
    
    public String toString()
    {
        return combi.stepname;
    }
    
    public void run()
    {
        // Add a new KettleVariable for this new Thread so that init() code also has access to these variables...
        localVariables.createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);
        
        try
        {
            if (combi.step.init(combi.meta, combi.data))
            {
                combi.data.setStatus(StepDataInterface.STATUS_IDLE);
                ok = true;
            }
            else
            {
                combi.step.setErrors(1);
                log.logError(toString(), Messages.getString("Trans.Log.ErrorInitializingStep", combi.step.getStepname())); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (Throwable e)
        {
            log.logError(toString(), Messages.getString("Trans.Log.ErrorInitializingStep", combi.step.getStepname())); //$NON-NLS-1$ //$NON-NLS-2$
            log.logError(toString(), Const.getStackTracker(e));
        }
        
        // Chuck away the KettleVariables, otherwise it leaks big time.
        localVariables.removeKettleVariables(Thread.currentThread().getName());
        
        finished=true;
    }
    
    public boolean isFinished()
    {
        return finished;
    }
    
    public boolean isOk()
    {
        return ok;
    }

    /**
     * @return Returns the combi.
     */
    public StepMetaDataCombi getCombi()
    {
        return combi;
    }

    /**
     * @param combi The combi to set.
     */
    public void setCombi(StepMetaDataCombi combi)
    {
        this.combi = combi;
    }
}
