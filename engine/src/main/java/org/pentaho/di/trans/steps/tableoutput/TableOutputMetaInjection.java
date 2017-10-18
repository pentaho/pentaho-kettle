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

package org.pentaho.di.trans.steps.tableoutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * This takes care of the external metadata injection into the TableOutputMeta class
 *
 * @author Chris
 */
public class TableOutputMetaInjection implements StepMetaInjectionInterface {

  public enum Entry implements StepMetaInjectionEntryInterface {

    TARGET_SCHEMA( ValueMetaInterface.TYPE_STRING, "The target schema" ),
      TARGET_TABLE( ValueMetaInterface.TYPE_STRING, "The target table" ),
      COMMIT_SIZE( ValueMetaInterface.TYPE_STRING, "The commit size" ),
      TRUNCATE_TABLE( ValueMetaInterface.TYPE_STRING, "Truncate table? (Y/N)" ),
      SPECIFY_DATABASE_FIELDS( ValueMetaInterface.TYPE_STRING, "Specify database fields? (Y/N)" ),
      IGNORE_INSERT_ERRORS( ValueMetaInterface.TYPE_STRING, "Ignore insert errors? (Y/N)" ),
      USE_BATCH_UPDATE( ValueMetaInterface.TYPE_STRING, "Use batch update for inserts? (Y/N)" ),

      PARTITION_OVER_TABLES( ValueMetaInterface.TYPE_STRING, "Partition data over tables? (Y/N)" ),
      PARTITIONING_FIELD( ValueMetaInterface.TYPE_STRING, "Partitioning field" ),
      PARTITION_DATA_PER( ValueMetaInterface.TYPE_STRING, "Partition data per (month/day)" ),

      TABLE_NAME_DEFINED_IN_FIELD( ValueMetaInterface.TYPE_STRING,
        "Is the name of the table defined in a field? (Y/N)" ),
      TABLE_NAME_FIELD( ValueMetaInterface.TYPE_STRING, "Field that contains the name of table" ),
      STORE_TABLE_NAME( ValueMetaInterface.TYPE_STRING, "Store the tablename field? (Y/N)" ),

      RETURN_AUTO_GENERATED_KEY( ValueMetaInterface.TYPE_STRING, "Return auto-generated key? (Y/N)" ),
      AUTO_GENERATED_KEY_FIELD( ValueMetaInterface.TYPE_STRING, "Name of auto-generated key field" ),

      DATABASE_FIELDS( ValueMetaInterface.TYPE_NONE, "The database fields" ),
      DATABASE_FIELD( ValueMetaInterface.TYPE_NONE, "One database field" ),
      DATABASE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Table field" ),
      STREAM_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Stream field" );

    private int valueType;
    private String description;

    private Entry( int valueType, String description ) {
      this.valueType = valueType;
      this.description = description;
    }

    /**
     * @return the valueType
     */
    public int getValueType() {
      return valueType;
    }

    /**
     * @return the description
     */
    public String getDescription() {
      return description;
    }

    public static Entry findEntry( String key ) {
      return Entry.valueOf( key );
    }
  }

  private TableOutputMeta meta;

  public TableOutputMetaInjection( TableOutputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.TARGET_SCHEMA, Entry.TARGET_TABLE, Entry.COMMIT_SIZE, Entry.TRUNCATE_TABLE,
        Entry.SPECIFY_DATABASE_FIELDS, Entry.IGNORE_INSERT_ERRORS, Entry.USE_BATCH_UPDATE,
        Entry.PARTITION_OVER_TABLES, Entry.PARTITIONING_FIELD, Entry.PARTITION_DATA_PER,
        Entry.TABLE_NAME_DEFINED_IN_FIELD, Entry.TABLE_NAME_FIELD, Entry.STORE_TABLE_NAME,
        Entry.RETURN_AUTO_GENERATED_KEY, Entry.AUTO_GENERATED_KEY_FIELD, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The fields
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.DATABASE_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.DATABASE_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.DATABASE_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.DATABASE_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.DATABASE_FIELDNAME, Entry.STREAM_FIELDNAME, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<String> databaseFields = new ArrayList<String>();
    List<String> streamFields = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case DATABASE_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.DATABASE_FIELD ) {

              String databaseFieldname = null;
              String streamFieldname = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case DATABASE_FIELDNAME:
                      databaseFieldname = value;
                      break;
                    case STREAM_FIELDNAME:
                      streamFieldname = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              databaseFields.add( databaseFieldname );
              streamFields.add( streamFieldname );
            }
          }
          break;

        case TARGET_SCHEMA:
          meta.setSchemaName( lookValue );
          break;
        case TARGET_TABLE:
          meta.setTableName( lookValue );
          break;
        case COMMIT_SIZE:
          meta.setCommitSize( lookValue );
          break;
        case TRUNCATE_TABLE:
          meta.setTruncateTable( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case SPECIFY_DATABASE_FIELDS:
          meta.setSpecifyFields( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case IGNORE_INSERT_ERRORS:
          meta.setIgnoreErrors( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case USE_BATCH_UPDATE:
          meta.setUseBatchUpdate( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case PARTITION_OVER_TABLES:
          meta.setPartitioningEnabled( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case PARTITIONING_FIELD:
          meta.setPartitioningField( lookValue );
          break;
        case PARTITION_DATA_PER:
          meta.setPartitioningDaily( "DAY".equalsIgnoreCase( lookValue ) );
          meta.setPartitioningMonthly( "MONTH".equalsIgnoreCase( lookValue ) );
          break;
        case TABLE_NAME_DEFINED_IN_FIELD:
          meta.setTableNameInField( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case TABLE_NAME_FIELD:
          meta.setTableNameField( lookValue );
          break;
        case STORE_TABLE_NAME:
          meta.setTableNameInTable( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case RETURN_AUTO_GENERATED_KEY:
          meta.setReturningGeneratedKeys( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case AUTO_GENERATED_KEY_FIELD:
          meta.setGeneratedKeyField( lookValue );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    //
    if ( databaseFields.size() > 0 ) {
      meta.setFieldDatabase( databaseFields.toArray( new String[databaseFields.size()] ) );
      meta.setFieldStream( streamFields.toArray( new String[streamFields.size()] ) );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> list = new ArrayList<StepInjectionMetaEntry>();

    list.add( StepInjectionUtil.getEntry( Entry.TARGET_SCHEMA, meta.getSchemaName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TARGET_TABLE, meta.getTableName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.COMMIT_SIZE, meta.getCommitSize() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TRUNCATE_TABLE, meta.truncateTable() ) );
    list.add( StepInjectionUtil.getEntry( Entry.SPECIFY_DATABASE_FIELDS, meta.specifyFields() ) );
    list.add( StepInjectionUtil.getEntry( Entry.IGNORE_INSERT_ERRORS, meta.ignoreErrors() ) );
    list.add( StepInjectionUtil.getEntry( Entry.USE_BATCH_UPDATE, meta.useBatchUpdate() ) );

    list.add( StepInjectionUtil.getEntry( Entry.PARTITION_OVER_TABLES, meta.isPartitioningEnabled() ) );
    list.add( StepInjectionUtil.getEntry( Entry.PARTITIONING_FIELD, meta.getPartitioningField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.PARTITION_DATA_PER, meta.isPartitioningDaily()
      ? "DAY"
      : meta.isPartitioningMonthly() ? "MONTH" : "" ) );

    list.add( StepInjectionUtil.getEntry( Entry.TABLE_NAME_DEFINED_IN_FIELD, meta.isTableNameInField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TABLE_NAME_FIELD, meta.getTableNameField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.STORE_TABLE_NAME, meta.isTableNameInTable() ) );

    list.add( StepInjectionUtil.getEntry( Entry.RETURN_AUTO_GENERATED_KEY, meta.isReturningGeneratedKeys() ) );
    list.add( StepInjectionUtil.getEntry( Entry.AUTO_GENERATED_KEY_FIELD, meta.getGeneratedKeyField() ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.DATABASE_FIELDS );
    list.add( fieldsEntry );
    for ( int i = 0; i < meta.getFieldDatabase().length; i++ ) {
      StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.DATABASE_FIELD );
      List<StepInjectionMetaEntry> details = fieldEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.DATABASE_FIELDNAME, meta.getFieldDatabase()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.STREAM_FIELDNAME, meta.getFieldStream()[i] ) );
      fieldsEntry.getDetails().add( fieldEntry );
    }

    return list;
  }

  public TableOutputMeta getMeta() {
    return meta;
  }
}
