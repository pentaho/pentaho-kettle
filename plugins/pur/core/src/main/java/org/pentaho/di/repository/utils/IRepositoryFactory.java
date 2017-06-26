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
package org.pentaho.di.repository.utils;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Implementations can be used to obtain a PDI Repository instance in the platform. Created by nbaker on 11/5/15.
 */
public interface IRepositoryFactory {

  String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";

  Repository connect( String repositoryName ) throws KettleException;

  IRepositoryFactory DEFAULT = new CachingRepositoryFactory() {
    {
      // Make sure we're registered with PentahoSystem so we can be found.
      PentahoSystem.registerObject( this );
    }
  };

  /**
   * Sets the "ID" of the Repository Plugin Type to use (filebased, db, enterprise)
   * 
   * @param id
   */
  void setRepositoryId( String id );

  /**
   * Decorating implementation which caches Repository instances by Principal name in the ICacheManager.
   * DefaultRepositoryFactory used by default if a delegate factory isn't supplied.
   */
  class CachingRepositoryFactory implements IRepositoryFactory {

    public static final String REGION = "pdi-repository-cache";
    private IRepositoryFactory delegate;
    private Logger logger = LoggerFactory.getLogger( getClass() );

    public CachingRepositoryFactory() {
      this( new DefaultRepositoryFactory() );
    }

    public CachingRepositoryFactory( IRepositoryFactory delegate ) {
      this.delegate = delegate;
    }

    @Override
    public void setRepositoryId( String id ) {
      delegate.setRepositoryId( id );
    }

    @Override
    public Repository connect( String repositoryName ) throws KettleException {

      IPentahoSession session = PentahoSessionHolder.getSession();
      if ( session == null ) {
        logger.debug( "No active Pentaho Session, attempting to load PDI repository unauthenticated." );
        throw new KettleException( "Attempting to create PDI Repository with no Active PentahoSession. "
            + "This is not allowed." );
      }
      ICacheManager cacheManager = PentahoSystem.getCacheManager( session );

      String sessionName = session.getName();
      Repository repository = (Repository) cacheManager.getFromRegionCache( REGION, sessionName );
      if ( repository == null ) {
        logger.debug( "Repository not cached for user: " + sessionName + ". Creating new Repository." );
        repository = delegate.connect( repositoryName );
        if ( !cacheManager.cacheEnabled( REGION ) ) {
          cacheManager.addCacheRegion( REGION );
        }
        cacheManager.putInRegionCache( REGION, sessionName, repository );
      } else {
        logger.debug( "Repository was cached for user: " + sessionName );
      }
      return repository;
    }
  }

  /**
   * Default implementation of the RepositoryFactory. Code moved from PDIImportUtil here pretty much as-is.
   */
  class DefaultRepositoryFactory implements IRepositoryFactory {
    private String repositoryId = "PentahoEnterpriseRepository";

    @Override
    public void setRepositoryId( String id ) {
      this.repositoryId = id;
    }

    @Override
    public Repository connect( String repositoryName ) throws KettleException {

      RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
      boolean singleDiServerInstance =
          "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      try {
        if ( singleDiServerInstance ) {

          // only load a default enterprise repository. If this option is set, then you cannot load
          // transformations or jobs from anywhere but the local server.

          String repositoriesXml =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?><repositories>" //$NON-NLS-1$
                  + "<repository><id>" + repositoryId + "</id>" //$NON-NLS-1$
                  + "<name>" + SINGLE_DI_SERVER_INSTANCE + "</name>" //$NON-NLS-1$ //$NON-NLS-2$
                  + "<description>" + SINGLE_DI_SERVER_INSTANCE + "</description>" //$NON-NLS-1$ //$NON-NLS-2$
                  + "<repository_location_url>"
                  + PentahoSystem.getApplicationContext().getFullyQualifiedServerURL()
                  + "</repository_location_url>" //$NON-NLS-1$ //$NON-NLS-2$
                  + "<version_comment_mandatory>N</version_comment_mandatory>" //$NON-NLS-1$
                  + "</repository>" //$NON-NLS-1$
                  + "</repositories>"; //$NON-NLS-1$

          ByteArrayInputStream sbis = new ByteArrayInputStream( repositoriesXml.getBytes( "UTF8" ) );
          repositoriesMeta.readDataFromInputStream( sbis );
        } else {
          // TODO: add support for specified repositories.xml files...
          repositoriesMeta.readData(); // Read from the default $HOME/.kettle/repositories.xml file.
        }
      } catch ( Exception e ) {
        throw new KettleException( "Meta repository not populated", e ); //$NON-NLS-1$
      }

      // Find the specified repository.
      RepositoryMeta repositoryMeta = null;
      try {
        if ( singleDiServerInstance ) {
          repositoryMeta = repositoriesMeta.findRepository( SINGLE_DI_SERVER_INSTANCE );
        } else {
          repositoryMeta = repositoriesMeta.findRepository( repositoryName );
        }

      } catch ( Exception e ) {
        throw new KettleException( "Repository not found", e ); //$NON-NLS-1$
      }

      if ( repositoryMeta == null ) {
        throw new KettleException( "RepositoryMeta is null" ); //$NON-NLS-1$
      }

      Repository repository = null;
      try {
        repository =
            PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta.getId(),
                Repository.class );
        repository.init( repositoryMeta );

      } catch ( Exception e ) {
        throw new KettleException( "Could not get repository instance", e ); //$NON-NLS-1$
      }

      // Two scenarios here: internal to server or external to server. If internal, you are already authenticated. If
      // external, you must provide a username and additionally specify that the IP address of the machine running this
      // code is trusted.
      repository.connect( PentahoSessionHolder.getSession().getName(), "password" );

      return repository;
    }
  }
}
