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

package org.pentaho.di.trans.steps.groupby;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * This takes care of the external metadata injection into the GroupByMeta class
 *
 * @author Matt
 */
public class GroupByMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    PASS_ALL_ROWS( ValueMetaInterface.TYPE_STRING, "Pass all rows? (Y/N)" ), TEMP_DIRECTORY(
      ValueMetaInterface.TYPE_STRING, "The temporary directory" ), TEMP_FILE_PREFIX(
      ValueMetaInterface.TYPE_STRING, "The temporary file prefix" ), GROUP_LINE_NUMBER_ENABLED(
      ValueMetaInterface.TYPE_STRING, "Group line number enabled? (Y/N)" ), GROUP_LINE_NUMBER_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "Group line number field name" ), ALLWAYS_PASS_A_ROW(
      ValueMetaInterface.TYPE_STRING, "Always give back a row? (Y/N)" ),

      GROUP_FIELDS( ValueMetaInterface.TYPE_NONE, "The group definition fields" ), GROUP_FIELD(
        ValueMetaInterface.TYPE_NONE, "One group definition field" ), GROUP_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Group definition field name" ),

      AGG_FIELDS( ValueMetaInterface.TYPE_NONE, "The aggregation fields" ), AGG_FIELD(
        ValueMetaInterface.TYPE_NONE, "One aggregation field" ), AGG_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Result field name" ), AGG_SUBJECT(
        ValueMetaInterface.TYPE_STRING, "Aggregation subject field name" ), AGG_TYPE(
        ValueMetaInterface.TYPE_STRING,
        "Aggregation type (for allowed values see: http://wiki.pentaho.com/display/EAI/Group+By)" ), AGG_VALUE(
        ValueMetaInterface.TYPE_STRING, "Value (field separator, ...)" );

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

  private GroupByMeta meta;

  public GroupByMetaInjection( GroupByMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.PASS_ALL_ROWS, Entry.TEMP_DIRECTORY, Entry.TEMP_FILE_PREFIX, Entry.GROUP_LINE_NUMBER_ENABLED,
        Entry.GROUP_LINE_NUMBER_FIELDNAME, Entry.ALLWAYS_PASS_A_ROW, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The group
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.GROUP_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.GROUP_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.GROUP_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.GROUP_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.GROUP_FIELDNAME, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    // The aggregations
    //
    StepInjectionMetaEntry aggsEntry =
      new StepInjectionMetaEntry(
        Entry.AGG_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.AGG_FIELDS.description );
    all.add( aggsEntry );
    StepInjectionMetaEntry aggEntry =
      new StepInjectionMetaEntry(
        Entry.AGG_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.AGG_FIELD.description );
    aggsEntry.getDetails().add( aggEntry );

    Entry[] aggEntries = new Entry[] { Entry.AGG_FIELDNAME, Entry.AGG_SUBJECT, Entry.AGG_TYPE, Entry.AGG_VALUE, };
    for ( Entry entry : aggEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      aggEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<String> groupFields = new ArrayList<String>();
    List<String> aggFields = new ArrayList<String>();
    List<String> aggSubjects = new ArrayList<String>();
    List<Integer> aggTypes = new ArrayList<Integer>();
    List<String> aggValues = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case GROUP_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.GROUP_FIELD ) {
              String groupFieldname = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case GROUP_FIELDNAME:
                      groupFieldname = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              groupFields.add( groupFieldname );
            }
          }
          break;

        case AGG_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.AGG_FIELD ) {

              String aggFieldname = null;
              String aggSubject = null;
              int aggType = -1;
              String aggValue = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case AGG_FIELDNAME:
                      aggFieldname = value;
                      break;
                    case AGG_SUBJECT:
                      aggSubject = value;
                      break;
                    case AGG_TYPE:
                      aggType = GroupByMeta.getType( value );
                      break;
                    case AGG_VALUE:
                      aggValue = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              aggFields.add( aggFieldname );
              aggSubjects.add( aggSubject );
              aggTypes.add( aggType );
              aggValues.add( aggValue );
            }
          }
          break;

        case PASS_ALL_ROWS:
          meta.setPassAllRows( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case TEMP_DIRECTORY:
          meta.setDirectory( lookValue );
          break;
        case TEMP_FILE_PREFIX:
          meta.setPrefix( lookValue );
          break;
        case GROUP_LINE_NUMBER_ENABLED:
          meta.setAddingLineNrInGroup( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case GROUP_LINE_NUMBER_FIELDNAME:
          meta.setLineNrInGroupField( lookValue );
          break;
        case ALLWAYS_PASS_A_ROW:
          meta.setAlwaysGivingBackOneRow( "Y".equalsIgnoreCase( lookValue ) );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    //
    if ( groupFields.size() > 0 ) {
      meta.setGroupField( groupFields.toArray( new String[groupFields.size()] ) );
    }
    if ( aggFields.size() > 0 ) {
      meta.setAggregateField( aggFields.toArray( new String[aggFields.size()] ) );
      meta.setSubjectField( aggSubjects.toArray( new String[aggSubjects.size()] ) );
      int[] types = new int[aggTypes.size()];
      for ( int i = 0; i < types.length; i++ ) {
        types[i] = aggTypes.get( i );
      }
      meta.setAggregateType( types );
      meta.setValueField( aggValues.toArray( new String[aggValues.size()] ) );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public GroupByMeta getMeta() {
    return meta;
  }
}
