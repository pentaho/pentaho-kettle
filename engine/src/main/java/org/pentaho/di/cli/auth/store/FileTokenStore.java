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

package org.pentaho.di.cli.auth.store;

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.cli.auth.BrokerAuthClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * {@link TokenStore} that persists credentials to a plaintext Java
 * {@link Properties} file under {@code ~/.kettle/.kettle-sessions}.
 *
 * <p>
 * File permissions are set to POSIX 600 (owner read/write only) on Linux and
 * macOS after every write. On Windows the file inherits the NTFS permissions of
 * the user home directory, which provides equivalent single-user protection
 * without requiring native system calls.
 *
 * <p>
 * <b>Security note:</b> Token values are stored in plaintext (only file
 * permissions protect them). This is intentional for the dev/demo fallback
 * path.
 */
public class FileTokenStore implements TokenStore {

  static final String SESSION_FILE_NAME = ".kettle-sessions";
  private static final LogChannelInterface log = new LogChannel( "FileTokenStore" );

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( BrokerAuthClient.class, key, tokens );
  }

  // ctor visible for tests so a custom kettleDirectory can be injected via
  // the subclass hook below; package-private intentionally.
  private final File sessionFile;

  public FileTokenStore() {
    this( new File( Const.getKettleDirectory(), SESSION_FILE_NAME ) );
  }

  /**
   * Constructor for tests that need to redirect the backing file.
   *
   * @param sessionFile absolute path to the properties file to use
   */
  FileTokenStore( File sessionFile ) {
    this.sessionFile = sessionFile;
  }

  /**
   * Always returns {@code true}: writing to the filesystem is always possible
   * as long as the Kettle directory exists (it is created on first run).
   */
  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public void save( StoredCredential credential ) {
    Properties props = CredentialSerializer.toProperties( credential );
    try {
      ensureParentDirectoryExists();
      try ( FileOutputStream fos = new FileOutputStream( sessionFile ) ) {
        props.store( fos, "Pentaho Pan session" );
      }
      setRestrictiveFilePermissions( sessionFile );
      if ( log.isDebug() ) {
        log.logDebug( message( "FileTokenStore.Persisted" ) );
      }
    } catch ( IOException e ) {
      // Do not surface I/O details — they may reference sensitive paths.
      log.logError( message( "FileTokenStore.PersistFailed" ) );
    }
  }

  @Override
  public Optional<StoredCredential> load() {
    if ( !sessionFile.exists() ) {
      return Optional.empty();
    }
    Properties props = new Properties();
    try ( FileInputStream fis = new FileInputStream( sessionFile ) ) {
      props.load( fis );
    } catch ( IOException e ) {
      log.logError( message( "FileTokenStore.ReadFailed", e.getMessage() ) );
      return Optional.empty();
    }
    StoredCredential credential = CredentialSerializer.fromProperties( props );
    if ( log.isDebug() ) {
      log.logDebug( message( "FileTokenStore.Loaded" ) );
    }
    return Optional.of( credential );
  }

  @Override
  public void delete() {
    CredentialFileSupport.deleteIfExists( sessionFile, log, "session file" );
  }

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  private void ensureParentDirectoryExists() throws IOException {
    CredentialFileSupport.ensureParentDirectoryExists( sessionFile );
  }

  private void setRestrictiveFilePermissions( File file ) {
    CredentialFileSupport.applyOwnerOnlyPermissions( file, log, "session file" );
  }
}
