/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2019 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.di.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;

import java.io.ByteArrayInputStream;

/**
 *  Provides a utility method to connect to repository and return the repository object
 */
public class RepositoryConnectionUtils {
  private static final Log logger = LogFactory.getLog( RepositoryConnectionUtils.class );
  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";
  private static Class<?> PKG = RepositoryConnectionUtils.class;


  /**
   * Connects to the PDI repository. If the  system settings "SINGLE_DI_SERVER_INSTANCE" is set, it will connect to
   * repository using the embedded repository.xml otherwise it will connect to repository with the given name.
   *
   * @param   repositoryName RepositoryName
   * @param   isSingleDiServerInstance Checking if the property "singleDiServerInstance" in PentahoSystems is set
   * @param   userName Repo user name
   * @param   fullyQualifiedServerUrl Fully qualified server Url used for connecting to the repository
   * @param   logBuffer
   * @return  Repository
   * @throws  KettleException
   * @throws  KettleSecurityException
   */
  public static Repository connectToRepository( final String repositoryName, boolean isSingleDiServerInstance,
             String userName, String fullyQualifiedServerUrl, LoggingBuffer logBuffer ) throws KettleSecurityException,
             KettleException {

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Creating Meta-repository" );
    }

    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();

    if ( logger.isDebugEnabled() ) {
      logger.debug( " Populating Meta repository" );
    }

    try {
      if ( isSingleDiServerInstance ) {
        if ( logger.isDebugEnabled() ) {
          logger.debug( "singleDiServerInstance=true, loading default repository" );
        }

        // Get the embedded repository xml
        String repositoriesXml = getEmbeddedDefaultRepositoryXml( fullyQualifiedServerUrl );

        ByteArrayInputStream sbis = new ByteArrayInputStream( repositoriesXml.getBytes( Const.XML_ENCODING ) );
        repositoriesMeta.readDataFromInputStream( sbis );
      } else {
        // TODO: add support for specified repositories.xml files...
        repositoriesMeta.readData(); // Read from the default $HOME/.kettle/repositories.xml file.
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        "RepositoryConnectionUtils.ERROR_0018_META_REPOSITORY_NOT_POPULATED" ), e ); //$NON-NLS-1$
    }

    if ( logger.isDebugEnabled() ) {
      logger.debug( " Finding repository metadata" );
    }
    // Find the specified repository.
    RepositoryMeta repositoryMeta = null;
    try {
      if ( isSingleDiServerInstance ) {
        repositoryMeta = repositoriesMeta.findRepository( SINGLE_DI_SERVER_INSTANCE );
      } else {
        repositoryMeta = repositoriesMeta.findRepository( repositoryName );
      }


    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
        "RepositoryConnectionUtils.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName ), e ); // $NON-NLS-1$
    }

    if ( repositoryMeta == null ) {
      if ( logger.isDebugEnabled() && logBuffer != null ) {
        logger.debug( logBuffer.getBuffer().toString() );
      }
      throw new KettleException( BaseMessages.getString( PKG,
        "RepositoryConnectionUtils.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName ) ); //$NON-NLS-1$
    }

    if ( logger.isDebugEnabled() ) {
      logger.debug( " Getting repository instance " ); //$NON-NLS-1$
    }
    Repository repository = null;
    try {
      repository =
        PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta.getId(),
          Repository.class );
      repository.init( repositoryMeta );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        "RepositoryConnectionUtils.ERROR_0016_COULD_NOT_GET_REPOSITORY_INSTANCE" ), e ); // $NON-NLS-1$
    }

    // OK, now try the username and password
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Connecting to repository " );
    }

    // Two scenarios here: internal to server or external to server. If internal, you are already authenticated. If
    // external, you must provide a username and additionally specify that the IP address of the machine running this
    // code is trusted.
    repository.connect( userName, "ignore" );

    // OK, the repository is open and ready to use.
    if ( logger.isDebugEnabled() ) {
      logger.debug( " Connected to repository " );
    }

    return repository;
  }

  private static String getEmbeddedDefaultRepositoryXml( final String fullyQualifiedServerUrl ) {
    // only load a default enterprise repository. If this option is set, then you cannot load
    // transformations or jobs from anywhere but the local server.

    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><repositories>" //$NON-NLS-1$
      + "<repository><id>PentahoEnterpriseRepository</id>" //$NON-NLS-1$
      + "<name>" + SINGLE_DI_SERVER_INSTANCE + "</name>" //$NON-NLS-1$ //$NON-NLS-2$
      + "<description>" + SINGLE_DI_SERVER_INSTANCE + "</description>" //$NON-NLS-1$ //$NON-NLS-2$
      + "<repository_location_url>" + fullyQualifiedServerUrl
      + "</repository_location_url>" //$NON-NLS-1$ //$NON-NLS-2$
      + "<version_comment_mandatory>N</version_comment_mandatory>" //$NON-NLS-1$
      + "</repository>" //$NON-NLS-1$
      + "</repositories>"; //$NON-NLS-1$
  }

}
