package org.pentaho.di.core.metrics;

import java.util.Date;

import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.MetricsInterface;
import org.pentaho.di.core.util.StringUtil;

public class MetricsSnapshot implements MetricsSnapshotInterface {

  private Date                date;
  private MetricsSnapshotType type;
  private MetricsInterface    metric;
  private String              subject;
  private String              logChannelId;
  private Long                value;

  /**
   * Create a new metrics snapshot
   * 
   * @param type
   * @param metric
   *          the metric to use (ex. Connect to database)
   * @param subject
   *          the name of the metric subject (ex. the name of the database we're
   *          connecting to)
   * @param logChannelId
   */
  public MetricsSnapshot(MetricsSnapshotType type, MetricsInterface metric, String subject, String logChannelId) {
    this.date = new Date();
    this.type = type;
    this.metric = metric;
    this.subject = subject;
    this.logChannelId = logChannelId;
  }

  /**
   * Create a new metrics snapshot without a subject
   * 
   * @param type
   * @param description
   * @param logChannelId
   */
  public MetricsSnapshot(MetricsSnapshotType type, MetricsInterface metric, String logChannelId) {
    this(type, metric, null, logChannelId);
  }

  /**
   * Create a snapshot metric with a value.
   * 
   * @param type
   *          The type. For metrics with a value it is usually MIN, MAX, SUM,
   *          COUNT, ...
   * @param metric
   *          The metric to use
   * @param subject
   *          The subject
   * @param value
   *          The value
   * @param logChannelId
   *          The logging channel to reference.
   */
  public MetricsSnapshot(MetricsSnapshotType type, MetricsInterface metric, String subject, long value, String logChannelId) {
    this(type, metric, subject, logChannelId);
    this.value = value;
  }

  /**
   * Create a snapshot metric with a value.
   * 
   * @param type
   *          The type. For metrics with a value it is usually MIN, MAX, SUM,
   *          COUNT, ...
   * @param description
   *          The description
   * @param value
   *          The value
   * @param logChannelId
   *          The logging channel to reference.
   */
  public MetricsSnapshot(MetricsSnapshotType type, MetricsInterface metric, long value, String logChannelId) {
    this(type, metric, null, value, logChannelId);
  }

  
  @Override
  public String getKey() {
    if (subject == null) {
      return metric.getCode();
    } else {
      return metric.getCode()+ " / " + subject;
    }
  }
  
  public static String getKey(MetricsInterface metric, String subject) {
    if (subject == null) {
      return metric.getCode();
    } else {
      return metric.getCode() + " / " + subject;
    }
  }
  

  @Override
  public String toString() {
    LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(logChannelId);
    String subject = null;
    if (loggingObject != null) {
      subject = loggingObject.getObjectName() + "(" + loggingObject.getObjectType() + ")";
    } else {
      subject = "-";
    }

    return subject + " - " + getKey() + " @ " + StringUtil.getFormattedDateTime(date, true) + " : " + type.toString();

  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date
   *          the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the type
   */
  public MetricsSnapshotType getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(MetricsSnapshotType type) {
    this.type = type;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject
   *          the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @return the value
   */
  public Long getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(Long value) {
    this.value = value;
  }

  /**
   * @return the logChannelId
   */
  public String getLogChannelId() {
    return logChannelId;
  }

  /**
   * @param logChannelId
   *          the logChannelId to set
   */
  public void setLogChannelId(String logChannelId) {
    this.logChannelId = logChannelId;
  }

  /**
   * @return the metric
   */
  public MetricsInterface getMetric() {
    return metric;
  }

  /**
   * @param metric the metric to set
   */
  public void setMetric(MetricsInterface metric) {
    this.metric = metric;
  }
  
  
}
