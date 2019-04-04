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

package org.pentaho.di.job.entries.shell;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.FileLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;

/**
 * Shell type of Job Entry. You can define shell scripts to be executed in a Job.
 *
 * @author Matt
 * @since 01-10-2003, rewritten on 18-06-2004
 */
public class JobEntryShell extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryShell.class; // for i18n purposes, needed by Translator2!!

  private String filename;

  private String workDirectory;

  public String[] arguments;

  public boolean argFromPrevious;

  public boolean setLogfile;

  public String logfile, logext;

  public boolean addDate, addTime;

  public LogLevel logFileLevel;

  public boolean execPerRow;

  public boolean setAppendLogfile;

  public boolean insertScript;

  public String script;

  public JobEntryShell( String name ) {
    super( name, "" );
  }

  public JobEntryShell() {
    this( "" );
    clear();
  }

  public void allocate( int nrFields ) {
    arguments = new String[nrFields];
  }

  public Object clone() {
    JobEntryShell je = (JobEntryShell) super.clone();
    if ( arguments != null ) {
      int nrFields = arguments.length;
      je.allocate( nrFields );
      System.arraycopy( arguments, 0, je.arguments, 0, nrFields );
    }
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "work_directory", workDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", argFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "exec_per_row", execPerRow ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "set_logfile", setLogfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logfile", logfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "set_append_logfile", setAppendLogfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logext", logext ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_date", addDate ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_time", addTime ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "insertScript", insertScript ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "script", script ) );

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "loglevel", ( logFileLevel == null ) ? null : logFileLevel.getCode() ) );

    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        // THIS IS A VERY BAD WAY OF READING/SAVING AS IT MAKES
        // THE XML "DUBIOUS". DON'T REUSE IT. (Sven B)
        retval.append( "      " ).append( XMLHandler.addTagValue( "argument" + i, arguments[i] ) );
      }
    }

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      setFileName( XMLHandler.getTagValue( entrynode, "filename" ) );
      setWorkDirectory( XMLHandler.getTagValue( entrynode, "work_directory" ) );
      argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      execPerRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "exec_per_row" ) );
      setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "set_logfile" ) );
      setAppendLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "set_append_logfile" ) );
      addDate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_date" ) );
      addTime = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_time" ) );
      logfile = XMLHandler.getTagValue( entrynode, "logfile" );
      logext = XMLHandler.getTagValue( entrynode, "logext" );
      logFileLevel = LogLevel.getLogLevelForCode( XMLHandler.getTagValue( entrynode, "loglevel" ) );
      insertScript = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "insertScript" ) );

      script = XMLHandler.getTagValue( entrynode, "script" );

      // How many arguments?
      int argnr = 0;
      while ( XMLHandler.getTagValue( entrynode, "argument" + argnr ) != null ) {
        argnr++;
      }
      allocate( argnr );

      // Read them all...
      // THIS IS A VERY BAD WAY OF READING/SAVING AS IT MAKES
      // THE XML "DUBIOUS". DON'T REUSE IT.
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = XMLHandler.getTagValue( entrynode, "argument" + a );
      }
    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'shell' from XML node", e );
    }
  }

  // Load the jobentry from repository
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      setFileName( rep.getJobEntryAttributeString( id_jobentry, "file_name" ) );
      setWorkDirectory( rep.getJobEntryAttributeString( id_jobentry, "work_directory" ) );
      argFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      execPerRow = rep.getJobEntryAttributeBoolean( id_jobentry, "exec_per_row" );

      setLogfile = rep.getJobEntryAttributeBoolean( id_jobentry, "set_logfile" );
      setAppendLogfile = rep.getJobEntryAttributeBoolean( id_jobentry, "set_append_logfile" );
      addDate = rep.getJobEntryAttributeBoolean( id_jobentry, "add_date" );
      addTime = rep.getJobEntryAttributeBoolean( id_jobentry, "add_time" );
      logfile = rep.getJobEntryAttributeString( id_jobentry, "logfile" );
      logext = rep.getJobEntryAttributeString( id_jobentry, "logext" );
      logFileLevel = LogLevel.getLogLevelForCode( rep.getJobEntryAttributeString( id_jobentry, "loglevel" ) );
      insertScript = rep.getJobEntryAttributeBoolean( id_jobentry, "insertScript" );

      script = rep.getJobEntryAttributeString( id_jobentry, "script" );
      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "argument" );
      allocate( argnr );

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = rep.getJobEntryAttributeString( id_jobentry, a, "argument" );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'shell' from the repository with id_jobentry="
        + id_jobentry, dbe );
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "file_name", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "work_directory", workDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", argFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "exec_per_row", execPerRow );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "set_logfile", setLogfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "set_append_logfile", setAppendLogfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_date", addDate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_time", addTime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logfile", logfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logext", logext );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "loglevel", logFileLevel == null ? LogLevel.NOTHING
        .getCode() : logFileLevel.getCode() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "insertScript", insertScript );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "script", script );

      // save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "argument", arguments[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'shell' to the repository", dbe );
    }
  }

  public void clear() {
    super.clear();

    filename = null;
    workDirectory = null;
    arguments = null;
    argFromPrevious = false;
    addDate = false;
    addTime = false;
    logfile = null;
    logext = null;
    setLogfile = false;
    execPerRow = false;
    setAppendLogfile = false;
    insertScript = false;
    script = null;
  }

  /**
   * @deprecated use {@link #setFilename(String)} instead
   * @param n
   */
  @Deprecated
  public void setFileName( String n ) {
    filename = n;
  }

  public void setFilename( String n ) {
    filename = n;
  }

  public String getFilename() {
    return filename;
  }

  public String getRealFilename() {
    return environmentSubstitute( getFilename() );
  }

  public void setWorkDirectory( String n ) {
    workDirectory = n;
  }

  public String getWorkDirectory() {
    return workDirectory;
  }

  public void setScript( String scriptin ) {
    script = scriptin;
  }

  public String getScript() {
    return script;
  }

  public String getLogFilename() {
    String retval = "";
    if ( setLogfile ) {
      retval += logfile == null ? "" : logfile;
      Calendar cal = Calendar.getInstance();
      if ( addDate ) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
        retval += "_" + sdf.format( cal.getTime() );
      }
      if ( addTime ) {
        SimpleDateFormat sdf = new SimpleDateFormat( "HHmmss" );
        retval += "_" + sdf.format( cal.getTime() );
      }
      if ( logext != null && logext.length() > 0 ) {
        retval += "." + logext;
      }
    }
    return retval;
  }

  public Result execute( Result result, int nr ) throws KettleException {
    FileLoggingEventListener loggingEventListener = null;
    LogLevel shellLogLevel = parentJob.getLogLevel();
    if ( setLogfile ) {
      String realLogFilename = environmentSubstitute( getLogFilename() );
      // We need to check here the log filename
      // if we do not have one, we must fail
      if ( Utils.isEmpty( realLogFilename ) ) {
        logError( BaseMessages.getString( PKG, "JobEntryShell.Exception.LogFilenameMissing" ) );
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }

      try {
        loggingEventListener = new FileLoggingEventListener( getLogChannelId(), realLogFilename, setAppendLogfile );
        KettleLogStore.getAppender().addLoggingEventListener( loggingEventListener );
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "JobEntryShell.Error.UnableopenAppenderFile", getLogFilename(), e
          .toString() ) );
        logError( Const.getStackTracker( e ) );
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }
      shellLogLevel = logFileLevel;
    }

    log.setLogLevel( shellLogLevel );

    result.setEntryNr( nr );

    // "Translate" the arguments for later
    String[] substArgs = null;
    if ( arguments != null ) {
      substArgs = new String[arguments.length];
      for ( int idx = 0; idx < arguments.length; idx++ ) {
        substArgs[idx] = environmentSubstitute( arguments[idx] );
      }
    }

    int iteration = 0;
    String[] args = substArgs;
    RowMetaAndData resultRow = null;
    boolean first = true;
    List<RowMetaAndData> rows = result.getRows();

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryShell.Log.FoundPreviousRows", ""
        + ( rows != null ? rows.size() : 0 ) ) );
    }

    while ( ( first && !execPerRow )
      || ( execPerRow && rows != null && iteration < rows.size() && result.getNrErrors() == 0 ) ) {
      first = false;
      if ( rows != null && execPerRow ) {
        resultRow = rows.get( iteration );
      } else {
        resultRow = null;
      }

      List<RowMetaAndData> cmdRows = null;

      if ( execPerRow ) {
        // Execute for each input row

        if ( argFromPrevious ) {
          // Copy the input row to the (command line) arguments

          if ( resultRow != null ) {
            args = new String[resultRow.size()];
            for ( int i = 0; i < resultRow.size(); i++ ) {
              args[i] = resultRow.getString( i, null );
            }
          }
        } else {
          // Just pass a single row
          List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
          newList.add( resultRow );
          cmdRows = newList;
        }
      } else {
        if ( argFromPrevious ) {
          // Only put the first Row on the arguments
          args = null;
          if ( resultRow != null ) {
            args = new String[resultRow.size()];
            for ( int i = 0; i < resultRow.size(); i++ ) {
              args[i] = resultRow.getString( i, null );
            }
          } else {
            cmdRows = rows;
          }
        } else {
          // Keep it as it was...
          cmdRows = rows;
        }
      }

      executeShell( result, cmdRows, args );

      iteration++;
    }

    if ( setLogfile ) {
      if ( loggingEventListener != null ) {
        KettleLogStore.getAppender().removeLoggingEventListener( loggingEventListener );
        loggingEventListener.close();

        ResultFile resultFile =
          new ResultFile(
            ResultFile.FILE_TYPE_LOG, loggingEventListener.getFile(), parentJob.getJobname(), getName() );
        result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
      }
    }

    return result;
  }

  private void executeShell( Result result, List<RowMetaAndData> cmdRows, String[] args ) {
    FileObject fileObject = null;
    String realScript = null;
    FileObject tempFile = null;

    try {
      // What's the exact command?
      String[] base = null;
      List<String> cmds = new ArrayList<String>();

      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "JobShell.RunningOn", Const.getOS() ) );
      }

      if ( insertScript ) {
        realScript = environmentSubstitute( script );
      } else {
        String realFilename = environmentSubstitute( getFilename() );
        fileObject = KettleVFS.getFileObject( realFilename, this );
      }

      if ( Const.getOS().equals( "Windows 95" ) ) {
        base = new String[] { "command.com", "/C" };
        if ( insertScript ) {
          tempFile =
            KettleVFS.createTempFile( "kettle", "shell.bat", System.getProperty( "java.io.tmpdir" ), this );
          fileObject = createTemporaryShellFile( tempFile, realScript );
        }
      } else if ( Const.getOS().startsWith( "Windows" ) ) {
        base = new String[] { "cmd.exe", "/C" };
        if ( insertScript ) {
          tempFile =
            KettleVFS.createTempFile( "kettle", "shell.bat", System.getProperty( "java.io.tmpdir" ), this );
          fileObject = createTemporaryShellFile( tempFile, realScript );
        }
      } else {
        if ( insertScript ) {
          tempFile = KettleVFS.createTempFile( "kettle", "shell", System.getProperty( "java.io.tmpdir" ), this );
          fileObject = createTemporaryShellFile( tempFile, realScript );
        }
        base = new String[] { KettleVFS.getFilename( fileObject ) };
      }

      // Construct the arguments...
      if ( argFromPrevious && cmdRows != null ) {
        // Add the base command...
        for ( int i = 0; i < base.length; i++ ) {
          cmds.add( base[i] );
        }

        if ( Const.getOS().equals( "Windows 95" ) || Const.getOS().startsWith( "Windows" ) ) {
          // for windows all arguments including the command itself
          // need to be
          // included in 1 argument to cmd/command.

          StringBuilder cmdline = new StringBuilder( 300 );

          cmdline.append( '"' );
          cmdline.append( Const.optionallyQuoteStringByOS( KettleVFS.getFilename( fileObject ) ) );
          // Add the arguments from previous results...
          for ( int i = 0; i < cmdRows.size(); i++ ) {
            // Normally just one row, but once in a while to remain compatible we have multiple.

            RowMetaAndData r = cmdRows.get( i );
            for ( int j = 0; j < r.size(); j++ ) {
              cmdline.append( ' ' );
              cmdline.append( Const.optionallyQuoteStringByOS( r.getString( j, null ) ) );
            }
          }
          cmdline.append( '"' );
          cmds.add( cmdline.toString() );
        } else {
          // Add the arguments from previous results...
          for ( int i = 0; i < cmdRows.size(); i++ ) {
            // Normally just one row, but once in a while to remain compatible we have multiple.

            RowMetaAndData r = cmdRows.get( i );
            for ( int j = 0; j < r.size(); j++ ) {
              cmds.add( Const.optionallyQuoteStringByOS( r.getString( j, null ) ) );
            }
          }
        }
      } else if ( args != null ) {
        // Add the base command...
        for ( int i = 0; i < base.length; i++ ) {
          cmds.add( base[i] );
        }

        if ( Const.getOS().equals( "Windows 95" ) || Const.getOS().startsWith( "Windows" ) ) {
          // for windows all arguments including the command itself
          // need to be
          // included in 1 argument to cmd/command.

          StringBuilder cmdline = new StringBuilder( 300 );

          cmdline.append( '"' );
          cmdline.append( Const.optionallyQuoteStringByOS( KettleVFS.getFilename( fileObject ) ) );

          for ( int i = 0; i < args.length; i++ ) {
            cmdline.append( ' ' );
            cmdline.append( Const.optionallyQuoteStringByOS( args[i] ) );
          }
          cmdline.append( '"' );
          cmds.add( cmdline.toString() );
        } else {
          for ( int i = 0; i < args.length; i++ ) {
            cmds.add( args[i] );
          }
        }
      }

      StringBuilder command = new StringBuilder();

      Iterator<String> it = cmds.iterator();
      boolean first = true;
      while ( it.hasNext() ) {
        if ( !first ) {
          command.append( ' ' );
        } else {
          first = false;
        }
        command.append( it.next() );
      }
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "JobShell.ExecCommand", command.toString() ) );
      }

      // Build the environment variable list...
      ProcessBuilder procBuilder = new ProcessBuilder( cmds );
      Map<String, String> env = procBuilder.environment();
      String[] variables = listVariables();
      for ( int i = 0; i < variables.length; i++ ) {
        env.put( variables[i], getVariable( variables[i] ) );
      }

      if ( getWorkDirectory() != null && !Utils.isEmpty( Const.rtrim( getWorkDirectory() ) ) ) {
        String vfsFilename = environmentSubstitute( getWorkDirectory() );
        File file = new File( KettleVFS.getFilename( KettleVFS.getFileObject( vfsFilename, this ) ) );
        procBuilder.directory( file );
      }
      Process proc = procBuilder.start();

      // any error message?
      StreamLogger errorLogger = new StreamLogger( log, proc.getErrorStream(), "(stderr)", true );

      // any output?
      StreamLogger outputLogger = new StreamLogger( log, proc.getInputStream(), "(stdout)" );

      // kick them off
      Thread errorLoggerThread = new Thread( errorLogger );
      errorLoggerThread.start();
      Thread outputLoggerThread = new Thread( outputLogger );
      outputLoggerThread.start();

      proc.waitFor();
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobShell.CommandFinished", command.toString() ) );
      }

      // What's the exit status?
      result.setExitStatus( proc.exitValue() );
      if ( result.getExitStatus() != 0 ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobShell.ExitStatus", environmentSubstitute( getFilename() ), "" + result.getExitStatus() ) );
        }

        result.setNrErrors( 1 );
      }

      // wait until loggers read all data from stdout and stderr
      errorLoggerThread.join();
      outputLoggerThread.join();

      // close the streams
      // otherwise you get "Too many open files, java.io.IOException" after a lot of iterations
      proc.getErrorStream().close();
      proc.getOutputStream().close();

    } catch ( IOException ioe ) {
      logError( BaseMessages.getString(
        PKG, "JobShell.ErrorRunningShell", environmentSubstitute( getFilename() ), ioe.toString() ), ioe );
      result.setNrErrors( 1 );
    } catch ( InterruptedException ie ) {
      logError( BaseMessages.getString(
        PKG, "JobShell.Shellinterupted", environmentSubstitute( getFilename() ), ie.toString() ), ie );
      result.setNrErrors( 1 );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobShell.UnexpectedError", environmentSubstitute( getFilename() ), e
        .toString() ), e );
      result.setNrErrors( 1 );
    } finally {
      // If we created a temporary file, remove it...
      //
      if ( tempFile != null ) {
        try {
          tempFile.delete();
        } catch ( Exception e ) {
          BaseMessages.getString( PKG, "JobShell.UnexpectedError", tempFile.toString(), e.toString() );
        }
      }
    }

    if ( result.getNrErrors() > 0 ) {
      result.setResult( false );
    } else {
      result.setResult( true );
    }
  }

  private FileObject createTemporaryShellFile( FileObject tempFile, String fileContent ) throws Exception {
    // Create a unique new temporary filename in the working directory, put the script in there
    // Set the permissions to execute and then run it...
    //
    if ( tempFile != null && fileContent != null ) {
      try {
        // flag indicates if current OS is Windows or not
        boolean isWindows = Const.isWindows();
        if ( !isWindows ) {
          fileContent = replaceWinEOL( fileContent );
        }
        tempFile.createFile();
        OutputStream outputStream = tempFile.getContent().getOutputStream();
        outputStream.write( fileContent.getBytes() );
        outputStream.close();
        if ( !isWindows ) {
          String tempFilename = KettleVFS.getFilename( tempFile );
          // Now we have to make this file executable...
          // On Unix-like systems this is done using the command "/bin/chmod +x filename"
          //
          ProcessBuilder procBuilder = new ProcessBuilder( "chmod", "+x", tempFilename );
          Process proc = procBuilder.start();
          // Eat/log stderr/stdout all messages in a different thread...
          StreamLogger errorLogger = new StreamLogger( log, proc.getErrorStream(), toString() + " (stderr)" );
          StreamLogger outputLogger = new StreamLogger( log, proc.getInputStream(), toString() + " (stdout)" );
          new Thread( errorLogger ).start();
          new Thread( outputLogger ).start();
          proc.waitFor();
        }

      } catch ( Exception e ) {
        throw new Exception( "Unable to create temporary file to execute script", e );
      }
    }
    return tempFile;
  }

  @VisibleForTesting
  String replaceWinEOL( String input ) {
    String result = input;
    // replace Windows's EOL if it's contained ( see PDI-12176 )
    result = result.replaceAll( "\\r\\n?", "\n" );

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( filename ) ) {
      String realFileName = jobMeta.environmentSubstitute( filename );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realFileName, ResourceType.FILE ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notBlankValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );

    JobEntryValidatorUtils.andValidator().validate( this, "workDirectory", remarks, ctx );
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );

    if ( setLogfile ) {
      JobEntryValidatorUtils.andValidator().validate( this, "logfile", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    }
  }

  protected String getLogfile() {
    return logfile;
  }
}
