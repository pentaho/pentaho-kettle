package org.pentaho.engine.spark.launcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.pentaho.engine.spark.core.SparkEngineException;
import org.pentaho.engine.spark.context.SparkExecutionContext;
import org.pentaho.engine.spark.launcher.api.ISparkLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.pentaho.engine.spark.launcher.api.SparkConstants.SPARK_CONF_DIR;
import static org.pentaho.engine.spark.launcher.api.SparkConstants.SPARK_HOME;

/**
 * Local Cluster Launcher
 *
 * Implementation of the ISparkLauncher that runs everything on the local machine.
 */
public class LocalLauncher implements ISparkLauncher {

  private final SparkExecutionContext context;

  public LocalLauncher( SparkExecutionContext context ) {
    this.context = context;
  }

  public SparkAppHandle launch() {
    try {
      File confDir = new File( context.getSparkHome(), "kettleConf" );

      Map<String, String> env = new HashMap<>();
      env.put( SPARK_HOME, context.getSparkHome().toString() );
      env.put( SPARK_CONF_DIR, confDir.toString() );

      createConfigFile( confDir );
      File ktrFile = createKtrFile( confDir );

      SparkLauncher sparkLauncher = new SparkLauncher( env );
      sparkLauncher.setMaster( "local" );
      sparkLauncher.setAppResource( context.getAppResource().toURI().toString() );
      sparkLauncher.setMainClass( context.getMainClass() );
      sparkLauncher.addFile(ktrFile.toURI().toString());

      return sparkLauncher.startApplication();
    } catch ( IOException e ) {
      throw new SparkEngineException( "Unexpected error trying to launch Spark application from Spark API.", e );
    }
  }

  // TODO Duplicate code, fix it
  private File createKtrFile( File confDir ) {
    try {
      File file = new File( confDir, "transformation.ktr" );
      FileUtils.writeStringToFile( file, context.getTransformation().getConfig() );
      return file;
    } catch ( IOException e ) {
      throw new SparkEngineException( "Unexpected error trying to write transformation file to config directory.", e );
    }
  }

  private void createConfigFile( File confDir ) {
    try {
      if ( confDir.exists() ) {
        FileUtils.deleteDirectory( confDir );
      }
      confDir.mkdirs();
      File conf = new File( confDir, "spark-defaults.conf" );

      StringBuilder sb = new StringBuilder();

      // Add Concatinated List of Kettle Jars from HDFS
      sb.append( "spark.jars   " );
      File[] jars = context.getKettleLib().listFiles();
      for ( File jar : jars ) {
        sb.append( "file:///" + jar.getPath().replace( "\\", "/" ) );
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
