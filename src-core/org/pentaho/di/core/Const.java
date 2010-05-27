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

package org.pentaho.di.core;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
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

import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;

/**
 * This class is used to define a number of default values for various settings throughout Kettle.
 * It also contains a number of static final methods to make your life easier.
 *
 * @author Matt 
 * @since 07-05-2003
 *
 */
public class Const
{
	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /**
     *  Version number
     */
    public static final String VERSION = "4.0.0";
    
    /**
     *  Release Type 
     */ 
    public enum ReleaseType {RELEASE_CANDIDATE, MILESTONE, PREVIEW, GA}

	/**
	 * Sleep time waiting when buffer is empty
	 */
	public static final int TIMEOUT_GET_MILLIS = 50;

	/**
	 * Sleep time waiting when buffer is full
	 */
	public static final int TIMEOUT_PUT_MILLIS = 50; // luxury problem!

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
	public static final int FETCH_SIZE = 5000;

	/**
	 * Sort size: how many rows do we sort in memory at once?
	 */
	public static final int SORT_SIZE = 5000;

	/**
	 * What's the file systems file separator on this operating system?
	 */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * What's the path separator on this operating system?
	 */
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");

	/**
	 * CR: operating systems specific Carriage Return
	 */
	public static final String CR = System.getProperty("line.separator");

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
	public static final String JAVA_VERSION = System.getProperty("java.vm.version");

    /**
     * Path to the users home directory (keep this entry above references to getKettleDirectory())
     */
    public static final String USER_HOME_DIRECTORY = NVL(System.getProperty("KETTLE_HOME"), System.getProperty("user.home"));
    
    /**
     * Path to the simple-jndi directory
     */
    
    public static String JNDI_DIRECTORY = NVL(System.getProperty("KETTLE_JNDI_ROOT"), System.getProperty("org.osjava.sj.root")); //$NON-NLS-1$ //$NON-NLS-2$

	/*
	 * The images directory
	 *
	public static final String IMAGE_DIRECTORY = "/ui/images/";
    */

    
  public static final String PLUGIN_BASE_FOLDERS_PROP = "KETTLE_PLUGIN_BASE_FOLDERS";
  /**
   * the default comma separated list of base plugin folders.
   */
	public static final String DEFAULT_PLUGIN_BASE_FOLDERS = "plugins," + getKettleDirectory() + FILE_SEPARATOR + "plugins";

	/**
	 * Default minimum date range...
	 */
	public static final Date MIN_DATE = new Date(-2208992400000L); // 1900/01/01 00:00:00.000

	/**
	 * Default maximum date range...
	 */
	public static final Date MAX_DATE = new Date(7258114799468L); // 2199/12/31 23:59:59.999

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
	public static final int SHADOW_SIZE = 4;

	/**
	 *  The size of relationship symbols
	 */
	public static final int SYMBOLSIZE = 10;

	/**
	 * Max nr. of files to remember
	 */
	public static final int MAX_FILE_HIST = 9;

	/**
	 * The default locale for the kettle environment (system defined)
	 */
	public static final Locale DEFAULT_LOCALE = Locale.getDefault(); // new Locale("nl", "BE");

	/**
	 * The default decimal separator . or ,
	 */
	public static final char DEFAULT_DECIMAL_SEPARATOR = (new DecimalFormatSymbols(DEFAULT_LOCALE)).getDecimalSeparator();

	/**
	 * The default grouping separator , or .
	 */
	public static final char DEFAULT_GROUPING_SEPARATOR = (new DecimalFormatSymbols(DEFAULT_LOCALE)).getGroupingSeparator();

	/**
	 * The default currency symbol
	 */
	public static final String DEFAULT_CURRENCY_SYMBOL = (new DecimalFormatSymbols(DEFAULT_LOCALE)).getCurrencySymbol();

	/**
	 * The default number format
	 */
	public static final String DEFAULT_NUMBER_FORMAT = ((DecimalFormat) (NumberFormat.getInstance())).toPattern();

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
	 * A number of tips that are shown when the application first starts.
	 */
	private static String tips[];

	/**
	 * An array of date conversion formats
	 */
	private static String dateFormats[];

	/**
	 * An array of number conversion formats
	 */
	private static String numberFormats[];

	/**
	 * Generalized date/time format:
	 * Wherever dates are used, date and time values are organized from the most to the least significant.
	 * see also method StringUtil.getFormattedDateTime()
	 */
	public static final String GENERALIZED_DATE_TIME_FORMAT = "yyyyddMM_hhmmss";
	public static final String GENERALIZED_DATE_TIME_FORMAT_MILLIS = "yyyyddMM_hhmmssSSS";

	/**
	 * Default we store our information in Unicode UTF-8 character set.
	 */
	public static final String XML_ENCODING = "UTF-8";

    /** The possible extensions a transformation XML file can have. */
    public static final String STRING_TRANS_AND_JOB_FILTER_EXT[] = new String[] { "*.ktr;*.kjb;*.xml", "*.ktr;*.xml", "*.kjb;*.xml", "*.xml", "*.*" };

    /** The discriptions of the possible extensions a transformation XML file can have. */
    private static String STRING_TRANS_AND_JOB_FILTER_NAMES[];

	/** The extension of a Kettle transformation XML file */
	public static final String STRING_TRANS_DEFAULT_EXT = "ktr";

	/** The possible extensions a transformation XML file can have. */
	public static final String STRING_TRANS_FILTER_EXT[] = new String[] { "*.ktr;*.xml", "*.xml", "*.*" };

	/** The discriptions of the possible extensions a transformation XML file can have. */
	private static String STRING_TRANS_FILTER_NAMES[];

	/** The extension of a Kettle job XML file */
	public static final String STRING_JOB_DEFAULT_EXT = "kjb";

	/** The possible extensions a job XML file can have. */
	public static final String STRING_JOB_FILTER_EXT[] = new String[] { "*.kjb;*.xml", "*.xml", "*.*" };

	/** The discriptions of the possible extensions a job XML file can have. */
	private static String STRING_JOB_FILTER_NAMES[];

	/** Name of the kettle parameters file */
	public static final String KETTLE_PROPERTIES = "kettle.properties";

    /** The prefix that all internal kettle variables should have */
    public static final String INTERNAL_VARIABLE_PREFIX = "Internal";

    /** The version number as an internal variable */
    public static final String INTERNAL_VARIABLE_KETTLE_VERSION = INTERNAL_VARIABLE_PREFIX+".Kettle.Version";

    /** The build version as an internal variable */
    public static final String INTERNAL_VARIABLE_KETTLE_BUILD_VERSION = INTERNAL_VARIABLE_PREFIX+".Kettle.Build.Version";

    /** The build date as an internal variable */
    public static final String INTERNAL_VARIABLE_KETTLE_BUILD_DATE = INTERNAL_VARIABLE_PREFIX+".Kettle.Build.Date";
    
    /** The job filename directory */
    public static final String INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY = INTERNAL_VARIABLE_PREFIX+".Job.Filename.Directory";

    /** The job filename name */
    public static final String INTERNAL_VARIABLE_JOB_FILENAME_NAME = INTERNAL_VARIABLE_PREFIX+".Job.Filename.Name";

    /** The job name */
    public static final String INTERNAL_VARIABLE_JOB_NAME = INTERNAL_VARIABLE_PREFIX+".Job.Name";

    /** The job directory */
    public static final String INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY = INTERNAL_VARIABLE_PREFIX+".Job.Repository.Directory";

	/** 
	 * All the internal job variables
	 */
	public static final String[] INTERNAL_TRANS_VARIABLES = new String[] { 
        		Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY,
        		Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME,
        		Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME,
        		Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY,
        };
	
	/** 
	 * All the internal job variables
	 */
	public static final String[] INTERNAL_JOB_VARIABLES = new String[] { 
        		Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY,
        		Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME,
        		Const.INTERNAL_VARIABLE_JOB_NAME,
        		Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY,
        };
	
    /** The transformation filename directory */
    public static final String INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY = INTERNAL_VARIABLE_PREFIX+".Transformation.Filename.Directory";

    /** The transformation filename name */
    public static final String INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME = INTERNAL_VARIABLE_PREFIX+".Transformation.Filename.Name";

    /** The transformation name */
    public static final String INTERNAL_VARIABLE_TRANSFORMATION_NAME = INTERNAL_VARIABLE_PREFIX+".Transformation.Name";

    /** The transformation directory */
    public static final String INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY = INTERNAL_VARIABLE_PREFIX+".Transformation.Repository.Directory";

    /** The step partition ID */
    public static final String INTERNAL_VARIABLE_STEP_PARTITION_ID = INTERNAL_VARIABLE_PREFIX+".Step.Partition.ID";

    /** The step partition number */
    public static final String INTERNAL_VARIABLE_STEP_PARTITION_NR = INTERNAL_VARIABLE_PREFIX+".Step.Partition.Number";

    /** The slave transformation number */
    public static final String INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER = INTERNAL_VARIABLE_PREFIX+".Slave.Transformation.Number";

    /** The slave transformation name */
    public static final String INTERNAL_VARIABLE_SLAVE_SERVER_NAME = INTERNAL_VARIABLE_PREFIX+".Slave.Server.Name";

    /** The size of the cluster : number of slaves */
    public static final String INTERNAL_VARIABLE_CLUSTER_SIZE = INTERNAL_VARIABLE_PREFIX+".Cluster.Size";

    /** The slave transformation number */
    public static final String INTERNAL_VARIABLE_STEP_UNIQUE_NUMBER = INTERNAL_VARIABLE_PREFIX+".Step.Unique.Number";

    /** Is this transformation running clustered, on the master? */
    public static final String INTERNAL_VARIABLE_CLUSTER_MASTER = INTERNAL_VARIABLE_PREFIX+".Cluster.Master";

    /** The size of the cluster : number of slaves */
    public static final String INTERNAL_VARIABLE_STEP_UNIQUE_COUNT = INTERNAL_VARIABLE_PREFIX+".Step.Unique.Count";
    
    /** The step name */
    public static final String INTERNAL_VARIABLE_STEP_NAME = INTERNAL_VARIABLE_PREFIX+".Step.Name";

    /** The step copy nr */
    public static final String INTERNAL_VARIABLE_STEP_COPYNR = INTERNAL_VARIABLE_PREFIX+".Step.CopyNr";

    /** The default maximum for the nr of lines in the GUI logs */
    public static final int MAX_NR_LOG_LINES = 5000;

    /** The default maximum for the nr of lines in the history views */
    public static final int MAX_NR_HISTORY_LINES = 50;

    /** The default log line timeout in minutes : 12 hours */
    public static final int MAX_LOG_LINE_TIMEOUT_MINUTES = 12*60;

    /** UI-agnostic flag for warnings */
    public static final int WARNING = 1;
    
    /** UI-agnostic flag for warnings */
    public static final int ERROR = 2;
    
    /** UI-agnostic flag for warnings */
    public static final int INFO = 3;

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
	 * If you set this environment variable you can limit the log size of all transformations and jobs 
	 * that don't have the "log size limit" property set in their respective properties.
	 */
	public static final String KETTLE_LOG_SIZE_LIMIT = "KETTLE_LOG_SIZE_LIMIT";

	/**
	 * The name of the variable that defines the log database connection by default for all transformations
	 */
	public static final String	KETTLE_TRANS_LOG_DB	= "KETTLE_TRANS_LOG_DB";

	/**
	 * The name of the variable that defines the logging schema for all transformations
	 */
	public static final String	KETTLE_TRANS_LOG_SCHEMA	= "KETTLE_TRANS_LOG_SCHEMA";

	/**
	 * The name of the variable that defines the logging table for all transformations
	 */
	public static final String	KETTLE_TRANS_LOG_TABLE	= "KETTLE_TRANS_LOG_TABLE";

	/**
	 * The name of the variable that defines the log database connection by default for all jobs
	 */
	public static final String	KETTLE_JOB_LOG_DB	= "KETTLE_JOB_LOG_DB";

	/**
	 * The name of the variable that defines the logging schema for all jobs
	 */
	public static final String	KETTLE_JOB_LOG_SCHEMA	= "KETTLE_JOB_LOG_SCHEMA";

	/**
	 * The name of the variable that defines the logging table for all jobs
	 */
	public static final String	KETTLE_JOB_LOG_TABLE	= "KETTLE_JOB_LOG_TABLE";

	/**
	 * The name of the variable that defines the transformation performance log schema by default for all transformations
	 */
	public static final String	KETTLE_TRANS_PERFORMANCE_LOG_DB	= "KETTLE_TRANS_PERFORMANCE_LOG_DB";

	/**
	 * The name of the variable that defines the transformation performance log database connection by default for all transformations
	 */
	public static final String	KETTLE_TRANS_PERFORMANCE_LOG_SCHEMA	= "KETTLE_TRANS_PERFORMANCE_LOG_SCHEMA";

	/**
	 * The name of the variable that defines the transformation performance log table by default for all transformations
	 */
	public static final String	KETTLE_TRANS_PERFORMANCE_LOG_TABLE	= "KETTLE_TRANS_PERFORMANCE_LOG_TABLE";

	/**
	 * The name of the variable that defines the job entry log database by default for all jobs
	 */
	public static final String	KETTLE_JOBENTRY_LOG_DB	= "KETTLE_JOBENTRY_LOG_DB";

	/**
	 * The name of the variable that defines the job entry log schema by default for all jobs
	 */
	public static final String	KETTLE_JOBENTRY_LOG_SCHEMA	= "KETTLE_JOBENTRY_LOG_SCHEMA";

	/**
	 * The name of the variable that defines the job entry log table by default for all jobs
	 */
	public static final String	KETTLE_JOBENTRY_LOG_TABLE	= "KETTLE_JOBENTRY_LOG_TABLE";

	/**
	 * The name of the variable that defines the steps log database by default for all transformations
	 */
	public static final String	KETTLE_STEP_LOG_DB	= "KETTLE_STEP_LOG_DB";

	/**
	 * The name of the variable that defines the steps log schema by default for all transformations
	 */
	public static final String	KETTLE_STEP_LOG_SCHEMA	= "KETTLE_STEP_LOG_SCHEMA";

	/**
	 * The name of the variable that defines the steps log table by default for all transformations
	 */
	public static final String	KETTLE_STEP_LOG_TABLE	= "KETTLE_STEP_LOG_TABLE";

	/**
	 * The name of the variable that defines the log channel log database by default for all transformations and jobs
	 */
	public static final String	KETTLE_CHANNEL_LOG_DB	= "KETTLE_CHANNEL_LOG_DB";

	/**
	 * The name of the variable that defines the log channel log schema by default for all transformations and jobs
	 */
	public static final String	KETTLE_CHANNEL_LOG_SCHEMA	= "KETTLE_CHANNEL_LOG_SCHEMA";

	/**
	 * The name of the variable that defines the log channel log table by default for all transformations and jobs
	 */
	public static final String	KETTLE_CHANNEL_LOG_TABLE	= "KETTLE_CHANNEL_LOG_TABLE";

	/**
	 * Name of the environment variable to set the location of the shared object file (xml) for transformations and jobs
	 */
	public static final String KETTLE_SHARED_OBJECTS = "KETTLE_SHARED_OBJECTS";

	/**
	 * System wide flag to drive the evaluation of null in ValueMeta.  If this setting is set to "Y", an empty string and null are different.
	 * Otherwise they are not.
	 */
	public static final String KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL";

	/**
	 * System wide flag to set the maximum number of log lines that are kept internally by Kettle.
	 * Set to 0 to keep all rows (default)
	 */
	public static final String KETTLE_MAX_LOG_SIZE_IN_LINES = "KETTLE_MAX_LOG_SIZE_IN_LINES";

	/**
	 * System wide flag to set the maximum age (in minutes) of a log line while being kept internally by Kettle.
	 * Set to 0 to keep all rows indefinitely (default)
	 */
	public static final String KETTLE_MAX_LOG_TIMEOUT_IN_MINUTES = "KETTLE_MAX_LOG_TIMEOUT_IN_MINUTES";

	 /**
   * System wide parameter: the maximum number of step performance snapshots to keep in memory. 
   * Set to 0 to keep all snapshots indefinitely (default)
   */
  public static final String KETTLE_STEP_PERFORMANCE_SNAPSHOT_LIMIT = "KETTLE_STEP_PERFORMANCE_SNAPSHOT_LIMIT";

	/**
	 * The name of the system wide variable that can contain the name of the SAP Connection factory for the test button in the DB dialog.
	 * This defaults to  
	 */
	public static final String	KETTLE_SAP_CONNECTION_FACTORY	= "KETTLE_SAP_CONNECTION_FACTORY";

	/**
	 * The default SAP ERP connection factory
	 */
	public static final String	KETTLE_SAP_CONNECTION_FACTORY_DEFAULT_NAME	= "org.pentaho.di.trans.steps.sapinput.sap.SAPConnectionFactory";

	/**
	 * Name of the environment variable to specify additional classes to scan for plugin annotations
	 */
	public static final String KETTLE_PLUGIN_CLASSES = "KETTLE_PLUGIN_CLASSES";

	/**
	 * A general initial version comment 
	 */
	public static final String	VERSION_COMMENT_INITIAL_VERSION	= "Creation of initial version";

	/**
	 * A general edit version comment 
	 */
	public static final String	VERSION_COMMENT_EDIT_VERSION	= "Modification by user";

	/**
	 * The XML file that contains the list of native Kettle steps
	 */
	public static final String	XML_FILE_KETTLE_STEPS	= "kettle-steps.xml";

	/**
	 * The XML file that contains the list of native partition plugins
	 */
	public static final String	XML_FILE_KETTLE_PARTITION_PLUGINS	= "kettle-partition-plugins.xml";

	/**
	 * The XML file that contains the list of native Kettle job entries
	 */
	public static final String	XML_FILE_KETTLE_JOB_ENTRIES	= "kettle-job-entries.xml";

	/**
	 * The XML file that contains the list of native Kettle repository types (DB, File, etc)
	 */
	public static final String	XML_FILE_KETTLE_REPOSITORIES = "kettle-repositories.xml";

	/**
	 * The XML file that contains the list of native Kettle database types (MySQL, Oracle, etc)
	 */
	public static final String	XML_FILE_KETTLE_DATABASE_TYPES = "kettle-database-types.xml";


    private static String[] emptyPaddedSpacesStrings;

    /**
     * The release type of this compilation
     */
    public static final ReleaseType RELEASE = ReleaseType.GA;
    
    /** 
     *  rounds double f to any number of places after decimal point
	 *  Does arithmetic using BigDecimal class to avoid integer overflow while rounding
	 *  TODO: make the rounding itself optional in the Props for performance reasons.
	 *  
	 * @param f The value to round
	 * @param places The number of decimal places
	 * @return The rounded floating point value
	 */

	public static final double round(double f, int places)
	{
		java.math.BigDecimal bdtemp = java.math.BigDecimal.valueOf(f);
		bdtemp = bdtemp.setScale(places, java.math.BigDecimal.ROUND_HALF_EVEN);
		return bdtemp.doubleValue();
	}

	/* OLD code: caused a lot of problems with very small and very large numbers.
	 * It's a miracle it worked at all.
	 * Go ahead, have a laugh...
	 public static final float round(double f, int places)
	 {
	 float temp = (float) (f * (Math.pow(10, places)));

	 temp = (Math.round(temp));

	 temp = temp / (int) (Math.pow(10, places));

	 return temp;

	 }
	 */

	/**
	 * Convert a String into an integer.  If the conversion fails, assign a default value.
	 * @param str The String to convert to an integer 
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final int toInt(String str, int def)
	{
		int retval;
		try
		{
			retval = Integer.parseInt(str);
		} catch (Exception e)
		{
			retval = def;
		}		
		return retval;
	}

	/**
	 * Convert a String into a long integer.  If the conversion fails, assign a default value.
	 * @param str The String to convert to a long integer
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final long toLong(String str, long def)
	{
		long retval;
		try
		{
			retval = Long.parseLong(str);
		} catch (Exception e)
		{
			retval = def;
		}
		return retval;
	}

	/**
	 * Convert a String into a double.  If the conversion fails, assign a default value.
	 * @param str The String to convert to a double
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final double toDouble(String str, double def)
	{
		double retval;
		try
		{
			retval = Double.parseDouble(str);
		} catch (Exception e)
		{
			retval = def;
		}
		return retval;
	}

	/**
	 * Convert a String into a date.  
	 * The date format is <code>yyyy/MM/dd HH:mm:ss.SSS</code>.  
	 * If the conversion fails, assign a default value.
	 * @param str The String to convert into a Date
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final Date toDate(String str, Date def)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US);
		try
		{
			return df.parse(str);
		} catch (ParseException e)
		{
			return def;
		}
	}

	/**
	 * Determines whether or not a character is considered a space.
	 * A character is considered a space in Kettle if it is a space, a tab, a newline or a cariage return.
	 * @param c The character to verify if it is a space.
	 * @return true if the character is a space. false otherwise. 
	 */
	public static final boolean isSpace(char c)
	{
		return c == ' ' || c == '\t' || c == '\r' || c == '\n' || Character.isWhitespace(c);
	}
	
	/**
	 * Left trim: remove spaces to the left of a String.
	 * @param str The String to left trim
	 * @return The left trimmed String
	 */
    public static String ltrim(String source) {
        if (source==null) return null;
		int from = 0;
		while (from < source.length() && isSpace(source.charAt(from)))
			from++;

		return source.substring(from);    	
    }

    /**
	 * Right trim: remove spaces to the right of a string
	 * @param str The string to right trim
	 * @return The trimmed string.
	 */
    public static String rtrim(String source) {
		if (source==null) return null;
		
		int max = source.length();
		while (max > 0 && isSpace(source.charAt(max - 1)))
			max--;

		return source.substring(0, max);
    }

	/**
	 * Trims a string: removes the leading and trailing spaces of a String.
	 * @param str The string to trim
	 * @return The trimmed string.
	 */
	public static final String trim(String str)
	{
		if (str==null) return null;
		
		int max = str.length() - 1;
		int min = 0;	

		while (min <= max && isSpace(str.charAt(min)))
			min++;
		while (max >= 0 && isSpace(str.charAt(max)))
			max--;

		if (max < min)
			return "";

		return str.substring(min, max + 1);
	}

	/**
	 * Right pad a string: adds spaces to a string until a certain length.
	 * If the length is smaller then the limit specified, the String is truncated.
	 * @param ret The string to pad
	 * @param limit The desired length of the padded string.
	 * @return The padded String.
	 */
	public static final String rightPad(String ret, int limit)
	{
		if (ret == null)
			return rightPad(new StringBuffer(), limit);
		else
			return rightPad(new StringBuffer(ret), limit);
	}

	/**
	 * Right pad a StringBuffer: adds spaces to a string until a certain length.
	 * If the length is smaller then the limit specified, the String is truncated.
	 * @param ret The StringBuffer to pad
	 * @param limit The desired length of the padded string.
	 * @return The padded String.
	 */
	public static final String rightPad(StringBuffer ret, int limit)
	{
		int len = ret.length();
		int l;

		if (len > limit)
		{
			ret.setLength(limit);
		} else
		{
			for (l = len; l < limit; l++)
				ret.append(' ');
		}
		return ret.toString();
	}

	/**
	 * Replace values in a String with another.
	 * @param string The original String.
	 * @param repl The text to replace
	 * @param with The new text bit
	 * @return The resulting string with the text pieces replaced.
	 */
	public static final String replace(String string, String repl, String with)
	{
		StringBuffer str = new StringBuffer(string);
		for (int i = str.length() - 1; i >= 0; i--)
		{
			if (str.substring(i).startsWith(repl))
			{
				str.delete(i, i + repl.length());
				str.insert(i, with);
			}
		}
		return str.toString();
	}

	/**
	 * Alternate faster version of string replace using a stringbuffer as input.
	 * 
	 * @param str The string where we want to replace in
	 * @param code The code to search for
	 * @param repl The replacement string for code
	 */
	public static void repl(StringBuffer str, String code, String repl)
	{
		int clength = code.length();

		int i = str.length() - clength;

		while (i >= 0)
		{
			String look = str.substring(i, i + clength);
			if (look.equalsIgnoreCase(code)) // Look for a match!
			{
				str.replace(i, i + clength, repl);
			}
			i--;
		}
	}

	/**
	 * Count the number of spaces to the left of a text. (leading)
	 * @param field The text to examine
	 * @return The number of leading spaces found.
	 */
	public static final int nrSpacesBefore(String field)
	{
		int nr = 0;
		int len = field.length();
		while (nr < len && field.charAt(nr) == ' ')
		{
			nr++;
		}
		return nr;
	}

	/**
	 * Count the number of spaces to the right of a text. (trailing)
	 * @param field The text to examine
	 * @return The number of trailing spaces found.
	 */
	public static final int nrSpacesAfter(String field)
	{
		int nr = 0;
		int len = field.length();
		while (nr < len && field.charAt(field.length() - 1 - nr) == ' ')
		{
			nr++;
		}
		return nr;
	}

	/**
	 * Checks whether or not a String consists only of spaces.
	 * @param str The string to check
	 * @return true if the string has nothing but spaces.
	 */
	public static final boolean onlySpaces(String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (!isSpace(str.charAt(i)))
				return false;
		return true;
	}

	/**
	 * determine the OS name
	 * @return The name of the OS
	 */
	public static final String getOS()
	{
		return System.getProperty("os.name");
	}
	/**
	 * Determine the quoting character depending on the OS. 
	 * Often used for shell calls, gives back " for Windows systems otherwise '
	 * @return quoting character
	 */	
	public static String getQuoteCharByOS() {
		if (isWindows()) {
			return "\"";
		} else {
			return "'";
		}
	}

	/**
	 * Quote a string depending on the OS. 
	 * Often used for shell calls.
	 * @return quoted string
	 */	
	public static String optionallyQuoteStringByOS(String string) {
		String quote=getQuoteCharByOS();
		if (isEmpty(string))
			return quote;

		// If the field already contains quotes, we don't touch it anymore, just
		// return the same string...
		// also return it if no spaces are found
		if (string.indexOf(quote) >= 0 || (string.indexOf(' ') < 0 && string.indexOf('=') < 0))
		{
			return string;
		} else
		{
			return quote + string + quote;
		}
	}
	
	/** 
	 * @return True if the OS is a Windows derivate. 
	 */
	public static final boolean isWindows()
	{
		return getOS().startsWith("Windows");
	}
	
	/** 
	 * @return True if the OS is a Linux derivate. 
	 */
	public static final boolean isLinux()
	{
		return getOS().startsWith("Linux");
	}
	
	/** 
	 * @return True if the OS is an OSX derivate. 
	 */
	public static final boolean isOSX()
	{
		return getOS().toUpperCase().contains("OS X");
	}

    /**
     * Determine the hostname of the machine Kettle is running on
     * @return The hostname
     */
    public static final String getHostname()
    {
        String lastHostname = "localhost";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                NetworkInterface nwi = en.nextElement();
                Enumeration<InetAddress> ip = nwi.getInetAddresses();

                while (ip.hasMoreElements())
                {
                    InetAddress in = (InetAddress) ip.nextElement();
                    lastHostname=in.getHostName();
                    //System.out.println("  ip address bound : "+in.getHostAddress());
                    //System.out.println("  hostname         : "+in.getHostName());
                    //System.out.println("  Cann.hostname    : "+in.getCanonicalHostName());
                    //System.out.println("  ip string        : "+in.toString());
                    if (!lastHostname.equalsIgnoreCase("localhost") && !(lastHostname.indexOf(':')>=0) )
                    {
                        return lastHostname;
                    }
                }
            }
        } catch (SocketException e)
        {

        }

        return lastHostname;
    }

	/**
	 * Determins the IP address of the machine Kettle is running on.
	 * @return The IP address
	 */
	public static final String getIPAddress() throws Exception
	{
		Enumeration<NetworkInterface> enumInterfaces = NetworkInterface.getNetworkInterfaces();
		while (enumInterfaces.hasMoreElements())
		{
			NetworkInterface nwi = (NetworkInterface) enumInterfaces.nextElement();
			Enumeration<InetAddress> ip = nwi.getInetAddresses();
			while (ip.hasMoreElements())
			{
				InetAddress in = (InetAddress) ip.nextElement();
				if (!in.isLoopbackAddress() && in.toString().indexOf(":") < 0)
				{
					return in.getHostAddress();
				}
			}
		}
		return "127.0.0.1";
	}

	/**
	 * Get the primary IP address tied to a network interface (excluding loop-back etc)
	 * @param networkInterfaceName the name of the network interface to interrogate
	 * @return null if the network interface or address wasn't found.
	 * 
	 * @throws SocketException in case of a security or network error
	 */
	public static final String getIPAddress(String networkInterfaceName) throws SocketException { 
		NetworkInterface networkInterface = NetworkInterface.getByName(networkInterfaceName);
		Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
		while (ipAddresses.hasMoreElements())
		{
			InetAddress inetAddress = (InetAddress) ipAddresses.nextElement();
			if (!inetAddress.isLoopbackAddress() && inetAddress.toString().indexOf(":") < 0)
			{
				String hostname = inetAddress.getHostAddress();
				return hostname;
			}
		}
		return null;
	}


	/**
	 * Tries to determine the MAC address of the machine Kettle is running on.
	 * @return The MAC address.
	 */
	public static final String getMACAddress() throws Exception
	{
		String ip = getIPAddress();
		String mac = "none";
		String os = getOS();
		String s = "";

		//System.out.println("os = "+os+", ip="+ip);

		if (os.equalsIgnoreCase("Windows NT") || os.equalsIgnoreCase("Windows 2000") || os.equalsIgnoreCase("Windows XP")
				|| os.equalsIgnoreCase("Windows 95") || os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows Me")
				|| os.startsWith("Windows"))
		{
			try
			{
				// System.out.println("EXEC> nbtstat -a "+ip);

				Process p = Runtime.getRuntime().exec("nbtstat -a " + ip);

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while (!procDone(p))
				{
					while ((s = stdInput.readLine()) != null)
					{
						// System.out.println("NBTSTAT> "+s);
						if (s.indexOf("MAC") >= 0)
						{
							int idx = s.indexOf('=');
							mac = s.substring(idx + 2);
						}
					}
				}
				stdInput.close();
			} catch (Exception e)
			{

			}
		} else if (os.equalsIgnoreCase("Linux"))
		{
			try
			{
				Process p = Runtime.getRuntime().exec("/sbin/ifconfig -a");

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while (!procDone(p))
				{
					while ((s = stdInput.readLine()) != null)
					{
						int idx = s.indexOf("HWaddr");
						if (idx >= 0)
						{
							mac = s.substring(idx + 7);
						}
					}
				}
				stdInput.close();
			} catch (Exception e)
			{

			}
		} else if (os.equalsIgnoreCase("Solaris"))
		{
			try
			{
				Process p = Runtime.getRuntime().exec("/usr/sbin/ifconfig -a");

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while (!procDone(p))
				{
					while ((s = stdInput.readLine()) != null)
					{
						int idx = s.indexOf("ether");
						if (idx >= 0)
						{
							mac = s.substring(idx + 6);
						}
					}
				}
				stdInput.close();
			} catch (Exception e)
			{

			}
		} else if (os.equalsIgnoreCase("HP-UX"))
		{
			try
			{
				Process p = Runtime.getRuntime().exec("/usr/sbin/lanscan -a");

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while (!procDone(p))
				{
					while ((s = stdInput.readLine()) != null)
					{
						if (s.indexOf("MAC") >= 0)
						{
							int idx = s.indexOf("0x");
							mac = s.substring(idx + 2);
						}
					}
				}
				stdInput.close();
			} catch (Exception e)
			{

			}
		}

		return Const.trim(mac);
	}

	private static final boolean procDone(Process p)
	{
		try
		{
			p.exitValue();
			return true;
		} catch (IllegalThreadStateException e)
		{
			return false;
		}
	}


	/**
	 * Determines the Kettle directory in the user's home directory.
	 * @return The Kettle directory.
	 */
	public static final String getKettleDirectory()
	{
		return USER_HOME_DIRECTORY + FILE_SEPARATOR + BasePropertyHandler.getProperty("userBaseDir", ".kettle");
	}
	
    
    /**
     * Determines the location of the shared objects file
     * @return the name of the shared objects file
     */
    public static final String getSharedObjectsFile()
    {
        return getKettleDirectory() + FILE_SEPARATOR + "shared.xml";
    }
    

	/**
	 * Returns the path to the Kettle local (current directory) repositories XML file.
	 * @return The local repositories file.
	 */
	public static final String getKettleLocalRepositoriesFile()
	{
		return "repositories.xml";
	}

	/**
	 * Returns the full path to the Kettle repositories XML file.
	 * @return The Kettle repositories file.
	 */
	public static final String getKettleUserRepositoriesFile()
	{
		return getKettleDirectory() + FILE_SEPARATOR + getKettleLocalRepositoriesFile();
	}

	/**
	 * Returns the path to the Kettle local (current directory) Carte password file:<p>
	 * ./pwd/kettle.pwd<br>
	 * 
	 * @return The local Carte password file.
	 */
	public static final String getKettleLocalCartePasswordFile()
	{
		return "pwd/kettle.pwd";
	}

	/**
	 * Returns the path to the Kettle Carte password file in the home directory:<p>
	 * $KETTLE_HOME/.kettle/kettle.pwd<br>
	 * 
	 * @return The Carte password file in the home directory.
	 */
	public static final String getKettleCartePasswordFile()
	{
		return getKettleDirectory() + FILE_SEPARATOR + "kettle.pwd";
	}

	/**
	 * Retrieves the content of an environment variable
	 * 
	 * @param variable The name of the environment variable
	 * @param deflt The default value in case no value was found
	 * @return The value of the environment variable or the value of deflt in case no variable was defined.
	 */
	public static String getEnvironmentVariable(String variable, String deflt)
	{
		return System.getProperty(variable, deflt);
	}

	/**
	 * Replaces environment variables in a string.
	 * For example if you set KETTLE_HOME as an environment variable, you can 
	 * use %%KETTLE_HOME%% in dialogs etc. to refer to this value.
	 * This procedures looks for %%...%% pairs and replaces them including the 
	 * name of the environment variable with the actual value.
	 * In case the variable was not set, nothing is replaced!
	 * 
	 * @param string The source string where text is going to be replaced.
	 *  
	 * @return The expanded string.
	 * @deprecated use StringUtil.environmentSubstitute(): handles both Windows and unix conventions
	 */
	public static final String replEnv(String string)
	{
		if (string == null)
			return null;
		StringBuffer str = new StringBuffer(string);

		int idx = str.indexOf("%%");
		while (idx >= 0)
		{
			//OK, so we found a marker, look for the next one...
			int to = str.indexOf("%%", idx + 2);
			if (to >= 0)
			{
				// OK, we found the other marker also...
				String marker = str.substring(idx, to + 2);
				String var = str.substring(idx + 2, to);

				if (var != null && var.length() > 0)
				{
					// Get the environment variable
					String newval = getEnvironmentVariable(var, null);

					if (newval != null)
					{
						// Replace the whole bunch
						str.replace(idx, to + 2, newval);
						//System.out.println("Replaced ["+marker+"] with ["+newval+"]");

						// The last position has changed...
						to += newval.length() - marker.length();
					}
				}

			} else
			// We found the start, but NOT the ending %% without closing %%
			{
				to = idx;
			}

			// Look for the next variable to replace...
			idx = str.indexOf("%%", to + 1);
		}

		return str.toString();
	}

	/**
	 * Replaces environment variables in an array of strings.<p>
	 * See also: replEnv(String string)
	 * @param string The array of strings that wants its variables to be replaced.
	 * @return the array with the environment variables replaced.
	 * @deprecated please use StringUtil.environmentSubstitute now.
	 */
	public static final String[] replEnv(String string[])
	{
		String retval[] = new String[string.length];
		for (int i = 0; i < string.length; i++)
		{
			retval[i] = Const.replEnv(string[i]);
		}
		return retval;
	}

	/**
	 * Implements Oracle style NVL function
	 * @param source The source argument
	 * @param def The default value in case source is null or the length of the string is 0
	 * @return source if source is not null, otherwise return def
	 */
	public static final String NVL(String source, String def)
	{
		if (source == null || source.length() == 0)
			return def;
		return source;
	}

	/**
	 * Search for a string in an array of strings and return the index.
	 * @param lookup The string to search for
	 * @param array The array of strings to look in
	 * @return The index of a search string in an array of strings. -1 if not found.
	 */
	public static final int indexOfString(String lookup, String array[])
	{
		if (array == null)
			return -1;
		if (lookup == null)
			return -1;

		for (int i = 0; i < array.length; i++)
		{
			if (lookup.equalsIgnoreCase(array[i]))
				return i;
		}
		return -1;
	}
    
    /**
     * Search for strings in an array of strings and return the indexes.
     * @param lookup The strings to search for
     * @param array The array of strings to look in
     * @return The indexes of strings in an array of strings. -1 if not found.
     */
    public static final int[] indexsOfStrings(String lookup[], String array[])
    {
        int[] indexes = new int[lookup.length];
        for (int i=0;i<indexes.length;i++) indexes[i] = indexOfString(lookup[i], array);
        return indexes;
    }
    
    /**
     * Search for strings in an array of strings and return the indexes.
     * If a string is not found, the index is not returned.
     * 
     * @param lookup The strings to search for
     * @param array The array of strings to look in
     * @return The indexes of strings in an array of strings.  Only existing indexes are returned (no -1)
     */
    public static final int[] indexsOfFoundStrings(String lookup[], String array[])
    {
        List<Integer> indexesList = new ArrayList<Integer>();
        for (int i=0;i<lookup.length;i++)
        {
            int idx = indexOfString(lookup[i], array);
            if (idx>=0) indexesList.add(Integer.valueOf(idx));
        }
        int[] indexes = new int[indexesList.size()];
        for (int i=0;i<indexesList.size();i++) indexes[i] = (indexesList.get(i)).intValue();
        return indexes;
    }

	/**
	 * Search for a string in a list of strings and return the index.
	 * @param lookup The string to search for
	 * @param list The ArrayList of strings to look in
	 * @return The index of a search string in an array of strings. -1 if not found.
	 */
	public static final int indexOfString(String lookup, List<String> list)
	{
		if (list == null)
			return -1;

		for (int i = 0; i < list.size(); i++)
		{
			String compare = list.get(i);
			if (lookup.equalsIgnoreCase(compare))
				return i;
		}
		return -1;
	}

	/**
	 * Sort the strings of an array in alphabetical order.
	 * @param input The array of strings to sort.
	 * @return The sorted array of strings.
	 */
	public static final String[] sortStrings(String input[])
	{
		Arrays.sort(input);
		return input;
	}
	
	/**
	 * Convert strings separated by a string into an array of strings.<p>
	 * <code>
	 Example: a;b;c;d    ==>  new String[] { a, b, c, d }
	 * </code>
	 * 
	 * <p><b>NOTE: this differs from String.split() in a way that the built-in method uses regular expressions and this one does not.</b>
	 *  
	 * @param string The string to split
	 * @param separator The separator used.
	 * @return the string split into an array of strings
	 */
	public static final String[] splitString(String string, String separator)
	{
		/*
		 *           0123456
		 *   Example a;b;c;d    -->    new String[] { a, b, c, d }
		 */
		// System.out.println("splitString ["+path+"] using ["+separator+"]");
		List<String> list = new ArrayList<String>();

		if (string == null || string.length() == 0)
		{
			return new String[] {};
		}

		int sepLen = separator.length();
		int from = 0;
	    int end = string.length() - sepLen + 1;

		for (int i = from; i < end; i += sepLen)
		{
			if (string.substring(i, i + sepLen).equalsIgnoreCase(separator))
			{
				// OK, we found a separator, the string to add to the list
				// is [from, i[
				list.add(NVL(string.substring(from, i), ""));
				from = i + sepLen;
			}
		}

		// Wait, if the string didn't end with a separator, we still have information at the end of the string...
		// In our example that would be "d"...
		if (from + sepLen <= string.length())
		{
			list.add(NVL(string.substring(from, string.length()), ""));
		}

		return list.toArray(new String[list.size()]);
	}

	/**
	 * Convert strings separated by a character into an array of strings.<p>
	 * <code>
	 Example: a;b;c;d    ==  new String[] { a, b, c, d }
	 * </code>
	 *  
	 * @param string The string to split
	 * @param separator The separator used.
	 * @return the string split into an array of strings
	 */
	public static final String[] splitString(String string, char separator)
	{
		/*
		 *           0123456
		 *   Example a;b;c;d    -->    new String[] { a, b, c, d }
		 */
		// System.out.println("splitString ["+path+"] using ["+separator+"]");
		List<String> list = new ArrayList<String>();

		if (string == null || string.length() == 0)
		{
			return new String[] {};
		}

		int from = 0;
	    int end = string.length();

		for (int i = from; i < end; i += 1)
		{
			if (string.charAt(i) == separator)
			{
				// OK, we found a separator, the string to add to the list
				// is [from, i[
				list.add(NVL(string.substring(from, i), ""));
				from = i + 1;
			}
		}

		// Wait, if the string didn't end with a separator, we still have information at the end of the string...
		// In our example that would be "d"...
		if (from + 1 <= string.length())
		{
			list.add(NVL(string.substring(from, string.length()), ""));
		}

		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Convert strings separated by a string into an array of strings.<p>
	 * <code>
	 *   Example /a/b/c --> new String[] { a, b, c }
	 * </code>
	 *  
	 * @param path The string to split
	 * @param separator The separator used.
	 * @return the string split into an array of strings
	 */
	public static final String[] splitPath(String path, String separator)
	{
		//
		//  Example /a/b/c    -->    new String[] { a, b, c }
		//
		// Make sure training slashes are removed
		//
		//  Example /a/b/c/    -->    new String[] { a, b, c }
		//
		
		// Check for empty paths...
		//
		if (path == null || path.length() == 0 || path.equals(separator))
		{
			return new String[] {};
		}

		// lose trailing separators
		//
		while (path.endsWith(separator)) {
			path = path.substring(0, path.length()-1);
		}
		
		int sepLen = separator.length();
		int nr_separators = 1;
		int from = path.startsWith(separator) ? sepLen : 0;

		for (int i = from; i < path.length(); i += sepLen)
		{
			if (path.substring(i, i + sepLen).equalsIgnoreCase(separator))
			{
				nr_separators++;
			}
		}

		String spath[] = new String[nr_separators];
		int nr = 0;
		for (int i = from; i < path.length(); i += sepLen)
		{
			if (path.substring(i, i + sepLen).equalsIgnoreCase(separator))
			{
				spath[nr] = path.substring(from, i);
				nr++;

				from = i + sepLen;
			}
		}
		if (nr < spath.length)
		{
			spath[nr] = path.substring(from);
		}

		// 
		// a --> { a }
		//
		if (spath.length == 0 && path.length() > 0)
		{
			spath = new String[] { path };
		}

		return spath;
	}


	/**
	 * Sorts the array of Strings, determines the uniquely occurring strings.  
	 * @param strings the array that you want to do a distinct on
	 * @return a sorted array of uniquely occurring strings
	 */
	public static final String[] getDistinctStrings(String[] strings)
	{
		if (strings == null)
			return null;
		if (strings.length == 0)
			return new String[] {};

		String[] sorted = sortStrings(strings);
		List<String> result = new ArrayList<String>();
		String previous = "";
		for (int i = 0; i < sorted.length; i++)
		{
			if (!sorted[i].equalsIgnoreCase(previous))
			{
				result.add(sorted[i]);
			}
			previous = sorted[i];
		}

		return result.toArray(new String[result.size()]);
	}
    
    /**
     * Returns a string of the stack trace of the specified exception
     */
    public static final String getStackTracker(Throwable e)
    {
    	return getCustomStackTrace(e);
    }

    public static final String getClassicStackTrace(Throwable e)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String string = stringWriter.toString();
        try { stringWriter.close(); } catch(IOException ioe) {} // is this really required?
        return string;
    }
    
    public static String getCustomStackTrace(Throwable aThrowable) {
        final StringBuilder result = new StringBuilder();
        String errorMessage = aThrowable.toString();
        result.append(errorMessage);
        if (!errorMessage.contains(Const.CR)) {
        	result.append(CR);
        }

        // add each element of the stack trace
        //
        for (StackTraceElement element : aThrowable.getStackTrace() ){
          result.append( element );
          result.append( CR );
        }
        return result.toString();
      }
    
    /**
     * Check if the string supplied is empty.  A String is empty when it is null or when the length is 0
     * @param string The string to check
     * @return true if the string supplied is empty
     */
    public static final boolean isEmpty(String string)
    {
    	return string==null || string.length()==0;
    }
    
    /**
     * Check if the stringBuffer supplied is empty.  A StringBuffer is empty when it is null or when the length is 0
     * @param string The stringBuffer to check
     * @return true if the stringBuffer supplied is empty
     */
    public static final boolean isEmpty(StringBuffer string)
    {
    	return string==null || string.length()==0;
    }
    
    /**
     * Check if the string array supplied is empty.  A String array is empty when it is null or when the number of elements is 0
     * @param string The string array to check
     * @return true if the string array supplied is empty
     */
    public static final boolean isEmpty(String[] strings)
    {
        return strings==null || strings.length==0;
    }

    /**
     * Check if the array supplied is empty.  An array is empty when it is null or when the length is 0
     * @param array The array to check
     * @return true if the array supplied is empty
     */
    public static final boolean isEmpty(Object[] array)
    {
     return array==null || array.length==0;
    }
    
    /**
     * Check if the list supplied is empty.  An array is empty when it is null or when the length is 0
     * @param list the list to check
     * @return true if the supplied list is empty
     */
    public static final boolean isEmpty(List<?> list)
    {
     return list==null || list.size()==0;
    }
    
    /**
     * @return a new ClassLoader
     */
    public static final ClassLoader createNewClassLoader() throws KettleException
    {
        try
        {
            // Nothing really in URL, everything is in scope.
            URL urls[] = new URL[] { };
            URLClassLoader ucl = new URLClassLoader(urls);

            return ucl;
        }
        catch (Exception e)
        {
            throw new KettleException("Unexpected error during classloader creation", e);
        }
    }
    
    /**
     * Utility class for use in JavaScript to create a new byte array.
     * This is surprisingly difficult to do in JavaScript.
     * 
     * @return a new java byte array
     */
    public static final byte[] createByteArray(int size)
    {
        return new byte[size];
    }


    /**
     * Sets the first character of each word in upper-case.
     * @param string The strings to convert to initcap
     * @return the input string but with the first character of each word converted to upper-case.
     */
    public static final String initCap(String string)
    {
        StringBuffer change=new StringBuffer(string);
        boolean new_word;
        int i;
        char lower, upper, ch;
            
        new_word=true;
        for (i=0 ; i<string.length() ; i++)
        {
            lower=change.substring(i,i+1).toLowerCase().charAt(0); // Lowercase is default.
            upper=change.substring(i,i+1).toUpperCase().charAt(0); // Uppercase for new words.
            ch=upper;
    
            if (new_word)
            { 
              change.setCharAt(i, upper);
            }
            else
            {          
              change.setCharAt(i, lower);  
            }

            new_word = false;
    
            // Cast to (int) is required for extended characters (SB)
            if ( !Character.isLetterOrDigit((int)ch) && 
                 ch!='_'
               ) new_word = true;
        }
    
        return change.toString();
    }
 
    /**
     * Create a valid filename using a name
     * We remove all special characters, spaces, etc.
     * @param name The name to use as a base for the filename
     * @return a valid filename
     */
    public static final String createFilename(String name)
    {
        StringBuffer filename = new StringBuffer();
        for (int i=0;i<name.length();i++)
        {
            char c = name.charAt(i);
            if ( Character.isUnicodeIdentifierPart(c) )
            {
                filename.append(c);
            }
            else
            if (Character.isWhitespace(c))
            {
                filename.append('_');
            }
        }
        return filename.toString().toLowerCase();
    }
    
    public static final String createFilename(String directory, String name, String extension)
    {
        if (directory.endsWith(Const.FILE_SEPARATOR))
        {
            return directory+createFilename(name)+extension;
        }
        else
        {
            return directory+Const.FILE_SEPARATOR+createFilename(name)+extension;
        }
    }

    public static final String createName(String filename)
    {
        if (Const.isEmpty(filename))
            return filename;
        
        String pureFilename = filenameOnly(filename);
        if (pureFilename.endsWith(".ktr") || pureFilename.endsWith(".kjb") ||  pureFilename.endsWith(".xml"))
        {
            pureFilename = pureFilename.substring(0, pureFilename.length()-4);
        }
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < pureFilename.length(); i++)
        {
            char c = pureFilename.charAt(i);
            if ( Character.isUnicodeIdentifierPart(c) )
            {
                sb.append(c);
            }
            else if (Character.isWhitespace(c))
            {
                sb.append(' ');
            }
            else if (c=='-')
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * <p>
     * Returns the pure filename of a filename with full path. E.g. if passed parameter is
     * <code>/opt/tomcat/logs/catalina.out</code> this method returns <code>catalina.out</code>.
     * The method works with the Environment variable <i>System.getProperty("file.separator")</i>,
     * so on linux/Unix it will check for the last occurrence of a frontslash, on windows for
     * the last occurrence of a backslash.
     * </p>
     * 
     * <p>
     * To make this OS independent, the method could check for the last occurrence of a
     * frontslash and backslash and use the higher value of both. Should work, since these
     * characters aren't allowed in filenames on neither OS types (or said differently:
     * Neither linux nor windows can carry frontslashes OR backslashes in filenames).
     * Just a suggestion of an improvement ...
     * </p>
     * 
     * @param sFullPath
     * @return
     */
    public static String filenameOnly(String sFullPath)
    {
        if (Const.isEmpty(sFullPath)) return sFullPath;
        
        int idx = sFullPath.lastIndexOf(FILE_SEPARATOR);
        if (idx != -1)
        {
            return sFullPath.substring(idx + 1);
        }
        else
        {
            idx = sFullPath.lastIndexOf('/'); // URL, VFS
            if (idx != -1)
            {
                return sFullPath.substring(idx + 1);
            }
            else
            {
                return sFullPath;
            }
        }
            
    }
    
    /**
     * Returning the internationalized tips of the days. They get created once on first
     * request.
     * 
     * @return
     */
    public static String[] getTips()
    {
        if (tips == null)
        {
            int tipsOfDayCount = toInt(BaseMessages.getString(PKG, "Const.TipOfDay.Count"), 0);
            tips = new String[tipsOfDayCount];
            for (int i = 1; i <= tipsOfDayCount; i++)
                tips[i - 1] = BaseMessages.getString(PKG, "Const.TipOfDay" + Integer.toString(i));
        }

        return tips;
    }

    /**
     * Returning the localized date conversion formats. They get created once on first request.
     * 
     * @return
     */
    public static String[] getDateFormats()
    {
        if (dateFormats == null)
        {
            int dateFormatsCount = toInt(BaseMessages.getString(PKG, "Const.DateFormat.Count"), 0);
            dateFormats = new String[dateFormatsCount];
            for (int i = 1; i <= dateFormatsCount; i++)
                dateFormats[i - 1] = BaseMessages.getString(PKG, "Const.DateFormat" + Integer.toString(i));
        }
        return dateFormats;
    }

    /**
     * Returning the localized number conversion formats. They get created once on first request.
     * 
     * @return
     */
    public static String[] getNumberFormats()
    {
        if (numberFormats == null)
        {
            int numberFormatsCount = toInt(BaseMessages.getString(PKG, "Const.NumberFormat.Count"), 0);
            numberFormats = new String[numberFormatsCount + 1];
            numberFormats[0] = DEFAULT_NUMBER_FORMAT;
            for (int i = 1; i <= numberFormatsCount; i++)
                numberFormats[i] = BaseMessages.getString(PKG, "Const.NumberFormat" + Integer.toString(i));
        }
        return numberFormats;
    }

	/**
	 * @return An array of all default conversion formats, to be used in dialogs etc.
	 */
	public static String[] getConversionFormats()
	{
		String dats[] = Const.getDateFormats();
		String nums[] = Const.getNumberFormats();
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x = 0; x < dats.length; x++)
			formats[x] = dats[x];
		for (int x = 0; x < nums.length; x++)
			formats[dats.length + x] = nums[x];

		return formats;
	}

    public static String[] getTransformationAndJobFilterNames()
    {
        if (STRING_TRANS_AND_JOB_FILTER_NAMES == null)
        {
            STRING_TRANS_AND_JOB_FILTER_NAMES = new String[] {
                                                            BaseMessages.getString(PKG, "Const.FileFilter.TransformationJob"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.Transformations"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.Jobs"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.XML"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.All")
            };
        }
        return STRING_TRANS_AND_JOB_FILTER_NAMES;
    }
                                                                                   
    public static String[] getTransformationFilterNames()
    {
        if (STRING_TRANS_FILTER_NAMES == null)
        {
            STRING_TRANS_FILTER_NAMES = new String[] {
                                                            BaseMessages.getString(PKG, "Const.FileFilter.Transformations"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.XML"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.All")
            };
        }
        return STRING_TRANS_FILTER_NAMES;
    }

    public static String[] getJobFilterNames()
    {
        if (STRING_JOB_FILTER_NAMES == null)
        {
            STRING_JOB_FILTER_NAMES = new String[] {
                                                            BaseMessages.getString(PKG, "Const.FileFilter.Jobs"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.XML"),
                                                            BaseMessages.getString(PKG, "Const.FileFilter.All")
            };
        }
        return STRING_JOB_FILTER_NAMES;
    }
    
    /**
     * Return the current time as nano-seconds.
     * 
     * @return time as nano-seconds.
     */
    public static long nanoTime()
    {
        return new Date().getTime()*1000;
    }
    
    /**
     * Return the input string trimmed as specified.
     * 
     * @param string String to be trimmed
     * @param trimType Type of trimming
     * 
     * @return Trimmed string.
     */
    public static String trimToType(String string,int trimType)
    {
    	switch(trimType)
    	{
    	case ValueMetaInterface.TRIM_TYPE_BOTH:
    		return trim(string);
    	case ValueMetaInterface.TRIM_TYPE_LEFT:
    		return ltrim(string);
    	case ValueMetaInterface.TRIM_TYPE_RIGHT:
    		return rtrim(string);
    	case ValueMetaInterface.TRIM_TYPE_NONE:
    	default:
    		return string;
    	}
    }
    
    /**
	 * implemented to help prevent errors in matching up pluggable LAF directories and paths/files
	 * eliminating malformed URLs - duplicate file separators or missing file separators.
	 * 
	 * @param dir
	 * @param file
	 * @return concatenated string representing a file url
	 */
	public static String safeAppendDirectory(String dir, String file) {
		boolean dirHasSeparator = ((dir.lastIndexOf(FILE_SEPARATOR)) == dir.length());
		boolean fileHasSeparator = (file.indexOf(FILE_SEPARATOR) != 0);
		if ((dirHasSeparator && !fileHasSeparator) || (!dirHasSeparator && fileHasSeparator))
			return dir + file;
		if (dirHasSeparator && fileHasSeparator)
			return dir + file.substring(1);
		return dir + FILE_SEPARATOR + file;
	}

	/**
	 * Create an array of Strings consisting of spaces. The index of a String in
	 * the array determines the number of spaces in that string.
	 * 
	 * @return array of 'space' Strings.
	 */
	public static String[] getEmptyPaddedStrings() {
		if (emptyPaddedSpacesStrings==null) {
			 emptyPaddedSpacesStrings = new String[250];
			 for (int i=0;i<emptyPaddedSpacesStrings.length;i++) {
				 emptyPaddedSpacesStrings[i] = rightPad("", i);
			 }
		}
		return emptyPaddedSpacesStrings;
	}
	
	/**
	 * Return the percentage of free memory for this JVM.
	 * 
	 * @return Percentage of free memory.
	 */
	public static final int getPercentageFreeMemory()
	{
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long totalFreeMemory = (freeMemory + (maxMemory - allocatedMemory));
		
		int percentage = (int)Math.round(100*(double)totalFreeMemory / (double)maxMemory);
		
		return percentage;
	}
	/**
	 * Return non digits only.
	 * 
	 * @return non digits in a string.
	 */

	public static String removeDigits(String input)
	{
		if(Const.isEmpty(input)) return null;
	    StringBuffer digitsOnly = new StringBuffer ();
	    char c;
	    for (int i = 0; i < input.length (); i++) {
	      c = input.charAt (i);
	      if (!Character.isDigit (c)) {
	        digitsOnly.append (c);
	      }
	    }
	    return digitsOnly.toString ();
	}
	/**
	 * Return digits only.
	 * 
	 * @return digits in a string.
	 */
	public static String getDigitsOnly(String input)
	{
		if(Const.isEmpty(input)) return null;
	    StringBuffer digitsOnly = new StringBuffer ();
	    char c;
	    for (int i = 0; i < input.length (); i++) {
	      c = input.charAt (i);
	      if (Character.isDigit (c)) {
	        digitsOnly.append (c);
	      }
	    }
	    return digitsOnly.toString ();
	}
	/**
	 * Remove time from a date.
	 * 
	 * @return a date without hour.
	 */
	public static Date removeTimeFromDate(Date input)
	{
		if(input==null) return null;
	    // Get an instance of the Calendar.
	    Calendar calendar = Calendar.getInstance();

	    // Make sure the calendar will not perform automatic correction.
	    calendar.setLenient(false);

	    // Set the time of the calendar to the given date.
	    calendar.setTime(input);

	    // Remove the hours, minutes, seconds and milliseconds.
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);

	    // Return the date again.
	    return calendar.getTime();
	}
	 /**
	 * 	Escape XML content.
	 *  i.e. replace characters with &values;
	 * 	@param content content
	 * 	@return escaped content
	 */
	public static String escapeXML (String content)
	{
		if (isEmpty(content))	return content;
		return StringEscapeUtils.escapeXml(content);
	}
	 /**
	 * 	Escape HTML content.
	 *  i.e. replace characters with &values;
	 * 	@param content content
	 * 	@return escaped content
	 */
	public static String escapeHtml (String content)
	{
		if (isEmpty(content))	return content;
		return StringEscapeUtils.escapeHtml(content);
	}
	 /**
	 * 	UnEscape HTML content.
	 *  i.e. replace characters with &values;
	 * 	@param content content
	 * 	@return unescaped content
	 */
	public static String unEscapeHtml (String content)
	{
		if (isEmpty(content))	return content;
		return StringEscapeUtils.unescapeHtml(content);
	}
	 /**
	 * 	UnEscape XML content.
	 *  i.e. replace characters with &values;
	 * 	@param content content
	 * 	@return unescaped content
	 */
	public static String unEscapeXml(String content)
	{
		if (isEmpty(content))	return content;
		return StringEscapeUtils.unescapeXml(content);
	}
	 /**
	 * 	Escape SQL content.
	 *  i.e. replace characters with &values;
	 * 	@param content content
	 * 	@return escaped content
	 */
	public static String escapeSQL(String content)
	{
		if (isEmpty(content))	return content;
		return StringEscapeUtils.escapeSql(content);
	}
	/**
	 * 	Remove CR / LF from String
	 * 	@param in input
	 * 	@return cleaned string
	 */
	public static String removeCRLF (String in)
	{
		char[] inArray = in.toCharArray();
		StringBuffer out = new StringBuffer (inArray.length);
		for (int i = 0; i < inArray.length; i++)
		{
			char c = inArray[i];
			if (c == '\n' || c == '\r')
				;
			else
				out.append(c);
		}
		return out.toString();
	}

	/**
	 * 	Remove CR / LF from String
	 * 	@param in input
	 * 	@return cleaned string
	 */
	public static String removeCR (String in)
	{
		char[] inArray = in.toCharArray();
		StringBuffer out = new StringBuffer (inArray.length);
		for (int i = 0; i < inArray.length; i++)
		{
			char c = inArray[i];
			if (c == '\n')
				;
			else
				out.append(c);
		}
		return out.toString();
	}	//	removeCR
	/**
	 * 	Remove CR / LF from String
	 * 	@param in input
	 * 	@return cleaned string
	 */
	public static String removeLF (String in)
	{
		char[] inArray = in.toCharArray();
		StringBuffer out = new StringBuffer (inArray.length);
		for (int i = 0; i < inArray.length; i++)
		{
			char c = inArray[i];
			if (c == '\r')
				;
			else
				out.append(c);
		}
		return out.toString();
	}	//	removeCRLF
	/**
	 * 	Remove Horizontan Tab from String
	 * 	@param in input
	 * 	@return cleaned string
	 */
	public static String removeTAB (String in)
	{
		char[] inArray = in.toCharArray();
		StringBuffer out = new StringBuffer (inArray.length);
		for (int i = 0; i < inArray.length; i++)
		{
			char c = inArray[i];
			if (c == '\t')
				;
			else
				out.append(c);
		}
		return out.toString();
	}	
	 /**
     * Add time to an input date
     * @param input the date
     * @param time the time to add (in string)
     * @param DateFormat the time format
     * @return date = input + time
     */
	public static Date addTimeToDate(Date input, String time,String DateFormat) throws Exception 
	{
		if(isEmpty(time)) return input;
		if(input==null) return null;
		String dateformatString=NVL(DateFormat,"HH:mm:ss");
	    int t = decodeTime(time,dateformatString);
	    Date d = new Date(input.getTime()+t);
	    return d;
	}
	// Decodes a time value in specified date format and returns it as milliseconds since midnight.
	public static int decodeTime (String s,String DateFormat) throws Exception 
	{
	   SimpleDateFormat f = new SimpleDateFormat(DateFormat);
	   TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
	   f.setTimeZone (utcTimeZone);
	   f.setLenient (false);
	   ParsePosition p = new ParsePosition(0);
	   Date d = f.parse(s,p);
	   if (d == null)  throw new Exception("Invalid time value " + DateFormat +": \"" + s + "\".");
	   return (int)d.getTime(); 
	 }
	/**
	 * 	Get the number of occurances of searchFor in string.
	 * 	@param string String to be searched
	 * 	@param searchFor to be counted string
	 * 	@return number of occurances
	 */
	public static int getOccurenceString (String string, String searchFor)
	{
	  if (string == null || string.length() == 0)
		  return 0;
	  int counter=0;
	  int len = searchFor.length();
	  int result = 0;
	  if (len > 0) {  
	  int start = string.indexOf(searchFor);
	  while (start != -1) {
	            result++;
	            start = string.indexOf(searchFor, start+len);
	        }
	    }
		return counter;
	}
	public static String[] GetAvailableFontNames() {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    Font[] fonts = ge.getAllFonts();
	    String []FontName= new String[fonts.length];
	    for (int i=0; i<fonts.length; i++) {
	    	FontName[i]=fonts[i].getFontName();
	    }
	    return FontName;
	}

	public static String getKettlePropertiesFileHeader() {
		StringBuilder out = new StringBuilder();
		
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line01", VERSION)+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line02")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line03")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line04")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line05")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line06")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line07")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line08")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line09")+CR);
		out.append(BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line10")+CR);
		
		return out.toString();
	}
	 /**
	 * 	Mask XML content.
	 *  i.e. protect with CDATA;
	 * 	@param content content
	 * 	@return protected content
	 */
	public static String protectXMLCDATA (String content)
	{
		if (isEmpty(content))	return content;
		return  "<![CDATA["+content+"]]>";
	}
	/**
	 * 	Get the number of occurances of searchFor in string.
	 * 	@param string String to be searched
	 * 	@param searchFor to be counted string
	 * 	@return number of occurances
	 */
	public static int getOcuranceString (String string, String searchFor)
	{
	  if (string == null || string.length() == 0)
		  return 0;
	  Pattern p = Pattern.compile(searchFor);
	  Matcher m = p.matcher(string);
	  int count = 0;
	  while (m.find()) { ++count; }
	  return count;
	}
}
