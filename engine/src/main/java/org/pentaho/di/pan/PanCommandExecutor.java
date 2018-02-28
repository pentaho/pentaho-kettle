/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.pan;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PanCommandExecutor {

  private static final String YES = "Y";

  private LogChannelInterface log;
  private Class<?> pkgClazz;
  DelegatingMetaStore metaStore;

  private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );

  public PanCommandExecutor( Class<?> pkgClazz ) {
    this( pkgClazz, new LogChannel( Pan.STRING_PAN ) );
  }

  public PanCommandExecutor( Class<?> pkgClazz, LogChannelInterface log ) {
    this.pkgClazz = pkgClazz;
    this.log = log;
  }

  public int execute( final String repoName, final String noRepo, final String username, final String password,
                      final String dirName, final String filename, final String jarFile, final String transName,
                      final String listTrans, final String listDirs, final String exportRepo, final String initialDir,
                      final String listRepos, final String safemode, final String metrics, final String listParams,
                      final NamedParams params, final String[] arguments ) throws Throwable {

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.StartingToRun" ) );

    Date start = Calendar.getInstance().getTime(); // capture execution start time

    logDebug( "Pan.Log.AllocatteNewTrans" );

    Trans trans = null;

    try {

      if ( getMetaStore() == null ) {
        setMetaStore( createDefaultMetastore() );
      }

      logDebug( "Pan.Log.StartingToLookOptions" );

      // Read kettle transformation specified
      if ( !Utils.isEmpty( repoName ) || !Utils.isEmpty( filename ) || !Utils.isEmpty( jarFile ) ) {

        logDebug( "Pan.Log.ParsingCommandline" );

        if ( !Utils.isEmpty( repoName ) && !YES.equalsIgnoreCase( noRepo ) ) {

          // In case we use a repository...
          // some commands are to load a Trans from the repo; others are merely to print some repo-related information
          trans = executeRepositoryBasedCommand( repoName, username, password, dirName, transName, listTrans, listDirs, exportRepo );
        }


        // Try to load the transformation from file, even if it failed to load from the repository
        // You could implement some fail-over mechanism this way.
        if ( trans == null ) {
          trans = executeFilesystemBasedCommand( initialDir, filename, jarFile );
        }

      }

      if ( YES.equalsIgnoreCase( listRepos ) ) {
        printRepositories( loadRepositoryInfo() ); // list the repositories placed at repositories.xml
      }

    } catch ( Exception e ) {

      trans = null;

      System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.ProcessStopError", e.getMessage() ) );
      e.printStackTrace();
      return PanReturnCode.ERRORS_DURING_PROCESSING.getCode();
    }

    if ( trans == null ) {

      if ( !YES.equalsIgnoreCase( listTrans ) && !YES.equalsIgnoreCase( listDirs )
              && !YES.equalsIgnoreCase( listRepos ) && Utils.isEmpty( exportRepo ) ) {

        System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.CanNotLoadTrans" ) );
        return PanReturnCode.COULD_NOT_LOAD_TRANS.getCode();
      } else {
          return PanReturnCode.SUCCESS.getCode();
      }
    }

    try {

      trans.setLogLevel( getLog().getLogLevel() );
      configureParameters( trans, params, trans.getTransMeta() );

      trans.setSafeModeEnabled( YES.equalsIgnoreCase( safemode ) ); // run in safe mode if requested
      trans.setGatheringMetrics( YES.equalsIgnoreCase( metrics ) ); // enable kettle metric gathering if requested

      // List the parameters defined in this transformation, and then simply exit
      if ( YES.equalsIgnoreCase( listParams ) ) {

        printTransformationParameters( trans );

        // stop right here...
        return PanReturnCode.COULD_NOT_LOAD_TRANS.getCode(); // same as the other list options
      }

      // allocate & run the required sub-threads
      try {
        trans.execute( arguments );

      } catch ( KettleException ke ) {
        logDebug( ke.getLocalizedMessage() );
        System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.UnablePrepareInitTrans" ) );
        return PanReturnCode.UNABLE_TO_PREP_INIT_TRANS.getCode();
      }

      waitUntilFinished( trans, 100 ); // Give the transformation up to 10 seconds to finish execution

      if ( trans.isRunning() ) {
        getLog().logError( BaseMessages.getString( getPkgClazz(), "Pan.Log.NotStopping" ) );
      }

      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.Finished" ) );
      Date stop = Calendar.getInstance().getTime(); // capture execution stop time

      int completionTimeSeconds = calculateAndPrintElapsedTime( start, stop );

      if ( trans.getResult().getNrErrors() == 0 ) {

        trans.printStats( completionTimeSeconds );
        return PanReturnCode.SUCCESS.getCode();

      } else {

        String transJVMExitCode = trans.getVariable( Const.KETTLE_TRANS_PAN_JVM_EXIT_CODE );

        // If the trans has a return code to return to the OS, then we exit with that
        if ( !Utils.isEmpty( transJVMExitCode ) ) {

          try {
            return Integer.parseInt( transJVMExitCode );

          } catch ( NumberFormatException nfe ) {
            getLog().logError( BaseMessages.getString( getPkgClazz(), "Pan.Error.TransJVMExitCodeInvalid",
                    Const.KETTLE_TRANS_PAN_JVM_EXIT_CODE, transJVMExitCode ) );
            getLog().logError( BaseMessages.getString( getPkgClazz(), "Pan.Log.JVMExitCode", "1" ) );
            return PanReturnCode.ERRORS_DURING_PROCESSING.getCode();
          }

        } else {
            // the trans does not have a return code.
            return PanReturnCode.ERRORS_DURING_PROCESSING.getCode();
        }
      }

    } catch ( KettleException ke ) {

      System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Log.ErrorOccurred", "" + ke.getMessage() ) );
      getLog().logError( BaseMessages.getString( getPkgClazz(), "Pan.Log.UnexpectedErrorOccurred", "" + ke.getMessage() ) );

      return PanReturnCode.UNEXPECTED_ERROR.getCode();

    }
  }

  public Trans executeRepositoryBasedCommand( final String repoName, final String username, final String password, final String dirName,
                                              final String transName, final String listTrans, final String listDirs, final String exportRepo ) throws Exception {


    if ( Utils.isEmpty( repoName ) ) {
      System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.NoRepProvided" ) );
      return null;
    }

    Repository repo = null;

    try {

      RepositoryMeta repositoryMeta = loadRepositoryConnection( repoName );

      if ( repositoryMeta != null ) {
        // Define and connect to the repository...
        logDebug( "Pan.Log.Allocate&ConnectRep" );

        repo = establishRepositoryConnection( repositoryMeta, username, password, RepositoryOperation.EXECUTE_TRANSFORMATION );

        // Default is the root directory
        RepositoryDirectoryInterface directory = repo.loadRepositoryDirectoryTree();

        // Add the IMetaStore of the repository to our delegation
        if ( repo.getMetaStore() != null && getMetaStore() != null ) {
          getMetaStore().addMetaStore( repo.getMetaStore() );
        }

        // Find the directory name if one is specified...
        if ( !Utils.isEmpty( dirName ) ) {
          directory = directory.findDirectory( dirName );
        }

        if ( directory != null ) {
          // Check username, password
          logDebug( "Pan.Log.CheckSuppliedUserPass" );

          // transname is not empty ? then command it to load a transformation
          if ( !Utils.isEmpty( transName ) ) {

            logDebug("Pan.Log.LoadTransInfo" );
            TransMeta transMeta = repo.loadTransformation( transName, directory, null, true, null );

            logDebug("Pan.Log.AllocateTrans" );
            Trans trans = new Trans( transMeta );
            trans.setRepository( repo );
            trans.setMetaStore( getMetaStore() );

            return trans; // return transformation loaded from the repo

          } else if ( YES.equalsIgnoreCase( listTrans ) ) {

            printRepositoryStoredTransformations( repo, directory ); // List the transformations in the repository

          } else if ( YES.equalsIgnoreCase( listDirs ) ) {

            printRepositoryDirectories( repo, directory ); // List the directories in the repository

          } else if ( !Utils.isEmpty( exportRepo ) ) {

            // Export the repository
            System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Log.ExportingObjectsRepToFile", "" + exportRepo ) );
            repo.getExporter().exportAllObjects( null, exportRepo, directory, "all" );
            System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Log.FinishedExportObjectsRepToFile", "" + exportRepo ) );

          } else {
            System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.NoTransNameSupplied" ) );
          }
        } else {
          System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.CanNotFindSpecifiedDirectory", "" + dirName ) );
        }
      } else {
        System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.NoRepProvided" ) );
      }

    } finally {

      if ( repo != null ) {
        repo.disconnect();
      }

    }

    return null;
  }

  public Trans executeFilesystemBasedCommand( final String initialDir, final String filename, final String jarFilename ) throws Exception {

    Trans trans = null;

    // Try to load the transformation from file
    if ( !Utils.isEmpty( filename ) ) {

      String filepath = filename;
      // If the filename starts with scheme like zip:, then isAbsolute() will return false even though the
      // the path following the zip is absolute. Check for isAbsolute only if the fileName does not start with scheme
      if ( !KettleVFS.startsWithScheme( filename ) && !FileUtil.isFullyQualified( filename ) ) {
        filepath = initialDir + filename;
      }

      logDebug( "Pan.Log.LoadingTransXML", "" + filepath );
      TransMeta transMeta = new TransMeta( filepath );
      trans = new Trans( transMeta );

    }

    if ( !Utils.isEmpty( jarFilename ) ) {

      try {

        logDebug( "Pan.Log.LoadingTransJar", jarFilename );

        InputStream inputStream = PanCommandExecutor.class.getResourceAsStream( jarFilename );
        StringBuilder xml = new StringBuilder();
        int c;
        while ( ( c = inputStream.read() ) != -1 ) {
          xml.append( (char) c );
        }
        inputStream.close();
        Document document = XMLHandler.loadXMLString( xml.toString() );
        TransMeta transMeta = new TransMeta( XMLHandler.getSubNode( document, "transformation" ), null );
        trans = new Trans( transMeta );

      } catch ( Exception e ) {

        System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Error.ReadingJar", e.toString() ) );
        System.out.println( Const.getStackTracker( e ) );
        throw e;
      }
    }

    return trans;
  }

  /**
   * Configures the transformation with the given parameters and their values
   *
   * @param trans        the executable transformation object
   * @param optionParams the list of parameters to set for the transformation
   * @param transMeta    the transformation metadata
   * @throws UnknownParamException
   */
  protected static void configureParameters( Trans trans, NamedParams optionParams,
                                               TransMeta transMeta ) throws UnknownParamException {
    trans.initializeVariablesFrom( null );
    trans.getTransMeta().setInternalKettleVariables( trans );

    // Map the command line named parameters to the actual named parameters.
    // Skip for the moment any extra command line parameter not known in the transformation.
    String[] transParams = trans.listParameters();
    for ( String param : transParams ) {
      String value = optionParams.getParameterValue( param );
      if ( value != null ) {
        trans.setParameterValue( param, value );
        transMeta.setParameterValue( param, value );
      }
    }

    // Put the parameters over the already defined variable space. Parameters get priority.
    trans.activateParameters();
  }

  public DelegatingMetaStore createDefaultMetastore() throws MetaStoreException {
    DelegatingMetaStore metaStore = new DelegatingMetaStore();
    metaStore.addMetaStore( MetaStoreConst.openLocalPentahoMetaStore() );
    metaStore.setActiveMetaStoreName( metaStore.getName() );
    return metaStore;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public Class<?> getPkgClazz() {
    return pkgClazz;
  }

  public DelegatingMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( DelegatingMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  protected void printTransformationParameters( Trans transformation ) throws UnknownParamException {

    if ( transformation == null || transformation.listParameters() == null ) {
      return;
    }

    for ( String paramName : transformation.listParameters() ) {
      String value = transformation.getParameterValue( paramName );
      String deflt = transformation.getParameterDefault( paramName );
      String descr = transformation.getParameterDescription( paramName );

      if ( deflt != null ) {
        System.out.println( "Parameter: " + paramName + "=" + Const.NVL( value, "" ) + ", default=" + deflt + " : " + Const.NVL( descr, "" ) );
      } else {
        System.out.println( "Parameter: " + paramName + "=" + Const.NVL( value, "" ) + " : " + Const.NVL( descr, "" ) );
      }
    }
  }

  protected void printRepositoryDirectories( Repository repository, RepositoryDirectoryInterface directory ) throws KettleException {

    String[] directories = repository.getDirectoryNames( directory.getObjectId() );

    if ( directories != null ) {
      for ( String dir :  directories ) {
        System.out.println( dir );
      }
    }
  }

  protected void printRepositoryStoredTransformations( Repository repository, RepositoryDirectoryInterface directory ) throws KettleException {

    logDebug( "Pan.Log.GettingListTransDirectory", "" + directory );
    String[] transformations = repository.getTransformationNames( directory.getObjectId(), false );

    if ( transformations != null ) {
      for ( String trans :  transformations ) {
        System.out.println( trans );
      }
    }
  }

  protected void printRepositories( RepositoriesMeta repositoriesMeta ) {

    if ( repositoriesMeta != null ) {

      logDebug( "Pan.Log.GettingListReps" );

      for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
        RepositoryMeta repInfo = repositoriesMeta.getRepository( i );
        System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Log.RepNameDesc", "" + ( i + 1 ),
                repInfo.getName(), repInfo.getDescription() ) );
      }
    }
  }

  protected RepositoryMeta loadRepositoryConnection( final String optionRepname ) throws KettleException {

    RepositoriesMeta repsinfo = null;

    if ( Utils.isEmpty( optionRepname ) || ( repsinfo = loadRepositoryInfo() ) == null ) {
      return null;
    }

    logDebug(  "Pan.Log.FindingRep", optionRepname );
    return repsinfo.findRepository( optionRepname );
  }

  protected RepositoriesMeta loadRepositoryInfo() throws KettleException {

    RepositoriesMeta repsinfo = new RepositoriesMeta();
    repsinfo.getLog().setLogLevel( getLog().getLogLevel() );

    logDebug( "Pan.Log.LoadingAvailableRep" );

    try {
      repsinfo.readData();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( getPkgClazz(), "Pan.Error.NoRepsDefined" ), e );
    }

    return repsinfo;
  }

  protected Repository establishRepositoryConnection( RepositoryMeta repositoryMeta, final String username, final String password,
                                                      final RepositoryOperation... operations ) throws KettleException, KettleSecurityException {

    Repository rep = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
    rep.init( repositoryMeta );
    rep.getLog().setLogLevel( getLog().getLogLevel() );
    rep.connect( username != null ? username : null, password != null ? password : null );

    if( operations != null ) {
      // throws KettleSecurityException if username does does have permission for given operations
      rep.getSecurityProvider().validateAction( operations );
    }

    return rep;
  }

  protected SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

  private void waitUntilFinished( Trans trans, final long waitMillis ) {

    if ( trans != null && trans.isRunning() ) {

      trans.waitUntilFinished();

      for ( int i = 0; i < 100; i++ ) {
        if ( !trans.isRunning() ) {
          break;
        }

        try {
          Thread.sleep( waitMillis );
        } catch ( Exception e ) {
          break;
        }
      }
    }
  }

  private void logDebug( final String messageKey ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey ) );
    }
  }

  private void logDebug( final String messageKey, String... messageTokens ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey, messageTokens ) );
    }
  }

  private int calculateAndPrintElapsedTime( Date start, Date stop ) {

    String begin = getDateFormat().format( start ).toString();
    String end = getDateFormat().format( stop ).toString();

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.StartStop", begin, end ) );

    long millis = stop.getTime() - start.getTime();
    int seconds = (int) ( millis / 1000 );
    if ( seconds <= 60 ) {
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.ProcessingEndAfter", String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 ) {
      int min = ( seconds / 60 );
      int rem = ( seconds % 60 );
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.ProcessingEndAfterLong", String.valueOf( min ),
                  String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 * 24 ) {
      int rem;
      int hour = ( seconds / ( 60 * 60 ) );
      rem = ( seconds % ( 60 * 60 ) );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal( BaseMessages.getString(  getPkgClazz(), "Pan.Log.ProcessingEndAfterLonger", String.valueOf( hour ),
                  String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else {
      int rem;
      int days = ( seconds / ( 60 * 60 * 24 ) );
      rem = ( seconds % ( 60 * 60 * 24 ) );
      int hour = rem / ( 60 * 60 );
      rem = rem % ( 60 * 60 );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.ProcessingEndAfterLongest", String.valueOf( days ),
                  String.valueOf( hour ), String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    }

    return seconds;
  }
}


