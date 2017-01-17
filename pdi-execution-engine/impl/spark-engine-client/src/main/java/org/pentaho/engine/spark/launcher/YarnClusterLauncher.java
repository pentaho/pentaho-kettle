package org.pentaho.engine.spark.launcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.pentaho.engine.spark.temp.HdfsService;
import org.pentaho.engine.spark.core.SparkEngineException;
import org.pentaho.engine.spark.context.SparkExecutionContext;
import org.pentaho.engine.spark.installer.KettleInstaller;
import org.pentaho.engine.spark.installer.SparkInstaller;
import org.pentaho.engine.spark.launcher.api.ISparkLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.pentaho.engine.spark.launcher.api.SparkConstants.*;

/**
 * YARN Cluster Launcher
 * <p>
 * Implementation of the ISparkLauncher that runs on YARN as well as the driver application.
 */
public class YarnClusterLauncher implements ISparkLauncher {

  private final SparkExecutionContext context;
  private final HdfsService hdfsService;

  public YarnClusterLauncher( SparkExecutionContext context ) {
    this.context = context;
    this.hdfsService = new HdfsService( context.getHadoopConfDir() );
  }

  public SparkAppHandle launch() {
    try {
      Path sparkLib = new Path( "/opt/pentaho/spark/lib" );
      Path kettleHdfs = new Path( "/opt/pentaho/spark/kettle" );

      SparkInstaller sparkInstaller = new SparkInstaller( hdfsService );
      sparkInstaller.install( context.getSparkHome(), sparkLib );

      KettleInstaller kettleInstaller = new KettleInstaller( hdfsService );
      kettleInstaller.install( context.getKettleHome(), kettleHdfs );

      File confDir = new File( context.getSparkHome(), "kettleConf" );

      Map<String, String> env = new HashMap<>();
      env.put( SPARK_HOME, context.getSparkHome().toString() );
      env.put( HADOOP_CONF_DIR, context.getHadoopConfDir().toString() );
      env.put( SPARK_CONF_DIR, confDir.toString() );
      env.put( HADOOP_USER_NAME, System.getProperty( "HADOOP_USER_NAME" ) );

      createConfigFile( confDir, sparkLib, new Path( kettleHdfs, "lib" ) );
      File ktrFile = createKtrFile( confDir );

      SparkLauncher sparkLauncher = new SparkLauncher( env );
      sparkLauncher.setMaster( "yarn" );
      sparkLauncher.setDeployMode( "cluster" );
      sparkLauncher.setAppResource( context.getAppResource().toURI().toString() );
      sparkLauncher.setMainClass( context.getMainClass() );
      sparkLauncher.addFile( ktrFile.toURI().toString() );

      return sparkLauncher.startApplication();
    } catch ( IOException e ) {
      throw new SparkEngineException( "Unexpected error trying to launch Spark application from Spark API.", e );
    }
  }

  private File createKtrFile( File confDir ) {
    try {
      File file = new File( confDir, "transformation.ktr" );
      FileUtils.writeStringToFile( file, context.getTransformation().getConfig() );
      return file;
    } catch ( IOException e ) {
      throw new SparkEngineException( "Unexpected error trying to write transformation file to config directory.", e );
    }
  }

  private void createConfigFile( File confDir, Path sparkLib, Path kettleLib ) {
    try {
      if ( confDir.exists() ) {
        FileUtils.deleteDirectory( confDir );
      }
      confDir.mkdirs();
      File conf = new File( confDir, "spark-defaults.conf" );

      StringBuilder sb = new StringBuilder();

      // Add Spark Libraries on HDFS
      sb.append( "spark.yarn.archive     " );
      sb.append( hdfsService.getFileStatus( sparkLib ).getPath().toString() );

      sb.append( "\n" );

      // Add Concatinated List of Kettle Jars from HDFS
      sb.append( "spark.yarn.dist.jars   " );
      FileStatus[] fileStatuses = hdfsService.list( kettleLib );
      for ( FileStatus fileStatus : fileStatuses ) {
        sb.append( fileStatus.getPath().toString() );
        sb.append( "," );
      }

      OutputStream outputStream = new FileOutputStream( conf );
      IOUtils.write( sb.toString(), outputStream );
      outputStream.close();
    } catch ( IOException e ) {
      throw new RuntimeException( "Unable to create Kettle Conf for Spark.", e );
    }
  }

}
