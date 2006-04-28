package be.ibridge.kettle.test;

public class ArgsTest
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.print("Arguments: ");
        for (int i=0;i<args.length;i++)
        {
            if (i>0) System.out.print(", ");
            System.out.print("["+args[i]+"]");
        }
        System.out.println();
        throw new RuntimeException("This is a test-exception");
    }

}
