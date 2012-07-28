package org.pentaho.di.core.jdbc;

import org.pentaho.di.core.row.RowMetaInterface;

public class ThinServiceInformation {
  private String           name;
  private RowMetaInterface serviceFields;

  /**
   * @param name
   * @param serviceFields
   */
  public ThinServiceInformation(String name, RowMetaInterface serviceFields) {
    this.name = name;
    this.serviceFields = serviceFields;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the serviceFields
   */
  public RowMetaInterface getServiceFields() {
    return serviceFields;
  }

  /**
   * @param serviceFields
   *          the serviceFields to set
   */
  public void setServiceFields(RowMetaInterface serviceFields) {
    this.serviceFields = serviceFields;
  }
}
