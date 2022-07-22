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

package org.pentaho.di.trans.steps.ldapoutput;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.ldapinput.LdapMeta;
import org.pentaho.di.trans.steps.ldapinput.LdapProtocolFactory;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class LDAPOutputMeta extends BaseStepMeta implements LdapMeta {
  private static Class<?> PKG = LDAPOutputMeta.class; // for i18n purposes, needed by Translator2!!

  /** Flag indicating that we use authentication for connection */
  private boolean useAuthentication;

  /** The Host name */
  private String Host;

  /** The User name */
  private String userName;

  /** The Password to use in LDAP authentication */
  private String password;

  /** The Port */
  private String port;

  /** The name of DN field */
  private String dnFieldName;

  private boolean failIfNotExist;

  /** Field value to update */
  private String[] updateLookup;

  /** Stream name to update value with */
  private String[] updateStream;

  /** boolean indicating if field needs to be updated */
  private Boolean[] update;

  /** Operations type */
  private String searchBase;

  /** Multi valued separator **/
  private String multiValuedSeparator;

  private int operationType;

  private String oldDnFieldName;
  private String newDnFieldName;
  private boolean deleteRDN;

  /**
   * The operations description
   */
  public static final String[] operationTypeDesc = {
    BaseMessages.getString( PKG, "LDAPOutputMeta.operationType.Insert" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.operationType.Upsert" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.operationType.Update" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.operationType.Add" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.operationType.Delete" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.operationType.Rename" ) };

  /**
   * The operations type codes
   */
  public static final String[] operationTypeCode = { "insert", "upsert", "update", "add", "delete", "rename" };

  public static final int OPERATION_TYPE_INSERT = 0;

  public static final int OPERATION_TYPE_UPSERT = 1;

  public static final int OPERATION_TYPE_UPDATE = 2;

  public static final int OPERATION_TYPE_ADD = 3;

  public static final int OPERATION_TYPE_DELETE = 4;

  public static final int OPERATION_TYPE_RENAME = 5;

  private int referralType;

  /**
   * The referrals description
   */
  public static final String[] referralTypeDesc = {
    BaseMessages.getString( PKG, "LDAPOutputMeta.referralType.Follow" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.referralType.Ignore" ) };

  /**
   * The referrals type codes
   */
  public static final String[] referralTypeCode = { "follow", "ignore" };

  public static final int REFERRAL_TYPE_FOLLOW = 0;

  public static final int REFERRAL_TYPE_IGNORE = 1;

  private int derefAliasesType;

  /**
   * The derefAliasess description
   */
  public static final String[] derefAliasesTypeDesc = {
    BaseMessages.getString( PKG, "LDAPOutputMeta.derefAliasesType.Always" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.derefAliasesType.Never" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.derefAliasesType.Searching" ),
    BaseMessages.getString( PKG, "LDAPOutputMeta.derefAliasesType.Finding" ) };

  /**
   * The derefAliasess type codes
   */
  public static final String[] derefAliasesTypeCode = { "always", "never", "searching", "finding" };

  public static final int DEREFALIASES_TYPE_ALWAYS = 0;

  public static final int DEREFALIASES_TYPE_NEVER = 1;

  public static final int DEREFALIASES_TYPE_SEARCHING = 2;

  public static final int DEREFALIASES_TYPE_FINDING = 3;

  /** Protocol **/
  private String protocol;

  /** Trust store **/
  private boolean useCertificate;
  private String trustStorePath;
  private String trustStorePassword;
  private boolean trustAllCertificates;

  public LDAPOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the input useCertificate.
   */
  public boolean isUseCertificate() {
    return useCertificate;
  }

  /**
   * @return Returns the useCertificate.
   */
  public void setUseCertificate( boolean value ) {
    this.useCertificate = value;
  }

  /**
   * @return Returns the input trustAllCertificates.
   */
  public boolean isTrustAllCertificates() {
    return trustAllCertificates;
  }

  /**
   * @return Returns the input trustAllCertificates.
   */
  public void setTrustAllCertificates( boolean value ) {
    this.trustAllCertificates = value;
  }

  /**
   * @return Returns the trustStorePath.
   */
  public String getTrustStorePassword() {
    return trustStorePassword;
  }

  /**
   * @param value
   *          the trustStorePassword to set.
   */
  public void setTrustStorePassword( String value ) {
    this.trustStorePassword = value;
  }

  /**
   * @return Returns the trustStorePath.
   */
  public String getTrustStorePath() {
    return trustStorePath;
  }

  /**
   * @param value
   *          the trustStorePath to set.
   */
  public void setTrustStorePath( String value ) {
    this.trustStorePath = value;
  }

  /**
   * @return Returns the protocol.
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * @param value
   *          the protocol to set.
   */
  public void setProtocol( String value ) {
    this.protocol = value;
  }

  public Boolean[] getUpdate() {
    return update;
  }

  public void setUpdate( Boolean[] update ) {
    this.update = update;
  }

  public int getOperationType() {
    return operationType;
  }

  public int getReferralType() {
    return referralType;
  }

  public int getDerefAliasesType() {
    return derefAliasesType;
  }

  public static int getOperationTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < operationTypeDesc.length; i++ ) {
      if ( operationTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getOperationTypeByCode( tt );
  }

  public static int getReferralTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < referralTypeDesc.length; i++ ) {
      if ( referralTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getReferralTypeByCode( tt );
  }

  public static int getDerefAliasesTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < derefAliasesTypeDesc.length; i++ ) {
      if ( derefAliasesTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getReferralTypeByCode( tt );
  }

  private static int getOperationTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < operationTypeCode.length; i++ ) {
      if ( operationTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getReferralTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < referralTypeCode.length; i++ ) {
      if ( referralTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getDerefAliasesTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < derefAliasesTypeCode.length; i++ ) {
      if ( derefAliasesTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void setOperationType( int operationType ) {
    this.operationType = operationType;
  }

  public void setReferralType( int value ) {
    this.referralType = value;
  }

  public void setDerefAliasesType( int value ) {
    this.derefAliasesType = value;
  }

  public static String getOperationTypeDesc( int i ) {
    if ( i < 0 || i >= operationTypeDesc.length ) {
      return operationTypeDesc[0];
    }
    return operationTypeDesc[i];
  }

  public static String getReferralTypeDesc( int i ) {
    if ( i < 0 || i >= referralTypeDesc.length ) {
      return referralTypeDesc[0];
    }
    return referralTypeDesc[i];
  }

  public static String getDerefAliasesTypeDesc( int i ) {
    if ( i < 0 || i >= derefAliasesTypeDesc.length ) {
      return derefAliasesTypeDesc[0];
    }
    return derefAliasesTypeDesc[i];
  }

  /**
   * @return Returns the updateStream.
   */
  public String[] getUpdateStream() {
    return updateStream;
  }

  /**
   * @param updateStream
   *          The updateStream to set.
   */
  public void setUpdateStream( String[] updateStream ) {
    this.updateStream = updateStream;
  }

  /**
   * @return Returns the updateLookup.
   */
  public String[] getUpdateLookup() {
    return updateLookup;
  }

  /**
   * @param updateLookup
   *          The updateLookup to set.
   */
  public void setUpdateLookup( String[] updateLookup ) {
    this.updateLookup = updateLookup;
  }

  /**
   * @return Returns the input useAuthentication.
   * Deprecated as it doesn't follow standards
   */
  @Deprecated
  public boolean UseAuthentication() {
    return useAuthentication;
  }

  /**
   * @return Returns the input useAuthentication.
   */
  public boolean getUseAuthentication() {
    return useAuthentication;
  }

  /**
   * @param useAuthentication
   *          The useAuthentication to set.
   */
  public void setUseAuthentication( boolean useAuthentication ) {
    this.useAuthentication = useAuthentication;
  }

  /**
   * @return Returns the host name.
   */
  public String getHost() {
    return Host;
  }

  /**
   * @param host
   *          The host to set.
   */
  public void setHost( String host ) {
    this.Host = host;
  }

  /**
   * @return Returns the user name.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName
   *          The username to set.
   */
  public void setUserName( String userName ) {
    this.userName = userName;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  public void setDnField( String value ) {
    this.dnFieldName = value;
  }

  public String getDnField() {
    return this.dnFieldName;
  }

  /**
   * @return Returns the Port.
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          The port to set.
   */
  public void setPort( String port ) {
    this.port = port;
  }

  /**
   * @return Returns the failIfNotExist.
   */
  public boolean isFailIfNotExist() {
    return failIfNotExist;
  }

  /**
   * @param failIfNotExist
   *          The failIfNotExist to set.
   */
  public void setFailIfNotExist( boolean value ) {
    this.failIfNotExist = value;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    LDAPOutputMeta retval = (LDAPOutputMeta) super.clone();
    int nrvalues = updateLookup.length;

    retval.allocate( nrvalues );
    System.arraycopy( updateLookup, 0, retval.updateLookup, 0, nrvalues );
    System.arraycopy( updateStream, 0, retval.updateStream, 0, nrvalues );
    System.arraycopy( update, 0, retval.update, 0, nrvalues );

    return retval;
  }

  /**
   * @param value
   *          The deleteRDN filed.
   */
  public void setDeleteRDN( boolean value ) {
    this.deleteRDN = value;
  }

  /**
   * @return Returns the deleteRDN.
   */
  public boolean isDeleteRDN() {
    return deleteRDN;
  }

  /**
   * @param value
   *          The newDnFieldName filed.
   */
  public void setNewDnFieldName( String value ) {
    this.newDnFieldName = value;
  }

  /**
   * @return Returns the newDnFieldName.
   */
  public String getNewDnFieldName() {
    return newDnFieldName;
  }

  /**
   * @param value
   *          The oldDnFieldName filed.
   */
  public void setOldDnFieldName( String value ) {
    this.oldDnFieldName = value;
  }

  /**
   * @return Returns the oldDnFieldName.
   */
  public String getOldDnFieldName() {
    return oldDnFieldName;
  }

  /**
   * @param searchBase
   *          The searchBase filed.
   */
  public void setSearchBaseDN( String searchBase ) {
    this.searchBase = searchBase;
  }

  /**
   * @return Returns the searchBase.
   */
  public String getSearchBaseDN() {
    return searchBase;
  }

  /**
   * @param multiValuedSeparator
   *          The multi-valued separator filed.
   */
  public void setMultiValuedSeparator( String multiValuedSeparator ) {
    this.multiValuedSeparator = multiValuedSeparator;
  }

  /**
   * @return Returns the multi valued separator.
   */
  public String getMultiValuedSeparator() {
    return multiValuedSeparator;
  }

  public void allocate( int nrvalues ) {
    updateLookup = new String[nrvalues];
    updateStream = new String[nrvalues];
    update = new Boolean[nrvalues];
  }

  private static String getOperationTypeCode( int i ) {
    if ( i < 0 || i >= operationTypeCode.length ) {
      return operationTypeCode[0];
    }
    return operationTypeCode[i];
  }

  public static String getReferralTypeCode( int i ) {
    if ( i < 0 || i >= referralTypeCode.length ) {
      return referralTypeCode[0];
    }
    return referralTypeCode[i];
  }

  public static String getDerefAliasesCode( int i ) {
    if ( i < 0 || i >= derefAliasesTypeCode.length ) {
      return derefAliasesTypeCode[0];
    }
    return derefAliasesTypeCode[i];
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "useauthentication", useAuthentication ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "host", Host ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dnFieldName", dnFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "failIfNotExist", failIfNotExist ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "operationType", getOperationTypeCode( operationType ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "multivaluedseparator", multiValuedSeparator ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "searchBase", searchBase ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "referralType", getReferralTypeCode( referralType ) ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "derefAliasesType", getDerefAliasesCode( derefAliasesType ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "oldDnFieldName", oldDnFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "newDnFieldName", newDnFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "deleteRDN", deleteRDN ) );

    retval.append( "    <fields>" + Const.CR );

    for ( int i = 0; i < updateLookup.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", updateLookup[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", updateStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "update", update[i].booleanValue() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }

    retval.append( "      </fields>" + Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "protocol", protocol ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "trustStorePath", trustStorePath ) );
    retval.append( "    " ).append(
      XMLHandler
        .addTagValue( "trustStorePassword", Encr.encryptPasswordIfNotUsingVariables( trustStorePassword ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "trustAllCertificates", trustAllCertificates ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useCertificate", useCertificate ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      useAuthentication = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useauthentication" ) );
      Host = XMLHandler.getTagValue( stepnode, "host" );
      userName = XMLHandler.getTagValue( stepnode, "username" );
      setPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) ) );

      port = XMLHandler.getTagValue( stepnode, "port" );
      dnFieldName = XMLHandler.getTagValue( stepnode, "dnFieldName" );
      failIfNotExist = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "failIfNotExist" ) );
      operationType =
        getOperationTypeByCode( Const.NVL( XMLHandler.getTagValue( stepnode, "operationType" ), "" ) );
      multiValuedSeparator = XMLHandler.getTagValue( stepnode, "multivaluedseparator" );
      searchBase = XMLHandler.getTagValue( stepnode, "searchBase" );
      referralType = getReferralTypeByCode( Const.NVL( XMLHandler.getTagValue( stepnode, "referralType" ), "" ) );
      derefAliasesType =
        getDerefAliasesTypeByCode( Const.NVL( XMLHandler.getTagValue( stepnode, "derefAliasesType" ), "" ) );

      oldDnFieldName = XMLHandler.getTagValue( stepnode, "oldDnFieldName" );
      newDnFieldName = XMLHandler.getTagValue( stepnode, "newDnFieldName" );
      deleteRDN = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "deleteRDN" ) );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        updateLookup[i] = XMLHandler.getTagValue( fnode, "name" );
        updateStream[i] = XMLHandler.getTagValue( fnode, "field" );
        if ( updateStream[i] == null ) {
          updateStream[i] = updateLookup[i]; // default: the same name!
        }
        String updateValue = XMLHandler.getTagValue( fnode, "update" );
        if ( updateValue == null ) {
          // default TRUE
          update[i] = Boolean.TRUE;
        } else {
          if ( updateValue.equalsIgnoreCase( "Y" ) ) {
            update[i] = Boolean.TRUE;
          } else {
            update[i] = Boolean.FALSE;
          }
        }
      }

      protocol = XMLHandler.getTagValue( stepnode, "protocol" );
      trustStorePath = XMLHandler.getTagValue( stepnode, "trustStorePath" );
      trustStorePassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "trustStorePassword" ) );
      trustAllCertificates = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "trustAllCertificates" ) );
      useCertificate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useCertificate" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "LDAPOutputMeta.UnableToLoadFromXML" ), e );
    }
  }

  public void setDefault() {
    useAuthentication = false;
    Host = "";
    userName = "";
    password = "";
    port = "389";
    dnFieldName = null;
    failIfNotExist = true;
    multiValuedSeparator = ";";
    searchBase = null;
    oldDnFieldName = null;
    newDnFieldName = null;
    deleteRDN = true;

    int nrFields = 0;
    allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      updateLookup[i] = "name" + ( i + 1 );
      updateStream[i] = "field" + ( i + 1 );
      update[i] = Boolean.TRUE;
    }
    operationType = OPERATION_TYPE_INSERT;
    referralType = REFERRAL_TYPE_FOLLOW;
    derefAliasesType = DEREFALIASES_TYPE_ALWAYS;
    this.trustStorePath = null;
    this.trustStorePassword = null;
    this.trustAllCertificates = false;
    this.protocol = LdapProtocolFactory.getConnectionTypes( log ).get( 0 );
    this.useCertificate = false;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {

    try {
      useAuthentication = rep.getStepAttributeBoolean( id_step, "useauthentication" );
      Host = rep.getStepAttributeString( id_step, "host" );
      userName = rep.getStepAttributeString( id_step, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) );
      port = rep.getStepAttributeString( id_step, "port" );
      dnFieldName = rep.getStepAttributeString( id_step, "dnFieldName" );
      failIfNotExist = rep.getStepAttributeBoolean( id_step, "failIfNotExist" );
      operationType =
        getOperationTypeByCode( Const.NVL( rep.getStepAttributeString( id_step, "operationType" ), "" ) );
      multiValuedSeparator = rep.getStepAttributeString( id_step, "multivaluedseparator" );
      searchBase = rep.getStepAttributeString( id_step, "searchBase" );
      referralType =
        getReferralTypeByCode( Const.NVL( rep.getStepAttributeString( id_step, "referralType" ), "" ) );
      derefAliasesType =
        getDerefAliasesTypeByCode( Const.NVL( rep.getStepAttributeString( id_step, "referralType" ), "" ) );

      newDnFieldName = rep.getStepAttributeString( id_step, "newDnFieldName" );
      oldDnFieldName = rep.getStepAttributeString( id_step, "oldDnFieldName" );
      deleteRDN = rep.getStepAttributeBoolean( id_step, "deleteRDN" );

      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );
      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        updateLookup[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        updateStream[i] = rep.getStepAttributeString( id_step, i, "field_attribut" );
        update[i] = Boolean.valueOf( rep.getStepAttributeBoolean( id_step, i, "value_update", true ) );
      }
      protocol = rep.getStepAttributeString( id_step, "protocol" );
      trustStorePath = rep.getStepAttributeString( id_step, "trustStorePath" );
      trustStorePassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "trustStorePassword" ) );
      trustAllCertificates = rep.getStepAttributeBoolean( id_step, "trustAllCertificates" );
      useCertificate = rep.getStepAttributeBoolean( id_step, "useCertificate" );

    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "LDAPOutputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "useauthentication", useAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, "host", Host );
      rep.saveStepAttribute( id_transformation, id_step, "username", userName );
      rep.saveStepAttribute( id_transformation, id_step, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );

      rep.saveStepAttribute( id_transformation, id_step, "port", port );
      rep.saveStepAttribute( id_transformation, id_step, "dnFieldName", dnFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "failIfNotExist", failIfNotExist );
      rep.saveStepAttribute( id_transformation, id_step, "operationType", getOperationTypeCode( operationType ) );
      rep.saveStepAttribute( id_transformation, id_step, "multivaluedseparator", multiValuedSeparator );
      rep.saveStepAttribute( id_transformation, id_step, "searchBase", searchBase );
      rep.saveStepAttribute( id_transformation, id_step, "referralType", getReferralTypeCode( referralType ) );
      rep.saveStepAttribute(
        id_transformation, id_step, "derefAliasesType", getDerefAliasesCode( derefAliasesType ) );

      rep.saveStepAttribute( id_transformation, id_step, "oldDnFieldName", oldDnFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "newDnFieldName", newDnFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "deleteRDN", deleteRDN );

      for ( int i = 0; i < updateLookup.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", updateLookup[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_attribut", updateStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "value_update", update[i].booleanValue() );

      }
      rep.saveStepAttribute( id_transformation, id_step, "protocol", protocol );
      rep.saveStepAttribute( id_transformation, id_step, "trustStorePath", trustStorePath );
      rep.saveStepAttribute( id_transformation, id_step, "trustStorePassword", Encr
        .encryptPasswordIfNotUsingVariables( trustStorePassword ) );
      rep.saveStepAttribute( id_transformation, id_step, "trustAllCertificates", trustAllCertificates );
      rep.saveStepAttribute( id_transformation, id_step, "useCertificate", useCertificate );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "LDAPOutputMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDAPOutputMeta.CheckResult.NoInputExpected" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDAPOutputMeta.CheckResult.NoInput" ), stepMeta );
    }
    remarks.add( cr );

    // Check hostname
    if ( Utils.isEmpty( Host ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDAPOutputMeta.CheckResult.HostnameMissing" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDAPOutputMeta.CheckResult.HostnameOk" ), stepMeta );
    }
    remarks.add( cr );

    // check return fields
    if ( updateLookup.length == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDAPOutputUpdateMeta.CheckResult.NoFields" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDAPOutputUpdateMeta.CheckResult.FieldsOk" ), stepMeta );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new LDAPOutput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new LDAPOutputData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  public String toString() {
    return "LDAPConnection " + getName();
  }

  @Override
  public String getDerefAliases() {
    return LDAPOutputMeta.getDerefAliasesCode( getDerefAliasesType() );
  }

  @Override
  public String getReferrals() {
    return getReferralTypeCode( getReferralType() );
  }
}
