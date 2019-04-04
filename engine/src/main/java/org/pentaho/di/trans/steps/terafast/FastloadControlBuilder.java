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

package org.pentaho.di.trans.steps.terafast;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.core.util.StringListPluginProperty;
import org.pentaho.di.core.util.Utils;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 */
public class FastloadControlBuilder {

  public static final String DATAFILE_COLUMN_SEPERATOR = "|";

  public static final String RECORD_FORMAT_UNFORMATTED = "unformatted";

  public static final String RECORD_VARTEXT = "VARTEXT \"" + DATAFILE_COLUMN_SEPERATOR + "\"";

  /* Fastload default error tables */
  public static final String DEFAULT_ERROR_TABLE1 = "error1";

  public static final String DEFAULT_ERROR_TABLE2 = "error2";

  public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

  public static final String DEFAULT_NULL_VALUE = "?";

  private final StringBuilder builder = new StringBuilder();

  /**
   * Append new line.
   *
   * @return this.
   */
  public FastloadControlBuilder newline() {
    this.builder.append( ';' );
    this.builder.append( SystemUtils.LINE_SEPARATOR );
    return this;
  }

  /**
   * Append log on. Connection string must be in form [DBHOST]/[USER],[PASSWORD], e.g. localtd/user,pass;
   *
   * @param connectionString
   *          the connection string.
   * @return this.
   * @throws IllegalArgumentException
   *           if connection string is blank.
   */
  public FastloadControlBuilder logon( final String connectionString ) throws IllegalArgumentException {
    Assert.assertNotBlank( connectionString, "Connection must not be blank" );
    this.builder.append( "LOGON " ).append( connectionString );
    return this.newline();
  }

  /**
   * Append log on.
   *
   * @param dbhost
   *          DB host, e.g localtd
   * @param user
   *          the user.
   * @param password
   *          the password.
   * @return this
   * @throws IllegalArgumentException
   *           if input is invalid.
   */
  public FastloadControlBuilder logon( final String dbhost, final String user, final String password ) throws IllegalArgumentException {
    Assert.assertNotBlank( dbhost, "DB host must not be blank" );
    Assert.assertNotBlank( user, "User must not be blank" );
    Assert.assertNotNull( password, "Password must not be null" );
    this.builder.append( "LOGON " );
    this.builder.append( dbhost );
    this.builder.append( '/' );
    this.builder.append( user );
    this.builder.append( ',' );
    this.builder.append( password );
    return this.newline();
  }

  /**
   *
   * @param format
   *          the format.
   * @return this.
   * @throws IllegalArgumentException
   *           if format is invalid.
   */
  public FastloadControlBuilder setRecordFormat( final String format ) throws IllegalArgumentException {
    Assert.assertNotBlank( format, "Format must not be blank" );
    return line( "SET RECORD " + format );
  }

  /**
   *
   * @param sessions
   *          the sesssions.
   * @return this.
   * @throws IllegalArgumentException
   *           if sessions <= 0
   */
  public FastloadControlBuilder setSessions( final int sessions ) throws IllegalArgumentException {
    Assert.assertGreaterZero( sessions );
    return line( "SESSIONS " + sessions );
  }

  /**
   *
   * @param errorLimit
   *          the errorLimit.
   * @return this.
   * @throws IllegalArgumentException
   *           if errorLimit <= 0
   */
  public FastloadControlBuilder setErrorLimit( final int errorLimit ) throws IllegalArgumentException {
    Assert.assertGreaterZero( errorLimit );
    return line( "ERRLIMIT " + errorLimit );
  }

  /**
   * @param targetTableFields
   *          ...
   * @param dataFile
   *          ...
   * @return this
   */
  public FastloadControlBuilder define( final RowMetaInterface targetTableFields,
    StringListPluginProperty tableFieldList, final String dataFile ) {
    Assert.assertNotNull( targetTableFields, "fields cannot be null" );
    Assert.assertNotNull( dataFile, "dataFile cannot be null" );

    this.builder.append( "DEFINE " );
    for ( int i = 0; i < targetTableFields.size(); i++ ) {
      ValueMetaInterface value = targetTableFields.getValueMeta( i );
      int tableIndex = tableFieldList.getValue().indexOf( value.getName() );
      if ( tableIndex >= 0 ) {
        this.builder.append( value.getName() );
        // all fields of type VARCHAR. converted by fastload if necessary
        int length = 0;
        if ( value.getType() == ValueMetaInterface.TYPE_DATE ) {
          length = DEFAULT_DATE_FORMAT.length();
        } else {
          length = value.getLength();
        }
        this.builder.append( "("
          + "VARCHAR(" + length + "), nullif = '" + String.format( "%1$" + length + "s", DEFAULT_NULL_VALUE )
          + "'), " );
        this.builder.append( SystemUtils.LINE_SEPARATOR );
      }
    }
    this.builder.append( " NEWLINECHAR(VARCHAR(" + SystemUtils.LINE_SEPARATOR.length() + "))" );
    this.builder.append( " FILE=" + dataFile );
    return this.newline();
  }

  /**
   * @param targetTableFields
   *          ...
   * @param tableFieldList
   * @param tableName
   *          ...
   * @return ...
   */
  public FastloadControlBuilder insert( final RowMetaInterface targetTableFields,
    StringListPluginProperty tableFieldList, final String tableName ) {
    Assert.assertNotNull( targetTableFields, "targetTableFields cannot be null." );
    Assert.assertNotNull( tableName, "TableName cannot be null." );

    this.builder.append( "INSERT INTO " + tableName + "(" );
    for ( int i = 0; i < targetTableFields.size(); i++ ) {
      int tableIndex = tableFieldList.getValue().indexOf( targetTableFields.getValueMeta( i ).getName() );
      if ( tableIndex >= 0 ) {
        this.builder.append( ":" + targetTableFields.getValueMeta( i ).getName() );
        if ( targetTableFields.getValueMeta( i ).getType() == ValueMetaInterface.TYPE_DATE ) {
          this.builder.append( "(DATE, FORMAT '" );
          this.builder.append( DEFAULT_DATE_FORMAT );
          this.builder.append( "')" );
        }
        if ( i < tableFieldList.size() - 1 ) {
          this.builder.append( "," );
        }
      }
    }
    this.builder.append( ")" );
    return this.newline();
  }

  /**
   * show field definition.
   *
   * @return this
   */
  public FastloadControlBuilder show() {
    return line( "SHOW" );
  }

  /**
   *
   * @return this
   */
  public FastloadControlBuilder endLoading() {
    return line( "END LOADING" );
  }

  /**
   * Issue begin loading with default error tables.
   *
   * @param table
   *          the target table.
   * @return this.
   * @throws IllegalArgumentException
   *           if table is invalid.
   */
  public FastloadControlBuilder beginLoading( final String schemaName, final String table ) throws IllegalArgumentException {
    Assert.assertNotBlank( table );
    this.builder.append( "BEGIN LOADING " );
    this.builder.append( table );
    this.builder.append( " ERRORFILES " );
    if ( !Utils.isEmpty( schemaName ) ) {
      this.builder.append( schemaName );
      this.builder.append( "." );
      this.builder.append( DEFAULT_ERROR_TABLE1 );
      this.builder.append( "," );
      this.builder.append( schemaName );
      this.builder.append( "." );
      this.builder.append( DEFAULT_ERROR_TABLE2 );
    } else {
      this.builder.append( DEFAULT_ERROR_TABLE1 );
      this.builder.append( "," );
      this.builder.append( DEFAULT_ERROR_TABLE2 );
    }
    return this.newline();
  }

  /**
   * Append line, nothing if line is blank.
   *
   * @param line
   *          line to append.
   * @return this.
   */
  public FastloadControlBuilder line( final String line ) {
    if ( StringUtils.isBlank( line ) ) {
      return this;
    }
    this.builder.append( line );
    return this.newline();
  }

  /**
   * Append log off.
   *
   * @return this.
   */
  public FastloadControlBuilder logoff() {
    this.builder.append( "LOGOFF" );
    return this.newline();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.builder.toString();
  }

}
