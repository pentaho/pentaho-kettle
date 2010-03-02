 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.systemdata;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/*
 * Created on 05-aug-2003
 *
 */

public class SystemDataMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SystemDataMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public final static int TYPE_SYSTEM_INFO_NONE             =  0;
    public final static int TYPE_SYSTEM_INFO_SYSTEM_DATE      =  1;
    public final static int TYPE_SYSTEM_INFO_SYSTEM_START     =  2;
    public final static int TYPE_SYSTEM_INFO_TRANS_DATE_FROM  =  3;
    public final static int TYPE_SYSTEM_INFO_TRANS_DATE_TO    =  4;
    public final static int TYPE_SYSTEM_INFO_JOB_DATE_FROM    =  5;
    public final static int TYPE_SYSTEM_INFO_JOB_DATE_TO      =  6;
    public final static int TYPE_SYSTEM_INFO_PREV_DAY_START   =  7;
    public final static int TYPE_SYSTEM_INFO_PREV_DAY_END     =  8;
    public final static int TYPE_SYSTEM_INFO_THIS_DAY_START   =  9;
    public final static int TYPE_SYSTEM_INFO_THIS_DAY_END     = 10;
    public final static int TYPE_SYSTEM_INFO_NEXT_DAY_START   = 11;
    public final static int TYPE_SYSTEM_INFO_NEXT_DAY_END     = 12;
    public final static int TYPE_SYSTEM_INFO_PREV_MONTH_START = 13;
    public final static int TYPE_SYSTEM_INFO_PREV_MONTH_END   = 14;
    public final static int TYPE_SYSTEM_INFO_THIS_MONTH_START = 15;
    public final static int TYPE_SYSTEM_INFO_THIS_MONTH_END   = 16;
    public final static int TYPE_SYSTEM_INFO_NEXT_MONTH_START = 17;
    public final static int TYPE_SYSTEM_INFO_NEXT_MONTH_END   = 18;
    public final static int TYPE_SYSTEM_INFO_COPYNR           = 19;
    public final static int TYPE_SYSTEM_INFO_TRANS_NAME       = 20;
    public final static int TYPE_SYSTEM_INFO_FILENAME         = 21;
    public final static int TYPE_SYSTEM_INFO_MODIFIED_USER    = 22;
    public final static int TYPE_SYSTEM_INFO_MODIFIED_DATE    = 23;
    public final static int TYPE_SYSTEM_INFO_TRANS_BATCH_ID   = 24;
    public final static int TYPE_SYSTEM_INFO_JOB_BATCH_ID     = 25;
    public final static int TYPE_SYSTEM_INFO_HOSTNAME         = 26;
    public final static int TYPE_SYSTEM_INFO_IP_ADDRESS       = 27;
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_01      = 28; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_02      = 29; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_03      = 30; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_04      = 31; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_05      = 32; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_06      = 33; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_07      = 34; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_08      = 35; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_09      = 36; 
    public final static int TYPE_SYSTEM_INFO_ARGUMENT_10      = 37; 
    
    public final static int TYPE_SYSTEM_INFO_KETTLE_VERSION        = 38; 
    public final static int TYPE_SYSTEM_INFO_KETTLE_BUILD_VERSION  = 39; 
    public final static int TYPE_SYSTEM_INFO_KETTLE_BUILD_DATE     = 40; 
    public final static int TYPE_SYSTEM_INFO_CURRENT_PID     = 41; 
    public final static int TYPE_SYSTEM_INFO_JVM_MAX_MEMORY        = 42;
    public final static int TYPE_SYSTEM_INFO_JVM_TOTAL_MEMORY      = 43; 
    public final static int TYPE_SYSTEM_INFO_JVM_FREE_MEMORY       = 44;
    public final static int TYPE_SYSTEM_INFO_JVM_AVAILABLE_MEMORY  = 45;
    public final static int TYPE_SYSTEM_INFO_AVAILABLE_PROCESSORS  = 46;
    public final static int TYPE_SYSTEM_INFO_JVM_CPU_TIME  = 47;
    public final static int TYPE_SYSTEM_INFO_TOTAL_PHYSICAL_MEMORY_SIZE  = 48;
    public final static int TYPE_SYSTEM_INFO_TOTAL_SWAP_SPACE_SIZE  = 49;
    public final static int TYPE_SYSTEM_INFO_COMMITTED_VIRTUAL_MEMORY_SIZE  = 50;
    public final static int TYPE_SYSTEM_INFO_FREE_PHYSICAL_MEMORY_SIZE  = 51;
    public final static int TYPE_SYSTEM_INFO_FREE_SWAP_SPACE_SIZE  = 52;
    public final static int TYPE_SYSTEM_INFO_PREV_WEEK_START   =  53;
    public final static int TYPE_SYSTEM_INFO_PREV_WEEK_END     =  54;
    public final static int TYPE_SYSTEM_INFO_PREV_WEEK_OPEN_END     =  55;
    public final static int TYPE_SYSTEM_INFO_PREV_WEEK_START_US   =  56;
    public final static int TYPE_SYSTEM_INFO_PREV_WEEK_END_US     =  57;
    public final static int TYPE_SYSTEM_INFO_THIS_WEEK_START   =  58;
    public final static int TYPE_SYSTEM_INFO_THIS_WEEK_END     =  59;
    public final static int TYPE_SYSTEM_INFO_THIS_WEEK_OPEN_END     =  60;
    public final static int TYPE_SYSTEM_INFO_THIS_WEEK_START_US   =  61;
    public final static int TYPE_SYSTEM_INFO_THIS_WEEK_END_US     =  62;
    public final static int TYPE_SYSTEM_INFO_NEXT_WEEK_START   =  63;
    public final static int TYPE_SYSTEM_INFO_NEXT_WEEK_END     =  64;
    public final static int TYPE_SYSTEM_INFO_NEXT_WEEK_OPEN_END     =  65;
    public final static int TYPE_SYSTEM_INFO_NEXT_WEEK_START_US   =  66;
    public final static int TYPE_SYSTEM_INFO_NEXT_WEEK_END_US     =  67;
    public final static int TYPE_SYSTEM_INFO_PREV_QUARTER_START   =  68;
    public final static int TYPE_SYSTEM_INFO_PREV_QUARTER_END     =  69;
    public final static int TYPE_SYSTEM_INFO_THIS_QUARTER_START   =  70;
    public final static int TYPE_SYSTEM_INFO_THIS_QUARTER_END     =  71;
    public final static int TYPE_SYSTEM_INFO_NEXT_QUARTER_START   =  72;
    public final static int TYPE_SYSTEM_INFO_NEXT_QUARTER_END     =  73;
    public final static int TYPE_SYSTEM_INFO_PREV_YEAR_START   =  74;
    public final static int TYPE_SYSTEM_INFO_PREV_YEAR_END     =  75;
    public final static int TYPE_SYSTEM_INFO_THIS_YEAR_START   =  76;
    public final static int TYPE_SYSTEM_INFO_THIS_YEAR_END     =  77;
    public final static int TYPE_SYSTEM_INFO_NEXT_YEAR_START   =  78;
    public final static int TYPE_SYSTEM_INFO_NEXT_YEAR_END     =  79;
    // Previous result
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_RESULT     =  80;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_EXIT_STATUS     =  81;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_ENTRY_NR     =  82;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ERRORS     =  83;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_INPUT     =  84;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_OUTPUT     =  85;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_READ     =  86;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_UPDATED     =  87;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_WRITTEN     =  88;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_DELETED     =  89;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_REJETED     =  90;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ROWS     =  91;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_IS_STOPPED     =  92;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES     =  93;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES_RETRIEVED     =  94;
    public final static int TYPE_SYSTEM_INFO_PREVIOUS_RESULT_LOG_TEXT  =  95;

    public static final SystemDataMetaFunction functions[] = new SystemDataMetaFunction[] {
            null,
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_SYSTEM_DATE      , "system date (variable)", BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.SystemDateVariable")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_SYSTEM_START     , "system date (fixed)", BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.SystemDateFixed")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_TRANS_DATE_FROM  , "start date range", BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.StartDateRange")),
        	new SystemDataMetaFunction(TYPE_SYSTEM_INFO_TRANS_DATE_TO    , "end date range", BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.EndDateRange")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JOB_DATE_FROM    , "job start date range",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JobStartDateRange")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JOB_DATE_TO      , "job end date range",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JobEndDateRange")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_DAY_START   , "yesterday start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.YesterdayStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_DAY_END     , "yesterday end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.YesterdayEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_DAY_START   , "today start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TodayStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_DAY_END     , "today end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TodayEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_DAY_START   , "tomorrow start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TomorrowStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_DAY_END     , "tomorrow end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TomorrowEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_MONTH_START , "last month start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastMonthStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_MONTH_END   , "last month end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastMonthEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_MONTH_START , "this month start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisMonthStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_MONTH_END   , "this month end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisMonthEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_MONTH_START , "next month start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextMonthStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_MONTH_END   , "next month end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextMonthEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_COPYNR           , "copy of step",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CopyOfStep")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_TRANS_NAME       , "transformation name",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TransformationName")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_FILENAME         , "transformation file name",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TransformationFileName")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_MODIFIED_USER    , "User modified",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.UserModified")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_MODIFIED_DATE    , "Date modified",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.DateModified")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_TRANS_BATCH_ID   , "batch ID",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.BatchID")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JOB_BATCH_ID     , "job batch ID",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JobBatchID")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_HOSTNAME         , "Hostname",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.Hostname")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_IP_ADDRESS       , "IP address",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.IPAddress")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_01      , "command line argument 1",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument1")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_02      , "command line argument 2",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument2")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_03      , "command line argument 3",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument3")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_04      , "command line argument 4",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument4")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_05      , "command line argument 5",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument5")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_06      , "command line argument 6",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument6")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_07      , "command line argument 7",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument7")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_08      , "command line argument 8",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument8")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_09      , "command line argument 9",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument9")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_ARGUMENT_10      , "command line argument 10",   BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommandLineArgument10")),

            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_KETTLE_VERSION       , "kettle version",       BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.KettleVersion")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_KETTLE_BUILD_VERSION , "kettle build version", BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.KettleBuildVersion")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_KETTLE_BUILD_DATE    , "kettle build date",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.KettleBuildDate")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_CURRENT_PID    , "Current PID",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CurrentPID")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JVM_MAX_MEMORY   , "jvm max memory",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JVMMaxMemory")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JVM_TOTAL_MEMORY    , "jvm total memory",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JVMTotalMemory")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JVM_FREE_MEMORY    , "jvm free memory",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JVMFreeMemory")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JVM_AVAILABLE_MEMORY   , "jvm available memory",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JVMAvailableMemory")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_AVAILABLE_PROCESSORS   , "available processors",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.AvailableProcessors")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_JVM_CPU_TIME   , "jvm cpu time",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.JVMCPUTime")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_TOTAL_PHYSICAL_MEMORY_SIZE   , "total physical memory size",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TotalPhysicalMemorySize")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_TOTAL_SWAP_SPACE_SIZE   , "total swap space size",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.TotalSwapSpaceSize")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_COMMITTED_VIRTUAL_MEMORY_SIZE   , "committed virtual memory size",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.CommittedVirtualMemorySize")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_FREE_PHYSICAL_MEMORY_SIZE   , "free physical memory size",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.FreePhysicalMemorySize")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_FREE_SWAP_SPACE_SIZE   , "free swap space size",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.FreeSwapSpaceSize")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_WEEK_START   , "last week start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastWeekStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_WEEK_END   , "last week end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastWeekEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_WEEK_OPEN_END   , "last week open end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastWeekOpenEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_WEEK_START_US   , "last week start us",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastWeekStartUS")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_WEEK_END_US   , "last week end us",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.LastWeekEndUS")),

            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_WEEK_START   , "this week start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisWeekStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_WEEK_END   , "this week end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisWeekEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_WEEK_OPEN_END   , "this week open end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisWeekOpenEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_WEEK_START_US   , "this week start us",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisWeekStartUS")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_WEEK_END_US   , "this week end us",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisWeekEndUS")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_WEEK_START   , "next week start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextWeekStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_WEEK_END   , "next week end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextWeekEnd")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_WEEK_OPEN_END   , "next week open end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextWeekOpenEnd")),
           
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_WEEK_START_US   , "next week start us",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextWeekStartUS")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_WEEK_END_US   , "next week end us",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextWeekEndUS")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_QUARTER_START   , "prev quarter start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PrevQuarterStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_QUARTER_END   , "prev quarter end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PrevQuarterEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_QUARTER_START   , "this quarter start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisQuarterStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_QUARTER_END   , "this quarter end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisQuarterEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_QUARTER_START   , "next quarter start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextQuarterStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_QUARTER_END   , "next quarter end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextQuarterEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_YEAR_START   , "prev year start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PrevYearStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREV_YEAR_END   , "prev year end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PrevYearEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_YEAR_START   , "this year start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisYearStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_THIS_YEAR_END   , "this year end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.ThisYearEnd")),
            
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_YEAR_START   , "next year start",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextYearStart")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_NEXT_YEAR_END   , "next year end",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.NextYearEnd")),
            
            // Previous result
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_RESULT   , "previous result result",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultResult")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_EXIT_STATUS   , "previous result exist status",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultExitStatus")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_ENTRY_NR  , "previous result entry nr",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultEntryNr")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ERRORS   , "previous result nr errors",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrErrors")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_INPUT   , "previous result nr lines input",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesInput")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_OUTPUT   , "previous result nr lines output",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesOutput")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_READ   , "previous result nr lines read",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesRead")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_UPDATED   , "previous result nr lines updated",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesUpdated")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_WRITTEN   , "previous result nr lines written",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesWritten")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_DELETED   , "previous result nr lines deleted",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesDeleted")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_REJETED   , "previous result nr lines rejeted",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesRejeted")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ROWS   , "previous result nr rows",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrLinesNrRows")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_IS_STOPPED   , "previous result is stopped",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultIsStopped")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES   , "previous result nr files",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrFiles")),
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES_RETRIEVED   , "previous result nr files retrieved",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultNrFilesRetrieved")),    
            new SystemDataMetaFunction(TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES_RETRIEVED   , "previous result nr files retrieved",    BaseMessages.getString(PKG, "SystemDataMeta.TypeDesc.PreviousResultLogText")), 
    };
    
	private String fieldName[];
	private int    fieldType[];
	
	public SystemDataMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @return Returns the fieldType.
     */
    public int[] getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int count)
	{
		fieldName = new String[count];
		fieldType = new int   [count];
	}

	public Object clone()
	{
		SystemDataMeta retval = (SystemDataMeta)super.clone();

		int count=fieldName.length;
		
		retval.allocate(count);
				
		for (int i=0;i<count;i++)
		{
			retval.fieldName[i] = fieldName[i];
			retval.fieldType[i] = fieldType[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int count= XMLHandler.countNodes(fields, "field");
			String type;
			
			allocate(count);
					
			for (int i=0;i<count;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i] = XMLHandler.getTagValue(fnode, "name");
				type         = XMLHandler.getTagValue(fnode, "type");
				fieldType[i] = getType(type);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}
	
	public static final int getType(String type)
	{
		for (int i=1;i<functions.length;i++)
		{
			if (functions[i].getCode().equalsIgnoreCase(type)) return i;
			if (functions[i].getDescription().equalsIgnoreCase(type)) return i;
		}
		return 0;
	}
	
	public static final String getTypeDesc(int t)
	{
        if (functions == null || functions.length == 0) return null;
		if (t<0 || t>=functions.length || functions[t] == null) return null;
		return functions[t].getDescription();
	}

	public void setDefault()
	{
		int count=0;
		
		allocate(count);

		for (int i=0;i<count;i++)
		{
			fieldName[i] = "field"+i;
			fieldType[i] = TYPE_SYSTEM_INFO_SYSTEM_DATE;
		}
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<fieldName.length;i++)
		{
			ValueMetaInterface v;

			switch(fieldType[i])
			{
			case TYPE_SYSTEM_INFO_SYSTEM_START:      // All date values...
			case TYPE_SYSTEM_INFO_SYSTEM_DATE:  
			case TYPE_SYSTEM_INFO_TRANS_DATE_FROM: 
			case TYPE_SYSTEM_INFO_TRANS_DATE_TO: 
            case TYPE_SYSTEM_INFO_JOB_DATE_FROM: 
            case TYPE_SYSTEM_INFO_JOB_DATE_TO: 
			case TYPE_SYSTEM_INFO_PREV_DAY_START: 
			case TYPE_SYSTEM_INFO_PREV_DAY_END: 
			case TYPE_SYSTEM_INFO_THIS_DAY_START: 
			case TYPE_SYSTEM_INFO_THIS_DAY_END: 
			case TYPE_SYSTEM_INFO_NEXT_DAY_START: 
			case TYPE_SYSTEM_INFO_NEXT_DAY_END: 
			case TYPE_SYSTEM_INFO_PREV_MONTH_START: 
			case TYPE_SYSTEM_INFO_PREV_MONTH_END: 
			case TYPE_SYSTEM_INFO_THIS_MONTH_START: 
			case TYPE_SYSTEM_INFO_THIS_MONTH_END: 
			case TYPE_SYSTEM_INFO_NEXT_MONTH_START: 
			case TYPE_SYSTEM_INFO_NEXT_MONTH_END: 
			case TYPE_SYSTEM_INFO_MODIFIED_DATE:
            case TYPE_SYSTEM_INFO_KETTLE_BUILD_DATE:
            case TYPE_SYSTEM_INFO_PREV_WEEK_START:
            case TYPE_SYSTEM_INFO_PREV_WEEK_END:
            case TYPE_SYSTEM_INFO_PREV_WEEK_OPEN_END:
            case TYPE_SYSTEM_INFO_PREV_WEEK_START_US:
            case TYPE_SYSTEM_INFO_PREV_WEEK_END_US:
            case TYPE_SYSTEM_INFO_THIS_WEEK_START:
            case TYPE_SYSTEM_INFO_THIS_WEEK_END:
            case TYPE_SYSTEM_INFO_THIS_WEEK_OPEN_END:
            case TYPE_SYSTEM_INFO_THIS_WEEK_START_US:
            case TYPE_SYSTEM_INFO_THIS_WEEK_END_US:
            case TYPE_SYSTEM_INFO_NEXT_WEEK_START:
            case TYPE_SYSTEM_INFO_NEXT_WEEK_END:
            case TYPE_SYSTEM_INFO_NEXT_WEEK_OPEN_END:
            case TYPE_SYSTEM_INFO_NEXT_WEEK_START_US:
            case TYPE_SYSTEM_INFO_NEXT_WEEK_END_US:
            case TYPE_SYSTEM_INFO_PREV_QUARTER_START:
            case TYPE_SYSTEM_INFO_PREV_QUARTER_END:
            case TYPE_SYSTEM_INFO_THIS_QUARTER_START:
            case TYPE_SYSTEM_INFO_THIS_QUARTER_END:
            case TYPE_SYSTEM_INFO_NEXT_QUARTER_START:
            case TYPE_SYSTEM_INFO_NEXT_QUARTER_END:
            case TYPE_SYSTEM_INFO_PREV_YEAR_START:
            case TYPE_SYSTEM_INFO_PREV_YEAR_END:
            case TYPE_SYSTEM_INFO_THIS_YEAR_START:
            case TYPE_SYSTEM_INFO_THIS_YEAR_END:
            case TYPE_SYSTEM_INFO_NEXT_YEAR_START:
            case TYPE_SYSTEM_INFO_NEXT_YEAR_END:
				v = new ValueMeta(fieldName[i], ValueMetaInterface.TYPE_DATE); 
				break;	
			case TYPE_SYSTEM_INFO_TRANS_NAME :
			case TYPE_SYSTEM_INFO_FILENAME   : 
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
			case TYPE_SYSTEM_INFO_MODIFIED_USER:
			case TYPE_SYSTEM_INFO_HOSTNAME:
			case TYPE_SYSTEM_INFO_IP_ADDRESS:
            case TYPE_SYSTEM_INFO_KETTLE_VERSION:
            case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_LOG_TEXT:	
				v=new ValueMeta(fieldName[i], ValueMetaInterface.TYPE_STRING);
				break;
			case TYPE_SYSTEM_INFO_COPYNR:
			case TYPE_SYSTEM_INFO_TRANS_BATCH_ID:
            case TYPE_SYSTEM_INFO_JOB_BATCH_ID:
            case TYPE_SYSTEM_INFO_KETTLE_BUILD_VERSION:
            case TYPE_SYSTEM_INFO_CURRENT_PID:
		    case TYPE_SYSTEM_INFO_JVM_TOTAL_MEMORY:
		    case TYPE_SYSTEM_INFO_JVM_FREE_MEMORY:
		    case TYPE_SYSTEM_INFO_JVM_MAX_MEMORY:
		    case TYPE_SYSTEM_INFO_JVM_AVAILABLE_MEMORY:
		    case TYPE_SYSTEM_INFO_AVAILABLE_PROCESSORS:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_EXIT_STATUS:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_ENTRY_NR:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ERRORS:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES_RETRIEVED:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_DELETED:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_INPUT:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_OUTPUT:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_READ:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_REJETED:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_UPDATED:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_WRITTEN:
				v=new ValueMeta(fieldName[i], ValueMetaInterface.TYPE_INTEGER);
				v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
				break;
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_RESULT:
		    case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_IS_STOPPED:
				v=new ValueMeta(fieldName[i], ValueMetaInterface.TYPE_BOOLEAN);
				break;
			default: 
				v = new ValueMeta(fieldName[i], ValueMetaInterface.TYPE_NONE);
				break;
			}
			v.setOrigin(name);
			row.addValueMeta(v);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append("    <fields>"+Const.CR);
		
		for (int i=0;i<fieldName.length;i++)
		{
			retval.append("      <field>"+Const.CR);
			retval.append("        "+XMLHandler.addTagValue("name", fieldName[i]));
			retval.append("        "+XMLHandler.addTagValue("type", functions[fieldType[i]]!=null ? functions[fieldType[i]].getCode() : "") );
			retval.append("        </field>"+Const.CR);
		}
		retval.append("      </fields>"+Const.CR);

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i] =          rep.getStepAttributeString(id_step, i, "field_name");
				fieldType[i] = getType( rep.getStepAttributeString(id_step, i, "field_type"));
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      functions[fieldType[i]]!=null ? functions[fieldType[i]].getCode() : "");
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}

	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		// See if we have input streams leading to this step!
		int nrRemarks = remarks.size();
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldType[i]<=TYPE_SYSTEM_INFO_NONE)
			{
				CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SystemDataMeta.CheckResult.FieldHasNoType", fieldName[i]), stepMeta);
				remarks.add(cr);
			}
		}
		if (remarks.size()==nrRemarks)
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SystemDataMeta.CheckResult.AllTypesSpecified"), stepMeta);
			remarks.add(cr);
		}
	}
    
    /**
     * Default a step doesn't use any arguments.
     * Implement this to notify the GUI that a window has to be displayed BEFORE launching a transformation.
     * You can also use this to specify certain Environment variable values.
     * 
     * @return A row of argument values. (name and optionally a default value)
     *         Put up to 10 values in the row for the possible 10 arguments.
     *         The name of the value is "1" through "10" for the 10 possible arguments.
     */
    public Map<String, String> getUsedArguments()
    {
    	Map<String, String> stepArgs = new HashMap<String, String>();
    	DecimalFormat df = new DecimalFormat("00");
    	
        for (int argNr=0;argNr<10;argNr++)
        {
            boolean found = false;
            for (int i=0;i<fieldName.length;i++)
            {
                if (fieldType[i]==TYPE_SYSTEM_INFO_ARGUMENT_01+argNr) found=true;
            }
            if (found)
            {
            	stepArgs.put(df.format(argNr+1), "");
            }
        }
        
        return stepArgs;
    }

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SystemData(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SystemDataData();
	}

}
