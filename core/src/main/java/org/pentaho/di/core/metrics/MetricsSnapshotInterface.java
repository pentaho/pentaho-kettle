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

import org.pentaho.di.core.logging.MetricsInterface;

public interface MetricsSnapshotInterface {

  /**
   * @return The metric that is being recorded, includes type, code and description
   */
  public MetricsInterface getMetric();

  public Date getDate();

  public void setDate( Date date );

  public String getSubject();

  public String getLogChannelId();

  public Long getValue();

  public void setValue( Long value );

  /**
   * Calculate the key for this snapshot, usually a combination of description and subject.
   *
   * @return the key for this snapshot
   */
  public String getKey();

}
