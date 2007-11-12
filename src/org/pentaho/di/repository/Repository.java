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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.Counters;
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
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
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
	public static final String FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION = "TRANSFORMATION";
	public static final String FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA = "ID_PARTITION_SCHEMA";

	public static final String TABLE_R_CLUSTER                = "R_CLUSTER";
	public static final String FIELD_CLUSTER_ID_CLUSTER = "ID_CLUSTER";
	public static final String FIELD_CLUSTER_NAME = "NAME";
	public static final String FIELD_CLUSTER_BASE_PORT = "BASE_PORT";
	public static final String FIELD_CLUSTER_SOCKETS_BUFFER_SIZE = "SOCKETS_BUFFER_SIZE";
	public static final String FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL = "SOCKETS_FLUSH_INTERVAL";
	public static final String FIELD_CLUSTER_SOCKETS_COMPRESSED = "SOCKETS_COMPRESSED";

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


    private final String repositoryTableNames[] = new String[] 
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
    public static final int REQUIRED_MINOR_VERSION = 0;
    
	private RepositoryMeta		repinfo;
	public  UserInfo			userinfo;
	private RepositoryDirectory	directoryTree;
	private Database			database;

	public  LogWriter			log;

	private String				locksource;

	private PreparedStatement	psStepAttributesLookup;
	private PreparedStatement	psStepAttributesInsert;
    private PreparedStatement   psTransAttributesLookup;
    private PreparedStatement   psTransAttributesInsert;
	
	private List<Object[]>           stepAttributesBuffer;
	private RowMetaInterface         stepAttributesRowMeta;
	
	private PreparedStatement	pstmt_entry_attributes;

	private StepLoader			steploader;

	private int					majorVersion;
	private int					minorVersion;
    private DatabaseMeta        databaseMeta;
    
	private boolean             useBatchProcessing;

    /** The maximum length of a text field in a Kettle repository : 2.000.000 is enough for everyone ;-) */ 
    private static final int REP_STRING_LENGTH      = 2000000;
    private static final int REP_STRING_CODE_LENGTH =     255;
    
	private static final String TRANS_ATTRIBUTE_ID_STEP_REJECTED = "ID_STEP_REJECTED";
	private static final String TRANS_ATTRIBUTE_UNIQUE_CONNECTIONS = "UNIQUE_CONNECTIONS";
	private static final String TRANS_ATTRIBUTE_FEEDBACK_SHOWN = "FEEDBACK_SHOWN";
	private static final String TRANS_ATTRIBUTE_FEEDBACK_SIZE = "FEEDBACK_SIZE";
	private static final String TRANS_ATTRIBUTE_USING_THREAD_PRIORITIES = "USING_THREAD_PRIORITIES";
	private static final String TRANS_ATTRIBUTE_SHARED_FILE = "SHARED_FILE";
		
    private static Repository currentRepository;

	public Repository(LogWriter log, RepositoryMeta repinfo, UserInfo userinfo)
	{
		this.repinfo = repinfo;
		this.log = log;
		this.userinfo = userinfo;

		steploader = StepLoader.getInstance();
		
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
        try
        {
            lastUpgrade = database.getOneRow("SELECT "+quote(FIELD_VERSION_MAJOR_VERSION)+", "+quote(FIELD_VERSION_MINOR_VERSION)+", "+quote(FIELD_VERSION_UPGRADE_DATE)+" FROM "+quote(TABLE_R_VERSION)+" ORDER BY "+quote(FIELD_VERSION_UPGRADE_DATE)+" DESC");
        }
        catch(Exception e)
        {
            // If we can't retrieve the last available upgrade date:
            // this means the R_VERSION table doesn't exist.
            // This table was introduced in version 2.3.0
            //
            log.logBasic(toString(), "There was an error getting information from the version table "+quote(TABLE_R_VERSION)+".");
            log.logBasic(toString(), "This table was introduced in version 2.3.0. so we assume the version is 2.2.2");
            log.logBasic(toString(), "Stack trace: "+Const.getStackTracker(e));

            majorVersion = 2;
            minorVersion = 2;

            lastUpgrade = null;
        }

        if (lastUpgrade != null)
        {
            majorVersion = (int)lastUpgrade.getInteger(FIELD_VERSION_MAJOR_VERSION, -1);
            minorVersion = (int)lastUpgrade.getInteger(FIELD_VERSION_MINOR_VERSION, -1);
        }
            
        if (majorVersion < REQUIRED_MAJOR_VERSION || ( majorVersion==REQUIRED_MAJOR_VERSION && minorVersion<REQUIRED_MINOR_VERSION))
        {
            throw new KettleException(Const.CR+
                    "The version of the repository is "+getVersion()+Const.CR+
                    "This Kettle edition requires it to be at least version "+getRequiredVersion()+Const.CR+
                    "Please upgrade the repository using the repository dialog (edit)"+Const.CR+
                    "Also see the Repository Upgrade Guide (in docs/English) for more information."
            );
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
	                 "FROM "+quote(TABLE_R_STEP_ATTRIBUTE) +" "+
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
		return getIDWithValue(quote(TABLE_R_JOB), quote(FIELD_JOB_ID_JOB), quote(FIELD_JOB_NAME), name, quote(FIELD_JOB_ID_DIRECTORY), id_directory);
	}

	public synchronized long getTransformationID(String name, long id_directory) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_TRANSFORMATION), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION), quote(FIELD_TRANSFORMATION_NAME), name, quote(FIELD_TRANSFORMATION_ID_DIRECTORY), id_directory);
	}

	public synchronized long getNoteID(String note) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_NOTE), quote(FIELD_NOTE_ID_NOTE), quote(FIELD_NOTE_VALUE_STR), note);
	}

	public synchronized long getDatabaseID(String name) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_DATABASE), quote(FIELD_DATABASE_ID_DATABASE), quote(FIELD_DATABASE_NAME), name);
	}
    
    public synchronized long getPartitionSchemaID(String name) throws KettleException
    {
        return getIDWithValue(quote(TABLE_R_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_NAME), name);
    }

    public synchronized long getSlaveID(String name) throws KettleException
    {
        return getIDWithValue(quote(TABLE_R_SLAVE), quote(FIELD_SLAVE_ID_SLAVE), quote(FIELD_SLAVE_NAME), name);
    }

    public synchronized long getClusterID(String name) throws KettleException
    {
        return getIDWithValue(quote(TABLE_R_CLUSTER), quote(FIELD_CLUSTER_ID_CLUSTER), quote(FIELD_CLUSTER_NAME), name);
    }

	public synchronized long getDatabaseTypeID(String code) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_CODE), code);
	}

	public synchronized long getDatabaseConTypeID(String code) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_CODE), code);
	}

	public synchronized long getStepTypeID(String code) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE), quote(FIELD_STEP_TYPE_CODE), code);
	}

	public synchronized long getJobEntryID(String name, long id_job) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_JOBENTRY), quote(FIELD_JOBENTRY_ID_JOBENTRY), quote(FIELD_JOBENTRY_NAME), name, quote(FIELD_JOBENTRY_ID_JOB), id_job);
	}

	public synchronized long getJobEntryTypeID(String code) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_TYPE_CODE), code);
	}

	public synchronized long getStepID(String name, long id_transformation) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_STEP), quote(FIELD_STEP_ID_STEP), quote(FIELD_STEP_NAME), name, quote(FIELD_STEP_ID_TRANSFORMATION), id_transformation);
	}

	public synchronized long getUserID(String login) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_USER), quote(FIELD_USER_ID_USER), quote(FIELD_USER_LOGIN), login);
	}

	public synchronized long getProfileID(String profilename) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_PROFILE), quote(FIELD_PROFILE_ID_PROFILE), quote(FIELD_PROFILE_NAME), profilename);
	}

	public synchronized long getPermissionID(String code) throws KettleException
	{
		return getIDWithValue(quote(TABLE_R_PERMISSION), quote(FIELD_PERMISSION_ID_PERMISSION), quote(FIELD_PERMISSION_CODE), code);
	}

	public synchronized long getTransHopID(long id_transformation, long id_step_from, long id_step_to) throws KettleException
	{
		String lookupkey[] = new String[] { quote(FIELD_TRANS_HOP_ID_TRANSFORMATION), quote(FIELD_TRANS_HOP_ID_STEP_FROM), quote(FIELD_TRANS_HOP_ID_STEP_TO), };
		long key[] = new long[] { id_transformation, id_step_from, id_step_to };

		return getIDWithValue(quote(TABLE_R_TRANS_HOP), quote(FIELD_TRANS_HOP_ID_TRANS_HOP), lookupkey, key);
	}

	public synchronized long getJobHopID(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to)
			throws KettleException
	{
		String lookupkey[] = new String[] { quote(FIELD_JOB_HOP_ID_JOB), quote(FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM), quote(FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO), };
		long key[] = new long[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

		return getIDWithValue(quote(TABLE_R_JOB_HOP), quote(FIELD_JOB_HOP_ID_JOB_HOP), lookupkey, key);
	}

	public synchronized long getDependencyID(long id_transformation, long id_database, String tablename) throws KettleException
	{
		String lookupkey[] = new String[] { quote(FIELD_DEPENDENCY_ID_TRANSFORMATION), quote(FIELD_DEPENDENCY_ID_DATABASE), };
		long key[] = new long[] { id_transformation, id_database };

		return getIDWithValue(quote(TABLE_R_DEPENDENCY), quote(FIELD_DEPENDENCY_ID_DEPENDENCY), quote(FIELD_DEPENDENCY_TABLE_NAME), tablename, lookupkey, key);
	}

	public synchronized long getRootDirectoryID() throws KettleException
	{
		RowMetaAndData result = database.getOneRow("SELECT "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quote(TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = 0");
		if (result != null && result.isNumeric(0))
			return result.getInteger(0, -1);
		return -1;
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0);
		}

		return retval;
	}

	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quote(TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory+" ORDER BY "+quote(FIELD_DIRECTORY_DIRECTORY_NAME));
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
		return getStringWithID(quote(TABLE_R_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE), id_database_type, quote(FIELD_DATABASE_TYPE_CODE));
	}

	public synchronized String getDatabaseConTypeCode(long id_database_contype) throws KettleException
	{
		return getStringWithID(quote(TABLE_R_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE), id_database_contype, quote(FIELD_DATABASE_CONTYPE_CODE));
	}

	public synchronized String getStepTypeCode(long id_database_type) throws KettleException
	{
		return getStringWithID(quote(TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE), id_database_type, quote(FIELD_STEP_TYPE_CODE));
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
		String sql = "UPDATE "+quote(TABLE_R_TRANSFORMATION)+" SET "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = ? WHERE "+nameField+" = ? AND "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = ?";

		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_to));
		par.addValue(new ValueMeta(FIELD_TRANSFORMATION_NAME,  ValueMetaInterface.TYPE_STRING), transname);
		par.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_from));

		database.execStatement(sql, par.getRowMeta(), par.getData());
	}

	public synchronized void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleException
	{
		String sql = "UPDATE "+quote(TABLE_R_JOB)+" SET "+quote(FIELD_JOB_ID_DIRECTORY)+" = ? WHERE "+quote(FIELD_JOB_NAME)+" = ? AND "+quote(FIELD_JOB_ID_DIRECTORY)+" = ?";

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
		return getNextID(quote(TABLE_R_TRANSFORMATION), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION));
	}

	public synchronized long getNextJobID() throws KettleException
	{
		return getNextID(quote(TABLE_R_JOB), quote(FIELD_JOB_ID_JOB));
	}

	public synchronized long getNextNoteID() throws KettleException
	{
		return getNextID(quote(TABLE_R_NOTE), quote(FIELD_NOTE_ID_NOTE));
	}
    
    public synchronized long getNextLogID() throws KettleException
    {
        return getNextID(quote(TABLE_R_REPOSITORY_LOG), quote(FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG));
    }

	public synchronized long getNextDatabaseID() throws KettleException
	{
		return getNextID(quote(TABLE_R_DATABASE), quote(FIELD_DATABASE_ID_DATABASE));
	}

	public synchronized long getNextDatabaseTypeID() throws KettleException
	{
		return getNextID(quote(TABLE_R_DATABASE_TYPE), quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE));
	}

	public synchronized long getNextDatabaseConnectionTypeID() throws KettleException
	{
		return getNextID(quote(TABLE_R_DATABASE_CONTYPE), quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE));
	}

	public synchronized long getNextLoglevelID() throws KettleException
	{
		return getNextID(quote(TABLE_R_LOGLEVEL), quote(FIELD_LOGLEVEL_ID_LOGLEVEL));
	}

	public synchronized long getNextStepTypeID() throws KettleException
	{
		return getNextID(quote(TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE));
	}

	public synchronized long getNextStepID() throws KettleException
	{
		return getNextID(quote(TABLE_R_STEP), quote(FIELD_STEP_ID_STEP));
	}

	public synchronized long getNextJobEntryID() throws KettleException
	{
		return getNextID(quote(TABLE_R_JOBENTRY), quote(FIELD_JOBENTRY_ID_JOBENTRY));
	}

	public synchronized long getNextJobEntryTypeID() throws KettleException
	{
		return getNextID(quote(TABLE_R_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE));
	}

	public synchronized long getNextJobEntryCopyID() throws KettleException
	{
		return getNextID(quote(TABLE_R_JOBENTRY_COPY), quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY));
	}

	public synchronized long getNextStepAttributeID() throws KettleException
	{
		return getNextID(quote(TABLE_R_STEP_ATTRIBUTE), quote(FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE));
	}

    public synchronized long getNextTransAttributeID() throws KettleException
    {
        return getNextID(quote(TABLE_R_TRANS_ATTRIBUTE), quote(FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE));
    }
    
    public synchronized long getNextDatabaseAttributeID() throws KettleException
    {
        return getNextID(quote(TABLE_R_DATABASE_ATTRIBUTE), quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE));
    }

	public synchronized long getNextTransHopID() throws KettleException
	{
		return getNextID(quote(TABLE_R_TRANS_HOP), quote(FIELD_TRANS_HOP_ID_TRANS_HOP));
	}

	public synchronized long getNextJobHopID() throws KettleException
	{
		return getNextID(quote(TABLE_R_JOB_HOP), quote(FIELD_JOB_HOP_ID_JOB_HOP));
	}

	public synchronized long getNextDepencencyID() throws KettleException
	{
		return getNextID(quote(TABLE_R_DEPENDENCY), quote(FIELD_DEPENDENCY_ID_DEPENDENCY));
	}
    
    public synchronized long getNextPartitionSchemaID() throws KettleException
    {
        return getNextID(quote(TABLE_R_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA));
    }

    public synchronized long getNextPartitionID() throws KettleException
    {
        return getNextID(quote(TABLE_R_PARTITION), quote(FIELD_PARTITION_ID_PARTITION));
    }

    public synchronized long getNextTransformationPartitionSchemaID() throws KettleException
    {
        return getNextID(quote(TABLE_R_TRANS_PARTITION_SCHEMA), quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA));
    }
    
    public synchronized long getNextClusterID() throws KettleException
    {
        return getNextID(quote(TABLE_R_CLUSTER), quote(FIELD_CLUSTER_ID_CLUSTER));
    }

    public synchronized long getNextSlaveServerID() throws KettleException
    {
        return getNextID(quote(TABLE_R_SLAVE), quote(FIELD_SLAVE_ID_SLAVE));
    }
    
    public synchronized long getNextClusterSlaveID() throws KettleException
    {
        return getNextID(quote(TABLE_R_CLUSTER_SLAVE), quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE));
    }
    
    public synchronized long getNextTransformationSlaveID() throws KettleException
    {
        return getNextID(quote(TABLE_R_TRANS_SLAVE), quote(FIELD_TRANS_SLAVE_ID_TRANS_SLAVE));
    }
    
    public synchronized long getNextTransformationClusterID() throws KettleException
    {
        return getNextID(quote(TABLE_R_TRANS_CLUSTER), quote(FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER));
    }
    
	public synchronized long getNextConditionID() throws KettleException
	{
		return getNextID(quote(TABLE_R_CONDITION), quote(FIELD_CONDITION_ID_CONDITION));
	}

	public synchronized long getNextValueID() throws KettleException
	{
		return getNextID(quote(TABLE_R_VALUE), quote(FIELD_VALUE_ID_VALUE));
	}

	public synchronized long getNextUserID() throws KettleException
	{
		return getNextID(quote(TABLE_R_USER), quote(FIELD_USER_ID_USER));
	}

	public synchronized long getNextProfileID() throws KettleException
	{
		return getNextID(quote(TABLE_R_PROFILE), quote(FIELD_PROFILE_ID_PROFILE));
	}

	public synchronized long getNextPermissionID() throws KettleException
	{
		return getNextID(quote(TABLE_R_PERMISSION), quote(FIELD_PERMISSION_ID_PERMISSION));
	}

	public synchronized long getNextJobEntryAttributeID() throws KettleException
	{
	    return getNextID(TABLE_R_JOBENTRY_ATTRIBUTE, quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE));
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
		return getNextID(quote(TABLE_R_DIRECTORY), quote(FIELD_DIRECTORY_ID_DIRECTORY));
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
        
		// Save the logging connection link...
		if (transMeta.getLogConnection()!=null) insertStepDatabase(transMeta.getId(), -1L, transMeta.getLogConnection().getID());

		// Save the maxdate connection link...
		if (transMeta.getMaxDateConnection()!=null) insertStepDatabase(transMeta.getId(), -1L, transMeta.getMaxDateConnection().getID());
	}

	public synchronized void insertJob(long id_job, long id_directory, String name, long id_database_log, String table_name_log,
			String modified_user, Date modified_date, boolean useBatchId, boolean batchIdPassed, boolean logfieldUsed, 
            String sharedObjectsFile, String description, String extended_description, String version, int status,
			String created_user, Date created_date) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory));
		table.addValue(new ValueMeta(FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(FIELD_JOB_DESCRIPTION, ValueMetaInterface.TYPE_STRING), description);
		table.addValue(new ValueMeta(FIELD_JOB_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING), extended_description);
		table.addValue(new ValueMeta(FIELD_JOB_JOB_VERSION, ValueMetaInterface.TYPE_STRING), version);
		table.addValue(new ValueMeta(FIELD_JOB_JOB_STATUS, ValueMetaInterface.TYPE_INTEGER), new Long(status  <0 ? -1L : status));

		table.addValue(new ValueMeta(FIELD_JOB_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_log));
		table.addValue(new ValueMeta(FIELD_JOB_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING), table_name_log);

		table.addValue(new ValueMeta(FIELD_JOB_CREATED_USER, ValueMetaInterface.TYPE_STRING), created_user);
		table.addValue(new ValueMeta(FIELD_JOB_CREATED_DATE, ValueMetaInterface.TYPE_DATE), created_date);
		table.addValue(new ValueMeta(FIELD_JOB_MODIFIED_USER, ValueMetaInterface.TYPE_STRING), modified_user);
		table.addValue(new ValueMeta(FIELD_JOB_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE), modified_date);
        table.addValue(new ValueMeta(FIELD_JOB_USE_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(useBatchId));
        table.addValue(new ValueMeta(FIELD_JOB_PASS_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(batchIdPassed));
        table.addValue(new ValueMeta(FIELD_JOB_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(logfieldUsed));
        table.addValue(new ValueMeta(FIELD_JOB_SHARED_FILE, ValueMetaInterface.TYPE_STRING), sharedObjectsFile);

		database.prepareInsert(table.getRowMeta(), TABLE_R_JOB);
		database.setValuesInsert(table);
		database.insertRow();
        if (log.isDebug()) log.logDebug(toString(), "Inserted new record into table "+quote(TABLE_R_JOB)+" with data : " + table);
		database.closeInsert();
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
        table.addValue(new ValueMeta(FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), slaveServer.getPassword());
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
        table.addValue(new ValueMeta(FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), slaveServer.getPassword());
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
			id_value = insertValue(v.getValueMeta().getName(), v.getValueMeta().getTypeDesc(), v.getValueMeta().getString(v.getValueData()), v.getValueMeta().isNull(v.getValueData()), condition.getRightExactID());
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
		String sql = "DELETE FROM "+quote(TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;
		database.execStatement(sql);
	}

	public synchronized void renameDirectory(long id_directory, String name) throws KettleException
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta(FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), name);

		String sql = "UPDATE "+quote(TABLE_R_DIRECTORY)+" SET "+quote(FIELD_DIRECTORY_DIRECTORY_NAME)+" = ? WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;

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

		String sql = "SELECT " + quote(FIELD_VALUE_ID_VALUE) + " FROM " + quote(TABLE_R_VALUE) + " ";
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

	public synchronized long insertJobEntry(long id_job, String name, String description, String jobentrytype)
			throws KettleException
	{
		long id = getNextJobEntryID();

		long id_jobentry_type = getJobEntryTypeID(jobentrytype);

		log.logDebug(toString(), "ID_JobEntry_type = " + id_jobentry_type + " for type = [" + jobentrytype + "]");

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(FIELD_JOBENTRY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_type));
		table.addValue(new ValueMeta(FIELD_JOBENTRY_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(FIELD_JOBENTRY_DESCRIPTION, ValueMetaInterface.TYPE_STRING), description);

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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOB);
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_TRANSFORMATION)+" WHERE "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOB)+" WHERE "+quote(FIELD_JOB_ID_DIRECTORY)+" = " + id_directory;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transforamtion;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transforamtion;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_CONDITION)+" WHERE "+quote(FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_TRANS_NOTE)+" WHERE "+quote(FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOB_NOTE)+" WHERE "+quote(FIELD_JOB_ID_JOB)+" = " + id_job;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_DATABASE);
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

        String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_STEP)+" WHERE "+quote(FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_DATABASE)+" = " + id_database;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+" = " + id_step;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_TRANS_HOP)+" WHERE "+quote(FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOB_HOP)+" WHERE "+quote(FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_DEPENDENCY)+" WHERE "+quote(FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOBENTRY)+" WHERE "+quote(FIELD_JOBENTRY_ID_JOB)+" = " + id_job;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job + " AND "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY)+" = "
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_USER);
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
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

		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_PROFILE);
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized String[] getTransformationNames(long id_directory) throws KettleException
	{
		return getStrings("SELECT "+quote(FIELD_TRANSFORMATION_NAME)+" FROM "+quote(TABLE_R_TRANSFORMATION)+" WHERE "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(FIELD_TRANSFORMATION_NAME));
	}
    
    public List<RepositoryObject> getJobObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects(quote(TABLE_R_JOB), RepositoryObject.STRING_OBJECT_TYPE_JOB, id_directory);
    }

    public List<RepositoryObject> getTransformationObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects(quote(TABLE_R_TRANSFORMATION), RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION, id_directory);
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
        return getStrings("SELECT "+quote(FIELD_JOB_NAME)+" FROM "+quote(TABLE_R_JOB)+" WHERE "+quote(FIELD_JOB_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(FIELD_JOB_NAME));
	}

	public synchronized String[] getDirectoryNames(long id_directory) throws KettleException
	{
        return getStrings("SELECT "+quote(FIELD_DIRECTORY_DIRECTORY_NAME)+" FROM "+quote(TABLE_R_DIRECTORY)+" WHERE "+quote(FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory + " ORDER BY "+quote(FIELD_DIRECTORY_DIRECTORY_NAME));
	}

	public synchronized String[] getJobNames() throws KettleException
	{
        return getStrings("SELECT "+quote(FIELD_JOB_NAME)+" FROM "+quote(TABLE_R_JOB)+" ORDER BY "+quote(FIELD_JOB_NAME));
	}

	public long[] getSubConditionIDs(long id_condition) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_CONDITION_ID_CONDITION)+" FROM "+quote(TABLE_R_CONDITION)+" WHERE "+quote(FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition);
	}

	public long[] getTransNoteIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_TRANS_NOTE_ID_NOTE)+" FROM "+quote(TABLE_R_TRANS_NOTE)+" WHERE "+quote(FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getConditionIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_TRANS_STEP_CONDITION_ID_CONDITION)+" FROM "+quote(TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getDatabaseIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_STEP_DATABASE_ID_DATABASE)+" FROM "+quote(TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getJobNoteIDs(long id_job) throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_JOB_NOTE_ID_NOTE)+" FROM "+quote(TABLE_R_JOB_NOTE)+" WHERE "+quote(FIELD_JOB_NOTE_ID_JOB)+" = " + id_job);
	}

	public long[] getDatabaseIDs() throws KettleException
	{
        return getIDs("SELECT "+quote(FIELD_DATABASE_ID_DATABASE)+" FROM "+quote(TABLE_R_DATABASE)+" ORDER BY "+quote(FIELD_DATABASE_ID_DATABASE));
	}
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE)+" FROM "+quote(TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database);
    }
    
    public long[] getPartitionSchemaIDs() throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" FROM "+quote(TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+quote(FIELD_PARTITION_SCHEMA_NAME));
    }
    
    public long[] getPartitionIDs(long id_partition_schema) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_PARTITION_ID_PARTITION)+" FROM "+quote(TABLE_R_PARTITION)+" WHERE "+quote(FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }

    public long[] getTransformationPartitionSchemaIDs(long id_transformation) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA)+" FROM "+quote(TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = "+id_transformation);
    }
    
    public long[] getTransformationClusterSchemaIDs(long id_transformation) throws KettleException
    {
        return getIDs("SELECT ID_TRANS_CLUSTER FROM "+quote(TABLE_R_TRANS_CLUSTER)+" WHERE ID_TRANSFORMATION = " + id_transformation);
    }
    
    public long[] getClusterIDs() throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_CLUSTER_ID_CLUSTER)+" FROM "+quote(TABLE_R_CLUSTER)+" ORDER BY "+quote(FIELD_CLUSTER_NAME)); 
    }

    public long[] getSlaveIDs() throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_SLAVE_ID_SLAVE)+" FROM "+quote(TABLE_R_SLAVE));
    }

    public long[] getSlaveIDs(long id_cluster_schema) throws KettleException
    {
        return getIDs("SELECT "+quote(FIELD_CLUSTER_SLAVE_ID_SLAVE)+" FROM "+quote(TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster_schema);
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
		return getStrings("SELECT "+nameField+" FROM "+quote(TABLE_R_DATABASE)+" ORDER BY "+nameField);
	}
    
    public synchronized String[] getPartitionSchemaNames() throws KettleException
    {
        String nameField = quote(FIELD_PARTITION_SCHEMA_NAME);
        return getStrings("SELECT "+nameField+" FROM "+quote(TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getSlaveNames() throws KettleException
    {
        String nameField = quote(FIELD_SLAVE_NAME);
        return getStrings("SELECT "+nameField+" FROM "+quote(TABLE_R_SLAVE)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getClusterNames() throws KettleException
    {
        String nameField = quote(FIELD_CLUSTER_NAME);
        return getStrings("SELECT "+nameField+" FROM "+quote(TABLE_R_CLUSTER)+" ORDER BY "+nameField);
    }

	public long[] getStepIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_STEP_ID_STEP)+" FROM "+quote(TABLE_R_STEP)+" WHERE "+quote(FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public synchronized String[] getTransformationsUsingDatabase(long id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" FROM "+quote(TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_DATABASE)+" = " + id_database;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
	}
    
    public synchronized String[] getClustersUsingSlave(long id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" FROM "+quote(TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(FIELD_CLUSTER_SLAVE_ID_SLAVE)+" = " + id_slave;

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
        String sql = "SELECT DISTINCT "+quote(FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" FROM "+quote(TABLE_R_TRANS_SLAVE)+" WHERE "+quote(FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingPartitionSchema(long id_partition_schema) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+
                     " FROM "+quote(TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingCluster(long id_cluster) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" FROM "+quote(TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(FIELD_TRANS_CLUSTER_ID_CLUSTER)+" = " + id_cluster;
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
		return getIDs("SELECT "+quote(FIELD_TRANS_HOP_ID_TRANS_HOP)+" FROM "+quote(TABLE_R_TRANS_HOP)+" WHERE "+quote(FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getJobHopIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOB_HOP_ID_JOB_HOP)+" FROM "+quote(TABLE_R_JOB_HOP)+" WHERE "+quote(FIELD_JOB_HOP_ID_JOB)+" = " + id_job);
	}

	public long[] getTransDependencyIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_DEPENDENCY_ID_DEPENDENCY)+" FROM "+quote(TABLE_R_DEPENDENCY)+" WHERE "+quote(FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getUserIDs() throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_USER_ID_USER)+" FROM "+quote(TABLE_R_USER));
	}

	public synchronized String[] getUserLogins() throws KettleException
	{
		String loginField = quote(FIELD_USER_LOGIN);
		return getStrings("SELECT "+loginField+" FROM "+quote(TABLE_R_USER)+" ORDER BY "+loginField);
	}

	public long[] getPermissionIDs(long id_profile) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_PROFILE_PERMISSION_ID_PERMISSION)+" FROM "+quote(TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile);
	}

	public long[] getJobEntryIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOBENTRY_ID_JOBENTRY)+" FROM "+quote(TABLE_R_JOBENTRY)+" WHERE "+quote(FIELD_JOBENTRY_ID_JOB)+" = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+" FROM "+quote(TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job, long id_jobentry) throws KettleException
	{
		return getIDs("SELECT "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+
				" FROM "+quote(TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job + " AND "+quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY)+" = " + id_jobentry);
	}

	public synchronized String[] getProfiles() throws KettleException
	{
		String nameField = quote(FIELD_PROFILE_NAME);
		return getStrings("SELECT "+nameField+" FROM "+quote(TABLE_R_PROFILE)+" ORDER BY "+nameField);
	}

	public RowMetaAndData getNote(long id_note) throws KettleException
	{
		return getOneRow(quote(TABLE_R_NOTE), quote(FIELD_NOTE_ID_NOTE), id_note);
	}

	public RowMetaAndData getDatabase(long id_database) throws KettleException
	{
		return getOneRow(quote(TABLE_R_DATABASE), quote(FIELD_DATABASE_ID_DATABASE), id_database);
	}

    public RowMetaAndData getDatabaseAttribute(long id_database_attribute) throws KettleException
    {
        return getOneRow(quote(TABLE_R_DATABASE_ATTRIBUTE), quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), id_database_attribute);
    }

    public Collection<RowMetaAndData> getDatabaseAttributes() throws KettleDatabaseException, KettleValueException
    {
    	List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    	List<Object[]> rows = database.getRows("SELECT * FROM " + quote(TABLE_R_DATABASE_ATTRIBUTE),0);
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
    	List<Object[]> rows = database.getRows("SELECT * FROM " + quote(TABLE_R_DATABASE_ATTRIBUTE) + " WHERE "+quote(FIELD_DATABASE_ID_DATABASE) +" = "+id_database, 0);
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
		return getOneRow(quote(TABLE_R_CONDITION), quote(FIELD_CONDITION_ID_CONDITION), id_condition);
	}

	public RowMetaAndData getValue(long id_value) throws KettleException
	{
		return getOneRow(quote(TABLE_R_VALUE), quote(FIELD_VALUE_ID_VALUE), id_value);
	}

	public RowMetaAndData getStep(long id_step) throws KettleException
	{
		return getOneRow(quote(TABLE_R_STEP), quote(FIELD_STEP_ID_STEP), id_step);
	}

	public RowMetaAndData getStepType(long id_step_type) throws KettleException
	{
		return getOneRow(quote(TABLE_R_STEP_TYPE), quote(FIELD_STEP_TYPE_ID_STEP_TYPE), id_step_type);
	}

	public RowMetaAndData getStepAttribute(long id_step_attribute) throws KettleException
	{
		return getOneRow(quote(TABLE_R_STEP_ATTRIBUTE), quote(FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE), id_step_attribute);
	}

	public RowMetaAndData getStepDatabase(long id_step) throws KettleException
	{
		return getOneRow(quote(TABLE_R_STEP_DATABASE), quote(FIELD_STEP_DATABASE_ID_STEP), id_step);
	}

	public RowMetaAndData getTransHop(long id_trans_hop) throws KettleException
	{
		return getOneRow(quote(TABLE_R_TRANS_HOP), quote(FIELD_TRANS_HOP_ID_TRANS_HOP), id_trans_hop);
	}

	public RowMetaAndData getJobHop(long id_job_hop) throws KettleException
	{
		return getOneRow(quote(TABLE_R_JOB_HOP), quote(FIELD_JOB_HOP_ID_JOB_HOP), id_job_hop);
	}

	public RowMetaAndData getTransDependency(long id_dependency) throws KettleException
	{
		return getOneRow(quote(TABLE_R_DEPENDENCY), quote(FIELD_DEPENDENCY_ID_DEPENDENCY), id_dependency);
	}

	public RowMetaAndData getTransformation(long id_transformation) throws KettleException
	{
		return getOneRow(quote(TABLE_R_TRANSFORMATION), quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION), id_transformation);
	}

	public RowMetaAndData getUser(long id_user) throws KettleException
	{
		return getOneRow(quote(TABLE_R_USER), quote(FIELD_USER_ID_USER), id_user);
	}

	public RowMetaAndData getProfile(long id_profile) throws KettleException
	{
		return getOneRow(quote(TABLE_R_PROFILE), quote(FIELD_PROFILE_ID_PROFILE), id_profile);
	}

	public RowMetaAndData getPermission(long id_permission) throws KettleException
	{
		return getOneRow(quote(TABLE_R_PERMISSION), quote(FIELD_PERMISSION_ID_PERMISSION), id_permission);
	}

	public RowMetaAndData getJob(long id_job) throws KettleException
	{
		return getOneRow(quote(TABLE_R_JOB), quote(FIELD_JOB_ID_JOB), id_job);
	}

	public RowMetaAndData getJobEntry(long id_jobentry) throws KettleException
	{
		return getOneRow(quote(TABLE_R_JOBENTRY), quote(FIELD_JOBENTRY_ID_JOBENTRY), id_jobentry);
	}

	public RowMetaAndData getJobEntryCopy(long id_jobentry_copy) throws KettleException
	{
		return getOneRow(quote(TABLE_R_JOBENTRY_COPY), quote(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY), id_jobentry_copy);
	}

	public RowMetaAndData getJobEntryType(long id_jobentry_type) throws KettleException
	{
		return getOneRow(quote(TABLE_R_JOBENTRY_TYPE), quote(FIELD_JOBENTRY_ID_JOBENTRY_TYPE), id_jobentry_type);
	}

	public RowMetaAndData getDirectory(long id_directory) throws KettleException
	{
		return getOneRow(quote(TABLE_R_DIRECTORY), quote(FIELD_DIRECTORY_ID_DIRECTORY), id_directory);
	}
	
    public RowMetaAndData getPartitionSchema(long id_partition_schema) throws KettleException
    {
        return getOneRow(quote(TABLE_R_PARTITION_SCHEMA), quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA), id_partition_schema);
    }
    
    public RowMetaAndData getPartition(long id_partition) throws KettleException
    {
        return getOneRow(quote(TABLE_R_PARTITION), quote(FIELD_PARTITION_ID_PARTITION), id_partition);
    }

    public RowMetaAndData getClusterSchema(long id_cluster_schema) throws KettleException
    {
        return getOneRow(quote(TABLE_R_CLUSTER), quote(FIELD_CLUSTER_ID_CLUSTER), id_cluster_schema);
    }

    public RowMetaAndData getSlaveServer(long id_slave) throws KettleException
    {
        return getOneRow(quote(TABLE_R_SLAVE), quote(FIELD_SLAVE_ID_SLAVE), id_slave);
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

	// STEP ATTRIBUTES: GET

	public synchronized void setLookupStepAttribute() throws KettleException
	{
		String sql = "SELECT "+quote(FIELD_STEP_ATTRIBUTE_VALUE_STR)+", "+quote(FIELD_STEP_ATTRIBUTE_VALUE_NUM)+
			" FROM "+quote(TABLE_R_STEP_ATTRIBUTE)+
			" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+" = ?  AND "+quote(FIELD_STEP_ATTRIBUTE_CODE)+" = ?  AND "+quote(FIELD_STEP_ATTRIBUTE_NR)+" = ? ";

		psStepAttributesLookup = database.prepareSQL(sql);
	}
    
    public synchronized void setLookupTransAttribute() throws KettleException
    {
        String sql = "SELECT "+quote(FIELD_TRANS_ATTRIBUTE_VALUE_STR)+", "+quote(FIELD_TRANS_ATTRIBUTE_VALUE_NUM)+
        	" FROM "+quote(TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = ?  AND "+quote(FIELD_TRANS_ATTRIBUTE_CODE)+" = ? AND "+FIELD_TRANS_ATTRIBUTE_NR+" = ? ";

        psTransAttributesLookup = database.prepareSQL(sql);
    }
    
    public synchronized void closeTransAttributeLookupPreparedStatement() throws KettleException
    {
        database.closePreparedStatement(psTransAttributesLookup);
        psTransAttributesLookup = null;
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
		    database.insertFinished(psStepAttributesInsert, useBatchProcessing); // batch mode!
			psStepAttributesInsert = null;
	    }
	}

    public synchronized void closeTransAttributeInsertPreparedStatement() throws KettleException
    {
        if (psTransAttributesInsert!=null)
        {
            database.insertFinished(psTransAttributesInsert, useBatchProcessing); // batch mode!
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

	public synchronized long getStepAttributeInteger(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return 0;
		return r.getInteger(FIELD_STEP_ATTRIBUTE_VALUE_NUM, 0L);
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
			String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_STEP)+" = ? AND "+quote(FIELD_STEP_ATTRIBUTE_CODE)+" = ?";
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
        String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = ? AND "+quote(FIELD_TRANS_ATTRIBUTE_CODE)+" = ?";
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
        	" FROM "+quote(TABLE_R_TRANS_ATTRIBUTE)+
        	" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = ? AND "+quote(FIELD_TRANS_ATTRIBUTE_CODE)+" = ? AND "+quote(FIELD_TRANS_ATTRIBUTE_NR)+" = ?"+
        	" ORDER BY "+quote(FIELD_TRANS_ATTRIBUTE_VALUE_NUM);
        
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(FIELD_TRANS_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        
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
		" FROM "+quote(TABLE_R_JOBENTRY_ATTRIBUTE)+
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
		String sql = "SELECT COUNT(*) FROM "+quote(TABLE_R_JOBENTRY_ATTRIBUTE)+" WHERE "+quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY)+" = ? AND "+quote(FIELD_JOBENTRY_ATTRIBUTE_CODE)+" = ?";
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
		String sql = "DELETE FROM "+quote(TABLE_R_STEP)+" WHERE "+quote(FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
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
			String sql = "DELETE FROM "+quote(TABLE_R_CONDITION)+" WHERE "+quote(FIELD_CONDITION_ID_CONDITION)+" = " + id_condition;
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
		String sql = "DELETE FROM "+quote(TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	/**
	 * Delete the relationships between the transformation/steps and the databases.
	 * @param id_transformation the transformation for which we want to delete the databases.
	 * @throws KettleException in case something unexpected happens.
	 */
	public synchronized void delStepDatabases(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_STEP_DATABASE)+" WHERE "+quote(FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobEntries(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_JOBENTRY)+" WHERE "+quote(FIELD_JOBENTRY_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delJobEntryCopies(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDependencies(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_DEPENDENCY)+" WHERE "+quote(FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delStepAttributes(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

    public synchronized void delTransAttributes(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quote(TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }
    
    public synchronized void delPartitionSchemas(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quote(TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delPartitions(long id_partition_schema) throws KettleException
    {
        // First see if the partition is used by a step, transformation etc.
        // 
        database.execStatement("DELETE FROM "+quote(TABLE_R_PARTITION)+" WHERE "+quote(FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }
    
    public synchronized void delClusterSlaves(long id_cluster) throws KettleException
    {
        String sql = "DELETE FROM "+quote(TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster;
        database.execStatement(sql);
    }
    
    public synchronized void delTransformationClusters(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quote(TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delTransformationSlaves(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quote(TABLE_R_TRANS_SLAVE)+" WHERE "+quote(FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" = " + id_transformation;
        database.execStatement(sql);
    }


	public synchronized void delJobEntryAttributes(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_JOBENTRY_ATTRIBUTE)+" WHERE "+quote(FIELD_JOBENTRY_ATTRIBUTE_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransHops(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_TRANS_HOP)+" WHERE "+quote(FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobHops(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_JOB_HOP)+" WHERE "+quote(FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransNotes(long id_transformation) throws KettleException
	{
		long ids[] = getTransNoteIDs(id_transformation);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+quote(TABLE_R_NOTE)+" WHERE "+quote(FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			database.execStatement(sql);
		}

		String sql = "DELETE FROM "+quote(TABLE_R_TRANS_NOTE)+" WHERE "+quote(FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobNotes(long id_job) throws KettleException
	{
		long ids[] = getJobNoteIDs(id_job);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+quote(TABLE_R_NOTE)+" WHERE "+quote(FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			database.execStatement(sql);
		}

		String sql = "DELETE FROM "+quote(TABLE_R_JOB_NOTE)+" WHERE "+quote(FIELD_JOB_NOTE_ID_JOB)+" = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTrans(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_TRANSFORMATION)+" WHERE "+quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJob(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_JOB)+" WHERE "+quote(FIELD_JOB_ID_JOB)+" = " + id_job;
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
			String sql = "DELETE FROM "+quote(TABLE_R_DATABASE)+" WHERE "+quote(FIELD_DATABASE_ID_DATABASE)+" = " + id_database;
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
        String sql = "DELETE FROM "+quote(TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = " + id_database;
        database.execStatement(sql);
    }

	public synchronized void delTransStepCondition(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delValue(long id_value) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_VALUE)+" WHERE "+quote(FIELD_VALUE_ID_VALUE)+" = " + id_value;
		database.execStatement(sql);
	}

	public synchronized void delUser(long id_user) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_USER)+" WHERE "+quote(FIELD_USER_ID_USER)+" = " + id_user;
		database.execStatement(sql);
	}

	public synchronized void delProfile(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_PROFILE)+" WHERE "+quote(FIELD_PROFILE_ID_PROFILE)+" = " + id_profile;
		database.execStatement(sql);
	}

	public synchronized void delProfilePermissions(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+quote(TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
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
            database.execStatement("DELETE FROM "+quote(TABLE_R_SLAVE)+" WHERE "+quote(FIELD_SLAVE_ID_SLAVE)+" = " + id_slave);
            database.execStatement("DELETE FROM "+quote(TABLE_R_TRANS_SLAVE)+" WHERE "+quote(FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave);
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
            database.execStatement("DELETE FROM "+quote(TABLE_R_PARTITION)+" WHERE "+quote(FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
            database.execStatement("DELETE FROM "+quote(TABLE_R_PARTITION_SCHEMA)+" WHERE "+quote(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
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
            database.execStatement("DELETE FROM "+quote(TABLE_R_CLUSTER)+" WHERE "+quote(FIELD_CLUSTER_ID_CLUSTER)+" = " + id_cluster);
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
		String sql = "UPDATE "+quote(TABLE_R_TRANSFORMATION)+" SET "+quote(FIELD_TRANSFORMATION_NAME)+" = ? WHERE "+quote(FIELD_TRANSFORMATION_ID_TRANSFORMATION)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_NAME,  ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_TRANSFORMATION_ID_TRANSFORMATION,  ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameUser(long id_user, String newname) throws KettleException
	{
		String sql = "UPDATE "+quote(TABLE_R_USER)+" SET "+quote(FIELD_USER_NAME)+" = ? WHERE "+quote(FIELD_USER_ID_USER)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER), new Long(id_user));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameProfile(long id_profile, String newname) throws KettleException
	{
		String sql = "UPDATE "+quote(TABLE_R_PROFILE)+" SET "+quote(FIELD_PROFILE_NAME)+" = ? WHERE "+quote(FIELD_PROFILE_ID_PROFILE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameDatabase(long id_database, String newname) throws KettleException
	{
		String sql = "UPDATE "+quote(TABLE_R_DATABASE)+" SET "+quote(FIELD_DATABASE_NAME)+" = ? WHERE "+quote(FIELD_DATABASE_ID_DATABASE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void delAllFromJob(long id_job) throws KettleException
	{
		// log.logBasic(toString(), "Deleting info in repository on ID_JOB: "+id_job);

		delJobNotes(id_job);
		delJobEntryAttributes(id_job);
		delJobEntries(id_job);
		delJobEntryCopies(id_job);
		delJobHops(id_job);
		delJob(id_job);

		// log.logBasic(toString(), "All deleted on job with ID_JOB: "+id_job);
	}

	public synchronized void renameJob(long id_job, String newname) throws KettleException
	{
		String sql = "UPDATE "+quote(TABLE_R_JOB)+" SET "+quote(FIELD_JOB_NAME)+" = ? WHERE "+quote(FIELD_JOB_ID_JOB)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

    /**
     * Create or upgrade repository tables & fields, populate lookup tables, ...
     * 
     * @param monitor The progress monitor to use, or null if no monitor is present.
     * @param upgrade True if you want to upgrade the repository, false if you want to create it.
     * @throws KettleException in case something goes wrong!
     */
	public synchronized void createRepositorySchema(IProgressMonitor monitor, boolean upgrade) throws KettleException
	{
		RowMetaInterface table;
		String sql;
		String tablename;
		String indexname;
		String keyfield[];
		String user[], pass[], code[], desc[], prof[];

		int KEY = 9; // integer, no need for bigint!

		log.logBasic(toString(), "Starting to create or modify the repository tables...");
        String message = (upgrade?"Upgrading ":"Creating")+" the Kettle repository...";
		if (monitor!=null) monitor.beginTask(message, 31);
        
        setAutoCommit(true);
        
        //////////////////////////////////////////////////////////////////////////////////
        // R_LOG
        //
        // Log the operations we do in the repository.
        //
        table = new RowMeta();
        tablename = quote(TABLE_R_REPOSITORY_LOG);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_REPOSITORY_LOG_REP_VERSION,    ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_REPOSITORY_LOG_LOG_DATE,       ValueMetaInterface.TYPE_DATE));
        table.addValueMeta(new ValueMeta(FIELD_REPOSITORY_LOG_LOG_USER,       ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_REPOSITORY_LOG_OPERATION_DESC, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, false);
        
        if (sql != null && sql.length() > 0)
        {
            try
            {
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
            }
            catch (KettleException dbe)
            {
                throw new KettleException("Unable to create or modify table " + tablename, dbe);
            }
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }

        
        insertLogEntry((upgrade?"Upgrade":"Creation")+" of the Kettle repository");

        //////////////////////////////////////////////////////////////////////////////////
        // R_VERSION
        //
        // Let's start with the version table
        //
        table = new RowMeta();
        tablename = quote(TABLE_R_VERSION);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_VERSION_ID_VERSION,       ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_VERSION_MAJOR_VERSION,    ValueMetaInterface.TYPE_INTEGER, 3, 0));
        table.addValueMeta(new ValueMeta(FIELD_VERSION_MINOR_VERSION,    ValueMetaInterface.TYPE_INTEGER, 3, 0));
        table.addValueMeta(new ValueMeta(FIELD_VERSION_UPGRADE_DATE,     ValueMetaInterface.TYPE_DATE, 0, 0));
        table.addValueMeta(new ValueMeta(FIELD_VERSION_IS_UPGRADE,       ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_VERSION_ID_VERSION, false);

        if (sql != null && sql.length() > 0)
        {
            try
            {
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
            }
            catch (KettleException dbe)
            {
                throw new KettleException("Unable to create or modify table " + tablename, dbe);
            }
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }

        // Insert an extra record in R_VERSION every time we pass here...
        //
        try
        {
            Object[] data = new Object[] {
                    Long.valueOf(getNextID(tablename, FIELD_VERSION_ID_VERSION)),
                    Long.valueOf(REQUIRED_MAJOR_VERSION),
                    Long.valueOf(REQUIRED_MINOR_VERSION),
                    new Date(),
                    Boolean.valueOf(upgrade),
                };
            database.execStatement("INSERT INTO "+quote(TABLE_R_VERSION)+" VALUES(?, ?, ?, ?, ?)", table, data);
        }
        catch(KettleException e)
        {
            throw new KettleException("Unable to insert new version log record into "+tablename, e);
        }
        
		//////////////////////////////////////////////////////////////////////////////////
		// R_DATABASE_TYPE
		//
		// Create table...
		//
		boolean ok_database_type = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_DATABASE_TYPE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_TYPE_CODE,             ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_TYPE_DESCRIPTION,      ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, false);

		if (sql != null && sql.length() > 0)
		{
			try
			{
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
			}
			catch (KettleException dbe)
			{
				throw new KettleException("Unable to create or modify table " + tablename, dbe);
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_database_type)
		{
			//
			// Populate...
			//
			code = DatabaseMeta.getDBTypeDescList();
			desc = DatabaseMeta.getDBTypeDescLongList();

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
				RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_DATABASE_TYPE_ID_DATABASE_TYPE)+" FROM " + tablename + " WHERE " 
                		+ quote(FIELD_DATABASE_TYPE_CODE) +" = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextDatabaseTypeID();

					Object[] tableData = new Object[] { new Long(nextid), code[i], desc[i], };
					database.setValuesInsert(table, tableData);
					database.insertRow();
				}
			}

			try
			{
				database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
			}
			catch (KettleException dbe)
			{
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
			}
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DATABASE_CONTYPE
		//
		// Create table...
		// 
		boolean ok_database_contype = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_DATABASE_CONTYPE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_CONTYPE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_CONTYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE, false);

		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_database_contype)
		{
			//
			// Populate with data...
			//
			code = DatabaseMeta.dbAccessTypeCode;
			desc = DatabaseMeta.dbAccessTypeDesc;

			database.prepareInsert(table, tablename);

			for (int i = 0; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE)+" FROM " + tablename + " WHERE " 
                		+ quote(FIELD_DATABASE_CONTYPE_CODE) + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextDatabaseConnectionTypeID();

                    Object[] tableData = new Object[] { 
                            new Long(nextid),
                            code[i],
                            desc[i],
                    };
					database.setValuesInsert(table, tableData);
					database.insertRow();
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_NOTE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_NOTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_NOTE_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_NOTE_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_NOTE_GUI_LOCATION_WIDTH, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_NOTE_GUI_LOCATION_HEIGHT, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_NOTE_ID_NOTE, false);
        
		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DATABASE
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_DATABASE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_HOST_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_DATABASE_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_PORT, ValueMetaInterface.TYPE_INTEGER, 7, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_USERNAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_PASSWORD, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_SERVERNAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_DATA_TBS, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DATABASE_INDEX_TBS, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_DATABASE_ID_DATABASE, false);
        
		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_DATABASE_ATTRIBUTE
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_DATABASE_ATTRIBUTE);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_DATABASE_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, false);
        
        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
            
            try
            {
                indexname = "IDX_" + tablename.substring(2) + "_AK";
                keyfield = new String[] { FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, FIELD_DATABASE_ATTRIBUTE_CODE, };
                if (!database.checkIndexExists(tablename, keyfield))
                {
                    sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                    database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
                }
            }
            catch(KettleException kdbe)
            {
                // Ignore this one: index is not properly detected, it already exists...
            }

        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DIRECTORY
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_DIRECTORY);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_DIRECTORY_ID_DIRECTORY,        ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DIRECTORY_ID_DIRECTORY_PARENT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DIRECTORY_DIRECTORY_NAME,      ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_DIRECTORY_ID_DIRECTORY, false);

		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
			
			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_AK";
				keyfield = new String[] { FIELD_DIRECTORY_ID_DIRECTORY_PARENT, FIELD_DIRECTORY_DIRECTORY_NAME };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANSFORMATION
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_TRANSFORMATION);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_TRANS_VERSION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_TRANS_STATUS, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_READ, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_WRITE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_INPUT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_OUTPUT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_STEP_UPDATE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_USE_BATCHID, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_ID_DATABASE_MAXDATE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_TABLE_NAME_MAXDATE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_FIELD_NAME_MAXDATE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_OFFSET_MAXDATE, ValueMetaInterface.TYPE_NUMBER, 12, 2));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_DIFF_MAXDATE, ValueMetaInterface.TYPE_NUMBER, 12, 2));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_CREATED_USER, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_CREATED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_MODIFIED_USER, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANSFORMATION_SIZE_ROWSET, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_TRANSFORMATION_ID_TRANSFORMATION, false);

        if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		// In case of an update, the added column R_TRANSFORMATION.ID_DIRECTORY == NULL!!!
		database.execStatement("UPDATE " + tablename + " SET "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+"=0 WHERE "+quote(FIELD_TRANSFORMATION_ID_DIRECTORY)+" IS NULL");

		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANS_ATTRIBUTE
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_TRANS_ATTRIBUTE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE, false);

		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_TRANS_ATTRIBUTE_LOOKUP";
				keyfield = new String[] { FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, FIELD_TRANS_ATTRIBUTE_CODE, FIELD_TRANS_ATTRIBUTE_NR };

				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DEPENDENCY
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_DEPENDENCY);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_DEPENDENCY_ID_DEPENDENCY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DEPENDENCY_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DEPENDENCY_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_DEPENDENCY_TABLE_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_DEPENDENCY_FIELD_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_DEPENDENCY_ID_DEPENDENCY, false);

		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_PARTITION_SCHEMA
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_PARTITION_SCHEMA);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_SCHEMA_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_PARTITION
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_PARTITION);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_ID_PARTITION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_PARTITION_PARTITION_ID, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_PARTITION_ID_PARTITION, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_PARTITION_SCHEMA
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_TRANS_PARTITION_SCHEMA);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);


        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_CLUSTER
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_CLUSTER);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_BASE_PORT, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_SOCKETS_BUFFER_SIZE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_SOCKETS_COMPRESSED, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_CLUSTER_ID_CLUSTER, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_SLAVE);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN));
        sql = database.getDDL(tablename, table, null, false, FIELD_SLAVE_ID_SLAVE, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_CLUSTER_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_CLUSTER_SLAVE);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_SLAVE_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_CLUSTER_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_TRANS_SLAVE);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_SLAVE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);


        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_CLUSTER
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_TRANS_CLUSTER);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = quote(TABLE_R_TRANS_SLAVE);
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta(FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_SLAVE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(FIELD_TRANS_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

		//
		// R_TRANS_HOP
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_TRANS_HOP);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_TRANS_HOP_ID_TRANS_HOP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_HOP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_HOP_ID_STEP_FROM, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_HOP_ID_STEP_TO, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_TRANS_HOP_ID_TRANS_HOP, false);

		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_TRANS_STEP_CONDITION
		//
		table = new RowMeta();
		tablename = quote(TABLE_R_TRANS_STEP_CONDITION);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_STEP_CONDITION_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_STEP_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_CONDITION
		//
		table = new RowMeta();
		tablename = quote(TABLE_R_CONDITION);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_ID_CONDITION_PARENT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_NEGATED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_OPERATOR, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_LEFT_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_CONDITION_FUNCTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_RIGHT_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_CONDITION_ID_VALUE_RIGHT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_CONDITION_ID_CONDITION, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_VALUE
		//
		tablename = quote(TABLE_R_VALUE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(FIELD_VALUE_ID_VALUE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_VALUE_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_VALUE_VALUE_TYPE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_VALUE_VALUE_STR, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_VALUE_IS_NULL, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_VALUE_ID_VALUE, false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_TYPE
		//
		// Create table...
		boolean ok_step_type = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_STEP_TYPE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_STEP_TYPE_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_TYPE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_TYPE_HELPTEXT, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_STEP_TYPE", false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_step_type)
		{
			updateStepTypes();
            if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP
		//
		// Create table
		table = new RowMeta();
		tablename = quote(TABLE_R_STEP);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_STEP_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_DISTRIBUTE, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_COPIES, ValueMetaInterface.TYPE_INTEGER, 3, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_STEP_ID_STEP, false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_ATTRIBUTE
		//
		// Create table...
		tablename = quote(TABLE_R_STEP_ATTRIBUTE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE, false);
        
		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LOOKUP";
				keyfield = new String[] { FIELD_STEP_ATTRIBUTE_ID_STEP, FIELD_STEP_ATTRIBUTE_CODE, FIELD_STEP_ATTRIBUTE_NR, };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_DATABASE
		//
		// Keeps the links between transformation steps and databases.
		// That way investigating dependencies becomes easier to program.
		//
		// Create table...
		tablename = quote(TABLE_R_STEP_DATABASE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(FIELD_STEP_DATABASE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_DATABASE_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_STEP_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);
        
		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LU1";
				keyfield = new String[] { FIELD_STEP_DATABASE_ID_TRANSFORMATION, };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, false, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LU2";
				keyfield = new String[] { FIELD_STEP_DATABASE_ID_DATABASE, };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, false, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANS_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_TRANS_NOTE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_TRANS_NOTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_TRANS_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOGLEVEL
		//
		// Create table...
		boolean ok_loglevel = true;
		tablename = quote(TABLE_R_LOGLEVEL);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(FIELD_LOGLEVEL_ID_LOGLEVEL, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOGLEVEL_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOGLEVEL_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_LOGLEVEL_ID_LOGLEVEL, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_loglevel)
		{
			//
			// Populate with data...
			//
			code = LogWriter.logLevelDescription;
			desc = LogWriter.log_level_desc_long;

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_LOGLEVEL_ID_LOGLEVEL)+" FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextLoglevelID();

					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(FIELD_LOGLEVEL_ID_LOGLEVEL, ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta(FIELD_LOGLEVEL_CODE, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(FIELD_LOGLEVEL_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);

					database.setValuesInsert(tableData.getRowMeta(), tableData.getData());
					database.insertRow();
				}
			}
            
            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOG
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_LOG);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_LOG_ID_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_ID_LOGLEVEL, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_LOGTYPE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_FILENAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_FILEEXTENTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_ADD_DATE, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_ADD_TIME, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_LOG_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_LOG_ID_LOG, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_JOB);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_JOB_VERSION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_JOB_STATUS, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_CREATED_USER, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_CREATED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_MODIFIED_USER, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_USE_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_PASS_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(FIELD_JOB_SHARED_FILE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0)); // 255 max length for now.
		sql = database.getDDL(tablename, table, null, false, FIELD_JOB_ID_JOB, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_TYPE
		//
		// Create table...
		boolean ok_jobentry_type = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_JOBENTRY_TYPE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_TYPE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_jobentry_type)
		{
			//
			// Populate with data...
			//
			updateJobEntryTypes();
            if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_JOBENTRY);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_JOBENTRY_ID_JOBENTRY, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_COPY
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_JOBENTRY_COPY);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_NR, ValueMetaInterface.TYPE_INTEGER, 4, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_COPY_PARALLEL, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_ATTRIBUTE
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_JOBENTRY_ATTRIBUTE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_NUMBER, 13, 2));
		table.addValueMeta(new ValueMeta(FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LOOKUP";
				keyfield = new String[] { FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, FIELD_JOBENTRY_ATTRIBUTE_CODE, FIELD_JOBENTRY_ATTRIBUTE_NR, };
	
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_HOP
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_JOB_HOP);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_ID_JOB_HOP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_EVALUATION, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_HOP_UNCONDITIONAL, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_JOB_HOP_ID_JOB_HOP, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = quote(TABLE_R_JOB_NOTE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_JOB_NOTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_JOB_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////////
		//
		//  User tables...
		//
		///////////////////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE
		//
		// Create table...
        Map<String, Long> profiles = new Hashtable<String, Long>();
        
		boolean ok_profile = true;
		tablename = quote(TABLE_R_PROFILE);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_PROFILE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_PROFILE_ID_PROFILE, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_profile)
		{
			//
			// Populate with data...
			//
			code = new String[] { "Administrator", "User", "Read-only" };
			desc = new String[] { "Administrator profile, manage users", "Normal user, all tools", "Read-only users" };

			database.prepareInsert(table, tablename);

			for (int i = 0; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_PROFILE_ID_PROFILE)+" FROM " + tablename + " WHERE "
                		+ quote(FIELD_PROFILE_NAME) + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextProfileID();

					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta(FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(FIELD_PROFILE_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);

					database.setValuesInsert(tableData);
					database.insertRow();
                    if (log.isDetailed()) log.logDetailed(toString(), "Inserted new row into table "+tablename+" : "+table);
                    profiles.put(code[i], new Long(nextid));
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_USER
		//
		// Create table...
        Map<String, Long> users = new Hashtable<String, Long>();
		boolean ok_user = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_USER);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_USER_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_USER_LOGIN, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_USER_PASSWORD, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_USER_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_USER_ENABLED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_USER_ID_USER, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_user)
		{
			//
			// Populate with data...
			//
			user = new String[] { "admin", "guest" };
			pass = new String[] { "admin", "guest" };
			code = new String[] { "Administrator", "Guest account" };
			desc = new String[] { "User manager", "Read-only guest account" };
			prof = new String[] { "Administrator", "Read-only" };

			database.prepareInsert(table, tablename);

			for (int i = 0; i < user.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_USER_ID_USER)+" FROM " + tablename + " WHERE "
                		+ quote(FIELD_USER_LOGIN) + " = '" + user[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextUserID();
					String password = Encr.encryptPassword(pass[i]);
                    
                    Long profileID = (Long)profiles.get( prof[i] );
                    long id_profile = -1L;
                    if (profileID!=null) id_profile = profileID.longValue();
                    
					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(nextid));
                    tableData.addValue(new ValueMeta(FIELD_USER_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_profile));
                    tableData.addValue(new ValueMeta(FIELD_USER_LOGIN, ValueMetaInterface.TYPE_STRING), user[i]);
                    tableData.addValue(new ValueMeta(FIELD_USER_PASSWORD, ValueMetaInterface.TYPE_STRING), password);
                    tableData.addValue(new ValueMeta(FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(FIELD_USER_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);
                    tableData.addValue(new ValueMeta(FIELD_USER_ENABLED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.TRUE);

					database.setValuesInsert(tableData);
					database.insertRow();
                    users.put(user[i], new Long(nextid));
				}
			}
            
            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PERMISSION
		//
		// Create table...
        Map<String, Long> permissions = new Hashtable<String, Long>();
		boolean ok_permission = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_PERMISSION);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_PERMISSION_CODE, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(FIELD_PERMISSION_DESCRIPTION, ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, FIELD_PERMISSION_ID_PERMISSION, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_permission)
		{
			//
			// Populate with data...
			//
			code = PermissionMeta.permissionTypeCode;
			desc = PermissionMeta.permissionTypeDesc;

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_PERMISSION_ID_PERMISSION)+" FROM " + tablename + " WHERE " 
                		+ quote(FIELD_PERMISSION_CODE) + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextPermissionID();

                    RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(FIELD_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta(FIELD_PERMISSION_CODE, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(FIELD_PERMISSION_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);

					database.setValuesInsert(tableData);
					database.insertRow();
                    if (log.isDetailed()) log.logDetailed(toString(), "Inserted new row into table "+tablename+" : "+table);
                    permissions.put(code[i], new Long(nextid));
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE_PERMISSION
		//
		// Create table...
		boolean ok_profile_permission = true;
		table = new RowMeta();
		tablename = quote(TABLE_R_PROFILE_PERMISSION);
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta(FIELD_PROFILE_PERMISSION_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(FIELD_PROFILE_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_PK";
				keyfield = new String[] { FIELD_PROFILE_PERMISSION_ID_PROFILE, FIELD_PROFILE_PERMISSION_ID_PERMISSION, };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_profile_permission)
		{
			database.prepareInsert(table, tablename);

			// Administrator default:
            Long profileID = (Long)profiles.get( "Administrator");
            long id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
			
            if (log.isDetailed()) log.logDetailed(toString(), "Administrator profile id = "+id_profile);
            String perms[] = new String[]
				{ 
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_ADMIN],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_JOB],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
				};
			
			for (int i=0;i < perms.length ; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();
                
                if (log.isDetailed()) log.logDetailed(toString(), "Permission id for '"+perms[i]+"' = "+id_permission);

				RowMetaAndData lookup = null;
                if (upgrade) 
                {
                    String lookupSQL = "SELECT "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+
                                   " FROM " + tablename + 
                                   " WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+"=" + id_profile + " AND +"+quote(FIELD_PROFILE_PERMISSION_ID_PERMISSION)+"=" + id_permission;
                    if (log.isDetailed()) log.logDetailed(toString(), "Executing SQL: "+lookupSQL);
                    lookup = database.getOneRow(lookupSQL);
                }
				if (lookup == null) // if the combination is not yet there, insert...
				{
                    String insertSQL="INSERT INTO "+tablename+"("+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+", "+quote(FIELD_PROFILE_PERMISSION_ID_PERMISSION)+")"
                    			+" VALUES("+id_profile+","+id_permission+")";
					database.execStatement(insertSQL);
                    if (log.isDetailed()) log.logDetailed(toString(), "insertSQL = ["+insertSQL+"]");
				}
				else
				{
                    if (log.isDetailed()) log.logDetailed(toString(), "Found id_profile="+id_profile+", id_permission="+id_permission);
				}
			}

			// User profile
            profileID = (Long)profiles.get( "User" );
            id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
            
            if (log.isDetailed()) log.logDetailed(toString(), "User profile id = "+id_profile);
            perms = new String[]
                { 
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_JOB],
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
                };

            for (int i = 0; i < perms.length; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();

                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+
                			" FROM " + tablename + 
                			" WHERE "+quote(FIELD_PROFILE_PERMISSION_ID_PROFILE)+"=" + id_profile + " AND "+quote(FIELD_PROFILE_PERMISSION_ID_PERMISSION)+"=" + id_permission);
				if (lookup == null) // if the combination is not yet there, insert...
				{
					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(FIELD_PROFILE_PERMISSION_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));
                    tableData.addValue(new ValueMeta(FIELD_PROFILE_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER), new Long(id_permission));

					database.setValuesInsert(tableData);
					database.insertRow();
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
        
		if (monitor!=null) monitor.worked(1);
		if (monitor!=null) monitor.done();
        
        log.logBasic(toString(), (upgrade?"Upgraded":"Created")+ " "+repositoryTableNames.length+" repository tables.");

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
				database.execStatement("DROP TABLE " + repositoryTableNames[i]);
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
	 * @throws KettleException if the update didn't go as planned.
	 */
	public synchronized void updateStepTypes() throws KettleException
	{
		// We should only do an update if something has changed...
		for (int i = 0; i < steploader.nrStepsWithType(StepPlugin.TYPE_ALL); i++)
		{
			StepPlugin sp = steploader.getStepWithType(StepPlugin.TYPE_ALL, i);
			long id = getStepTypeID(sp.getID()[0]);
			if (id < 0) // Not found, we need to add this one...
			{
				// We need to add this one ...
				id = getNextStepTypeID();

				RowMetaAndData table = new RowMetaAndData();
				table.addValue(new ValueMeta(FIELD_STEP_TYPE_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
				table.addValue(new ValueMeta(FIELD_STEP_TYPE_CODE, ValueMetaInterface.TYPE_STRING), sp.getID()[0]);
				table.addValue(new ValueMeta(FIELD_STEP_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING), sp.getDescription());
				table.addValue(new ValueMeta(FIELD_STEP_TYPE_HELPTEXT, ValueMetaInterface.TYPE_STRING), sp.getTooltip());

				database.prepareInsert(table.getRowMeta(), TABLE_R_STEP_TYPE);

				database.setValuesInsert(table);
				database.insertRow();
				database.closeInsert();
			}
		}
	}
	
	
	/**
	 * Update the list in R_JOBENTRY_TYPE 
	 * 
	 * @exception KettleException if something went wrong during the update.
	 */
	public synchronized void updateJobEntryTypes() throws KettleException
	{
        // We should only do an update if something has changed...
        JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
        JobPlugin[] jobPlugins = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_ALL);
        
        for (int i = 0; i < jobPlugins.length; i++)
        {
            String type_desc = jobPlugins[i].getID();
            String type_desc_long = jobPlugins[i].getDescription();
            long id = getJobEntryTypeID(type_desc);
            if (id < 0) // Not found, we need to add this one...
            {
                // We need to add this one ...
                id = getNextJobEntryTypeID();

                RowMetaAndData table = new RowMetaAndData();
                table.addValue(new ValueMeta(FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
                table.addValue(new ValueMeta(FIELD_JOBENTRY_TYPE_CODE, ValueMetaInterface.TYPE_STRING), type_desc);
                table.addValue(new ValueMeta(FIELD_JOBENTRY_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING), type_desc_long);

                database.prepareInsert(table.getRowMeta(), TABLE_R_JOBENTRY_TYPE);

                database.setValuesInsert(table);
                database.insertRow();
                database.closeInsert();
            }
        }
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
            database.lockTables( new String[] { TABLE_R_REPOSITORY_LOG } );
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
            database.unlockTables(new String[] { TABLE_R_REPOSITORY_LOG });
        }
    }
    
    public synchronized void exportAllObjects(IProgressMonitor monitor, String xmlFilename, RepositoryDirectory root) throws KettleException
    {
        if (monitor!=null) monitor.beginTask("Exporting the repository to XML...", 3);
        
        root = ((null == root) ? getDirectoryTree() : root);
        
        StringBuffer xml = new StringBuffer(XMLHandler.getXMLHeader()); 
        xml.append("<repository>"+Const.CR+Const.CR);

        // Dump the transformations...
        xml.append("<transformations>"+Const.CR);
        xml.append(exportTransformations(monitor, root));
        xml.append("</transformations>"+Const.CR);

        // Now dump the jobs...
        xml.append("<jobs>"+Const.CR);
        xml.append(exportJobs(monitor, root));
        xml.append("</jobs>"+Const.CR);

        xml.append("</repository>"+Const.CR+Const.CR);
        
        if (monitor!=null) monitor.worked(1);

        if (monitor==null || (monitor!=null && !monitor.isCanceled()))
        {
            if (monitor!=null) monitor.subTask("Saving XML to file ["+xmlFilename+"]");

            try
            {
                OutputStream os = KettleVFS.getOutputStream(xmlFilename, false);
                os.write(xml.toString().getBytes(Const.XML_ENCODING));
                os.close();
            }
            catch(IOException e)
            {
                System.out.println("Couldn't create file ["+xmlFilename+"]");
            }
            if (monitor!=null) monitor.worked(1);
        }
        
        if (monitor!=null) monitor.done();
    }

    private String exportJobs(IProgressMonitor monitor, RepositoryDirectory dirTree) throws KettleException
    {
        StringBuffer xml = new StringBuffer();
        
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
                    
                    xml.append(ji.getXML()+Const.CR);
                }
                catch(KettleException ke)
                {
                    log.logError(toString(), "An error occurred reading job ["+jobs[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
                    log.logError(toString(), "Job ["+jobs[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
                }
            }
            
            // OK, then export the jobs in the sub-directories as well!
            if (repdir.getID()!=dirTree.getID()) exportJobs(null, repdir);
        }

        return xml.toString();
    }

    private String exportTransformations(IProgressMonitor monitor, RepositoryDirectory dirTree) throws KettleException
    {
        StringBuffer xml = new StringBuffer();
        
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
                    
                    xml.append(ti.getXML()+Const.CR);
                }
                catch(KettleException ke)
                {
                    log.logError(toString(), "An error occurred reading transformation ["+trans[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
                    log.logError(toString(), "Transformation ["+trans[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
                }
            }
            
            // OK, then export the transformations in the sub-directories as well!
            if (repdir.getID()!=dirTree.getID()) exportTransformations(null, repdir);
        }
        if (monitor!=null) monitor.worked(1);
        
        return xml.toString();
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
            DatabaseMeta databaseMeta = new DatabaseMeta(this, databaseIDs[i]);
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
	
	private String quote(String identifier) {
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
			DatabaseMeta databaseMeta = new DatabaseMeta(this, ids[i]);
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
}