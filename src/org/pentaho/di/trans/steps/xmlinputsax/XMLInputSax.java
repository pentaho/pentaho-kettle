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

package org.pentaho.di.trans.steps.xmlinputsax;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

// import org.w3c.dom.Node;

// import be.ibridge.kettle.core.Const;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or
 * more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class XMLInputSax extends BaseStep implements StepInterface
{
	private XMLInputSaxMeta meta;

	private XMLInputSaxData data;

	public XMLInputSax(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] row = getRowFromXML();
		if (row.length == 0)
		{
			setOutputDone(); // signal end to receiver(s)
			return false; // This is the end of this step.
		}

		if (log.isRowLevel())
			logRowlevel("Read row: " + row.toString());
		data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null);
		putRow(data.outputRowMeta, row);

		if (meta.getRowLimit() > 0 && data.rownr >= meta.getRowLimit()) // limit
																		// has
																		// been
																		// reached:
																		// stop
																		// now.
		{
			setOutputDone();
			return false;
		}

		return true;
	}

	private Object[] getRowFromXML() throws KettleValueException
	{
		Object[] row = new Object[0];

		if (data.document == null) // finished reading the file, read the next
									// file!
		{
			data.filename = null;
		} else if (!data.document.hasNext())
		{
			data.filename = null;
		}

		// First, see if we need to open a new file
		if (data.filename == null)
		{
			if (!openNextFile())
			{
				return null;
			}
		}

		row = data.document.getNext();

		// Node itemNode = XMLHandler.getSubNodeByNr(data.section,
		// data.itemElement, data.itemPosition);
		// data.itemPosition++;

		// See if we need to add the filename to the row...
		if (meta.includeFilename() && meta.getFilenameField() != null && meta.getFilenameField().length() > 0)
		{
			ValueMetaAndData fn = new ValueMetaAndData(meta.getFilenameField(), data.filename);
			row = RowDataUtil.addValueData(row, fn);
		}

		// See if we need to add the row number to the row...
		if (meta.includeRowNumber() && meta.getRowNumberField() != null
				&& meta.getRowNumberField().length() > 0)
		{
			ValueMetaAndData fn = new ValueMetaAndData(meta.getRowNumberField(), new Long(data.rownr));
			row = RowDataUtil.addValueData(row, fn);
		}

		System.arraycopy(row, 0, data.previousRow, 0, row.length); // copy it
																	// to make
																	// sure the
																	// next step
																	// doesn't
																	// change it
																	// in
																	// between...
		data.rownr++;

		return row;
	}

	private boolean openNextFile()
	{
		try
		{
			if (data.filenr >= data.files.length) // finished processing!
			{
				logDetailed("Finished processing files.");
				return false;
			}

			// Is this the last file?
			data.last_file = (data.filenr == data.files.length - 1);
			data.filename = data.files[data.filenr];

			logBasic("Opening file: " + data.filename);

			// Move file pointer ahead!
			data.filenr++;

			// Open the XML document
			data.document = new XMLInputSaxDataRetriever(data.filename, meta, data);
			data.document.runExample();

		} catch (Exception e)
		{
			logError("Couldn't open file #" + data.filenr + " : " + data.filename + " --> " + e.toString()
					+ e.getMessage() + e.getLocalizedMessage());
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLInputSaxMeta) smi;
		data = (XMLInputSaxData) sdi;

		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles(this);
			if (data.files == null || data.files.length == 0)
			{
				logError("No file(s) specified! Stop processing.");
				return false;
			}

			data.rownr = 1L;

			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLInputSaxMeta) smi;
		data = (XMLInputSaxData) sdi;

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