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

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
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

@Step(name="CsvInput",image="ui/images/TFI.png",tooltip="BaseStep.TypeTooltipDesc.CsvInput",description="BaseStep.TypeLongDesc.CsvInput",
		category=StepCategory.CATEGORY_INPUT)
public class CsvInputMeta extends BaseStepMeta implements StepMetaInterface, InputFileMetaInterface
{
	private String filename;
	
	private boolean headerPresent;

	private String delimiter;
	private String enclosure;

	private String bufferSize;
	
	private boolean lazyConversionActive;
	
	private TextFileInputField[] inputFields;
	
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
		bufferSize="50000";
	}
	
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			filename = XMLHandler.getTagValue(stepnode, "filename");
			delimiter = XMLHandler.getTagValue(stepnode, "separator");
			enclosure = XMLHandler.getTagValue(stepnode, "enclosure");
			bufferSize  = XMLHandler.getTagValue(stepnode, "buffer_size");
			headerPresent = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
			lazyConversionActive= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "lazy_conversion"));

			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields = XMLHandler.countNodes(fields, "field");
			
			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				inputFields[i] = new TextFileInputField();
				
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

				inputFields[i].setName( XMLHandler.getTagValue(fnode, "name") );
				inputFields[i].setType(  ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")) );
				inputFields[i].setFormat( XMLHandler.getTagValue(fnode, "format") );
				inputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
				inputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
				inputFields[i].setGroupSymbol( XMLHandler.getTagValue(fnode, "group") );
				inputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
				inputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
				inputFields[i].setTrimType( ValueMeta.getTrimTypeByCode( XMLHandler.getTagValue(fnode, "trim_type") ) );
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

		retval.append("    ").append(XMLHandler.addTagValue("filename", filename));
		retval.append("    ").append(XMLHandler.addTagValue("separator", delimiter));
		retval.append("    ").append(XMLHandler.addTagValue("enclosure", enclosure));
		retval.append("    ").append(XMLHandler.addTagValue("header", headerPresent));
		retval.append("    ").append(XMLHandler.addTagValue("buffer_size", bufferSize));
		retval.append("    ").append(XMLHandler.addTagValue("lazy_conversion", lazyConversionActive));

		retval.append("    <fields>").append(Const.CR);
		for (int i = 0; i < inputFields.length; i++)
		{
			TextFileInputField field = inputFields[i];
			
			retval.append("      <field>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("name", field.getName()));
			retval.append("        ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(field.getType())));
			retval.append("        ").append(XMLHandler.addTagValue("format", field.getFormat()));
			retval.append("        ").append(XMLHandler.addTagValue("currency", field.getCurrencySymbol()));
			retval.append("        ").append(XMLHandler.addTagValue("decimal", field.getDecimalSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue("group", field.getGroupSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue("length", field.getLength()));
			retval.append("        ").append(XMLHandler.addTagValue("precision", field.getPrecision()));
			retval.append("        ").append(XMLHandler.addTagValue("trim_type", ValueMeta.getTrimTypeCode(field.getTrimType())));
			retval.append("      </field>").append(Const.CR);
		}
		retval.append("    </fields>").append(Const.CR);

		return retval.toString();
	}


	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			filename = rep.getStepAttributeString(id_step, "filename");
			delimiter = rep.getStepAttributeString(id_step, "separator");
			enclosure = rep.getStepAttributeString(id_step, "enclosure");
			headerPresent = rep.getStepAttributeBoolean(id_step, "header");
			bufferSize = rep.getStepAttributeString(id_step, "buffer_size");
			lazyConversionActive = rep.getStepAttributeBoolean(id_step, "lazy_conversion");
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				inputFields[i] = new TextFileInputField();
				
				inputFields[i].setName( rep.getStepAttributeString(id_step, i, "field_name") );
				inputFields[i].setType( ValueMeta.getType(rep.getStepAttributeString(id_step, i, "field_type")) );
				inputFields[i].setFormat( rep.getStepAttributeString(id_step, i, "field_format") );
				inputFields[i].setCurrencySymbol( rep.getStepAttributeString(id_step, i, "field_currency") );
				inputFields[i].setDecimalSymbol( rep.getStepAttributeString(id_step, i, "field_decimal") );
				inputFields[i].setGroupSymbol( rep.getStepAttributeString(id_step, i, "field_group") );
				inputFields[i].setLength( (int) rep.getStepAttributeInteger(id_step, i, "field_length") );
				inputFields[i].setPrecision( (int) rep.getStepAttributeInteger(id_step, i, "field_precision") );
				inputFields[i].setTrimType( ValueMeta.getTrimTypeByCode( rep.getStepAttributeString(id_step, i, "field_trim_type")) );
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "filename", filename);
			rep.saveStepAttribute(id_transformation, id_step, "separator", delimiter);
			rep.saveStepAttribute(id_transformation, id_step, "enclosure", enclosure);
			rep.saveStepAttribute(id_transformation, id_step, "buffer_size", bufferSize);
			rep.saveStepAttribute(id_transformation, id_step, "header", headerPresent);
			rep.saveStepAttribute(id_transformation, id_step, "lazy_conversion", lazyConversionActive);

			for (int i = 0; i < inputFields.length; i++)
			{
				TextFileInputField field = inputFields[i];
				
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name", field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type", ValueMeta.getTypeDesc(field.getType()));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format", field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency", field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group", field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length", field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type", ValueMeta.getTrimTypeCode( field.getTrimType()));
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
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
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("CsvInputMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("CsvInputMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("CsvInputMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("CsvInputMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
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
}