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


package org.pentaho.amazon.s3;

import java.util.List;

import mondrian.olap.Util;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step( id = "S3FileOutputPlugin", image = "S3O.svg", name = "S3FileOutput.Name",
    description = "S3FileOutput.Description",
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/s3-file-output",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
    i18nPackageName = "org.pentaho.amazon.s3" )
@InjectionSupported( localizationPrefix = "S3FileOutput.Injection.", groups = { "OUTPUT_FIELDS" } )
public class S3FileOutputMeta extends TextFileOutputMeta {

  private static final String ACCESS_KEY_TAG = "access_key";
  private static final String SECRET_KEY_TAG = "secret_key";
  private static final String FILE_TAG = "file";
  private static final String NAME_TAG = "name";


  private String accessKey = null;

  private String secretKey = null;

  public String getAccessKey() {
    return accessKey;
  }
  public void setAccessKey( String accessKey ) {
    this.accessKey = accessKey;
  }
  public String getSecretKey() {
    return secretKey;
  }
  public void setSecretKey( String secretKey ) {
    this.secretKey = secretKey;
  }

  @Override
  public void setDefault() {
    // call the base classes method
    super.setDefault();

    // now set the default for the
    // filename to an empty string
    setFileName( "s3n://" );
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer( 1000 );
    retval.append( super.getXML() );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( ACCESS_KEY_TAG, Encr.encryptPasswordIfNotUsingVariables( accessKey ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( SECRET_KEY_TAG, Encr.encryptPasswordIfNotUsingVariables( secretKey ) ) );
    return retval.toString();
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      super.saveRep( rep, metaStore, id_transformation, id_step );
      rep.saveStepAttribute( id_transformation, id_step, ACCESS_KEY_TAG, Encr
        .encryptPasswordIfNotUsingVariables( accessKey ) );
      rep.saveStepAttribute( id_transformation, id_step, SECRET_KEY_TAG, Encr
        .encryptPasswordIfNotUsingVariables( secretKey ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      super.readRep( rep, metaStore, id_step, databases );
      updateForRetroCompatibility();
      setAccessKey( Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, ACCESS_KEY_TAG ) ) );
      setSecretKey( Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, SECRET_KEY_TAG ) ) );
      processFilename( fileName );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public void readData( Node stepnode ) throws KettleXMLException {
    try {
      super.readData( stepnode );
      updateForRetroCompatibility();
      accessKey = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, ACCESS_KEY_TAG ) );
      secretKey = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, SECRET_KEY_TAG ) );
      processFilename( fileName );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  private void updateForRetroCompatibility() {
    if ( System.getProperty( Const.KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI, "N" ).equals( "Y" ) ) {
      getLog().logDebug( "[KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI] flag is enable" );
      if ( fileName.startsWith( "s3://s3/" ) ) {
        updateFilenameForRetroCompatibility( this.fileName.replace( "s3://s3/", "s3://" ) );
      } else if ( fileName.startsWith( "s3n://s3n/" ) ) {
        updateFilenameForRetroCompatibility( this.fileName.replace( "s3n://s3n/", "s3n://" ) );
      } else if ( fileName.startsWith( "s3a://s3a/" ) ) {
        updateFilenameForRetroCompatibility( this.fileName.replace( "s3a://s3a/", "s3a://" ) );
      }
    }
  }

  private void updateFilenameForRetroCompatibility( String newFilename ) {
    getLog().logDebug( "[KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI] Filename was updated from {} to {}", this.fileName, newFilename );
    this.fileName = newFilename;
  }
  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new S3FileOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  /**
   * New filenames obey the rule s3://<any_string>/<s3_bucket_name>/<path>. However, we maintain old filenames
   * s3://<access_key>:<secret_key>@s3/<s3_bucket_name>/<path>
   *
   * @param filename
   * @return
   */
  protected void processFilename( String filename ) throws Exception {
    if ( Util.isEmpty( filename ) ) {
      filename = "s3n://";
    }
    setFileName( filename );
  }

  protected String decodeAccessKey( String key ) {
    if ( Const.isEmpty( key ) ) {
      return key;
    }
    return key.replaceAll( "%2B", "\\+" ).replaceAll( "%2F", "/" );
  }

}
