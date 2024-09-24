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
package org.pentaho.repositoryvfs.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

public class RepositoryVfsFileName extends AbstractFileName {

  public RepositoryVfsFileName( final String absPath, final FileType type ) {
    super( "repo", absPath, type );
  }

  @Override
  protected void appendRootUri( StringBuilder buffer, boolean b ) {
    buffer.append( getScheme() );
    buffer.append( ":/" );
    buffer.append( getPath() );
  }

  @Override
  public FileName createName( final String absPath, final FileType fileType ) {
    FileName name = new RepositoryVfsFileName( absPath, fileType );
    return name;
  }
}
