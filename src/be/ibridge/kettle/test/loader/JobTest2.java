package be.ibridge.kettle.test.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class JobTest2
{
    public static void main(String[] args) throws Exception
    {
        URL url1[] = new URL[] { new URL("file:libvars/kettle-variables.jar")};
        URL url2[] = new URL[] { new URL("file:libvars/kettle-variables.jar")};
        
        ClassLoader loader1 = new URLClassLoader( url1 );
        ClassLoader loader2 = new URLClassLoader( url2 );
        
        System.out.println("Loading classes...");
        Class varClass1 = Class.forName("be.ibridge.kettle.test.loader.Loadee", true, loader1);
        Class varClass2 = Class.forName("be.ibridge.kettle.test.loader.Loadee", true, loader2);

        System.out.println("Creating instances...");
        System.out.println("one: "+varClass1.newInstance().toString());
        System.out.println("two: "+varClass2.newInstance().toString());
    }
}
