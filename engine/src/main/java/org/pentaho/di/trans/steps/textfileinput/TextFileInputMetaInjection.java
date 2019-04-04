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

package org.pentaho.di.trans.steps.textfileinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * This takes care of the external metadata injection into the TextFileInputMeta class
 *
 * @author Matt
 * @deprecated replaced by implementation in the ...steps.fileinput.text package
 */
@Deprecated
public class TextFileInputMetaInjection implements StepMetaInjectionInterface {

  enum Entry implements StepMetaInjectionEntryInterface {

    FILE_TYPE( ValueMetaInterface.TYPE_STRING, "File type (CSV/Fixed)" ), SEPARATOR(
      ValueMetaInterface.TYPE_STRING, "The field separator" ), ENCLOSURE(
      ValueMetaInterface.TYPE_STRING, "The field enclosure" ), ESCAPE_CHAR(
      ValueMetaInterface.TYPE_STRING, "The escape character" ), BREAK_IN_ENCLOSURE(
      ValueMetaInterface.TYPE_STRING, "Is a break allowed in an enclosure? (Y/N)" ), HEADER_PRESENT(
      ValueMetaInterface.TYPE_STRING, "Is there a header present? (Y/N)" ), NR_HEADER_LINES(
      ValueMetaInterface.TYPE_STRING, "The number of header lines" ), HAS_FOOTER(
      ValueMetaInterface.TYPE_STRING, "Is there a footer present? (Y/N)" ), NR_FOOTER_LINES(
      ValueMetaInterface.TYPE_STRING, "The number of footer lines" ), HAS_WRAPPED_LINES(
      ValueMetaInterface.TYPE_STRING, "Are the lines wrapped? (Y/N)" ), NR_WRAPS(
      ValueMetaInterface.TYPE_STRING, "The number of times a line is wrapped" ), HAS_PAGED_LAYOUT(
      ValueMetaInterface.TYPE_STRING, "Is the layout paged? (Y/N)" ), NR_DOC_HEADER_LINES(
      ValueMetaInterface.TYPE_STRING, "The number of document header lines" ), NR_LINES_PER_PAGE(
      ValueMetaInterface.TYPE_STRING, "The number of lines per page" ), COMPRESSION_TYPE(
      ValueMetaInterface.TYPE_STRING, "The compression type used (None, Zip or GZip)" ), NO_EMPTY_LINES(
      ValueMetaInterface.TYPE_STRING, "Skip empty lines? (Y/N)" ), INCLUDE_FILENAME(
      ValueMetaInterface.TYPE_STRING, "Include filename in the output? (Y/N)" ), FILENAME_FIELD(
      ValueMetaInterface.TYPE_STRING, "The name of the filename field in the output" ), INCLUDE_ROW_NUMBER(
      ValueMetaInterface.TYPE_STRING, "Include a row number in the output? (Y/N)" ), ROW_NUMBER_BY_FILE(
      ValueMetaInterface.TYPE_STRING, "Reset the row number for each file? (Y/N)" ), ROW_NUMBER_FIELD(
      ValueMetaInterface.TYPE_STRING, "The name of the row number field in the output" ), FILE_FORMAT(
      ValueMetaInterface.TYPE_STRING, "File format (DOS, UNIX, mixed)" ), ENCODING(
      ValueMetaInterface.TYPE_STRING,
      "Encoding type (for allowed values see: http://wiki.pentaho.com/display/EAI/Text+File+Input)" ), ROW_LIMIT(
      ValueMetaInterface.TYPE_STRING, "The maximum number of lines to read." ), DATE_FORMAT_LENIENT(
      ValueMetaInterface.TYPE_STRING, "Use a lenient date parsing algorithm? (Y/N)" ), DATE_FORMAT_LOCALE(
      ValueMetaInterface.TYPE_STRING, "The date format locale" ), ACCEPT_FILE_NAMES(
      ValueMetaInterface.TYPE_STRING, "Accept file names? (Y/N)" ), ACCEPT_FILE_STEP(
      ValueMetaInterface.TYPE_STRING, "The source step for the file names" ), ACCEPT_FILE_FIELD(
      ValueMetaInterface.TYPE_STRING, "The input field for the file names" ), PASS_THROUGH_FIELDS(
      ValueMetaInterface.TYPE_STRING, "Pass through fields? (Y/N)" ), ADD_FILES_TO_RESULT(
      ValueMetaInterface.TYPE_STRING, "Add file names to the result files? (Y/N)" ), FILE_SHORT_FILE_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The short file output fieldname" ), FILE_PATH_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The path output fieldname" ), FILE_HIDDEN_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The hidden output fieldname" ), FILE_LAST_MODIFICATION_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The last modification time output fieldname" ), FILE_URI_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The URI output fieldname" ), FILE_EXTENSION_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The extension output fieldname" ), FILE_SIZE_FIELDNAME(
      ValueMetaInterface.TYPE_STRING, "The file size output fieldname" ), SKIP_BAD_FILES(
      ValueMetaInterface.TYPE_STRING, "Skip bad files? (Y/N)" ), FILE_ERROR_FIELD(
      ValueMetaInterface.TYPE_STRING, "The output field for the error files" ), FILE_ERROR_MESSAGE_FIELD(
      ValueMetaInterface.TYPE_STRING, "The output field for the file error messages" ), IGNORE_ERRORS(
      ValueMetaInterface.TYPE_STRING, "Ignore errors? (Y/N)" ), ERROR_COUNT_FIELD(
      ValueMetaInterface.TYPE_STRING, "The output field for the number of errors" ), ERROR_FIELDS_FIELD(
      ValueMetaInterface.TYPE_STRING, "The output field for the fields in error" ), ERROR_TEXT_FIELD(
      ValueMetaInterface.TYPE_STRING, "The output field for the error text" ), WARNING_FILES_TARGET_DIR(
      ValueMetaInterface.TYPE_STRING, "The target directory for the warning files" ), WARNING_FILES_EXTENTION(
      ValueMetaInterface.TYPE_STRING, "The warning files' extension" ), ERROR_FILES_TARGET_DIR(
      ValueMetaInterface.TYPE_STRING, "The target directory for the error files" ), ERROR_FILES_EXTENTION(
      ValueMetaInterface.TYPE_STRING, "The error files' extension" ), LINE_NR_FILES_TARGET_DIR(
      ValueMetaInterface.TYPE_STRING, "The target directory for the line number files" ),
    LINE_NR_FILES_EXTENTION( ValueMetaInterface.TYPE_STRING, "The line number files' extension" ),
    ERROR_LINES_SKIPPED( ValueMetaInterface.TYPE_STRING, "Skip error lines? (Y/N)" ),

    FILENAME_LINES( ValueMetaInterface.TYPE_NONE, "The list of file definitions" ), FILENAME_LINE(
      ValueMetaInterface.TYPE_NONE, "One file definition line" ), FILENAME(
      ValueMetaInterface.TYPE_STRING, "The filename or directory" ), FILEMASK(
      ValueMetaInterface.TYPE_STRING, "The file mask (regex)" ), EXCLUDE_FILEMASK(
      ValueMetaInterface.TYPE_STRING, "The mask for the files to exclude (regex)" ), FILE_REQUIRED(
      ValueMetaInterface.TYPE_STRING, "Is this a required file (Y/N)" ), INCLUDE_SUBFOLDERS(
      ValueMetaInterface.TYPE_STRING, "Include sub-folders when searching files? (Y/N)" ),

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
      ValueMetaInterface.TYPE_STRING, "The default value if null" ),

    // The filters
    //
    FILTERS( ValueMetaInterface.TYPE_NONE, "The filter definitions" ), FILTER(
      ValueMetaInterface.TYPE_NONE, "One filter definition" ), FILTER_POSITION(
      ValueMetaInterface.TYPE_STRING, "Position" ), FILTER_STRING(
      ValueMetaInterface.TYPE_STRING, "Filter string" ), FILTER_LAST_LINE(
      ValueMetaInterface.TYPE_STRING, "Stop reading when filter found? (Y/N)" ), FILTER_POSITIVE(
      ValueMetaInterface.TYPE_STRING, "Only match the filter lines? (Y/N)" );

    private int valueType;
    private String description;

    private Entry( int valueType, String description ) {
      this.valueType = valueType;
      this.description = description;
    }

    /**
     * @return the valueType
     */
    @Override
    public int getValueType() {
      return valueType;
    }

    /**
     * @return the description
     */
    @Override
    public String getDescription() {
      return description;
    }

    public static Entry findEntry( String key ) {
      return Entry.valueOf( key );
    }

    public static Entry[] getTopEntries() {
      return new Entry[] {
        FILE_TYPE, SEPARATOR, ENCLOSURE, ESCAPE_CHAR, BREAK_IN_ENCLOSURE,
        HEADER_PRESENT, NR_HEADER_LINES, HAS_FOOTER, NR_FOOTER_LINES,
        HAS_WRAPPED_LINES, NR_WRAPS, HAS_PAGED_LAYOUT, NR_DOC_HEADER_LINES,
        NR_LINES_PER_PAGE, COMPRESSION_TYPE, NO_EMPTY_LINES, INCLUDE_FILENAME,
        FILENAME_FIELD, INCLUDE_ROW_NUMBER, ROW_NUMBER_BY_FILE, ROW_NUMBER_FIELD,
        FILE_FORMAT, ENCODING, ROW_LIMIT, DATE_FORMAT_LENIENT, DATE_FORMAT_LOCALE,
        ACCEPT_FILE_NAMES, ACCEPT_FILE_STEP, ACCEPT_FILE_FIELD, PASS_THROUGH_FIELDS,
        ADD_FILES_TO_RESULT, FILE_SHORT_FILE_FIELDNAME, FILE_PATH_FIELDNAME,
        FILE_HIDDEN_FIELDNAME, FILE_LAST_MODIFICATION_FIELDNAME, FILE_URI_FIELDNAME,
        FILE_EXTENSION_FIELDNAME, FILE_SIZE_FIELDNAME, SKIP_BAD_FILES,
        FILE_ERROR_FIELD, FILE_ERROR_MESSAGE_FIELD, IGNORE_ERRORS, ERROR_COUNT_FIELD,
        ERROR_FIELDS_FIELD, ERROR_TEXT_FIELD, WARNING_FILES_TARGET_DIR,
        WARNING_FILES_EXTENTION, ERROR_FILES_TARGET_DIR, ERROR_FILES_EXTENTION,
        LINE_NR_FILES_TARGET_DIR, LINE_NR_FILES_EXTENTION, ERROR_LINES_SKIPPED
      };
    }

    public static Entry[] getFileFieldsEntries() {
      return new Entry[] { FILENAME, FILEMASK, EXCLUDE_FILEMASK, FILE_REQUIRED, INCLUDE_SUBFOLDERS };
    }

    public static Entry[] getAggEntries() {
      return new Entry[] {
        FIELD_NAME, FIELD_POSITION, FIELD_LENGTH, FIELD_TYPE, FIELD_IGNORE,
        FIELD_FORMAT, FIELD_TRIM_TYPE, FIELD_PRECISION, FIELD_DECIMAL,
        FIELD_GROUP, FIELD_CURRENCY, FIELD_REPEAT, FIELD_NULL_STRING,
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
      all.add( StepInjectionUtil.getEntry( topEntry ) );
    }

    // The file name lines
    //
    StepInjectionMetaEntry filesEntry = StepInjectionUtil.getEntry( Entry.FILENAME_LINES );
    all.add( filesEntry );
    StepInjectionMetaEntry fileEntry = StepInjectionUtil.getEntry( Entry.FILENAME_LINE );
    filesEntry.getDetails().add( fileEntry );

    Entry[] fileFieldsEntries = Entry.getFileFieldsEntries();
    List<StepInjectionMetaEntry> fileEntryDetails = fileEntry.getDetails();
    for ( Entry entry : fileFieldsEntries ) {
      StepInjectionMetaEntry metaEntry = StepInjectionUtil.getEntry( entry );
      fileEntryDetails.add( metaEntry );
    }

    // The fields...
    //
    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.FIELDS );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.FIELD );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] aggEntries = Entry.getAggEntries();
    List<StepInjectionMetaEntry> fieldEntryDetails = fieldEntry.getDetails();
    for ( Entry entry : aggEntries ) {
      StepInjectionMetaEntry metaEntry = StepInjectionUtil.getEntry( entry );
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
    List<TextFileInputField> fields = new ArrayList<TextFileInputField>();
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
          meta.setFileType( lookValue );
          break;
        case SEPARATOR:
          meta.setSeparator( lookValue );
          break;
        case ENCLOSURE:
          meta.setEnclosure( lookValue );
          break;
        case ESCAPE_CHAR:
          meta.setEscapeCharacter( lookValue );
          break;
        case BREAK_IN_ENCLOSURE:
          meta.setBreakInEnclosureAllowed( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case HEADER_PRESENT:
          meta.setHeader( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case NR_HEADER_LINES:
          meta.setNrHeaderLines( Const.toInt( lookValue, -1 ) );
          break;
        case HAS_FOOTER:
          meta.setFooter( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case NR_FOOTER_LINES:
          meta.setNrFooterLines( Const.toInt( lookValue, -1 ) );
          break;
        case HAS_WRAPPED_LINES:
          meta.setLineWrapped( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case NR_WRAPS:
          meta.setNrWraps( Const.toInt( lookValue, -1 ) );
          break;
        case HAS_PAGED_LAYOUT:
          meta.setLayoutPaged( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case NR_DOC_HEADER_LINES:
          meta.setNrLinesDocHeader( Const.toInt( lookValue, -1 ) );
          break;
        case NR_LINES_PER_PAGE:
          meta.setNrLinesPerPage( Const.toInt( lookValue, -1 ) );
          break;
        case COMPRESSION_TYPE:
          meta.setFileCompression( lookValue );
          break;
        case NO_EMPTY_LINES:
          meta.setNoEmptyLines( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case INCLUDE_FILENAME:
          meta.setIncludeFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FILENAME_FIELD:
          meta.setFilenameField( lookValue );
          break;
        case INCLUDE_ROW_NUMBER:
          meta.setIncludeRowNumber( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ROW_NUMBER_BY_FILE:
          meta.setRowNumberByFile( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ROW_NUMBER_FIELD:
          meta.setRowNumberField( lookValue );
          break;
        case FILE_FORMAT:
          meta.setFileFormat( lookValue );
          break;
        case ENCODING:
          meta.setEncoding( lookValue );
          break;
        case ROW_LIMIT:
          meta.setRowLimit( Const.toInt( lookValue, -1 ) );
          break;
        case DATE_FORMAT_LENIENT:
          meta.setDateFormatLenient( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case DATE_FORMAT_LOCALE:
          meta.setDateFormatLocale( new Locale( lookValue ) );
          break;
        case ACCEPT_FILE_NAMES:
          meta.setAcceptingFilenames( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ACCEPT_FILE_STEP:
          meta.setAcceptingStepName( lookValue );
          break;
        case ACCEPT_FILE_FIELD:
          meta.setAcceptingField( lookValue );
          break;
        case PASS_THROUGH_FIELDS:
          meta.setPassingThruFields( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ADD_FILES_TO_RESULT:
          meta.setAddResultFile( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FILE_SHORT_FILE_FIELDNAME:
          meta.setShortFileNameField( lookValue );
          break;
        case FILE_PATH_FIELDNAME:
          meta.setPathField( lookValue );
          break;
        case FILE_HIDDEN_FIELDNAME:
          meta.setIsHiddenField( lookValue );
          break;
        case FILE_LAST_MODIFICATION_FIELDNAME:
          meta.setLastModificationDateField( lookValue );
          break;
        case FILE_URI_FIELDNAME:
          meta.setUriField( lookValue );
          break;
        case FILE_EXTENSION_FIELDNAME:
          meta.setExtensionField( lookValue );
          break;
        case FILE_SIZE_FIELDNAME:
          meta.setSizeField( lookValue );
          break;
        case SKIP_BAD_FILES:
          meta.setSkipBadFiles( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FILE_ERROR_FIELD:
          meta.setFileErrorField( lookValue );
          break;
        case FILE_ERROR_MESSAGE_FIELD:
          meta.setFileErrorMessageField( lookValue );
          break;
        case IGNORE_ERRORS:
          meta.setErrorIgnored( "Y".equalsIgnoreCase( lookValue ) );
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
          meta.setWarningFilesDestinationDirectory( lookValue );
          break;
        case WARNING_FILES_EXTENTION:
          meta.setWarningFilesExtension( lookValue );
          break;
        case ERROR_FILES_TARGET_DIR:
          meta.setErrorFilesDestinationDirectory( lookValue );
          break;
        case ERROR_FILES_EXTENTION:
          meta.setErrorLineFilesExtension( lookValue );
          break;
        case LINE_NR_FILES_TARGET_DIR:
          meta.setLineNumberFilesDestinationDirectory( lookValue );
          break;
        case LINE_NR_FILES_EXTENTION:
          meta.setLineNumberFilesExtension( lookValue );
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
      meta.setInputFields( fields.toArray( new TextFileInputField[fields.size()] ) );
    }
    if ( fileLines.size() > 0 ) {
      meta.allocateFiles( fileLines.size() );
      //CHECKSTYLE:Indentation:OFF
      for ( int i = 0; i < fileLines.size(); i++ ) {
        FileLine fileLine = fileLines.get( i );
        meta.getFileName()[i] = fileLine.filename;
        meta.getFileMask()[i] = fileLine.includeMask;
        meta.getExludeFileMask()[i] = fileLine.excludeMask;
        meta.getExludeFileMask()[i] = fileLine.excludeMask;
        meta.getFileRequired()[i] = fileLine.required;
        meta.getIncludeSubFolders()[i] = fileLine.includeSubfolders;
      }
    }
    if ( filters.size() > 0 ) {
      meta.setFilter( filters.toArray( new TextFileFilter[filters.size()] ) );
    }

  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> result = new ArrayList<StepInjectionMetaEntry>();
    result.add( StepInjectionUtil.getEntry( Entry.FILE_TYPE, meta.getFileType() ) );
    result.add( StepInjectionUtil.getEntry( Entry.SEPARATOR, meta.getSeparator() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ENCLOSURE, meta.getEnclosure() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ESCAPE_CHAR, meta.getEscapeCharacter() ) );
    result.add( StepInjectionUtil.getEntry( Entry.BREAK_IN_ENCLOSURE, meta.isBreakInEnclosureAllowed() ) );
    result.add( StepInjectionUtil.getEntry( Entry.HEADER_PRESENT, meta.hasHeader() ) );
    result.add( StepInjectionUtil.getEntry( Entry.NR_HEADER_LINES, meta.getNrHeaderLines() ) );
    result.add( StepInjectionUtil.getEntry( Entry.HAS_FOOTER, meta.hasFooter() ) );
    result.add( StepInjectionUtil.getEntry( Entry.NR_FOOTER_LINES, meta.getNrFooterLines() ) );
    result.add( StepInjectionUtil.getEntry( Entry.HAS_WRAPPED_LINES, meta.isLineWrapped() ) );
    result.add( StepInjectionUtil.getEntry( Entry.NR_WRAPS, meta.getNrWraps() ) );
    result.add( StepInjectionUtil.getEntry( Entry.HAS_PAGED_LAYOUT, meta.isLayoutPaged() ) );
    result.add( StepInjectionUtil.getEntry( Entry.NR_DOC_HEADER_LINES, meta.getNrLinesDocHeader() ) );
    result.add( StepInjectionUtil.getEntry( Entry.NR_LINES_PER_PAGE, meta.getNrLinesPerPage() ) );
    result.add( StepInjectionUtil.getEntry( Entry.COMPRESSION_TYPE, meta.getFileCompression() ) );
    result.add( StepInjectionUtil.getEntry( Entry.NO_EMPTY_LINES, meta.noEmptyLines() ) );
    result.add( StepInjectionUtil.getEntry( Entry.INCLUDE_FILENAME, meta.includeFilename() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILENAME_FIELD, meta.getFilenameField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.INCLUDE_ROW_NUMBER, meta.includeRowNumber() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ROW_NUMBER_BY_FILE, meta.isRowNumberByFile() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ROW_NUMBER_FIELD, meta.getRowNumberField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_FORMAT, meta.getFileFormat() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ENCODING, meta.getEncoding() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ROW_LIMIT, meta.getRowLimit() ) );
    result.add( StepInjectionUtil.getEntry( Entry.DATE_FORMAT_LENIENT, meta.isDateFormatLenient() ) );
    result.add( StepInjectionUtil.getEntry( Entry.DATE_FORMAT_LOCALE, meta.getDateFormatLocale() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ACCEPT_FILE_NAMES, meta.isAcceptingFilenames() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ACCEPT_FILE_STEP, meta.getAcceptingStepName() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ACCEPT_FILE_FIELD, meta.getAcceptingField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.PASS_THROUGH_FIELDS, meta.isPassingThruFields() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ADD_FILES_TO_RESULT, meta.isAddResultFile() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_SHORT_FILE_FIELDNAME, meta.getShortFileNameField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_PATH_FIELDNAME, meta.getPathField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_HIDDEN_FIELDNAME, meta.isHiddenField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_LAST_MODIFICATION_FIELDNAME, meta.getLastModificationDateField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_URI_FIELDNAME, meta.getUriField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_EXTENSION_FIELDNAME, meta.getExtensionField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_SIZE_FIELDNAME, meta.getSizeField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.SKIP_BAD_FILES, meta.isSkipBadFiles() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_ERROR_FIELD, meta.getFileErrorField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.FILE_ERROR_MESSAGE_FIELD, meta.getFileErrorMessageField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.IGNORE_ERRORS, meta.isErrorIgnored() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ERROR_COUNT_FIELD, meta.getErrorCountField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ERROR_FIELDS_FIELD, meta.getErrorFieldsField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ERROR_TEXT_FIELD, meta.getErrorTextField() ) );
    result.add( StepInjectionUtil.getEntry( Entry.WARNING_FILES_TARGET_DIR, meta.getWarningFilesDestinationDirectory() ) );
    result.add( StepInjectionUtil.getEntry( Entry.WARNING_FILES_EXTENTION, meta.getWarningFilesExtension() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ERROR_FILES_TARGET_DIR, meta.getErrorFilesDestinationDirectory() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ERROR_FILES_EXTENTION, meta.getErrorLineFilesExtension() ) );
    result.add( StepInjectionUtil.getEntry( Entry.LINE_NR_FILES_TARGET_DIR, meta.getLineNumberFilesDestinationDirectory() ) );
    result.add( StepInjectionUtil.getEntry( Entry.LINE_NR_FILES_EXTENTION, meta.getLineNumberFilesExtension() ) );
    result.add( StepInjectionUtil.getEntry( Entry.ERROR_LINES_SKIPPED, meta.isErrorLineSkipped() ) );

    StepInjectionMetaEntry filenameLinesEntry = StepInjectionUtil.getEntry( Entry.FILENAME_LINES );
    if ( !Utils.isEmpty( meta.getFileName() ) ) {
      for ( int i = 0, len = meta.getFileName().length; i < len; i++ ) {
        StepInjectionMetaEntry filenameLineEntry = StepInjectionUtil.getEntry( Entry.FILENAME_LINE );
        filenameLinesEntry.getDetails().add( filenameLineEntry );

        List<StepInjectionMetaEntry> filenameLineEntryDetails = filenameLineEntry.getDetails();
        filenameLineEntryDetails.add( StepInjectionUtil.getEntry( Entry.FILENAME, meta.getFileName()[ i ] ) );
        filenameLineEntryDetails.add( StepInjectionUtil.getEntry( Entry.FILEMASK, meta.getFileMask()[ i ] ) );
        filenameLineEntryDetails.add( StepInjectionUtil.getEntry( Entry.EXCLUDE_FILEMASK, meta.getExludeFileMask()[ i ] ) );
        filenameLineEntryDetails.add( StepInjectionUtil.getEntry( Entry.FILE_REQUIRED, meta.getFileRequired()[ i ] ) );
        filenameLineEntryDetails.add( StepInjectionUtil.getEntry( Entry.INCLUDE_SUBFOLDERS, meta.getIncludeSubFolders()[ i ] ) );
      }
    }
    result.add( filenameLinesEntry );

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.FIELDS );
    if ( !Utils.isEmpty( meta.getInputFields() ) ) {
      for ( TextFileInputField inputField : meta.getInputFields() ) {
        StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.FIELD );
        fieldsEntry.getDetails().add( fieldEntry );

        List<StepInjectionMetaEntry> fieldDetails = fieldEntry.getDetails();
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD, inputField.getName() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_POSITION, inputField.getPosition() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_LENGTH, inputField.getLength() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_TYPE, inputField.getType() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_IGNORE, inputField.isIgnored() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_FORMAT, inputField.getFormat() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_TRIM_TYPE, inputField.getTrimType() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_PRECISION, inputField.getPrecision() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_DECIMAL, inputField.getDecimalSymbol() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_GROUP, inputField.getGroupSymbol() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_CURRENCY, inputField.getCurrencySymbol() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_REPEAT, inputField.isRepeated() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_NULL_STRING, inputField.getNullString() ) );
        fieldDetails.add( StepInjectionUtil.getEntry( Entry.FIELD_IF_NULL, inputField.getIfNullValue() ) );
      }
    }
    result.add( fieldsEntry );

    return result;
  }

  public TextFileInputMeta getMeta() {
    return meta;
  }
}
