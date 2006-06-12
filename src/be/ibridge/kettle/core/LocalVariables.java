package be.ibridge.kettle.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class is a container for "Local" enrvironment variables.
 * This is a singleton.  We are going to launch jobs using a customer classloader.
 * This will make the variables inside it local.
 * 
 * @author Matt
 */
public class LocalVariables
{
    ThreadLocal local;
    private static LocalVariables localVariables;
    
    private Map map;
    
    /**
     * Create a new KettleVariables variable map in the local variables map for the specified thread.
     * @param localThread The local thread to attach to
     * @param parentThread The parent thread, null if there is no parent thread.  The initial value of the variables will be taken from the variables that are attached to this thread.
     * @param sameNamespace true if you want to use the same namespace as the parent (if any) or false if you want to use a new namespace, create a new KettleVariables object.
     */
    public synchronized KettleVariables createKettleVariables(String localThread, String parentThread, boolean sameNamespace)
    {
        if (parentThread!=null && parentThread.equals(localThread)) 
        {
            throw new RuntimeException("local thread can't be the same as the parent thread!");
        }
        
        LogWriter.getInstance().logDebug("LocalVariables", "---> Create new KettleVariables for thread ["+localThread+"] for parent thread ["+parentThread+"], same namespace? ["+sameNamespace+"]");
        
        // See if the thread already has an entry in the map
        //
        KettleVariables vars = new KettleVariables(localThread, parentThread);
        
        // Copy the initial values from the parent thread if it is specified
        if (parentThread!=null)
        {
            KettleVariables initialValue = getKettleVariables(parentThread);
            if (initialValue!=null)
            {
                if (sameNamespace)
                {
                    vars = new KettleVariables(localThread, parentThread);
                    vars.setProperties(initialValue.getProperties());
                }
                else
                {
                    vars.putAll(initialValue.getProperties());
                }
            }
            else
            {
                throw new RuntimeException("No parent Kettle Variables found for thread ["+parentThread+"], local thread is ["+localThread+"]");
            }
        }

        // Before we add this, for debugging, just see if we're not overwriting anything.
        // Overwriting is a big No-No
        KettleVariables checkVars = (KettleVariables) map.get(localThread); 
        if (checkVars!=null)
        {
            // throw new RuntimeException("There are already variables in the local variables map for ["+localThread+"]");
        }
        
        // LogWriter.getInstance().logBasic("LocalVariables!", "---> Store new KettleVariables in key ["+localThread+"], vars.local ["+vars.getLocalThread()+"], vars.parent ["+vars.getParentThread()+"]");
        
        // Put this one in the map, attached to the local thread
        map.put(localThread, vars);
        
        return vars;
    }

    public LocalVariables()
    {
        map = new Hashtable();
    }

    public static final LocalVariables getInstance()
    {
        if (localVariables==null) // Not the first time we call this, see if we have properties for this thread
        {
            // System.out.println("Init of new local variables object");
            localVariables = new LocalVariables();
        }
        return localVariables;
    }
    
    public Map getMap()
    {
        return map;
    }
    
    public static final KettleVariables getKettleVariables()
    {
        return getInstance().getVariables(Thread.currentThread().getName());
    }
    
    public static final KettleVariables getKettleVariables(String thread)
    {
        return getInstance().getVariables(thread);
    }

    /**
     * Find the KettleVariables in the map, attached to the specified Thread.
     * This is not singleton stuff, we return null in case we don't have anything attached to the current thread.
     * That makes it easier to find the "missing links"
     * @param localThread The thread to look for
     * @return The KettleVariables attached to the specified thread.
     */
    private KettleVariables getVariables(String localThread)
    {
        KettleVariables kettleVariables = (KettleVariables) map.get(localThread); 
        return kettleVariables;
    }

    
    public void removeKettleVariables(String thread)
    {
        if (thread==null) return;
        removeKettleVariables(thread, 1);
    }
    
    /**
     * Remove all KettleVariables objects in the map, including the one for this thread, but also the ones with this thread as parent, etc. 
     * @param thread the grand-parent thread to look for to remove
     */
    private void removeKettleVariables(String thread, int level)
    {
        LogWriter log = LogWriter.getInstance();
        
        List children = getKettleVariablesWithParent(thread);
        
        for (int i=0;i<children.size();i++)
        {
            String child = (String)children.get(i);
            removeKettleVariables(child, level+1);
        }
        
        // See if it was in there in the first place...
        if (map.get(thread)==null)
        {
            // We should not ever arrive here...
            log.logError("LocalVariables!!!!!!!", "The variables you are trying to remove, do not exist for thread ["+thread+"]");
            log.logError("LocalVariables!!!!!!!", "Please report this error to the Kettle developers.");
        }
        else
        {
            map.remove(thread);
        }
    }
    
    private List getKettleVariablesWithParent(String parentThread)
    {
        List children = new ArrayList();
        List values;
        synchronized (map) {
            values = new ArrayList(map.values());
        }
        
        for (int i=0;i<values.size();i++)
        {
            KettleVariables kv = (KettleVariables)values.get(i);
            if ( ( kv.getParentThread()==null && parentThread==null) || 
                 ( kv.getParentThread()!=null && parentThread!=null && kv.getParentThread().equals(parentThread) ) 
               ) 
            {
                if (kv.getLocalThread().equals(parentThread))
                {
                    System.out.println("---> !!!! This should not happen! Thread ["+parentThread+"] is linked to itself!");
                }
                else
                {
                    children.add(kv.getLocalThread());
                }
            }
        }
        return children;
    }
}
