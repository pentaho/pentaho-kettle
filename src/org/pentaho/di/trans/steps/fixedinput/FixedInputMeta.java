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
 
package org.pentaho.di.trans.steps.fixedinput;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/**
 * @since 2007-07-05
 * @author matt
 * @version 3.0
 */

@Step(name="FixedInput",image="TFI.png",tooltip="BaseStep.TypeTooltipDesc.FixedInput",description="BaseStep.TypeLongDesc.FixedInput",
		category=StepCategory.INPUT)
public class FixedInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String filename;
	
	private boolean headerPresent;

	private String lineWidth;

	private String bufferSize;
	
	private boolean lazyConversionActive;

	private boolean lineFeedPresent;

	// TODO: wrap these field* members in a new class...
	//
	private String[] fieldNames;
	private int      fieldTypes[];
	private int      fieldWidth[];
	private int      fieldLength[];
	private int      fieldPrecision[];
	private String   fieldFormat[];
	private String   fieldDecimal[];
	private String   fieldGrouping[];
	private String   fieldCurrency[];
	
	public FixedInputMeta()
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
		lineWidth = "80"  ;
		headerPresent = true;
		lazyConversionActive=true;
		bufferSize="50000";
		lineFeedPresent=true;
	}
	
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			filename = XMLHandler.getTagValue(stepnode, "filename");
			lineWidth = XMLHandler.getTagValue(stepnode, "line_width");
			bufferSize  = XMLHandler.getTagValue(stepnode, "buffer_size");
			headerPresent = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "header"));
			lineFeedPresent = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "line_feed"));
			lazyConversionActive= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "lazy_conversion"));

			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields = XMLHandler.countNodes(fields, "field");
			
			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

				fieldNames[i] = XMLHandler.getTagValue(fnode, "name");
				fieldTypes[i] = ValueMeta.getType(XMLHandler.getTagValue(fnode, "type"));
				fieldFormat[i] = XMLHandler.getTagValue(fnode, "format");
				fieldCurrency[i] = XMLHandler.getTagValue(fnode, "currency");
				fieldDecimal[i] = XMLHandler.getTagValue(fnode, "decimal");
				fieldGrouping[i] = XMLHandler.getTagValue(fnode, "group");
				fieldWidth[i] = Const.toInt(XMLHandler.getTagValue(fnode, "width"), -1);
				fieldLength[i] = Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1);
				fieldPrecision[i] = Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1);
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrFields) {
		fieldNames = new String[nrFields];
		fieldTypes = new int[nrFields];
		fieldWidth = new int[nrFields];
		fieldLength = new int[nrFields];
		fieldPrecision = new int[nrFields];
		fieldFormat = new String[nrFields];
		fieldDecimal = new String[nrFields];
		fieldGrouping = new String[nrFields];
		fieldCurrency = new String[nrFields];
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer();

		retval.append("    " + XMLHandler.addTagValue("filename", filename));
		retval.append("    " + XMLHandler.addTagValue("line_width", lineWidth));
		retval.append("    " + XMLHandler.addTagValue("header", headerPresent));
		retval.append("    " + XMLHandler.addTagValue("buffer_size", bufferSize));
		retval.append("    " + XMLHandler.addTagValue("lazy_conversion", lazyConversionActive));
		retval.append("    " + XMLHandler.addTagValue("line_feed", lineFeedPresent));

		retval.append("    <fields>" + Const.CR);
		for (int i = 0; i < fieldNames.length; i++)
		{
			retval.append("      <field>" + Const.CR);
			retval.append("        " + XMLHandler.addTagValue("name", fieldNames[i]));
			retval.append("        " + XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(fieldTypes[i])));
			retval.append("        " + XMLHandler.addTagValue("format", fieldFormat[i]));
			retval.append("        " + XMLHandler.addTagValue("currency", fieldCurrency[i]));
			retval.append("        " + XMLHandler.addTagValue("decimal", fieldDecimal[i]));
			retval.append("        " + XMLHandler.addTagValue("group", fieldGrouping[i]));
			retval.append("        " + XMLHandler.addTagValue("width", fieldWidth[i]));
			retval.append("        " + XMLHandler.addTagValue("length", fieldLength[i]));
			retval.append("        " + XMLHandler.addTagValue("precision", fieldPrecision[i]));
			retval.append("        </field>" + Const.CR);
		}
		retval.append("      </fields>" + Const.CR);

		return retval.toString();
	}


	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			filename = rep.getStepAttributeString(id_step, "filename");
			lineWidth = rep.getStepAttributeString(id_step, "line_width");
			headerPresent = rep.getStepAttributeBoolean(id_step, "header");
			lineFeedPresent = rep.getStepAttributeBoolean(id_step, "line_feed");
			bufferSize = rep.getStepAttributeString(id_step, "buffer_size");
			lazyConversionActive = rep.getStepAttributeBoolean(id_step, "lazy_conversion");
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++)
			{
				fieldNames[i] = rep.getStepAttributeString(id_step, i, "field_name");
				fieldTypes[i] = ValueMeta.getType(rep.getStepAttributeString(id_step, i, "field_type"));
				fieldFormat[i] = rep.getStepAttributeString(id_step, i, "field_format");
				fieldCurrency[i] = rep.getStepAttributeString(id_step, i, "field_currency");
				fieldDecimal[i] = rep.getStepAttributeString(id_step, i, "field_decimal");
				fieldGrouping[i] = rep.getStepAttributeString(id_step, i, "field_group");
				fieldWidth[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_width");
				fieldLength[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_precision");
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
			rep.saveStepAttribute(id_transformation, id_step, "line_width", lineWidth);
			rep.saveStepAttribute(id_transformation, id_step, "buffer_size", bufferSize);
			rep.saveStepAttribute(id_transformation, id_step, "header", headerPresent);
			rep.saveStepAttribute(id_transformation, id_step, "lazy_conversion", lazyConversionActive);
			rep.saveStepAttribute(id_transformation, id_step, "line_feed", lineFeedPresent);

			for (int i = 0; i < fieldNames.length; i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldNames[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type", ValueMeta.getTypeDesc(fieldTypes[i]));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format", fieldFormat[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency", fieldCurrency[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal", fieldDecimal[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group", fieldGrouping[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_width", fieldWidth[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length", fieldLength[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]);
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<fieldNames.length;i++) {
			ValueMetaInterface valueMeta = new ValueMeta(fieldNames[i], fieldTypes[i]);
			valueMeta.setLength(fieldLength[i]);
			valueMeta.setPrecision(fieldPrecision[i]);
			valueMeta.setConversionMask(fieldFormat[i]);
			valueMeta.setDecimalSymbol(fieldDecimal[i]);
			valueMeta.setGroupingSymbol(fieldGrouping[i]);
			valueMeta.setCurrencySymbol(fieldCurrency[i]);
			if (lazyConversionActive) valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			
			// In case we want to convert Strings...
			//
			ValueMetaInterface storageMetadata = (ValueMetaInterface) valueMeta.clone();
			storageMetadata.setType(ValueMetaInterface.TYPE_STRING);
			storageMetadata.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			
			valueMeta.setStorageMetadata(storageMetadata);
			
			valueMeta.setOrigin(origin);
			
			rowMeta.addValueMeta(valueMeta);
		}
	}
	
	public void check(List<CheckResult> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FixedInputMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FixedInputMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FixedInputMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FixedInputMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new FixedInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new FixedInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new FixedInputData();
	}

	/**
	 * @return the fieldNames
	 */
	public String[] getFieldNames() {
		return fieldNames;
	}

	/**
	 * @param fieldNames the fieldNames to set
	 */
	public void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
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
	 * @return the fieldTypes
	 */
	public int[] getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * @return the fieldFormat
	 */
	public String[] getFieldFormat() {
		return fieldFormat;
	}

	/**
	 * @param fieldTypes the fieldTypes to set
	 */
	public void setFieldTypes(int[] fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	/**
	 * @param fieldFormat the fieldFormat to set
	 */
	public void setFieldFormat(String[] fieldFormat) {
		this.fieldFormat = fieldFormat;
	}

	/**
	 * @return the fieldDecimal
	 */
	public String[] getFieldDecimal() {
		return fieldDecimal;
	}

	/**
	 * @return the fieldGrouping
	 */
	public String[] getFieldGrouping() {
		return fieldGrouping;
	}

	/**
	 * @return the fieldCurrency
	 */
	public String[] getFieldCurrency() {
		return fieldCurrency;
	}

	/**
	 * @param fieldDecimal the fieldDecimal to set
	 */
	public void setFieldDecimal(String[] fieldDecimal) {
		this.fieldDecimal = fieldDecimal;
	}

	/**
	 * @param fieldGrouping the fieldGrouping to set
	 */
	public void setFieldGrouping(String[] fieldGrouping) {
		this.fieldGrouping = fieldGrouping;
	}

	/**
	 * @param fieldCurrency the fieldCurrency to set
	 */
	public void setFieldCurrency(String[] fieldCurrency) {
		this.fieldCurrency = fieldCurrency;
	}

	/**
	 * @return the fieldLength
	 */
	public int[] getFieldLength() {
		return fieldLength;
	}

	/**
	 * @return the fieldPrecision
	 */
	public int[] getFieldPrecision() {
		return fieldPrecision;
	}

	/**
	 * @param fieldLength the fieldLength to set
	 */
	public void setFieldLength(int[] fieldLength) {
		this.fieldLength = fieldLength;
	}

	/**
	 * @param fieldPrecision the fieldPrecision to set
	 */
	public void setFieldPrecision(int[] fieldPrecision) {
		this.fieldPrecision = fieldPrecision;
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
	 * @return the lineWidth
	 */
	public String getLineWidth() {
		return lineWidth;
	}

	/**
	 * @return the lineFeedPresent
	 */
	public boolean isLineFeedPresent() {
		return lineFeedPresent;
	}

	/**
	 * @return the fieldWidth
	 */
	public int[] getFieldWidth() {
		return fieldWidth;
	}

	/**
	 * @param lineWidth the lineWidth to set
	 */
	public void setLineWidth(String lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * @param lineFeedPresent the lineFeedPresent to set
	 */
	public void setLineFeedPresent(boolean lineFeedPresent) {
		this.lineFeedPresent = lineFeedPresent;
	}

	/**
	 * @param fieldWidth the fieldWidth to set
	 */
	public void setFieldWidth(int[] fieldWidth) {
		this.fieldWidth = fieldWidth;
	}


}
