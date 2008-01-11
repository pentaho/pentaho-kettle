package org.pentaho.di.trans.steps.validator;

import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class ValidatorField implements Cloneable {
	public static final String XML_TAG = "validator_field";

	private String name;
	
	private int maximumLength;
	private int minimumLength;
	private boolean limitingToMaximum;
	private boolean paddedToMinimum;
	private String  paddingString;
	
	private boolean nullAllowed;

	private int    dataType;
	private String conversionMask;
	private String decimalSymbol;
	private String groupingSymbol;

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
		return null; // TODO FIXME
	}

	public ValidatorField(Node calcnode) {
		this();
		// TODO FIXME
	}
	
	public ValidatorField(Repository rep, long id_step, int i) {
		// TODO FIXME
	}

	public void saveRep(Repository rep, long id_transformation, long id_step, int i) {
		// TODO FIXME
		
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
	 * @return the limitingToMaximum
	 */
	public boolean isLimitingToMaximum() {
		return limitingToMaximum;
	}

	/**
	 * @param limitingToMaximum the limitingToMaximum to set
	 */
	public void setLimitingToMaximum(boolean limitingToMaximum) {
		this.limitingToMaximum = limitingToMaximum;
	}

	/**
	 * @return the paddedToMinimum
	 */
	public boolean isPaddedToMinimum() {
		return paddedToMinimum;
	}

	/**
	 * @param paddedToMinimum the paddedToMinimum to set
	 */
	public void setPaddedToMinimum(boolean paddedToMinimum) {
		this.paddedToMinimum = paddedToMinimum;
	}

	/**
	 * @return the paddingString
	 */
	public String getPaddingString() {
		return paddingString;
	}

	/**
	 * @param paddingString the paddingString to set
	 */
	public void setPaddingString(String paddingString) {
		this.paddingString = paddingString;
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

}
