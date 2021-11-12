// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.http.conn.util.InetAddressUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.support.encryption.Encr;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * This class is used to define a number of default values for various settings throughout Kettle. It also contains a
 * number of static final methods to make your life easier.
 *
 * @author Matt
 * @since 07-05-2003
 *
 */
public class Const {
  private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!

  /**
   * Version number
   *
   * @deprecated Use {@link BuildVersion#getVersion()} instead
   */
  @Deprecated
  public static final String VERSION = BuildVersion.getInstance().getVersion();

  /**
   * Copyright year
   */
  public static final String COPYRIGHT_YEAR = "2015";

  /**
   * Release Type
   */
  public enum ReleaseType {
    RELEASE_CANDIDATE {
      public String getMessage() {
        return BaseMessages.getString( PKG, "Const.PreviewRelease.HelpAboutText" );
      }
    },
    MILESTONE {
      public String getMessage() {
        return BaseMessages.getString( PKG, "Const.Candidate.HelpAboutText" );
      }
    },
    PREVIEW {
      public String getMessage() {
        return BaseMessages.getString( PKG, "Const.Milestone.HelpAboutText" );
      }
    },
    GA {
      public String getMessage() {
        return BaseMessages.getString( PKG, "Const.GA.HelpAboutText" );
      }
    };
    public abstract String getMessage();
  }

  /**
   * Sleep time waiting when buffer is empty (the default)
   */
  public static final int TIMEOUT_GET_MILLIS = 50;

  /**
   * Sleep time waiting when buffer is full (the default)
   */
  public static final int TIMEOUT_PUT_MILLIS = 50;

  /**
   * print update every ... lines
   */
  public static final int ROWS_UPDATE = 50000;

  /**
   * Size of rowset: bigger = faster for large amounts of data
   */
  public static final int ROWS_IN_ROWSET = 10000;

  /**
   * Fetch size in rows when querying a database
   */
  public static final int FETCH_SIZE = 10000;

  /**
   * Sort size: how many rows do we sort in memory at once?
   */
  public static final int SORT_SIZE = 5000;

  /**
   * job/trans heartbeat scheduled executor periodic interval ( in seconds )
   */
  public static final int HEARTBEAT_PERIODIC_INTERVAL_IN_SECS = 10;

  /**
   * What's the file systems file separator on this operating system?
   */
  public static final String FILE_SEPARATOR = System.getProperty( "file.separator" );

  /**
   * What's the path separator on this operating system?
   */
  public static final String PATH_SEPARATOR = System.getProperty( "path.separator" );

  /**
   * CR: operating systems specific Carriage Return
   */
  public static final String CR = System.getProperty( "line.separator" );

  /**
   * DOSCR: MS-DOS specific Carriage Return
   */
  public static final String DOSCR = "\n\r";

  /**
   * An empty ("") String.
   */
  public static final String EMPTY_STRING = "";

  /**
   * The Java runtime version
   */
  public static final String JAVA_VERSION = System.getProperty( "java.vm.version" );

  /**
   * Path to the users home directory (keep this entry above references to getKettleDirectory())
   *
   * @deprecated Use {@link Const#getUserHomeDirectory()} instead.
   */
  @Deprecated
  public static final String USER_HOME_DIRECTORY = NVL( System.getProperty( "KETTLE_HOME" ), System
    .getProperty( "user.home" ) );

  /**
   * Path to the simple-jndi directory
   */

  public static String JNDI_DIRECTORY = NVL( System.getProperty( "KETTLE_JNDI_ROOT" ), System
    .getProperty( "org.osjava.sj.root" ) );

  /*
   * The images directory
   *
   * public static final String IMAGE_DIRECTORY = "/ui/images/";
   */

  public static final String PLUGIN_BASE_FOLDERS_PROP = "KETTLE_PLUGIN_BASE_FOLDERS";
  /**
   * the default comma separated list of base plugin folders.
   */
  public static final String DEFAULT_PLUGIN_BASE_FOLDERS = "plugins,"
    + ( Utils.isEmpty( getDIHomeDirectory() ) ? "" : getDIHomeDirectory() + FILE_SEPARATOR + "plugins," )
    + getKettleDirectory() + FILE_SEPARATOR + "plugins";

  /**
   * Default minimum date range...
   */
  public static final Date MIN_DATE = new Date( -2208992400000L ); // 1900/01/01 00:00:00.000

  /**
   * Default maximum date range...
   */
  public static final Date MAX_DATE = new Date( 7258114799468L ); // 2199/12/31 23:59:59.999

  /**
   * The default minimum year in a dimension date range
   */
  public static final int MIN_YEAR = 1900;

  /**
   * The default maximum year in a dimension date range
   */
  public static final int MAX_YEAR = 2199;

  /**
   * Specifies the number of pixels to the right we have to go in dialog boxes.
   */
  public static final int RIGHT = 400;

  /**
   * Specifies the length (width) of fields in a number of pixels in dialog boxes.
   */
  public static final int LENGTH = 350;

  /**
   * The margin between the different dialog components & widgets
   */
  public static final int MARGIN = 4;

  /**
   * The default percentage of the width of screen where we consider the middle of a dialog.
   */
  public static final int MIDDLE_PCT = 35;

  /**
   * The default width of an arrow in the Graphical Views
   */
  public static final int ARROW_WIDTH = 1;

  /**
   * The horizontal and vertical margin of a dialog box.
   */
  public static final int FORM_MARGIN = 5;

  /**
   * The default shadow size on the graphical view.
   */
  public static final int SHADOW_SIZE = 0;

  /**
   * The size of relationship symbols
   */
  public static final int SYMBOLSIZE = 10;

  /**
   * Max nr. of files to remember
   */
  public static final int MAX_FILE_HIST = 9; // Having more than 9 files in the file history is not compatible with pre
                                             // 5.0 versions

  /**
   * The default locale for the kettle environment (system defined)
   */
  public static final Locale DEFAULT_LOCALE = Locale.getDefault();

  /**
   * The default decimal separator . or ,
   */
  public static final char DEFAULT_DECIMAL_SEPARATOR = ( new DecimalFormatSymbols( DEFAULT_LOCALE ) )
    .getDecimalSeparator();

  /**
   * The default grouping separator , or .
   */
  public static final char DEFAULT_GROUPING_SEPARATOR = ( new DecimalFormatSymbols( DEFAULT_LOCALE ) )
    .getGroupingSeparator();

  /**
   * The default currency symbol
   */
  public static final String DEFAULT_CURRENCY_SYMBOL = ( new DecimalFormatSymbols( DEFAULT_LOCALE ) )
    .getCurrencySymbol();

  /**
   * The default number format
   */
  public static final String DEFAULT_NUMBER_FORMAT = ( (DecimalFormat) ( NumberFormat.getInstance() ) )
    .toPattern();

  /**
   * Default string representing Null String values (empty)
   */
  public static final String NULL_STRING = "";

  /**
   * Default string representing Null Number values (empty)
   */
  public static final String NULL_NUMBER = "";

  /**
   * Default string representing Null Date values (empty)
   */
  public static final String NULL_DATE = "";

  /**
   * Default string representing Null BigNumber values (empty)
   */
  public static final String NULL_BIGNUMBER = "";

  /**
   * Default string representing Null Boolean values (empty)
   */
  public static final String NULL_BOOLEAN = "";

  /**
   * Default string representing Null Integer values (empty)
   */
  public static final String NULL_INTEGER = "";

  /**
   * Default string representing Null Binary values (empty)
   */
  public static final String NULL_BINARY = "";

  /**
   * Default string representing Null Undefined values (empty)
   */
  public static final String NULL_NONE = "";

  /**
   * Rounding mode, not implemented in {@code BigDecimal}. Method java.lang.Math.round(double) processes this way. <br/>
   * Rounding mode to round towards {@literal "nearest neighbor"} unless both neighbors are equidistant, in which case
   * round ceiling. <br/>
   * Behaves as for {@code ROUND_CEILING} if the discarded fraction is &ge; 0.5; otherwise, behaves as for
   * {@code ROUND_FLOOR}. Note that this is the most common arithmetical rounding mode.
   */
  public static final int ROUND_HALF_CEILING = -1;

  /**
   * The base name of the Chef logfile
   */
  public static final String CHEF_LOG_FILE = "chef";

  /**
   * The base name of the Spoon logfile
   */
  public static final String SPOON_LOG_FILE = "spoon";

  /**
   * The base name of the Menu logfile
   */
  public static final String MENU_LOG_FILE = "menu";

  /**
   * An array of date conversion formats
   */
  private static String[] dateFormats;

  /**
   * An array of date (timeless) conversion formats
   */
  private static String[] dateTimelessFormats;

  /**
   * An array of number conversion formats
   */
  private static String[] numberFormats;

  /**
   * Generalized date/time format: Wherever dates are used, date and time values are organized from the most to the
   * least significant. see also method StringUtil.getFormattedDateTime()
   */
  public static final String GENERALIZED_DATE_TIME_FORMAT = "yyyyddMM_hhmmss";
  public static final String GENERALIZED_DATE_TIME_FORMAT_MILLIS = "yyyyddMM_hhmmssSSS";

  /**
   * Default we store our information in Unicode UTF-8 character set.
   */
  public static final String XML_ENCODING = "UTF-8";

  /** The possible extensions a transformation XML file can have. */
  public static final String[] STRING_TRANS_AND_JOB_FILTER_EXT = new String[] {
    "*.ktr;*.kjb;*.xml", "*.ktr;*.xml", "*.kjb;*.xml", "*.xml", "*.*" };

  /** The descriptions of the possible extensions a transformation XML file can have. */
  private static String[] STRING_TRANS_AND_JOB_FILTER_NAMES;

  /** The extension of a Kettle transformation XML file */
  public static final String STRING_TRANS_DEFAULT_EXT = "ktr";

  /** The possible extensions a transformation XML file can have. */
  public static final String[] STRING_TRANS_FILTER_EXT = new String[] { "*.ktr;*.xml", "*.xml", "*.*" };

  /** The descriptions of the possible extensions a transformation XML file can have. */
  private static String[] STRING_TRANS_FILTER_NAMES;

  /** The extension of a Kettle job XML file */
  public static final String STRING_JOB_DEFAULT_EXT = "kjb";

  /** The possible extensions a job XML file can have. */
  public static final String[] STRING_JOB_FILTER_EXT = new String[] { "*.kjb;*.xml", "*.xml", "*.*" };

  /** The descriptions of the possible extensions a job XML file can have. */
  private static String[] STRING_JOB_FILTER_NAMES;

  /** Name of the kettle parameters file */
  public static final String KETTLE_PROPERTIES = "kettle.properties";

  /** Name of the kettle shared data file */
  public static final String SHARED_DATA_FILE = "shared.xml";

  /** The prefix that all internal kettle variables should have */
  public static final String INTERNAL_VARIABLE_PREFIX = "Internal";

  /** The version number as an internal variable */
  public static final String INTERNAL_VARIABLE_KETTLE_VERSION = INTERNAL_VARIABLE_PREFIX + ".Kettle.Version";

  /** The build version as an internal variable */
  public static final String INTERNAL_VARIABLE_KETTLE_BUILD_VERSION = INTERNAL_VARIABLE_PREFIX
    + ".Kettle.Build.Version";

  /** The build date as an internal variable */
  public static final String INTERNAL_VARIABLE_KETTLE_BUILD_DATE = INTERNAL_VARIABLE_PREFIX + ".Kettle.Build.Date";

  /** The job filename directory */
  public static final String INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY = INTERNAL_VARIABLE_PREFIX
    + ".Job.Filename.Directory";

  /** The job filename name */
  public static final String INTERNAL_VARIABLE_JOB_FILENAME_NAME = INTERNAL_VARIABLE_PREFIX + ".Job.Filename.Name";

  /** The job name */
  public static final String INTERNAL_VARIABLE_JOB_NAME = INTERNAL_VARIABLE_PREFIX + ".Job.Name";

  /** The job directory */
  public static final String INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY = INTERNAL_VARIABLE_PREFIX
    + ".Job.Repository.Directory";

  /** The job run ID */
  public static final String INTERNAL_VARIABLE_JOB_RUN_ID = INTERNAL_VARIABLE_PREFIX + ".Job.Run.ID";

  /** The job run attempt nr */
  public static final String INTERNAL_VARIABLE_JOB_RUN_ATTEMPTNR = INTERNAL_VARIABLE_PREFIX + ".Job.Run.AttemptNr";

  /** job/trans heartbeat scheduled executor periodic interval ( in seconds ) */
  public static final String VARIABLE_HEARTBEAT_PERIODIC_INTERVAL_SECS = "heartbeat.periodic.interval.seconds";

  /** comma-separated list of extension point plugins for which snmp traps should be sent */
  public static final String VARIABLE_MONITORING_SNMP_TRAPS_ENABLED = "monitoring.snmp.traps.enabled";

  /** The current transformation directory */
  public static final String INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY = INTERNAL_VARIABLE_PREFIX
    + ".Entry.Current.Directory";

  /**
   * All the internal transformation variables
   */
  public static final String[] INTERNAL_TRANS_VARIABLES = new String[] {
    Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY,
    Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY,
    Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME,
    Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY,
  };

  /**
   * All the internal job variables
   */
  public static final String[] INTERNAL_JOB_VARIABLES = new String[] {
    Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY,
    Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME,
    Const.INTERNAL_VARIABLE_JOB_NAME, Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY,
    Const.INTERNAL_VARIABLE_JOB_RUN_ID, Const.INTERNAL_VARIABLE_JOB_RUN_ATTEMPTNR, };

  /*
   * Deprecated variables array.
   * Variables in this array will display with the prefix (deprecated) and will be moved
   * at the bottom of the variables dropdown when pressing ctrl+space
   * */
  public static final String[] DEPRECATED_VARIABLES = new String[] {
    Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY,
    Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME,
    Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY,
    Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY,
    Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME,
    Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY
  };

  /** The transformation filename directory */
  public static final String INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY = INTERNAL_VARIABLE_PREFIX
    + ".Transformation.Filename.Directory";

  /** The transformation filename name */
  public static final String INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME = INTERNAL_VARIABLE_PREFIX
    + ".Transformation.Filename.Name";

  /** The transformation name */
  public static final String INTERNAL_VARIABLE_TRANSFORMATION_NAME = INTERNAL_VARIABLE_PREFIX
    + ".Transformation.Name";

  /** The transformation directory */
  public static final String INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY = INTERNAL_VARIABLE_PREFIX
    + ".Transformation.Repository.Directory";

  /** The step partition ID */
  public static final String INTERNAL_VARIABLE_STEP_PARTITION_ID = INTERNAL_VARIABLE_PREFIX + ".Step.Partition.ID";

  /** The step partition number */
  public static final String INTERNAL_VARIABLE_STEP_PARTITION_NR = INTERNAL_VARIABLE_PREFIX
    + ".Step.Partition.Number";

  /** The slave transformation number */
  public static final String INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER = INTERNAL_VARIABLE_PREFIX
    + ".Slave.Transformation.Number";

  /** The slave transformation name */
  public static final String INTERNAL_VARIABLE_SLAVE_SERVER_NAME = INTERNAL_VARIABLE_PREFIX + ".Slave.Server.Name";

  /** The size of the cluster : number of slaves */
  public static final String INTERNAL_VARIABLE_CLUSTER_SIZE = INTERNAL_VARIABLE_PREFIX + ".Cluster.Size";

  /** The slave transformation number */
  public static final String INTERNAL_VARIABLE_STEP_UNIQUE_NUMBER = INTERNAL_VARIABLE_PREFIX
    + ".Step.Unique.Number";

  /** Is this transformation running clustered, on the master? */
  public static final String INTERNAL_VARIABLE_CLUSTER_MASTER = INTERNAL_VARIABLE_PREFIX + ".Cluster.Master";

  /**
   * The internal clustered run ID, unique across a clustered execution, important while doing parallel clustered runs
   */
  public static final String INTERNAL_VARIABLE_CLUSTER_RUN_ID = INTERNAL_VARIABLE_PREFIX + ".Cluster.Run.ID";

  /** The size of the cluster : number of slaves */
  public static final String INTERNAL_VARIABLE_STEP_UNIQUE_COUNT = INTERNAL_VARIABLE_PREFIX + ".Step.Unique.Count";

  /** The step name */
  public static final String INTERNAL_VARIABLE_STEP_NAME = INTERNAL_VARIABLE_PREFIX + ".Step.Name";

  /** The step copy nr */
  public static final String INTERNAL_VARIABLE_STEP_COPYNR = INTERNAL_VARIABLE_PREFIX + ".Step.CopyNr";

  /** The default maximum for the nr of lines in the GUI logs */
  public static final int MAX_NR_LOG_LINES = 5000;

  /** The default maximum for the nr of lines in the history views */
  public static final int MAX_NR_HISTORY_LINES = 50;

  /** The default fetch size for lines of history. */
  public static final int HISTORY_LINES_FETCH_SIZE = 10;

  /** The default log line timeout in minutes : 12 hours */
  public static final int MAX_LOG_LINE_TIMEOUT_MINUTES = 12 * 60;

  /** UI-agnostic flag for warnings */
  public static final int WARNING = 1;

  /** UI-agnostic flag for warnings */
  public static final int ERROR = 2;

  /** UI-agnostic flag for warnings */
  public static final int INFO = 3;

  public static final int SHOW_MESSAGE_DIALOG_DB_TEST_DEFAULT = 0;

  public static final int SHOW_MESSAGE_DIALOG_DB_TEST_SUCCESS = 1;

  public static final int SHOW_FATAL_ERROR = 2;

  /**
   * The margin between the text of a note and its border.
   */
  public static final int NOTE_MARGIN = 5;

  /**
   * The default undo level for Kettle
   */
  public static final int MAX_UNDO = 100;

  /**
   * The file that documents these variables.
   */
  public static final String KETTLE_VARIABLES_FILE = "kettle-variables.xml";

  /**
   * If you set this environment variable you can limit the log size of all transformations and jobs that don't have the
   * "log size limit" property set in their respective properties.
   */
  public static final String KETTLE_LOG_SIZE_LIMIT = "KETTLE_LOG_SIZE_LIMIT";

  /**
   * The name of the variable that defines the log database connection by default for all transformations
   */
  public static final String KETTLE_TRANS_LOG_DB = "KETTLE_TRANS_LOG_DB";

  /**
   * The name of the variable that defines the logging schema for all transformations
   */
  public static final String KETTLE_TRANS_LOG_SCHEMA = "KETTLE_TRANS_LOG_SCHEMA";

  /**
   * The name of the variable that defines the logging table for all transformations
   */
  public static final String KETTLE_TRANS_LOG_TABLE = "KETTLE_TRANS_LOG_TABLE";

  /**
   * The name of the variable that defines the log database connection by default for all jobs
   */
  public static final String KETTLE_JOB_LOG_DB = "KETTLE_JOB_LOG_DB";

  /**
   * The name of the variable that defines the logging schema for all jobs
   */
  public static final String KETTLE_JOB_LOG_SCHEMA = "KETTLE_JOB_LOG_SCHEMA";

  /**
   * The name of the variable that defines the timer used for detecting slave nodes.
   */
  public static final String KETTLE_SLAVE_DETECTION_TIMER = "KETTLE_SLAVE_DETECTION_TIMER";

  /**
   * The name of the variable that defines the logging table for all jobs
   */
  public static final String KETTLE_JOB_LOG_TABLE = "KETTLE_JOB_LOG_TABLE";

  /**
   * The name of the variable that defines the transformation performance log schema by default for all transformations
   */
  public static final String KETTLE_TRANS_PERFORMANCE_LOG_DB = "KETTLE_TRANS_PERFORMANCE_LOG_DB";

  /**
   * The name of the variable that defines the transformation performance log database connection by default for all
   * transformations
   */
  public static final String KETTLE_TRANS_PERFORMANCE_LOG_SCHEMA = "KETTLE_TRANS_PERFORMANCE_LOG_SCHEMA";

  /**
   * The name of the variable that defines the transformation performance log table by default for all transformations
   */
  public static final String KETTLE_TRANS_PERFORMANCE_LOG_TABLE = "KETTLE_TRANS_PERFORMANCE_LOG_TABLE";

  /**
   * The name of the variable that defines the job entry log database by default for all jobs
   */
  public static final String KETTLE_JOBENTRY_LOG_DB = "KETTLE_JOBENTRY_LOG_DB";

  /**
   * The name of the variable that defines the job entry log schema by default for all jobs
   */
  public static final String KETTLE_JOBENTRY_LOG_SCHEMA = "KETTLE_JOBENTRY_LOG_SCHEMA";

  /**
   * The name of the variable that defines the job entry log table by default for all jobs
   */
  public static final String KETTLE_JOBENTRY_LOG_TABLE = "KETTLE_JOBENTRY_LOG_TABLE";

  /**
   * The name of the variable that defines the steps log database by default for all transformations
   */
  public static final String KETTLE_STEP_LOG_DB = "KETTLE_STEP_LOG_DB";

  /**
   * The name of the variable that defines the steps log schema by default for all transformations
   */
  public static final String KETTLE_STEP_LOG_SCHEMA = "KETTLE_STEP_LOG_SCHEMA";

  /**
   * The name of the variable that defines the steps log table by default for all transformations
   */
  public static final String KETTLE_STEP_LOG_TABLE = "KETTLE_STEP_LOG_TABLE";

  /**
   * The name of the variable that defines the log channel log database by default for all transformations and jobs
   */
  public static final String KETTLE_CHANNEL_LOG_DB = "KETTLE_CHANNEL_LOG_DB";

  /**
   * The name of the variable that defines the log channel log schema by default for all transformations and jobs
   */
  public static final String KETTLE_CHANNEL_LOG_SCHEMA = "KETTLE_CHANNEL_LOG_SCHEMA";

  /**
   * The name of the variable that defines the log channel log table by default for all transformations and jobs
   */
  public static final String KETTLE_CHANNEL_LOG_TABLE = "KETTLE_CHANNEL_LOG_TABLE";

  /**
   * The name of the variable that defines the metrics log database by default for all transformations and jobs
   */
  public static final String KETTLE_METRICS_LOG_DB = "KETTLE_METRICS_LOG_DB";

  /**
   * The name of the variable that defines the metrics log schema by default for all transformations and jobs
   */
  public static final String KETTLE_METRICS_LOG_SCHEMA = "KETTLE_METRICS_LOG_SCHEMA";

  /**
   * The name of the variable that defines the metrics log table by default for all transformations and jobs
   */
  public static final String KETTLE_METRICS_LOG_TABLE = "KETTLE_METRICS_LOG_TABLE";

  /**
   * The name of the variable that defines the checkpoint log database by default for all jobs
   */
  public static final String KETTLE_CHECKPOINT_LOG_DB = "KETTLE_CHECKPOINT_LOG_DB";

  /**
   * The name of the variable that defines the checkpoint log schema by default for all jobs
   */
  public static final String KETTLE_CHECKPOINT_LOG_SCHEMA = "KETTLE_CHECKPOINT_LOG_SCHEMA";

  /**
   * The name of the variable that defines the checkpoint log table by default for all jobs
   */
  public static final String KETTLE_CHECKPOINT_LOG_TABLE = "KETTLE_CHECKPOINT_LOG_TABLE";

  /**
   * Name of the environment variable to set the location of the shared object file (xml) for transformations and jobs
   */
  public static final String KETTLE_SHARED_OBJECTS = "KETTLE_SHARED_OBJECTS";

  /**
   * System wide flag to drive the evaluation of null in ValueMeta. If this setting is set to "Y", an empty string and
   * null are different. Otherwise they are not.
   */
  public static final String KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL";

  /**
   * This flag will prevent Kettle from converting {@code null} strings to empty strings in {@link org.pentaho.di.core.row.value.ValueMetaBase}
   * The default value is {@code false}.
   */
  public static final String KETTLE_DO_NOT_NORMALIZE_NULL_STRING_TO_EMPTY = "KETTLE_DO_NOT_NORMALIZE_NULL_STRING_TO_EMPTY";


  /**
   * This flag will force to return the original string with only spaces instead of an empty string.
   */
  public static final String KETTLE_DO_NOT_NORMALIZE_SPACES_ONLY_STRING_TO_EMPTY = "KETTLE_DO_NOT_NORMALIZE_SPACES_ONLY_STRING_TO_EMPTY";

  /**
   * This flag will prevent Kettle from yielding {@code null} as the value of an empty XML tag in {@link org.pentaho.di.core.xml.XMLHandler}
   * The default value is {@code false} and an empty XML tag will produce a {@code null} value.
   */
  public static final String KETTLE_XML_EMPTY_TAG_YIELDS_EMPTY_VALUE = "KETTLE_XML_EMPTY_TAG_YIELDS_EMPTY_VALUE";

  /**
   * This flag will cause the "Get XML data" step to yield null values on missing elements and empty values on empty elements when set to "Y".
   * By default, both empty elements and missing elements will yield empty values.
   */
  public static final String KETTLE_XML_MISSING_TAG_YIELDS_NULL_VALUE = "KETTLE_XML_MISSING_TAG_YIELDS_NULL_VALUE";

  /**
   * System wide flag to allow non-strict string to number conversion for backward compatibility. If this setting is set
   * to "Y", an string starting with digits will be converted successfully into a number. (example: 192.168.1.1 will be
   * converted into 192 or 192.168 depending on the decimal symbol). The default (N) will be to throw an error if
   * non-numeric symbols are found in the string.
   */
  public static final String KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION =
    "KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION";

  /**
   * System wide flag to ignore timezone while writing date/timestamp value to the database. See PDI-10749 for details.
   */
  public static final String KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE = "KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE";

  /**
   * System wide flag to use the root path prefix for a directory reference. See PDI-6779 for details.
   */
  public static final String KETTLE_COMPATIBILITY_IMPORT_PATH_ADDITION_ON_VARIABLES = "KETTLE_COMPATIBILITY_IMPORT_PATH_ADDITION_ON_VARIABLES";

  /**
   * System wide flag to ignore logging table. See BACKLOG-15706 for details.
   */
  public static final String KETTLE_COMPATIBILITY_IGNORE_TABLE_LOGGING = "KETTLE_COMPATIBILITY_IGNORE_TABLE_LOGGING";

  /**
   * System wide flag to set or not append and header options dependency on Text file output step. See PDI-5252 for
   * details.
   */
  public static final String KETTLE_COMPATIBILITY_TEXT_FILE_OUTPUT_APPEND_NO_HEADER =
    "KETTLE_COMPATIBILITY_TEXT_FILE_OUTPUT_APPEND_NO_HEADER";

  /**
   * System wide flag to control behavior of the merge rows (diff) step in case of "identical" comparison. (PDI-736)
   * 'Y' preserves the old behavior and takes the fields from the reference stream
   * 'N' enables the documented behavior and takes the fields from the comparison stream (correct behavior)
   */
  public static final String KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL =
    "KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL";

  /**
   * System wide flag to control behavior of the Memory Group By step in case of SUM and AVERAGE aggregation. (PDI-5537)
   * 'Y' preserves the old behavior and always returns a Number type for SUM and Average aggregations
   * 'N' enables the documented behavior of returning the same type as the input fields use (correct behavior).
   */
  public static final String KETTLE_COMPATIBILITY_MEMORY_GROUP_BY_SUM_AVERAGE_RETURN_NUMBER_TYPE =
    "KETTLE_COMPATIBILITY_MEMORY_GROUP_BY_SUM_AVERAGE_RETURN_NUMBER_TYPE";

  /**
   * System wide flag to control behavior of the ExecuteTransformationStep and ExecuteJobStep when a file is specified.
   * This only is used when PDI is connected to repository
   * 'Y' It is possible specify a file with the extension or not that is saved in repository
   * 'N' Should not be specified the extension, in other words, should be specified the name of file saved in repository.
   */
  public static final String KETTLE_COMPATIBILITY_INVOKE_FILES_WITH_OR_WITHOUT_FILE_EXTENSION =
    "KETTLE_COMPATIBILITY_INVOKE_FILES_WITH_OR_WITHOUT_FILE_EXTENSION";

  /**
   * You can use this variable to speed up hostname lookup.
   * Hostname lookup is performed by Kettle so that it is capable of logging the server on which a job or transformation is executed.
   */
  public static final String KETTLE_SYSTEM_HOSTNAME = "KETTLE_SYSTEM_HOSTNAME";

  /**
   * System wide flag to set the maximum number of log lines that are kept internally by Kettle. Set to 0 to keep all
   * rows (default)
   */
  public static final String KETTLE_MAX_LOG_SIZE_IN_LINES = "KETTLE_MAX_LOG_SIZE_IN_LINES";

  /**
   * System wide flag to set the maximum age (in minutes) of a log line while being kept internally by Kettle. Set to 0
   * to keep all rows indefinitely (default)
   */
  public static final String KETTLE_MAX_LOG_TIMEOUT_IN_MINUTES = "KETTLE_MAX_LOG_TIMEOUT_IN_MINUTES";

  /**
   * System wide flag to determine whether standard error will be redirected to Kettle logging facilities. Will redirect
   * if the value is equal ignoring case to the string "Y"
   */
  public static final String KETTLE_REDIRECT_STDERR = "KETTLE_REDIRECT_STDERR";

  /**
   * System wide flag to determine whether standard out will be redirected to Kettle logging facilities. Will redirect
   * if the value is equal ignoring case to the string "Y"
   */
  public static final String KETTLE_REDIRECT_STDOUT = "KETTLE_REDIRECT_STDOUT";

  /**
   * This environment variable will set a time-out after which waiting, completed or stopped transformations and jobs
   * will be automatically cleaned up. The default value is 1440 (one day).
   */
  public static final String KETTLE_CARTE_OBJECT_TIMEOUT_MINUTES = "KETTLE_CARTE_OBJECT_TIMEOUT_MINUTES";

  /**
   * System wide parameter: the maximum number of step performance snapshots to keep in memory. Set to 0 to keep all
   * snapshots indefinitely (default)
   */
  public static final String KETTLE_STEP_PERFORMANCE_SNAPSHOT_LIMIT = "KETTLE_STEP_PERFORMANCE_SNAPSHOT_LIMIT";

  /**
   * A variable to configure the maximum number of job trackers kept in memory.
   */
  public static final String KETTLE_MAX_JOB_TRACKER_SIZE = "KETTLE_MAX_JOB_TRACKER_SIZE";

  /**
   * A variable to configure the maximum number of job entry results kept in memory for logging purposes.
   */
  public static final String KETTLE_MAX_JOB_ENTRIES_LOGGED = "KETTLE_MAX_JOB_ENTRIES_LOGGED";

  /**
   * A variable to configure the maximum number of logging registry entries kept in memory for logging purposes.
   */
  public static final String KETTLE_MAX_LOGGING_REGISTRY_SIZE = "KETTLE_MAX_LOGGING_REGISTRY_SIZE";

  /**
   * A variable to configure the logging registry's purge timer which will trigger the registry to cleanup entries.
   */
  public static final String KETTLE_LOGGING_REGISTRY_PURGE_TIMEOUT = "KETTLE_LOGGING_REGISTRY_PURGE_TIMEOUT";

  /**
   * A variable to configure the kettle log tab refresh delay.
   */
  public static final String KETTLE_LOG_TAB_REFRESH_DELAY = "KETTLE_LOG_TAB_REFRESH_DELAY";

  /**
   * A variable to configure the kettle log tab refresh period.
   */
  public static final String KETTLE_LOG_TAB_REFRESH_PERIOD = "KETTLE_LOG_TAB_REFRESH_PERIOD";

  /**
   * The name of the system wide variable that can contain the name of the SAP Connection factory for the test button in
   * the DB dialog. This defaults to
   */
  public static final String KETTLE_SAP_CONNECTION_FACTORY = "KETTLE_SAP_CONNECTION_FACTORY";

  /**
   * The default SAP ERP connection factory
   */
  public static final String KETTLE_SAP_CONNECTION_FACTORY_DEFAULT_NAME =
    "org.pentaho.di.trans.steps.sapinput.sap.SAPConnectionFactory";

  /**
   * Name of the environment variable to specify additional classes to scan for plugin annotations
   */
  public static final String KETTLE_PLUGIN_CLASSES = "KETTLE_PLUGIN_CLASSES";

  /**
   * Name of the environment variable to specify additional packaged to scan for plugin annotations (warning: slow!)
   */
  public static final String KETTLE_PLUGIN_PACKAGES = "KETTLE_PLUGIN_PACKAGES";

  /**
   * Name of the environment variable that contains the size of the transformation rowset size. This overwrites values
   * that you set transformation settings.
   */
  public static final String KETTLE_TRANS_ROWSET_SIZE = "KETTLE_TRANS_ROWSET_SIZE";

  /**
   * A general initial version comment
   */
  public static final String VERSION_COMMENT_INITIAL_VERSION = "Creation of initial version";

  /**
   * A general edit version comment
   */
  public static final String VERSION_COMMENT_EDIT_VERSION = "Modification by user";

  /**
   * The XML file that contains the list of native Kettle steps
   */
  public static final String XML_FILE_KETTLE_STEPS = "kettle-steps.xml";

  /**
   * The name of the environment variable that will contain the alternative location of the kettle-steps.xml file
   */
  public static final String KETTLE_CORE_STEPS_FILE = "KETTLE_CORE_STEPS_FILE";

  /**
   * The XML file that contains the list of native partition plugins
   */
  public static final String XML_FILE_KETTLE_PARTITION_PLUGINS = "kettle-partition-plugins.xml";

  /**
   * The name of the environment variable that will contain the alternative location of the kettle-job-entries.xml file
   */
  public static final String KETTLE_CORE_JOBENTRIES_FILE = "KETTLE_CORE_JOBENTRIES_FILE";

  /**
   * The XML file that contains the list of native Kettle Carte Servlets
   */
  public static final String XML_FILE_KETTLE_SERVLETS = "kettle-servlets.xml";

  /**
   * The XML file that contains the list of native Kettle value metadata plugins
   */
  public static final String XML_FILE_KETTLE_VALUEMETA_PLUGINS = "kettle-valuemeta-plugins.xml";

  /**
   * The XML file that contains the list of native Kettle two-way password encoder plugins
   */
  @SuppressWarnings( "squid:S2068" )
  public static final String XML_FILE_KETTLE_PASSWORD_ENCODER_PLUGINS = Encr.XML_FILE_KETTLE_PASSWORD_ENCODER_PLUGINS;

  /**
   * The name of the environment variable that will contain the alternative location of the kettle-valuemeta-plugins.xml
   * file
   */
  public static final String KETTLE_VALUEMETA_PLUGINS_FILE = "KETTLE_VALUEMETA_PLUGINS_FILE";

  /**
   * Specifies the password encoding plugin to use by ID (Kettle is the default).
   */
  @SuppressWarnings( "squid:S2068" )
  public static final String KETTLE_PASSWORD_ENCODER_PLUGIN = Encr.KETTLE_PASSWORD_ENCODER_PLUGIN;

  /**
   * The name of the environment variable that will contain the alternative location of the kettle-password-encoder-plugins.xml
   * file
   */
  @SuppressWarnings( "squid:S2068" )
  public static final String KETTLE_PASSWORD_ENCODER_PLUGINS_FILE = Encr.KETTLE_PASSWORD_ENCODER_PLUGINS_FILE;

  /**
   * The name of the Kettle encryption seed environment variable for the KettleTwoWayPasswordEncoder class
   */
  @SuppressWarnings( "squid:S2068" )
  public static final String KETTLE_TWO_WAY_PASSWORD_ENCODER_SEED = Encr.KETTLE_TWO_WAY_PASSWORD_ENCODER_SEED;

  /**
   * The XML file that contains the list of native Kettle logging plugins
   */
  public static final String XML_FILE_KETTLE_LOGGING_PLUGINS = "kettle-logging-plugins.xml";

  /**
   * The name of the environment variable that will contain the alternative location of the kettle-logging-plugins.xml
   * file
   */
  public static final String KETTLE_LOGGING_PLUGINS_FILE = "KETTLE_LOGGING_PLUGINS_FILE";

  /**
   * The name of the environment variable that will contain the alternative location of the kettle-servlets.xml file
   */
  public static final String KETTLE_CORE_SERVLETS_FILE = "KETTLE_CORE_SERVLETS_FILE";

  /**
   * The name of the variable that optionally contains an alternative rowset get timeout (in ms). This only makes a
   * difference for extremely short lived transformations.
   */
  public static final String KETTLE_ROWSET_GET_TIMEOUT = "KETTLE_ROWSET_GET_TIMEOUT";

  /**
   * The name of the variable that optionally contains an alternative rowset put timeout (in ms). This only makes a
   * difference for extremely short lived transformations.
   */
  public static final String KETTLE_ROWSET_PUT_TIMEOUT = "KETTLE_ROWSET_PUT_TIMEOUT";

  /**
   * Set this variable to Y if you want to test a more efficient batching row set. (default = N)
   */
  public static final String KETTLE_BATCHING_ROWSET = "KETTLE_BATCHING_ROWSET";

  /**
   * Set this variable to limit max number of files the Text File Output step can have open at one time.
   */
  public static final String KETTLE_FILE_OUTPUT_MAX_STREAM_COUNT = "KETTLE_FILE_OUTPUT_MAX_STREAM_COUNT";

  /**
   * This variable contains the number of milliseconds between flushes of all open files in the Text File Output step.
   */
  public static final String KETTLE_FILE_OUTPUT_MAX_STREAM_LIFE = "KETTLE_FILE_OUTPUT_MAX_STREAM_LIFE";

  /**
   * Set this variable to Y to disable standard Kettle logging to the console. (stdout)
   */
  public static final String KETTLE_DISABLE_CONSOLE_LOGGING = "KETTLE_DISABLE_CONSOLE_LOGGING";

  /**
   * Set this variable to with the intended repository name ( in repositories.xml )
   */
  public static final String KETTLE_REPOSITORY = "KETTLE_REPOSITORY";

  /**
   * Set this variable to with the intended username to pass as repository credentials
   */
  public static final String KETTLE_USER = "KETTLE_USER";

  /**
   * Set this variable to with the intended password to pass as repository credentials
   */
  @SuppressWarnings( "squid:S2068" )
  public static final String KETTLE_PASSWORD = "KETTLE_PASSWORD";

  /**
   * The XML file that contains the list of native Kettle job entries
   */
  public static final String XML_FILE_KETTLE_JOB_ENTRIES = "kettle-job-entries.xml";

  /**
   * The XML file that contains the list of native Kettle repository types (DB, File, etc)
   */
  public static final String XML_FILE_KETTLE_REPOSITORIES = "kettle-repositories.xml";

  /**
   * The XML file that contains the list of native Kettle database types (MySQL, Oracle, etc)
   */
  public static final String XML_FILE_KETTLE_DATABASE_TYPES = "kettle-database-types.xml";

  /**
   * The XML file that contains the list of native Kettle compression providers (None, ZIP, GZip, etc.)
   */
  public static final String XML_FILE_KETTLE_COMPRESSION_PROVIDERS = "kettle-compression-providers.xml";

  /**
   * The XML file that contains the list of native Kettle compression providers (None, ZIP, GZip, etc.)
   */
  public static final String XML_FILE_KETTLE_AUTHENTICATION_PROVIDERS = "kettle-authentication-providers.xml";

  /**
   * The XML file that contains the list of native extension points (None by default, this is mostly for OEM purposes)
   */
  public static final String XML_FILE_KETTLE_EXTENSION_POINTS = "kettle-extension-points.xml";

  /**
   * The XML file that contains the list of native extension points (None by default, this is mostly for OEM purposes)
   */
  public static final String XML_FILE_KETTLE_REGISTRY_EXTENSIONS = "kettle-registry-extensions.xml";

  /**
   * The XML file that contains the list of lifecycle listeners
   */
  public static final String XML_FILE_KETTLE_LIFECYCLE_LISTENERS = "kettle-lifecycle-listeners.xml";

  /**
   * The XML file that contains the list of native engines
   */
  public static final String XML_FILE_KETTLE_ENGINES = "kettle-engines.xml";

  /**
   * the value the Pan JVM should return on exit.
   */
  public static final String KETTLE_TRANS_PAN_JVM_EXIT_CODE = "KETTLE_TRANS_PAN_JVM_EXIT_CODE";

  /**
   * The name of the variable containing an alternative default number format
   */
  public static final String KETTLE_DEFAULT_NUMBER_FORMAT = "KETTLE_DEFAULT_NUMBER_FORMAT";

  /**
   * The name of the variable containing an alternative default bignumber format
   */
  public static final String KETTLE_DEFAULT_BIGNUMBER_FORMAT = "KETTLE_DEFAULT_BIGNUMBER_FORMAT";

  /**
   * The name of the variable containing an alternative default integer format
   */
  public static final String KETTLE_DEFAULT_INTEGER_FORMAT = "KETTLE_DEFAULT_INTEGER_FORMAT";

  /**
   * The name of the variable containing an alternative default date format
   */
  public static final String KETTLE_DEFAULT_DATE_FORMAT = "KETTLE_DEFAULT_DATE_FORMAT";

  // Null values tweaks
  public static final String KETTLE_AGGREGATION_MIN_NULL_IS_VALUED = "KETTLE_AGGREGATION_MIN_NULL_IS_VALUED";
  public static final String KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO = "KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO";

  /**
   * The name of the variable containing an alternative default timestamp format
   */
  public static final String KETTLE_DEFAULT_TIMESTAMP_FORMAT = "KETTLE_DEFAULT_TIMESTAMP_FORMAT";

  /**
   * Variable that is responsible for removing enclosure symbol after splitting the string
   */
  public static final String KETTLE_SPLIT_FIELDS_REMOVE_ENCLOSURE = "KETTLE_SPLIT_FIELDS_REMOVE_ENCLOSURE";

  /**
   * Variable that is responsible for checking empty field names and types.
   */
  public static final String KETTLE_ALLOW_EMPTY_FIELD_NAMES_AND_TYPES = "KETTLE_ALLOW_EMPTY_FIELD_NAMES_AND_TYPES";

  /**
   * Set this variable to false to preserve global log variables defined in transformation / job Properties -> Log panel.
   * Changing it to true will clear all global log variables when export transformation / job
   */
  public static final String KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT = "KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT";

  /**
   * Property controls the capacity of the transFinishedBlockingQueue in Trans.
   */
  public static final String KETTLE_TRANS_FINISHED_BLOCKING_QUEUE_SIZE = "KETTLE_TRANS_FINISHED_BLOCKING_QUEUE_SIZE";

  /**
   * Compatibility settings for {@link org.pentaho.di.core.row.ValueDataUtil#hourOfDay(ValueMetaInterface, Object)}.
   *
   * Switches off the fix for calculation of timezone decomposition.
   */
  public static final String KETTLE_COMPATIBILITY_CALCULATION_TIMEZONE_DECOMPOSITION =
    "KETTLE_COMPATIBILITY_CALCULATION_TIMEZONE_DECOMPOSITION";

  /**
   * Compatibility settings for setNrErrors
   */
  // see PDI-10270 for details.
  public static final String KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES =
    "KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES";

  // See PDI-15781 for details
  public static final String KETTLE_COMPATIBILITY_SEND_RESULT_XML_WITH_FULL_STATUS = "KETTLE_COMPATIBILITY_SEND_RESULT_XML_WITH_FULL_STATUS";

  // See PDI-16388 for details
  public static final String KETTLE_COMPATIBILITY_SELECT_VALUES_TYPE_CHANGE_USES_TYPE_DEFAULTS = "KETTLE_COMPATIBILITY_SELECT_VALUES_TYPE_CHANGE_USES_TYPE_DEFAULTS";

  // See PDI-17203 for details
  public static final String KETTLE_COMPATIBILITY_XML_OUTPUT_NULL_VALUES = "KETTLE_COMPATIBILITY_XML_OUTPUT_NULL_VALUES";

  // See PDI-17980 for details
  public static final String KETTLE_COMPATIBILITY_USE_JDBC_METADATA = "KETTLE_COMPATIBILITY_USE_JDBC_METADATA";

  // See PDI-18470 for details
  public static final String KETTLE_COMPATIBILITY_DB_LOOKUP_USE_FIELDS_RETURN_TYPE_CHOSEN_IN_UI = "KETTLE_COMPATIBILITY_DB_LOOKUP_USE_FIELDS_RETURN_TYPE_CHOSEN_IN_UI";

  // See PDI-PDI-18739 for details
  public static final String KETTLE_COMPATIBILITY_TEXT_FILE_INPUT_USE_LENIENT_ENCLOSURE_HANDLING = "KETTLE_COMPATIBILITY_TEXT_FILE_INPUT_USE_LENIENT_ENCLOSURE_HANDLING";

  // See PDI-18810 for details
  public static final String KETTLE_COMPATIBILITY_MDI_INJECTED_FILE_ALWAYS_IN_FILESYSTEM = "KETTLE_COMPATIBILITY_MDI_INJECTED_FILE_ALWAYS_IN_FILESYSTEM";

  // See PDI-19138 for details
  public static final String KETTLE_JSON_INPUT_INCLUDE_NULLS = "KETTLE_JSON_INPUT_INCLUDE_NULLS";

  /**
   * This property when set to Y force the same output file even when splits is required.
   * See PDI-19064 for details
   */
  public static final String KETTLE_JSON_OUTPUT_FORCE_SAME_OUTPUT_FILE = "KETTLE_JSON_OUTPUT_FORCE_SAME_OUTPUT_FILE";

  /**
   * The XML file that contains the list of native import rules
   */
  public static final String XML_FILE_KETTLE_IMPORT_RULES = "kettle-import-rules.xml";

  private static String[] emptyPaddedSpacesStrings;

  /**
   * The release type of this compilation
   */
  public static final ReleaseType RELEASE = ReleaseType.GA;

  /**
   * The system environment variable indicating where the alternative location for the Pentaho metastore folder is
   * located.
   */
  public static final String PENTAHO_METASTORE_FOLDER = "PENTAHO_METASTORE_FOLDER";

  /**
   * The name of the local client MetaStore
   *
   */
  public static final String PENTAHO_METASTORE_NAME = "Pentaho Local Client Metastore";

  /**
   * A variable to configure turning on/off detailed subjects in log.
   */
  public static final String KETTLE_LOG_MARK_MAPPINGS = "KETTLE_LOG_MARK_MAPPINGS";

  /**
   * A variable to configure jetty option: acceptors for Carte
   */
  public static final String KETTLE_CARTE_JETTY_ACCEPTORS = "KETTLE_CARTE_JETTY_ACCEPTORS";

  /**
   * A variable to configure jetty option: acceptQueueSize for Carte
   */
  public static final String KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE = "KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE";

  /**
   * A variable to configure jetty option: lowResourcesMaxIdleTime for Carte
   */
  public static final String KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME = "KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME";

  /**
   * A variable to configure refresh for carte job/trans status page
   */
  public static final String KETTLE_CARTE_REFRESH_STATUS = "KETTLE_CARTE_REFRESH_STATUS";

  /**
   * A variable to configure s3vfs to use a temporary file on upload data to S3 Amazon."
   */
  public static final String S3VFS_USE_TEMPORARY_FILE_ON_UPLOAD_DATA = "s3.vfs.useTempFileOnUploadData";

  /**
   * A variable to configure Tab size"
   */
  public static final String KETTLE_MAX_TAB_LENGTH = "KETTLE_MAX_TAB_LENGTH";

  /**
   * A variable to configure VFS USER_DIR_IS_ROOT option: should be "true" or "false"
   * {@linkplain org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder#USER_DIR_IS_ROOT}
   */
  public static final String VFS_USER_DIR_IS_ROOT = "vfs.sftp.userDirIsRoot";

  /**
   * A variable to configure environment variables to ignore when initializing shell step
   * */
  public static final String SHELL_STEP_ENVIRONMENT_VARIABLES_TO_IGNORE = "SHELL_STEP_ENVIRONMENT_VARIABLES_TO_IGNORE";

  /**
   * The default value for the variable to configure environment variables to ignore when initializing shell step
   * */
  public static final String SHELL_STEP_ENVIRONMENT_VARIABLES_TO_IGNORE_DEFAULT = "";

  /**
   * <p>A variable to configure the minimum allowed ratio between de- and inflated bytes to detect a zipbomb.</p>
   * <p>If not set or if the configured value is invalid, it defaults to {@value
   * #KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT}</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT
   * @see #KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT_STRING
   */
  public static final String KETTLE_ZIP_MIN_INFLATE_RATIO = "KETTLE_ZIP_MIN_INFLATE_RATIO";

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_MIN_INFLATE_RATIO} as a Double.</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MIN_INFLATE_RATIO
   * @see #KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT_STRING
   */
  public static final Double KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT = 0.01d;

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_MIN_INFLATE_RATIO} as a String.</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MIN_INFLATE_RATIO
   * @see #KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT
   */
  public static final String KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT_STRING =
    String.valueOf( KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT );

  /**
   * <p>A variable to configure the maximum file size of a single zip entry.</p>
   * <p>If not set or if the configured value is invalid, it defaults to {@value #KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT}</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT
   * @see #KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT_STRING
   */
  public static final String KETTLE_ZIP_MAX_ENTRY_SIZE = "KETTLE_ZIP_MAX_ENTRY_SIZE";

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_MAX_ENTRY_SIZE} as a Long.</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MAX_ENTRY_SIZE
   * @see #KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT_STRING
   */
  public static final Long KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT = 0xFFFFFFFFL;

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_MAX_ENTRY_SIZE} as a String.</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MAX_ENTRY_SIZE
   * @see #KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT
   */
  public static final String KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT_STRING =
    String.valueOf( KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT );

  /**
   * <p>A variable to configure the maximum number of characters of text that are extracted before an exception is
   * thrown during extracting text from documents.</p>
   * <p>If not set or if the configured value is invalid, it defaults to {@value #KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT}</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT
   * @see #KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT_STRING
   */
  public static final String KETTLE_ZIP_MAX_TEXT_SIZE = "KETTLE_ZIP_MAX_TEXT_SIZE";

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_MAX_TEXT_SIZE} as a Long.</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MAX_TEXT_SIZE
   * @see #KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT_STRING
   */
  public static final Long KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT = 10 * 1024 * 1024L;

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_MAX_TEXT_SIZE} as a Long.</p>
   * <p>Check PDI-17586 for more details.</p>
   *
   * @see #KETTLE_ZIP_MAX_TEXT_SIZE
   * @see #KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT
   */
  public static final String KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT_STRING =
    String.valueOf( KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT );

  /**
   * <p>The default value for the {@link #KETTLE_ZIP_NEGATIVE_MIN_INFLATE} as a Double.</p>
   * <p>Check PDI-18489 for more details.</p>
   */
  public static final Double KETTLE_ZIP_NEGATIVE_MIN_INFLATE = -1.0d;

  /**
   * <p>This environment variable is used to define whether the check of xlsx zip bomb is performed. This is set to false by default.</p>
   */
  public static final String KETTLE_XLSX_ZIP_BOMB_CHECK = "KETTLE_XLSX_ZIP_BOMB_CHECK";
  private static final String KETTLE_XLSX_ZIP_BOMB_CHECK_DEFAULT = "false";
  public static boolean checkXlsxZipBomb() {
    String checkZipBomb = System.getProperty( KETTLE_XLSX_ZIP_BOMB_CHECK, KETTLE_XLSX_ZIP_BOMB_CHECK_DEFAULT );
    return Boolean.valueOf( checkZipBomb );
  }

  /**
   * <p>A variable to configure if the S3 input / output steps should use the Amazon Default Credentials Provider Chain
   * even if access credentials are specified within the transformation.</p>
   */
  public static final String KETTLE_USE_AWS_DEFAULT_CREDENTIALS = "KETTLE_USE_AWS_DEFAULT_CREDENTIALS";

  /**
   * <p>This environment variable is used by streaming consumer steps to limit the total of concurrent batches across transformations.</p>
   */
  public static final String SHARED_STREAMING_BATCH_POOL_SIZE = "SHARED_STREAMING_BATCH_POOL_SIZE";

  /**
   * <p>This environment variable is used to specify a location used to deploy a shim driver into PDI.</p>
   */
  public static final String SHIM_DRIVER_DEPLOYMENT_LOCATION = "SHIM_DRIVER_DEPLOYMENT_LOCATION";
  private static final String DEFAULT_DRIVERS_DIR = "DEFAULT";
  public static String getShimDriverDeploymentLocation() {

    String driversLocation = System.getProperty( Const.SHIM_DRIVER_DEPLOYMENT_LOCATION, DEFAULT_DRIVERS_DIR );
    if ( driversLocation.equals( DEFAULT_DRIVERS_DIR ) ) {
      String karafDir = System.getProperty( "karaf.home" );
      return Paths.get( karafDir ).getParent().getParent().toString() + File.separator + "drivers";
    }
    return driversLocation;
  }

  /**
   * <p>This environment is used to specify how many attempts before failing to read an XML from within a Zip file
   * while multy-thread execution and using XMLHandler.</p>
   */
  public static final String KETTLE_RETRY_OPEN_XML_STREAM = "KETTLE_RETRY_OPEN_XML_STREAM";

  /**
   * <p>This environment variable is used by XSD validation steps to enable or disable external entities.</p>
   * <p>By default external entities are allowed.</p>
   */
  public static final String ALLOW_EXTERNAL_ENTITIES_FOR_XSD_VALIDATION = "ALLOW_EXTERNAL_ENTITIES_FOR_XSD_VALIDATION";
  public static final String ALLOW_EXTERNAL_ENTITIES_FOR_XSD_VALIDATION_DEFAULT = "true";

  /**
   * <p>This environment variable is used to define the default division result precision between BigDecimals.</p>
   * <p>By default, and when precision is -1, precision is unlimited.</p>
   */
  public static final String KETTLE_BIGDECIMAL_DIVISION_PRECISION = "KETTLE_BIGDECIMAL_DIVISION_PRECISION";
  public static final String KETTLE_BIGDECIMAL_DIVISION_PRECISION_DEFAULT = "-1";

  /**
   * <p>This environment variable is used to define the default division result rounding mode between BigDecimals.</p>
   * <p>By default, rouding mode is half even.</p>
   */
  public static final String KETTLE_BIGDECIMAL_DIVISION_ROUNDING_MODE = "KETTLE_BIGDECIMAL_DIVISION_ROUNDING_MODE";
  public static final String KETTLE_BIGDECIMAL_DIVISION_ROUNDING_MODE_DEFAULT = "HALF_EVEN";

  /**
   * <p>This environment variable is used to define how Timestamp should be converted to a number and vice-versa.</p>
   * <p>Three options exist:</p>
   * <ul>
   *   <li>{@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY}: converting a Timestamp to a number uses
   *   milliseconds but converting a number to Timestamp assumes the value is in nanoseconds</li>
   *   <li>{@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS}: both Timestamp to number and number to
   *   Timestamp use milliseconds</li>
   *   <li>{@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS}: both Timestamp to number and number to
   *   Timestamp use nanoseconds</li>
   * </ul>
   * <p>The default is {@value #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_DEFAULT}.</p>
   *
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_DEFAULT
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS
   */
  public static final String KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE = "KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE";

  /**
   * <p>The value to use for setting the {@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE} as it behaved on former
   * versions.</p>
   *
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS
   */
  public static final String KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY = "LEGACY";

  /**
   * <p>The value to use for setting the {@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE} to use milliseconds.</p>
   *
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS
   */
  public static final String KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS = "MILLISECONDS";

  /**
   * <p>The value to use for setting the {@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE} to use nanoseconds.</p>
   *
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS
   */
  public static final String KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS = "NANOSECONDS";

  /**
   * <p>The default value for the {@link #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE}.</p>
   *
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS
   * @see #KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS
   */
  public static final String KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_DEFAULT =
    KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_LEGACY;

  /**
   * This environment variable will be used to determine whether file URI strings returned from input steps are returned
   * encoded (spaces and other special characters escaped) or decoded (default legacy behavior).
   */
  public static final String KETTLE_RETURN_ESCAPED_URI_STRINGS = "KETTLE_RETURN_ESCAPED_URI_STRINGS";

  /**
   * <p>This environment variable is used to define how which calculation method is to be used by the 'Add a Checksum'
   * step.</p>
   * <p>Three options exist:</p>
   * <ul>
   *   <li>{@link #KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES}: calculate Checksum based on Byte representation of
   *   fields; as in versions since 8.1</li>
   *   <li>{@link #KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS}: calculate Checksum based on Pentaho String
   *   representation of fields (applying format masks); as in versions until 7.1</li>
   *   <li>{@link #KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS}: calculate Checksum based on Native String
   *   representation of fields; as in version 8.0</li>
   * </ul>
   * <p>The default is {@value #KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT}.</p>
   *
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT
   */
  public static final String KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD = "KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD";

  /**
   * <p>The value to use for setting the {@link #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD}, so that Checksum is
   * calculated based on Byte representation of fields. Calculation method used by version 8.1 and after.</p>
   *
   * @see #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT
   */
  public static final String KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES = "BYTES";

  /**
   * <p>The value to use for setting the {@link #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD}, so that Checksum is
   * calculated based on Pentaho String representation of fields (applying format masks). Calculation method used by
   * version 7.1 and prior versions.</p>
   *
   * @see #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT
   */
  public static final String KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS = "PENTAHO_STRINGS";

  /**
   * <p>The value to use for setting the {@link #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD}, so that Checksum is
   * calculated based on Native String representation of fields. Calculation method used by version 8.0.</p>
   *
   * @see #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT
   */
  public static final String KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS = "NATIVE_STRINGS";

  /**
   * <p>The default value for the {@link #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD}.</p>
   *
   * @see #KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS
   */
  public static final String KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT = KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES;

  /**
   * <p>While one assumes that a day has 24 hours, due to daylight savings time settings, it may have 23 hours (the day
   * Summer time goes into effect) or 25 hours (Winter time).</p>
   * <p>Imagine Summer time: when clocks reach 1:00, it goes forward 1 hour to 2:00</p>
   * <p>This means that, when adding 2 hours to 0:30, one gets 3:30</p>
   * <p>By setting this environment variable to {@code "N"}, DateDiff performs calculations based on local time; as so,
   * the difference between these two values (3:30 and 0:30) will be 3 hours difference.</p>
   * <p>Setting this environment variable to {@code "Y"}, DateDiff performs calculations based on UTC time; as so, the
   * difference between these two values (3:30 and 0:30) will be 2 hours difference.</p>
   * <p>The default is {@value #KETTLE_DATEDIFF_DST_AWARE_DEFAULT}.</p>
   *
   * @see #KETTLE_DATEDIFF_DST_AWARE_DEFAULT
   */
  public static final String KETTLE_DATEDIFF_DST_AWARE = "KETTLE_DATEDIFF_DST_AWARE";

  /**
   * <p>The default value for the {@link #KETTLE_DATEDIFF_DST_AWARE}.</p>
   *
   * @see #KETTLE_DATEDIFF_DST_AWARE
   */
  public static final String KETTLE_DATEDIFF_DST_AWARE_DEFAULT = "N";

  /**
   * If true, kettle check for new site files to update in the named cluster every time a named cluster is resolved
   */
  public static final String KETTLE_AUTO_UPDATE_SITE_FILE = "KETTLE_AUTO_UPDATE_SITE_FILE";

  /**
   * rounds double f to any number of places after decimal point Does arithmetic using BigDecimal class to avoid integer
   * overflow while rounding
   *
   * @param f
   *          The value to round
   * @param places
   *          The number of decimal places
   * @return The rounded floating point value
   */

  public static double round( double f, int places ) {
    return round( f, places, java.math.BigDecimal.ROUND_HALF_EVEN );
  }

  /**
   * rounds double f to any number of places after decimal point Does arithmetic using BigDecimal class to avoid integer
   * overflow while rounding
   *
   * @param f
   *          The value to round
   * @param places
   *          The number of decimal places
   * @param roundingMode
   *          The mode for rounding, e.g. java.math.BigDecimal.ROUND_HALF_EVEN
   * @return The rounded floating point value
   */
  public static double round( double f, int places, int roundingMode ) {
    // We can't round non-numbers or infinite values
    //
    if ( Double.isNaN( f ) || f == Double.NEGATIVE_INFINITY || f == Double.POSITIVE_INFINITY ) {
      return f;
    }

    // Do the rounding...
    //
    java.math.BigDecimal bdtemp = round( java.math.BigDecimal.valueOf( f ), places, roundingMode );
    return bdtemp.doubleValue();
  }

  /**
   * rounds BigDecimal f to any number of places after decimal point Does arithmetic using BigDecimal class to avoid
   * integer overflow while rounding
   *
   * @param f
   *          The value to round
   * @param places
   *          The number of decimal places
   * @param roundingMode
   *          The mode for rounding, e.g. java.math.BigDecimal.ROUND_HALF_EVEN
   * @return The rounded floating point value
   */
  public static BigDecimal round( BigDecimal f, int places, int roundingMode ) {
    if ( roundingMode == ROUND_HALF_CEILING ) {
      if ( f.signum() >= 0 ) {
        return round( f, places, BigDecimal.ROUND_HALF_UP );
      } else {
        return round( f, places, BigDecimal.ROUND_HALF_DOWN );
      }
    } else {
      return f.setScale( places, roundingMode );
    }
  }

  /**
   * rounds long f to any number of places after decimal point Does arithmetic using BigDecimal class to avoid integer
   * overflow while rounding
   *
   * @param f
   *          The value to round
   * @param places
   *          The number of decimal places
   * @param roundingMode
   *          The mode for rounding, e.g. java.math.BigDecimal.ROUND_HALF_EVEN
   * @return The rounded floating point value
   */
  public static long round( long f, int places, int roundingMode ) {
    if ( places >= 0 ) {
      return f;
    }
    BigDecimal bdtemp = round( BigDecimal.valueOf( f ), places, roundingMode );
    return bdtemp.longValue();
  }

  /*
   * OLD code: caused a lot of problems with very small and very large numbers. It's a miracle it worked at all. Go
   * ahead, have a laugh... public static float round(double f, int places) { float temp = (float) (f *
   * (Math.pow(10, places)));
   *
   * temp = (Math.round(temp));
   *
   * temp = temp / (int) (Math.pow(10, places));
   *
   * return temp;
   *
   * }
   */

  /**
   * Convert a String into an integer. If the conversion fails, assign a default value.
   *
   * @param str
   *          The String to convert to an integer
   * @param def
   *          The default value
   * @return The converted value or the default.
   */
  public static int toInt( String str, int def ) {
    int retval;
    try {
      retval = Integer.parseInt( str );
    } catch ( Exception e ) {
      retval = def;
    }
    return retval;
  }

  /**
   * Convert a String into a long integer. If the conversion fails, assign a default value.
   *
   * @param str
   *          The String to convert to a long integer
   * @param def
   *          The default value
   * @return The converted value or the default.
   */
  public static long toLong( String str, long def ) {
    long retval;
    try {
      retval = Long.parseLong( str );
    } catch ( Exception e ) {
      retval = def;
    }
    return retval;
  }

  /**
   * Convert a String into a double. If the conversion fails, assign a default value.
   *
   * @param str
   *          The String to convert to a double
   * @param def
   *          The default value
   * @return The converted value or the default.
   */
  public static double toDouble( String str, double def ) {
    double retval;
    try {
      retval = Double.parseDouble( str );
    } catch ( Exception e ) {
      retval = def;
    }
    return retval;
  }

  /**
   * Convert a String into a date. The date format is <code>yyyy/MM/dd HH:mm:ss.SSS</code>. If the conversion fails,
   * assign a default value.
   *
   * @param str
   *          The String to convert into a Date
   * @param def
   *          The default value
   * @return The converted value or the default.
   */
  public static Date toDate( String str, Date def ) {
    SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS", Locale.US );
    try {
      return df.parse( str );
    } catch ( ParseException e ) {
      return def;
    }
  }

  /**
   * Determines whether or not a character is considered a space. A character is considered a space in Kettle if it is a
   * space, a tab, a newline or a cariage return.
   *
   * @param c
   *          The character to verify if it is a space.
   * @return true if the character is a space. false otherwise.
   */
  public static boolean isSpace( char c ) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n' || Character.isWhitespace( c );
  }

  /**
   * Left trim: remove spaces to the left of a String.
   *
   * @param source
   *          The String to left trim
   * @return The left trimmed String
   */
  public static String ltrim( String source ) {
    if ( source == null ) {
      return null;
    }
    int from = 0;
    while ( from < source.length() && isSpace( source.charAt( from ) ) ) {
      from++;
    }

    return source.substring( from );
  }

  /**
   * Right trim: remove spaces to the right of a string
   *
   * @param source
   *          The string to right trim
   * @return The trimmed string.
   */
  public static String rtrim( String source ) {
    if ( source == null ) {
      return null;
    }

    int max = source.length();
    while ( max > 0 && isSpace( source.charAt( max - 1 ) ) ) {
      max--;
    }

    return source.substring( 0, max );
  }

  /**
   * Trims a string: removes the leading and trailing spaces of a String.
   *
   * @param str
   *          The string to trim
   * @return The trimmed string.
   */
  public static String trim( String str ) {
    if ( str == null ) {
      return null;
    }

    int max = str.length() - 1;
    int min = 0;

    while ( min <= max && isSpace( str.charAt( min ) ) ) {
      min++;
    }
    while ( max >= 0 && isSpace( str.charAt( max ) ) ) {
      max--;
    }

    if ( max < min ) {
      return "";
    }

    return str.substring( min, max + 1 );
  }

  /**
   * Right pad a string: adds spaces to a string until a certain length. If the length is smaller then the limit
   * specified, the String is truncated.
   *
   * @param ret
   *          The string to pad
   * @param limit
   *          The desired length of the padded string.
   * @return The padded String.
   */
  public static String rightPad( String ret, int limit ) {
    if ( ret == null ) {
      return rightPad( new StringBuilder(), limit );
    } else {
      return rightPad( new StringBuilder( ret ), limit );
    }
  }

  /**
   * Right pad a StringBuffer: adds spaces to a string until a certain length. If the length is smaller then the limit
   * specified, the String is truncated.
   *
   * MB - New version is nearly 25% faster
   *
   * @param ret
   *          The StringBuffer to pad
   * @param limit
   *          The desired length of the padded string.
   * @return The padded String.
   */
  public static String rightPad( StringBuffer ret, int limit ) {
    if ( ret != null ) {
      while ( ret.length() < limit ) {
        ret.append( "                    " );
      }
      ret.setLength( limit );
      return ret.toString();
    } else {
      return null;
    }
  }

  /**
   * Right pad a StringBuilder: adds spaces to a string until a certain length. If the length is smaller then the limit
   * specified, the String is truncated.
   *
   * MB - New version is nearly 25% faster
   *
   * @param ret
   *          The StringBuilder to pad
   * @param limit
   *          The desired length of the padded string.
   * @return The padded String.
   */
  public static String rightPad( StringBuilder ret, int limit ) {
    if ( ret != null ) {
      while ( ret.length() < limit ) {
        ret.append( "                    " );
      }
      ret.setLength( limit );
      return ret.toString();
    } else {
      return null;
    }
  }

  /**
   * Replace values in a String with another.
   *
   * 33% Faster using replaceAll this way than original method
   *
   * @param string
   *          The original String.
   * @param repl
   *          The text to replace
   * @param with
   *          The new text bit
   * @return The resulting string with the text pieces replaced.
   */
  public static String replace( String string, String repl, String with ) {
    if ( string != null && repl != null && with != null ) {
      return string.replaceAll( Pattern.quote( repl ), Matcher.quoteReplacement( with ) );
    } else {
      return null;
    }
  }

  /**
   * Alternate faster version of string replace using a stringbuffer as input.
   *
   * 33% Faster using replaceAll this way than original method
   *
   * @param str
   *          The string where we want to replace in
   * @param code
   *          The code to search for
   * @param repl
   *          The replacement string for code
   */
  public static void repl( StringBuffer str, String code, String repl ) {
    if ( ( code == null ) || ( repl == null ) || ( code.length() == 0 ) || ( repl.length() == 0 ) || ( str == null ) || ( str.length() == 0 ) ) {
      return; // do nothing
    }
    String aString = str.toString();
    str.setLength( 0 );
    str.append( aString.replaceAll( Pattern.quote( code ), Matcher.quoteReplacement( repl ) ) );
  }

  /**
   * Alternate faster version of string replace using a stringbuilder as input (non-synchronized).
   *
   * 33% Faster using replaceAll this way than original method
   *
   * @param str
   *          The string where we want to replace in
   * @param code
   *          The code to search for
   * @param repl
   *          The replacement string for code
   */
  public static void repl( StringBuilder str, String code, String repl ) {
    if ( ( code == null ) || ( repl == null ) || ( str == null ) ) {
      return; // do nothing
    }
    String aString = str.toString();
    str.setLength( 0 );
    str.append( aString.replaceAll( Pattern.quote( code ), Matcher.quoteReplacement( repl ) ) );
  }

  /**
   * Count the number of spaces to the left of a text. (leading)
   *
   * @param field
   *          The text to examine
   * @return The number of leading spaces found.
   */
  public static int nrSpacesBefore( String field ) {
    int nr = 0;
    int len = field.length();
    while ( nr < len && field.charAt( nr ) == ' ' ) {
      nr++;
    }
    return nr;
  }

  /**
   * Count the number of spaces to the right of a text. (trailing)
   *
   * @param field
   *          The text to examine
   * @return The number of trailing spaces found.
   */
  public static int nrSpacesAfter( String field ) {
    int nr = 0;
    int len = field.length();
    while ( nr < len && field.charAt( field.length() - 1 - nr ) == ' ' ) {
      nr++;
    }
    return nr;
  }

  /**
   * Checks whether or not a String consists only of spaces.
   *
   * @param str
   *          The string to check
   * @return true if the string has nothing but spaces.
   */
  public static boolean onlySpaces( String str ) {
    for ( int i = 0; i < str.length(); i++ ) {
      if ( !isSpace( str.charAt( i ) ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * determine the OS name
   *
   * @return The name of the OS
   */
  public static String getOS() {
    return System.getProperty( "os.name" );
  }

  /**
   * Determine the quoting character depending on the OS. Often used for shell calls, gives back " for Windows systems
   * otherwise '
   *
   * @return quoting character
   */
  public static String getQuoteCharByOS() {
    if ( isWindows() ) {
      return "\"";
    } else {
      return "'";
    }
  }

  /**
   * Quote a string depending on the OS. Often used for shell calls.
   *
   * @return quoted string
   */
  public static String optionallyQuoteStringByOS( String string ) {
    String quote = getQuoteCharByOS();
    if ( Utils.isEmpty( string ) ) {
      return quote;
    }

    // If the field already contains quotes, we don't touch it anymore, just
    // return the same string...
    // also return it if no spaces are found
    if ( string.indexOf( quote ) >= 0 || ( string.indexOf( ' ' ) < 0 && string.indexOf( '=' ) < 0 ) ) {
      return string;
    } else {
      return quote + string + quote;
    }
  }

  /**
   * @return True if the OS is a Windows derivate.
   */
  public static boolean isWindows() {
    return getOS().startsWith( "Windows" );
  }

  /**
   * @return True if the OS is a Linux derivate.
   */
  public static boolean isLinux() {
    return getOS().startsWith( "Linux" );
  }

  /**
   * @return True if the OS is an OSX derivate.
   */
  public static boolean isOSX() {
    return getOS().toUpperCase().contains( "OS X" );
  }

  /**
   * @return True if KDE is in use.
   */
  public static boolean isKDE() {
    return StringUtils.isNotBlank( System.getenv( "KDE_SESSION_VERSION" ) );
  }

  private static String cachedHostname;

  /**
   * Determine the hostname of the machine Kettle is running on
   *
   * @return The hostname
   */
  public static String getHostname() {

    if ( cachedHostname != null ) {
      return cachedHostname;
    }

    // In case we don't want to leave anything to doubt...
    //
    String systemHostname = EnvUtil.getSystemProperty( KETTLE_SYSTEM_HOSTNAME );
    if ( !Utils.isEmpty( systemHostname ) ) {
      cachedHostname = systemHostname;
      return systemHostname;
    }

    String lastHostname = "localhost";
    try {
      Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
      while ( en.hasMoreElements() ) {
        NetworkInterface nwi = en.nextElement();
        Enumeration<InetAddress> ip = nwi.getInetAddresses();

        while ( ip.hasMoreElements() ) {
          InetAddress in = ip.nextElement();
          boolean hasNewHostName = !lastHostname.equalsIgnoreCase( "localhost" );
          if ( InetAddressUtils.isIPv4Address( in.getHostAddress() ) || !hasNewHostName  ) {
            lastHostname = in.getHostName();
          }
          if ( hasNewHostName && lastHostname.indexOf( ':' ) < 0 )  {
            break;
          }
        }
      }
    } catch ( SocketException e ) {
      // Eat exception, just return what you have
    }

    cachedHostname = lastHostname;

    return lastHostname;
  }

  /**
   * Determine the hostname of the machine Kettle is running on
   *
   * @return The hostname
   */
  public static String getHostnameReal() {

    // In case we don't want to leave anything to doubt...
    //
    String systemHostname = EnvUtil.getSystemProperty( KETTLE_SYSTEM_HOSTNAME );
    if ( !Utils.isEmpty( systemHostname ) ) {
      return systemHostname;
    }

    if ( isWindows() ) {
      // Windows will always set the 'COMPUTERNAME' variable
      return System.getenv( "COMPUTERNAME" );
    } else {
      // If it is not Windows then it is most likely a Unix-like operating system
      // such as Solaris, AIX, HP-UX, Linux or MacOS.
      // Most modern shells (such as Bash or derivatives) sets the
      // HOSTNAME variable so lets try that first.
      String hostname = System.getenv( "HOSTNAME" );
      if ( hostname != null ) {
        return hostname;
      } else {
        BufferedReader br;
        try {
          Process pr = Runtime.getRuntime().exec( "hostname" );
          br = new BufferedReader( new InputStreamReader( pr.getInputStream() ) );
          String line;
          if ( ( line = br.readLine() ) != null ) {
            return line;
          }
          pr.waitFor();
          br.close();
        } catch ( IOException e ) {
          return getHostname();
        } catch ( InterruptedException e ) {
          return getHostname();
        }
      }
    }
    return getHostname();
  }

  /**
   * Determins the IP address of the machine Kettle is running on.
   *
   * @return The IP address
   */
  public static String getIPAddress() throws Exception {
    Enumeration<NetworkInterface> enumInterfaces = NetworkInterface.getNetworkInterfaces();
    while ( enumInterfaces.hasMoreElements() ) {
      NetworkInterface nwi = enumInterfaces.nextElement();
      Enumeration<InetAddress> ip = nwi.getInetAddresses();
      while ( ip.hasMoreElements() ) {
        InetAddress in = ip.nextElement();
        if ( !in.isLoopbackAddress() && in.toString().indexOf( ":" ) < 0 ) {
          return in.getHostAddress();
        }
      }
    }
    return "127.0.0.1";
  }

  /**
   * Get the primary IP address tied to a network interface (excluding loop-back etc)
   *
   * @param networkInterfaceName
   *          the name of the network interface to interrogate
   * @return null if the network interface or address wasn't found.
   *
   * @throws SocketException
   *           in case of a security or network error
   */
  public static String getIPAddress( String networkInterfaceName ) throws SocketException {
    NetworkInterface networkInterface = NetworkInterface.getByName( networkInterfaceName );
    Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
    while ( ipAddresses.hasMoreElements() ) {
      InetAddress inetAddress = ipAddresses.nextElement();
      if ( !inetAddress.isLoopbackAddress() && inetAddress.toString().indexOf( ":" ) < 0 ) {
        String hostname = inetAddress.getHostAddress();
        return hostname;
      }
    }
    return null;
  }

  /**
   * Tries to determine the MAC address of the machine Kettle is running on.
   *
   * @return The MAC address.
   */
  public static String getMACAddress() throws Exception {
    String ip = getIPAddress();
    String mac = "none";
    String os = getOS();
    String s = "";
    @SuppressWarnings( "unused" )
    Boolean errorOccured = false;
    // System.out.println("os = "+os+", ip="+ip);

    if ( os.equalsIgnoreCase( "Windows NT" )
      || os.equalsIgnoreCase( "Windows 2000" ) || os.equalsIgnoreCase( "Windows XP" )
      || os.equalsIgnoreCase( "Windows 95" ) || os.equalsIgnoreCase( "Windows 98" )
      || os.equalsIgnoreCase( "Windows Me" ) || os.startsWith( "Windows" ) ) {
      try {
        // System.out.println("EXEC> nbtstat -a "+ip);

        Process p = Runtime.getRuntime().exec( "nbtstat -a " + ip );

        // read the standard output of the command
        BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

        while ( !procDone( p ) ) {
          while ( ( s = stdInput.readLine() ) != null ) {
            // System.out.println("NBTSTAT> "+s);
            if ( s.indexOf( "MAC" ) >= 0 ) {
              int idx = s.indexOf( '=' );
              mac = s.substring( idx + 2 );
            }
          }
        }
        stdInput.close();
      } catch ( Exception e ) {
        errorOccured = true;
      }
    } else if ( os.equalsIgnoreCase( "Linux" ) ) {
      try {
        Process p = Runtime.getRuntime().exec( "/sbin/ifconfig -a" );

        // read the standard output of the command
        BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

        while ( !procDone( p ) ) {
          while ( ( s = stdInput.readLine() ) != null ) {
            int idx = s.indexOf( "HWaddr" );
            if ( idx >= 0 ) {
              mac = s.substring( idx + 7 );
            }
          }
        }
        stdInput.close();
      } catch ( Exception e ) {
        errorOccured = true;

      }
    } else if ( os.equalsIgnoreCase( "Solaris" ) ) {
      try {
        Process p = Runtime.getRuntime().exec( "/usr/sbin/ifconfig -a" );

        // read the standard output of the command
        BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

        while ( !procDone( p ) ) {
          while ( ( s = stdInput.readLine() ) != null ) {
            int idx = s.indexOf( "ether" );
            if ( idx >= 0 ) {
              mac = s.substring( idx + 6 );
            }
          }
        }
        stdInput.close();
      } catch ( Exception e ) {
        errorOccured = true;

      }
    } else if ( os.equalsIgnoreCase( "HP-UX" ) ) {
      try {
        Process p = Runtime.getRuntime().exec( "/usr/sbin/lanscan -a" );

        // read the standard output of the command
        BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

        while ( !procDone( p ) ) {
          while ( ( s = stdInput.readLine() ) != null ) {
            if ( s.indexOf( "MAC" ) >= 0 ) {
              int idx = s.indexOf( "0x" );
              mac = s.substring( idx + 2 );
            }
          }
        }
        stdInput.close();
      } catch ( Exception e ) {
        errorOccured = true;

      }
    }
    // should do something if we got an error processing!
    return Const.trim( mac );
  }

  private static final boolean procDone( Process p ) {
    try {
      p.exitValue();
      return true;
    } catch ( IllegalThreadStateException e ) {
      return false;
    }
  }

  /**
   * Determines if the RUNNING_ON_WEBSPOON_MODE flag is set and returns its boolean value.
   * This is per user-basis.
   *
   * @return Boolean signalig the use of Webspoon mode.
   */
  public static boolean isRunningOnWebspoonMode() {
    return Boolean.parseBoolean( NVL( System.getenv( "RUNNING_ON_WEBSPOON_MODE" ), NVL( System.getProperty( "RUNNING_ON_WEBSPOON_MODE" ),
            "false" ) ) );
  }

  /**
   * Looks up the user's home directory (or KETTLE_HOME) for every invocation. This is no longer a static property so
   * the value may be set after this class is loaded.
   *
   * @return The path to the users home directory, or the System property {@code KETTLE_HOME} if set.
   */
  public static String getUserHomeDirectory() {
    return NVL( System.getenv( "KETTLE_HOME" ), NVL( System.getProperty( "KETTLE_HOME" ),
        System.getProperty( "user.home" ) ) );
  }

  /**
   * Determines the Kettle absolute directory in the user's home directory.
   *
   * @return The Kettle absolute directory.
   */
  public static String getKettleDirectory() {
    return getUserHomeDirectory() + FILE_SEPARATOR + getUserBaseDir();
  }

  /**
   * Determines the Kettle user data directory in the user's home directory.
   * This is per user-basis.
   *
   * @return The Kettle user data directory.
   */
  public static String getUserDataDirectory() {
    String dataDir =  getKettleDirectory() + Const.FILE_SEPARATOR + "data";
    return NVL( System.getenv( "WEBSPOON_USER_HOME" ), NVL( System.getProperty( "WEBSPOON_USER_HOME" ),
            dataDir ) );
  }

  /**
   * Determines the Kettle directory in the user's home directory.
   *
   * @return The Kettle directory.
   */
  public static String getUserBaseDir() {
    return BasePropertyHandler.getProperty( "userBaseDir", ".kettle" );
  }

  /**
   * Returns the value of DI_HOME.
   */
  public static String getDIHomeDirectory() {
    return System.getProperty( "DI_HOME" );
  }

  /**
   * Determines the location of the shared objects file
   *
   * @return the name of the shared objects file
   */
  public static String getSharedObjectsFile() {
    return getKettleDirectory() + FILE_SEPARATOR + SHARED_DATA_FILE;
  }

  /**
   * Returns the path to the Kettle local (current directory) repositories XML file.
   *
   * @return The local repositories file.
   */
  public static String getKettleLocalRepositoriesFile() {
    return "repositories.xml";
  }

  /**
   * Returns the full path to the Kettle repositories XML file.
   *
   * @return The Kettle repositories file.
   */
  public static String getKettleUserRepositoriesFile() {
    return getKettleDirectory() + FILE_SEPARATOR + getKettleLocalRepositoriesFile();
  }

  /**
   * Returns the full path to the Kettle properties XML file.
   *
   * @return The Kettle properties file.
   */
  public static String getKettlePropertiesFilename() {
    return Const.getKettleDirectory() + FILE_SEPARATOR + Const.KETTLE_PROPERTIES;
  }

  /**
   * Returns the path to the Kettle local (current directory) Carte password file:
   * <p>
   * ./pwd/kettle.pwd<br>
   *
   * @return The local Carte password file.
   */
  public static String getKettleLocalCartePasswordFile() {
    return "pwd/kettle.pwd";
  }

  /**
   * Returns the path to the Kettle Carte password file in the home directory:
   * <p>
   * $KETTLE_HOME/.kettle/kettle.pwd<br>
   *
   * @return The Carte password file in the home directory.
   */
  public static String getKettleCartePasswordFile() {
    return getKettleDirectory() + FILE_SEPARATOR + "kettle.pwd";
  }

  /**
   * Provides the base documentation url (top-level help)
   *
   * @return the fully qualified base documentation URL
   */
  public static String getBaseDocUrl() {
    return BaseMessages.getString( PKG, "Const.BaseDocUrl" );
  }

  /**
   * Provides the documentation url with the configured base + the given URI.
   *
   * @param uri
   *          the resource identifier for the documentation
   *          (eg. Products/Data_Integration/Data_Integration_Perspective/050/000)
   *
   * @return the fully qualified documentation URL for the given URI
   */
  public static String getDocUrl( final String uri ) {
    // initialize the docUrl to point to the top-level doc page
    String docUrl = getBaseDocUrl();
    if ( !Utils.isEmpty( uri ) ) {
      // if the uri is not empty, use it to build the URL
      if ( uri.startsWith( "http" ) ) {
        // use what is provided, it's already absolute
        docUrl = uri;
      } else {
        // the uri provided needs to be assembled
        docUrl = uri.startsWith( "/" ) ? docUrl + uri.substring( 1 ) : docUrl + uri;
      }
    }
    return docUrl;
  }

  /**
   * Retrieves the content of an environment variable
   *
   * @param variable
   *          The name of the environment variable
   * @param deflt
   *          The default value in case no value was found
   * @return The value of the environment variable or the value of deflt in case no variable was defined.
   */
  public static String getEnvironmentVariable( String variable, String deflt ) {
    return System.getProperty( variable, deflt );
  }

  /**
   * Replaces environment variables in a string. For example if you set KETTLE_HOME as an environment variable, you can
   * use %%KETTLE_HOME%% in dialogs etc. to refer to this value. This procedures looks for %%...%% pairs and replaces
   * them including the name of the environment variable with the actual value. In case the variable was not set,
   * nothing is replaced!
   *
   * @param string
   *          The source string where text is going to be replaced.
   *
   * @return The expanded string.
   * @deprecated use StringUtil.environmentSubstitute(): handles both Windows and unix conventions
   */
  @Deprecated
  public static String replEnv( String string ) {
    if ( string == null ) {
      return null;
    }
    StringBuilder str = new StringBuilder( string );

    int idx = str.indexOf( "%%" );
    while ( idx >= 0 ) {
      // OK, so we found a marker, look for the next one...
      int to = str.indexOf( "%%", idx + 2 );
      if ( to >= 0 ) {
        // OK, we found the other marker also...
        String marker = str.substring( idx, to + 2 );
        String var = str.substring( idx + 2, to );

        if ( var != null && var.length() > 0 ) {
          // Get the environment variable
          String newval = getEnvironmentVariable( var, null );

          if ( newval != null ) {
            // Replace the whole bunch
            str.replace( idx, to + 2, newval );

            // The last position has changed...
            to += newval.length() - marker.length();
          }
        }

      } else {
        // We found the start, but NOT the ending %% without closing %%
        to = idx;
      }

      // Look for the next variable to replace...
      idx = str.indexOf( "%%", to + 1 );
    }

    return str.toString();
  }

  /**
   * Replaces environment variables in an array of strings.
   * <p>
   * See also: replEnv(String string)
   *
   * @param string
   *          The array of strings that wants its variables to be replaced.
   * @return the array with the environment variables replaced.
   * @deprecated please use StringUtil.environmentSubstitute now.
   */
  @Deprecated
  public static String[] replEnv( String[] string ) {
    String[] retval = new String[string.length];
    for ( int i = 0; i < string.length; i++ ) {
      retval[i] = Const.replEnv( string[i] );
    }
    return retval;
  }

  /**
   * Implements Oracle style NVL function
   *
   * @param source
   *          The source argument
   * @param def
   *          The default value in case source is null or the length of the string is 0
   * @return source if source is not null, otherwise return def
   */
  public static String NVL( String source, String def ) {
    if ( source == null || source.length() == 0 ) {
      return def;
    }
    return source;
  }

  /**
   * Return empty string "" in case the given parameter is null, otherwise return the same value.
   *
   * @param source
   *          The source value to check for null.
   * @return empty string if source is null, otherwise simply return the source value.
   */
  public static String nullToEmpty( String source ) {
    if ( source == null ) {
      return EMPTY_STRING;
    }
    return source;
  }

  /**
   * Search for a string in an array of strings and return the index.
   *
   * @param lookup
   *          The string to search for
   * @param array
   *          The array of strings to look in
   * @return The index of a search string in an array of strings. -1 if not found.
   */
  public static int indexOfString( String lookup, String[] array ) {
    if ( array == null ) {
      return -1;
    }
    if ( lookup == null ) {
      return -1;
    }

    for ( int i = 0; i < array.length; i++ ) {
      if ( lookup.equalsIgnoreCase( array[i] ) ) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Search for strings in an array of strings and return the indexes.
   *
   * @param lookup
   *          The strings to search for
   * @param array
   *          The array of strings to look in
   * @return The indexes of strings in an array of strings. -1 if not found.
   */
  public static int[] indexsOfStrings( String[] lookup, String[] array ) {
    int[] indexes = new int[lookup.length];
    for ( int i = 0; i < indexes.length; i++ ) {
      indexes[i] = indexOfString( lookup[i], array );
    }
    return indexes;
  }

  /**
   * Search for strings in an array of strings and return the indexes. If a string is not found, the index is not
   * returned.
   *
   * @param lookup
   *          The strings to search for
   * @param array
   *          The array of strings to look in
   * @return The indexes of strings in an array of strings. Only existing indexes are returned (no -1)
   */
  public static int[] indexsOfFoundStrings( String[] lookup, String[] array ) {
    List<Integer> indexesList = new ArrayList<>();
    for ( int i = 0; i < lookup.length; i++ ) {
      int idx = indexOfString( lookup[i], array );
      if ( idx >= 0 ) {
        indexesList.add( Integer.valueOf( idx ) );
      }
    }
    int[] indexes = new int[indexesList.size()];
    for ( int i = 0; i < indexesList.size(); i++ ) {
      indexes[i] = ( indexesList.get( i ) ).intValue();
    }
    return indexes;
  }

  /**
   * Search for a string in a list of strings and return the index.
   *
   * @param lookup
   *          The string to search for
   * @param list
   *          The ArrayList of strings to look in
   * @return The index of a search string in an array of strings. -1 if not found.
   */
  public static int indexOfString( String lookup, List<String> list ) {
    if ( list == null ) {
      return -1;
    }

    for ( int i = 0; i < list.size(); i++ ) {
      String compare = list.get( i );
      if ( lookup.equalsIgnoreCase( compare ) ) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Sort the strings of an array in alphabetical order.
   *
   * @param input
   *          The array of strings to sort.
   * @return The sorted array of strings.
   */
  public static String[] sortStrings( String[] input ) {
    Arrays.sort( input );
    return input;
  }

  /**
   * Convert strings separated by a string into an array of strings.
   * <p>
   * <code>
  Example: a;b;c;d    ==>    new String[] { a, b, c, d }
   * </code>
   *
   * <p>
   * <b>NOTE: this differs from String.split() in a way that the built-in method uses regular expressions and this one
   * does not.</b>
   *
   * @param string
   *          The string to split
   * @param separator
   *          The separator used.
   * @return the string split into an array of strings
   */
  public static String[] splitString( String string, String separator ) {
    /*
     * 0123456 Example a;b;c;d --> new String[] { a, b, c, d }
     */
    // System.out.println("splitString ["+path+"] using ["+separator+"]");
    List<String> list = new ArrayList<>();

    if ( string == null || string.length() == 0 ) {
      return new String[] {};
    }

    int sepLen = separator.length();
    int from = 0;
    int end = string.length() - sepLen + 1;

    for ( int i = from; i < end; i += sepLen ) {
      if ( string.substring( i, i + sepLen ).equalsIgnoreCase( separator ) ) {
        // OK, we found a separator, the string to add to the list
        // is [from, i[
        list.add( nullToEmpty( string.substring( from, i ) ) );
        from = i + sepLen;
      }
    }

    // Wait, if the string didn't end with a separator, we still have information at the end of the string...
    // In our example that would be "d"...
    if ( from + sepLen <= string.length() ) {
      list.add( nullToEmpty( string.substring( from, string.length() ) ) );
    }

    return list.toArray( new String[list.size()] );
  }

  /**
   * Convert strings separated by a character into an array of strings.
   * <p>
   * <code>
   Example: a;b;c;d    ==  new String[] { a, b, c, d }
   * </code>
   *
   * @param string
   *          The string to split
   * @param separator
   *          The separator used.
   * @return the string split into an array of strings
   */
  public static String[] splitString( String string, char separator ) {
    return splitString( string, separator, false );
  }

  /**
   * Convert strings separated by a character into an array of strings.
   * <p>
   * <code>
    Example: a;b;c;d    ==  new String[] { a, b, c, d }
   * </code>
   *
   * @param string
   *          The string to split
   * @param separator
   *          The separator used.
   * @param escape
   *          in case the separator can be escaped (\;) The escape characters are NOT removed!
   * @return the string split into an array of strings
   */
  public static String[] splitString( String string, char separator, boolean escape ) {
    /*
     * 0123456 Example a;b;c;d --> new String[] { a, b, c, d }
     */
    // System.out.println("splitString ["+path+"] using ["+separator+"]");
    List<String> list = new ArrayList<>();

    if ( string == null || string.length() == 0 ) {
      return new String[] {};
    }

    int from = 0;
    int end = string.length();

    for ( int i = from; i < end; i += 1 ) {
      boolean found = string.charAt( i ) == separator;
      if ( found && escape && i > 0 ) {
        found &= string.charAt( i - 1 ) != '\\';
      }
      if ( found ) {
        // OK, we found a separator, the string to add to the list
        // is [from, i[
        list.add( nullToEmpty( string.substring( from, i ) ) );
        from = i + 1;
      }
    }

    // Wait, if the string didn't end with a separator, we still have information at the end of the string...
    // In our example that would be "d"...
    if ( from + 1 <= string.length() ) {
      list.add( nullToEmpty( string.substring( from, string.length() ) ) );
    }

    return list.toArray( new String[list.size()] );
  }

  /**
   * Convert strings separated by a string into an array of strings.
   * <p>
   * <code>
   *   Example /a/b/c --> new String[] { a, b, c }
   * </code>
   *
   * @param path
   *          The string to split
   * @param separator
   *          The separator used.
   * @return the string split into an array of strings
   */
  public static String[] splitPath( String path, String separator ) {
    //
    // Example /a/b/c --> new String[] { a, b, c }
    //
    // Make sure training slashes are removed
    //
    // Example /a/b/c/ --> new String[] { a, b, c }
    //

    // Check for empty paths...
    //
    if ( path == null || path.length() == 0 || path.equals( separator ) ) {
      return new String[] {};
    }

    // lose trailing separators
    //
    while ( path.endsWith( separator ) ) {
      path = path.substring( 0, path.length() - 1 );
    }

    int sepLen = separator.length();
    int nr_separators = 1;
    int from = path.startsWith( separator ) ? sepLen : 0;

    for ( int i = from; i < path.length(); i += sepLen ) {
      if ( path.substring( i, i + sepLen ).equalsIgnoreCase( separator ) ) {
        nr_separators++;
      }
    }

    String[] spath = new String[nr_separators];
    int nr = 0;
    for ( int i = from; i < path.length(); i += sepLen ) {
      if ( path.substring( i, i + sepLen ).equalsIgnoreCase( separator ) ) {
        spath[nr] = path.substring( from, i );
        nr++;

        from = i + sepLen;
      }
    }
    if ( nr < spath.length ) {
      spath[nr] = path.substring( from );
    }

    //
    // a --> { a }
    //
    if ( spath.length == 0 && path.length() > 0 ) {
      spath = new String[] { path };
    }

    return spath;
  }

  /**
   * Split the given string using the given delimiter and enclosure strings.
   *
   * The delimiter and enclosures are not regular expressions (regexes); rather they are literal strings that will be
   * quoted so as not to be treated like regexes.
   *
   * This method expects that the data contains an even number of enclosure strings in the input; otherwise the results
   * are undefined
   *
   * @param stringToSplit
   *          the String to split
   * @param delimiter
   *          the delimiter string
   * @param enclosure
   *          the enclosure string
   * @return an array of strings split on the delimiter (ignoring those in enclosures), or null if the string to split
   *         is null.
   */
  public static String[] splitString( String stringToSplit, String delimiter, String enclosure ) {
    return splitString( stringToSplit, delimiter, enclosure, false );
  }

  /**
   * Split the given string using the given delimiter and enclosure strings.
   *
   * The delimiter and enclosures are not regular expressions (regexes); rather they are literal strings that will be
   * quoted so as not to be treated like regexes.
   *
   * This method expects that the data contains an even number of enclosure strings in the input; otherwise the results
   * are undefined
   *
   * @param stringToSplit
   *          the String to split
   * @param delimiter
   *          the delimiter string
   * @param enclosure
   *          the enclosure string
   * @param removeEnclosure
   *          removes enclosure from split result
   * @return an array of strings split on the delimiter (ignoring those in enclosures), or null if the string to split
   *         is null.
   */
  public static String[] splitString( String stringToSplit, String delimiter, String enclosure, boolean removeEnclosure ) {

    ArrayList<String> splitList = null;

    // Handle "bad input" cases
    if ( stringToSplit == null ) {
      return null;
    }
    if ( delimiter == null ) {
      return ( new String[] { stringToSplit } );
    }

    // Split the string on the delimiter, we'll build the "real" results from the partial results
    String[] delimiterSplit = stringToSplit.split( Pattern.quote( delimiter ) );

    // At this point, if the enclosure is null or empty, we will return the delimiter split
    if ( Utils.isEmpty( enclosure ) ) {
      return delimiterSplit;
    }

    // Keep track of partial splits and concatenate them into a legit split
    StringBuilder concatSplit = null;

    if ( delimiterSplit != null && delimiterSplit.length > 0 ) {

      // We'll have at least one result so create the result list object
      splitList = new ArrayList<>();

      // Proceed through the partial splits, concatenating if the splits are within the enclosure
      for ( String currentSplit : delimiterSplit ) {
        if ( !currentSplit.contains( enclosure ) ) {

          // If we are currently concatenating a split, we are inside an enclosure. Since this
          // split doesn't contain an enclosure, we can concatenate it (with a delimiter in front).
          // If we're not concatenating, the split is fine so add it to the result list.
          if ( concatSplit != null ) {
            concatSplit.append( delimiter );
            concatSplit.append( currentSplit );
          } else {
            splitList.add( currentSplit );
          }
        } else {
          // Find number of enclosures in the split, and whether that number is odd or even.
          int numEnclosures = StringUtils.countMatches( currentSplit, enclosure );
          boolean oddNumberOfEnclosures = ( numEnclosures % 2 != 0 );
          boolean addSplit = false;

          // This split contains an enclosure, so either start or finish concatenating
          if ( concatSplit == null ) {
            concatSplit = new StringBuilder( currentSplit ); // start concatenation
            addSplit = !oddNumberOfEnclosures;
          } else {
            // Check to make sure a new enclosure hasn't started within this split. This method expects
            // that there are no non-delimiter characters between a delimiter and a starting enclosure.

            // At this point in the code, the split shouldn't start with the enclosure, so add a delimiter
            concatSplit.append( delimiter );

            // Add the current split to the concatenated split
            concatSplit.append( currentSplit );

            // If the number of enclosures is odd, the enclosure is closed so add the split to the list
            // and reset the "concatSplit" buffer. Otherwise continue
            addSplit = oddNumberOfEnclosures;
          }
          // Check if enclosure is also using inside data
          if ( addSplit || numEnclosures > 2 ) {
            String splitResult = concatSplit.toString();
            //remove enclosure from resulting split
            if ( removeEnclosure ) {
              splitResult = removeEnclosure( splitResult, enclosure );
            }

            splitList.add( splitResult );
            concatSplit = null;
            addSplit = false;
          }
        }
      }
    }

    // Return list as array
    return splitList.toArray( new String[splitList.size()] );
  }

  private static String removeEnclosure( String stringToSplit, String enclosure ) {

    int firstIndex = stringToSplit.indexOf( enclosure );
    int lastIndex = stringToSplit.lastIndexOf( enclosure );
    if ( firstIndex == lastIndex ) {
      return stringToSplit;
    }
    StrBuilder strBuilder = new StrBuilder( stringToSplit );
    strBuilder.replace( firstIndex, enclosure.length() + firstIndex, "" );
    strBuilder.replace( lastIndex - enclosure.length(), lastIndex, "" );

    return strBuilder.toString();
  }

  /**
   * Sorts the array of Strings, determines the uniquely occurring strings.
   *
   * @param strings
   *          the array that you want to do a distinct on
   * @return a sorted array of uniquely occurring strings
   */
  public static String[] getDistinctStrings( String[] strings ) {
    if ( strings == null ) {
      return null;
    }
    if ( strings.length == 0 ) {
      return new String[] {};
    }

    String[] sorted = sortStrings( strings );
    List<String> result = new ArrayList<>();
    String previous = "";
    for ( int i = 0; i < sorted.length; i++ ) {
      if ( !sorted[i].equalsIgnoreCase( previous ) ) {
        result.add( sorted[i] );
      }
      previous = sorted[i];
    }

    return result.toArray( new String[result.size()] );
  }

  /**
   * Returns a string of the stack trace of the specified exception
   */
  public static String getStackTracker( Throwable e ) {
    return getClassicStackTrace( e );
  }

  public static String getClassicStackTrace( Throwable e ) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter( stringWriter );
    e.printStackTrace( printWriter );
    String string = stringWriter.toString();
    try {
      stringWriter.close();
    } catch ( IOException ioe ) {
      // is this really required?
    }
    return string;
  }

  public static String getCustomStackTrace( Throwable aThrowable ) {
    final StringBuilder result = new StringBuilder();
    String errorMessage = aThrowable.toString();
    result.append( errorMessage );
    if ( !errorMessage.contains( Const.CR ) ) {
      result.append( CR );
    }

    // add each element of the stack trace
    //
    for ( StackTraceElement element : aThrowable.getStackTrace() ) {
      result.append( element );
      result.append( CR );
    }
    return result.toString();
  }

  /**
   * Check if the string supplied is empty. A String is empty when it is null or when the length is 0
   *
   * @param val
   *          The value to check
   * @return true if the string supplied is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(CharSequence)
   */
  @Deprecated
  public static boolean isEmpty( String val ) {
    return Utils.isEmpty( val );
  }

  /**
   * Check if the stringBuffer supplied is empty. A StringBuffer is empty when it is null or when the length is 0
   *
   * @param val
   *          The stringBuffer to check
   * @return true if the stringBuffer supplied is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(CharSequence)
   */
  @Deprecated
  public static boolean isEmpty( StringBuffer val ) {
    return Utils.isEmpty( val );
  }

  /**
   * Check if the string array supplied is empty. A String array is empty when it is null or when the number of elements
   * is 0
   *
   * @param vals
   *          The string array to check
   * @return true if the string array supplied is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(CharSequence[])
   */
  @Deprecated
  public static boolean isEmpty( String[] vals ) {
    return Utils.isEmpty( vals );
  }

  /**
   * Check if the CharSequence supplied is empty. A CharSequence is empty when it is null or when the length is 0
   *
   * @param val
   *          The stringBuffer to check
   * @return true if the stringBuffer supplied is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(CharSequence)
   */
  @Deprecated
  public static boolean isEmpty( CharSequence val ) {
    return Utils.isEmpty( val );
  }

  /**
   * Check if the CharSequence array supplied is empty. A CharSequence array is empty when it is null or when the number of elements
   * is 0
   *
   * @param vals
   *          The string array to check
   * @return true if the string array supplied is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(CharSequence[])
   */
  @Deprecated
  public static boolean isEmpty( CharSequence[] vals ) {
    return Utils.isEmpty( vals );
  }

  /**
   * Check if the array supplied is empty. An array is empty when it is null or when the length is 0
   *
   * @param array
   *          The array to check
   * @return true if the array supplied is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(Object[])
   */
  @Deprecated
  public static boolean isEmpty( Object[] array ) {
    return Utils.isEmpty( array );
  }

  /**
   * Check if the list supplied is empty. An array is empty when it is null or when the length is 0
   *
   * @param list
   *          the list to check
   * @return true if the supplied list is empty
   * @deprecated
   * @see org.pentaho.di.core.util.Utils#isEmpty(List)
   */
  @Deprecated
  public static boolean isEmpty( List<?> list ) {
    return Utils.isEmpty( list );
  }

  /**
   * @return a new ClassLoader
   */
  public static ClassLoader createNewClassLoader() throws KettleException {
    try {
      // Nothing really in URL, everything is in scope.
      URL[] urls = new URL[] {};
      URLClassLoader ucl = new URLClassLoader( urls );

      return ucl;
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error during classloader creation", e );
    }
  }

  /**
   * Utility class for use in JavaScript to create a new byte array. This is surprisingly difficult to do in JavaScript.
   *
   * @return a new java byte array
   */
  public static byte[] createByteArray( int size ) {
    return new byte[size];
  }

  /**
   * Sets the first character of each word in upper-case.
   *
   * @param string
   *          The strings to convert to initcap
   * @return the input string but with the first character of each word converted to upper-case.
   */
  public static String initCap( String string ) {
    StringBuilder change = new StringBuilder( string );
    boolean new_word;
    int i;
    char lower, upper, ch;

    new_word = true;
    for ( i = 0; i < string.length(); i++ ) {
      lower = change.substring( i, i + 1 ).toLowerCase().charAt( 0 ); // Lowercase is default.
      upper = change.substring( i, i + 1 ).toUpperCase().charAt( 0 ); // Uppercase for new words.
      ch = upper;

      if ( new_word ) {
        change.setCharAt( i, upper );
      } else {
        change.setCharAt( i, lower );
      }

      new_word = false;

      // Cast to (int) is required for extended characters (SB)
      if ( !Character.isLetterOrDigit( (int) ch ) && ch != '_' ) {
        new_word = true;
      }
    }

    return change.toString();
  }

  /**
   * Create a valid filename using a name We remove all special characters, spaces, etc.
   *
   * @param name
   *          The name to use as a base for the filename
   * @return a valid filename
   */
  public static String createFilename( String name ) {
    StringBuilder filename = new StringBuilder();
    for ( int i = 0; i < name.length(); i++ ) {
      char c = name.charAt( i );
      if ( Character.isUnicodeIdentifierPart( c ) ) {
        filename.append( c );
      } else if ( Character.isWhitespace( c ) ) {
        filename.append( '_' );
      }
    }
    return filename.toString().toLowerCase();
  }

  public static String createFilename( String directory, String name, String extension ) {
    if ( directory.endsWith( Const.FILE_SEPARATOR ) ) {
      return directory + createFilename( name ) + extension;
    } else {
      return directory + Const.FILE_SEPARATOR + createFilename( name ) + extension;
    }
  }

  public static String createName( String filename ) {
    if ( Utils.isEmpty( filename ) ) {
      return filename;
    }

    String pureFilename = filenameOnly( filename );
    if ( pureFilename.endsWith( ".ktr" ) || pureFilename.endsWith( ".kjb" ) || pureFilename.endsWith( ".xml" ) ) {
      pureFilename = pureFilename.substring( 0, pureFilename.length() - 4 );
    }
    StringBuilder sb = new StringBuilder();
    for ( int i = 0; i < pureFilename.length(); i++ ) {
      char c = pureFilename.charAt( i );
      if ( Character.isUnicodeIdentifierPart( c ) ) {
        sb.append( c );
      } else if ( Character.isWhitespace( c ) ) {
        sb.append( ' ' );
      } else if ( c == '-' ) {
        sb.append( c );
      }
    }
    return sb.toString();
  }

  /**
   * <p>
   * Returns the pure filename of a filename with full path. E.g. if passed parameter is
   * <code>/opt/tomcat/logs/catalina.out</code> this method returns <code>catalina.out</code>. The method works with the
   * Environment variable <i>System.getProperty("file.separator")</i>, so on linux/Unix it will check for the last
   * occurrence of a frontslash, on windows for the last occurrence of a backslash.
   * </p>
   *
   * <p>
   * To make this OS independent, the method could check for the last occurrence of a frontslash and backslash and use
   * the higher value of both. Should work, since these characters aren't allowed in filenames on neither OS types (or
   * said differently: Neither linux nor windows can carry frontslashes OR backslashes in filenames). Just a suggestion
   * of an improvement ...
   * </p>
   *
   * @param sFullPath
   * @return
   */
  public static String filenameOnly( String sFullPath ) {
    if ( Utils.isEmpty( sFullPath ) ) {
      return sFullPath;
    }

    int idx = sFullPath.lastIndexOf( FILE_SEPARATOR );
    if ( idx != -1 ) {
      return sFullPath.substring( idx + 1 );
    } else {
      idx = sFullPath.lastIndexOf( '/' ); // URL, VFS/**/
      if ( idx != -1 ) {
        return sFullPath.substring( idx + 1 );
      } else {
        return sFullPath;
      }
    }
  }

  /**
   * Returning the localized date conversion formats. They get created once on first request.
   *
   * @return
   */
  public static String[] getDateFormats() {
    if ( dateFormats == null ) {
      int dateFormatsCount = toInt( BaseMessages.getString( PKG, "Const.DateFormat.Count" ), 0 );
      dateFormats = new String[dateFormatsCount];
      for ( int i = 1; i <= dateFormatsCount; i++ ) {
        dateFormats[i - 1] = BaseMessages.getString( PKG, "Const.DateFormat" + Integer.toString( i ) );
      }
    }
    return dateFormats;
  }

  /**
   * Returning the localized date conversion formats without time. They get created once on first request.
   *
   * @return
   */
  public static String[] getTimelessDateFormats() {
    if ( dateTimelessFormats == null ) {
      List<String> dateFormats = Arrays.asList( Const.getDateFormats() );
      dateFormats = dateFormats.stream()
        .filter( date -> !date.toLowerCase().contains( "hh" ) )
        .collect( Collectors.toList() );
      dateTimelessFormats = dateFormats.toArray( new String[dateFormats.size()] );
    }
    return dateTimelessFormats;
  }

  /**
   * Returning the localized number conversion formats. They get created once on first request.
   *
   * @return
   */
  public static String[] getNumberFormats() {
    if ( numberFormats == null ) {
      int numberFormatsCount = toInt( BaseMessages.getString( PKG, "Const.NumberFormat.Count" ), 0 );
      numberFormats = new String[numberFormatsCount + 1];
      numberFormats[0] = DEFAULT_NUMBER_FORMAT;
      for ( int i = 1; i <= numberFormatsCount; i++ ) {
        numberFormats[i] = BaseMessages.getString( PKG, "Const.NumberFormat" + Integer.toString( i ) );
      }
    }
    return numberFormats;
  }

  /**
   * @return An array of all default conversion formats, to be used in dialogs etc.
   */
  public static String[] getConversionFormats() {
    String[] dats = Const.getDateFormats();
    String[] nums = Const.getNumberFormats();
    int totsize = dats.length + nums.length;
    String[] formats = new String[totsize];
    for ( int x = 0; x < dats.length; x++ ) {
      formats[x] = dats[x];
    }
    for ( int x = 0; x < nums.length; x++ ) {
      formats[dats.length + x] = nums[x];
    }

    return formats;
  }

  public static String[] getTransformationAndJobFilterNames() {
    if ( STRING_TRANS_AND_JOB_FILTER_NAMES == null ) {
      STRING_TRANS_AND_JOB_FILTER_NAMES =
        new String[] {
          BaseMessages.getString( PKG, "Const.FileFilter.TransformationJob" ),
          BaseMessages.getString( PKG, "Const.FileFilter.Transformations" ),
          BaseMessages.getString( PKG, "Const.FileFilter.Jobs" ),
          BaseMessages.getString( PKG, "Const.FileFilter.XML" ),
          BaseMessages.getString( PKG, "Const.FileFilter.All" ) };
    }
    return STRING_TRANS_AND_JOB_FILTER_NAMES;
  }

  public static String[] getTransformationFilterNames() {
    if ( STRING_TRANS_FILTER_NAMES == null ) {
      STRING_TRANS_FILTER_NAMES =
        new String[] {
          BaseMessages.getString( PKG, "Const.FileFilter.Transformations" ),
          BaseMessages.getString( PKG, "Const.FileFilter.XML" ),
          BaseMessages.getString( PKG, "Const.FileFilter.All" ) };
    }
    return STRING_TRANS_FILTER_NAMES;
  }

  public static String[] getJobFilterNames() {
    if ( STRING_JOB_FILTER_NAMES == null ) {
      STRING_JOB_FILTER_NAMES =
        new String[] {
          BaseMessages.getString( PKG, "Const.FileFilter.Jobs" ),
          BaseMessages.getString( PKG, "Const.FileFilter.XML" ),
          BaseMessages.getString( PKG, "Const.FileFilter.All" ) };
    }
    return STRING_JOB_FILTER_NAMES;
  }

  /**
   * Return the current time as nano-seconds.
   *
   * @return time as nano-seconds.
   */
  public static long nanoTime() {
    return new Date().getTime() * 1000;
  }

  /**
   * Return the input string trimmed as specified.
   *
   * @param string
   *          String to be trimmed
   * @param trimType
   *          Type of trimming
   *
   * @return Trimmed string.
   */
  public static String trimToType( String string, int trimType ) {
    switch ( trimType ) {
      case ValueMetaInterface.TRIM_TYPE_BOTH:
        return trim( string );
      case ValueMetaInterface.TRIM_TYPE_LEFT:
        return ltrim( string );
      case ValueMetaInterface.TRIM_TYPE_RIGHT:
        return rtrim( string );
      case ValueMetaInterface.TRIM_TYPE_NONE:
      default:
        return string;
    }
  }

  /**
   * Trims a Date by resetting the time part to zero
   * @param date a Date object to trim (reset time to zero)
   * @return a Date object with time part reset to zero
   */
  public static Date trimDate( Date date ) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime( date );
    calendar.set( Calendar.MILLISECOND, 0 );
    calendar.set( Calendar.SECOND, 0 );
    calendar.set( Calendar.MINUTE, 0 );
    calendar.set( Calendar.HOUR_OF_DAY, 0 );
    return calendar.getTime();
  }

  /**
   * implemented to help prevent errors in matching up pluggable LAF directories and paths/files eliminating malformed
   * URLs - duplicate file separators or missing file separators.
   *
   * @param dir
   * @param file
   * @return concatenated string representing a file url
   */
  public static String safeAppendDirectory( String dir, String file ) {
    boolean dirHasSeparator = ( ( dir.lastIndexOf( FILE_SEPARATOR ) ) == dir.length() - 1 );
    boolean fileHasSeparator = ( file.indexOf( FILE_SEPARATOR ) == 0 );
    if ( ( dirHasSeparator && !fileHasSeparator ) || ( !dirHasSeparator && fileHasSeparator ) ) {
      return dir + file;
    }
    if ( dirHasSeparator && fileHasSeparator ) {
      return dir + file.substring( 1 );
    }
    return dir + FILE_SEPARATOR + file;
  }

  /**
   * Create an array of Strings consisting of spaces. The index of a String in the array determines the number of spaces
   * in that string.
   *
   * @return array of 'space' Strings.
   */
  public static String[] getEmptyPaddedStrings() {
    if ( emptyPaddedSpacesStrings == null ) {
      emptyPaddedSpacesStrings = new String[250];
      for ( int i = 0; i < emptyPaddedSpacesStrings.length; i++ ) {
        emptyPaddedSpacesStrings[i] = rightPad( "", i );
      }
    }
    return emptyPaddedSpacesStrings;
  }

  /**
   * Return the percentage of free memory for this JVM.
   *
   * @return Percentage of free memory.
   */
  public static int getPercentageFreeMemory() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();
    long allocatedMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long totalFreeMemory = ( freeMemory + ( maxMemory - allocatedMemory ) );

    return (int) Math.round( 100 * (double) totalFreeMemory / maxMemory );
  }

  /**
   * Return non digits only.
   *
   * @return non digits in a string.
   */

  public static String removeDigits( String input ) {
    if ( Utils.isEmpty( input ) ) {
      return null;
    }
    StringBuilder digitsOnly = new StringBuilder();
    char c;
    for ( int i = 0; i < input.length(); i++ ) {
      c = input.charAt( i );
      if ( !Character.isDigit( c ) ) {
        digitsOnly.append( c );
      }
    }
    return digitsOnly.toString();
  }

  /**
   * Return digits only.
   *
   * @return digits in a string.
   */
  public static String getDigitsOnly( String input ) {
    if ( Utils.isEmpty( input ) ) {
      return null;
    }
    StringBuilder digitsOnly = new StringBuilder();
    char c;
    for ( int i = 0; i < input.length(); i++ ) {
      c = input.charAt( i );
      if ( Character.isDigit( c ) ) {
        digitsOnly.append( c );
      }
    }
    return digitsOnly.toString();
  }

  /**
   * Remove time from a date.
   *
   * @return a date without hour.
   */
  public static Date removeTimeFromDate( Date input ) {
    if ( input == null ) {
      return null;
    }
    // Get an instance of the Calendar.
    Calendar calendar = Calendar.getInstance();

    // Make sure the calendar will not perform automatic correction.
    calendar.setLenient( false );

    // Set the time of the calendar to the given date.
    calendar.setTime( input );

    // Remove the hours, minutes, seconds and milliseconds.
    calendar.set( Calendar.HOUR_OF_DAY, 0 );
    calendar.set( Calendar.MINUTE, 0 );
    calendar.set( Calendar.SECOND, 0 );
    calendar.set( Calendar.MILLISECOND, 0 );

    // Return the date again.
    return calendar.getTime();
  }

  /**
   * Escape XML content. i.e. replace characters with &values;
   *
   * @param content
   *          content
   * @return escaped content
   */
  public static String escapeXML( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return StringEscapeUtils.escapeXml( content );
  }

  /**
   * Escape HTML content. i.e. replace characters with &values;
   *
   * @param content
   *          content
   * @return escaped content
   */
  public static String escapeHtml( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return StringEscapeUtils.escapeHtml( content );
  }

  /**
   * UnEscape HTML content. i.e. replace characters with &values;
   *
   * @param content
   *          content
   * @return unescaped content
   */
  public static String unEscapeHtml( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return StringEscapeUtils.unescapeHtml( content );
  }

  /**
   * UnEscape XML content. i.e. replace characters with &values;
   *
   * @param content
   *          content
   * @return unescaped content
   */
  public static String unEscapeXml( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return StringEscapeUtils.unescapeXml( content );
  }

  /**
   * Escape SQL content. i.e. replace characters with &values;
   *
   * @param content
   *          content
   * @return escaped content
   */
  public static String escapeSQL( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return StringEscapeUtils.escapeSql( content );
  }

  /**
   * Remove CR / LF from String - Better performance version
   *   - Doesn't NPE
   *   - 40 times faster on an empty string
   *   - 2 times faster on a mixed string
   *   - 25% faster on 2 char string with only CRLF in it
   *
   * @param in
   *          input
   * @return cleaned string
   */
  public static String removeCRLF( String in ) {
    if ( ( in != null ) && ( in.length() > 0 ) ) {
      int inLen = in.length(), posn = 0;
      char[] tmp = new char[ inLen ];
      char ch;
      for ( int i = 0; i < inLen; i++ ) {
        ch = in.charAt( i );
        if ( ( ch != '\n' && ch != '\r' ) ) {
          tmp[posn] = ch;
          posn++;
        }
      }
      return new String( tmp, 0, posn );
    } else {
      return "";
    }
  }

  /**
   * Remove Character from String - Better performance version
   *   - Doesn't NPE
   *   - 40 times faster on an empty string
   *   - 2 times faster on a mixed string
   *   - 25% faster on 2 char string with only CR/LF/TAB in it
   *
   * @param in
   *          input
   * @return cleaned string
   */
  public static String removeChar( String in, char badChar ) {
    if ( ( in != null ) && ( in.length() > 0 ) ) {
      int inLen = in.length(), posn = 0;
      char[] tmp = new char[ inLen ];
      char ch;
      for ( int i = 0; i < inLen; i++ ) {
        ch = in.charAt( i );
        if ( ch != badChar ) {
          tmp[posn] = ch;
          posn++;
        }
      }
      return new String( tmp, 0, posn );
    } else {
      return "";
    }
  }

  /**
   * Remove CR / LF from String
   *
   * @param in
   *          input
   * @return cleaned string
   */
  public static String removeCR( String in ) {
    return removeChar( in, '\r' );
  } // removeCR

  /**
   * Remove CR / LF from String
   *
   * @param in
   *          input
   * @return cleaned string
   */
  public static String removeLF( String in ) {
    return removeChar( in, '\n' );
  } // removeCRLF

  /**
   * Remove horizontal tab from string
   *
   * @param in
   *          input
   * @return cleaned string
   */
  public static String removeTAB( String in ) {
    return removeChar( in, '\t' );
  }

  /**
   * Add time to an input date
   *
   * @param input
   *          the date
   * @param time
   *          the time to add (in string)
   * @param dateFormat
   *          the time format
   * @return date = input + time
   */
  public static Date addTimeToDate( Date input, String time, String dateFormat ) throws Exception {
    if ( Utils.isEmpty( time ) ) {
      return input;
    }
    if ( input == null ) {
      return null;
    }
    String dateformatString = NVL( dateFormat, "HH:mm:ss" );
    int t = decodeTime( time, dateformatString );
    return new Date( input.getTime() + t );
  }

  // Decodes a time value in specified date format and returns it as milliseconds since midnight.
  public static int decodeTime( String s, String dateFormat ) throws Exception {
    SimpleDateFormat f = new SimpleDateFormat( dateFormat );
    TimeZone utcTimeZone = TimeZone.getTimeZone( "UTC" );
    f.setTimeZone( utcTimeZone );
    f.setLenient( false );
    ParsePosition p = new ParsePosition( 0 );
    Date d = f.parse( s, p );
    if ( d == null ) {
      throw new Exception( "Invalid time value " + dateFormat + ": \"" + s + "\"." );
    }
    return (int) d.getTime();
  }

  /**
   * Get the number of occurrences of searchFor in string.
   *
   * @param string
   *          String to be searched
   * @param searchFor
   *          to be counted string
   * @return number of occurrences
   */
  public static int getOccurenceString( String string, String searchFor ) {
    if ( string == null || string.length() == 0 ) {
      return 0;
    }
    int counter = 0;
    int len = searchFor.length();
    if ( len > 0 ) {
      int start = string.indexOf( searchFor );
      while ( start != -1 ) {
        counter++;
        start = string.indexOf( searchFor, start + len );
      }
    }
    return counter;
  }

  public static String[] GetAvailableFontNames() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Font[] fonts = ge.getAllFonts();
    String[] FontName = new String[fonts.length];
    for ( int i = 0; i < fonts.length; i++ ) {
      FontName[i] = fonts[i].getFontName();
    }
    return FontName;
  }

  public static String getKettlePropertiesFileHeader() {
    StringBuilder out = new StringBuilder();

    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line01", BuildVersion
      .getInstance().getVersion() )
      + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line02" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line03" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line04" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line05" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line06" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line07" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line08" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line09" ) + CR );
    out.append( BaseMessages.getString( PKG, "Props.Kettle.Properties.Sample.Line10" ) + CR );

    return out.toString();
  }

  /**
   * Mask XML content. i.e. protect with CDATA;
   *
   * @param content
   *          content
   * @return protected content
   */
  public static String protectXMLCDATA( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return "<![CDATA[" + content + "]]>";
  }

  /**
   * Get the number of occurrences of searchFor in string.
   *
   * @param string
   *          String to be searched
   * @param searchFor
   *          to be counted string
   * @return number of occurrences
   */
  public static int getOcuranceString( String string, String searchFor ) {
    if ( string == null || string.length() == 0 ) {
      return 0;
    }
    Pattern p = Pattern.compile( searchFor );
    Matcher m = p.matcher( string );
    int count = 0;
    while ( m.find() ) {
      ++count;
    }
    return count;
  }

  /**
   * Mask XML content. i.e. replace characters with &values;
   *
   * @param content
   *          content
   * @return masked content
   */
  public static String escapeXml( String content ) {
    if ( Utils.isEmpty( content ) ) {
      return content;
    }
    return StringEscapeUtils.escapeXml( content );
  }


  /**
   * Convert a string containing a URI with escaped special characters and return the decoded version depending on
   * system property settings.
   * @param uri
   * @return decoded URI string
   */
  public static String optionallyDecodeUriString( String uri ) {
    boolean decodeUri = !System.getProperty( KETTLE_RETURN_ESCAPED_URI_STRINGS, "N" )
      .equalsIgnoreCase( "Y" );
    if ( decodeUri ) {
      try {
        return UriParser.decode( uri );
      } catch ( FileSystemException e ) {
        // return the raw string if the URI is malformed (bad escape sequence)
        return uri;
      }
    } else {
      return uri;
    }
  }

  /**
   * New method avoids string concatenation is between 20% and > 2000% faster
   * depending on length of the string to pad, and the size to pad it to.
   * For larger amounts to pad, (e.g. pad a 4 character string out to 20 places)
   * this is orders of magnitude faster.
   *
   * @param valueToPad
   *    the string to pad
   * @param filler
   *    the pad string to fill with
   * @param size
   *    the size to pad to
   * @return
   *    the new string, padded to the left
   *
   * Note - The original method was flawed in a few cases:
   *
   *   1- The filler could be a string of any length - and the returned
   *   string was not necessarily limited to size. So a 3 character pad
   *   of an 11 character string could end up being 17 characters long.
   *   2- For a pad of zero characters ("") the former method would enter
   *   an infinite loop.
   *   3- For a null pad, it would throw an NPE
   *   4- For a null valueToPad, it would throw an NPE
   */
  public static String Lpad( String valueToPad, String filler, int size ) {
    if ( ( size == 0 ) || ( valueToPad == null ) || ( filler == null ) ) {
      return valueToPad;
    }
    int vSize = valueToPad.length();
    int fSize = filler.length();
    // This next if ensures previous behavior, but prevents infinite loop
    // if "" is passed in as a filler.
    if ( ( vSize >= size ) || ( fSize == 0 )  ) {
      return valueToPad;
    }
    int tgt = ( size - vSize );
    StringBuilder sb = new StringBuilder( size );
    sb.append( filler );
    while ( sb.length() < tgt ) {
      // instead of adding one character at a time, this
      // is exponential - much fewer times in loop
      sb.append( sb );
    }
    sb.append( valueToPad );
    return sb.substring( Math.max( 0, sb.length() - size ) ); // this makes sure you have the right size string returned.
  }

  /**
   * New method avoids string concatenation is between 50% and > 2000% faster
   * depending on length of the string to pad, and the size to pad it to.
   * For larger amounts to pad, (e.g. pad a 4 character string out to 20 places)
   * this is orders of magnitude faster.
   *
   * @param valueToPad
   *    the string to pad
   * @param filler
   *    the pad string to fill with
   * @param size
   *    the size to pad to
   * @return
   *   The string, padded to the right
   *
   *   1- The filler can still be a string of any length - and the returned
   *   string was not necessarily limited to size. So a 3 character pad
   *   of an 11 character string with a size of 15 could end up being 17
   *   characters long (instead of the "asked for 15").
   *   2- For a pad of zero characters ("") the former method would enter
   *   an infinite loop.
   *   3- For a null pad, it would throw an NPE
   *   4- For a null valueToPad, it would throw an NPE
   */
  public static String Rpad( String valueToPad, String filler, int size ) {
    if ( ( size == 0 ) || ( valueToPad == null ) || ( filler == null ) ) {
      return valueToPad;
    }
    int vSize = valueToPad.length();
    int fSize = filler.length();
    // This next if ensures previous behavior, but prevents infinite loop
    // if "" is passed in as a filler.
    if ( ( vSize >= size ) || ( fSize == 0 )  ) {
      return valueToPad;
    }
    int tgt = ( size - vSize );
    StringBuilder sb1 = new StringBuilder( size );
    sb1.append( filler );
    while ( sb1.length() < tgt ) {
      // instead of adding one character at a time, this
      // is exponential - much fewer times in loop
      sb1.append( sb1 );
    }
    StringBuilder sb = new StringBuilder( valueToPad );
    sb.append( sb1 );
    return sb.substring( 0, size );
  }

  public static boolean classIsOrExtends( Class<?> clazz, Class<?> superClass ) {
    if ( clazz.equals( Object.class ) ) {
      return false;
    }
    return clazz.equals( superClass ) || classIsOrExtends( clazz.getSuperclass(), superClass );
  }

  public static String getDeprecatedPrefix() {
    return " " + BaseMessages.getString( PKG, "Const.Deprecated" );
  }
}
