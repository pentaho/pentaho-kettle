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



package org.pentaho.s3common;

import java.io.ByteArrayInputStream;

/**
 * Custom InputStream that only virtually skips data to mimic a long running stream
 *
 * @author asimoes
 * @since 09-11-2017
 */
public class S3CommonWindowedSubstream extends ByteArrayInputStream {

  public S3CommonWindowedSubstream( byte[] buf ) {
    super( buf );
  }

  @Override public synchronized long skip( long n ) {
    // virtual skip
    return n;
  }
}
