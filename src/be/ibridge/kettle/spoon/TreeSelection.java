package be.ibridge.kettle.spoon;

public class TreeSelection
{
    private Object selection;

    private Object parent;

    private Object grandParent;

    /**
     * @param selection
     * @param parent
     * @param grandParent
     */
    public TreeSelection(Object selection, Object parent, Object grandParent)
    {
        this.selection = selection;
        this.parent = parent;
        this.grandParent = grandParent;
    }

    /**
     * @param selection
     * @param parent
     */
    public TreeSelection(Object selection, Object parent)
    {
        this(selection, parent, null);
    }

    /**
     * @param selection
     */
    public TreeSelection(Object selection)
    {
        this(selection, null, null);
    }

    /**
     * @return the grandParent
     */
    public Object getGrandParent()
    {
        return grandParent;
    }

    /**
     * @param grandParent the grandParent to set
     */
    public void setGrandParent(Object grandParent)
    {
        this.grandParent = grandParent;
    }

    /**
     * @return the parent
     */
    public Object getParent()
    {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Object parent)
    {
        this.parent = parent;
    }

    /**
     * @return the selection
     */
    public Object getSelection()
    {
        return selection;
    }

    /**
     * @param selection the selection to set
     */
    public void setSelection(Object selection)
    {
        this.selection = selection;
    }

}
