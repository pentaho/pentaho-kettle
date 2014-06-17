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

package org.pentaho.di.trans.steps.exceloutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * Injection support for the Excel Writer step.
 *
 * Injection only supported for the name, type, and format of the output field.
 *
 * @author Jeffrey Lo
 */
public class ExcelOutputMetaInjection implements StepMetaInjectionInterface {

  private ExcelOutputMeta meta;

  public ExcelOutputMetaInjection( ExcelOutputMeta meta ) {
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

    List<ExcelOutputField> excelOutputFields = new ArrayList<ExcelOutputField>();

    // Parse the fields in the Excel Step, setting the metdata based on values passed.

    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        if ( fieldsEntry == Entry.FIELDS ) {
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry != null ) {
              if ( fieldEntry == Entry.FIELD ) {

                ExcelOutputField excelOutputField = new ExcelOutputField();

                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for ( StepInjectionMetaEntry entry : entries ) {
                  Entry metaEntry = Entry.findEntry( entry.getKey() );
                  if ( metaEntry != null ) {
                    String value = (String) entry.getValue();
                    switch ( metaEntry ) {
                      case NAME:
                        excelOutputField.setName( value );
                        break;
                      case TYPE:
                        excelOutputField.setType( value );
                        break;
                      case FORMAT:
                        excelOutputField.setFormat( value );
                        break;
                      default:
                        break;
                    }
                  }
                }

                excelOutputFields.add( excelOutputField );
              }
            }
          }
        }
      }
    }

    // Pass the grid to the step metadata

    meta.allocate( excelOutputFields.size() );
    for ( int i = 0; i < excelOutputFields.size(); i++ ) {

      ExcelField outputField = new ExcelField();

      outputField.setName( excelOutputFields.get( i ).getName() );
      outputField.setType( excelOutputFields.get( i ).getTypeDesc() );
      outputField.setFormat( excelOutputFields.get( i ).getFormat() );

      //CHECKSTYLE:Indentation:OFF
      meta.getOutputFields()[i] = outputField;
    }

  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public ExcelOutputMeta getMeta() {
    return meta;
  }

  private class ExcelOutputField {
    private String name;
    private int type;
    private String format;

    /**
     * @param name
     * @param type
     * @param norm
     */
    private ExcelOutputField() {
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
     * @return the type
     */
    public String getTypeDesc() {
      return ValueMeta.getTypeDesc( type );
    }

    /**
     * @param type
     *          the type to set
     */
    public void setType( String typeDesc ) {
      this.type = ValueMeta.getType( typeDesc );
    }

    /**
     * @return the format
     */
    public String getFormat() {
      return format;
    }

    /**
     * @param format
     *          the format to set
     */
    public void setFormat( String format ) {
      this.format = format;
    }

  }

  private enum Entry {

    FIELDS( ValueMetaInterface.TYPE_NONE, "All the fields" ), FIELD( ValueMetaInterface.TYPE_NONE, "One field" ),

      NAME( ValueMetaInterface.TYPE_STRING, "Input field name" ), TYPE(
        ValueMetaInterface.TYPE_STRING, "Type field value" ), FORMAT(
        ValueMetaInterface.TYPE_STRING, "Format field name" );

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
