package org.pentaho.di.core.row;

public class RowData
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
}
