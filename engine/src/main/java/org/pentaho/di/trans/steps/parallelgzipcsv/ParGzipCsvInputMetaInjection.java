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

package org.pentaho.di.trans.steps.parallelgzipcsv;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * This takes care of the external metadata injection into the ParGzipCsvInputMeta class
 *
 * @author Matt
 */
public class ParGzipCsvInputMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    FILENAME( ValueMetaInterface.TYPE_STRING, "The file name to read" ), FILENAME_FIELD(
      ValueMetaInterface.TYPE_STRING, "The filename field (if the step reads file names)" ),
      INCLUDING_FILENAMES( ValueMetaInterface.TYPE_STRING, "Include file name in output? (Y/N)" ),
      ROW_NUMBER_FIELD(
        ValueMetaInterface.TYPE_STRING, "The row number field" ), HEADER_PRESENT(
        ValueMetaInterface.TYPE_STRING, "Is there a header row? (Y/N)" ), DELIMITER(
        ValueMetaInterface.TYPE_STRING, "The field delimiter" ), ENCLOSURE(
        ValueMetaInterface.TYPE_STRING, "The field enclosure" ), BUFFER_SIZE(
        ValueMetaInterface.TYPE_STRING, "I/O buffer size" ), LAZY_CONVERSION(
        ValueMetaInterface.TYPE_STRING, "Use lazy conversion? (Y/N)" ), ADD_FILES_TO_RESULT(
        ValueMetaInterface.TYPE_STRING, "Add files to result? (Y/N)" ), RUN_IN_PARALLEL(
        ValueMetaInterface.TYPE_STRING, "Run in parallel? (Y/N)" ), ENCODING(
        ValueMetaInterface.TYPE_STRING, "The file encoding" ),

      FIELDS( ValueMetaInterface.TYPE_NONE, "The fields" ), FIELD( ValueMetaInterface.TYPE_NONE, "One field" ),
      FIELD_NAME( ValueMetaInterface.TYPE_STRING, "Name" ), FIELD_POSITION(
        ValueMetaInterface.TYPE_STRING, "Position" ), FIELD_LENGTH( ValueMetaInterface.TYPE_STRING, "Length" ),
      FIELD_TYPE( ValueMetaInterface.TYPE_STRING, "Data type (String, Number, ...)" ), FIELD_IGNORE(
        ValueMetaInterface.TYPE_STRING, "Ignore? (Y/N)" ),
      FIELD_FORMAT( ValueMetaInterface.TYPE_STRING, "Format" ), FIELD_TRIM_TYPE(
        ValueMetaInterface.TYPE_STRING, "Trim type (none, left, right, both)" ), FIELD_PRECISION(
        ValueMetaInterface.TYPE_STRING, "Precision" ), FIELD_DECIMAL(
        ValueMetaInterface.TYPE_STRING, "Decimal symbol" ), FIELD_GROUP(
        ValueMetaInterface.TYPE_STRING, "Grouping symbol" ), FIELD_CURRENCY(
        ValueMetaInterface.TYPE_STRING, "Currency symbol" ), FIELD_REPEAT(
        ValueMetaInterface.TYPE_STRING, "Repeat values? (Y/N)" ), FIELD_NULL_STRING(
        ValueMetaInterface.TYPE_STRING, "The null string" ), FIELD_IF_NULL(
        ValueMetaInterface.TYPE_STRING, "The default value if null" );

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

  private ParGzipCsvInputMeta meta;

  public ParGzipCsvInputMetaInjection( ParGzipCsvInputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.FILENAME, Entry.FILENAME_FIELD, Entry.INCLUDING_FILENAMES, Entry.ROW_NUMBER_FIELD,
        Entry.HEADER_PRESENT, Entry.DELIMITER, Entry.ENCLOSURE, Entry.BUFFER_SIZE, Entry.LAZY_CONVERSION,
        Entry.ADD_FILES_TO_RESULT, Entry.RUN_IN_PARALLEL, Entry.ENCODING, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The fields...
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry( Entry.FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry( Entry.FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] aggEntries =
      new Entry[] {
        Entry.FIELD_NAME, Entry.FIELD_POSITION, Entry.FIELD_LENGTH, Entry.FIELD_TYPE, Entry.FIELD_IGNORE,
        Entry.FIELD_FORMAT, Entry.FIELD_TRIM_TYPE, Entry.FIELD_PRECISION, Entry.FIELD_DECIMAL,
        Entry.FIELD_GROUP, Entry.FIELD_CURRENCY, Entry.FIELD_REPEAT, Entry.FIELD_NULL_STRING,
        Entry.FIELD_IF_NULL, };
    for ( Entry entry : aggEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<TextFileInputField> fields = new ArrayList<TextFileInputField>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.FIELD ) {

              TextFileInputField field = new TextFileInputField();

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case FIELD_NAME:
                      field.setName( value );
                      break;
                    case FIELD_POSITION:
                      field.setPosition( Const.toInt( value, -1 ) );
                      break;
                    case FIELD_LENGTH:
                      field.setLength( Const.toInt( value, -1 ) );
                      break;
                    case FIELD_TYPE:
                      field.setType( ValueMetaFactory.getIdForValueMeta( value ) );
                      break;
                    case FIELD_IGNORE:
                      field.setIgnored( "Y".equalsIgnoreCase( value ) );
                      break;
                    case FIELD_FORMAT:
                      field.setFormat( value );
                      break;
                    case FIELD_TRIM_TYPE:
                      field.setTrimType( ValueMetaString.getTrimTypeByCode( value ) );
                      break;
                    case FIELD_PRECISION:
                      field.setPrecision( Const.toInt( value, -1 ) );
                      break;
                    case FIELD_DECIMAL:
                      field.setDecimalSymbol( value );
                      break;
                    case FIELD_GROUP:
                      field.setGroupSymbol( value );
                      break;
                    case FIELD_CURRENCY:
                      field.setCurrencySymbol( value );
                      break;
                    case FIELD_REPEAT:
                      field.setRepeated( "Y".equalsIgnoreCase( value ) );
                      break;
                    case FIELD_NULL_STRING:
                      field.setNullString( value );
                      break;
                    case FIELD_IF_NULL:
                      field.setIfNullValue( value );
                      break;
                    default:
                      break;
                  }
                }
              }
              fields.add( field );
            }
          }
          break;

        case FILENAME:
          meta.setFilename( lookValue );
          break;
        case FILENAME_FIELD:
          meta.setFilenameField( lookValue );
          break;
        case ROW_NUMBER_FIELD:
          meta.setRowNumField( lookValue );
          break;
        case INCLUDING_FILENAMES:
          meta.setIncludingFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case DELIMITER:
          meta.setDelimiter( lookValue );
          break;
        case ENCLOSURE:
          meta.setEnclosure( lookValue );
          break;
        case HEADER_PRESENT:
          meta.setHeaderPresent( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case BUFFER_SIZE:
          meta.setBufferSize( lookValue );
          break;
        case LAZY_CONVERSION:
          meta.setLazyConversionActive( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ADD_FILES_TO_RESULT:
          meta.setAddResultFile( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case RUN_IN_PARALLEL:
          meta.setRunningInParallel( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ENCODING:
          meta.setEncoding( lookValue );
          break;
        default:
          break;
      }
    }

    // If we got fields, use them, otherwise leave the defaults alone.
    //
    if ( fields.size() > 0 ) {
      meta.setInputFields( fields.toArray( new TextFileInputField[fields.size()] ) );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public ParGzipCsvInputMeta getMeta() {
    return meta;
  }
}
