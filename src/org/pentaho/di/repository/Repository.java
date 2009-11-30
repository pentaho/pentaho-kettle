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

package org.pentaho.di.repository;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.Counters;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;


/**
 * 
 * This class handles interactions with a Kettle repository.
 * 
 * @author Matt
 * Created on 31-mrt-2004
 *
 */
public class Repository
{
	public static final String TABLE_R_VERSION             = "R_VERSION";
	public static final String FIELD_VERSION_ID_VERSION    = "ID_VERSION";
	public static final String FIELD_VERSION_MAJOR_VERSION = "MAJOR_VERSION";
	public static final String FIELD_VERSION_MINOR_VERSION = "MINOR_VERSION";
	public static final String FIELD_VERSION_IS_UPGRADE    = "IS_UPGRADE";
	public static final String FIELD_VERSION_UPGRADE_DATE  = "UPGRADE_DATE";
	
	public static final String TABLE_R_REPOSITORY_LOG                 = "R_REPOSITORY_LOG";
	public static final String FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG = "ID_REPOSITORY_LOG";
	public static final String FIELD_REPOSITORY_LOG_REP_VERSION       = "REP_VERSION";
	public static final String FIELD_REPOSITORY_LOG_LOG_DATE          = "LOG_DATE";
	public static final String FIELD_REPOSITORY_LOG_LOG_USER          = "LOG_USER";
	public static final String FIELD_REPOSITORY_LOG_OPERATION_DESC    = "OPERATION_DESC";

	public static final String TABLE_R_DATABASE_TYPE          = "R_DATABASE_TYPE";
	public static final String FIELD_DATABASE_TYPE_ID_DATABASE_TYPE = "ID_DATABASE_TYPE";
	public static final String FIELD_DATABASE_TYPE_CODE             = "CODE";
	public static final String FIELD_DATABASE_TYPE_DESCRIPTION      = "DESCRIPTION";

	public static final String TABLE_R_DATABASE_CONTYPE       = "R_DATABASE_CONTYPE";
	public static final String FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE = "ID_DATABASE_CONTYPE";
	public static final String FIELD_DATABASE_CONTYPE_CODE                = "CODE";
	public static final String FIELD_DATABASE_CONTYPE_DESCRIPTION         = "DESCRIPTION";
	
	public static final String TABLE_R_DATABASE               = "R_DATABASE";
	public static final String FIELD_DATABASE_ID_DATABASE         = "ID_DATABASE";
	public static final String FIELD_DATABASE_NAME                = "NAME";
	public static final String FIELD_DATABASE_ID_DATABASE_TYPE    = "ID_DATABASE_TYPE";
	public static final String FIELD_DATABASE_ID_DATABASE_CONTYPE = "ID_DATABASE_CONTYPE";
	public static final String FIELD_DATABASE_HOST_NAME           = "HOST_NAME";
	public static final String FIELD_DATABASE_DATABASE_NAME       = "DATABASE_NAME";
	public static final String FIELD_DATABASE_PORT                = "PORT";
	public static final String FIELD_DATABASE_USERNAME            = "USERNAME";
	public static final String FIELD_DATABASE_DATA_TBS            = "DATA_TBS";
	public static final String FIELD_DATABASE_PASSWORD            = "PASSWORD";
	public static final String FIELD_DATABASE_SERVERNAME          = "SERVERNAME";
	public static final String FIELD_DATABASE_INDEX_TBS           = "INDEX_TBS";
	
	public static final String TABLE_R_DATABASE_ATTRIBUTE     = "R_DATABASE_ATTRIBUTE";
	public static final String FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE = "ID_DATABASE_ATTRIBUTE";
	public static final String FIELD_DATABASE_ATTRIBUTE_ID_DATABASE           = "ID_DATABASE";
	public static final String FIELD_DATABASE_ATTRIBUTE_CODE                  = "CODE";
	public static final String FIELD_DATABASE_ATTRIBUTE_VALUE_STR             = "VALUE_STR";

	public static final String TABLE_R_NOTE                   = "R_NOTE";
	public static final String FIELD_NOTE_ID_NOTE             = "ID_NOTE";
	public static final String FIELD_NOTE_VALUE_STR           = "VALUE_STR";
	public static final String FIELD_NOTE_GUI_LOCATION_X      = "GUI_LOCATION_X";
	public static final String FIELD_NOTE_GUI_LOCATION_Y      = "GUI_LOCATION_Y";
	public static final String FIELD_NOTE_GUI_LOCATION_WIDTH  = "GUI_LOCATION_WIDTH";
	public static final String FIELD_NOTE_GUI_LOCATION_HEIGHT = "GUI_LOCATION_HEIGHT";

	public static final String TABLE_R_TRANSFORMATION         = "R_TRANSFORMATION";
	public static final String FIELD_TRANSFORMATION_ID_TRANSFORMATION    = "ID_TRANSFORMATION";
	public static final String FIELD_TRANSFORMATION_ID_DIRECTORY         = "ID_DIRECTORY";
	public static final String FIELD_TRANSFORMATION_NAME                 = "NAME";
	public static final String FIELD_TRANSFORMATION_DESCRIPTION          = "DESCRIPTION";
	public static final String FIELD_TRANSFORMATION_EXTENDED_DESCRIPTION = "EXTENDED_DESCRIPTION";
	public static final String FIELD_TRANSFORMATION_TRANS_VERSION        = "TRANS_VERSION";
	public static final String FIELD_TRANSFORMATION_TRANS_STATUS         = "TRANS_STATUS";
	public static final String FIELD_TRANSFORMATION_ID_STEP_READ         = "ID_STEP_READ";
	public static final String FIELD_TRANSFORMATION_ID_STEP_WRITE        = "ID_STEP_WRITE";
	public static final String FIELD_TRANSFORMATION_ID_STEP_INPUT        = "ID_STEP_INPUT";
	public static final String FIELD_TRANSFORMATION_ID_STEP_OUTPUT       = "ID_STEP_OUTPUT";
	public static final String FIELD_TRANSFORMATION_ID_STEP_UPDATE       = "ID_STEP_UPDATE";
	public static final String FIELD_TRANSFORMATION_ID_DATABASE_LOG      = "ID_DATABASE_LOG";
	public static final String FIELD_TRANSFORMATION_TABLE_NAME_LOG       = "TABLE_NAME_LOG";
	public static final String FIELD_TRANSFORMATION_USE_BATCHID          = "USE_BATCHID";
	public static final String FIELD_TRANSFORMATION_USE_LOGFIELD         = "USE_LOGFIELD";
	public static final String FIELD_TRANSFORMATION_ID_DATABASE_MAXDATE  = "ID_DATABASE_MAXDATE";
	public static final String FIELD_TRANSFORMATION_TABLE_NAME_MAXDATE   = "TABLE_NAME_MAXDATE";
	public static final String FIELD_TRANSFORMATION_FIELD_NAME_MAXDATE   = "FIELD_NAME_MAXDATE";
	public static final String FIELD_TRANSFORMATION_OFFSET_MAXDATE       = "OFFSET_MAXDATE";
	public static final String FIELD_TRANSFORMATION_DIFF_MAXDATE         = "DIFF_MAXDATE";
	public static final String FIELD_TRANSFORMATION_CREATED_USER         = "CREATED_USER";
	public static final String FIELD_TRANSFORMATION_CREATED_DATE         = "CREATED_DATE";
	public static final String FIELD_TRANSFORMATION_MODIFIED_USER        = "MODIFIED_USER";
	public static final String FIELD_TRANSFORMATION_MODIFIED_DATE        = "MODIFIED_DATE";
	public static final String FIELD_TRANSFORMATION_SIZE_ROWSET          = "SIZE_ROWSET";

	public static final String TABLE_R_DIRECTORY              = "R_DIRECTORY";
    public static final String FIELD_DIRECTORY_ID_DIRECTORY        = "ID_DIRECTORY";
	public static final String FIELD_DIRECTORY_ID_DIRECTORY_PARENT = "ID_DIRECTORY_PARENT";
	public static final String FIELD_DIRECTORY_DIRECTORY_NAME      = "DIRECTORY_NAME";
	
	public static final String TABLE_R_TRANS_ATTRIBUTE        = "R_TRANS_ATTRIBUTE";
	public static final String FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE = "ID_TRANS_ATTRIBUTE";
	public static final String FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_ATTRIBUTE_NR = "NR";
	public static final String FIELD_TRANS_ATTRIBUTE_CODE = "CODE";
	public static final String FIELD_TRANS_ATTRIBUTE_VALUE_NUM = "VALUE_NUM";
	public static final String FIELD_TRANS_ATTRIBUTE_VALUE_STR = "VALUE_STR";
	
	public static final String TABLE_R_JOB_ATTRIBUTE        = "R_JOB_ATTRIBUTE";
	public static final String FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE = "ID_JOB_ATTRIBUTE";
	public static final String FIELD_JOB_ATTRIBUTE_ID_JOB = "ID_JOB";
	public static final String FIELD_JOB_ATTRIBUTE_NR = "NR";
	public static final String FIELD_JOB_ATTRIBUTE_CODE = "CODE";
	public static final String FIELD_JOB_ATTRIBUTE_VALUE_NUM = "VALUE_NUM";
	public static final String FIELD_JOB_ATTRIBUTE_VALUE_STR = "VALUE_STR";
	
	public static final String TABLE_R_DEPENDENCY             = "R_DEPENDENCY";
	public static final String FIELD_DEPENDENCY_ID_DEPENDENCY = "ID_DEPENDENCY";
	public static final String FIELD_DEPENDENCY_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_DEPENDENCY_ID_DATABASE = "ID_DATABASE";
	public static final String FIELD_DEPENDENCY_TABLE_NAME = "TABLE_NAME";
	public static final String FIELD_DEPENDENCY_FIELD_NAME = "FIELD_NAME";

	public static final String TABLE_R_TRANS_STEP_CONDITION   = "R_TRANS_STEP_CONDITION";
	public static final String FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_STEP_CONDITION_ID_STEP = "ID_STEP";
	public static final String FIELD_TRANS_STEP_CONDITION_ID_CONDITION = "ID_CONDITION";
	
	public static final String TABLE_R_CONDITION              = "R_CONDITION";
	public static final String FIELD_CONDITION_ID_CONDITION = "ID_CONDITION";
	public static final String FIELD_CONDITION_ID_CONDITION_PARENT = "ID_CONDITION_PARENT";
	public static final String FIELD_CONDITION_NEGATED = "NEGATED";
	public static final String FIELD_CONDITION_OPERATOR = "OPERATOR";
	public static final String FIELD_CONDITION_LEFT_NAME = "LEFT_NAME";
	public static final String FIELD_CONDITION_CONDITION_FUNCTION = "CONDITION_FUNCTION";
	public static final String FIELD_CONDITION_RIGHT_NAME = "RIGHT_NAME";
	public static final String FIELD_CONDITION_ID_VALUE_RIGHT = "ID_VALUE_RIGHT";

	public static final String TABLE_R_VALUE                  = "R_VALUE";
	public static final String FIELD_VALUE_ID_VALUE = "ID_VALUE";
	public static final String FIELD_VALUE_NAME = "NAME";
	public static final String FIELD_VALUE_VALUE_TYPE = "VALUE_TYPE";
	public static final String FIELD_VALUE_VALUE_STR = "VALUE_STR";
	public static final String FIELD_VALUE_IS_NULL = "IS_NULL";

	public static final String TABLE_R_TRANS_HOP              = "R_TRANS_HOP";
	public static final String FIELD_TRANS_HOP_ID_TRANS_HOP = "ID_TRANS_HOP";
	public static final String FIELD_TRANS_HOP_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_HOP_ID_STEP_FROM = "ID_STEP_FROM";
	public static final String FIELD_TRANS_HOP_ID_STEP_TO = "ID_STEP_TO";
	public static final String FIELD_TRANS_HOP_ENABLED = "ENABLED";

	public static final String TABLE_R_STEP_TYPE              = "R_STEP_TYPE";
	public static final String FIELD_STEP_TYPE_ID_STEP_TYPE = "ID_STEP_TYPE";
	public static final String FIELD_STEP_TYPE_CODE = "CODE";
	public static final String FIELD_STEP_TYPE_DESCRIPTION = "DESCRIPTION";
	public static final String FIELD_STEP_TYPE_HELPTEXT = "HELPTEXT";
	
	public static final String TABLE_R_STEP                   = "R_STEP";
	public static final String FIELD_STEP_ID_STEP = "ID_STEP";
	public static final String FIELD_STEP_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_STEP_NAME = "NAME";
	public static final String FIELD_STEP_DESCRIPTION = "DESCRIPTION";
	public static final String FIELD_STEP_ID_STEP_TYPE = "ID_STEP_TYPE";
	public static final String FIELD_STEP_DISTRIBUTE = "DISTRIBUTE";
	public static final String FIELD_STEP_COPIES = "COPIES";
	public static final String FIELD_STEP_GUI_LOCATION_X = "GUI_LOCATION_X";
	public static final String FIELD_STEP_GUI_LOCATION_Y = "GUI_LOCATION_Y";
	public static final String FIELD_STEP_GUI_DRAW = "GUI_DRAW";

	public static final String TABLE_R_STEP_ATTRIBUTE         = "R_STEP_ATTRIBUTE";
	public static final String FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE = "ID_STEP_ATTRIBUTE";
	public static final String FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_STEP_ATTRIBUTE_ID_STEP = "ID_STEP";
	public static final String FIELD_STEP_ATTRIBUTE_CODE = "CODE";
	public static final String FIELD_STEP_ATTRIBUTE_NR = "NR";
	public static final String FIELD_STEP_ATTRIBUTE_VALUE_NUM = "VALUE_NUM";
	public static final String FIELD_STEP_ATTRIBUTE_VALUE_STR = "VALUE_STR";

	public static final String TABLE_R_TRANS_NOTE             = "R_TRANS_NOTE";
	public static final String FIELD_TRANS_NOTE_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_NOTE_ID_NOTE = "ID_NOTE";

	public static final String TABLE_R_JOB                    = "R_JOB";
	public static final String FIELD_JOB_ID_JOB = "ID_JOB";
	public static final String FIELD_JOB_ID_DIRECTORY = "ID_DIRECTORY";
	public static final String FIELD_JOB_NAME = "NAME";
	public static final String FIELD_JOB_DESCRIPTION = "DESCRIPTION";
	public static final String FIELD_JOB_EXTENDED_DESCRIPTION = "EXTENDED_DESCRIPTION";
	public static final String FIELD_JOB_JOB_VERSION = "JOB_VERSION";
	public static final String FIELD_JOB_JOB_STATUS = "JOB_STATUS";
	public static final String FIELD_JOB_ID_DATABASE_LOG = "ID_DATABASE_LOG";
	public static final String FIELD_JOB_TABLE_NAME_LOG = "TABLE_NAME_LOG";
	public static final String FIELD_JOB_CREATED_USER = "CREATED_USER";
	public static final String FIELD_JOB_CREATED_DATE = "CREATED_DATE";
	public static final String FIELD_JOB_MODIFIED_USER = "MODIFIED_USER";
	public static final String FIELD_JOB_MODIFIED_DATE = "MODIFIED_DATE";
	public static final String FIELD_JOB_USE_BATCH_ID = "USE_BATCH_ID";
	public static final String FIELD_JOB_PASS_BATCH_ID = "PASS_BATCH_ID";
	public static final String FIELD_JOB_USE_LOGFIELD = "USE_LOGFIELD";
	public static final String FIELD_JOB_SHARED_FILE = "SHARED_FILE";

	public static final String TABLE_R_LOGLEVEL               = "R_LOGLEVEL";
	public static final String FIELD_LOGLEVEL_ID_LOGLEVEL = "ID_LOGLEVEL";
	public static final String FIELD_LOGLEVEL_CODE = "CODE";
	public static final String FIELD_LOGLEVEL_DESCRIPTION = "DESCRIPTION";
	
	public static final String TABLE_R_LOG                    = "R_LOG";
	public static final String FIELD_LOG_ID_LOG = "ID_LOG";
	public static final String FIELD_LOG_NAME = "NAME";
	public static final String FIELD_LOG_ID_LOGLEVEL = "ID_LOGLEVEL";
	public static final String FIELD_LOG_LOGTYPE = "LOGTYPE";
	public static final String FIELD_LOG_FILENAME = "FILENAME";
	public static final String FIELD_LOG_FILEEXTENTION = "FILEEXTENTION";
	public static final String FIELD_LOG_ADD_DATE = "ADD_DATE";
	public static final String FIELD_LOG_ADD_TIME = "ADD_TIME";
	public static final String FIELD_LOG_ID_DATABASE_LOG = "ID_DATABASE_LOG";
	public static final String FIELD_LOG_TABLE_NAME_LOG = "TABLE_NAME_LOG";

	public static final String TABLE_R_JOBENTRY               = "R_JOBENTRY";
	public static final String FIELD_JOBENTRY_ID_JOBENTRY = "ID_JOBENTRY";
	public static final String FIELD_JOBENTRY_ID_JOB = "ID_JOB";
	public static final String FIELD_JOBENTRY_ID_JOBENTRY_TYPE = "ID_JOBENTRY_TYPE";
	public static final String FIELD_JOBENTRY_NAME = "NAME";
	public static final String FIELD_JOBENTRY_DESCRIPTION = "DESCRIPTION";

	public static final String TABLE_R_JOBENTRY_COPY          = "R_JOBENTRY_COPY";
	public static final String FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY = "ID_JOBENTRY_COPY";
	public static final String FIELD_JOBENTRY_COPY_ID_JOBENTRY = "ID_JOBENTRY";
	public static final String FIELD_JOBENTRY_COPY_ID_JOB = "ID_JOB";
	public static final String FIELD_JOBENTRY_COPY_ID_JOBENTRY_TYPE = "ID_JOBENTRY_TYPE";
	public static final String FIELD_JOBENTRY_COPY_NR = "NR";
	public static final String FIELD_JOBENTRY_COPY_GUI_LOCATION_X = "GUI_LOCATION_X";
	public static final String FIELD_JOBENTRY_COPY_GUI_LOCATION_Y = "GUI_LOCATION_Y";
	public static final String FIELD_JOBENTRY_COPY_GUI_DRAW = "GUI_DRAW";
	public static final String FIELD_JOBENTRY_COPY_PARALLEL = "PARALLEL";

	public static final String TABLE_R_JOBENTRY_TYPE          = "R_JOBENTRY_TYPE";
	public static final String FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE = "ID_JOBENTRY_TYPE";
	public static final String FIELD_JOBENTRY_TYPE_CODE = "CODE";
	public static final String FIELD_JOBENTRY_TYPE_DESCRIPTION = "DESCRIPTION";

	public static final String TABLE_R_JOBENTRY_ATTRIBUTE     = "R_JOBENTRY_ATTRIBUTE";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE = "ID_JOBENTRY_ATTRIBUTE";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_ID_JOB = "ID_JOB";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY = "ID_JOBENTRY";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_NR = "NR";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_CODE = "CODE";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM = "VALUE_NUM";
	public static final String FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR = "VALUE_STR";

	public static final String TABLE_R_JOB_HOP                = "R_JOB_HOP";
	public static final String FIELD_JOB_HOP_ID_JOB_HOP = "ID_JOB_HOP";
	public static final String FIELD_JOB_HOP_ID_JOB = "ID_JOB";
	public static final String FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM = "ID_JOBENTRY_COPY_FROM";
	public static final String FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO = "ID_JOBENTRY_COPY_TO";
	public static final String FIELD_JOB_HOP_ENABLED = "ENABLED";
	public static final String FIELD_JOB_HOP_EVALUATION = "EVALUATION";
	public static final String FIELD_JOB_HOP_UNCONDITIONAL = "UNCONDITIONAL";

	public static final String TABLE_R_JOB_NOTE               = "R_JOB_NOTE";
	public static final String FIELD_JOB_NOTE_ID_JOB = "ID_JOB";
	public static final String FIELD_JOB_NOTE_ID_NOTE = "ID_NOTE";
	
	public static final String TABLE_R_PROFILE                = "R_PROFILE";
	public static final String FIELD_PROFILE_ID_PROFILE = "ID_PROFILE";
	public static final String FIELD_PROFILE_NAME = "NAME";
	public static final String FIELD_PROFILE_DESCRIPTION = "DESCRIPTION";

	public static final String TABLE_R_USER                   = "R_USER";
	public static final String FIELD_USER_ID_USER = "ID_USER";
	public static final String FIELD_USER_ID_PROFILE = "ID_PROFILE";
	public static final String FIELD_USER_LOGIN = "LOGIN";
	public static final String FIELD_USER_PASSWORD = "PASSWORD";
	public static final String FIELD_USER_NAME = "NAME";
	public static final String FIELD_USER_DESCRIPTION = "DESCRIPTION";
	public static final String FIELD_USER_ENABLED = "ENABLED";

	public static final String TABLE_R_PERMISSION             = "R_PERMISSION";
	public static final String FIELD_PERMISSION_ID_PERMISSION = "ID_PERMISSION";
	public static final String FIELD_PERMISSION_CODE = "CODE";
	public static final String FIELD_PERMISSION_DESCRIPTION = "DESCRIPTION";

	public static final String TABLE_R_PROFILE_PERMISSION     = "R_PROFILE_PERMISSION";
	public static final String FIELD_PROFILE_PERMISSION_ID_PROFILE = "ID_PROFILE";
	public static final String FIELD_PROFILE_PERMISSION_ID_PERMISSION = "ID_PERMISSION";

	public static final String TABLE_R_STEP_DATABASE          = "R_STEP_DATABASE";
	public static final String FIELD_STEP_DATABASE_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_STEP_DATABASE_ID_STEP = "ID_STEP";
	public static final String FIELD_STEP_DATABASE_ID_DATABASE = "ID_DATABASE";

	public static final String TABLE_R_PARTITION_SCHEMA       = "R_PARTITION_SCHEMA";
	public static final String FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA = "ID_PARTITION_SCHEMA";
	public static final String FIELD_PARTITION_SCHEMA_NAME = "NAME";
	public static final String FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION = "DYNAMIC_DEFINITION";
	public static final String FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE = "PARTITIONS_PER_SLAVE";
	
	public static final String TABLE_R_PARTITION              = "R_PARTITION";
	public static final String FIELD_PARTITION_ID_PARTITION = "ID_PARTITION";
	public static final String FIELD_PARTITION_ID_PARTITION_SCHEMA = "ID_PARTITION_SCHEMA";
	public static final String FIELD_PARTITION_PARTITION_ID = "PARTITION_ID";
	
	public static final String TABLE_R_TRANS_PARTITION_SCHEMA = "R_TRANS_PARTITION_SCHEMA";
	public static final String FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA = "ID_TRANS_PARTITION_SCHEMA";
	public static final String FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA = "ID_PARTITION_SCHEMA";

	public static final String TABLE_R_CLUSTER                = "R_CLUSTER";
	public static final String FIELD_CLUSTER_ID_CLUSTER = "ID_CLUSTER";
	public static final String FIELD_CLUSTER_NAME = "NAME";
	public static final String FIELD_CLUSTER_BASE_PORT = "BASE_PORT";
	public static final String FIELD_CLUSTER_SOCKETS_BUFFER_SIZE = "SOCKETS_BUFFER_SIZE";
	public static final String FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL = "SOCKETS_FLUSH_INTERVAL";
	public static final String FIELD_CLUSTER_SOCKETS_COMPRESSED = "SOCKETS_COMPRESSED";
	public static final String FIELD_CLUSTER_DYNAMIC = "DYNAMIC_CLUSTER";

	public static final String TABLE_R_SLAVE                  = "R_SLAVE";
	public static final String FIELD_SLAVE_ID_SLAVE = "ID_SLAVE";
	public static final String FIELD_SLAVE_NAME = "NAME";
	public static final String FIELD_SLAVE_HOST_NAME = "HOST_NAME";
	public static final String FIELD_SLAVE_PORT = "PORT";
	public static final String FIELD_SLAVE_USERNAME = "USERNAME";
	public static final String FIELD_SLAVE_PASSWORD = "PASSWORD";
	public static final String FIELD_SLAVE_PROXY_HOST_NAME = "PROXY_HOST_NAME";
	public static final String FIELD_SLAVE_PROXY_PORT = "PROXY_PORT";
	public static final String FIELD_SLAVE_NON_PROXY_HOSTS = "NON_PROXY_HOSTS";
	public static final String FIELD_SLAVE_MASTER = "MASTER";

	public static final String TABLE_R_CLUSTER_SLAVE          = "R_CLUSTER_SLAVE";
	public static final String FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE = "ID_CLUSTER_SLAVE";
	public static final String FIELD_CLUSTER_SLAVE_ID_CLUSTER = "ID_CLUSTER";
	public static final String FIELD_CLUSTER_SLAVE_ID_SLAVE = "ID_SLAVE";

	public static final String TABLE_R_TRANS_CLUSTER          = "R_TRANS_CLUSTER";
	public static final String FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER = "ID_TRANS_CLUSTER";
	public static final String FIELD_TRANS_CLUSTER_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_CLUSTER_ID_CLUSTER = "ID_CLUSTER";

	public static final String TABLE_R_TRANS_SLAVE            = "R_TRANS_SLAVE";
	public static final String FIELD_TRANS_SLAVE_ID_TRANS_SLAVE = "ID_TRANS_SLAVE";
	public static final String FIELD_TRANS_SLAVE_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_SLAVE_ID_SLAVE = "ID_SLAVE";

    public static final String repositoryTableNames[] = new String[] 
         { 
    		  TABLE_R_CLUSTER
    		, TABLE_R_CLUSTER_SLAVE
    		, TABLE_R_CONDITION
    		, TABLE_R_DATABASE
    		, TABLE_R_DATABASE_ATTRIBUTE
    		, TABLE_R_DATABASE_CONTYPE
    		, TABLE_R_DATABASE_TYPE
    		, TABLE_R_DEPENDENCY
    		, TABLE_R_DIRECTORY
    		, TABLE_R_JOB
    		, TABLE_R_JOBENTRY
    		, TABLE_R_JOBENTRY_ATTRIBUTE
    		, TABLE_R_JOBENTRY_COPY
    		, TABLE_R_JOBENTRY_TYPE
    		, TABLE_R_JOB_HOP
    		, TABLE_R_JOB_NOTE
    		, TABLE_R_LOG
    		, TABLE_R_LOGLEVEL
    		, TABLE_R_NOTE
    		, TABLE_R_PARTITION
    		, TABLE_R_PARTITION_SCHEMA
    		, TABLE_R_PERMISSION
    		, TABLE_R_PROFILE
    		, TABLE_R_PROFILE_PERMISSION
    		, TABLE_R_REPOSITORY_LOG
    		, TABLE_R_SLAVE
    		, TABLE_R_STEP
    		, TABLE_R_STEP_ATTRIBUTE
    		, TABLE_R_STEP_DATABASE
    		, TABLE_R_STEP_TYPE
    		, TABLE_R_TRANSFORMATION
    		, TABLE_R_TRANS_ATTRIBUTE
    		, TABLE_R_JOB_ATTRIBUTE
    		, TABLE_R_TRANS_CLUSTER
    		, TABLE_R_TRANS_HOP
    		, TABLE_R_TRANS_NOTE
    		, TABLE_R_TRANS_PARTITION_SCHEMA
    		, TABLE_R_TRANS_SLAVE
    		, TABLE_R_TRANS_STEP_CONDITION
    		, TABLE_R_USER
    		, TABLE_R_VALUE
    		, TABLE_R_VERSION
         };

    private final static int[] KEY_POSITIONS = new int[] {0, 1, 2};

    public static final int REQUIRED_MAJOR_VERSION = 3;
    public static final int REQUIRED_MINOR_VERSION = 2;
    
	private RepositoryMeta		repinfo;
	public  UserInfo			userinfo;
	private RepositoryDirectory	directoryTree;
	private RepositoryDirectory importBaseDirectory;
	private Database			database;

	public  LogWriter			log;

	private String				locksource;

	private PreparedStatement	psStepAttributesLookup;
	private PreparedStatement	psStepAttributesInsert;
    private PreparedStatement   psTransAttributesLookup;
    private PreparedStatement   psTransAttributesInsert;
    
    private PreparedStatement   psJobAttributesLookup;
    private PreparedStatement   psJobAttributesInsert;
	
	private List<Object[]>           stepAttributesBuffer;
	private RowMetaInterface         stepAttributesRowMeta;
	
	private PreparedStatement	pstmt_entry_attributes;

	private int					majorVersion;
	private int					minorVersion;
    private DatabaseMeta        databaseMeta;
    
	private boolean             useBatchProcessing;

    /** The maximum length of a text field in a Kettle repository : 2.000.000 is enough for everyone ;-) */ 
    public static final int REP_STRING_LENGTH      = 2000000;
    public static final int REP_STRING_CODE_LENGTH =     255;
    
    public static final String TRANS_ATTRIBUTE_ID_STEP_REJECTED = "ID_STEP_REJECTED";
	public static final String TRANS_ATTRIBUTE_UNIQUE_CONNECTIONS = "UNIQUE_CONNECTIONS";
	public static final String TRANS_ATTRIBUTE_FEEDBACK_SHOWN = "FEEDBACK_SHOWN";
	public static final String TRANS_ATTRIBUTE_FEEDBACK_SIZE = "FEEDBACK_SIZE";
	public static final String TRANS_ATTRIBUTE_USING_THREAD_PRIORITIES = "USING_THREAD_PRIORITIES";
	public static final String TRANS_ATTRIBUTE_SHARED_FILE = "SHARED_FILE";
	public static final String TRANS_ATTRIBUTE_CAPTURE_STEP_PERFORMANCE = "CAPTURE_STEP_PERFORMANCE";
	public static final String TRANS_ATTRIBUTE_STEP_PERFORMANCE_CAPTURING_DELAY = "STEP_PERFORMANCE_CAPTURING_DELAY";
	public static final String TRANS_ATTRIBUTE_STEP_PERFORMANCE_LOG_TABLE = "STEP_PERFORMANCE_LOG_TABLE";
	public static final String TRANS_ATTRIBUTE_LOG_SIZE_LIMIT = "LOG_SIZE_LIMIT";
	public static final String TRANS_ATTRIBUTE_PARAM_KEY         = "PARAM_KEY";
	public static final String TRANS_ATTRIBUTE_PARAM_DEFAULT     = "PARAM_DEFAULT";
	public static final String TRANS_ATTRIBUTE_PARAM_DESCRIPTION = "PARAM_DESC";
	
	public static final String JOB_ATTRIBUTE_PARAM_KEY           = "PARAM_KEY";
	public static final String JOB_ATTRIBUTE_PARAM_DEFAULT       = "PARAM_DEFAULT";
	public static final String JOB_ATTRIBUTE_PARAM_DESCRIPTION   = "PARAM_DESC";	
	public static final String JOB_ATTRIBUTE_LOG_SIZE_LIMIT      = "LOG_SIZE_LIMIT";
		
    private static Repository currentRepository;
    
    private RepositoryCreationHelper creationHelper;

	public Repository(LogWriter log, RepositoryMeta repinfo, UserInfo userinfo)
	{
		this.repinfo = repinfo;
		this.log = log;
		this.userinfo = userinfo;
		
		useBatchProcessing = true; // defaults to true;
		
		database = new Database(repinfo.getConnection());
		databaseMeta = database.getDatabaseMeta();
            
		psStepAttributesLookup = null;
		psStepAttributesInsert = null;
        psTransAttributesLookup = null;
		pstmt_entry_attributes = null;

		this.majorVersion = REQUIRED_MAJOR_VERSION;
		this.minorVersion = REQUIRED_MINOR_VERSION;

		directoryTree = null;
		
		creationHelper = new RepositoryCreationHelper(this);
	}

	public RepositoryMeta getRepositoryInfo()
	{
		return repinfo;
	}

	public UserInfo getUserInfo()
	{
		return userinfo;
	}

	public String getName()
	{
		if (repinfo == null)
			return null;
		return repinfo.getName();
	}

	/**
	 * Return the major repository version.
	 * @return the major repository version.
	 */
	public int getMajorVersion()
	{
		return majorVersion;
	}

	/**
	 * Return the minor repository version.
	 * @return the minor repository version.
	 */
	public int getMinorVersion()
	{
		return minorVersion;
	}

	/**
	 * Get the repository version.
	 * @return The repository version as major version + "." + minor version
	 */
	public String getVersion()
	{
		return majorVersion + "." + minorVersion;
	}
    
    /**
     * Get the required repository version for this version of Kettle.
     * @return the required repository version for this version of Kettle.
     */
    public static final String getRequiredVersion()
    {
        return REQUIRED_MAJOR_VERSION + "." + REQUIRED_MINOR_VERSION;
    }

    /**
     * @return The source specified at connect() time.
     */
    public String getLocksource()
    {
        return locksource;
    }
    
	/**
	 * Connect to the repository 
	 * @param locksource
	 * @return true if the connection went well, false if we couldn't connect.
	 */
	public synchronized boolean connect(String locksource) throws KettleException
	{
		return connect(false, true, locksource, false);
	}

    public synchronized boolean connect(boolean no_lookup, boolean readDirectory, String locksource) throws KettleException
    {
        return connect(no_lookup, readDirectory, locksource, false);
    }

	public synchronized boolean connect(boolean no_lookup, boolean readDirectory, String locksource, boolean ignoreVersion) throws KettleException
	{
		if (repinfo.isLocked())
		{
			log.logError(toString(), "Repository is locked by class " + locksource);
			return false;
		}
		boolean retval = true;
		try
		{
			database.initializeVariablesFrom(null); 
			database.connect();
            if (!ignoreVersion) verifyVersion();
			setAutoCommit(false);
			repinfo.setLock(true);
			this.locksource = locksource;
			if (!no_lookup)
			{
				try
				{
					setLookupStepAttribute();
                    setLookupTransAttribute();
					setLookupJobEntryAttribute();
					setLookupJobAttribute(); 
				}
				catch (KettleException dbe)
				{
					log.logError(toString(), "Error setting lookup prep.statements: " + dbe.getMessage());
				}
			}

			// Load the directory tree.
            if (readDirectory)
            {
    			try
    			{
    				refreshRepositoryDirectoryTree();
    			}
    			catch (KettleException e)
    			{
    				log.logError(toString(), e.toString());
    			}
            }
            else
            {
                directoryTree = new RepositoryDirectory();
            }
            
            // OK, the repository is available
            currentRepository = this;
		}
		catch (KettleException e)
		{
			retval = false;
			log.logError(toString(), "Error connecting to the repository!" + e.getMessage());
            throw new KettleException(e);
		}

		return retval;
	}
    
    private void verifyVersion() throws KettleException
    {
        RowMetaAndData lastUpgrade = null;
    	String versionTable = databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_VERSION);
        try
        {
            lastUpgrade = database.getOneRow("SELECT "+quote(FIELD_VERSION_MAJOR_VERSION)+", "+quote(FIELD_VERSION_MINOR_VERSION)+", "+quote(FIELD_VERSION_UPGRADE_DATE)+" FROM "+versionTable+" ORDER BY "+quote(FIELD_VERSION_UPGRADE_DATE)+" DESC");
        }
        catch(Exception e)
        {
        	try
        	{
	        	// See if the repository exists at all.  For this we verify table R_USER.
	        	//
            	String userTable = databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER);
	        	database.getOneRow("SELECT * FROM "+userTable);
	        	
	        	// Still here?  That means we have a repository...
	        	//
	            // If we can't retrieve the last available upgrade date:
	            // this means the R_VERSION table doesn't exist.
	            // This table was introduced in version 2.3.0
	            //
	            if(log.isBasic()) 
	            {
	            	log.logBasic(toString(), Messages.getString("Repository.Error.GettingInfoVersionTable",versionTable));
	            	log.logBasic(toString(), Messages.getString("Repository.Error.NewTable"));
	            	log.logBasic(toString(), "Stack trace: "+Const.getStackTracker(e));
	            }
	            majorVersion = 2;
	            minorVersion = 2;
	
	            lastUpgrade = null;
        	}
        	catch(Exception ex)
        	{
        		throw new KettleException(Messages.getString("Repository.NoRepositoryExists.Messages"));
        	}
        }

        if (lastUpgrade != null)
        {
            majorVersion = (int)lastUpgrade.getInteger(FIELD_VERSION_MAJOR_VERSION, -1);
            minorVersion = (int)lastUpgrade.getInteger(FIELD_VERSION_MINOR_VERSION, -1);
        }
            
        if (majorVersion < REQUIRED_MAJOR_VERSION || ( majorVersion==REQUIRED_MAJOR_VERSION && minorVersion<REQUIRED_MINOR_VERSION))
        {
            throw new KettleException(Messages.getString("Repository.UpgradeRequired.Message", getVersion(), getRequiredVersion()));
        }
        
        if (majorVersion==3 && minorVersion==0) {
        	// The exception: someone upgraded the repository to version 3.0.0
        	// In that version, one column got named incorrectly.
        	// Another upgrade to 3.0.1 or later will fix that.
        	// However, since we don't have point versions in here, we'll have to look at the column in question...
        	//
        	String tableName = databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_PARTITION_SCHEMA);
        	String errorColumn = "TRANSFORMATION";
    		RowMetaInterface tableFields = database.getTableFields(tableName);
    		if (tableFields.indexOfValue(errorColumn)>=0)
    		{
    			throw new KettleException(Messages.getString("Repository.FixFor300Required.Message"));
    		}        	
        }
    }

    public synchronized void refreshRepositoryDirectoryTree() throws KettleException
    {
        try
        {
            directoryTree = new RepositoryDirectory(this);
        }
        catch (KettleException e)
        {
            directoryTree = new RepositoryDirectory();
            throw new KettleException("Unable to read the directory tree from the repository!", e);
        }

    }

	public synchronized void disconnect()
	{
		try
		{
            currentRepository=null;
            
			closeStepAttributeLookupPreparedStatement();
            closeTransAttributeLookupPreparedStatement();
            closeLookupJobEntryAttribute();
            
            if (!database.isAutoCommit()) commit();
			repinfo.setLock(false);			
		}
		catch (KettleException dbe)
		{
			log.logError(toString(), "Error disconnecting from database : " + dbe.getMessage());
		}
		finally
		{
			database.disconnect();
		}
	}

	public synchronized void setAutoCommit(boolean autocommit)
	{
		if (!autocommit)
			database.setCommit(99999999);
		else
			database.setCommit(0);
	}

	public synchronized void commit() throws KettleException
	{
		try
		{
			if (!database.isAutoCommit()) database.commit();
			
			// Also, clear the counters, reducing the risk of collisions!
			//
			Counters.getInstance().clear();
		}
		catch (KettleException dbe)
		{
			throw new KettleException("Unable to commit repository connection", dbe);
		}
	}

	public synchronized void rollback()
	{
		try
		{
			database.rollback();
			
			// Also, clear the counters, reducing the risk of collisions!
			//
			Counters.getInstance().clear();
		}
		catch (KettleException dbe)
		{
			log.logError(toString(), "Error rolling back repository.");
		}
	}
	
	/**
     * @return Returns the stepAttributesBuffer.
     */
    public List<Object[]> getStepAttributesBuffer()
    {
        return stepAttributesBuffer;
    }
    
    /**
     * @param stepAttributesBuffer The stepAttributesBuffer to set.
     */
    public void setStepAttributesBuffer(List<Object[]> stepAttributesBuffer)
    {
        this.stepAttributesBuffer = stepAttributesBuffer;
    }
	
	public synchronized void fillStepAttributesBuffer(long id_transformation) throws KettleException
	{
	    String sql = "SELECT "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+", "+quote(FIELD_STEP_ATTRIBUTE_CODE)+", "+quote(FIELD_STEP_ATTRIBUTE_NR)+", "+quote(FIELD_STEP_ATTRIBUTE_VALUE_NUM)+", "+quote(FIELD_STEP_ATTRIBUTE_VALUE_STR)+" "+
	                 "FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE) +" "+
	                 "WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION)+" = "+id_transformation+" "+
	                 "ORDER BY "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+", "+quote(FIELD_STEP_ATTRIBUTE_CODE)+", "+quote(FIELD_STEP_ATTRIBUTE_NR)
	                 ;
	    
	    stepAttributesBuffer = database.getRows(sql, -1);
	    stepAttributesRowMeta = database.getReturnRowMeta();
        
	    // must use java-based sort to ensure compatibility with binary search
	    // database ordering may or may not be case-insensitive
	    //
        Collections.sort(stepAttributesBuffer, new StepAttributeComparator());  // in case db sort does not match our sort
	}
	
	private synchronized RowMetaAndData searchStepAttributeInBuffer(long id_step, String code, long nr) throws KettleValueException
	{
	    int index = searchStepAttributeIndexInBuffer(id_step, code, nr);
	    if (index<0) return null;
	    
	    // Get the row
	    //
        Object[] r = stepAttributesBuffer.get(index);
        
	    // and remove it from the list...
        // stepAttributesBuffer.remove(index);
	    
	    return new RowMetaAndData(stepAttributesRowMeta, r);
	}
	
	
    private class StepAttributeComparator implements Comparator<Object[]> {

    	public int compare(Object[] r1, Object[] r2) 
    	{
    		try {
    			return stepAttributesRowMeta.compare(r1, r2, KEY_POSITIONS);
    		} catch (KettleValueException e) {
    			return 0; // conversion errors
    		}
    	}
    }
	
	private synchronized int searchStepAttributeIndexInBuffer(long id_step, String code, long nr) throws KettleValueException
	{
        Object[] key = new Object[] {
        		new Long(id_step), // ID_STEP
        		code, // CODE
        		new Long(nr), // NR
        };
        

        int index = Collections.binarySearch(stepAttributesBuffer, key, new StepAttributeComparator());

        if (index>=stepAttributesBuffer.size() || index<0) return -1;
        
        // 
        // Check this...  If it is not in there, we didn't find it!
        // stepAttributesRowMeta.compare returns 0 when there are conversion issues
        // so the binarySearch could have 'found' a match when there really isn't one
        //
        Object[] look = stepAttributesBuffer.get(index);
        
        if (stepAttributesRowMeta.compare(look, key, KEY_POSITIONS)==0)
        {
            return index;
        }
        
        return -1;
	}

	private synchronized int searchNrStepAttributes(long id_step, String code) throws KettleValueException
	{
	    // Search the index of the first step attribute with the specified code...
		//
	    int idx = searchStepAttributeIndexInBuffer(id_step, code, 0L);
	    if (idx<0) return 0;
	    
	    int nr = 1;
	    int offset = 1;
        
        if (idx+offset>=stepAttributesBuffer.size())
        {
        	// Only 1, the last of the attributes buffer.
        	//
            return 1; 
        }
        Object[] look = (Object[])stepAttributesBuffer.get(idx+offset);
        RowMetaInterface rowMeta = stepAttributesRowMeta;
        
	    long lookID = rowMeta.getInteger(look, 0);
	    String lookCode = rowMeta.getString(look, 1);
	    
	    while (lookID==id_step && code.equalsIgnoreCase( lookCode ) )
	    {
	    	// Find the maximum
	    	//
	        nr = rowMeta.getInteger(look, 2).intValue() + 1; 
	        offset++;
            if (idx+offset<stepAttributesBuffer.size())
            {
                look = (Object[])stepAttributesBuffer.get(idx+offset);
                
                lookID = rowMeta.getInteger(look, 0);
                lookCode = rowMeta.getString(look, 1);
            }
            else
            {
                return nr;
            }
	    }
	    return nr;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// LOOKUP ID
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized long getJobID(String name, long id_directory) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB), quote(FIELD_JOB_ID_JOB), quote(FIELD_JOB_NAME), name, quote(FIELD_JOB_ID_DIRECTORY), id_directory);
	}

	public synchronized long getTransformationID(String name, long id_directory) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION), quote(FIELD_TRANSFORMATION_NAME), name, quote(FIELD_TRANSFORMATION_ID_DIRECTORY), id_directory);
	}

	public synchronized long getNoteID(String note) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_NOTE), quote(FIELD_NOTE_ID_NOTE), quote(FIELD_NOTE_VALUE_STR), note);
	}

	public synchronized long getDatabaseID(String name) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE), quote(FIELD_DATABASE_ID_DATABASE), quote(FIELD_DATABASE_NAME), name);
	}
    
    public synchronized long getPartitionSchemaID(String name) throws KettleException
    {
        return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_NAME), name);
    }

    public synchronized long getSlaveID(String name) throws KettleException
    {
        return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_SLAVE), quote(FIELD_SLAVE_ID_SLAVE), quote(FIELD_SLAVE_NAME), name);
    }

    public synchronized long getClusterID(String name) throws KettleException
    {
        return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER), quote(FIELD_CLUSTER_ID_CLUSTER), quote(FIELD_CLUSTER_NAME), name);
    }

	public synchronized long getDatabaseTypeID(String code) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_CODE), code);
	}

	public synchronized long getDatabaseConTypeID(String code) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_CODE), code);
	}

	public synchronized long getStepTypeID(String code) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE), quote(FIELD_STEP_TYPE_CODE), code);
	}

	public synchronized long getJobEntryID(String name, long id_job) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY), quote(FIELD_JOBENTRY_ID_JOBENTRY), quote(FIELD_JOBENTRY_NAME), name, quote(FIELD_JOBENTRY_ID_JOB), id_job);
	}

	public synchronized long getJobEntryTypeID(String code) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_TYPE_CODE), code);
	}

	public synchronized long getStepID(String name, long id_transformation) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP), quote(FIELD_STEP_ID_STEP), quote(FIELD_STEP_NAME), name, quote(FIELD_STEP_ID_TRANSFORMATION), id_transformation);
	}

	public synchronized long getUserID(String login) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER), quote(FIELD_USER_ID_USER), quote(FIELD_USER_LOGIN), login);
	}

	public synchronized long getProfileID(String profilename) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE), quote(FIELD_PROFILE_ID_PROFILE), quote(FIELD_PROFILE_NAME), profilename);
	}

	public synchronized long getPermissionID(String code) throws KettleException
	{
		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PERMISSION), quote(FIELD_PERMISSION_ID_PERMISSION), quote(FIELD_PERMISSION_CODE), code);
	}

	public synchronized long getTransHopID(long id_transformation, long id_step_from, long id_step_to) throws KettleException
	{
		String lookupkey[] = new String[] { quote(FIELD_TRANS_HOP_ID_TRANSFORMATION), quote(FIELD_TRANS_HOP_ID_STEP_FROM), quote(FIELD_TRANS_HOP_ID_STEP_TO), };
		long key[] = new long[] { id_transformation, id_step_from, id_step_to };

		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_HOP), quote(FIELD_TRANS_HOP_ID_TRANS_HOP), lookupkey, key);
	}

	public synchronized long getJobHopID(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to)
			throws KettleException
	{
		String lookupkey[] = new String[] { quote(FIELD_JOB_HOP_ID_JOB), quote(FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM), quote(FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO), };
		long key[] = new long[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_HOP), quote(FIELD_JOB_HOP_ID_JOB_HOP), lookupkey, key);
	}

	public synchronized long getDependencyID(long id_transformation, long id_database, String tablename) throws KettleException
	{
		String lookupkey[] = new String[] { quote(FIELD_DEPENDENCY_ID_TRANSFORMATION), quote(FIELD_DEPENDENCY_ID_DATABASE), };
		long key[] = new long[] { id_transformation, id_database };

		return getIDWithValue(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DEPENDENCY), quote(FIELD_DEPENDENCY_ID_DEPENDENCY), quote(FIELD_DEPENDENCY_TABLE_NAME), tablename, lookupkey, key);
	}

	public synchronized long getRootDirectoryID() throws KettleException
	{
		RowMetaAndData result = database.getOneRow("SELECT "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = 0");
		if (result != null && result.isNumeric(0))
			return result.getInteger(0, -1);
		return -1;
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0);
		}

		return retval;
	}

	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory+" ORDER BY "+quote(FIELD_DIRECTORY_DIRECTORY_NAME));
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta("value", ValueMetaInterface.TYPE_STRING), value);
		RowMetaAndData result = database.getOneRow("SELECT " + idfield + " FROM " + tablename+ " WHERE " + lookupfield + " = ?", par.getRowMeta(), par.getData());

		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey, long key) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta("value", ValueMetaInterface.TYPE_STRING), value);
        par.addValue(new ValueMeta("key", ValueMetaInterface.TYPE_INTEGER), new Long(key));
		RowMetaAndData result = database.getOneRow("SELECT " + idfield + " FROM " + tablename + " WHERE " + lookupfield + " = ? AND "
									+ lookupkey + " = ?", par.getRowMeta(), par.getData());

		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupkey[], long key[]) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		String sql = "SELECT " + idfield + " FROM " + tablename + " ";

		for (int i = 0; i < lookupkey.length; i++)
		{
			if (i == 0)
				sql += "WHERE ";
			else
				sql += "AND   ";
			par.addValue(new ValueMeta(lookupkey[i], ValueMetaInterface.TYPE_INTEGER), new Long(key[i]));
			sql += lookupkey[i] + " = ? ";
		}
		RowMetaAndData result = database.getOneRow(sql, par.getRowMeta(), par.getData());
		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey[], long key[]) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta(lookupfield, ValueMetaInterface.TYPE_STRING), value);
        
		String sql = "SELECT " + idfield + " FROM " + tablename + " WHERE " + lookupfield + " = ? ";

		for (int i = 0; i < lookupkey.length; i++)
		{
			par.addValue( new ValueMeta(lookupkey[i], ValueMetaInterface.TYPE_STRING), new Long(key[i]) );
			sql += "AND " + lookupkey[i] + " = ? ";
		}

		RowMetaAndData result = database.getOneRow(sql, par.getRowMeta(), par.getData());
		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	public synchronized String getDatabaseTypeCode(long id_database_type) throws KettleException
	{
		return getStringWithID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE), id_database_type, quote(FIELD_DATABASE_TYPE_CODE));
	}

	public synchronized String getDatabaseConTypeCode(long id_database_contype) throws KettleException
	{
		return getStringWithID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE), id_database_contype, quote(FIELD_DATABASE_CONTYPE_CODE));
	}

	public synchronized String getStepTypeCode(long id_database_type) throws KettleException
	{
		return getStringWithID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE), id_database_type, quote(FIELD_STEP_TYPE_CODE));
	}

	private synchronized String getStringWithID(String tablename, String keyfield, long id, String fieldname) throws KettleException
	{
		String sql = "SELECT " + fieldname + " FROM " + tablename + " WHERE " + keyfield + " = ?";
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(keyfield, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		RowMetaAndData result = database.getOneRow(sql, par.getRowMeta(), par.getData());
		if (result != null && result.getData()!=null)
		{
			return result.getString(0, null);
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// DIRECTORIES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void moveTransformation(String transname, long id_directory_from, long id_directory_to) throws KettleException
	{
        String nameField = quote(FIELD_TRANSFORMATION_NAME);
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION)+" SET "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = ? WHERE "+nameField+" = ? AND "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = ?";

		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_to));
		par.addValue(new ValueMeta(FIELD_TRANSFORMATION_NAME,  ValueMetaInterface.TYPE_STRING), transname);
		par.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_from));

		database.execStatement(sql, par.getRowMeta(), par.getData());
	}

	public synchronized void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleException
	{
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" SET "+quote(FIELD_JOB_ID_DIRECTORY)+" = ? WHERE "+quote(FIELD_JOB_NAME)+" = ? AND "+quote(FIELD_JOB_ID_DIRECTORY)+" = ?";

		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_to));
		par.addValue(new ValueMeta(FIELD_JOB_NAME,  ValueMetaInterface.TYPE_STRING), jobname);
		par.addValue(new ValueMeta(FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_from));

		database.execStatement(sql, par.getRowMeta(), par.getData());
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// GET NEW IDS
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized long getNextTransformationID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION));
	}

	public synchronized long getNextJobID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB), quote(FIELD_JOB_ID_JOB));
	}

	public synchronized long getNextNoteID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_NOTE), quote(FIELD_NOTE_ID_NOTE));
	}
    
    public synchronized long getNextLogID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_REPOSITORY_LOG), quote(FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG));
    }

	public synchronized long getNextDatabaseID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE), quote(FIELD_DATABASE_ID_DATABASE));
	}

	public synchronized long getNextDatabaseTypeID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE));
	}

	public synchronized long getNextDatabaseConnectionTypeID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE));
	}

	public synchronized long getNextLoglevelID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_LOGLEVEL), quote(FIELD_LOGLEVEL_ID_LOGLEVEL));
	}

	public synchronized long getNextStepTypeID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE));
	}

	public synchronized long getNextStepID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP), quote(FIELD_STEP_ID_STEP));
	}

	public synchronized long getNextJobEntryID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY), quote(FIELD_JOBENTRY_ID_JOBENTRY));
	}

	public synchronized long getNextJobEntryTypeID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE));
	}

	public synchronized long getNextJobEntryCopyID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY), quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY));
	}

	public synchronized long getNextStepAttributeID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE), quote(FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE));
	}
	
    public synchronized long getNextTransAttributeID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_ATTRIBUTE), quote(FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE));
    }

    public synchronized long getNextJobAttributeID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_ATTRIBUTE), quote(FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE));                                                                                                         
    }    
    
    public synchronized long getNextDatabaseAttributeID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE), quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE));
    }

	public synchronized long getNextTransHopID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_HOP), quote(FIELD_TRANS_HOP_ID_TRANS_HOP));
	}

	public synchronized long getNextJobHopID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_HOP), quote(FIELD_JOB_HOP_ID_JOB_HOP));
	}

	public synchronized long getNextDepencencyID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DEPENDENCY), quote(FIELD_DEPENDENCY_ID_DEPENDENCY));
	}
    
    public synchronized long getNextPartitionSchemaID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA));
    }

    public synchronized long getNextPartitionID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION), quote(FIELD_PARTITION_ID_PARTITION));
    }

    public synchronized long getNextTransformationPartitionSchemaID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_PARTITION_SCHEMA), quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA));
    }
    
    public synchronized long getNextClusterID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER), quote(FIELD_CLUSTER_ID_CLUSTER));
    }

    public synchronized long getNextSlaveServerID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_SLAVE), quote(FIELD_SLAVE_ID_SLAVE));
    }
    
    public synchronized long getNextClusterSlaveID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER_SLAVE), quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE));
    }
    
    public synchronized long getNextTransformationSlaveID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_SLAVE), quote(FIELD_TRANS_SLAVE_ID_TRANS_SLAVE));
    }
    
    public synchronized long getNextTransformationClusterID() throws KettleException
    {
        return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_CLUSTER), quote(FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER));
    }
    
	public synchronized long getNextConditionID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CONDITION), quote(FIELD_CONDITION_ID_CONDITION));
	}

	public synchronized long getNextValueID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_VALUE), quote(FIELD_VALUE_ID_VALUE));
	}

	public synchronized long getNextUserID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER), quote(FIELD_USER_ID_USER));
	}

	public synchronized long getNextProfileID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE), quote(FIELD_PROFILE_ID_PROFILE));
	}

	public synchronized long getNextPermissionID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PERMISSION), quote(FIELD_PERMISSION_ID_PERMISSION));
	}

	public synchronized long getNextJobEntryAttributeID() throws KettleException
	{
	    return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_ATTRIBUTE), quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE));
	}
	
	public synchronized long getNextID(String tableName, String fieldName) throws KettleException
	{
	    String counterName = tableName+"."+fieldName;
	    Counter counter = Counters.getInstance().getCounter(counterName);
	    if (counter==null)
	    {
	        long id = getNextTableID(tableName, fieldName);
	        counter = new Counter(id);
	        Counters.getInstance().setCounter(counterName, counter);
	        return counter.next();
	    }
	    else
	    {
	        return counter.next();
	    }
	}
    
    public synchronized void clearNextIDCounters()
    {
        Counters.getInstance().clear();
    }

	public synchronized long getNextDirectoryID() throws KettleException
	{
		return getNextID(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY), quote(FIELD_DIRECTORY_ID_DIRECTORY));
	}

	private synchronized long getNextTableID(String tablename, String idfield) throws KettleException
	{
		long retval = -1;

		RowMetaAndData r = database.getOneRow("SELECT MAX(" + idfield + ") FROM " + tablename);
		if (r != null)
		{
			Long id = r.getInteger(0);
			
			if (id == null)
			{
				if (log.isDebug()) log.logDebug(toString(), "no max(" + idfield + ") found in table " + tablename);
				retval = 1;
			}
			else
			{
                if (log.isDebug()) log.logDebug(toString(), "max(" + idfield + ") found in table " + tablename + " --> " + idfield + " number: " + id);
				retval = id.longValue() + 1L;
			}
		}
		return retval;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// INSERT VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void insertTransformation(TransMeta transMeta) throws KettleException
    {
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getId()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_NAME, ValueMetaInterface.TYPE_STRING), transMeta.getName());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_DESCRIPTION, ValueMetaInterface.TYPE_STRING), transMeta.getDescription());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING), transMeta.getExtendedDescription());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_TRANS_VERSION, ValueMetaInterface.TYPE_STRING), transMeta.getTransversion());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_TRANS_STATUS, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getTransstatus()  <0 ? -1L : transMeta.getTransstatus()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_READ, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getReadStep()  ==null ? -1L : transMeta.getReadStep().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_WRITE, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getWriteStep() ==null ? -1L : transMeta.getWriteStep().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_INPUT, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getInputStep() ==null ? -1L : transMeta.getInputStep().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_OUTPUT, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getOutputStep()==null ? -1L : transMeta.getOutputStep().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_UPDATE, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getUpdateStep()==null ? -1L : transMeta.getUpdateStep().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getLogConnection()==null ? -1L : transMeta.getLogConnection().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING), transMeta.getLogTable());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_USE_BATCHID, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(transMeta.isBatchIdUsed()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(transMeta.isLogfieldUsed()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DATABASE_MAXDATE, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getMaxDateConnection()==null ? -1L : transMeta.getMaxDateConnection().getID()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_TABLE_NAME_MAXDATE, ValueMetaInterface.TYPE_STRING), transMeta.getMaxDateTable());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_FIELD_NAME_MAXDATE, ValueMetaInterface.TYPE_STRING), transMeta.getMaxDateField());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_OFFSET_MAXDATE, ValueMetaInterface.TYPE_NUMBER), new Double(transMeta.getMaxDateOffset()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_DIFF_MAXDATE, ValueMetaInterface.TYPE_NUMBER), new Double(transMeta.getMaxDateDifference()));

		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_CREATED_USER, ValueMetaInterface.TYPE_STRING),        transMeta.getCreatedUser());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_CREATED_DATE, ValueMetaInterface.TYPE_DATE), transMeta.getCreatedDate());
		
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_MODIFIED_USER, ValueMetaInterface.TYPE_STRING), transMeta.getModifiedUser());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE), transMeta.getModifiedDate());
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_SIZE_ROWSET, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getSizeRowset()));
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getDirectory().getID()));

		database.prepareInsert(table.getRowMeta(), TABLE_R_TRANSFORMATION);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

        if (transMeta.getRejectedStep()!=null)
        {
            insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_ID_STEP_REJECTED, transMeta.getRejectedStep().getID(), null);
        }

        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_UNIQUE_CONNECTIONS, 0, transMeta.isUsingUniqueConnections()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_FEEDBACK_SHOWN, 0, transMeta.isFeedbackShown()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_FEEDBACK_SIZE, transMeta.getFeedbackSize(), "");
		insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_USING_THREAD_PRIORITIES, 0, transMeta.isUsingThreadPriorityManagment()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_SHARED_FILE, 0, transMeta.getSharedObjectsFile());
        
        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_CAPTURE_STEP_PERFORMANCE, 0, transMeta.isCapturingStepPerformanceSnapShots()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_STEP_PERFORMANCE_CAPTURING_DELAY, transMeta.getStepPerformanceCapturingDelay(), "");
        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_STEP_PERFORMANCE_LOG_TABLE, 0, transMeta.getStepPerformanceLogTable());

        insertTransAttribute(transMeta.getId(), 0, TRANS_ATTRIBUTE_LOG_SIZE_LIMIT, 0, transMeta.getLogSizeLimit());

		// Save the logging connection link...
		if (transMeta.getLogConnection()!=null) insertStepDatabase(transMeta.getId(), -1L, transMeta.getLogConnection().getID());

		// Save the maxdate connection link...
		if (transMeta.getMaxDateConnection()!=null) insertStepDatabase(transMeta.getId(), -1L, transMeta.getMaxDateConnection().getID());
	}

	public synchronized void insertJob(JobMeta jobMeta) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();
		
		table.addValue(new ValueMeta(FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER), jobMeta.getID());
		table.addValue(new ValueMeta(FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), jobMeta.getDirectory().getID());
		table.addValue(new ValueMeta(FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING), jobMeta.getName());
		table.addValue(new ValueMeta(FIELD_JOB_DESCRIPTION, ValueMetaInterface.TYPE_STRING), jobMeta.getDescription());
		table.addValue(new ValueMeta(FIELD_JOB_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING), jobMeta.getExtendedDescription());
		table.addValue(new ValueMeta(FIELD_JOB_JOB_VERSION, ValueMetaInterface.TYPE_STRING), jobMeta.getJobversion());
		table.addValue(new ValueMeta(FIELD_JOB_JOB_STATUS, ValueMetaInterface.TYPE_INTEGER), new Long(jobMeta.getJobstatus()  <0 ? -1L : jobMeta.getJobstatus()));

		table.addValue(new ValueMeta(FIELD_JOB_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER), jobMeta.getLogConnection()!=null ? jobMeta.getLogConnection().getID() : -1L);
		table.addValue(new ValueMeta(FIELD_JOB_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING), jobMeta.getLogTable());

		table.addValue(new ValueMeta(FIELD_JOB_CREATED_USER, ValueMetaInterface.TYPE_STRING), jobMeta.getCreatedUser());
		table.addValue(new ValueMeta(FIELD_JOB_CREATED_DATE, ValueMetaInterface.TYPE_DATE), jobMeta.getCreatedDate());
		table.addValue(new ValueMeta(FIELD_JOB_MODIFIED_USER, ValueMetaInterface.TYPE_STRING), jobMeta.getModifiedUser());
		table.addValue(new ValueMeta(FIELD_JOB_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE), jobMeta.getModifiedDate());
        table.addValue(new ValueMeta(FIELD_JOB_USE_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN), jobMeta.isBatchIdUsed());
        table.addValue(new ValueMeta(FIELD_JOB_PASS_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN), jobMeta.isBatchIdPassed());
        table.addValue(new ValueMeta(FIELD_JOB_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN), jobMeta.isLogfieldUsed());
        table.addValue(new ValueMeta(FIELD_JOB_SHARED_FILE, ValueMetaInterface.TYPE_STRING), jobMeta.getSharedObjectsFile());
        
		database.prepareInsert(table.getRowMeta(), TABLE_R_JOB);
		database.setValuesInsert(table);
		database.insertRow();
        if (log.isDebug()) log.logDebug(toString(), "Inserted new record into table "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" with data : " + table);
		database.closeInsert();
		
        insertJobAttribute(jobMeta.getID(), 0, JOB_ATTRIBUTE_LOG_SIZE_LIMIT, 0, jobMeta.getLogSizeLimit());
	}

	public synchronized long insertNote(String note, long gui_location_x, long gui_location_y, long gui_location_width, long gui_location_height) throws KettleException
	{
		long id = getNextNoteID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_NOTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), note);
		table.addValue(new ValueMeta(FIELD_NOTE_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta(FIELD_NOTE_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta(FIELD_NOTE_GUI_LOCATION_WIDTH, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_width));
		table.addValue(new ValueMeta(FIELD_NOTE_GUI_LOCATION_HEIGHT, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_height));

		database.prepareInsert(table.getRowMeta(), TABLE_R_NOTE);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}
    
    public synchronized long insertLogEntry(String description) throws KettleException
    {
        long id = getNextLogID();

        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_REPOSITORY_LOG_REP_VERSION, ValueMetaInterface.TYPE_STRING), getVersion());
        table.addValue(new ValueMeta(FIELD_REPOSITORY_LOG_LOG_DATE, ValueMetaInterface.TYPE_DATE), new Date());
        table.addValue(new ValueMeta(FIELD_REPOSITORY_LOG_LOG_USER, ValueMetaInterface.TYPE_STRING), userinfo!=null?userinfo.getLogin():"admin");
        table.addValue(new ValueMeta(FIELD_REPOSITORY_LOG_OPERATION_DESC, ValueMetaInterface.TYPE_STRING), description);

        database.prepareInsert(table.getRowMeta(), TABLE_R_REPOSITORY_LOG);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

	public synchronized void insertTransNote(long id_transformation, long id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_TRANS_NOTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(FIELD_TRANS_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), new Long(id_note));

		database.prepareInsert(table.getRowMeta(), TABLE_R_TRANS_NOTE);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized void insertJobNote(long id_job, long id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOB_NOTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(FIELD_JOB_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), new Long(id_note));

		database.prepareInsert(table.getRowMeta(), TABLE_R_JOB_NOTE);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized long insertDatabase(String name, String type, String access, String host, String dbname, String port,
			String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleException
	{

		long id = getNextDatabaseID();

		long id_database_type = getDatabaseTypeID(type);
		if (id_database_type < 0) // New support database type: add it!
		{
			id_database_type = getNextDatabaseTypeID();

			String tablename = TABLE_R_DATABASE_TYPE;
			RowMetaInterface tableMeta = new RowMeta();
            
            tableMeta.addValueMeta(new ValueMeta(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER, 5, 0));
            tableMeta.addValueMeta(new ValueMeta(FIELD_DATABASE_TYPE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
            tableMeta.addValueMeta(new ValueMeta(FIELD_DATABASE_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));

			database.prepareInsert(tableMeta, tablename);

			Object[] tableData = new Object[3];
            int tableIndex = 0;
            
			tableData[tableIndex++] = new Long(id_database_type);
            tableData[tableIndex++] = type;
            tableData[tableIndex++] = type;

			database.setValuesInsert(tableMeta, tableData);
			database.insertRow();
			database.closeInsert();
		}

		long id_database_contype = getDatabaseConTypeID(access);

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_type));
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_contype));
		table.addValue(new ValueMeta(FIELD_DATABASE_HOST_NAME, ValueMetaInterface.TYPE_STRING), host);
		table.addValue(new ValueMeta(FIELD_DATABASE_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), dbname);
		table.addValue(new ValueMeta(FIELD_DATABASE_PORT, ValueMetaInterface.TYPE_INTEGER), new Long(Const.toInt(port, -1)));
		table.addValue(new ValueMeta(FIELD_DATABASE_USERNAME, ValueMetaInterface.TYPE_STRING), user);
		table.addValue(new ValueMeta(FIELD_DATABASE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(pass));
		table.addValue(new ValueMeta(FIELD_DATABASE_SERVERNAME, ValueMetaInterface.TYPE_STRING), servername);
		table.addValue(new ValueMeta(FIELD_DATABASE_DATA_TBS, ValueMetaInterface.TYPE_STRING), data_tablespace);
		table.addValue(new ValueMeta(FIELD_DATABASE_INDEX_TBS, ValueMetaInterface.TYPE_STRING), index_tablespace);

		database.prepareInsert(table.getRowMeta(), TABLE_R_DATABASE);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertStep(long id_transformation, String name, String description, String steptype,
			boolean distribute, long copies, long gui_location_x, long gui_location_y, boolean gui_draw)
			throws KettleException
	{
		long id = getNextStepID();

		long id_step_type = getStepTypeID(steptype);

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_STEP_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_STEP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(FIELD_STEP_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(FIELD_STEP_DESCRIPTION, ValueMetaInterface.TYPE_STRING), description);
		table.addValue(new ValueMeta(FIELD_STEP_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_step_type));
		table.addValue(new ValueMeta(FIELD_STEP_DISTRIBUTE, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(distribute));
		table.addValue(new ValueMeta(FIELD_STEP_COPIES, ValueMetaInterface.TYPE_INTEGER), new Long(copies));
		table.addValue(new ValueMeta(FIELD_STEP_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta(FIELD_STEP_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta(FIELD_STEP_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(gui_draw));

		database.prepareInsert(table.getRowMeta(), TABLE_R_STEP);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertStepAttribute(long id_transformation, long id_step, long nr, String code, double value_num,
			String value_str) throws KettleException
	{
		long id = getNextStepAttributeID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_NUMBER), new Double(value_num));
		table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);

		/* If we have prepared the insert, we don't do it again.
		 * We asume that all the step insert statements come one after the other.
		 */
		
		if (psStepAttributesInsert == null)
		{
		    String sql = database.getInsertStatement(TABLE_R_STEP_ATTRIBUTE, table.getRowMeta());
		    psStepAttributesInsert = database.prepareSQL(sql);
		}
		database.setValues(table, psStepAttributesInsert);
		database.insertRow(psStepAttributesInsert, useBatchProcessing);
		
        if (log.isDebug()) log.logDebug(toString(), "saved attribute ["+code+"]");
		
		return id;
	}
    
    public synchronized long insertTransAttribute(long id_transformation, long nr, String code, long value_num, String value_str) throws KettleException
    {
        long id = getNextTransAttributeID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER), new Long(value_num));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        
        if (psTransAttributesInsert == null)
        {
            String sql = database.getInsertStatement(TABLE_R_TRANS_ATTRIBUTE, table.getRowMeta());
            psTransAttributesInsert = database.prepareSQL(sql);
        }
        database.setValues(table, psTransAttributesInsert);
        database.insertRow(psTransAttributesInsert, useBatchProcessing);
        
        if (log.isDebug()) log.logDebug(toString(), "saved transformation attribute ["+code+"]");
        
        return id;
    }

    public synchronized long insertJobAttribute(long id_job, long nr, String code, long value_num, String value_str) throws KettleException
    {
    	long id = getNextJobAttributeID();
    	
    	System.out.println("Insert job attribute : id_job="+id_job+", code="+code+", value_str="+value_str);

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER), new Long(value_num));
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        
        if (psJobAttributesInsert == null)
        {
            String sql = database.getInsertStatement(TABLE_R_JOB_ATTRIBUTE, table.getRowMeta());
            psJobAttributesInsert = database.prepareSQL(sql);
        }
        database.setValues(table, psJobAttributesInsert);
        database.insertRow(psJobAttributesInsert, useBatchProcessing);
        
        if (log.isDebug()) log.logDebug(toString(), "saved job attribute ["+code+"]");
        
        return id;
    }    

	public synchronized void insertStepDatabase(long id_transformation, long id_step, long id_database)
			throws KettleException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		RowMetaAndData check = getStepDatabase(id_step);
		if (check == null)
		{
			RowMetaAndData table = new RowMetaAndData();

			table.addValue(new ValueMeta(FIELD_STEP_DATABASE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
			table.addValue(new ValueMeta(FIELD_STEP_DATABASE_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
			table.addValue(new ValueMeta(FIELD_STEP_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

			database.insertRow(TABLE_R_STEP_DATABASE, table.getRowMeta(), table.getData());
		}
	}
	
    public synchronized long insertDatabaseAttribute(long id_database, String code, String value_str) throws KettleException
    {
        long id = getNextDatabaseAttributeID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));
        table.addValue(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        database.prepareInsert(table.getRowMeta(), TABLE_R_DATABASE_ATTRIBUTE);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();
        
        if (log.isDebug()) log.logDebug(toString(), "saved database attribute ["+code+"]");
        
        return id;
    }

	
	public synchronized long insertJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value_num,
			String value_str) throws KettleException
	{
		long id = getNextJobEntryAttributeID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_NUMBER), new Double(value_num));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);

		database.prepareInsert(table.getRowMeta(), TABLE_R_JOBENTRY_ATTRIBUTE);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertTransHop(long id_transformation, long id_step_from, long id_step_to, boolean enabled)
			throws KettleException
	{
		long id = getNextTransHopID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_TRANS_HOP_ID_TRANS_HOP, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
		table.addValue(new ValueMeta(FIELD_TRANS_HOP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_transformation));
		table.addValue(new ValueMeta(FIELD_TRANS_HOP_ID_STEP_FROM, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_step_from));
		table.addValue(new ValueMeta(FIELD_TRANS_HOP_ID_STEP_TO, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_step_to));
		table.addValue(new ValueMeta(FIELD_TRANS_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(enabled));

		database.prepareInsert(table.getRowMeta(), TABLE_R_TRANS_HOP);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertJobHop(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to, boolean enabled,
			boolean evaluation, boolean unconditional) throws KettleException
	{
		long id = getNextJobHopID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOB_HOP_ID_JOB_HOP, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
		table.addValue(new ValueMeta(FIELD_JOB_HOP_ID_JOB, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_job));
		table.addValue(new ValueMeta(FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_jobentry_copy_from));
		table.addValue(new ValueMeta(FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_jobentry_copy_to));
		table.addValue(new ValueMeta(FIELD_JOB_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(enabled));
		table.addValue(new ValueMeta(FIELD_JOB_HOP_EVALUATION, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(evaluation));
		table.addValue(new ValueMeta(FIELD_JOB_HOP_UNCONDITIONAL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(unconditional));

		database.prepareInsert(table.getRowMeta(), TABLE_R_JOB_HOP);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertDependency(long id_transformation, long id_database, String tablename, String fieldname)
			throws KettleException
	{
		long id = getNextDepencencyID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_DEPENDENCY_ID_DEPENDENCY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_DEPENDENCY_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(FIELD_DEPENDENCY_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));
		table.addValue(new ValueMeta(FIELD_DEPENDENCY_TABLE_NAME, ValueMetaInterface.TYPE_STRING), tablename);
		table.addValue(new ValueMeta(FIELD_DEPENDENCY_FIELD_NAME, ValueMetaInterface.TYPE_STRING), fieldname);

		database.prepareInsert(table.getRowMeta(), TABLE_R_DEPENDENCY);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

    public synchronized long insertPartitionSchema(PartitionSchema partitionSchema) throws KettleException
    {
        long id = getNextPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_NAME, ValueMetaInterface.TYPE_STRING), partitionSchema.getName());
        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION, ValueMetaInterface.TYPE_BOOLEAN), partitionSchema.isDynamicallyDefined());
        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE, ValueMetaInterface.TYPE_STRING), partitionSchema.getNumberOfPartitionsPerSlave());

        database.prepareInsert(table.getRowMeta(), TABLE_R_PARTITION_SCHEMA);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized void updatePartitionSchema(PartitionSchema partitionSchema) throws KettleException
    {
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_NAME, ValueMetaInterface.TYPE_STRING), partitionSchema.getName());
        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION, ValueMetaInterface.TYPE_BOOLEAN), partitionSchema.isDynamicallyDefined());
        table.addValue(new ValueMeta(FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE, ValueMetaInterface.TYPE_STRING), partitionSchema.getNumberOfPartitionsPerSlave());
        updateTableRow(TABLE_R_PARTITION_SCHEMA, FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, table, partitionSchema.getId());
    }

    public synchronized long insertPartition(long id_partition_schema, String partition_id) throws KettleException
    {
        long id = getNextPartitionID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_PARTITION_ID_PARTITION, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_PARTITION_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id_partition_schema));
        table.addValue(new ValueMeta(FIELD_PARTITION_PARTITION_ID, ValueMetaInterface.TYPE_STRING), partition_id);

        database.prepareInsert(table.getRowMeta(), TABLE_R_PARTITION);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationPartitionSchema(long id_transformation, long id_partition_schema) throws KettleException
    {
        long id = getNextTransformationPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id_partition_schema));

        database.prepareInsert(table.getRowMeta(), TABLE_R_TRANS_PARTITION_SCHEMA);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized long insertCluster(ClusterSchema clusterSchema) throws KettleException
    {
        long id = getNextClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
        table.addValue(new ValueMeta(FIELD_CLUSTER_NAME, ValueMetaInterface.TYPE_STRING), clusterSchema.getName());
        table.addValue(new ValueMeta(FIELD_CLUSTER_BASE_PORT, ValueMetaInterface.TYPE_STRING), clusterSchema.getBasePort());
        table.addValue(new ValueMeta(FIELD_CLUSTER_SOCKETS_BUFFER_SIZE, ValueMetaInterface.TYPE_STRING), clusterSchema.getSocketsBufferSize());
        table.addValue(new ValueMeta(FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL, ValueMetaInterface.TYPE_STRING), clusterSchema.getSocketsFlushInterval());
        table.addValue(new ValueMeta(FIELD_CLUSTER_SOCKETS_COMPRESSED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(clusterSchema.isSocketsCompressed()));
        table.addValue(new ValueMeta(FIELD_CLUSTER_DYNAMIC, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(clusterSchema.isDynamic()));

        database.prepareInsert(table.getRowMeta(), TABLE_R_CLUSTER);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertSlave(SlaveServer slaveServer) throws KettleException
    {
        long id = getNextSlaveServerID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
        table.addValue(new ValueMeta(FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getName());
        table.addValue(new ValueMeta(FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getHostname());
        table.addValue(new ValueMeta(FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getPort());
        table.addValue(new ValueMeta(FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING), slaveServer.getUsername());
        table.addValue(new ValueMeta(FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(slaveServer.getPassword()));
        table.addValue(new ValueMeta(FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyHostname());
        table.addValue(new ValueMeta(FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyPort());
        table.addValue(new ValueMeta(FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING), slaveServer.getNonProxyHosts());
        table.addValue(new ValueMeta(FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(slaveServer.isMaster()));

        database.prepareInsert(table.getRowMeta(), TABLE_R_SLAVE);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized void updateSlave(SlaveServer slaveServer) throws KettleException
    {
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getName());
        table.addValue(new ValueMeta(FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getHostname());
        table.addValue(new ValueMeta(FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getPort());
        table.addValue(new ValueMeta(FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING), slaveServer.getUsername());
        table.addValue(new ValueMeta(FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(slaveServer.getPassword()));
        table.addValue(new ValueMeta(FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyHostname());
        table.addValue(new ValueMeta(FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyPort());
        table.addValue(new ValueMeta(FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING), slaveServer.getNonProxyHosts());
        table.addValue(new ValueMeta(FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(slaveServer.isMaster()));

        updateTableRow(TABLE_R_SLAVE, FIELD_SLAVE_ID_SLAVE, table, slaveServer.getId());
    }
    
    public synchronized long insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        long id = getNextClusterSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_CLUSTER_SLAVE_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), new Long(clusterSchema.getId()));
        table.addValue(new ValueMeta(FIELD_CLUSTER_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(slaveServer.getId()));

        database.prepareInsert(table.getRowMeta(), TABLE_R_CLUSTER_SLAVE);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationCluster(long id_transformation, long id_cluster) throws KettleException
    {
        long id = getNextTransformationClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), new Long(id_cluster));

        database.prepareInsert(table.getRowMeta(), TABLE_R_TRANS_CLUSTER);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationSlave(long id_transformation, long id_slave) throws KettleException
    {
        long id = getNextTransformationSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(FIELD_TRANS_SLAVE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(id_slave));

        database.prepareInsert(table.getRowMeta(), TABLE_R_TRANS_SLAVE);
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    
	public synchronized long insertCondition(long id_condition_parent, Condition condition) throws KettleException
	{
		long id = getNextConditionID();

		String tablename = TABLE_R_CONDITION;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
		table.addValue(new ValueMeta(FIELD_CONDITION_ID_CONDITION_PARENT, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_condition_parent));
		table.addValue(new ValueMeta(FIELD_CONDITION_NEGATED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(condition.isNegated()));
		table.addValue(new ValueMeta(FIELD_CONDITION_OPERATOR, ValueMetaInterface.TYPE_STRING), condition.getOperatorDesc());
		table.addValue(new ValueMeta(FIELD_CONDITION_LEFT_NAME, ValueMetaInterface.TYPE_STRING), condition.getLeftValuename());
		table.addValue(new ValueMeta(FIELD_CONDITION_CONDITION_FUNCTION, ValueMetaInterface.TYPE_STRING), condition.getFunctionDesc());
		table.addValue(new ValueMeta(FIELD_CONDITION_RIGHT_NAME, ValueMetaInterface.TYPE_STRING), condition.getRightValuename());

		long id_value = -1L;
		ValueMetaAndData v = condition.getRightExact();

		if (v != null)
		{
			
			// We have to make sure that all data is saved irrespective of locale differences.
			// Here is where we force that
			//
			ValueMetaInterface valueMeta = v.getValueMeta();
			valueMeta.setDecimalSymbol(ValueMetaAndData.VALUE_REPOSITORY_DECIMAL_SYMBOL);
			valueMeta.setGroupingSymbol(ValueMetaAndData.VALUE_REPOSITORY_GROUPING_SYMBOL);
			switch(valueMeta.getType())
			{
			case ValueMetaInterface.TYPE_NUMBER:
				valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_NUMBER_CONVERSION_MASK);
				break;
			case ValueMetaInterface.TYPE_INTEGER:
				valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_INTEGER_CONVERSION_MASK);
				break;
			case ValueMetaInterface.TYPE_DATE:
				valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_DATE_CONVERSION_MASK);
				break;
			default:
				break;
			}
			String stringValue = valueMeta.getString(v.getValueData());
			
			id_value = insertValue(valueMeta.getName(), valueMeta.getTypeDesc(), stringValue, valueMeta.isNull(v.getValueData()), condition.getRightExactID());
			condition.setRightExactID(id_value);
		}
		table.addValue(new ValueMeta(FIELD_CONDITION_ID_VALUE_RIGHT, ValueMetaInterface.TYPE_INTEGER), new Long(id_value));

		database.prepareInsert(table.getRowMeta(), tablename);

		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized void insertTransStepCondition(long id_transformation, long id_step, long id_condition)
			throws KettleException
	{
		String tablename = TABLE_R_TRANS_STEP_CONDITION;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(FIELD_TRANS_STEP_CONDITION_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		table.addValue(new ValueMeta(FIELD_TRANS_STEP_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER), new Long(id_condition));

		database.prepareInsert(table.getRowMeta(), tablename);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized long insertDirectory(long id_directory_parent, RepositoryDirectory dir) throws KettleException
	{
		long id = getNextDirectoryID();

		String tablename = TABLE_R_DIRECTORY;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_DIRECTORY_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_DIRECTORY_ID_DIRECTORY_PARENT, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_parent));
		table.addValue(new ValueMeta(FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), dir.getDirectoryName());

		database.prepareInsert(table.getRowMeta(), tablename);

		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized void deleteDirectory(long id_directory) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;
		database.execStatement(sql);
	}

	public synchronized void renameDirectory(long id_directory, String name) throws KettleException
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta(FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), name);

		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" SET "+quote(FIELD_DIRECTORY_DIRECTORY_NAME)+" = ? WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;

		log.logBasic(toString(), "sql = [" + sql + "]");
		log.logBasic(toString(), "row = [" + r + "]");

		database.execStatement(sql, r.getRowMeta(), r.getData());
	}

	public synchronized long lookupValue(String name, String type, String value_str, boolean isnull) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_VALUE_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(FIELD_VALUE_VALUE_TYPE, ValueMetaInterface.TYPE_STRING), type);
		table.addValue(new ValueMeta(FIELD_VALUE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);
		table.addValue(new ValueMeta(FIELD_VALUE_IS_NULL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(isnull));

		String sql = "SELECT " + quote(FIELD_VALUE_ID_VALUE) + " FROM " + databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_VALUE) + " ";
		sql += "WHERE " + quote(FIELD_VALUE_NAME) + "       = ? ";
		sql += "AND   " + quote(FIELD_VALUE_VALUE_TYPE) + " = ? ";
		sql += "AND   " + quote(FIELD_VALUE_VALUE_STR) + "  = ? ";
		sql += "AND   " + quote(FIELD_VALUE_IS_NULL) + "    = ? ";

		RowMetaAndData result = database.getOneRow(sql, table.getRowMeta(), table.getData());
		if (result != null && result.getData()!=null && result.isNumeric(0))
			return result.getInteger(0, 0L);
		else
			return -1;
	}

	public synchronized long insertValue(String name, String type, String value_str, boolean isnull, long id_value_prev) throws KettleException
	{
		long id_value = lookupValue(name, type, value_str, isnull);
		// if it didn't exist yet: insert it!!

		if (id_value < 0)
		{
			id_value = getNextValueID();

			// Let's see if the same value is not yet available?
			String tablename = TABLE_R_VALUE;
			RowMetaAndData table = new RowMetaAndData();
			table.addValue(new ValueMeta(FIELD_VALUE_ID_VALUE, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_value));
			table.addValue(new ValueMeta(FIELD_VALUE_NAME, ValueMetaInterface.TYPE_STRING), name);
			table.addValue(new ValueMeta(FIELD_VALUE_VALUE_TYPE, ValueMetaInterface.TYPE_STRING), type);
			table.addValue(new ValueMeta(FIELD_VALUE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);
			table.addValue(new ValueMeta(FIELD_VALUE_IS_NULL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(isnull));

			database.prepareInsert(table.getRowMeta(), tablename);
			database.setValuesInsert(table);
			database.insertRow();
			database.closeInsert();
		}

		return id_value;
	}

	public synchronized long insertJobEntry(long id_job, JobEntryBase jobEntryBase)
			throws KettleException
	{
		long id = getNextJobEntryID();

		long id_jobentry_type = getJobEntryTypeID(jobEntryBase.getTypeCode());

		log.logDebug(toString(), "ID_JobEntry_type = " + id_jobentry_type + " for type = [" + jobEntryBase.getTypeCode() + "]");

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOBENTRY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_type));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_NAME, ValueMetaInterface.TYPE_STRING), jobEntryBase.getName());
		table.addValue(new ValueMeta(FIELD_JOBENTRY_DESCRIPTION, ValueMetaInterface.TYPE_STRING), jobEntryBase.getDescription());

		database.prepareInsert(table.getRowMeta(), TABLE_R_JOBENTRY);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertJobEntryCopy(long id_job, long id_jobentry, long id_jobentry_type, int nr, long gui_location_x,
			long gui_location_y, boolean gui_draw, boolean parallel) throws KettleException
	{
		long id = getNextJobEntryCopyID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_type));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(gui_draw));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_COPY_PARALLEL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(parallel));

		database.prepareInsert(table.getRowMeta(), TABLE_R_JOBENTRY_COPY);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized void insertTableRow(String tablename, RowMetaAndData values) throws KettleException
	{
		database.prepareInsert(values.getRowMeta(), tablename);
		database.setValuesInsert(values);
		database.insertRow();
		database.closeInsert();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// UPDATE VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void updateDatabase(long id_database, String name, String type, String access, String host, String dbname,
			String port, String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleException
	{
		long id_database_type = getDatabaseTypeID(type);
		long id_database_contype = getDatabaseConTypeID(access);

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_type));
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_contype));
		table.addValue(new ValueMeta(FIELD_DATABASE_HOST_NAME, ValueMetaInterface.TYPE_STRING), host);
		table.addValue(new ValueMeta(FIELD_DATABASE_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), dbname);
		table.addValue(new ValueMeta(FIELD_DATABASE_PORT, ValueMetaInterface.TYPE_INTEGER), new Long(Const.toInt(port, -1)));
		table.addValue(new ValueMeta(FIELD_DATABASE_USERNAME, ValueMetaInterface.TYPE_STRING), user);
		table.addValue(new ValueMeta(FIELD_DATABASE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(pass));
		table.addValue(new ValueMeta(FIELD_DATABASE_SERVERNAME, ValueMetaInterface.TYPE_STRING), servername);
		table.addValue(new ValueMeta(FIELD_DATABASE_DATA_TBS, ValueMetaInterface.TYPE_STRING), data_tablespace);
		table.addValue(new ValueMeta(FIELD_DATABASE_INDEX_TBS, ValueMetaInterface.TYPE_STRING), index_tablespace);

		updateTableRow(TABLE_R_DATABASE, FIELD_DATABASE_ID_DATABASE, table, id_database);
	}

	public synchronized void updateTableRow(String tablename, String idfield, RowMetaAndData values, long id) throws KettleException
	{
		String sets[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			sets[i] = values.getValueMeta(i).getName();
		String codes[] = new String[] { idfield };
		String condition[] = new String[] { "=" };

		database.prepareUpdate(tablename, codes, condition, sets);

		values.addValue(new ValueMeta(idfield, ValueMetaInterface.TYPE_INTEGER), new Long(id));

		database.setValuesUpdate(values.getRowMeta(), values.getData());
		database.updateRow();
		database.closeUpdate();
	}

	public synchronized void updateTableRow(String tablename, String idfield, RowMetaAndData values) throws KettleException
	{
		long id = values.getInteger(idfield, 0L);
		values.removeValue(idfield);
		String sets[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			sets[i] = values.getValueMeta(i).getName();
		String codes[] = new String[] { idfield };
		String condition[] = new String[] { "=" };

		database.prepareUpdate(tablename, codes, condition, sets);

		values.addValue(new ValueMeta(idfield, ValueMetaInterface.TYPE_INTEGER), new Long(id));

		database.setValuesUpdate(values.getRowMeta(), values.getData());
		database.updateRow();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// READ DATA FROM REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized int getNrJobs() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB);
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransformations(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION)+" WHERE "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobs(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" WHERE "+quote(FIELD_JOB_ID_DIRECTORY)+" = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

    public synchronized int getNrDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrConditions(long id_transforamtion) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transforamtion;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrDatabases(long id_transforamtion) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transforamtion;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrSubConditions(long id_condition) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CONDITION)+" WHERE "+quote(FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransNotes(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_NOTE)+" WHERE "+quote(FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobNotes(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_NOTE)+" WHERE "+quote(FIELD_JOB_ID_JOB)+" = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrDatabases() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE);
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

    public synchronized int getNrDatabaseAttributes(long id_database) throws KettleException
    {
        int retval = 0;

        String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database;
        RowMetaAndData r = database.getOneRow(sql);
        if (r != null)
        {
            retval = (int) r.getInteger(0, 0L);
        }

        return retval;
    }

	public synchronized int getNrSteps(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP)+" WHERE "+quote(FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrStepDatabases(long id_database) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_DATABASE)+" = " + id_database;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrStepAttributes(long id_step) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+" = " + id_step;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransHops(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_HOP)+" WHERE "+quote(FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobHops(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_HOP)+" WHERE "+quote(FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransDependencies(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DEPENDENCY)+" WHERE "+quote(FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobEntries(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY)+" WHERE "+quote(FIELD_JOBENTRY_ID_JOB)+" = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobEntryCopies(long id_job, long id_jobentry) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job + " AND "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY)+" = "
						+ id_jobentry;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobEntryCopies(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrUsers() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER);
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrPermissions(long id_profile) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrProfiles() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE);
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized String[] getTransformationNames(long id_directory) throws KettleException
	{
		return getStrings("SELECT "+quote(FIELD_TRANSFORMATION_NAME)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION)+" WHERE "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(FIELD_TRANSFORMATION_NAME));
	}
    
    public List<RepositoryObject> getJobObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB), RepositoryObject.STRING_OBJECT_TYPE_JOB, id_directory);
    }

    public List<RepositoryObject> getTransformationObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION), RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION, id_directory);
    }

    /**
     * @param id_directory
     * @return A list of RepositoryObjects
     * 
     * @throws KettleException
     */
    private synchronized List<RepositoryObject> getRepositoryObjects(String tableName, String objectType, long id_directory) throws KettleException
    {
        String sql = "SELECT "+quote(FIELD_TRANSFORMATION_NAME)+", "+quote(FIELD_TRANSFORMATION_MODIFIED_USER)+", "+quote(FIELD_TRANSFORMATION_MODIFIED_DATE)+", "+quote(FIELD_TRANSFORMATION_DESCRIPTION)+" " +
                "FROM "+tableName+" " +
                "WHERE "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory + " "
                ;

        List<RepositoryObject> repositoryObjects = new ArrayList<RepositoryObject>();
        
        ResultSet rs = database.openQuery(sql);
        if (rs != null)
        {
        	try
        	{
                Object[] r = database.getRow(rs);
                while (r != null)
                {
                    RowMetaInterface rowMeta = database.getReturnRowMeta();
                    
                    repositoryObjects.add(new RepositoryObject( rowMeta.getString(r, 0), rowMeta.getString(r, 1), rowMeta.getDate(r, 2), objectType, rowMeta.getString(r, 3)));
                    r = database.getRow(rs);
                }
        	}
        	finally 
        	{
        		if ( rs != null )
        		{
        			database.closeQuery(rs);
        		}
        	}                
        }

        return repositoryObjects;
    }
    

	public synchronized String[] getJobNames(long id_directory) throws KettleException
	{
        return getStrings("SELECT "+quote(FIELD_JOB_NAME)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" WHERE "+quote(FIELD_JOB_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(FIELD_JOB_NAME));
	}

	public synchronized String[] getDirectoryNames(long id_directory) throws KettleException
	{
        return getStrings("SELECT "+quote(FIELD_DIRECTORY_DIRECTORY_NAME)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory + " ORDER BY "+quote(FIELD_DIRECTORY_DIRECTORY_NAME));
	}

	public synchronized String[] getJobNames() throws KettleException
	{
        return getStrings("SELECT "+quote(FIELD_JOB_NAME)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" ORDER BY "+quote(FIELD_JOB_NAME));
	}

	public long[] getSubConditionIDs(long id_condition) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_CONDITION_ID_CONDITION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CONDITION)+" WHERE "+quote(FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition);
	}

	public long[] getTransNoteIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_TRANS_NOTE_ID_NOTE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_NOTE)+" WHERE "+quote(FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getConditionIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_TRANS_STEP_CONDITION_ID_CONDITION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getDatabaseIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_STEP_DATABASE_ID_DATABASE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getJobNoteIDs(long id_job) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_JOB_NOTE_ID_NOTE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_NOTE)+" WHERE "+quote(FIELD_JOB_NOTE_ID_JOB)+" = " + id_job);
	}

	public long[] getDatabaseIDs() throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_DATABASE_ID_DATABASE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE)+" ORDER BY "+quote(FIELD_DATABASE_ID_DATABASE));
	}
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database);
    }
    
    public long[] getPartitionSchemaIDs() throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+quote(FIELD_PARTITION_SCHEMA_NAME));
    }
    
    public long[] getPartitionIDs(long id_partition_schema) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_PARTITION_ID_PARTITION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION)+" WHERE "+quote(FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }

    public long[] getTransformationPartitionSchemaIDs(long id_transformation) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = "+id_transformation);
    }
    
    public long[] getTransformationClusterSchemaIDs(long id_transformation) throws KettleException
    {
        return getIDs("SELECT ID_TRANS_CLUSTER FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_CLUSTER)+" WHERE ID_TRANSFORMATION = " + id_transformation);
    }
    
    public long[] getClusterIDs() throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_CLUSTER_ID_CLUSTER)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER)+" ORDER BY "+quote(FIELD_CLUSTER_NAME)); 
    }

    public long[] getSlaveIDs() throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_SLAVE_ID_SLAVE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_SLAVE));
    }

    public long[] getSlaveIDs(long id_cluster_schema) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_CLUSTER_SLAVE_ID_SLAVE)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster_schema);
    }
    
    private long[] getIDs(String sql) throws KettleException
    {
        List<Long> ids = new ArrayList<Long>();
        
        ResultSet rs = database.openQuery(sql);
        try 
        {
            Object[] r = database.getRow(rs);
            while (r != null)
            {
                RowMetaInterface rowMeta = database.getReturnRowMeta();
                Long id = rowMeta.getInteger(r, 0);
                if (id==null) id=new Long(0);
                
                ids.add(id);
                r = database.getRow(rs);
            }
        }
        finally
        {
        	if ( rs != null )
        	{
        		database.closeQuery(rs);        		
        	}
        }
        return convertLongList(ids);
    }
    
    private String[] getStrings(String sql) throws KettleException
    {
        List<String> ids = new ArrayList<String>();
        
        ResultSet rs = database.openQuery(sql);
        try 
        {
            Object[] r = database.getRow(rs);
            while (r != null)
            {
                RowMetaInterface rowMeta = database.getReturnRowMeta();
                ids.add( rowMeta.getString(r, 0) );
                r = database.getRow(rs);
            }
        }
        finally 
        {
        	if ( rs != null )
        	{
        		database.closeQuery(rs);        		
        	}
        }            

        return (String[]) ids.toArray(new String[ids.size()]);

    }
    
    private long[] convertLongList(List<Long> list)
    {
        long[] ids = new long[list.size()];
        for (int i=0;i<ids.length;i++) ids[i] = list.get(i);
        return ids;
    }

	public synchronized String[] getDatabaseNames() throws KettleException
	{
		String nameField = quote(FIELD_DATABASE_NAME);
		return getStrings("SELECT "+nameField+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE)+" ORDER BY "+nameField);
	}
    
    public synchronized String[] getPartitionSchemaNames() throws KettleException
    {
        String nameField = quote(FIELD_PARTITION_SCHEMA_NAME);
        return getStrings("SELECT "+nameField+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getSlaveNames() throws KettleException
    {
        String nameField = quote(FIELD_SLAVE_NAME);
        return getStrings("SELECT "+nameField+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_SLAVE)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getClusterNames() throws KettleException
    {
        String nameField = quote(FIELD_CLUSTER_NAME);
        return getStrings("SELECT "+nameField+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER)+" ORDER BY "+nameField);
    }

	public long[] getStepIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_STEP_ID_STEP)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP)+" WHERE "+quote(FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public synchronized String[] getTransformationsUsingDatabase(long id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_DATABASE)+" = " + id_database;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
	}
    
    public synchronized String[] getClustersUsingSlave(long id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(FIELD_CLUSTER_SLAVE_ID_SLAVE)+" = " + id_slave;

        List<Object[]> list = database.getRows(sql, 100);
        RowMetaInterface rowMeta = database.getReturnRowMeta();
        List<String> clusterList = new ArrayList<String>();

        for (int i=0;i<list.size();i++)
        {
            long id_cluster_schema = rowMeta.getInteger(list.get(i), quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER), -1L); 
            if (id_cluster_schema > 0)
            {
                RowMetaAndData transRow =  getClusterSchema(id_cluster_schema);
                if (transRow!=null)
                {
                    String clusterName = transRow.getString(quote(FIELD_CLUSTER_NAME), "<name not found>");
                    if (clusterName!=null) clusterList.add(clusterName);
                }
            }
        }

        return (String[]) clusterList.toArray(new String[clusterList.size()]);
    }

    public synchronized String[] getTransformationsUsingSlave(long id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_SLAVE)+" WHERE "+quote(FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingPartitionSchema(long id_partition_schema) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+
                     " FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingCluster(long id_cluster) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(FIELD_TRANS_CLUSTER_ID_CLUSTER)+" = " + id_cluster;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }

	private String[] getTransformationsWithIDList(List<Object[]> list, RowMetaInterface rowMeta) throws KettleException
    {
        String[] transList = new String[list.size()];
        for (int i=0;i<list.size();i++)
        {
            long id_transformation = rowMeta.getInteger( list.get(i), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION), -1L); 
            if (id_transformation > 0)
            {
                RowMetaAndData transRow =  getTransformation(id_transformation);
                if (transRow!=null)
                {
                    String transName = transRow.getString(quote(FIELD_TRANSFORMATION_NAME), "<name not found>");
                    long id_directory = transRow.getInteger(quote(FIELD_TRANSFORMATION_ID_DIRECTORY), -1L);
                    RepositoryDirectory dir = directoryTree.findDirectory(id_directory);
                    
                    transList[i]=dir.getPathObjectCombination(transName);
                }
            }            
        }

        return transList;
    }

    public long[] getTransHopIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_TRANS_HOP_ID_TRANS_HOP)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_HOP)+" WHERE "+quote(FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getJobHopIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOB_HOP_ID_JOB_HOP)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_HOP)+" WHERE "+quote(FIELD_JOB_HOP_ID_JOB)+" = " + id_job);
	}

	public long[] getTransDependencyIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_DEPENDENCY_ID_DEPENDENCY)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DEPENDENCY)+" WHERE "+quote(FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getUserIDs() throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_USER_ID_USER)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER));
	}

	public synchronized String[] getUserLogins() throws KettleException
	{
		String loginField = quote(FIELD_USER_LOGIN);
		return getStrings("SELECT "+loginField+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER)+" ORDER BY "+loginField);
	}

	public long[] getPermissionIDs(long id_profile) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_PROFILE_PERMISSION_ID_PERMISSION)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile);
	}

	public long[] getJobEntryIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOBENTRY_ID_JOBENTRY)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY)+" WHERE "+quote(FIELD_JOBENTRY_ID_JOB)+" = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job, long id_jobentry) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+
				" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job + " AND "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY)+" = " + id_jobentry);
	}

	public synchronized String[] getProfiles() throws KettleException
	{
		String nameField = quote(FIELD_PROFILE_NAME);
		return getStrings("SELECT "+nameField+" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE)+" ORDER BY "+nameField);
	}

	public RowMetaAndData getNote(long id_note) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_NOTE), quote(FIELD_NOTE_ID_NOTE), id_note);
	}

	public RowMetaAndData getDatabase(long id_database) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE), quote(FIELD_DATABASE_ID_DATABASE), id_database);
	}

    public RowMetaAndData getDatabaseAttribute(long id_database_attribute) throws KettleException
    {
        return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE), quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), id_database_attribute);
    }

    public Collection<RowMetaAndData> getDatabaseAttributes() throws KettleDatabaseException, KettleValueException
    {
    	List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    	List<Object[]> rows = database.getRows("SELECT * FROM " + databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE),0);
    	for (Object[] row : rows) 
    	{
    		RowMetaAndData rowWithMeta = new RowMetaAndData(database.getReturnRowMeta(), row);
    		long id = rowWithMeta.getInteger(quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), 0);
    		if (id >0) {
    			attrs.add(rowWithMeta);
    		}
    	}
    	return attrs;
    }

    public Collection<RowMetaAndData> getDatabaseAttributes(long id_database) throws KettleDatabaseException, KettleValueException
    {
    	List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    	List<Object[]> rows = database.getRows("SELECT * FROM " + databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE) + " WHERE "+quote(FIELD_DATABASE_ID_DATABASE) +" = "+id_database, 0);
    	for (Object[] row : rows) 
    	{
    		RowMetaAndData rowWithMeta = new RowMetaAndData(database.getReturnRowMeta(), row);
    		long id = rowWithMeta.getInteger(quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), 0);
    		if (id >0) {
    			attrs.add(rowWithMeta);
    		}
    	}
    	return attrs;
    }
    
	public RowMetaAndData getCondition(long id_condition) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CONDITION), quote(FIELD_CONDITION_ID_CONDITION), id_condition);
	}

	public RowMetaAndData getValue(long id_value) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_VALUE), quote(FIELD_VALUE_ID_VALUE), id_value);
	}

	public RowMetaAndData getStep(long id_step) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP), quote(FIELD_STEP_ID_STEP), id_step);
	}

	public RowMetaAndData getStepType(long id_step_type) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE), id_step_type);
	}

	public RowMetaAndData getStepAttribute(long id_step_attribute) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE), quote(FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE), id_step_attribute);
	}

	public RowMetaAndData getStepDatabase(long id_step) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_DATABASE), quote(FIELD_STEP_DATABASE_ID_STEP), id_step);
	}

	public RowMetaAndData getTransHop(long id_trans_hop) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_HOP), quote(FIELD_TRANS_HOP_ID_TRANS_HOP), id_trans_hop);
	}

	public RowMetaAndData getJobHop(long id_job_hop) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_HOP), quote(FIELD_JOB_HOP_ID_JOB_HOP), id_job_hop);
	}

	public RowMetaAndData getTransDependency(long id_dependency) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DEPENDENCY), quote(FIELD_DEPENDENCY_ID_DEPENDENCY), id_dependency);
	}

	public RowMetaAndData getTransformation(long id_transformation) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION), id_transformation);
	}

	public RowMetaAndData getUser(long id_user) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER), quote(FIELD_USER_ID_USER), id_user);
	}

	public RowMetaAndData getProfile(long id_profile) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE), quote(FIELD_PROFILE_ID_PROFILE), id_profile);
	}

	public RowMetaAndData getPermission(long id_permission) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PERMISSION), quote(FIELD_PERMISSION_ID_PERMISSION), id_permission);
	}

	public RowMetaAndData getJob(long id_job) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB), quote(FIELD_JOB_ID_JOB), id_job);
	}

	public RowMetaAndData getJobEntry(long id_jobentry) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY), quote(FIELD_JOBENTRY_ID_JOBENTRY), id_jobentry);
	}

	public RowMetaAndData getJobEntryCopy(long id_jobentry_copy) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY), quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY), id_jobentry_copy);
	}

	public RowMetaAndData getJobEntryType(long id_jobentry_type) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_ID_JOBENTRY_TYPE), id_jobentry_type);
	}

	public RowMetaAndData getDirectory(long id_directory) throws KettleException
	{
		return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DIRECTORY), quote(FIELD_DIRECTORY_ID_DIRECTORY), id_directory);
	}
	
    public RowMetaAndData getPartitionSchema(long id_partition_schema) throws KettleException
    {
        return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA), id_partition_schema);
    }
    
    public RowMetaAndData getPartition(long id_partition) throws KettleException
    {
        return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION), quote(FIELD_PARTITION_ID_PARTITION), id_partition);
    }

    public RowMetaAndData getClusterSchema(long id_cluster_schema) throws KettleException
    {
        return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER), quote(FIELD_CLUSTER_ID_CLUSTER), id_cluster_schema);
    }

    public RowMetaAndData getSlaveServer(long id_slave) throws KettleException
    {
        return getOneRow(databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_SLAVE), quote(FIELD_SLAVE_ID_SLAVE), id_slave);
    }

	private RowMetaAndData getOneRow(String tablename, String keyfield, long id) throws KettleException
	{
		String sql = "SELECT * FROM " + tablename + " WHERE " + keyfield + " = " + id;

		return database.getOneRow(sql);
	}

	// STEP ATTRIBUTES: SAVE

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, String value)
			throws KettleException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, double value)
			throws KettleException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, value, null);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, boolean value) throws KettleException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, String value) throws KettleException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, double value) throws KettleException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, value, null);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, boolean value) throws KettleException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	private long saveStepAttribute(String code, long nr, long id_transformation, long id_step, double value_num, String value_str) throws KettleException
	{
		return insertStepAttribute(id_transformation, id_step, nr, code, value_num, value_str);
	}

  /**
   * GZips and then base64 encodes an array of bytes to a String
   *
   * @param val the array of bytes to convert to a string
   * @return the base64 encoded string
   * @throws IOException in the case there is a Base64 or GZip encoding problem
   */
  public static final String byteArrayToString(byte[] val) throws IOException {
    
    String string;
    if (val == null) {
      string = null;
    } else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = new GZIPOutputStream(baos);
      BufferedOutputStream bos = new BufferedOutputStream(gzos);
      bos.write( val );
      bos.flush();
      bos.close();
      
      string = new String(Base64.encodeBase64(baos.toByteArray()));
    }

    return string;
  }

	// STEP ATTRIBUTES: GET

	public synchronized void setLookupStepAttribute() throws KettleException
	{
		String sql = "SELECT "+quote(FIELD_STEP_ATTRIBUTE_VALUE_STR)+", "+quote(FIELD_STEP_ATTRIBUTE_VALUE_NUM)+
			" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE)+
			" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+" = ?  AND "+quote(FIELD_STEP_ATTRIBUTE_CODE)+" = ?  AND "+quote(FIELD_STEP_ATTRIBUTE_NR)+" = ? ";

		psStepAttributesLookup = database.prepareSQL(sql);
	}
    
    public synchronized void setLookupTransAttribute() throws KettleException
    {
        String sql = "SELECT "+quote(FIELD_TRANS_ATTRIBUTE_VALUE_STR)+", "+quote(FIELD_TRANS_ATTRIBUTE_VALUE_NUM)+
        	" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = ?  AND "+quote(FIELD_TRANS_ATTRIBUTE_CODE)+" = ? AND "+FIELD_TRANS_ATTRIBUTE_NR+" = ? ";

        psTransAttributesLookup = database.prepareSQL(sql);
    }
    
    public synchronized void closeTransAttributeLookupPreparedStatement() throws KettleException
    {
        database.closePreparedStatement(psTransAttributesLookup);
        psTransAttributesLookup = null;
    }

    public synchronized void setLookupJobAttribute() throws KettleException
    {
        String sql = "SELECT "+quote(FIELD_JOB_ATTRIBUTE_VALUE_STR)+", "+quote(FIELD_JOB_ATTRIBUTE_VALUE_NUM)+
        	" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_ATTRIBUTE)+" WHERE "+quote(FIELD_JOB_ATTRIBUTE_ID_JOB)+" = ?  AND "+quote(FIELD_JOB_ATTRIBUTE_CODE)+" = ? AND "+FIELD_JOB_ATTRIBUTE_NR+" = ? ";

        psJobAttributesLookup = database.prepareSQL(sql);
    }
    
    public synchronized void closeJobAttributeLookupPreparedStatement() throws KettleException
    {
        database.closePreparedStatement(psTransAttributesLookup);
        psJobAttributesLookup = null;
    }    

	public synchronized void closeStepAttributeLookupPreparedStatement() throws KettleException
	{
		database.closePreparedStatement(psStepAttributesLookup);
		psStepAttributesLookup = null;
	}
	
	public synchronized void closeStepAttributeInsertPreparedStatement() throws KettleException
	{
	    if (psStepAttributesInsert!=null)
	    {
		    database.emptyAndCommit(psStepAttributesInsert, useBatchProcessing, 1); // batch mode!
			psStepAttributesInsert = null;
	    }
	}

    public synchronized void closeTransAttributeInsertPreparedStatement() throws KettleException
    {
        if (psTransAttributesInsert!=null)
        {
            database.emptyAndCommit(psTransAttributesInsert, useBatchProcessing, 1); // batch mode!
            psTransAttributesInsert = null;
        }
    }


	private RowMetaAndData getStepAttributeRow(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		par.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
		par.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));

		database.setValues(par.getRowMeta(), par.getData(), psStepAttributesLookup);

		Object[] rowData =  database.getLookup(psStepAttributesLookup);
        return new RowMetaAndData(database.getReturnRowMeta(), rowData);
	}

    public RowMetaAndData getTransAttributeRow(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        par.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        par.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));

        database.setValues(par, psTransAttributesLookup);
        Object[] r = database.getLookup(psTransAttributesLookup);
        if (r==null) return null;
        return new RowMetaAndData(database.getReturnRowMeta(), r);
    }
    
    public RowMetaAndData getJobAttributeRow(long id_job, int nr, String code) throws KettleException
    {
        RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
        par.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        par.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));

        database.setValues(par, psJobAttributesLookup);
        Object[] r = database.getLookup(psJobAttributesLookup);
        if (r==null) return null;
        return new RowMetaAndData(database.getReturnRowMeta(), r);
    }    

	public synchronized long getStepAttributeInteger(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return 0;
		return r.getInteger(FIELD_STEP_ATTRIBUTE_VALUE_NUM, 0L);
	}

	public synchronized long findStepAttributeID(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null) return -1L;
		
		return r.getInteger(FIELD_STEP_ATTRIBUTE_ID_STEP, -1L);
	}
	public synchronized String getStepAttributeString(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return null;
		return r.getString(FIELD_STEP_ATTRIBUTE_VALUE_STR, null);
	}

	public boolean getStepAttributeBoolean(long id_step, int nr, String code, boolean def) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		
		if (r == null) return def;
        String v = r.getString(FIELD_STEP_ATTRIBUTE_VALUE_STR, null);
        if (v==null || Const.isEmpty(v)) return def;
		return ValueMeta.convertStringToBoolean(v).booleanValue();
	}

    public boolean getStepAttributeBoolean(long id_step, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
        else                            r = getStepAttributeRow(id_step, nr, code);
        if (r == null)
            return false;
        return ValueMeta.convertStringToBoolean(r.getString(FIELD_STEP_ATTRIBUTE_VALUE_STR, null)).booleanValue();
    }

	public synchronized long getStepAttributeInteger(long id_step, String code) throws KettleException
	{
		return getStepAttributeInteger(id_step, 0, code);
	}

	public synchronized String getStepAttributeString(long id_step, String code) throws KettleException
	{
		return getStepAttributeString(id_step, 0, code);
	}

	public boolean getStepAttributeBoolean(long id_step, String code) throws KettleException
	{
		return getStepAttributeBoolean(id_step, 0, code);
	}

	public synchronized int countNrStepAttributes(long id_step, String code) throws KettleException
	{
	    if (stepAttributesBuffer!=null) // see if we can do this in memory...
	    {
	        int nr = searchNrStepAttributes(id_step, code);
            return nr;
	    }
	    else
	    {
			String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+" = ? AND "+quote(FIELD_STEP_ATTRIBUTE_CODE)+" = ?";
			RowMetaAndData table = new RowMetaAndData();
			table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
			table.addValue(new ValueMeta(FIELD_STEP_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
			RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
			if (r == null || r.getData()==null) return 0;
            return (int) r.getInteger(0, 0L);
	    }
	}
    
    // TRANS ATTRIBUTES: get
    
    public synchronized String getTransAttributeString(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return null;
        return r.getString(FIELD_TRANS_ATTRIBUTE_VALUE_STR, null);
    }

    public synchronized boolean getTransAttributeBoolean(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return false;
        return r.getBoolean(FIELD_TRANS_ATTRIBUTE_VALUE_STR, false);
    }

    public synchronized double getTransAttributeNumber(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return 0.0;
        return r.getNumber(FIELD_TRANS_ATTRIBUTE_VALUE_NUM, 0.0);
    }

    public synchronized long getTransAttributeInteger(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return 0L;
        return r.getInteger(FIELD_TRANS_ATTRIBUTE_VALUE_NUM, 0L);
    }
    
    public synchronized int countNrTransAttributes(long id_transformation, String code) throws KettleException
    {
        String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = ? AND "+quote(FIELD_TRANS_ATTRIBUTE_CODE)+" = ?";
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
        if (r == null|| r.getData()==null)
            return 0;
        
        return (int) r.getInteger(0, 0L);
    }

    public synchronized List<Object[]> getTransAttributes(long id_transformation, String code, long nr) throws KettleException
    {
        String sql = "SELECT *"+
        	" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_ATTRIBUTE)+
        	" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = ? AND "+quote(FIELD_TRANS_ATTRIBUTE_CODE)+" = ? AND "+quote(FIELD_TRANS_ATTRIBUTE_NR)+" = ?"+
        	" ORDER BY "+quote(FIELD_TRANS_ATTRIBUTE_VALUE_NUM);
        
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        
        return database.getRows(sql, 0);
    }

    // JOB ATTRIBUTES: get
    
    public synchronized String getJobAttributeString(long id_job, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getJobAttributeRow(id_job, nr, code);
        if (r == null)
            return null;
        return r.getString(FIELD_JOB_ATTRIBUTE_VALUE_STR, null);
    }

    public synchronized boolean getJobAttributeBoolean(long id_job, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getJobAttributeRow(id_job, nr, code);
        if (r == null)
            return false;
        return r.getBoolean(FIELD_JOB_ATTRIBUTE_VALUE_STR, false);
    }

    public synchronized double getJobAttributeNumber(long id_job, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getJobAttributeRow(id_job, nr, code);
        if (r == null)
            return 0.0;
        return r.getNumber(FIELD_JOB_ATTRIBUTE_VALUE_NUM, 0.0);
    }

    public synchronized long getJobAttributeInteger(long id_job, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getJobAttributeRow(id_job, nr, code);
        if (r == null)
            return 0L;
        return r.getInteger(FIELD_JOB_ATTRIBUTE_VALUE_NUM, 0L);
    }
    
    public synchronized int countNrJobAttributes(long id_job, String code) throws KettleException
    {
        String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_ATTRIBUTE)+" WHERE "+quote(FIELD_JOB_ATTRIBUTE_ID_JOB)+" = ? AND "+quote(FIELD_JOB_ATTRIBUTE_CODE)+" = ?";
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
        if (r == null|| r.getData()==null)
            return 0;
        
        return (int) r.getInteger(0, 0L);
    }

    public synchronized List<Object[]> getJobAttributes(long id_job, String code, long nr) throws KettleException
    {
        String sql = "SELECT *"+
        	" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_ATTRIBUTE)+
        	" WHERE "+quote(FIELD_JOB_ATTRIBUTE_ID_JOB)+" = ? AND "+quote(FIELD_JOB_ATTRIBUTE_CODE)+" = ? AND "+quote(FIELD_JOB_ATTRIBUTE_NR)+" = ?"+
        	" ORDER BY "+quote(FIELD_JOB_ATTRIBUTE_VALUE_NUM);
        
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(FIELD_JOB_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        
        return database.getRows(sql, 0);
    }    
    
	// JOBENTRY ATTRIBUTES: SAVE

	// WANTED: throw extra exceptions to locate storage problems (strings too long etc)
	//
	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, String value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, double value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, value, null);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, boolean value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, String value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, value, null);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, boolean value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	private long saveJobEntryAttribute(String code, long nr, long id_job, long id_jobentry, double value_num,
			String value_str) throws KettleException
	{
		return insertJobEntryAttribute(id_job, id_jobentry, nr, code, value_num, value_str);
	}

	// JOBENTRY ATTRIBUTES: GET

	public synchronized void setLookupJobEntryAttribute() throws KettleException
	{
		String sql = "SELECT "+quote(FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR)+", "+quote(FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM)+
		" FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_ATTRIBUTE)+
		" WHERE "+quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY)+" = ? AND "+quote(FIELD_JOBENTRY_ATTRIBUTE_CODE)+" = ?  AND "+quote(FIELD_JOBENTRY_ATTRIBUTE_NR)+" = ? ";

		pstmt_entry_attributes = database.prepareSQL(sql);
	}

	public synchronized void closeLookupJobEntryAttribute() throws KettleException
	{
		database.closePreparedStatement(pstmt_entry_attributes);
        pstmt_entry_attributes = null;
	}

	private RowMetaAndData getJobEntryAttributeRow(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		par.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
		par.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));

		database.setValues(par.getRowMeta(), par.getData(), pstmt_entry_attributes);
		Object[] rowData = database.getLookup(pstmt_entry_attributes);
        return new RowMetaAndData(database.getReturnRowMeta(), rowData);
	}

	public synchronized long getJobEntryAttributeInteger(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return 0;
		return r.getInteger(FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM, 0L);
	}

	public double getJobEntryAttributeNumber(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return 0.0;
		return r.getNumber(FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM, 0.0);
	}

	public synchronized String getJobEntryAttributeString(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return null;
		return r.getString(FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR, null);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code) throws KettleException
	{
		return getJobEntryAttributeBoolean(id_jobentry, nr, code, false);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code, boolean def) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null) return def;
        String v = r.getString(FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR, null);
        if (v==null || Const.isEmpty(v)) return def;
        return ValueMeta.convertStringToBoolean(v).booleanValue();
	}

	public double getJobEntryAttributeNumber(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeNumber(id_jobentry, 0, code);
	}

	public synchronized long getJobEntryAttributeInteger(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeInteger(id_jobentry, 0, code);
	}

	public synchronized String getJobEntryAttributeString(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeString(id_jobentry, 0, code);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code, false);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code, boolean def) throws KettleException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code, def);
	}

	public synchronized int countNrJobEntryAttributes(long id_jobentry, String code) throws KettleException
	{
		String sql = "SELECT COUNT(*) FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_ATTRIBUTE)+" WHERE "+quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY)+" = ? AND "+quote(FIELD_JOBENTRY_ATTRIBUTE_CODE)+" = ?";
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
		RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
		if (r == null || r.getData()==null) return 0;
		return (int) r.getInteger(0, 0L);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// DELETE DATA IN REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized void delSteps(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP)+" WHERE "+quote(FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delCondition(long id_condition) throws KettleException
	{
		boolean ok = true;
		long ids[] = getSubConditionIDs(id_condition);
		if (ids.length > 0)
		{
			// Delete the sub-conditions...
			for (int i = 0; i < ids.length && ok; i++)
			{
				delCondition(ids[i]);
			}

			// Then delete the main condition
			delCondition(id_condition);
		}
		else
		{
			String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CONDITION)+" WHERE "+quote(FIELD_CONDITION_ID_CONDITION)+" = " + id_condition;
			database.execStatement(sql);
		}
	}

	public synchronized void delStepConditions(long id_transformation) throws KettleException
	{
		long ids[] = getConditionIDs(id_transformation);
		for (int i = 0; i < ids.length; i++)
		{
			delCondition(ids[i]);
		}
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	/**
	 * Delete the relationships between the transformation/steps and the databases.
	 * @param id_transformation the transformation for which we want to delete the databases.
	 * @throws KettleException in case something unexpected happens.
	 */
	public synchronized void delStepDatabases(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobEntries(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY)+" WHERE "+quote(FIELD_JOBENTRY_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delJobEntryCopies(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDependencies(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DEPENDENCY)+" WHERE "+quote(FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delStepAttributes(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

    public synchronized void delTransAttributes(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delJobAttributes(long id_job) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_ATTRIBUTE)+" WHERE "+quote(FIELD_JOB_ATTRIBUTE_ID_JOB)+" = " + id_job;
        database.execStatement(sql);
    }   
    
    public synchronized void delPartitionSchemas(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delPartitions(long id_partition_schema) throws KettleException
    {
        // First see if the partition is used by a step, transformation etc.
        // 
        database.execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION)+" WHERE "+quote(FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }
    
    public synchronized void delClusterSlaves(long id_cluster) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster;
        database.execStatement(sql);
    }
    
    public synchronized void delTransformationClusters(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delTransformationSlaves(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_SLAVE)+" WHERE "+quote(FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }


	public synchronized void delJobEntryAttributes(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOBENTRY_ATTRIBUTE)+" WHERE "+quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransHops(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_HOP)+" WHERE "+quote(FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobHops(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_HOP)+" WHERE "+quote(FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransNotes(long id_transformation) throws KettleException
	{
		long ids[] = getTransNoteIDs(id_transformation);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_NOTE)+" WHERE "+quote(FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			database.execStatement(sql);
		}

		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_NOTE)+" WHERE "+quote(FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobNotes(long id_job) throws KettleException
	{
		long ids[] = getJobNoteIDs(id_job);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_NOTE)+" WHERE "+quote(FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			database.execStatement(sql);
		}

		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB_NOTE)+" WHERE "+quote(FIELD_JOB_NOTE_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTrans(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION)+" WHERE "+quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJob(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" WHERE "+quote(FIELD_JOB_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDatabase(long id_database) throws KettleException
	{
		// First, see if the database connection is still used by other connections...
		// If so, generate an error!!
		// We look in table R_STEP_DATABASE to see if there are any steps using this database.
		//
		String[] transList = getTransformationsUsingDatabase(id_database);
        
		// TODO: add check for jobs too.
		// TODO: add R_JOBENTRY_DATABASE table & lookups.
		
		if (transList.length==0)
		{
			String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE)+" WHERE "+quote(FIELD_DATABASE_ID_DATABASE)+" = " + id_database;
			database.execStatement(sql);
		}
		else
		{
			
			String message = "Database used by the following transformations:"+Const.CR;
			for (int i = 0; i < transList.length; i++)
			{
				message+="	"+transList[i]+Const.CR;
			}
			KettleDependencyException e = new KettleDependencyException(message);
			throw new KettleDependencyException("This database is still in use by one or more transformations ("+transList.length+" references)", e);
		}
	}
    
    public synchronized void delDatabaseAttributes(long id_database) throws KettleException
    {
        String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = " + id_database;
        database.execStatement(sql);
    }

	public synchronized void delTransStepCondition(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delValue(long id_value) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_VALUE)+" WHERE "+quote(FIELD_VALUE_ID_VALUE)+" = " + id_value;
		database.execStatement(sql);
	}

	public synchronized void delUser(long id_user) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER)+" WHERE "+quote(FIELD_USER_ID_USER)+" = " + id_user;
		database.execStatement(sql);
	}

	public synchronized void delProfile(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE)+" WHERE "+quote(FIELD_PROFILE_ID_PROFILE)+" = " + id_profile;
		database.execStatement(sql);
	}

	public synchronized void delProfilePermissions(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
		database.execStatement(sql);
	}
    
    public synchronized void delSlave(long id_slave) throws KettleException
    {
        // First, see if the slave is still used by other objects...
        // If so, generate an error!!
        // We look in table R_TRANS_SLAVE to see if there are any transformations using this slave.
        // We obviously also look in table R_CLUSTER_SLAVE to see if there are any clusters that use this slave.
    	//
        String[] transList = getTransformationsUsingSlave(id_slave);
        String[] clustList = getClustersUsingSlave(id_slave);

        if (transList.length==0 && clustList.length==0)
        {
            database.execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_SLAVE)+" WHERE "+quote(FIELD_SLAVE_ID_SLAVE)+" = " + id_slave);
            database.execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANS_SLAVE)+" WHERE "+quote(FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            if (transList.length>0)
            {
                message.append("Slave used by the following transformations:").append(Const.CR);
                for (int i = 0; i < transList.length; i++)
                {
                    message.append("  ").append(transList[i]).append(Const.CR);
                }
                message.append(Const.CR);
            }
            if (clustList.length>0)
            {
                message.append("Slave used by the following cluster schemas:").append(Const.CR);
                for (int i = 0; i < clustList.length; i++)
                {
                    message.append("  ").append(clustList[i]).append(Const.CR);
                }
            }
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This slave server is still in use by one or more transformations ("+transList.length+") or cluster schemas ("+clustList.length+") :", e);
        }
    }
   
    public synchronized void delPartitionSchema(long id_partition_schema) throws KettleException
    {
        // First, see if the schema is still used by other objects...
        // If so, generate an error!!
        //
        // We look in table R_TRANS_PARTITION_SCHEMA to see if there are any transformations using this schema.
        String[] transList = getTransformationsUsingPartitionSchema(id_partition_schema);

        if (transList.length==0)
        {
            database.execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION)+" WHERE "+quote(FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
            database.execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            message.append("The partition schema is used by the following transformations:").append(Const.CR);
            for (int i = 0; i < transList.length; i++)
            {
                message.append("  ").append(transList[i]).append(Const.CR);
            }
            message.append(Const.CR);
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This partition schema is still in use by one or more transformations ("+transList.length+") :", e);
        }
    }
    
    public synchronized void delClusterSchema(long id_cluster) throws KettleException
    {
        // First, see if the schema is still used by other objects...
        // If so, generate an error!!
        //
        // We look in table R_TRANS_CLUSTER to see if there are any transformations using this schema.
        String[] transList = getTransformationsUsingCluster(id_cluster);

        if (transList.length==0)
        {
            database.execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_CLUSTER)+" WHERE "+quote(FIELD_CLUSTER_ID_CLUSTER)+" = " + id_cluster);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            message.append("The cluster schema is used by the following transformations:").append(Const.CR);
            for (int i = 0; i < transList.length; i++)
            {
                message.append("  ").append(transList[i]).append(Const.CR);
            }
            message.append(Const.CR);
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This cluster schema is still in use by one or more transformations ("+transList.length+") :", e);
        }
    }


	public synchronized void delAllFromTrans(long id_transformation) throws KettleException
	{
		delTransNotes(id_transformation);
		delStepAttributes(id_transformation);
		delSteps(id_transformation);
		delStepConditions(id_transformation);
		delStepDatabases(id_transformation);
		delTransHops(id_transformation);
		delDependencies(id_transformation);
        delTransAttributes(id_transformation);
        delPartitionSchemas(id_transformation);
        delTransformationClusters(id_transformation);
        delTransformationSlaves(id_transformation);
		delTrans(id_transformation);
	}

	public synchronized void renameTransformation(long id_transformation, String newname) throws KettleException
	{
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_TRANSFORMATION)+" SET "+quote(FIELD_TRANSFORMATION_NAME)+" = ? WHERE "+quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_NAME,  ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_TRANSFORMATION,  ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameUser(long id_user, String newname) throws KettleException
	{
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_USER)+" SET "+quote(FIELD_USER_NAME)+" = ? WHERE "+quote(FIELD_USER_ID_USER)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER), new Long(id_user));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameProfile(long id_profile, String newname) throws KettleException
	{
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_PROFILE)+" SET "+quote(FIELD_PROFILE_NAME)+" = ? WHERE "+quote(FIELD_PROFILE_ID_PROFILE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameDatabase(long id_database, String newname) throws KettleException
	{
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_DATABASE)+" SET "+quote(FIELD_DATABASE_NAME)+" = ? WHERE "+quote(FIELD_DATABASE_ID_DATABASE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void delAllFromJob(long id_job) throws KettleException
	{
		// log.logBasic(toString(), "Deleting info in repository on ID_JOB: "+id_job);

		delJobNotes(id_job);
		delJobAttributes(id_job);
		delJobEntryAttributes(id_job);
		delJobEntries(id_job);
		delJobEntryCopies(id_job);
		delJobHops(id_job);
		delJob(id_job);

		// log.logBasic(toString(), "All deleted on job with ID_JOB: "+id_job);
	}

	public synchronized void renameJob(long id_job, String newname) throws KettleException
	{
		String sql = "UPDATE "+databaseMeta.getQuotedSchemaTableCombination(null, TABLE_R_JOB)+" SET "+quote(FIELD_JOB_NAME)+" = ? WHERE "+quote(FIELD_JOB_ID_JOB)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}


	public boolean dropRepositorySchema() throws KettleException
	{
		// Make sure we close shop before dropping everything. 
		// Some DB's can't handle the drop otherwise.
		//
		closeStepAttributeInsertPreparedStatement();
		closeLookupJobEntryAttribute();
		
		for (int i = 0; i < repositoryTableNames.length; i++)
		{
			try
			{
				database.execStatement("DROP TABLE " + databaseMeta.getQuotedSchemaTableCombination(null, repositoryTableNames[i]));
                if (log.isDetailed()) log.logDetailed(toString(), "dropped table "+repositoryTableNames[i]);
			}
			catch (KettleException dbe)
			{
                if (log.isDetailed()) log.logDetailed(toString(), "Unable to drop table: " + repositoryTableNames[i]);
			}
		}
        log.logBasic(toString(), "Dropped all "+repositoryTableNames.length+" repository tables.");
        
        // perform commit, for some DB's drop is not auto commit.
        if (!database.isAutoCommit()) database.commit(); 
        
		return true;
	}

	/**
	 * Update the list in R_STEP_TYPE using the StepLoader StepPlugin entries
	 * 
	 * @return the SQL statements executed
	 * @throws KettleException if the update didn't go as planned.
	 */
	public void updateStepTypes() throws KettleException
	{
		creationHelper.updateStepTypes(new ArrayList<String>(), false, false);
	}
	
	/**
	 * Update the list in R_JOBENTRY_TYPE 
	 * 
	 * @exception KettleException if something went wrong during the update.
	 */
	public void updateJobEntryTypes() throws KettleException
	{
		creationHelper.updateJobEntryTypes(new ArrayList<String>(), false, false);
	}


	public synchronized String toString()
	{
		if (repinfo == null)
			return getClass().getName();
		return repinfo.getName();
	}

	/**
	 * @return Returns the database.
	 */
	public Database getDatabase()
	{
		return database;
	}
	
	/**
	 * @param database The database to set.
	 */
	public void setDatabase(Database database)
	{
		this.database = database;
        this.databaseMeta = database.getDatabaseMeta();
	}

    /**
     * @return Returns the directoryTree.
     */
    public RepositoryDirectory getDirectoryTree()
    {
        return directoryTree;
    }

    /**
     * @param directoryTree The directoryTree to set.
     */
    public synchronized void setDirectoryTree(RepositoryDirectory directoryTree)
    {
        this.directoryTree = directoryTree;
    }
    
    public synchronized void lockRepository() throws KettleException
    {
        if (database.getDatabaseMeta().needsToLockAllTables())
        {
            database.lockTables(repositoryTableNames);
        }
        else
        {
            database.lockTables( new String[] { TABLE_R_REPOSITORY_LOG, } );
        }
    }
    
    public synchronized void unlockRepository() throws KettleException
    {
        if (database.getDatabaseMeta().needsToLockAllTables())
        {
            database.unlockTables(repositoryTableNames);
        }
        else
        {
            database.unlockTables( new String[] { TABLE_R_REPOSITORY_LOG, } );
        }
    }
    
    public synchronized void exportAllObjects(ProgressMonitorListener monitor, String xmlFilename, RepositoryDirectory root, String exportType) throws KettleException
    {
    	OutputStream os = null;
    	OutputStreamWriter writer = null;
    	try
        {
            os = new BufferedOutputStream(KettleVFS.getOutputStream(xmlFilename, false));
            writer = new OutputStreamWriter(os, Const.XML_ENCODING);
            
	        if (monitor!=null) monitor.beginTask("Exporting the repository to XML...", 3);
	        
	        root = ((null == root) ? getDirectoryTree() : root);
	        
	        writer.write(XMLHandler.getXMLHeader()); 
	        writer.write("<repository>"+Const.CR+Const.CR);
	
	        if(exportType.equals("all"))
	        {
		        // Dump the directories first, otherwise empty directories don't get exported...
	        	writer.write("<directories>"+Const.CR);
	        	exportDirectories(monitor, root, writer);
	        	writer.write("</directories>"+Const.CR);
	        }
	        
	        if(exportType.equals("all") || exportType.equals("trans"))
	        {
		        // Dump the transformations...
	        	writer.write("<transformations>"+Const.CR);
	        	exportTransformations(monitor, root, writer);
	        	writer.write("</transformations>"+Const.CR);
	        }
	
	        if(exportType.equals("all") || exportType.equals("jobs"))
	        {
		        // Now dump the jobs...
	        	writer.write("<jobs>"+Const.CR);
		        exportJobs(monitor, root, writer);
		        writer.write("</jobs>"+Const.CR);
	        }
	
	        writer.write("</repository>"+Const.CR+Const.CR);
	        
	        if (monitor!=null) monitor.worked(1);

            if (monitor!=null) monitor.subTask("Saving XML to file ["+xmlFilename+"]");

            if (monitor!=null) monitor.worked(1);

        }
        catch(IOException e)
        {
            System.out.println("Couldn't create file ["+xmlFilename+"]");
        }
        finally
        {
        	try {
	        	if (writer!=null) writer.close();
	        	if (os!=null) os.close();
        	}
        	catch(Exception e) {
                System.out.println("Exception closing XML file writer to ["+xmlFilename+"]");
        	}
        }
        
        if (monitor!=null) monitor.done();
    }

    private void exportJobs(ProgressMonitorListener monitor, RepositoryDirectory dirTree, OutputStreamWriter writer) throws KettleException
    {
    	try {
	        // Loop over all the directory id's
	        long dirids[] = dirTree.getDirectoryIDs();
	        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
	 
	        if (monitor!=null) monitor.subTask("Exporting the jobs...");
	        
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));d++)
	        {
	            RepositoryDirectory repdir = dirTree.findDirectory(dirids[d]);
	
	            String jobs[]  = getJobNames(dirids[d]);
	            for (int i=0;i<jobs.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
	            {
	                try
	                {
	                    JobMeta ji = new JobMeta(log, this, jobs[i], repdir);
	                    System.out.println("Loading/Exporting job ["+repdir.getPath()+" : "+jobs[i]+"]");
	                    if (monitor!=null) monitor.subTask("Exporting job ["+jobs[i]+"]");
	                    
	                    writer.write(ji.getXML()+Const.CR);
	                }
	                catch(KettleException ke)
	                {
	                    log.logError(toString(), "An error occurred reading job ["+jobs[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
	                    log.logError(toString(), "Job ["+jobs[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
	                }
	            }
	        }
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository jobs", e);
    	}
    }
    
    private void exportTransformations(ProgressMonitorListener monitor, RepositoryDirectory dirTree, OutputStreamWriter writer) throws KettleException
    {
    	try {
	        if (monitor!=null) monitor.subTask("Exporting the transformations...");
	
	        // Loop over all the directory id's
	        long dirids[] = dirTree.getDirectoryIDs();
	        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
	        
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()) );d++)
	        {
	            RepositoryDirectory repdir = dirTree.findDirectory(dirids[d]);
	
	            System.out.println("Directory ID #"+d+" : "+dirids[d]+" : "+repdir);
	
	            String trans[] = getTransformationNames(dirids[d]);
	            for (int i=0;i<trans.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
	            {
	                try
	                {
	                    TransMeta ti = new TransMeta(this, trans[i], repdir);
	                    System.out.println("Loading/Exporting transformation ["+repdir.getPath()+" : "+trans[i]+"]  ("+ti.getDirectory().getPath()+")");
	                    if (monitor!=null) monitor.subTask("Exporting transformation ["+trans[i]+"]");
	                    
	                    writer.write(ti.getXML()+Const.CR);
	                }
	                catch(KettleException ke)
	                {
	                    log.logError(toString(), "An error occurred reading transformation ["+trans[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
	                    log.logError(toString(), "Transformation ["+trans[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
	                }
	            }
	        }
	        if (monitor!=null) monitor.worked(1);
	        
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository transformations", e);
    	}
    }
    private void exportDirectories(ProgressMonitorListener monitor, RepositoryDirectory dirTree, OutputStreamWriter writer) throws KettleException
    {
    	try {
	        if (monitor!=null) monitor.subTask("Exporting the directories...");
	
	        // Loop over all the directory id's
	        long dirids[] = dirTree.getDirectoryIDs();
	        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
	        
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()) );d++)
	        {
	            RepositoryDirectory repdir = dirTree.findDirectory(dirids[d]);
	
	            System.out.println("Directory ID #"+d+" : "+dirids[d]+" : "+repdir);
	            if (!repdir.isRoot())
	            	writer.write("    " + XMLHandler.addTagValue("directory", repdir.getPath()));
	        }
	        if (monitor!=null) monitor.worked(1);
	        
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository directories", e);
    	}
    }

    /**
     * @return the current repository
     * @deprecated this is not thread safe
     */
    public synchronized static Repository getCurrentRepository()
    {
        return currentRepository;
    }
    /**
     * @param currentRepository the current repository
     * @deprecated this is not thread safe
     */

    public synchronized static void setCurrentRepository(Repository currentRepository)
    {
        Repository.currentRepository = currentRepository;
    }

    /**
     * @return a list of all the databases in the repository.
     * @throws KettleException
     */
    public List<DatabaseMeta> getDatabases() throws KettleException
    {
        List<DatabaseMeta> list = new ArrayList<DatabaseMeta>();
        long[] databaseIDs = getDatabaseIDs();
        for (int i=0;i<databaseIDs.length;i++)
        {
            DatabaseMeta databaseMeta = RepositoryUtil.loadDatabaseMeta(this, databaseIDs[i]);
            list.add(databaseMeta);
        }
            
        return list;
    }
    
    /**
     * @return a list of all the slave servers in the repository.
     * @throws KettleException
     */
    public List<SlaveServer> getSlaveServers() throws KettleException
    {
        List<SlaveServer> list = new ArrayList<SlaveServer>();
        long[] slaveIDs = getSlaveIDs();
        for (int i=0;i<slaveIDs.length;i++)
        {
            SlaveServer slaveServer = new SlaveServer(this, slaveIDs[i]);
            list.add(slaveServer);
        }
            
        return list;
    }

	/**
	 * @return the stepAttributesRowMeta
	 */
	public RowMetaInterface getStepAttributesRowMeta() {
		return stepAttributesRowMeta;
	}

	/**
	 * @param stepAttributesRowMeta the stepAttributesRowMeta to set
	 */
	public void setStepAttributesRowMeta(RowMetaInterface stepAttributesRowMeta) {
		this.stepAttributesRowMeta = stepAttributesRowMeta;
	}
	
	public String quote(String identifier) {
		return databaseMeta.quoteField(identifier);
	}

	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}
	
	/**
	 * Read all the databases defined in the repository
	 * @return a list of all the databases defined in the repository
	 * @throws KettleException
	 */
	public List<DatabaseMeta> readDatabases() throws KettleException
	{
		List<DatabaseMeta> databases = new ArrayList<DatabaseMeta>();
		long[] ids = getDatabaseIDs();
		for (int i=0;i<ids.length;i++) 
		{
			DatabaseMeta databaseMeta = RepositoryUtil.loadDatabaseMeta(this, ids[i]);
			databases.add(databaseMeta);
		}
		return databases;
	}

	/**
	 * @return the useBatchProcessing
	 */
	public boolean isUseBatchProcessing() {
		return useBatchProcessing;
	}

	/**
	 * @param useBatchProcessing the useBatchProcessing to set
	 */
	public void setUseBatchProcessing(boolean useBatchProcessing) {
		this.useBatchProcessing = useBatchProcessing;
	}
	
	/**
	 * Set this directory during import to signal that job entries like Trans and Job need to point to job entries relative to this directory.
	 * 
	 * @param importBaseDirectory the base import directory, selected by the user
	 */
	public void setImportBaseDirectory(RepositoryDirectory importBaseDirectory) {
		this.importBaseDirectory = importBaseDirectory;
	}
	
	/**
	 * The directory set during import to signal that job entries like Trans and Job need to point to job entries relative to this directory
	 * 
	 * @return the base import directory, selected by the user
	 */
	public RepositoryDirectory getImportBaseDirectory() {
		return importBaseDirectory;
	}

	/**
	 * @param userinfo the UserInfo object to set
	 */
	public void setUserInfo(UserInfo userinfo) {
		this.userinfo = userinfo;
	}

	/**
     * Create or upgrade repository tables & fields, populate lookup tables, ...
     * 
     * @param monitor The progress monitor to use, or null if no monitor is present.
     * @param upgrade True if you want to upgrade the repository, false if you want to create it.
     * @param statements the list of statements to populate
     * @param dryrun true if we don't actually execute the statements
     * 
     * @throws KettleException in case something goes wrong!
     */
	public void createRepositorySchema(ProgressMonitorListener monitor, boolean upgrade, List<String> statements, boolean dryRun) throws KettleException {
		creationHelper.createRepositorySchema(monitor, upgrade, statements, dryRun);
	}
	
	/**
	 * Count the number of parameters of a transformation.
	 * 
	 * @param id_transformation transformation id
	 * @return the number of parameters of a transformation
	 * 
	 * @throws KettleException Upon any error.
	 */
	public int countTransParameter(long id_transformation) throws KettleException  {
		return countNrTransAttributes(id_transformation, TRANS_ATTRIBUTE_PARAM_KEY);
	}
	
	/**
	 * Get a transformation parameter key. You can count the number of parameters up front.
	 * 
	 * @param id_transformation transformation id
	 * @param nr number of the parameter 
	 * @return they key/name of specified parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getTransParameterKey(long id_transformation, int nr) throws KettleException  {
 		return getTransAttributeString(id_transformation, nr, TRANS_ATTRIBUTE_PARAM_KEY);		
	}

	/**
	 * Get a transformation parameter default. You can count the number of parameters up front. 
	 * 
	 * @param id_transformation transformation id
	 * @param nr number of the parameter
	 * @return the default value of the parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getTransParameterDefault(long id_transformation, int nr) throws KettleException  {
 		return getTransAttributeString(id_transformation, nr, TRANS_ATTRIBUTE_PARAM_DEFAULT);		
	}	
	
	/**
	 * Get a transformation parameter description. You can count the number of parameters up front. 
	 * 
	 * @param id_transformation transformation id
	 * @param nr number of the parameter
	 * @return the description of the parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getTransParameterDescription(long id_transformation, int nr) throws KettleException  {
 		return getTransAttributeString(id_transformation, nr, TRANS_ATTRIBUTE_PARAM_DESCRIPTION);		
	}
	
	/**
	 * Insert a parameter for a transformation in the repository.
	 * 
	 * @param id_transformation transformation id
	 * @param nr number of the parameter to insert
	 * @param key key to insert
	 * @param defValue default value
	 * @param description description to insert
	 * 
	 * @throws KettleException Upon any error.
	 */
	public void insertTransParameter(long id_transformation, long nr, String key, String defValue, String description) throws KettleException {
	    insertTransAttribute(id_transformation, nr, TRANS_ATTRIBUTE_PARAM_KEY, 0, key != null ? key : "");		
	    insertTransAttribute(id_transformation, nr, TRANS_ATTRIBUTE_PARAM_DEFAULT, 0, defValue != null ? defValue : "");
	    insertTransAttribute(id_transformation, nr, TRANS_ATTRIBUTE_PARAM_DESCRIPTION, 0, description != null ? description : "");
	}
	
	/**
	 * Count the number of parameters of a job.
	 * 
	 * @param id_job job id
	 * @return the number of parameters of a job
	 * 
	 * @throws KettleException Upon any error.
	 */
	public int countJobParameter(long id_job) throws KettleException  {
		return countNrJobAttributes(id_job, JOB_ATTRIBUTE_PARAM_KEY);
	}
	
	/**
	 * Get a job parameter key. You can count the number of parameters up front.
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter 
	 * @return they key/name of specified parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getJobParameterKey(long id_job, int nr) throws KettleException  {
 		return getJobAttributeString(id_job, nr, JOB_ATTRIBUTE_PARAM_KEY);		
	}

	/**
	 * Get a job parameter default. You can count the number of parameters up front. 
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter
	 * @return the default value of the parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getJobParameterDefault(long id_job, int nr) throws KettleException  {
 		return getJobAttributeString(id_job, nr, JOB_ATTRIBUTE_PARAM_DEFAULT);		
	}	
	
	/**
	 * Get a job parameter description. You can count the number of parameters up front. 
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter
	 * @return the description of the parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getJobParameterDescription(long id_job, int nr) throws KettleException  {
 		return getJobAttributeString(id_job, nr, JOB_ATTRIBUTE_PARAM_DESCRIPTION);		
	}
	
	/**
	 * Insert a parameter for a job in the repository.
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter to insert
	 * @param key key to insert
	 * @param defValue default value for key
	 * @param description description to insert
	 * 
	 * @throws KettleException Upon any error.
	 */
	public void insertJobParameter(long id_job, long nr, String key, String defValue, String description) throws KettleException {
	    insertJobAttribute(id_job, nr, JOB_ATTRIBUTE_PARAM_KEY, 0, key != null ? key : "");
	    insertJobAttribute(id_job, nr, JOB_ATTRIBUTE_PARAM_DEFAULT, 0, defValue != null ? defValue : "");
	    insertJobAttribute(id_job, nr, JOB_ATTRIBUTE_PARAM_DESCRIPTION, 0, description != null ? description : "");
	}	
}