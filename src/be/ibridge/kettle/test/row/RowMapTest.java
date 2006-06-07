package be.ibridge.kettle.test.row;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

public class RowMapTest
{
    private static final int SIZE    =   50000; 
    private static final int STEPS   =       20;
    private static final int RETRIES =      10;
    
    private ArrayList rows;
    private ArrayList maps;
    
    private static final String fieldNames[] = { "name", "firstname", "zipcode", "city", "street", "number", "bus", "country", "phone", "e-mail" };
    
    public void initTestWith()
    {
        rows = new ArrayList(SIZE);
        maps = new ArrayList(SIZE);
        
        for (int i=0;i<SIZE;i++)
        {
            Row row = new Row();
            Map map = new HashMap(fieldNames.length);

            for (int f=0;f<fieldNames.length;f++)
            {
                Value value = new Value(fieldNames[f], "dummy");

                row.addValue(value);
                map.put(fieldNames[f], new Integer(f));
            }
            
            rows.add(row);
            maps.add(map);
        }
    }
    
    /**
     * After init we have a number of rows and maps
     * Let's see how fast it is to process
     *
     */
    public void runTestsWith()
    {
        int count = 0;
        for (int s=0;s<STEPS;s++)
        {
            for (int i=0;i<SIZE;i++)
            {
                Row row = (Row)rows.get(i);
                Map map = (Map)maps.get(i); 

                for (int f=0;f<fieldNames.length;f++)
                {
                    Value value = row.getValue( ((Integer)map.get(fieldNames[f])).intValue() );
                    String str = value.getString();
                    if (str!=null) count++;
                }
            }
        }
        // System.out.println("count = "+count);
    }
    
    public void clear()
    {
        rows=null;
        maps=null;
    }

    public void initTestWithout()
    {
        rows = new ArrayList(SIZE);
        maps = new ArrayList(SIZE);

        for (int i=0;i<SIZE;i++)
        {
            Row row = new Row();
            
            for (int f=0;f<fieldNames.length;f++)
            {
                Value value = new Value(fieldNames[f], "dummy");
                row.addValue(value);
            }
            
            rows.add(row);

        }
    }

    /**
     * After init we have a number of rows and maps
     * Let's see how fast it is to process
     *
     */
    public void runTestsWithout()
    {
        int fieldIndexes[] = null;
        
        int count = 0;
        boolean first=true;
        
        for (int s=0;s<STEPS;s++)
        {
            for (int i=0;i<SIZE;i++)
            {
                Row row = (Row)rows.get(i);
                
                if (first)
                {
                    first=false;
                    fieldIndexes = new int[fieldNames.length];
                    for (int f=0;f<fieldNames.length;f++)
                    {
                        fieldIndexes[f] = row.searchValueIndex(fieldNames[f]);
                    }
                }
                
                for (int f=0;f<fieldNames.length;f++)
                {
                    Value value = row.getValue( fieldIndexes[f] );
                    String str = value.getString();
                    if (str!=null) count++;
                }
            }
        }
        // System.out.println("count = "+count);
    }
    
    public void runTestsFastSearch()
    {
        int fieldIndexes[] = null;
        
        int count = 0;
        boolean first=true;
        
        for (int s=0;s<STEPS;s++)
        {
            for (int i=0;i<SIZE;i++)
            {
                Row row = (Row)rows.get(i);
                
                if (first)
                {
                    first=false;
                    fieldIndexes = new int[fieldNames.length];
                    for (int f=0;f<fieldNames.length;f++)
                    {
                        fieldIndexes[f] = searchValueIndex(row, fieldNames[f]);
                    }
                }
                
                for (int f=0;f<fieldNames.length;f++)
                {
                    Value value = row.getValue( fieldIndexes[f] );
                    String str = value.getString();
                    if (str!=null) count++;
                }
            }
        }
        // System.out.println("count = "+count);
    }
    
    public void runTestsFastMap()
    {
        int fieldIndexes[] = null;
        
        int count = 0;
        boolean first=true;
        
        for (int s=0;s<STEPS;s++)
        {
            for (int i=0;i<SIZE;i++)
            {
                Row row = (Row)rows.get(i);
                
                if (first)
                {
                    Map map = (Map)maps.get(i); 
                    first=false;
                    fieldIndexes = new int[fieldNames.length];
                    for (int f=0;f<fieldNames.length;f++)
                    {
                        int index = ((Integer)map.get(fieldNames[f])).intValue();
                        fieldIndexes[f] = index;
                    }
                }
                
                for (int f=0;f<fieldNames.length;f++)
                {
                    Value value = row.getValue( fieldIndexes[f] );
                    String str = value.getString();
                    if (str!=null) count++;
                }
            }
        }
        // System.out.println("count = "+count);
    }
    
    public int searchValueIndex(Row row, String name)
    {
        if (name==null) return -1;

        for (int i = 0; i < row.size(); i++)
        {
            Value v = row.getValue(i);
            if ( v.getName().equals(name) ) 
            { 
                return i; 
            }
        }

        return -1;
    }
    
    public static void main(String[] args)
    {
        RowMapTest rowMapTest = new RowMapTest();
        
        System.out.println("Populating test data for "+SIZE+" rows, "+fieldNames.length+" fields in "+STEPS+" steps.");

        long totalDiffWith = 0;
        long totalDiffWithout = 0;
        long totalDiffFast = 0;
        long totalDiffFastSearch = 0;
        long totalDiffWithButInit = 0;
        
        for (int i=1;i<=RETRIES;i++)
        {
            rowMapTest.clear(); // to even the odds
            System.gc();
            
            System.out.println("RUN #"+i+" : Starting run with maps...");
            long startMillis = new Date().getTime();
            rowMapTest.initTestWith();
            rowMapTest.runTestsWith();
            long endMillis = new Date().getTime();
            long diff = endMillis-startMillis;
            System.out.println("RUN #"+i+" time lapsed with (ms)   : "+diff);
            totalDiffWith+=diff;
            
            rowMapTest.clear(); // to even the odds
            
            System.gc();
            
            System.out.println("RUN #"+i+" Starting run without maps...");
            long startMillis2 = new Date().getTime();
            rowMapTest.initTestWithout();
            rowMapTest.runTestsWithout();
            long endMillis2 = new Date().getTime();
            long diff2 = endMillis2-startMillis2;
            totalDiffWithout+=diff2;
            System.out.println("RUN #"+i+" time lapsed without (ms): "+diff2);

            
            rowMapTest.clear(); // to even the odds
            
            System.gc();
            
            System.out.println("RUN #"+i+" Starting run fast map...");
            long startMillis3 = new Date().getTime();
            rowMapTest.initTestWith();
            rowMapTest.runTestsFastMap();
            long endMillis3 = new Date().getTime();
            long diff3 = endMillis3-startMillis3;
            totalDiffFast+=diff3;
            System.out.println("RUN #"+i+" time lapsed fast (ms): "+diff3);
            
            rowMapTest.clear(); // to even the odds
            
            System.gc();
            
            System.out.println("RUN #"+i+" Starting run fast search...");
            long startMillis4 = new Date().getTime();
            rowMapTest.initTestWithout();
            rowMapTest.runTestsFastSearch();
            long endMillis4 = new Date().getTime();
            long diff4 = endMillis4-startMillis4;
            totalDiffFastSearch+=diff4;

            System.out.println("RUN #"+i+" time lapsed fast (ms): "+diff4);            
            
            rowMapTest.clear(); // to even the odds
            
            System.gc();
            
            System.out.println("RUN #"+i+" Starting run without maps, but map init...");
            long startMillis5 = new Date().getTime();
            rowMapTest.initTestWith();
            rowMapTest.runTestsWithout();
            long endMillis5 = new Date().getTime();
            long diff5 = endMillis5-startMillis5;
            totalDiffWithButInit+=diff5;
            System.out.println("RUN #"+i+" time lapsed without maps, but map init (ms): "+diff5);

            System.out.println();
            
        }

        System.out.println("Average runtime using maps  : "+(totalDiffWith/RETRIES));
        System.out.println("Average runtime without maps: "+(totalDiffWithout/RETRIES));
        System.out.println("Average runtime fast map: "+(totalDiffFast/RETRIES));
        System.out.println("Average runtime fast search: "+(totalDiffFastSearch/RETRIES));
        System.out.println("Average runtime without maps, with map init: "+(totalDiffWithButInit/RETRIES));
    }
}
