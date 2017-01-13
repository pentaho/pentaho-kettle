package org.pentaho.di.engine.kettleclassic;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;

import java.util.concurrent.TimeUnit;

/**
 * Created by nbaker on 1/4/17.
 */
public class ClassicKettleEngineTest {
  @Test
  public void execute() throws Exception {

    KettleEnvironment.init();
    ClassicKettleEngine engine = new ClassicKettleEngine();
    TransMeta meta = new TransMeta( "src/test/resources/test2.ktr" );
    ITransformation transformation = ClassicUtils.convert( meta );
    ClassicKettleExecutionContext context = (ClassicKettleExecutionContext) engine.prepare( transformation );
    IExecutionResult result = context.execute().get( 30, TimeUnit.SECONDS );
  }

}