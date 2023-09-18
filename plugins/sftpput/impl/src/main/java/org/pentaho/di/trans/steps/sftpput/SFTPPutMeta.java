/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.sftpput;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.sftpput.JobEntrySFTPPUT;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Send file to SFTP host.
 *
 * @author Samatar Hassan
 * @since 30-April-2012
 */
@Step( id = "SFTPPut", name = "BaseStep.TypeLongDesc.SFTPPut",
        description = "BaseStep.TypeTooltipDesc.SFTPPut",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental",
        image = "SFP.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/SFTP+Put",
        i18nPackageName = "org.pentaho.di.trans.steps.sftpput" )
public class SFTPPutMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SFTPPutMeta.class; // for i18n purposes, needed by Translator2!!

  private String serverName;
  private String serverPort;
  private String userName;
  private String password;
  private String sourceFileFieldName;
  private String remoteDirectoryFieldName;
  private boolean addFilenameResut;
  private boolean inputIsStream;
  private boolean usekeyfilename;
  private String keyfilename;
  private String keyfilepass;
  private String compression;
  private boolean createRemoteFolder;
  // proxy
  private String proxyType;
  private String proxyHost;
  private String proxyPort;
  private String proxyUsername;
  private String proxyPassword;

  private String destinationfolderFieldName;
  private boolean createDestinationFolder;
  private int afterFTPS;
  private String remoteFilenameFieldName;

  public SFTPPutMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      serverName = XMLHandler.getTagValue( stepnode, "servername" );
      serverPort = XMLHandler.getTagValue( stepnode, "serverport" );
      userName = XMLHandler.getTagValue( stepnode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) );
      sourceFileFieldName = XMLHandler.getTagValue( stepnode, "sourceFileFieldName" );
      remoteDirectoryFieldName = XMLHandler.getTagValue( stepnode, "remoteDirectoryFieldName" );

      inputIsStream = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "inputIsStream" ) );
      addFilenameResut = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addFilenameResut" ) );

      usekeyfilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usekeyfilename" ) );
      keyfilename = XMLHandler.getTagValue( stepnode, "keyfilename" );
      keyfilepass = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "keyfilepass" ) );
      compression = XMLHandler.getTagValue( stepnode, "compression" );
      proxyType = XMLHandler.getTagValue( stepnode, "proxyType" );
      proxyHost = XMLHandler.getTagValue( stepnode, "proxyHost" );
      proxyPort = XMLHandler.getTagValue( stepnode, "proxyPort" );
      proxyUsername = XMLHandler.getTagValue( stepnode, "proxyUsername" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "proxyPassword" ) );

      createRemoteFolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "createRemoteFolder" ) );

      boolean remove = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "remove" ) );
      setAfterFTPS( JobEntrySFTPPUT.getAfterSFTPPutByCode( Const.NVL( XMLHandler.getTagValue(
        stepnode, "aftersftpput" ), "" ) ) );
      if ( remove && getAfterFTPS() == JobEntrySFTPPUT.AFTER_FTPSPUT_NOTHING ) {
        setAfterFTPS( JobEntrySFTPPUT.AFTER_FTPSPUT_DELETE );
      }
      destinationfolderFieldName = XMLHandler.getTagValue( stepnode, "destinationfolderFieldName" );
      createDestinationFolder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "createdestinationfolder" ) );
      remoteFilenameFieldName = XMLHandler.getTagValue( stepnode, "remoteFilenameFieldName" );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    serverName = null;
    serverPort = "22";
    inputIsStream = false;
    addFilenameResut = false;
    usekeyfilename = false;
    keyfilename = null;
    keyfilepass = null;
    compression = "none";
    proxyType = null;
    proxyHost = null;
    proxyPort = null;
    proxyUsername = null;
    proxyPassword = null;
    createRemoteFolder = false;
    afterFTPS = JobEntrySFTPPUT.AFTER_FTPSPUT_NOTHING;
    destinationfolderFieldName = null;
    createDestinationFolder = false;
    remoteFilenameFieldName = null;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "serverport", serverPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sourceFileFieldName", sourceFileFieldName ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "remoteDirectoryFieldName", remoteDirectoryFieldName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "inputIsStream", inputIsStream ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addFilenameResut", addFilenameResut ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usekeyfilename", usekeyfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "keyfilename", keyfilename ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "keyfilepass", Encr.encryptPasswordIfNotUsingVariables( keyfilepass ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compression", compression ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyType", proxyType ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyHost", proxyHost ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyPort", proxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyUsername", proxyUsername ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "proxyPassword", Encr.encryptPasswordIfNotUsingVariables( proxyPassword ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "createRemoteFolder", createRemoteFolder ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "aftersftpput", JobEntrySFTPPUT.getAfterSFTPPutCode( getAfterFTPS() ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "destinationfolderFieldName", destinationfolderFieldName ) );
    retval
      .append( "      " ).append( XMLHandler.addTagValue( "createdestinationfolder", createDestinationFolder ) );
    retval
      .append( "      " ).append( XMLHandler.addTagValue( "remoteFilenameFieldName", remoteFilenameFieldName ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      serverName = rep.getStepAttributeString( id_step, "servername" );
      serverPort = rep.getStepAttributeString( id_step, "serverport" );

      userName = rep.getStepAttributeString( id_step, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) );
      sourceFileFieldName = rep.getStepAttributeString( id_step, "sourceFileFieldName" );
      remoteDirectoryFieldName = rep.getStepAttributeString( id_step, "remoteDirectoryFieldName" );
      inputIsStream = rep.getStepAttributeBoolean( id_step, "inputIsStream" );
      addFilenameResut = rep.getStepAttributeBoolean( id_step, "addFilenameResut" );

      usekeyfilename = rep.getStepAttributeBoolean( id_step, "usekeyfilename" );
      keyfilename = rep.getStepAttributeString( id_step, "keyfilename" );
      keyfilepass = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "keyfilepass" ) );
      compression = rep.getStepAttributeString( id_step, "compression" );
      proxyType = rep.getStepAttributeString( id_step, "proxyType" );
      proxyHost = rep.getStepAttributeString( id_step, "proxyHost" );
      proxyPort = rep.getStepAttributeString( id_step, "proxyPort" );
      proxyUsername = rep.getStepAttributeString( id_step, "proxyUsername" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "proxyPassword" ) );

      createRemoteFolder = rep.getStepAttributeBoolean( id_step, "createRemoteFolder" );

      boolean remove = rep.getStepAttributeBoolean( id_step, "remove" );
      setAfterFTPS( JobEntrySFTPPUT.getAfterSFTPPutByCode( Const.NVL( rep.getStepAttributeString(
        id_step, "aftersftpput" ), "" ) ) );
      if ( remove && getAfterFTPS() == JobEntrySFTPPUT.AFTER_FTPSPUT_NOTHING ) {
        setAfterFTPS( JobEntrySFTPPUT.AFTER_FTPSPUT_DELETE );
      }
      destinationfolderFieldName = rep.getStepAttributeString( id_step, "destinationfolderFieldName" );
      createDestinationFolder = rep.getStepAttributeBoolean( id_step, "createdestinationfolder" );
      remoteFilenameFieldName = rep.getStepAttributeString( id_step, "remoteFilenameFieldName" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "servername", serverName );
      rep.saveStepAttribute( id_transformation, id_step, "serverport", serverPort );
      rep.saveStepAttribute( id_transformation, id_step, "username", userName );
      rep.saveStepAttribute( id_transformation, id_step, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveStepAttribute( id_transformation, id_step, "sourceFileFieldName", sourceFileFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "remoteDirectoryFieldName", remoteDirectoryFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "inputIsStream", inputIsStream );
      rep.saveStepAttribute( id_transformation, id_step, "addFilenameResut", addFilenameResut );

      rep.saveStepAttribute( id_transformation, id_step, "usekeyfilename", usekeyfilename );
      rep.saveStepAttribute( id_transformation, id_step, "keyfilename", keyfilename );
      rep.saveStepAttribute( id_transformation, id_step, "keyfilepass", Encr
        .encryptPasswordIfNotUsingVariables( keyfilepass ) );
      rep.saveStepAttribute( id_transformation, id_step, "compression", compression );
      rep.saveStepAttribute( id_transformation, id_step, "proxyType", proxyType );
      rep.saveStepAttribute( id_transformation, id_step, "proxyHost", proxyHost );
      rep.saveStepAttribute( id_transformation, id_step, "proxyPort", proxyPort );
      rep.saveStepAttribute( id_transformation, id_step, "proxyUsername", proxyUsername );
      rep.saveStepAttribute( id_transformation, id_step, "proxyPassword", Encr
        .encryptPasswordIfNotUsingVariables( proxyPassword ) );
      rep.saveStepAttribute( id_transformation, id_step, "aftersftpput", JobEntrySFTPPUT
        .getAfterSFTPPutCode( getAfterFTPS() ) );

      rep.saveStepAttribute( id_transformation, id_step, "createRemoteFolder", createRemoteFolder );
      rep.saveStepAttribute( id_transformation, id_step, "destinationfolderFieldName", destinationfolderFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "createdestinationfolder", createDestinationFolder );
      rep.saveStepAttribute( id_transformation, id_step, "remoteFilenameFieldName", remoteFilenameFieldName );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "SFTPPutMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SFTPPutMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SFTPPutMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SFTPPutMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new SFTPPut( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new SFTPPutData();
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  /**
   * @return Returns the serverName.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName
   *          The serverName to set.
   */
  public void setServerName( String serverName ) {
    this.serverName = serverName;
  }

  /**
   * @return Returns the userName.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName
   *          The userName to set.
   */
  public void setUserName( String userName ) {
    this.userName = userName;
  }

  /**
   * @return Returns the afterFTPS.
   */
  public int getAfterFTPS() {
    return afterFTPS;
  }

  /**
   * @param value
   *          The afterFTPS to set.
   */
  public void setAfterFTPS( int value ) {
    this.afterFTPS = value;
  }

  public String getServerPort() {
    return serverPort;
  }

  public void setServerPort( String serverPort ) {
    this.serverPort = serverPort;
  }

  public boolean isUseKeyFile() {
    return usekeyfilename;
  }

  public void setUseKeyFile( boolean value ) {
    this.usekeyfilename = value;
  }

  public String getKeyFilename() {
    return keyfilename;
  }

  public void setKeyFilename( String value ) {
    this.keyfilename = value;
  }

  public String getKeyPassPhrase() {
    return keyfilepass;
  }

  public void setKeyPassPhrase( String value ) {
    this.keyfilepass = value;
  }

  /**
   * @return Returns the compression.
   */
  public String getCompression() {
    return compression;
  }

  /**
   * @param compression
   *          The compression to set.
   */
  public void setCompression( String compression ) {
    this.compression = compression;
  }

  public String getProxyType() {
    return proxyType;
  }

  public void setProxyType( String value ) {
    this.proxyType = value;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost( String value ) {
    this.proxyHost = value;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort( String value ) {
    this.proxyPort = value;
  }

  public String getProxyUsername() {
    return proxyUsername;
  }

  public void setProxyUsername( String value ) {
    this.proxyUsername = value;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword( String value ) {
    this.proxyPassword = value;
  }

  public boolean isCreateRemoteFolder() {
    return this.createRemoteFolder;
  }

  public void setCreateRemoteFolder( boolean value ) {
    this.createRemoteFolder = value;
  }

  public boolean isAddFilenameResut() {
    return addFilenameResut;
  }

  public void setAddFilenameResut( boolean addFilenameResut ) {
    this.addFilenameResut = addFilenameResut;
  }

  public boolean isInputStream() {
    return inputIsStream;
  }

  public void setInputStream( boolean value ) {
    this.inputIsStream = value;
  }

  /**
   * @param createDestinationFolder
   *          The create destination folder flag to set.
   */
  public void setCreateDestinationFolder( boolean createDestinationFolder ) {
    this.createDestinationFolder = createDestinationFolder;
  }

  /**
   * @return Returns the create destination folder flag
   */
  public boolean isCreateDestinationFolder() {
    return createDestinationFolder;
  }

  public String getRemoteDirectoryFieldName() {
    return remoteDirectoryFieldName;
  }

  public void setRemoteDirectoryFieldName( String value ) {
    this.remoteDirectoryFieldName = value;
  }

  public void setSourceFileFieldName( String value ) {
    this.sourceFileFieldName = value;
  }

  public String getSourceFileFieldName() {
    return sourceFileFieldName;
  }

  public void setDestinationFolderFieldName( String value ) {
    this.destinationfolderFieldName = value;
  }

  public String getDestinationFolderFieldName() {
    return destinationfolderFieldName;
  }

  public String getRemoteFilenameFieldName() {
    return remoteFilenameFieldName;
  }

  public void setRemoteFilenameFieldName( String value ) {
    remoteFilenameFieldName = value;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}
