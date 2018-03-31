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
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Port;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;

import org.pentaho.di.core.exception.KettleException;

import com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl;

/**
 * Utilities for getting extensibility elements.
 */
final class WsdlUtils {

  // extensibility element names
  private static final String SOAP_PORT_ADDRESS_NAME = "address";
  private static final String SOAP_BINDING_ELEMENT_NAME = "binding";
  private static final String SOAP_BODY_ELEMENT_NAME = "body";
  private static final String SOAP_HEADER_ELEMENT_NAME = "header";
  private static final String SOAP_OPERATION_ELEMENT_NAME = "operation";

  // return values
  private static final String SOAP_BINDING_DEFAULT = "document";

  // schema names
  protected static final String ELEMENT_FORM_DEFAULT_ATTR = "elementFormDefault";
  protected static final String ANY_TAG_NAME = "any";
  protected static final String TARGET_NAMESPACE_ATTR = "targetNamespace";
  protected static final String ELEMENT_FORM_QUALIFIED = "qualified";
  protected static final String ELEMENT_NAME = "element";
  protected static final String COMPLEX_TYPE_NAME = "complexType";
  protected static final String SCHEMA_ELEMENT_NAME = "schema";
  protected static final String SEQUENCE_TAG_NAME = "sequence";
  protected static final String SIMPLE_TYPE_NAME = "simpleType";
  protected static final String NAME_ATTR = "name";
  protected static final String ELEMENT_TYPE_ATTR = "type";
  protected static final String ELEMENT_REF_ATTR = "ref";
  protected static final String MAXOCCURS_ATTR = "maxOccurs";
  protected static final String MINOCCURS_ATTR = "minOccurs";

  /**
   * Get the SOAP address location for the specified port.
   *
   * @param p
   *          A WSDL Port instance.
   * @return The SOAP address URI.
   */
  protected static String getSOAPAddress( Port p ) {
    ExtensibilityElement e = findExtensibilityElement( p, SOAP_PORT_ADDRESS_NAME );
    if ( e instanceof SOAP12Address ) {
      return ( (SOAP12Address) e ).getLocationURI();
    } else if ( e instanceof SOAPAddress ) {
      return ( (SOAPAddress) e ).getLocationURI();
    }

    return null;
  }

  /**
   * Get the SOAPBinding style for the specified WSDL Port.
   *
   * @param binding
   *          A WSDL Binding instance.
   * @return String either 'document' or 'rpc', if not found in WSDL defaults to 'document'.
   */
  protected static String getSOAPBindingStyle( Binding binding ) throws KettleException {
    String style = SOAP_BINDING_DEFAULT;
    ExtensibilityElement soapBindingElem = findExtensibilityElement( binding, SOAP_BINDING_ELEMENT_NAME );

    if ( soapBindingElem != null ) {
      if ( soapBindingElem instanceof SOAP12Binding ) {
        style = ( (SOAP12Binding) soapBindingElem ).getStyle();
      } else if ( soapBindingElem instanceof SOAPBinding ) {
        style = ( (SOAPBinding) soapBindingElem ).getStyle();
      } else {
        throw new KettleException( "Binding type "
          + soapBindingElem + " encountered. The Web Service Lookup step only supports SOAP Bindings!" );
      }
    }
    return style;
  }

  /**
   * Get the SOAP Use type for the specified operation.
   *
   * @param binding
   *          A WSDL Binding instance.
   * @param operationName
   *          The name of the operation.
   * @return Either 'literal' or 'encoded'.
   * @throws RuntimeException
   *           If the use type cannot be determined.
   */
  protected static String getSOAPBindingUse( Binding binding, String operationName ) {

    BindingOperation bindingOperation = binding.getBindingOperation( operationName, null, null );

    if ( bindingOperation == null ) {
      throw new IllegalArgumentException( "Can not find operation: " + operationName );
    }

    // first try getting the use setting from the input message
    BindingInput bindingInput = bindingOperation.getBindingInput();
    if ( bindingInput != null ) {
      ExtensibilityElement soapBodyElem =
        WsdlUtils.findExtensibilityElement( bindingInput, SOAP_BODY_ELEMENT_NAME );
      if ( soapBodyElem != null ) {
        if ( soapBodyElem instanceof SOAP12BodyImpl ) {
          return ( (SOAP12BodyImpl) soapBodyElem ).getUse();
        } else {
          return ( (SOAPBody) soapBodyElem ).getUse();
        }
      }
    }

    // if there was no input message try getting the use from the output message
    BindingOutput bindingOutput = bindingOperation.getBindingOutput();
    if ( bindingOutput != null ) {
      ExtensibilityElement soapBodyElem =
        WsdlUtils.findExtensibilityElement( bindingOutput, SOAP_BODY_ELEMENT_NAME );
      if ( soapBodyElem != null ) {
        if ( soapBodyElem instanceof SOAP12BodyImpl ) {
          return ( (SOAP12BodyImpl) soapBodyElem ).getUse();
        } else {
          return ( (SOAPBody) soapBodyElem ).getUse();
        }
      }
    }

    throw new RuntimeException( "Unable to determine SOAP use for operation: " + operationName );
  }

  /**
   * Get the Soap Action URI from the operation's soap:operation extensiblity element.
   *
   * @param operation
   *          A WSDL Operation.
   * @return Soap action URI as string, null if not defined.
   */
  protected static String getSOAPAction( BindingOperation operation ) {
    ExtensibilityElement e = findExtensibilityElement( operation, SOAP_OPERATION_ELEMENT_NAME );
    if ( e != null ) {
      if ( e instanceof SOAP12Operation ) {
        return ( (SOAP12Operation) e ).getSoapActionURI();
      } else {
        return ( (SOAPOperation) e ).getSoapActionURI();
      }
    }
    return null;
  }

  /**
   * Determine if this parameter has a parameter style of WRAPPED. It does if the <tt>name</tt> attribute of the part is
   * NOT the same as its operation name.
   *
   * @param operationName
   *          Name of the part's operation.
   * @param outputParam
   *          true if this is an output parameter.
   * @param messagePartName
   *          Name of the message part.
   * @return true if parameter style is wrapped.
   */
  protected static boolean isWrappedParameterStyle( String operationName, boolean outputParam,
    String messagePartName ) {

    if ( outputParam ) {
      if ( messagePartName.equals( operationName + "Response" ) ) {
        return false;
      }
    } else {
      if ( messagePartName.equals( operationName ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Build a HashSet of SOAP header names for the specified operation and binding.
   *
   * @param binding
   *          WSDL Binding instance.
   * @param operationName
   *          Name of the operation.
   * @return HashSet of soap header names, empty set if no headers present.
   */
  protected static HashSet<String> getSOAPHeaders( Binding binding, String operationName ) {

    List<ExtensibilityElement> headers = new ArrayList<ExtensibilityElement>();
    BindingOperation bindingOperation = binding.getBindingOperation( operationName, null, null );
    if ( bindingOperation == null ) {
      throw new IllegalArgumentException( "Can not find operation: " + operationName );
    }

    BindingInput bindingInput = bindingOperation.getBindingInput();
    if ( bindingInput != null ) {
      headers.addAll( WsdlUtils.findExtensibilityElements( bindingInput, SOAP_HEADER_ELEMENT_NAME ) );
    }

    BindingOutput bindingOutput = bindingOperation.getBindingOutput();
    if ( bindingOutput != null ) {
      headers.addAll( WsdlUtils.findExtensibilityElements( bindingOutput, SOAP_HEADER_ELEMENT_NAME ) );
    }

    HashSet<String> headerSet = new HashSet<String>( headers.size() );
    for ( ExtensibilityElement element : headers ) {
      if ( element instanceof SOAP12Header ) {
        headerSet.add( ( (SOAP12Header) element ).getPart() );
      } else {
        headerSet.add( ( (SOAPHeader) element ).getPart() );
      }
    }

    return headerSet;
  }

  /**
   * Find the specified extensibility element, if more than one with the specified name exists in the list, return the
   * first one found.
   *
   * @param extensibleElement
   *          WSDL type which extends ElementExtensible.
   * @param elementType
   *          Name of the extensiblity element to find.
   * @return ExtensibilityElement The ExtensiblityElement, if not found return null.
   */
  @SuppressWarnings( "unchecked" )
  protected static ExtensibilityElement findExtensibilityElement( ElementExtensible extensibleElement,
    String elementType ) {

    List<ExtensibilityElement> extensibilityElements = extensibleElement.getExtensibilityElements();
    if ( extensibilityElements != null ) {
      for ( ExtensibilityElement element : extensibilityElements ) {
        if ( element.getElementType().getLocalPart().equalsIgnoreCase( elementType ) ) {
          return element;
        }
      }
    }
    return null;
  }

  /**
   * Find all of the extensibility elements with the specified name.
   *
   * @param extensibleElement
   *          WSDL type which extends ElementExtensible.
   * @param elementType
   *          Name of the extensibility element to find.
   * @return List of ExtensibilityElements, may be empty.
   */
  @SuppressWarnings( "unchecked" )
  protected static List<ExtensibilityElement> findExtensibilityElements( ElementExtensible extensibleElement,
    String elementType ) {

    List<ExtensibilityElement> elements = new ArrayList<ExtensibilityElement>();
    List<ExtensibilityElement> extensibilityElements = extensibleElement.getExtensibilityElements();

    if ( extensibilityElements != null ) {
      for ( ExtensibilityElement element : extensibilityElements ) {
        if ( element.getElementType().getLocalPart().equalsIgnoreCase( elementType ) ) {
          elements.add( element );
        }
      }
    }
    return elements;
  }

  /**
   *
   * @param port
   * @return
   */
  protected static boolean isSoapPort( Port port ) {
    return getSOAPAddress( port ) != null;
  }

}
