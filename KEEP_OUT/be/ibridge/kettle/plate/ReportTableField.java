package be.ibridge.kettle.plate;

/**
 * This class contains all the information about a Reporting table field
 * @author Matt
 *
 */
public class ReportTableField
{
    public static final int SORT_TYPE_NONE       = 0;
    public static final int SORT_TYPE_ASCENDING  = 1;
    public static final int SORT_TYPE_DESCENDING = 2;
    
    private String fieldName;
    private int width;
    private int height;
    private int sortType;

    public ReportTableField(String fieldName, int width, int height)
    {
        this.fieldName = fieldName;
        this.width = width;
        this.height = height;
    }

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return Returns the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @return Returns the sortType.
     */
    public int getSortType()
    {
        return sortType;
    }

    /**
     * @param sortType The sortType to set.
     */
    public void setSortType(int sortType)
    {
        this.sortType = sortType;
    }
    
    
}
