package org.pentaho.di.engine.kettleclassic;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.Status;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.trans.TransMeta;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by nbaker on 1/4/17.
 */
public class ClassicKettleEngineTest {
  @Test
  public void execute() throws Exception {

    KettleEnvironment.init();
    ClassicKettleEngine engine = new ClassicKettleEngine();
    TransMeta meta = new TransMeta( "src/test/resources/test.ktr" );
    ITransformation transformation = ClassicUtils.convert( meta );
    ClassicKettleExecutionContext context = (ClassicKettleExecutionContext) engine.prepare( transformation );
    context.setTransMeta( meta );

    context.subscribe( transformation, Status.class,  d -> {
      System.out.println( "Received Transformation Status event: " + d );
    } );

    context.subscribe( transformation.getOperations().get( 0 ), Status.class,  d -> {
      System.out.println( "Received Operation Status event: " + d );
    } );

    context.subscribe( transformation.getOperations().get( 0 ), Metrics.class, d -> {
      System.out.println( "Received Operation Status event: \n" + d );
    } );

    IExecutionResult result = context.execute().get( 30, TimeUnit.SECONDS );
  }

}