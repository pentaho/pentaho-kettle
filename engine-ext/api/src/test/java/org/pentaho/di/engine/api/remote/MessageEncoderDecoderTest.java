/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.engine.api.remote;

import org.junit.Test;
import org.pentaho.di.engine.api.events.MetricsEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.model.Transformation;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.pentaho.di.engine.api.reporting.Metrics;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests MessageEncoder & MessageDecoder classes
 * <p>
 * Created by ccaspanello on 8/14/17.
 */
public class MessageEncoderDecoderTest {

  private MessageEncoder encoder = new MessageEncoder();
  private MessageDecoder decoder = new MessageDecoder();

  @Test
  public void testExecutionRequest() throws Exception {
    Message expected = executionRequest();
    String sMessage = encoder.encode( expected );
    Message actual = decoder.decode( sMessage );

    assertExecutionRequest( (ExecutionRequest) expected, (ExecutionRequest) actual );
  }

  @Test
  public void testMetricEvent() throws Exception {
    Message expected = metricsEvent();
    String sMessage = encoder.encode( expected );
    Message actual = decoder.decode( sMessage );

    assertMetricsEvent( (MetricsEvent) expected, (MetricsEvent) actual );
  }

  @Test
  public void testExecutionFetchRequest() throws Exception {
    Message expected = new ExecutionFetchRequest( UUID.randomUUID().toString() );
    String sMessage = encoder.encode( expected );
    Message actual = decoder.decode( sMessage );

    ExecutionFetchRequest oExpected = (ExecutionFetchRequest) expected;
    ExecutionFetchRequest oActual = (ExecutionFetchRequest) actual;

    assertThat( oExpected.getRequestId(), equalTo( oActual.getRequestId() ) );
  }

  @Test
  public void testStopMessage() throws Exception {
    Message expected = new StopMessage( "MyStop Reason" );
    String sMessage = encoder.encode( expected );
    Message actual = decoder.decode( sMessage );

    StopMessage oExpected = (StopMessage) expected;
    StopMessage oActual = (StopMessage) actual;

    assertThat( oExpected.getReasonPhrase(), equalTo( oActual.getReasonPhrase() ) );
  }

  private MetricsEvent metricsEvent() {
    Operation step1 = new TestOperation( "step2" );
    Operation step2 = new TestOperation( "step2" );
    Operation step3 = new TestOperation( "step2" );

    step1.getTo().add( step2 );
    step2.getFrom().add( step1 );
    step2.getTo().add( step3 );
    step3.getFrom().add( step2 );

    Metrics metrics = new Metrics( 1, 2, 3, 4 );

    return new MetricsEvent<>( step2, metrics );
  }

  private void assertMetricsEvent( MetricsEvent expected, MetricsEvent actual ) {
    // Assert Operation
    Operation expectedOperation = (Operation) expected.getSource();
    Operation actualOperation = (Operation) actual.getSource();

    assertThat( expectedOperation.getId(), equalTo( actualOperation.getId() ) );
    assertThat( expectedOperation.getFrom().size(), equalTo( actualOperation.getFrom().size() ) );
    assertThat( expectedOperation.getTo().size(), equalTo( actualOperation.getTo().size() ) );

    // Assert Metrics
    Metrics expectedMetrics = (Metrics) expected.getData();
    Metrics actualMetrics = (Metrics) actual.getData();

    assertThat( expectedMetrics.getIn(), equalTo( actualMetrics.getIn() ) );
    assertThat( expectedMetrics.getOut(), equalTo( actualMetrics.getOut() ) );
    assertThat( expectedMetrics.getDropped(), equalTo( actualMetrics.getDropped() ) );
    assertThat( expectedMetrics.getInFlight(), equalTo( actualMetrics.getInFlight() ) );
  }


  private ExecutionRequest executionRequest() {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put( "paramKey1", "paramValue1" );
    parameters.put( "paramKey2", "paramValue2" );

    Map<String, Object> environment = new HashMap<>();
    environment.put( "environmentKey1", "environmentParam1" );
    environment.put( "environmentKey2", "environmentParam2" );


    Map<String, Set<Class<? extends Serializable>>> reportingTopics = new HashMap<>();
    Set<Class<? extends Serializable>> topics = new HashSet<>();
    topics.add( Metrics.class );
    topics.add( LogEntry.class );
    reportingTopics.put( "operation1", topics );
    reportingTopics.put( "operation2", topics );

    LogLevel loggingLogLevel = LogLevel.BASIC;
    Principal actingPrincipal = null;

    Transformation transformation = new Transformation( "myTransformation" );
    return new ExecutionRequest( parameters, environment, transformation, reportingTopics, loggingLogLevel,
      actingPrincipal );
  }

  private void assertExecutionRequest( ExecutionRequest expected, ExecutionRequest actual ) {
    assertThat( actual.getRequestId(), equalTo( expected.getRequestId() ) );
    assertThat( actual.getParameters(), equalTo( expected.getParameters() ) );
    assertThat( actual.getEnvironment(), equalTo( expected.getEnvironment() ) );

    assertThat( actual.getTransformation().getId(), equalTo( expected.getTransformation().getId() ) );
    assertThat( actual.getTransformation().getHops(), equalTo( expected.getTransformation().getHops() ) );
    assertThat( actual.getTransformation().getOperations(), equalTo( expected.getTransformation().getOperations() ) );
    assertThat( actual.getTransformation().getConfig(), equalTo( expected.getTransformation().getConfig() ) );

    assertThat( actual.getReportingTopics(), equalTo( expected.getReportingTopics() ) );
    assertThat( actual.getActingPrincipal(), equalTo( expected.getActingPrincipal() ) );
    assertThat( actual.getLoggingLogLevel(), equalTo( expected.getLoggingLogLevel() ) );
  }

}
