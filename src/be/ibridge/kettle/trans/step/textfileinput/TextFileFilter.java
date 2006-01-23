package be.ibridge.kettle.trans.step.textfileinput;

public class TextFileFilter implements Cloneable
{
    /** The position of the occurence of the filter string to check at */
    private int                filterPosition;

    /** The string to filter on */
    private String             filterString;

    /** True if we want to stop when we reach a filter line */
    private boolean            filterLastLine;

    /**
     * @param filterPosition The position of the occurence of the filter string to check at
     * @param filterString   The string to filter on
     * @param filterLastLine True if we want to stop when we reach a filter string on the specified position
     *                       False if you just want to skip the line.
     */
    public TextFileFilter(int filterPosition, String filterString, boolean filterLastLine)
    {
        this.filterPosition = filterPosition;
        this.filterString = filterString;
        this.filterLastLine = filterLastLine;
    }

    public TextFileFilter()
    {
    }

    public Object clone()
    {
        try
        {
            Object retval = super.clone();
            return retval;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }
    
    /**
     * @return Returns the filterLastLine.
     */
    public boolean isFilterLastLine()
    {
        return filterLastLine;
    }

    /**
     * @param filterLastLine The filterLastLine to set.
     */
    public void setFilterLastLine(boolean filterLastLine)
    {
        this.filterLastLine = filterLastLine;
    }

    /**
     * @return Returns the filterPosition.
     */
    public int getFilterPosition()
    {
        return filterPosition;
    }

    /**
     * @param filterPosition The filterPosition to set.
     */
    public void setFilterPosition(int filterPosition)
    {
        this.filterPosition = filterPosition;
    }

    /**
     * @return Returns the filterString.
     */
    public String getFilterString()
    {
        return filterString;
    }

    /**
     * @param filterString The filterString to set.
     */
    public void setFilterString(String filterString)
    {
        this.filterString = filterString;
    }

    
    
    
}
