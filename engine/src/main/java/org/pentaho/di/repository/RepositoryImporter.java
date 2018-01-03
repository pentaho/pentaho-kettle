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

package org.pentaho.di.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.exception.LookupReferencesException;
import org.pentaho.di.core.gui.HasOverwritePrompter;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

public class RepositoryImporter implements IRepositoryImporter, CanLimitDirs {
  public static final String IMPORT_ASK_ABOUT_REPLACE_DB = "IMPORT_ASK_ABOUT_REPLACE_DB";
  public static final String IMPORT_ASK_ABOUT_REPLACE_SS = "IMPORT_ASK_ABOUT_REPLACE_SS";
  public static final String IMPORT_ASK_ABOUT_REPLACE_CS = "IMPORT_ASK_ABOUT_REPLACE_CS";
  public static final String IMPORT_ASK_ABOUT_REPLACE_PS = "IMPORT_ASK_ABOUT_REPLACE_PS";

  private static final Class<?> PKG = RepositoryImporter.class;

  private Repository rep;
  private LogChannelInterface log;

  private SharedObjects sharedObjects;
  private RepositoryDirectoryInterface baseDirectory;
  private RepositoryDirectoryInterface root;

  private boolean overwrite;
  private boolean askOverwrite = true;
  // Ask Before Replacing Objects property handler
  private boolean askReplace;

  private String versionComment;

  private boolean needToCheckPathForVariables;

  private boolean continueOnError;

  private String transDirOverride = null;
  private String jobDirOverride = null;

  private ImportRules importRules;

  private List<String> limitDirs;

  private List<RepositoryObject> referencingObjects;

  private List<Exception> exceptions;

  private OverwritePrompter overwritePrompter;

  private final Set<String> rememberPropertyNamesToOverwrite = new HashSet<String>();

  public RepositoryImporter( Repository repository ) {
    this( repository, new ImportRules(), new ArrayList<String>() );
  }

  public RepositoryImporter( Repository repository, LogChannelInterface log ) {
    this( repository, new ImportRules(), Collections.<String>emptyList(), log );
  }

  public RepositoryImporter( Repository repository, ImportRules importRules, List<String> limitDirs ) {
    this( repository, importRules, limitDirs, new LogChannel( "Repository import" ) );
  }

  public RepositoryImporter( Repository repository, ImportRules importRules, List<String> limitDirs,
      LogChannelInterface log ) {
    this.log = log;
    this.rep = repository;
    this.importRules = importRules;
    this.limitDirs = limitDirs;
    this.exceptions = new ArrayList<Exception>();
  }

  private boolean isRemembered( String rememberPropertyName ) {
    return !"Y".equalsIgnoreCase( Props.getInstance().getProperty( rememberPropertyName ) );
  }

  private boolean getPromptResult( String message, String rememberText, String rememberPropertyName ) {
    boolean result = false;
    // There is nothing to remember in case of Ask Before Replacing option is turned off.
    // Thus is returned value of Replace existing objects checkbox
    if ( !askReplace ) {
      result = overwritePrompter.overwritePrompt( message, rememberText, rememberPropertyName );
      return result;
    }
    if ( isRemembered( rememberPropertyName ) ) {
      result = rememberPropertyNamesToOverwrite.contains( rememberPropertyName );
      return result;
    }
    result = overwritePrompter.overwritePrompt( message, rememberText, rememberPropertyName );
    // Save result so we'll know what to return if the user selects to remember
    if ( result ) {
      rememberPropertyNamesToOverwrite.add( rememberPropertyName );
    } else {
      rememberPropertyNamesToOverwrite.remove( rememberPropertyName );
    }
    return result;
  }

  @Override
  public synchronized void importAll( RepositoryImportFeedbackInterface feedback, String fileDirectory,
      String[] filenames, RepositoryDirectoryInterface baseDirectory, boolean overwrite, boolean continueOnError,
      String versionComment ) {
    this.baseDirectory = baseDirectory;
    this.overwrite = overwrite;
    this.continueOnError = continueOnError;
    this.versionComment = versionComment;

    String importPathCompatibility =
        System.getProperty( Const.KETTLE_COMPATIBILITY_IMPORT_PATH_ADDITION_ON_VARIABLES, "N" );
    this.needToCheckPathForVariables = "N".equalsIgnoreCase( importPathCompatibility );

    askReplace = Props.getInstance().askAboutReplacingDatabaseConnections();

    if ( askReplace ) {
      if ( feedback instanceof HasOverwritePrompter ) {
        Props.getInstance().setProperty( IMPORT_ASK_ABOUT_REPLACE_CS, "Y" );
        Props.getInstance().setProperty( IMPORT_ASK_ABOUT_REPLACE_DB, "Y" );
        Props.getInstance().setProperty( IMPORT_ASK_ABOUT_REPLACE_PS, "Y" );
        Props.getInstance().setProperty( IMPORT_ASK_ABOUT_REPLACE_SS, "Y" );
        this.overwritePrompter = ( (HasOverwritePrompter) feedback ).getOverwritePrompter();
      } else {
        this.overwritePrompter = new OverwritePrompter() {

          @Override
          public boolean overwritePrompt( String arg0, String arg1, String arg2 ) {
            throw new RuntimeException( BaseMessages.getString( PKG, "RepositoryImporter.CannotPrompt.Label" ) );
          }
        };
      }
    } else {
      final boolean replaceExisting = Props.getInstance().replaceExistingDatabaseConnections();
      this.overwritePrompter = new OverwritePrompter() {

        @Override
        public boolean overwritePrompt( String arg0, String arg1, String arg2 ) {
          return replaceExisting;
        }
      };
    }

    referencingObjects = new ArrayList<RepositoryObject>();

    feedback.setLabel( BaseMessages.getString( PKG, "RepositoryImporter.ImportXML.Label" ) );
    try {

      loadSharedObjects();

      RepositoryImportLocation.setRepositoryImportLocation( baseDirectory );

      for ( int ii = 0; ii < filenames.length; ++ii ) {

        final String filename =
            ( !Utils.isEmpty( fileDirectory ) ) ? fileDirectory + Const.FILE_SEPARATOR + filenames[ii] : filenames[ii];
        if ( log.isBasic() ) {
          log.logBasic( "Import objects from XML file [" + filename + "]" );
        }
        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.WhichFile.Log", filename ) );

        // To where?
        feedback.setLabel( BaseMessages.getString( PKG, "RepositoryImporter.WhichDir.Label" ) );

        // Read it using SAX...
        //
        try {
          RepositoryExportSaxParser parser = new RepositoryExportSaxParser( filename, feedback );
          parser.parse( this );
        } catch ( Exception e ) {
          addException( e );
          feedback.showError( BaseMessages.getString( PKG, "RepositoryImporter.ErrorGeneral.Title" ), BaseMessages
              .getString( PKG, "RepositoryImporter.ErrorGeneral.Message" ), e );
        }
      }

      // Correct those jobs and transformations that contain references to other objects.
      for ( RepositoryObject repoObject : referencingObjects ) {
        switch ( repoObject.getObjectType() ) {
          case TRANSFORMATION:
            TransMeta transMeta = rep.loadTransformation( repoObject.getObjectId(), null );
            saveTransformationToRepo( transMeta, feedback );
            break;
          case JOB:
            JobMeta jobMeta = rep.loadJob( repoObject.getObjectId(), null );
            saveJobToRepo( jobMeta, feedback );
            break;
          default:
            throw new KettleException( BaseMessages.getString( PKG, "RepositoryImporter.ErrorDetectFileType" ) );
        }
      }

      feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.ImportFinished.Log" ) );
    } catch ( Exception e ) {
      addException( e );
      feedback.showError( BaseMessages.getString( PKG, "RepositoryImporter.ErrorGeneral.Title" ), BaseMessages
          .getString( PKG, "RepositoryImporter.ErrorGeneral.Message" ), e );
    } finally {
      // set the repository import location to null when done!
      RepositoryImportLocation.setRepositoryImportLocation( null );
    }
  }

  /**
   * Load the shared objects up front, replace them in the xforms/jobs loaded from XML. We do this for performance
   * reasons.
   *
   * @throws KettleException
   */
  protected void loadSharedObjects() throws KettleException {
    sharedObjects = new SharedObjects();

    for ( ObjectId id : rep.getDatabaseIDs( false ) ) {
      DatabaseMeta databaseMeta = rep.loadDatabaseMeta( id, null );
      validateImportedElement( importRules, databaseMeta );
      sharedObjects.storeObject( databaseMeta );
    }

    ObjectId[] slaveIDs = rep.getSlaveIDs( false );
    List<SlaveServer> slaveServers = new ArrayList<SlaveServer>( slaveIDs.length );
    for ( ObjectId id : slaveIDs ) {
      SlaveServer slaveServer = rep.loadSlaveServer( id, null );
      validateImportedElement( importRules, slaveServer );
      sharedObjects.storeObject( slaveServer );
      slaveServers.add( slaveServer );
    }

    for ( ObjectId id : rep.getClusterIDs( false ) ) {
      ClusterSchema clusterSchema = rep.loadClusterSchema( id, slaveServers, null );
      validateImportedElement( importRules, clusterSchema );
      sharedObjects.storeObject( clusterSchema );
    }
    for ( ObjectId id : rep.getPartitionSchemaIDs( false ) ) {
      PartitionSchema partitionSchema = rep.loadPartitionSchema( id, null );
      validateImportedElement( importRules, partitionSchema );
      sharedObjects.storeObject( partitionSchema );
    }
  }

  /**
   * Validates the repository element that is about to get imported against the list of import rules.
   *
   * @param importRules
   *          import rules to validate against.
   * @param subject
   * @throws KettleException
   */
  public static void validateImportedElement( ImportRules importRules, Object subject ) throws KettleException {
    List<ImportValidationFeedback> feedback = importRules.verifyRules( subject );
    List<ImportValidationFeedback> errors = ImportValidationFeedback.getErrors( feedback );
    if ( !errors.isEmpty() ) {
      StringBuilder message =
          new StringBuilder( BaseMessages.getString( PKG, "RepositoryImporter.ValidationFailed.Message", subject
              .toString() ) );
      message.append( Const.CR );
      for ( ImportValidationFeedback error : errors ) {
        message.append( " - " );
        message.append( error.toString() );
        message.append( Const.CR );
      }
      throw new KettleException( message.toString() );
    }
  }

  @Override
  public void addLog( String line ) {
    log.logBasic( line );
  }

  @Override
  public void setLabel( String labelText ) {
    log.logBasic( labelText );
  }

  @Override
  public boolean transOverwritePrompt( TransMeta transMeta ) {
    return overwrite;
  }

  @Override
  public boolean jobOverwritePrompt( JobMeta jobMeta ) {
    return overwrite;
  }

  @Override
  public void updateDisplay() {
  }

  @Override
  public void showError( String title, String message, Exception e ) {
    log.logError( message, e );
  }

  @SuppressWarnings( "unchecked" )
  protected <T extends SharedObjectInterface> List<T> getSharedObjects( Class<T> clazz ) {
    List<T> result = new ArrayList<T>();
    for ( SharedObjectInterface sharedObject : sharedObjects.getObjectsMap().values() ) {
      if ( clazz.isInstance( sharedObject ) ) {
        result.add( (T) sharedObject );
      }
    }
    return result;
  }

  private boolean equals( Object obj1, Object obj2 ) {
    if ( obj1 == null ) {
      if ( obj2 == null ) {
        return true;
      }
      return false;
    }
    if ( obj2 == null ) {
      return false;
    }
    return obj1.equals( obj2 );
  }

  /**
   * Adapted from KettleDatabaseRepositoryDatabaseDelegate.saveDatabaseMeta
   */
  protected boolean equals( DatabaseMeta databaseMeta, DatabaseMeta databaseMeta2 ) {
    if ( !equals( databaseMeta.getName(), databaseMeta2.getName() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getPluginId(), databaseMeta2.getPluginId() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getAccessType(), databaseMeta2.getAccessType() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getHostname(), databaseMeta2.getHostname() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getDatabaseName(), databaseMeta2.getDatabaseName() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getDatabasePortNumberString(), databaseMeta2.getDatabasePortNumberString() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getUsername(), databaseMeta2.getUsername() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getPassword(), databaseMeta2.getPassword() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getServername(), databaseMeta2.getServername() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getDataTablespace(), databaseMeta2.getDataTablespace() ) ) {
      return false;
    } else if ( !equals( databaseMeta.getIndexTablespace(), databaseMeta2.getIndexTablespace() ) ) {
      return false;
    }
    Map<Object, Object> databaseMeta2Attributes = new HashMap<Object, Object>( databaseMeta2.getAttributes() );
    for ( Entry<Object, Object> databaseMetaEntry : new HashMap<Object, Object>( databaseMeta.getAttributes() )
        .entrySet() ) {
      Object value = databaseMeta2Attributes.remove( databaseMetaEntry.getKey() );
      if ( !equals( value, databaseMetaEntry.getValue() ) ) {
        return false;
      }
    }
    if ( databaseMeta2Attributes.size() > 0 ) {
      return false;
    }
    return true;
  }

  protected boolean equals( SlaveServer slaveServer, SlaveServer slaveServer2 ) {
    if ( !equals( slaveServer.getName(), slaveServer2.getName() ) ) {
      return false;
    } else if ( !equals( slaveServer.getHostname(), slaveServer2.getHostname() ) ) {
      return false;
    } else if ( !equals( slaveServer.getPort(), slaveServer2.getPort() ) ) {
      return false;
    } else if ( !equals( slaveServer.getWebAppName(), slaveServer2.getWebAppName() ) ) {
      return false;
    } else if ( !equals( slaveServer.getUsername(), slaveServer2.getUsername() ) ) {
      return false;
    } else if ( !equals( slaveServer.getPassword(), slaveServer2.getPassword() ) ) {
      return false;
    } else if ( !equals( slaveServer.getProxyHostname(), slaveServer2.getProxyHostname() ) ) {
      return false;
    } else if ( !equals( slaveServer.getProxyPort(), slaveServer2.getProxyPort() ) ) {
      return false;
    } else if ( !equals( slaveServer.getNonProxyHosts(), slaveServer2.getNonProxyHosts() ) ) {
      return false;
    } else if ( !equals( slaveServer.isMaster(), slaveServer2.isMaster() ) ) {
      return false;
    }
    return true;
  }

  protected boolean equals( PartitionSchema partitionSchema, PartitionSchema partitionSchema2 ) {
    if ( !equals( partitionSchema.getName(), partitionSchema2.getName() ) ) {
      return false;
    } else if ( !equals( partitionSchema.getPartitionIDs(), partitionSchema2.getPartitionIDs() ) ) {
      return false;
    } else if ( !equals( partitionSchema.isDynamicallyDefined(), partitionSchema2.isDynamicallyDefined() ) ) {
      return false;
    } else if ( !equals( partitionSchema.getNumberOfPartitionsPerSlave(), partitionSchema2
        .getNumberOfPartitionsPerSlave() ) ) {
      return false;
    }
    return true;
  }

  protected boolean equals( ClusterSchema clusterSchema, ClusterSchema clusterSchema2 ) {
    if ( !equals( clusterSchema.getName(), clusterSchema2.getName() ) ) {
      return false;
    } else if ( !equals( clusterSchema.getBasePort(), clusterSchema2.getBasePort() ) ) {
      return false;
    } else if ( !equals( clusterSchema.getSocketsBufferSize(), clusterSchema2.getSocketsBufferSize() ) ) {
      return false;
    } else if ( !equals( clusterSchema.getSocketsFlushInterval(), clusterSchema2.getSocketsFlushInterval() ) ) {
      return false;
    } else if ( !equals( clusterSchema.isSocketsCompressed(), clusterSchema2.isSocketsCompressed() ) ) {
      return false;
    } else if ( !equals( clusterSchema.isDynamic(), clusterSchema2.isDynamic() ) ) {
      return false;
    } else if ( !equals( clusterSchema.getSlaveServers(), clusterSchema2.getSlaveServers() ) ) {
      return false;
    }
    return true;
  }

  private void replaceSharedObjects( AbstractMeta abstractMeta ) {
    for ( DatabaseMeta databaseMeta : getSharedObjects( DatabaseMeta.class ) ) {
      // Database...
      int index = abstractMeta.indexOfDatabase( databaseMeta );
      if ( index < 0 ) {
        abstractMeta.addDatabase( databaseMeta );
      } else {
        DatabaseMeta imported = abstractMeta.getDatabase( index );
        // Preserve the object id so we can update without having to look up the id
        imported.setObjectId( databaseMeta.getObjectId() );
        if ( equals( databaseMeta, imported )
            || !getPromptResult( BaseMessages.getString( PKG,
                "RepositoryImporter.Dialog.ConnectionExistsOverWrite.Message", imported.getName() ), BaseMessages
                .getString( PKG, "RepositoryImporter.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ),
                IMPORT_ASK_ABOUT_REPLACE_DB ) ) {
          imported.replaceMeta( databaseMeta );
          // We didn't actually change anything
          imported.clearChanged();
        } else {
          imported.setChanged();
        }
      }
    }

    for ( SlaveServer slaveServer : getSharedObjects( SlaveServer.class ) ) {
      int index = abstractMeta.getSlaveServers().indexOf( slaveServer );
      if ( index < 0 ) {
        abstractMeta.getSlaveServers().add( slaveServer );
      } else {
        SlaveServer imported = abstractMeta.getSlaveServers().get( index );
        // Preserve the object id so we can update without having to look up the id
        imported.setObjectId( slaveServer.getObjectId() );
        if ( equals( slaveServer, imported )
            || !getPromptResult( BaseMessages.getString( PKG,
                "RepositoryImporter.Dialog.SlaveServerExistsOverWrite.Message", imported.getName() ), BaseMessages
                .getString( PKG, "RepositoryImporter.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ),
                IMPORT_ASK_ABOUT_REPLACE_SS ) ) {
          imported.replaceMeta( slaveServer );
          // We didn't actually change anything
          imported.clearChanged();
        } else {
          imported.setChanged();
        }
      }
    }
  }

  protected void replaceSharedObjects( TransMeta transMeta ) throws KettleException {
    replaceSharedObjects( (AbstractMeta) transMeta );
    for ( ClusterSchema clusterSchema : getSharedObjects( ClusterSchema.class ) ) {
      int index = transMeta.getClusterSchemas().indexOf( clusterSchema );
      if ( index < 0 ) {
        transMeta.getClusterSchemas().add( clusterSchema );
      } else {
        ClusterSchema imported = transMeta.getClusterSchemas().get( index );
        // Preserve the object id so we can update without having to look up the id
        imported.setObjectId( clusterSchema.getObjectId() );
        if ( equals( clusterSchema, imported )
            || !getPromptResult( BaseMessages.getString( PKG,
                "RepositoryImporter.Dialog.ClusterSchemaExistsOverWrite.Message", imported.getName() ), BaseMessages
                .getString( PKG, "RepositoryImporter.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ),
                IMPORT_ASK_ABOUT_REPLACE_CS ) ) {
          imported.replaceMeta( clusterSchema );
          // We didn't actually change anything
          imported.clearChanged();
        } else {
          imported.setChanged();
        }
      }
    }

    for ( PartitionSchema partitionSchema : getSharedObjects( PartitionSchema.class ) ) {
      int index = transMeta.getPartitionSchemas().indexOf( partitionSchema );
      if ( index < 0 ) {
        transMeta.getPartitionSchemas().add( partitionSchema );
      } else {
        PartitionSchema imported = transMeta.getPartitionSchemas().get( index );
        // Preserve the object id so we can update without having to look up the id
        imported.setObjectId( partitionSchema.getObjectId() );
        if ( equals( partitionSchema, imported )
            || !getPromptResult( BaseMessages.getString( PKG,
                "RepositoryImporter.Dialog.PartitionSchemaExistsOverWrite.Message", imported.getName() ), BaseMessages
                .getString( PKG, "RepositoryImporter.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ),
                IMPORT_ASK_ABOUT_REPLACE_PS ) ) {
          imported.replaceMeta( partitionSchema );
          // We didn't actually change anything
          imported.clearChanged();
        } else {
          imported.setChanged();
        }
      }
    }
  }

  protected void replaceSharedObjects( JobMeta transMeta ) throws KettleException {
    replaceSharedObjects( (AbstractMeta) transMeta );
  }

  /**
   * package-local visibility for testing purposes
   */
  void patchTransSteps( TransMeta transMeta ) {
    for ( StepMeta stepMeta : transMeta.getSteps() ) {
      StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
      if ( stepMetaInterface instanceof HasRepositoryDirectories ) {
        patchRepositoryDirectories( stepMetaInterface.isReferencedObjectEnabled(), (HasRepositoryDirectories) stepMetaInterface  );
      }
    }
  }

  private void patchJobEntries( JobMeta jobMeta ) {
    for ( JobEntryCopy copy : jobMeta.getJobCopies() ) {
      JobEntryInterface jobEntryInterface = copy.getEntry();
      if ( jobEntryInterface instanceof HasRepositoryDirectories ) {
        patchRepositoryDirectories( jobEntryInterface.isReferencedObjectEnabled(), (HasRepositoryDirectories) jobEntryInterface  );
      }
    } }

  private void patchRepositoryDirectories( boolean[] referenceEnabled, HasRepositoryDirectories metaWithReferences ) {
    String[] repDirectories = metaWithReferences.getDirectories();
    if ( referenceEnabled != null && repDirectories != null ) {
      ObjectLocationSpecificationMethod[] specificationMethods = metaWithReferences.getSpecificationMethods();
      String[] resolvedDirectories = Arrays.copyOf( repDirectories, repDirectories.length );
      for ( int i = 0; i < referenceEnabled.length; i++ ) {
        if ( referenceEnabled[ i ] ) {
          if ( specificationMethods[ i ] == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ) {
            if ( transDirOverride != null ) {
              resolvedDirectories[ i ] = transDirOverride;
            } else {
              resolvedDirectories[ i ] = resolvePath( baseDirectory.getPath(), repDirectories[ i ] );
            }
          }
        }
      }
      metaWithReferences.setDirectories( resolvedDirectories );
    }
  }

  /**
   * package-local visibility for testing purposes
   */
  String resolvePath( String rootPath, String entryPath ) {
    String extraPath = Const.NVL( entryPath, "/" );
    if ( needToCheckPathForVariables() ) {
      if ( containsVariables( entryPath ) ) {
        return extraPath;
      }
    }
    String newPath = Const.NVL( rootPath, "/" );
    if ( newPath.endsWith( "/" ) && extraPath.startsWith( "/" ) ) {
      newPath = newPath.substring( 0, newPath.length() - 1 );
    } else if ( !newPath.endsWith( "/" ) && !extraPath.startsWith( "/" ) ) {
      newPath += "/";
    } else if ( extraPath.equals( "/" ) ) {
      extraPath = "";
    }
    return newPath + extraPath;
  }

  private static boolean containsVariables( String entryPath ) {
    List<String> variablesList = new ArrayList<String>();
    StringUtil.getUsedVariables( entryPath, variablesList, true );
    return !variablesList.isEmpty();
  }

  boolean needToCheckPathForVariables() {
    return needToCheckPathForVariables;
  }

  protected void saveTransMeta( TransMeta transMeta ) throws KettleException {
    rep.save( transMeta, versionComment, this, overwrite );
  }

  /**
   *
   * @param transnode
   *          The XML DOM node to read the transformation from
   * @return false if the import should be canceled.
   * @throws KettleException
   *           in case there is an unexpected error
   */
  protected boolean importTransformation( Node transnode, RepositoryImportFeedbackInterface feedback )
    throws KettleException {
    //
    // Load transformation from XML into a directory, possibly created!
    //
    TransMeta transMeta = createTransMetaForNode( transnode ); // ignore shared objects
    feedback.setLabel( BaseMessages.getString( PKG, "RepositoryImporter.ImportTrans.Label", Integer
        .toString( transformationNumber ), transMeta.getName() ) );

    validateImportedElement( importRules, transMeta );

    // What's the directory path?
    String directoryPath = Const.NVL( XMLHandler.getTagValue( transnode, "info", "directory" ), Const.FILE_SEPARATOR );
    if ( transDirOverride != null ) {
      directoryPath = transDirOverride;
    }

    if ( directoryPath.startsWith( "/" ) ) {
      // remove the leading root, we don't need it.
      directoryPath = directoryPath.substring( 1 );
    }

    // If we have a set of source directories to limit ourselves to, consider this.
    //
    if ( limitDirs.size() > 0 && Const.indexOfString( directoryPath, limitDirs ) < 0 ) {
      // Not in the limiting set of source directories, skip the import of this transformation...
      //
      feedback.addLog( BaseMessages.getString( PKG,
          "RepositoryImporter.SkippedTransformationNotPartOfLimitingDirectories.Log", transMeta.getName() ) );
      return true;
    }

    RepositoryDirectoryInterface targetDirectory = getTargetDirectory( directoryPath, transDirOverride, feedback );

    // OK, we loaded the transformation from XML and all went well...
    // See if the transformation already existed!
    ObjectId existingId = rep.getTransformationID( transMeta.getName(), targetDirectory );
    if ( existingId != null && askOverwrite ) {
      overwrite = feedback.transOverwritePrompt( transMeta );
      askOverwrite = feedback.isAskingOverwriteConfirmation();
    } else {
      updateDisplay();
    }

    if ( existingId == null || overwrite ) {
      replaceSharedObjects( transMeta );
      transMeta.setObjectId( existingId );
      transMeta.setRepositoryDirectory( targetDirectory );
      patchTransSteps( transMeta );

      try {
        // Keep info on who & when this transformation was created...
        if ( transMeta.getCreatedUser() == null || transMeta.getCreatedUser().equals( "-" ) ) {
          transMeta.setCreatedDate( new Date() );
          if ( rep.getUserInfo() != null ) {
            transMeta.setCreatedUser( rep.getUserInfo().getLogin() );
          } else {
            transMeta.setCreatedUser( null );
          }
        }
        saveTransMeta( transMeta );
        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.TransSaved.Log", Integer
            .toString( transformationNumber ), transMeta.getName() ) );

        if ( transMeta.hasRepositoryReferences() ) {
          referencingObjects.add( new RepositoryObject( transMeta.getObjectId(), transMeta.getName(), transMeta
              .getRepositoryDirectory(), null, null, RepositoryObjectType.TRANSFORMATION, null, false ) );
        }

      } catch ( Exception e ) {
        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.ErrorSavingTrans.Log", Integer
            .toString( transformationNumber ), transMeta.getName(), Const.getStackTracker( e ) ) );

        if ( !feedback.askContinueOnErrorQuestion( BaseMessages.getString( PKG,
            "RepositoryImporter.DoYouWantToContinue.Title" ), BaseMessages.getString( PKG,
            "RepositoryImporter.DoYouWantToContinue.Message" ) ) ) {
          return false;
        }
      }
    } else {
      feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.SkippedExistingTransformation.Log", transMeta
          .getName() ) );
    }
    return true;
  }

  TransMeta createTransMetaForNode( Node transnode ) throws KettleMissingPluginsException, KettleXMLException {
    return new TransMeta( transnode, null );
  }

  protected void saveJobMeta( JobMeta jobMeta ) throws KettleException {
    // Keep info on who & when this transformation was created...
    if ( jobMeta.getCreatedUser() == null || jobMeta.getCreatedUser().equals( "-" ) ) {
      jobMeta.setCreatedDate( new Date() );
      if ( rep.getUserInfo() != null ) {
        jobMeta.setCreatedUser( rep.getUserInfo().getLogin() );
      } else {
        jobMeta.setCreatedUser( null );
      }
    }

    rep.save( jobMeta, versionComment, null, overwrite );
  }

  protected boolean importJob( Node jobnode, RepositoryImportFeedbackInterface feedback ) throws KettleException {
    // Load the job from the XML node.
    //
    JobMeta jobMeta = createJobMetaForNode( jobnode );
    feedback.setLabel( BaseMessages.getString( PKG, "RepositoryImporter.ImportJob.Label",
        Integer.toString( jobNumber ), jobMeta.getName() ) );
    validateImportedElement( importRules, jobMeta );

    // What's the directory path?
    String directoryPath = Const.NVL( XMLHandler.getTagValue( jobnode, "directory" ), Const.FILE_SEPARATOR );

    if ( jobDirOverride != null ) {
      directoryPath = jobDirOverride;
    }

    if ( directoryPath.startsWith( "/" ) ) {
      // remove the leading root, we don't need it.
      directoryPath = directoryPath.substring( 1 );
    }

    // If we have a set of source directories to limit ourselves to, consider this.
    //
    if ( limitDirs.size() > 0 && Const.indexOfString( directoryPath, limitDirs ) < 0 ) {
      // Not in the limiting set of source directories, skip the import of this transformation...
      //
      feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.SkippedJobNotPartOfLimitingDirectories.Log",
          jobMeta.getName() ) );
      return true;
    }

    RepositoryDirectoryInterface targetDirectory = getTargetDirectory( directoryPath, jobDirOverride, feedback );

    // OK, we loaded the job from XML and all went well...
    // See if the job already exists!
    ObjectId existintId = rep.getJobId( jobMeta.getName(), targetDirectory );
    if ( existintId != null && askOverwrite ) {
      overwrite = feedback.jobOverwritePrompt( jobMeta );
      askOverwrite = feedback.isAskingOverwriteConfirmation();
    } else {
      updateDisplay();
    }

    if ( existintId == null || overwrite ) {
      replaceSharedObjects( jobMeta );
      jobMeta.setRepositoryDirectory( targetDirectory );
      jobMeta.setObjectId( existintId );
      patchJobEntries( jobMeta );
      try {
        saveJobMeta( jobMeta );

        if ( jobMeta.hasRepositoryReferences() ) {
          referencingObjects.add( new RepositoryObject( jobMeta.getObjectId(), jobMeta.getName(), jobMeta
              .getRepositoryDirectory(), null, null, RepositoryObjectType.JOB, null, false ) );
        }

        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.JobSaved.Log", Integer.toString( jobNumber ),
            jobMeta.getName() ) );
      } catch ( Exception e ) {
        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.ErrorSavingJob.Log", Integer
            .toString( jobNumber ), jobMeta.getName(), Const.getStackTracker( e ) ) );

        if ( !feedback.askContinueOnErrorQuestion( BaseMessages.getString( PKG,
            "RepositoryImporter.DoYouWantToContinue.Title" ), BaseMessages.getString( PKG,
            "RepositoryImporter.DoYouWantToContinue.Message" ) ) ) {
          return false;
        }
      }
    } else {
      feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.SkippedExistingJob.Log", jobMeta.getName() ) );
    }
    return true;
  }

  private int transformationNumber = 1;
  private DocumentBuilder documentBuilder;

  JobMeta createJobMetaForNode( Node jobnode ) throws KettleXMLException {
    return new JobMeta( jobnode, null, false, SpoonFactory.getInstance() );
  }

  private DocumentBuilder getOrCreateDb() throws KettleXMLException {
    if ( documentBuilder == null ) {
      documentBuilder = XMLHandler.createDocumentBuilder( false, true );
    }
    return documentBuilder;
  }

  @Override
  public boolean transformationElementRead( String xml, RepositoryImportFeedbackInterface feedback ) {
    try {
      Document doc = XMLHandler.loadXMLString( getOrCreateDb(), xml );
      Node transformationNode = XMLHandler.getSubNode( doc, RepositoryExportSaxParser.STRING_TRANSFORMATION );
      if ( !importTransformation( transformationNode, feedback ) ) {
        return false;
      }
      transformationNumber++;
    } catch ( Exception e ) {
      // Some unexpected error occurred during transformation import
      // This is usually a problem with a missing plugin or something
      // like that...
      //
      feedback.showError( BaseMessages.getString( PKG,
          "RepositoryImporter.UnexpectedErrorDuringTransformationImport.Title" ), BaseMessages.getString( PKG,
          "RepositoryImporter.UnexpectedErrorDuringTransformationImport.Message" ), e );

      if ( !feedback.askContinueOnErrorQuestion( BaseMessages.getString( PKG,
          "RepositoryImporter.DoYouWantToContinue.Title" ), BaseMessages.getString( PKG,
          "RepositoryImporter.DoYouWantToContinue.Message" ) ) ) {
        return false;
      }
    }
    return true;
  }

  private int jobNumber = 1;

  @Override
  public boolean jobElementRead( String xml, RepositoryImportFeedbackInterface feedback ) {
    try {
      Document doc = XMLHandler.loadXMLString( getOrCreateDb(), xml );
      Node jobNode = XMLHandler.getSubNode( doc, RepositoryExportSaxParser.STRING_JOB );
      if ( !importJob( jobNode, feedback ) ) {
        return false;
      }
      jobNumber++;
    } catch ( Exception e ) {
      // Some unexpected error occurred during job import
      // This is usually a problem with a missing plugin or something
      // like that...
      //
      showError( BaseMessages.getString( PKG, "RepositoryImporter.UnexpectedErrorDuringJobImport.Title" ), BaseMessages
          .getString( PKG, "RepositoryImporter.UnexpectedErrorDuringJobImport.Message" ), e );

      if ( !feedback.askContinueOnErrorQuestion( BaseMessages.getString( PKG,
          "RepositoryImporter.DoYouWantToContinue.Title" ), BaseMessages.getString( PKG,
          "RepositoryImporter.DoYouWantToContinue.Message" ) ) ) {
        return false;
      }
    }
    return true;
  }

  private RepositoryDirectoryInterface getTargetDirectory( String directoryPath, String dirOverride,
      RepositoryImportFeedbackInterface feedback ) throws KettleException {
    if ( directoryPath.isEmpty() ) {
      return baseDirectory;
    }
    RepositoryDirectoryInterface targetDirectory = null;
    if ( dirOverride != null ) {
      targetDirectory = rep.findDirectory( directoryPath );
      if ( targetDirectory == null ) {
        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.CreateDir.Log", directoryPath,
            getRepositoryRoot().toString() ) );
        targetDirectory = rep.createRepositoryDirectory( getRepositoryRoot(), directoryPath );
      }
    } else {
      targetDirectory = baseDirectory.findDirectory( directoryPath );
      if ( targetDirectory == null ) {
        feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.CreateDir.Log", directoryPath, baseDirectory
            .toString() ) );
        targetDirectory = rep.createRepositoryDirectory( baseDirectory, directoryPath );
      }
    }
    return targetDirectory;
  }

  private RepositoryDirectoryInterface getRepositoryRoot() throws KettleException {
    if ( root == null ) {
      root = rep.loadRepositoryDirectoryTree();
    }
    return root;
  }

  @Override
  public void fatalXmlErrorEncountered( SAXParseException e ) {
    showError( BaseMessages.getString( PKG, "RepositoryImporter.ErrorInvalidXML.Message" ), BaseMessages.getString(
        PKG, "RepositoryImporter.ErrorInvalidXML.Title" ), e );
  }

  @Override
  public boolean askContinueOnErrorQuestion( String title, String message ) {
    return continueOnError;
  }

  @Override
  public void beginTask( String message, int nrWorks ) {
    addLog( message );
  }

  @Override
  public void done() {
  }

  @Override
  public boolean isCanceled() {
    return false;
  }

  @Override
  public void setTaskName( String taskName ) {
    addLog( taskName );
  }

  @Override
  public void subTask( String message ) {
    addLog( message );
  }

  @Override
  public void worked( int nrWorks ) {
  }

  public String getTransDirOverride() {
    return transDirOverride;
  }

  @Override
  public void setTransDirOverride( String transDirOverride ) {
    this.transDirOverride = transDirOverride;
  }

  public String getJobDirOverride() {
    return jobDirOverride;
  }

  @Override
  public void setJobDirOverride( String jobDirOverride ) {
    this.jobDirOverride = jobDirOverride;
  }

  @Override
  public void setImportRules( ImportRules importRules ) {
    this.importRules = importRules;
  }

  public ImportRules getImportRules() {
    return importRules;
  }

  @Override
  public boolean isAskingOverwriteConfirmation() {
    return askOverwrite;
  }

  private void addException( Exception exception ) {
    if ( this.exceptions == null ) {
      this.exceptions = new ArrayList<Exception>();
    }
    exceptions.add( exception );
  }

  @Override
  public List<Exception> getExceptions() {
    return exceptions;
  }

  @Override
  public void setLimitDirs( List<String> limitDirs ) {
    this.limitDirs = new ArrayList<String>( limitDirs );
  }

  protected void setBaseDirectory( RepositoryDirectoryInterface baseDirectory ) {
    this.baseDirectory = baseDirectory;
  }

  public void setOverwrite( boolean overwrite ) {
    this.overwrite = overwrite;
  }

  public String getVersionComment() {
    return versionComment;
  }

  private void saveTransformationToRepo( TransMeta transMeta, RepositoryImportFeedbackInterface feedback )
      throws KettleException {
    try {
      transMeta.lookupRepositoryReferences( rep );
    } catch ( LookupReferencesException e ) {
      // log and continue; might fail from exports performed before PDI-5294
      feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.LookupRepoRefsError.Log", transMeta.getName(),
          RepositoryObjectType.TRANSFORMATION ) );
      feedback.addLog( BaseMessages
          .getString( PKG, "RepositoryImporter.LookupRepoRefsError.Log.Cause", e.objectTypePairsToString() ) );
    }
    rep.save( transMeta, "import object reference specification", null );
  }

  private void saveJobToRepo( JobMeta jobMeta, RepositoryImportFeedbackInterface feedback ) throws KettleException {
    try {
      jobMeta.lookupRepositoryReferences( rep );
    } catch ( LookupReferencesException e ) {
      // log and continue; might fail from exports performed before PDI-5294
      feedback.addLog( BaseMessages.getString( PKG, "RepositoryImporter.LookupRepoRefsError.Log", jobMeta.getName(),
          RepositoryObjectType.JOB ) );
      feedback.addLog( BaseMessages
          .getString( PKG, "RepositoryImporter.LookupRepoRefsError.Log.Cause", e.objectTypePairsToString() ) );
    }
    rep.save( jobMeta, "import object reference specification", null );
  }

}
