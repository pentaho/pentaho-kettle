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

package org.pentaho.di.www;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class SlaveSequence {
  public static final String XML_TAG = "sequence";

  /**
   * The name of the slave sequence
   */
  private String name;

  /**
   * The start value
   */
  private long startValue;

  /**
   * The database to use
   */
  private DatabaseMeta databaseMeta;

  /**
   * The schema to use
   */
  private String schemaName;

  /**
   * The table to use
   */
  private String tableName;

  /**
   * The sequence name field in the table
   */
  private String sequenceNameField;

  /**
   * The current value of the sequence
   */
  private String valueField;

  public SlaveSequence() {
    startValue = 1;
  }

  /**
   * @param name
   * @param startValue
   * @param incrementValue
   * @param databaseMeta
   * @param schemaName
   * @param tableName
   * @param sequenceNameField
   * @param valueField
   */
  public SlaveSequence( String name, long startValue, DatabaseMeta databaseMeta, String schemaName,
    String tableName, String sequenceNameField, String valueField ) {
    this.name = name;
    this.startValue = startValue;
    this.databaseMeta = databaseMeta;
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.sequenceNameField = sequenceNameField;
    this.valueField = valueField;
  }

  public synchronized long getNextValue( LoggingObjectInterface log, long incrementValue ) throws KettleException {

    Database db = null;
    try {
      db = new Database( log, databaseMeta );
      db.connect();

      String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
      String seqField = databaseMeta.quoteField( sequenceNameField );
      String valField = databaseMeta.quoteField( valueField );

      boolean update = false;

      String sql = "SELECT " + valField + " FROM " + schemaTable + " WHERE " + seqField + " = ?";
      RowMetaAndData param = new RowMetaAndData();
      param.addValue( seqField, ValueMetaInterface.TYPE_STRING, name );
      RowMetaAndData row = db.getOneRow( sql, param.getRowMeta(), param.getData() );
      long value;
      if ( row != null && row.getData() != null ) {
        update = true;
        Long longValue = row.getInteger( 0 );
        if ( longValue == null ) {
          value = startValue;
        } else {
          value = longValue.longValue();
        }
      } else {
        value = startValue;
      }

      long maximum = value + incrementValue;

      // Update the value in the table...
      //
      if ( update ) {
        sql = "UPDATE " + schemaTable + " SET " + valField + "= ? WHERE " + seqField + "= ? ";
        param = new RowMetaAndData();
        param.addValue( valField, ValueMetaInterface.TYPE_INTEGER, Long.valueOf( maximum ) );
        param.addValue( seqField, ValueMetaInterface.TYPE_STRING, name );

      } else {
        sql = "INSERT INTO " + schemaTable + "(" + seqField + ", " + valField + ") VALUES( ? , ? )";
        param = new RowMetaAndData();
        param.addValue( seqField, ValueMetaInterface.TYPE_STRING, name );
        param.addValue( valField, ValueMetaInterface.TYPE_INTEGER, Long.valueOf( maximum ) );
      }
      db.execStatement( sql, param.getRowMeta(), param.getData() );

      return value;

    } catch ( Exception e ) {
      throw new KettleException( "Unable to get next value for slave sequence '"
        + name + "' on database '" + databaseMeta.getName() + "'", e );
    } finally {
      db.disconnect();
    }
  }

  public SlaveSequence( Node node, List<DatabaseMeta> databases ) throws KettleXMLException {
    name = XMLHandler.getTagValue( node, "name" );
    startValue = Const.toInt( XMLHandler.getTagValue( node, "start" ), 0 );
    databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( node, "connection" ) );
    schemaName = XMLHandler.getTagValue( node, "schema" );
    tableName = XMLHandler.getTagValue( node, "table" );
    sequenceNameField = XMLHandler.getTagValue( node, "sequence_field" );
    valueField = XMLHandler.getTagValue( node, "value_field" );
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 100 );

    xml.append( XMLHandler.addTagValue( "name", name ) );
    xml.append( XMLHandler.addTagValue( "start", startValue ) );
    xml.append( XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    xml.append( XMLHandler.addTagValue( "schema", schemaName ) );
    xml.append( XMLHandler.addTagValue( "table", tableName ) );
    xml.append( XMLHandler.addTagValue( "sequence_field", sequenceNameField ) );
    xml.append( XMLHandler.addTagValue( "value_field", valueField ) );

    return xml.toString();
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the startValue
   */
  public long getStartValue() {
    return startValue;
  }

  /**
   * @param startValue
   *          the startValue to set
   */
  public void setStartValue( long startValue ) {
    this.startValue = startValue;
  }

  /**
   * @return the databaseMeta
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param databaseMeta
   *          the databaseMeta to set
   */
  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName
   *          the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  /**
   * @return the tableName
   */
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

  /**
   * @return the sequenceNameField
   */
  public String getSequenceNameField() {
    return sequenceNameField;
  }

  /**
   * @param sequenceNameField
   *          the sequenceNameField to set
   */
  public void setSequenceNameField( String sequenceNameField ) {
    this.sequenceNameField = sequenceNameField;
  }

  /**
   * @return the valueField
   */
  public String getValueField() {
    return valueField;
  }

  /**
   * @param valueField
   *          the valueField to set
   */
  public void setValueField( String valueField ) {
    this.valueField = valueField;
  }

  /**
   * Find a slave sequence with a certain name
   *
   * @param name
   *          the name to look for
   * @return the slave sequence with the specified name or null of the sequence couldn't be found.
   */
  public static SlaveSequence findSlaveSequence( String name, List<SlaveSequence> slaveSequences ) {
    for ( SlaveSequence slaveSequence : slaveSequences ) {
      if ( slaveSequence.getName().equalsIgnoreCase( name ) ) {
        return slaveSequence;
      }
    }
    return null;
  }
}
