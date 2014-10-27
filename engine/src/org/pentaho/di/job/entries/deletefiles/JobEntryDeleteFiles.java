/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.deletefiles;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
import org.pentaho.di.job.Job;
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

/**
 * This defines a 'delete files' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryDeleteFiles.class; // for i18n purposes, needed by Translator2!!

  public boolean argFromPrevious;

  public boolean includeSubfolders;

  public String[] arguments;

  public String[] filemasks;

  public JobEntryDeleteFiles( String n ) {
    super( n, "" );
    argFromPrevious = false;
    arguments = null;

    includeSubfolders = false;
  }

  public JobEntryDeleteFiles() {
    this( "" );
  }

  public Object clone() {
    JobEntryDeleteFiles je = (JobEntryDeleteFiles) super.clone();
    return je;
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
      arguments = new String[nrFields];
      filemasks = new String[nrFields];

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        arguments[i] = XMLHandler.getTagValue( fnode, "name" );
        filemasks[i] = XMLHandler.getTagValue( fnode, "filemask" );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryDeleteFiles.UnableToLoadFromXml" ), xe );
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
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryDeleteFiles.UnableToLoadFromRepo", String
        .valueOf( id_jobentry ) ), dbe );
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
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryDeleteFiles.UnableToSaveToRepo", String
        .valueOf( id_job ) ), dbe );
    }
  }

  public Result execute( Result result, int nr ) throws KettleException {
    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    int NrErrFiles = 0;

    result.setResult( false );
    result.setNrErrors( 1 );

    if ( argFromPrevious ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.FoundPreviousRows", String
          .valueOf( ( rows != null ? rows.size() : 0 ) ) ) );
      }
    }

    if ( argFromPrevious && rows != null ) // Copy the input row to the (command line) arguments
    {
      for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
        resultRow = rows.get( iteration );

        String args_previous = resultRow.getString( 0, null );
        String fmasks_previous = resultRow.getString( 1, null );

        // ok we can process this file/folder
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEntryDeleteFiles.ProcessingRow", args_previous, fmasks_previous ) );
        }

        if ( !ProcessFile( args_previous, fmasks_previous, parentJob ) ) {
          NrErrFiles++;
        }
      }
    } else if ( arguments != null ) {

      for ( int i = 0; i < arguments.length && !parentJob.isStopped(); i++ ) {

        // ok we can process this file/folder
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEntryDeleteFiles.ProcessingArg", arguments[i], filemasks[i] ) );
        }
        if ( !ProcessFile( arguments[i], filemasks[i], parentJob ) ) {
          NrErrFiles++;
        }
      }
    }

    if ( NrErrFiles == 0 ) {
      result.setResult( true );
      result.setNrErrors( 0 );
    } else {
      result.setNrErrors( NrErrFiles );
      result.setResult( false );
    }

    return result;
  }

  private boolean ProcessFile( String filename, String wildcard, Job parentJob ) {
    boolean rcode = false;
    FileObject filefolder = null;
    String realFilefoldername = environmentSubstitute( filename );
    String realwildcard = environmentSubstitute( wildcard );

    try {
      filefolder = KettleVFS.getFileObject( realFilefoldername, this );

      if ( filefolder.exists() ) {
        // the file or folder exists
        if ( filefolder.getType() == FileType.FOLDER ) {
          // It's a folder
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.ProcessingFolder", realFilefoldername ) );
            // Delete Files
          }

          int Nr = filefolder.delete( new TextFileSelector( filefolder.toString(), realwildcard, parentJob ) );

          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.TotalDeleted", String.valueOf( Nr ) ) );
          }
          rcode = true;
        } else {
          // It's a file
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.ProcessingFile", realFilefoldername ) );
          }
          boolean deleted = filefolder.delete();
          if ( !deleted ) {
            logError( BaseMessages.getString( PKG, "JobEntryDeleteFiles.CouldNotDeleteFile", realFilefoldername ) );
          } else {
            if ( log.isBasic() ) {
              logBasic( BaseMessages.getString( PKG, "JobEntryDeleteFiles.FileDeleted", filename ) );
            }
            rcode = true;
          }
        }
      } else {
        // File already deleted, no reason to try to delete it
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "JobEntryDeleteFiles.FileAlreadyDeleted", realFilefoldername ) );
        }
        rcode = true;
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntryDeleteFiles.CouldNotProcess", realFilefoldername, e
        .getMessage() ), e );
    } finally {
      if ( filefolder != null ) {
        try {
          filefolder.close();
        } catch ( IOException ex ) {
          // Ignore
        }
      }
    }

    return rcode;
  }

  private class TextFileSelector implements FileSelector {
    String fileWildcard = null;
    String sourceFolder = null;
    Job parentjob;

    public TextFileSelector( String sourcefolderin, String filewildcard, Job parentJob ) {

      if ( !Const.isEmpty( sourcefolderin ) ) {
        sourceFolder = sourcefolderin;
      }

      if ( !Const.isEmpty( filewildcard ) ) {
        fileWildcard = filewildcard;
      }
      parentjob = parentJob;
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean returncode = false;
      try {

        if ( !info.getFile().toString().equals( sourceFolder ) && !parentjob.isStopped() ) {
          // Pass over the Base folder itself

          String short_filename = info.getFile().getName().getBaseName();

          if ( !info.getFile().getParent().equals( info.getBaseFolder() ) ) {

            // Not in the Base Folder..Only if include sub folders
            if ( includeSubfolders
              && ( info.getFile().getType() == FileType.FILE ) && GetFileWildcard( short_filename, fileWildcard ) ) {
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.DeletingFile", info
                  .getFile().toString() ) );
              }

              returncode = true;

            }
          } else {
            // In the Base Folder...

            if ( ( info.getFile().getType() == FileType.FILE ) && GetFileWildcard( short_filename, fileWildcard ) ) {
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.DeletingFile", info
                  .getFile().toString() ) );
              }

              returncode = true;

            }

          }

        }

      } catch ( Exception e ) {

        log.logError(
          BaseMessages.getString( PKG, "JobDeleteFiles.Error.Exception.DeleteProcessError" ), BaseMessages
            .getString( PKG, "JobDeleteFiles.Error.Exception.DeleteProcess", info.getFile().toString(), e
              .getMessage() ) );

        returncode = false;
      }

      return returncode;
    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return true;
    }
  }

  /**********************************************************
   *
   * @param selectedfile
   * @param wildcard
   * @return True if the selectedfile matches the wildcard
   **********************************************************/
  private boolean GetFileWildcard( String selectedfile, String wildcard ) {
    boolean getIt = true;

    if ( !Const.isEmpty( wildcard ) ) {
      Pattern pattern = Pattern.compile( wildcard );
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

  public void setPrevious( boolean argFromPrevious ) {
    this.argFromPrevious = argFromPrevious;
  }

  public boolean evaluates() {
    return true;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean res = andValidator().validate( this, "arguments", remarks, putValidators( notNullValidator() ) );

    if ( !res ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace( ctx, getVariables() );
    putValidators( ctx, notNullValidator(), fileExistsValidator() );

    for ( int i = 0; i < arguments.length; i++ ) {
      andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
    }
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( arguments != null ) {
      ResourceReference reference = null;
      for ( int i = 0; i < arguments.length; i++ ) {
        String filename = jobMeta.environmentSubstitute( arguments[i] );
        if ( reference == null ) {
          reference = new ResourceReference( this );
          references.add( reference );
        }
        reference.getEntries().add( new ResourceEntry( filename, ResourceType.FILE ) );
      }
    }
    return references;
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

}
