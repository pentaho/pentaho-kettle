package be.ibridge.kettle.core;

import java.util.Comparator;

public class ObjectUsageCount implements Comparator, Comparable
{
    private String objectName;

    private int    nrUses;

    /**
     * @param objectName
     * @param nrUses
     */
    public ObjectUsageCount(String objectName, int nrUses)
    {
        this.objectName = objectName;
        this.nrUses = nrUses;
    }
    
    public String toString()
    {
        return objectName+";"+nrUses;
    }
    
    public static ObjectUsageCount fromString(String string)
    {
        String[] splits = string.split(";");
        if (splits.length>=2) return new ObjectUsageCount(splits[0], Const.toInt(splits[1], 1));
        return new ObjectUsageCount(string, 1);
    }
    
    public int compare(Object o1, Object o2)
    {
        ObjectUsageCount count1 = (ObjectUsageCount) o1;
        ObjectUsageCount count2 = (ObjectUsageCount) o2;
        
        return count1.compareTo(count2);
    }
    
    public int compareTo(Object o)
    {
        ObjectUsageCount count = (ObjectUsageCount) o;
        return new Integer(count.getNrUses()).compareTo(new Integer(getNrUses()));
    }
    
    public void reset()
    {
        nrUses=0;
    }
    
    /**
     * Increment the nr of uses with 1
     * @return the nr of uses
     */
    public int increment()
    {
        nrUses++;
        return nrUses;
    }

    /**
     * @return the nrUses
     */
    public int getNrUses()
    {
        return nrUses;
    }

    /**
     * @param nrUses the nrUses to set
     */
    public void setNrUses(int nrUses)
    {
        this.nrUses = nrUses;
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

}
