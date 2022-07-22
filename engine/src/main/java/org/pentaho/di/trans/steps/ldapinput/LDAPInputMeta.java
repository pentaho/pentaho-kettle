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

package org.pentaho.di.trans.steps.ldapinput;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class LDAPInputMeta extends BaseStepMeta implements LdapMeta {
  private static Class<?> PKG = LDAPInputMeta.class; // for i18n purposes, needed by Translator2!!

  /** Flag indicating that we use authentication for connection */
  private boolean useAuthentication;

  /** Flag indicating that we use paging */
  private boolean usePaging;

  /** page size */
  private String pagesize;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The maximum number or lines to read */
  private int rowLimit;

  /** The Host name */
  private String Host;

  /** The User name */
  private String userName;

  /** The Password to use in LDAP authentication */
  private String password;

  /** The Port */
  private String port;

  /** The Filter string */
  private String filterString;

  /** The Search Base */
  private String searchBase;

  /** The fields to import... */
  private LDAPInputField[] inputFields;

  /** The Time limit **/
  private int timeLimit;

  /** Multi valued separator **/
  private String multiValuedSeparator;

  private static final String YES = "Y";

  private boolean dynamicSearch;
  private String dynamicSeachFieldName;

  private boolean dynamicFilter;
  private String dynamicFilterFieldName;

  /** Search scope */
  private int searchScope;

  /**
   * The search scopes description
   */
  public static final String[] searchScopeDesc = {
    BaseMessages.getString( PKG, "LDAPInputMeta.SearchScope.Object" ),
    BaseMessages.getString( PKG, "LDAPInputMeta.SearchScope.OneLevel" ),
    BaseMessages.getString( PKG, "LDAPInputMeta.SearchScope.Subtree" ) };

  /**
   * The search scope codes
   */
  public static final String[] searchScopeCode = { "object", "onelevel", "subtree" };

  /** Protocol **/
  private String protocol;

  /** Trust store **/
  private boolean useCertificate;
  private String trustStorePath;
  private String trustStorePassword;
  private boolean trustAllCertificates;

  public LDAPInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the input useCertificate.
   */
  @Override
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
  @Override
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
  @Override
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
  @Override
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
  @Override
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

  /**
   * @return Returns the input dynamicSearch.
   */
  public boolean isDynamicSearch() {
    return dynamicSearch;
  }

  /**
   * @return Returns the input dynamicSearch.
   */
  public void setDynamicSearch( boolean dynamicSearch ) {
    this.dynamicSearch = dynamicSearch;
  }

  /**
   * @return Returns the input dynamicSeachFieldName.
   */
  public String getDynamicSearchFieldName() {
    return dynamicSeachFieldName;
  }

  /**
   * @return Returns the input dynamicSeachFieldName.
   */
  public void setDynamicSearchFieldName( String dynamicSeachFieldName ) {
    this.dynamicSeachFieldName = dynamicSeachFieldName;
  }

  /**
   * @return Returns the input dynamicFilter.
   */
  public boolean isDynamicFilter() {
    return dynamicFilter;
  }

  /**
   * @param dynamicFilter
   *          the dynamicFilter to set.
   */
  public void setDynamicFilter( boolean dynamicFilter ) {
    this.dynamicFilter = dynamicFilter;
  }

  /**
   * @return Returns the input dynamicFilterFieldName.
   */
  public String getDynamicFilterFieldName() {
    return dynamicFilterFieldName;
  }

  /**
   * param dynamicFilterFieldName the dynamicFilterFieldName to set.
   */
  public void setDynamicFilterFieldName( String dynamicFilterFieldName ) {
    this.dynamicFilterFieldName = dynamicFilterFieldName;
  }

  /**
   * @return Returns the input useAuthentication.
   */
  public boolean UseAuthentication() {
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
   * @return Returns the input usePaging.
   */
  public boolean isPaging() {
    return usePaging;
  }

  /**
   * @param usePaging
   *          The usePaging to set.
   */
  public void setPaging( boolean usePaging ) {
    this.usePaging = usePaging;
  }

  /**
   * @return Returns the input fields.
   */
  public LDAPInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( LDAPInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the host name.
   */
  @Override
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

  /**
   * @return Returns the Port.
   */
  @Override
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
   * @return Returns the filter string.
   */
  public String getFilterString() {
    return filterString;
  }

  /**
   * @param filterString
   *          The filter string to set.
   */
  public void setFilterString( String filterString ) {
    this.filterString = filterString;
  }

  /**
   * @return Returns the search string.
   */
  public String getSearchBase() {
    return searchBase;
  }

  /**
   * @param searchBase
   *          The filter Search Base to set.
   */
  public void setSearchBase( String searchBase ) {
    this.searchBase = searchBase;
  }

  /**
   * @return Returns the rowLimit.
   */
  public int getRowLimit() {
    return rowLimit;
  }

  /**
   * @param timeLimit
   *          The timeout time limit to set.
   */
  public void setTimeLimit( int timeLimit ) {
    this.timeLimit = timeLimit;
  }

  /**
   * @return Returns the time limit.
   */
  public int getTimeLimit() {
    return timeLimit;
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

  /**
   * @param pagesize
   *          The pagesize.
   */
  public void setPageSize( String pagesize ) {
    this.pagesize = pagesize;
  }

  /**
   * @return Returns the pagesize.
   */
  public String getPageSize() {
    return pagesize;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( int rowLimit ) {
    this.rowLimit = rowLimit;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    LDAPInputMeta retval = (LDAPInputMeta) super.clone();

    int nrFields = inputFields.length;

    retval.allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[i] != null ) {
        retval.inputFields[i] = (LDAPInputField) inputFields[i].clone();
      }
    }

    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "usepaging", usePaging ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "pagesize", pagesize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useauthentication", useAuthentication ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "host", Host ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filterstring", filterString ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "searchbase", searchBase ) );

    /*
     * Describe the fields to read
     */
    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", inputFields[i].getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "attribute", inputFields[i].getAttribute() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "attribute_fetch_as", inputFields[i].getFetchAttributeAsCode() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "sorted_key", inputFields[i].isSortedKey() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "type", inputFields[i].getTypeDesc() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "format", inputFields[i].getFormat() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "length", inputFields[i].getLength() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "precision", inputFields[i].getPrecision() ) );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( "currency", inputFields[i].getCurrencySymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", inputFields[i].getDecimalSymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "group", inputFields[i].getGroupSymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type", inputFields[i].getTrimTypeCode() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "repeat", inputFields[i].isRepeated() ) );

      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", rowLimit ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "timelimit", timeLimit ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "multivaluedseparator", multiValuedSeparator ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicsearch", dynamicSearch ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicseachfieldname", dynamicSeachFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicfilter", dynamicFilter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicfilterfieldname", dynamicFilterFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "searchScope", getSearchScopeCode( searchScope ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "protocol", protocol ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "trustStorePath", trustStorePath ) );
    retval.append( "    " ).append(
      XMLHandler
        .addTagValue( "trustStorePassword", Encr.encryptPasswordIfNotUsingVariables( trustStorePassword ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "trustAllCertificates", trustAllCertificates ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "useCertificate", useCertificate ) );

    return retval.toString();
  }

  private static String getSearchScopeCode( int i ) {
    if ( i < 0 || i >= searchScopeCode.length ) {
      return searchScopeCode[0];
    }
    return searchScopeCode[i];
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      usePaging = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usepaging" ) );
      pagesize = XMLHandler.getTagValue( stepnode, "pagesize" );
      useAuthentication = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useauthentication" ) );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      Host = XMLHandler.getTagValue( stepnode, "host" );
      userName = XMLHandler.getTagValue( stepnode, "username" );
      setPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) ) );

      port = XMLHandler.getTagValue( stepnode, "port" );
      filterString = XMLHandler.getTagValue( stepnode, "filterstring" );
      searchBase = XMLHandler.getTagValue( stepnode, "searchbase" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        inputFields[i] = new LDAPInputField();

        inputFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
        inputFields[i].setAttribute( XMLHandler.getTagValue( fnode, "attribute" ) );
        inputFields[i].setFetchAttributeAs( LDAPInputField.getFetchAttributeAsByCode( XMLHandler.getTagValue(
          fnode, "attribute_fetch_as" ) ) );
        String sortedkey = XMLHandler.getTagValue( fnode, "sorted_key" );
        if ( sortedkey != null ) {
          inputFields[i].setSortedKey( YES.equalsIgnoreCase( sortedkey ) );
        } else {
          inputFields[i].setSortedKey( false );
        }
        inputFields[i].setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
        inputFields[i].setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
        inputFields[i].setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
        String srepeat = XMLHandler.getTagValue( fnode, "repeat" );
        if ( srepeat != null ) {
          inputFields[i].setRepeated( YES.equalsIgnoreCase( srepeat ) );
        } else {
          inputFields[i].setRepeated( false );
        }
        inputFields[i].setTrimType( ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );

        inputFields[i].setFormat( XMLHandler.getTagValue( fnode, "format" ) );
        inputFields[i].setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
        inputFields[i].setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
        inputFields[i].setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );

      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "limit" ), 0 );
      timeLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "timelimit" ), 0 );
      multiValuedSeparator = XMLHandler.getTagValue( stepnode, "multivaluedseparator" );
      dynamicSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "dynamicsearch" ) );
      dynamicSeachFieldName = XMLHandler.getTagValue( stepnode, "dynamicseachfieldname" );
      dynamicFilter = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "dynamicfilter" ) );
      dynamicFilterFieldName = XMLHandler.getTagValue( stepnode, "dynamicfilterfieldname" );
      searchScope =
        getSearchScopeByCode( Const.NVL(
          XMLHandler.getTagValue( stepnode, "searchScope" ),
          getSearchScopeCode( LDAPConnection.SEARCH_SCOPE_SUBTREE_SCOPE ) ) );

      protocol = XMLHandler.getTagValue( stepnode, "protocol" );
      trustStorePath = XMLHandler.getTagValue( stepnode, "trustStorePath" );
      trustStorePassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "trustStorePassword" ) );
      trustAllCertificates = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "trustAllCertificates" ) );
      useCertificate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useCertificate" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "LDAPInputMeta.UnableToLoadFromXML" ), e );
    }
  }

  private static int getSearchScopeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < searchScopeCode.length; i++ ) {
      if ( searchScopeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void allocate( int nrfields ) {

    inputFields = new LDAPInputField[nrfields];
  }

  @Override
  public void setDefault() {
    this.usePaging = false;
    this.pagesize = "1000";
    this.useAuthentication = false;
    this.includeRowNumber = false;
    this.rowNumberField = "";
    this.Host = "";
    this.userName = "";
    this.password = "";
    this.port = "389";
    this.filterString = LDAPConnection.DEFAUL_FILTER_STRING;
    this.searchBase = "";
    this.multiValuedSeparator = ";";
    this.dynamicSearch = false;
    this.dynamicSeachFieldName = null;
    this.dynamicFilter = false;
    this.dynamicFilterFieldName = null;
    int nrFields = 0;

    allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      this.inputFields[i] = new LDAPInputField( "field" + ( i + 1 ) );
    }

    this.rowLimit = 0;
    this.timeLimit = 0;
    this.searchScope = LDAPConnection.SEARCH_SCOPE_SUBTREE_SCOPE;
    this.trustStorePath = null;
    this.trustStorePassword = null;
    this.trustAllCertificates = false;
    this.protocol = LdapProtocolFactory.getConnectionTypes( log ).get( 0 );
    this.useCertificate = false;
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    int i;
    for ( i = 0; i < inputFields.length; i++ ) {
      LDAPInputField field = inputFields[i];

      int type = field.getType();
      if ( type == ValueMetaInterface.TYPE_NONE ) {
        type = ValueMetaInterface.TYPE_STRING;
      }
      try {
        ValueMetaInterface v =
          ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
        v.setLength( field.getLength(), field.getPrecision() );
        v.setOrigin( name );
        r.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }

    }

    if ( includeRowNumber ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {

    try {
      usePaging = rep.getStepAttributeBoolean( id_step, "usepaging" );
      pagesize = rep.getStepAttributeString( id_step, "pagesize" );
      useAuthentication = rep.getStepAttributeBoolean( id_step, "useauthentication" );
      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );
      Host = rep.getStepAttributeString( id_step, "host" );
      userName = rep.getStepAttributeString( id_step, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) );
      port = rep.getStepAttributeString( id_step, "port" );
      filterString = rep.getStepAttributeString( id_step, "filterstring" );
      searchBase = rep.getStepAttributeString( id_step, "searchbase" );

      rowLimit = (int) rep.getStepAttributeInteger( id_step, "limit" );
      timeLimit = (int) rep.getStepAttributeInteger( id_step, "timelimit" );
      multiValuedSeparator = rep.getStepAttributeString( id_step, "multivaluedseparator" );
      dynamicSearch = rep.getStepAttributeBoolean( id_step, "dynamicsearch" );
      dynamicSeachFieldName = rep.getStepAttributeString( id_step, "dynamicseachfieldname" );
      dynamicFilter = rep.getStepAttributeBoolean( id_step, "dynamicfilter" );
      dynamicFilterFieldName = rep.getStepAttributeString( id_step, "dynamicfilterfieldname" );

      protocol = rep.getStepAttributeString( id_step, "protocol" );
      trustStorePath = rep.getStepAttributeString( id_step, "trustStorePath" );
      trustStorePassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "trustStorePassword" ) );
      trustAllCertificates = rep.getStepAttributeBoolean( id_step, "trustAllCertificates" );
      useCertificate = rep.getStepAttributeBoolean( id_step, "useCertificate" );

      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrFields );

      for ( int i = 0; i < nrFields; i++ ) {
        LDAPInputField field = new LDAPInputField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setAttribute( rep.getStepAttributeString( id_step, i, "field_attribute" ) );
        field.setFetchAttributeAs( LDAPInputField.getFetchAttributeAsByCode( rep.getStepAttributeString(
          id_step, i, "field_attribute_fetch_as" ) ) );
        field.setSortedKey( rep.getStepAttributeBoolean( id_step, i, "sorted_key" ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( LDAPInputField.getTrimTypeByCode( rep.getStepAttributeString(
          id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

        inputFields[i] = field;
      }
      searchScope =
        getSearchScopeByCode( Const.NVL(
          rep.getStepAttributeString( id_step, "searchScope" ),
          getSearchScopeCode( LDAPConnection.SEARCH_SCOPE_SUBTREE_SCOPE ) ) );
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "LDAPInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public static String getSearchScopeDesc( int i ) {
    if ( i < 0 || i >= searchScopeDesc.length ) {
      return searchScopeDesc[0];
    }
    return searchScopeDesc[i];
  }

  public static int getSearchScopeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < searchScopeDesc.length; i++ ) {
      if ( searchScopeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getSearchScopeByCode( tt );
  }

  public void setSearchScope( int value ) {
    this.searchScope = value;
  }

  public int getSearchScope() {
    return searchScope;
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "usepaging", usePaging );
      rep.saveStepAttribute( id_transformation, id_step, "pagesize", pagesize );
      rep.saveStepAttribute( id_transformation, id_step, "useauthentication", useAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "host", Host );
      rep.saveStepAttribute( id_transformation, id_step, "username", userName );
      rep.saveStepAttribute( id_transformation, id_step, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );

      rep.saveStepAttribute( id_transformation, id_step, "port", port );
      rep.saveStepAttribute( id_transformation, id_step, "filterstring", filterString );
      rep.saveStepAttribute( id_transformation, id_step, "searchbase", searchBase );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "timelimit", timeLimit );
      rep.saveStepAttribute( id_transformation, id_step, "multivaluedseparator", multiValuedSeparator );
      rep.saveStepAttribute( id_transformation, id_step, "dynamicsearch", dynamicSearch );
      rep.saveStepAttribute( id_transformation, id_step, "dynamicseachfieldname", dynamicSeachFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "dynamicfilter", dynamicFilter );
      rep.saveStepAttribute( id_transformation, id_step, "dynamicfilterfieldname", dynamicFilterFieldName );

      rep.saveStepAttribute( id_transformation, id_step, "protocol", protocol );
      rep.saveStepAttribute( id_transformation, id_step, "trustStorePath", trustStorePath );
      rep.saveStepAttribute( id_transformation, id_step, "trustStorePassword", Encr
        .encryptPasswordIfNotUsingVariables( trustStorePassword ) );
      rep.saveStepAttribute( id_transformation, id_step, "trustAllCertificates", trustAllCertificates );
      rep.saveStepAttribute( id_transformation, id_step, "useCertificate", useCertificate );

      for ( int i = 0; i < inputFields.length; i++ ) {
        LDAPInputField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_attribute", field.getAttribute() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_attribute_fetch_as", field
          .getFetchAttributeAsCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "sorted_key", field.isSortedKey() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_group", field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type", field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_repeat", field.isRepeated() );

      }
      rep.saveStepAttribute( id_transformation, id_step, "searchScope", getSearchScopeCode( searchScope ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "LDAPInputMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    // Check output fields
    if ( inputFields.length == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDAPInputMeta.CheckResult.NoOutputFields" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDAPInputMeta.CheckResult.OutputFieldsOk" ), stepMeta );
    }
    remarks.add( cr );

    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDAPInputMeta.CheckResult.NoInputExpected" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDAPInputMeta.CheckResult.NoInput" ), stepMeta );
    }
    remarks.add( cr );

    // Check hostname
    if ( Utils.isEmpty( Host ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDAPInputMeta.CheckResult.HostnameMissing" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDAPInputMeta.CheckResult.HostnameOk" ), stepMeta );
    }
    remarks.add( cr );

    if ( isDynamicSearch() ) {
      if ( Utils.isEmpty( dynamicSeachFieldName ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.DynamicSearchBaseFieldNameMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.DynamicSearchBaseFieldNameOk" ), stepMeta );
      }
      remarks.add( cr );
    } else {
      // Check search base
      if ( Utils.isEmpty( searchBase ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.SearchBaseMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.SearchBaseOk" ), stepMeta );
      }
      remarks.add( cr );
    }
    if ( isDynamicFilter() ) {
      if ( Utils.isEmpty( dynamicFilterFieldName ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.DynamicFilterFieldNameMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.DynamicFilterFieldNameOk" ), stepMeta );
      }
      remarks.add( cr );
    } else {
      // Check filter String
      if ( Utils.isEmpty( filterString ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.FilterStringMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "LDAPInputMeta.CheckResult.FilterStringOk" ), stepMeta );
      }
      remarks.add( cr );
    }

  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new LDAPInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new LDAPInputData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public String toString() {
    return "LDAPConnection " + getName();
  }

  @Override
  public String getDerefAliases() {
    return "always";
  }

  @Override
  public String getReferrals() {
    return "follow";
  }
}
