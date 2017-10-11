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

package org.pentaho.di.trans.steps.getxmldata;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.BaseStepMetaInjection;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionEnumEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * To keep it simple, this metadata injection interface only supports the fields in the spreadsheet for the time being.
 * 
 * @author Matt
 */
public class GetXMLDataMetaInjection extends BaseStepMetaInjection implements StepMetaInjectionInterface {

  private GetXMLDataMeta meta;

  public GetXMLDataMetaInjection( GetXMLDataMeta meta ) {
    this.meta = meta;
  }

  private enum Entry implements StepMetaInjectionEnumEntry {

    INCLUDE_ROWNUMBER( ValueMetaInterface.TYPE_STRING,
        "Flag indicating that a row number field should be included in the output (Y/N)" ), ROWNUMBER_FIELD(
        ValueMetaInterface.TYPE_STRING, "The name of the field in the output containing the row number" ), ROWLIMIT(
        ValueMetaInterface.TYPE_STRING, "The maximum number or lines to read (integer)" ), LOOP_XPATH(
        ValueMetaInterface.TYPE_STRING, "The maximum number or lines to read (The XPath location to loop over" ), ENCODING(
        ValueMetaInterface.TYPE_STRING, "The file encoding" ), XML_FIELD( ValueMetaInterface.TYPE_STRING,
        "The name of the input field which contains the XML" ), IN_FIELD( ValueMetaInterface.TYPE_STRING,
        "Flag indicating that the XML source is in a field" ), IN_FILE( ValueMetaInterface.TYPE_STRING,
        "Flag indicating that the XML source is in a file" ), ADD_RESULT_FILE( ValueMetaInterface.TYPE_STRING,
        "Add the file(s) to the result? (Y/N)" ), NAMESPACE_AWARE( ValueMetaInterface.TYPE_STRING,
        "Parse namespace aware? (Y/N)" ), VALIDATE( ValueMetaInterface.TYPE_STRING, "Validate the XML? (Y/N)" ), USE_TOKENS(
        ValueMetaInterface.TYPE_STRING, "Process using tokens? (Y/N)" ), IGNORE_EMPTY_FILES(
        ValueMetaInterface.TYPE_STRING, "Ignore empty files? (Y/N)" ), IGNORE_MISSING_FILES(
        ValueMetaInterface.TYPE_STRING, "Ignore missing files? (Y/N)" ), IGNORE_COMMENTS(
        ValueMetaInterface.TYPE_STRING, "Ignore comments? (Y/N)" ), READ_URL( ValueMetaInterface.TYPE_STRING,
        "Read URL as source? (Y/N)" ), PRUNE_PATH( ValueMetaInterface.TYPE_STRING,
        "If you set this path, it activates the streaming algorithm to process large files" ), SHORT_FILE_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: short file name" ), FILE_PATH_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: file path" ), FILE_HIDDEN_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: hidden file" ), FILE_MODIFICATION_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: file modification date" ), FILE_URI_NAME_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: file URI name" ), FILE_ROOT_URI_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: file root URI" ), FILE_EXTENSION_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: file extesion" ), FILE_SIZE_FIELDNAME(
        ValueMetaInterface.TYPE_STRING, "Output field: file size" ),

    INPUTFIELDS( ValueMetaInterface.TYPE_NONE, "All the input fields" ), INPUTFIELD( ValueMetaInterface.TYPE_NONE,
        "One input field" ), INPUTFIELD_NAME( INPUTFIELD, ValueMetaInterface.TYPE_STRING, "The name of the field" ), INPUTFIELD_XPATH(
        INPUTFIELD, ValueMetaInterface.TYPE_STRING, "The xpath of the field" ), INPUTFIELD_TYPE( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING, "The type of the field (String, Integer, Date, ...)" ), INPUTFIELD_ELEMENT_TYPE(
        INPUTFIELD, ValueMetaInterface.TYPE_STRING, "The element type of the field (node, attribute)" ), INPUTFIELD_RESULT_TYPE(
        INPUTFIELD, ValueMetaInterface.TYPE_STRING, "The element result type (valueof, singlenode)" ), INPUTFIELD_LENGTH(
        INPUTFIELD, ValueMetaInterface.TYPE_STRING, "The length of the field" ), INPUTFIELD_PRECISION( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING, "The precision of the field" ), INPUTFIELD_FORMAT( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING, "The format mask of the field" ), INPUTFIELD_TRIM_TYPE( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING, "The trim type of the field (none, left, right, both)" ), INPUTFIELD_CURRENCY(
        INPUTFIELD, ValueMetaInterface.TYPE_STRING, "The currency symbol" ), INPUTFIELD_GROUPING( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING, "The grouping symbol" ), INPUTFIELD_DECIMAL( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING, "The decimal symbol" ), INPUTFIELD_REPEAT( INPUTFIELD,
        ValueMetaInterface.TYPE_STRING,
        "Flag to indicate we need to repeat the previous row value if the current value is null (Y/N)" ),

    FILENAMES( ValueMetaInterface.TYPE_NONE, "All the file names" ), FILENAME( ValueMetaInterface.TYPE_NONE,
        "One file name" ), FILE_PATH( FILENAME, ValueMetaInterface.TYPE_STRING, "The path to the file" ), FILE_INCLUDE_MASK(
        FILENAME, ValueMetaInterface.TYPE_STRING, "The regular expression to match files to include" ), FILE_EXCLUDE_MASK(
        FILENAME, ValueMetaInterface.TYPE_STRING, "The regular expression to match files to exclude" ), FILE_REQUIRED(
        FILENAME, ValueMetaInterface.TYPE_STRING, "Flag to indicate that this file is required or not (Y/N)" ), FILE_INCLUDE_SUBFOLDERS(
        FILENAME, ValueMetaInterface.TYPE_STRING, "Flag to indicate that subfolders should be included or not (Y/N)" );

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

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    // All top level entries: just add them
    //
    addTopLevelStepMetaInjectionEntries( all, Entry.values() );

    // Add the file names & fields
    //
    addNestedStepMetaInjectionEntries( all, Entry.values(), Entry.FILENAMES, Entry.FILENAME );
    addNestedStepMetaInjectionEntries( all, Entry.values(), Entry.INPUTFIELDS, Entry.INPUTFIELD );

    return all;
  }

  protected class FilenameLine {
    public String fileName;
    public String fileMask;
    public String fileRequired;
    public String excludeFileMask;
    public String includeSubFolders;

    public FilenameLine( String fileName, String fileMask, String fileRequired, String excludeFileMask,
        String includeSubFolders ) {
      super();
      this.fileName = fileName;
      this.fileMask = fileMask;
      this.fileRequired = fileRequired;
      this.excludeFileMask = excludeFileMask;
      this.includeSubFolders = includeSubFolders;
    }
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<GetXMLDataMetaInjection.FilenameLine> filenameLines = new ArrayList<GetXMLDataMetaInjection.FilenameLine>();
    List<GetXMLDataField> fields = new ArrayList<GetXMLDataField>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      String lookFieldsValue = lookFields.getValue() instanceof String ? (String) lookFields.getValue() : null;
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        switch ( fieldsEntry ) {
          case FILENAMES:
            for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
              Entry fieldEntry = Entry.findEntry( lookField.getKey() );
              if ( fieldEntry != null ) {
                if ( fieldEntry == Entry.FILENAME ) {

                  String fileName = null;
                  String fileMask = null;
                  String fileRequired = null;
                  String excludeFileMask = null;
                  String includeSubFolders = null;

                  List<StepInjectionMetaEntry> entries = lookField.getDetails();
                  for ( StepInjectionMetaEntry entry : entries ) {
                    Entry metaEntry = Entry.findEntry( entry.getKey() );
                    if ( metaEntry != null ) {
                      String value = (String) entry.getValue();
                      switch ( metaEntry ) {
                        case FILE_PATH:
                          fileName = value;
                          break;
                        case FILE_INCLUDE_MASK:
                          fileMask = value;
                          break;
                        case FILE_EXCLUDE_MASK:
                          excludeFileMask = value;
                          break;
                        case FILE_REQUIRED:
                          fileRequired = value;
                          break;
                        case FILE_INCLUDE_SUBFOLDERS:
                          includeSubFolders = value;
                          break;
                        default:
                          break;
                      }
                    }
                  }

                  if ( !Utils.isEmpty( fileName ) ) {
                    filenameLines.add( new FilenameLine( fileName, fileMask, fileRequired, excludeFileMask,
                        includeSubFolders ) );
                  }
                }
              }
            }
            break;

          case INPUTFIELDS:
            for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
              Entry fieldEntry = Entry.findEntry( lookField.getKey() );
              if ( fieldEntry != null ) {
                if ( fieldEntry == Entry.INPUTFIELD ) {
                  GetXMLDataField field = new GetXMLDataField();
                  List<StepInjectionMetaEntry> entries = lookField.getDetails();
                  for ( StepInjectionMetaEntry entry : entries ) {
                    Entry metaEntry = Entry.findEntry( entry.getKey() );
                    if ( metaEntry != null ) {
                      String value = (String) entry.getValue();
                      switch ( metaEntry ) {
                        case INPUTFIELD_NAME:
                          field.setName( value );
                          break;
                        case INPUTFIELD_XPATH:
                          field.setXPath( value );
                          break;
                        case INPUTFIELD_TYPE:
                          field.setType( ValueMetaFactory.getIdForValueMeta( value ) );
                          break;
                        case INPUTFIELD_ELEMENT_TYPE:
                          field.setElementType( GetXMLDataField.getElementTypeByCode( value ) );
                          break;
                        case INPUTFIELD_RESULT_TYPE:
                          field.setResultType( GetXMLDataField.getResultTypeByCode( value ) );
                          break;
                        case INPUTFIELD_LENGTH:
                          field.setLength( Const.toInt( value, -1 ) );
                          break;
                        case INPUTFIELD_PRECISION:
                          field.setPrecision( Const.toInt( value, -1 ) );
                          break;
                        case INPUTFIELD_FORMAT:
                          field.setFormat( value );
                          break;
                        case INPUTFIELD_TRIM_TYPE:
                          field.setTrimType( ValueMeta.getTrimTypeByCode( value ) );
                          break;
                        case INPUTFIELD_CURRENCY:
                          field.setCurrencySymbol( value );
                          break;
                        case INPUTFIELD_GROUPING:
                          field.setGroupSymbol( value );
                          break;
                        case INPUTFIELD_DECIMAL:
                          field.setDecimalSymbol( value );
                          break;
                        case INPUTFIELD_REPEAT:
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
            }
            break;

          case INCLUDE_ROWNUMBER:
            meta.setIncludeRowNumber( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case ROWNUMBER_FIELD:
            meta.setRowNumberField( lookFieldsValue );
            break;
          case ROWLIMIT:
            meta.setRowLimit( Const.toLong( lookFieldsValue, 0L ) );
            break;
          case LOOP_XPATH:
            meta.setLoopXPath( lookFieldsValue );
            break;
          case ENCODING:
            meta.setEncoding( lookFieldsValue );
            break;
          case XML_FIELD:
            meta.setXMLField( lookFieldsValue );
            break;
          case IN_FIELD:
            meta.setInFields( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case IN_FILE:
            meta.setIsAFile( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case ADD_RESULT_FILE:
            meta.setAddResultFile( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case NAMESPACE_AWARE:
            meta.setNamespaceAware( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case VALIDATE:
            meta.setValidating( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case USE_TOKENS:
            meta.setuseToken( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case IGNORE_EMPTY_FILES:
            meta.setIgnoreEmptyFile( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case IGNORE_MISSING_FILES:
            meta.setdoNotFailIfNoFile( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case IGNORE_COMMENTS:
            meta.setIgnoreComments( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case READ_URL:
            meta.setReadUrl( "Y".equalsIgnoreCase( lookFieldsValue ) );
            break;
          case PRUNE_PATH:
            meta.setPrunePath( lookFieldsValue );
            break;
          case SHORT_FILE_FIELDNAME:
            meta.setShortFileNameField( lookFieldsValue );
            break;
          case FILE_PATH_FIELDNAME:
            meta.setFilenameField( lookFieldsValue );
            break;
          case FILE_HIDDEN_FIELDNAME:
            meta.setIsHiddenField( lookFieldsValue );
            break;
          case FILE_MODIFICATION_FIELDNAME:
            meta.setLastModificationDateField( lookFieldsValue );
            break;
          case FILE_URI_NAME_FIELDNAME:
            meta.setUriField( lookFieldsValue );
            break;
          case FILE_ROOT_URI_FIELDNAME:
            meta.setRootUriField( lookFieldsValue );
            break;
          case FILE_EXTENSION_FIELDNAME:
            meta.setExtensionField( lookFieldsValue );
            break;
          case FILE_SIZE_FIELDNAME:
            meta.setSizeField( lookFieldsValue );
            break;
          default:
            break;
        }
      }
    }

    // Only modify fields or file names if there was injection taking place...
    //
    if ( fields.size() > 0 ) {
      meta.setInputFields( fields.toArray( new GetXMLDataField[fields.size()] ) );
    }
    if ( filenameLines.size() > 0 ) {
      meta.allocateFiles( filenameLines.size() );
      // CHECKSTYLE:Indentation:OFF
      for ( int i = 0; i < filenameLines.size(); i++ ) {
        FilenameLine line = filenameLines.get( i );
        meta.getFileName()[i] = line.fileName;
        meta.getFileMask()[i] = line.fileMask;
        meta.getFileRequired()[i] = line.fileRequired;
        meta.getExludeFileMask()[i] = line.excludeFileMask;
        meta.getIncludeSubFolders()[i] = line.includeSubFolders;
      }
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public GetXMLDataMeta getMeta() {
    return meta;
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
