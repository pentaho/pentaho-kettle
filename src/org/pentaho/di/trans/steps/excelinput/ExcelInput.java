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

package org.pentaho.di.trans.steps.excelinput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueDataUtil;
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
import org.pentaho.di.trans.step.errorhandling.CompositeFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerContentLineNumber;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerMissingFiles;


/**
 * This class reads data from one or more Microsoft Excel files.
 * 
 * @author Matt
 * @since 19-NOV-2003
 */
public class ExcelInput extends BaseStep implements StepInterface
{
	private ExcelInputMeta meta;

	private ExcelInputData data;

	public ExcelInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private Object[] fillRow(int startcolumn, ExcelInputRow excelInputRow) throws KettleException
	{
		Object[] r = new Object[data.outputRowMeta.size()];

		// Keep track whether or not we handled an error for this line yet.
		boolean errorHandled = false;

		// Set values in the row...
		for (int i = startcolumn; i < excelInputRow.cells.length && i - startcolumn < r.length; i++)
		{
			Cell cell = excelInputRow.cells[i];

			int rowcolumn = i - startcolumn;

			ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta(rowcolumn);
			ValueMetaInterface sourceMeta = null;
            
			try
			{
				checkType(cell, targetMeta);
			}
			catch (KettleException ex)
			{
				if (!meta.isErrorIgnored()) throw ex;
				logBasic("Warning processing [" + targetMeta + "] from Excel file [" + data.filename + "] : " + ex.getMessage());
				if (!errorHandled)
				{
					data.errorHandler.handleLineError(excelInputRow.rownr, excelInputRow.sheetName);
					errorHandled = true;
				}
				
				if (meta.isErrorLineSkipped())
				{
					return null;
				}
			}

			CellType cellType = cell.getType();
			if (CellType.BOOLEAN.equals(cellType) ||
				CellType.BOOLEAN_FORMULA.equals(cellType))
			{
				r[rowcolumn] = Boolean.valueOf( ((BooleanCell)cell).getValue() );
                sourceMeta = data.valueMetaBoolean;
			}
			else
			{
				if (CellType.DATE.equals(cellType) ||
					CellType.DATE_FORMULA.equals(cellType) )
				{
					Date date = ((DateCell) cell).getDate();
					long time = date.getTime();
					int offset = TimeZone.getDefault().getOffset(time);
                    r[rowcolumn] = new Date(time - offset);
                    sourceMeta = data.valueMetaDate;
				}
				else
				{
					if (CellType.LABEL.equals(cellType) ||
					    CellType.STRING_FORMULA.equals(cellType))
					{
                        String string = ((LabelCell) cell).getString();
						switch (meta.getField()[rowcolumn].getTrimType())
						{
						case ExcelInputMeta.TYPE_TRIM_LEFT:
							string = ValueDataUtil.leftTrim(string);
							break;
						case ExcelInputMeta.TYPE_TRIM_RIGHT:
							string = ValueDataUtil.rightTrim(string);
							break;
						case ExcelInputMeta.TYPE_TRIM_BOTH:
							string = ValueDataUtil.trim(string);
							break;
						default:
							break;
						}
                        r[rowcolumn] = string;
                        sourceMeta = data.valueMetaString;
					}
					else
					{
						if (CellType.NUMBER.equals(cellType) ||
						    CellType.NUMBER_FORMULA.equals(cellType))
						{
                            r[rowcolumn] = new Double( ((NumberCell)cell).getValue() );
                            sourceMeta = data.valueMetaNumber;
						}
						else
						{
							if (log.isDetailed()) logDetailed("Unknown type : " + cell.getType().toString() + " : [" + cell.getContents() + "]");
                            r[rowcolumn] = null;
						}
					}
				}
			}
			
			ExcelInputField field = meta.getField()[rowcolumn];

			// Change to the appropriate type if needed...
			//
			try
			{
                // Null stays null folks.
                //
				if (sourceMeta!=null  && sourceMeta.getType() != targetMeta.getType() && r[rowcolumn]!=null)
				{
                    ValueMetaInterface sourceMetaCopy = sourceMeta.clone();
                    sourceMetaCopy.setConversionMask(field.getFormat());
                    sourceMetaCopy.setGroupingSymbol(field.getGroupSymbol());
                    sourceMetaCopy.setDecimalSymbol(field.getDecimalSymbol());
                    sourceMetaCopy.setCurrencySymbol(field.getCurrencySymbol());
                    
					switch (targetMeta.getType())
					{
					// Use case: we find a numeric value: convert it using the supplied format to the desired data type...
					//
					case ValueMetaInterface.TYPE_NUMBER:
					case ValueMetaInterface.TYPE_INTEGER:
						switch (field.getType())
						{
						case ValueMetaInterface.TYPE_DATE:
                            // number to string conversion (20070522.00 --> "20070522")
                            //
                            ValueMetaInterface valueMetaNumber = new ValueMeta("num", ValueMetaInterface.TYPE_NUMBER);
                            valueMetaNumber.setConversionMask("#");
                            Object string = sourceMetaCopy.convertData(valueMetaNumber, r[rowcolumn]);
                            
                            // String to date with mask...
                            //
                            r[rowcolumn] = targetMeta.convertData(sourceMetaCopy, string);
							break;
						default:
                            r[rowcolumn] = targetMeta.convertData(sourceMetaCopy, r[rowcolumn]);
							break;
						}
						break;
					// Use case: we find a date: convert it using the supplied format to String...
					//
					default:
                        r[rowcolumn] = targetMeta.convertData(sourceMetaCopy, r[rowcolumn]);
					}
				}
			}
			catch (KettleException ex)
			{
				if (!meta.isErrorIgnored()) throw ex;
				logBasic("Warning processing [" + targetMeta + "] from Excel file [" + data.filename + "] : " + ex.toString());
				if (!errorHandled) // check if we didn't log an error already for this one.
				{
					data.errorHandler.handleLineError(excelInputRow.rownr, excelInputRow.sheetName);
					errorHandled=true;
				}

				if (meta.isErrorLineSkipped())
				{
					return null;
				}
				else
				{
				    r[rowcolumn] = null;
				}
			}
		}

        int rowIndex = meta.getField().length;

		// Do we need to include the filename?
		if (!Const.isEmpty(meta.getFileField()))
		{
			r[rowIndex] = data.filename;
            rowIndex++;
		}

		// Do we need to include the sheetname?
		if (!Const.isEmpty(meta.getSheetField()))
		{
            r[rowIndex] = excelInputRow.sheetName;
            rowIndex++;
		}

		// Do we need to include the sheet rownumber?
		if (!Const.isEmpty(meta.getSheetRowNumberField()))
		{
            r[rowIndex] = new Long(data.rownr);
            rowIndex++;
		}
		
		// Do we need to include the rownumber?
		if (!Const.isEmpty(meta.getRowNumberField()))
		{
            r[rowIndex] = new Long(linesWritten + 1);
            rowIndex++;
		}

		return r;
	}

	private void checkType(Cell cell, ValueMetaInterface v) throws KettleException
	{
		if (!meta.isStrictTypes()) return;
		CellType cellType = cell.getType();
		if (cellType.equals(CellType.BOOLEAN))
		{
			if (!(v.getType() == ValueMetaInterface.TYPE_STRING || v.getType() == ValueMetaInterface.TYPE_NONE || v.getType() == ValueMetaInterface.TYPE_BOOLEAN))
				throw new KettleException("Invalid type Boolean, expected " + v.getTypeDesc());
		}
		else if (cellType.equals(CellType.DATE))
		{
			if (!(v.getType() == ValueMetaInterface.TYPE_STRING || v.getType() == ValueMetaInterface.TYPE_NONE || v.getType() == ValueMetaInterface.TYPE_DATE))
				throw new KettleException("Invalid type Date: " + cell.getContents() + ", expected " + v.getTypeDesc());
		}
		else if (cellType.equals(CellType.LABEL))
		{
			if (v.getType() == ValueMetaInterface.TYPE_BOOLEAN || v.getType() == ValueMetaInterface.TYPE_DATE || v.getType() == ValueMetaInterface.TYPE_INTEGER || v.getType() == ValueMetaInterface.TYPE_NUMBER)
				throw new KettleException("Invalid type Label: " + cell.getContents() + ", expected " + v.getTypeDesc());
		}
		else if (cellType.equals(CellType.EMPTY))
		{
			// ok
		}
		else if (cellType.equals(CellType.NUMBER))
		{
			if (!(v.getType() == ValueMetaInterface.TYPE_STRING || v.getType() == ValueMetaInterface.TYPE_NONE || v.getType() == ValueMetaInterface.TYPE_INTEGER || v.getType() == ValueMetaInterface.TYPE_BIGNUMBER || v.getType() == ValueMetaInterface.TYPE_NUMBER))
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
            
            data.outputRowMeta = new RowMeta(); // start from scratch!
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            if (meta.isAcceptingFilenames())
            {
                // Read the files from the specified input stream...
                data.files.getFiles().clear();
                
                int idx = -1;
                RowSet rowSet = findInputRowSet(meta.getAcceptingStepName());
                Object[] fileRow = getRowFrom(rowSet);
                while (fileRow!=null)
                {
                    if (idx<0)
                    {
                        idx = rowSet.getRowMeta().indexOfValue(meta.getAcceptingField());
                        if (idx<0)
                        {
                            logError("The filename field ["+meta.getAcceptingField()+"] could not be found in the input rows.");
                            setErrors(1);
                            stopAll();
                            return false;
                        }
                    }
                    String fileValue = rowSet.getRowMeta().getString(fileRow, idx);
                    try
                    {
                        data.files.addFile(KettleVFS.getFileObject(fileValue));
                    }
                    catch(IOException e)
                    {
                        throw new KettleException("Unexpected error creating file object for "+fileValue, e);
                    }
                    
                    // Grab another row
                    fileRow = getRowFrom(rowSet);
                }
                
            }

			handleMissingFiles();
		}

		// See if we're not done processing...
		// We are done processing if the filenr >= number of files.
		if (data.filenr >= data.files.nrOfFiles())
		{
			if (log.isDetailed()) logDetailed("No more files to be processed! (" + data.filenr + " files done)");
			setOutputDone(); // signal end to receiver(s)
			return false; // end of data or error.
		}

		if (meta.getRowLimit() > 0 && data.rownr > meta.getRowLimit())
		{
			// The close of the openFile is in dispose()
			if (log.isDetailed()) logDetailed("Row limit of [" + meta.getRowLimit() + "] reached: stop processing.");
			setOutputDone(); // signal end to receiver(s)
			return false; // end of data or error.
		}
       
		Object[] r = getRowFromWorkbooks();
		if (r != null)
		{
			// OK, see if we need to repeat values.
			if (data.previousRow != null)
			{
				for (int i = 0; i < meta.getField().length; i++)
				{
					ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(i);
                    Object valueData = r[i];
                    
					if (valueMeta.isNull(valueData) && meta.getField()[i].isRepeated())
					{
						// Take the value from the previous row.
						r[i] = data.previousRow[i];
					}
				}
			}

			// Remember this row for the next time around!
			data.previousRow = data.outputRowMeta.cloneRow(r);

			// Send out the good news: we found a row of data!
			putRow(data.outputRowMeta, r);
            
			return true;
		}
        else
        {
            // This row is ignored / eaten
            // We continue though.
            return true;
        }
	}

	private void handleMissingFiles() throws KettleException
	{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			log.logBasic("Required files", "WARNING: Missing " + message);
			if (meta.isErrorIgnored())
				for (FileObject fileObject : nonExistantFiles)
				{
					data.errorHandler.handleNonExistantFile( fileObject );
				}
			else
				throw new KettleException("Following required files are missing: " + message);
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			log.logBasic("Required files", "WARNING: Not accessible " + message);
			if (meta.isErrorIgnored())
				for (FileObject fileObject : nonAccessibleFiles)
				{
					data.errorHandler.handleNonAccessibleFile(fileObject);
				}
			else
				throw new KettleException("Following required files are not accessible: " + message);
		}
	}

	public Object[] getRowFromWorkbooks()
	{
		// This procedure outputs a single Excel data row on the destination
		// rowsets...
        
        Object[] retval = null;

		try
		{
			// First, see if a file has been opened?
			if (data.workbook == null)
			{
				// Open a new openFile..
				data.file = data.files.getFile(data.filenr);
				data.filename = KettleVFS.getFilename( data.file );
				
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), toString());
				resultFile.setComment("File was read by an Excel input step");
				addResultFile(resultFile);
				
				if (log.isDetailed()) logDetailed("Opening openFile #" + data.filenr + " : " + data.filename);
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
                        retval = null; // placeholder, was already null
					}
					else
					{
						if (log.isRowLevel()) logRowlevel("Get line #" + lineNr + " from sheet #" + data.filenr + "." + data.sheetnr);

						if (log.isRowLevel()) logRowlevel("Read line with " + line.length + " cells");
						ExcelInputRow excelInputRow = new ExcelInputRow(sheet.getName(), lineNr, line);
						Object[] r = fillRow(data.colnr, excelInputRow);
						if (log.isRowLevel()) logRowlevel("Converted line to row #" + lineNr + " : " + data.outputRowMeta.getString(r));

                        boolean isEmpty = isLineEmpty(line);
						if (!isEmpty || !meta.ignoreEmptyRows())
						{
							// Put the row
							retval = r;
						}
						else
						{
							if (data.rownr>sheet.getRows())
							{
								nextsheet=true;
							}
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

		// Close the openFile!
		data.workbook.close();
		data.workbook = null; // marker to open again.
		data.errorHandler.close();

		// advance to the next file!
		data.filenr++;
	}

	private void initErrorHandling()
	{
		List<FileErrorHandler> errorHandlers = new ArrayList<FileErrorHandler>(2);

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
			data.files = meta.getFileList(this);
			if (data.files.nrOfFiles() == 0 && data.files.nrOfMissingFiles() == 0 && !meta.isAcceptingFilenames())
			{
				logError("No file(s) specified! Stop processing.");
				return false;
			}

			if (meta.getEmptyFields().size() > 0)
			{
				// Determine the maximum filename length...
				data.maxfilelength = -1;

				for (FileObject file : data.files.getFiles())
				{
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