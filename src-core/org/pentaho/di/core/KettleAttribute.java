package org.pentaho.di.core;


public class KettleAttribute implements KettleAttributeInterface {
  private String key;

  private String xmlCode;
  private String repCode;
  private String description;
  private String tooltip;
  private int    type;
  private KettleAttributeInterface parent;
  
  /**
   * @param key
   * @param xmlCode
   * @param repCode
   * @param description
   * @param tooltip
   * @param type
   */
  public KettleAttribute(String key, String xmlCode, String repCode, String description, String tooltip, int type, KettleAttributeInterface parent) {
    this.key = key;
    this.xmlCode = xmlCode;
    this.repCode = repCode;
    this.description = description;
    this.tooltip = tooltip;
    this.type = type;
    this.parent = parent;
  }

  public String getKey() {
    return key;
  }
  
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the xmlCode
   */
  public String getXmlCode() {
    return xmlCode;
  }

  /**
   * @param xmlCode the xmlCode to set
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
   * @param repCode the repCode to set
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
   * @param description the description to set
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
   * @param tooltip the tooltip to set
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
   * @param type the type to set
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * @return the parent
   */
  public KettleAttributeInterface getParent() {
    return parent;
  }

  /**
   * @param parent the parent to set
   */
  public void setParent(KettleAttributeInterface parent) {
    this.parent = parent;
  }
}
