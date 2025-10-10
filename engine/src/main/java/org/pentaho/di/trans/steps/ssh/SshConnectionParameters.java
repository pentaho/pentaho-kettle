/*
 * ! ******************************************************************************
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

package org.pentaho.di.trans.steps.ssh;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * Parameter object for SSH connection configuration.
 * Groups all the parameters needed for establishing an SSH connection.
 */
public class SshConnectionParameters {

  private final Bowl bowl;
  private final String server;
  private final int port;
  private final String username;
  private final String password;
  private final boolean useKey;
  private final String keyFilename;
  private final String passPhrase;
  private final int timeOut;
  private final VariableSpace space;
  private final String proxyhost;
  private final int proxyport;
  private final String proxyusername;
  private final String proxypassword;

  private SshConnectionParameters( Builder builder ) {
    this.bowl = builder.bowl;
    this.server = builder.server;
    this.port = builder.port;
    this.username = builder.username;
    this.password = builder.password;
    this.useKey = builder.useKey;
    this.keyFilename = builder.keyFilename;
    this.passPhrase = builder.passPhrase;
    this.timeOut = builder.timeOut;
    this.space = builder.space;
    this.proxyhost = builder.proxyhost;
    this.proxyport = builder.proxyport;
    this.proxyusername = builder.proxyusername;
    this.proxypassword = builder.proxypassword;
  }

  /**
   * Creates a new builder for SSH connection parameters.
   */
  public static Builder builder() {
    return new Builder();
  }

  // Getters
  public Bowl getBowl() {
    return bowl;
  }

  public String getServer() {
    return server;
  }

  public int getPort() {
    return port;
  }  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public boolean isUseKey() {
    return useKey;
  }

  public String getKeyFilename() {
    return keyFilename;
  }

  public String getPassPhrase() {
    return passPhrase;
  }

  public int getTimeOut() {
    return timeOut;
  }

  public VariableSpace getSpace() {
    return space;
  }

  public String getProxyhost() {
    return proxyhost;
  }

  public int getProxyport() {
    return proxyport;
  }

  public String getProxyusername() {
    return proxyusername;
  }

  public String getProxypassword() {
    return proxypassword;
  }

  /**
   * Builder class for creating SSH connection parameters.
   */
  public static class Builder {
    private Bowl bowl;
    private String server;
    private int port = 22; // Default SSH port
    private String username;
    private String password;
    private boolean useKey = false;
    private String keyFilename;
    private String passPhrase;
    private int timeOut = 30000; // Default 30 seconds
    private VariableSpace space;
    private String proxyhost;
    private int proxyport;
    private String proxyusername;
    private String proxypassword;

    public Builder bowl( Bowl bowl ) {
      this.bowl = bowl;
      return this;
    }

    public Builder server( String server ) {
      this.server = server;
      return this;
    }

    public Builder port( int port ) {
      this.port = port;
      return this;
    }

    public Builder username( String username ) {
      this.username = username;
      return this;
    }

    public Builder password( String password ) {
      this.password = password;
      return this;
    }

    public Builder useKey( boolean useKey ) {
      this.useKey = useKey;
      return this;
    }

    public Builder keyFilename( String keyFilename ) {
      this.keyFilename = keyFilename;
      return this;
    }

    public Builder passPhrase( String passPhrase ) {
      this.passPhrase = passPhrase;
      return this;
    }

    public Builder timeOut( int timeOut ) {
      this.timeOut = timeOut;
      return this;
    }

    public Builder space( VariableSpace space ) {
      this.space = space;
      return this;
    }

    public Builder proxyhost( String proxyhost ) {
      this.proxyhost = proxyhost;
      return this;
    }

    public Builder proxyport( int proxyport ) {
      this.proxyport = proxyport;
      return this;
    }

    public Builder proxyusername( String proxyusername ) {
      this.proxyusername = proxyusername;
      return this;
    }

    public Builder proxypassword( String proxypassword ) {
      this.proxypassword = proxypassword;
      return this;
    }

    /**
     * Builds the SSH connection parameters.
     */
    public SshConnectionParameters build() {
      return new SshConnectionParameters( this );
    }
  }
}
