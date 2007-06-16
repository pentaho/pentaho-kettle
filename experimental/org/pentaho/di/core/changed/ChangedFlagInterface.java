package org.pentaho.di.core.changed;

public interface ChangedFlagInterface
{
    public boolean hasChanged();
    public void setChanged(boolean changed);
    public void setChanged();
}
