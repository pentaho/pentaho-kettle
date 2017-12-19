/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;

import java.util.Deque;
import java.util.List;
import java.util.Queue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.metrics.MetricsDuration;
import org.pentaho.di.core.metrics.MetricsSnapshotInterface;
import org.pentaho.di.core.metrics.MetricsSnapshotType;
import org.pentaho.di.core.metrics.MetricsUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MetricsIT {

  public static final String STRING_TEST_TASK_CODE = "TEST_TASK";
  public static final String STRING_TEST_TASK_DESCRIPTION = "Test task";
  public static Metrics METRIC_START = new Metrics(
    MetricsSnapshotType.START, STRING_TEST_TASK_CODE, STRING_TEST_TASK_DESCRIPTION );
  public static Metrics METRIC_STOP = new Metrics(
    MetricsSnapshotType.STOP, STRING_TEST_TASK_CODE, STRING_TEST_TASK_DESCRIPTION );

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void testBasics() throws Exception {
    LogChannel log = new LogChannel( "BASICS" );
    log.setGatheringMetrics( true );

    log.snap( METRIC_START );
    Thread.sleep( 50 );
    log.snap( METRIC_STOP );

    Queue<MetricsSnapshotInterface> snapshotList =
      MetricsRegistry.getInstance().getSnapshotList( log.getLogChannelId() );
    assertEquals( 2, snapshotList.size() );

    List<MetricsDuration> durationList = MetricsUtil.getDuration( log.getLogChannelId(), METRIC_START );
    assertEquals( 1, durationList.size() );

    MetricsDuration duration = durationList.get( 0 );
    assertTrue( duration.getDuration() >= 50 && duration.getDuration() <= 100 );
  }

  @Test
  public void testTransformation() throws Exception {

    TransMeta transMeta = new TransMeta( "src/it/resources/metrics/simple-test.ktr" );
    transMeta.setGatheringMetrics( true );
    Trans trans = new Trans( transMeta );
    trans.setGatheringMetrics( true );
    trans.execute( null );
    trans.waitUntilFinished();

    LogChannelInterface log = trans.getLogChannel();

    Queue<MetricsSnapshotInterface> snapshotList =
      MetricsRegistry.getInstance().getSnapshotList( log.getLogChannelId() );
    assertTrue( snapshotList.size() >= 4 );

    List<MetricsDuration> durationList =
      MetricsUtil.getDuration( log.getLogChannelId(), Metrics.METRIC_TRANSFORMATION_EXECUTION_START );
    assertEquals( 1, durationList.size() );
    MetricsDuration duration = durationList.get( 0 );
    assertTrue( duration.getDuration() >= 20 && duration.getDuration() <= 5000 );
    assertEquals( Long.valueOf( 1L ), duration.getCount() );

    // Get all durations...
    //
    // durationList = MetricsUtil.getDurations(trans.getLogChannelId());

  }

  @Test
  public void testDatabaseGetRow() throws Exception {

    MetricsRegistry metricsRegistry = MetricsRegistry.getInstance();

    TransMeta insertTransMeta = new TransMeta( "src/it/resources/metrics/insert-data.ktr" );
    Trans insertTrans = new Trans( insertTransMeta );
    insertTrans.setGatheringMetrics( true );
    insertTrans.execute( null );
    insertTrans.waitUntilFinished();

    LogChannelInterface log = insertTrans.getLogChannel();
    Queue<MetricsSnapshotInterface> snapshotList = metricsRegistry.getSnapshotList( log.getLogChannelId() );
    assertTrue( snapshotList.size() >= 4 );

    TransMeta readTransMeta = new TransMeta( "src/it/resources/metrics/read-data.ktr" );
    Trans readTrans = new Trans( readTransMeta );
    readTrans.setGatheringMetrics( true );
    readTrans.execute( null );
    readTrans.waitUntilFinished();

    log = readTrans.getLogChannel();
    snapshotList = metricsRegistry.getSnapshotList( log.getLogChannelId() );
    assertTrue( snapshotList.size() >= 4 );

    Long rowCount = MetricsUtil.getResult( Metrics.METRIC_DATABASE_GET_ROW_COUNT );
    assertNotNull( rowCount );
    assertEquals( Long.valueOf( 1001 ), rowCount );

    Long sumTime = MetricsUtil.getResult( Metrics.METRIC_DATABASE_GET_ROW_SUM_TIME );
    assertNotNull( sumTime );
    assertTrue( sumTime > 0 );

    Long minTime = MetricsUtil.getResult( Metrics.METRIC_DATABASE_GET_ROW_MIN_TIME );
    assertNotNull( minTime );
    assertTrue( minTime < sumTime );

    Long maxTime = MetricsUtil.getResult( Metrics.METRIC_DATABASE_GET_ROW_MAX_TIME );
    assertNotNull( maxTime );
    assertTrue( maxTime >= minTime );
  }
}
