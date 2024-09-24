/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.repository.pur.provider;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a PUR repository in OSGI environment via a proxy.
 * The repository is connected to the first time it is accessed.
 */
public class RepositoryProvider implements KettleRepositoryProvider {

  private static final Logger logger = LoggerFactory.getLogger( RepositoryProvider.class );
  private String username;
  private String password;
  private boolean shouldReconnect = true;
  private Repository repository;

  public String getUsername() {
    return username;
  }

  public void setUsername( String repoUsername ) {
    this.username = repoUsername;
    this.shouldReconnect = true;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
    this.shouldReconnect = true;
  }

  @Override
  public Repository getRepository() {
    if ( this.shouldReconnect ) {
      this.reconnectToRepository();
    }
    return this.repository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }


  /**
   * Resets the repository so that next time {@link #getRepository()} is called a new repository is returned.
   * This is called whenever the user or password is changed for this instance of the repository
   */
  public void reconnectToRepository() {
    if ( this.repository == null ) {
      return;
    }

    if ( this.repository.isConnected() ) {
      this.repository.disconnect();
    }

    try {
      this.repository.connect( this.getUsername(), this.getPassword() );
      this.shouldReconnect = false;
    } catch ( KettleException e ) {
      logger.debug( "Unable to connect to repository \"{}\".", this.repository.getRepositoryMeta().getId() );
    }
  }
}
