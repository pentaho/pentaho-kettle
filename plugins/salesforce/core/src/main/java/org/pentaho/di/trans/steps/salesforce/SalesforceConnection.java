/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputMeta;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.DeletedRecord;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.GetDeletedResult;
import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.wsdl.Constants;

public class SalesforceConnection {
  private static final FieldType ID_FIELD_TYPE = FieldType.id;
  private static final FieldType REFERENCE_FIELD_TYPE = FieldType.reference;

  private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!

  private String url;
  private String username;
  private String password;
  private String module;
  private int timeout;
  private PartnerConnection binding;
  private LoginResult loginResult;
  private GetUserInfoResult userInfo;
  private String sql;
  private Date serverTimestamp;
  private QueryResult qr;
  private GregorianCalendar startDate;
  private GregorianCalendar endDate;
  private SObject[] sObjects;
  private int recordsFilter;
  private String fieldsList;
  private int queryResultSize;
  private int recordsCount;
  private boolean useCompression;
  private boolean rollbackAllChangesOnError;
  private boolean queryAll;
  private HashMap<String, Date> getDeletedList;

  private LogChannelInterface log;

  /**
   * Construct a new Salesforce Connection
   */
  public SalesforceConnection( LogChannelInterface logInterface, String url, String username, String password ) throws KettleException {
    if ( logInterface == null ) {
      this.log = KettleLogStore.getLogChannelInterfaceFactory().create( this );
    } else {
      this.log = logInterface;
    }
    this.url = url;
    setUsername( username );
    setPassword( password );
    setTimeOut( 0 );

    this.binding = null;
    this.loginResult = null;
    this.userInfo = null;
    this.sql = null;
    this.serverTimestamp = null;
    this.qr = null;
    this.startDate = null;
    this.endDate = null;
    this.sObjects = null;
    this.recordsFilter = SalesforceConnectionUtils.RECORDS_FILTER_ALL;
    this.fieldsList = null;
    this.queryResultSize = 0;
    this.recordsCount = 0;
    setUsingCompression( false );
    setRollbackAllChangesOnError( false );

    // check target URL
    if ( Utils.isEmpty( getURL() ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.TargetURLMissing.Error" ) );
    }

    // check username
    if ( Utils.isEmpty( getUsername() ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.UsernameMissing.Error" ) );
    }

    if ( log.isDetailed() ) {
      logInterface.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.NewConnection" ) );
    }
  }

  public boolean isRollbackAllChangesOnError() {
    return this.rollbackAllChangesOnError;
  }

  /**
   *
   * @see #isRollbackAllChangesOnError(boolean)
   */
  @Deprecated
  public void rollbackAllChangesOnError( boolean value ) {
    setRollbackAllChangesOnError( value );
  }

  public void setRollbackAllChangesOnError( boolean value ) {
    this.rollbackAllChangesOnError = value;
  }

  public boolean isQueryAll() {
    return this.queryAll;
  }

  /**
   *
   * @see #setQueryAll(boolean)
   */
  @Deprecated
  public void queryAll( boolean value ) {
    setQueryAll( value );
  }

  public void setQueryAll( boolean value ) {
    this.queryAll = value;
  }

  public void setCalendar( int recordsFilter, GregorianCalendar startDate, GregorianCalendar endDate ) throws KettleException {
    this.startDate = startDate;
    this.endDate = endDate;
    this.recordsFilter = recordsFilter;
    if ( this.startDate == null || this.endDate == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.EmptyStartDateOrEndDate" ) );
    }
    if ( this.startDate.getTime().compareTo( this.endDate.getTime() ) >= 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.WrongDates" ) );
    }
    // Calculate difference in days
    long diffDays =
      ( this.endDate.getTime().getTime() - this.startDate.getTime().getTime() ) / ( 24 * 60 * 60 * 1000 );
    if ( diffDays > 30 ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.StartDateTooOlder" ) );
    }
  }

  public void setSQL( String sql ) {
    this.sql = sql;
  }

  public void setFieldsList( String fieldsList ) {
    this.fieldsList = fieldsList;
  }

  public void setModule( String module ) {
    this.module = module;
  }

  public String getURL() {
    return this.url;
  }

  public String getSQL() {
    return this.sql;
  }

  public Date getServerTimestamp() {
    return this.serverTimestamp;
  }

  public String getModule() {
    return this.module;
  }

  public QueryResult getQueryResult() {
    return this.qr;
  }

  public PartnerConnection createBinding( ConnectorConfig config ) throws ConnectionException {
    if ( this.binding == null ) {
      this.binding = new PartnerConnection( config );
    }
    return this.binding;
  }

  public PartnerConnection getBinding() {
    return this.binding;
  }

  public void setTimeOut( int timeout ) {
    this.timeout = timeout;
  }

  public int getTimeOut() {
    return this.timeout;
  }

  public boolean isUsingCompression() {
    return this.useCompression;
  }

  public void setUsingCompression( boolean useCompression ) {
    this.useCompression = useCompression;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername( String value ) {
    this.username = value;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword( String value ) {
    this.password = value;
  }

  public void connect() throws KettleException {
    ConnectorConfig config = new ConnectorConfig();
    config.setAuthEndpoint( getURL() );
    config.setServiceEndpoint( getURL() );
    config.setUsername( getUsername() );
    config.setPassword( getPassword() );
    config.setCompression( isUsingCompression() );
    config.setManualLogin( true );

    String proxyUrl = System.getProperty( "http.proxyHost", null );
    if ( StringUtils.isNotEmpty( proxyUrl ) ) {
      int proxyPort = Integer.parseInt( System.getProperty( "http.proxyPort", "80" ) );
      String proxyUser = System.getProperty( "http.proxyUser", null );
      String proxyPassword = Encr.decryptPasswordOptionallyEncrypted(
          System.getProperty( "http.proxyPassword", null ) );
      config.setProxy( proxyUrl, proxyPort );
      config.setProxyUsername( proxyUser );
      config.setProxyPassword( proxyPassword );
    }

    // Set timeout
    if ( getTimeOut() > 0 ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.SettingTimeout", "" + this.timeout ) );
      }
      config.setConnectionTimeout( getTimeOut() );
      config.setReadTimeout( getTimeOut() );
    }

    try {
      PartnerConnection pConnection = createBinding( config );

      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.LoginURL", config.getAuthEndpoint() ) );
      }

      if ( isRollbackAllChangesOnError() ) {
        // Set the SOAP header to rollback all changes
        // unless all records are processed successfully.
        pConnection.setAllOrNoneHeader( true );
      }

      // Attempt the login giving the user feedback
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.LoginNow" ) );
        log.logDetailed( "----------------------------------------->" );
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.LoginURL", getURL() ) );
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.LoginUsername", getUsername() ) );
        if ( getModule() != null ) {
          log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.LoginModule", getModule() ) );
        }
        log.logDetailed( "<-----------------------------------------" );
      }

      // Login
      this.loginResult =
          pConnection.login( config.getUsername(), Encr.decryptPasswordOptionallyEncrypted( config.getPassword() ) );

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.SessionId" )
          + " : " + this.loginResult.getSessionId() );
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.NewServerURL" )
          + " : " + this.loginResult.getServerUrl() );
      }

      // Create a new session header object and set the session id to that
      // returned by the login
      pConnection.setSessionHeader( loginResult.getSessionId() );
      config.setServiceEndpoint( loginResult.getServerUrl() );

      // Return the user Infos
      this.userInfo = pConnection.getUserInfo();
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.UserInfos" )
          + " : " + this.userInfo.getUserFullName() );
        log.logDebug( "----------------------------------------->" );
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.UserName" )
          + " : " + this.userInfo.getUserFullName() );
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.UserEmail" )
          + " : " + this.userInfo.getUserEmail() );
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.UserLanguage" )
          + " : " + this.userInfo.getUserLanguage() );
        log.logDebug( BaseMessages.getString( PKG, "SalesforceInput.Log.UserOrganization" )
          + " : " + this.userInfo.getOrganizationName() );
        log.logDebug( "<-----------------------------------------" );
      }

      this.serverTimestamp = pConnection.getServerTimestamp().getTimestamp().getTime();
      if ( log.isDebug() ) {
        BaseMessages.getString( PKG, "SalesforceInput.Log.ServerTimestamp", getServerTimestamp() );
      }

      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.Connected" ) );
      }

    } catch ( LoginFault ex ) {
      // The LoginFault derives from AxisFault
      ExceptionCode exCode = ex.getExceptionCode();
      if ( exCode == ExceptionCode.FUNCTIONALITY_NOT_ENABLED
        || exCode == ExceptionCode.INVALID_CLIENT || exCode == ExceptionCode.INVALID_LOGIN
        || exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_DOMAIN
        || exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_TIME || exCode == ExceptionCode.ORG_LOCKED
        || exCode == ExceptionCode.PASSWORD_LOCKOUT || exCode == ExceptionCode.SERVER_UNAVAILABLE
        || exCode == ExceptionCode.TRIAL_EXPIRED || exCode == ExceptionCode.UNSUPPORTED_CLIENT ) {
        throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.InvalidUsernameOrPassword" ) );
      }
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.Connection" ), ex );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.Connection" ), e );
    }
  }

  public void query( boolean specifyQuery ) throws KettleException {

    if ( getBinding() == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Exception.CanNotGetBiding" ) );
    }

    try {
      if ( !specifyQuery ) {
        // check if we can query this Object
        DescribeSObjectResult describeSObjectResult = getBinding().describeSObject( getModule() );
        if ( describeSObjectResult == null ) {
          throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.ErrorGettingObject" ) );
        }
        if ( !describeSObjectResult.isQueryable() ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "SalesforceInputDialog.ObjectNotQueryable", module ) );
        }
        if ( this.recordsFilter == SalesforceConnectionUtils.RECORDS_FILTER_UPDATED
          || this.recordsFilter == SalesforceConnectionUtils.RECORDS_FILTER_DELETED ) {
          // The object must be replicateable
          if ( !describeSObjectResult.isReplicateable() ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "SalesforceInput.Error.ObjectNotReplicateable", getModule() ) );
          }
        }
      }

      if ( getSQL() != null && log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.SQLString" ) + " : " + getSQL() );
      }

      switch ( this.recordsFilter ) {
        case SalesforceConnectionUtils.RECORDS_FILTER_UPDATED:
          // Updated records ...
          GetUpdatedResult updatedRecords = getBinding().getUpdated( getModule(), this.startDate, this.endDate );

          if ( updatedRecords.getIds() != null ) {
            int nr = updatedRecords.getIds().length;
            if ( nr > 0 ) {
              String[] ids = updatedRecords.getIds();
              // We can pass a maximum of 2000 object IDs
              if ( nr > SalesforceConnectionUtils.MAX_UPDATED_OBJECTS_IDS ) {
                this.sObjects = new SObject[nr];
                List<String> list = new ArrayList<String>();
                int desPos = 0;
                for ( int i = 0; i < nr; i++ ) {
                  list.add( updatedRecords.getIds()[i] );

                  if ( i % SalesforceConnectionUtils.MAX_UPDATED_OBJECTS_IDS == 0 || i == nr - 1 ) {
                    SObject[] s =
                      getBinding().retrieve(
                        this.fieldsList, getModule(), list.toArray( new String[list.size()] ) );
                    System.arraycopy( s, 0, this.sObjects, desPos, s.length );
                    desPos += s.length;
                    s = null;
                    list = new ArrayList<String>();
                  }
                }
              } else {
                this.sObjects = getBinding().retrieve( this.fieldsList, getModule(), ids );
              }
              if ( this.sObjects != null ) {
                this.queryResultSize = this.sObjects.length;
              }
            }
          }
          break;
        case SalesforceConnectionUtils.RECORDS_FILTER_DELETED:
          // Deleted records ...
          GetDeletedResult deletedRecordsResult =
            getBinding().getDeleted( getModule(), this.startDate, this.endDate );

          DeletedRecord[] deletedRecords = deletedRecordsResult.getDeletedRecords();

          if ( log.isDebug() ) {
            log.logDebug( toString(), BaseMessages.getString(
              PKG, "SalesforceConnection.DeletedRecordsFound", String.valueOf( deletedRecords == null
                ? 0 : deletedRecords.length ) ) );
          }

          if ( deletedRecords != null && deletedRecords.length > 0 ) {
            getDeletedList = new HashMap<>();

            for ( DeletedRecord dr : deletedRecords ) {
              getDeletedList.put( dr.getId(), dr.getDeletedDate().getTime() );
            }
            this.qr = getBinding().queryAll( getSQL() );
            this.sObjects = getQueryResult().getRecords();
            this.queryResultSize = getQueryResult().getSize();
          }
          break;
        default:
          // return query result
          this.qr = isQueryAll() ? getBinding().queryAll( getSQL() ) : getBinding().query( getSQL() );
          this.sObjects = getQueryResult().getRecords();
          this.queryResultSize = getQueryResult().getSize();
          break;
      }
      if ( this.sObjects != null ) {
        this.recordsCount = this.sObjects.length;
      } else {
        this.recordsCount = 0;
      }
    } catch ( Exception e ) {
      log.logError( Const.getStackTracker( e ) );
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceConnection.Exception.Query" ), e );
    }
  }

  public void close() throws KettleException {
    try {
      if ( !getQueryResult().isDone() ) {
        this.qr.setDone( true );
        this.qr = null;
      }
      if ( this.sObjects != null ) {
        this.sObjects = null;
      }
      if ( this.binding != null ) {
        this.binding = null;
      }
      if ( this.loginResult != null ) {
        this.loginResult = null;
      }
      if ( this.userInfo != null ) {
        this.userInfo = null;
      }
      if ( this.getDeletedList != null ) {
        getDeletedList.clear();
        getDeletedList = null;
      }
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "SalesforceInput.Log.ConnectionClosed" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.ClosingConnection" ), e );
    }
  }

  public int getQueryResultSize() {
    return this.queryResultSize;
  }

  public int getRecordsCount() {
    return this.recordsCount;
  }

  public SalesforceRecordValue getRecord( int recordIndex ) {
    int index = recordIndex;

    SObject con = this.sObjects[index];
    SalesforceRecordValue retval = new SalesforceRecordValue( index );
    if ( con == null ) {
      return null;
    }
    if ( this.recordsFilter == SalesforceConnectionUtils.RECORDS_FILTER_DELETED ) {
      // Special case from deleted records
      // We need to compare each record with the deleted ids
      // in getDeletedList
      if ( getDeletedList.containsKey( con.getId() ) ) {
        // this record was deleted in the specified range datetime
        // We will return it
        retval.setRecordValue( con );
        retval.setDeletionDate( getDeletedList.get( con.getId() ) );
      } else if ( index < getRecordsCount() - 1 ) {
        // this record was not deleted in the range datetime
        // let's move forward and see if we find records that might interest us

        while ( con != null && index < getRecordsCount() - 1 && !getDeletedList.containsKey( con.getId() ) ) {
          // still not a record for us !!!
          // let's continue ...
          index++;
          con = this.sObjects[index];
        }
        // if we are here, it means that
        // we found a record to take
        // or we have fetched all available records
        retval.setRecordIndexChanges( true );
        retval.setRecordIndex( index );
        if ( con != null && getChildren( con )[index] != null && getDeletedList.containsKey( con.getId() ) ) {
          retval.setRecordValue( con );
          retval.setDeletionDate( getDeletedList.get( con.getId() ) );
        }
      }
      retval.setAllRecordsProcessed( index >= getRecordsCount() - 1 );
    } else {
      // Case for retrieving record also for updated records
      retval.setRecordValue( con );
    }

    return retval;
  }

  public String getRecordValue( SObject con, String fieldname ) throws KettleException {
    String[] fieldHierarchy = fieldname.split( "\\." );
    if ( con == null ) {
      return null;
    } else {
      XmlObject element = getMessageElementForHierarchy( con, fieldHierarchy );
      if ( element != null ) {
        Object object = element.getValue();
        if ( object != null ) {
          if ( object instanceof QueryResult ) {
            return buildJsonQueryResult( (QueryResult) object );
          }
          return String.valueOf( object );
        } else {
          return (String) element.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Drill down the SObject hierarchy based on the given field hierarchy until either null or the correct MessageElement
   * is found
   */
  private XmlObject getMessageElementForHierarchy( SObject con, String[] fieldHierarchy ) {
    final int lastIndex = fieldHierarchy.length - 1;
    SObject currentSObject = con;
    for ( int index = 0; index <= lastIndex; index++ ) {
      for ( XmlObject element : getChildren( currentSObject ) ) {
        if ( element.getName().getLocalPart().equals( fieldHierarchy[index] ) ) {
          if ( index == lastIndex || ( element.getValue() == null && !element.hasChildren() ) ) {
            return element;
          } else {
            if ( element instanceof  SObject ) {
              // Found the next level, keep going
              currentSObject = (SObject) element;
            }
            break;
          }
        }
      }
    }
    return null;
  }

  @SuppressWarnings( "unchecked" )
  private String buildJsonQueryResult( QueryResult queryResult ) throws KettleException {
    JSONArray list = new JSONArray();
    for ( SObject sobject : queryResult.getRecords() ) {
      list.add( buildJSONSObject( sobject ) );
    }
    StringWriter sw = new StringWriter();
    try {
      list.writeJSONString( sw );
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
    return sw.toString();
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject buildJSONSObject( SObject sobject ) {
    JSONObject jsonObject = new JSONObject();
    for ( XmlObject element : getChildren( sobject ) ) {
      Object object = element.getValue();
      if ( object != null && object instanceof SObject ) {
        jsonObject.put( element.getName(), buildJSONSObject( (SObject) object ) );
      } else {
        jsonObject.put( element.getName(), element.getValue() );
      }
    }
    return jsonObject;
  }

  // Get SOQL meta data (not a Good way but i don't see any other way !)
  // TODO : Go back to this one
  // I am sure there is an easy way to return meta for a SOQL result
  public XmlObject[] getElements() throws Exception {
    // Query first
    this.qr = getBinding().query( getSQL() );
    // and then return records
    SObject con = getQueryResult().getRecords()[0];
    if ( con == null ) {
      return null;
    }
    return getChildren( con );
  }

  public boolean queryMore() throws KettleException {
    try {
      // check the done attribute on the QueryResult and call QueryMore
      // with the QueryLocator if there are more records to be retrieved
      if ( !getQueryResult().isDone() ) {
        this.qr = getBinding().queryMore( getQueryResult().getQueryLocator() );
        this.sObjects = getQueryResult().getRecords();
        if ( this.sObjects != null ) {
          this.recordsCount = this.sObjects.length;
        }
        this.queryResultSize = getQueryResult().getSize();
        return true;
      } else {
        // Query is done .. we finished !
        return false;
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.QueringMore" ), e );
    }
  }

  public String[] getAllAvailableObjects( boolean OnlyQueryableObjects ) throws KettleException {
    DescribeGlobalResult dgr = null;
    List<String> objects = null;
    DescribeGlobalSObjectResult[] sobjectResults = null;
    try {
      // Get object
      dgr = getBinding().describeGlobal();
      // let's get all objects
      sobjectResults = dgr.getSobjects();
      int nrObjects = dgr.getSobjects().length;

      objects = new ArrayList<String>();

      for ( int i = 0; i < nrObjects; i++ ) {
        DescribeGlobalSObjectResult o = dgr.getSobjects()[i];
        if ( ( OnlyQueryableObjects && o.isQueryable() ) || !OnlyQueryableObjects ) {
          objects.add( o.getName() );
        }
      }
      return objects.toArray( new String[objects.size()] );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.Error.GettingModules" ), e );
    } finally {
      if ( dgr != null ) {
        dgr = null;
      }
      if ( objects != null ) {
        objects.clear();
        objects = null;
      }
      if ( sobjectResults != null ) {
        sobjectResults = null;
      }
    }
  }

  public Field[] getObjectFields( String objectName ) throws KettleException {
    DescribeSObjectResult describeSObjectResult = null;
    try {
      // Get object
      describeSObjectResult = getBinding().describeSObject( objectName );
      if ( describeSObjectResult == null ) {
        return null;
      }

      if ( !describeSObjectResult.isQueryable() ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "SalesforceInputDialog.ObjectNotQueryable", this.module ) );
      } else {
        // we can query this object
        return describeSObjectResult.getFields();
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SalesforceInput.Error.GettingModuleFields", this.module ), e );
    } finally {
      if ( describeSObjectResult != null ) {
        describeSObjectResult = null;
      }
    }
  }

  /**Returns only updatable object fields and ID field if <b>excludeNonUpdatableFields</b> is true,
   * otherwise all object field
   * @param objectName the name of Saleforce object
   * @param excludeNonUpdatableFields the flag that indicates if non-updatable fields should be excluded or not
   * @return the list of object fields depending on filter or not non-updatable fields.
   * @throws KettleException if any exception occurs
   */
  public Field[] getObjectFields( String objectName, boolean excludeNonUpdatableFields ) throws KettleException {
    Field[] fieldList = getObjectFields( objectName );
    if ( excludeNonUpdatableFields ) {
      ArrayList<Field> finalFieldList = new ArrayList<Field>();
      for ( Field f : fieldList ) {
        // Leave out fields that can't be updated but
        if ( isIdField( f ) || !f.isCalculated() && f.isUpdateable() ) {
          finalFieldList.add( f );
        }
      }
      fieldList = finalFieldList.toArray( new Field[finalFieldList.size()] );
    }
    return fieldList;
  }

  private boolean isIdField( Field field ) {
    return field.getType() == ID_FIELD_TYPE ? true : false;
  }

  private boolean isReferenceField( Field field ) {
    return field.getType() == REFERENCE_FIELD_TYPE ? true : false;
  }

  /**
   * Method returns specified object's fields' names, use #getObjectFields to get fields itself
   * @param objectName object name
   * @return fields' names
   * @throws KettleException in case of error
   * @see #getObjectFields(String)
   */
  public String[] getFields( String objectName ) throws KettleException {
    return getFields( getObjectFields( objectName ) );
  }

  /**
   * Method returns specified object's fields' names, use #getObjectFields to get fields itself
   *
   * @param objectName
   *          object name
   * @param excludeNonUpdatableFields
   *          the flag that indicates if non-updatable fields should be excluded or not
   * @return fields' names
   * @throws KettleException
   *           in case of error
   */
  public String[] getFields( String objectName, boolean excludeNonUpdatableFields ) throws KettleException {
    return getFields( getObjectFields( objectName, excludeNonUpdatableFields ), excludeNonUpdatableFields );
  }

  /**
   * Method returns names of the fields specified.
   * @param fields fields
   * @return fields' names
   * @throws KettleException in case of error
   * @see #getObjectFields(String)
   */
  public String[] getFields( Field[] fields ) throws KettleException {
    if ( fields != null ) {
      int nrFields = fields.length;
      String[] fieldsMapp = new String[nrFields];

      for ( int i = 0; i < nrFields; i++ ) {
        Field field = fields[i];
        fieldsMapp[i] = field.getName();
      }
      return fieldsMapp;
    }
    return null;
  }

  /**
   * Method returns names of the fields specified.<br>
   * For the type='reference' it also returns name in the
   * <code>format: objectReferenceTo:externalIdField/lookupField</code>
   *
   * @param fields
   *          fields
   * @param excludeNonUpdatableFields
   *          the flag that indicates if non-updatable fields should be excluded or not
   * @return fields' names
   * @throws KettleException
   */
  public String[] getFields( Field[] fields, boolean excludeNonUpdatableFields ) throws KettleException {
    if ( fields != null ) {
      ArrayList<String> fieldsList = new ArrayList<String>( fields.length );
      for ( Field field : fields ) {
        //Add the name of the field - always
        fieldsList.add( field.getName() );
        //Get the referenced to the field object and for this object get all its field to find possible idLookup fields
        if ( isReferenceField( field ) ) {
          String referenceTo = field.getReferenceTo()[0];
          Field[] referenceObjectFields = this.getObjectFields( referenceTo, excludeNonUpdatableFields );

          for ( Field f : referenceObjectFields ) {
            if ( f.isIdLookup() && !isIdField( f ) ) {
              fieldsList.add( String.format( "%s:%s/%s", referenceTo, f.getName(), field.getRelationshipName() ) );
            }
          }
        }
      }
      return fieldsList.toArray( new String[fieldsList.size()] );
    }
    return null;
  }


  public UpsertResult[] upsert( String upsertField, SObject[] sfBuffer ) throws KettleException {
    try {
      return getBinding().upsert( upsertField, sfBuffer );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.ErrorUpsert", e ) );
    }
  }

  public SaveResult[] insert( SObject[] sfBuffer ) throws KettleException {
    try {
      List<SObject> normalizedSfBuffer = new ArrayList<>();
      for ( SObject part : sfBuffer ) {
        if ( part != null ) {
          normalizedSfBuffer.add( part );
        }
      }
      return getBinding().create( normalizedSfBuffer.toArray( new SObject[normalizedSfBuffer.size()] ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.ErrorInsert", e ) );
    }
  }

  public SaveResult[] update( SObject[] sfBuffer ) throws KettleException {
    try {
      return getBinding().update( sfBuffer );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.ErrorUpdate", e ) );
    }
  }

  public DeleteResult[] delete( String[] id ) throws KettleException {
    try {
      return getBinding().delete( id );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SalesforceInput.ErrorDelete", e ) );
    }
  }

  public static XmlObject createMessageElement( String name, Object value, boolean useExternalKey ) throws Exception {

    XmlObject me = null;

    if ( useExternalKey ) {
      // We use an external key
      // the structure should be like this :
      // object:externalId/lookupField
      // where
      // object is the type of the object
      // externalId is the name of the field in the object to resolve the value
      // lookupField is the name of the field in the current object to update (is the "__r" version)

      int indexOfType = name.indexOf( ":" );
      if ( indexOfType > 0 ) {
        String type = name.substring( 0, indexOfType );
        String extIdName = null;
        String lookupField = null;

        String rest = name.substring( indexOfType + 1, name.length() );
        int indexOfExtId = rest.indexOf( "/" );
        if ( indexOfExtId > 0 ) {
          extIdName = rest.substring( 0, indexOfExtId );
          lookupField = rest.substring( indexOfExtId + 1, rest.length() );
        } else {
          extIdName = rest;
          lookupField = extIdName;
        }
        me = createForeignKeyElement( type, lookupField, extIdName, value );
      } else {
        throw new KettleException( BaseMessages.getString( PKG, "SalesforceConnection.UnableToFindObjectType" ) );
      }
    } else {
      me = fromTemplateElement( name, value, true );
    }

    return me;
  }

  private static XmlObject createForeignKeyElement( String type, String lookupField, String extIdName,
    Object extIdValue ) throws Exception {

    // Foreign key relationship to the object
    XmlObject me = fromTemplateElement( lookupField, null, false );
    me.addField( "type", type );
    me.addField( extIdName, extIdValue );

    return me;
  }

  public static XmlObject fromTemplateElement( String name, Object value, boolean setValue ) throws SOAPException {
    // Use the TEMPLATE org.w3c.dom.Element to create new Message Elements
    XmlObject me = new XmlObject();
    if ( setValue ) {
      me.setValue( value );
    }
    me.setName( new QName( name ) );
    return me;
  }

  public static XmlObject[] getChildren( SObject object ) {
    List<String> reservedFieldNames = Arrays.asList( "type", "fieldsToNull" );
    if ( object == null ) {
      return null;
    }
    List<XmlObject> children = new ArrayList<>();
    Iterator<XmlObject> iterator = object.getChildren();
    while ( iterator.hasNext() ) {
      XmlObject child = iterator.next();
      if ( child.getName().getNamespaceURI().equals( Constants.PARTNER_SOBJECT_NS )
          && reservedFieldNames.contains( child.getName().getLocalPart() ) ) {
        continue;
      }
      children.add( child );
    }
    if ( children.size() == 0 ) {
      return null;
    }
    return children.toArray( new XmlObject[children.size()] );
  }
}
