package be.ibridge.kettle.spoon;

import org.eclipse.swt.custom.CTabItem;

public class TabMapEntry
{
    private CTabItem tabItem;
    
    private String objectName;

    private TabItemInterface object;

    /**
     * @param tabName
     * @param objectName
     * @param objectType
     * @param object
     */
    public TabMapEntry(CTabItem tabItem, String objectName, TabItemInterface object)
    {
        this.tabItem = tabItem;
        this.objectName = objectName;
        this.object = object;
    }

    /**
     * @return the objectName
     */
    public String getObjectName()
    {
        return objectName;
    }

    /**
     * @param objectName the objectName to set
     */
    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    /**
     * @return the object
     */
    public TabItemInterface getObject()
    {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(TabItemInterface object)
    {
        this.object = object;
    }

    /**
     * @return the tabItem
     */
    public CTabItem getTabItem()
    {
        return tabItem;
    }

    /**
     * @param tabItem the tabItem to set
     */
    public void setTabItem(CTabItem tabItem)
    {
        this.tabItem = tabItem;
    }
}
