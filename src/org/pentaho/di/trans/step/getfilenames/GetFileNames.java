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

package org.pentaho.di.trans.step.getfilenames;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

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

	public GetFileNames(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public static final String getLine(LogWriter log, InputStreamReader reader, String format)
			throws KettleFileException
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
						if (c != '\r' && c != '\n')
						{
							// make sure its really a linefeed or cariage return
							// raise an error this is not a DOS file
							// so we have pulled a character from the next line
							throw new KettleFileException(
									"DOS format was specified but only a single line feed character was found, not 2");
						}
					}
					return line.toString();
				}
				if (c >= 0)
					line.append((char) c);
			}
		} catch (KettleFileException e)
		{
			throw e;
		} catch (Exception e)
		{
			if (line.length() == 0)
			{
				log.logError("get line", "Exception reading line: " + e.toString());
				return null;
			}
			return line.toString();
		}
		if (line.length() > 0)
			return line.toString();

		return null;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{

		if (data.filenr >= data.files.nrOfFiles())
		{
			setOutputDone();
			return false;
		}

		try
		{
			Object[] r = new Object[13];

			FileObject file = data.files.getFile(data.filenr);

			if (meta.getFilterFileType().equals("all_files")
					|| (meta.getFilterFileType().equals("only_files") && file.getType() == FileType.FILE)
					|| meta.getFilterFileType().equals("only_folders") && file.getType() == FileType.FOLDER)
			{

				ValueMetaAndData filename = new ValueMetaAndData("filename", KettleVFS.getFilename(file));
				filename.setLength(500, -1);
				r[0] = filename;

				ValueMetaAndData short_filename = new ValueMetaAndData("short_filename", file.getName()
						.getBaseName());
				short_filename.setLength(500, -1);
				r[1] = short_filename;

				try
				{
					ValueMetaAndData path = new ValueMetaAndData("path", KettleVFS.getFilename(file
							.getParent()));
					path.setLength(500, -1);
					r[2] = path;

					ValueMetaAndData type = new ValueMetaAndData("type", file.getType().toString());
					type.setLength(500, -1);
					r[3] = type;

					ValueMetaAndData exists = new ValueMetaAndData("exists", new Boolean(file.exists()));
					r[4] = exists;

					ValueMetaAndData ishidden = new ValueMetaAndData("ishidden", new Boolean(file.isHidden()));
					r[5] = ishidden;

					ValueMetaAndData isreadable = new ValueMetaAndData("isreadable", new Boolean(file
							.isReadable()));
					r[6] = isreadable;
					
					ValueMetaAndData iswriteable = new ValueMetaAndData("iswriteable", new Boolean(file
							.isWriteable()));
					r[7] = iswriteable;

					Date ladate = new Date(file.getContent().getLastModifiedTime());
					ValueMetaAndData lastmodifiedtime = new ValueMetaAndData("lastmodifiedtime", ladate
							.toString());
					r[8] = lastmodifiedtime;

					ValueMetaAndData size = new ValueMetaAndData("size", "");

					if (file.getType().equals(FileType.FILE))
					{
						size = new ValueMetaAndData("size", new Long(file.getContent().getSize()));
					}

					r[9] = size;
				} catch (IOException e)
				{
					throw new KettleException(e);
				}

				ValueMetaAndData extension = new ValueMetaAndData("extension", file.getName().getExtension());
				extension.setLength(500, -1);
				r[10] = extension;

				ValueMetaAndData uri = new ValueMetaAndData("uri", file.getName().getURI());
				uri.setLength(500, -1);
				r[11] = uri;

				ValueMetaAndData rooturi = new ValueMetaAndData("rooturi", file.getName().getRootURI());
				uri.setLength(500, -1);
				r[12] = rooturi;
				data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
				meta.getFields(data.outputRowMeta, getStepname(), null);
				putRow(data.outputRowMeta,r);
			}
		} catch (Exception e)
		{
			log.logError(toString(), "Error exception: " + e.getMessage());
		}

		data.filenr++;

		if ((linesInput > 0) && (linesInput % Const.ROWS_UPDATE) == 0)
			logBasic("linenr " + linesInput);

		return true;
	}

	private void handleMissingFiles() throws KettleException
	{
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
			} catch (Exception e)
			{
				logError("Error initializing step: " + e.toString());
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
		} catch (Exception e)
		{
			logError("Unexpected error : " + e.toString());
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}