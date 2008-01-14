package org.pentaho.di.trans.steps.validator;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class ValidatorField implements Cloneable {
	public static final String XML_TAG = "validator_field";
	public static final String XML_TAG_ALLOWED = "allowed_value";

	private String name;
	
	private int maximumLength;
	private int minimumLength;
	
	private boolean nullAllowed;

	private int     dataType;
	private boolean dataTypeVerified;
	private String  conversionMask;
	private String  decimalSymbol;
	private String  groupingSymbol;

	private String   minimumValue;
	private String   maximumValue;
	private String[] allowedValues;
	
	public ValidatorField() {
		maximumLength=-1;
		minimumLength=-1;
		nullAllowed=true;
	}
	
	public ValidatorField(String name) {
		this();
		this.name = name;
		
	}
	
	@Override
	public ValidatorField clone() {
		try {
			return (ValidatorField) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public String getXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append(XMLHandler.openTag(XML_TAG));
		
		xml.append(XMLHandler.addTagValue("name", name));
		xml.append(XMLHandler.addTagValue("max_length", maximumLength));
		xml.append(XMLHandler.addTagValue("min_length", minimumLength));

		xml.append(XMLHandler.addTagValue("null_allowed", nullAllowed));

		xml.append(XMLHandler.addTagValue("data_type", ValueMeta.getTypeDesc(dataType)));
		xml.append(XMLHandler.addTagValue("data_type_verified", dataTypeVerified));
		xml.append(XMLHandler.addTagValue("conversion_mask", conversionMask));
		xml.append(XMLHandler.addTagValue("decimal_symbol", decimalSymbol));
		xml.append(XMLHandler.addTagValue("grouping_symbol", groupingSymbol));

		xml.append(XMLHandler.addTagValue("max_value", maximumValue));
		xml.append(XMLHandler.addTagValue("min_value", minimumValue));
		
		xml.append(XMLHandler.openTag(XML_TAG_ALLOWED));
		if (allowedValues!=null) {
				
			for (String allowedValue : allowedValues) {
				xml.append(XMLHandler.addTagValue("value", allowedValue));
			}
		}
		xml.append(XMLHandler.closeTag(XML_TAG_ALLOWED));

		xml.append(XMLHandler.closeTag(XML_TAG));
		
		return xml.toString();
	}

	public ValidatorField(Node calcnode) {
		this();

		name = XMLHandler.getTagValue(calcnode, "name");
		maximumLength = Const.toInt(XMLHandler.getTagValue(calcnode, "max_length"), -1);
		minimumLength = Const.toInt(XMLHandler.getTagValue(calcnode, "min_length"), -1);

		nullAllowed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "null_allowed"));

		dataType = ValueMeta.getType( XMLHandler.getTagValue(calcnode, "data_type") );
		dataTypeVerified = "Y".equalsIgnoreCase( XMLHandler.getTagValue(calcnode, "data_type_verified"));
		conversionMask = XMLHandler.getTagValue(calcnode, "conversion_mask");
		decimalSymbol = XMLHandler.getTagValue(calcnode, "decimal_symbol");
		groupingSymbol = XMLHandler.getTagValue(calcnode, "grouping_symbol");

		minimumValue = XMLHandler.getTagValue(calcnode, "min_value");
		maximumValue = XMLHandler.getTagValue(calcnode, "max_value");

		Node allowedValuesNode = XMLHandler.getSubNode(calcnode, XML_TAG_ALLOWED);
		int nrValues = XMLHandler.countNodes(allowedValuesNode, "value");
		allowedValues = new String[nrValues];
		for (int i=0;i<nrValues;i++) {
			Node allowedNode = XMLHandler.getSubNodeByNr(allowedValuesNode, "value", i);
			allowedValues[i] = XMLHandler.getNodeValue(allowedNode);
		}
	}
	
	public ValidatorField(Repository rep, long id_step, int i) throws KettleException {
		name = rep.getStepAttributeString(id_step, i, "validator_field_name");
		
		maximumLength = (int)rep.getStepAttributeInteger(id_step, i, "validator_field_max_length");
		minimumLength = (int)rep.getStepAttributeInteger(id_step, i, "validator_field_min_length");

		nullAllowed = rep.getStepAttributeBoolean(id_step, i, "validator_field_null_allowed");

		dataType = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "validator_field_data_type") );
		dataTypeVerified = rep.getStepAttributeBoolean(id_step, i, "validator_field_data_type_verified");
		conversionMask = rep.getStepAttributeString(id_step, i, "validator_field_conversion_mask");
		decimalSymbol = rep.getStepAttributeString(id_step, i, "validator_field_decimal_symbol");
		groupingSymbol = rep.getStepAttributeString(id_step, i, "validator_field_grouping_symbol");

		minimumValue = rep.getStepAttributeString(id_step, i, "validator_field_min_value");
		maximumValue = rep.getStepAttributeString(id_step, i, "validator_field_max_value");

		List<String> allowed = new ArrayList<String>();
		
		int nr = 1;
		String value = rep.getStepAttributeString(id_step, i, "validator_field_value_"+nr);
		while (value!=null) {
			allowed.add(value);
			nr++;
			value = rep.getStepAttributeString(id_step, i, "validator_field_value_"+nr);
		}
		allowedValues = allowed.toArray(new String[allowed.size()]);
	}

	public void saveRep(Repository rep, long id_transformation, long id_step, int i) throws KettleException {
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_name", name);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_max_length", maximumLength);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_min_length", minimumLength);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_null_allowed", nullAllowed);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_data_type", ValueMeta.getTypeDesc(dataType));
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_data_type_verified", dataTypeVerified);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_conversion_mask", conversionMask);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_decimal_symbol", decimalSymbol);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_grouping_symbol", groupingSymbol);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_max_value", maximumValue);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_min_value", minimumValue);
		
		if (allowedValues!=null) {
			for (int nr=1;nr<=allowedValues.length;nr++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_value_"+nr, allowedValues[nr-1]);
			}
		}

	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the maximumLength
	 */
	public int getMaximumLength() {
		return maximumLength;
	}

	/**
	 * @param maximumLength the maximumLength to set
	 */
	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	/**
	 * @return the minimumLength
	 */
	public int getMinimumLength() {
		return minimumLength;
	}

	/**
	 * @param minimumLength the minimumLength to set
	 */
	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

	/**
	 * @return the nullAllowed
	 */
	public boolean isNullAllowed() {
		return nullAllowed;
	}

	/**
	 * @param nullAllowed the nullAllowed to set
	 */
	public void setNullAllowed(boolean nullAllowed) {
		this.nullAllowed = nullAllowed;
	}

	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the conversionMask
	 */
	public String getConversionMask() {
		return conversionMask;
	}

	/**
	 * @param conversionMask the conversionMask to set
	 */
	public void setConversionMask(String conversionMask) {
		this.conversionMask = conversionMask;
	}

	/**
	 * @return the decimalSymbol
	 */
	public String getDecimalSymbol() {
		return decimalSymbol;
	}

	/**
	 * @param decimalSymbol the decimalSymbol to set
	 */
	public void setDecimalSymbol(String decimalSymbol) {
		this.decimalSymbol = decimalSymbol;
	}

	/**
	 * @return the groupingSymbol
	 */
	public String getGroupingSymbol() {
		return groupingSymbol;
	}

	/**
	 * @param groupingSymbol the groupingSymbol to set
	 */
	public void setGroupingSymbol(String groupingSymbol) {
		this.groupingSymbol = groupingSymbol;
	}

	/**
	 * @return the minimumValue
	 */
	public String getMinimumValue() {
		return minimumValue;
	}

	/**
	 * @param minimumValue the minimumValue to set
	 */
	public void setMinimumValue(String minimumValue) {
		this.minimumValue = minimumValue;
	}

	/**
	 * @return the maximumValue
	 */
	public String getMaximumValue() {
		return maximumValue;
	}

	/**
	 * @param maximumValue the maximumValue to set
	 */
	public void setMaximumValue(String maximumValue) {
		this.maximumValue = maximumValue;
	}

	/**
	 * @return the allowedValues
	 */
	public String[] getAllowedValues() {
		return allowedValues;
	}

	/**
	 * @param allowedValues the allowedValues to set
	 */
	public void setAllowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	/**
	 * @return the dataTypeVerified
	 */
	public boolean isDataTypeVerified() {
		return dataTypeVerified;
	}

	/**
	 * @param dataTypeVerified the dataTypeVerified to set
	 */
	public void setDataTypeVerified(boolean dataTypeVerified) {
		this.dataTypeVerified = dataTypeVerified;
	}
}
