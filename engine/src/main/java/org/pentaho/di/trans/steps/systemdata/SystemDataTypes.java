/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
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

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepMetaInjectionEnumEntry;

public enum SystemDataTypes implements StepMetaInjectionEnumEntry {
    NONE( "", "" ),
    SYSTEM_DATE( "system date (variable)", "SystemDateVariable" ),
    SYSTEM_START( "system date (fixed)", "SystemDateFixed" ),
    TRANS_DATE_FROM( "start date range", "StartDateRange" ),
    TRANS_DATE_TO( "end date range", "EndDateRange" ),
    JOB_DATE_FROM( "job start date range", "JobStartDateRange" ),
    JOB_DATE_TO( "job end date range", "JobEndDateRange" ),
    PREV_DAY_START( "yesterday start", "YesterdayStart" ),
    PREV_DAY_END( "yesterday end", "YesterdayEnd" ),
    THIS_DAY_START( "today start", "TodayStart" ),
    THIS_DAY_END( "today end", "TodayEnd" ),
    NEXT_DAY_START( "tomorrow start", "TomorrowStart" ),
    NEXT_DAY_END( "tomorrow end", "TomorrowEnd" ),
    PREV_MONTH_START( "last month start", "LastMonthStart" ),
    PREV_MONTH_END( "last month end", "LastMonthEnd" ),
    THIS_MONTH_START( "this month start", "ThisMonthStart" ),
    THIS_MONTH_END( "this month end", "ThisMonthEnd" ),
    NEXT_MONTH_START( "next month start", "NextMonthStart" ),
    NEXT_MONTH_END( "next month end", "NextMonthEnd" ),
    COPYNR( "copy of step", "CopyOfStep" ),
    TRANS_NAME( "transformation name", "TransformationName" ),
    FILENAME( "transformation file name", "TransformationFileName" ),
    MODIFIED_USER( "User modified", "UserModified" ),
    MODIFIED_DATE( "Date modified", "DateModified" ),
    TRANS_BATCH_ID( "batch ID", "BatchID" ),
    JOB_BATCH_ID( "job batch ID", "JobBatchID" ),
    HOSTNAME( "Hostname", "HostnameNetworkSetup" ),
    HOSTNAME_REAL( "Hostname real", "Hostname" ),
    IP_ADDRESS( "IP address", "IPAddress" ),
    ARGUMENT_01( "command line argument 1", "CommandLineArgument1" ),
    ARGUMENT_02( "command line argument 2", "CommandLineArgument2" ),
    ARGUMENT_03( "command line argument 3", "CommandLineArgument3" ),
    ARGUMENT_04( "command line argument 4", "CommandLineArgument4" ),
    ARGUMENT_05( "command line argument 5", "CommandLineArgument5" ),
    ARGUMENT_06( "command line argument 6", "CommandLineArgument6" ),
    ARGUMENT_07( "command line argument 7", "CommandLineArgument7" ),
    ARGUMENT_08( "command line argument 8", "CommandLineArgument8" ),
    ARGUMENT_09( "command line argument 9", "CommandLineArgument9" ),
    ARGUMENT_10( "command line argument 10", "CommandLineArgument10" ),

    KETTLE_VERSION( "kettle version", "KettleVersion" ),
    KETTLE_BUILD_VERSION( "kettle build version", "KettleBuildVersion" ),
    KETTLE_BUILD_DATE( "kettle build date", "KettleBuildDate" ),
    CURRENT_PID( "Current PID", "CurrentPID" ),

    JVM_MAX_MEMORY( "jvm max memory", "JVMMaxMemory" ),
    JVM_TOTAL_MEMORY( "jvm total memory", "JVMTotalMemory" ),
    JVM_FREE_MEMORY( "jvm free memory", "JVMFreeMemory" ),
    JVM_AVAILABLE_MEMORY( "jvm available memory", "JVMAvailableMemory" ),
    AVAILABLE_PROCESSORS( "available processors", "AvailableProcessors" ),
    JVM_CPU_TIME( "jvm cpu time", "JVMCPUTime" ),
    TOTAL_PHYSICAL_MEMORY_SIZE( "total physical memory size", "TotalPhysicalMemorySize" ),
    TOTAL_SWAP_SPACE_SIZE( "total swap space size", "TotalSwapSpaceSize" ),
    COMMITTED_VIRTUAL_MEMORY_SIZE( "committed virtual memory size", "CommittedVirtualMemorySize" ),
    FREE_PHYSICAL_MEMORY_SIZE( "free physical memory size", "FreePhysicalMemorySize" ),
    FREE_SWAP_SPACE_SIZE( "free swap space size", "FreeSwapSpaceSize" ),

    PREV_WEEK_START( "last week start", "LastWeekStart" ),
    PREV_WEEK_END( "last week end", "LastWeekEnd" ),
    PREV_WEEK_OPEN_END( "last week open end", "LastWeekOpenEnd" ),

    PREV_WEEK_START_US( "last week start us", "LastWeekStartUS" ),
    PREV_WEEK_END_US( "last week end us", "LastWeekEndUS" ),

    THIS_WEEK_START( "this week start", "ThisWeekStart" ),
    THIS_WEEK_END( "this week end", "ThisWeekEnd" ),
    THIS_WEEK_OPEN_END( "this week open end", "ThisWeekOpenEnd" ),

    THIS_WEEK_START_US( "this week start us", "ThisWeekStartUS" ),
    THIS_WEEK_END_US( "this week end us", "ThisWeekEndUS" ),

    NEXT_WEEK_START( "next week start", "NextWeekStart" ),
    NEXT_WEEK_END( "next week end", "NextWeekEnd" ),
    NEXT_WEEK_OPEN_END( "next week open end", "NextWeekOpenEnd" ),

    NEXT_WEEK_START_US( "next week start us", "NextWeekStartUS" ),
    NEXT_WEEK_END_US( "next week end us", "NextWeekEndUS" ),

    PREV_QUARTER_START( "prev quarter start", "PrevQuarterStart" ),
    PREV_QUARTER_END( "prev quarter end", "PrevQuarterEnd" ),

    THIS_QUARTER_START( "this quarter start", "ThisQuarterStart" ),
    THIS_QUARTER_END( "this quarter end", "ThisQuarterEnd" ),

    NEXT_QUARTER_START( "next quarter start", "NextQuarterStart" ),
    NEXT_QUARTER_END( "next quarter end", "NextQuarterEnd" ),

    PREV_YEAR_START( "prev year start", "PrevYearStart" ),
    PREV_YEAR_END( "prev year end", "PrevYearEnd" ),

    THIS_YEAR_START( "this year start", "ThisYearStart" ),
    THIS_YEAR_END( "this year end", "ThisYearEnd" ),
    NEXT_YEAR_START( "next year start", "NextYearStart" ),
    NEXT_YEAR_END( "next year end", "NextYearEnd" ),

    PREVIOUS_RESULT_RESULT( "previous result result", "PreviousResultResult" ),
    PREVIOUS_RESULT_EXIT_STATUS( "previous result exist status", "PreviousResultExitStatus" ),
    PREVIOUS_RESULT_ENTRY_NR( "previous result entry nr", "PreviousResultEntryNr" ),
    PREVIOUS_RESULT_NR_ERRORS( "previous result nr errors", "PreviousResultNrErrors" ),
    PREVIOUS_RESULT_NR_LINES_INPUT( "previous result nr lines input", "PreviousResultNrLinesInput" ),
    PREVIOUS_RESULT_NR_LINES_OUTPUT( "previous result nr lines output",
            "PreviousResultNrLinesOutput" ),
    PREVIOUS_RESULT_NR_LINES_READ( "previous result nr lines read",
            "PreviousResultNrLinesRead" ),
    PREVIOUS_RESULT_NR_LINES_UPDATED( "previous result nr lines updated",
            "PreviousResultNrLinesUpdated" ),
    PREVIOUS_RESULT_NR_LINES_WRITTEN( "previous result nr lines written",
            "PreviousResultNrLinesWritten" ),
    PREVIOUS_RESULT_NR_LINES_DELETED( "previous result nr lines deleted",
            "PreviousResultNrLinesDeleted" ),
    PREVIOUS_RESULT_NR_LINES_REJECTED( "previous result nr lines rejected",
            "PreviousResultNrLinesRejected" ),
    PREVIOUS_RESULT_NR_ROWS( "previous result nr rows", "PreviousResultNrLinesNrRows" ),
    PREVIOUS_RESULT_IS_STOPPED( "previous result is stopped", "PreviousResultIsStopped" ),
    PREVIOUS_RESULT_NR_FILES( "previous result nr files", "PreviousResultNrFiles" ),
    PREVIOUS_RESULT_NR_FILES_RETRIEVED( "previous result nr files retrieved",
            "PreviousResultNrFilesRetrieved" ),
    PREVIOUS_RESULT_LOG_TEXT( "previous result log text", "PreviousResultLogText" );

  private String code;
  private String description;

  private static Class<?> PKG = SystemDataMeta.class; // for i18n purposes, needed by Translator2!!

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public static SystemDataTypes getTypeFromString( String typeStr ) {
    for ( SystemDataTypes type : SystemDataTypes.values() ) {
      if ( typeStr.equalsIgnoreCase( type.code )
          || typeStr.equalsIgnoreCase( type.description )
          || typeStr.equalsIgnoreCase( "previous result nr lines rejeted" ) /* attempt to purge this typo */ ) {
        return type;
      }
    }

    return NONE;
  }

  private static String getDescription( String name ) {
    if ( PKG == null ) {
      PKG = SystemDataMeta.class;
    }
    return BaseMessages.getString( PKG, "SystemDataMeta.TypeDesc." + name );
  }

  SystemDataTypes( String code, String descriptionName ) {
    this.code = code;
    this.description = getDescription( descriptionName );
  }
}
