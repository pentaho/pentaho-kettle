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

package org.pentaho.di.trans.steps.accessinput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * To keep it simple, this metadata injection interface only supports the fields in the spreadsheet for the time being.
 *
 * @author Matt
 */
public class AccessInputMetaInjection implements StepMetaInjectionInterface {

  private AccessInputMeta meta;

  public AccessInputMetaInjection( AccessInputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    for ( Entry entry : Entry.values() ) {
      if ( entry.getParent() == null && entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
        all.add( new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() ) );
      }
    }

    // The file name lines
    //
    StepInjectionMetaEntry filesEntry =
      new StepInjectionMetaEntry(
        Entry.FILENAME_LINES.name(), ValueMetaInterface.TYPE_NONE, Entry.FILENAME_LINES.description );
    all.add( filesEntry );
    StepInjectionMetaEntry fileEntry =
      new StepInjectionMetaEntry(
        Entry.FILENAME_LINE.name(), ValueMetaInterface.TYPE_NONE, Entry.FILENAME_LINE.description );
    filesEntry.getDetails().add( fileEntry );
    for ( Entry entry : Entry.values() ) {
      if ( entry.getParent() == Entry.FILENAME_LINE ) {
        StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
        fileEntry.getDetails().add( metaEntry );
      }
    }

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
      }
    }

    return all;
  }

  private class FileLine {
    String filename;
    String includeMask;
    String excludeMask;
    String required;
    String includeSubfolders;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<AccessInputField> fields = new ArrayList<AccessInputField>();
    List<FileLine> fileLines = new ArrayList<FileLine>();

    // Parse the filenames, fields,... & inject into the meta class..
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

              AccessInputField field = new AccessInputField();

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case FIELD_NAME:
                      field.setName( value );
                      break;
                    case FIELD_COLUMN:
                      field.setColumn( value );
                      break;
                    case FIELD_LENGTH:
                      field.setLength( Const.toInt( value, -1 ) );
                      break;
                    case FIELD_TYPE:
                      field.setType( ValueMetaFactory.getIdForValueMeta( value ) );
                      break;
                    case FIELD_FORMAT:
                      field.setFormat( value );
                      break;
                    case FIELD_TRIM_TYPE:
                      field.setTrimType( ValueMetaBase.getTrimTypeByCode( value ) );
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
                    default:
                      break;
                  }
                }
              }
              fields.add( field );
            }
          }

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
          meta.setResetRowNumber( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ROW_NUMBER_FIELD:
          meta.setRowNumberField( lookValue );
          break;
        case ROW_LIMIT:
          meta.setRowLimit( Const.toInt( lookValue, -1 ) );
          break;
        case ACCEPT_FILE_NAMES:
          meta.setFileField( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ACCEPT_FILE_FIELD:
          meta.setDynamicFilenameField( lookValue );
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
        case TABLENAME:
          meta.setTableName( lookValue );
          break;
        case INCLUDE_TABLENAME:
          meta.setIncludeTablename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case TABLENAME_FIELD:
          meta.setTableName( lookValue );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    // Only change a list when you need to, don't clear/reset existing content if you don't send new content.
    //
    if ( fields.size() > 0 ) {
      meta.setInputFields( fields.toArray( new AccessInputField[fields.size()] ) );
    }
    if ( fileLines.size() > 0 ) {
      meta.allocateFiles( fileLines.size() );
      for ( int i = 0; i < fileLines.size(); i++ ) {
        FileLine fileLine = fileLines.get( i );
        ( meta.getFileName() )[i] = fileLine.filename;
        ( meta.getFileMask() )[i] = fileLine.includeMask;
        ( meta.getExludeFileMask() )[i] = fileLine.excludeMask;
        ( meta.getExludeFileMask() )[i] = fileLine.excludeMask;
        ( meta.getFileRequired() )[i] = fileLine.required;
        ( meta.getIncludeSubFolders() )[i] = fileLine.includeSubfolders;
      }
    }
  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public AccessInputMeta getMeta() {
    return meta;
  }

  private enum Entry {

    FILENAME_LINES( ValueMetaInterface.TYPE_NONE, "The list of file definitions" ), FILENAME_LINE(
      ValueMetaInterface.TYPE_NONE, "One file definition line" ), FILENAME(
      FILENAME_LINE, ValueMetaInterface.TYPE_STRING, "The filename or directory" ), FILEMASK(
      FILENAME_LINE, ValueMetaInterface.TYPE_STRING, "The file mask (regex)" ), EXCLUDE_FILEMASK(
      FILENAME_LINE, ValueMetaInterface.TYPE_STRING, "The mask for the files to exclude (regex)" ),
      FILE_REQUIRED( FILENAME_LINE, ValueMetaInterface.TYPE_STRING, "Is this a required file (Y/N)" ),
      INCLUDE_SUBFOLDERS(
        FILENAME_LINE, ValueMetaInterface.TYPE_STRING, "Include sub-folders when searching files? (Y/N)" ),

      INCLUDE_FILENAME( ValueMetaInterface.TYPE_STRING, "Include filename in the output? (Y/N)" ), FILENAME_FIELD(
        ValueMetaInterface.TYPE_STRING, "The name of the filename field in the output" ),

      INCLUDE_TABLENAME( ValueMetaInterface.TYPE_STRING, "Include the table name in the output? (Y/N)" ),
      TABLENAME_FIELD( ValueMetaInterface.TYPE_STRING, "The name of the table name field in the output" ),

      INCLUDE_ROW_NUMBER( ValueMetaInterface.TYPE_STRING, "Include a row number in the output? (Y/N)" ),
      ROW_NUMBER_BY_FILE( ValueMetaInterface.TYPE_STRING, "Reset the row number for each file? (Y/N)" ),
      ROW_NUMBER_FIELD( ValueMetaInterface.TYPE_STRING, "The name of the row number field in the output" ),

      TABLENAME( ValueMetaInterface.TYPE_STRING, "The name of the table to retrieve data from" ),

      ROW_LIMIT( ValueMetaInterface.TYPE_STRING, "The maximum number of lines to read." ),

      FIELDS( ValueMetaInterface.TYPE_NONE, "All the fields on the spreadsheets" ), FIELD(
        ValueMetaInterface.TYPE_NONE, "One field" ), FIELD_NAME(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field name" ), FIELD_COLUMN(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field source column name" ), FIELD_TYPE(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field data type" ), FIELD_LENGTH(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field length" ), FIELD_PRECISION(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field precision" ), FIELD_TRIM_TYPE(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field trim type (none, left, right, both)" ), FIELD_FORMAT(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field conversion format" ), FIELD_CURRENCY(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field currency symbol" ), FIELD_DECIMAL(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field decimal symbol" ), FIELD_GROUP(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field group symbol" ), FIELD_REPEAT(
        FIELD, ValueMetaInterface.TYPE_STRING, "Field repeat (Y/N)" ),

      ACCEPT_FILE_NAMES( ValueMetaInterface.TYPE_STRING, "Accept file names? (Y/N)" ), ACCEPT_FILE_FIELD(
        ValueMetaInterface.TYPE_STRING, "The input field for the file names" ),

      ADD_FILES_TO_RESULT( ValueMetaInterface.TYPE_STRING, "Add file names to the result files? (Y/N)" ),

      FILE_SHORT_FILE_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The short file output fieldname" ),
      FILE_PATH_FIELDNAME( ValueMetaInterface.TYPE_STRING, "The path output fieldname" ), FILE_HIDDEN_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "The hidden output fieldname" ), FILE_LAST_MODIFICATION_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "The last modification time output fieldname" ), FILE_URI_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "The URI output fieldname" ), FILE_EXTENSION_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "The extension output fieldname" ), FILE_SIZE_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "The file size output fieldname" );

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

  public class ExcelInputSheet {
    public String sheetName;
    public int startCol;
    public int startRow;

    /**
     * @param sheetName
     * @param startCol
     * @param startRow
     */
    private ExcelInputSheet( String sheetName, int startCol, int startRow ) {
      this.sheetName = sheetName;
      this.startCol = startCol;
      this.startRow = startRow;
    }
  }
}
