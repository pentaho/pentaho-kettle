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

package org.pentaho.di.trans.steps.concatfields;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;

/**
 * This takes care of the external metadata injection into the TableOutputMeta class
 *
 * @author Chris
 */
public class ConcatFieldsMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    TARGET_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The target field name" ),
      TARGET_LENGTH( ValueMetaInterface.TYPE_STRING, "The length of the target field" ),
      SEPARATOR( ValueMetaInterface.TYPE_STRING, "The separator" ),
      ENCLOSURE( ValueMetaInterface.TYPE_STRING, "The enclosure" ),

      REMOVE_FIELDS( ValueMetaInterface.TYPE_STRING, "Remove selected fields? (Y/N)" ),
      FORCE_ENCLOSURE( ValueMetaInterface.TYPE_STRING, "Force the enclosure around fields? (Y/N)" ),
      DISABLE_ENCLOSURE_FIX( ValueMetaInterface.TYPE_STRING, "Disable the enclosure fix? (Y/N)" ),
      HEADER( ValueMetaInterface.TYPE_STRING, "Include header row? (Y/N)" ),
      FOOTER( ValueMetaInterface.TYPE_STRING, "Include footer row? (Y/N)" ),
      ENCODING( ValueMetaInterface.TYPE_STRING,
        "Encoding type (for allowed values see: http://wiki.pentaho.com/display/EAI/Concat+Fields)" ),
      RIGHT_PAD_FIELDS( ValueMetaInterface.TYPE_STRING, "Right pad fields? (Y/N)" ),
      FAST_DATA_DUMP( ValueMetaInterface.TYPE_STRING, "Fast data dump? (Y/N)" ),
      SPLIT_EVERY( ValueMetaInterface.TYPE_STRING, "Split every ... rows" ),
      ADD_ENDING_LINE( ValueMetaInterface.TYPE_STRING, "Add ending line after last row" ),

      CONCAT_FIELDS( ValueMetaInterface.TYPE_NONE, "The fields to concatenate" ),
      CONCAT_FIELD( ValueMetaInterface.TYPE_NONE, "One field to concatenate" ),
      CONCAT_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Field to concatenate" ),
      CONCAT_TYPE( ValueMetaInterface.TYPE_STRING,
        "Field type (for allowed values see: http://wiki.pentaho.com/display/EAI/Concat+Fields)" ),
      CONCAT_FORMAT( ValueMetaInterface.TYPE_STRING, "Field format" ),
      CONCAT_LENGTH( ValueMetaInterface.TYPE_STRING, "Field length" ),
      CONCAT_PRECISION( ValueMetaInterface.TYPE_STRING, "Field precision" ),
      CONCAT_CURRENCY( ValueMetaInterface.TYPE_STRING, "Field currency symbol" ),
      CONCAT_DECIMAL( ValueMetaInterface.TYPE_STRING, "Field decimal symbol" ),
      CONCAT_GROUP( ValueMetaInterface.TYPE_STRING, "Field grouping symbol" ),
      CONCAT_TRIM( ValueMetaInterface.TYPE_STRING, "Field trim type (none,left,both,right)" ),
      CONCAT_NULL( ValueMetaInterface.TYPE_STRING, "Value to replace nulls with" );

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

  private ConcatFieldsMeta meta;

  public ConcatFieldsMetaInjection( ConcatFieldsMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.TARGET_FIELDNAME, Entry.TARGET_LENGTH, Entry.SEPARATOR, Entry.ENCLOSURE,
        Entry.REMOVE_FIELDS, Entry.FORCE_ENCLOSURE, Entry.DISABLE_ENCLOSURE_FIX,
        Entry.HEADER, Entry.FOOTER, Entry.ENCODING, Entry.RIGHT_PAD_FIELDS,
        Entry.FAST_DATA_DUMP, Entry.SPLIT_EVERY, Entry.ADD_ENDING_LINE, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The fields
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.CONCAT_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.CONCAT_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.CONCAT_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.CONCAT_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.CONCAT_FIELDNAME, Entry.CONCAT_TYPE, Entry.CONCAT_LENGTH,
      Entry.CONCAT_FORMAT, Entry.CONCAT_PRECISION, Entry.CONCAT_CURRENCY, Entry.CONCAT_DECIMAL,
      Entry.CONCAT_GROUP, Entry.CONCAT_TRIM, Entry.CONCAT_NULL, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<String> concatFields = new ArrayList<String>();
    List<String> concatTypes = new ArrayList<String>();
    List<String> concatLengths = new ArrayList<String>();
    List<String> concatFormats = new ArrayList<String>();
    List<String> concatPrecisions = new ArrayList<String>();
    List<String> concatCurrencies = new ArrayList<String>();
    List<String> concatDecimals = new ArrayList<String>();
    List<String> concatGroups = new ArrayList<String>();
    List<String> concatTrims = new ArrayList<String>();
    List<String> concatNulls = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case CONCAT_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.CONCAT_FIELD ) {

              String concatFieldname = null;
              String concatType = null;
              String concatLength = null;
              String concatFormat = null;
              String concatPrecision = null;
              String concatCurrency = null;
              String concatDecimal = null;
              String concatGroup = null;
              String concatTrim = null;
              String concatNull = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case CONCAT_FIELDNAME:
                      concatFieldname = value;
                      break;
                    case CONCAT_TYPE:
                      concatType = value;
                      break;
                    case CONCAT_LENGTH:
                      concatLength = value;
                      break;
                    case CONCAT_FORMAT:
                      concatFormat = value;
                      break;
                    case CONCAT_PRECISION:
                      concatPrecision = value;
                      break;
                    case CONCAT_CURRENCY:
                      concatCurrency = value;
                      break;
                    case CONCAT_DECIMAL:
                      concatDecimal = value;
                      break;
                    case CONCAT_GROUP:
                      concatGroup = value;
                      break;
                    case CONCAT_TRIM:
                      concatTrim = value;
                      break;
                    case CONCAT_NULL:
                      concatNull = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              concatFields.add( concatFieldname );
              concatTypes.add( concatType );
              concatLengths.add( concatLength );
              concatFormats.add( concatFormat );
              concatPrecisions.add( concatPrecision );
              concatCurrencies.add( concatCurrency );
              concatDecimals.add( concatDecimal );
              concatGroups.add( concatGroup );
              concatTrims.add( concatTrim );
              concatNulls.add( concatNull );
            }
          }
          break;

        case TARGET_FIELDNAME:
          meta.setTargetFieldName( lookValue );
          break;
        case TARGET_LENGTH:
          meta.setTargetFieldLength( Const.toInt( lookValue, 0 ) );
          break;
        case SEPARATOR:
          meta.setSeparator( lookValue );
          break;
        case ENCLOSURE:
          meta.setEnclosure( lookValue );
          break;
        case REMOVE_FIELDS:
          meta.setRemoveSelectedFields( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FORCE_ENCLOSURE:
          meta.setEnclosureForced( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case DISABLE_ENCLOSURE_FIX:
          meta.setEnclosureFixDisabled( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case HEADER:
          meta.setHeaderEnabled( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FOOTER:
          meta.setFooterEnabled( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ENCODING:
          meta.setEncoding( lookValue );
          break;
        case RIGHT_PAD_FIELDS:
          meta.setPadded( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FAST_DATA_DUMP:
          meta.setFastDump( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case SPLIT_EVERY:
          meta.setSplitEvery( Const.toInt( lookValue, 0 ) );
          break;
        case ADD_ENDING_LINE:
          meta.setEndedLine( lookValue );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    //
    if ( concatFields.size() > 0 ) {
      TextFileField[] tff = new TextFileField[concatFields.size()];
      Iterator<String> iConcatFields = concatFields.iterator();
      Iterator<String> iConcatTypes = concatTypes.iterator();
      Iterator<String> iConcatLengths = concatLengths.iterator();
      Iterator<String> iConcatFormats = concatFormats.iterator();
      Iterator<String> iConcatPrecisions = concatPrecisions.iterator();
      Iterator<String> iConcatCurrencies = concatCurrencies.iterator();
      Iterator<String> iConcatDecimals = concatDecimals.iterator();
      Iterator<String> iConcatGroups = concatGroups.iterator();
      Iterator<String> iConcatTrims = concatTrims.iterator();
      Iterator<String> iConcatNulls = concatNulls.iterator();

      int i = 0;
      while ( iConcatFields.hasNext() ) {
        TextFileField field = new TextFileField();
        field.setName( iConcatFields.next() );
        field.setType( ValueMetaFactory.getIdForValueMeta( iConcatTypes.next() ) );
        field.setFormat( iConcatFormats.next() );
        field.setLength( Const.toInt( iConcatLengths.next(), -1 ) );
        field.setPrecision( Const.toInt( iConcatPrecisions.next(), -1 ) );
        field.setCurrencySymbol( iConcatCurrencies.next() );
        field.setDecimalSymbol( iConcatDecimals.next() );
        field.setGroupingSymbol( iConcatGroups.next() );
        field.setNullString( iConcatNulls.next() );
        field.setTrimType( ValueMetaBase.getTrimTypeByDesc( iConcatTrims.next() ) );
        tff[i] = field;
        i++;
      }
      meta.setOutputFields( tff );
    }
  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public ConcatFieldsMeta getMeta() {
    return meta;
  }
}
