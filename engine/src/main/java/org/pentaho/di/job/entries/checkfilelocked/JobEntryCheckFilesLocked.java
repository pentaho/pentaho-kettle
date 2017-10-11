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

package org.pentaho.di.job.entries.checkfilelocked;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
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
    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", argFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", includeSubfolders ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "name", arguments[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "filemask", filemasks[i] ) );
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
      argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      includeSubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_subfolders" ) );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        arguments[i] = XMLHandler.getTagValue( fnode, "name" );
        filemasks[i] = XMLHandler.getTagValue( fnode, "filemask" );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.UnableToLoadFromXml" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      argFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      includeSubfolders = rep.getJobEntryAttributeBoolean( id_jobentry, "include_subfolders" );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "name" );
      arguments = new String[argnr];
      filemasks = new String[argnr];

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = rep.getJobEntryAttributeString( id_jobentry, a, "name" );
        filemasks[a] = rep.getJobEntryAttributeString( id_jobentry, a, "filemask" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryCheckFilesLocked.UnableToLoadFromRepo", String.valueOf( id_jobentry ) ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {

      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", argFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_subfolders", includeSubfolders );

      // save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "name", arguments[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "filemask", filemasks[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryCheckFilesLocked.UnableToSaveToRepo", String.valueOf( id_job ) ), dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) {

    Result result = previousResult;
    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    oneFileLocked = false;
    result.setResult( true );

    try {
      if ( argFromPrevious ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.FoundPreviousRows", String
            .valueOf( ( rows != null ? rows.size() : 0 ) ) ) );
        }
      }

      if ( argFromPrevious && rows != null ) {
        // Copy the input row to the (command line) arguments
        for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
          resultRow = rows.get( iteration );

          // Get values from previous result
          String filefolder_previous = resultRow.getString( 0, "" );
          String fmasks_previous = resultRow.getString( 1, "" );

          // ok we can process this file/folder
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingRow", filefolder_previous, fmasks_previous ) );
          }

          ProcessFile( filefolder_previous, fmasks_previous );
        }
      } else if ( arguments != null ) {

        for ( int i = 0; i < arguments.length && !parentJob.isStopped(); i++ ) {
          // ok we can process this file/folder
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingArg", arguments[i], filemasks[i] ) );
          }

          ProcessFile( arguments[i], filemasks[i] );
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

  private void ProcessFile( String filename, String wildcard ) {

    FileObject filefolder = null;
    String realFilefoldername = environmentSubstitute( filename );
    String realwilcard = environmentSubstitute( wildcard );

    try {
      filefolder = KettleVFS.getFileObject( realFilefoldername );
      FileObject[] files = new FileObject[] { filefolder };
      if ( filefolder.exists() ) {
        // the file or folder exists
        if ( filefolder.getType() == FileType.FOLDER ) {
          // It's a folder
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingFolder", realFilefoldername ) );
          }
          // Retrieve all files
          files = filefolder.findFiles( new TextFileSelector( filefolder.toString(), realwilcard ) );

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.TotalFilesToCheck", String
              .valueOf( files.length ) ) );
          }
        } else {
          // It's a file
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryCheckFilesLocked.ProcessingFile", realFilefoldername ) );
          }
        }
        // Check files locked
        checkFilesLocked( files );
      } else {
        // We can not find thsi file
        logBasic( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.FileNotExist", realFilefoldername ) );
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.CouldNotProcess", realFilefoldername, e
        .getMessage() ) );
    } finally {
      if ( filefolder != null ) {
        try {
          filefolder.close();
        } catch ( IOException ex ) {
          // Ignore
        }
      }
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
      FileObject file_name = null;
      try {

        if ( !info.getFile().toString().equals( sourceFolder ) ) {
          // Pass over the Base folder itself

          String short_filename = info.getFile().getName().getBaseName();

          if ( !info.getFile().getParent().equals( info.getBaseFolder() ) ) {

            // Not in the Base Folder..Only if include sub folders
            if ( includeSubfolders
              && ( info.getFile().getType() == FileType.FILE ) && GetFileWildcard( short_filename, fileWildcard ) ) {
              if ( isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.CheckingFile", info
                  .getFile().toString() ) );
              }

              returncode = true;

            }
          } else {
            // In the Base Folder...

            if ( ( info.getFile().getType() == FileType.FILE ) && GetFileWildcard( short_filename, fileWildcard ) ) {
              if ( isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobEntryCheckFilesLocked.CheckingFile", info
                  .getFile().toString() ) );
              }

              returncode = true;

            }
          }
        }

      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "JobCheckFilesLocked.Error.Exception.ProcessError" ), BaseMessages
          .getString( PKG, "JobCheckFilesLocked.Error.Exception.Process", info.getFile().toString(), e
            .getMessage() ) );
        returncode = false;
      } finally {
        if ( file_name != null ) {
          try {
            file_name.close();

          } catch ( IOException ex ) { /* Ignore */
          }
        }
      }

      return returncode;
    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return info.getDepth() == 0 || includeSubfolders;
    }
  }

  /**********************************************************
   *
   * @param selectedfile
   * @param wildcard
   * @return True if the selectedfile matches the wildcard
   **********************************************************/
  private boolean GetFileWildcard( String selectedfile, String wildcard ) {
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

    if ( res == false ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );

    for ( int i = 0; i < arguments.length; i++ ) {
      JobEntryValidatorUtils.andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
    }
  }

}
