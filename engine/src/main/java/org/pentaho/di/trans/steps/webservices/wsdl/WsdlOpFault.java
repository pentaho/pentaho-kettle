/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
