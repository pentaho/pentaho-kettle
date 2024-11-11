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

import java.util.HashMap;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;

import org.w3c.dom.Element;

/**
 * Represents a map of all named complex types in the WSDL.
 */
public final class WsdlComplexTypes implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private HashMap<String, ComplexType> _complexTypes = new HashMap<String, ComplexType>();

  /**
   * Create a new instance, parse the WSDL file for named complex types.
   *
   * @param wsdlTypes
   *          Name space resolver.
   */
  protected WsdlComplexTypes( WsdlTypes wsdlTypes ) {

    List<ExtensibilityElement> schemas = wsdlTypes.getSchemas();
    for ( ExtensibilityElement schema : schemas ) {
      Element schemaRoot = ( (Schema) schema ).getElement();

      List<Element> types = DomUtils.getChildElementsByName( schemaRoot, WsdlUtils.COMPLEX_TYPE_NAME );
      for ( Element t : types ) {
        String schemaTypeName = t.getAttribute( WsdlUtils.NAME_ATTR );
        _complexTypes.put( schemaTypeName, new ComplexType( t, wsdlTypes ) );
      }
    }
  }

  /**
   * Get the complex type specified by complexTypeName.
   *
   * @param complexTypeName
   *          Name of complex type.
   * @return ComplexType instance, null if complex type was not defined in the wsdl file.
   */
  public ComplexType getComplexType( String complexTypeName ) {
    return _complexTypes.get( complexTypeName );
  }
}
