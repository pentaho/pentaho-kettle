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
 

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Converts input rows to text and then writes this text to one or more files.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileOutput extends BaseStep implements StepInterface
{
    private static final String FILE_COMPRESSION_TYPE_NONE = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_NONE];
    private static final String FILE_COMPRESSION_TYPE_ZIP  = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_ZIP];
    private static final String FILE_COMPRESSION_TYPE_GZIP = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_GZIP];
    
	private TextFileOutputMeta meta;
	private TextFileOutputData data;
	 
	public TextFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		boolean result=true;
		boolean bEndedLineWrote=false;
		Object[] r=getRow();       // This also waits for a row to be finished.

        if (r!=null && first)
        {
            first=false;
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null);
            
            if (!meta.isFileAppended() && ( meta.isHeaderEnabled() || meta.isFooterEnabled())) // See if we have to write a header-line)
            {
                if (meta.isHeaderEnabled() && data.outputRowMeta!=null)
                {
                    writeHeader();
                }
            }
            
            data.fieldnrs=new int[meta.getOutputFields().length];
            for (int i=0;i<meta.getOutputFields().length;i++)
            {
                data.fieldnrs[i]=data.outputRowMeta.indexOfValue(meta.getOutputFields()[i].getName());
                if (data.fieldnrs[i]<0)
                {
                    throw new KettleStepException("Field ["+meta.getOutputFields()[i].getName()+"] couldn't be found in the input stream!");
                }
            }
        }

		if ( ( r==null && data.outputRowMeta!=null && meta.isFooterEnabled() ) ||
		     ( r!=null && linesOutput>0 && meta.getSplitEvery()>0 && ((linesOutput+1)%meta.getSplitEvery())==0)
		   )
		{
			if (data.outputRowMeta!=null) 
			{
			   if ( meta.isFooterEnabled() )
			   {
			      writeHeader();
			   }
			}
			
			if (r==null)
			{
				//add tag to last line if needed
				writeEndedLine();
				bEndedLineWrote=true;
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

				if (meta.isHeaderEnabled() && data.outputRowMeta!=null) if (writeHeader()) linesOutput++;
			}
		}
		
		if (r==null)  // no more input to be expected...
		{
			if (false==bEndedLineWrote)
			{
				//add tag to last line if needed
				writeEndedLine();
				bEndedLineWrote=true;
			}
			
			setOutputDone();
			return false;
		}
		
		writeRowToFile(data.outputRowMeta, r);
		putRow(data.outputRowMeta, r);       // in case we want it to go further...
		
        if (checkFeedback(linesOutput)) logBasic("linenr "+linesOutput);
		
		return result;
	}

	private void writeRowToFile(RowMetaInterface rowMeta, Object[] r) throws KettleStepException
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
					if (i>0 && meta.getSeparator()!=null && meta.getSeparator().length()>0)
                    {
						data.writer.write(meta.getSeparator().toCharArray());
                    }
					ValueMetaInterface v=rowMeta.getValueMeta(i);
                    Object valueData = r[i];
                    
					writeField(v, valueData);
				}
                data.writer.write(meta.getNewline().toCharArray());
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					if (i>0 && meta.getSeparator()!=null && meta.getSeparator().length()>0)
						data.writer.write(meta.getSeparator().toCharArray());
	
					ValueMetaInterface v = rowMeta.getValueMeta(data.fieldnrs[i]);
					Object valueData = r[i];
					writeField(v, valueData);
				}
                data.writer.write(meta.getNewline().toCharArray());
			}

            linesOutput++;
            
            // flush every 4k lines
            // if (linesOutput>0 && (linesOutput&0xFFF)==0) data.writer.flush();
		}
		catch(Exception e)
		{
			throw new KettleStepException("Error writing line", e);
		}
	}

    private String formatField(ValueMetaInterface v, Object valueData) throws KettleValueException
    {
        return v.getString(valueData);
    }
    
    private void writeField(ValueMetaInterface v, Object valueData) throws KettleStepException
    {
        try
        {
            String str = meta.isFastDump() ? valueData.toString() : formatField(v, valueData);
            if (str!=null) data.writer.write(str.toCharArray());
        }
        catch(Exception e)
        {
            throw new KettleStepException("Error writing field content to file", e);
        }
    }

	private boolean writeEndedLine()
	{
		boolean retval=false;
		try
		{
			String sLine = meta.getEndedLine();
			if (sLine!=null)
			{
				if (sLine.trim().length()>0)
				{
					data.writer.write(sLine.toCharArray());
					linesOutput++;
				}
			}
		}
		catch(Exception e)
		{
			logError("Error writing ended tag line: "+e.toString());
            logError(Const.getStackTracker(e));
			retval=true;
		}
		
		return retval;
	}
	
	private boolean writeHeader()
	{
		boolean retval=false;
		RowMetaInterface r=data.outputRowMeta;
		
		try
		{
			// If we have fields specified: list them in this order!
			if (meta.getOutputFields()!=null && meta.getOutputFields().length>0)
			{
				String header = "";
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
                    String fieldName = meta.getOutputFields()[i].getName();
                    ValueMetaInterface v = r.searchValueMeta(fieldName);
                    
					if (i>0 && meta.getSeparator()!=null && meta.getSeparator().length()>0)
					{
						header+=meta.getSeparator();
					}
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v!=null && v.isString())
                    {
                        header+=meta.getEnclosure();
                    }
					header+=fieldName;
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v!=null && v.isString())
                    {
                        header+=meta.getEnclosure();
                    }
				}
				header+=meta.getNewline();
                data.writer.write(header.toCharArray());
			}
			else
			if (r!=null)  // Just put all field names in the header/footer
			{
				for (int i=0;i<r.size();i++)
				{
					if (i>0 && meta.getSeparator()!=null && meta.getSeparator().length()>0)
						data.writer.write(meta.getSeparator().toCharArray());
					ValueMetaInterface v = r.getValueMeta(i);
					
                    // Header-value contains the name of the value
					ValueMetaInterface header_value = new ValueMeta(v.getName(), ValueMetaInterface.TYPE_STRING);

                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v.isString())
                    {
                        data.writer.write(meta.getEnclosure().toCharArray());
                    }
                    data.writer.write(header_value.getName().toCharArray());
                    if (meta.isEnclosureForced() && meta.getEnclosure()!=null && v.isString())
                    {
                        data.writer.write(meta.getEnclosure().toCharArray());
                    }
				}
                data.writer.write(meta.getNewline().toCharArray());
			}
			else
			{
                data.writer.write(("no rows selected"+Const.CR).toCharArray());
			}
		}
		catch(Exception e)
		{
			logError("Error writing header line: "+e.toString());
            logError(Const.getStackTracker(e));
			retval=true;
		}
		linesOutput++;
		return retval;
	}

	public String buildFilename(boolean ziparchive)
	{
		return meta.buildFilename(getCopy(), getPartitionID(), data.splitnr, ziparchive);
	}
	
	public boolean openNewFile()
	{
		boolean retval=false;
		data.writer=null;
		
		try
		{
            if (meta.isFileAsCommand())
            {
            	logDebug("Spawning external process");
            	if (data.cmdProc != null)
            	{
            		logError("Previous command not correctly terminated");
            		setErrors(1);
            	}
            	String cmdstr = StringUtil.environmentSubstitute(meta.getFileName());
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
            	logDetailed("Starting: " + cmdstr);
            	Runtime r = Runtime.getRuntime();
            	data.cmdProc = r.exec(cmdstr, EnvUtil.getEnvironmentVariablesForRuntimeExec());
            	data.writer = new OutputStreamWriter(data.cmdProc.getOutputStream());
            	StreamLogger stdoutLogger = new StreamLogger( data.cmdProc.getInputStream(), "(stdout)" );
            	StreamLogger stderrLogger = new StreamLogger( data.cmdProc.getErrorStream(), "(stderr)" );
            	new Thread(stdoutLogger).start();
            	new Thread(stderrLogger).start();
            	retval = true;
            }
            else
            {
                String filename = buildFilename(true);
                
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename), getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with a text file output step");
	            addResultFile(resultFile);
	
	            OutputStream outputStream;
                
                if (!Const.isEmpty(meta.getFileCompression()) && !meta.getFileCompression().equals(FILE_COMPRESSION_TYPE_NONE))
                {
    				if (meta.getFileCompression().equals(FILE_COMPRESSION_TYPE_ZIP))
    				{
    		            log.logDetailed(toString(), "Opening output stream in zipped mode");
                        
                        data.fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
                        data.zip = new ZipOutputStream(data.fos);
    					File entry = new File(buildFilename(false));
    					ZipEntry zipentry = new ZipEntry(entry.getName());
    					zipentry.setComment("Compressed by Kettle");
    					data.zip.putNextEntry(zipentry);
    					outputStream=data.zip;
    				}
    				else if (meta.getFileCompression().equals(FILE_COMPRESSION_TYPE_GZIP))
    				{
    		            log.logDetailed(toString(), "Opening output stream in gzipped mode");
                        data.fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
                        data.gzip = new GZIPOutputStream(data.fos);
    					outputStream=data.gzip;
    				}
                    else
                    {
                        throw new KettleFileException("No compression method specified!");
                    }
                }
				else
				{
		            log.logDetailed(toString(), "Opening output stream in nocompress mode");
                    OutputStream fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
                    outputStream=fos;
				}
                
	            if (!Const.isEmpty(meta.getEncoding()))
	            {
	                log.logBasic(toString(), "Opening output stream in encoding: "+meta.getEncoding());
	                data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000), meta.getEncoding());
	            }
	            else
	            {
	                log.logBasic(toString(), "Opening output stream in default encoding");
	                data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000));
	            }
	
	            logDetailed("Opened new file with name ["+filename+"]");
				
				retval=true;
            }
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
			logDebug("Closing output stream");
			data.writer.close();
			logDebug("Closed output stream");
			data.writer = null;
			if (data.cmdProc != null)
			{
				logDebug("Ending running external command");
				int procStatus = data.cmdProc.waitFor();
				data.cmdProc = null;
				logBasic("Command exit status: " + procStatus);
			}
			else
			{
				logDebug("Closing normal file ..");
				if (meta.getFileCompression() == "Zip")
				{
					//System.out.println("close zip entry ");
					data.zip.closeEntry();
					//System.out.println("finish file...");
					data.zip.finish();
					data.zip.close();
				}
				if (meta.getFileCompression() == "GZip")
				{
					data.gzip.finish();
				}
                if (data.fos!=null)
                {
                    data.fos.close();
                    data.fos=null;
                }
			}
			//System.out.println("Closed file...");

			retval=true;
		}
		catch(Exception e)
		{
			logError("Exception trying to close file: " + e.toString());
			setErrors(1);
			retval = false;
		}

		return retval;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		if (super.init(smi, sdi))
		{
			data.splitnr=0;
			
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
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;
		
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