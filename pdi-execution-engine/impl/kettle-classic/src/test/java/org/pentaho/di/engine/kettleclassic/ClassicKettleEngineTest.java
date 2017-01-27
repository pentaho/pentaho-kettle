package org.pentaho.di.engine.kettleclassic;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.trans.TransMeta;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Created by nbaker on 1/4/17.
 */
public class ClassicKettleEngineTest {
  @Test
  public void execute() throws Exception {

    KettleEnvironment.init();
    ClassicKettleEngine engine = new ClassicKettleEngine();
    TransMeta meta = new TransMeta( "src/test/resources/test.ktr" );
    Transformation transformation = ClassicUtils.convert( meta );
    ClassicKettleExecutionContext context = (ClassicKettleExecutionContext) engine.prepare( transformation );

    context.subscribe( transformation, Serializable.class, d -> {
      System.out.println( "Received Transformation Event: " + d );
    } );

    // Receives both Status and Metrics
    context.subscribe( transformation.getOperations().get( 0 ), Serializable.class, d -> {
      System.out.println( "Received Operation Event: " + d );
    } );

    context.subscribe( transformation.getOperations().get( 0 ), Status.class,  d -> {
      System.out.println( "Received Operation Status event: " + d );
    } );

    context.subscribe( transformation.getOperations().get( 0 ), Metrics.class, d -> {
      System.out.println( "Received Operation Metrics event: \n" + d );
    } );

    ExecutionResult result = context.execute().get( 30, TimeUnit.SECONDS );
  }

}