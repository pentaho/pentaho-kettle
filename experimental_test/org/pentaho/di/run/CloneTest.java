package org.pentaho.di.run;

import java.util.Date;

public class CloneTest
{
    public static void main(String[] args)
    {
        Object a = new Date();
        Object b = a;
        
        System.out.println("a="+a+", b="+b);
        
        System.out.println("a="+a+", b="+b);
    }
}
