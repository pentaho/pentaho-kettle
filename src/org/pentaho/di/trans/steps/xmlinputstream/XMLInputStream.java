/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.xmlinputstream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

/**
 * Use a StAX parser to read XML in a flexible and fast way.
 * 
 * @author Jens Bleuel
 * @since 2011-01-13
 */
//TODO black box testing
public class XMLInputStream extends BaseStep implements StepInterface
{
	private static Class<?> PKG = XMLInputStream.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static int PARENT_ID_ALLOCATE_SIZE = 1000; //max. number of nested elements, we may let the user configure this

	private XMLInputStreamMeta meta;

	private XMLInputStreamData data;

	static final String[] eventDescription= {"UNKNOWN","START_ELEMENT","END_ELEMENT","PROCESSING_INSTRUCTION","CHARACTERS","COMMENT","SPACE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		"START_DOCUMENT","END_DOCUMENT","ENTITY_REFERENCE","ATTRIBUTE","DTD","CDATA","NAMESPACE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		"NOTATION_DECLARATION","ENTITY_DECLARATION"}; //$NON-NLS-1$ //$NON-NLS-2$

	public XMLInputStream(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (first)
		{
			first=false;

			// replace variables
			data.filename=this.environmentSubstitute(meta.getFilename());
			data.nrRowsToSkip=Const.toLong(this.environmentSubstitute(meta.getNrRowsToSkip()), 0);
			data.rowLimit=Const.toLong(this.environmentSubstitute(meta.getRowLimit()), 0);
			data.encoding=this.environmentSubstitute(meta.getEncoding());
			
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			// get and save field positions
			data.pos_xml_filename=data.outputRowMeta.indexOfValue(meta.getFilenameField());
			data.pos_xml_row_number=data.outputRowMeta.indexOfValue(meta.getRowNumberField());
			data.pos_xml_data_type_numeric=data.outputRowMeta.indexOfValue(meta.getXmlDataTypeNumericField());
			data.pos_xml_data_type_description=data.outputRowMeta.indexOfValue(meta.getXmlDataTypeDescriptionField());
			data.pos_xml_location_line=data.outputRowMeta.indexOfValue(meta.getXmlLocationLineField());
			data.pos_xml_location_column=data.outputRowMeta.indexOfValue(meta.getXmlLocationColumnField());
			data.pos_xml_element_id=data.outputRowMeta.indexOfValue(meta.getXmlElementIDField());
			data.pos_xml_parent_element_id=data.outputRowMeta.indexOfValue(meta.getXmlParentElementIDField());
			data.pos_xml_element_level=data.outputRowMeta.indexOfValue(meta.getXmlElementLevelField());
			data.pos_xml_path=data.outputRowMeta.indexOfValue(meta.getXmlPathField());
			data.pos_xml_parent_path=data.outputRowMeta.indexOfValue(meta.getXmlParentPathField());
			data.pos_xml_data_name=data.outputRowMeta.indexOfValue(meta.getXmlDataNameField());
			data.pos_xml_data_value=data.outputRowMeta.indexOfValue(meta.getXmlDataValueField());

			data.fileObject = KettleVFS.getFileObject(data.filename, getTransMeta());

			try {
				data.fileInputStream = new FileInputStream(FileUtils.toFile(data.fileObject.getURL()));
			} catch (FileNotFoundException e) { // by FileInputStream
				throw new KettleException(e);
			} catch (FileSystemException e) { // by fileObject.getURL()
				throw new KettleException(e);
			}
			try {
				data.xmlEventReader = data.staxInstance.createXMLEventReader(data.fileInputStream, data.encoding);
			} catch (XMLStreamException e) {
				throw new KettleException(e);
			}

			if(meta.isAddResultFile()) {
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.fileObject, getTransMeta()
						.getName(), getStepname());
				resultFile.setComment("File was read by an XML Input Stream step"); //TODO externalize
				addResultFile(resultFile);
			}

			resetElementCounters();
		}

		Object[] outputRowData = getRowFromXML();
		if (outputRowData==null)
		{
			setOutputDone(); // signal end to receiver(s)
			return false; // This is the end of this step.
		}

		putRowOut(outputRowData);

		// limit has been reached: stop now. (not exact science since some attributes could be mixed within the last row)
		if (data.rowLimit > 0 && data.rowNumber >= data.rowLimit) {
			setOutputDone();
			return false;
		}

		return true;
	}

	// sends the normal row and attributes
	private void putRowOut(Object r[]) throws KettleStepException, KettleValueException {

		data.rowNumber++;
		if (data.pos_xml_filename!=-1) r[data.pos_xml_filename]=new String(data.filename); 
		if (data.pos_xml_row_number!=-1) r[data.pos_xml_row_number]=new Long(data.rowNumber);
		if (data.pos_xml_element_id!=-1) r[data.pos_xml_element_id]=data.elementLevelID[data.elementLevel];
		if (data.pos_xml_element_level!=-1) r[data.pos_xml_element_level]=new Long(data.elementLevel);
		if (data.pos_xml_parent_element_id!=-1) r[data.pos_xml_parent_element_id]=data.elementParentID[data.elementLevel];
		if (data.pos_xml_path!=-1) r[data.pos_xml_path]=data.elementPath[data.elementLevel];
		if (data.pos_xml_parent_path!=-1 && data.elementLevel>0) r[data.pos_xml_parent_path]=data.elementPath[data.elementLevel-1];

		// We could think of adding an option to filter Start_end Document / Elements, RegEx?
		// We could think of adding columns identifying Element-Blocks
		
		// Skip rows? (not exact science since some attributes could be mixed within the last row)
		if (data.nrRowsToSkip == 0 || data.rowNumber > data.nrRowsToSkip) {
			if (log.isRowLevel()) logRowlevel("Read row: " + data.outputRowMeta.getString(r)); //$NON-NLS-1$
			putRow(data.outputRowMeta, r);
		}
	}

	private Object[] getRowFromXML() throws KettleException
	{

		Object[] outputRowData = null;
		// loop until significant data is there and more data is there
		while (data.xmlEventReader.hasNext() && outputRowData==null && !isStopped()) {
			outputRowData = processEvent();
			// log all events (but no attributes sent by the EventReader)
			incrementLinesInput();
			if (checkFeedback(getLinesInput()) && isBasic()) logBasic(BaseMessages.getString(PKG, "XMLInputStream.Log.LineNumber", Long.toString(getLinesInput()))); //$NON-NLS-1$
		}

		return outputRowData;
	}

	private Object[] processEvent() throws KettleException
	{

		Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		XMLEvent e = null;
		try {
			e = data.xmlEventReader.nextEvent();
		} catch (XMLStreamException ex) {
			throw new KettleException(ex);
		}

		int eventType=e.getEventType();
		if (data.pos_xml_data_type_numeric!=-1) outputRowData[data.pos_xml_data_type_numeric]=new Long(eventType);
		if (data.pos_xml_data_type_description!=-1) {
			if (eventType==0 || eventType>eventDescription.length) {
				//unknown eventType
				outputRowData[data.pos_xml_data_type_description]=eventDescription[0]+"("+eventType+")";
			} else {
				outputRowData[data.pos_xml_data_type_description]=eventDescription[eventType];
			}
		}
		if (data.pos_xml_location_line!=-1) outputRowData[data.pos_xml_location_line]=new Long(e.getLocation().getLineNumber());
		if (data.pos_xml_location_column!=-1) outputRowData[data.pos_xml_location_column]=new Long(e.getLocation().getColumnNumber());

		switch (eventType) {

		case XMLStreamConstants.START_ELEMENT:
			data.elementLevel++;
			if(data.elementLevel>PARENT_ID_ALLOCATE_SIZE-1) 
				throw new KettleException("Too many nested XML elements, more than "+PARENT_ID_ALLOCATE_SIZE);
			if (data.elementParentID[data.elementLevel]==null) data.elementParentID[data.elementLevel]=data.elementID;
			data.elementID++;
			data.elementLevelID[data.elementLevel]=data.elementID;

			outputRowData[data.pos_xml_data_name]=e.asStartElement().getName().getLocalPart();
			//store the name
			data.elementName[data.elementLevel]=new String((String)outputRowData[data.pos_xml_data_name]);
			//store simple path
			data.elementPath[data.elementLevel]=data.elementPath[data.elementLevel-1]+"/"+outputRowData[data.pos_xml_data_name];

			outputRowData = parseAttributes(outputRowData, e);
			//TODO printNamespaces(xmlr);###
			break;

		case XMLStreamConstants.END_ELEMENT:
			outputRowData[data.pos_xml_data_name]=e.asEndElement().getName().getLocalPart();
			putRowOut(outputRowData);
			data.elementParentID[data.elementLevel+1]=null;
			data.elementLevel--;
			outputRowData=null; // continue
			break;

		case XMLStreamConstants.SPACE:
			outputRowData=null; // ignore & continue
			break;

		case XMLStreamConstants.CHARACTERS:
			outputRowData[data.pos_xml_data_name]=data.elementName[data.elementLevel];
			outputRowData[data.pos_xml_data_value]=e.asCharacters().getData();
			//optional trim is also eliminating white spaces, tab, cr, lf
			if (meta.isEnableTrim()) outputRowData[data.pos_xml_data_value]=Const.trim((String)outputRowData[data.pos_xml_data_value]);
			if (Const.isEmpty((String)outputRowData[data.pos_xml_data_value])) outputRowData=null; // ignore & continue
			break;

		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			outputRowData=null; // ignore & continue
			//TODO test if possible
			break;

		case XMLStreamConstants.CDATA:
			// normally this is automatically in CHARACTERS
			outputRowData[data.pos_xml_data_name]=data.elementName[data.elementLevel];
			outputRowData[data.pos_xml_data_value]=e.asCharacters().getData();
			//optional trim is also eliminating white spaces, tab, cr, lf
			if (meta.isEnableTrim()) outputRowData[data.pos_xml_data_value]=Const.trim((String)outputRowData[data.pos_xml_data_value]);
			if (Const.isEmpty((String)outputRowData[data.pos_xml_data_value])) outputRowData=null; // ignore & continue
			break;

		case XMLStreamConstants.COMMENT:
			outputRowData=null; // ignore & continue
			//TODO test if possible
			break;

		case XMLStreamConstants.ENTITY_REFERENCE:
			// should be resolved by default
			outputRowData=null; // ignore & continue
			break;

		case XMLStreamConstants.START_DOCUMENT:
			// just get this information out
			break;

		case XMLStreamConstants.END_DOCUMENT:
			// just get this information out
			break;			

		default:
			outputRowData=null; // ignore & continue
		}

		return outputRowData;
	}

	// Attributes: put an extra row out for each attribute
	@SuppressWarnings("unchecked")
	private Object[] parseAttributes(Object[] outputRowData, XMLEvent e) throws KettleValueException, KettleStepException {
		Iterator<Attribute> iter = e.asStartElement().getAttributes();
		if (iter.hasNext()) {
			Object[] outputRowDataAttribute=data.outputRowMeta.cloneRow(outputRowData);	    		 
			putRowOut(outputRowDataAttribute);	// first put the element name info out 
			// change data_type to ATTRIBUTE
			if (data.pos_xml_data_type_numeric!=-1) outputRowData[data.pos_xml_data_type_numeric]=new Long(XMLStreamConstants.ATTRIBUTE);
			if (data.pos_xml_data_type_description!=-1) outputRowData[data.pos_xml_data_type_description]=eventDescription[XMLStreamConstants.ATTRIBUTE];
		}
		while( iter.hasNext() ) {
			Object[] outputRowDataAttribute=data.outputRowMeta.cloneRow(outputRowData);	    		 
			Attribute a = iter.next();
			outputRowDataAttribute[data.pos_xml_data_name]=a.getName().getLocalPart();
			outputRowDataAttribute[data.pos_xml_data_value]=a.getValue();
			if(iter.hasNext()) {
				// send out the Attribute row
				putRowOut(outputRowDataAttribute);
			} else {
				// last row: this will be sent out by the outer loop
				outputRowData=outputRowDataAttribute;
			}
		}

		return outputRowData;
	}

	private void resetElementCounters() {
		data.rowNumber=new Long(0);
		data.elementLevel=0;
		data.elementID=new Long(0); // init value, could be parameterized later on
		data.elementLevelID=new Long[PARENT_ID_ALLOCATE_SIZE];	
		data.elementLevelID[0]=data.elementID; //inital id for level 0
		data.elementParentID=new Long[PARENT_ID_ALLOCATE_SIZE];
		data.elementName=new String[PARENT_ID_ALLOCATE_SIZE];
		data.elementPath=new String[PARENT_ID_ALLOCATE_SIZE];
		data.elementPath[0]=""; //initial empty
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLInputStreamMeta) smi;
		data = (XMLInputStreamData) sdi;

		if (super.init(smi, sdi))
		{
			data.staxInstance = XMLInputFactory.newInstance();  // could select the parser later on
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLInputStreamMeta) smi;
		data = (XMLInputStreamData) sdi;

		// free resources
		if(data.xmlEventReader!=null) {
			try {
				data.xmlEventReader.close();
			} catch (XMLStreamException e) {
				// intentionally ignored on closing
			}
			data.xmlEventReader = null;
		}
		if(data.fileInputStream!=null) {
			try {
				data.fileInputStream.close();
			} catch (IOException e) {
				// intentionally ignored on closing
			}
			data.fileInputStream = null;
		}
		if(data.fileObject!=null) {
			try {
				data.fileObject.close();
			} catch (IOException e) {
				// intentionally ignored on closing
			}
			data.fileObject = null;
		}

		data.staxInstance = null;

		super.dispose(smi, sdi);
	}

}