
package be.ibridge.kettle.trans;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public class KettleURLClassLoader extends URLClassLoader
{
    private String name;
    
    public KettleURLClassLoader(URL[] url, ClassLoader classLoader)
    {
        super(url, classLoader);
    }
    
    public KettleURLClassLoader(URL[] url, ClassLoader classLoader, String name)
    {
        this(url, classLoader);
        this.name = name;
    }
    
    public String toString()
    {
        return super.toString()+" : "+name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    /*
        Cglib doe's not creates custom class loader (to access package methotds and classes ) it uses reflection to invoke "defineClass", 
        but you can call protected method in subclass without problems:
    */
    public Class loadClass(String name, ProtectionDomain protectionDomain) 
    {
        Class loaded = findLoadedClass(name);
        if (loaded == null)
        {
            // Get the jar, load the bytes from the jar file, construct class from scratch as in snippet below...

            /*
            
            loaded = super.findClass(name);
            
            URL url = super.findResource(newName);
            
            InputStream clis = getResourceAsStream(newName);
            
            */
           
            String newName = name.replace('.','/');
            InputStream is = super.getResourceAsStream(newName);
            byte[] driverBytes = toBytes( is );
            
            loaded = super.defineClass(name, driverBytes, 0, driverBytes.length, protectionDomain);

        }
        return loaded;
    }
    
    private byte[] toBytes(InputStream is)
    {
        byte[] retval = new byte[0];
        try
        {
            int a = is.available();
	        while (a>0)
	        {
	            byte[] buffer = new byte[a];
	            is.read(buffer);
	            
	            byte[] newretval = new byte[retval.length+a];
	            
	            for (int i=0;i<retval.length;i++) newretval[i] = retval[i]; // old part
	            for (int i=0;i<a;i++) newretval[retval.length+i] = buffer[i]; // new part
	            
	            retval = newretval;
	            
	            a = is.available(); // see what's left
	        }
            return retval; 
        }
        catch(Exception e)
        {
            System.out.println(Messages.getString("KettleURLClassLoader.Exception.UnableToReadClass")+e.toString()); //$NON-NLS-1$
            return null;
        }
    }
}
