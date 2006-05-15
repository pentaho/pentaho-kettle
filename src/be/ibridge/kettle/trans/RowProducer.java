package be.ibridge.kettle.trans;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
import be.ibridge.kettle.trans.step.StepInterface;

/**
 * Allows you to "Feed" rows to a step.
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
    
    public void putRow(Row row)
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
