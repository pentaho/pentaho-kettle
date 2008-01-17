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
 

package be.ibridge.kettle.trans.step.xmloutput;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class XMLOutput extends BaseStep implements StepInterface
{
	private XMLOutputMeta meta;
	private XMLOutputData data;
	 
	public XMLOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XMLOutputMeta)smi;
		data=(XMLOutputData)sdi;

		Row r;
		boolean result=true;
		
		r=getRow();       // This also waits for a row to be finished.
		
		if ( ( r!=null && linesOutput>0 && meta.getSplitEvery()>0 && (linesOutput%meta.getSplitEvery())==0) )
		{
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
		
        if (checkFeedback(linesOutput)) logBasic("linenr "+linesOutput);
		
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
				
				data.fieldnrs=new int[meta.getOutputFields().length];
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					data.fieldnrs[i]=r.searchValueIndex(meta.getOutputFields()[i].getFieldName());
					if (data.fieldnrs[i]<0)
					{
						logError("Field ["+meta.getOutputFields()[i].getFieldName()+"] couldn't be found in the input stream!");
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
                
                // OK, write a new row to the XML file:
                data.writer.write((" <"+meta.getRepeatElement()+">").toCharArray());
                
				for (int i=0;i<r.size();i++)
				{
					if (i>0) data.writer.write(' '); // put a space between the XML elements of the row.
	
					v=r.getValue(i);
	
					writeField(v, -1, v.getName());
				}
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
                
                // Write a new row to the XML file:
                data.writer.write((" <"+meta.getRepeatElement()+">").toCharArray());

				for (int i=0;i<meta.getOutputFields().length;i++)
				{
                    XMLField outputField = meta.getOutputFields()[i];
                    
					if (i>0) data.writer.write(' '); // a space between elements
	
					v=r.getValue(data.fieldnrs[i]);
                    
					v.setLength(outputField.getLength(), outputField.getPrecision());
					
                    String element;
                    if (outputField.getElementName()!=null && outputField.getElementName().length()>0)
                    {
                        element=outputField.getElementName();
                    }
                    else
                    {
                        element=v.getName();
                    }
					writeField(v, i, element);
				}
			}

            data.writer.write((" </"+meta.getRepeatElement()+">").toCharArray());
            data.writer.write(Const.CR.toCharArray());
		}
		catch(Exception e)
		{
			logError("Error writing XML row :"+e.toString()+Const.CR+"Row: "+r);
			return false;
		}

		linesOutput++;
		
		return true;
	}
	
	private String formatField(Value v, int idx)
	{
		String retval="";

		XMLField field = null;
		if (idx>=0)
        {
            field = meta.getOutputFields()[idx];
        }
		
        if (v.isNumeric())
        {
			if (idx>=0 && field!=null && !Const.isEmpty(field.getFormat()))
			{
				if (v.isNull())
				{
					if (!Const.isEmpty(field.getNullString()))
                    {
                        retval=field.getNullString();
                    }
					else
                    {
                        retval = Const.NULL_NUMBER;
                    }
				}
				else
				{
                    // Formatting
                    if ( !Const.isEmpty(field.getFormat()) )
                    {
                        data.df.applyPattern(field.getFormat());
                    }
                    else
                    {
                        data.df.applyPattern(data.defaultDecimalFormat.toPattern());
                    }
                    // Decimal 
					if ( !Const.isEmpty( field.getDecimalSymbol()) )
                    {
                        data.dfs.setDecimalSeparator( field.getDecimalSymbol().charAt(0) );
                    }
                    else
                    {
                        data.dfs.setDecimalSeparator( data.defaultDecimalFormatSymbols.getDecimalSeparator() );
                    }
                    // Grouping
					if ( !Const.isEmpty( field.getGroupingSymbol()) )
                    {
                        data.dfs.setGroupingSeparator( field.getGroupingSymbol().charAt(0) );
                    }
                    else
                    {
                        data.dfs.setGroupingSeparator( data.defaultDecimalFormatSymbols.getGroupingSeparator() );
                    }
                    // Currency symbol
					if ( !Const.isEmpty( field.getCurrencySymbol()) ) 
                    {
                        data.dfs.setCurrencySymbol( field.getCurrencySymbol() );
                    }
                    else
                    {
                        data.dfs.setCurrencySymbol( data.defaultDecimalFormatSymbols.getCurrencySymbol() );
                    }
							
					data.df.setDecimalFormatSymbols(data.dfs);
                    
                    if (v.isBigNumber())
                    {
                        retval=data.df.format(v.getBigNumber());
                    }
                    else if (v.isNumber())
                    {
                        retval=data.df.format(v.getNumber());
                    }
                    else // Integer
                    {
                        retval=data.df.format(v.getInteger());
                    }
				}
			}
			else
			{
				if (v.isNull()) 
				{
					if (idx>=0 && field!=null && !Const.isEmpty(field.getNullString()))
                    {
                        retval=field.getNullString();
                    }
					else
                    {
                        retval = Const.NULL_NUMBER;
                    }
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
			if (idx>=0 && field!=null && !Const.isEmpty(field.getFormat()) && v.getDate()!=null)
			{
                if (!Const.isEmpty(field.getFormat()))
                {
                    data.daf.applyPattern( field.getFormat() );
                }
                else
                {
                    data.daf.applyPattern( data.defaultDateFormat.toPattern() );
                }
				data.daf.setDateFormatSymbols(data.dafs);
				retval= data.daf.format(v.getDate());
			}
			else
			{
				if (v.isNull() || v.getDate()==null) 
				{
					if (idx>=0 && field!=null && !Const.isEmpty(field.getNullString()))
                    {
                        retval=field.getNullString();
                    }
                    else
                    {
                        retval = Const.NULL_DATE;
                    }
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
				if (idx>=0 && field!=null && !Const.isEmpty(field.getNullString()))
                {
                    retval=field.getNullString();
                }
                else
                {
                    retval = Const.NULL_STRING;
                }			}
			else
			{
				retval=v.toString();
			}
		}
        else if (v.isBinary())
        {
            if (v.isNull())
            {
                if (!Const.isEmpty(field.getNullString()))
                {
                    retval=field.getNullString();
                }
                else
                {
                    retval=Const.NULL_BINARY;
                }
            }
            else
            {                   
                try 
                {
                    retval=new String(v.getBytes(), "UTF-8");
                } 
                catch (UnsupportedEncodingException e) 
                {
                    // chances are small we'll get here. UTF-8 is
                    // mandatory.
                    retval=Const.NULL_BINARY;   
                }                   
            }
        }        
		else // Boolean
		{
			if (v.isNull()) 
			{
				if (idx>=0 && field!=null && !Const.isEmpty(field.getNullString()))
                {
                    retval=field.getNullString();
                }
                else
                {
                    retval = Const.NULL_BOOLEAN;
                }
			}
			else
			{
				retval=v.toString();
			}
		}
        
		return retval;
	}
	
	private void writeField(Value v, int idx, String element) throws KettleStepException
	{
		try
		{
            String str = XMLHandler.addTagValue( element, formatField(v, idx), false);
            if (str!=null) data.writer.write(str.toCharArray());
		}
		catch(Exception e)
		{
			throw new KettleStepException("Error writing line :", e);
		}
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
			FileObject file = KettleVFS.getFileObject(buildFilename(true));

			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname());
			resultFile.setComment("This file was created with a xml output step");
            addResultFile(resultFile);
            
            OutputStream outputStream;
			if (meta.isZipped())
			{
				OutputStream fos = KettleVFS.getOutputStream(file, false);
				data.zip = new ZipOutputStream(fos);
				File entry = new File(buildFilename(false));
				ZipEntry zipentry = new ZipEntry(entry.getName());
				zipentry.setComment("Compressed by Kettle");
				data.zip.putNextEntry(zipentry);
				outputStream=data.zip;
			}
			else
			{
				OutputStream fos=KettleVFS.getOutputStream(file, false);
				outputStream=fos;
			}
            if (meta.getEncoding()!=null && meta.getEncoding().length()>0)
            {
                log.logBasic(toString(), "Opening output stream in encoding: "+meta.getEncoding());
                data.writer = new OutputStreamWriter(outputStream, meta.getEncoding());
                data.writer.write( XMLHandler.getXMLHeader(meta.getEncoding()).toCharArray());
            }
            else
            {
                log.logBasic(toString(), "Opening output stream in default encoding : "+Const.XML_ENCODING);
                data.writer = new OutputStreamWriter(outputStream);
                data.writer.write( XMLHandler.getXMLHeader(Const.XML_ENCODING).toCharArray());
            }
            
            // OK, write the header & the parent element:
            data.writer.write( ("<"+meta.getMainElement()+">"+Const.CR).toCharArray());
						
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
            // Close the parent element
            data.writer.write( ("</"+meta.getMainElement()+">"+Const.CR).toCharArray());
            
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
		meta=(XMLOutputMeta)smi;
		data=(XMLOutputData)sdi;

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
		meta=(XMLOutputMeta)smi;
		data=(XMLOutputData)sdi;
		
		closeFile();
        
        super.dispose(smi, sdi);
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
