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


package org.pentaho.s3common;

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

public abstract class S3CommonFileProvider extends AbstractOriginatingFileProvider {

  /**
   * Authentication types.
   */
  public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
    new UserAuthenticationData.Type[] {
      UserAuthenticationData.USERNAME,
      UserAuthenticationData.PASSWORD
    };

  /**
   * The provider's capabilities.
   */
  protected static final Collection<Capability> capabilities =
    Collections.unmodifiableCollection( Arrays.asList(
      new Capability[] {
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
      } ) );

  public S3CommonFileProvider() {
    super();
  }

  protected abstract FileSystem doCreateFileSystem( final FileName name, final FileSystemOptions fileSystemOptions )
    throws FileSystemException;

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
