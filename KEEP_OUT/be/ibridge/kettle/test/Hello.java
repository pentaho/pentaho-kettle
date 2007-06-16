package be.ibridge.kettle.test;

import be.ibridge.kettle.core.value.Value;

public class Hello
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("Hello, world!\n");
        
        Value one = new Value("one", Value.VALUE_TYPE_STRING);
        Value two = new Value("two", Value.VALUE_TYPE_STRING);

        one.setValue((String)null);
        two.setValue((String)"BlaBla");
        
        System.out.println("one.compare(two)="+one.compare(two));
    }

}
