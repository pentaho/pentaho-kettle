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

package org.pentaho.di.trans.steps.xmlinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
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
	private static Class<?> PKG = XMLInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// For String to <type> conversions, we allocate a conversion meta data row as well...
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (int i=0;i<data.convertRowMeta.size();i++) {
				data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);
			}
		}

		Object[] outputRowData = getRowFromXML();
		if (outputRowData == null)
		{
			setOutputDone(); // signal end to receiver(s)
			return false; // This is the end of this step.
		}

		if (log.isRowLevel())
			logRowlevel(BaseMessages.getString(PKG, "XMLInput.Log.ReadRow", outputRowData.toString()));

		incrementLinesInput();

		putRow(data.outputRowMeta, outputRowData);

		// limit has been reached, stop now.
		if (meta.getRowLimit() > 0 && data.rownr >= meta.getRowLimit()) 
		{
			setOutputDone();
			return false;
		}

		return true;
	}

	private Object[] getRowFromXML() throws KettleValueException
	{
		// finished reading the file, read the next file

		while (data.itemPosition >= data.itemCount || data.file == null) 
		{
			data.file = null;
			if (!openNextFile())
			{
				return null;
			}
		}

		Object[] outputRowData = buildEmptyRow();

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

			// This value will contain the value we're looking for...
			//
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
								logDebug(BaseMessages.getString(PKG, "XMLInput.Log.UnableToFindPosition", pos
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
								logDebug(BaseMessages.getString(PKG, "XMLInput.Log.UnableToFindPosition", pos
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

			// OK, we have grabbed the string called value
			// Trim it, convert it, ...
			
			// DO Trimming!
			switch (xmlInputField.getTrimType())
			{
			case XMLInputField.TYPE_TRIM_LEFT:
				value = Const.ltrim(value);
				break;
			case XMLInputField.TYPE_TRIM_RIGHT:
				value = Const.rtrim(value);
				break;
			case XMLInputField.TYPE_TRIM_BOTH:
				value = Const.trim(value);
				break;
			default:
				break;
			}

			// System.out.println("after trim, field #"+i+" : "+v);

			// DO CONVERSIONS...
			//
			ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(i);
			ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(i);
			outputRowData[i] = targetValueMeta.convertData(sourceValueMeta, value);

			// Do we need to repeat this field if it is null?
			if (meta.getInputFields()[i].isRepeated())
			{
				if (data.previousRow!=null && Const.isEmpty(value))
				{
					outputRowData[i] = data.previousRow[i];
				}
			}
		} // End of loop over fields...

		int outputIndex = meta.getInputFields().length;
		
		// See if we need to add the filename to the row...
		if ( meta.includeFilename() && !Const.isEmpty(meta.getFilenameField()) ) {
			outputRowData[outputIndex++] = KettleVFS.getFilename(data.file);
		}

		// See if we need to add the row number to the row...
		if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField())) {
			outputRowData[outputIndex++] = new Long(data.rownr);
		}

		RowMetaInterface irow = getInputRowMeta();
		
		data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
		// surely the next step doesn't change it in between...
		data.rownr++;

		// Throw away the information in the item?
		NodeList nodeList = itemNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			itemNode.removeChild(nodeList.item(i));
		}

		return outputRowData;
	}

	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */
	private Object[] buildEmptyRow()
	{
		return RowDataUtil.allocateRowData(data.outputRowMeta.size());
	}

	private boolean openNextFile()
	{
		try
		{
			if (data.filenr >= data.files.size()) // finished processing!
			{
				if (log.isDetailed())
					logDetailed(BaseMessages.getString(PKG, "XMLInput.Log.FinishedProcessing"));
				return false;
			}

			// Is this the last file?
			data.last_file = (data.filenr == data.files.size() - 1);
			data.file = (FileObject) data.files.get(data.filenr);

			logBasic(BaseMessages.getString(PKG, "XMLInput.Log.OpeningFile", data.file.toString()));

			// Move file pointer ahead!
			data.filenr++;
			
			String baseURI = this.environmentSubstitute(meta.getFileBaseURI());
			if (Const.isEmpty(baseURI)) {
				baseURI = data.file.getParent().getName().getURI();
			}

			// Open the XML document
			data.document = XMLHandler.loadXMLFile(data.file, baseURI, meta.isIgnoreEntities(), meta.isNamespaceAware());

			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta()
					.getName(), getStepname());
			resultFile.setComment("File was read by an XML input step");
			addResultFile(resultFile);

			if (log.isDetailed())
				logDetailed(BaseMessages.getString(PKG, "XMLInput.Log.FileOpened", data.file.toString()));

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
			logError(BaseMessages.getString(PKG, "XMLInput.Log.UnableToOpenFile", "" + data.filenr, data.file
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
			data.files = meta.getFiles(this).getFiles();
			if (data.files == null || data.files.size() == 0)
			{
				logError(BaseMessages.getString(PKG, "XMLInput.Log.NoFiles"));
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

}