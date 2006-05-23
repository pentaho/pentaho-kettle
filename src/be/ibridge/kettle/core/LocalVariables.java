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
    public KettleVariables createKettleVariables(Thread localThread, Thread parentThread, boolean sameNamespace)
    {
        // System.out.println("---> Create new KettleVariables for thread ["+localThread+"]");
        
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
                    vars.setProperties(initialValue.getProperties());
                }
                else
                {
                    vars.putAll(initialValue.getProperties());
                }
            }
            else
            {
                // System.out.println("---> No parent Kettle Variables found for thread ["+parentThread+"]");
            }
        }

        // Before we add this, for debugging, just see if we're not overwriting anything.
        // Overwriting is a big No-No
        KettleVariables checkVars = (KettleVariables) map.get(localThread); 
        if (checkVars!=null)
        {
            // System.out.println("---> There are already variables in the local variables map for ["+localThread+"]");
        }
        
        
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
    
    public static final KettleVariables getKettleVariables()
    {
        return getInstance().getVariables(Thread.currentThread());
    }
    
    public static final KettleVariables getKettleVariables(Thread thread)
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
    private KettleVariables getVariables(Thread localThread)
    {
        KettleVariables kettleVariables = (KettleVariables) map.get(localThread); 
        return kettleVariables;
    }

    /**
     * Remove all KettleVariables objects in the map, including the one for this thread, but also the ones with this thread as parent, etc. 
     * @param thread the grand-parent thread to look for to remove
     */
    public void removeKettleVariables(Thread thread)
    {
        List children = getKettleVariablesWithParent(thread);
        for (int i=0;i<children.size();i++)
        {
            Thread child = (Thread)children.get(i);
            // System.out.println("--> removing child #"+i+"/"+children.size()+" ["+child+"] for thread ["+thread+"]");
            removeKettleVariables(child);
        }
        map.remove(thread);
    }
    
    private List getKettleVariablesWithParent(Thread parentThread)
    {
        List children = new ArrayList();
        List values = new ArrayList(map.values());
        for (int i=0;i<values.size();i++)
        {
            KettleVariables kv = (KettleVariables)values.get(i);
            if (kv.getParentThread()!=null && kv.getParentThread().equals(parentThread))
            {
                children.add(kv.getLocalThread());
            }
        }
        return children;
    }
}
