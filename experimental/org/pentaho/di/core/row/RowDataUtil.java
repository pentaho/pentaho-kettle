package org.pentaho.di.core.row;

/**
 * 
 * We use this class to do row manipulations like add, delete, resize, etc.
 * That way, when we want to go for a metadata driven system with 
 * hiding deletes, oversized arrays etc, we can change these methods to find occurences.
 * 
 * @author Matt
 *
 */
public class RowDataUtil
{
    /**
     * Resize an object array
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
}
