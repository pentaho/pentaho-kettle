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

import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.pentaho.di.messages.Messages;
import com.sun.xml.bind.StringInputStream;

@WebService ( endpointInterface = "com.pentaho.pdi.ws.IRepositorySyncWebService", serviceName = "repositorySync", portName = "repositorySyncPort", targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class RepositorySyncWebService implements IRepositorySyncWebService, Serializable {

  private static final long serialVersionUID = 743647084187858081L; /* EESOURCE: UPDATE SERIALVERUID */

  private static Log log = LogFactory.getLog( RepositorySyncWebService.class );
  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";

  public RepositorySyncStatus sync( String repositoryId, String repositoryUrl ) throws RepositorySyncException {
    boolean singleDiServerInstance = "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( singleDiServerInstance ) {
      return RepositorySyncStatus.SINGLE_DI_SERVER_INSTANCE;
    }

    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    try {
      repositoriesMeta.readData();
    } catch ( Exception e ) {
      log.error( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_READ_DATA" ), e ); //$NON-NLS-1$
      throw new RepositorySyncException( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_READ_DATA" ), e ); //$NON-NLS-1$
    }
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryId );
    if ( repositoryMeta == null ) {
      try {
        repositoryMeta = getRepositoryMeta( repositoryId, repositoryUrl );
        if ( repositoryMeta == null ) {
          log.error( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_LOAD_PLUGIN" ) ); //$NON-NLS-1$
          throw new RepositorySyncException( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_LOAD_PLUGIN" ) ); //$NON-NLS-1$
        }
        repositoriesMeta.addRepository( repositoryMeta );
        repositoriesMeta.writeData();
        return RepositorySyncStatus.REGISTERED;
      } catch ( KettleException e ) {
        log.error( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_REGISTER_REPOSITORY", repositoryId ), e ); //$NON-NLS-1$
        throw new RepositorySyncException( Messages.getInstance().getString( "RepositorySyncWebService.UNABLE_TO_REGISTER_REPOSITORY", repositoryId ), e ); //$NON-NLS-1$
      }
    } else {
      String xml = repositoryMeta.getXML();
      Element node;
      try {
        node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new StringInputStream( xml ) ).getDocumentElement();
      } catch ( Exception e ) {
        node = null;
      }
      if ( node != null ) {
        NodeList list = node.getElementsByTagName( "repository_location_url" ); //$NON-NLS-1$
        if ( list != null && list.getLength() == 1 ) {
          String url = list.item( 0 ).getTextContent();
          if ( url.equals( repositoryUrl ) ) {

            // now test base URL
            String fullyQualifiedServerUrl = null;
            if ( PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() != null ) {
              fullyQualifiedServerUrl = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
              if ( url.endsWith( "/" ) ) { //$NON-NLS-1$
                url = url.substring( 0, url.length() - 2 );
              }
              if ( fullyQualifiedServerUrl.endsWith( "/" ) ) { //$NON-NLS-1$
                fullyQualifiedServerUrl = fullyQualifiedServerUrl.substring( 0, fullyQualifiedServerUrl.length() - 2 );
              }
              if ( url.startsWith( fullyQualifiedServerUrl ) ) {
                return RepositorySyncStatus.ALREADY_REGISTERED;
              }
            }
            log.error( Messages.getInstance().getString( "RepositorySyncWebService.FULLY_QUALIFIED_SERVER_URL_SYNC_PROBLEM", fullyQualifiedServerUrl, url ) ); //$NON-NLS-1$
            throw new RepositorySyncException( Messages.getInstance().getString( "RepositorySyncWebService.FULLY_QUALIFIED_SERVER_URL_SYNC_PROBLEM", fullyQualifiedServerUrl, url ) ); //$NON-NLS-1$
          } else {
            log.error( Messages.getInstance().getString( "RepositorySyncWebService.REPOSITORY_URL_SYNC_PROBLEM", repositoryId, url, repositoryUrl ) ); //$NON-NLS-1$
            throw new RepositorySyncException( Messages.getInstance().getString( "RepositorySyncWebService.REPOSITORY_URL_SYNC_PROBLEM", repositoryId, url, repositoryUrl ) ); //$NON-NLS-1$
          }
        }
      }
      log.error( Messages.getInstance().getString( "RepositorySyncWebService.REPOSITORY_URL_XML_PARSING_PROBLEM", repositoryId, xml ) ); //$NON-NLS-1$
      throw new RepositorySyncException( Messages.getInstance().getString( "RepositorySyncWebService.REPOSITORY_URL_XML_PARSING_PROBLEM_CLIENT_MESSAGE", repositoryId ) ); //$NON-NLS-1$
    }
  }

  private static RepositoryMeta getRepositoryMeta( String repositoryId, String repositoryUrl ) throws KettleException {
    RepositoryMeta repMeta = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, "PentahoEnterpriseRepository", RepositoryMeta.class ); //$NON-NLS-1$
    // this repository is not available
    if ( repMeta == null ) {
      return null;
    }

    String xml = "<repo>" + //$NON-NLS-1$
        "<id>PentahoEnterpriseRepository</id>" + //$NON-NLS-1$
        "<name>" + repositoryId + "</name>" + //$NON-NLS-1$ //$NON-NLS-2$
        "<description>" + repositoryId + "</description>" + //$NON-NLS-1$ //$NON-NLS-2$
        "<repository_location_url>" + repositoryUrl + "</repository_location_url> </repo>"; //$NON-NLS-1$ //$NON-NLS-2$

    Element node;
    try {
      node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new StringInputStream( xml ) ).getDocumentElement();
    } catch ( Exception e ) {
      node = null;
    }
    repMeta.loadXML( node, null );
    return repMeta;
  }
}
