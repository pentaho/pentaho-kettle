/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.selectvalues;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;

public class SelectMetadataChange implements Cloneable, XMLInterface{
	
	public static final String XML_TAG = "meta";
	
	// META-DATA mode
	/** Fields of which we want to change the meta data  */
	private String name;
	/** Meta: new name of field  */
	private String rename;
	/** Meta: new Value type for this field or TYPE_NONE if no change needed!  */
	private int    type;
	/** Meta: new length of field  */
	private int    length;
	/** Meta: new precision of field (for numbers)  */
	private int    precision;
	/** Meta: the storage type, NORMAL or BINARY_STRING  */
	private int    storageType;
	/** The conversion metadata if any conversion needs to take place */
	private String conversionMask;
	/** The decimal symbol for number conversions */
	private String decimalSymbol; 
	/** The grouping symbol for number conversions */
	private String groupingSymbol; 
	/** The currency symbol for number conversions */
	private String currencySymbol;
	/** The encoding to use when decoding binary data to Strings */
  private String encoding;
	
	public SelectMetadataChange() {
		storageType=-1; // storage type is not used by default!
	}

	/**
	 * @param name
	 * @param rename
	 * @param type
	 * @param length
	 * @param precision
	 * @param storageType
	 * @param conversionMask
	 * @param decimalSymbol
	 * @param groupingSymbol
	 * @param currencySymbol
	 */
	public SelectMetadataChange(String name, String rename, int type, int length, int precision, int storageType,
			String conversionMask, String decimalSymbol, String groupingSymbol, String currencySymbol) {
		this.name = name;
		this.rename = rename;
		this.type = type;
		this.length = length;
		this.precision = precision;
		this.storageType = storageType;
		this.conversionMask = conversionMask;
		this.decimalSymbol = decimalSymbol;
		this.groupingSymbol = groupingSymbol;
		this.currencySymbol = currencySymbol;
	}
	
	public String getXML() {
		StringBuffer retval = new StringBuffer();
		retval.append("      ").append(XMLHandler.openTag(XML_TAG)); //$NON-NLS-1$
		retval.append("        ").append(XMLHandler.addTagValue("name",            name)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("rename",          rename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("type",            ValueMeta.getTypeDesc(type)) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("length",          length)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("precision",       precision)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("conversion_mask", conversionMask)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("encoding",        encoding)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("decimal_symbol",  decimalSymbol)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("grouping_symbol", groupingSymbol)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        ").append(XMLHandler.addTagValue("currency_symbol", currencySymbol)); //$NON-NLS-1$ //$NON-NLS-2$		
		retval.append("        ").append(XMLHandler.addTagValue("storage_type",    ValueMeta.getStorageTypeCode(storageType))); //$NON-NLS-1$ //$NON-NLS-2$		
		retval.append("      ").append(XMLHandler.closeTag(XML_TAG)); //$NON-NLS-1$
		return retval.toString();
	}
	
	public SelectMetadataChange(Node metaNode) {
		name           = XMLHandler.getTagValue(metaNode, "name"); //$NON-NLS-1$
		rename         = XMLHandler.getTagValue(metaNode, "rename"); //$NON-NLS-1$
		type           = ValueMeta.getType(XMLHandler.getTagValue(metaNode, "type")); //$NON-NLS-1$
		length         = Const.toInt(XMLHandler.getTagValue(metaNode, "length"), -2); //$NON-NLS-1$
		precision      = Const.toInt(XMLHandler.getTagValue(metaNode, "precision"), -2); //$NON-NLS-1$
		storageType    = ValueMeta.getStorageType( XMLHandler.getTagValue(metaNode, "storage_type") ); //$NON-NLS-1$
		conversionMask = XMLHandler.getTagValue(metaNode, "conversion_mask"); //$NON-NLS-1$
		encoding       = XMLHandler.getTagValue(metaNode, "encoding"); //$NON-NLS-1$
		decimalSymbol  = XMLHandler.getTagValue(metaNode, "decimal_symbol"); //$NON-NLS-1$
		groupingSymbol = XMLHandler.getTagValue(metaNode, "grouping_symbol"); //$NON-NLS-1$
		currencySymbol = XMLHandler.getTagValue(metaNode, "currency_symbol"); //$NON-NLS-1$
	}
	
	public SelectMetadataChange clone() {
		try {
			return (SelectMetadataChange) super.clone();
		}
		catch(CloneNotSupportedException e) {
			return null;
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
	 * @return the rename
	 */
	public String getRename() {
		return rename;
	}
	
	/**
	 * @param rename the rename to set
	 */
	public void setRename(String rename) {
		this.rename = rename;
	}
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * @return the precision
	 */
	public int getPrecision() {
		return precision;
	}
	
	/**
	 * @param precision the precision to set
	 */
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	
	/**
	 * @return the storageType
	 */
	public int getStorageType() {
		return storageType;
	}
	
	/**
	 * @param storageType the storageType to set
	 */
	public void setStorageType(int storageType) {
		this.storageType = storageType;
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
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	
	/**
	 * @param currencySymbol the currencySymbol to set
	 */
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	/**
	 * @return the encoding to use when decoding binary data to strings
	 */
  public String getEncoding() {
    return encoding;
  }
  
  /**
   * @param encoding the encoding to use when decoding binary data to strings
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
	
	
}
