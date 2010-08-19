package org.pentaho.di.core;

public interface KettleAttributeInterface {

  /**
   * @return the key for this attribute, usually the repository code.
   */
  public String getKey();

  /**
   * @return the xmlCode
   */
  public String getXmlCode();
  
  /**
   * @return the repCode
   */
  public String getRepCode();

  /**
   * @return the description
   */
  public String getDescription();

  /**
   * @return the tooltip
   */
  public String getTooltip();

  /**
   * @return the type
   */
  public int getType();
  
  /**
   * @return The parent interface.
   */
  public KettleAttributeInterface getParent();
}
