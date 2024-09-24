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
 * Wsdl operation fault abstraction.
 */
public final class WsdlOpFault extends WsdlOpReturnType implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private final QName _name;
  private final boolean _complexType;
  private final boolean _elementFormQualified;

  /**
   * Create a new WsdlOpFault instance.
   *
   * @param name
   *          QName of the parameter.
   * @param xmlType
   *          XML type of the parameter.
   * @param wsdlTypes
   *          Wsdl type information.
   */
  protected WsdlOpFault( String name, QName xmlType, boolean isComplex, WsdlTypes wsdlTypes ) {

    _name = wsdlTypes.getTypeQName( name );
    _elementFormQualified = wsdlTypes.isElementFormQualified( _name.getNamespaceURI() );
    _xmlType = xmlType;
    _complexType = isComplex;
  }

  /**
   * Get the name of this fault.
   *
   * @return QName.
   */
  public QName getName() {
    return _name;
  }

  /**
   * Is the XML type a complex type?
   *
   * @return true if xmltype is a complex type.
   */
  public boolean isComplexType() {
    return _complexType;
  }

  /**
   * Is this element part of an element form qualifed schema?
   *
   * @return true if it is.
   */
  public boolean isFaultNameElementFormQualified() {
    return _elementFormQualified;
  }
}
