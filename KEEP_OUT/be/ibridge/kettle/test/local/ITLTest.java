package be.ibridge.kettle.test.local;

import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;

public class ITLTest extends Thread
{
    private static final LocalVariables local = LocalVariables.getInstance();
    
    private TLTest parent;

    public ITLTest(TLTest parent)
    {
        this.parent = parent;
        
        // if (parent!=null) vars.set(parent.getProperties());
    }
    
    public void start()
    {
        // Create a new variables object and store it in the list.
        // We take the variables we found in the parent to use as the basis.
        // 
        KettleVariables vars = local.createKettleVariables(this.getName(), parent.getName(), false);
        
        System.out.println("** ITLTest started, initial value of TODAY="+vars.getVariable(TLTest.TODAY, "?"));
        
        vars.setVariable(TLTest.TODAY, "Child thread value");
        
        System.out.println("** Start of thread ("+getName()+")");
        
        try
        {
            System.out.println("** ITLTest started, TODAY="+vars.getVariable(TLTest.TODAY, "?"));
            
            // OK, this thread is running, wait a bit too..
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
            
        }
        System.out.println("** ITLTest finished");
    }
}
