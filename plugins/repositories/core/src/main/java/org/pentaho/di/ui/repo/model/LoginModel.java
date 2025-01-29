/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.repo.model;

/**
 * Created by bmorrise on 10/20/16.
 */
public class LoginModel {
  private String repositoryName;
  private String username;
  private String password;

  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName( String repositoryName ) {
    this.repositoryName = repositoryName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }
}
