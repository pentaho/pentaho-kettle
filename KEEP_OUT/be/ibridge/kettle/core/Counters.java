package be.ibridge.kettle.core;

import java.util.Hashtable;

/**
 * This class contains the counters for Kettle, the transformations, jobs and also the repository.
 * @author Matt
 * @since  17-apr-2005
 * 
 */
public class Counters
{
    private static Counters counters = null; 
    private Hashtable counterTable = null;
    
    private Counters()
    {
        counterTable = new Hashtable();
    }
    
    public static final Counters getInstance()
    {
        if (counters!=null) return counters;
        counters = new Counters();
        return counters;
    }
    
    public Counter getCounter(String name)
    {
        return (Counter)counterTable.get(name);
    }
    
    public void setCounter(String name, Counter counter)
    {
        counterTable.put(name, counter);
    }
    
    public void clearCounter(String name)
    {
        counterTable.remove(name);
    }
    
    public void clear()
    {
        counterTable.clear();
    }
}
