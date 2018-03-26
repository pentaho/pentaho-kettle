/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

public abstract class BaseLogTable {
  public static final String XML_TAG = "field";

  public static String PROP_LOG_TABLE_CONNECTION_NAME = "_LOG_TABLE_CONNECTION_NAME";
  public static String PROP_LOG_TABLE_SCHEMA_NAME = "_LOG_TABLE_SCHEMA_NAME";
  public static String PROP_LOG_TABLE_TABLE_NAME = "_LOG_TABLE_TABLE_NAME";

  public static String PROP_LOG_TABLE_FIELD_ID = "_LOG_TABLE_FIELD_ID";
  public static String PROP_LOG_TABLE_FIELD_NAME = "_LOG_TABLE_FIELD_NAME";
  public static String PROP_LOG_TABLE_FIELD_ENABLED = "_LOG_TABLE_FIELD_ENABLED";
  public static String PROP_LOG_TABLE_FIELD_SUBJECT = "_LOG_TABLE_FIELD_SUBJECT";

  public static String PROP_LOG_TABLE_INTERVAL = "LOG_TABLE_INTERVAL";
  public static String PROP_LOG_TABLE_SIZE_LIMIT = "LOG_TABLE_SIZE_LIMIT";
  public static String PROP_LOG_TABLE_TIMEOUT_DAYS = "_LOG_TABLE_TIMEOUT_IN_DAYS";

  protected VariableSpace space;
  protected HasDatabasesInterface databasesInterface;

  protected String connectionName;

  protected String schemaName;
  protected String tableName;
  protected String timeoutInDays;

  protected List<LogTableField> fields;

  public BaseLogTable( VariableSpace space, HasDatabasesInterface databasesInterface, String connectionName,
    String schemaName, String tableName ) {
    this.space = space;
    this.databasesInterface = databasesInterface;
    this.connectionName = connectionName;
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.fields = new ArrayList<LogTableField>();
  }

  public void replaceMeta( BaseLogTable baseLogTable ) {
    this.space = baseLogTable.space;
    this.databasesInterface = baseLogTable.databasesInterface;
    this.connectionName = baseLogTable.connectionName;
    this.schemaName = baseLogTable.schemaName;
    this.tableName = baseLogTable.tableName;
    this.timeoutInDays = baseLogTable.timeoutInDays;

    fields.clear();
    for ( LogTableField field : baseLogTable.fields ) {
      try {
        fields.add( (LogTableField) field.clone() );
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( "Clone problem with the base log table", e );
      }
    }
  }

  public String toString() {
    if ( isDefined() ) {
      return getDatabaseMeta().getName() + "-" + getActualTableName();
    }
    return super.toString();
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * Save this core information of the log table to the repository using the specified attribute interface.
   *
   * @param attributeInterface
   *          The attribute interface to use to set attributes
   * @throws KettleException
   */
  public void saveToRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException {
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_CONNECTION_NAME, getConnectionName() );
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_SCHEMA_NAME, getSchemaName() );
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_TABLE_NAME, getTableName() );
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_TIMEOUT_DAYS, getTimeoutInDays() );

    // Store the fields too...
    //
    for ( int i = 0; i < getFields().size(); i++ ) {
      LogTableField field = getFields().get( i );
      attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_FIELD_ID + i, field.getId() );
      attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_FIELD_NAME + i, field.getFieldName() );
      attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_FIELD_ENABLED + i, field.isEnabled() );

      if ( field.isSubjectAllowed() ) {
        attributeInterface.setAttribute(
          getLogTableCode() + PROP_LOG_TABLE_FIELD_SUBJECT + i, field.getSubject() == null ? null : field
            .getSubject().toString() );
      }
    }
  }

  public void loadFromRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException {
    connectionName = schemaName = tableName = null;

    String connectionNameFromRepository =
      attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_CONNECTION_NAME );
    if ( connectionNameFromRepository != null ) {
      connectionName = connectionNameFromRepository;
    }
    String schemaNameFromRepository =
      attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_SCHEMA_NAME );
    if ( schemaNameFromRepository != null ) {
      schemaName = schemaNameFromRepository;
    }
    String tableNameFromRepository =
      attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_TABLE_NAME );
    if ( tableNameFromRepository != null ) {
      tableName = tableNameFromRepository;
    }
    timeoutInDays = attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_TIMEOUT_DAYS );
    for ( int i = 0; i < getFields().size(); i++ ) {
      String id = attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_FIELD_ID + i );
      // Only read further if the ID is available.
      // For backward compatibility, this might not be provided yet!
      //
      if ( id != null ) {
        LogTableField field = findField( id );
        if ( field != null ) {
          field.setFieldName( attributeInterface.getAttributeString( getLogTableCode()
            + PROP_LOG_TABLE_FIELD_NAME + i ) );
          field.setEnabled( attributeInterface.getAttributeBoolean( getLogTableCode()
            + PROP_LOG_TABLE_FIELD_ENABLED + i ) );
          if ( field.isSubjectAllowed() ) {
            field.setSubject( attributeInterface.getAttributeString( getLogTableCode()
              + PROP_LOG_TABLE_FIELD_SUBJECT + i ) );
          }
        }
      }
    }
  }

  public abstract String getLogTableCode();

  public abstract String getConnectionNameVariable();

  public abstract String getSchemaNameVariable();

  public abstract String getTableNameVariable();

  /**
   * @return the databaseMeta
   */
  public DatabaseMeta getDatabaseMeta() {

    String name = getActualConnectionName();
    if ( name == null ) {
      return null;
    }
    if ( databasesInterface == null ) {
      return null;
    }

    return databasesInterface.findDatabase( name );
  }

  /**
   * @return the connectionName
   */
  public String getActualConnectionName() {
    String name = space.environmentSubstitute( connectionName );
    if ( Utils.isEmpty( name ) ) {
      name = space.getVariable( getConnectionNameVariable() );
    }
    if ( Utils.isEmpty( name ) ) {
      return null;
    } else {
      return name;
    }
  }

  /**
   * @return the schemaName
   */
  public String getActualSchemaName() {
    if ( !Utils.isEmpty( schemaName ) ) {
      return space.environmentSubstitute( schemaName );
    }

    String name = space.getVariable( getSchemaNameVariable() );
    if ( Utils.isEmpty( name ) ) {
      return null;
    } else {
      return name;
    }
  }

  /**
   * @param schemaName
   *          the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @return the tableName
   */
  public String getActualTableName() {
    if ( !Utils.isEmpty( tableName ) ) {
      return space.environmentSubstitute( tableName );
    }

    String name = space.getVariable( getTableNameVariable() );
    if ( Utils.isEmpty( name ) ) {
      return null;
    } else {
      return name;
    }
  }

  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          the tableName to set
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  public String getQuotedSchemaTableCombination() {
    return getDatabaseMeta().getQuotedSchemaTableCombination( getActualSchemaName(), getActualTableName() );
  }

  /**
   * @return the fields
   */
  public List<LogTableField> getFields() {
    return fields;
  }

  /**
   * @param fields
   *          the fields to set
   */
  public void setFields( List<LogTableField> fields ) {
    this.fields = fields;
  }

  /**
   * Find a log table field in this log table definition. Use the id of the field to do the lookup.
   *
   * @param id
   *          the id of the field to search for
   * @return the log table field or null if nothing was found.
   */
  public LogTableField findField( String id ) {
    for ( LogTableField field : fields ) {
      if ( field.getId().equals( id ) ) {
        return field;
      }
    }
    return null;
  }

  /**
   * Get the subject of a field with the specified ID
   *
   * @param id
   * @return the subject or null if no field could be find with the specified id
   */
  public Object getSubject( String id ) {
    LogTableField field = findField( id );
    if ( field == null ) {
      return null;
    }
    return field.getSubject();
  }

  /**
   * Return the subject in the form of a string for the specified ID.
   *
   * @param id
   *          the id of the field to look for.
   * @return the string of the subject (name of step) or null if nothing was found.
   */
  public String getSubjectString( String id ) {
    LogTableField field = findField( id );
    if ( field == null ) {
      return null;
    }
    if ( field.getSubject() == null ) {
      return null;
    }
    return field.getSubject().toString();
  }

  public boolean containsKeyField() {
    for ( LogTableField field : fields ) {
      if ( field.isKey() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the field that represents the log date field or null if none was defined.
   */
  public LogTableField getLogDateField() {
    for ( LogTableField field : fields ) {
      if ( field.isLogDateField() ) {
        return field;
      }
    }
    return null;
  }

  /**
   * @return the field that represents the key to this logging table (batch id etc)
   */
  public LogTableField getKeyField() {
    for ( LogTableField field : fields ) {
      if ( field.isKey() ) {
        return field;
      }
    }
    return null;
  }

  /**
   * @return the field that represents the logging text (or null if none is found)
   */
  public LogTableField getLogField() {
    for ( LogTableField field : fields ) {
      if ( field.isLogField() ) {
        return field;
      }
    }
    return null;
  }

  /**
   * @return the field that represents the status (or null if none is found)
   */
  public LogTableField getStatusField() {
    for ( LogTableField field : fields ) {
      if ( field.isStatusField() ) {
        return field;
      }
    }
    return null;
  }

  /**
   * @return the field that represents the number of errors (or null if none is found)
   */
  public LogTableField getErrorsField() {
    for ( LogTableField field : fields ) {
      if ( field.isErrorsField() ) {
        return field;
      }
    }
    return null;
  }

  /**
   * @return the field that represents the name of the object that is being used (or null if none is found)
   */
  public LogTableField getNameField() {
    for ( LogTableField field : fields ) {
      if ( field.isNameField() ) {
        return field;
      }
    }
    return null;
  }

  protected String getFieldsXML() {
    StringBuilder retval = new StringBuilder();

    for ( LogTableField field : fields ) {
      retval.append( "        " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );

      retval.append( "          " ).append( XMLHandler.addTagValue( "id", field.getId() ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "enabled", field.isEnabled() ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "name", field.getFieldName() ) );
      if ( field.isSubjectAllowed() ) {
        retval.append( "          " ).append( XMLHandler.addTagValue( "subject",
          field.getSubject() == null ? null : field.getSubject().toString() ) );
      }

      retval.append( "        " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    }

    return retval.toString();
  }

  public void loadFieldsXML( Node node ) {
    int nr = XMLHandler.countNodes( node, BaseLogTable.XML_TAG );
    for ( int i = 0; i < nr; i++ ) {
      Node fieldNode = XMLHandler.getSubNodeByNr( node, BaseLogTable.XML_TAG, i );
      String id = XMLHandler.getTagValue( fieldNode, "id" );
      LogTableField field = findField( id );
      if ( field == null && i < fields.size() ) {
        field = fields.get( i ); // backward compatible until we go GA
      }
      if ( field != null ) {
        field.setFieldName( XMLHandler.getTagValue( fieldNode, "name" ) );
        field.setEnabled( "Y".equalsIgnoreCase( XMLHandler.getTagValue( fieldNode, "enabled" ) ) );
      }
    }
  }

  public boolean isDefined() {
    return getDatabaseMeta() != null && !Utils.isEmpty( getActualTableName() );
  }

  /**
   * @return the timeoutInDays
   */
  public String getTimeoutInDays() {
    return timeoutInDays;
  }

  /**
   * @param timeoutInDays
   *          the timeoutInDays to set
   */
  public void setTimeoutInDays( String timeoutInDays ) {
    this.timeoutInDays = timeoutInDays;
  }

  /**
   * @return the connectionName
   */
  public String getConnectionName() {
    return connectionName;
  }

  /**
   * @param connectionName
   *          the connectionName to set
   */
  public void setConnectionName( String connectionName ) {
    this.connectionName = connectionName;
  }

  protected String getLogBuffer( VariableSpace space, String logChannelId, LogStatus status, String limit ) {

    StringBuffer buffer = KettleLogStore.getAppender().getBuffer( logChannelId, true );

    if ( Utils.isEmpty( limit ) ) {
      String defaultLimit = space.getVariable( Const.KETTLE_LOG_SIZE_LIMIT, null );
      if ( !Utils.isEmpty( defaultLimit ) ) {
        limit = defaultLimit;
      }
    }

    // See if we need to limit the amount of rows
    //
    int nrLines = Utils.isEmpty( limit ) ? -1 : Const.toInt( space.environmentSubstitute( limit ), -1 );

    if ( nrLines > 0 ) {
      int start = buffer.length() - 1;
      for ( int i = 0; i < nrLines && start > 0; i++ ) {
        start = buffer.lastIndexOf( Const.CR, start - 1 );
      }
      if ( start > 0 ) {
        buffer.delete( 0, start + Const.CR.length() );
      }
    }

    return buffer.append( Const.CR + status.getStatus().toUpperCase() + Const.CR ).toString();
  }

  // PDI-7070: implement equals for comparison of job/trans log table to its parent log table
  @Override
  public boolean equals( Object obj ) {
    if ( obj == null || !( obj instanceof BaseLogTable ) ) {
      return false;
    }
    BaseLogTable blt = (BaseLogTable) obj;

    // Get actual names for comparison
    String cName = this.getActualConnectionName();
    String sName = this.getActualSchemaName();
    String tName = this.getActualTableName();

    return ( ( cName == null ? blt.getActualConnectionName() == null : cName
      .equals( blt.getActualConnectionName() ) )
      && ( sName == null ? blt.getActualSchemaName() == null : sName.equals( blt.getActualSchemaName() ) )
      && ( tName == null ? blt.getActualTableName() == null : tName.equals( blt.getActualTableName() ) ) );
  }

  public void setAllGlobalParametersToNull() {
    boolean clearGlobalVariables = Boolean.valueOf( System.getProperties().getProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" ) );
    if ( clearGlobalVariables ) {
      schemaName = isGlobalParameter( schemaName ) ? null : schemaName;
      connectionName = isGlobalParameter( connectionName ) ? null : connectionName;
      tableName = isGlobalParameter( tableName ) ? null : tableName;
      timeoutInDays = isGlobalParameter( timeoutInDays ) ? null : timeoutInDays;
    }
  }

  protected boolean isGlobalParameter( String parameter ) {
    if ( parameter == null ) {
      return false;
    }

    if ( parameter.startsWith( "${" ) && parameter.endsWith( "}" ) ) {
      return System.getProperty( parameter.substring( 2, parameter.length() - 1 ) ) != null;
    }

    return false;
  }
}
