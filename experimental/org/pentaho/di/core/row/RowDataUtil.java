package org.pentaho.di.core.row;

/**
 * 
 * We use this class to do row manipulations like add, delete, resize, etc.
 * That way, when we want to go for a metadata driven system with 
 * hiding deletes, oversized arrays etc, we can change these methods to find occurrences.
 * 
 * @author Matt
 *
 */
public class RowDataUtil
{
    /**
     * Resize an object array making it bigger.
     * 
     * @param objects
     * @param newSize
     * @return A new object array, resized.
     */
    public static Object[] resizeArray(Object[] objects, int newSize)
    {
        Object[] newObjects = new Object[newSize];
        
        for (int i=0;i<objects.length;i++)
        {
            newObjects[i]=objects[i];
        }
        
        return newObjects;
    }
    
    /**
     * Remove an item from an Object array.  This is a slow operation, later we want to just flag this object and discard it at the next resize.
     * The question is of-course if it makes that much of a difference in the end.
     * 
     * @param objects
     * @param index
     * @return
     */
    public static Object[] removeItem(Object[] objects, int index)
    {
        Object[] newObjects = new Object[objects.length-1];
        for (int i=0;i<index;i++)
        {
            newObjects[i]=objects[i];
        }
        for (int i=index+1;i<objects.length;i++)
        {
            newObjects[i-1]=objects[i];
        }
        return newObjects;
    }
    
    /**
     * Add two arrays and make one new one.
     * 
     * @param one The first array
     * @param two The second array
     * @return a new Array containing all elements from one and two after one another
     */
    public static Object[] addRowData(Object[] one, Object[] two)
    {
        Object[] result = new Object[one.length + two.length];
        for (int i=0;i<one.length;i++)
        {
            result[i] = one[i];
        }
        for (int i=0;i<two.length;i++)
        {
            result[one.length+i] = two[i];
        }
        return result;
    }
    
    /**
     * Add a single value to a row of data
     * 
     * @param rowData The original row of data
     * @param extra The extra value to add
     * @return a new Array containing all elements, including the extra one
     */
    public static Object[] addValueData(Object[] rowData, Object extra)
    {
        Object[] result = new Object[rowData.length + 1];
        for (int i=0;i<rowData.length;i++)
        {
            result[i] = rowData[i];
        }
        result[rowData.length] = extra;
        return result;
    }

    /**
     * Remove a number of items in a row of data.
     * 
     * @param rowData the row of data to remove from
     * @param index the index of all the items in the source table to remove.  
     *        We don't check if the same index gets deleted twice!
     */
    public static Object[] removeItems(Object[] rowData, int[] index)
    {
        Object[] data = new Object[rowData.length - index.length];
        
        int count=data.length-1;
        int removenr=index.length-1;
        for (int i=rowData.length-1;i>=0;i--)
        {
            if (removenr>=0 && i==index[removenr])
            {
                removenr--;
            }
            else
            {
                data[count] = rowData[i];
                count--;
            }
        }
        
        return data;
    }
}