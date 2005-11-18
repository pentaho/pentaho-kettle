/***********************************************************************
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

/**
 * Handles file reading from XBase (DBF) type of files.
 * 
 *  @author Matt
 *  @since 12-08-2004
 *
 */

public class XBase
{
    private LogWriter log;
    private String file_dbf;
    private DBFReader   reader;
    private InputStream inputstream;
    private boolean     error;
    private byte        datatype[];
    
    public XBase(String file_dbf)
    {
        this.log      = LogWriter.getInstance();
        this.file_dbf = file_dbf;
        error         = false;
        reader        = null;
        inputstream   = null;
     }
    
    public void open() throws KettleException
    {
        try
        {
        	inputstream = new FileInputStream( file_dbf );
	        reader = new DBFReader(inputstream);
        }
        catch(DBFException e)
        {
            throw new KettleException("Error opening DBF metadata", e);
        }
        catch(IOException e)
        {
            throw new KettleException("Error reading DBF file", e);
        }
    }
        
    public Row getFields()
    	throws KettleException
    {
        String debug="get fields from XBase file";
        Row row = new Row();
        
        try
        {
            // Fetch all field information
            //
            debug="allocate data types";
        	datatype = new byte[reader.getFieldCount()];
        		
            for( int i=0; i<reader.getFieldCount(); i++) 
            {
              debug="get field #"+i;

              DBFField field = reader.getField(i);
              Value value = null;
              
              datatype[i] = field.getDataType();
              switch(datatype[i])
              {
              case DBFField.FIELD_TYPE_M: // Memo
                  debug="memo field";
				  System.out.println("Field #"+i+" is a memo-field! ("+field.getName()+")");
              case DBFField.FIELD_TYPE_C: // Character
                  // case DBFField.FIELD_TYPE_P: // Picture
                  debug="character field";
                  value = new Value(field.getName(), Value.VALUE_TYPE_STRING);
              	  value.setLength(field.getFieldLength());
              	  break;
              case DBFField.FIELD_TYPE_N: // Numeric
              case DBFField.FIELD_TYPE_F: // Float
                  debug="Number field";
                  value = new Value(field.getName(), Value.VALUE_TYPE_NUMBER);
              	  value.setLength(field.getFieldLength(), field.getDecimalCount());
              	  break;
              case DBFField.FIELD_TYPE_L: // Logical
                  debug="Logical field";
                  value = new Value(field.getName(), Value.VALUE_TYPE_BOOLEAN);
              	  value.setLength(-1, -1);
          	  	  break;
              case DBFField.FIELD_TYPE_D: // Date
                  debug="Date field";
                  value = new Value(field.getName(), Value.VALUE_TYPE_DATE);
              	  value.setLength(-1, -1);
          	  	  break;
          	  default: break;
              }
              
              if (value!=null)
              {
                  row.addValue(value);
              }
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Error reading DBF metadata (in part "+debug+")", e);
        }
        
        return row;
    }
    
    public Row getRow(Row fields)
    {
    	String debug = "-";
        Row r = null;
        
        // if (recnr>dbf.getRecordCount()) return null; // not available!
        
        try
        {
        	// Read the next record
        	debug = "get next record from reader!";
            Object rowobj[] = reader.nextRecord();
            
            // Are we at the end yet?
            if (rowobj == null) return null;
            
            // Copy the default row for speed...
        	debug = "copy the default row for speed!";
        	r = new Row(fields);
        	
        	debug = "set the values in the row";
        	// Set the values in the row...
			for( int i=0; i<reader.getFieldCount(); i++)
			{
	        	debug = "getting value #"+i;
				Value v = r.getValue(i);
				
	        	debug = "setting value #"+i+" : "+v.getName()+" datatype='"+(char)datatype[i]+"'";
				switch(datatype[i])
				{
				case DBFField.FIELD_TYPE_M: // Memo
					if (rowobj[i]==null) v.setNull();
					else v.setValue((String)rowobj[i]);
					break;
				case DBFField.FIELD_TYPE_C: // Character
					v.setValue( Const.rtrim( (String)rowobj[i] ) ); 
					break; 
				case DBFField.FIELD_TYPE_N: // Numeric
			    	// Convert to Double!!
			    	try
					{
			    		if (rowobj[i]==null) v.setNull();
			    		else                 v.setValue( ((Double)rowobj[i]).doubleValue() );
					}
			    	catch(NumberFormatException e)
					{
			    		v.setNull();
					}
					break;
			    case DBFField.FIELD_TYPE_F: // Float
			    	// Convert to double!!
			    	try
					{
			    		if (rowobj[i]==null) v.setNull();
			    		else                 v.setValue( ((Float)rowobj[i]).doubleValue() );
					}
			    	catch(NumberFormatException e)
					{
			    		v.setNull();
					}
					break;
				case DBFField.FIELD_TYPE_L:  // Logical
					v.setValue( ((Boolean)rowobj[i]).booleanValue() ); 
					break; 
				case DBFField.FIELD_TYPE_D:  // Date
					v.setValue( ((Date)rowobj[i]) ); 
					break;
				/*
				case DBFField.FIELD_TYPE_P:  // Picture
					v.setValue( (String)rowobj[i] ); // Set to String at first...
					break;
				*/
				default: break;
				}
			}
        }
        catch(DBFException e)
        {
            log.logError(toString(), "Unable to read row in part ["+debug+"] : "+e.toString());
            error = true;
            return null;
        }
        catch(Exception e)
		{
            log.logError(toString(), "Unexpected error in part ["+debug+"] : "+e.toString());
            error = true;
            return null;
		}
        
        return r;
    }
    
    public boolean close()
    {
        boolean retval = false;;
        try
        {
            if (inputstream!=null) inputstream.close();

            retval=true;
        }
        catch(IOException e)
        {
            log.logError(toString(), "Couldn't close file ["+file_dbf+"] : "+e.toString());
            error = true;
        }
        
        return retval;
    }
    
    public boolean hasError()
    {
    	return error;
    }

    public String toString()
    {
    	if (file_dbf!=null)	return "["+file_dbf+"]";
    	else 				return getClass().getName();
    }
    
    public String getVersionInfo()
    {
    	return reader.getHeader().getSignatureDesc();
    }
    
    public boolean setMemo(String memo_file)
    {
    	try
		{
    		if (reader.hasMemo())
    		{
    			RandomAccessFile raf = new RandomAccessFile(memo_file, "r");
    			reader.setMemoFile(raf);
    			
    			// System.out.println("Memo set! ");
    		}
    		return true;
		}
    	catch(Exception e)
		{
    		return false;
		}
    }
}
