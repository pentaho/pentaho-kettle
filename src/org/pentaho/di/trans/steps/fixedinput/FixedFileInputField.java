package org.pentaho.di.trans.steps.fixedinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;

public class FixedFileInputField implements Cloneable, XMLInterface {
	
	public static final String XML_TAG = "field";

	private String name;

	private int type;

	private int width;

	private int length;

	private int precision;

	private String format;

	private String decimal;

	private String grouping;

	private String currency;
	
	public FixedFileInputField(Node fnode) {
		name = XMLHandler.getTagValue(fnode, "name");
		type = ValueMeta.getType(XMLHandler.getTagValue(fnode, "type"));
		format = XMLHandler.getTagValue(fnode, "format");
		currency = XMLHandler.getTagValue(fnode, "currency");
		decimal = XMLHandler.getTagValue(fnode, "decimal");
		grouping = XMLHandler.getTagValue(fnode, "group");
		width = Const.toInt(XMLHandler.getTagValue(fnode, "width"), -1);
		length = Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1);
		precision = Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1);
	}

	public FixedFileInputField() {
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("      ").append(XMLHandler.openTag(XML_TAG)).append(Const.CR);
		retval.append("        " + XMLHandler.addTagValue("name", name));
		retval.append("        " + XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(type)));
		retval.append("        " + XMLHandler.addTagValue("format", format));
		retval.append("        " + XMLHandler.addTagValue("currency", currency));
		retval.append("        " + XMLHandler.addTagValue("decimal", decimal));
		retval.append("        " + XMLHandler.addTagValue("group", grouping));
		retval.append("        " + XMLHandler.addTagValue("width", width));
		retval.append("        " + XMLHandler.addTagValue("length", length));
		retval.append("        " + XMLHandler.addTagValue("precision", precision));
		retval.append("        ").append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);

		return retval.toString();
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
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
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
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the decimal
	 */
	public String getDecimal() {
		return decimal;
	}

	/**
	 * @param decimal the decimal to set
	 */
	public void setDecimal(String decimal) {
		this.decimal = decimal;
	}

	/**
	 * @return the grouping
	 */
	public String getGrouping() {
		return grouping;
	}

	/**
	 * @param grouping the grouping to set
	 */
	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}
		
}
