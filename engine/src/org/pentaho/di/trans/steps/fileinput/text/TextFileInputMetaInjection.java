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

package org.pentaho.di.trans.steps.fileinput.text;

import static org.pentaho.di.trans.step.StepInjectionUtil.getEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;

/**
 * This takes care of the external metadata injection into the TextFileInputMeta class
 *
 * @author Matt
 */
public class TextFileInputMetaInjection implements StepMetaInjectionInterface {

  enum Entry implements StepMetaInjectionEntryInterface {

    FILE_TYPE( ValueMetaInterface.TYPE_STRING, "File type (CSV/Fixed)" ), 
    SEPARATOR( ValueMetaInterface.TYPE_STRING, "The field separator" ), 
    ENCLOSURE( ValueMetaInterface.TYPE_STRING, "The field enclosure" ), 
    ESCAPE_CHAR( ValueMetaInterface.TYPE_STRING, "The escape character" ), 
    BREAK_IN_ENCLOSURE( ValueMetaInterface.TYPE_STRING, "Is a break allowed in an enclosure? (Y/N)" ), 
    HEADER_PRESENT( ValueMetaInterface.TYPE_STRING, "Is there a header present? (Y/N)" ), 
    NR_HEADER_LINES( ValueMetaInterface.TYPE_STRING, "The number of header lines" ), 
    HAS_FOOTER( ValueMetaInterface.TYPE_STRING, "Is there a footer present? (Y/N)" ), 
    NR_FOOTER_LINES( ValueMetaInterface.TYPE_STRING, "The number of footer lines" ), 
    HAS_WRAPPED_LINES( ValueMetaInterface.TYPE_STRING, "Are the lines wrapped? (Y/N)" ), 
    NR_WRAPS( ValueMetaInterface.TYPE_STRING, "The number of times a line is wrapped" ), 
    HAS_PAGED_LAYOUT( ValueMetaInterface.TYPE_STRING, "Is the layout paged? (Y/N)" ), 
    NR_DOC_HEADER_LINES( ValueMetaInterface.TYPE_STRING, "The number of document header lines" ), 
    NR_LINES_PER_PAGE( ValueMetaInterface.TYPE_STRING, "The number of lines per page" ), 
    COMPRESSION_TYPE( ValueMetaInterface.TYPE_STRING, "The compression type used (None, Zip or GZip)" ), 
    NO_EMPTY_LINES( ValueMetaInterface.TYPE_STRING, "Skip empty lines? (Y/N)" ), 
    INCLUDE_FILENAME( ValueMetaInterface.TYPE_STRING, "Include filename in the output? (Y/N)" ), 
    FILENAME_FIELD( ValueMetaInterface.TYPE_STRING, "The name of the filename field in the output" ), 
    INCLUDE_ROW_NUMBER( ValueMetaInterface.TYPE_STRING, "Include a row number in the output? (Y/N)" ), 
    ROW_NUMBER_BY_FILE( ValueMetaInterface.TYPE_STRING, "Reset the row number for each file? (Y/N)" ), 
    ROW_NUMBER_FIELD( ValueMetaInterface.TYPE_STRING, "The name of the row number field in the output" ), 
    FILE_FORMAT( ValueMetaInterface.TYPE_STRING, "File format (DOS, UNIX, mixed)" ), 
    ENCODING( ValueMetaInterface.TYPE_STRING, "Encoding type (for allowed values see: http://wiki.pentaho.com/display/EAI/Text+File+Input)" ), 
    ROW_LIMIT( ValueMetaInterface.TYPE_STRING, "The maximum number of lines to read." ), 
    DATE_FORMAT_LENIENT( ValueMetaInterface.TYPE_STRING, "Use a lenient date parsing algorithm? (Y/N)" ), 
    DATE_FORMAT_LOCALE( ValueMetaInterface.TYPE_STRING, "The date format locale" ), 
    ACCEPT_FILE_NAMES( ValueMetaInterface.TYPE_STRING, "Accept file names? (Y/N)" ), 
    ACCEPT_FILE_STEP( ValueMetaInterface.TYPE_STRING, "The source step for the file names" ), 
    ACCEPT_FILE_FIELD( ValueMetaInterface.TYPE_STRING, "The input field for the file names" ), 
    PASS_THROUGH_FIELDS( ValueMetaInterface.TYPE_STRING, "Pass through fields? (Y/N)" ), 
    ADD_FILES_TO_RESULT( ValueMetaInterface.TYPE_STRING, "Add file names to the result files? (Y/N)" ), 
    FILE_SHORT_FILE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The short file output fieldname" ), 
    FILE_PATH_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The path output fieldname" ), 
    FILE_HIDDEN_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The hidden output fieldname" ), 
    FILE_LAST_MODIFICATION_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The last modification time output fieldname" ), 
    FILE_URI_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The URI output fieldname" ), 
    FILE_EXTENSION_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The extension output fieldname" ), 
    FILE_SIZE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The file size output fieldname" ), 
    SKIP_BAD_FILES( ValueMetaInterface.TYPE_STRING, "Skip bad files? (Y/N)" ), 
    FILE_ERROR_FIELD( ValueMetaInterface.TYPE_STRING, "The output field for the error files" ), 
    FILE_ERROR_MESSAGE_FIELD( ValueMetaInterface.TYPE_STRING, "The output field for the file error messages" ), 
    IGNORE_ERRORS( ValueMetaInterface.TYPE_STRING, "Ignore errors? (Y/N)" ), 
    ERROR_COUNT_FIELD( ValueMetaInterface.TYPE_STRING, "The output field for the number of errors" ), 
    ERROR_FIELDS_FIELD( ValueMetaInterface.TYPE_STRING, "The output field for the fields in error" ), 
    ERROR_TEXT_FIELD( ValueMetaInterface.TYPE_STRING, "The output field for the error text" ), 
    WARNING_FILES_TARGET_DIR( ValueMetaInterface.TYPE_STRING, "The target directory for the warning files" ), 
    WARNING_FILES_EXTENTION( ValueMetaInterface.TYPE_STRING, "The warning files' extension" ), 
    ERROR_FILES_TARGET_DIR( ValueMetaInterface.TYPE_STRING, "The target directory for the error files" ), 
    ERROR_FILES_EXTENTION( ValueMetaInterface.TYPE_STRING, "The error files' extension" ), 
    LINE_NR_FILES_TARGET_DIR( ValueMetaInterface.TYPE_STRING, "The target directory for the line number files" ), 
    LINE_NR_FILES_EXTENTION( ValueMetaInterface.TYPE_STRING, "The line number files' extension" ), 
    ERROR_LINES_SKIPPED( ValueMetaInterface.TYPE_STRING, "Skip error lines? (Y/N)" ),

    FILENAME_LINES( ValueMetaInterface.TYPE_NONE, "The list of file definitions" ), 
    FILENAME_LINE( ValueMetaInterface.TYPE_NONE, "One file definition line" ), 
    FILENAME( ValueMetaInterface.TYPE_STRING, "The filename or directory" ), 
    FILEMASK( ValueMetaInterface.TYPE_STRING, "The file mask (regex)" ), 
    EXCLUDE_FILEMASK( ValueMetaInterface.TYPE_STRING, "The mask for the files to exclude (regex)" ), 
    FILE_REQUIRED( ValueMetaInterface.TYPE_STRING, "Is this a required file (Y/N)" ), 
    INCLUDE_SUBFOLDERS( ValueMetaInterface.TYPE_STRING, "Include sub-folders when searching files? (Y/N)" ),

    FIELDS( ValueMetaInterface.TYPE_NONE, "The fields" ), 
    FIELD( ValueMetaInterface.TYPE_NONE, "One field" ), 
    FIELD_NAME( ValueMetaInterface.TYPE_STRING, "Name" ), 
    FIELD_POSITION( ValueMetaInterface.TYPE_STRING, "Position" ), 
    FIELD_LENGTH( ValueMetaInterface.TYPE_STRING, "Length" ), 
    FIELD_TYPE( ValueMetaInterface.TYPE_STRING, "Data type (String, Number, ...)" ), 
    FIELD_IGNORE( ValueMetaInterface.TYPE_STRING, "Ignore? (Y/N)" ), 
    FIELD_FORMAT( ValueMetaInterface.TYPE_STRING, "Format" ), 
    FIELD_TRIM_TYPE( ValueMetaInterface.TYPE_STRING, "Trim type (none, left, right, both)" ), 
    FIELD_PRECISION( ValueMetaInterface.TYPE_STRING, "Precision" ), 
    FIELD_DECIMAL( ValueMetaInterface.TYPE_STRING, "Decimal symbol" ), 
    FIELD_GROUP( ValueMetaInterface.TYPE_STRING, "Grouping symbol" ), 
    FIELD_CURRENCY( ValueMetaInterface.TYPE_STRING, "Currency symbol" ), 
    FIELD_REPEAT( ValueMetaInterface.TYPE_STRING, "Repeat values? (Y/N)" ), 
    FIELD_NULL_STRING( ValueMetaInterface.TYPE_STRING, "The null string" ), 
    FIELD_IF_NULL( ValueMetaInterface.TYPE_STRING, "The default value if null" ),

    // The filters
    //
    FILTERS( ValueMetaInterface.TYPE_NONE, "The filter definitions" ), 
    FILTER( ValueMetaInterface.TYPE_NONE, "One filter definition" ), 
    FILTER_POSITION( ValueMetaInterface.TYPE_STRING, "Position" ), 
    FILTER_STRING( ValueMetaInterface.TYPE_STRING, "Filter string" ), 
    FILTER_LAST_LINE( ValueMetaInterface.TYPE_STRING, "Stop reading when filter found? (Y/N)" ), 
    FILTER_POSITIVE( ValueMetaInterface.TYPE_STRING, "Only match the filter lines? (Y/N)" );

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

    public static Entry[] getTopEntries() {
      return new Entry[] { FILE_TYPE, SEPARATOR, ENCLOSURE, ESCAPE_CHAR, BREAK_IN_ENCLOSURE, HEADER_PRESENT,
        NR_HEADER_LINES, HAS_FOOTER, NR_FOOTER_LINES, HAS_WRAPPED_LINES, NR_WRAPS, HAS_PAGED_LAYOUT,
        NR_DOC_HEADER_LINES, NR_LINES_PER_PAGE, COMPRESSION_TYPE, NO_EMPTY_LINES, INCLUDE_FILENAME, FILENAME_FIELD,
        INCLUDE_ROW_NUMBER, ROW_NUMBER_BY_FILE, ROW_NUMBER_FIELD, FILE_FORMAT, ENCODING, ROW_LIMIT, DATE_FORMAT_LENIENT,
        DATE_FORMAT_LOCALE, ACCEPT_FILE_NAMES, ACCEPT_FILE_STEP, ACCEPT_FILE_FIELD, PASS_THROUGH_FIELDS,
        ADD_FILES_TO_RESULT, FILE_SHORT_FILE_FIELDNAME, FILE_PATH_FIELDNAME, FILE_HIDDEN_FIELDNAME,
        FILE_LAST_MODIFICATION_FIELDNAME, FILE_URI_FIELDNAME, FILE_EXTENSION_FIELDNAME, FILE_SIZE_FIELDNAME,
        SKIP_BAD_FILES, FILE_ERROR_FIELD, FILE_ERROR_MESSAGE_FIELD, IGNORE_ERRORS, ERROR_COUNT_FIELD,
        ERROR_FIELDS_FIELD, ERROR_TEXT_FIELD, WARNING_FILES_TARGET_DIR, WARNING_FILES_EXTENTION, ERROR_FILES_TARGET_DIR,
        ERROR_FILES_EXTENTION, LINE_NR_FILES_TARGET_DIR, LINE_NR_FILES_EXTENTION, ERROR_LINES_SKIPPED };
    }

    public static Entry[] getFileFieldsEntries() {
      return new Entry[] { FILENAME, FILEMASK, EXCLUDE_FILEMASK, FILE_REQUIRED, INCLUDE_SUBFOLDERS };
    }

    public static Entry[] getAggEntries() {
      return new Entry[] { FIELD_NAME, FIELD_POSITION, FIELD_LENGTH, FIELD_TYPE, FIELD_IGNORE, FIELD_FORMAT,
        FIELD_TRIM_TYPE, FIELD_PRECISION, FIELD_DECIMAL, FIELD_GROUP, FIELD_CURRENCY, FIELD_REPEAT, FIELD_NULL_STRING,
        FIELD_IF_NULL };
    }
  }

  private final TextFileInputMeta meta;

  public TextFileInputMetaInjection( TextFileInputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    for ( Entry topEntry : Entry.getTopEntries() ) {
      all.add( getEntry( topEntry ) );
    }

    // The file name lines
    //
    StepInjectionMetaEntry filesEntry = getEntry( Entry.FILENAME_LINES );
    all.add( filesEntry );
    StepInjectionMetaEntry fileEntry = getEntry( Entry.FILENAME_LINE );
    filesEntry.getDetails().add( fileEntry );

    Entry[] fileFieldsEntries = Entry.getFileFieldsEntries();
    List<StepInjectionMetaEntry> fileEntryDetails = fileEntry.getDetails();
    for ( Entry entry : fileFieldsEntries ) {
      StepInjectionMetaEntry metaEntry = getEntry( entry );
      fileEntryDetails.add( metaEntry );
    }

    // The fields...
    //
    StepInjectionMetaEntry fieldsEntry = getEntry( Entry.FIELDS );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = getEntry( Entry.FIELD );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] aggEntries = Entry.getAggEntries();
    List<StepInjectionMetaEntry> fieldEntryDetails = fieldEntry.getDetails();
    for ( Entry entry : aggEntries ) {
      StepInjectionMetaEntry metaEntry = getEntry( entry );
      fieldEntryDetails.add( metaEntry );
    }

    return all;
  }

  private static class FileLine {
    String filename;
    String includeMask;
    String excludeMask;
    String required;
    String includeSubfolders;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<FileLine> fileLines = new ArrayList<FileLine>();
    List<BaseFileInputField> fields = new ArrayList<BaseFileInputField>();
    List<TextFileFilter> filters = new ArrayList<TextFileFilter>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case FILENAME_LINES:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.FILENAME_LINE ) {
              FileLine fileLine = new FileLine();

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case FILENAME:
                      fileLine.filename = value;
                      break;
                    case FILEMASK:
                      fileLine.includeMask = value;
                      break;
                    case EXCLUDE_FILEMASK:
                      fileLine.excludeMask = value;
                      break;
                    case FILE_REQUIRED:
                      fileLine.required = value;
                      break;
                    case INCLUDE_SUBFOLDERS:
                      fileLine.includeSubfolders = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              fileLines.add( fileLine );
            }
          }
          break;

        case FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.FIELD ) {

              BaseFileInputField field = new BaseFileInputField();

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
                      field.setType( ValueMeta.getType( value ) );
                      break;
                    case FIELD_IGNORE:
                      field.setIgnored( "Y".equalsIgnoreCase( value ) );
                      break;
                    case FIELD_FORMAT:
                      field.setFormat( value );
                      break;
                    case FIELD_TRIM_TYPE:
                      field.setTrimType( ValueMeta.getTrimTypeByCode( value ) );
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

        case FILTERS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.FILTER ) {
              TextFileFilter filterLine = new TextFileFilter();

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case FILTER_POSITION:
                      filterLine.setFilterPosition( Const.toInt( value, 0 ) );
                      break;
                    case FILTER_STRING:
                      filterLine.setFilterString( value );
                      break;
                    case FILTER_LAST_LINE:
                      filterLine.setFilterLastLine( "Y".equalsIgnoreCase( value ) );
                      break;
                    case FILTER_POSITIVE:
                      filterLine.setFilterPositive( "Y".equalsIgnoreCase( value ) );
                      break;
                    default:
                      break;
                  }
                }
              }
              filters.add( filterLine );
            }
          }
          break;

        case FILE_TYPE:
          meta.content.fileType = lookValue;
          break;
        case SEPARATOR:
          meta.content.separator = lookValue;
          break;
        case ENCLOSURE:
          meta.content.enclosure = lookValue;
          break;
        case ESCAPE_CHAR:
          meta.content.escapeCharacter = lookValue;
          break;
        case BREAK_IN_ENCLOSURE:
          meta.content.breakInEnclosureAllowed = "Y".equalsIgnoreCase( lookValue );
          break;
        case HEADER_PRESENT:
          meta.content.header = "Y".equalsIgnoreCase( lookValue );
          break;
        case NR_HEADER_LINES:
          meta.content.nrHeaderLines = Const.toInt( lookValue, -1 );
          break;
        case HAS_FOOTER:
          meta.content.footer = "Y".equalsIgnoreCase( lookValue );
          break;
        case NR_FOOTER_LINES:
          meta.content.nrFooterLines = Const.toInt( lookValue, -1 );
          break;
        case HAS_WRAPPED_LINES:
          meta.content.lineWrapped = "Y".equalsIgnoreCase( lookValue );
          break;
        case NR_WRAPS:
          meta.content.nrWraps = Const.toInt( lookValue, -1 );
          break;
        case HAS_PAGED_LAYOUT:
          meta.content.layoutPaged = "Y".equalsIgnoreCase( lookValue );
          break;
        case NR_DOC_HEADER_LINES:
          meta.content.nrLinesDocHeader = Const.toInt( lookValue, -1 );
          break;
        case NR_LINES_PER_PAGE:
          meta.content.nrLinesPerPage = Const.toInt( lookValue, -1 );
          break;
        case COMPRESSION_TYPE:
          meta.content.fileCompression = lookValue;
          break;
        case NO_EMPTY_LINES:
          meta.content.noEmptyLines = "Y".equalsIgnoreCase( lookValue );
          break;
        case INCLUDE_FILENAME:
          meta.content.includeFilename = "Y".equalsIgnoreCase( lookValue );
          break;
        case FILENAME_FIELD:
          meta.content.filenameField = lookValue;
          break;
        case INCLUDE_ROW_NUMBER:
          meta.content.includeRowNumber = "Y".equalsIgnoreCase( lookValue );
          break;
        case ROW_NUMBER_BY_FILE:
          meta.content.rowNumberByFile = "Y".equalsIgnoreCase( lookValue );
          break;
        case ROW_NUMBER_FIELD:
          meta.content.rowNumberField = lookValue;
          break;
        case FILE_FORMAT:
          meta.content.fileFormat = lookValue;
          break;
        case ENCODING:
          meta.content.encoding = lookValue;
          break;
        case ROW_LIMIT:
          meta.content.rowLimit = Const.toInt( lookValue, -1 );
          break;
        case DATE_FORMAT_LENIENT:
          meta.content.dateFormatLenient = "Y".equalsIgnoreCase( lookValue );
          break;
        case DATE_FORMAT_LOCALE:
          meta.content.dateFormatLocale = new Locale( lookValue );
          break;
        case ACCEPT_FILE_NAMES:
          meta.inputFiles.acceptingFilenames = "Y".equalsIgnoreCase( lookValue );
          break;
        case ACCEPT_FILE_STEP:
          meta.inputFiles.acceptingStepName = lookValue;
          break;
        case ACCEPT_FILE_FIELD:
          meta.inputFiles.acceptingField = lookValue;
          break;
        case PASS_THROUGH_FIELDS:
          meta.inputFiles.passingThruFields = "Y".equalsIgnoreCase( lookValue );
          break;
        case ADD_FILES_TO_RESULT:
          meta.inputFiles.isaddresult = "Y".equalsIgnoreCase( lookValue );
          break;
        case FILE_SHORT_FILE_FIELDNAME:
          meta.additionalOutputFields.shortFilenameField = lookValue;
          break;
        case FILE_PATH_FIELDNAME:
          meta.additionalOutputFields.pathField = lookValue;
          break;
        case FILE_HIDDEN_FIELDNAME:
          meta.additionalOutputFields.hiddenField = lookValue;
          break;
        case FILE_LAST_MODIFICATION_FIELDNAME:
          meta.additionalOutputFields.lastModificationField = lookValue;
          break;
        case FILE_URI_FIELDNAME:
          meta.additionalOutputFields.uriField = lookValue;
          break;
        case FILE_EXTENSION_FIELDNAME:
          meta.additionalOutputFields.extensionField = lookValue;
          break;
        case FILE_SIZE_FIELDNAME:
          meta.additionalOutputFields.sizeField = lookValue;
          break;
        case SKIP_BAD_FILES:
          meta.errorHandling.skipBadFiles = "Y".equalsIgnoreCase( lookValue );
          break;
        case FILE_ERROR_FIELD:
          meta.errorHandling.fileErrorField = lookValue;
          break;
        case FILE_ERROR_MESSAGE_FIELD:
          meta.errorHandling.fileErrorMessageField = lookValue;
          break;
        case IGNORE_ERRORS:
          meta.errorHandling.errorIgnored = "Y".equalsIgnoreCase( lookValue );
          break;
        case ERROR_COUNT_FIELD:
          meta.setErrorCountField( lookValue );
          break;
        case ERROR_FIELDS_FIELD:
          meta.setErrorFieldsField( lookValue );
          break;
        case ERROR_TEXT_FIELD:
          meta.setErrorTextField( lookValue );
          break;
        case WARNING_FILES_TARGET_DIR:
          meta.errorHandling.warningFilesDestinationDirectory = lookValue;
          break;
        case WARNING_FILES_EXTENTION:
          meta.errorHandling.warningFilesExtension = lookValue;
          break;
        case ERROR_FILES_TARGET_DIR:
          meta.errorHandling.errorFilesDestinationDirectory = lookValue;
          break;
        case ERROR_FILES_EXTENTION:
          meta.errorHandling.errorFilesExtension = lookValue;
          break;
        case LINE_NR_FILES_TARGET_DIR:
          meta.errorHandling.lineNumberFilesDestinationDirectory = lookValue;
          break;
        case LINE_NR_FILES_EXTENTION:
          meta.errorHandling.lineNumberFilesExtension = lookValue;
          break;
        case ERROR_LINES_SKIPPED:
          meta.setErrorLineSkipped( "Y".equalsIgnoreCase( lookValue ) );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    // Only change a list when you need to, don't clear/reset existing content if you don't send new content.
    //
    if ( fields.size() > 0 ) {
      meta.inputFiles.inputFields = fields.toArray( new BaseFileInputField[fields.size()] );
    }
    if ( fileLines.size() > 0 ) {
      meta.allocateFiles( fileLines.size() );
      // CHECKSTYLE:Indentation:OFF
      for ( int i = 0; i < fileLines.size(); i++ ) {
        FileLine fileLine = fileLines.get( i );
        meta.getFileName()[i] = fileLine.filename;
        meta.inputFiles.fileMask[i] = fileLine.includeMask;
        meta.inputFiles.excludeFileMask[i] = fileLine.excludeMask;
        meta.inputFiles.fileRequired[i] = fileLine.required;
        meta.inputFiles.includeSubFolders[i] = fileLine.includeSubfolders;
      }
    }
    if ( filters.size() > 0 ) {
      meta.setFilter( filters.toArray( new TextFileFilter[filters.size()] ) );
    }

  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> result = new ArrayList<StepInjectionMetaEntry>();
    result.add( getEntry( Entry.FILE_TYPE, meta.content.fileType ) );
    result.add( getEntry( Entry.SEPARATOR, meta.content.separator ) );
    result.add( getEntry( Entry.ENCLOSURE, meta.content.enclosure ) );
    result.add( getEntry( Entry.ESCAPE_CHAR, meta.content.escapeCharacter ) );
    result.add( getEntry( Entry.BREAK_IN_ENCLOSURE, meta.content.breakInEnclosureAllowed ) );
    result.add( getEntry( Entry.HEADER_PRESENT, meta.content.header ) );
    result.add( getEntry( Entry.NR_HEADER_LINES, meta.content.nrHeaderLines ) );
    result.add( getEntry( Entry.HAS_FOOTER, meta.content.footer ) );
    result.add( getEntry( Entry.NR_FOOTER_LINES, meta.content.nrFooterLines ) );
    result.add( getEntry( Entry.HAS_WRAPPED_LINES, meta.content.lineWrapped ) );
    result.add( getEntry( Entry.NR_WRAPS, meta.content.nrWraps ) );
    result.add( getEntry( Entry.HAS_PAGED_LAYOUT, meta.content.layoutPaged ) );
    result.add( getEntry( Entry.NR_DOC_HEADER_LINES, meta.content.nrLinesDocHeader ) );
    result.add( getEntry( Entry.NR_LINES_PER_PAGE, meta.content.nrLinesPerPage ) );
    result.add( getEntry( Entry.COMPRESSION_TYPE, meta.content.fileCompression ) );
    result.add( getEntry( Entry.NO_EMPTY_LINES, meta.content.noEmptyLines ) );
    result.add( getEntry( Entry.INCLUDE_FILENAME, meta.content.includeFilename ) );
    result.add( getEntry( Entry.FILENAME_FIELD, meta.content.filenameField ) );
    result.add( getEntry( Entry.INCLUDE_ROW_NUMBER, meta.content.includeRowNumber ) );
    result.add( getEntry( Entry.ROW_NUMBER_BY_FILE, meta.content.rowNumberByFile ) );
    result.add( getEntry( Entry.ROW_NUMBER_FIELD, meta.content.rowNumberField ) );
    result.add( getEntry( Entry.FILE_FORMAT, meta.content.fileFormat ) );
    result.add( getEntry( Entry.ENCODING, meta.content.encoding ) );
    result.add( getEntry( Entry.ROW_LIMIT, meta.content.rowLimit ) );
    result.add( getEntry( Entry.DATE_FORMAT_LENIENT, meta.content.dateFormatLenient ) );
    result.add( getEntry( Entry.DATE_FORMAT_LOCALE, meta.content.dateFormatLocale ) );
    result.add( getEntry( Entry.ACCEPT_FILE_NAMES, meta.inputFiles.acceptingFilenames ) );
    result.add( getEntry( Entry.ACCEPT_FILE_STEP, meta.inputFiles.acceptingStepName ) );
    result.add( getEntry( Entry.ACCEPT_FILE_FIELD, meta.inputFiles.acceptingField ) );
    result.add( getEntry( Entry.PASS_THROUGH_FIELDS, meta.inputFiles.passingThruFields ) );
    result.add( getEntry( Entry.ADD_FILES_TO_RESULT, meta.inputFiles.isaddresult ) );
    result.add( getEntry( Entry.FILE_SHORT_FILE_FIELDNAME, meta.additionalOutputFields.shortFilenameField ) );
    result.add( getEntry( Entry.FILE_PATH_FIELDNAME, meta.additionalOutputFields.pathField ) );
    result.add( getEntry( Entry.FILE_HIDDEN_FIELDNAME, meta.additionalOutputFields.hiddenField ) );
    result.add( getEntry( Entry.FILE_LAST_MODIFICATION_FIELDNAME, meta.additionalOutputFields.lastModificationField ) );
    result.add( getEntry( Entry.FILE_URI_FIELDNAME, meta.additionalOutputFields.uriField ) );
    result.add( getEntry( Entry.FILE_EXTENSION_FIELDNAME, meta.additionalOutputFields.extensionField ) );
    result.add( getEntry( Entry.FILE_SIZE_FIELDNAME, meta.additionalOutputFields.sizeField ) );
    result.add( getEntry( Entry.SKIP_BAD_FILES, meta.errorHandling.skipBadFiles ) );
    result.add( getEntry( Entry.FILE_ERROR_FIELD, meta.errorHandling.fileErrorField ) );
    result.add( getEntry( Entry.FILE_ERROR_MESSAGE_FIELD, meta.errorHandling.fileErrorMessageField ) );
    result.add( getEntry( Entry.IGNORE_ERRORS, meta.errorHandling.errorIgnored ) );
    result.add( getEntry( Entry.ERROR_COUNT_FIELD, meta.getErrorCountField() ) );
    result.add( getEntry( Entry.ERROR_FIELDS_FIELD, meta.getErrorFieldsField() ) );
    result.add( getEntry( Entry.ERROR_TEXT_FIELD, meta.getErrorTextField() ) );
    result.add( getEntry( Entry.WARNING_FILES_TARGET_DIR, meta.errorHandling.warningFilesDestinationDirectory ) );
    result.add( getEntry( Entry.WARNING_FILES_EXTENTION, meta.errorHandling.warningFilesExtension ) );
    result.add( getEntry( Entry.ERROR_FILES_TARGET_DIR, meta.errorHandling.errorFilesDestinationDirectory ) );
    result.add( getEntry( Entry.ERROR_FILES_EXTENTION, meta.errorHandling.errorFilesExtension ) );
    result.add( getEntry( Entry.LINE_NR_FILES_TARGET_DIR, meta.errorHandling.lineNumberFilesDestinationDirectory ) );
    result.add( getEntry( Entry.LINE_NR_FILES_EXTENTION, meta.errorHandling.lineNumberFilesExtension ) );
    result.add( getEntry( Entry.ERROR_LINES_SKIPPED, meta.isErrorLineSkipped() ) );

    StepInjectionMetaEntry filenameLinesEntry = getEntry( Entry.FILENAME_LINES );
    if ( !Const.isEmpty( meta.getFileName() ) ) {
      for ( int i = 0, len = meta.getFileName().length; i < len; i++ ) {
        StepInjectionMetaEntry filenameLineEntry = getEntry( Entry.FILENAME_LINE );
        filenameLinesEntry.getDetails().add( filenameLineEntry );

        List<StepInjectionMetaEntry> filenameLineEntryDetails = filenameLineEntry.getDetails();
        filenameLineEntryDetails.add( getEntry( Entry.FILENAME, meta.getFileName()[i] ) );
        filenameLineEntryDetails.add( getEntry( Entry.FILEMASK, meta.inputFiles.fileMask[i] ) );
        filenameLineEntryDetails.add( getEntry( Entry.EXCLUDE_FILEMASK, meta.inputFiles.excludeFileMask[i] ) );
        filenameLineEntryDetails.add( getEntry( Entry.FILE_REQUIRED, meta.inputFiles.fileRequired[i] ) );
        filenameLineEntryDetails.add( getEntry( Entry.INCLUDE_SUBFOLDERS, meta.inputFiles.includeSubFolders[i] ) );
      }
    }
    result.add( filenameLinesEntry );

    StepInjectionMetaEntry fieldsEntry = getEntry( Entry.FIELDS );
    if ( !Const.isEmpty( meta.inputFiles.inputFields ) ) {
      for ( BaseFileInputField inputField : meta.inputFiles.inputFields ) {
        StepInjectionMetaEntry fieldEntry = getEntry( Entry.FIELD );
        fieldsEntry.getDetails().add( fieldEntry );

        List<StepInjectionMetaEntry> fieldDetails = fieldEntry.getDetails();
        fieldDetails.add( getEntry( Entry.FIELD, inputField.getName() ) );
        fieldDetails.add( getEntry( Entry.FIELD_POSITION, inputField.getPosition() ) );
        fieldDetails.add( getEntry( Entry.FIELD_LENGTH, inputField.getLength() ) );
        fieldDetails.add( getEntry( Entry.FIELD_TYPE, inputField.getType() ) );
        fieldDetails.add( getEntry( Entry.FIELD_IGNORE, inputField.isIgnored() ) );
        fieldDetails.add( getEntry( Entry.FIELD_FORMAT, inputField.getFormat() ) );
        fieldDetails.add( getEntry( Entry.FIELD_TRIM_TYPE, inputField.getTrimType() ) );
        fieldDetails.add( getEntry( Entry.FIELD_PRECISION, inputField.getPrecision() ) );
        fieldDetails.add( getEntry( Entry.FIELD_DECIMAL, inputField.getDecimalSymbol() ) );
        fieldDetails.add( getEntry( Entry.FIELD_GROUP, inputField.getGroupSymbol() ) );
        fieldDetails.add( getEntry( Entry.FIELD_CURRENCY, inputField.getCurrencySymbol() ) );
        fieldDetails.add( getEntry( Entry.FIELD_REPEAT, inputField.isRepeated() ) );
        fieldDetails.add( getEntry( Entry.FIELD_NULL_STRING, inputField.getNullString() ) );
        fieldDetails.add( getEntry( Entry.FIELD_IF_NULL, inputField.getIfNullValue() ) );
      }
    }
    result.add( fieldsEntry );

    return result;
  }

  public TextFileInputMeta getMeta() {
    return meta;
  }
}
