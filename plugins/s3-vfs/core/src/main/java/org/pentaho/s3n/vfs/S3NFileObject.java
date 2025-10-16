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


package org.pentaho.s3n.vfs;

import org.apache.commons.vfs2.provider.AbstractFileName;
import org.pentaho.s3common.S3CommonFileObject;

public class S3NFileObject extends S3CommonFileObject {

  protected S3NFileObject( final AbstractFileName name, final S3NFileSystem fileSystem ) {
    super( name, fileSystem );
  }
}
