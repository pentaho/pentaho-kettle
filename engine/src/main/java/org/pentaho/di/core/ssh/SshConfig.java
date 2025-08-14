package org.pentaho.di.core.ssh;

import java.nio.file.Path;

public class SshConfig {
  public enum AuthType {
    PASSWORD, PUBLIC_KEY
  }

  private String host;
  private int port = 22;
  private String username;
  private String password;
  private AuthType authType = AuthType.PASSWORD;
  private Path keyPath;
  private String passphrase;
  private String proxyHost;
  private int proxyPort;
  private String proxyUser;
  private String proxyPassword;
  private String knownHostsFile;
  private boolean cacheHostKey;
  private long connectTimeoutMillis;
  private long commandTimeoutMillis;
  private SshImplementation implementation;

  public static SshConfig create() {
    return new SshConfig();
  }

  public SshConfig host( String h ) {
    this.host = h;
    return this;
  }

  public SshConfig port( int p ) {
    this.port = p;
    return this;
  }

  public SshConfig username( String u ) {
    this.username = u;
    return this;
  }

  public SshConfig password( String p ) {
    this.password = p;
    return this;
  }

  public SshConfig authType( AuthType a ) {
    this.authType = a;
    return this;
  }

  public SshConfig keyPath( Path k ) {
    this.keyPath = k;
    return this;
  }

  public SshConfig passphrase( String p ) {
    this.passphrase = p;
    return this;
  }

  public SshConfig proxy( String h, int p ) {
    this.proxyHost = h;
    this.proxyPort = p;
    return this;
  }

  public SshConfig proxyAuth( String u, String pw ) {
    this.proxyUser = u;
    this.proxyPassword = pw;
    return this;
  }

  public SshConfig knownHostsFile( String f ) {
    this.knownHostsFile = f;
    return this;
  }

  public SshConfig cacheHostKey( boolean c ) {
    this.cacheHostKey = c;
    return this;
  }

  public SshConfig connectTimeoutMillis( long t ) {
    this.connectTimeoutMillis = t;
    return this;
  }

  public SshConfig commandTimeoutMillis( long t ) {
    this.commandTimeoutMillis = t;
    return this;
  }

  public SshConfig implementation( SshImplementation i ) {
    this.implementation = i;
    return this;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public AuthType getAuthType() {
    return authType;
  }

  public Path getKeyPath() {
    return keyPath;
  }

  public String getPassphrase() {
    return passphrase;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public String getProxyUser() {
    return proxyUser;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public String getKnownHostsFile() {
    return knownHostsFile;
  }

  public boolean isCacheHostKey() {
    return cacheHostKey;
  }

  public long getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public long getCommandTimeoutMillis() {
    return commandTimeoutMillis;
  }

  public SshImplementation getImplementation() {
    return implementation;
  }
}
