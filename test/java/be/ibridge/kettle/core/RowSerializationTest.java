 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.value.Value;

/**
 * Test class for the bug #3775 "Sort Rows Step". This testcase
 * makes sure that a Row written on a stream is read back in the same
 * state. This especially checks for "" and nulls.
 *
 * @author Sven Boden
 */
public class RowSerializationTest extends TestCase
{
	/**
	 * Test serialization().
	 */
	public void testSerialization()
	{
		Value values1[] = {
		    new Value("field1", "KETTLE"),                // String
			new Value("field2", 123L),                    // integer
			new Value("field3", 10.5D),                   // double
			new Value("field4", new Date()),              // Date
			new Value("field5", true),                    // Boolean
			new Value("field6", new BigDecimal(123.45))   // BigDecimal
		};

		Value values2[] = {
		    new Value("field1", ""),                  // String
			new Value("field2", 0L),                  // integer
			new Value("field3", 0.0D),                // double
			new Value("field4", (Date)null),          // Date
			new Value("field5", true),                // Boolean
			new Value("field6", (BigDecimal)null)     // BigDecimal
		};

		Value values3[] = {
		    new Value("field1", (String)null),        // String
			new Value("field2", 0L),                  // integer
			new Value("field3", 0.0D),                // double
			new Value("field4", (Date)null),          // Date
			new Value("field5", true),                // Boolean
			new Value("field6", (BigDecimal)null)     // BigDecimal
		};		
		
        Row r1 = new Row();
        for (int i=0; i < values1.length; i++ )
        {
            r1.addValue(values1[i]);
        }
        
        Row r1null = new Row();
        for (int i=0; i < values1.length; i++ )
        {
            Value nullValue = (Value) values1[i].clone();
            nullValue.setNull();
            r1null.addValue(nullValue);
        }

        Row r2 = new Row();
        for (int i=0; i < values2.length; i++ )
        {
            r2.addValue(values2[i]);
        }

        Row r2null = new Row();
        for (int i=0; i < values2.length; i++ )
        {
            Value nullValue = (Value) values2[i].clone();
            nullValue.setNull();
            r2null.addValue(nullValue);
        }

        Row r3 = new Row();
        for (int i=0; i < values3.length; i++ )
        {
            r3.addValue(values3[i]);
        }

        Row r3null = new Row();
        for (int i=0; i < values3.length; i++ )
        {
            Value nullValue = (Value) values3[i].clone();
            nullValue.setNull();
            r3null.addValue(nullValue);
        }

        Vector out = new Vector();
        out.add(r1);
        out.add(r1null);
        out.add(r2);
        out.add(r2null);
        out.add(r3);
        out.add(r3null);

		// Then write them to disk...
		File             fil = null;
		FileOutputStream fos;
		DataOutputStream dos;
		int p;
		
		try
		{
			fil=File.createTempFile("sorttmp", ".tmp", new File("."));
			fil.deleteOnExit();
			
			fos=new FileOutputStream(fil);
			dos=new DataOutputStream(fos);
		
			for (p=0;p<out.size();p++)
			{
				((Row)out.get(p)).write(dos);
			}
			// Close temp-file
			dos.close();  // close data stream
			fos.close();  // close file stream
		}
		catch(Exception e)
		{
			fail("raised an unpected error: "+e.toString()+Const.CR+Const.getStackTracker(e));
		}
		
		FileInputStream fi = null;
		try {
			fi = new FileInputStream( fil );
			
			DataInputStream di=new DataInputStream( fi );
			
			// Read rows from temp-file
			try {
				Row r1i = new Row(di);
                Row r1nulli = new Row(di);
                Row r2i = new Row(di);
                Row r2nulli = new Row(di);
		     	Row r3i = new Row(di);
                Row r3nulli = new Row(di);
		     	
		     	// We can't use Row.compare() here as the compare function
		     	// regards "" and null to be equal as string values.
                if ( ! r1i.toString().equals(r1.toString()) )
                    fail("r1 is load wrongly");

                if ( ! r1nulli.toString().equals(r1null.toString()) )
                    fail("r1null is load wrongly");

                if ( ! r2i.toString().equals(r2.toString()) )
                    fail("r2 is loaded wrongly");

                if ( ! r2nulli.toString().equals(r2null.toString()) )
                    fail("r2null is loaded wrongly");

                if ( ! r3i.toString().equals(r3.toString()) )
                    fail("r3 is loaded wrongly");           

                if ( ! r3nulli.toString().equals(r3null.toString()) )
                    fail("r3null is loaded wrongly"); 		
		     	
			} catch (KettleFileException e) {
				fail("raised an unpected error: "+e.getMessage()+Const.CR+Const.getStackTracker(e));
			}								
		} catch (FileNotFoundException e) {
			fail("raised an unpected error");
		}		

	}
}