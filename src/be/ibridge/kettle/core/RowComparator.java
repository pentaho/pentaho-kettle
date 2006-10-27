package be.ibridge.kettle.core;

import java.util.Comparator;

public class RowComparator implements Comparator
{
    private int[]     fieldIndices;

    private boolean[] ascending;

    private boolean[] caseInsensitive;

    public RowComparator()
    {
    }
    
    /**
     * @param fieldIndices the field indices
     * @param ascending the ascending flags
     */
    public RowComparator(int[] fieldIndices, boolean[] ascending)
    {
        this(fieldIndices, ascending, null);
    }

    /**
     * @param fieldIndices the field indices
     * @param ascending the ascending flags
     * @param caseInsensitive the "case insensitive" flags
     */
    public RowComparator(int[] fieldIndices, boolean[] ascending, boolean[] caseInsensitive)
    {
        super();
        this.fieldIndices = fieldIndices;
        this.ascending = ascending;
        this.caseInsensitive = caseInsensitive;
    }



    public int compare(Object o1, Object o2)
    {
        if (ascending != null && fieldIndices != null)
        {
            return ((Row) o1).compare((Row) o2, fieldIndices, ascending, caseInsensitive);
        }
        else
        {
            return ((Row) o1).compare((Row) o2);
        }
    }

    /**
     * @return the ascending flags
     */
    public boolean[] getAscending()
    {
        return ascending;
    }

    /**
     * @param ascending the ascending flags to set
     */
    public void setAscending(boolean[] ascending)
    {
        this.ascending = ascending;
    }

    /**
     * @return the "case insensitive" flags
     */
    public boolean[] getCaseInsensitive()
    {
        return caseInsensitive;
    }

    /**
     * @param caseInsensitive the "case insensitive" flags to set
     */
    public void setCaseInsensitive(boolean[] caseInsensitive)
    {
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * @return the field indices
     */
    public int[] getFieldIndices()
    {
        return fieldIndices;
    }

    /**
     * @param indices the field indices to set
     */
    public void setFieldIndices(int[] indices)
    {
        this.fieldIndices = indices;
    }

}
