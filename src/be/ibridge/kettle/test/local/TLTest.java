package be.ibridge.kettle.test.local;

import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;

public class TLTest extends Thread
{
    private static final LocalVariables local = LocalVariables.getInstance();
    
    public static final String TODAY = "TODAY";

    private Thread parent;
    
    public TLTest(Thread parent)
    {
        this.parent = parent;
    }
    
    public void start()
    {
        // Create a new variables object and store it in the list.
        // We start with an empty list as this thread has no parents.
        // 
        KettleVariables vars = local.createKettleVariables(this, parent, false);

        System.out.println("Start of test");
        System.out.println("TLTest started initial value: TODAY="+vars.getVariable(TODAY, "?"));
        
        vars.setVariable(TODAY, "Original value");
        System.out.println("TLTest after setVar(), TODAY="+vars.getVariable(TODAY, "?"));
        try
        {
            // OK, this thread is running, open the next...
            
            ITLTest itl = new ITLTest(this);
            itl.start();
            
            Thread.sleep(2000);
        }
        catch(Exception e)
        {
            System.out.println("Exception occurred: "+e.toString());
            e.printStackTrace();
        }
        
        System.out.println("TLTest finished, TODAY="+vars.getVariable(TODAY, "?"));
    }
    
    public static void main(String[] args)
    {
        // This is top level: initialize the variables...
        KettleVariables vars = local.createKettleVariables(Thread.currentThread(), null, false);

        // Store some inital value in there..
        vars.setVariable(TODAY, "main thread initial value");
        
        TLTest test = new TLTest(Thread.currentThread());
        test.start();
    }

    public KettleVariables getProperties()
    {
        return LocalVariables.getKettleVariables();
    }
}
