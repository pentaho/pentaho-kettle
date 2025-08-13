/*
 * ! ******************************************************************************
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


package org.pentaho.di.trans.steps.ssh;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.ssh.SshImplementation;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.trilead.ssh2.Connection;

/*
 * Created on 03-Juin-2008
 *
 */

public class SSHMeta extends BaseStepMeta implements StepMetaInterface {
  static final Class<?> PKG = SSHMeta.class; // for i18n purposes, needed by Translator2!!
  private static final int DEFAULT_PORT = 22;

  private String command;
  private boolean dynamicCommandField;
  /** dynamic command fieldname */
  private String commandfieldname;

  private String serverName;
  private String port;
  private String userName;
  private String password;
  // key
  private boolean usePrivateKey;
  private String keyFileName;
  private String passPhrase;

  private String stdOutFieldName;
  private String stdErrFieldName;
  private String timeOut;
  // Proxy
  private String proxyHost;
  private String proxyPort;
  private String proxyUsername;
  private String proxyPassword;

  // SSH Implementation preference (null = auto-detect)
  private SshImplementation sshImplementation;

  public SSHMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    SSHMeta retval = (SSHMeta) super.clone();

    return retval;
  }

  @Override
  public void setDefault() {
    dynamicCommandField = false;
    command = null;
    commandfieldname = null;
    port = String.valueOf( DEFAULT_PORT );
    serverName = null;
    userName = null;
    password = null;
    usePrivateKey = true;
    keyFileName = null;
    stdOutFieldName = "stdOut";
    stdErrFieldName = "stdErr";
    timeOut = "0";
    proxyHost = null;
    proxyPort = null;
    proxyUsername = null;
    proxyPassword = null;
  }

  /**
   * @return Returns the serverName.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName The serverName to set.
   */
  public void setServerName( String serverName ) {
    this.serverName = serverName;
  }

  /**
   * @return Returns the userName.
   */
  public String getuserName() {
    return userName;
  }

  /**
   * @param userName The userName to set.
   */
  public void setuserName( String userName ) {
    this.userName = userName;
  }

  /**
   * @param password The password to set.
   */
  public void setpassword( String password ) {
    this.password = password;
  }

  /**
   * @return Returns the password.
   */
  public String getpassword() {
    return password;
  }

  /**
   * @param commandfieldname The commandfieldname to set.
   */
  public void setcommandfieldname( String commandfieldname ) {
    this.commandfieldname = commandfieldname;
  }

  /**
   * @return Returns the commandfieldname.
   */
  public String getcommandfieldname() {
    return commandfieldname;
  }

  /**
   * @param command The commandfieldname to set.
   */
  public void setCommand( String value ) {
    this.command = value;
  }

  /**
   * @return Returns the command.
   */
  public String getCommand() {
    return command;
  }

  /**
   * @param value The dynamicCommandField to set.
   */
  public void setDynamicCommand( boolean value ) {
    this.dynamicCommandField = value;
  }

  /**
   * @return Returns the dynamicCommandField.
   */
  public boolean isDynamicCommand() {
    return dynamicCommandField;
  }

  /**
   * @return Returns the port.
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port The port to set.
   */
  public void setPort( String port ) {
    this.port = port;
  }

  public void usePrivateKey( boolean value ) {
    this.usePrivateKey = value;
  }

  /**
   * @return Returns the usePrivateKey.
   */
  public boolean isusePrivateKey() {
    return usePrivateKey;
  }

  /**
   * @param value The keyFileName to set.
   */
  public void setKeyFileName( String value ) {
    this.keyFileName = value;
  }

  /**
   * @return Returns the keyFileName.
   */
  public String getKeyFileName() {
    return keyFileName;
  }

  /**
   * @param value The passPhrase to set.
   */
  public void setPassphrase( String value ) {
    this.passPhrase = value;
  }

  /**
   * @return Returns the passPhrase.
   */
  public String getPassphrase() {
    return passPhrase;
  }

  /**
   * @param timeOut The timeOut to set.
   */
  public void setTimeOut( String timeOut ) {
    this.timeOut = timeOut;
  }

  /**
   * @return Returns the timeOut.
   */
  public String getTimeOut() {
    return timeOut;
  }

  /**
   * @param value The stdOutFieldName to set.
   */
  public void setstdOutFieldName( String value ) {
    this.stdOutFieldName = value;
  }

  /**
   * @return Returns the stdOutFieldName.
   */
  public String getStdOutFieldName() {
    return stdOutFieldName;
  }

  /**
   * @param value The stdErrFieldName to set.
   */
  public void setStdErrFieldName( String value ) {
    this.stdErrFieldName = value;
  }

  /**
   * @return Returns the stdErrFieldName.
   */
  public String getStdErrFieldName() {
    return stdErrFieldName;
  }

  /**
   * @param value The proxyHost to set.
   */
  public void setProxyHost( String value ) {
    this.proxyHost = value;
  }

  /**
   * @return Returns the proxyHost.
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * @param value The proxyPort to set.
   */
  public void setProxyPort( String value ) {
    this.proxyPort = value;
  }

  /**
   * @return Returns the proxyPort.
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * @param value The proxyUsername to set.
   */
  public void setProxyUsername( String value ) {
    this.proxyUsername = value;
  }

  /**
   * @return Returns the proxyUsername.
   */
  public String getProxyUsername() {
    return proxyUsername;
  }

  /**
   * @param value The proxyPassword to set.
   */
  public void setProxyPassword( String value ) {
    this.proxyPassword = value;
  }

  /**
   * @return Returns the proxyPassword.
   */
  public String getProxyPassword() {
    return proxyPassword;
  }

  /**
   * @param implementation The SSH implementation to prefer (null for auto-detect).
   */
  public void setSshImplementation( SshImplementation implementation ) {
    this.sshImplementation = implementation;
  }

  /**
   * @return Returns the preferred SSH implementation (null for auto-detect).
   */
  public SshImplementation getSshImplementation() {
    return sshImplementation;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicCommandField", dynamicCommandField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "command", command ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "commandfieldname", commandfieldname ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "userName", userName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "usePrivateKey", usePrivateKey ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "keyFileName", keyFileName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "passPhrase", Encr.encryptPasswordIfNotUsingVariables( passPhrase ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "stdOutFieldName", stdOutFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "stdErrFieldName", stdErrFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "timeOut", timeOut ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "proxyHost", proxyHost ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "proxyPort", proxyPort ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "proxyUsername", proxyUsername ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "proxyPassword", Encr.encryptPasswordIfNotUsingVariables( proxyPassword ) ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "sshImplementation", sshImplementation != null ? sshImplementation.name() : null ) );
    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      dynamicCommandField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "dynamicCommandField" ) );
      command = XMLHandler.getTagValue( stepnode, "command" );
      commandfieldname = XMLHandler.getTagValue( stepnode, "commandfieldname" );
      port = XMLHandler.getTagValue( stepnode, "port" );
      serverName = XMLHandler.getTagValue( stepnode, "servername" );
      userName = XMLHandler.getTagValue( stepnode, "userName" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) );

      usePrivateKey = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usePrivateKey" ) );
      keyFileName = XMLHandler.getTagValue( stepnode, "keyFileName" );
      passPhrase =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "passPhrase" ) );
      stdOutFieldName = XMLHandler.getTagValue( stepnode, "stdOutFieldName" );
      stdErrFieldName = XMLHandler.getTagValue( stepnode, "stdErrFieldName" );
      timeOut = XMLHandler.getTagValue( stepnode, "timeOut" );
      proxyHost = XMLHandler.getTagValue( stepnode, "proxyHost" );
      proxyPort = XMLHandler.getTagValue( stepnode, "proxyPort" );
      proxyUsername = XMLHandler.getTagValue( stepnode, "proxyUsername" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "proxyPassword" ) );

      // SSH Implementation preference
      String sshImplStr = XMLHandler.getTagValue( stepnode, "sshImplementation" );
      if ( !Utils.isEmpty( sshImplStr ) ) {
        try {
          sshImplementation = SshImplementation.valueOf( sshImplStr );
        } catch ( IllegalArgumentException e ) {
          // Unknown implementation, use auto-detect
          sshImplementation = null;
        }
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "SSHMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases )
    throws KettleException {

    try {
      dynamicCommandField = rep.getStepAttributeBoolean( idStep, "dynamicCommandField" );
      command = rep.getStepAttributeString( idStep, "command" );
      commandfieldname = rep.getStepAttributeString( idStep, "commandfieldname" );
      serverName = rep.getStepAttributeString( idStep, "servername" );
      port = rep.getStepAttributeString( idStep, "port" );
      userName = rep.getStepAttributeString( idStep, "userName" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( idStep, "password" ) );

      usePrivateKey = rep.getStepAttributeBoolean( idStep, "usePrivateKey" );
      keyFileName = rep.getStepAttributeString( idStep, "keyFileName" );
      passPhrase =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( idStep, "passPhrase" ) );
      stdOutFieldName = rep.getStepAttributeString( idStep, "stdOutFieldName" );
      stdErrFieldName = rep.getStepAttributeString( idStep, "stdErrFieldName" );
      timeOut = rep.getStepAttributeString( idStep, "timeOut" );
      proxyHost = rep.getStepAttributeString( idStep, "proxyHost" );
      proxyPort = rep.getStepAttributeString( idStep, "proxyPort" );
      proxyUsername = rep.getStepAttributeString( idStep, "proxyUsername" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( idStep, "proxyPassword" ) );

      // SSH Implementation preference
      String sshImplStr = rep.getStepAttributeString( idStep, "sshImplementation" );
      if ( !Utils.isEmpty( sshImplStr ) ) {
        try {
          sshImplementation = SshImplementation.valueOf( sshImplStr );
        } catch ( IllegalArgumentException e ) {
          // Unknown implementation, use auto-detect
          sshImplementation = null;
        }
      }

    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "SSHMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep )
    throws KettleException {
    try {
      rep.saveStepAttribute( idTransformation, idStep, "dynamicCommandField", dynamicCommandField );
      rep.saveStepAttribute( idTransformation, idStep, "command", command );
      rep.saveStepAttribute( idTransformation, idStep, "commandfieldname", commandfieldname );
      rep.saveStepAttribute( idTransformation, idStep, "port", port );
      rep.saveStepAttribute( idTransformation, idStep, "servername", serverName );
      rep.saveStepAttribute( idTransformation, idStep, "userName", userName );
      rep.saveStepAttribute( idTransformation, idStep, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );

      rep.saveStepAttribute( idTransformation, idStep, "usePrivateKey", usePrivateKey );
      rep.saveStepAttribute( idTransformation, idStep, "keyFileName", keyFileName );
      rep.saveStepAttribute( idTransformation, idStep, "passPhrase", Encr
        .encryptPasswordIfNotUsingVariables( passPhrase ) );
      rep.saveStepAttribute( idTransformation, idStep, "stdOutFieldName", stdOutFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "stdErrFieldName", stdErrFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "timeOut", timeOut );
      rep.saveStepAttribute( idTransformation, idStep, "proxyHost", proxyHost );
      rep.saveStepAttribute( idTransformation, idStep, "proxyPort", proxyPort );
      rep.saveStepAttribute( idTransformation, idStep, "proxyUsername", proxyUsername );
      rep.saveStepAttribute( idTransformation, idStep, "proxyPassword", Encr
        .encryptPasswordIfNotUsingVariables( proxyPassword ) );
      rep.saveStepAttribute( idTransformation, idStep, "sshImplementation",
        sshImplementation != null ? sshImplementation.name() : null );

    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "SSHMeta.Exception.UnableToSaveStepInfo" ) + idStep, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     Repository repository, IMetaStore metaStore ) {

    CheckResult cr;
    String errorMessage = "";

    // Target hostname
    if ( Utils.isEmpty( getServerName() ) ) {
      errorMessage = BaseMessages.getString( PKG, "SSHMeta.CheckResult.TargetHostMissing" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
      remarks.add( cr );
    } else {
      errorMessage = BaseMessages.getString( PKG, "SSHMeta.CheckResult.TargetHostOK" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, errorMessage, stepMeta );
      remarks.add( cr );
    }
    if ( isusePrivateKey() ) {
      String keyfilename = transMeta.environmentSubstitute( getKeyFileName() );
      if ( Utils.isEmpty( keyfilename ) ) {
        errorMessage = BaseMessages.getString( PKG, "SSHMeta.CheckResult.PrivateKeyFileNameMissing" );
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
        remarks.add( cr );
      } else {
        errorMessage = BaseMessages.getString( PKG, "SSHMeta.CheckResult.PrivateKeyFileNameOK" );
        cr = new CheckResult( CheckResult.TYPE_RESULT_OK, errorMessage, stepMeta );
        remarks.add( cr );
        boolean keyFileExists = false;
        try {
          keyFileExists = KettleVFS.getInstance( transMeta.getBowl() ).fileExists( keyfilename );
        } catch ( Exception e ) { /* Ignore */
        }
        if ( !keyFileExists ) {
          errorMessage = BaseMessages.getString( PKG, "SSHMeta.CheckResult.PrivateKeyFileNotExist", keyfilename );
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
          remarks.add( cr );
        } else {
          errorMessage = BaseMessages.getString( PKG, "SSHMeta.CheckResult.PrivateKeyFileExists", keyfilename );
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, errorMessage, stepMeta );
          remarks.add( cr );
        }
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SSHMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SSHMeta.CheckResult.NoInpuReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    if ( !isDynamicCommand() ) {
      row.clear();
    }
    ValueMetaInterface v =
      new ValueMetaString( space.environmentSubstitute( getStdOutFieldName() ) );
    v.setOrigin( name );
    row.addValueMeta( v );

    String stderrfield = space.environmentSubstitute( getStdErrFieldName() );
    if ( !Utils.isEmpty( stderrfield ) ) {
      v = new ValueMetaBoolean( stderrfield );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                TransMeta transMeta, Trans trans ) {
    return new SSH( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new SSHData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   *
   * @param bowl
   * @param serveur
   * @param port
   * @param username
   * @param password
   * @param useKey
   * @param keyFilename
   * @param passPhrase
   * @param timeOut
   * @param space
   * @param proxyhost
   * @param proxyport
   * @param proxyusername
   * @param proxypassword
   * @return
   * @throws KettleException
   * @deprecated Use
   *             {@link SSHData#OpenConnection(String, int, String, String, boolean, String, String, int, VariableSpace, String, int, String, String)}
   *             instead
   */
  @Deprecated
  public static Connection OpenConnection( Bowl bowl, String serveur, int port, String username, String password,
                                           boolean useKey, String keyFilename, String passPhrase, int timeOut,
                                           VariableSpace space, String proxyhost,
                                           int proxyport, String proxyusername, String proxypassword )
    throws KettleException {
    return SSHData.OpenConnection( bowl, serveur, port, username,
      password, useKey, keyFilename, passPhrase, timeOut, space, proxyhost, proxyport, proxyusername, proxypassword );
  }

  /**
   * Returns the Input/Output metadata for this step.
   *
   */
  @Override
  public StepIOMetaInterface getStepIOMeta() {
    return new StepIOMeta( isDynamicCommand(), true, false, false, false, false );
  }
}
