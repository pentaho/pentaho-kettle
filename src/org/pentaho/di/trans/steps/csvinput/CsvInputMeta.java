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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface.FileNamingType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.InputFileMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.w3c.dom.Node;


/**
 * @since 2007-07-05
 * @author matt
 * @version 3.0
 */

public class CsvInputMeta extends BaseStepMeta implements StepMetaInterface, InputFileMetaInterface, StepMetaInjectionInterface
{
	private static Class<?> PKG = CsvInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private String filename;
	
	private String filenameField;

	private boolean includingFilename; 
	
	private String rowNumField;

	private boolean headerPresent;

	private String delimiter;
	private String enclosure;

	private String bufferSize;
	
	private boolean lazyConversionActive;
	
	private TextFileInputField[] inputFields;
	
	private boolean isaddresult;
	
	private boolean runningInParallel;
	
	private String encoding;
	
	public CsvInputMeta()
	{
		super(); // allocate BaseStepMeta
		allocate(0);
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}

	public void setDefault() {
		delimiter = ","  ;
		enclosure = "\""  ;
		headerPresent = true;
		lazyConversionActive=true;
		isaddresult=false;
		bufferSize="50000";
	}
	
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			filename = XMLHandler.getTagValue(stepnode, CsvInputAttr.FILENAME);
			filenameField = XMLHandler.getTagValue(stepnode, CsvInputAttr.FILENAME_FIELD);
			rowNumField = XMLHandler.getTagValue(stepnode, CsvInputAttr.ROW_NUM_FIELD);
			includingFilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, CsvInputAttr.INCLUDE_FILENAME));
			delimiter = XMLHandler.getTagValue(stepnode, CsvInputAttr.DELIMITER);
			enclosure = XMLHandler.getTagValue(stepnode, CsvInputAttr.ENCLOSURE);
			bufferSize  = XMLHandler.getTagValue(stepnode, CsvInputAttr.BUFFERSIZE);
			headerPresent = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, CsvInputAttr.HEADER_PRESENT));
			lazyConversionActive= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, CsvInputAttr.LAZY_CONVERSION));
			isaddresult= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, CsvInputAttr.ADD_FILENAME_RESULT));
			runningInParallel = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, CsvInputAttr.PARALLEL));
			encoding = XMLHandler.getTagValue(stepnode, CsvInputAttr.ENCODING);
			
			Node fields = XMLHandler.getSubNode(stepnode, CsvInputAttr.FIELDS.getXmlCode());
			int nrfields = XMLHandler.countNodes(fields, CsvInputAttr.FIELD.getXmlCode());
			
			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				inputFields[i] = new TextFileInputField();
				
				Node fnode = XMLHandler.getSubNodeByNr(fields, CsvInputAttr.FIELD.getXmlCode(), i);

				inputFields[i].setName( XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_NAME) );
				inputFields[i].setType(  ValueMeta.getType(XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_TYPE)) );
				inputFields[i].setFormat( XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_FORMAT) );
				inputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_CURRENCY) );
				inputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_DECIMAL) );
				inputFields[i].setGroupSymbol( XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_GROUP) );
				inputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_LENGTH), -1) );
				inputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_PRECISION), -1) );
				inputFields[i].setTrimType( ValueMeta.getTrimTypeByCode( XMLHandler.getTagValue(fnode, CsvInputAttr.FIELD_TRIM_TYPE) ) );
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrFields) {
		inputFields = new TextFileInputField[nrFields];
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(500);

		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.FILENAME, filename));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.FILENAME_FIELD, filenameField));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.ROW_NUM_FIELD, rowNumField));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.INCLUDE_FILENAME, includingFilename));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.DELIMITER, delimiter));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.ENCLOSURE, enclosure));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.HEADER_PRESENT, headerPresent));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.BUFFERSIZE, bufferSize));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.LAZY_CONVERSION, lazyConversionActive));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.ADD_FILENAME_RESULT, isaddresult));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.PARALLEL, runningInParallel));
		retval.append("    ").append(XMLHandler.addTagValue(CsvInputAttr.ENCODING, encoding));

		retval.append("    ").append(XMLHandler.openTag(CsvInputAttr.FIELDS.getXmlCode())).append(Const.CR);
		for (int i = 0; i < inputFields.length; i++)
		{
			TextFileInputField field = inputFields[i];
			
	        retval.append("      ").append(XMLHandler.openTag(CsvInputAttr.FIELD.getXmlCode())).append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_NAME, field.getName()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_TYPE, ValueMeta.getTypeDesc(field.getType())));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_FORMAT, field.getFormat()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_CURRENCY, field.getCurrencySymbol()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_DECIMAL, field.getDecimalSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_GROUP, field.getGroupSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_LENGTH, field.getLength()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_PRECISION, field.getPrecision()));
			retval.append("        ").append(XMLHandler.addTagValue(CsvInputAttr.FIELD_TRIM_TYPE, ValueMeta.getTrimTypeCode(field.getTrimType())));
            retval.append("      ").append(XMLHandler.closeTag(CsvInputAttr.FIELD.getXmlCode())).append(Const.CR);
		}
        retval.append("    ").append(XMLHandler.closeTag(CsvInputAttr.FIELDS.getXmlCode())).append(Const.CR);

		return retval.toString();
	}


	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			filename = rep.getStepAttributeString(id_step, CsvInputAttr.FILENAME.getRepCode());
			filenameField = rep.getStepAttributeString(id_step, CsvInputAttr.FILENAME_FIELD.getRepCode());
			rowNumField = rep.getStepAttributeString(id_step, CsvInputAttr.ROW_NUM_FIELD.getRepCode());
			includingFilename = rep.getStepAttributeBoolean(id_step, CsvInputAttr.INCLUDE_FILENAME.getRepCode());
			delimiter = rep.getStepAttributeString(id_step, CsvInputAttr.DELIMITER.getRepCode());
			enclosure = rep.getStepAttributeString(id_step, CsvInputAttr.ENCLOSURE.getRepCode());
			headerPresent = rep.getStepAttributeBoolean(id_step, CsvInputAttr.HEADER_PRESENT.getRepCode());
			bufferSize = rep.getStepAttributeString(id_step, CsvInputAttr.BUFFERSIZE.getRepCode());
			lazyConversionActive = rep.getStepAttributeBoolean(id_step, CsvInputAttr.LAZY_CONVERSION.getRepCode());
			isaddresult = rep.getStepAttributeBoolean(id_step, CsvInputAttr.ADD_FILENAME_RESULT.getRepCode());
			runningInParallel = rep.getStepAttributeBoolean(id_step, CsvInputAttr.PARALLEL.getRepCode());
			encoding = rep.getStepAttributeString(id_step, CsvInputAttr.ENCODING.getRepCode());
			
			int nrfields = rep.countNrStepAttributes(id_step, CsvInputAttr.FIELD_NAME.getRepCode());

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				inputFields[i] = new TextFileInputField();
				
				inputFields[i].setName( rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_NAME.getRepCode()) );
				inputFields[i].setType( ValueMeta.getType(rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_TYPE.getRepCode())) );
				inputFields[i].setFormat( rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_FORMAT.getRepCode()) );
				inputFields[i].setCurrencySymbol( rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_CURRENCY.getRepCode()) );
				inputFields[i].setDecimalSymbol( rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_DECIMAL.getRepCode()) );
				inputFields[i].setGroupSymbol( rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_GROUP.getRepCode()) );
				inputFields[i].setLength( (int) rep.getStepAttributeInteger(id_step, i, CsvInputAttr.FIELD_LENGTH.getRepCode()) );
				inputFields[i].setPrecision( (int) rep.getStepAttributeInteger(id_step, i, CsvInputAttr.FIELD_PRECISION.getRepCode()) );
				inputFields[i].setTrimType( ValueMeta.getTrimTypeByCode( rep.getStepAttributeString(id_step, i, CsvInputAttr.FIELD_TRIM_TYPE.getRepCode())) );
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.FILENAME.getRepCode(), filename);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.FILENAME_FIELD.getRepCode(), filenameField);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.ROW_NUM_FIELD.getRepCode(), rowNumField);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.INCLUDE_FILENAME.getRepCode(), includingFilename);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.DELIMITER.getRepCode(), delimiter);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.ENCLOSURE.getRepCode(), enclosure);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.BUFFERSIZE.getRepCode(), bufferSize);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.HEADER_PRESENT.getRepCode(), headerPresent);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.LAZY_CONVERSION.getRepCode(), lazyConversionActive);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.ADD_FILENAME_RESULT.getRepCode(), isaddresult);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.PARALLEL.getRepCode(), runningInParallel);
			rep.saveStepAttribute(id_transformation, id_step, CsvInputAttr.ENCODING.getRepCode(), encoding);

			for (int i = 0; i < inputFields.length; i++)
			{
				TextFileInputField field = inputFields[i];
				
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_NAME.getRepCode(), field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_TYPE.getRepCode(), ValueMeta.getTypeDesc(field.getType()));
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_FORMAT.getRepCode(), field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_CURRENCY.getRepCode(), field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_DECIMAL.getRepCode(), field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_GROUP.getRepCode(), field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_LENGTH.getRepCode(), field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_PRECISION.getRepCode(), field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, CsvInputAttr.FIELD_TRIM_TYPE.getRepCode(), ValueMeta.getTrimTypeCode( field.getTrimType()));
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		rowMeta.clear(); // Start with a clean slate, eats the input
		
		for (int i=0;i<inputFields.length;i++) {
			TextFileInputField field = inputFields[i];
			
			ValueMetaInterface valueMeta = new ValueMeta(field.getName(), field.getType());
			valueMeta.setConversionMask( field.getFormat() );
			valueMeta.setLength( field.getLength() );
			valueMeta.setPrecision( field.getPrecision() );
			valueMeta.setConversionMask( field.getFormat() );
			valueMeta.setDecimalSymbol( field.getDecimalSymbol() );
			valueMeta.setGroupingSymbol( field.getGroupSymbol() );
			valueMeta.setCurrencySymbol( field.getCurrencySymbol() );
			valueMeta.setTrimType( field.getTrimType() );
			if (lazyConversionActive) valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			valueMeta.setStringEncoding(space.environmentSubstitute(encoding));
			
			// In case we want to convert Strings...
			// Using a copy of the valueMeta object means that the inner and outer representation format is the same.
			// Preview will show the data the same way as we read it.
			// This layout is then taken further down the road by the metadata through the transformation.
			//
			ValueMetaInterface storageMetadata = valueMeta.clone();
			storageMetadata.setType(ValueMetaInterface.TYPE_STRING);
			storageMetadata.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
			storageMetadata.setLength(-1,-1); // we don't really know the lengths of the strings read in advance.
			valueMeta.setStorageMetadata(storageMetadata);
			
			valueMeta.setOrigin(origin);
			
			rowMeta.addValueMeta(valueMeta);
		}
		
		if (!Const.isEmpty(filenameField) && includingFilename) {
			ValueMetaInterface filenameMeta = new ValueMeta(filenameField, ValueMetaInterface.TYPE_STRING);
			filenameMeta.setOrigin(origin);
			if (lazyConversionActive) {
				filenameMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
				filenameMeta.setStorageMetadata(new ValueMeta(filenameField, ValueMetaInterface.TYPE_STRING));
			}
			rowMeta.addValueMeta(filenameMeta);
		}
		
		if (!Const.isEmpty(rowNumField)) {
			ValueMetaInterface rowNumMeta = new ValueMeta(rowNumField, ValueMetaInterface.TYPE_INTEGER);
			rowNumMeta.setLength(10);
			rowNumMeta.setOrigin(origin);
			rowMeta.addValueMeta(rowNumMeta);
		}
		
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CsvInputMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new CsvInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new CsvInputData();
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the bufferSize
	 */
	public String getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(String bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return true if lazy conversion is turned on: conversions are delayed as long as possible, perhaps to never occur at all.
	 */
	public boolean isLazyConversionActive() {
		return lazyConversionActive;
	}

	/**
	 * @param lazyConversionActive true if lazy conversion is to be turned on: conversions are delayed as long as possible, perhaps to never occur at all.
	 */
	public void setLazyConversionActive(boolean lazyConversionActive) {
		this.lazyConversionActive = lazyConversionActive;
	}

	/**
	 * @return the headerPresent
	 */
	public boolean isHeaderPresent() {
		return headerPresent;
	}

	/**
	 * @param headerPresent the headerPresent to set
	 */
	public void setHeaderPresent(boolean headerPresent) {
		this.headerPresent = headerPresent;
	}

	/**
	 * @return the enclosure
	 */
	public String getEnclosure() {
		return enclosure;
	}

	/**
	 * @param enclosure the enclosure to set
	 */
	public void setEnclosure(String enclosure) {
		this.enclosure = enclosure;
	}


    @Override
	public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
		List<ResourceReference> references = new ArrayList<ResourceReference>(5);

		ResourceReference reference = new ResourceReference(stepInfo);
		references.add(reference);
		if (!Const.isEmpty(filename)) {
			// Add the filename to the references, including a reference to this
			// step meta data.
			//
			reference.getEntries().add(new ResourceEntry(transMeta.environmentSubstitute(filename), ResourceType.FILE));
		}
		return references;
	}

	/**
	 * @return the inputFields
	 */
	public TextFileInputField[] getInputFields() {
		return inputFields;
	}

	/**
	 * @param inputFields
	 *            the inputFields to set
	 */
	public void setInputFields(TextFileInputField[] inputFields) {
		this.inputFields = inputFields;
	}

	public int getFileFormatTypeNr() {
		return TextFileInputMeta.FILE_FORMAT_MIXED; // TODO: check this
	}

	public String[] getFilePaths(VariableSpace space) {
		return new String[] { space.environmentSubstitute(filename), };
	}

	public int getNrHeaderLines() {
		return 1;
	}

	public boolean hasHeader() {
		return isHeaderPresent();
	}

	public String getErrorCountField() {
		return null;
	}

	public String getErrorFieldsField() {
		return null;
	}

	public String getErrorTextField() {
		return null;
	}

	public String getEscapeCharacter() {
		return null;
	}

	public String getFileType() {
		return "CSV";
	}

	public String getSeparator() {
		return delimiter;
	}

	public boolean includeFilename() {
		return false;
	}

	public boolean includeRowNumber() {
		return false;
	}

	public boolean isErrorIgnored() {
		return false;
	}

	public boolean isErrorLineSkipped() {
		return false;
	}

	/**
	 * @return the filenameField
	 */
	public String getFilenameField() {
		return filenameField;
	}

	/**
	 * @param filenameField the filenameField to set
	 */
	public void setFilenameField(String filenameField) {
		this.filenameField = filenameField;
	}

	/**
	 * @return the includingFilename
	 */
	public boolean isIncludingFilename() {
		return includingFilename;
	}

	/**
	 * @param includingFilename the includingFilename to set
	 */
	public void setIncludingFilename(boolean includingFilename) {
		this.includingFilename = includingFilename;
	}

	/**
	 * @return the rowNumField
	 */
	public String getRowNumField() {
		return rowNumField;
	}

	/**
	 * @param rowNumField the rowNumField to set
	 */
	public void setRowNumField(String rowNumField) {
		this.rowNumField = rowNumField;
	}	
	
	 /**
     * @param isaddresult The isaddresult to set.
     */
    public void setAddResultFile(boolean isaddresult)
    {
        this.isaddresult = isaddresult;
    }
    
    /**
     *  @return Returns isaddresult.
     */
    public boolean isAddResultFile()
    {
        return isaddresult;
    }

	/**
	 * @return the runningInParallel
	 */
	public boolean isRunningInParallel() {
		return runningInParallel;
	}

	/**
	 * @param runningInParallel the runningInParallel to set
	 */
	public void setRunningInParallel(boolean runningInParallel) {
		this.runningInParallel = runningInParallel;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	/**
	 * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
	 * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like that.

	 * TODO: create options to configure this behavior 
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			// In case the name of the file comes from previous steps, forget about this!
			//
			if (Const.isEmpty(filenameField)) {
				// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.csv
				// To   : /home/matt/test/files/foo/bar.csv
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(filename), space);
				
				// If the file doesn't exist, forget about this effort too!
				//
				if (fileObject.exists()) {
					// Convert to an absolute path...
					// 
					filename = resourceNamingInterface.nameResource(fileObject.getName().getBaseName(), fileObject.getParent().getName().getPath(), space.toString(), FileNamingType.DATA_FILE);
					
					return filename;
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
	
	public boolean supportsErrorHandling() {
		return true;
	}
	
	public StepMetaInjectionInterface getStepMetaInjectionInterface() {
	  return this;
	}

    public void injectStepMetadataEntries(List<StepInjectionMetaEntry> metadata) {
      for (StepInjectionMetaEntry entry : metadata) {
        CsvInputAttr attr = CsvInputAttr.findByKey(entry.getKey());
        
        // Set top level attributes...
        //
        if (entry.getValueType()!=ValueMetaInterface.TYPE_NONE) {
          switch(attr) {
          case FILENAME : filename = (String) entry.getValue(); break;
          case FILENAME_FIELD : filenameField = (String) entry.getValue(); break;
          case ROW_NUM_FIELD: rowNumField = (String) entry.getValue(); break;
          case HEADER_PRESENT: headerPresent = (Boolean) entry.getValue(); break;
          case DELIMITER: delimiter = (String) entry.getValue(); break;
          case ENCLOSURE: enclosure = (String) entry.getValue(); break;
          case BUFFERSIZE: bufferSize = (String) entry.getValue(); break;
          case LAZY_CONVERSION: lazyConversionActive = (Boolean) entry.getValue(); break;
          case PARALLEL: runningInParallel = (Boolean) entry.getValue(); break;
          case ADD_FILENAME_RESULT: isaddresult = (Boolean) entry.getValue(); break;
          case ENCODING: encoding = (String) entry.getValue(); break;
          default: throw new RuntimeException("Unhandled metadata injection of attribute: "+attr.toString()+" - "+attr.getDescription());
          }
        } else {
          if (attr == CsvInputAttr.FIELDS) {
            // This entry contains a list of lists...
            // Each list contains a single CSV input field definition (one line in the dialog)
            //
            List<StepInjectionMetaEntry> inputFieldEntries = entry.getDetails();
            inputFields = new TextFileInputField[inputFieldEntries.size()];
            for (int row=0;row<inputFieldEntries.size();row++) {
              StepInjectionMetaEntry inputFieldEntry = inputFieldEntries.get(row);
              TextFileInputField inputField = new TextFileInputField();

              List<StepInjectionMetaEntry> fieldAttributes = inputFieldEntry.getDetails();
              for (int i=0;i<fieldAttributes.size();i++) {
                StepInjectionMetaEntry fieldAttribute = fieldAttributes.get(i);
                CsvInputAttr fieldAttr = CsvInputAttr.findByKey(fieldAttribute.getKey());

                String attributeValue = (String)fieldAttribute.getValue();
                switch(fieldAttr) {
                case FIELD_NAME : inputField.setName(attributeValue); break;
                case FIELD_TYPE : inputField.setType(ValueMeta.getType(attributeValue)); break;
                case FIELD_FORMAT : inputField.setFormat(attributeValue); break;
                case FIELD_LENGTH : inputField.setLength(attributeValue==null ? -1 : Integer.parseInt(attributeValue)); break;
                case FIELD_PRECISION : inputField.setPrecision(attributeValue==null ? -1 : Integer.parseInt(attributeValue)); break;
                case FIELD_CURRENCY : inputField.setCurrencySymbol(attributeValue); break;
                case FIELD_DECIMAL :inputField.setDecimalSymbol(attributeValue); break;
                case FIELD_GROUP :inputField.setGroupSymbol(attributeValue); break;
                case FIELD_TRIM_TYPE : inputField.setTrimType(ValueMeta.getTrimTypeByCode(attributeValue)); break;
                default: throw new RuntimeException("Unhandled metadata injection of attribute: "+fieldAttr.toString()+" - "+fieldAttr.getDescription());
                }
              }
              
              inputFields[row] = inputField;
            }
          }
        }
      }
    }

    /**
     * Describe the metadata attributes that can be injected into this step metadata object.
     */
    public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() {
      return getStepInjectionMetadataEntries(CsvInputAttr.values(), PKG);
    }
}