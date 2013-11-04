package org.pentaho.di.job.entries.ftp;

import java.io.File;

public class TestUtils {

  public static String createTempDir() {
    String ret = null;
    try {
      File file = File.createTempFile( "temp_pentaho_test_dir", "" + System.currentTimeMillis() );
      file.delete();
      file.mkdir();
      file.deleteOnExit();
      ret = file.getAbsolutePath();
    } catch ( Exception ex ) {
      System.out.println( "Can't create temp folder" );
      ex.printStackTrace();
    }
    return ret;
  }

}
