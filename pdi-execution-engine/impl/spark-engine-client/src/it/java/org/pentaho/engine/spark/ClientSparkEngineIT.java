package org.pentaho.engine.spark;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.engine.spark.context.SparkExecutionContext;
import org.pentaho.engine.spark.core.SparkRuntime;
import org.pentaho.engine.spark.core.SparkTransformation;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ccaspanello on 1/12/2017.
 */
public class ClientSparkEngineIT {

  private String mainClass = "org.pentaho.spark.application.SparkDriver";
  private String sparkAppProject = "C:/workspace/pentaho-kettle/pdi-execution-engine/impl/spark-application/";
  private File appResource = new File( sparkAppProject + "target/spark-application-7.1-SNAPSHOT.jar" );
  private File hadoopConfDir = new File( "C:/Users/ccaspanello/Desktop/Cluster Config/svqxbdcn6cdh57spark" );
  private File kettleHome = new File( "C:/release/ce/ael" );
  private File sparkHome = new File( "C:/DevTools/spark-2.0.2-bin-hadoop2.7" );
  private File ktrFile = new File( "C:/Users/ccaspanello/Desktop/AEL/SimpleSample.ktr" );

  @Before
  public void before() {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      throw new RuntimeException( "Unable to Init Kettle", e );
    }
  }

  @After
  public void after() {
    KettleEnvironment.shutdown();
  }

  @Test
  public void testLocal() throws Exception {
    TransMeta transMeta = new TransMeta( ktrFile.getAbsolutePath() );

    ClientSparkEngine engine = new ClientSparkEngine();
    ITransformation trans = new SparkTransformation( transMeta );

    SparkExecutionContext context = (SparkExecutionContext) engine.prepare( trans );
    context.setAppResource( appResource );
    context.setMainClass( mainClass );
    context.setSparkHome( sparkHome );
    context.setKettleHome( kettleHome );

    CompletableFuture<IExecutionResult> future = context.execute();
    future.get();
  }

  @Test
  public void testYarnCluster() throws Exception {

    System.setProperty( "HADOOP_USER_NAME", "devuser" );

    TransMeta transMeta = new TransMeta( ktrFile.getAbsolutePath() );

    ClientSparkEngine engine = new ClientSparkEngine();
    ITransformation trans = new SparkTransformation( transMeta );

    SparkExecutionContext context = (SparkExecutionContext) engine.prepare( trans );
    context.setAppResource( appResource );
    context.setMainClass( mainClass );
    context.setSparkHome( sparkHome );
    context.setKettleHome( kettleHome );
    context.setSparkRuntime( SparkRuntime.YARN_CLUSTER );
    context.setHadoopConfDir( hadoopConfDir );

    CompletableFuture<IExecutionResult> future = context.execute();
    future.get();
  }
}
