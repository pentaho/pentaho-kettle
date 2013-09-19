package org.pentaho.di.trans.step;

public interface StepMetaInjectionEnumEntry {
  
  public String name();
  
  /**
   * @return the valueType
   */
  public int getValueType();

  /**
   * @return the description
   */
  public String getDescription();
  
  /**
   * @return The parent entry
   */
  public StepMetaInjectionEnumEntry getParent();
}
