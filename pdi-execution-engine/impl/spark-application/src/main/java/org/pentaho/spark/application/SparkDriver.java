package org.pentaho.spark.application;

import org.apache.spark.SparkFiles;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.EnginePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.kettleclassic.ClassicKettleExecutionContext;
import org.pentaho.di.engine.kettleclassic.ClassicUtils;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import java.util.concurrent.CompletableFuture;

/**
 * Created by ccaspanello on 1/13/2017.
 */
public class SparkDriver {

  private static final String SPARK_YARN_STAGING_DIR = "SPARK_YARN_STAGING_DIR";
  private static final String KTR_FILENAME = "transformation.ktr";

  public static void main( String[] args ) throws Exception {

    SparkSession spark = SparkSession
      .builder()
      .appName( "SparkDriver" )
      .getOrCreate();


    String yarnStagingDir = System.getenv( SPARK_YARN_STAGING_DIR );
    String ktrFile;
    if ( yarnStagingDir == null ) {
      ktrFile = SparkFiles.get( KTR_FILENAME );
    } else {
      ktrFile = yarnStagingDir + "/" + KTR_FILENAME;
    }


    KettleEnvironment.init();

    TransMeta transMeta = new TransMeta( ktrFile );

    runLegacy( transMeta );
    //runClassic(transMeta);

    spark.stop();
  }

  private static void runLegacy( TransMeta transMeta ) {
    try {
      Trans trans = new Trans( transMeta );
      trans.prepareExecution( null );
      trans.startThreads();
      trans.waitUntilFinished();
    } catch ( KettleException e ) {
      throw new RuntimeException( "Unexpected error: ", e );
    }
  }

  private static void runClassic( TransMeta transMeta ) {
    try {
      PluginRegistry pluginRegistry = PluginRegistry.getInstance();
      PluginInterface pluginInterface = pluginRegistry.findPluginWithId( EnginePluginType.class, "Classic" );
      IEngine engine = (IEngine) pluginRegistry.loadClass( pluginInterface );

      ITransformation transformation = ClassicUtils.convert( transMeta );
      ClassicKettleExecutionContext context = (ClassicKettleExecutionContext) engine.prepare( transformation );
      TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
      context.setExecutionConfiguration( executionConfiguration );
      CompletableFuture<IExecutionResult> future = context.execute();

      // TODO Do something with future
      future.get();

    } catch ( Exception e ) {
      throw new RuntimeException( "Unexpected error: ", e );
    }
  }
}