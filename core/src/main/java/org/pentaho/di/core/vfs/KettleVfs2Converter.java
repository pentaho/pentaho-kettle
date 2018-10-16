/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Methods to help with conversion between Kettle UI and Apache VFS2.
 * @see <a href="https://commons.apache.org/proper/commons-vfs/filesystems.html">Apache vfs2</a>
 */
public class KettleVfs2Converter {

  /**
   * Supported Apache vfs2 formats kettle supports.
   */
  private static final List<String> SUPPORTED_PREFIXES;

  static {
    SUPPORTED_PREFIXES = new ArrayList<>( Arrays.asList( "zip", "gz", "tgz", "tar:gz" ) );
  }

  /**
   * Apply apache vfs2 prefix to <code>filePath</code> if certain conditions are meet.
   * @param filePath normal system file path
   * @param kettleFileCompression kettle compression type
   * @param fileMask regex files to include
   * @param excludeFileMask regex files to exclude
   * @return
   */
  public static String normalizeFilePath( String filePath, String kettleFileCompression, String fileMask,
                                          String excludeFileMask ) {
    String vfs2filename = filePath;

    if ( StringUtils.isNotBlank( filePath ) && !isSupportedPrefix( filePath ) ) {
      String compression = ( kettleFileCompression != null ) ? kettleFileCompression.toLowerCase() : "";
      switch ( compression ) {
        case "zip":
          vfs2filename = "zip:" + filePath;
          break;
        case "gzip":
          vfs2filename = ( isDirectory( fileMask, excludeFileMask ) )
            ? "tgz:" + filePath
            : "gz:" + filePath;
          break;
        default:
          // DO NOTHING
          break;
      }
    }

    return vfs2filename;
  }

  /**
   * Determine if masks signify a folder.
   * @param fileMask regex for files to include
   * @param excludeFileMask regex for files to exclude
   * @return true if one or more is not empty, false otherwise.
   */
  private static boolean isDirectory( String fileMask, String excludeFileMask ) {
    return  StringUtils.isNotBlank( fileMask ) || StringUtils.isNotBlank( excludeFileMask );
  }

  /**
   * Determines if <code>filePath</code> has the prefixes that this class supports.
   * @param filePath
   * @return true if prefix matches, false otherwise.
   */
  private static boolean isSupportedPrefix( String filePath ) {
    return SUPPORTED_PREFIXES.stream().anyMatch( p -> filePath.startsWith( p ) );
  }
}
