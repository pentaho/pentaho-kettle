package be.ibridge.kettle.test.loader;

public class Loadee
{
    public Loadee()
    { 
        System.out.println("Loadee class just got instantiated, singleton: "+SingleTon.getInstance().toString()); 
        
    }
}
