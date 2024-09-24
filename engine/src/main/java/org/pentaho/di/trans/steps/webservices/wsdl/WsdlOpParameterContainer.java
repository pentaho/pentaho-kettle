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

import org.pentaho.di.trans.steps.webservices.WebServiceMeta;

public class WsdlOpParameterContainer implements WsdlParamContainer {
  private WsdlOpParameter parameter;

  public WsdlOpParameterContainer( WsdlOpParameter parameter ) {
    this.parameter = parameter;
  }

  public String getContainerName() {
    return parameter.getName().getLocalPart();
  }

  public String[] getParamNames() {
    if ( parameter.isArray() ) {
      if ( parameter.getItemComplexType() != null ) {
        return parameter.getItemComplexType().getElementNames().toArray(
          new String[parameter.getItemComplexType().getElementNames().size()] );
      } else {
        if ( parameter.getItemXmlType() != null ) {
          return new String[] { parameter.getItemXmlType().getLocalPart() };
        } else {
          return new String[] { parameter.getName().getLocalPart(), };
        }
      }
    } else {
      return new String[] { parameter.getName().getLocalPart() };
    }
  }

  public String getParamType( String paramName ) {
    if ( parameter.isArray() ) {
      if ( parameter.getItemComplexType() != null ) {
        QName name = parameter.getItemComplexType().getElementType( paramName );
        return name == null ? null : name.getLocalPart();
      } else {
        if ( parameter.getItemXmlType() != null ) {
          return parameter.getItemXmlType().getLocalPart();
        } else {
          return null;
        }
      }
    } else if ( paramName.equals( parameter.getName().getLocalPart() ) ) {
      return parameter.getXmlType().getLocalPart();
    } else {
      return null;
    }
  }

  public String getItemName() {
    if ( parameter.isArray() ) {
      if ( parameter.getItemXmlType() != null
        && !WebServiceMeta.XSD_NS_URI.equals( parameter.getItemXmlType().getNamespaceURI() ) ) {
        return parameter.getItemXmlType().getLocalPart();
      } else {
        return null;
      }
    } else {
      return parameter.getName().getLocalPart();
    }
  }

  public boolean isArray() {
    return parameter.isArray();
  }
}
