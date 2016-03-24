/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsonoutput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This takes care of the external metadata injection into the JsonOutputMeta class
 *
 * @author Chris
 */
public class JsonOutputMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    OPERATION( ValueMetaInterface.TYPE_STRING, "The operation to perform (Output Value, Write to File, Ouptut value and write to file)" ),
      JSON_BLOC_NAME( ValueMetaInterface.TYPE_STRING, "The name of the Json Bloc" ),
      NR_ROWS_IN_BLOC( ValueMetaInterface.TYPE_STRING, "The number of rows in a bloc" ),
      OUTPUT_VALUE( ValueMetaInterface.TYPE_STRING, "The field to contain the output JSON" ),
      COMPATIBILITY_MODE( ValueMetaInterface.TYPE_STRING, "Run in Compatibility Mode? (Y/N)" ),

      FILE_NAME( ValueMetaInterface.TYPE_STRING, "The output file name" ),
      APPEND( ValueMetaInterface.TYPE_STRING, "Append if the file exists? (Y/N)" ),
      CREATE_PARENT_FOLDER( ValueMetaInterface.TYPE_STRING, "Create the parent folder? (Y/N)" ),
      DONT_CREATE_AT_START( ValueMetaInterface.TYPE_STRING, "Do not create the file at start? (Y/N)" ),
      EXTENSION( ValueMetaInterface.TYPE_STRING, "The file extension" ),
      ENCODING( ValueMetaInterface.TYPE_STRING,
        "Encoding type (for allowed values see: http://wiki.pentaho.com/display/EAI/JSON+output)" ),
      PASS_TO_SERVLET( ValueMetaInterface.TYPE_STRING, "Pass output to servlet? (Y/N)" ),
      INC_DATE_IN_FILENAME( ValueMetaInterface.TYPE_STRING, "Include date in filename? (Y/N)" ),
      INC_TIME_IN_FILENAME( ValueMetaInterface.TYPE_STRING, "Include time in filename? (Y/N)" ),
      ADD_TO_RESULT( ValueMetaInterface.TYPE_STRING, "Add file to result filenames? (Y/N)" ),

      JSON_FIELDS( ValueMetaInterface.TYPE_NONE, "The fields to add to the JSON" ),
      JSON_FIELD( ValueMetaInterface.TYPE_NONE, "One field to add to the JSON" ),
      JSON_FIELDNAME( ValueMetaInterface.TYPE_STRING, "Stream field name" ),
      JSON_ELEMENTNAME( ValueMetaInterface.TYPE_STRING, "Name of the JSON element" );

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

  private JsonOutputMeta meta;

  public JsonOutputMetaInjection( JsonOutputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.OPERATION, Entry.JSON_BLOC_NAME, Entry.NR_ROWS_IN_BLOC, Entry.OUTPUT_VALUE,
        Entry.COMPATIBILITY_MODE, Entry.FILE_NAME, Entry.APPEND, Entry.CREATE_PARENT_FOLDER,
        Entry.DONT_CREATE_AT_START, Entry.EXTENSION, Entry.ENCODING,
        Entry.PASS_TO_SERVLET, Entry.INC_DATE_IN_FILENAME, Entry.INC_TIME_IN_FILENAME,
        Entry.ADD_TO_RESULT, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    // The fields
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.JSON_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.JSON_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.JSON_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.JSON_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.JSON_FIELDNAME, Entry.JSON_ELEMENTNAME, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<String> jsonFields = new ArrayList<String>();
    List<String> jsonElements = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case JSON_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.JSON_FIELD ) {

              String jsonFieldname = null;
              String jsonElement = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case JSON_FIELDNAME:
                      jsonFieldname = value;
                      break;
                    case JSON_ELEMENTNAME:
                      jsonElement = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              jsonFields.add( jsonFieldname );
              jsonElements.add( jsonElement );
            }
          }
          break;

        case OPERATION:
          meta.setOperationType( JsonOutputMeta.getOperationTypeByDesc( lookValue ) );
          break;
        case JSON_BLOC_NAME:
          meta.setJsonBloc( lookValue );
          break;
        case NR_ROWS_IN_BLOC:
          meta.setNrRowsInBloc( lookValue );
          break;
        case OUTPUT_VALUE:
          meta.setOutputValue( lookValue );
          break;
        case COMPATIBILITY_MODE:
          meta.setCompatibilityMode( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case FILE_NAME:
          meta.setFileName( lookValue );
          break;
        case APPEND:
          meta.setFileAppended( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case CREATE_PARENT_FOLDER:
          meta.setCreateParentFolder( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case DONT_CREATE_AT_START:
          meta.setDoNotOpenNewFileInit( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case EXTENSION:
          meta.setExtension( lookValue );
          break;
        case ENCODING:
          meta.setEncoding( lookValue );
          break;
        case PASS_TO_SERVLET:
          meta.setServletOutput( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case INC_DATE_IN_FILENAME:
          meta.setDateInFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case INC_TIME_IN_FILENAME:
          meta.setTimeInFilename( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case ADD_TO_RESULT:
          meta.setAddToResult( "Y".equalsIgnoreCase( lookValue ) );
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    //
    if ( jsonFields.size() > 0 ) {
      JsonOutputField[] jof = new JsonOutputField[jsonFields.size()];
      Iterator<String> iJsonFields = jsonFields.iterator();
      Iterator<String> iJsonElements = jsonElements.iterator();

      int i = 0;
      while ( iJsonFields.hasNext() ) {
        JsonOutputField field = new JsonOutputField();
        field.setFieldName( iJsonFields.next() );
        field.setElementName( iJsonElements.next() );
        jof[i] = field;
        i++;
      }
      meta.setOutputFields( jof );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public JsonOutputMeta getMeta() {
    return meta;
  }
}
