package be.ibridge.kettle.core;

public interface ChangedFlagInterface
{
    public boolean hasChanged();
    public void setChanged(boolean changed);
    public void setChanged();
}
