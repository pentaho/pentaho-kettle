package org.pentaho.engine.spark.context;

import org.apache.spark.launcher.SparkAppHandle;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import org.pentaho.engine.spark.core.SparkEngineException;
import org.pentaho.engine.spark.core.SparkRuntime;
import org.pentaho.engine.spark.launcher.LocalLauncher;
import org.pentaho.engine.spark.launcher.YarnClusterLauncher;
import org.pentaho.engine.spark.launcher.api.ISparkLauncher;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Spark Execution Context
 * <p>
 * Contains specific properties to be set in order to run the Spark engine.
 */
public class SparkExecutionContext extends BasicExecutionContext {

  private static final Logger LOG = LoggerFactory.getLogger( SparkExecutionContext.class );

  private SparkRuntime sparkRuntime;
  private File appResource;
  private String mainClass;
  private File hadoopConfDir;
  private File sparkHome;
  private File kettleHome;

  public SparkExecutionContext( ITransformation transformation ) {
    super( transformation );
    LOG.trace( "SparkExecutionContext(transformation: {})", transformation );
    sparkRuntime = SparkRuntime.LOCAL;
  }

  @Override
  public CompletableFuture<IExecutionResult> execute() {
    LOG.trace( "execute()" );

    CompletableFuture<IExecutionResult> future = new CompletableFuture<>();

    // Note:  If Application cannot be found Spark Launcher will freeze
    if ( !appResource.exists() ) {
      throw new SparkEngineException( "Unable to find Spark Application to run: " + getKettleLib().toURI() );
    }

    ISparkLauncher launcher = createSparkLauncher();
    SparkAppHandle sparkHandle = launcher.launch();

    sparkHandle.addListener( new SparkAppHandle.Listener() {
      @Override
      public void stateChanged( SparkAppHandle sparkAppHandle ) {
        if ( sparkAppHandle.getState().isFinal() ) {
          future.complete( () -> {
            // TODO: Eventually have results.
            return null;
          } );
        }
      }

      @Override
      public void infoChanged( SparkAppHandle sparkAppHandle ) {
      }
    } );

    return future;
  }

  @Override
  public <S extends IReportingEventSource, D extends Serializable> Publisher<IReportingEvent<S, D>> eventStream(
    S source, Class<D> type ) {
    LOG.trace( "eventStream(source: {}, type: {})", source, type );
    return null;
  }

  @Override
  public Collection<IReportingEventSource> getReportingSources() {
    LOG.trace( "getReportingSources()" );
    return null;
  }

  private ISparkLauncher createSparkLauncher() {
    LOG.trace( "createSparkLauncher()" );
    switch( sparkRuntime ) {
      case LOCAL:
        return new LocalLauncher( this );
      case YARN_CLUSTER:
        return new YarnClusterLauncher( this );
      default:
        throw new SparkEngineException( "Unsupported Spark Context: " + sparkRuntime );
    }
  }

  public File getKettleLib() {
    LOG.trace( "getKettleLib()" );
    return new File( kettleHome, "lib" );
  }

  //<editor-fold desc="Getters & Setters">
  public SparkRuntime getSparkRuntime() {
    return sparkRuntime;
  }

  public void setSparkRuntime( SparkRuntime sparkRuntime ) {
    this.sparkRuntime = sparkRuntime;
  }

  public File getHadoopConfDir() {
    return hadoopConfDir;
  }

  public void setHadoopConfDir( File hadoopConfDir ) {
    this.hadoopConfDir = hadoopConfDir;
  }

  public File getSparkHome() {
    return sparkHome;
  }

  public void setSparkHome( File sparkHome ) {
    this.sparkHome = sparkHome;
  }

  public File getKettleHome() {
    return kettleHome;
  }

  public void setKettleHome( File kettleHome ) {
    this.kettleHome = kettleHome;
  }

  public File getAppResource() {
    return appResource;
  }

  public void setAppResource( File appResource ) {
    this.appResource = appResource;
  }

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass( String mainClass ) {
    this.mainClass = mainClass;
  }
  //</editor-fold>

}
