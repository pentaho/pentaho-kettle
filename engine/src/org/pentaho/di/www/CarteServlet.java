/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

public class CarteServlet extends HttpServlet {

  private static final long serialVersionUID = 2434694833497859776L;

  public static final String STRING_CARTE_SERVLET = "Carte Servlet";

  private Map<String, CartePluginInterface> cartePluginRegistry;

  private final LogChannelInterface log;

  public CarteServlet() {
    this.log = new LogChannel( STRING_CARTE_SERVLET );
  }

  public void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    doGet( req, resp );
  }

  public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    String servletPath = req.getPathInfo();
    if ( servletPath.endsWith( "/" ) ) {
      servletPath = servletPath.substring( 0, servletPath.length() - 1 );
    }
    CartePluginInterface plugin = cartePluginRegistry.get( servletPath );
    if ( plugin != null ) {
      try {
        plugin.doGet( req, resp );
      } catch ( ServletException e ) {
        throw e;
      } catch ( Exception e ) {
        throw new ServletException( e );
      }
    } else {
      if ( log.isDebug() ) {
        log.logDebug( "Unable to find CartePlugin for key: /kettle" + req.getPathInfo() );
      }
      resp.setStatus( 404 );
    }
  }

  private String getServletKey( CartePluginInterface servlet ) {
    String key = servlet.getContextPath();
    if ( key.startsWith( "/kettle" ) ) {
      key = key.substring( "/kettle".length() );
    }
    return key;
  }

  @Override
  public void init( ServletConfig config ) throws ServletException {
    final TransformationMap transformationMap = CarteSingleton.getInstance().getTransformationMap();
    final JobMap jobMap = CarteSingleton.getInstance().getJobMap();

    List<SlaveServerDetection> detections = Collections.synchronizedList( new ArrayList<SlaveServerDetection>() );
    SocketRepository socketRepository = CarteSingleton.getInstance().getSocketRepository();

    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = pluginRegistry.getPlugins( CartePluginType.class );
    Map<String, CartePluginInterface> pluginMap = new HashMap<String, CartePluginInterface>();

    for ( PluginInterface plugin : plugins ) {
      CartePluginInterface servlet;
      try {
        servlet = (CartePluginInterface) pluginRegistry.loadClass( plugin );
        pluginMap.put( getServletKey( servlet ), servlet );
      } catch ( KettlePluginException e ) {
        log.logError( "Unable to instantiate plugin for use with CarteServlet " + plugin.getName() );
      }
    }

    // Servlets configured in web.xml take precedence to those discovered during plugin scan
    @SuppressWarnings( "unchecked" )
    Enumeration<String> initParameterNames = config.getInitParameterNames();
    while ( initParameterNames.hasMoreElements() ) {
      final String paramName = initParameterNames.nextElement();
      final String className = config.getInitParameter( paramName );
      final Class<?> clazz;
      try {
        clazz = Class.forName( className );
        final CartePluginInterface servlet = (CartePluginInterface) clazz.newInstance();
        pluginMap.put( getServletKey( servlet ), servlet );
      } catch ( ClassNotFoundException e ) {
        log.logError( "Unable to find configured " + paramName + " of " + className, e );
      } catch ( InstantiationException e ) {
        log.logError( "Unable to instantiate configured " + paramName + " of " + className, e );
      } catch ( IllegalAccessException e ) {
        log.logError( "Unable to access configured " + paramName + " of " + className, e );
      } catch ( ClassCastException e ) {
        log.logError( "Unable to cast configured "
          + paramName + " of " + className + " to " + CartePluginInterface.class, e );
      }
    }

    for ( CartePluginInterface servlet : pluginMap.values() ) {
      servlet.setup( transformationMap, jobMap, socketRepository, detections );
      servlet.setJettyMode( false );
    }

    cartePluginRegistry = Collections.unmodifiableMap( pluginMap );
  }
}
