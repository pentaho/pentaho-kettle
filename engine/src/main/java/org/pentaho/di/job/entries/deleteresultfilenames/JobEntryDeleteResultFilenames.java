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

package org.pentaho.di.job.entries.deleteresultfilenames;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'deleteresultfilenames' job entry. Its main use would be to create empty folder that can be used to
 * control the flow in ETL cycles.
 *
 * @author Samatar
 * @since 26-10-2007
 *
 */
public class JobEntryDeleteResultFilenames extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryDeleteResultFilenames.class; // for i18n purposes, needed by Translator2!!

  private String foldername;
  private boolean specifywildcard;
  private String wildcard;
  private String wildcardexclude;

  public JobEntryDeleteResultFilenames( String n ) {
    super( n, "" );
    foldername = null;
    wildcardexclude = null;
    wildcard = null;
    specifywildcard = false;
  }

  public JobEntryDeleteResultFilenames() {
    this( "" );
  }

  public Object clone() {
    JobEntryDeleteResultFilenames je = (JobEntryDeleteResultFilenames) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 ); // 75 chars in just tag names and spaces

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "foldername", foldername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "specify_wildcard", specifywildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcardexclude", wildcardexclude ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      foldername = XMLHandler.getTagValue( entrynode, "foldername" );
      specifywildcard = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "specify_wildcard" ) );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      wildcardexclude = XMLHandler.getTagValue( entrynode, "wildcardexclude" );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEntryDeleteResultFilenames.CanNotLoadFromXML", xe.getMessage() ) );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      foldername = rep.getJobEntryAttributeString( id_jobentry, "foldername" );
      specifywildcard = rep.getJobEntryAttributeBoolean( id_jobentry, "specify_wildcard" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      wildcardexclude = rep.getJobEntryAttributeString( id_jobentry, "wildcardexclude" );
    } catch ( KettleException dbe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEntryDeleteResultFilenames.CanNotLoadFromRep", "" + id_jobentry, dbe.getMessage() ) );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "foldername", foldername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "specify_wildcard", specifywildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcardexclude", wildcardexclude );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEntryDeleteResultFilenames.CanNotSaveToRep", "" + id_job, dbe.getMessage() ) );
    }
  }

  public void setSpecifyWildcard( boolean specifywildcard ) {
    this.specifywildcard = specifywildcard;
  }

  public boolean isSpecifyWildcard() {
    return specifywildcard;
  }

  public void setFoldername( String foldername ) {
    this.foldername = foldername;
  }

  public String getFoldername() {
    return foldername;
  }

  public String getWildcard() {
    return wildcard;
  }

  public String getWildcardExclude() {
    return wildcardexclude;
  }

  public String getRealWildcard() {
    return environmentSubstitute( getWildcard() );
  }

  public void setWildcard( String wildcard ) {
    this.wildcard = wildcard;
  }

  public void setWildcardExclude( String wildcardexclude ) {
    this.wildcardexclude = wildcardexclude;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    if ( previousResult != null ) {
      try {
        int size = previousResult.getResultFiles().size();
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "JobEntryDeleteResultFilenames.log.FilesFound", "" + size ) );
        }
        if ( !specifywildcard ) {
          // Delete all files
          previousResult.getResultFiles().clear();
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteResultFilenames.log.DeletedFiles", "" + size ) );
          }
        } else {

          List<ResultFile> resultFiles = result.getResultFilesList();
          if ( resultFiles != null && resultFiles.size() > 0 ) {
            for ( Iterator<ResultFile> it = resultFiles.iterator(); it.hasNext() && !parentJob.isStopped(); ) {
              ResultFile resultFile = it.next();
              FileObject file = resultFile.getFile();
              if ( file != null && file.exists() ) {
                if ( CheckFileWildcard( file.getName().getBaseName(), environmentSubstitute( wildcard ), true )
                  && !CheckFileWildcard(
                    file.getName().getBaseName(), environmentSubstitute( wildcardexclude ), false ) ) {
                  // Remove file from result files list
                  result.getResultFiles().remove( resultFile.getFile().toString() );

                  if ( log.isDetailed() ) {
                    logDetailed( BaseMessages.getString(
                      PKG, "JobEntryDeleteResultFilenames.log.DeletedFile", file.toString() ) );
                  }
                }

              }
            }
          }
        }
        result.setResult( true );
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "JobEntryDeleteResultFilenames.Error", e.toString() ) );
      }
    }
    return result;
  }

  /**********************************************************
   *
   * @param selectedfile
   * @param wildcard
   * @return True if the selectedfile matches the wildcard
   **********************************************************/
  private boolean CheckFileWildcard( String selectedfile, String wildcard, boolean include ) {
    Pattern pattern = null;
    boolean getIt = include;

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

  public boolean evaluates() {
    return true;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(), JobEntryValidatorUtils.fileDoesNotExistValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks, ctx );
  }

}
