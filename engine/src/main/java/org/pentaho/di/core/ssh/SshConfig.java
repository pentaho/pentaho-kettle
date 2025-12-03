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

package org.pentaho.di.core.ssh;

import java.nio.file.Path;
import org.pentaho.di.core.logging.LogChannelInterface;

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
  private byte[] keyContent;
  private String passphrase;
  private String proxyHost;
  private int proxyPort;
  private String proxyUser;
  private String proxyPassword;
  private String knownHostsFile;
  private boolean cacheHostKey;
  private long connectTimeoutMillis;
  private long commandTimeoutMillis;
  private LogChannelInterface logChannel;

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
    this.keyContent = null; // Clear content when path is set
    return this;
  }

  public SshConfig keyContent( byte[] content ) {
    this.keyContent = content != null ? content.clone() : null;
    this.keyPath = null; // Clear path when content is set
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

  public SshConfig logChannel( LogChannelInterface log ) {
    this.logChannel = log;
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

  public byte[] getKeyContent() {
    return keyContent != null ? keyContent.clone() : null;
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

  public LogChannelInterface getLogChannel() {
    return logChannel;
  }
}
