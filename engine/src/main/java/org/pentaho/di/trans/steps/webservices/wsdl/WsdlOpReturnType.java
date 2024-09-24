/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.webservices.wsdl;

import javax.xml.namespace.QName;

/**
 * Represents the return value for a WSDL operation.
 */
public class WsdlOpReturnType implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  protected QName _xmlType;

  protected boolean _isArray;

  // The xmlType of an array's items
  protected QName _itemXmlType;

  protected ComplexType _itemComplexType;

  /**
   * Constructor.
   */
  protected WsdlOpReturnType() {
  }

  /**
   * Get the Xml type.
   *
   * @return QName for the XML type.
   */
  public QName getXmlType() {
    return _xmlType;
  }

  /**
   * If the return type is an array, get the xml type of the items in the array.
   *
   * @return QName for the item XML type, null if not an array.
   */
  public QName getItemXmlType() {
    return _itemXmlType;
  }

  /**
   * Is this an array type?
   *
   * @return true if this is an array type.
   */
  public boolean isArray() {
    return _isArray;
  }

  public ComplexType getItemComplexType() {
    return _itemComplexType;
  }
}
