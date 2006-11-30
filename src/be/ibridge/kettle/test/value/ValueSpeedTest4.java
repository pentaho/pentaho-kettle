package be.ibridge.kettle.test.value;

import java.math.BigDecimal;
import java.util.Date;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.value.ValueInterface;

public class ValueSpeedTest4
{
    private static final int ITERATIONS = 10000000;

    public static void main(String[] args)
    {
        // Let's create rows now and see what happens...
        //
        Row reference = getRow();
        
        Date t1 = new Date();
        for (int x=0;x<ITERATIONS;x++)
        {
            Row r = new Row();

            for (int i=0;i<reference.size();i++)
            {
                Value value = reference.getValue(i);
    
                Value copy = new Value(value.getName());
                copy.setLength(value.getLength(), value.getPrecision());
                copy.setNull(value.isNull());
                copy.setValueInterface( (ValueInterface) value.getValueInterface().clone() );
                
                r.addValue(copy);
            }
        }
        Date t2 = new Date();
        
        System.out.println("Total time spent: "+getTime(t1, t2));
    }

    private static String getTime(Date t1, Date t2)
    {
        return Double.toString( ((double)t2.getTime()-(double)t1.getTime())/1000 );
    }
    
    private static Row getRow()
    {
        Row row = new Row();

        // Strings
        Value A = new Value("A", "abracadabrastring");
        A.setLength(40);
        row.addValue(A);

        // Numbers
        Value B = new Value("B", 87343843.23);
        B.setLength(12, 4);
        row.addValue(B);

        // Dates
        Value C = new Value("C", new Date());
        row.addValue(C);

        // BigNumber
        Value D = new Value("D", new BigDecimal(1239434.3943493));
        D.setLength(56, 12);
        row.addValue(D);

        // Boolean
        Value E = new Value("E", true);
        row.addValue(E);

        // INTEGER
        Value F = new Value("F", (long)934982);
        F.setLength(7);
        row.addValue(F);
        
        return row;
    }
}
