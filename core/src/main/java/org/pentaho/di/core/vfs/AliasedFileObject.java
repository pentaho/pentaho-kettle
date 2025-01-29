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
   * @return
   */
  String getAELSafeURIString();

  public static boolean isAliasedFile( FileObject file) {
    return AliasedFileObject.class.isAssignableFrom( file.getClass() );
  }

}
