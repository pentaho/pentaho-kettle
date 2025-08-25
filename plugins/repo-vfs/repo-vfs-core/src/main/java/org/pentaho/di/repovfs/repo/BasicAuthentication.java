package org.pentaho.di.repovfs.repo;

import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class BasicAuthentication {

  private final String user;
  private final String password;

  public BasicAuthentication( String user, String password ) {
    this.user = user;
    this.password = password;
  }

  public void applyToClient( Client client ) {
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic( user, password );
    client.register( feature );
  }
}
