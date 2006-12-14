package be.ibridge.kettle.spoon;

public interface TabItemInterface
{
    /** 
     * Closes the content behind the tab, de-allocates resources.
     * 
     * @return true if the tab was closed, false if it was prevented by the user. (are you sure dialog)
     */
    public boolean close();
    
    public Object getManagedObject();
}
