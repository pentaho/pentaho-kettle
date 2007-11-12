/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.step;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.trans.Messages;

public class StepInitThread implements Runnable
{
    public boolean ok;
    public boolean finished;
    
    private StepMetaDataCombi combi;

    private LogWriter log;
    
    public StepInitThread(StepMetaDataCombi combi, LogWriter log)
    {
        this.combi = combi;
        this.log = log;
        this.ok = false;
        this.finished=false;
    }
    
    public String toString()
    {
        return combi.stepname;
    }
    
    public void run()
    {
        // Set the internal variables also on the init thread!
        // ((BaseStep)combi.step).setInternalVariables();
        
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
