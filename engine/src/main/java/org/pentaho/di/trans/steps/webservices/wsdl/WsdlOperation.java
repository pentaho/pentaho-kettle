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

import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;

/**
 * WSDL operation abstraction.
 */
public final class WsdlOperation implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Parameter style enumeration.
   */
  public enum SOAPParameterStyle {
    BARE, WRAPPED
  }

  /**
   * SOAP Binding style enumeration.
   */
  public enum SOAPBindingStyle {
    DOCUMENT, RPC
  }

  /**
   * SOAP Binding use enumeration.
   */
  public enum SOAPBindingUse {
    LITERAL, ENCODED
  }

  private final SOAPBindingStyle _bindingStyle;
  private final SOAPBindingUse _bindingUse;
  private final WsdlOpFaultList _faults;
  private final QName _operationQName;
  private final WsdlOpParameterList _params;
  private final SOAPParameterStyle _parameterStyle;
  private final String _soapAction;
  private final WsdlOpReturnType _returnType;

  private boolean _oneway;

  /**
   * Create a new wsdl operation instance for the specified binding and operation.
   *
   * @param binding
   *          Binding for the operation.
   * @param op
   *          The operation.
   * @param wsdlTypes
   *          WSDL type information.
   */
  protected WsdlOperation( Binding binding, Operation op, WsdlTypes wsdlTypes ) throws KettleException {

    _operationQName = new QName( wsdlTypes.getTargetNamespace(), op.getName() );
    _oneway = true;

    String soapBindingStyle = WsdlUtils.getSOAPBindingStyle( binding );
    if ( "rpc".equals( soapBindingStyle ) ) {
      _bindingStyle = SOAPBindingStyle.RPC;
    } else {
      _bindingStyle = SOAPBindingStyle.DOCUMENT;
    }

    String soapBindingUse = WsdlUtils.getSOAPBindingUse( binding, op.getName() );
    if ( "encoded".equals( soapBindingUse ) ) {
      _bindingUse = SOAPBindingUse.ENCODED;
    } else {
      _bindingUse = SOAPBindingUse.LITERAL;
    }

    _soapAction = WsdlUtils.getSOAPAction( binding.getBindingOperation( op.getName(), null, null ) );

    _params = new WsdlOpParameterList( op, binding, wsdlTypes );
    loadParameters( op );

    _faults = new WsdlOpFaultList( wsdlTypes );
    loadFaults( op );

    _returnType = _params.getReturnType();
    _parameterStyle = _params.getParameterStyle();
  }

  /**
   * Get the faults defined for this operation.
   *
   * @return WsdlOpFaultList
   */
  public WsdlOpFaultList getFaults() {
    return _faults;
  }

  /**
   * Get the operation's QName.
   *
   * @return Operation QName.
   */
  public QName getOperationQName() {
    return _operationQName;
  }

  /**
   * Get the SOAP action uri for this operation.
   *
   * @return String, null if SOAPAction not defined for this operation.
   */
  public String getSOAPAction() {
    return _soapAction;
  }

  /**
   * Get the SOAPBinding Style.
   *
   * @return A SOAPBindingStyle enum type value.
   */
  public SOAPBindingStyle getSOAPBindingStyle() {
    return _bindingStyle;
  }

  /**
   * Get the SOAPBinding use.
   *
   * @return A SOAPBindingUse enum type value.
   */
  public SOAPBindingUse getSOAPBindingUse() {
    return _bindingUse;
  }

  /**
   * Get the SOAPParameter style.
   *
   * @return A SOAPParameterStyle enum type value.
   */
  public SOAPParameterStyle getSOAPParameterStyle() {
    return _parameterStyle;
  }

  /**
   * Get the parameter list for this operation.
   *
   * @return An ordered list of parameters, empty list if this operation has no parameters.
   */
  public WsdlOpParameterList getParameters() {
    return _params;
  }

  /**
   * Get the return type for this operation.
   *
   * @return A WsdlOpReturnType instance, null if this operation does not have a return value.
   */
  public WsdlOpReturnType getReturnType() {
    return _returnType;
  }

  /**
   * Is this a oneway operation?
   *
   * @return true if this is a oneway operation.
   */
  public boolean isOneway() {
    return _oneway;
  }

  /**
   * Create the fault list for this operation.
   *
   * @param op
   *          Operation
   */
  @SuppressWarnings( "unchecked" )
  private void loadFaults( Operation op ) throws KettleStepException {
    Map<?, Fault> faultMap = op.getFaults();
    for ( Fault fault : faultMap.values() ) {
      _faults.add( fault );
    }
  }

  /**
   * Create the parameter list for this operations parameter set.
   *
   * @param op
   *          Operation.
   * @throws KettleStepException
   */
  @SuppressWarnings( "unchecked" )
  private void loadParameters( Operation op ) throws KettleStepException {

    Input input = op.getInput();
    if ( input != null ) {
      Message in = input.getMessage();
      List<Object> paramOrdering = op.getParameterOrdering();
      List<Part> inParts = in.getOrderedParts( paramOrdering );

      for ( Part part : inParts ) {
        _params.add( part, true );
      }
    }

    Output output = op.getOutput();
    if ( output != null ) {
      Message out = output.getMessage();
      List<Part> outParts = out.getOrderedParts( null );

      for ( Part part : outParts ) {
        _oneway = false;
        _params.add( part, false );
      }
    }
  }
}
