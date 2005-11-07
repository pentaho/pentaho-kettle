 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.trans.step.textfileoutput;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Converts input rows to text and then writes this text to one or more files.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileOutput extends BaseStep implements StepInterface
{
	private TextFileOutputMeta meta;
	private TextFileOutputData data;
	 
	public TextFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		Row r;
		boolean result=true;
		
		r=getRow();       // This also waits for a row to be finished.
		
		if ( ( r==null && data.headerrow!=null && meta.footer ) ||
		     ( r!=null && linesOutput>0 && meta.splitEvery>0 && (linesOutput%meta.splitEvery)==0)
		   )
		{
			if (writeHeader()) linesOutput++;
			
			// Done with this part or with everything.
			closeFile();
			
			// Not finished: open another file...
			if (r!=null)
			{
				if (!openNewFile())
				{
					logError("Unable to open new file (split #"+data.splitnr+"...");
					setErrors(1);
					return false;
				}

				if (meta.header && data.headerrow!=null) if (writeHeader()) linesOutput++;
			}
		
		}
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		result=writeRowToFile(r);
		if (!result)
		{
			setErrors(1);
			stopAll();
			return false;
		}
		
		putRow(r);       // in case we want it to go further...
		
		if ((linesOutput>0) && (linesOutput%Const.ROWS_UPDATE)==0)logBasic("linenr "+linesOutput);
		
		return result;
	}

	private boolean writeRowToFile(Row r)
	{
		debug="row to file";
		Value v;
		
		try
		{	
			debug="Start";
			if (first)
			{
				first=false;
				if (!meta.fileAppended && ( meta.header || meta.footer)) // See if we have to write a header-line)
				{
					data.headerrow=new Row(r); // copy the row for the footer!
					if (meta.header)
					{
						if (writeHeader()) return false;
					}
				}
				
				data.fieldnrs=new int[meta.getOutputFields().length];
				debug="Get fieldnrs... field_name.length="+meta.getOutputFields().length;
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					data.fieldnrs[i]=r.searchValueIndex(meta.getOutputFields()[i].getName());
					if (data.fieldnrs[i]<0)
					{
						logError("Field ["+meta.getOutputFields()[i].getName()+"] couldn't be found in the input stream!");
						setErrors(1);
						stopAll();
						return false;
					}
				}
			}

			if (meta.getOutputFields()==null || meta.getOutputFields().length==0)
			{
				/*
				 * Write all values in stream to text file.
				 */
				debug="Loop fields 0.."+r.size();
	
				for (int i=0;i<r.size();i++)
				{
					debug="start for loop";
					if (i>0) data.fw.write(meta.separator.getBytes());
	
					debug="Get value "+i+" of "+r.size();
					v=r.getValue(i);
	
					debug="Write field to output stream: ["+v.toString()+"] of type ["+v.toStringMeta()+"]";
					writeField(v, -1);
				}
				data.fw.write(meta.newline.getBytes());
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
				debug="Loop fields 0.."+meta.getOutputFields().length;
	
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					debug="start for loop";
					if (i>0) data.fw.write(meta.separator.getBytes());
	
					debug="Get value "+data.fieldnrs[i]+" of row ";
					v=r.getValue(data.fieldnrs[i]);
					v.setLength(meta.getOutputFields()[i].getLength(), meta.getOutputFields()[i].getPrecision());
					
					writeField(v, i);
				}
				data.fw.write(meta.newline.getBytes());
			}
		}
		catch(Exception e)
		{
			logError("Error writing line in ["+debug+"] :"+e.toString());
			return false;
		}

		linesOutput++;
		
		return true;
	}
	
	private String formatField(Value v, int idx)
	{
		String retval="";

		TextFileField field = null;
		if (idx>=0) field = meta.getOutputFields()[idx];
		
        if (v.isBigNumber())
        {
            retval+=v.getString(); // Sorry, no formatting yet, just dump it...
        }
        else
		if (v.isNumeric())
		{
			debug="Number is formatted?";
			if (idx>=0 && field.getFormat()!=null)
			{
				if (v.isNull())
				{
					if (field.getNullString()!=null) retval=field.getNullString();
				}
				else
				{
					debug="Formatted number pattern, field="+v.getName()+", idx="+idx;
	
					data.df.applyPattern(field.getFormat());
					if (field.getDecimalSymbol()!=null && field.getDecimalSymbol().length()>0)  data.dfs.setDecimalSeparator( field.getDecimalSymbol().charAt(0) );
					if (field.getGroupingSymbol()!=null && field.getGroupingSymbol().length()>0)    data.dfs.setGroupingSeparator( field.getGroupingSymbol().charAt(0) );
					if (field.getCurrencySymbol()!=null) data.dfs.setCurrencySymbol( field.getCurrencySymbol() );
							
					data.df.setDecimalFormatSymbols(data.dfs);
					retval=data.df.format(v.getNumber());
				}
			}
			else
			{
				debug="Not formatted number";
				if (v.isNull()) 
				{
					if (idx>=0 && field.getNullString()!=null) retval=field.getNullString();
				}
				else
				{
					retval=v.toString();
				}
			}
		}
		else
		if (v.isDate())
		{
			debug="Number is formatted?";
			if (idx>=0 && field.getFormat()!=null && v.getDate()!=null)
			{
				debug="Formatted date pattern, field="+v.getName()+", idx="+idx;
							
				data.daf.applyPattern( field.getFormat() );
				data.daf.setDateFormatSymbols(data.dafs);
				retval= data.daf.format(v.getDate());
			}
			else
			{
				if (v.isNull() || v.getDate()==null) 
				{
					if (idx>=0 && field.getNullString()!=null) retval=field.getNullString();
				}
				else
				{
					retval=v.toString();
				}
			}
		}
		else
		if (v.isString())
		{
			debug="String length="+v.getLength()+", value="+v.toString();
			if (v.isNull() || v.getString()==null) 
			{
				if (idx>=0 && field.getNullString()!=null) retval=field.getNullString();
			}
			else
			{
				// Any separators in string?
				// example: 123.4;"a;b;c";Some name
				// 
				int seppos = v.toString().indexOf(meta.separator);
				
				if (seppos<0) retval=v.toString();
				else          retval=meta.enclosure+v.toString()+meta.enclosure;
			}
		}
		else // Boolean
		{
			debug="Boolean: "+v.getBoolean();
			if (v.isNull()) 
			{
				if (idx>=0 && field.getNullString()!=null) retval=field.getNullString();
			}
			else
			{
				retval=v.toString();
			}
		}
		
		if (meta.padded) // maybe we need to rightpad to field length?
		{
			// What's the field length?
			int length, precision;
			
			if (idx<0) // Nothing specified: get it from the values themselves.
			{
				length   =v.getLength()<0?0:v.getLength();
				precision=v.getPrecision()<0?0:v.getPrecision();
			}
			else // Get the info from the specified lengths & precision...
			{
				length   =field.getLength()   <0?0:field.getLength();
				precision=field.getPrecision()<0?0:field.getPrecision();
			}

			if (v.isNumber())
			{
				length++; // for the sign...
				if (precision>0) length++; // for the decimal point... 
			}
			if (v.isDate()) { length=23; precision=0; }
			if (v.isBoolean()) { length=5; precision=0; }
			
			retval=Const.rightPad(new StringBuffer(retval), length+precision);
		}
		
		return retval;
	}
	
	private boolean writeField(Value v, int idx)
	{
		try
		{
			String str = formatField(v, idx);
			data.fw.write(str.getBytes());
		}
		catch(Exception e)
		{
			logError("Error writing line in ["+debug+"] :"+e.toString());
			return false;
		}
		return true;
	}
	
	private boolean writeHeader()
	{
		debug="header";
		boolean retval=false;
		Row r=data.headerrow;
		
		try
		{
			// If we have fields specified: list them in this order!
			if (meta.getOutputFields()!=null && meta.getOutputFields().length>0)
			{
				String header = "";
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					if (i>0 && meta.separator!=null && meta.separator.length()>0)
					{
						header+=meta.separator;
					}
					header+=meta.getOutputFields()[i].getName();
				}
				header+=meta.newline;
				data.fw.write(header.getBytes());
			}
			else
			if (r!=null)
			{
				for (int i=0;i<r.size();i++)
				{
					if (i>0) data.fw.write(meta.separator.getBytes());
					Value v = r.getValue(i);
					
					// Header-value contains the name of the value
					Value header_value = new Value(v.getName(), Value.VALUE_TYPE_STRING);
					header_value.setValue(v.getName());
					
					// What's the fields index?
					int idx=-1;
					for (int x=0;x<meta.getOutputFields().length && idx<0; x++)
					{
						if (meta.getOutputFields()[x].getName().equalsIgnoreCase(v.getName())) idx=x;
					}
					switch(v.getType())
					{
					case Value.VALUE_TYPE_DATE:   v.setValue(new Date()); break;
					case Value.VALUE_TYPE_STRING: v.setValue("a"); break;
					default: break; 
					}
					//String fmt = formatField( v, idx );
					//header_value.setLength( fmt.length() );
					data.fw.write(header_value.toString().getBytes());
				}
				data.fw.write(meta.newline.getBytes());
			}
			else
			{
				data.fw.write(("no rows selected"+Const.CR).getBytes());
			}
		}
		catch(Exception e)
		{
			logError("Error writing header line: "+e.toString());
			e.printStackTrace();
			retval=true;
		}
		linesOutput++;
		return retval;
	}

	public String buildFilename(boolean ziparchive)
	{
		return meta.buildFilename(getCopy(), data.splitnr, ziparchive);
	}
	
	public boolean openNewFile()
	{
		boolean retval=false;
		data.fw=null;
		
		try
		{
			File file = new File(buildFilename(true));

			if (meta.zipped)
			{
				FileOutputStream fos = new FileOutputStream(file, meta.fileAppended);
				data.zip = new ZipOutputStream(fos);
				File entry = new File(buildFilename(false));
				ZipEntry zipentry = new ZipEntry(entry.getName());
				zipentry.setComment("Compressed by Kettle");
				data.zip.putNextEntry(zipentry);
				data.fw=data.zip;
			}
			else
			{
				FileOutputStream fos=new FileOutputStream(file, meta.fileAppended);
				data.fw=fos;
			}
						
			retval=true;
		}
		catch(Exception e)
		{
			logError("Error opening new file : "+e.toString());
		}
		// System.out.println("end of newFile(), splitnr="+splitnr);

		data.splitnr++;

		return retval;
	}
	
	private boolean closeFile()
	{
		boolean retval=false;
		
		try
		{
			if (meta.zipped)
			{
				//System.out.println("close zip entry ");
				data.zip.closeEntry();
				//System.out.println("finish file...");
				data.zip.finish();
				data.zip.close();
			}
			else
			{
				data.fw.close();
			}

			//System.out.println("Closed file...");
			
			retval=true;
		}
		catch(Exception e)
		{
		}
		
		return retval;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		if (super.init(smi, sdi))
		{
			data.splitnr=0;
			
			if (openNewFile())
			{
				debug="start";
				return true;
			}
			else
			{
				logError("Couldn't open file "+meta.fileName+" ["+debug+"]");
				setErrors(1L);
				stopAll();
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;
		
		super.dispose(smi, sdi);
		
		closeFile();
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}	
}
