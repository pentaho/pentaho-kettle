package org.pentaho.di.core.logging;

import java.util.Deque;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.metrics.MetricsDuration;
import org.pentaho.di.core.metrics.MetricsSnapshotInterface;
import org.pentaho.di.core.metrics.MetricsSnapshotType;
import org.pentaho.di.core.metrics.MetricsUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class MetricsTest extends TestCase {
  
  public static final String STRING_TEST_TASK_CODE = "TEST_TASK";
  public static final String STRING_TEST_TASK_DESCRIPTION = "Test task";
  public static Metrics METRIC_START= new Metrics(MetricsSnapshotType.START, STRING_TEST_TASK_CODE, STRING_TEST_TASK_DESCRIPTION);
  public static Metrics METRIC_STOP= new Metrics(MetricsSnapshotType.STOP, STRING_TEST_TASK_CODE, STRING_TEST_TASK_DESCRIPTION);

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testBasics() throws Exception {
    LogChannel log = new LogChannel("BASICS");
    log.setGatheringMetrics(true);
    
    log.snap(METRIC_START);
    Thread.sleep(50);
    log.snap(METRIC_STOP);
    
    Deque<MetricsSnapshotInterface> snapshotList = MetricsRegistry.getInstance().getSnapshotList(log.getLogChannelId());
    assertEquals(2, snapshotList.size());
    
    List<MetricsDuration> durationList = MetricsUtil.getDuration(log.getLogChannelId(), STRING_TEST_TASK_DESCRIPTION);
    assertEquals(1, durationList.size());
    
    MetricsDuration duration = durationList.get(0);
    assertTrue(duration.getDuration()>=50 && duration.getDuration()<=100);
  }
  
  public void testTransformation() throws Exception {
    
    TransMeta transMeta = new TransMeta("testfiles/metrics/simple-test.ktr");
    Trans trans = new Trans(transMeta);
    trans.execute(null);
    trans.waitUntilFinished();
    
    LogChannelInterface log = trans.getLogChannel();
    
    Deque<MetricsSnapshotInterface> snapshotList = MetricsRegistry.getInstance().getSnapshotList(log.getLogChannelId());
    assertEquals(2, snapshotList.size());
    
    List<MetricsDuration> durationList = MetricsUtil.getDuration(log.getLogChannelId(), STRING_TEST_TASK_DESCRIPTION);
    assertEquals(1, durationList.size());
    
    MetricsDuration duration = durationList.get(0);
    assertTrue(duration.getDuration()>=50 && duration.getDuration()<=100);
  }
}
