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

package be.ibridge.kettle.trans.step.getfilenames;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.fileinput.FileInputList;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or
 * more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class GetFileNames extends BaseStep implements StepInterface
{
	private GetFileNamesMeta meta;

	private GetFileNamesData data;

	public GetFileNames(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public static final String getLine(LogWriter log, InputStreamReader reader, String format) throws KettleFileException
	{
		StringBuffer line = new StringBuffer(256);
		int c = 0;

		try
		{
			while (c >= 0)
			{
				c = reader.read();
				if (c == '\n' || c == '\r')
				{
					if (format.equalsIgnoreCase("DOS")) 
					{
						c = reader.read(); // skip \n and \r
					     if( c != '\r' && c != '\n' ) 
					     { 
					       // make sure its really a linefeed or cariage return
					       // raise an error this is not a DOS file 
					       // so we have pulled a character from the next line
					       throw new KettleFileException("DOS format was specified but only a single line feed character was found, not 2");
					     }
					}
					return line.toString();
				}
				if (c >= 0) line.append((char) c);
			}
		}
		catch(KettleFileException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			if (line.length() == 0)
			{
				log.logError("get line", "Exception reading line: " + e.toString());
				return null;
			}
			return line.toString();
		}
		if (line.length() > 0) return line.toString();

		return null;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (data.filenr>=data.files.nrOfFiles())
		{
			setOutputDone();
			return false;
		}
		
		Row r = new Row();
		
		File file = data.files.getFile(data.filenr);
		Value v = new Value("filename", file.getAbsolutePath());
		v.setLength(500,-1);
		r.addValue(v);
		
		data.filenr++;
		
		putRow(r);
		
		if ((linesInput > 0) && (linesInput % Const.ROWS_UPDATE) == 0) logBasic("linenr " + linesInput);

		return true;
	}

	private void handleMissingFiles() throws KettleException
	{
		debug = "Required files";
		List nonExistantFiles = data.files.getNonExistantFiles();

		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			logBasic("ERROR: Missing " + message);
			throw new KettleException("Following required files are missing: " + message);
		}

		List nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			logBasic("WARNING: Not accessible " + message);
			throw new KettleException("Following required files are not accessible: " + message);
		}
		debug = "End of Required files";
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GetFileNamesMeta) smi;
		data = (GetFileNamesData) sdi;

		if (super.init(smi, sdi))
		{
			try
			{
				data.files = meta.getTextFileList();
				handleMissingFiles();
	
				return true;
			}
			catch(Exception e)
			{
				logError("Error initializing step: "+e.toString());
				logError(Const.getStackTracker(e));
				return false;
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GetFileNamesMeta) smi;
		data = (GetFileNamesData) sdi;

		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	//
	//
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
			logError("Unexpected error in '" + debug + "' : " + e.toString());
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
