package org.pentaho.engine.spark.installer;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.pentaho.engine.spark.temp.HdfsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Spark Installer
 * <p>
 * Copies Spark related JARs to HDFS.  This prevents jars from being copied every time a job runs and therefore reduces
 * the time it takes for the job to run.
 */
public class SparkInstaller {

  private final HdfsService hdfsService;

  public SparkInstaller( HdfsService hdfsService ) {
    this.hdfsService = hdfsService;
  }

  public void install( File sparkHome, Path destination ) {
    try {

      // Do not install if directory already exists
      if ( hdfsService.exists( destination ) ) {
        return;
      }

      File[] jarFiles = new File( sparkHome, "jars" ).listFiles();
      for ( File jar : jarFiles ) {
        String filename = jar.getName();
        Path output = new Path( destination, filename );
        InputStream inputStream = new FileInputStream( jar );
        FSDataOutputStream outputStream = hdfsService.create( output );
        IOUtils.copy( inputStream, outputStream );
        inputStream.close();
        outputStream.close();
      }
    } catch ( IOException e ) {
      throw new RuntimeException( "Unable to copy Spark Libraries to HDFS.", e );
    }
  }
}
