package org.pentaho.di.core;

/**
 * This class described a kettle attribute.
 * 
 * @author matt
 * 
 */
public class KettleAttribute {
  private String xmlCode;
  private String repCode;
  private String description;
  private String tooltip;
  private int    type;

  /**
   * @param xmlCode
   * @param repCode
   * @param description
   * @param tooltip
   * @param type
   */
  private KettleAttribute(String xmlCode, String repCode, String description, String tooltip, int type) {
    this.xmlCode = xmlCode;
    this.repCode = repCode;
    this.description = description;
    this.tooltip = tooltip;
    this.type = type;
  }

  /**
   * @param xmlCode
   * @param description
   * @param tooltip
   * @param type
   */
  private KettleAttribute(String code, String description, String tooltip, int type) {
    this(code, code, description, tooltip, type);
  }

  /**
   * @return the xmlCode
   */
  public String getXmlCode() {
    return xmlCode;
  }

  /**
   * @param xmlCode
   *          the xmlCode to set
   */
  public void setXmlCode(String xmlCode) {
    this.xmlCode = xmlCode;
  }

  /**
   * @return the repCode
   */
  public String getRepCode() {
    return repCode;
  }

  /**
   * @param repCode
   *          the repCode to set
   */
  public void setRepCode(String repCode) {
    this.repCode = repCode;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the tooltip
   */
  public String getTooltip() {
    return tooltip;
  }

  /**
   * @param tooltip
   *          the tooltip to set
   */
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  /**
   * @return the type
   */
  public int getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(int type) {
    this.type = type;
  }

}
