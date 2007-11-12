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

			// The conversion logic for when the lazy conversion is turned of is simple:
			// Pretend it's a lazy conversion object anyway and get the native type during conversion.
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList())
			{
				valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			}
						
			if (meta.isHeaderPresent()) {
				readOneRow(false); // skip this row.
			}
		}
		
		Object[] outputRowData=readOneRow(true);    // get row, set busy!
		if (outputRowData==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		putRow(data.outputRowMeta, outputRowData);     // copy row to possible alternate rowset(s).

        if (checkFeedback(linesInput)) logBasic(Messages.getString("CsvInput.Log.LineNumber", Long.toString(linesInput))); //$NON-NLS-1$
			
		return true;
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
			while (!newLineFound) {
				
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
			try {
				data.preferredBufferSize = Integer.parseInt(environmentSubstitute(meta.getBufferSize()));
				data.filename = environmentSubstitute(meta.getFilename());
				
				FileObject fileObject = KettleVFS.getFileObject(data.filename);
				if (!(fileObject instanceof LocalFile)) {
					// We can only use NIO on local files at the moment, so that's what we limit ourselves to.
					//
					logError(Messages.getString("CsvInput.Log.OnlyLocalFilesAreSupported"));
					return false;
				}
				
				FileInputStream fis = (FileInputStream)((LocalFile)fileObject).getInputStream();
				data.fc = fis.getChannel();
				data.bb = ByteBuffer.allocateDirect( data.preferredBufferSize );
				
				data.delimiter = environmentSubstitute(meta.getDelimiter()).getBytes();

				if( meta.getEnclosure() != null ) {
					data.enclosure = environmentSubstitute(meta.getEnclosure()).getBytes();
				} else {
					data.enclosure = null;
				}
				
				return true;
			} catch (IOException e) {
				logError("Error opening file '"+meta.getFilename()+"' : "+e.toString());
				logError(Const.getStackTracker(e));
			}
		}
		return false;
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		
		try {
			if (data.fc!=null) {
				data.fc.close();
			}
		} catch (IOException e) {
			logError("Unable to close file channel for file '"+meta.getFilename()+"' : "+e.toString());
			logError(Const.getStackTracker(e));
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