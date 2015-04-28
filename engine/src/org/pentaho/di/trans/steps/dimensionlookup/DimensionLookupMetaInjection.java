/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.dimensionlookup;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * This takes care of the external metadata injection into the DimensionLookupMeta class
 *
 * @author Chris
 */
public class DimensionLookupMetaInjection implements StepMetaInjectionInterface {

  public enum Entry implements StepMetaInjectionEntryInterface {

    UPDATE_DIMENSION( ValueMetaInterface.TYPE_STRING, "Update the dimension? (Y/N)" ),
    TARGET_SCHEMA( ValueMetaInterface.TYPE_STRING, "The target schema" ),
    TARGET_TABLE( ValueMetaInterface.TYPE_STRING, "The target table" ),
    COMMIT_SIZE( ValueMetaInterface.TYPE_STRING, "The commit size" ),
    PRELOAD_CACHE( ValueMetaInterface.TYPE_STRING, "Pre-load the cache? (Y/N)" ),
    CACHE_SIZE( ValueMetaInterface.TYPE_STRING, "Cache size in rows" ),

    TECHNICAL_KEY_FIELD( ValueMetaInterface.TYPE_STRING, "The technical key field" ),
    TECHNICAL_KEY_NEW_NAME( ValueMetaInterface.TYPE_STRING, "The new name for the technical key" ),
    TECHNICAL_KEY_CREATION( ValueMetaInterface.TYPE_STRING, "The technical key creation method. ("
      + DimensionLookupMeta.CREATION_METHOD_AUTOINC + ", " + DimensionLookupMeta.CREATION_METHOD_SEQUENCE + ", "
      + DimensionLookupMeta.CREATION_METHOD_TABLEMAX + ")" ),
    TECHNICAL_KEY_SEQUENCE( ValueMetaInterface.TYPE_STRING, "The technical key sequence." ),
    VERSION_FIELD( ValueMetaInterface.TYPE_STRING, "The version field" ),
    STREAM_DATE_FIELD( ValueMetaInterface.TYPE_STRING, "The stream date field" ),
    DATE_RANGE_START_FIELD( ValueMetaInterface.TYPE_STRING, "The date range start field" ),
    MIN_YEAR( ValueMetaInterface.TYPE_STRING, "The minimum year for date range start." ),
    USE_ALTERNATIVE_START_DATE( ValueMetaInterface.TYPE_STRING, "Use an alternative start date? (Y/N)" ),
    ALTERNATIVE_START_OPTION( ValueMetaInterface.TYPE_STRING, "The alternative start date option to use (none, "
      + "sysdate, trans_start, null, column_value)" ),
    ALTERNATIVE_START_COLUMN( ValueMetaInterface.TYPE_STRING, "The alternative start date column to use" ),
    DATE_RANGE_END_FIELD( ValueMetaInterface.TYPE_STRING, "The date range end field" ),
    MAX_YEAR( ValueMetaInterface.TYPE_STRING, "The maximum year for date range end." ),

    KEY_FIELDS( ValueMetaInterface.TYPE_NONE, "The key fields" ),
    KEY_FIELD( ValueMetaInterface.TYPE_NONE, "One key field" ),
    KEY_DATABASE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Table field" ),
    KEY_STREAM_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Stream field" ),

    DATABASE_FIELDS( ValueMetaInterface.TYPE_NONE, "The database fields" ),
    DATABASE_FIELD( ValueMetaInterface.TYPE_NONE, "One database field" ),
    DATABASE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The table field" ),
    STREAM_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The stream field" ),
    UPDATE_TYPE( ValueMetaInterface.TYPE_STRING, "Type of dimension update. If update dimension "
      + "(Insert, Update, Punch through, DateInsertedOrUpdated, DateInserted, DateUpdated, LastVersion)."
      + "If not update dimension (BigNumber, Binary, Boolean, Date, Integer, Internet Address, Number, String,"
      + "Timestamp)." );

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

  private DimensionLookupMeta meta;

  public DimensionLookupMetaInjection( DimensionLookupMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();


    Entry[] topEntries =
        new Entry[] {
            Entry.UPDATE_DIMENSION, Entry.TARGET_SCHEMA, Entry.TARGET_TABLE, Entry.COMMIT_SIZE,
            Entry.PRELOAD_CACHE, Entry.CACHE_SIZE, Entry.TECHNICAL_KEY_FIELD, Entry.TECHNICAL_KEY_NEW_NAME,
            Entry.TECHNICAL_KEY_CREATION, Entry.TECHNICAL_KEY_SEQUENCE, Entry.VERSION_FIELD, Entry.STREAM_DATE_FIELD,
            Entry.DATE_RANGE_START_FIELD, Entry.MIN_YEAR, Entry.USE_ALTERNATIVE_START_DATE,
            Entry.ALTERNATIVE_START_OPTION, Entry.ALTERNATIVE_START_COLUMN, Entry.DATE_RANGE_END_FIELD,
            Entry.MAX_YEAR, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    StepInjectionMetaEntry keysEntry =
        new StepInjectionMetaEntry(
          Entry.KEY_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.KEY_FIELDS.description );
    all.add( keysEntry );
    StepInjectionMetaEntry keyEntry =
        new StepInjectionMetaEntry(
          Entry.KEY_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.KEY_FIELD.description );
    keysEntry.getDetails().add( keyEntry );

    Entry[] keysEntries = new Entry[] { Entry.KEY_DATABASE_FIELDNAME, Entry.KEY_STREAM_FIELDNAME, };
    for ( Entry entry : keysEntries ) {
      StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      keyEntry.getDetails().add( metaEntry );
    }

    StepInjectionMetaEntry fieldsEntry =
        new StepInjectionMetaEntry(
          Entry.DATABASE_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.DATABASE_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
        new StepInjectionMetaEntry(
          Entry.DATABASE_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.DATABASE_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.DATABASE_FIELDNAME, Entry.STREAM_FIELDNAME, Entry.UPDATE_TYPE, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {
    List<String> keyDbFields = new ArrayList<String>();
    List<String> keyStreamFields = new ArrayList<String>();

    List<String> databaseFields = new ArrayList<String>();
    List<String> streamFields = new ArrayList<String>();
    List<String> updateTypes = new ArrayList<String>();

    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case KEY_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.KEY_FIELD ) {
              String keyDbField = null;
              String keyStreamField = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case KEY_DATABASE_FIELDNAME:
                      keyDbField = value;
                      break;
                    case KEY_STREAM_FIELDNAME:
                      keyStreamField = value;
                      break;
                    default:
                      break;
                  }
                }
              }

              keyDbFields.add( keyDbField );
              keyStreamFields.add( keyStreamField );
            }
          }
          break;

        case DATABASE_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.DATABASE_FIELD ) {
              String databaseField = null;
              String streamField = null;
              String updateType = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case DATABASE_FIELDNAME:
                      databaseField = value;
                      break;
                    case STREAM_FIELDNAME:
                      streamField = value;
                      break;
                    case UPDATE_TYPE:
                      updateType = value;
                      break;
                    default:
                      break;
                  }
                }
              }

              databaseFields.add( databaseField );
              streamFields.add( streamField );
              updateTypes.add( updateType );
            }
          }
          break;

        case UPDATE_DIMENSION:
          meta.setUpdate( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case TARGET_SCHEMA:
          meta.setSchemaName( lookValue );
          break;
        case TARGET_TABLE:
          meta.setTableName( lookValue );
          break;
        case COMMIT_SIZE:
          meta.setCommitSize( Const.toInt( lookValue, 1000 ) );
          break;
        case PRELOAD_CACHE:
          meta.setPreloadingCache( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case CACHE_SIZE:
          meta.setCacheSize( Const.toInt( lookValue, 5000 ) );
          break;
        case TECHNICAL_KEY_FIELD:
          meta.setKeyField( lookValue );
          break;
        case TECHNICAL_KEY_NEW_NAME:
          meta.setKeyRename( lookValue );
          break;
        case TECHNICAL_KEY_CREATION:
          meta.setTechKeyCreation( lookValue );
          break;
        case TECHNICAL_KEY_SEQUENCE:
          meta.setSequenceName( lookValue );
          break;
        case VERSION_FIELD:
          meta.setVersionField( lookValue );
          break;
        case STREAM_DATE_FIELD:
          meta.setDateField( lookValue );
          break;
        case DATE_RANGE_START_FIELD:
          meta.setDateFrom( lookValue );
          break;
        case USE_ALTERNATIVE_START_DATE:
          meta.setUsingStartDateAlternative( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ALTERNATIVE_START_OPTION:
          meta.setStartDateAlternative( DimensionLookupMeta.getStartDateAlternative( lookValue ) );
          break;
        case ALTERNATIVE_START_COLUMN:
          meta.setStartDateFieldName( lookValue );
          break;
        case DATE_RANGE_END_FIELD:
          meta.setDateTo( lookValue );
          break;
        case MIN_YEAR:
          meta.setMinYear( Const.toInt( lookValue, 1900 ) );
          break;
        case MAX_YEAR:
          meta.setMaxYear( Const.toInt( lookValue, 2199 ) );
          break;
        default:
          break;
      }
    }

    meta.setKeyLookup( keyDbFields.toArray( new String[keyDbFields.size()] ) );
    meta.setKeyStream( keyStreamFields.toArray( new String[keyStreamFields.size()] ) );

    meta.setFieldLookup( databaseFields.toArray( new String[databaseFields.size()] ) );
    meta.setFieldStream( streamFields.toArray( new String[streamFields.size()] ) );
    int[] updateTypesInt = new int[updateTypes.size()];
    for ( int i = 0; i < updateTypesInt.length; i++ ) {
      updateTypesInt[i] = DimensionLookupMeta.getUpdateType( meta.isUpdate(), updateTypes.get( i ) );
    }
    meta.setFieldUpdate( updateTypesInt );
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {

    List<StepInjectionMetaEntry> list = new ArrayList<StepInjectionMetaEntry>();

    list.add( StepInjectionUtil.getEntry( Entry.UPDATE_DIMENSION, meta.isUpdate() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TARGET_SCHEMA, meta.getSchemaName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TARGET_TABLE, meta.getTableName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.COMMIT_SIZE, meta.getCommitSize() ) );
    list.add( StepInjectionUtil.getEntry( Entry.PRELOAD_CACHE, meta.isPreloadingCache() ) );
    list.add( StepInjectionUtil.getEntry( Entry.COMMIT_SIZE, meta.getCommitSize() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TECHNICAL_KEY_FIELD, meta.getKeyField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TECHNICAL_KEY_NEW_NAME, meta.getKeyRename() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TECHNICAL_KEY_CREATION, meta.getTechKeyCreation() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TECHNICAL_KEY_SEQUENCE, meta.getSequenceName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.VERSION_FIELD, meta.getVersionField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.STREAM_DATE_FIELD, meta.getDateField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.DATE_RANGE_START_FIELD, meta.getDateFrom() ) );
    list.add( StepInjectionUtil.getEntry( Entry.MIN_YEAR, meta.getMinYear() ) );
    list.add( StepInjectionUtil.getEntry( Entry.USE_ALTERNATIVE_START_DATE, meta.isUsingStartDateAlternative() ) );
    list.add( StepInjectionUtil.getEntry( Entry.ALTERNATIVE_START_OPTION,
        DimensionLookupMeta.getStartDateAlternativeCode( meta.getStartDateAlternative() ) ) );
    list.add( StepInjectionUtil.getEntry( Entry.ALTERNATIVE_START_COLUMN, meta.getStartDateFieldName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.DATE_RANGE_END_FIELD, meta.getDateTo() ) );
    list.add( StepInjectionUtil.getEntry( Entry.MAX_YEAR, meta.getMaxYear() ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.DATABASE_FIELDS );
    list.add( fieldsEntry );
    for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
      StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.DATABASE_FIELD );
      List<StepInjectionMetaEntry> details = fieldEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.DATABASE_FIELDNAME, meta.getFieldLookup()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.STREAM_FIELDNAME, meta.getFieldStream()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.UPDATE_TYPE,
          DimensionLookupMeta.getUpdateTypeCode( meta.isUpdate(), meta.getFieldUpdate()[i] ) ) );
      fieldsEntry.getDetails().add( fieldEntry );
    }

    StepInjectionMetaEntry keysEntry = StepInjectionUtil.getEntry( Entry.KEY_FIELDS );
    list.add( keysEntry );
    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      StepInjectionMetaEntry keyEntry = StepInjectionUtil.getEntry( Entry.KEY_FIELD );
      List<StepInjectionMetaEntry> details = keyEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.KEY_DATABASE_FIELDNAME, meta.getKeyLookup() ) );
      details.add( StepInjectionUtil.getEntry( Entry.KEY_STREAM_FIELDNAME, meta.getKeyStream() ) );
    }

    return list;
  }

  public DimensionLookupMeta getMeta() {
    return meta;
  }
}
