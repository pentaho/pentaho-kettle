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

package org.pentaho.di.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.HasOverwritePrompter;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.repository.CanLimitDirs;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportFeedbackInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.version.BuildVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Import {
  private static final Class<?> PKG = Import.class; // i18n

  public static final String STRING_IMPORT = "Import";
  public static final String ROOT_DIRECTORY = "/";

  private static class ImportFeedback implements RepositoryImportFeedbackInterface, HasOverwritePrompter {
    private final LogChannelInterface log;
    private final boolean continueOnError;
    private final boolean replace;
    private final BufferedReader reader;

    public ImportFeedback( LogChannelInterface log, boolean continueOnError, boolean replace, BufferedReader reader ) {
      this.log = log;
      this.continueOnError = continueOnError;
      this.replace = replace;
      this.reader = reader;
    }

    @Override
    public void updateDisplay() {
    }

    @Override
    public boolean transOverwritePrompt( TransMeta transMeta ) {
      return replace;
    }

    @Override
    public void showError( String title, String message, Exception e ) {
      log.logError( title + " : " + message, e );
    }

    @Override
    public void setLabel( String labelText ) {
      log.logBasic( labelText );
    }

    @Override
    public boolean jobOverwritePrompt( JobMeta jobMeta ) {
      return replace;
    }

    @Override
    public boolean askContinueOnErrorQuestion( String title, String message ) {
      return continueOnError;
    }

    @Override
    public void addLog( String line ) {
      log.logBasic( line );
    }

    @Override
    public boolean isAskingOverwriteConfirmation() {
      return false;
    }

    @Override
    public OverwritePrompter getOverwritePrompter() {
      return new OverwritePrompter() {
        private final String yes = BaseMessages.getString( PKG, "Import.Yes" );
        private final String no = BaseMessages.getString( PKG, "Import.No" );
        private final String none = BaseMessages.getString( PKG, "Import.None" );
        private final String all = BaseMessages.getString( PKG, "Import.All" );
        private final String prompt = "[" + yes + "," + no + "," + none + "," + all + "]";

        @Override
        public boolean overwritePrompt( String message, String rememberText, String rememberPropertyName ) {
          log.logBasic( message );
          String line;
          Boolean result = null;
          boolean remember = false;
          while ( result == null ) {
            log.logBasic( prompt );
            try {
              line = reader.readLine().trim();
            } catch ( IOException e ) {
              throw new RuntimeException( BaseMessages.getString( PKG, "Import.CouldntReadline" ) );
            }
            if ( line.equalsIgnoreCase( yes ) || line.equalsIgnoreCase( all ) ) {
              result = true;
            } else if ( line.equalsIgnoreCase( no ) || line.equalsIgnoreCase( none ) ) {
              result = false;
            }
            if ( line.equalsIgnoreCase( all ) || line.equalsIgnoreCase( none ) ) {
              remember = true;
            }
          }
          Props.getInstance().setProperty( rememberPropertyName, ( !remember ) ? "Y" : "N" );
          return result;
        }
      };
    }
  }

  public static void main( String[] a ) throws KettleException {
    BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
    KettleEnvironment.init();
    Props.init( Props.TYPE_PROPERTIES_SPOON );

    List<String> args = pickupCmdArguments( a );

    StringBuilder optionRepname, optionUsername, optionPassword, optionDirname;
    StringBuilder optionLimitDir, optionFilename, optionRules, optionComment;
    StringBuilder optionReplace, optionContinueOnError, optionVersion, optionFileDir, optionNoRules;

    CommandLineOption[] options =
      new CommandLineOption[] {
        // Basic options
        //
        createOption( "rep", "Import.CmdLine.RepName", optionRepname = new StringBuilder() ),
        createOption( "user", "Import.CmdLine.RepUsername", optionUsername = new StringBuilder() ),
        createOption( "pass", "Import.CmdLine.RepPassword", optionPassword = new StringBuilder() ),
        createOption( "dir", "Import.CmdLine.RepDir", optionDirname = new StringBuilder() ),
        createOption( "limitdir", "Import.CmdLine.LimitDir", optionLimitDir = new StringBuilder() ),
        createOption( "file", "Import.CmdLine.File", optionFilename = new StringBuilder() ),
        createOption( "filedir", "Import.CmdLine.FileDir", optionFileDir = new StringBuilder() ),
        createOption( "rules", "Import.CmdLine.RulesFile", optionRules = new StringBuilder() ),
        createOption( "norules", "Import.CmdLine.NoRules", optionNoRules = new StringBuilder(), true, false ),
        createOption( "comment", "Import.CmdLine.Comment", optionComment = new StringBuilder(), false, false ),
        createOption( "replace", "Import.CmdLine.Replace", optionReplace = new StringBuilder(), true, false ),
        createOption( "coe", "Import.CmdLine.ContinueOnError", optionContinueOnError = new StringBuilder(), true, false ),
        createOption( "version", "Import.CmdLine.Version", optionVersion = new StringBuilder(), true, false ),

        new CommandLineOption(
          "", BaseMessages.getString( PKG, "Import.CmdLine.ExtraFiles" ), new StringBuilder(), false, true,
          true ),
      };

    if ( args.isEmpty() ) {
      CommandLineOption.printUsage( options );
      exitJVM( 9 );
    }

    final LogChannelInterface log = new LogChannel( STRING_IMPORT );
    CommandLineOption.parseArguments( args, options, log );

    // The arguments that are still left in args are in fact filenames that need to be imported.
    // This list is otherwise empty.
    // To that we add the normal filename option
    //
    List<String> filenames = new ArrayList<String>( args );
    if ( !Utils.isEmpty( optionFilename ) ) {
      filenames.add( optionFilename.toString() );
    }


    String kettleRepname = Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null );
    String kettleUsername = Const.getEnvironmentVariable( "KETTLE_USER", null );
    String kettlePassword = Const.getEnvironmentVariable( "KETTLE_PASSWORD", null );

    if ( !Utils.isEmpty( kettleRepname ) ) {
      optionRepname = new StringBuilder( kettleRepname );
    }
    if ( !Utils.isEmpty( kettleUsername ) ) {
      optionUsername = new StringBuilder( kettleUsername );
    }
    if ( !Utils.isEmpty( kettlePassword ) ) {
      optionPassword = new StringBuilder( kettlePassword );
    }


    if ( !Utils.isEmpty( optionVersion ) ) {
      BuildVersion buildVersion = BuildVersion.getInstance();
      log.logBasic( BaseMessages.getString(
        PKG, "Import.Log.KettleVersion", buildVersion.getVersion(), buildVersion.getRevision(), buildVersion
          .getBuildDate() ) );
      if ( a.length == 1 ) {
        exitJVM( 6 );
      }
    }


    // Verify repository options...
    //
    if ( Utils.isEmpty( optionRepname ) ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.NoRepProvided" ) );
      exitJVM( 1 );
    }

    if ( Utils.isEmpty( filenames ) ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.NoExportFileProvided" ) );
      exitJVM( 1 );
    }

    if ( Utils.isEmpty( optionDirname ) ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.NoRepositoryDirectoryProvided" ) );
      exitJVM( 1 );
    }

    if ( Utils.isEmpty( optionRules )
      && Utils.isEmpty( optionNoRules ) && !"Y".equalsIgnoreCase( optionNoRules.toString() ) ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.NoRulesFileProvided" ) );
      exitJVM( 1 );
    }


    // Load the rules file!
    //
    ImportRules importRules = new ImportRules();
    String rulesFile = optionRules.toString();

    if ( !Utils.isEmpty( rulesFile ) ) {
      try {
        Document document = XMLHandler.loadXMLFile( rulesFile );
        Node rulesNode = XMLHandler.getSubNode( document, ImportRules.XML_TAG );
        importRules.loadXML( rulesNode );
        log.logMinimal( BaseMessages.getString( PKG, "Import.Log.RulesLoaded", rulesFile, Integer.toString(
          importRules.getRules().size() ) ) );
        for ( ImportRuleInterface rule : importRules.getRules() ) {
          log.logBasic( " - " + rule.toString() );
        }
      } catch ( KettleException e ) {
        log.logError( BaseMessages.getString( PKG, "Import.Log.ExceptionLoadingRules", rulesFile ), e );
        exitJVM( 7 );
      }
    }

    // Get the list of limiting source directories
    //
    List<String> limitDirs;
    if ( !Utils.isEmpty( optionLimitDir ) ) {
      String[] directories = optionLimitDir.toString().split( "," );
      limitDirs = Arrays.asList( directories );
    } else {
      limitDirs = Collections.emptyList();
    }

    // Find the repository metadata...
    //
    RepositoriesMeta repsinfo = new RepositoriesMeta();
    repsinfo.getLog().setLogLevel( log.getLogLevel() );
    try {
      repsinfo.readData();
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.UnableToLoadRepositoryInformation" ), e );
      exitJVM( 1 );
    }

    RepositoryMeta repositoryMeta = repsinfo.findRepository( optionRepname.toString() );
    if ( repositoryMeta == null ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.RepositoryCouldNotBeFound", optionRepname
        .toString() ) );
      exitJVM( 1 );
    }

    if ( Utils.isEmpty( optionRepname ) ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.NoRepProvided" ) );
      exitJVM( 1 );
    }

    // Load the repository object as a plugin...
    //
    Repository repository = null;
    try {
      repository =
        PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
      repository.init( repositoryMeta );
      repository.getLog().setLogLevel( log.getLogLevel() );
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.UnableToLoadOrInitializeRepository" ) );
      exitJVM( 1 );
    }
    try {
      repository.connect( optionUsername != null ? optionUsername.toString() : null, optionPassword != null
        ? optionPassword.toString() : null );
    } catch ( KettleException ke ) {
      log.logError( ke.getMessage() );
      exitJVM( 1 );
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.UnableToConnectToRepository" ) );
      exitJVM( 1 );
    }

    final boolean replace =
      Utils.isEmpty( optionReplace ) ? false : ValueMetaString.convertStringToBoolean( optionReplace.toString() );
    final boolean continueOnError =
      Utils.isEmpty( optionContinueOnError ) ? false : ValueMetaString.convertStringToBoolean( optionContinueOnError
        .toString() );

    // Start the import!
    //
    log.logMinimal( BaseMessages.getString( PKG, "Import.Log.Starting" ) );

    Date start, stop;
    SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
    start = new Date(  );
    int returnCode = 0;

    if ( ROOT_DIRECTORY.equals( optionDirname.toString() ) ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.TargetDirectoryIsRootDirectory" ) );
      exitJVM( 1 );
    }

    try {
      RepositoryDirectoryInterface tree = repository.loadRepositoryDirectoryTree();

      RepositoryDirectoryInterface targetDirectory = tree.findDirectory( optionDirname.toString() );
      if ( targetDirectory == null ) {
        log.logError( BaseMessages.getString(
          PKG, "Import.Error.UnableToFindTargetDirectoryInRepository", optionDirname.toString() ) );
        exitJVM( 1 );
      }

      // Perform the actual import
      IRepositoryImporter importer = repository.getImporter();
      importer.setImportRules( importRules );
      if ( !limitDirs.isEmpty() ) {
        if ( importer instanceof CanLimitDirs ) {
          ( (CanLimitDirs) importer ).setLimitDirs( limitDirs );
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "Import.CouldntLimitDirs", importer.getClass()
              .getCanonicalName() ) );
        }
      }
      RepositoryImportFeedbackInterface feedbackInterface = new ImportFeedback( log, continueOnError, replace, reader );

      // Import files in a certain directory
      importer.importAll( feedbackInterface, optionFileDir.toString(), filenames.toArray( new String[filenames
        .size()] ), targetDirectory, replace, continueOnError, optionComment.toString() );

      // If the importer has exceptions, then our return code is 2
      List<Exception> exceptions = importer.getExceptions();
      if ( exceptions != null && !exceptions.isEmpty() ) {
        log.logError( BaseMessages.getString( PKG, "Import.Error.UnexpectedErrorDuringImport" ), exceptions
          .get( 0 ) );
        returnCode = 2;
      }
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "Import.Error.UnexpectedErrorDuringImport" ), e );
      exitJVM( 2 );
    }
    log.logMinimal( BaseMessages.getString( PKG, "Import.Log.Finished" ) );

    stop = new Date(  );

    String begin = df.format( start );
    String end = df.format( stop );
    log.logMinimal( BaseMessages.getString( PKG, "Import.Log.StartStop", begin, end ) );

    long seconds = ( stop.getTime() - start.getTime() ) / 1000;
    if ( seconds <= 60 ) {
      log.logMinimal( BaseMessages.getString( PKG, "Import.Log.ProcessEndAfter", String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 ) {
      int min = (int) ( seconds / 60 );
      int rem = (int) ( seconds % 60 );
      log.logMinimal( BaseMessages.getString( PKG, "Import.Log.ProcessEndAfterLong", String.valueOf( min ), String
        .valueOf( rem ), String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 * 24 ) {
      int rem;
      int hour = (int) ( seconds / ( 60 * 60 ) );
      rem = (int) ( seconds % ( 60 * 60 ) );
      int min = rem / 60;
      rem = rem % 60;
      log.logMinimal( BaseMessages.getString(
        PKG, "Import.Log.ProcessEndAfterLonger", String.valueOf( hour ), String.valueOf( min ), String
          .valueOf( rem ), String.valueOf( seconds ) ) );
    } else {
      int rem;
      int days = (int) ( seconds / ( 60 * 60 * 24 ) );
      rem = (int) ( seconds % ( 60 * 60 * 24 ) );
      int hour = rem / ( 60 * 60 );
      rem = rem % ( 60 * 60 );
      int min = rem / 60;
      rem = rem % 60;
      log.logMinimal( BaseMessages.getString(
        PKG, "Import.Log.ProcessEndAfterLongest", String.valueOf( days ), String.valueOf( hour ), String
          .valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    }

    exitJVM( returnCode );

  }

  private static List<String> pickupCmdArguments( String[] a ) {
    List<String> args = new ArrayList<String>( a.length );
    for ( String arg : a ) {
      if ( !arg.isEmpty() ) {
        args.add( arg );
      }
    }
    return args;
  }

  private static CommandLineOption createOption( String option, String i18nKey, StringBuilder sb ) {
    return new CommandLineOption( option, BaseMessages.getString( PKG, i18nKey ), sb );
  }

  private static CommandLineOption createOption( String option, String i18nKey, StringBuilder sb, boolean yesNo, boolean hidden  ) {
    return new CommandLineOption( option, BaseMessages.getString( PKG, i18nKey ), sb, yesNo, hidden );
  }

  /**
   * Parse an argument as an integer.
   *
   * @param option
   *          Command Line Option to parse argument of
   * @param def
   *          Default if the argument is not set
   * @return The parsed argument or the default if the argument was not specified
   * @throws KettleException
   *           Error parsing provided argument as an integer
   */
  protected static int parseIntArgument( final CommandLineOption option, final int def ) throws KettleException {
    if ( !Utils.isEmpty( option.getArgument() ) ) {
      try {
        return Integer.parseInt( option.getArgument().toString() );
      } catch ( NumberFormatException ex ) {
        throw new KettleException( BaseMessages.getString( PKG, "Import.Error.InvalidNumberArgument", option
          .getOption(), option.getArgument() ) );
      }
    }
    return def;
  }

  private static void exitJVM( int status ) {
    System.exit( status );
  }
}
