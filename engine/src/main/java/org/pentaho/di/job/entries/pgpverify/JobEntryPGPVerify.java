/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.pgpverify;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.pgpencryptfiles.GPG;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a PGP verify job entry.
 *
 * @author Samatar
 * @since 25-02-2011
 *
 */

public class JobEntryPGPVerify extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryPGPVerify.class; // for i18n purposes, needed by Translator2!!

  private String gpglocation;
  private String filename;
  private String detachedfilename;
  private boolean useDetachedSignature;

  public JobEntryPGPVerify( String n ) {
    super( n, "" );
    gpglocation = null;
    filename = null;
    detachedfilename = null;
    useDetachedSignature = false;
  }

  public JobEntryPGPVerify() {
    this( "" );
  }

  public Object clone() {
    JobEntryPGPVerify je = (JobEntryPGPVerify) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "gpglocation", gpglocation ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "detachedfilename", detachedfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "useDetachedSignature", useDetachedSignature ) );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      gpglocation = XMLHandler.getTagValue( entrynode, "gpglocation" );
      filename = XMLHandler.getTagValue( entrynode, "filename" );
      detachedfilename = XMLHandler.getTagValue( entrynode, "detachedfilename" );
      useDetachedSignature = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "useDetachedSignature" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEntryPGPVerify.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      gpglocation = rep.getJobEntryAttributeString( id_jobentry, "gpglocation" );
      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );
      detachedfilename = rep.getJobEntryAttributeString( id_jobentry, "detachedfilename" );
      useDetachedSignature = rep.getJobEntryAttributeBoolean( id_jobentry, "useDetachedSignature" );
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryPGPVerify.ERROR_0002_Cannot_Load_Job_From_Repository", id_jobentry ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "gpglocation", gpglocation );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "detachedfilename", detachedfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "useDetachedSignature", useDetachedSignature );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryPGPVerify.ERROR_0003_Cannot_Save_Job_Entry", id_job ), dbe );
    }
  }

  public void setGPGLocation( String gpglocation ) {
    this.gpglocation = gpglocation;
  }

  public String getGPGLocation() {
    return gpglocation;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public void setDetachedfilename( String detachedfilename ) {
    this.detachedfilename = detachedfilename;
  }

  public String getDetachedfilename() {
    return detachedfilename;
  }

  public void setUseDetachedfilename( boolean useDetachedSignature ) {
    this.useDetachedSignature = useDetachedSignature;
  }

  public boolean useDetachedfilename() {
    return useDetachedSignature;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );
    result.setNrErrors( 1 );

    FileObject file = null;
    FileObject detachedSignature = null;
    try {

      String realFilename = environmentSubstitute( getFilename() );
      if ( Utils.isEmpty( realFilename ) ) {
        logError( BaseMessages.getString( PKG, "JobPGPVerify.FilenameMissing" ) );
        return result;
      }
      file = KettleVFS.getInstance( parentJobMeta.getBowl() ).getFileObject( realFilename );

      GPG gpg = new GPG( parentJobMeta.getBowl(), environmentSubstitute( getGPGLocation() ), log );

      if ( useDetachedfilename() ) {
        String signature = environmentSubstitute( getDetachedfilename() );

        if ( Utils.isEmpty( signature ) ) {
          logError( BaseMessages.getString( PKG, "JobPGPVerify.DetachedSignatureMissing" ) );
          return result;
        }
        detachedSignature = KettleVFS.getInstance( parentJobMeta.getBowl() ).getFileObject( signature );

        gpg.verifyDetachedSignature( detachedSignature, file );
      } else {
        gpg.verifySignature( file );
      }

      result.setNrErrors( 0 );
      result.setResult( true );

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobPGPVerify.Error" ), e );
    } finally {
      try {
        if ( file != null ) {
          file.close();
        }
        if ( detachedSignature != null ) {
          detachedSignature.close();
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( gpglocation ) ) {
      String realFileName = jobMeta.environmentSubstitute( gpglocation );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realFileName, ResourceType.FILE ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "gpglocation", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

  /**
   * Exports the object to a flat-file system, adding content with filename keys to a set of definitions. The supplied
   * resource naming interface allows the object to name appropriately without worrying about those parts of the
   * implementation specific details.
   *
   * @param executionBowl
   *          For file access
   * @param globalManagementBowl
   *          if needed for access to the current "global" (System or Repository) level config for export. If null, no
   *          global config will be exported.
   * @param space
   *          The variable space to resolve (environment) variables with.
   * @param definitions
   *          The map containing the filenames and content
   * @param namingInterface
   *          The resource naming interface allows the object to be named appropriately
   * @param repository
   *          The repository to load resources from
   * @param metaStore
   *          the metaStore to load external metadata from
   *
   * @return The filename for this object. (also contained in the definitions map)
   * @throws KettleException
   *           in case something goes wrong during the export
   */
  @Override
  public String exportResources( Bowl executionBowl, Bowl globalManagementBowl, VariableSpace space,
      Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface,
      Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the gpglocation from relative to absolute by grabbing the file object...
      // In case the name of the file comes from previous steps, forget about this!
      //
      if ( !Utils.isEmpty( gpglocation ) ) {
        // From : ${FOLDER}/../foo/bar.csv
        // To : /home/matt/test/files/foo/bar.csv
        //
        FileObject fileObject = KettleVFS.getInstance( parentJobMeta.getBowl() )
          .getFileObject( space.environmentSubstitute( gpglocation ), space );

        // If the file doesn't exist, forget about this effort too!
        //
        if ( fileObject.exists() ) {
          // Convert to an absolute path...
          //
          gpglocation = namingInterface.nameResource( fileObject, space, true );

          return gpglocation;
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }
}
