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

import java.beans.beancontext.BeanContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.xml.WSDLLocator;

import org.xml.sax.InputSource;

/**
 * Implementation of WSDLLocator for Beehive controls. This WSDLLocator implementation may open multiple input streams,
 * its <tt>cleanup()</tt> method should always be called once the WSDL file has been parsed.
 */
public final class ControlWsdlLocator implements WSDLLocator {
  private final String _wsdlName;
  private final BeanContext _beanContext;
  private List<InputStream> _openStreams = new ArrayList<InputStream>();

  /**
   * Create a new wsdl locator for the wsdl file with the specified name.
   *
   * @param wsdlName
   *          Name of the WSDL file to try to load. Name may include file path elements.
   * @param beanContext
   *          The ControlBeanContext of the control which wants to load a WSDL file.
   */
  public ControlWsdlLocator( String wsdlName, BeanContext beanContext ) {

    if ( wsdlName == null ) {
      throw new IllegalArgumentException( "ERROR: WSDL path is null!" );
    }

    _wsdlName = wsdlName;
    _beanContext = beanContext;
  }

  /**
   * Close any InputStreams opened by this locator.
   */
  public void cleanup() {

    for ( InputStream inputStream : _openStreams ) {
      try {
        inputStream.close();
      } catch ( IOException ioe ) {
        // TODO: log a warning!!
      }
    }
  }

  /**
   * Find the InputSource for the WSDL file stored in _wsdlName. This method attempts to find a WSDL file by:
   * <ol>
   * <li>If the _wsdlName can be converted to a URL, use url.openStream()</li>
   * <li>Attempt to locate _wsdlName using the bean context's getResourceAsStream()</li>
   * <li>Attempt to locate _wsdlName using the current class loader's getResourceAsStream()</li>
   * </ol>
   *
   * @return An InputSource for the WSDL file.
   */
  public InputSource getBaseInputSource() {

    InputStream wsdlStream = null;

    // try to open as URL first --
    try {
      URL url = new URL( _wsdlName );
      wsdlStream = url.openStream();
    } catch ( MalformedURLException e ) {
      // not fatal keep trying
    } catch ( IOException e ) {
      // fatal - abort
      throw new RuntimeException( "Cannot load WSDL file: " + _wsdlName, e );
    }

    if ( wsdlStream == null ) {
      wsdlStream = _beanContext.getBeanContext().getResourceAsStream( _wsdlName, _beanContext );
    }

    if ( wsdlStream == null ) {
      wsdlStream = this.getClass().getClassLoader().getResourceAsStream( _wsdlName );
    }

    if ( wsdlStream == null ) {
      throw new RuntimeException( "Cannot find WSDL file: " + _wsdlName, null );
    }

    _openStreams.add( wsdlStream );
    return new InputSource( wsdlStream );
  }

  /**
   * Get the base URI for the wsdl file.
   *
   * @return null if _wsdlName is not a valid URI.
   */
  public String getBaseURI() {

    try {
      URI uri = new URI( _wsdlName );
      return uri.toString();
    } catch ( URISyntaxException e ) {
      return null;
    }
  }

  /**
   * Not implemented.
   *
   * @param string
   * @param string1
   * @return null
   */
  public InputSource getImportInputSource( String string, String string1 ) {
    return null;
  }

  /**
   * Not implemented.
   *
   * @return null
   */
  public String getLatestImportURI() {
    return null;
  }

  public void close() {
    cleanup();
  }

}
