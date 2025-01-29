/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.logging;


import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.pentaho.di.core.metrics.MetricsSnapshotInterface;

/**
 * This singleton will capture all the metrics coming from the various log channels based on the log channel ID.
 *
 * @author matt
 *
 */
public class MetricsRegistry {
  private static MetricsRegistry registry = new MetricsRegistry();

  private Map<String, Map<String, MetricsSnapshotInterface>> snapshotMaps;
  private Map<String, Queue<MetricsSnapshotInterface>> snapshotLists;

  public static MetricsRegistry getInstance() {
    return registry;
  }

  private MetricsRegistry() {
    snapshotMaps = new ConcurrentHashMap<String, Map<String, MetricsSnapshotInterface>>();
    snapshotLists = new ConcurrentHashMap<String, Queue<MetricsSnapshotInterface>>();
  }

  public void addSnapshot( LogChannelInterface logChannel, MetricsSnapshotInterface snapshot ) {
    MetricsInterface metric = snapshot.getMetric();
    String channelId = logChannel.getLogChannelId();
    switch ( metric.getType() ) {
      case START:
      case STOP:
        Queue<MetricsSnapshotInterface> list = getSnapshotList( channelId );
        list.add( snapshot );

        break;
      case MIN:
      case MAX:
      case SUM:
      case COUNT:
        Map<String, MetricsSnapshotInterface> map = getSnapshotMap( channelId );
        map.put( snapshot.getKey(), snapshot );

        break;
      default:
        break;
    }
  }

  public Map<String, Queue<MetricsSnapshotInterface>> getSnapshotLists() {
    return snapshotLists;
  }

  public Map<String, Map<String, MetricsSnapshotInterface>> getSnapshotMaps() {
    return snapshotMaps;
  }

  /**
   * Get the snapshot list for the given log channel ID. If no list is available, one is created (and stored).
   *
   * @param logChannelId
   *          The log channel to use.
   * @return an existing or a new metrics snapshot list.
   */
  public Queue<MetricsSnapshotInterface> getSnapshotList( String logChannelId ) {
    Queue<MetricsSnapshotInterface> list = snapshotLists.get( logChannelId );
    if ( list == null ) {
      list = new ConcurrentLinkedQueue<MetricsSnapshotInterface>();
      snapshotLists.put( logChannelId, list );
    }
    return list;

  }

  /**
   * Get the snapshot map for the given log channel ID. If no map is available, one is created (and stored).
   *
   * @param logChannelId
   *          The log channel to use.
   * @return an existing or a new metrics snapshot map.
   */
  public Map<String, MetricsSnapshotInterface> getSnapshotMap( String logChannelId ) {
    Map<String, MetricsSnapshotInterface> map = snapshotMaps.get( logChannelId );
    if ( map == null ) {
      map = new ConcurrentHashMap<String, MetricsSnapshotInterface>();
      snapshotMaps.put( logChannelId, map );
    }
    return map;
  }

  public void reset() {
    snapshotMaps.clear();
    snapshotLists.clear();
  }
}
