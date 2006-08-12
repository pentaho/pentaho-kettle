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
 

package be.ibridge.kettle.trans.step.textfileoutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
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
		
		if ( ( r==null && data.headerrow!=null && meta.isFooterEnabled() ) ||
		     ( r!=null && linesOutput>0 && meta.getSplitEvery()>0 && (linesOutput%meta.getSplitEvery())==0)
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

				if (meta.isHeaderEnabled() && data.headerrow!=null) if (writeHeader()) linesOutput++;
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
		Value v;
		
		try
		{	
			if (first)
			{
				first=false;
				if (!meta.isFileAppended() && ( meta.isHeaderEnabled() || meta.isFooterEnabled())) // See if we have to write a header-line)
				{
					data.headerrow=new Row(r); // copy the row for the footer!
					if (meta.isHeaderEnabled())
					{
						if (writeHeader()) return false;
					}
				}
				
				data.fieldnrs=new int[meta.getOutputFields().length];
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
				for (int i=0;i<r.size();i++)
				{
					if (i>0) data.writer.write(meta.getSeparator().toCharArray());
					v=r.getValue(i);
					if(!writeField(v, -1)) return false;
				}
                data.writer.write(meta.getNewline().toCharArray());
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					if (i>0) data.writer.write(meta.getSeparator().toCharArray());
	
					v=r.getValue(data.fieldnrs[i]);
					v.setLength(meta.getOutputFields()[i].getLength(), meta.getOutputFields()[i].getPrecision());
					
					if(!writeField(v, i)) return false;
				}
                data.writer.write(meta.getNewline().toCharArray());
			}
		}
		catch(Exception e)
		{
			logError("Error writing line :"+e.toString());
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
			if (idx>=0 && field!=null && field.getFormat()!=null)
			{
				if (v.isNull())
				{
					if (field.getNullString()!=null) retval=field.getNullString();
					else retval = "";
				}
				else
				{
					data.df.applyPattern(field.getFormat());
					if (field.getDecimalSymbol()!=null && field.getDecimalSymbol().length()>0)  data.dfs.setDecimalSeparator( field.getDecimalSymbol().charAt(0) );
					if (field.getGroupingSymbol()!=null && field.getGroupingSymbol().length()>0)    data.dfs.setGroupingSeparator( field.getGroupingSymbol().charAt(0) );
					if (field.getCurrencySymbol()!=null) data.dfs.setCurrencySymbol( field.getCurrencySymbol() );
							
					data.df.setDecimalFormatSymbols(data.dfs);
					retval=data.df.format(v.getBigNumber());
				}
			}
			else
			{
				if (v.isNull()) 
				{
					if (idx>=0 && field!=null && field.getNullString()!=null) retval=field.getNullString();
					else retval = "";
				}
				else
				{
					retval=v.toString();
				}
			}
        }
        else
		if (v.isNumeric())
		{
			if (idx>=0 && field!=null && field.getFormat()!=null)
			{
				if (v.isNull())
				{
					if (field.getNullString()!=null) retval=field.getNullString();
					else retval = "";
				}
				else
				{
					data.df.applyPattern(field.getFormat());
					if (field.getDecimalSymbol()!=null && field.getDecimalSymbol().length()>0)  data.dfs.setDecimalSeparator( field.getDecimalSymbol().charAt(0) );
					if (field.getGroupingSymbol()!=null && field.getGroupingSymbol().length()>0)    data.dfs.setGroupingSeparator( field.getGroupingSymbol().charAt(0) );
					if (field.getCurrencySymbol()!=null) data.dfs.setCurrencySymbol( field.getCurrencySymbol() );
							
					data.df.setDecimalFormatSymbols(data.dfs);
					if ( v.isInteger() )
					{
					    retval=data.df.format(v.getInteger());
					}
					else if ( v.isNumber() )
					{
					    retval=data.df.format(v.getNumber());
					}						
				}
			}
			else
			{
				if (v.isNull()) 
				{
					if (idx>=0 && field!=null && field.getNullString()!=null) retval=field.getNullString();
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
			if (idx>=0 && field!=null && field.getFormat()!=null && v.getDate()!=null)
			{
				data.daf.applyPattern( field.getFormat() );
				data.daf.setDateFormatSymbols(data.dafs);
				retval= data.daf.format(v.getDate());
			}
			else
			{
				if (v.isNull() || v.getDate()==null) 
				{
					if (idx>=0 && field!=null && field.getNullString()!=null) retval=field.getNullString();
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
			if (v.isNull() || v.getString()==null) 
			{
				if (idx>=0 && field!=null && field.getNullString()!=null) 
                {
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null)
                    {
                        retval=meta.getEnclosure()+field.getNullString()+meta.getEnclosure();
                    }
                    else
                    {
                        retval=field.getNullString();
                    }
                }
			}
			else
			{
				// Any separators in string?
				// example: 123.4;"a;b;c";Some name
				//
                if (meta.isEnclosureForced() && meta.getEnclosure()!=null) // Force enclosure?
                {
                    retval=meta.getEnclosure()+v.toString()+meta.getEnclosure();
                }
                else // See if there is a separator in the String...
                {
    				int seppos = v.toString().indexOf(meta.getSeparator());
    				
    				if (seppos<0) retval=v.toString();
    				else          retval=meta.getEnclosure()+v.toString()+meta.getEnclosure();
                }
			}
		}
		else // Boolean
		{
			if (v.isNull()) 
			{
				if (idx>=0 && field!=null && field.getNullString()!=null) retval=field.getNullString();
			}
			else
			{
				retval=v.toString();
			}
		}
		
		if (meta.isPadded()) // maybe we need to rightpad to field length?
		{
			// What's the field length?
			int length, precision;
			
			if (idx<0 || field==null) // Nothing specified: get it from the values themselves.
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
            if (str!=null) data.writer.write(str.toCharArray());
		}
		catch(Exception e)
		{
			logError("Error writing line :"+e.toString());
			return false;
		}
		return true;
	}
	
	private boolean writeHeader()
	{
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
                    String fieldName = meta.getOutputFields()[i].getName();
                    Value v = r.searchValue(fieldName);
                    
					if (i>0 && meta.getSeparator()!=null && meta.getSeparator().length()>0)
					{
						header+=meta.getSeparator();
					}
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v!=null && v.isString())
                    {
                        header+=meta.getEnclosure();
                    }
					header+=fieldName;
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v!=null && v.isString())
                    {
                        header+=meta.getEnclosure();
                    }
				}
				header+=meta.getNewline();
                data.writer.write(header.toCharArray());
			}
			else
			if (r!=null)  // Just put all field names in the header/footer
			{
				for (int i=0;i<r.size();i++)
				{
					if (i>0) data.writer.write(meta.getSeparator().toCharArray());
					Value v = r.getValue(i);
					
                    // Header-value contains the name of the value
					Value header_value = new Value(v.getName(), Value.VALUE_TYPE_STRING);
					header_value.setValue(v.getName());

                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v.isString())
                    {
                        data.writer.write(meta.getEnclosure().toCharArray());
                    }
                    data.writer.write(header_value.toString().toCharArray());
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v.isString())
                    {
                        data.writer.write(meta.getEnclosure().toCharArray());
                    }
				}
                data.writer.write(meta.getNewline().toCharArray());
			}
			else
			{
                data.writer.write(("no rows selected"+Const.CR).toCharArray());
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
		data.writer=null;
		
		try
		{
			File file = new File(buildFilename(true));

			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname());
			resultFile.setComment("This file was created with a text file output step");
            addResultFile(resultFile);

            OutputStream outputStream;
			if (meta.isZipped())
			{
				FileOutputStream fos = new FileOutputStream(file, meta.isFileAppended());
				data.zip = new ZipOutputStream(fos);
				File entry = new File(buildFilename(false));
				ZipEntry zipentry = new ZipEntry(entry.getName());
				zipentry.setComment("Compressed by Kettle");
				data.zip.putNextEntry(zipentry);
				outputStream=data.zip;
			}
			else
			{
				FileOutputStream fos=new FileOutputStream(file, meta.isFileAppended());
				outputStream=fos;
			}
            if (meta.getEncoding()!=null && meta.getEncoding().length()>0)
            {
                log.logBasic(toString(), "Opening output stream in encoding: "+meta.getEncoding());
                data.writer = new OutputStreamWriter(outputStream, meta.getEncoding());
            }
            else
            {
                log.logBasic(toString(), "Opening output stream in default encoding");
                data.writer = new OutputStreamWriter(outputStream);
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
			if (meta.isZipped())
			{
				//System.out.println("close zip entry ");
				data.zip.closeEntry();
				//System.out.println("finish file...");
				data.zip.finish();
				data.zip.close();
			}
			else
			{
				data.writer.close();
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
				return true;
			}
			else
			{
				logError("Couldn't open file "+meta.getFileName());
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
			logError("Unexpected error : "+e.toString());
            logError(Const.getStackTracker(e));
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
