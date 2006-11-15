package be.ibridge.kettle.www;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import be.ibridge.kettle.trans.Trans;

/**
 * This is a map between the transformation name and the (running/waiting/finished) transformation.
 * 
 * @author Matt
 *
 */
public class TransformationMap
{
    private Map transformationMap;
    private String parentThreadName;
    
    public TransformationMap(String parentThreadName)
    {
        this.parentThreadName = parentThreadName;
        
        transformationMap = new Hashtable();
    }
    
    public synchronized void addTransformation(String transformationName, Trans trans)
    {
        transformationMap.put(transformationName, trans);
    }
    
    public synchronized Trans getTransformation(String transformationName)
    {
        return (Trans)transformationMap.get(transformationName);
    }

    public synchronized void removeTransformation(String transformationName)
    {
        transformationMap.remove(transformationName);
    }
    
    public String[] getTransformationNames()
    {
        Set keySet = transformationMap.keySet();
        return (String[]) keySet.toArray(new String[keySet.size()]);
    }

    /**
     * @return the parentThreadName
     */
    public String getParentThreadName()
    {
        return parentThreadName;
    }
}
