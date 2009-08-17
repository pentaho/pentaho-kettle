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
 

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.*;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.util.*;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;


/**
 * Converts input rows to text and then writes this text to one or more files.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileOutput extends BaseStep implements StepInterface
{
    private static final String FILE_COMPRESSION_TYPE_ZIP  = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_ZIP];
    private static final String FILE_COMPRESSION_TYPE_GZIP = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_GZIP];
    
	private TextFileOutputMeta meta;
	private TextFileOutputData data;
	 
	public TextFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (TextFileOutputMeta) smi;
		data = (TextFileOutputData) sdi;

		boolean result = true;
		Object[] r = getRow(); // This also waits for a row to be finished.

		if (r != null && first) {
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// This happens if init() opened a file that should have headers but we didn't have an outputRowMeta yet.
			if (data.delayedHeaderWrite)
			{
			    writeFieldNames();
			}

			// if file name in field is enabled then set field name and open file
			if (meta.isFileNameInField()) {

				// find and set index of file name field in input stream
				data.fileNameFieldIndex = getInputRowMeta().indexOfValue(meta.getFileNameField());

				// set the file name for this row
				if (data.fileNameFieldIndex < 0) {
					throw new KettleStepException(Messages.getString("TextFileOutput.Exception.FileNameFieldNotFound", meta.getFileNameField())); // $NON-NLS-1$
				}

				data.fileNameMeta = getInputRowMeta().getValueMeta(data.fileNameFieldIndex);
			} 
			else if (meta.isDoNotOpenNewFileInit()) {
				openNewFile(meta.getFileName());
				data.oneFileOpened = true;
				initBinaryDataFields();
			}

			if (meta.getOutputFields()!=null)
			{
			    data.fieldnrs = new int[meta.getOutputFields().length];
			    for (int i = 0; i < meta.getOutputFields().length; i++) {
			        data.fieldnrs[i] = data.outputRowMeta.indexOfValue(meta.getOutputFields()[i].getName());
			        if (data.fieldnrs[i] < 0) {
			            throw new KettleStepException("Field [" + meta.getOutputFields()[i].getName() + "] couldn't be found in the input stream!");
			        }
			    }
			}
		}

		if (r != null)
		{
	        if (meta.isFileNameInField()) {
	            setOutputMetaForFilename(data.fileNameMeta.getString(r[data.fileNameFieldIndex]));
	        }

		    if (needToSplitFile())
	        {
	            closeFile();
	            data.outputMeta.splitnr++;
	            openNewFile(meta.getFileName());
	        }
		}
		else  // no more input to be expected...
		{
            setOutputDone();
            return false;
		}

		writeRowToFile(data.outputRowMeta, r);
		putRow(data.outputRowMeta, r); // in case we want it to go further...

		if (checkFeedback(getLinesOutput()))
			logBasic("linenr " + getLinesOutput());

		return result;
	}
	
	/**
	 * Used for calculating when to split files
	 * @return True if the next line should be output to a new split file.
	 */
	boolean needToSplitFile()
	{
		final long lines = getLinesOutput() - data.extraLinesWritten;
        final int splitEvery = meta.getSplitEvery();
        return data.isSplitting && lines > 0 &&
			(lines % splitEvery == 0);
	}

	/**
	 * This method should only be used when you have a filename in the input stream.
	 * 
	 * @param filename the filename to set the data.writer field for
	 * @throws KettleException 
	 */
	void setOutputMetaForFilename(String filename) throws KettleException {
		if (filename.equals(data.lastRowFileName)) return;

		try
		{
            data.lastRowFileName = filename;
			data.outputMeta = data.outputMetaMap.get(filename);
			if (data.outputMeta == null)
			{
				openNewFile(filename);
				data.outputMetaMap.put(filename, data.outputMeta);
			}
			else if (data.parentZipOutputMeta != null)
			{
				// This is a special case.  The user is trying to write multiple
				// files into a zip, but the filenames are not sorted.  Since this
				// is an unsupported case, stop the transformation with an error.
				logError("Filenames in row field are not sorted. This is not supported for multi file Zip writing.");
				setErrors(1L);
				stopAll();
			}

		}
		catch (NullPointerException e)
		{
			logError("Filename in row field was null");
			setErrors(1L);
			stopAll();
		}

	}

	void writeRowToFile(RowMetaInterface rowMeta, Object[] r) throws KettleStepException
	{
		try
		{   
			if (meta.getOutputFields()==null || meta.getOutputFields().length==0)
			{
				/*
				 * Write all values in stream to text file.
				 */
				for (int i=0;i<rowMeta.size();i++)
				{
					if (i>0 && data.binarySeparator.length>0)
					{
						data.outputMeta.writer.write(data.binarySeparator);
					}
					ValueMetaInterface v=rowMeta.getValueMeta(i);
                    Object valueData = r[i];
                    
                    // no special null value default was specified since no fields are specified at all
                    // As such, we pass null
                    //
					writeField(v, valueData, null); 
				}
				data.outputMeta.writer.write(data.binaryNewline);
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					if (i>0 && data.binarySeparator.length>0)
						data.outputMeta.writer.write(data.binarySeparator);

					ValueMetaInterface v = rowMeta.getValueMeta(data.fieldnrs[i]);
					Object valueData = r[data.fieldnrs[i]];
					writeField(v, valueData, data.binaryNullValue[i]);
				}
				data.outputMeta.writer.write(data.binaryNewline);
			}

			incrementLinesOutput();
            
            // flush every 4k lines
            // if (linesOutput>0 && (linesOutput&0xFFF)==0) data.writer.flush();
		}
		catch(Exception e)
		{
			throw new KettleStepException("Error writing line", e);
		}
	}

    byte[] formatField(ValueMetaInterface v, Object valueData) throws KettleValueException
    {
    	if( v.isString() )
    	{    	
    		String svalue = (valueData instanceof String)?(String)valueData:v.getString(valueData);
    		return convertStringToBinaryString(v,Const.trimToType(svalue, v.getTrimType()));
    	} 
    	else 
    	{
            return v.getBinaryString(valueData);
    	}
    }
    

    byte[] convertStringToBinaryString(ValueMetaInterface v, String string) throws KettleValueException
    {
    	int length = v.getLength();
    	   	
    	if (string==null) return new byte[] {};
    	
    	if( length > -1 && length < string.length() ) {
    		// we need to truncate
    		String tmp = string.substring(0, length);
            if (Const.isEmpty(v.getStringEncoding()))
            {
            	return tmp.getBytes();
            }
            else
            {
                try
                {
                	return tmp.getBytes(v.getStringEncoding());
                }
                catch(UnsupportedEncodingException e)
                {
                    throw new KettleValueException("Unable to convert String to Binary with specified string encoding ["+v.getStringEncoding()+"]", e);
                }
            }
    	}
    	else {
    		byte text[];
            if (Const.isEmpty(v.getStringEncoding()))
            {
            	text = string.getBytes();
            }
            else
            {
                try
                {
                	text = string.getBytes(v.getStringEncoding());
                }
                catch(UnsupportedEncodingException e)
                {
                    throw new KettleValueException("Unable to convert String to Binary with specified string encoding ["+v.getStringEncoding()+"]", e);
                }
            }
        	if( length > string.length() ) 
        	{
        		// we need to pad this
        		
        		// Also for PDI-170: not all encoding use single characters, so we need to cope
        		// with this.
        		byte filler[] = " ".getBytes();
        		int size = text.length + filler.length*(length - string.length());
        		byte bytes[] = new byte[size];
        		System.arraycopy( text, 0, bytes, 0, text.length );
        		if( filler.length == 1 ) {
            		java.util.Arrays.fill( bytes, text.length, size, filler[0] );
        		} 
        		else 
        		{
        			// need to copy the filler array in lots of times
        			// TODO: this was not finished.
        		}        		        		
        		return bytes;
        	}
        	else
        	{
        		// do not need to pad or truncate
        		return text;
        	}
    	}
    }
    
    byte[] getBinaryString(String string) throws KettleStepException {
    	try {
    		if (data.hasEncoding) {
        		return string.getBytes(meta.getEncoding());
    		}
    		else {
        		return string.getBytes();
    		}
    	}
    	catch(Exception e) {
    		throw new KettleStepException(e);
    	}
    }
    
    void writeField(ValueMetaInterface v, Object valueData, byte[] nullString) throws KettleStepException
    {
        try
        {
        	byte[] str;
        	
        	// First check whether or not we have a null string set
        	// These values should be set when a null value passes
        	//
        	if (nullString!=null && v.isNull(valueData)) {
        		str = nullString;
        	}
        	else {
	        	if (meta.isFastDump()) {
	        		if( valueData instanceof byte[] )
	        		{
	            		str = (byte[]) valueData;
	        		} else {
	       				str = getBinaryString((valueData == null) ? "" : valueData.toString());
	        		}
	        	}
	        	else {
	    			str = formatField(v, valueData);
	        	}
        	}
        	
    		if (str!=null && str.length>0)
    		{
    			List<Integer> enclosures = null;
    			
        		if (v.isString() && meta.isEnclosureForced() && !meta.isPadded())
        		{
					data.outputMeta.writer.write(data.binaryEnclosure);
        			
        			// Also check for the existence of the enclosure character.
        			// If needed we double (escape) the enclosure character.
        			//
        			enclosures = getEnclosurePositions(str);
        		}

        		if (enclosures == null) 
        		{
					data.outputMeta.writer.write(str);
        		}
        		else
        		{
        			// Skip the enclosures, double them instead...
        			int from=0;
        			for (int i=0;i<enclosures.size();i++)
        			{
        				int position = enclosures.get(i);
						data.outputMeta.writer.write(str, from, position + data.binaryEnclosure.length - from);
						data.outputMeta.writer.write(data.binaryEnclosure); // write enclosure a second time
        				from=position+data.binaryEnclosure.length;
        			}
        			if (from<str.length)
        			{
						data.outputMeta.writer.write(str, from, str.length-from);
        			}
        		}
        		
        		if (v.isString() && meta.isEnclosureForced() && !meta.isPadded())
        		{
					data.outputMeta.writer.write(data.binaryEnclosure);
        		}
    		}
        }
        catch(Exception e)
        {
            throw new KettleStepException("Error writing field content to file", e);
        }
    }

	List<Integer> getEnclosurePositions(byte[] str) {
		List<Integer> positions = null;
		if (data.binaryEnclosure!=null && data.binaryEnclosure.length>0)
		{
			for (int i=0;i<str.length - data.binaryEnclosure.length +1;i++)  //+1 because otherwise we will not find it at the end
			{
				// verify if on position i there is an enclosure
				// 
				boolean found = true;
				for (int x=0;found && x<data.binaryEnclosure.length;x++)
				{
					if (str[i+x] != data.binaryEnclosure[x]) found=false;
				}
				if (found)
				{
					if (positions==null) positions=new ArrayList<Integer>();
					positions.add(i);
				}
			}
		}
		return positions;
	}

	void writeEndedLine()
	{
		try
		{
			String sLine = meta.getEndedLine();
			if (sLine!=null)
			{
				if (sLine.trim().length()>0)
				{
					data.outputMeta.writer.write(getBinaryString(sLine));
					data.outputMeta.writer.write(data.binaryNewline);
					incrementLinesOutput();
					data.extraLinesWritten++;
				}
			}
		}
		catch(Exception e)
		{
			logError("Error writing ended tag line: "+e.toString());
            logError(Const.getStackTracker(e));
		}
	}
	
	void writeFieldNames() throws KettleException
	{
		RowMetaInterface rowMeta = data.outputRowMeta;

		// If the file is opened in init, we don't have an outputRowMeta yet so we can't
		// write the headers.  Set a flag so we'll do it upon first processRow()
		data.delayedHeaderWrite = rowMeta == null;
		if (data.delayedHeaderWrite) return;

		try
		{
			// If we have fields specified: list them in this order!
			if (meta.getOutputFields()!=null && meta.getOutputFields().length>0)
			{
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
                    String fieldName = meta.getOutputFields()[i].getName();
                    ValueMetaInterface v = rowMeta.searchValueMeta(fieldName);
                    
					if (i>0 && data.binarySeparator.length>0)
					{
						data.outputMeta.writer.write(data.binarySeparator);
					}
					if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && v!=null && v.isString())
					{
						data.outputMeta.writer.write(data.binaryEnclosure);
					}
					data.outputMeta.writer.write(getBinaryString(fieldName));
					if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && v!=null && v.isString())
					{
						data.outputMeta.writer.write(data.binaryEnclosure);                    }
				}
				data.outputMeta.writer.write(data.binaryNewline);
			}
			else
			{
                /*
                 * Write all values in stream to text file.
                 */
                for (int i=0;i<rowMeta.size();i++)
                {
                    if (i>0 && data.binarySeparator.length>0)
                    {
                        data.outputMeta.writer.write(data.binarySeparator);
                    }
                    ValueMetaInterface valueMeta=rowMeta.getValueMeta(i);
                    if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && valueMeta.isString())
                    {
                        data.outputMeta.writer.write(data.binaryEnclosure);
                    }
                    data.outputMeta.writer.write(getBinaryString(valueMeta.getName()));
                    if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && valueMeta.isString())
                    {
                        data.outputMeta.writer.write(data.binaryEnclosure);
                    }
                }
                data.outputMeta.writer.write(data.binaryNewline);
			}
		}
		catch(Exception e)
		{
			logError("Error writing header line: "+e.toString());
			logError(Const.getStackTracker(e));
		}
		incrementLinesOutput();
		data.extraLinesWritten++;
	}

	public String buildFilename(String filename, boolean ziparchive)
	{
		return TextFileOutputMeta.buildFilename(filename, meta.getExtension(), this, getCopy(), getPartitionID(), data.outputMeta.splitnr, ziparchive, meta);
	}
	
	public void openNewFile(String baseFilename) throws KettleException
	{
	    if (data.outputMeta == null)
	        data.outputMeta = new TextFileOutputData.OutputMeta();
	    
		data.outputMeta.writer = null;

		boolean fileAlreadyExisted = false;
		data.outputMeta.name = buildFilename(environmentSubstitute(baseFilename), false);

		try
		{
			if (meta.isFileAsCommand())
			{
				if(log.isDebug()) logDebug("Spawning external process");
				if (data.outputMeta.cmdProc != null)
				{
					logError("Previous command not correctly terminated");
					setErrors(1L);
				}
				String cmdstr = environmentSubstitute(meta.getFileName());
				if (Const.getOS().equals("Windows 95"))
				{
					cmdstr = "command.com /C " + cmdstr;
				}
				else
				{
					if (Const.getOS().startsWith("Windows"))
					{
						cmdstr = "cmd.exe /C " + cmdstr;
					}
				}
				if(log.isDetailed()) logDetailed("Starting: " + cmdstr);
				Runtime r = Runtime.getRuntime();
				data.outputMeta.cmdProc = r.exec(cmdstr, EnvUtil.getEnvironmentVariablesForRuntimeExec());
				data.outputMeta.writer = data.outputMeta.cmdProc.getOutputStream();
				StreamLogger stdoutLogger = new StreamLogger( data.outputMeta.cmdProc.getInputStream(), "(stdout)" );
				StreamLogger stderrLogger = new StreamLogger( data.outputMeta.cmdProc.getErrorStream(), "(stderr)" );
				new Thread(stdoutLogger).start();
				new Thread(stderrLogger).start();
			}
			else
			{
				OutputStream outputStream;

				if (FILE_COMPRESSION_TYPE_ZIP.equals(meta.getFileCompression()))
				{
					if (data.parentZipOutputMeta == null)
					{
						if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in zipped mode");

						// Even if the zip existed, we have to create a new file inside it so set this to false.
						fileAlreadyExisted = false;

						data.parentZipOutputMeta = new TextFileOutputData.OutputMeta();
						if (meta.isFileNameInField())
						{
							// Even if we are using filename in field, if we have a zip file,
							// use the main filename as the parent zip filename.
							data.parentZipOutputMeta.name = buildFilename(environmentSubstitute(meta.getFileName()), true);
						}
						else
						{
							data.parentZipOutputMeta.name = buildFilename(environmentSubstitute(baseFilename), true);
						}

						data.parentZipOutputStream = new ZipOutputStream(
								KettleVFS.getOutputStream(
										KettleVFS.getFileObject(data.parentZipOutputMeta.name),
										meta.isFileAppended()));
						outputStream = data.parentZipOutputStream;
					}
					else
					{
						// The zip file already has an entry in it so we should flush
						// the buffered output stream before calling setNextEntry();
						data.parentZipOutputMeta.writer.flush();
						
						// In this case, this variable is unused because we already have a buffered
						// writer stored in data.parentZipMeta so we are setting to null to enforce this assumption.
						outputStream = null;
					}

					ZipEntry zipEntry = new ZipEntry(data.outputMeta.name);
					zipEntry.setComment("Compressed by Kettle");
					data.parentZipOutputStream.putNextEntry(zipEntry);
				}
				else
				{
					// If we ever have code to intermediately close files before we are done
					// with them, then we'll need code to track whether it was previously opened or not.
					FileObject f = KettleVFS.getFileObject(data.outputMeta.name);
					fileAlreadyExisted = f.exists();
					outputStream = KettleVFS.getOutputStream(f, meta.isFileAppended());
					
					if (FILE_COMPRESSION_TYPE_GZIP.equals(meta.getFileCompression()))
					{
						if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in gzipped mode");
						outputStream = new GZIPOutputStream(outputStream);
					}
					else
					{
						if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in nocompress mode");
					}
				}

				if (data.hasEncoding)
				{
					if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in encoding: "+meta.getEncoding());
				}
				else
				{
					if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in default encoding");
				}

				
				if (FILE_COMPRESSION_TYPE_ZIP.equals(meta.getFileCompression()))
				{
					if (data.parentZipOutputMeta.writer == null)
					{
						data.parentZipOutputMeta.writer = new BufferedOutputStream(outputStream, 5000);
					}
					
					data.outputMeta.writer = data.parentZipOutputMeta.writer;
				}
				else
				{
					data.outputMeta.writer = new BufferedOutputStream(outputStream, 5000);
				}
				
				if(log.isDetailed()) logDetailed("Opened new file with name ["+data.outputMeta.name+"]");
				
				if (meta.isAddToResultFiles())
				{
					ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(data.outputMeta.name), getTransMeta().getName(), getStepname());
					resultFile.setComment("This file was created with a text file output step");
					addResultFile(resultFile);
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Error opening new file : "+e.toString());
		}
		
		// If the user selected "Append" and the file already existed, we don't want to write a header.
		if (meta.isHeaderEnabled() && !(fileAlreadyExisted && meta.isFileAppended()))
			writeFieldNames();
		
		// Record the number of files opened in the Updated statistic
		incrementLinesUpdated();
	}

	void closeFile()
	{
		if (data.outputMeta.writer == null) return;
		
		try
		{
			if (!meta.isFileAppended())
			{
				if (meta.isFooterEnabled())
					writeFieldNames();

				writeEndedLine();
			}

			if(log.isDebug()) logDebug("Closing output stream: "+data.outputMeta.name);
			data.outputMeta.writer.close();
			if(log.isDebug()) logDebug("Closed output stream: "+data.outputMeta.name);

			data.outputMeta.writer = null;
			
			// Note that multiple files in a zip don't get closed.
			// Removing these references ensures that if we are creating multiple zip files,
			// the next openNewFile() will create the zip properly.
			data.parentZipOutputMeta = null;
			data.parentZipOutputStream = null;

			if (data.outputMeta.cmdProc != null)
			{
				if(log.isDebug()) logDebug("Waiting for external command to terminate: "+data.outputMeta.name);
				int procStatus = data.outputMeta.cmdProc.waitFor();

				// close the logging streams
				// otherwise you get "Too many open files, java.io.IOException" after a lot of iterations
				try {
					data.outputMeta.cmdProc.getErrorStream().close();
					data.outputMeta.cmdProc.getInputStream().close();
				} catch (IOException e) {
					if(log.isDetailed()) logDetailed("Warning: Error closing external command stream ("+data.outputMeta.name +"): " + e.getMessage());
				}               
				data.outputMeta.cmdProc = null;
				if(log.isBasic() && procStatus != 0) logBasic("External command exit status ("+data.outputMeta.name +"): " + procStatus);
			}
		}
		catch(Exception e)
		{
			logError("Exception trying to close file ("+data.outputMeta.name +"): " + e.toString());
			setErrors(1);
		}
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		if (super.init(smi, sdi))
		{
			// Otherwise, file will be opened on first record processed
			if(!meta.isDoNotOpenNewFileInit() && !meta.isFileNameInField())
			{
				try
				{
					openNewFile(meta.getFileName());
					data.oneFileOpened=true;
				}	
				catch(Exception e)
				{
					logError("Couldn't open file "+meta.getFileName(), e);
					setErrors(1L);
					stopAll();
					return false;
				}
			}

			try {
				initBinaryDataFields();
			} catch(Exception e)
			{
				logError("Couldn't initialize binary data fields", e);
				setErrors(1L);
				stopAll();
				return false;
			}

			data.isSplitting = meta.getSplitEvery() > 0;
			
			return true;
		}
	
		return false;
	}
	
	void initBinaryDataFields() throws KettleException
	{
		try {
			data.hasEncoding = !Const.isEmpty(meta.getEncoding());
			data.binarySeparator = new byte[] {};
			data.binaryEnclosure = new byte[] {};
			data.binaryNewline   = new byte[] {};
			
			if (data.hasEncoding) {
				if (!Const.isEmpty(meta.getSeparator())) data.binarySeparator= environmentSubstitute(meta.getSeparator()).getBytes(meta.getEncoding());
				if (!Const.isEmpty(meta.getEnclosure())) data.binaryEnclosure = environmentSubstitute(meta.getEnclosure()).getBytes(meta.getEncoding());
				if (!Const.isEmpty(meta.getNewline()))   data.binaryNewline   = meta.getNewline().getBytes(meta.getEncoding());
			}
			else {
				if (!Const.isEmpty(meta.getSeparator())) data.binarySeparator= environmentSubstitute(meta.getSeparator()).getBytes();
				if (!Const.isEmpty(meta.getEnclosure())) data.binaryEnclosure = environmentSubstitute(meta.getEnclosure()).getBytes();
				if (!Const.isEmpty(meta.getNewline()))   data.binaryNewline   = environmentSubstitute(meta.getNewline()).getBytes();
			}
			
			if (meta.getOutputFields() != null)
			{
			    data.binaryNullValue = new byte[meta.getOutputFields().length][];
			    for (int i=0;i<meta.getOutputFields().length;i++)
			    {
			        data.binaryNullValue[i] = null;
			        String nullString = meta.getOutputFields()[i].getNullString();
			        if (!Const.isEmpty(nullString)) 
			        {
			            if (data.hasEncoding)
			            {
			                data.binaryNullValue[i] = nullString.getBytes(meta.getEncoding());
			            }
			            else
			            {
			                data.binaryNullValue[i] = nullString.getBytes();
			            }
			        }
			    }
			}
		}
		catch(Exception e) {
			throw new KettleException("Unexpected error while encoding binary fields", e);
		}
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		if (data.oneFileOpened)
		{
			closeFile();
		}
		else
		{
			for (TextFileOutputData.OutputMeta outputMeta : data.outputMetaMap.values())
			{
				data.outputMeta = outputMeta;
				closeFile();
			}
		}
		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}	
}
