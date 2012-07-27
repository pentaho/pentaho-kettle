/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import java.util.Date;
import java.util.Deque;
import java.util.Map;

import org.pentaho.di.core.metrics.MetricsSnapshot;
import org.pentaho.di.core.metrics.MetricsSnapshotInterface;
import org.pentaho.di.core.metrics.MetricsSnapshotType;


public class LogChannel implements LogChannelInterface {
	
	public static LogChannelInterface GENERAL  = new LogChannel("General");
	public static LogChannelInterface METADATA = new LogChannel("Metadata");
	public static LogChannelInterface UI       = new LogChannel("GUI");
	
	private static LogWriter log = LogWriter.getInstance();
	private String logChannelId;
	
	private LogLevel logLevel;
	
	private String containerObjectId;
	
  private boolean gatheringMetrics;
    
  private static MetricsRegistry metricsRegistry = MetricsRegistry.getInstance();

  public LogChannel(Object subject) {
    logLevel = DefaultLogLevel.getLogLevel();
    logChannelId = LoggingRegistry.getInstance().registerLoggingSource(subject);
  }

  public LogChannel(Object subject, boolean gatheringMetrics) {
    this(subject);
    this.gatheringMetrics = gatheringMetrics;
  }

  public LogChannel(Object subject, LoggingObjectInterface parentObject) {
    if (parentObject != null) {
      this.logLevel = parentObject.getLogLevel();
      this.containerObjectId = parentObject.getContainerObjectId();
    } else {
      this.logLevel = DefaultLogLevel.getLogLevel();
      this.containerObjectId = null;
    }
    logChannelId = LoggingRegistry.getInstance().registerLoggingSource(subject);
  }

  public LogChannel(Object subject, LoggingObjectInterface parentObject, boolean gatheringMetrics) {
    this(subject, parentObject);
    this.gatheringMetrics = gatheringMetrics;
  }
	
	@Override
	public String toString() {
		return logChannelId;
	}
	
	public String getLogChannelId() {
		return logChannelId;
	}
		
    public void logMinimal(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogLevel.MINIMAL), logLevel); //$NON-NLS-1$
    }

    public void logBasic(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogLevel.BASIC), logLevel); //$NON-NLS-1$
    }

    public void logError(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogLevel.ERROR), logLevel); //$NON-NLS-1$
    }

    public void logError(String s, Throwable e)
    {
    	log.println(new LogMessage(s, logChannelId, LogLevel.ERROR), e, logLevel); //$NON-NLS-1$
    }

    public void logBasic(String s, Object...arguments) 
    {
    	log.println(new LogMessage(s, logChannelId, arguments, LogLevel.BASIC), logLevel);
    }

    public void logDetailed(String s, Object...arguments) 
    {
    	log.println(new LogMessage(s, logChannelId, arguments, LogLevel.DETAILED), logLevel);
    }

    public void logError(String s, Object...arguments) 
    {
    	log.println(new LogMessage(s, logChannelId, arguments, LogLevel.ERROR), logLevel);
    }

    public void logDetailed(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogLevel.DETAILED), logLevel);
    }

    public void logDebug(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogLevel.DEBUG), logLevel);
    }
    
	public void logDebug(String message, Object... arguments) {
        log.println(new LogMessage(message, logChannelId, arguments, LogLevel.DEBUG), logLevel);
	}

    public void logRowlevel(String s)
    {
        log.println(new LogMessage(s, logChannelId, LogLevel.ROWLEVEL), logLevel);
    }

	public void logMinimal(String message, Object... arguments) {
		log.println(new LogMessage(message, logChannelId, arguments, LogLevel.MINIMAL), logLevel);
	}

	public void logRowlevel(String message, Object... arguments) {
		log.println(new LogMessage(message, logChannelId, arguments, LogLevel.ROWLEVEL), logLevel);
	}

	public boolean isBasic() {
		return logLevel.isBasic();
	}

	public boolean isDebug() {
		return logLevel.isDebug();
	}

	public boolean isDetailed() {
	  try {
	    return logLevel.isDetailed();
	  } catch(NullPointerException ex) {
	    System.out.println("Oops!");
	    return false;
	  }
	}

	public boolean isRowLevel() {
		return logLevel.isRowlevel();
	}

	 public boolean isError() {
	    return logLevel.isError();
	  }

	public LogLevel getLogLevel() {
	  return logLevel;
	}
	
	public void setLogLevel(LogLevel logLevel) {
	  this.logLevel = logLevel;
	}

  /**
   * @return the containerObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the containerObjectId to set
   */
  public void setContainerObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * @return the gatheringMetrics
   */
  public boolean isGatheringMetrics() {
    return gatheringMetrics;
  }

  /**
   * @param gatheringMetrics the gatheringMetrics to set
   */
  public void setGatheringMetrics(boolean gatheringMetrics) {
    this.gatheringMetrics = gatheringMetrics;
  }

  public void snap(MetricsInterface metric, long...value) {
    snap(metric, null, value);
  }
  
  public void snap(MetricsInterface metric, String subject, long...value) {
    if (!isGatheringMetrics()) return;
    
    String key = MetricsSnapshot.getKey(metric, subject);
    
    switch(metric.getType()) {
    case MAX:
      {
        // Calculate and store the maximum value for this metric
        //
        if (value.length!=1) break; // ignore
        
        Map<String, MetricsSnapshotInterface> metricsMap = metricsRegistry.getSnapshotMap(logChannelId);
        MetricsSnapshotInterface snapshot = metricsMap.get(key);
        if (snapshot!=null) {
          if (value[0]>snapshot.getValue()) {
            snapshot.setValue(value[0]);
            snapshot.setDate(new Date());
          }
        } else {
          snapshot = new MetricsSnapshot(MetricsSnapshotType.MAX, metric, subject, value[0], logChannelId);
          metricsMap.put(key, snapshot);
        }
      } 
      break;
    case MIN:
      {
        // Calculate and store the minimum value for this metric
        //
        if (value.length!=1) break; // ignore
        
        Map<String, MetricsSnapshotInterface> metricsMap = metricsRegistry.getSnapshotMap(logChannelId);
        MetricsSnapshotInterface snapshot = metricsMap.get(key);
        if (snapshot!=null) {
          if (value[0]<snapshot.getValue()) {
            snapshot.setValue(value[0]);
            snapshot.setDate(new Date());
          }
        } else {
          snapshot = new MetricsSnapshot(MetricsSnapshotType.MIN, metric, subject, value[0], logChannelId);
          metricsMap.put(key, snapshot);
        }
      }
      break;
    case SUM:
      {
        Map<String, MetricsSnapshotInterface> metricsMap = metricsRegistry.getSnapshotMap(logChannelId);
        MetricsSnapshotInterface snapshot = metricsMap.get(key);
        if (snapshot!=null) {
          snapshot.setValue(snapshot.getValue()+value[0]);
        } else {
          snapshot = new MetricsSnapshot(MetricsSnapshotType.SUM, metric, subject, value[0], logChannelId);
          metricsMap.put(key, snapshot);
        }  
      }
      break;
    case COUNT:
      {
        Map<String, MetricsSnapshotInterface> metricsMap = metricsRegistry.getSnapshotMap(logChannelId);
        MetricsSnapshotInterface snapshot = metricsMap.get(key);
        if (snapshot!=null) {
          snapshot.setValue(snapshot.getValue()+1L);
        } else {
          snapshot = new MetricsSnapshot(MetricsSnapshotType.COUNT, metric, subject, 1L, logChannelId);
          metricsMap.put(key, snapshot);
        }
      }
      break;
    case START:
      {
        Deque<MetricsSnapshotInterface> metricsList = metricsRegistry.getSnapshotList(logChannelId);
        MetricsSnapshotInterface snapshot = new MetricsSnapshot(MetricsSnapshotType.START, metric, subject, 1L, logChannelId);
        metricsList.add(snapshot);
      }
      break;
    case STOP:
    {
      Deque<MetricsSnapshotInterface> metricsList = metricsRegistry.getSnapshotList(logChannelId);
      MetricsSnapshotInterface snapshot = new MetricsSnapshot(MetricsSnapshotType.STOP, metric, subject, 1L, logChannelId);
      metricsList.add(snapshot);
    }
    break;
    }
    
  }
}
