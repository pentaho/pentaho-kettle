package org.pentaho.di.core.logging;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.metrics.MetricsSnapshotInterface;

/**
 * This singleton will capture all the metrics coming from the various log channels based on the log channel ID.
 * 
 * @author matt
 *
 */
public class MetricsRegistry {
  private static MetricsRegistry registry;
  
  private Map<String, Map<String, MetricsSnapshotInterface>> snapshotMaps;
  private Map<String, Deque<MetricsSnapshotInterface>> snapshotLists;
  
  public static MetricsRegistry getInstance() {
    if (registry==null) {
      registry = new MetricsRegistry();
    }
    return registry;
  }
  
  private MetricsRegistry() {
    snapshotMaps = new HashMap<String, Map<String,MetricsSnapshotInterface>>();
    snapshotLists = new HashMap<String, Deque<MetricsSnapshotInterface>>();
  }
  
  public void addSnapshot(LogChannelInterface logChannel, MetricsSnapshotInterface snapshot) {
    MetricsInterface metric = snapshot.getMetric();
    String channelId = logChannel.getLogChannelId();
    switch(metric.getType()) {
    case START:
    case STOP:
      {
        Deque<MetricsSnapshotInterface> list = getSnapshotList(channelId);
        list.add(snapshot);
      }
      break;
    case MIN:
    case MAX:
    case SUM:
    case COUNT:
      {
        Map<String, MetricsSnapshotInterface> map = getSnapshotMap(channelId);
        map.put(snapshot.getKey(), snapshot);
      }
      break;
    }
  }
  
  public Map<String, Deque<MetricsSnapshotInterface>> getSnapshotLists() {
    return snapshotLists;
  }

  public Map<String, Map<String, MetricsSnapshotInterface>> getSnapshotMaps() {
    return snapshotMaps;
  }
  
  /**
   * Get the snapshot list for the given log channel ID.
   * If no list is available, one is created (and stored).
   * 
   * @param logChannelId The log channel to use.
   * @return an existing or a new metrics snapshot list.
   */
  public Deque<MetricsSnapshotInterface> getSnapshotList(String logChannelId) {
    Deque<MetricsSnapshotInterface> list = snapshotLists.get(logChannelId);
    if (list==null) {
      list = new ArrayDeque<MetricsSnapshotInterface>();
      snapshotLists.put(logChannelId, list);
    }
    return list;
    
  }

  /**
   * Get the snapshot map for the given log channel ID.
   * If no map is available, one is created (and stored).
   * 
   * @param logChannelId The log channel to use.
   * @return an existing or a new metrics snapshot map.
   */
  public Map<String, MetricsSnapshotInterface> getSnapshotMap(String logChannelId) {
    Map<String, MetricsSnapshotInterface> map = snapshotMaps.get(logChannelId);
    if (map==null) {
      map = Collections.synchronizedMap(new HashMap<String, MetricsSnapshotInterface>());
      snapshotMaps.put(logChannelId, map);
    }
    return map;
  }
}
