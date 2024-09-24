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
import java.util.HashSet;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Operation;
import javax.wsdl.Part;

import org.pentaho.di.core.exception.KettleStepException;
import org.w3c.dom.Element;

/**
 * WsdlOpParameterList represents the list of parameters for an operation.
 */
public final class WsdlOpParameterList extends ArrayList<WsdlOpParameter> {

  private static final long serialVersionUID = 1L;
  private final Operation _operation;
  private final WsdlTypes _wsdlTypes;
  private final HashSet<String> _headerNames;

  private WsdlOperation.SOAPParameterStyle _parameterStyle;
  private WsdlOpParameter _returnParam;
  private boolean _outOnly = false;

  /**
   * Constructor.
   *
   * @param op
   *          Operation this arg list is for.
   * @param binding
   *          Binding for the operation.
   * @param wsdlTypes
   *          Wsdl types.
   */
  protected WsdlOpParameterList( Operation op, Binding binding, WsdlTypes wsdlTypes ) {

    _wsdlTypes = wsdlTypes;
    _returnParam = null;
    _operation = op;
    _parameterStyle = WsdlOperation.SOAPParameterStyle.BARE;
    _headerNames = WsdlUtils.getSOAPHeaders( binding, op.getName() );
  }

  /**
   * Was there a 'return type' parameter in this list? If so return its XML type.
   *
   * @return QName of the XML type, null if not present.
   */
  protected WsdlOpReturnType getReturnType() {
    return _returnParam;
  }

  /**
   * Get the style (WRAPPED or BARE) of the parameters in this list.
   *
   * @return WsdlOperation.SOAPParamaterStyle enumeration value.
   */
  protected WsdlOperation.SOAPParameterStyle getParameterStyle() {
    return _parameterStyle;
  }

  /**
   * @return the operation for this parameter list
   */
  public Operation getOperation() {
    return _operation;
  }

  /**
   * Add a parameter to this list.
   *
   * @param p
   *          Message part defining the parameter.
   * @param requestPart
   *          tue if this parameter is part of an reqest message.
   * @return true if this collection was modified as a result of this call.
   */
  protected boolean add( Part p, boolean requestPart ) throws KettleStepException {

    List<WsdlOpParameter> params = getParameter( p, requestPart );
    for ( WsdlOpParameter op : params ) {

      if ( _headerNames.contains( op.getName().getLocalPart() ) ) {
        op.setHeader();
      }

      if ( requestPart ) {
        // just set mode and add
        op.setMode( op.getMode() ); // TODO: WTF??
        add( op );
      } else {
        addOutputParameter( op );
      }
    }
    return true;
  }

  /**
   * Generate a WsdlOpParameter from the message part.
   *
   * @param part
   *          A list of message part.
   * @param requesPart
   *          true if part from request message.
   */
  private List<WsdlOpParameter> getParameter( Part part, boolean requesPart ) throws KettleStepException {

    List<WsdlOpParameter> params = new ArrayList<WsdlOpParameter>();

    if ( part.getElementName() != null ) {
      if ( WsdlUtils.isWrappedParameterStyle( _operation.getName(), !requesPart, part.getName() ) ) {
        _parameterStyle = WsdlOperation.SOAPParameterStyle.WRAPPED;
      }
      params.addAll( resolvePartElement( part ) );
    } else {
      params.add( new WsdlOpParameter( part.getName(), part.getTypeName(), _wsdlTypes.findNamedType( part
        .getTypeName() ), _wsdlTypes ) );
    }
    return params;
  }

  /**
   * Add an response param to the parameter list. Some rules for determining if the request param is the return value
   * for the operation:
   * <p/>
   * <ol>
   * <li>If the operation has 'parameterOrder' set:</li>
   * <ol>
   * <li>If the response parameter is not in the operation's parameterOrder attribute, then it represents the return
   * value of the call. If there is no such part, then the method does not return a value.</li>
   * b) If the response parameter is found in the parameterOrder list, add it as an OUT mode parameter.</li>
   * </ol>
   * <li>If the operation does not have 'parameterOrder' set:</li>
   * <ol>
   * <li>If there is a single part in the output message that is not also in the input message it is mapped to the
   * return type of the method.</li>
   * <li>If there is more than one part in the output message that is not in the input message they are all mapped as
   * out arguments and the return type of the method is void.</li>
   * </ol>
   * </ol>
   *
   * @param responseParam
   *          Parameter to process.
   */
  @SuppressWarnings( "unchecked" )
  private void addOutputParameter( WsdlOpParameter responseParam ) {
    //
    // is this in IN/OUT param ?
    //
    /*
     * for (WsdlOpParameter param : this) { if (param.equals(responseParam)) {
     * param.setMode(WsdlOpParameter.ParameterMode.INOUT); return; } }
     */

    // If made we it to this far, we're talking about an out mode param
    // responseParam.setMode(WsdlOpParameter.ParameterMode.OUT);

    List<String> parameterOrder = _operation.getParameterOrdering();
    if ( parameterOrder != null ) {
      if ( !parameterOrder.contains( responseParam.getName().getLocalPart() ) ) {
        // assert _returnParam == null : "Invalid state!!!";
        _returnParam = responseParam;
      } else {
        add( responseParam );
      }
    } else {
      if ( _returnParam == null && !_outOnly ) {
        _returnParam = responseParam;
      } else if ( _returnParam != null ) {
        // move _returnParam into main arg list
        add( _returnParam );
        _returnParam = null;
        _outOnly = true;

        add( responseParam );
      } else {
        add( responseParam );
      }
    }
  }

  /**
   * Resolve a Part's element attribute value to a concrete XML type.
   *
   * @param p
   *          A message part.
   * @return A list of parameters resulting from the schema type -- typically the list will only contains a single
   *         parameter.
   */
  private List<WsdlOpParameter> resolvePartElement( Part p ) throws KettleStepException {

    List<WsdlOpParameter> resolvedParams = new ArrayList<WsdlOpParameter>();
    Element schemaElement = _wsdlTypes.findNamedElement( p.getElementName() );

    if ( schemaElement.hasAttribute( WsdlUtils.ELEMENT_TYPE_ATTR ) ) {
      // this is a simple type
      resolvedParams.add( new WsdlOpParameter( p.getName(), schemaElement, _wsdlTypes ) );
    } else {
      // this is a complex type
      Element complex = DomUtils.getChildElementByName( schemaElement, WsdlUtils.COMPLEX_TYPE_NAME );
      Element sequence = DomUtils.getChildElementByName( complex, WsdlUtils.SEQUENCE_TAG_NAME );

      // may occasionally find a <complex/> tag map to empty but this may be a bug in WSM
      //
      if ( sequence == null ) {
        return resolvedParams;
      }

      List<Element> seqElements = DomUtils.getChildElementsByName( sequence, WsdlUtils.ELEMENT_NAME );

      for ( Element e : seqElements ) {
        WsdlOpParameter op = new WsdlOpParameter( e, _wsdlTypes );

        // special case for bare arrays, change the name of the param
        // to the name of the complex type.
        if ( op.isArray() && _parameterStyle == WsdlOperation.SOAPParameterStyle.BARE ) {
          op.setName( schemaElement.getAttribute( WsdlUtils.NAME_ATTR ), _wsdlTypes );
        }
        resolvedParams.add( op );
      }
    }
    return resolvedParams;
  }

  public HashSet<String> getHeaderNames() {
    return _headerNames;
  }
}
