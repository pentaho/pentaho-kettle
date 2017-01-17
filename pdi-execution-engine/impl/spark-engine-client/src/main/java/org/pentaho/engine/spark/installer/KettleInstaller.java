package org.pentaho.engine.spark.installer;

import org.apache.hadoop.fs.Path;
import org.pentaho.engine.spark.temp.HdfsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Kettle Installer
 * <p>
 * Copies the Kettle distribution (current PMR zip) to HDFS.  This prevents jars from being copied every time a job runs
 * and therefore reduces the time it takes for the job to run.
 *
 * // TODO Revisit to see if we can share common code with PMR.
 */
public class KettleInstaller {

  private static final Logger LOG = LoggerFactory.getLogger( KettleInstaller.class );
  private static final String RELATIVE_LOCATION = "/plugins/pentaho-big-data-plugin/pentaho-mapreduce-libraries.zip";

  private final HdfsService hdfsService;

  public KettleInstaller(HdfsService hdfsService){
    this.hdfsService = hdfsService;
  }

  public void install(File kettleHome, Path destination) {
    try {

      // Do not install if directory already exists
      if(hdfsService.exists( destination ))
        return;

      File source = new File(kettleHome, RELATIVE_LOCATION);
      ZipFile zipFile = new ZipFile(source.toString());
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      LOG.debug("Copying Files:");
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        Path path = new Path(destination.toString(), entry.getName());
        if (entry.isDirectory()) {
          hdfsService.mkdirs(path);
        } else {
          OutputStream outputStream = hdfsService.create(path);
          InputStream inputStream = zipFile.getInputStream(entry);
          org.apache.commons.io.IOUtils.copy(inputStream, outputStream);
          outputStream.close();
          inputStream.close();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Unexpected error copying Kettle Installation.", e);
    }
  }
}
