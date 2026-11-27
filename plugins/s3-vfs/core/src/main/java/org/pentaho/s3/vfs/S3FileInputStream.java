package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.util.MonitorInputStream;

import java.io.InputStream;

public class S3FileInputStream extends MonitorInputStream {

  public S3FileInputStream( InputStream in ) {
    super( in );
  }
}
