 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.exceloutput;

import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.DateFormat;
import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Converts input rows to excel cells and then writes this information to one or more files.
 * 
 * @author Matt
 * @since 7-sep-2006
 */
public class ExcelOutput extends BaseStep implements StepInterface
{
	private ExcelOutputMeta meta;
	private ExcelOutputData data;
	 
	public ExcelOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(ExcelOutputMeta)smi;
		data=(ExcelOutputData)sdi;

		Object[] r;
		boolean result=true;
		r=getRow();       // This also waits for a row to be finished.
		if (first) {
			// get the RowMeta
			data.previousMeta = getInputRowMeta().clone();
			//do not set first=false, below is another part that uses first
		}
		
		if ( ( r==null && data.headerrow!=null && meta.isFooterEnabled() ) ||
		     ( r!=null && linesOutput>0 && meta.getSplitEvery()>0 && ((linesOutput+1)%meta.getSplitEvery())==0)
		   )
		{
			if (data.headerrow!=null) 
			{
			   if ( meta.isFooterEnabled() )
			   {
			      writeHeader();
			   }
			}
			
			// Not finished: open another file...
			if (r!=null)
			{
				closeFile();
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
		
		putRow(data.previousMeta, r);       // in case we want it to go further...
		
        if (checkFeedback(linesOutput)) logBasic("linenr "+linesOutput);
		
		return result;
	}

	private boolean writeRowToFile(Object[] r)
	{
		Object v;
		
		try
		{	
			if (first)
			{
				first=false;
				if ( meta.isHeaderEnabled() || meta.isFooterEnabled()) // See if we have to write a header-line)
				{
					data.headerrow=data.previousMeta.cloneRow(r); // copy the row for the footer!
					if (meta.isHeaderEnabled() && data.headerrow!=null)
					{
						if (writeHeader() )
                        {
							return false;
                        }
					}
				}
				
				data.fieldnrs=new int[meta.getOutputFields().length];
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					data.fieldnrs[i]=data.previousMeta.indexOfValue(meta.getOutputFields()[i].getName());
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
				for (int i=0;i<data.previousMeta.size();i++)
				{
					v=r[i];
					if(!writeField(v, data.previousMeta.getValueMeta(i), null, i)) return false;
				}
                // go to the next line
                data.positionX = 0;
                data.positionY++;
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					v=r[data.fieldnrs[i]];
					
					if(!writeField(v, data.previousMeta.getValueMeta(data.fieldnrs[i]), meta.getOutputFields()[i], i)) return false;
				}

                // go to the next line
                data.positionX = 0;
                data.positionY++;
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

    /**
     * Write a value to Excel, increasing data.positionX with one afterwards.
     * @param v The value to write
     * @param vMeta The valueMeta to write
     * @param excelField the field information (if any, otherwise : null)
     * @param column the excel column for getting the template format
     * @return
     */
    private boolean writeField(Object v, ValueMetaInterface vMeta, ExcelField excelField, int column)
    {
        return writeField(v, vMeta, excelField, column, false);
    }

    /**
     * Write a value to Excel, increasing data.positionX with one afterwards.
     * @param v The value to write
     * @param vMeta The valueMeta to write
     * @param excelField the field information (if any, otherwise : null)
     * @param column the excel column for getting the template format
     * @param isHeader true if this is part of the header/footer
     * @return
     */
	private boolean writeField(Object v, ValueMetaInterface vMeta, ExcelField excelField, int column, boolean isHeader)
	{
		try
		{
            String hashName = vMeta.getName();
            if (isHeader) hashName = "____header_field____"; // all strings, can map to the same format.
            
            WritableCellFormat cellFormat=(WritableCellFormat) data.formats.get(hashName);

            // when template is used, take over the column format
            if (cellFormat==null && meta.isTemplateEnabled() && !isHeader)
            {
            	try {
            		if (column<data.templateColumns)
            		{
            			cellFormat=new WritableCellFormat(data.sheet.getColumnView(column).getFormat());
            			data.formats.put(hashName, cellFormat); // save for next time around...
            		}
				} catch (RuntimeException e) {
					//ignore if the column is not found, format as usual
				}
            }
            
            switch(vMeta.getType())
            {
            case ValueMetaInterface.TYPE_DATE:
                {
                    if (v!=null && vMeta.getDate(v)!=null)
                    {
                        if (cellFormat==null)
                        {
                            if (excelField!=null && excelField.getFormat()!=null)
                            {
                                DateFormat dateFormat = new DateFormat(excelField.getFormat());
                                cellFormat=new WritableCellFormat(dateFormat);
                            }
                            else
                            {
                                cellFormat =  new WritableCellFormat(DateFormats.FORMAT9);
                            }
                            data.formats.put(hashName, cellFormat); // save for next time around...
                        }
                        DateTime dateTime = new DateTime(data.positionX, data.positionY, vMeta.getDate(v), cellFormat);
                        data.sheet.addCell(dateTime);
                    }
                    else
                    {
                        data.sheet.addCell(new Label(data.positionX, data.positionY, ""));
                    }
                }
                break;
            case ValueMetaInterface.TYPE_STRING:
            case ValueMetaInterface.TYPE_BOOLEAN:
            case ValueMetaInterface.TYPE_BINARY:
                {
                    if (v!=null)
                    {
                        if (cellFormat==null)
                        {
                            cellFormat = new WritableCellFormat(data.writableFont);
                            data.formats.put(hashName, cellFormat);
                        }
                        Label label = new Label(data.positionX, data.positionY, vMeta.getString(v), cellFormat);
                        data.sheet.addCell(label);
                    }
                    else
                    {
                        data.sheet.addCell(new Label(data.positionX, data.positionY, ""));
                    }
                }
                break;
            case ValueMetaInterface.TYPE_NUMBER:
            case ValueMetaInterface.TYPE_BIGNUMBER:
            case ValueMetaInterface.TYPE_INTEGER:
                {
	                if (v!=null)
	                {
		            	if (cellFormat==null)
	                    {
	                        String format;
	                        if (excelField!=null && excelField.getFormat()!=null)
	                        {
	                            format=excelField.getFormat();
	                        }
	                        else
	                        {
	                            format = "###,###.00";
	                        }
	                        NumberFormat numberFormat = new NumberFormat(format);
	                        cellFormat = new WritableCellFormat(numberFormat);
	                        data.formats.put(vMeta.getName(), cellFormat); // save for next time around...
	                    }
	                    jxl.write.Number number = new jxl.write.Number(data.positionX, data.positionY, vMeta.getNumber(v), cellFormat);
	                    data.sheet.addCell(number);
	                }
	                else
	                {
	                    data.sheet.addCell(new Label(data.positionX, data.positionY, ""));
	                }
                }
                break;
            default: break;
            }
		}
		catch(Exception e)
		{
			logError("Error writing field ("+data.positionX+","+data.positionY+") : "+e.toString());
            logError(Const.getStackTracker(e));
			return false;
		}
        finally
        {
            data.positionX++; // always advance :-)
        }
		return true;
	}
		
	private boolean writeHeader()
	{
        boolean retval=false;
		Object[] r=data.headerrow;
		
		try
		{
			// If we have fields specified: list them in this order!
			if (meta.getOutputFields()!=null && meta.getOutputFields().length>0)
			{
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
                    String fieldName = meta.getOutputFields()[i].getName();
                    ValueMetaInterface vMeta=new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);
                    writeField(fieldName, vMeta, null, i, true);
				}
			}
			else
			if (r!=null)  // Just put all field names in the header/footer
			{
				for (int i=0;i<data.previousMeta.size();i++)
				{
					String fieldName = data.previousMeta.getFieldNames()[i];
					ValueMetaInterface vMeta=new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);
                    writeField(fieldName, vMeta, null, i, true);
				}
			}
		}
		catch(Exception e)
		{
			logError("Error writing header line: "+e.toString());
			logError(Const.getStackTracker(e));
			retval=true;
		}
        finally
        {
            data.positionX=0;
            data.positionY++;
        }
		linesOutput++;
		return retval;
	}

	public String buildFilename()
	{
		return meta.buildFilename(this, getCopy(), data.splitnr);
	}
	
	public boolean openNewFile()
	{
		boolean retval=false;
		
		try
		{
			WorkbookSettings ws = new WorkbookSettings();
            ws.setLocale(Locale.getDefault());
            
            if (!Const.isEmpty(meta.getEncoding()))
            {
                ws.setEncoding(meta.getEncoding());
            }
            
            FileObject file = KettleVFS.getFileObject(buildFilename());

		
			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname());
			resultFile.setComment("This file was created with an Excel output step by Pentaho Data Integration");
            addResultFile(resultFile);

            // Create the workboook
            if (!meta.isTemplateEnabled())
            {				
            	if (file.exists())
            	{
            		// Attempts to load it from the local file failed in the past.
            		// As such we will try to remove the file first...
            		//
            		file.delete();
            	}
            	
				// Create a new Workbook
            	data.outputStream = file.getContent().getOutputStream();
				data.workbook = Workbook.createWorkbook(data.outputStream, ws);

				// Create a sheet?
				String sheetname = "Sheet1";
            	data.sheet = data.workbook.getSheet(sheetname);
            	if (data.sheet==null)
            	{
            		data.sheet = data.workbook.createSheet(sheetname, 0);
            	}

            } else {

            	FileObject fo = KettleVFS.getFileObject(environmentSubstitute(meta.getTemplateFileName()));
				// create the openFile from the template

				Workbook tmpWorkbook=Workbook.getWorkbook(fo.getContent().getInputStream(), ws);
				data.workbook = Workbook.createWorkbook(file.getContent().getOutputStream(), tmpWorkbook);
				
            	tmpWorkbook.close();
            	// use only the first sheet as template
            	data.sheet = data.workbook.getSheet(0);
            	// save inital number of columns
            	data.templateColumns = data.sheet.getColumns();
            }
			
            // Rename Sheet
			if (!Const.isEmpty(environmentSubstitute(meta.getSheetname()))) 
			{
				data.sheet.setName(environmentSubstitute(meta.getSheetname())); 
			}

			if (meta.isSheetProtected())
			{
				// Protect Sheet by setting password
				data.sheet.getSettings().setProtected(true); 
				data.sheet.getSettings().setPassword(environmentSubstitute(meta.getPassword()));
			}
            

            // Set the initial position...
            
            data.positionX = 0;
            if (meta.isTemplateEnabled() && meta.isTemplateAppend())
            {
            	data.positionY = data.sheet.getRows();
            } else {
            	data.positionY = 0;
            }

			retval=true;
		}
		catch(Exception e)
		{
			logError("Error opening new file", e);
			setErrors(1);
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
			if ( data.workbook != null )
			{
			    data.workbook.write();
                data.workbook.close();
                data.outputStream.close();
                data.outputStream=null;
                data.workbook = null;
			}
            data.formats.clear();

            // Explicitly call garbage collect to have file handle
            // released. Bug tracker: PDI-48
			System.gc();
            
			retval=true;
		}
		catch(Exception e)
		{
            logError("Unable to close openFile file", e);
			setErrors(1);
		}

		return retval;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ExcelOutputMeta)smi;
		data=(ExcelOutputData)sdi;

		if (super.init(smi, sdi))
		{
			data.splitnr=0;
            
            try
            {
                // Create the default font TODO: allow to change this later on.
                data.writableFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
            }
            catch(Exception we)
            {
                logError("Unexpected error preparing to write to Excel file : "+we.toString());
                logError(Const.getStackTracker(we));
                return false;
            }
            
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
		meta=(ExcelOutputMeta)smi;
		data=(ExcelOutputData)sdi;
		
		closeFile();
        
        super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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