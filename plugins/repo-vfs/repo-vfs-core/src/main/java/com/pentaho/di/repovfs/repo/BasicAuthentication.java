package com.pentaho.di.repovfs.repo;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class BasicAuthentication {

  private final String user;
  private final String password;

  public BasicAuthentication( String user, String password ) {
    this.user = user;
    this.password = password;
  }

  public void applyToClient( Client client ) {
    client.addFilter( new HTTPBasicAuthFilter( user, password ) );
  }
}
