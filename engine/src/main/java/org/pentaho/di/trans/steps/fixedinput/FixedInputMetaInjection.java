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

package org.pentaho.di.trans.steps.fixedinput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * Metadata injection interface for the Fixed File Input step.
 *
 * @author Matt
 */
public class FixedInputMetaInjection implements StepMetaInjectionInterface {

  private FixedInputMeta meta;

  public FixedInputMetaInjection( FixedInputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    // Add the fields...
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry( Entry.FIELDS.name(), Entry.FIELDS.getValueType(), Entry.FIELDS
        .getDescription() );
    all.add( fieldsEntry );

    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry( Entry.FIELD.name(), Entry.FIELD.getValueType(), Entry.FIELD.getDescription() );
    fieldsEntry.getDetails().add( fieldEntry );

    for ( Entry entry : Entry.values() ) {
      if ( entry.getParent() == Entry.FIELD ) {
        StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
        fieldEntry.getDetails().add( metaEntry );
      } else {
        if ( entry.getParent() == null && entry != Entry.FIELDS && entry != Entry.FIELD ) {
          StepInjectionMetaEntry metaEntry =
            new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
          all.add( metaEntry );
        }
      }
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<FixedFileInputField> fixedInputFields = new ArrayList<FixedFileInputField>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      String lookValue = (String) lookFields.getValue();
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        switch ( fieldsEntry ) {
          case FIELDS:
            for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
              Entry fieldEntry = Entry.findEntry( lookField.getKey() );
              if ( fieldEntry != null ) {
                if ( fieldEntry == Entry.FIELD ) {

                  FixedFileInputField inputField = new FixedFileInputField();

                  List<StepInjectionMetaEntry> entries = lookField.getDetails();
                  for ( StepInjectionMetaEntry entry : entries ) {
                    Entry metaEntry = Entry.findEntry( entry.getKey() );
                    if ( metaEntry != null ) {
                      String value = (String) entry.getValue();
                      switch ( metaEntry ) {
                        case NAME:
                          inputField.setName( value );
                          break;
                        case TYPE:
                          inputField.setType( ValueMetaFactory.getIdForValueMeta( value ) );
                          break;
                        case WIDTH:
                          inputField.setWidth( Const.toInt( value, -1 ) );
                          break;
                        case LENGTH:
                          inputField.setLength( Const.toInt( value, -1 ) );
                          break;
                        case PRECISION:
                          inputField.setPrecision( Const.toInt( value, -1 ) );
                          break;
                        case CURRENCY:
                          inputField.setCurrency( value );
                          break;
                        case GROUP:
                          inputField.setGrouping( value );
                          break;
                        case DECIMAL:
                          inputField.setDecimal( value );
                          break;
                        case FORMAT:
                          inputField.setFormat( value );
                          break;
                        case TRIM_TYPE:
                          inputField.setTrimType( ValueMetaString.getTrimTypeByCode( value ) );
                          break;
                        default:
                          break;
                      }
                    }
                  }

                  fixedInputFields.add( inputField );
                }
              }
            }
            break;

          case FILENAME:
            meta.setFilename( lookValue );
            break;
          case HEADER_PRESENT:
            meta.setHeaderPresent( "Y".equalsIgnoreCase( lookValue ) );
            break;
          case LINE_WIDTH:
            meta.setLineWidth( lookValue );
            break;
          case BUFFER_SIZE:
            meta.setBufferSize( lookValue );
            break;
          case LAZY_CONVERSION_ACTIVE:
            meta.setLazyConversionActive( "Y".equalsIgnoreCase( lookValue ) );
            break;
          case LINE_FEED_PRESENT:
            meta.setLineFeedPresent( "Y".equalsIgnoreCase( lookValue ) );
            break;
          case RUNNING_IN_PARALLEL:
            meta.setRunningInParallel( "Y".equalsIgnoreCase( lookValue ) );
            break;
          case FILE_TYPE_CODE:
            meta.setFileType( FixedInputMeta.getFileType( lookValue ) );
            break;
          case ADD_TO_RESULT:
            meta.setAddResultFile( "Y".equalsIgnoreCase( lookValue ) );
            break;
          default:
            break;
        }
      }
    }

    // Pass the grid to the step metadata
    //
    meta.setFieldDefinition( fixedInputFields.toArray( new FixedFileInputField[fixedInputFields.size()] ) );
  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public FixedInputMeta getMeta() {
    return meta;
  }

  private enum Entry {

    FIELDS( ValueMetaInterface.TYPE_NONE, "All the data fields in the fixed width file" ), FIELD(
      ValueMetaInterface.TYPE_NONE, "One data field" ),

      NAME( FIELD, ValueMetaInterface.TYPE_STRING, "Field name" ), TYPE(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field data type" ), WIDTH(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field width" ), LENGTH(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field length" ), PRECISION(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field precision" ), FORMAT(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field conversion format" ), TRIM_TYPE(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field trim type (none, left, right, both)" ), CURRENCY(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field currency symbol" ), DECIMAL(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field decimal symbol" ), GROUP(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field group symbol" ),

      FILENAME( ValueMetaInterface.TYPE_STRING, "Filename" ), HEADER_PRESENT(
        ValueMetaInterface.TYPE_STRING, "Header present? (Y/N)" ), LINE_WIDTH(
        ValueMetaInterface.TYPE_STRING, "The line width" ), BUFFER_SIZE(
        ValueMetaInterface.TYPE_STRING, "The buffer size" ), LAZY_CONVERSION_ACTIVE(
        ValueMetaInterface.TYPE_STRING, "Lazy conversion active? (Y/N)" ), LINE_FEED_PRESENT(
        ValueMetaInterface.TYPE_STRING, "Line feed present? (Y/N)" ), RUNNING_IN_PARALLEL(
        ValueMetaInterface.TYPE_STRING, "Running in parallel? (Y/N)" ), FILE_TYPE_CODE(
        ValueMetaInterface.TYPE_STRING, "File type code (NONE, UNIX, DOS)" ), ADD_TO_RESULT(
        ValueMetaInterface.TYPE_STRING, "Add filename to result? (Y/N)" );

    private int valueType;
    private String description;
    private Entry parent;

    private Entry( int valueType, String description ) {
      this.valueType = valueType;
      this.description = description;
    }

    private Entry( Entry parent, int valueType, String description ) {
      this.parent = parent;
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

    public Entry getParent() {
      return parent;
    }
  }
}
