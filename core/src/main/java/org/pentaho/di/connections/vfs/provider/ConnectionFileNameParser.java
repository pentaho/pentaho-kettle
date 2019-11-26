/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

public class ConnectionFileNameParser extends AbstractFileNameParser {
  @Override public FileName parseUri( VfsComponentContext vfsComponentContext, FileName fileName, String uri )
    throws FileSystemException {
    return parseUri( uri, this );
  }

  public static AbstractFileName parseUri( String uri, FileNameParser fileNameParser ) throws FileSystemException {
    StringBuilder name = new StringBuilder();

    String scheme = UriParser.extractScheme( uri, name );
    UriParser.canonicalizePath( name, 0, name.length(), fileNameParser );

    UriParser.fixSeparators( name );

    FileType fileType = UriParser.normalisePath( name );

    // Extract the named connection name
    final String connection = UriParser.extractFirstElement( name );

    String path = name.toString();

    return new ConnectionFileName( scheme, connection, path, fileType );
  }
}
