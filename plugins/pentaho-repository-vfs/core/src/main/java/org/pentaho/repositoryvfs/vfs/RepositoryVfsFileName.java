/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
