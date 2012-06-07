/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.job;

import org.pentaho.ui.xul.XulEventSource;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * User: RFellows
 * Date: 6/5/12
 */
public class BlockableJobConfig implements XulEventSource, Cloneable {
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  protected String jobEntryName = null;
  protected String blockingPollingInterval = String.valueOf(300);
  protected String blockingExecution = Boolean.TRUE.toString();

  public static final String JOB_ENTRY_NAME = "jobEntryName";
  public static final String BLOCKING_EXECUTION = "blockingExecution";
  public static final String BLOCKING_POLLING_INTERVAL = "blockingPollingInterval";

  public String getJobEntryName() {
    return jobEntryName;
  }

  public void setJobEntryName(String jobEntryName) {
    String old = this.jobEntryName;
    this.jobEntryName = jobEntryName;
    pcs.firePropertyChange(JOB_ENTRY_NAME, old, this.jobEntryName);
  }

  public String getBlockingPollingInterval() {
    return blockingPollingInterval;
  }

  public void setBlockingPollingInterval(String blockingPollingInterval) {
    String old = this.blockingPollingInterval;
    this.blockingPollingInterval = blockingPollingInterval;
    pcs.firePropertyChange(BLOCKING_POLLING_INTERVAL, old, this.blockingPollingInterval);
  }

  public String getBlockingExecution() {
    return blockingExecution;
  }

  public void setBlockingExecution(String blockingExecution) {
    String old = this.blockingExecution;
    this.blockingExecution = blockingExecution;
    pcs.firePropertyChange(BLOCKING_EXECUTION, old, this.blockingExecution);
  }

  /**
   * @see {@link PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)}
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  /**
   * @see {@link PropertyChangeSupport#addPropertyChangeListener(String, java.beans.PropertyChangeListener)}
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
    pcs.addPropertyChangeListener(propertyName, l);
  }

  /**
   * @see {@link PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)}
   */
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  /**
   * @see {@link PropertyChangeSupport#removePropertyChangeListener(String, java.beans.PropertyChangeListener)}
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
    pcs.removePropertyChangeListener(propertyName, l);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BlockableJobConfig that = (BlockableJobConfig) o;

    if (jobEntryName != null ? !jobEntryName.equals(that.jobEntryName) : that.jobEntryName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return jobEntryName != null ? jobEntryName.hashCode() : 0;
  }
}
