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

package org.pentaho.di.trans.steps.pentahoreporting;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMetaInjection;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionEnumEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta.ProcessorType;

/**
 * To keep it simple, this metadata injection interface only supports the fields in the spreadsheet for the time being.
 *
 * @author Matt
 */
public class PentahoReportingOutputMetaInjection extends BaseStepMetaInjection implements
  StepMetaInjectionInterface {

  private PentahoReportingOutputMeta meta;

  public PentahoReportingOutputMetaInjection( PentahoReportingOutputMeta meta ) {
    this.meta = meta;
  }

  private enum Entry implements StepMetaInjectionEnumEntry {

    INPUT_FILE_FIELD(
      ValueMetaInterface.TYPE_STRING, "The name of the field containing the report file path (.prpt)" ),
      OUTPUT_FILE_FIELD( ValueMetaInterface.TYPE_STRING, "The name of the field containing the output file name" ),
      OUTPUT_PROCESSOR_TYPE(
        ValueMetaInterface.TYPE_STRING,
        "The output processor type, one of PDF, PagedHtml, StreamingHtml, CSV, Excel, Excel 2007 or RTF" ),

      PARAMETERS( ValueMetaInterface.TYPE_NONE, "All the parameters for the report" ), PARAMETER(
        ValueMetaInterface.TYPE_NONE, "One parameter" ), PARAMETER_NAME(
        PARAMETER, ValueMetaInterface.TYPE_STRING, "The name of the report parameter" ), FIELDNAME(
        PARAMETER, ValueMetaInterface.TYPE_STRING, "The field name providing the source data" );

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

    all.add( createStepMetaInjectionEntry( Entry.INPUT_FILE_FIELD ) );
    all.add( createStepMetaInjectionEntry( Entry.OUTPUT_FILE_FIELD ) );
    all.add( createStepMetaInjectionEntry( Entry.OUTPUT_PROCESSOR_TYPE ) );

    // Add the fields...
    //
    StepInjectionMetaEntry fieldsEntry = createStepMetaInjectionEntry( Entry.PARAMETERS );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = createStepMetaInjectionEntry( Entry.PARAMETER );
    fieldsEntry.getDetails().add( fieldEntry );
    for ( Entry entry : Entry.values() ) {
      if ( entry.getParent() == Entry.PARAMETER ) {
        fieldEntry.getDetails().add( createStepMetaInjectionEntry( entry ) );
      }
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    boolean cleared = false;

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        switch ( fieldsEntry ) {
          case PARAMETERS:
            for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
              Entry fieldEntry = Entry.findEntry( lookField.getKey() );
              if ( fieldEntry != null ) {
                if ( fieldEntry == Entry.PARAMETER ) {

                  String parameter = null;
                  String fieldname = null;

                  List<StepInjectionMetaEntry> entries = lookField.getDetails();
                  for ( StepInjectionMetaEntry entry : entries ) {
                    Entry metaEntry = Entry.findEntry( entry.getKey() );
                    if ( metaEntry != null ) {
                      String value = (String) entry.getValue();
                      switch ( metaEntry ) {
                        case PARAMETER_NAME:
                          parameter = value;
                          break;
                        case FIELDNAME:
                          fieldname = value;
                          break;
                        default:
                          break;
                      }
                    }
                  }

                  if ( !Const.isEmpty( parameter ) && !Const.isEmpty( fieldname ) ) {
                    // Only clear the parameters IF we provide parameters
                    // Otherwise, keep the original data
                    //
                    if ( !cleared ) {
                      meta.getParameterFieldMap().clear();
                      cleared = true;
                    }
                    meta.getParameterFieldMap().put( parameter, fieldname );
                  }

                }
              }
            }
            break;

          case INPUT_FILE_FIELD:
            meta.setInputFileField( (String) lookFields.getValue() );
            break;
          case OUTPUT_FILE_FIELD:
            meta.setOutputFileField( (String) lookFields.getValue() );
            break;
          case OUTPUT_PROCESSOR_TYPE:
            meta.setOutputProcessorType( ProcessorType.getProcessorTypeByCode( (String) lookFields.getValue() ) );
            break;
          default:
            break;
        }
      }
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public PentahoReportingOutputMeta getMeta() {
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
