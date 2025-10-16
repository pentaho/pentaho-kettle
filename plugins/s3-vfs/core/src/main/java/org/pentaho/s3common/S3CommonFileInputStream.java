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

package org.pentaho.s3common;

import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.vfs2.util.MonitorInputStream;

import java.io.IOException;
import java.io.InputStream;

public class S3CommonFileInputStream extends MonitorInputStream {

  private S3Object s3Object;

  public S3CommonFileInputStream( InputStream in, S3Object s3Object ) {
    super( in );
    this.s3Object = s3Object;
  }

  @Override
  protected void onClose() throws IOException {
    super.onClose();
    this.s3Object.close();
  }
}
