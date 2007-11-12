/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.row;

import java.util.Date;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.util.StringUtil;

public class SpeedTest
{
    private Object[] rowString10;
    private Object[] rowString100;
    private Object[] rowString1000;

    private Object[] rowMixed10;
    private Object[] rowMixed100;
    private Object[] rowMixed1000;

    private RowMetaInterface metaString10;
    private RowMetaInterface metaMixed10;

    private RowMetaInterface metaString100;
    private RowMetaInterface metaMixed100;

    private RowMetaInterface metaString1000;
    private RowMetaInterface metaMixed1000;

    public SpeedTest()
    {
        rowString10 = new Object[10];
        rowString100 = new Object[100];
        rowString1000 = new Object[1000];

        rowMixed10 = new Object[50];
        rowMixed100 = new Object[500];
        rowMixed1000 = new Object[5000];

        metaString10 = new RowMeta();
        metaMixed10 = new RowMeta();

        metaString100 = new RowMeta();
        metaMixed100 = new RowMeta();

        metaString1000 = new RowMeta();
        metaMixed1000 = new RowMeta();

        for (int i=0;i<10;i++)
        {
            populateMetaAndData(i, rowString10, metaString10, rowMixed10, metaMixed10);
        }

        for (int i=0;i<100;i++)
        {
            populateMetaAndData(i, rowString100, metaString100, rowMixed100, metaMixed100);
        }
        
        for (int i=0;i<1000;i++)
        {
            populateMetaAndData(i, rowString1000, metaString1000, rowMixed1000, metaMixed1000);
        }


    }

    private static void populateMetaAndData(int i, Object[] rowString10, RowMetaInterface metaString10, Object[] rowMixed10, RowMetaInterface metaMixed10)
    {
        rowString10[i]      = StringUtil.generateRandomString(20, "", "", false);
        ValueMetaInterface meta = new ValueMeta("String"+(i+1), ValueMetaInterface.TYPE_STRING, 20, 0);
        metaString10.addValueMeta(meta);

        rowMixed10[i*5 + 0] = StringUtil.generateRandomString(20, "", "", false);
        ValueMetaInterface meta0 = new ValueMeta("String"+(i*5+1), ValueMetaInterface.TYPE_STRING, 20, 0);
        metaMixed10.addValueMeta(meta0);

        rowMixed10[i*5 + 1] = new Date();
        ValueMetaInterface meta1 = new ValueMeta("String"+(i*5+1), ValueMetaInterface.TYPE_DATE);
        metaMixed10.addValueMeta(meta1);
        
        rowMixed10[i*5 + 2] = new Double( Math.random() * 1000000 );
        ValueMetaInterface meta2 = new ValueMeta("String"+(i*5+1), ValueMetaInterface.TYPE_NUMBER, 12, 4);
        metaMixed10.addValueMeta(meta2);

        rowMixed10[i*5 + 3] = new Long( (long)(Math.random() * 1000000) );
        ValueMetaInterface meta3 = new ValueMeta("String"+(i*5+1), ValueMetaInterface.TYPE_INTEGER, 8, 0);
        metaMixed10.addValueMeta(meta3);
        
        rowMixed10[i*5 + 4] = Boolean.valueOf( Math.random() > 0.5 ? true : false );
        ValueMetaInterface meta4 = new ValueMeta("String"+(i*5+1), ValueMetaInterface.TYPE_BOOLEAN);
        metaMixed10.addValueMeta(meta4);
    }

    public long runTestStrings10(int iterations) throws KettleValueException
    {
        long startTime = System.currentTimeMillis();
        
        for (int i=0;i<iterations;i++)
        {
            metaString10.cloneRow(rowString10);
        }
        
        long stopTime = System.currentTimeMillis();
        
        return stopTime - startTime;
    }

    public long runTestMixed10(int iterations) throws KettleValueException
    {
        long startTime = System.currentTimeMillis();
        
        for (int i=0;i<iterations;i++)
        {
            metaMixed10.cloneRow(rowMixed10);
        }
        
        long stopTime = System.currentTimeMillis();
        
        return stopTime - startTime;
    }

    public long runTestStrings100(int iterations) throws KettleValueException
    {
        long startTime = System.currentTimeMillis();
        
        for (int i=0;i<iterations;i++)
        {
            metaString100.cloneRow(rowString100);
        }
        
        long stopTime = System.currentTimeMillis();
        
        return stopTime - startTime;
    }

    public long runTestMixed100(int iterations) throws KettleValueException
    {
        long startTime = System.currentTimeMillis();
        
        for (int i=0;i<iterations;i++)
        {
            metaMixed100.cloneRow(rowMixed100);
        }
        
        long stopTime = System.currentTimeMillis();
        
        return stopTime - startTime;
    }
    
    public long runTestStrings1000(int iterations) throws KettleValueException
    {
        long startTime = System.currentTimeMillis();
        
        for (int i=0;i<iterations;i++)
        {
            metaString1000.cloneRow(rowString1000);
        }
        
        long stopTime = System.currentTimeMillis();
        
        return stopTime - startTime;
    }

    public long runTestMixed1000(int iterations) throws KettleValueException
    {
        long startTime = System.currentTimeMillis();
        
        for (int i=0;i<iterations;i++)
        {
            metaMixed1000.cloneRow(rowMixed1000);
        }
        
        long stopTime = System.currentTimeMillis();
        
        return stopTime - startTime;
    }

    public static final int ITERATIONS = 1000000;

    public static void main(String[] args) throws KettleValueException
    {
        SpeedTest speedTest = new SpeedTest();
        
        long timeString10 = speedTest.runTestStrings10(ITERATIONS);
        System.out.println("Time to run 'String10' test "+ITERATIONS+" times : "+timeString10+" ms ("+(1000*ITERATIONS/timeString10)+" r/s)");
        long timeMixed10 = speedTest.runTestMixed10(ITERATIONS);
        System.out.println("Time to run 'Mixed10' test "+ITERATIONS+" times : "+timeMixed10+" ms ("+(1000*ITERATIONS/timeMixed10)+" r/s)");
        System.out.println();

        long timeString100 = speedTest.runTestStrings100(ITERATIONS);
        System.out.println("Time to run 'String100' test "+ITERATIONS+" times : "+timeString100+" ms ("+(1000*ITERATIONS/timeString100)+" r/s)");
        long timeMixed100 = speedTest.runTestMixed100(ITERATIONS);
        System.out.println("Time to run 'Mixed100' test "+ITERATIONS+" times : "+timeMixed100+" ms ("+(1000*ITERATIONS/timeMixed100)+" r/s)");
        System.out.println();

        long timeString1000 = speedTest.runTestStrings1000(ITERATIONS);
        System.out.println("Time to run 'String1000' test "+ITERATIONS+" times : "+timeString1000+" ms ("+(1000*ITERATIONS/timeString1000)+" r/s)");
        long timeMixed1000 = speedTest.runTestMixed1000(ITERATIONS);
        System.out.println("Time to run 'Mixed1000' test "+ITERATIONS+" times : "+timeMixed1000+" ms ("+(1000*ITERATIONS/timeMixed1000)+" r/s)");
        System.out.println();
    }

}
