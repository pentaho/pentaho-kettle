/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.checkfilelocked;

import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'check files locked' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */

public class JobEntryCheckFilesLocked extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static final String ARG_FROM_PREVIOUS_ATTR = "arg_from_previous";
  private static final String FILE_MASK_ATTR = "filemask";
  private static final String INCLUDE_SUBFOLDERS_ATTR = "include_subfolders";
  private static final String NAME_ATTR = "name";

  private static Class<?> PKG = JobEntryCheckFilesLocked.class; // for i18n purposes, needed by Translator2!!

  public boolean argFromPrevious;

  public boolean includeSubfolders;

  public String[] arguments;

  public String[] filemasks;

  private boolean oneFileLocked;

  public JobEntryCheckFilesLocked( String n ) {
    super( n, "" );
    argFromPrevious = false;
    arguments = null;

    includeSubfolders = false;
  }

  public JobEntryCheckFilesLocked() {
    this( "" );
  }

  public Object clone() {
    JobEntryCheckFilesLocked je = (JobEntryCheckFilesLocked) super.clone();
    if ( arguments != null ) {
      int nrFields = arguments.length;
      je.allocate( nrFields );
      System.arraycopy( arguments, 0, je.arguments, 0, nrFields );
      System.arraycopy( filemasks, 0, je.filemasks, 0, nrFields );
    }
    return je;
  }

  public void allocate( int nrFields ) {
    arguments = new String[nrFields];
    filemasks = new String[nrFields];
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( ARG_FROM_PREVIOUS_ATTR, argFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( INCLUDE_SUBFOLDERS_ATTR, includeSubfolders ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( NAME_ATTR, arguments[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( FILE_MASK_ATTR, filemasks[i] ) );
        retval.append( "        </field>" ).append( Const.CR );
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, ARG_FROM_PREVIOUS_ATTR ) );
      includeSubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, INCLUDE_SUBFOLDERS_ATTR ) );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        arguments[i] = XMLHandler.getTagValue( fnode, NAME_ATTR );
        filemasks[i] = XMLHandler.getTagValue( fnode, FILE_MASK_ATTR );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.UnableToLoadFromXml" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId idJobEntry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      argFromPrevious = rep.getJobEntryAttributeBoolean( idJobEntry, ARG_FROM_PREVIOUS_ATTR );
      includeSubfolders = rep.getJobEntryAttributeBoolean( idJobEntry, INCLUDE_SUBFOLDERS_ATTR );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( idJobEntry, NAME_ATTR );
      arguments = new String[argnr];
      filemasks = new String[argnr];

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = rep.getJobEntryAttributeString( idJobEntry, a, NAME_ATTR );
        filemasks[a] = rep.getJobEntryAttributeString( idJobEntry, a, FILE_MASK_ATTR );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryCheckFilesLocked.UnableToLoadFromRepo", String.valueOf( idJobEntry ) ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idJob ) throws KettleException {
    try {

      rep.saveJobEntryAttribute( idJob, getObjectId(), ARG_FROM_PREVIOUS_ATTR, argFromPrevious );
      rep.saveJobEntryAttribute( idJob, getObjectId(), INCLUDE_SUBFOLDERS_ATTR, includeSubfolders );

      // save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( idJob, getObjectId(), i, NAME_ATTR, arguments[i] );
          rep.saveJobEntryAttribute( idJob, getObjectId(), i, FILE_MASK_ATTR, filemasks[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryCheckFilesLocked.UnableToSaveToRepo", String.valueOf( idJob ) ), dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) {

    Result result = previousResult;
    List<RowMetaAndData> rows = result.getRows();

    oneFileLocked = false;
    result.setResult( true );

    try {
      if ( argFromPrevious  && isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.FoundPreviousRows", String
          .valueOf( ( rows != null ? rows.size() : 0 ) ) ) );
      }

      if ( argFromPrevious && rows != null ) {
        processFromPreviousArgument( rows );
      } else if ( arguments != null ) {

        for ( int i = 0; i < arguments.length && !parentJob.isStopped(); i++ ) {
          // ok we can process this file/folder
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingArg", arguments[i], filemasks[i] ) );
          }

          processFile( arguments[i], filemasks[i] );
        }
      }

      if ( oneFileLocked ) {
        result.setResult( false );
        result.setNrErrors( 1 );
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.ErrorRunningJobEntry", e ) );
    }

    return result;
  }

  private void processFromPreviousArgument( List<RowMetaAndData> rows ) throws KettleValueException {
    RowMetaAndData resultRow;
    // Copy the input row to the (command line) arguments
    for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
      resultRow = rows.get( iteration );

      // Get values from previous result
      String fileFolderPrevious = resultRow.getString( 0, "" );
      String fileMasksPrevious = resultRow.getString( 1, "" );

      // ok we can process this file/folder
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString(
          PKG, "JobEntryCheckFilesLocked.ProcessingRow", fileFolderPrevious, fileMasksPrevious ) );
      }

      processFile( fileFolderPrevious, fileMasksPrevious );
    }
  }

  private void processFile( String filename, String wildcard ) {

    String realFileFolderName = environmentSubstitute( filename );
    String realWildcard = environmentSubstitute( wildcard );

    try ( FileObject fileFolder = KettleVFS.getFileObject( realFileFolderName ) ) {
      FileObject[] files = new FileObject[] { fileFolder };
      if ( fileFolder.exists() ) {
        // the file or folder exists
        if ( fileFolder.getType() == FileType.FOLDER ) {
          // It's a folder
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingFolder", realFileFolderName ) );
          }
          // Retrieve all files
          files = fileFolder.findFiles( new TextFileSelector( fileFolder.toString(), realWildcard ) );

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.TotalFilesToCheck", String
              .valueOf( files.length ) ) );
          }
        } else {
          // It's a file
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingFile", realFileFolderName ) );
          }
        }
        // Check files locked
        checkFilesLocked( files );
      } else {
        // We can not find thsi file
        logBasic( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.FileNotExist", realFileFolderName ) );
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.CouldNotProcess", realFileFolderName, e
        .getMessage() ) );
    }
  }

  private void checkFilesLocked( FileObject[] files ) throws KettleException {

    for ( int i = 0; i < files.length && !oneFileLocked; i++ ) {
      FileObject file = files[i];
      String filename = KettleVFS.getFilename( file );
      LockFile locked = new LockFile( filename );
      if ( locked.isLocked() ) {
        oneFileLocked = true;
        logError( BaseMessages.getString( PKG, "JobCheckFilesLocked.Log.FileLocked", filename ) );
      } else {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobCheckFilesLocked.Log.FileNotLocked", filename ) );
        }
      }
    }
  }

  private class TextFileSelector implements FileSelector {
    String fileWildcard = null;
    String sourceFolder = null;

    public TextFileSelector( String sourcefolderin, String filewildcard ) {

      if ( !Utils.isEmpty( sourcefolderin ) ) {
        sourceFolder = sourcefolderin;
      }

      if ( !Utils.isEmpty( filewildcard ) ) {
        fileWildcard = filewildcard;
      }
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean returncode = false;
      try {

        if ( !info.getFile().toString().equals( sourceFolder ) ) {
          // Pass over the Base folder itself

          String shortFilename = info.getFile().getName().getBaseName();

          if ( !info.getFile().getParent().equals( info.getBaseFolder() ) ) {
            // Not in the Base Folder..Only if include sub folders
            if ( includeSubfolders ) {
              returncode = includeFileCheck( info, shortFilename );
            }
          } else {
            // In the Base Folder...
            returncode = includeFileCheck( info, shortFilename );
          }
        }

      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "JobCheckFilesLocked.Error.Exception.ProcessError" ), BaseMessages
          .getString( PKG, "JobCheckFilesLocked.Error.Exception.Process", info.getFile().toString(), e
            .getMessage() ) );
      }

      return returncode;
    }

    private boolean includeFileCheck( FileSelectInfo info, String shortFilename  ) throws FileSystemException {
      if ( ( info.getFile().getType() == FileType.FILE ) && getFileWildcard( shortFilename, fileWildcard ) ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.CheckingFile", info
            .getFile().toString() ) );
        }
        return true;
      }
      return false;
    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return info.getDepth() == 0 || includeSubfolders;
    }

    /**********************************************************
     *
     * @param selectedfile
     * @param wildcard
     * @return True if the selectedfile matches the wildcard
     **********************************************************/
    private boolean getFileWildcard( String selectedfile, String wildcard ) {
      Pattern pattern = null;
      boolean getIt = true;

      if ( !Utils.isEmpty( wildcard ) ) {
        pattern = Pattern.compile( wildcard );
        // First see if the file matches the regular expression!
        if ( pattern != null ) {
          Matcher matcher = pattern.matcher( selectedfile );
          getIt = matcher.matches();
        }
      }

      return getIt;
    }
  }

  public void setIncludeSubfolders( boolean includeSubfolders ) {
    this.includeSubfolders = includeSubfolders;
  }

  public void setargFromPrevious( boolean argFromPrevious ) {
    this.argFromPrevious = argFromPrevious;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isArgFromPrevious() {
    return argFromPrevious;
  }

  public String[] getArguments() {
    return arguments;
  }

  public String[] getFilemasks() {
    return filemasks;
  }

  public boolean isIncludeSubfolders() {
    return includeSubfolders;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean res = JobEntryValidatorUtils.andValidator().validate( this, "arguments", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    if ( res ) {
      ValidatorContext ctx = new ValidatorContext();
      AbstractFileValidator.putVariableSpace( ctx, getVariables() );
      AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );

      for ( int i = 0; i < arguments.length; i++ ) {
        JobEntryValidatorUtils.andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
      }
    }
  }

}
