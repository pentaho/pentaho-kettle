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

package org.pentaho.di.trans.steps.denormaliser;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * To keep it simple, this metadata injection interface only supports the fields to denormalize for the time being.
 *
 * @author Matt
 */
public class DenormaliserMetaInjection implements StepMetaInjectionInterface {

  private DenormaliserMeta meta;

  public DenormaliserMetaInjection( DenormaliserMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry( "FIELDS", ValueMetaInterface.TYPE_NONE, "All the fields on the spreadsheets" );
    all.add( fieldsEntry );

    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry( "FIELD", ValueMetaInterface.TYPE_NONE, "All the fields on the spreadsheets" );
    fieldsEntry.getDetails().add( fieldEntry );

    for ( Entry entry : Entry.values() ) {
      if ( entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
        StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
        fieldEntry.getDetails().add( metaEntry );
      }
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<DenormaliserTargetField> denormaliserTargetFields = new ArrayList<DenormaliserTargetField>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        if ( fieldsEntry == Entry.FIELDS ) {
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry != null ) {
              if ( fieldEntry == Entry.FIELD ) {

                DenormaliserTargetField inputField = new DenormaliserTargetField();

                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for ( StepInjectionMetaEntry entry : entries ) {
                  Entry metaEntry = Entry.findEntry( entry.getKey() );
                  if ( metaEntry != null ) {
                    String value = (String) entry.getValue();
                    switch ( metaEntry ) {
                      case NAME:
                        inputField.setFieldName( value );
                        break;
                      case KEY_VALUE:
                        inputField.setKeyValue( value );
                        break;
                      case TARGET_NAME:
                        inputField.setTargetName( value );
                        break;
                      case TARGET_TYPE:
                        inputField.setTargetType( ValueMetaFactory.getIdForValueMeta( value ) );
                        break;
                      case TARGET_LENGTH:
                        inputField.setTargetLength( Const.toInt( value, -1 ) );
                        break;
                      case TARGET_PRECISION:
                        inputField.setTargetPrecision( Const.toInt( value, -1 ) );
                        break;
                      case TARGET_CURRENCY:
                        inputField.setTargetCurrencySymbol( value );
                        break;
                      case TARGET_GROUP:
                        inputField.setTargetGroupingSymbol( value );
                        break;
                      case TARGET_DECIMAL:
                        inputField.setTargetDecimalSymbol( value );
                        break;
                      case TARGET_FORMAT:
                        inputField.setTargetFormat( value );
                        break;
                      case TARGET_AGGREGATION:
                        inputField.setTargetAggregationType( DenormaliserTargetField.getAggregationType( value ) );
                        break;
                      default:
                        break;
                    }
                  }
                }

                denormaliserTargetFields.add( inputField );
              }
            }
          }
        }
      }
    }

    if ( !denormaliserTargetFields.isEmpty() ) {
      // Pass the grid to the step metadata
      //
      meta.setDenormaliserTargetField( denormaliserTargetFields.toArray(
          new DenormaliserTargetField[denormaliserTargetFields.size()] ) );
    }

  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public DenormaliserMeta getMeta() {
    return meta;
  }

  private enum Entry {

    FIELDS( ValueMetaInterface.TYPE_NONE, "All the fields" ),
      FIELD( ValueMetaInterface.TYPE_NONE, "One field" ),

      TARGET_NAME( ValueMetaInterface.TYPE_STRING, "Target field name" ),
      NAME( ValueMetaInterface.TYPE_STRING, "Value field name" ),
      KEY_VALUE( ValueMetaInterface.TYPE_STRING, "Key value" ),
      TARGET_TYPE( ValueMetaInterface.TYPE_STRING, "Target field type" ),
      TARGET_LENGTH( ValueMetaInterface.TYPE_STRING, "Target field length" ),
      TARGET_PRECISION( ValueMetaInterface.TYPE_STRING, "Target field precision" ),
      TARGET_CURRENCY( ValueMetaInterface.TYPE_STRING, "Target field currency symbol" ),
      TARGET_DECIMAL( ValueMetaInterface.TYPE_STRING, "Target field decimal symbol" ),
      TARGET_GROUP( ValueMetaInterface.TYPE_STRING, "Target field group symbol" ),
      TARGET_FORMAT( ValueMetaInterface.TYPE_STRING, "Target field format" ),
      TARGET_AGGREGATION(
        ValueMetaInterface.TYPE_STRING, "Target aggregation (-, SUM, AVERAGE, MIN, MAX, COUNT_ALL, CONCAT_COMMA)" );

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

}
