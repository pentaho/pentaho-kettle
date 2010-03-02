package org.pentaho.di.repository.kdr;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.RepositoryDirectory;

public class KettleDatabaseRepositoryBase {

//	private static Class<?> PKG = Repository.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
	public static final String FIELD_NOTE_FONT_NAME			  = "FONT_NAME";
	public static final String FIELD_NOTE_FONT_SIZE			  = "FONT_SIZE";
	public static final String FIELD_NOTE_FONT_BOLD			  = "FONT_BOLD";
	public static final String FIELD_NOTE_FONT_ITALIC		  = "FONT_ITALIC";
	public static final String FIELD_NOTE_COLOR_RED		  	  = "FONT_COLOR_RED";
	public static final String FIELD_NOTE_COLOR_GREEN		  = "FONT_COLOR_GREEN";
	public static final String FIELD_NOTE_COLOR_BLUE		  = "FONT_COLOR_BLUE";
	public static final String FIELD_NOTE_BACK_GROUND_COLOR_RED		  = "FONT_BACK_GROUND_COLOR_RED";
	public static final String FIELD_NOTE_BACK_GROUND_COLOR_GREEN	  = "FONT_BACK_GROUND_COLOR_GREEN";
	public static final String FIELD_NOTE_BACK_GROUND_COLOR_BLUE	  = "FONT_BACK_GROUND_COLOR_BLUE";
	public static final String FIELD_NOTE_BORDER_COLOR_RED		      = "FONT_BORDER_COLOR_RED";
	public static final String FIELD_NOTE_BORDER_COLOR_GREEN		  = "FONT_BORDER_COLOR_GREEN";
	public static final String FIELD_NOTE_BORDER_COLOR_BLUE		      = "FONT_BORDER_COLOR_BLUE";
	public static final String FIELD_NOTE_DRAW_SHADOW		 		  = "DRAW_SHADOW";

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
	
	public static final String TABLE_R_USER                   = "R_USER";
	public static final String FIELD_USER_ID_USER = "ID_USER";
	public static final String FIELD_USER_LOGIN = "LOGIN";
	public static final String FIELD_USER_PASSWORD = "PASSWORD";
	public static final String FIELD_USER_NAME = "NAME";
	public static final String FIELD_USER_DESCRIPTION = "DESCRIPTION";
	public static final String FIELD_USER_ENABLED = "ENABLED";

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
	
	public static final String TABLE_R_JOBENTRY_DATABASE          = "R_JOBENTRY_DATABASE";
	public static final String FIELD_JOBENTRY_DATABASE_ID_JOB = "ID_JOB";
	public static final String FIELD_JOBENTRY_DATABASE_ID_JOBENTRY = "ID_JOBENTRY";
	public static final String FIELD_JOBENTRY_DATABASE_ID_DATABASE = "ID_DATABASE";
	
	public static final String TABLE_R_TRANS_LOCK = "R_TRANS_LOCK";
	public static final String FIELD_TRANS_LOCK_ID_TRANS_LOCK = "ID_TRANS_LOCK";
	public static final String FIELD_TRANS_LOCK_ID_TRANSFORMATION = "ID_TRANSFORMATION";
	public static final String FIELD_TRANS_LOCK_ID_USER = "ID_USER";
	public static final String FIELD_TRANS_LOCK_LOCK_MESSAGE = "LOCK_MESSAGE";
	public static final String FIELD_TRANS_LOCK_LOCK_DATE = "LOCK_DATE";

	public static final String TABLE_R_JOB_LOCK = "R_JOB_LOCK";
	public static final String FIELD_JOB_LOCK_ID_JOB_LOCK = "ID_JOB_LOCK";
	public static final String FIELD_JOB_LOCK_ID_JOB= "ID_JOB";
	public static final String FIELD_JOB_LOCK_ID_USER = "ID_USER";
	public static final String FIELD_JOB_LOCK_LOCK_MESSAGE = "LOCK_MESSAGE";
	public static final String FIELD_JOB_LOCK_LOCK_DATE = "LOCK_DATE";


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
    		, TABLE_R_JOBENTRY_DATABASE
    		, TABLE_R_JOBENTRY_TYPE
    		, TABLE_R_JOB_HOP
    		, TABLE_R_JOB_NOTE
    		, TABLE_R_LOG
    		, TABLE_R_LOGLEVEL
    		, TABLE_R_NOTE
    		, TABLE_R_PARTITION
    		, TABLE_R_PARTITION_SCHEMA
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
    		, TABLE_R_TRANS_LOCK
    		, TABLE_R_JOB_LOCK
         };

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
	public static final String TRANS_ATTRIBUTE_LOG_INTERVAL = "LOG_INTERVAL";
	public static final String TRANS_ATTRIBUTE_TRANSFORMATION_TYPE = "TRANSFORMATION_TYPE";
	
	public static final String TRANS_ATTRIBUTE_PARAM_KEY         = "PARAM_KEY";
	public static final String TRANS_ATTRIBUTE_PARAM_DEFAULT     = "PARAM_DEFAULT";
	public static final String TRANS_ATTRIBUTE_PARAM_DESCRIPTION = "PARAM_DESC";
	
	public static final String JOB_ATTRIBUTE_PARAM_KEY           = "PARAM_KEY";
	public static final String JOB_ATTRIBUTE_PARAM_DEFAULT       = "PARAM_DEFAULT";
	public static final String JOB_ATTRIBUTE_PARAM_DESCRIPTION   = "PARAM_DESC";	
	public static final String JOB_ATTRIBUTE_LOG_SIZE_LIMIT      = "LOG_SIZE_LIMIT";

	
	protected KettleDatabaseRepositoryMeta		repositoryMeta;

	protected RepositoryDirectory importBaseDirectory;

	protected LogChannelInterface log;

	protected boolean			connected;
   
    protected KettleDatabaseRepositoryCreationHelper creationHelper;

    public KettleDatabaseRepositoryBase() {
    }
	
	public KettleDatabaseRepositoryMeta getRepositoryMeta()
	{
		return repositoryMeta;
	}
	
	public void setRepositoryMeta(KettleDatabaseRepositoryMeta repositoryMeta) {
		this.repositoryMeta = repositoryMeta;
	}

	public String getName()
	{
		if (repositoryMeta == null)
			return null;
		return repositoryMeta.getName();
	}
	
	public LogChannelInterface getLog() {
		return log;
	}

    /**
     * @return If the repository is in a connected state.
     */
    public boolean isConnected()
    {
        return connected;
    }

    // Utility classes
    
    public String quote(String identifier) {
		return repositoryMeta.getConnection().quoteField(identifier);
	}

    public String quoteTable(String table) {
    	return repositoryMeta.getConnection().getQuotedSchemaTableCombination(null, table);
    }

	/**
	 * @param connected the connected to set
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}


}
