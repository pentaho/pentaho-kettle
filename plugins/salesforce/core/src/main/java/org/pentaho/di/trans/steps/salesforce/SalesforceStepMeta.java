/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforce;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public abstract class SalesforceStepMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = SalesforceStepMeta.class;

  /** The Salesforce Target URL */
  @Injection( name = "SALESFORCE_URL" )
  private String targetURL;

  /** The userName */
  @Injection( name = "SALESFORCE_USERNAME" )
  private String username;

  /** The password */
  @Injection( name = "SALESFORCE_PASSWORD" )
  private String password;

  /** The time out */
  @Injection( name = "TIME_OUT" )
  private String timeout;

  /** The connection compression */
  @Injection( name = "USE_COMPRESSION" )
  private boolean compression;

  /** The Salesforce module */
  @Injection( name = "MODULE" )
  private String module;

  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "    " ).append( XMLHandler.addTagValue( "targeturl", getTargetURL() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "username", getUsername() ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( getPassword() ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "timeout", getTimeout() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useCompression", isCompression() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "module", getModule() ) );
    return retval.toString();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    setTargetURL( XMLHandler.getTagValue( stepnode, "targeturl" ) );
    setUsername( XMLHandler.getTagValue( stepnode, "username" ) );
    setPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) ) );
    setTimeout( XMLHandler.getTagValue( stepnode, "timeout" ) );
    setCompression( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useCompression" ) ) );
    setModule( XMLHandler.getTagValue( stepnode, "module" ) );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "targeturl", getTargetURL() );
    rep.saveStepAttribute( id_transformation, id_step, "username", getUsername() );
    rep.saveStepAttribute( id_transformation, id_step, "password",
      Encr.encryptPasswordIfNotUsingVariables( getPassword() ) );
    rep.saveStepAttribute( id_transformation, id_step, "timeout", getTimeout() );
    rep.saveStepAttribute( id_transformation, id_step, "useCompression", isCompression() );
    rep.saveStepAttribute( id_transformation, id_step, "module", getModule() );
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    setTargetURL( rep.getStepAttributeString( id_step, "targeturl" ) );
    setUsername( rep.getStepAttributeString( id_step, "username" ) );
    setPassword( Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) ) );
    setTimeout( rep.getStepAttributeString( id_step, "timeout" ) );
    setCompression( rep.getStepAttributeBoolean( id_step, "useCompression" ) );
    setModule( rep.getStepAttributeString( id_step, "module" ) );
  }

  public Object clone() {
    SalesforceStepMeta retval = (SalesforceStepMeta) super.clone();
    return retval;
  }

  public void setDefault() {
    setTargetURL( SalesforceConnectionUtils.TARGET_DEFAULT_URL );
    setUsername( "" );
    setPassword( "" );
    setTimeout( "60000" );
    setCompression( false );
    setModule( "Account" );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
      RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
      Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // check URL
    if ( Utils.isEmpty( getTargetURL() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceMeta.CheckResult.NoURL" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceMeta.CheckResult.URLOk" ), stepMeta );
    }
    remarks.add( cr );

    // check user name
    if ( Utils.isEmpty( getUsername() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceMeta.CheckResult.NoUsername" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceMeta.CheckResult.UsernameOk" ), stepMeta );
    }
    remarks.add( cr );

    // check module
    if ( Utils.isEmpty( getModule() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SalesforceMeta.CheckResult.NoModule" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SalesforceMeta.CheckResult.ModuleOk" ), stepMeta );
    }
    remarks.add( cr );
  }

  /**
   * @return Returns the Target URL.
   */
  public String getTargetURL() {
    return targetURL;
  }

  /**
   * @param targetURL
   *          The Target URL to set.
   */
  public void setTargetURL( String targetURL ) {
    this.targetURL = targetURL;
  }

  /**
   * @return Returns the UserName.
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          The Username to set.
   */
  public void setUsername( String username ) {
    this.username = username;
  }

  @Deprecated
  public String getUserName() {
    return getUsername();
  }

  @Deprecated
  public void setUserName( String username ) {
    setUsername( username );
  }

  /**
   * @return Returns the Password.
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
   * @return Returns the connection timeout.
   */
  public String getTimeout() {
    return timeout;
  }

  /**
   * @param timeOut
   *          The connection timeout to set.
   */
  public void setTimeout( String timeout ) {
    this.timeout = timeout;
  }

  @Deprecated
  public String getTimeOut() {
    return getTimeout();
  }

  @Deprecated
  public void setTimeOut( String timeOut ) {
    setTimeout( timeOut );
  }

  public boolean isCompression() {
    return compression;
  }

  public void setCompression( boolean compression ) {
    this.compression = compression;
  }

  /**
   * @return Returns the useCompression.
   */
  @Deprecated
  public boolean isUsingCompression() {
    return isCompression();
  }

  /**
   * @param useCompression
   *          The useCompression to set.
   */
  @Deprecated
  public void setUseCompression( boolean useCompression ) {
    setCompression( useCompression );
  }

  public String getModule() {
    return module;
  }

  public void setModule( String module ) {
    this.module = module;
  }
}
