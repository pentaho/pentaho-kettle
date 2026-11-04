/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class S3FileProvider extends AbstractOriginatingFileProvider {

  /**
   * The scheme this provider was designed to support.
   */
  public static final String SCHEME = "s3";

  // S3A compatibility: retain s3 as the PVFS connection type.
  public static final String S3A_SCHEME = "s3a";
  public static final String[] SCHEMES = { SCHEME, S3A_SCHEME };

  /**
   * User Information.
   */
  public static final String ATTR_USER_INFO = "UI";

  /**
   * Authentication types.
   */
  protected static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
    new UserAuthenticationData.Type[] {
      UserAuthenticationData.USERNAME,
      UserAuthenticationData.PASSWORD
    };

  /**
   * The provider's capabilities.
   */
  protected static final Collection<Capability> capabilities =
    Collections.unmodifiableCollection( Arrays.asList(
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.GET_LAST_MODIFIED,
        Capability.RANDOM_ACCESS_READ
      ) );

  public S3FileProvider() {
    super();
    setFileNameParser( S3FileNameParser.getInstance() );
  }

  protected FileSystem doCreateFileSystem( final FileName name, final FileSystemOptions fileSystemOptions ) {
    return new S3FileSystem( name,
      fileSystemOptions == null ? new FileSystemOptions() : (FileSystemOptions) fileSystemOptions.clone() );
  }

  @Override
  protected FileSystem findFileSystem( Comparable<?> key, FileSystemOptions options ) {
    return super.findFileSystem( new S3FileSystemCacheKey( key, options ), options );
  }

  @Override
  protected void addFileSystem( Comparable<?> key, FileSystem fileSystem ) throws FileSystemException {
    super.addFileSystem( new S3FileSystemCacheKey( key, fileSystem.getFileSystemOptions() ), fileSystem );
  }

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

}
