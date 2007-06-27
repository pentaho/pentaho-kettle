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

package org.pentaho.di.trans.steps.xmlinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or
 * more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class XMLInput extends BaseStep implements StepInterface
{
	private XMLInputMeta meta;

	private XMLInputData data;

	public XMLInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{

		if (first) // we just got started
		{
			first = false;
			RowMetaInterface irow = getInputRowMeta();
			data.outputRowMeta = irow != null ? (RowMetaInterface)irow.clone() : new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null);
		}

		Object[] row = getRowFromXML();
		if (row == null)
		{
			setOutputDone(); // signal end to receiver(s)
			return false; // This is the end of this step.
		}

		if (log.isRowLevel())
			logRowlevel(Messages.getString("XMLInput.Log.ReadRow", row.toString()));

		linesInput++;

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
		while (data.itemPosition >= data.itemCount || data.file == null) // finished
		// reading
		// the
		// file,
		// read
		// the
		// next
		// file!
		{
			data.file = null;
			if (!openNextFile())
			{
				return null;
			}
		}

		Object[] row = buildEmptyRow();

		// Get the item in the XML file...

		// First get the appropriate node

		Node itemNode;
		if (meta.getInputPosition().length > 1)
		{
			itemNode = XMLHandler.getSubNodeByNr(data.section, data.itemElement, data.itemPosition);
		} else
		{
			itemNode = data.section; // Only the root node, 1 element to read
			// in the whole document.
		}
		data.itemPosition++;

		// Read from the Node...
		for (int i = 0; i < meta.getInputFields().length; i++)
		{
			Node node = itemNode;

			XMLInputField xmlInputField = meta.getInputFields()[i];

			String value = null;

			for (int p = 0; (value == null) && node != null && p < xmlInputField.getFieldPosition().length; p++)
			{
				XMLInputFieldPosition pos = xmlInputField.getFieldPosition()[p];

				switch (pos.getType())
				{
				case XMLInputFieldPosition.XML_ELEMENT:
				{
					if (pos.getElementNr() <= 1)
					{
						Node subNode = XMLHandler.getSubNode(node, pos.getName());
						if (subNode != null)
						{
							if (p == xmlInputField.getFieldPosition().length - 1) // last
							// level
							{
								value = XMLHandler.getNodeValue(subNode);
							}
						} else
						{
							if (log.isDebug())
								logDebug(Messages.getString("XMLInput.Log.UnableToFindPosition", pos
										.toString(), node.toString()));
						}
						node = subNode;
					} else
					// Multiple possible values: get number
					// pos.getElementNr()!
					{
						Node subNode = XMLHandler.getSubNodeByNr(node, pos.getName(), pos.getElementNr() - 1,
								false);
						if (subNode != null)
						{
							if (p == xmlInputField.getFieldPosition().length - 1) // last
							// level
							{
								value = XMLHandler.getNodeValue(subNode);
							}
						} else
						{
							if (log.isDebug())
								logDebug(Messages.getString("XMLInput.Log.UnableToFindPosition", pos
										.toString(), node.toString()));
						}
						node = subNode;
					}
				}
					break;

				case XMLInputFieldPosition.XML_ATTRIBUTE:
				{
					value = XMLHandler.getTagAttribute(node, pos.getName());
				}
					break;
				case XMLInputFieldPosition.XML_ROOT:
				{
					value = XMLHandler.getNodeValue(node);
				}
					break;
				default:
					break;
				}

			}

			// OK, we have the string...
			ValueMetaAndData v = new ValueMetaAndData(data.outputRowMeta.getValueMeta(i).getName(), value);

			// DO Trimming!
			switch (xmlInputField.getTrimType())
			{
			case XMLInputField.TYPE_TRIM_LEFT:
				v.setValueData(Const.ltrim(v.getValueData().toString()));
				break;
			case XMLInputField.TYPE_TRIM_RIGHT:
				v.setValueData(Const.rtrim(v.getValueData().toString()));
				break;
			case XMLInputField.TYPE_TRIM_BOTH:
				v.setValueData(v.getValueData().toString().trim());
				break;
			default:
				break;
			}

			// System.out.println("after trim, field #"+i+" : "+v);

			// DO CONVERSIONS...
			Object val = v.getValueData();
			String sval = val != null ? val.toString() : "";
			switch (xmlInputField.getType())
			{
			case ValueMeta.TYPE_STRING:
				// System.out.println("Convert value to String :"+v);
				break;
			case ValueMeta.TYPE_NUMBER:
				// System.out.println("Convert value to Number :"+v);
				if (xmlInputField.getFormat() != null && xmlInputField.getFormat().length() > 0)
				{
					if (xmlInputField.getDecimalSymbol() != null
							&& xmlInputField.getDecimalSymbol().length() > 0)
					{
						if (xmlInputField.getGroupSymbol() != null
								&& xmlInputField.getGroupSymbol().length() > 0)
						{
							if (xmlInputField.getCurrencySymbol() != null
									&& xmlInputField.getCurrencySymbol().length() > 0)
							{
								double dval = StringUtil.str2num(xmlInputField.getFormat(), xmlInputField
										.getDecimalSymbol(), xmlInputField.getGroupSymbol(), xmlInputField
										.getCurrencySymbol(), sval);
								v.setValueData(new Double(dval));
							} else
							{
								v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(),
										xmlInputField.getDecimalSymbol(), xmlInputField.getGroupSymbol(),
										null, sval)));
							}
						} else
						{
							v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(),
									xmlInputField.getDecimalSymbol(), null, null, sval)));
						}
					} else
					{
						v.setValueData(new Double(StringUtil.str2num(xmlInputField.getFormat(), null, null,
								null, sval))); // just a format mask
					}
				} else
				{
					v.setValueData(new Double(StringUtil.str2num(null, null, null, null, null)));
				}
				v.getValueMeta().setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
				break;
			case ValueMeta.TYPE_INTEGER:
				// System.out.println("Convert value to integer :"+v);
				v.setValueData(v.getValueData());
				v.getValueMeta().setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
				break;
			case ValueMeta.TYPE_BIGNUMBER:
				// System.out.println("Convert value to BigNumber :"+v);
				v.setValueData(v.getValueData());
				v.getValueMeta().setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
				break;
			case ValueMeta.TYPE_DATE:
				// System.out.println("Convert value to Date :"+v);

				if (xmlInputField.getFormat() != null && xmlInputField.getFormat().length() > 0)
				{

					v.setValueData(StringUtil.str2dat(xmlInputField.getFormat(), null, sval));
					v.getValueMeta().setType(ValueMeta.TYPE_DATE);
					v.getValueMeta().setLength(-1, -1);
				} else
				{
					v.setValueData(v.getValueData());
				}
				break;
			case ValueMeta.TYPE_BOOLEAN:
				v.setValueData(v.getValueData());
				break;
			default:
				break;
			}

			// Do we need to repeat this field if it is null?
			if (meta.getInputFields()[i].isRepeated())
			{
				if (v.getValueMeta().isNull(v.getValueData()) && data.previousRow != null)
				{
					v.setValueData(data.previousRow[i]);
				}
			}
			row[i]=v.getValueData();
		} // End of loop over fields...

		// See if we need to add the filename to the row...
		if (meta.includeFilename() && meta.getFilenameField() != null && meta.getFilenameField().length() > 0)
		{
			ValueMetaAndData fn = new ValueMetaAndData(meta.getFilenameField(), KettleVFS
					.getFilename(data.file));
			row = RowDataUtil.addValueData(row, fn.getValueMeta());
		}

		// See if we need to add the row number to the row...
		if (meta.includeRowNumber() && meta.getRowNumberField() != null
				&& meta.getRowNumberField().length() > 0)
		{
			ValueMetaAndData fn = new ValueMetaAndData(meta.getRowNumberField(), new Long(data.rownr));
			row = RowDataUtil.addValueData(row, fn.getValueMeta());
		}

		RowMetaInterface irow = getInputRowMeta();
		
		data.previousRow = irow==null?row:(Object[])irow.cloneRow(row); // copy it to make
		// sure the next
		// step doesn't
		// change it in
		// between...
		data.rownr++;

		// Throw away the information in the item?
		NodeList nodeList = itemNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
			itemNode.removeChild(nodeList.item(i));

		return row;
	}

	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */
	private Object[] buildEmptyRow()
	{
		return new Object[meta.getInputFields().length];
	}

	private boolean openNextFile()
	{
		try
		{
			if (data.filenr >= data.files.size()) // finished processing!
			{
				if (log.isDetailed())
					logDetailed(Messages.getString("XMLInput.Log.FinishedProcessing"));
				return false;
			}

			// Is this the last file?
			data.last_file = (data.filenr == data.files.size() - 1);
			data.file = (FileObject) data.files.get(data.filenr);

			logBasic(Messages.getString("XMLInput.Log.OpeningFile", data.file.toString()));

			// Move file pointer ahead!
			data.filenr++;

			// Open the XML document
			data.document = XMLHandler.loadXMLFile(data.file);

			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta()
					.getName(), getStepname());
			resultFile.setComment("File was read by an XML input step");
			addResultFile(resultFile);

			if (log.isDetailed())
				logDetailed(Messages.getString("XMLInput.Log.FileOpened", data.file.toString()));

			// Position in the file...
			data.section = data.document;

			for (int i = 0; i < meta.getInputPosition().length - 1; i++)
			{
				data.section = XMLHandler.getSubNode(data.section, meta.getInputPosition()[i]);
			}
			// Last element gets repeated: what's the name?
			data.itemElement = meta.getInputPosition()[meta.getInputPosition().length - 1];

			data.itemCount = XMLHandler.countNodes(data.section, data.itemElement);
			data.itemPosition = (int) meta.getNrRowsToSkip();
		} catch (Exception e)
		{
			logError(Messages.getString("XMLInput.Log.UnableToOpenFile", "" + data.filenr, data.file
					.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLInputMeta) smi;
		data = (XMLInputData) sdi;

		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles().getFiles();
			if (data.files == null || data.files.size() == 0)
			{
				logError(Messages.getString("XMLInput.Log.NoFiles"));
				return false;
			}

			data.rownr = 1L;

			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLInputMeta) smi;
		data = (XMLInputData) sdi;

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