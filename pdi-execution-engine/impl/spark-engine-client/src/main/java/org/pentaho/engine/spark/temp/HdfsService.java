package org.pentaho.engine.spark.temp;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.pentaho.engine.spark.core.SparkEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * HDFS Service
 * <p>
 * TODO Replace with some shim mechanism
 */
public class HdfsService {

  private static final Logger LOG = LoggerFactory.getLogger( HdfsService.class );

  public FileSystem fs;

  public HdfsService( File hadoopConfDir ) {
    LOG.trace( "HdfsService(hadoopConfDir: {}", hadoopConfDir );
    try {
      Configuration configuration = new Configuration();
      for ( File file : hadoopConfDir.listFiles() ) {
        if ( file.getName().endsWith( "site.xml" ) ) {
          LOG.debug( "Added file to Hadoop Config: " + file.getName() );
          configuration.addResource( file.toURI().toURL() );
        }
      }
      fs = FileSystem.get( configuration );
    } catch ( IOException e ) {
      throw new SparkEngineException( "Unable to create HDFS Service for cluster.", e );
    }
  }

  public FileStatus[] list( Path path ) {
    try {
      LOG.trace( "list(path: {})", path );
      return fs.listStatus( path );
    } catch ( IOException e ) {
      String msg = String.format( "Unexpected error calling: list(%s)", path );
      throw new SparkEngineException( msg, e );
    }
  }

  public boolean mkdirs( Path path ) {
    try {
      LOG.trace( "mkdirs(path: {})", path );
      return fs.mkdirs( path );
    } catch ( IOException e ) {
      String msg = String.format( "Unexpected error calling: mkdris(%s)", path );
      throw new SparkEngineException( msg, e );
    }
  }

  public boolean delete( Path path, boolean recursive ) {
    try {
      LOG.trace( "delete(path: {})", path );
      return fs.delete( path, recursive );
    } catch ( IOException e ) {
      String msg = String.format( "Unexpected error calling: delete(%s, %s)", path, recursive );
      throw new SparkEngineException( msg, e );
    }
  }

  public FSDataOutputStream create( Path path ) {
    try {
      LOG.trace( "create(path: {})", path );
      return fs.create( path );
    } catch ( IOException e ) {
      String msg = String.format( "Unexpected error calling: create(%s)", path );
      throw new SparkEngineException( msg, e );
    }
  }

  public FileStatus getFileStatus( Path path ) {
    try {
      LOG.trace( "getFileStatus(path: {})", path );
      return fs.getFileStatus( path );
    } catch ( IOException e ) {
      String msg = String.format( "Unexpected error calling: getFileStatus(%s)", path );
      throw new SparkEngineException( msg, e );
    }
  }


  public boolean exists( Path path ) {
    try {
      LOG.trace( "exists(path: {})", path );
      return fs.exists( path );
    } catch ( IOException e ) {
      String msg = String.format( "Unexpected error calling: exists(%s)", path );
      throw new SparkEngineException( msg, e );
    }
  }
}
