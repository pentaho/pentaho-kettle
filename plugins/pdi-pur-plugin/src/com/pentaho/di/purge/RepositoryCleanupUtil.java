/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.pentaho.di.purge;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.RepositoryPathEncoder;

import com.pentaho.di.messages.Messages;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Command line utility for purging files, file revisions, or shared objects. Passes through to purge services.
 */
public class RepositoryCleanupUtil {

  public static Client client; // public so can be injected by unit test
  public static boolean testMode; // system.exit() is disabled for unit tests

  // Utility parameters.
  private final String URL = "url";
  private final String USER = "user";
  private final String PASS = "password";
  private final String VER_COUNT = "versionCount";
  private final String DEL_DATE = "purgeBeforeDate";
  private final String PURGE_FILES = "purgeFiles";
  private final String PURGE_REV = "purgeRevisions";

  private final String LOG_LEVEL = "logLevel";
  private final String LOG_FILE = "logFileName";
  private final String PURGE_SHARED = "purgeSharedObjects";

  // parameters in rest call that are not in command line
  private final String FILE_FILTER = "fileFilter";

  // Constants.
  private final String SERVICE_NAME = "purge";
  private final String BASE_PATH = "/plugin/pur-repository-plugin/api/purge";
  private final String AUTHENTICATION = "/api/authorization/action/isauthorized?authAction=";
  private final String purgeBeforeDateFormat = "MM/dd/yyyy";
  private final String logFileNameDateFormat = "YYYYMMdd-HHmmss";
  private final String DEFAULT_LOG_FILE_PREFIX = "purge-utility-log-";
  private final String OPTION_PREFIX = "-";
  private final String NEW_LINE = "\n";

  // Class properties.
  private String url = null;
  private String username = null;
  private String password = null;
  private int verCount = -1;
  private String delFrom = null;
  private String logLevel = null;
  private boolean purgeFiles = false;
  private boolean purgeRev = false;
  private boolean purgeShared = false;
  private String logFile = null;
  private String fileFilter;
  private String repositoryPath;

  /**
   * Main method
   * 
   * @param args
   */
  public static void main( String[] args ) {
    try {
      new RepositoryCleanupUtil().purge( args );
    } catch ( Exception e ) {
      writeOut( e );
    }
    exit( 0 );
  }

  /**
   * Create parameters and send HTTP request to purge REST endpoint
   * 
   * @param options
   */
  public void purge( String[] options ) {
    FormDataMultiPart form = null;
    try {
      Map<String, String> parameters = parseExecutionOptions( options );
      validateParameters( parameters );
      authenticateLoginCredentials();

      String serviceURL = createPurgeServiceURL();
      form = createParametersForm();

      WebResource resource = client.resource( serviceURL );
      ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, form );

      if ( response != null && response.getStatus() == 200 ) {
        String resultLog = response.getEntity( String.class );
        String logName = writeLog( resultLog );
        writeOut( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0001.OP_SUCCESS", logName ), false );
      } else {
        writeOut( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0001.OP_FAILURE" ), true );
      }

    } catch ( Exception e ) {
      if ( e.getMessage() != null ) {
        System.out.println( e.getMessage() );
      } else {
        if ( !( e instanceof NormalExitException ) ) {
          e.printStackTrace();
        }
      }
    } finally {
      if ( client != null ) {
        client.destroy();
      }

      if ( form != null ) {
        form.cleanup();
      }
    }
  }

  /**
   * Attempt to parse command line arguments and create map of values
   * 
   * @param args
   * @return
   * @throws Exception
   */
  private Map<String, String> parseExecutionOptions( String[] args ) throws Exception {
    Map<String, String> arguments = new HashMap<String, String>();
    String param;
    String value;
    try {
      for ( String arg : args ) {
        int equalsPos = arg.indexOf( "=" );
        if ( equalsPos == -1 ) {
          param = arg;
          value = "true";
        } else {
          param = arg.substring( 0, equalsPos );
          value = arg.substring( equalsPos + 1, arg.length() );
        }
        arguments.put( param, value );
      }
    } catch ( Exception e ) {
      writeOut( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0002.ERROR_PROC_PARAMS" ), true );
    }
    if ( arguments.size() == 0 ) {
      writeOut( printHelp(), true );
    }
    return arguments;
  }

  /**
   * Ensure provided parameters are present and what we expect
   * 
   * @param arguments
   * @throws Exception
   */
  private void validateParameters( Map<String, String> arguments ) throws Exception {
    String aUrl = arguments.get( OPTION_PREFIX + URL );
    String aUser = arguments.get( OPTION_PREFIX + USER );
    String aPassword = arguments.get( OPTION_PREFIX + PASS );
    String aVerCount = arguments.get( OPTION_PREFIX + VER_COUNT );
    String aDelFrom = arguments.get( OPTION_PREFIX + DEL_DATE );
    String aPurgeFiles = arguments.get( OPTION_PREFIX + PURGE_FILES );
    String aPurgeRev = arguments.get( OPTION_PREFIX + PURGE_REV );
    String aPurgeShared = arguments.get( OPTION_PREFIX + PURGE_SHARED );

    String aLogLevel = arguments.get( OPTION_PREFIX + LOG_LEVEL );
    String aLogFile = arguments.get( OPTION_PREFIX + LOG_FILE );

    StringBuffer errors = new StringBuffer();

    boolean isValidOperationSelected = false;

    fileFilter = "*.kjb|*.ktr";
    repositoryPath = "/";
    purgeShared = false;

    if ( aLogLevel != null
        && !( aLogLevel.equals( "DEBUG" ) || aLogLevel.equals( "ERROR" ) || aLogLevel.equals( "FATAL" )
            || aLogLevel.equals( "INFO" ) || aLogLevel.equals( "OFF" ) || aLogLevel.equals( "TRACE" ) || aLogLevel
              .equals( "WARN" ) ) ) {
      errors.append( OPTION_PREFIX + LOG_LEVEL + "=" + aLogLevel + " "
          + Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0003.INVALID_LOGLEVEL" ) + "\n" );
    } else {
      logLevel = aLogLevel;
    }

    if ( aLogFile != null ) {
      File f = new File( aLogFile );
      if ( f.exists() && f.isDirectory() ) {
        errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0004.FOLDER_EXISTS",
            OPTION_PREFIX + LOG_FILE )
            + "\n" );
      }
      logFile = aLogFile;
    }

    if ( aUrl == null ) {
      errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0005.MISSING_PARAM",
          OPTION_PREFIX + URL )
          + "\n" );
    } else {
      url = aUrl;
    }

    if ( aUser == null ) {
      errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0005.MISSING_PARAM",
          OPTION_PREFIX + USER )
          + "\n" );
    } else {
      username = aUser;
    }

    if ( aPassword == null ) {
      errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0005.MISSING_PARAM",
          OPTION_PREFIX + PASS )
          + "\n" );
    } else {
      password = aPassword;
    }

    if ( aPurgeFiles != null ) {
      if ( ( aPurgeFiles.equalsIgnoreCase( Boolean.TRUE.toString() ) || aPurgeFiles.equalsIgnoreCase( Boolean.FALSE
          .toString() ) ) ) {
        purgeFiles = Boolean.parseBoolean( aPurgeFiles );
        isValidOperationSelected = true;
      } else {
        errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0006.INVALID_BOOLEAN",
            OPTION_PREFIX + PURGE_FILES + "=" + aPurgeFiles )
            + "\n" );
      }
    }

    if ( aPurgeRev != null ) {
      if ( aPurgeRev.equalsIgnoreCase( Boolean.TRUE.toString() )
          || aPurgeRev.equalsIgnoreCase( Boolean.FALSE.toString() ) ) {
        if ( isValidOperationSelected ) {
          errors.append( Messages.getInstance().getString(
              "REPOSITORY_CLEANUP_UTIL.ERROR_0010.INVALID_COMBINATION_OF_PARAMS" )
              + "\n" );
        } else {
          purgeRev = Boolean.parseBoolean( aPurgeRev );
          isValidOperationSelected = true;
        }
      } else {
        errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0006.INVALID_BOOLEAN",
            OPTION_PREFIX + PURGE_REV + "=" + aPurgeRev )
            + "\n" );
      }
    }

    if ( aPurgeShared != null ) {
      if ( Boolean.parseBoolean( aPurgeFiles ) != Boolean.TRUE ) {
        errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0014.INVALID_PURGE_SHARED" ) );
      } else {
        purgeShared = Boolean.parseBoolean( aPurgeShared );
      }
    }

    if ( aDelFrom != null ) {
      // only allow one operation
      if ( isValidOperationSelected ) {
        errors.append( Messages.getInstance().getString(
            "REPOSITORY_CLEANUP_UTIL.ERROR_0010.INVALID_COMBINATION_OF_PARAMS" )
            + "\n" );
      } else {
        SimpleDateFormat sdf = new SimpleDateFormat( purgeBeforeDateFormat );
        sdf.setLenient( false );
        try {
          sdf.parse( aDelFrom );
          delFrom = aDelFrom;
          isValidOperationSelected = true;
        } catch ( ParseException e ) {
          errors.append( Messages.getInstance().getString(
              "REPOSITORY_CLEANUP_UTIL.ERROR_0008.IMPROPERLY_FORMATTED_DATE",
              OPTION_PREFIX + DEL_DATE + "=" + aDelFrom, purgeBeforeDateFormat )
              + "\n" );
        }
      }
    }

    if ( aVerCount != null ) {
      // only allow one operation
      if ( isValidOperationSelected ) {
        errors.append( Messages.getInstance().getString(
            "REPOSITORY_CLEANUP_UTIL.ERROR_0010.INVALID_COMBINATION_OF_PARAMS" )
            + "\n" );
      } else {
        try {
          verCount = Integer.parseInt( aVerCount );
          isValidOperationSelected = true;
        } catch ( NumberFormatException e ) {
          errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0009.INVALID_INTEGER",
              OPTION_PREFIX + VER_COUNT + "=" + aVerCount )
              + "\n" );
        }
      }
    }

    if ( !isValidOperationSelected ) {
      errors.append( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0013.MISSING_OPERATION" ) + "\n" );
    }

    if ( errors.length() != 0 ) {
      errors.insert( 0, "\n\n" + Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0011.ERRORS_HEADER" )
          + "\n" );
      throw new Exception( errors.toString() );
    }
  }

  /**
   * Use REST API to authenticate provided credentials
   * 
   * @throws Exception
   */
  @VisibleForTesting
  void authenticateLoginCredentials() throws Exception {
    KettleClientEnvironment.init();

    if ( client == null ) {
      ClientConfig clientConfig = new DefaultClientConfig();
      clientConfig.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
      client = Client.create( clientConfig );
      client.addFilter( new HTTPBasicAuthFilter( username, Encr.decryptPasswordOptionallyEncrypted( password ) ) );
    }

    WebResource resource = client.resource( url + AUTHENTICATION + AdministerSecurityAction.NAME );
    String response = resource.get( String.class );

    if ( !response.equals( "true" ) ) {
      throw new Exception( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.ERROR_0012.ACCESS_DENIED" ) );
    }
  }

  /**
   * Create URL to access REST API based on provided parameters
   * 
   * @return
   * @throws Exception
   */
  private String createPurgeServiceURL() throws Exception {
    StringBuffer service = new StringBuffer();
    service.append( url );
    service.append( BASE_PATH );
    service.append( "/" );

    String path = RepositoryPathEncoder.encodeRepositoryPath( repositoryPath );
    path = RepositoryPathEncoder.encode( path );
    service.append( path + "/" );

    service.append( SERVICE_NAME );
    return service.toString();
  }

  /**
   * Create payload to supply with POST to REST API
   * 
   * @return
   */
  private FormDataMultiPart createParametersForm() {
    FormDataMultiPart form = new FormDataMultiPart();
    if ( verCount != -1 && !purgeRev ) {
      form.field( VER_COUNT, Integer.toString( verCount ), MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( delFrom != null && !purgeRev ) {
      form.field( DEL_DATE, delFrom, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( fileFilter != null ) {
      form.field( FILE_FILTER, fileFilter, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( logLevel != null ) {
      form.field( LOG_LEVEL, logLevel, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( purgeFiles ) {
      form.field( PURGE_FILES, Boolean.toString( purgeFiles ), MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( purgeRev ) {
      form.field( PURGE_REV, Boolean.toString( purgeRev ), MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( purgeShared ) {
      form.field( PURGE_SHARED, Boolean.toString( purgeShared ), MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    return form;
  }

  /**
   * Generate help output
   * 
   * @return
   */
  private String printHelp() {

    // TODO improve this help description....
    StringBuffer help = new StringBuffer();

    help.append( "\n\n" + Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0003.OPTIONS_HEADER" ) );

    help.append( optionHelp( URL, Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0004.URL_REQUIRED",
        URL ) ) );

    help.append( optionHelp( USER, Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0005.USER_REQUIRED",
        USER ) ) );

    help.append( optionHelp( PASS, Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0006.PASS_REQUIRED",
        PASS ) ) );

    help.append( "\n" );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0008.PARAMS_HELP",
        PURGE_SHARED ), 0, 0 ) );

    help.append( optionHelp( VER_COUNT, Messages.getInstance().getString(
        "REPOSITORY_CLEANUP_UTIL.INFO_0009.VERSIONCOUNT_HELP", VER_COUNT ) ) );

    help.append( optionHelp( DEL_DATE, Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0010.DATE_HELP",
        DEL_DATE ) ) );

    help.append( optionHelp( PURGE_FILES, Messages.getInstance().getString(
        "REPOSITORY_CLEANUP_UTIL.INFO_0011.PURGE_FILES_HELP", PURGE_FILES ) ) );

    help.append( optionHelp( PURGE_REV, Messages.getInstance().getString(
        "REPOSITORY_CLEANUP_UTIL.INFO_0012.PURGE_REVS_HELP", PURGE_REV ) ) );

    help.append( "\n\n" + Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0013.OPTIONAL_PARAMS" ) );

    help.append( optionHelp( LOG_FILE, Messages.getInstance().getString(
        "REPOSITORY_CLEANUP_UTIL.INFO_0014.LOGFILE_HELP", DEFAULT_LOG_FILE_PREFIX + logFileNameDateFormat,
        logFileNameDateFormat ) ) );

    help.append( optionHelp( LOG_LEVEL, Messages.getInstance().getString(
        "REPOSITORY_CLEANUP_UTIL.INFO_0015.LOGLEVEL_HELP", LOG_LEVEL ) ) );

    help.append( optionHelp( PURGE_SHARED, Messages.getInstance().getString(
        "REPOSITORY_CLEANUP_UTIL.INFO_0007.PURGE_SHARED", PURGE_FILES ) ) );

    help.append( "\n\n" + Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0016.EXAMPLES" ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0017.EXAMPLE_1",
        OPTION_PREFIX + URL, OPTION_PREFIX + USER, OPTION_PREFIX + PASS, OPTION_PREFIX + PURGE_FILES ), 0, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0018.EXAMPLE_1_DESC" ),
        3, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0019.EXAMPLE_2",
        OPTION_PREFIX + URL, OPTION_PREFIX + USER, OPTION_PREFIX + PASS, OPTION_PREFIX + PURGE_REV ), 0, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0020.EXAMPLE_2_DESC" ),
        3, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0021.EXAMPLE_3",
        OPTION_PREFIX + URL, OPTION_PREFIX + USER, OPTION_PREFIX + PASS, OPTION_PREFIX + DEL_DATE ), 0, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0022.EXAMPLE_3_DESC" ),
        3, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0023.EXAMPLE_4",
        OPTION_PREFIX + URL, OPTION_PREFIX + USER, OPTION_PREFIX + PASS, OPTION_PREFIX + PURGE_FILES,
        OPTION_PREFIX + PURGE_SHARED ), 0, 3 ) );

    help.append( indentFormat( Messages.getInstance().getString( "REPOSITORY_CLEANUP_UTIL.INFO_0024.EXAMPLE_4_DESC" ),
        3, 3 ) );

    return help.toString();
  }

  /**
   * Format string for option help
   * 
   * @param optionName
   * @param descriptionText
   * @return
   */
  private String optionHelp( String optionName, String descriptionText ) {
    int indentFirstLine = 2;
    int indentBalance = Math.min( OPTION_PREFIX.length() + optionName.length() + 4, 10 );
    return indentFormat( OPTION_PREFIX + optionName + ": " + descriptionText, indentFirstLine, indentBalance );
  }

  /**
   * Format strings for command line output
   * 
   * @param unformattedText
   * @param indentFirstLine
   * @param indentBalance
   * @return
   */
  private String indentFormat( String unformattedText, int indentFirstLine, int indentBalance ) {
    final int maxWidth = 79;
    String leadLine = WordUtils.wrap( unformattedText, maxWidth - indentFirstLine );
    StringBuilder result = new StringBuilder();
    result.append( "\n" );
    if ( leadLine.indexOf( NEW_LINE ) == -1 ) {
      result.append( NEW_LINE ).append( StringUtils.repeat( " ", indentFirstLine ) ).append( unformattedText );
    } else {
      int lineBreakPoint = leadLine.indexOf( NEW_LINE );
      String indentString = StringUtils.repeat( " ", indentBalance );
      result.append( NEW_LINE ).append( StringUtils.repeat( " ", indentFirstLine ) ).append(
          leadLine.substring( 0, lineBreakPoint ) );
      String formattedText = WordUtils.wrap( unformattedText.substring( lineBreakPoint ), maxWidth - indentBalance );
      for ( String line : formattedText.split( NEW_LINE ) ) {
        result.append( NEW_LINE ).append( indentString ).append( line );
      }
    }
    return result.toString();
  }

  /**
   * Write out to log file
   * 
   * @param message
   * @return
   * @throws Exception
   */
  private String writeLog( String message ) throws Exception {
    String logName;
    if ( logFile != null ) {
      logName = logFile;
    } else {
      DateFormat df = new SimpleDateFormat( logFileNameDateFormat );
      logName = DEFAULT_LOG_FILE_PREFIX + df.format( new Date() ) + ".txt";
    }
    File file = new File( logName );
    FileOutputStream fout = FileUtils.openOutputStream( file );
    IOUtils.copy( IOUtils.toInputStream( message ), fout );
    fout.close();
    return logName;
  }

  /**
   * Print output or error message
   * 
   * @param message
   * @param isError
   */
  private static void writeOut( String message, boolean isError ) {
    if ( isError ) {
      System.err.println( message );
      exit( 1 );
    } else {
      System.out.println( message );
    }
  }

  private static void exit( int exitCode ) {
    if ( !testMode ) {
      System.exit( exitCode );
    } else {
      throw new NormalExitException( exitCode );
    }
  }

  /**
   * Print stack trace on error
   * 
   * @param t
   */
  private static void writeOut( Throwable t ) {
    t.printStackTrace();
    exit( 1 );
  }

  /**
   * When the command line would normally exit, this exception is thrown instead if we are running in test mode. This
   * prevents the junit tests from abnormally terminating but still captures the exit code.
   * 
   * @author tkafalas
   * 
   */
  public static class NormalExitException extends RuntimeException {
    public int exitCode;

    public NormalExitException( int exitCode ) {
      super();
      this.exitCode = exitCode;
    }
  }
}
