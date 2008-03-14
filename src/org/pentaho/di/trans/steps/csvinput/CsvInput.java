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
 
package org.pentaho.di.trans.steps.csvinput;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.ResultFile;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
 * Read a simple CSV file
 * Just output Strings found in the file...
 * 
 * @author Matt
 * @since 2007-07-05
 */
public class CsvInput extends BaseStep implements StepInterface
{
	private CsvInputMeta meta;
	private CsvInputData data;
	
	public CsvInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(CsvInputMeta)smi;
		data=(CsvInputData)sdi;

		if (first) {
			first=false;
			
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			if (data.filenames==null) {
				// We're expecting the list of filenames from the previous step(s)...
				//
				getFilenamesFromPreviousSteps();
			}
			
			// The conversion logic for when the lazy conversion is turned of is simple:
			// Pretend it's a lazy conversion object anyway and get the native type during conversion.
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList())
			{
				valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			}
			
			// Open the next file...
			//
			if (!openNextFile()) {
				setOutputDone();
				return false; // nothing to see here, move along...
			}
			
		}
		
		Object[] outputRowData=readOneRow(true);    // get row, set busy!
		if (outputRowData==null)  // no more input to be expected...
		{
			if (openNextFile()) {
				return true; // try again on the next loop...
			}
			else {
				setOutputDone(); // last file, end here
				return false;
			}
		}
		else 
		{
			putRow(data.outputRowMeta, outputRowData);     // copy row to possible alternate rowset(s).
	        if (checkFeedback(linesInput)) logBasic(Messages.getString("CsvInput.Log.LineNumber", Long.toString(linesInput))); //$NON-NLS-1$
		}
			
		return true;
	}

	
	private void getFilenamesFromPreviousSteps() throws KettleException {
		List<String> filenames = new ArrayList<String>();
		boolean firstRow = true;
		int index=-1;
		Object[] row = getRow();
		while (row!=null) {
			
			if (firstRow) {
				firstRow=false;
				
				// Get the filename field index...
				//
				String filenameField = environmentSubstitute(meta.getFilenameField());
				index = getInputRowMeta().indexOfValue(filenameField);
				if (index<0) {
					throw new KettleException(Messages.getString("CsvInput.Exception.FilenameFieldNotFound", filenameField));
				}
			}
				
			String filename = getInputRowMeta().getString(row, index);
			filenames.add(filename);  // add it to the list...
			
			row = getRow(); // Grab another row...
		}
		
		data.filenames = filenames.toArray(new String[filenames.size()]);
	}

	private boolean openNextFile() throws KettleException {
		try {
			
			// Close the previous file...
			//
			if (data.fc!=null) {
				data.fc.close();
			}
			
			if (data.fis!=null) {
				data.fis.close();
			}
			
			if (data.filenr>=data.filenames.length) {
				return false;
			}

			// Open the next one...
			//
			FileObject fileObject = KettleVFS.getFileObject(data.filenames[data.filenr]);
			if (!(fileObject instanceof LocalFile)) {
				// We can only use NIO on local files at the moment, so that's what we limit ourselves to.
				//
				throw new KettleException(Messages.getString("CsvInput.Log.OnlyLocalFilesAreSupported"));
			}
			
			if (meta.isLazyConversionActive()) {
				data.binaryFilename=data.filenames[data.filenr].getBytes();
			}

			data.fis = (FileInputStream)((LocalFile)fileObject).getInputStream();
			data.fc = data.fis.getChannel();
			data.bb = ByteBuffer.allocateDirect( data.preferredBufferSize );
			
			// Add filename to result filenames ?
			if(meta.isAddResultFile())
			{
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString());
				resultFile.setComment("File was read by an Csv input step");
				addResultFile(resultFile);
			}
			
			
			// Move to the next filename
			//
			data.filenr++;
			
			// Reset the row number pointer...
			//
			data.rowNumber = 1L;
			
			// See if we need to skip the header row...
			//
			if (meta.isHeaderPresent()) {
				readOneRow(false); // skip this row.
			}
			
			return true;
		}
		catch(Exception e) {
			throw new KettleException(e);
		}
	}

	/** Read a single row of data from the file... 
	 * 
	 * @param doConversions if you want to do conversions, set to false for the header row.
	 * @return a row of data...
	 * @throws KettleException
	 */
	private Object[] readOneRow(boolean doConversions) throws KettleException {

		try {

			Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			int outputIndex=0;
			boolean newLineFound = false;
			int newLines = 0;
			
			// The strategy is as follows...
			// We read a block of byte[] from the file.
			// We scan for the separators in the file (NOT for line feeds etc)
			// Then we scan that block of data.
			// We keep a byte[] that we extend if needed..
			// At the end of the block we read another, etc.
			//
			// Let's start by looking where we left off reading.
			//
			while (!newLineFound && outputIndex<data.convertRowMeta.size()) {
				
				if (data.endBuffer>=data.bufferSize) {
					// Oops, we need to read more data...
					// Better resize this before we read other things in it...
					//
					data.resizeByteBuffer();
					
					// Also read another chunk of data, now that we have the space for it...
					if (!data.readBufferFromFile()) {
						// TODO handle EOF properly for EOF in the middle of the row, etc.
						return null;
					}
				}

				// OK, at this point we should have data in the byteBuffer and we should be able to scan for the next 
				// delimiter (;)
				// So let's look for a delimiter.
				// Also skip over the enclosures ("), it is NOT taking into account escaped enclosures.
				// Later we can add an option for having escaped or double enclosures in the file. <sigh>
				//
				boolean delimiterFound = false;
				boolean enclosureFound = false;
				int escapedEnclosureFound = 0;
				while (!delimiterFound) {
					// If we find the first char, we might find others as well ;-)
					// Single byte delimiters only for now.
					//
					if (data.byteBuffer[data.endBuffer]==data.delimiter[0]) {
						delimiterFound = true;
					}
					// Perhaps we found a new line?
					// 
					//
					else if (data.byteBuffer[data.endBuffer]=='\n' || data.byteBuffer[data.endBuffer]=='\r') {
						
						data.endBuffer++;
						newLines=1;
						
						if (data.endBuffer>=data.bufferSize) {
							// Oops, we need to read more data...
							// Better resize this before we read other things in it...
							//
							data.resizeByteBuffer();
							
							// Also read another chunk of data, now that we have the space for it...
							// Ignore EOF, there might be other stuff in the buffer.
							//
							data.readBufferFromFile();
						}
						
						// re-check for double delimiters...
						if (data.byteBuffer[data.endBuffer]=='\n' || data.byteBuffer[data.endBuffer]=='\r') {
							data.endBuffer++;
							newLines=2;
							if (data.endBuffer>=data.bufferSize) {
								// Oops, we need to read more data...
								// Better resize this before we read other things in it...
								//
								data.resizeByteBuffer();
								
								// Also read another chunk of data, now that we have the space for it...
								// Ignore EOF, there might be other stuff in the buffer.
								//
								data.readBufferFromFile();
							}
						}
						
						newLineFound = true;
						delimiterFound = true;
					}
					// Perhaps we need to skip over an enclosed part?
					// We always expect exactly one enclosure character
					// If we find the enclosure doubled, we consider it escaped.
					// --> "" is converted to " later on.
					//
					else if (data.enclosure != null && data.byteBuffer[data.endBuffer]==data.enclosure[0]) {
						
						enclosureFound=true;
						boolean keepGoing;
						do {
							if (data.increaseEndBuffer())
							{
								enclosureFound=false;
								break;
							}
							keepGoing = data.byteBuffer[data.endBuffer]!=data.enclosure[0];
							if (!keepGoing)
							{
								// We found an enclosure character.
								// Read another byte...
								if (data.increaseEndBuffer())
								{
									enclosureFound=false;
									break;
								}
								
								// If this character is also an enclosure, we can consider the enclosure "escaped".
								// As such, if this is an enclosure, we keep going...
								//
								keepGoing = data.byteBuffer[data.endBuffer]==data.enclosure[0];
								if (keepGoing) escapedEnclosureFound++;
							}
						} while (keepGoing);
						
						// Did we reach the end of the buffer?
						//
						if (data.endBuffer>=data.bufferSize)
						{
							newLineFound=true; // consider it a newline to break out of the upper while loop
							newLines+=2; // to remove the enclosures in case of missing newline on last line.
							break;
						}
					}
						
					else {
						data.endBuffer++;
						
						if (data.endBuffer>=data.bufferSize) {
							// Oops, we need to read more data...
							// Better resize this before we read other things in it...
							//
							data.resizeByteBuffer();
							
							// Also read another chunk of data, now that we have the space for it...
							if (!data.readBufferFromFile()) {
								// Break out of the loop if we don't have enough buffer space to continue...
								//
								if (data.endBuffer>=data.bufferSize)
								{
									newLineFound=true; // consider it a newline to break out of the upper while loop
									break;
								}
							}
						}
					}
				}
				
				// If we're still here, we found a delimiter..
				// Since the starting point never changed really, we just can grab range:
				//
				//    [startBuffer-endBuffer[
				//
				// This is the part we want.
				//
				int length = data.endBuffer-data.startBuffer;
				if (newLineFound) {
					length-=newLines;
					if (length<=0) length=0;
				}
				if (enclosureFound) {
					data.startBuffer++;
					length-=2;
					if (length<=0) length=0;
				}
				if (length<=0) length=0;
				
				byte[] field = new byte[length];
				System.arraycopy(data.byteBuffer, data.startBuffer, field, 0, length);

				// Did we have any escaped characters in there?
				//
				if (escapedEnclosureFound>0)
				{
					if (log.isRowLevel()) logRowlevel("Escaped enclosures found in "+new String(field));
					field = data.removeEscapedEnclosures(field, escapedEnclosureFound);
				}
				
				if (doConversions) {
					if (meta.isLazyConversionActive()) {
						outputRowData[outputIndex++] = field;
					}
					else {
						// We're not lazy so we convert the data right here and now.
						// The convert object uses binary storage as such we just have to ask the native type from it.
						// That will do the actual conversion.
						//
						ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(outputIndex);
						outputRowData[outputIndex++] = sourceValueMeta.convertBinaryStringToNativeType(field);
					}
				}
				else {
					outputRowData[outputIndex++] = null; // nothing for the header, no conversions here.
				}
				
				// OK, move on to the next field...
				if( !newLineFound) 
				{
					data.endBuffer++;
				}
				data.startBuffer = data.endBuffer;
			}
			
			// Optionally add the current filename to the mix as well...
			//
			if (meta.isIncludingFilename() && !Const.isEmpty(meta.getFilenameField())) {
				if (meta.isLazyConversionActive()) {
					outputRowData[outputIndex++] = data.binaryFilename;
				}
				else {
					outputRowData[outputIndex++] = data.filenames[data.filenr-1];
				}
			}
			
			if (data.isAddingRowNumber) {
				outputRowData[outputIndex++] = new Long(data.rowNumber++);
			}
		
			linesInput++;
			return outputRowData;
		}
		catch (Exception e)
		{
			throw new KettleFileException("Exception reading line using NIO", e);
		}

	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CsvInputMeta)smi;
		data=(CsvInputData)sdi;
		
		if (super.init(smi, sdi)) {
			data.preferredBufferSize = Integer.parseInt(environmentSubstitute(meta.getBufferSize()));
			
			// If the step doesn't have any previous steps, we just get the filename.
			// Otherwise, we'll grab the list of filenames later...
			//
			if (getTransMeta().findNrPrevSteps(getStepMeta())==0) {
				String filename = environmentSubstitute(meta.getFilename());

				if (Const.isEmpty(filename)) {
					logError(Messages.getString("CsvInput.MissingFilename.Message"));
					return false;
				}

				data.filenames = new String[] { filename, };
			}
			else {
				data.filenames = null;
				data.filenr = 0;
			}
							
			data.delimiter = environmentSubstitute(meta.getDelimiter()).getBytes();

			if( Const.isEmpty(meta.getEnclosure()) ) {
				data.enclosure = null;
			} else {
				data.enclosure = environmentSubstitute(meta.getEnclosure()).getBytes();
			}
			
			data.isAddingRowNumber = !Const.isEmpty(meta.getRowNumField());
			
			return true;

		}
		return false;
	}
	
	public void closeFile() throws KettleException {
		
		try {
			if (data.fc!=null) {
				data.fc.close();
			}
			if (data.fis!=null) {
				data.fis.close();
			}
		} catch (IOException e) {
			throw new KettleException("Unable to close file channel for file '"+data.filenames[data.filenr-1],e);
		}
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}