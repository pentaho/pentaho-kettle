package org.pentaho.di.core.changed;

public class ChangedFlag implements ChangedFlagInterface
{
    private boolean changed;
    
    public ChangedFlag()
    {
        changed = false;
    }
    
    public boolean hasChanged()
    {
        return changed;
    }
    
    public void setChanged(boolean changed)
    {
        this.changed = changed;
    }
    
    public void setChanged()
    {
        this.changed = true;
    }
}
