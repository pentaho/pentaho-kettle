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
 
package be.ibridge.kettle.trans.step.excelinput;

import java.io.File;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
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
 * This class reads data from one or more Microsoft Excel files.
 *  
 * @author Matt
 * @since 19-NOV-2003
 *
 */
public class ExcelInput extends BaseStep implements StepInterface
{
	private ExcelInputMeta meta;
	private ExcelInputData data;
	
	public ExcelInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private Row fillRow(Row baserow, Sheet sheet, Cell line[], int startcolumn, int rownr)
	{
        debug = "fillRow start";
		Row r = new Row(baserow);
		
		// Set values in the row...
		for (int i=startcolumn;i<line.length && i-startcolumn<r.size();i++)
		{
            debug = "get cell #"+i;
			Cell cell = line[i];

            int rowcolumn=i-startcolumn;
            debug = "Rowcolumn = "+rowcolumn;

            Value v = r.getValue(rowcolumn);
            debug = "Value v = "+v;

			if (cell.getType().equals(CellType.BOOLEAN))
			{
				v.setValue( ((BooleanCell)cell).getValue() );
			}
			else
			if (cell.getType().equals(CellType.DATE))
			{
				v.setValue( ((DateCell)cell).getDate() );
			}
			else
			if (cell.getType().equals(CellType.LABEL))
			{
				v.setValue( ((LabelCell)cell).getString() );
                switch(meta.getFieldTrimType()[rowcolumn])
                {
                case ExcelInputMeta.TYPE_TRIM_LEFT: v.ltrim(); break;
                case ExcelInputMeta.TYPE_TRIM_RIGHT: v.rtrim(); break;
                case ExcelInputMeta.TYPE_TRIM_BOTH: v.trim(); break;
                default: break;
                }
			}
			else
			if (cell.getType().equals(CellType.NUMBER))
			{
				v.setValue( ((NumberCell)cell).getValue() );
			}
			else
			{
				logDetailed("Unknown type : "+cell.getType().toString()+" : ["+cell.getContents()+"]");
				v.setNull();
			}
			
			// Change to the appropriate type...
			// 
			v.setType(meta.getFieldType()[rowcolumn]);
			v.setLength(meta.getFieldLength()[rowcolumn], meta.getFieldPrecision()[rowcolumn]);
		}
		
        debug = "filename";

		// Do we need to include the filename?
		if (meta.getFileField()!=null && meta.getFileField().length()>0)
		{
			Value value = new Value(meta.getFileField(), data.files[data.filenr]);
			value.setLength(data.maxfilelength);
			r.addValue(value);
		}

        debug = "sheetname";

		// Do we need to include the sheetname?
		if (meta.getSheetField()!=null && meta.getSheetField().length()>0)
		{
			Value value = new Value(meta.getSheetField(), sheet.getName());
			value.setLength(data.maxsheetlength);
			r.addValue(value);
		}

        debug = "rownumber";

		// Do we need to include the rownumber?
		if (meta.getRowNumberField()!=null && meta.getRowNumberField().length()>0)
		{
			Value value = new Value(meta.getRowNumberField(), linesWritten+1);
			r.addValue(value);
		}

        debug = "end of fillRow";

		return r;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(ExcelInputMeta)smi;
		data=(ExcelInputData)sdi;

		// See if we're not done processing...
		// We are done processing if the filenr >= number of files.
		if (data.filenr >= data.files.length)
		{
            logDetailed("No more files to be processes! ("+data.filenr+" files done)");
			setOutputDone(); // signal end to receiver(s)
			return false; // end of data or error.
		}

		Row r = getRowFromWorkbooks();
		if (r!=null)
		{
            if (!r.isIgnored())
            {
                // OK, see if we need to repeat values.
                if (data.previousRow!=null)
                {
                    for (int i=0;i<meta.getFieldRepeat().length;i++)
                    {
                        Value field = r.getValue(i);
                        if (field.isNull() && meta.getFieldRepeat()[i])
                        {
                            // Take the value from the previous row.
                            Value repeat = data.previousRow.getValue(i);
                            field.setValue(repeat);
                        }
                    }
                }
                
                // Remember this row for the next time around!
                data.previousRow = r;
                
                // Send out the good news: we found a row of data!
    			putRow(r);
            }
			return true;
		}
		else
		{
			return false;
		}
	}

	public Row getRowFromWorkbooks() throws KettleException
	{
		debug="processRow()";
		// This procedure outputs a single Excel data row on the destination rowsets...

		Row retval=new Row();
		retval.setIgnore();

		try
		{
			// First, see if a file has been opened?
			if (data.workbook == null)
			{
				// Open a new workbook..
				debug="open workbook #"+data.filenr+" : "+data.files[data.filenr];
				logDetailed("Opening workbook #"+data.filenr+" : "+data.files[data.filenr]);
				data.workbook = Workbook.getWorkbook(new File(data.files[data.filenr]));
                
                // Start at the first sheet again...
                data.sheetnr = 0;
                
			}
			
            boolean nextsheet=false; 
            
			// What sheet were we handling?
			debug="Get sheet #"+data.filenr+"."+data.sheetnr;
			logDetailed(debug);
			Sheet sheet = data.workbook.getSheet(meta.getSheetName()[data.sheetnr] );
			if (sheet!=null)
            {
    			// at what row do we continue reading?
    			if (data.rownr<0)
    			{
    				data.rownr = meta.getStartRow()[data.sheetnr];
                    
                    // Add an extra row if we have a header row to skip...
                    if (meta.startsWithHeader())
                    {
                        data.rownr++;
                    }
                    
    				debug="startrow = "+data.rownr;
    			}
    			// Start at the specified column
    			data.colnr = meta.getStartColumn()[data.sheetnr];
    			debug="startcol = "+data.colnr;
    
    			// Build a new row and fill in the data from the sheet...
    			try
    			{
    				debug="Get line #"+data.rownr+" from sheet #"+data.filenr+"."+data.sheetnr;
    				logRowlevel(debug);
    				Cell line[] = sheet.getRow(data.rownr);
    
                    logRowlevel("Read line with "+line.length+" cells");
    				Row r = fillRow(data.row, sheet, line, data.colnr, data.rownr);
                    logRowlevel("Converted line to row #"+data.rownr+" : "+r);
    				data.rownr++;
    	
    				if (line.length>0 || !meta.ignoreEmptyRows())
    				{
    					// Put the row 
    					retval=r;
    				}
    				
    				if (line.length==0 && meta.stopOnEmpty())
    				{
    					nextsheet=true;
    				}
    			}
    			catch(ArrayIndexOutOfBoundsException e)
    			{
                    logRowlevel("Out of index error: move to next sheet! ("+debug+")");
    				// We tried to read below the last line in the sheet.
    				// Go to the next sheet...
    				nextsheet=true;
    			}
            }
            else
            {
                nextsheet=true;
            }

			if (nextsheet)
			{
				// Go to the next sheet
				data.sheetnr++;
				
				// Reset the start-row:
				data.rownr = -1;
				
				// no previous row yet, don't take it from the previous sheet! (that whould be plain wrong!)
                data.previousRow = null; 

				// Perhaps it was the last sheet?
				if (data.sheetnr >= meta.getSheetName().length)
				{
					// Close the workbook!
					data.workbook.close();
					data.workbook = null; // marker to open again.
					
					// advance to the next file!
					data.filenr++;
				}
			}
		}
		catch(Exception e)
		{
			logError("Error processing row in ["+debug+"] from Excel file ["+data.files[data.filenr]+"] : "+e.toString());
			setErrors(1);
			stopAll();
			return null;
		}
		
		return retval;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ExcelInputMeta)smi;
		data=(ExcelInputData)sdi;

		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles();
			if (data.files!=null && data.files.length>0)
			{
				data.row = meta.getEmptyFields();
				if (data.row.size()>0)
				{
					// Determine the maximum filename length...
					data.maxfilelength = -1;
					for (int i=0;i<data.files.length;i++) if (data.files[i].length()>data.maxfilelength) data.maxfilelength=data.files[i].length();
				
					// Determine the maximum sheetname length...
					data.maxsheetlength = -1;
					for (int i=0;i<meta.getSheetName().length;i++) if (meta.getSheetName()[i].length()>data.maxsheetlength) data.maxsheetlength=meta.getSheetName()[i].length();

					return true;
				}
				else
				{
					logError("No input fields defined!");
				}
			}
			else
			{
				logError("No file specified! Stop processing.");
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (ExcelInputMeta)smi;
	    data = (ExcelInputData)sdi;
	    
		if (data.workbook!=null) data.workbook.close();
		
		super.dispose(smi, sdi);
	}

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
