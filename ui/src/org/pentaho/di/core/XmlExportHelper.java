/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core;

import org.pentaho.di.core.logging.BaseLogTable;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.MetricsLogTable;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that filterers information, before exporting meta to xml.
 *
 * @author IvanNikolaychuk
 */
public class XmlExportHelper {

  /**
   * When exporting meta we should not export user global parameters.
   * Method makes clone for each table and deletes all global parameters.
   * We have to make clones of each table, because we don't want to change real tables content.
   *
   * @param transMeta
   *              meta, that contains log tables to be refactored before export
   */
  public static void swapTables( TransMeta transMeta ) {
    TransLogTable transLogTable = transMeta.getTransLogTable();
    if ( transLogTable != null ) {
      TransLogTable cloneTransLogTable = (TransLogTable) transLogTable.clone();
      cloneTransLogTable.setAllGlobalParametersToNull();
      transMeta.setTransLogTable( cloneTransLogTable );
    }

    StepLogTable stepLogTable = transMeta.getStepLogTable();
    if ( stepLogTable != null ) {
      StepLogTable cloneStepLogTable = (StepLogTable) stepLogTable.clone();
      cloneStepLogTable.setAllGlobalParametersToNull();
      transMeta.setStepLogTable( cloneStepLogTable );
    }

    PerformanceLogTable performanceLogTable = transMeta.getPerformanceLogTable();
    if ( performanceLogTable != null ) {
      PerformanceLogTable clonePerformanceLogTable = (PerformanceLogTable) performanceLogTable.clone();
      clonePerformanceLogTable.setAllGlobalParametersToNull();
      transMeta.setPerformanceLogTable( clonePerformanceLogTable );
    }

    ChannelLogTable channelLogTable = transMeta.getChannelLogTable();
    if ( channelLogTable != null ) {
      ChannelLogTable cloneChannelLogTable = (ChannelLogTable) channelLogTable.clone();
      cloneChannelLogTable.setAllGlobalParametersToNull();
      transMeta.setChannelLogTable( cloneChannelLogTable );
    }

    MetricsLogTable metricsLogTable = transMeta.getMetricsLogTable();
    if ( metricsLogTable != null ) {
      MetricsLogTable cloneMetricsLogTable = (MetricsLogTable) metricsLogTable.clone();
      cloneMetricsLogTable.setAllGlobalParametersToNull();
      transMeta.setMetricsLogTable( cloneMetricsLogTable );
    }
  }

  /**
   * @param jobMeta
   *            contains log tables to be refactored before export
   */
  public static void swapTables( JobMeta jobMeta ) {
    JobLogTable jobLogTable = jobMeta.getJobLogTable();
    if ( jobLogTable != null ) {
      JobLogTable cloneJobLogTable = (JobLogTable) jobLogTable.clone();
      cloneJobLogTable.setAllGlobalParametersToNull();
      jobMeta.setJobLogTable( cloneJobLogTable );
    }

    JobEntryLogTable jobEntryLogTable = jobMeta.getJobEntryLogTable();
    if ( jobEntryLogTable != null ) {
      JobEntryLogTable cloneEntryLogTable = (JobEntryLogTable) jobEntryLogTable.clone();
      cloneEntryLogTable.setAllGlobalParametersToNull();
      jobMeta.setJobEntryLogTable( cloneEntryLogTable );
    }

    ChannelLogTable channelLogTable = jobMeta.getChannelLogTable();
    if ( channelLogTable != null ) {
      ChannelLogTable cloneChannelLogTable = (ChannelLogTable) channelLogTable.clone();
      cloneChannelLogTable.setAllGlobalParametersToNull();
      jobMeta.setChannelLogTable( cloneChannelLogTable );
    }

    List<LogTableInterface> extraLogTables = jobMeta.getExtraLogTables();
    if ( extraLogTables != null ) {
      List<LogTableInterface> cloneExtraLogTables = new ArrayList<>();
      for ( LogTableInterface logTable : extraLogTables ) {
        if ( logTable instanceof BaseLogTable ) {
          if ( logTable instanceof Cloneable ) {
            BaseLogTable cloneExtraLogTable = (BaseLogTable) logTable.clone();
            cloneExtraLogTable.setAllGlobalParametersToNull();
            cloneExtraLogTables.add( (LogTableInterface) cloneExtraLogTable );
          }
        }
      }
      jobMeta.setExtraLogTables( cloneExtraLogTables );
    }
  }
}
