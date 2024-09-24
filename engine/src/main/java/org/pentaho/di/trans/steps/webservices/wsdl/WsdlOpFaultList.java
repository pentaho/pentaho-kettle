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

import java.util.ArrayList;
import java.util.Map;

import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.pentaho.di.core.exception.KettleStepException;
import org.w3c.dom.Element;

/**
 * WsdlOpFaultList represents the list of parameters for a WSDL operaton.
 */
public final class WsdlOpFaultList extends ArrayList<WsdlOpFault> {

  private static final long serialVersionUID = 1L;

  private final WsdlTypes _wsdlTypes;

  /**
   * Constructor.
   *
   * @param wsdlTypes
   *          Type information from the WSDL.
   */
  protected WsdlOpFaultList( WsdlTypes wsdlTypes ) {
    _wsdlTypes = wsdlTypes;
  }

  /**
   * Add a fault to this list.
   *
   * @param fault
   *          Fault to add.
   * @return true if this collection was modified as a result of this call.
   */
  protected boolean add( Fault fault ) throws KettleStepException {
    return add( getFault( fault ) );
  }

  /**
   * Create a WsdlOpFault from the Fault.
   *
   * @param fault
   *          Fault to process.
   * @return WsdlOpFault Result of processing.
   */
  @SuppressWarnings( "unchecked" )
  private WsdlOpFault getFault( Fault fault ) throws KettleStepException {
    Message m = fault.getMessage();

    // a fault should only have one message part.
    Map<?, Part> partMap = m.getParts();
    if ( partMap.size() != 1 ) {
      throw new IllegalArgumentException( "Invalid part count for fault!!" );
    }
    Part faultPart = partMap.values().iterator().next();
    boolean complexType = false;

    // type of fault is specified either in Part's type or element attribute.
    QName type = faultPart.getTypeName();
    if ( type == null ) {
      type = faultPart.getElementName();
      Element schemaElement = _wsdlTypes.findNamedElement( type );
      type = _wsdlTypes.getTypeQName( schemaElement.getAttribute( "type" ) );
      complexType = true;
    }
    return new WsdlOpFault( fault.getName(), type, complexType, _wsdlTypes );
  }
}
