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


package org.pentaho.di.core.vfs;

import org.apache.commons.vfs2.FileObject;

public interface AliasedFileObject {

  /**
   * Returns the string that contains original file object URI.
   *
   * @return original URI string
   */
  String getOriginalURIString();

  /**
   * Returns the original file object URI but swaps s3:// for s3a://
   * when needed
   *
   * @return URI string with the scheme adjusted to s3a://
   * @deprecated As of 11.1.0.0, implement this logic per provider as needed. This method was added to support a
   * specific use case in AEL that is no longer relevant for this class and method.
   */
  @Deprecated( since = "11.1.0.0", forRemoval = true )
  String getAELSafeURIString();

  static boolean isAliasedFile( FileObject file ) {
    return AliasedFileObject.class.isAssignableFrom( file.getClass() );
  }

}
