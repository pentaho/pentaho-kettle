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



package org.pentaho.s3a.vfs;

import org.apache.commons.vfs2.provider.AbstractFileName;
import org.pentaho.s3common.S3CommonFileObject;

public class S3AFileObject extends S3CommonFileObject {

  S3AFileObject( final AbstractFileName name, final S3AFileSystem fileSystem ) {
    super( name, fileSystem );
  }
}
