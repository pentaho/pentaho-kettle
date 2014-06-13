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

package org.pentaho.di.trans.steps.textfileoutput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This takes care of the external metadata injection into the TextFileOutputMeta class
 *
 * @author Chris
 */
public class TextFileOutputMetaInjection implements StepMetaInjectionInterface {

  public enum Entry implements StepMetaInjectionEntryInterface {

    FILENAME( ValueMetaInterface.TYPE_STRING, "The filename" ),
      RUN_AS_COMMAND( ValueMetaInterface.TYPE_STRING, "Run this as command instead? (Y/N)" ),
      PASS_TO_SERVLET( ValueMetaInterface.TYPE_STRING, "Pass output to servlet? (Y/N)" ),
      CREATE_PARENT_FOLDER( ValueMetaInterface.TYPE_STRING, "Create parent folder? (Y/N)" ),
      FILENAME_IN_FIELD( ValueMetaInterface.TYPE_STRING, "Accept filename from field? (Y/N)" ),
      FILENAME_FIELD( ValueMetaInterface.TYPE_STRING, "The filename field" ),
      EXTENSION( ValueMetaInterface.TYPE_STRING, "The file extension" ),
      INC_STEPNR_IN_FILENAME( ValueMetaInterface.TYPE_STRING, "Include stepnr in filename? (Y/N)" ),
      INC_PARTNR_IN_FILENAME( ValueMetaInterface.TYPE_STRING, "Include partition nr in filename? (Y/N)" ),
      INC_DATE_IN_FILENAME( ValueMetaInterface.TYPE_STRING, "Include date in filename? (Y/N)" ),
      INC_TIME_IN_FILENAME( ValueMetaInterface.TYPE_STRING, "Include time in filename? (Y/N)" ),
      SPECIFY_DATE_FORMAT( ValueMetaInterface.TYPE_STRING, "Specify date time format for filename? (Y/N)" ),
      DATE_FORMAT( ValueMetaInterface.TYPE_STRING, "Date time format for filename" ),
      ADD_TO_RESULT( ValueMetaInterface.TYPE_STRING, "Add filenames to result? (Y/N)" ),

      APPEND( ValueMetaInterface.TYPE_STRING, "Append if file exists? (Y/N)" ),
      SEPARATOR( ValueMetaInterface.TYPE_STRING, "The separator" ),
      ENCLOSURE( ValueMetaInterface.TYPE_STRING, "The enclosure" ),
      FORCE_ENCLOSURE( ValueMetaInterface.TYPE_STRING, "Force the enclosure around fields? (Y/N)" ),
      DISABLE_ENCLOSURE_FIX( ValueMetaInterface.TYPE_STRING, "Disable the enclosure fix? (Y/N)" ),
      HEADER( ValueMetaInterface.TYPE_STRING, "Include header row? (Y/N)" ),
      FOOTER( ValueMetaInterface.TYPE_STRING, "Include footer row? (Y/N)" ),
      FORMAT( ValueMetaInterface.TYPE_STRING, "The file format line termination? (DOS, UNIX, CR, None)" ),
      COMPRESSION( ValueMetaInterface.TYPE_STRING, "The compression? (GZip, Hadoop-Snappy, Snappy, Zip, None)" ),
      ENCODING( ValueMetaInterface.TYPE_STRING,
        "Encoding type (for allowed values see: http://wiki.pentaho.com/display/EAI/Text+File+Output)" ),
      RIGHT_PAD_FIELDS( ValueMetaInterface.TYPE_STRING, "Right pad fields? (Y/N)" ),
      FAST_DATA_DUMP( ValueMetaInterface.TYPE_STRING, "Fast data dump? (Y/N)" ),
      SPLIT_EVERY( ValueMetaInterface.TYPE_STRING, "Split every ... rows" ),
      ADD_ENDING_LINE( ValueMetaInterface.TYPE_STRING, "Add ending line after last row" ),

      OUTPUT_FIELDS( ValueMetaInterface.TYPE_NONE, "The fields to output" ),
      OUTPUT_FIELD( ValueMetaInterface.TYPE_NONE, "One field to output" ),
      OUTPUT_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Field to output" ),
      OUTPUT_TYPE( ValueMetaInterface.TYPE_STRING,
        "Field type (for allowed values see: http://wiki.pentaho.com/display/EAI/Text+File+Output)" ),
      OUTPUT_FORMAT( ValueMetaInterface.TYPE_STRING, "Field format" ),
      OUTPUT_LENGTH( ValueMetaInterface.TYPE_STRING, "Field length" ),
      OUTPUT_PRECISION( ValueMetaInterface.TYPE_STRING, "Field precision" ),
      OUTPUT_CURRENCY( ValueMetaInterface.TYPE_STRING, "Field currency symbol" ),
      OUTPUT_DECIMAL( ValueMetaInterface.TYPE_STRING, "Field decimal symbol" ),
      OUTPUT_GROUP( ValueMetaInterface.TYPE_STRING, "Field grouping symbol" ),
      OUTPUT_TRIM( ValueMetaInterface.TYPE_STRING, "Field trim type (none,left,both,right)" ),
      OUTPUT_NULL( ValueMetaInterface.TYPE_STRING, "Value to replace nulls with" );

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

  private TextFileOutputMeta meta;

  public TextFileOutputMetaInjection( TextFileOutputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.FILENAME, Entry.RUN_AS_COMMAND, Entry.PASS_TO_SERVLET, Entry.CREATE_PARENT_FOLDER,
        Entry.FILENAME_IN_FIELD, Entry.FILENAME_FIELD, Entry.EXTENSION, Entry.INC_STEPNR_IN_FILENAME,
        Entry.INC_PARTNR_IN_FILENAME, Entry.INC_DATE_IN_FILENAME, Entry.INC_TIME_IN_FILENAME,
        Entry.SPECIFY_DATE_FORMAT, Entry.DATE_FORMAT, Entry.ADD_TO_RESULT,

        Entry.APPEND, Entry.SEPARATOR, Entry.ENCLOSURE, Entry.FORCE_ENCLOSURE,
        Entry.DISABLE_ENCLOSURE_FIX, Entry.HEADER, Entry.FOOTER, Entry.FORMAT, Entry.COMPRESSION,
        Entry.ENCODING, Entry.RIGHT_PAD_FIELDS, Entry.FAST_DATA_DUMP, Entry.SPLIT_EVERY,
        Entry.ADD_ENDING_LINE, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The fields
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.OUTPUT_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.OUTPUT_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.OUTPUT_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.OUTPUT_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.OUTPUT_FIELDNAME, Entry.OUTPUT_TYPE, Entry.OUTPUT_LENGTH,
      Entry.OUTPUT_FORMAT, Entry.OUTPUT_PRECISION, Entry.OUTPUT_CURRENCY, Entry.OUTPUT_DECIMAL,
      Entry.OUTPUT_GROUP, Entry.OUTPUT_TRIM, Entry.OUTPUT_NULL, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<String> outputFields = new ArrayList<String>();
    List<String> outputTypes = new ArrayList<String>();
    List<String> outputLengths = new ArrayList<String>();
    List<String> outputFormats = new ArrayList<String>();
    List<String> outputPrecisions = new ArrayList<String>();
    List<String> outputCurrencies = new ArrayList<String>();
    List<String> outputDecimals = new ArrayList<String>();
    List<String> outputGroups = new ArrayList<String>();
    List<String> outputTrims = new ArrayList<String>();
    List<String> outputNulls = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case OUTPUT_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.OUTPUT_FIELD ) {

              String outputFieldname = null;
              String outputType = null;
              String outputLength = null;
              String outputFormat = null;
              String outputPrecision = null;
              String outputCurrency = null;
              String outputDecimal = null;
              String outputGroup = null;
              String outputTrim = null;
              String outputNull = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case OUTPUT_FIELDNAME:
                      outputFieldname = value;
                      break;
                    case OUTPUT_TYPE:
                      outputType = value;
                      break;
                    case OUTPUT_LENGTH:
                      outputLength = value;
                      break;
                    case OUTPUT_FORMAT:
                      outputFormat = value;
                      break;
                    case OUTPUT_PRECISION:
                      outputPrecision = value;
                      break;
                    case OUTPUT_CURRENCY:
                      outputCurrency = value;
                      break;
                    case OUTPUT_DECIMAL:
                      outputDecimal = value;
                      break;
                    case OUTPUT_GROUP:
                      outputGroup = value;
                      break;
                    case OUTPUT_TRIM:
                      outputTrim = value;
                      break;
                    case OUTPUT_NULL:
                      outputNull = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              outputFields.add( outputFieldname );
              outputTypes.add( outputType );
              outputLengths.add( outputLength );
              outputFormats.add( outputFormat );
              outputPrecisions.add( outputPrecision );
              outputCurrencies.add( outputCurrency );
              outputDecimals.add( outputDecimal );
              outputGroups.add( outputGroup );
              outputTrims.add( outputTrim );
              outputNulls.add( outputNull );
            }
          }
          break;

        case FILENAME:
          meta.setFileName( lookValue );
          break;
        case RUN_AS_COMMAND:
          meta.setFileAsCommand( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case PASS_TO_SERVLET:
          meta.setServletOutput( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case CREATE_PARENT_FOLDER:
          meta.setCreateParentFolder( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FILENAME_IN_FIELD:
          meta.setFileNameInField( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FILENAME_FIELD:
          meta.setFileNameField( lookValue );
          break;
        case EXTENSION:
          meta.setExtension( lookValue );
          break;
        case INC_STEPNR_IN_FILENAME:
          meta.setStepNrInFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case INC_PARTNR_IN_FILENAME:
          meta.setPartNrInFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case INC_DATE_IN_FILENAME:
          meta.setDateInFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case INC_TIME_IN_FILENAME:
          meta.setTimeInFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case SPECIFY_DATE_FORMAT:
          meta.setSpecifyingFormat( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case DATE_FORMAT:
          meta.setDateTimeFormat( lookValue );
          break;
        case ADD_TO_RESULT:
          meta.setAddToResultFiles( "Y".equalsIgnoreCase( lookValue ) );
          break;

        case APPEND:
          meta.setFileAppended( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case SEPARATOR:
          meta.setSeparator( lookValue );
          break;
        case ENCLOSURE:
          meta.setEnclosure( lookValue );
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
        case FORMAT:
          meta.setFileFormat( lookValue );
          break;
        case COMPRESSION:
          meta.setFileCompression( lookValue );
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
    if ( outputFields.size() > 0 ) {
      TextFileField[] tff = new TextFileField[outputFields.size()];
      Iterator<String> iOutputFields = outputFields.iterator();
      Iterator<String> iOutputTypes = outputTypes.iterator();
      Iterator<String> iOutputLengths = outputLengths.iterator();
      Iterator<String> iOutputFormats = outputFormats.iterator();
      Iterator<String> iOutputPrecisions = outputPrecisions.iterator();
      Iterator<String> iOutputCurrencies = outputCurrencies.iterator();
      Iterator<String> iOutputDecimals = outputDecimals.iterator();
      Iterator<String> iOutputGroups = outputGroups.iterator();
      Iterator<String> iOutputTrims = outputTrims.iterator();
      Iterator<String> iOutputNulls = outputNulls.iterator();

      int i = 0;
      while ( iOutputFields.hasNext() ) {
        TextFileField field = new TextFileField();
        field.setName( iOutputFields.next() );
        field.setType( ValueMeta.getType( iOutputTypes.next() ) );
        field.setFormat( iOutputFormats.next() );
        field.setLength( Const.toInt( iOutputLengths.next(), -1 ) );
        field.setPrecision( Const.toInt( iOutputPrecisions.next(), -1 ) );
        field.setCurrencySymbol( iOutputCurrencies.next() );
        field.setDecimalSymbol( iOutputDecimals.next() );
        field.setGroupingSymbol( iOutputGroups.next() );
        field.setNullString( iOutputNulls.next() );
        field.setTrimType( ValueMeta.getTrimTypeByDesc( iOutputTrims.next() ) );
        tff[i] = field;
        i++;
      }
      meta.setOutputFields( tff );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() {
    List<StepInjectionMetaEntry> list = new ArrayList<StepInjectionMetaEntry>();

    list.add( StepInjectionUtil.getEntry( Entry.FILENAME, meta.getFileName() ) );
    list.add( StepInjectionUtil.getEntry( Entry.RUN_AS_COMMAND, meta.isFileAsCommand() ) );
    list.add( StepInjectionUtil.getEntry( Entry.PASS_TO_SERVLET, meta.isServletOutput() ) );
    list.add( StepInjectionUtil.getEntry( Entry.CREATE_PARENT_FOLDER, meta.isCreateParentFolder() ) );
    list.add( StepInjectionUtil.getEntry( Entry.FILENAME_IN_FIELD, meta.isFileNameInField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.FILENAME_FIELD, meta.getFileNameField() ) );
    list.add( StepInjectionUtil.getEntry( Entry.EXTENSION, meta.getExtension() ) );
    list.add( StepInjectionUtil.getEntry( Entry.INC_STEPNR_IN_FILENAME, meta.isStepNrInFilename() ) );
    list.add( StepInjectionUtil.getEntry( Entry.INC_PARTNR_IN_FILENAME, meta.isPartNrInFilename() ) );
    list.add( StepInjectionUtil.getEntry( Entry.INC_DATE_IN_FILENAME, meta.isDateInFilename() ) );
    list.add( StepInjectionUtil.getEntry( Entry.INC_TIME_IN_FILENAME, meta.isTimeInFilename() ) );
    list.add( StepInjectionUtil.getEntry( Entry.SPECIFY_DATE_FORMAT, meta.isSpecifyingFormat() ) );
    list.add( StepInjectionUtil.getEntry( Entry.DATE_FORMAT, meta.getDateTimeFormat() ) );
    list.add( StepInjectionUtil.getEntry( Entry.ADD_TO_RESULT, meta.isAddToResultFiles() ) );

    list.add( StepInjectionUtil.getEntry( Entry.APPEND, meta.isFileAppended() ) );
    list.add( StepInjectionUtil.getEntry( Entry.SEPARATOR, meta.getSeparator() ) );
    list.add( StepInjectionUtil.getEntry( Entry.ENCLOSURE, meta.getEnclosure() ) );
    list.add( StepInjectionUtil.getEntry( Entry.FORCE_ENCLOSURE, meta.isEnclosureForced() ) );
    list.add( StepInjectionUtil.getEntry( Entry.DISABLE_ENCLOSURE_FIX, meta.isEnclosureFixDisabled() ) );
    list.add( StepInjectionUtil.getEntry( Entry.HEADER, meta.isHeaderEnabled() ) );
    list.add( StepInjectionUtil.getEntry( Entry.FOOTER, meta.isFooterEnabled() ) );
    list.add( StepInjectionUtil.getEntry( Entry.FORMAT, meta.getFileFormat() ) );
    list.add( StepInjectionUtil.getEntry( Entry.COMPRESSION, meta.getFileCompression() ) );
    list.add( StepInjectionUtil.getEntry( Entry.ENCODING, meta.getEncoding() ) );
    list.add( StepInjectionUtil.getEntry( Entry.RIGHT_PAD_FIELDS, meta.isPadded() ) );
    list.add( StepInjectionUtil.getEntry( Entry.FAST_DATA_DUMP, meta.isFastDump() ) );
    list.add( StepInjectionUtil.getEntry( Entry.SPLIT_EVERY, meta.getSplitEvery() ) );
    list.add( StepInjectionUtil.getEntry( Entry.ADD_ENDING_LINE, meta.getEndedLine() ) );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.OUTPUT_FIELDS );
    list.add( fieldsEntry );
    for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
      StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.OUTPUT_FIELD );
      List<StepInjectionMetaEntry> details = fieldEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_FIELDNAME, meta.getOutputFields()[i].getName() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_TYPE, meta.getOutputFields()[i].getTypeDesc() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_FORMAT, meta.getOutputFields()[i].getFormat() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_LENGTH, meta.getOutputFields()[i].getLength() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_PRECISION, meta.getOutputFields()[i].getPrecision() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_CURRENCY, meta.getOutputFields()[i].getCurrencySymbol() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_DECIMAL, meta.getOutputFields()[i].getDecimalSymbol() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_GROUP, meta.getOutputFields()[i].getGroupingSymbol() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_TRIM, meta.getOutputFields()[i].getTrimTypeDesc() ) );
      details.add( StepInjectionUtil.getEntry( Entry.OUTPUT_NULL, meta.getOutputFields()[i].getNullString() ) );

      fieldsEntry.getDetails().add( fieldEntry );
    }

    return list;
  }

  public TextFileOutputMeta getMeta() {
    return meta;
  }
}
