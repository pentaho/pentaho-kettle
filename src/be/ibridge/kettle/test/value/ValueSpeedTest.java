package be.ibridge.kettle.test.value;

import java.math.BigDecimal;
import java.util.Date;

import be.ibridge.kettle.core.value.Value;

public class ValueSpeedTest
{
    private static final int ITERATIONS = 10000000;

    public static void main(String[] args)
    {
        // Let's create 1.000.000 values and see how long that takes
        // Create a value of each data type.
        //
        // We first create the metadata and then re-use those objects...
        
        /*
        // String called 'A', length 40
        ValueMeta stringMeta = new ValueMeta(ValueMeta.VALUE_TYPE_STRING, "A", 40);
        
        // Number called 'B', length 12, precision 4
        ValueMeta numberMeta = new ValueMeta(ValueMeta.VALUE_TYPE_NUMBER, "B", 12, 4);
        
        //  Date called 'C'
        ValueMeta dateMeta = new ValueMeta(ValueMeta.VALUE_TYPE_DATE, "C");

        //  BigNumber called 'D', length 56, precision 12
        ValueMeta bigNumberMeta = new ValueMeta(ValueMeta.VALUE_TYPE_BIGNUMBER, "D", 56, 12);

        //  Boolean called 'E'
        ValueMeta booleanMeta = new ValueMeta(ValueMeta.VALUE_TYPE_BOOLEAN, "E");
        
        //  Integer called 'F', length 7
        ValueMeta integerMeta = new ValueMeta(ValueMeta.VALUE_TYPE_INTEGER, "F", 7);
        */
        
        // Create Strings...
        String string = "abracadabrastring";
        Date t1 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            Value v = new Value("A", string);
            v.setLength(40);
        }
        Date t2 = new Date();
        System.out.println("Time to create "+ITERATIONS+" STRING values: "+getTime(t1, t2));
        
        // Numbers
        Date t3 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            Value v = new Value("B", 87343843.23);
            v.setLength(12, 4);
        }
        Date t4 = new Date();
        System.out.println("Time to create "+ITERATIONS+" NUMBER values: "+getTime(t3, t4));

        // Dates
        Date date = new Date();
        Date t5 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            new Value("C", date); 
        }
        Date t6 = new Date();
        System.out.println("Time to create "+ITERATIONS+" DATE values: "+getTime(t5, t6));
        
        // BigNumber
        BigDecimal bigDecimal = new BigDecimal(1239434.3943493);
        Date t7 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            Value v = new Value("D", bigDecimal);
            v.setLength(56, 12);
        }
        Date t8 = new Date();
        System.out.println("Time to create "+ITERATIONS+" BIG-NUMBER values: "+getTime(t7, t8));

        // Boolean
        Date t9 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            new Value("E", true); 
        }
        Date t10 = new Date();
        System.out.println("Time to create "+ITERATIONS+" BOOLEAN values: "+getTime(t9, t10));
        
        // INTEGER
        Date t11 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            Value v = new Value("F", i);
            v.setLength(7);
        }
        Date t12 = new Date();
        System.out.println("Time to create "+ITERATIONS+" INTEGER values: "+getTime(t11, t12));

        System.out.println("Total time spent: "+getTime(t1, t12));
    }

    private static String getTime(Date t1, Date t2)
    {
        return Double.toString( ((double)t2.getTime()-(double)t1.getTime())/1000 );
    }
}
