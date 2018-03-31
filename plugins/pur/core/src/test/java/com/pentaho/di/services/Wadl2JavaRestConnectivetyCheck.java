/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
package com.pentaho.di.services;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.pentaho.di.repository.pur.PurRepositoryRestService;
import org.pentaho.di.repository.pur.WebServiceManager;
import org.pentaho.platform.plugin.services.importexport.InitializationException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * This class is an integrated test, not a unit test. Assuming there is a running pdi/merges server on localhost, this
 * class will test rest service connectivity. The first test will see if the generated code from Wadl2java can run a web
 * service. The second test will test if our WebServiceManager can do the same.
 * 
 * @author tkafalas
 * 
 */
@Ignore
public class Wadl2JavaRestConnectivetyCheck {
  private Client client = null;
  private static String uri = "http://localHost:8080/pentaho";

  public static void main( String[] args ) {
    Wadl2JavaRestConnectivetyCheck test = new Wadl2JavaRestConnectivetyCheck();
    try {
      test.initRestService1();
      test.initRestService2();
    } catch ( InitializationException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Test stand alone generated code web service connectivity
   */
  private void initRestService1() throws InitializationException {
    // get information about the remote connection
    String username = "admin";
    String password = "password";

    ClientConfig clientConfig = new DefaultClientConfig();
    client = Client.create( clientConfig );
    client.addFilter( new HTTPBasicAuthFilter( username, password ) );

    URI baseUri = null;
    try {
      baseUri = new URI( uri + "/plugin/" );
    } catch ( URISyntaxException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    PentahoDiPlugin.PurRepositoryPluginApiRevision revisionService =
        PentahoDiPlugin.purRepositoryPluginApiRevision( client, baseUri );

    PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdVersioningConfiguration versioningConfigurationMethod =
        revisionService.pathIdVersioningConfiguration( "foo.ktr" );
    FileVersioningConfiguration fileVersioningConfiguration =
        versioningConfigurationMethod.getAsXml( FileVersioningConfiguration.class );
    System.out.println( "Test1: " + fileVersioningConfiguration.isVersioningEnabled() );
  }

  /**
   * Test web service connectivity using the the WebServiceManager
   * 
   * @throws InitializationException
   */
  private void initRestService2() throws InitializationException {
    // simulate the registration of web service classes
    String username = "admin";
    String password = "password";
    WebServiceManager webServiceManager = new WebServiceManager( uri, username );

    // Now get a class representing the services in the class
    // PentahoDiPlugin
    PurRepositoryRestService.PurRepositoryPluginApiRevision servicePort = null;
    try {
      servicePort =
          webServiceManager.createService( username, password,
              PurRepositoryRestService.PurRepositoryPluginApiRevision.class );
    } catch ( MalformedURLException e ) {
      // Should never happen
      e.printStackTrace();
    }

    // Call any of the web services here
    FileVersioningConfiguration fileVersioningConfiguration =
        servicePort.pathIdVersioningConfiguration( "foo.ktr" ).getAsFileVersioningConfigurationXml();
    System.out.println( "Test2: " + fileVersioningConfiguration.isVersioningEnabled() );

    webServiceManager.close();
  }
}
