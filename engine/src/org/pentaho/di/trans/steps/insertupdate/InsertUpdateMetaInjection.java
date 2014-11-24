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

package org.pentaho.di.trans.steps.insertupdate;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * This takes care of the external metadata injection into the InsertUpdateMeta class
 *
 * @author Chris
 */
public class InsertUpdateMetaInjection implements StepMetaInjectionInterface {

  public enum Entry implements StepMetaInjectionEntryInterface {

    TARGET_SCHEMA( ValueMetaInterface.TYPE_STRING, "The target schema" ),
      TARGET_TABLE( ValueMetaInterface.TYPE_STRING, "The target table" ),
      COMMIT_SIZE( ValueMetaInterface.TYPE_STRING, "The commit size" ),
      DONT_UPDATE( ValueMetaInterface.TYPE_STRING, "Don't perform updates? (Y/N)" ),

      COMPARE_FIELDS( ValueMetaInterface.TYPE_STRING, "The fields to compare" ),
      COMPARE_FIELD( ValueMetaInterface.TYPE_STRING, "One field to compare" ),
      COMPARE_DATABASE_FIELD( ValueMetaInterface.TYPE_STRING, "The table field to compare" ),
      COMPARATOR( ValueMetaInterface.TYPE_STRING, "The comparator to use. (=, = ~NULL, <>, <, <=, >, >=, LIKE, BETWEEN, IS NULL, IS NOT NULL)" ),
      COMPARE_STREAM_FIELD( ValueMetaInterface.TYPE_STRING, "The stream field to compare" ),
      COMPARE_STREAM_FIELD2( ValueMetaInterface.TYPE_STRING, "The second stream field to compare" ),


      DATABASE_FIELDS( ValueMetaInterface.TYPE_NONE, "The database fields" ),
      DATABASE_FIELD( ValueMetaInterface.TYPE_NONE, "One database field" ),
      DATABASE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Table field" ),
      STREAM_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Stream field" ),
      UPDATE_FIELD( ValueMetaInterface.TYPE_STRING, "Update field? (Y/N)" );

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

  private InsertUpdateMeta meta;

  public InsertUpdateMetaInjection( InsertUpdateMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.TARGET_SCHEMA, Entry.TARGET_TABLE, Entry.COMMIT_SIZE, Entry.DONT_UPDATE, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The fields
    //
    StepInjectionMetaEntry compareFieldsEntry =
      new StepInjectionMetaEntry(
        Entry.COMPARE_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.COMPARE_FIELDS.description );
    all.add( compareFieldsEntry );
    StepInjectionMetaEntry compareFieldEntry =
      new StepInjectionMetaEntry(
        Entry.COMPARE_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.COMPARE_FIELD.description );
    compareFieldsEntry.getDetails().add( compareFieldEntry );

    Entry[] compareFieldsEntries = new Entry[] { Entry.COMPARE_DATABASE_FIELD, Entry.COMPARATOR,
      Entry.COMPARE_STREAM_FIELD, Entry.COMPARE_STREAM_FIELD2, };
    for ( Entry entry : compareFieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      compareFieldEntry.getDetails().add( metaEntry );
    }

    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.DATABASE_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.DATABASE_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.DATABASE_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.DATABASE_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.DATABASE_FIELDNAME, Entry.STREAM_FIELDNAME, Entry.UPDATE_FIELD, };
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
    List<Boolean> updateFields = new ArrayList<Boolean>();

    List<String> compareTableFields = new ArrayList<String>();
    List<String> comparators = new ArrayList<String>();
    List<String> compareStreamFields = new ArrayList<String>();
    List<String> compareStreamFields2 = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case COMPARE_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.COMPARE_FIELD ) {
              String compareTableField = null;
              String comparator = "=";
              String compareStreamField = null;
              String compareStreamField2 = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch( metaEntry ) {
                    case COMPARE_DATABASE_FIELD:
                      compareTableField = value;
                      break;
                    case COMPARATOR:
                      comparator = value;
                      break;
                    case COMPARE_STREAM_FIELD:
                      compareStreamField = value;
                      break;
                    case COMPARE_STREAM_FIELD2:
                      compareStreamField2 = value;
                      break;
                    default:
                      break;
                  }
                }
              }

              compareTableFields.add( compareTableField );
              comparators.add( comparator );
              compareStreamFields.add( compareStreamField );
              compareStreamFields2.add( compareStreamField2 );
            }
          }
          break;

        case DATABASE_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.DATABASE_FIELD ) {

              String databaseFieldname = null;
              String streamFieldname = null;
              boolean updateField = true;

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
                    case UPDATE_FIELD:
                      updateField = "Y".equalsIgnoreCase( value );
                      break;
                    default:
                      break;
                  }
                }

              }
              databaseFields.add( databaseFieldname );
              streamFields.add( streamFieldname );
              updateFields.add( updateField );
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
        case DONT_UPDATE:
          meta.setUpdateBypassed( "Y".equalsIgnoreCase( lookValue ) );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    //
    if ( databaseFields.size() > 0 ) {
      meta.setUpdateLookup( databaseFields.toArray( new String[databaseFields.size()] ) );
      meta.setUpdateStream( streamFields.toArray( new String[streamFields.size()] ) );
      meta.setUpdate( updateFields.toArray( new Boolean[updateFields.size()] ) );
    }

    if ( compareTableFields.size() > 0 ) {
      meta.setKeyLookup( compareTableFields.toArray( new String[compareTableFields.size()] ) );
      meta.setKeyCondition( comparators.toArray( new String[comparators.size()] ) );
      meta.setKeyStream( compareStreamFields.toArray( new String[compareStreamFields.size()] ) );
      meta.setKeyStream2( compareStreamFields2.toArray( new String[ compareStreamFields2.size()] ) );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> list = new ArrayList<StepInjectionMetaEntry>();

    list.add( StepInjectionUtil.getEntry( Entry.TARGET_SCHEMA, meta.getSchemaName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.TARGET_TABLE, meta.getTableName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.COMMIT_SIZE, meta.getCommitSizeVar() ) );
    list.add( StepInjectionUtil.getEntry( Entry.DONT_UPDATE, meta.isUpdateBypassed() ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.DATABASE_FIELDS );
    list.add( fieldsEntry );
    for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
      StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.DATABASE_FIELD );
      List<StepInjectionMetaEntry> details = fieldEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.DATABASE_FIELDNAME, meta.getUpdateLookup()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.STREAM_FIELDNAME, meta.getUpdateStream()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.UPDATE_FIELD, meta.getUpdate()[i] ) );
      fieldsEntry.getDetails().add( fieldEntry );
    }

    StepInjectionMetaEntry compareFieldsEntry = StepInjectionUtil.getEntry( Entry.COMPARE_FIELDS );
    list.add( compareFieldsEntry );
    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.COMPARE_FIELD );
      List<StepInjectionMetaEntry> details = fieldEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.COMPARE_DATABASE_FIELD, meta.getKeyLookup()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.COMPARATOR, meta.getKeyCondition()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.COMPARE_STREAM_FIELD, meta.getKeyStream()[i] ) );
      details.add( StepInjectionUtil.getEntry( Entry.COMPARE_STREAM_FIELD2, meta.getKeyStream2()[i] ) );
    }

    return list;
  }

  public InsertUpdateMeta getMeta() {
    return meta;
  }
}
