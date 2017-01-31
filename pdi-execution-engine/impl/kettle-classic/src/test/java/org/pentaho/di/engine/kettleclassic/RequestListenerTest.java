package org.pentaho.di.engine.kettleclassic;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.TransMeta;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 1/31/17.
 */
public class RequestListenerTest {
  @Test
  public void requestAdded() throws Exception {

    KettleEnvironment.init();

    RequestListener listener = new RequestListener();
    TransMeta meta = new TransMeta( "src/test/resources/test.ktr" );
    Transformation transformation = ClassicUtils.convert( meta );

    ExecutionRequest request = mock( ExecutionRequest.class );
    when( request.getTransformation() ).thenReturn( transformation );
    when( request.getEnvironment() ).thenReturn( Collections.emptyMap() );
    when( request.getParameters() ).thenReturn( Collections.emptyMap() );
    Map<String, Set<Class<? extends Serializable>>> topics = new HashMap<>(  );
    topics.put( transformation.getId(), new HashSet<>( Collections.singletonList( Status.class ) ) );
    when( request.getReportingTopics() ).thenReturn( topics );

    CountDownLatch latch = new CountDownLatch( 1 );
    when( request.update( transformation.getId(), Status.STOPPED ) ).then( invocationOnMock -> {
      latch.countDown();
      return true;
    } );

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit( () -> listener.requestAdded( request ) );

    latch.await( 2, TimeUnit.SECONDS );
    verify( request, times( 1 ) ).update( anyObject() );
    verify( request, times( 1 ) ).update( transformation.getId(), Status.RUNNING );
    verify( request, times( 1 ) ).update( transformation.getId(), Status.FINISHED );
  }

}