package org.pentaho.di.trans;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * Allows you to "Inject" rows into a step.
 * 
 * @author Matt
 *
 */
public class RowProducer
{
    private RowSet rowSet;
    private StepInterface stepInterface;
    
    public RowProducer(StepInterface stepInterface, RowSet rowSet)
    {
        this.stepInterface = stepInterface;
        this.rowSet = rowSet;
    }
    
    public void putRow(RowMetaInterface rowMeta, Object[] row)
    {
        rowSet.putRow(row);
    }
    
    public void finished()
    {
        rowSet.setDone();
    }

    /**
     * @return Returns the rowSet.
     */
    public RowSet getRowSet()
    {
        return rowSet;
    }

    /**
     * @param rowSet The rowSet to set.
     */
    public void setRowSet(RowSet rowSet)
    {
        this.rowSet = rowSet;
    }

    /**
     * @return Returns the stepInterface.
     */
    public StepInterface getStepInterface()
    {
        return stepInterface;
    }

    /**
     * @param stepInterface The stepInterface to set.
     */
    public void setStepInterface(StepInterface stepInterface)
    {
        this.stepInterface = stepInterface;
    }
    
    
}
