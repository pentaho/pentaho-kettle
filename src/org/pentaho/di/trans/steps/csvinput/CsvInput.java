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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleConversionException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.steps.textfileinput.EncodingType;

/**
 * Read a simple CSV file
 * Just output Strings found in the file...
 * 
 * @author Matt
 * @since 2007-07-05
 */
public class CsvInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = CsvInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
			
			// We only run in parallel if we have at least one file to process
			// AND if we have more than one step copy running...
			//
			data.parallel = meta.isRunningInParallel() && data.totalNumberOfSteps>1;
			
			// The conversion logic for when the lazy conversion is turned of is simple:
			// Pretend it's a lazy conversion object anyway and get the native type during conversion.
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList())
			{
				valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			}
			
			// Calculate the indexes for the filename and row number fields
			//
			data.filenameFieldIndex = -1;
			if (!Const.isEmpty(meta.getFilenameField()) && meta.isIncludingFilename()) {
				data.filenameFieldIndex = meta.getInputFields().length;
			}
			
			data.rownumFieldIndex = -1;
			if (!Const.isEmpty(meta.getRowNumField())) {
				data.rownumFieldIndex = meta.getInputFields().length;
				if (data.filenameFieldIndex>=0) {
					data.rownumFieldIndex++;
				}
			}
			
			// Now handle the parallel reading aspect: determine total of all the file sizes
			// Then skip to the appropriate file and location in the file to start reading...
			// Also skip to right after the first newline
			//
			if (data.parallel) {
				prepareToRunInParallel();
			}
			
			// Open the next file...
			//
			if (!openNextFile()) {
				setOutputDone();
				return false; // nothing to see here, move along...
			}	
		}
		
		// If we are running in parallel, make sure we don't read too much in this step copy...
		//
		if (data.parallel) {
			if (data.totalBytesRead>data.blockToRead) {
				setOutputDone(); // stop reading
				return false;
			}
		}
		
		try {
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
		        if (checkFeedback(getLinesInput())) 
		        {
		        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "CsvInput.Log.LineNumber", Long.toString(getLinesInput()))); //$NON-NLS-1$
		        }
			}
		}
		catch(KettleConversionException e) {
			if (getStepMeta().isDoingErrorHandling()) {
				StringBuffer errorDescriptions = new StringBuffer(100);
				StringBuffer errorFields = new StringBuffer(50);
				for (int i=0;i<e.getCauses().size();i++) {
					if (i>0) {
						errorDescriptions.append(", "); //$NON-NLS-1$
						errorFields.append(", "); //$NON-NLS-1$
					}
					errorDescriptions.append(e.getCauses().get(i).getMessage());
					errorFields.append(e.getFields().get(i).toStringMeta());
				}
				
				putError(data.outputRowMeta, e.getRowData(), e.getCauses().size(), errorDescriptions.toString(), errorFields.toString(), "CSVINPUT001"); //$NON-NLS-1$
			} else {
			  // Only forward the first cause.
			  //
			  throw new KettleException(e.getMessage(), e.getCauses().get(0));
			}
		}
			
		return true;
	}

	
	private void prepareToRunInParallel() throws KettleException {
		try {
			// At this point it doesn't matter if we have 1 or more files.
			// We'll use the same algorithm...
			//
	        for (String filename : data.filenames) { 
	        	long size = KettleVFS.getFileObject(filename, getTransMeta()).getContent().getSize();
	        	data.fileSizes.add(size);
	        	data.totalFileSize+=size;
	        }
	        
	        // Now we can determine the range to read.
	        //
	        // For example, the total file size is 50000, spread over 5 files of 10000
	        // Suppose we have 2 step copies running (clustered or not)
	        // That means step 0 has to read 0-24999 and step 1 has to read 25000-49999
	        //
	        // The size of the block to read (25000 in the example) :
	        //
	        data.blockToRead = Math.round( (double)data.totalFileSize / (double)data.totalNumberOfSteps ); 
	        
	        // Now we calculate the position to read (0 and 25000 in our sample) :
	        //
	        data.startPosition = data.blockToRead * data.stepNumber;
	        data.endPosition = data.startPosition + data.blockToRead;
	        
	        // Determine the start file number (0 or 2 in our sample) :
	        // >0<,1000,>2000<,3000,4000
	        //
	        long totalFileSize=0L;
	        for (int i=0;i<data.fileSizes.size();i++) {
	        	long size = data.fileSizes.get(i);

	        	// Start of file range: totalFileSize
	        	// End of file range: totalFileSize+size
	        	
	        	if (data.startPosition>=totalFileSize && data.startPosition<totalFileSize+size) {
	        		// This is the file number to start reading from...
	        		//
	        		data.filenr = i;
	        		
	        		// remember where we started to read to allow us to know that we have to skip the header row in the next files (if any)
	        		//
	        		data.startFilenr = i; 
	        		
	        		
	        		// How many bytes do we skip in that first file?
	        		//
	        		if (data.startPosition==0) {
	        			data.bytesToSkipInFirstFile=0L;
	        		} else {
	        			data.bytesToSkipInFirstFile = data.startPosition - totalFileSize;
	        		}
	        		
	        		break;
	        	}
	        	totalFileSize+=size;
	        }
	        
	        if (data.filenames.length > 0)
	        	logBasic(BaseMessages.getString(PKG, "CsvInput.Log.ParallelFileNrAndPositionFeedback", data.filenames[data.filenr], Long.toString(data.fileSizes.get(data.filenr)), Long.toString(data.bytesToSkipInFirstFile), Long.toString(data.blockToRead))); //$NON-NLS-1$
		}
		catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "CsvInput.Exception.ErrorPreparingParallelRun"), e); //$NON-NLS-1$
		}
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
					throw new KettleException(BaseMessages.getString(PKG, "CsvInput.Exception.FilenameFieldNotFound", filenameField)); //$NON-NLS-1$
				}
			}
				
			String filename = getInputRowMeta().getString(row, index);
			filenames.add(filename);  // add it to the list...
			
			row = getRow(); // Grab another row...
		}
		
		data.filenames = filenames.toArray(new String[filenames.size()]);
		
		logBasic(BaseMessages.getString(PKG, "CsvInput.Log.ReadingFromNrFiles", Integer.toString(data.filenames.length))); //$NON-NLS-1$
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
			FileObject fileObject = KettleVFS.getFileObject(data.filenames[data.filenr], getTransMeta());
			if (!(fileObject instanceof LocalFile)) {
				// We can only use NIO on local files at the moment, so that's what we limit ourselves to.
				//
				throw new KettleException(BaseMessages.getString(PKG, "CsvInput.Log.OnlyLocalFilesAreSupported")); //$NON-NLS-1$
			}
			
			if (meta.isLazyConversionActive()) {
				data.binaryFilename=data.filenames[data.filenr].getBytes();
			}
			
			data.fis = new FileInputStream(KettleVFS.getFilename(fileObject));
			data.fc = data.fis.getChannel();
			data.bb = ByteBuffer.allocateDirect( data.preferredBufferSize );

			// If we are running in parallel and we need to skip bytes in the first file, let's do so here.
			//
			if (data.parallel) {
				if (data.bytesToSkipInFirstFile>0) {
					data.fc.position(data.bytesToSkipInFirstFile);
	
					// Now, we need to skip the first row, until the first CR that is.
					//
					readOneRow(false);
				}
			}

			// Add filename to result filenames ?
			if(meta.isAddResultFile())
			{
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString());
				resultFile.setComment("File was read by a Csv input step");
				addResultFile(resultFile);
			}
			
			// Move to the next filename
			//
			data.filenr++;
			
			// See if we need to skip a row...
			// - If you have a header row checked and if you're not running in parallel
			// - If you're running in parallel, if a header row is checked, if you're at the beginning of a file
			//
			if (meta.isHeaderPresent()) {
				if ( (!data.parallel) || // Standard flat file : skip header 
					(data.parallel && data.bytesToSkipInFirstFile<=0)
					) {
					readOneRow(false); // skip this row.
					logBasic(BaseMessages.getString(PKG, "CsvInput.Log.HeaderRowSkipped", data.filenames[data.filenr-1])); //$NON-NLS-1$
				}
			}
			
			// Reset the row number pointer...
			//
			data.rowNumber = 1L;

			// Don't skip again in the next file...
			//
			data.bytesToSkipInFirstFile=-1L;

			
			return true;
		}
		catch(KettleException e) {
			throw e;
		}
		catch(Exception e) {
			throw new KettleException(e);
		}
	}

	/**
	 * Check to see if the buffer size is large enough given the data.endBuffer pointer.<br>
	 * Resize the buffer if there is not enough room.
	 * 
	 * @return false if everything is OK, true if there is a problem and we should stop.
	 * @throws IOException in case there is a I/O problem (read error)
	 */
	private boolean checkBufferSize() throws IOException {
		if (data.endBuffer>=data.bufferSize) {
			// Oops, we need to read more data...
			// Better resize this before we read other things in it...
			//
			data.resizeByteBufferArray();
			
			// Also read another chunk of data, now that we have the space for it...
			//
			int n = data.readBufferFromFile();

			// If we didn't manage to read something, we return true to indicate we're done
			//  
			return n<0;
		}
		return false;
	}
	
	/*
    private boolean isReturn(byte[] source, int location) {
      switch (data.encodingType) {
      case SINGLE:
        return source[location] == '\n';
  
      case DOUBLE_BIG_ENDIAN:
        if (location >= 1) {
          return source[location - 1] == 0 && source[location] == 0x0d;
        } else {
          return false;
        }
  
      case DOUBLE_LITTLE_ENDIAN:
        if (location >= 1) {
          return source[location - 1] == 0x0d && source[location] == 0x00;
        } else {
          return false;
        }
  
      default:
        return source[location] == '\n';
      }
    }

    private boolean isLineFeed(byte[] source, int location) {
      switch (data.encodingType) {
      case SINGLE:
        return source[location] == '\r';
  
      case DOUBLE_BIG_ENDIAN:
        if (location >= 1) {
          return source[location - 1] == 0 && source[location] == 0x0a;
        } else {
          return false;
        }
  
      case DOUBLE_LITTLE_ENDIAN:
        if (location >= 1) {
          return source[location - 1] == 0x0a && source[location] == 0x00;
        } else {
          return false;
        }
  
      default:
        return source[location] == '\r';
      }
    }
	*/
	
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
			boolean endOfBuffer = false;
			int newLines = 0;
			List<Exception> conversionExceptions = null;
			List<ValueMetaInterface> exceptionFields = null;
			
			// The strategy is as follows...
			// We read a block of byte[] from the file.
			// We scan for the separators in the file (NOT for line feeds etc)
			// Then we scan that block of data.
			// We keep a byte[] that we extend if needed..
			// At the end of the block we read another, etc.
			//
			// Let's start by looking where we left off reading.
			//
			while (!newLineFound && outputIndex<meta.getInputFields().length) {
				
				if (checkBufferSize()) {
					// Last row was being discarded if the last item is null and
					// there is no end of line delimiter
					if (outputRowData != null) {
						// Make certain that at least one record exists before
						// filling the rest of them with null
						if (outputIndex > 0) {
							return (outputRowData);
						}
					}
           
					return null; // nothing more to read, call it a day.
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
					if (data.delimiterMatcher.matchesPattern(data.byteBuffer, data.endBuffer, data.delimiter)) {
						delimiterFound = true;
					}
					// Perhaps we found a new line?
					// 
					//
					else if (data.crLfMatcher.isReturn(data.byteBuffer, data.endBuffer) || 
					    data.crLfMatcher.isLineFeed(data.byteBuffer, data.endBuffer)) {
						
						if(data.encodingType.equals(EncodingType.DOUBLE_LITTLE_ENDIAN) || data.encodingType.equals(EncodingType.DOUBLE_BIG_ENDIAN)) {
							data.endBuffer += 2;
						} else {
							data.endBuffer ++;
						}
						
						data.totalBytesRead++;
						newLines=1;
						
						if (data.endBuffer>=data.bufferSize) {
							// Oops, we need to read more data...
							// Better resize this before we read other things in it...
							//
							data.resizeByteBufferArray();
							
							// Also read another chunk of data, now that we have the space for it...
							// Ignore EOF, there might be other stuff in the buffer.
							//
							data.readBufferFromFile();
						}
						
						// re-check for double delimiters...
						if (data.crLfMatcher.isReturn(data.byteBuffer, data.endBuffer) || 
						    data.crLfMatcher.isLineFeed(data.byteBuffer, data.endBuffer)) {
							data.endBuffer++;
							data.totalBytesRead++;
							newLines=2;
							if (data.endBuffer>=data.bufferSize) {
								// Oops, we need to read more data...
								// Better resize this before we read other things in it...
								//
								data.resizeByteBufferArray();
								
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
					else if (data.enclosure != null && data.enclosureMatcher.matchesPattern(data.byteBuffer, data.endBuffer, data.enclosure)) {
						
						enclosureFound=true;
						boolean keepGoing;
						do {
							if (data.increaseEndBuffer())
							{
								enclosureFound=false;
								break;
							}
							keepGoing = !data.enclosureMatcher.matchesPattern(data.byteBuffer, data.endBuffer, data.enclosure);
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
								keepGoing = data.enclosureMatcher.matchesPattern(data.byteBuffer, data.endBuffer, data.enclosure);
								if (keepGoing) escapedEnclosureFound++;
							}
						} while (keepGoing);
						
						// Did we reach the end of the buffer?
						//
						if (data.endBuffer>=data.bufferSize)
						{
							newLineFound=true; // consider it a newline to break out of the upper while loop
							newLines+=2; // to remove the enclosures in case of missing newline on last line.
							endOfBuffer=true;
							break;
						}
					}
						
					else {
						
						data.endBuffer++;
						data.totalBytesRead++;

						if (checkBufferSize()) {
							if (data.endBuffer>=data.bufferSize) {
								newLineFound=true;
								break;
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
				// data.byteBuffer[data.startBuffer]
				//
				int length = calculateFieldLength(newLineFound, newLines, enclosureFound, endOfBuffer);
				
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
						try {
							outputRowData[outputIndex++] = sourceValueMeta.convertBinaryStringToNativeType(field);
						} catch(KettleValueException e) {
							// There was a conversion error,
							//
							outputRowData[outputIndex++] = null;
							
							if (conversionExceptions==null) {
								conversionExceptions = new ArrayList<Exception>();
								exceptionFields = new ArrayList<ValueMetaInterface>();
							}
							
							conversionExceptions.add(e);
							exceptionFields.add(sourceValueMeta);
						}
					}
				}
				else {
					outputRowData[outputIndex++] = null; // nothing for the header, no conversions here.
				}
				
				// OK, move on to the next field...
				if( !newLineFound) 
				{
					data.endBuffer++;
					data.totalBytesRead++;
				}
				data.startBuffer = data.endBuffer;
			}
			
			// See if we reached the end of the line.
			// If not, we need to skip the remaining items on the line until the next newline...
			//
			if (!newLineFound && !checkBufferSize()) 
			{
				do {
					data.endBuffer++;
					data.totalBytesRead++;
				
					if (checkBufferSize()) {
						break; // nothing more to read.
					}
					
					// TODO: if we're using quoting we might be dealing with a very dirty file with quoted newlines in trailing fields. (imagine that)
					// In that particular case we want to use the same logic we use above (refactored a bit) to skip these fields.
					
				} while (!data.crLfMatcher.isReturn(data.byteBuffer, data.endBuffer) && 
				    !data.crLfMatcher.isLineFeed(data.byteBuffer, data.endBuffer));
				
				if (!checkBufferSize()) {
					while (data.crLfMatcher.isReturn(data.byteBuffer, data.endBuffer) || 
					    data.crLfMatcher.isLineFeed(data.byteBuffer, data.endBuffer)) {
						data.endBuffer++;
						data.totalBytesRead++;
						if (checkBufferSize()) {
							break; // nothing more to read.
						}
					}
				}
				
				// Make sure we start at the right position the next time around.
				data.startBuffer = data.endBuffer;
			}
			
			// Optionally add the current filename to the mix as well...
			//
			if (meta.isIncludingFilename() && !Const.isEmpty(meta.getFilenameField())) {
				if (meta.isLazyConversionActive()) {
					outputRowData[data.filenameFieldIndex] = data.binaryFilename;
				}
				else {
					outputRowData[data.filenameFieldIndex] = data.filenames[data.filenr-1];
				}
			}
			
			if (data.isAddingRowNumber) {
				outputRowData[data.rownumFieldIndex] = new Long(data.rowNumber++);
			}
		
			incrementLinesInput();
			
			if (conversionExceptions!=null && conversionExceptions.size()>0) {
				// Forward the first exception
				//
				throw new KettleConversionException("There were "+conversionExceptions.size()+" conversion errors on line "+getLinesInput(), conversionExceptions, exceptionFields, outputRowData);
			}
			
			return outputRowData;
		}
		catch(KettleConversionException e) {
			throw e;
		}
		catch (Exception e)
		{
			throw new KettleFileException("Exception reading line using NIO", e);
		}

	}

	private int calculateFieldLength(boolean newLineFound, int newLines, boolean enclosureFound, boolean endOfBuffer) {
	  
	  int length = data.endBuffer-data.startBuffer;
      if (newLineFound) {
          length-=newLines;
          if (length<=0) length=0;
          if (endOfBuffer) data.startBuffer++; // offset for the enclosure in last field before EOF
      }
      if (enclosureFound) {
          data.startBuffer++;
          length-=2;
          if (length<=0) length=0;
      }
      if (length<=0) length=0;
      if (data.encodingType!=EncodingType.SINGLE) {
        length--;
      }
      return length;
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
					logError(BaseMessages.getString(PKG, "CsvInput.MissingFilename.Message")); //$NON-NLS-1$
					return false;
				}

				data.filenames = new String[] { filename, };
			}
			else {
				data.filenames = null;
				data.filenr = 0;
			}
			
			data.totalBytesRead=0L;
			
			data.encodingType = EncodingType.guessEncodingType(meta.getEncoding());
							
            // PDI-2489 - set the delimiter byte value to the code point of the
            // character as represented in the input file's encoding
            try {
              data.delimiter = data.encodingType.getBytes( environmentSubstitute(meta.getDelimiter()), meta.getEncoding());

  			  if( Const.isEmpty(meta.getEnclosure()) ) {
  				data.enclosure = null;
  			  } else {
  				data.enclosure = data.encodingType.getBytes( environmentSubstitute(meta.getEnclosure()), meta.getEncoding() );
  			  }
  			  
            } catch (UnsupportedEncodingException e) {
              logError(BaseMessages.getString(PKG, "CsvInput.BadEncoding.Message"), e); //$NON-NLS-1$
              return false;
            }
			
			data.isAddingRowNumber = !Const.isEmpty(meta.getRowNumField());
			
			// Handle parallel reading capabilities...
			//
			data.stopReading = false;
			
			if (meta.isRunningInParallel()) {
				data.stepNumber = getUniqueStepNrAcrossSlaves();
				data.totalNumberOfSteps = getUniqueStepCountAcrossSlaves();
				
				// We are not handling a single file, but possibly a list of files...
				// As such, the fair thing to do is calculate the total size of the files
				// Then read the required block.
				//
				
	            data.fileSizes = new ArrayList<Long>();
	            data.totalFileSize = 0L;
			}
			
			// Set the most efficient pattern matcher to match the delimiter.
			//
			if (data.delimiter.length==1) {
      		  data.delimiterMatcher = new SingleBytePatternMatcher();
			} else {
              data.delimiterMatcher = new MultiBytePatternMatcher();
			}

	         // Set the most efficient pattern matcher to match the enclosure.
            //
			if (data.enclosure==null) {
			  data.enclosureMatcher = new EmptyPatternMatcher();
			} else {
              if (data.enclosure.length==1) {
                  data.enclosureMatcher = new SingleBytePatternMatcher();
              } else {
                data.enclosureMatcher = new MultiBytePatternMatcher();
              }
			}
            
            switch(data.encodingType) {
            case DOUBLE_BIG_ENDIAN: data.crLfMatcher = new MultiByteBigCrLfMatcher(); break;
            case DOUBLE_LITTLE_ENDIAN: data.crLfMatcher = new MultiByteLittleCrLfMatcher(); break;
            default: data.crLfMatcher = new SingleByteCrLfMatcher(); break;
            }

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
	
	/**
	 * This method is borrowed from TextFileInput
	 * 
	 * @param log
	 * @param line
	 * @param delimiter
	 * @param enclosure
	 * @param escapeCharacter
	 * @return
	 * @throws KettleException
	 */
	public static final String[] guessStringsFromLine(LogChannelInterface log, String line, String delimiter, String enclosure, String escapeCharacter) throws KettleException
  {
    List<String> strings = new ArrayList<String>();
        int fieldnr;
        
    String pol; // piece of line

    try
    {
      if (line == null) return null;

      // Split string in pieces, only for CSV!

      fieldnr = 0;
      int pos = 0;
      int length = line.length();
      boolean dencl = false;

              int len_encl = (enclosure == null ? 0 : enclosure.length());
              int len_esc = (escapeCharacter == null ? 0 : escapeCharacter.length());

      while (pos < length)
      {
        int from = pos;
        int next;

        boolean encl_found;
        boolean contains_escaped_enclosures = false;
        boolean contains_escaped_separators = false;

        // Is the field beginning with an enclosure?
        // "aa;aa";123;"aaa-aaa";000;...
        if (len_encl > 0 && line.substring(from, from + len_encl).equalsIgnoreCase(enclosure))
        {
                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "CsvInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "CsvInput.Log.ConvertLineToRow",line.substring(from, from + len_encl))); //$NON-NLS-1$ //$NON-NLS-2$
          encl_found = true;
          int p = from + len_encl;

          boolean is_enclosure = len_encl > 0 && p + len_encl < length
              && line.substring(p, p + len_encl).equalsIgnoreCase(enclosure);
          boolean is_escape = len_esc > 0 && p + len_esc < length
              && line.substring(p, p + len_esc).equalsIgnoreCase(escapeCharacter);

          boolean enclosure_after = false;
          
          // Is it really an enclosure? See if it's not repeated twice or escaped!
          if ((is_enclosure || is_escape) && p < length - 1) 
          {
            String strnext = line.substring(p + len_encl, p + 2 * len_encl);
            if (strnext.equalsIgnoreCase(enclosure))
            {
              p++;
              enclosure_after = true;
              dencl = true;

              // Remember to replace them later on!
              if (is_escape) contains_escaped_enclosures = true; 
            }
          }

          // Look for a closing enclosure!
          while ((!is_enclosure || enclosure_after) && p < line.length())
          {
            p++;
            enclosure_after = false;
            is_enclosure = len_encl > 0 && p + len_encl < length && line.substring(p, p + len_encl).equals(enclosure);
            is_escape = len_esc > 0 && p + len_esc < length && line.substring(p, p + len_esc).equals(escapeCharacter);

            // Is it really an enclosure? See if it's not repeated twice or escaped!
            if ((is_enclosure || is_escape) && p < length - 1) // Is
            {
              String strnext = line.substring(p + len_encl, p + 2 * len_encl);
              if (strnext.equals(enclosure))
              {
                p++;
                enclosure_after = true;
                dencl = true;

                // Remember to replace them later on!
                if (is_escape) contains_escaped_enclosures = true; // remember
              }
            }
          }

          if (p >= length) next = p;
          else next = p + len_encl;

                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "CsvInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "CsvInput.Log.EndOfEnclosure", ""+ p)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else
        {
          encl_found = false;
          boolean found = false;
          int startpoint = from;
          int tries = 1;
          do
          {
            next = line.indexOf(delimiter, startpoint); 

            // See if this position is preceded by an escape character.
            if (len_esc > 0 && next - len_esc > 0)
            {
              String before = line.substring(next - len_esc, next);

              if (escapeCharacter != null && escapeCharacter.equals(before))
              {
                // take the next separator, this one is escaped...
                startpoint = next + 1; 
                tries++;
                contains_escaped_separators = true;
              }
              else
              {
                found = true;
              }
            }
            else
            {
              found = true;
            }
          }
          while (!found && next >= 0);
        }
        if (next == -1) next = length;

        if (encl_found)
        {
          pol = line.substring(from + len_encl, next - len_encl);
                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "CsvInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "CsvInput.Log.EnclosureFieldFound", ""+ pol)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else
        {
          pol = line.substring(from, next);
                      if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "CsvInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "CsvInput.Log.NormalFieldFound",""+ pol)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (dencl)
        {
          StringBuilder sbpol = new StringBuilder(pol);
          int idx = sbpol.indexOf(enclosure + enclosure);
          while (idx >= 0)
          {
            sbpol.delete(idx, idx + (enclosure == null ? 0 : enclosure.length()) );
            idx = sbpol.indexOf(enclosure + enclosure);
          }
          pol = sbpol.toString();
        }

        //  replace the escaped enclosures with enclosures... 
        if (contains_escaped_enclosures) 
        {
          String replace = escapeCharacter + enclosure;
          String replaceWith = enclosure;

          pol = Const.replace(pol, replace, replaceWith);
        }

        //replace the escaped separators with separators...
        if (contains_escaped_separators) 
        {
          String replace = escapeCharacter + delimiter;
          String replaceWith = delimiter;
          
          pol = Const.replace(pol, replace, replaceWith);
        }

        // Now add pol to the strings found!
        strings.add(pol);

        pos = next + delimiter.length();
        fieldnr++;
      }
      if ( pos == length )
      {
        if (log.isRowLevel()) log.logRowlevel(BaseMessages.getString(PKG, "CsvInput.Log.ConvertLineToRowTitle"), BaseMessages.getString(PKG, "CsvInput.Log.EndOfEmptyLineFound")); //$NON-NLS-1$ //$NON-NLS-2$
        strings.add(""); //$NON-NLS-1$
                  fieldnr++;
      }
    }
    catch (Exception e)
    {
      throw new KettleException(BaseMessages.getString(PKG, "CsvInput.Log.Error.ErrorConvertingLine",e.toString()), e); //$NON-NLS-1$
    }

    return strings.toArray(new String[strings.size()]);
  }

}