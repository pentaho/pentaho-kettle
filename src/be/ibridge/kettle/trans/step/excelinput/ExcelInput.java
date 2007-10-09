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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.errorhandling.CompositeFileErrorHandler;
import be.ibridge.kettle.trans.step.errorhandling.FileErrorHandlerContentLineNumber;
import be.ibridge.kettle.trans.step.errorhandling.FileErrorHandlerMissingFiles;
import be.ibridge.kettle.trans.step.fileinput.FileInputList;
import be.ibridge.kettle.trans.step.playlist.FilePlayListAll;
import be.ibridge.kettle.trans.step.playlist.FilePlayListReplay;

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

	private Row fillRow(Row baserow, int startcolumn, ExcelInputRow excelInputRow) throws KettleException
	{
		Row r = new Row(baserow);

		// Keep track whether or not we handled an error for this line yet.
		boolean errorHandled = false;

		// Set values in the row...
		for (int i = startcolumn; i < excelInputRow.cells.length && i - startcolumn < r.size(); i++)
		{
			Cell cell = excelInputRow.cells[i];

			int rowcolumn = i - startcolumn;

			Value v = r.getValue(rowcolumn);

			try
			{
				checkType(cell, v);
			}
			catch (KettleException ex)
			{
				if (!meta.isErrorIgnored()) throw ex;
				logBasic("Warning processing [" + v + "] from Excel file [" + data.filename + "] : " + ex.getMessage());
				if (!errorHandled)
				{
					data.errorHandler.handleLineError(excelInputRow.rownr, excelInputRow.sheetName);
					errorHandled = true;
				}
				
				if (meta.isErrorLineSkipped())
				{
					r.setIgnore();
					return r;
				}
			}

			CellType cellType = cell.getType();
			if (CellType.BOOLEAN.equals(cellType) ||
				CellType.BOOLEAN_FORMULA.equals(cellType))
			{
				v.setValue(((BooleanCell) cell).getValue());
			}
			else
			{
				if (CellType.DATE.equals(cellType) ||
					CellType.DATE_FORMULA.equals(cellType) )
				{
					Date date = ((DateCell) cell).getDate();
					long time = date.getTime();
					int offset = TimeZone.getDefault().getOffset(time);
					v.setValue(new Date(time - offset));
				}
				else
				{
					if (CellType.LABEL.equals(cellType) ||
					    CellType.STRING_FORMULA.equals(cellType))
					{
						v.setValue(((LabelCell) cell).getString());
						switch (meta.getField()[rowcolumn].getTrimType())
						{
						case ExcelInputMeta.TYPE_TRIM_LEFT:
							v.ltrim();
							break;
						case ExcelInputMeta.TYPE_TRIM_RIGHT:
							v.rtrim();
							break;
						case ExcelInputMeta.TYPE_TRIM_BOTH:
							v.trim();
							break;
						default:
							break;
						}
					}
					else
					{
						if (CellType.NUMBER.equals(cellType) ||
						    CellType.NUMBER_FORMULA.equals(cellType))
						{
							v.setValue(((NumberCell) cell).getValue());
						}
						else
						{
							if (log.isDetailed()) logDetailed("Unknown type : " + cell.getType().toString() + " : [" + cell.getContents() + "]");
							v.setNull();
						}
					}
				}
			}

			ExcelInputField field = meta.getField()[rowcolumn];

			// Change to the appropriate type if needed...
			//
			try
			{
				if (v.getType() != field.getType())
				{
					switch (v.getType())
					{
					// Use case: we find a String: convert it using the supplied format to the desired type...
					//
					case Value.VALUE_TYPE_STRING:
						switch (field.getType())
						{
						case Value.VALUE_TYPE_DATE:
							v.str2dat(field.getFormat());
							break;
						case Value.VALUE_TYPE_NUMBER:
							v.str2num(field.getFormat(), field.getDecimalSymbol(), field.getGroupSymbol(), field.getCurrencySymbol());
							break;
						default:
							v.setType(field.getType());
							break;
						}
						break;

					// Use case: we find a numeric value: convert it using the supplied format to the desired data type...
					//
					case Value.VALUE_TYPE_NUMBER:
					case Value.VALUE_TYPE_INTEGER:
						switch (field.getType())
						{
						case Value.VALUE_TYPE_STRING:
							v.num2str(field.getFormat(), field.getDecimalSymbol(), field.getGroupSymbol(), field.getCurrencySymbol());
							break;
						case Value.VALUE_TYPE_DATE:
							v.num2str("#").str2dat(field.getFormat());
							break;
						default:
							v.setType(field.getType());
							break;
						}
						break;
					// Use case: we find a date: convert it using the supplied format to String...
					//
					case Value.VALUE_TYPE_DATE:
						switch (field.getType())
						{
						case Value.VALUE_TYPE_STRING:
							v.dat2str(field.getFormat());
							break;
						default:
							v.setType(field.getType());
							break;
						}
						break;
					default:
						v.setType(field.getType());
					}
				}
			}
			catch (KettleException ex)
			{
				if (!meta.isErrorIgnored()) throw ex;
				logBasic("Warning processing [" + v + "] from Excel file [" + data.filename + "] : " + ex.toString());
				if (!errorHandled) // check if we didn't log an error already for this one.
				{
					data.errorHandler.handleLineError(excelInputRow.rownr, excelInputRow.sheetName);
					errorHandled=true;
				}

				if (meta.isErrorLineSkipped())
				{
					r.setIgnore();
					return r;
				}
				else
				{
					v.setNull();
				}
			}

			// Set the meta-data of the field: length and precision
			//
			v.setLength(meta.getField()[rowcolumn].getLength(), meta.getField()[rowcolumn].getPrecision());
		}

		// Do we need to include the filename?
		if (meta.getFileField() != null && meta.getFileField().length() > 0)
		{
			Value value = new Value(meta.getFileField(), data.filename);
			value.setLength(data.maxfilelength);
			r.addValue(value);
		}

		// Do we need to include the sheetname?
		if (meta.getSheetField() != null && meta.getSheetField().length() > 0)
		{
			Value value = new Value(meta.getSheetField(), excelInputRow.sheetName);
			value.setLength(data.maxsheetlength);
			r.addValue(value);
		}

		// Do we need to include the sheet rownumber?
		if (meta.getSheetRowNumberField() != null && meta.getSheetRowNumberField().length() > 0)
		{
			Value value = new Value(meta.getSheetRowNumberField(), Value.VALUE_TYPE_INTEGER);
			value.setValue(data.rownr);
			r.addValue(value);
		}
		
		// Do we need to include the rownumber?
		if (meta.getRowNumberField() != null && meta.getRowNumberField().length() > 0)
		{
			Value value = new Value(meta.getRowNumberField(), Value.VALUE_TYPE_INTEGER);
			value.setValue(linesWritten + 1);
			r.addValue(value);
		}

		return r;
	}

	private void checkType(Cell cell, Value v) throws KettleException
	{
		if (!meta.isStrictTypes()) return;
		CellType cellType = cell.getType();
		if (cellType.equals(CellType.BOOLEAN))
		{
			if (!(v.getType() == Value.VALUE_TYPE_STRING || v.getType() == Value.VALUE_TYPE_NONE || v.getType() == Value.VALUE_TYPE_BOOLEAN))
				throw new KettleException("Invalid type Boolean, expected " + v.getTypeDesc());
		}
		else if (cellType.equals(CellType.DATE))
		{
			if (!(v.getType() == Value.VALUE_TYPE_STRING || v.getType() == Value.VALUE_TYPE_NONE || v.getType() == Value.VALUE_TYPE_DATE))
				throw new KettleException("Invalid type Date: " + cell.getContents() + ", expected " + v.getTypeDesc());
		}
		else if (cellType.equals(CellType.LABEL))
		{
			if (v.getType() == Value.VALUE_TYPE_BOOLEAN || v.getType() == Value.VALUE_TYPE_DATE || v.getType() == Value.VALUE_TYPE_INTEGER || v.getType() == Value.VALUE_TYPE_NUMBER)
				throw new KettleException("Invalid type Label: " + cell.getContents() + ", expected " + v.getTypeDesc());
		}
		else if (cellType.equals(CellType.EMPTY))
		{
			// ok
		}
		else if (cellType.equals(CellType.NUMBER))
		{
			if (!(v.getType() == Value.VALUE_TYPE_STRING || v.getType() == Value.VALUE_TYPE_NONE || v.getType() == Value.VALUE_TYPE_INTEGER || v.getType() == Value.VALUE_TYPE_BIGNUMBER || v.getType() == Value.VALUE_TYPE_NUMBER))
				throw new KettleException("Invalid type Number: " + cell.getContents() + ", expected " + v.getTypeDesc());
		}
		else
		{
			throw new KettleException("Unsupported type " + cellType + " with value: " + cell.getContents());
		}
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (ExcelInputMeta) smi;
		data = (ExcelInputData) sdi;

		if (first)
		{
			first = false;
            
            if (meta.isAcceptingFilenames())
            {
                // Read the files from the specified input stream...
                data.files.getFiles().clear();
                
                int idx = -1;
                Row fileRow = getRowFrom(meta.getAcceptingStepName());
                while (fileRow!=null)
                {
                    if (idx<0)
                    {
                        idx = fileRow.searchValueIndex(meta.getAcceptingField());
                        if (idx<0)
                        {
                            logError("The filename field ["+meta.getAcceptingField()+"] could not be found in the input rows.");
                            setErrors(1);
                            stopAll();
                            return false;
                        }
                    }
                    Value fileValue = fileRow.getValue(idx);
                    try
                    {
                        data.files.addFile(KettleVFS.getFileObject(fileValue.getString()));
                    }
                    catch(IOException e)
                    {
                        throw new KettleException("Unexpected error creating file object for "+fileValue.getString(), e);
                    }
                    
                    // Grab another row
                    fileRow = getRowFrom(meta.getAcceptingStepName());
                }
                
            }

			handleMissingFiles();
		}

		// See if we're not done processing...
		// We are done processing if the filenr >= number of files.
		if (data.filenr >= data.files.nrOfFiles())
		{
			if (log.isDetailed()) logDetailed("No more files to be processes! (" + data.filenr + " files done)");
			setOutputDone(); // signal end to receiver(s)
			return false; // end of data or error.
		}

		if (meta.getRowLimit() > 0 && data.rownr > meta.getRowLimit())
		{
			// The close of the workbook is in dispose()
			if (log.isDetailed()) logDetailed("Row limit of [" + meta.getRowLimit() + "] reached: stop processing.");
			setOutputDone(); // signal end to receiver(s)
			return false; // end of data or error.
		}

		Row r = getRowFromWorkbooks();
		if (r != null)
		{
			if (!r.isIgnored())
			{
				// OK, see if we need to repeat values.
				if (data.previousRow != null)
				{
					for (int i = 0; i < meta.getField().length; i++)
					{
						Value field = r.getValue(i);
						if (field.isNull() && meta.getField()[i].isRepeated())
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

	private void handleMissingFiles() throws KettleException
	{
		List nonExistantFiles = data.files.getNonExistantFiles();

		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			log.logBasic("Required files", "WARNING: Missing " + message);
			if (meta.isErrorIgnored())
				for (Iterator iter = nonExistantFiles.iterator(); iter.hasNext();)
				{
					data.errorHandler.handleNonExistantFile((FileObject) iter.next());
				}
			else
				throw new KettleException("Following required files are missing: " + message);
		}

		List nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			log.logBasic("Required files", "WARNING: Not accessible " + message);
			if (meta.isErrorIgnored())
				for (Iterator iter = nonAccessibleFiles.iterator(); iter.hasNext();)
				{
					data.errorHandler.handleNonAccessibleFile((FileObject) iter.next());
				}
			else
				throw new KettleException("Following required files are not accessible: " + message);
		}
	}

	public Row getRowFromWorkbooks()
	{
		// This procedure outputs a single Excel data row on the destination
		// rowsets...

		Row retval = new Row();
		retval.setIgnore();

		try
		{
			// First, see if a file has been opened?
			if (data.workbook == null)
			{
				// Open a new workbook..
				data.file = data.files.getFile(data.filenr);
				data.filename = KettleVFS.getFilename( data.file );
				
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), toString());
				resultFile.setComment("File was read by an Excel input step");
				addResultFile(resultFile);
				
				if (log.isDetailed()) logDetailed("Opening workbook #" + data.filenr + " : " + data.filename);
                WorkbookSettings ws = new WorkbookSettings();
                if (!Const.isEmpty(meta.getEncoding()))
                {
                    ws.setEncoding(meta.getEncoding());
                }
				data.workbook = Workbook.getWorkbook(data.file.getContent().getInputStream(), ws);
                
				data.errorHandler.handleFile(data.file);
				// Start at the first sheet again...
				data.sheetnr = 0;
				
				// See if we have sheet names to retrieve, otherwise we'll have to get all sheets...
				//
				if (meta.readAllSheets())
				{
					data.sheetNames = data.workbook.getSheetNames();
					data.startColumn = new int[data.sheetNames.length];
					data.startRow    = new int[data.sheetNames.length];
					for (int i=0;i<data.sheetNames.length;i++)
					{
						data.startColumn[i] = data.defaultStartColumn;
						data.startRow[i]    = data.defaultStartRow;
					}
				}
			}

			boolean nextsheet = false;

			// What sheet were we handling?
			if (log.isDebug()) logDetailed("Get sheet #" + data.filenr + "." + data.sheetnr);
			String sheetName = data.sheetNames[data.sheetnr];
			Sheet sheet = data.workbook.getSheet(sheetName);
			if (sheet != null)
			{
				// at what row do we continue reading?
				if (data.rownr < 0)
				{
					data.rownr = data.startRow[data.sheetnr];

					// Add an extra row if we have a header row to skip...
					if (meta.startsWithHeader())
					{
						data.rownr++;
					}
				}
				// Start at the specified column
				data.colnr = data.startColumn[data.sheetnr];

				// Build a new row and fill in the data from the sheet...
				try
				{
					Cell line[] = sheet.getRow(data.rownr);
					// Already increase cursor 1 row					
					int lineNr = ++data.rownr;
					// Excel starts counting at 0
					if (!data.filePlayList.isProcessingNeeded(data.file, lineNr, sheetName))
					{
						retval.setIgnore();
					}
					else
					{
						if (log.isRowLevel()) logRowlevel("Get line #" + lineNr + " from sheet #" + data.filenr + "." + data.sheetnr);

						if (log.isRowLevel()) logRowlevel("Read line with " + line.length + " cells");
						ExcelInputRow excelInputRow = new ExcelInputRow(sheet.getName(), lineNr, line);
						Row r = fillRow(data.row, data.colnr, excelInputRow);
						if (log.isRowLevel()) logRowlevel("Converted line to row #" + lineNr + " : " + r);

                        boolean isEmpty = isLineEmpty(line);
						if (!isEmpty || !meta.ignoreEmptyRows())
						{
							// Put the row
							retval = r;
						}

						if (isEmpty && meta.stopOnEmpty())
						{
							nextsheet = true;
						}
					}
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					if (log.isRowLevel()) logRowlevel("Out of index error: move to next sheet!");
					// We tried to read below the last line in the sheet.
					// Go to the next sheet...
					nextsheet = true;
				}
			}
			else
			{
				nextsheet = true;
			}

			if (nextsheet)
			{
				// Go to the next sheet
				data.sheetnr++;

				// Reset the start-row:
				data.rownr = -1;

				// no previous row yet, don't take it from the previous sheet!
				// (that whould be plain wrong!)
				data.previousRow = null;

				// Perhaps it was the last sheet?
				if (data.sheetnr >= data.sheetNames.length)
				{
					jumpToNextFile();
				}
			}
		}
		catch (Exception e)
		{
			logError("Error processing row from Excel file [" + data.filename + "] : " + e.toString());
			setErrors(1);
			stopAll();
			return null;
		}

		return retval;
	}

	private boolean isLineEmpty(Cell[] line)
    {
        if (line.length == 0) return true;
        
        boolean isEmpty = true;
        for (int i=0;i<line.length && isEmpty;i++)
        {
            if ( !Const.isEmpty(line[i].getContents()) ) isEmpty=false;
        }
        return isEmpty;
    }

    private void jumpToNextFile() throws KettleException
	{
		data.sheetnr = 0;

		// Reset the start-row:
		data.rownr = -1;

		// no previous row yet, don't take it from the previous sheet! (that
		// whould be plain wrong!)
		data.previousRow = null;

		// Close the workbook!
		data.workbook.close();
		data.workbook = null; // marker to open again.
		data.errorHandler.close();

		// advance to the next file!
		data.filenr++;
	}

	private void initErrorHandling()
	{
		List errorHandlers = new ArrayList(2);
		if (meta.getLineNumberFilesDestinationDirectory() != null)
			errorHandlers.add(new FileErrorHandlerContentLineNumber(getTrans().getCurrentDate(), meta.getLineNumberFilesDestinationDirectory(), meta.getLineNumberFilesExtension(), "Latin1", this));
		if (meta.getErrorFilesDestinationDirectory() != null)
			errorHandlers.add(new FileErrorHandlerMissingFiles(getTrans().getCurrentDate(), meta.getErrorFilesDestinationDirectory(), meta.getErrorFilesExtension(), "Latin1", this));
		data.errorHandler = new CompositeFileErrorHandler(errorHandlers);
	}

	private void initReplayFactory()
	{
		Date replayDate = getTrans().getReplayDate();
		if (replayDate == null)
			data.filePlayList = FilePlayListAll.INSTANCE;
		else
			data.filePlayList = new FilePlayListReplay(replayDate, meta.getLineNumberFilesDestinationDirectory(), meta.getLineNumberFilesExtension(), meta.getErrorFilesDestinationDirectory(), meta
					.getErrorFilesExtension(), "Latin1");
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (ExcelInputMeta) smi;
		data = (ExcelInputData) sdi;

		if (super.init(smi, sdi))
		{
			initErrorHandling();
			initReplayFactory();
			data.files = meta.getFileList();
			if (data.files.nrOfFiles() == 0 && data.files.nrOfMissingFiles() == 0 && !meta.isAcceptingFilenames())
			{
				logError("No file(s) specified! Stop processing.");
				return false;
			}

			data.row = meta.getEmptyFields();
			if (data.row.size() > 0)
			{
				// Determine the maximum filename length...
				data.maxfilelength = -1;

				for (Iterator iter = data.files.getFiles().iterator(); iter.hasNext();)
				{
					FileObject file = (FileObject) iter.next();
					String name = KettleVFS.getFilename(file);
					if (name.length() > data.maxfilelength) data.maxfilelength = name.length();
				}

				// Determine the maximum sheet name length...
				data.maxsheetlength = -1;
				if (!meta.readAllSheets())
				{
					data.sheetNames = new String[meta.getSheetName().length];
					data.startColumn = new int[meta.getSheetName().length];
					data.startRow    = new int[meta.getSheetName().length];
					for (int i = 0; i < meta.getSheetName().length; i++)
					{
						data.sheetNames[i] = meta.getSheetName()[i];
						data.startColumn[i] = meta.getStartColumn()[i];
						data.startRow[i] = meta.getStartRow()[i];
						
						if (meta.getSheetName()[i].length() > data.maxsheetlength) 
						{
							data.maxsheetlength = meta.getSheetName()[i].length();
						}
					}
				}
				else
				{
					// Allocated at open file time: we want ALL sheets.
					if (meta.getStartRow().length==1)
					{
						data.defaultStartRow = meta.getStartRow()[0];
					}
					else
					{
						data.defaultStartRow = 0;
					}
					if (meta.getStartColumn().length==1)
					{
						data.defaultStartColumn = meta.getStartColumn()[0];
					}
					else
					{
						data.defaultStartColumn = 0;
					}
				}

				return true;
			}
			else
			{
				logError("No input fields defined!");
			}

		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (ExcelInputMeta) smi;
		data = (ExcelInputData) sdi;

		if (data.workbook != null) data.workbook.close();
		try
		{
			data.errorHandler.close();
		}
		catch (KettleException e)
		{
			if (log.isDebug()) 
            {
                logDebug("Could not close errorHandler: "+e.toString());
                logDebug(Const.getStackTracker(e));
            }
		}

		super.dispose(smi, sdi);
	}

	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped())
				;
		}
		catch (Exception e)
		{
			logError("Unexpected error : " + e.toString());
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