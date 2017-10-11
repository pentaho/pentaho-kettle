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

package org.pentaho.di.trans.steps.systemdata;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.version.BuildVersion;

/**
 * Get information from the System or the supervising transformation.
 *
 * @author Matt
 * @since 4-aug-2003
 */
public class SystemData extends BaseStep implements StepInterface {
  private SystemDataMeta meta;
  private SystemDataData data;

  public SystemData( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] getSystemData( RowMetaInterface inputRowMeta, Object[] inputRowData ) throws KettleException {
    Object[] row = new Object[data.outputRowMeta.size()];
    for ( int i = 0; i < inputRowMeta.size(); i++ ) {
      row[i] = inputRowData[i]; // no data is changed, clone is not needed here.
    }
    for ( int i = 0, index = inputRowMeta.size(); i < meta.getFieldName().length; i++, index++ ) {
      Calendar cal;

      int argnr = 0;

      switch ( meta.getFieldType()[i] ) {
        case TYPE_SYSTEM_INFO_SYSTEM_START:
          row[index] = getTrans().getCurrentDate();
          break;
        case TYPE_SYSTEM_INFO_SYSTEM_DATE:
          row[index] = new Date();
          break;
        case TYPE_SYSTEM_INFO_TRANS_DATE_FROM:
          row[index] = getTrans().getStartDate();
          break;
        case TYPE_SYSTEM_INFO_TRANS_DATE_TO:
          row[index] = getTrans().getEndDate();
          break;
        case TYPE_SYSTEM_INFO_JOB_DATE_FROM:
          row[index] = getTrans().getJobStartDate();
          break;
        case TYPE_SYSTEM_INFO_JOB_DATE_TO:
          row[index] = getTrans().getJobEndDate();
          break;
        case TYPE_SYSTEM_INFO_PREV_DAY_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.DAY_OF_MONTH, -1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_DAY_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.DAY_OF_MONTH, -1 );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_DAY_START:
          cal = Calendar.getInstance();
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_DAY_END:
          cal = Calendar.getInstance();
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_DAY_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_DAY_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_MONTH_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, -1 );
          cal.set( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_MONTH_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, -1 );
          cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_MONTH_START:
          cal = Calendar.getInstance();
          cal.set( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_MONTH_END:
          cal = Calendar.getInstance();
          cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_MONTH_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, 1 );
          cal.set( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_MONTH_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, 1 );
          cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_COPYNR:
          row[index] = new Long( getCopy() );
          break;
        case TYPE_SYSTEM_INFO_TRANS_NAME:
          row[index] = getTransMeta().getName();
          break;
        case TYPE_SYSTEM_INFO_MODIFIED_USER:
          row[index] = getTransMeta().getModifiedUser();
          break;
        case TYPE_SYSTEM_INFO_MODIFIED_DATE:
          row[index] = getTransMeta().getModifiedDate();
          break;
        case TYPE_SYSTEM_INFO_TRANS_BATCH_ID:
          row[index] = new Long( getTrans().getBatchId() );
          break;
        case TYPE_SYSTEM_INFO_JOB_BATCH_ID:
          row[index] = new Long( getTrans().getPassedBatchId() );
          break;
        case TYPE_SYSTEM_INFO_HOSTNAME_REAL:
          row[index] = Const.getHostnameReal();
          break;
        case TYPE_SYSTEM_INFO_HOSTNAME:
          row[index] = Const.getHostname();
          break;
        case TYPE_SYSTEM_INFO_IP_ADDRESS:
          try {
            row[index] = Const.getIPAddress();
          } catch ( Exception e ) {
            throw new KettleException( e );
          }
          break;
        case TYPE_SYSTEM_INFO_FILENAME:
          row[index] = getTransMeta().getFilename();
          break;
        case TYPE_SYSTEM_INFO_ARGUMENT_01:
        case TYPE_SYSTEM_INFO_ARGUMENT_02:
        case TYPE_SYSTEM_INFO_ARGUMENT_03:
        case TYPE_SYSTEM_INFO_ARGUMENT_04:
        case TYPE_SYSTEM_INFO_ARGUMENT_05:
        case TYPE_SYSTEM_INFO_ARGUMENT_06:
        case TYPE_SYSTEM_INFO_ARGUMENT_07:
        case TYPE_SYSTEM_INFO_ARGUMENT_08:
        case TYPE_SYSTEM_INFO_ARGUMENT_09:
        case TYPE_SYSTEM_INFO_ARGUMENT_10:
          argnr = meta.getFieldType()[i].ordinal() - SystemDataTypes.TYPE_SYSTEM_INFO_ARGUMENT_01.ordinal();
          if ( getTrans().getArguments() != null && argnr < getTrans().getArguments().length ) {
            row[index] = getTrans().getArguments()[argnr];
          } else {
            row[index] = null;
          }
          break;
        case TYPE_SYSTEM_INFO_KETTLE_VERSION:
          row[index] = BuildVersion.getInstance().getVersion();
          break;
        case TYPE_SYSTEM_INFO_KETTLE_BUILD_VERSION:
          row[index] = BuildVersion.getInstance().getVersion();
          break;
        case TYPE_SYSTEM_INFO_KETTLE_BUILD_DATE:
          row[index] = BuildVersion.getInstance().getBuildDateAsLocalDate();
          break;
        case TYPE_SYSTEM_INFO_CURRENT_PID:
          row[index] = new Long( Management.getPID() );
          break;
        case TYPE_SYSTEM_INFO_JVM_TOTAL_MEMORY:
          row[index] = Runtime.getRuntime().totalMemory();
          break;
        case TYPE_SYSTEM_INFO_JVM_FREE_MEMORY:
          row[index] = Runtime.getRuntime().freeMemory();
          break;
        case TYPE_SYSTEM_INFO_JVM_MAX_MEMORY:
          row[index] = Runtime.getRuntime().maxMemory();
          break;
        case TYPE_SYSTEM_INFO_JVM_AVAILABLE_MEMORY:
          Runtime rt = Runtime.getRuntime();
          row[index] = rt.freeMemory() + ( rt.maxMemory() - rt.totalMemory() );
          break;
        case TYPE_SYSTEM_INFO_AVAILABLE_PROCESSORS:
          row[index] = (long) Runtime.getRuntime().availableProcessors();
          break;
        case TYPE_SYSTEM_INFO_JVM_CPU_TIME:
          row[index] = Management.getJVMCpuTime() / 1000000;
          break;
        case TYPE_SYSTEM_INFO_TOTAL_PHYSICAL_MEMORY_SIZE:
          row[index] = Management.getTotalPhysicalMemorySize();
          break;
        case TYPE_SYSTEM_INFO_TOTAL_SWAP_SPACE_SIZE:
          row[index] = Management.getTotalSwapSpaceSize();
          break;
        case TYPE_SYSTEM_INFO_COMMITTED_VIRTUAL_MEMORY_SIZE:
          row[index] = Management.getCommittedVirtualMemorySize();
          break;
        case TYPE_SYSTEM_INFO_FREE_PHYSICAL_MEMORY_SIZE:
          row[index] = Management.getFreePhysicalMemorySize();
          break;
        case TYPE_SYSTEM_INFO_FREE_SWAP_SPACE_SIZE:
          row[index] = Management.getFreeSwapSpaceSize();
          break;

        case TYPE_SYSTEM_INFO_PREV_WEEK_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.WEEK_OF_YEAR, -1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_WEEK_END:
          cal = Calendar.getInstance();
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_WEEK_OPEN_END:
          cal = Calendar.getInstance( Locale.ROOT );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          cal.add( Calendar.DAY_OF_WEEK, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_WEEK_START_US:
          cal = Calendar.getInstance( Locale.US );
          cal.add( Calendar.WEEK_OF_YEAR, -1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_WEEK_END_US:
          cal = Calendar.getInstance( Locale.US );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_WEEK_START:
          cal = Calendar.getInstance();
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_WEEK_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.WEEK_OF_YEAR, 1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_WEEK_OPEN_END:
          cal = Calendar.getInstance( Locale.ROOT );
          cal.add( Calendar.WEEK_OF_YEAR, 1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          cal.add( Calendar.DAY_OF_WEEK, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_WEEK_START_US:
          cal = Calendar.getInstance( Locale.US );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_WEEK_END_US:
          cal = Calendar.getInstance( Locale.US );
          cal.add( Calendar.WEEK_OF_YEAR, 1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_WEEK_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.WEEK_OF_YEAR, 1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_WEEK_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.WEEK_OF_YEAR, 2 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_WEEK_OPEN_END:
          cal = Calendar.getInstance( Locale.ROOT );
          cal.add( Calendar.WEEK_OF_YEAR, 2 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          cal.add( Calendar.DAY_OF_WEEK, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_WEEK_START_US:
          cal = Calendar.getInstance( Locale.US );
          cal.add( Calendar.WEEK_OF_YEAR, 1 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_WEEK_END_US:
          cal = Calendar.getInstance( Locale.US );
          cal.add( Calendar.WEEK_OF_YEAR, 2 );
          cal.set( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, -1 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_QUARTER_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, -3 - ( cal.get( Calendar.MONTH ) % 3 ) );
          cal.set( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_QUARTER_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, -1 - ( cal.get( Calendar.MONTH ) % 3 ) );
          cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DATE ) );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_QUARTER_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, 0 - ( cal.get( Calendar.MONTH ) % 3 ) );
          cal.set( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_QUARTER_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, 2 - ( cal.get( Calendar.MONTH ) % 3 ) );
          cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DATE ) );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_QUARTER_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, 3 - ( cal.get( Calendar.MONTH ) % 3 ) );
          cal.set( Calendar.DAY_OF_MONTH, 1 );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_QUARTER_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.MONTH, 5 - ( cal.get( Calendar.MONTH ) % 3 ) );
          cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DATE ) );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_YEAR_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.YEAR, -1 );
          cal.set( Calendar.DAY_OF_YEAR, cal.getActualMinimum( Calendar.DATE ) );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREV_YEAR_END:
          cal = Calendar.getInstance();
          cal.set( Calendar.DAY_OF_YEAR, cal.getActualMinimum( Calendar.DATE ) );
          cal.add( Calendar.DAY_OF_YEAR, -1 );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_YEAR_START:
          cal = Calendar.getInstance();
          cal.set( Calendar.DAY_OF_YEAR, cal.getActualMinimum( Calendar.DATE ) );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_THIS_YEAR_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.YEAR, 1 );
          cal.set( Calendar.DAY_OF_YEAR, cal.getActualMinimum( Calendar.DATE ) );
          cal.add( Calendar.DAY_OF_YEAR, -1 );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_YEAR_START:
          cal = Calendar.getInstance();
          cal.add( Calendar.YEAR, 1 );
          cal.set( Calendar.DAY_OF_YEAR, cal.getActualMinimum( Calendar.DATE ) );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_NEXT_YEAR_END:
          cal = Calendar.getInstance();
          cal.add( Calendar.YEAR, 2 );
          cal.set( Calendar.DAY_OF_YEAR, cal.getActualMinimum( Calendar.DATE ) );
          cal.add( Calendar.DAY_OF_YEAR, -1 );
          cal.set( Calendar.HOUR_OF_DAY, 23 );
          cal.set( Calendar.MINUTE, 59 );
          cal.set( Calendar.SECOND, 59 );
          cal.set( Calendar.MILLISECOND, 999 );
          row[index] = cal.getTime();
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_RESULT:
          Result previousResult = getTrans().getPreviousResult();
          boolean result = false;
          if ( previousResult != null ) {
            result = previousResult.getResult();
          }
          row[index] = result;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_EXIT_STATUS:
          previousResult = getTrans().getPreviousResult();
          long value = 0;
          if ( previousResult != null ) {
            value = previousResult.getExitStatus();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_ENTRY_NR:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getEntryNr();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES:
          previousResult = getTrans().getPreviousResult();
          value = 0;

          if ( previousResult != null ) {
            value = previousResult.getResultFiles().size();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES_RETRIEVED:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrFilesRetrieved();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_DELETED:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesDeleted();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_INPUT:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesInput();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_OUTPUT:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesOutput();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_READ:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesRead();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_REJETED:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesRejected();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_UPDATED:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesUpdated();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_WRITTEN:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrLinesWritten();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ROWS:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getRows().size();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_IS_STOPPED:
          previousResult = getTrans().getPreviousResult();
          boolean stop = false;
          if ( previousResult != null ) {
            stop = previousResult.isStopped();
          }
          row[index] = stop;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ERRORS:
          previousResult = getTrans().getPreviousResult();
          value = 0;
          if ( previousResult != null ) {
            value = previousResult.getNrErrors();
          }
          row[index] = value;
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_LOG_TEXT:
          previousResult = getTrans().getPreviousResult();
          String errorReason = null;
          if ( previousResult != null ) {
            errorReason = previousResult.getLogText();
          }
          row[index] = errorReason;
          break;

        default:
          break;
      }
    }

    return row;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] row;
    if ( data.readsRows ) {
      row = getRow();
      if ( row == null ) {
        setOutputDone();
        return false;
      }

      if ( first ) {
        first = false;
        data.outputRowMeta = getInputRowMeta().clone();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      }

    } else {
      row = new Object[] {}; // empty row
      incrementLinesRead();

      if ( first ) {
        first = false;
        data.outputRowMeta = new RowMeta();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      }
    }

    RowMetaInterface imeta = getInputRowMeta();
    if ( imeta == null ) {
      imeta = new RowMeta();
      this.setInputRowMeta( imeta );
    }

    row = getSystemData( imeta, row );

    if ( log.isRowLevel() ) {
      logRowlevel( "System info returned: " + data.outputRowMeta.getString( row ) );
    }

    putRow( data.outputRowMeta, row );

    if ( !data.readsRows ) {
      // Just one row and then stop!
      setOutputDone();
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SystemDataMeta) smi;
    data = (SystemDataData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.readsRows = getStepMeta().getRemoteInputSteps().size() > 0;
      List<StepMeta> previous = getTransMeta().findPreviousSteps( getStepMeta() );
      if ( previous != null && previous.size() > 0 ) {
        data.readsRows = true;
      }

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
  }

}
