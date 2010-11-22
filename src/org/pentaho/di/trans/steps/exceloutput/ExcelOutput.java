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

import java.io.File;
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
import org.pentaho.di.i18n.BaseMessages;
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
	private static Class<?> PKG = ExcelOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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

		Object[] r=getRow();       // This also waits for a row to be finished.
		if (first && r != null) {
			// get the RowMeta, rowMeta is only set when a row is read
			data.previousMeta = getInputRowMeta().clone();
			//do not set first=false, below is another part that uses first
			
			if (meta.isAutoSizeColums())
			{
				if(meta.getOutputFields()!=null && meta.getOutputFields().length>0)
					data.fieldsWidth = new int[meta.getOutputFields().length];
				else
					data.fieldsWidth = new int[data.previousMeta.size()];
			}
			
			
			if(meta.isDoNotOpenNewFileInit())
			{
				data.oneFileOpened=true;
			
		         if (!openNewFile())
				 {
					 logError("Couldn't open file "+buildFilename());
					 return false;
				 }
		          // If we need to write a header, do so...
		          //
		         if(meta.isHeaderEnabled() && !data.headerWrote)
				 {
		        	 writeHeader();
		        	 data.headerWrote=true;
				 }
			}else
			{
				  // If we need to write a header, do so...
		          //
		         if(meta.isHeaderEnabled() && !data.headerWrote)
				 {
		        	 writeHeader();
		        	 data.headerWrote=true;
				 }
			}
		}
		
		// If we split the data stream in small XLS files, we need to do this here...
		//
		if ( r!=null && getLinesOutput()>0 && meta.getSplitEvery()>0 && ((getLinesOutput()+1)%meta.getSplitEvery())==0)
		{
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
		          // If we need to write a header, do so...
		          //
		         if(meta.isHeaderEnabled() && !data.headerWrote)
				 {
		        	 writeHeader();
		        	 data.headerWrote=true;
				 }
			}
		}
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		boolean result=writeRowToFile(r);
		if (!result)
		{
			setErrors(1);
			stopAll();
			return false;
		}
		
		putRow(data.previousMeta, r);       // in case we want it to go further...
		
        if (checkFeedback(getLinesOutput())) 
        {
        	if(log.isBasic()) logBasic("linenr "+getLinesOutput());
        }
		
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

		incrementLinesOutput();
		
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

		WritableFont writableFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
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
            if(meta.isAutoSizeColums())
            {
	            // prepare auto size colums
	            int vlen=vMeta.getName().length();
	            if(!isHeader && v!=null) vlen=v.toString().trim().length();
	        	if(vlen>0 && vlen>data.fieldsWidth[column]) data.fieldsWidth[column]=vlen+1;
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
                    else if (!meta.isNullBlank())
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
                            cellFormat = new WritableCellFormat(writableFont);
                            data.formats.put(hashName, cellFormat);
                        }
                        Label label = new Label(data.positionX, data.positionY, vMeta.getString(v), cellFormat);
                        data.sheet.addCell(label);
                    }
                    else if (!meta.isNullBlank())
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
	                else if (!meta.isNullBlank())
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
			if (data.previousMeta!=null)  // Just put all field names in the header/footer
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
        incrementLinesOutput();
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

            if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "ExcelOutput.Log.OpeningFile", data.file.toString()));
            
            // Create the workbook
            if (!meta.isTemplateEnabled())
            {				
            	/*if (file.exists())
            	{
            		// Attempts to load it from the local file failed in the past.
            		// As such we will try to remove the file first...
            		//
            		file.delete();
            	}*/

            	File fle = new File(KettleVFS.getFilename(data.file));
               	if(meta.isAppend() && fle.exists())
            	{ 
            		Workbook workbook = Workbook.getWorkbook(fle);
                    data.workbook =Workbook.createWorkbook(fle,workbook);
                    if(workbook!=null) workbook.close();
                    
                    if(data.workbook.getSheet(data.realSheetname)!=null) 
                    {    
	            		// get available sheets
	            		String listSheets[]=data.workbook.getSheetNames();
	            
	            		// Let's see if this sheet already exist...
	            		for (int i=0;i<listSheets.length;i++) 
	            		{
	            			if(listSheets[i].equals(data.realSheetname))
	            			{
	                			// let's remove sheet
	                			data.workbook.removeSheet(i);
	            			}	
	            		}
                    }
                	// and now .. we create the sheet
                	data.sheet = data.workbook.createSheet(data.realSheetname,data.workbook.getNumberOfSheets());
            	}else{
            		// Create a new Workbook
    				data.outputStream = KettleVFS.getOutputStream(data.file, false);
					data.workbook = Workbook.createWorkbook(data.outputStream, ws);
    				
    				// Create a sheet?
    				String sheetname = "Sheet1";
                	data.sheet = data.workbook.getSheet(sheetname);
                	if (data.sheet==null)
                	{
                		data.sheet = data.workbook.createSheet(sheetname, 0);
                	} 
            	}
            } else {

            	FileObject fo = KettleVFS.getFileObject(environmentSubstitute(meta.getTemplateFileName()), getTransMeta());
				// create the openFile from the template

				Workbook tmpWorkbook=Workbook.getWorkbook(KettleVFS.getInputStream(fo), ws);
				data.outputStream = KettleVFS.getOutputStream(data.file,false);
				data.workbook = Workbook.createWorkbook(data.outputStream, tmpWorkbook);
				
            	tmpWorkbook.close();
            	fo.close();
            	// use only the first sheet as template
            	data.sheet = data.workbook.getSheet(0);
            	// save initial number of columns
            	data.templateColumns = data.sheet.getColumns();
            }
			
            // Rename Sheet
			if (!Const.isEmpty(data.realSheetname)) 
			{
				data.sheet.setName(data.realSheetname); 
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
            
            // If we need to write a header, do so...
            //
           /* if (meta.isHeaderEnabled()) {
            	writeHeader();
            }*/
            data.headerWrote=false;
            
            if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "ExcelOutput.Log.FileOpened", data.file.toString()));
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
		String filename=null;
		try
		{
			if (meta.isFooterEnabled())
			{
				writeHeader();
			}

			if ( data.workbook != null )
			{
				if(data.fieldsWidth!=null)
				{
					if(meta.isAutoSizeColums())
					{
						// auto resize columns
						int nrfields=data.fieldsWidth.length;
						for(int i=0;i<nrfields;i++)
						{
							data.sheet.setColumnView(i,data.fieldsWidth[i]);
						}
					}
					data.fieldsWidth=null;
				}
			    data.workbook.write();
                data.workbook.close();
                data.workbook = null;
                if(data.outputStream!=null) 
                {	
                	data.outputStream.close();
                	data.outputStream=null;
                }
                
                if (data.sheet!=null) {
                	data.sheet = null;
                }
                if(data.file!=null)
                {
                	filename=data.file.toString();
                	data.file.close();
                	data.file=null;
                }
               
			}
            //data.formats.clear();
			if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "ExcelOutput.Log.FileClosed",filename));
            
			retval=true;
		}
		catch(Exception e)
		{
            logError("Unable to close openFile file : " + data.file.toString(), e);
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
			data.realSheetname=environmentSubstitute(meta.getSheetname());
			
	        // See if we need to add the filename to the result.
            // If the file doesn't exist we report the problem.
            //
            if (!addFilenameToResult()) {
              return false;
            }
            
			if(!meta.isDoNotOpenNewFileInit())
			{
				data.oneFileOpened=true;
				
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
			}else
			{
				return true;
			}
		}
		return false;
	}
		   
    private boolean addFilenameToResult() {
      try {
        String buildFilename=buildFilename();
        data.file = KettleVFS.getFileObject(buildFilename, getTransMeta());

        if(meta.isAddToResultFiles())
        {
            // Add this to the result file names...
            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
            resultFile.setComment("This file was created with an Excel output step by Pentaho Data Integration");
            addResultFile(resultFile);
        }
        
        return true;

      } catch(Exception e) {
        log.logError("Unable to add filename to the result", e);
        return false;
      }
    }

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ExcelOutputMeta)smi;
		data=(ExcelOutputData)sdi;

		if(data.oneFileOpened) closeFile();
        if(data.file!=null)
        {
        	try{
        		data.file.close();
        		data.file=null;
        	}catch(Exception e){}
        }
        super.dispose(smi, sdi);
	}
	
}