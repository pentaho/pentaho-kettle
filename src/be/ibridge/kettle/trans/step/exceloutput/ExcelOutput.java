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
 

package be.ibridge.kettle.trans.step.exceloutput;

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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
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

		Row r;
		boolean result=true;
		r=getRow();       // This also waits for a row to be finished.
		
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
				if ( meta.isHeaderEnabled() || meta.isFooterEnabled()) // See if we have to write a header-line)
				{
					data.headerrow=new Row(r); // copy the row for the footer!
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
					v=r.getValue(i);
					if(!writeField(v, null, i)) return false;
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
					v=r.getValue(data.fieldnrs[i]);
					
					if(!writeField(v, meta.getOutputFields()[i], i)) return false;
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
     * @param excelField the field information (if any, otherwise : null)
     * @param column the excel column for getting the template format
     * @return
     */
    private boolean writeField(Value v, ExcelField excelField, int column)
    {
        return writeField(v, excelField, column, false);
    }

    /**
     * Write a value to Excel, increasing data.positionX with one afterwards.
     * @param v The value to write
     * @param excelField the field information (if any, otherwise : null)
     * @param column the excel column for getting the template format
     * @param isHeader true if this is part of the header/footer
     * @return
     */
	private boolean writeField(Value v, ExcelField excelField, int column, boolean isHeader)
	{
		try
		{
            String hashName = v.getName();
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
            
            switch(v.getType())
            {
            case Value.VALUE_TYPE_DATE:
                {
                    if (!v.isNull() && v.getDate()!=null)
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
                        DateTime dateTime = new DateTime(data.positionX, data.positionY, v.getDate(), cellFormat);
                        data.sheet.addCell(dateTime);
                    }
                    else
                    {
                        data.sheet.addCell(new Label(data.positionX, data.positionY, ""));
                    }
                }
                break;
            case Value.VALUE_TYPE_STRING:
            case Value.VALUE_TYPE_BOOLEAN:
            case Value.VALUE_TYPE_BINARY:
                {
                    if (!v.isNull())
                    {
                        if (cellFormat==null)
                        {
                            cellFormat = new WritableCellFormat(data.writableFont);
                            data.formats.put(hashName, cellFormat);
                        }
                        Label label = new Label(data.positionX, data.positionY, v.getString(), cellFormat);
                        data.sheet.addCell(label);
                    }
                    else
                    {
                        data.sheet.addCell(new Label(data.positionX, data.positionY, ""));
                    }
                }
                break;
            case Value.VALUE_TYPE_NUMBER:
            case Value.VALUE_TYPE_BIGNUMBER:
            case Value.VALUE_TYPE_INTEGER:
                {
	                if (!v.isNull())
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
	                        data.formats.put(v.getName(), cellFormat); // save for next time around...
	                    }
	                    jxl.write.Number number = new jxl.write.Number(data.positionX, data.positionY, v.getNumber(), cellFormat);
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
		Row r=data.headerrow;
		
		try
		{
			// If we have fields specified: list them in this order!
			if (meta.getOutputFields()!=null && meta.getOutputFields().length>0)
			{
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
                    String fieldName = meta.getOutputFields()[i].getName();

                    Value headerValue = new Value(fieldName, fieldName);
                    writeField(headerValue, null, i, true);
				}
			}
			else
			if (r!=null)  // Just put all field names in the header/footer
			{
				for (int i=0;i<r.size();i++)
				{
					String fieldName = r.getValue(i).getName();

                    Value headerValue = new Value(fieldName, fieldName);
                    writeField(headerValue, null, i, true);
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
		return meta.buildFilename(getCopy(), data.splitnr);
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
			resultFile.setComment("This file was created with an Excel output step");
            addResultFile(resultFile);

            // Create the workboook
            if (!meta.isTemplateEnabled())
            {
				
				// Create a new Workbook
				data.workbook = Workbook.createWorkbook(file.getContent().getOutputStream(), ws);

				// Create a sheet?
				data.sheet = data.workbook.createSheet("Sheet1", 0);    

            } else {
            	FileObject fo = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getTemplateFileName()));
				// create the openFile from the template

				Workbook tmpWorkbook=Workbook.getWorkbook(
						                  fo.getContent().getInputStream(), ws);

				data.workbook = Workbook.createWorkbook(file.getContent().getOutputStream(), tmpWorkbook);
				
            	tmpWorkbook.close();
            	// use only the first sheet as template
            	data.sheet = data.workbook.getSheet(0);
            	// save inital number of columns
            	data.templateColumns = data.sheet.getColumns();
            }
			
            // Renamme Sheet
			if (!Const.isEmpty(meta.getSheetname())) 
			{
				data.sheet.setName(meta.getSheetname()); 
			}

			if (meta.isSheetProtected())
			{
				// Protect Sheet by setting password
				data.sheet.getSettings().setProtected(true); 
				data.sheet.getSettings().setPassword(meta.getPassword());
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
			data.workbook.write();
                        data.workbook.close();
                        data.formats.clear();

                        // Fix for PDI-48: call gc to release file handle.
                        System.gc();

            
			retval=true;
		}
		catch(Exception e)
		{
            logError("Unable to close workbook file : "+e.toString());
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
