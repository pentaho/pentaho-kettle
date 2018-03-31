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

package org.pentaho.di.core.metrics;

import java.util.Date;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.util.StringUtil;

public class MetricsDuration {
  private Date date;
  private Date endDate;
  private String description;
  private String subject;
  private String logChannelId;
  private Long duration;
  private Long count;

  /**
   * @param date
   * @param description
   * @param subject
   * @param logChannelId
   * @param duration
   */
  public MetricsDuration( Date date, String description, String subject, String logChannelId, Long duration ) {
    this( date, description, subject, logChannelId, duration, 1L );
  }

  /**
   * @param date
   * @param description
   * @param subject
   * @param logChannelId
   * @param duration
   */
  public MetricsDuration( Date date, String description, String subject, String logChannelId, Long duration,
    Long count ) {
    this.date = date;
    this.description = description;
    this.subject = subject;
    this.logChannelId = logChannelId;
    this.duration = duration;
    this.count = count;
    this.endDate = new Date( date.getTime() + duration );
  }

  @Override
  public String toString() {
    if ( Utils.isEmpty( subject ) ) {
      return description
        + " @ " + StringUtil.getFormattedDateTime( date, true ) + " : "
        + ( duration == null ? "-" : duration.toString() ) + ( count == null ? "" : " (x" + count + ")" );
    } else {
      return description
        + " / " + subject + " @ " + StringUtil.getFormattedDateTime( date, true ) + " : "
        + ( duration == null ? "-" : duration.toString() ) + ( count == null ? "" : " (x" + count + ")" );
    }
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
  public void setDate( Date date ) {
    this.date = date;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * @return the duration
   */
  public Long getDuration() {
    return duration;
  }

  /**
   * @param duration
   *          the duration to set
   */
  public void setDuration( Long duration ) {
    this.duration = duration;
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
  public void setSubject( String subject ) {
    this.subject = subject;
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
  public void setLogChannelId( String logChannelId ) {
    this.logChannelId = logChannelId;
  }

  public Long getCount() {
    return count;
  }

  public void setCount( Long count ) {
    this.count = count;
  }

  public void incrementCount() {
    if ( count == null ) {
      count = Long.valueOf( 1L );
    } else {
      count = Long.valueOf( count + 1 );
    }
  }

  /**
   * @return the endDate
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @param endDate
   *          the endDate to set
   */
  public void setEndDate( Date endDate ) {
    this.endDate = endDate;
  }
}
