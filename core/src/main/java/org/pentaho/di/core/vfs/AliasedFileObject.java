/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
