package org.pentaho.di.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

public class TestUtils {

  public static String createTempDir() {
    String ret = null;
    try {
      /*
       * Java.io.File only creates Temp files, so repurpose the filename for a temporary folder
       * Delete the file that's created, and re-create as a folder. 
       */
      File file = File.createTempFile( "temp_pentaho_test_dir", String.valueOf( System.currentTimeMillis() ) );
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

  public static File getInputFile( String prefix, String suffix ) throws IOException {
    File inputFile = File.createTempFile( prefix, suffix );
    inputFile.deleteOnExit();
    FileUtils.writeStringToFile( inputFile, UUID.randomUUID().toString(), "UTF-8" );
    return inputFile;
  }
}
