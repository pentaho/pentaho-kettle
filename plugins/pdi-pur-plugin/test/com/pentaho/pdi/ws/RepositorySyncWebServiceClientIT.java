/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.pentaho.pdi.ws;

import java.io.Serializable;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.junit.Before;

@SuppressWarnings( "nls" )
public class RepositorySyncWebServiceClientIT extends RepositorySyncWebServiceTest implements Serializable {

  private static final long serialVersionUID = -6806897012063786589L; /* EESOURCE: UPDATE SERIALVERUID */

  @Before
  public void before() {
    System.out.println( "Starting server..." );
    String address = "http://localhost:9988/repo";
    Endpoint.publish( address, new RepositorySyncWebService() );
    System.out.println( "Server Started." );
  }

  @Override
  public IRepositorySyncWebService getRepositorySyncWebService() {
    try {
      Service service =
          Service.create( new URL( "http://localhost:9988/repo?wsdl" ), new QName( "http://www.pentaho.org/ws/1.0",
              "repositorySync" ) );

      return service.getPort( IRepositorySyncWebService.class );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }
}
