/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.http.auth.AuthenticationException;
import org.pentaho.di.core.HTTPProtocol;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;

/**
 * Wsdl abstraction.
 */
public final class Wsdl implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  private Port _port;
  private final Definition _wsdlDefinition;
  private final Service _service;
  private final WsdlTypes _wsdlTypes;
  private HashMap<String, WsdlOperation> _operationCache;
  private URI wsdlURI = null;

  /**
   * Loads and parses the specified WSDL file.
   *
   * @param wsdlURI
   *          URI of a WSDL file.
   * @param serviceQName
   *          Name of the service in the WSDL, if null default to first service in WSDL.
   * @param portName
   *          The service port name, if null default to first port in service.
   */
  public Wsdl( URI wsdlURI, QName serviceQName, String portName ) throws AuthenticationException {
    this( wsdlURI, serviceQName, portName, null, null );
  }

  public Wsdl( URI wsdlURI, QName serviceQName, String portName, String username, String password ) throws AuthenticationException {

    this.wsdlURI = wsdlURI;
    try {
      _wsdlDefinition = parse( wsdlURI, username, password );
    } catch ( AuthenticationException ae ) {
      // throw this again since KettleException is catching it
      throw ae;
    } catch ( WSDLException e ) {
      throw new RuntimeException( "Could not load WSDL file: " + e.getMessage(), e );
    } catch ( KettleException e ) {
      throw new RuntimeException( "Could not load WSDL file: " + e.getMessage(), e );
    }
    if ( serviceQName == null ) {
      _service = (Service) _wsdlDefinition.getServices().values().iterator().next();
    } else {
      _service = _wsdlDefinition.getService( serviceQName );
      if ( _service == null ) {
        throw new IllegalArgumentException( "Service: "
          + serviceQName + " is not defined in the WSDL file " + wsdlURI );
      }
    }

    if ( portName == null ) {
      _port = getSoapPort( _service.getPorts().values() );
    } else {
      _port = _service.getPort( portName );
      if ( _port == null ) {
        throw new IllegalArgumentException( "Port: "
          + portName + " is not defined in the service: " + serviceQName );
      } else {
        _port = _service.getPort( portName );
      }
    }

    _wsdlTypes = new WsdlTypes( _wsdlDefinition );
    _operationCache = new HashMap<String, WsdlOperation>();
  }

  /**
   * Returns the first Soap port from the passed collection of Ports.
   *
   * @param portCollection
   * @return
   */
  private Port getSoapPort( Collection<?> portCollection ) {
    Port soapPort = null;
    Iterator<?> iterator = portCollection.iterator();
    while ( iterator.hasNext() ) {
      Port port = (Port) iterator.next();
      if ( WsdlUtils.isSoapPort( port ) ) {
        soapPort = port;
        break;
      }
    }
    return soapPort;
  }

  /**
   * Loads and parses the specified WSDL file.
   *
   * @param wsdlLocator
   *          A javax.wsdl.WSDLLocator instance.
   * @param serviceQName
   *          Name of the service in the WSDL.
   * @param portName
   *          The service port name.
   */
  public Wsdl( WSDLLocator wsdlLocator, QName serviceQName, String portName ) throws AuthenticationException {
    this( wsdlLocator, serviceQName, portName, null, null );
  }

  public Wsdl( WSDLLocator wsdlLocator, QName serviceQName, String portName, String username, String password ) throws AuthenticationException {

    // load and parse the WSDL
    try {
      _wsdlDefinition = parse( wsdlLocator, username, password );
    } catch ( AuthenticationException ae ) {
      // throw it again or KettleException will catch it
      throw ae;
    } catch ( WSDLException e ) {
      throw new RuntimeException( "Could not load WSDL file: " + e.getMessage(), e );
    } catch ( KettleException e ) {
      throw new RuntimeException( "Could not load WSDL file: " + e.getMessage(), e );
    }

    _service = _wsdlDefinition.getService( serviceQName );
    if ( _service == null ) {
      throw new IllegalArgumentException( "Service: " + serviceQName + " is not defined in the WSDL file." );
    }

    _port = _service.getPort( portName );
    if ( _port == null ) {
      throw new IllegalArgumentException( "Port: " + portName + " is not defined in the service: " + serviceQName );
    }

    _wsdlTypes = new WsdlTypes( _wsdlDefinition );
    _operationCache = new HashMap<String, WsdlOperation>();
  }

  /**
   * Get the WsdlComplexTypes instance of this wsdl. WsdlComplex types provides type information for named complextypes
   * defined in the wsdl's &lt;types&gt; section.
   *
   * @return WsdlComplexTypes instance.
   */
  public WsdlComplexTypes getComplexTypes() {
    return _wsdlTypes.getNamedComplexTypes();
  }

  /**
   * Find the specified operation in the WSDL definition.
   *
   * @param operationName
   *          Name of operation to find.
   * @return A WsdlOperation instance, null if operation can not be found in WSDL.
   */
  public WsdlOperation getOperation( String operationName ) throws KettleStepException {

    // is the operation in the cache?
    if ( _operationCache.containsKey( operationName ) ) {
      return _operationCache.get( operationName );
    }

    Binding b = _port.getBinding();
    PortType pt = b.getPortType();
    Operation op = pt.getOperation( operationName, null, null );
    if ( op != null ) {
      try {
        WsdlOperation wop = new WsdlOperation( b, op, _wsdlTypes );
        // cache the operation
        _operationCache.put( operationName, wop );
        return wop;
      } catch ( KettleException kse ) {
        LogChannel.GENERAL.logError( "Could not retrieve WSDL Operator for operation name: " + operationName );
        throw new KettleStepException(
          "Could not retrieve WSDL Operator for operation name: " + operationName, kse );
      }
    }
    return null;
  }

  /**
   * Get a list of all operations defined in this WSDL.
   *
   * @return List of WsdlOperations.
   */
  @SuppressWarnings( "unchecked" )
  public List<WsdlOperation> getOperations() throws KettleStepException {

    List<WsdlOperation> opList = new ArrayList<WsdlOperation>();
    PortType pt = _port.getBinding().getPortType();

    List<Operation> operations = pt.getOperations();
    for ( Iterator<Operation> itr = operations.iterator(); itr.hasNext(); ) {
      WsdlOperation operation = getOperation( itr.next().getName() );
      if ( operation != null ) {
        opList.add( operation );
      }
    }
    return opList;
  }

  /**
   * Get the name of the current port.
   *
   * @return Name of the current port.
   */
  public String getPortName() {
    return _port.getName();
  }

  /**
   * Get the PortType name for the service which has been specified by serviceName and portName at construction time.
   *
   * @return QName of the PortType.
   */
  public QName getPortTypeQName() {

    Binding b = _port.getBinding();
    return b.getPortType().getQName();
  }

  /**
   * Get the service endpoint.
   *
   * @return String containing the service endpoint.
   */
  public String getServiceEndpoint() {
    return WsdlUtils.getSOAPAddress( _port );
  }

  /**
   * Get the name of this service.
   *
   * @return Service name.
   */
  public String getServiceName() {
    return _service.getQName().getLocalPart();
  }

  /**
   * Get the target namespace for the WSDL.
   *
   * @return The targetNamespace
   */
  public String getTargetNamespace() {
    return _wsdlDefinition.getTargetNamespace();
  }

  /**
   * Change the port of the service.
   *
   * @param portName
   *          The new port name.
   * @throws IllegalArgumentException
   *           if port name is not defined in WSDL.
   */
  public void setPort( QName portName ) {

    Port port = _service.getPort( portName.getLocalPart() );
    if ( port == null ) {
      throw new IllegalArgumentException( "Port name: '" + portName + "' was not found in the WSDL file." );
    }

    _port = port;
    _operationCache.clear();
  }

  /**
   * Get a WSDLReader.
   *
   * @return WSDLReader.
   * @throws WSDLException
   *           on error.
   */
  private WSDLReader getReader() throws WSDLException {

    WSDLFactory wsdlFactory = WSDLFactory.newInstance();
    WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
    ExtensionRegistry registry = wsdlFactory.newPopulatedExtensionRegistry();
    wsdlReader.setExtensionRegistry( registry );
    wsdlReader.setFeature( "javax.wsdl.verbose", true );
    wsdlReader.setFeature( "javax.wsdl.importDocuments", true );
    return wsdlReader;
  }

  /**
   * Load and parse the WSDL file using the wsdlLocator.
   *
   * @param wsdlLocator
   *          A WSDLLocator instance.
   * @param username
   *          to use for authentication
   * @param password
   *          to use for authentication
   * @return wsdl Definition.
   * @throws WSDLException
   *           on error.
   */
  private Definition parse( WSDLLocator wsdlLocator, String username, String password ) throws WSDLException,
    KettleException, AuthenticationException {

    WSDLReader wsdlReader = getReader();
    try {

      return wsdlReader.readWSDL( wsdlLocator );
    } catch ( WSDLException we ) {
      readWsdl( wsdlReader, wsdlURI.toString(), username, password );
      return null;
    }
  }

  /**
   * Load and parse the WSDL file at the specified URI.
   *
   * @param wsdlURI
   *          URI of the WSDL file.
   * @param username
   *          to use for authentication
   * @param password
   *          to use for authentication
   * @return wsdl Definition
   * @throws WSDLException
   *           on error.
   */
  private Definition parse( URI wsdlURI, String username, String password ) throws WSDLException, KettleException,
    AuthenticationException {
    WSDLReader wsdlReader = getReader();
    return readWsdl( wsdlReader, wsdlURI.toString(), username, password );
  }

  private Definition readWsdl( WSDLReader wsdlReader, String uri, String username, String password ) throws WSDLException, KettleException, AuthenticationException {

    try {
      HTTPProtocol http = new HTTPProtocol();
      Document doc = XMLHandler.loadXMLString( http.get( wsdlURI.toString(), username, password ), true, false );
      if ( doc != null ) {
        return ( wsdlReader.readWSDL( doc.getBaseURI(), doc ) );
      } else {
        throw new KettleException( "Unable to get document." );
      }
    } catch ( MalformedURLException mue ) {
      throw new KettleException( mue );
    } catch ( AuthenticationException ae ) {
      // re-throw this. If not IOException seems to catch it
      throw ae;
    } catch ( IOException ioe ) {
      throw new KettleException( ioe );
    }
  }

  /**
   * Returns this objects WSDL types.
   *
   * @return WsdlTepes
   */
  public WsdlTypes getWsdlTypes() {
    return this._wsdlTypes;
  }
}
