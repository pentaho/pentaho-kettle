/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.gpbulkloader;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;


/**
 * Does the opening of the output "stream". It's either a file or inter process
 * communication which is transparant to users of this class.
 *
 * Copied from Sven Boden's Oracle version
 *
 * @author Luke Lonergan
 * @since  28-mar-2008
 */
public class GPBulkDataOutput
{
	private GPBulkLoaderMeta meta;
	private PrintWriter       output = null;
	private boolean           first = true;
	private int               fieldNumbers[] = null;
	private String            enclosure = null;
	private SimpleDateFormat  sdfDate = null;
	private SimpleDateFormat  sdfDateTime = null;

	public GPBulkDataOutput(GPBulkLoaderMeta meta)
	{
		this.meta = meta;
	}
	
	public void open(VariableSpace space, Process sqlldrProcess) throws KettleException
	{
		// String loadMethod = meta.getLoadMethod();
		try 
		{
			OutputStream os = null;

		//	if ( GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(loadMethod))
		//	{
		//		String dataFile = meta.getControlFile();
		//		dataFile = StringUtil.environmentSubstitute(dataFile);
				
        //      os = new FileOutputStream(dataFile, true);
		//	}
		//	else
		//	{
				// Else open the data file filled in.
				String dataFile = meta.getDataFile();
				dataFile = space.environmentSubstitute(dataFile);
				
                os = new FileOutputStream(dataFile, false);
		//	}	
			
            String encoding = meta.getEncoding();
            if ( Const.isEmpty(encoding) )
            {
            	// Use the default encoding.
			    output = new PrintWriter(
  	                      	     new BufferedWriter(
							      new OutputStreamWriter(os)));
            }
            else
            {
            	// Use the specified encoding
			    output = new PrintWriter(
  	                      	     new BufferedWriter(
							      new OutputStreamWriter(os, encoding)));
            }
		}
		catch ( IOException e )
		{
			throw new KettleException("IO exception occured: "  + e.getMessage(), e);
		}
	}
	
	public void close() throws IOException
	{
		if ( output != null )
		{
			output.close();
		}
	}
	
	PrintWriter getOutput()
	{
	    return output;
	}
	
	private String createEscapedString(String orig, String enclosure)
	{
		StringBuffer buf = new StringBuffer(orig);
		
		Const.repl(buf, enclosure, enclosure + enclosure);
		return buf.toString();
	}
	
	public void writeLine(RowMetaInterface mi, Object row[]) throws KettleException
	{
        if ( first )
        {
            first = false;
     
            enclosure = meta.getEnclosure();
            
            // Setup up the fields we need to take for each of the rows
            // as this speeds up processing.
            fieldNumbers=new int[meta.getFieldStream().length];
			for (int i=0;i<fieldNumbers.length;i++) 
			{
				fieldNumbers[i]=mi.indexOfValue(meta.getFieldStream()[i]);
				if (fieldNumbers[i]<0)
				{
					throw new KettleException("Could not find field " + 
							                  meta.getFieldStream()[i] + " in stream");
				}
			}
			
			sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        
        // Write the data to the output
        ValueMetaInterface v = null;
        int number = 0;
		for (int i=0;i<fieldNumbers.length;i++) 
		{
			if ( i!=0 ) output.print(",");
			number = fieldNumbers[i];
			v = mi.getValueMeta(number);
			if ( row[number] == null)
			{
				// TODO (SB): special check for null in case of Strings.
				output.print(enclosure);
				output.print(enclosure);
			}
			else
			{
				switch ( v.getType() )
				{
				case ValueMetaInterface.TYPE_STRING:
					String s = mi.getString(row, number);
					if ( s.indexOf(enclosure) >= 0 )
						s = createEscapedString(s, enclosure);
					output.print(enclosure);
					output.print(s);
					output.print(enclosure);
					break;
				case ValueMetaInterface.TYPE_INTEGER:
					Long l = mi.getInteger(row, number);
					output.print(enclosure);
					output.print(l);
					output.print(enclosure);
					break;
				case ValueMetaInterface.TYPE_NUMBER:
					Double d = mi.getNumber(row, number);
					output.print(enclosure);
					output.print(d);
					output.print(enclosure);
					break;
				case ValueMetaInterface.TYPE_BIGNUMBER:
					BigDecimal bd = mi.getBigNumber(row, number);
					output.print(enclosure);
					output.print(bd);
					output.print(enclosure);
					break;
				case ValueMetaInterface.TYPE_DATE:
					Date dt = mi.getDate(row, number);
					output.print(enclosure);
					String mask = meta.getDateMask()[i];
					if ( GPBulkLoaderMeta.DATE_MASK_DATETIME.equals(mask))
					{
						output.print(sdfDateTime.format(dt));	
					}
					else
					{
						// Default is date format
						output.print(sdfDate.format(dt));
					}					   
					output.print(enclosure);
					break;
				case ValueMetaInterface.TYPE_BOOLEAN:
					Boolean b = mi.getBoolean(row, number);
					output.print(enclosure);
					if ( b.booleanValue() )
						output.print("Y");
					else
						output.print("N");
					output.print(enclosure);
					break;			    	
				case ValueMetaInterface.TYPE_BINARY:
					byte byt[] = mi.getBinary(row, number);
					output.print("<startlob>");
					output.print(byt);
					output.print("<endlob>");
					break;			    
				default:
					throw new KettleException("Unsupported type");
				}
			}
		}
		output.print(Const.CR);
	}
}