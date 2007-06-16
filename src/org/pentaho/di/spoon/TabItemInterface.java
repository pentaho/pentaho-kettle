package org.pentaho.di.spoon;



public interface TabItemInterface
{
    /** 
     * Closes the content behind the tab, de-allocates resources.
     * 
     * @return true if the tab was closed, false if it was prevented by the user. (are you sure dialog)
     */
    public boolean canBeClosed();
    public Object getManagedObject();
    public boolean hasContentChanged();    
    public int showChangedWarning();
    public boolean applyChanges();
    
}
