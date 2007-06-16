package be.ibridge.kettle.test.loader;

public class SingleTon
{
    private static SingleTon singleTon = new SingleTon();
    
    private SingleTon()
    {
        System.out.println("Singleton created");
    }
    
    public static final SingleTon getInstance()
    {
        return singleTon;
    }
}
