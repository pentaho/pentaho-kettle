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

package org.pentaho.di.trans.steps.datagrid;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * To keep it simple, this metadata injection interface only supports the fields in the spreadsheet for the time being.
 *
 * @author Matt
 */
public class DataGridMetaInjection implements StepMetaInjectionInterface {

  private DataGridMeta meta;

  public DataGridMetaInjection( DataGridMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    // Add the fields...
    //
    StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry( Entry.FIELDS.name(), Entry.FIELDS.getValueType(),
      Entry.FIELDS.getDescription() );
    all.add( fieldsEntry );

    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry( Entry.FIELD.name(), Entry.FIELD.getValueType(),
      Entry.FIELD.getDescription() );
    fieldsEntry.getDetails().add( fieldEntry );

    for ( Entry entry : Entry.values() ) {
      if ( entry.getParent() == Entry.FIELD ) {
        StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
        fieldEntry.getDetails().add( metaEntry );
      }
    }

    // And the data fields
    //
    StepInjectionMetaEntry sheetsEntry = new StepInjectionMetaEntry( Entry.DATA_LINES.name(),
      Entry.DATA_LINES.getValueType(), Entry.DATA_LINES.getDescription() );
    all.add( sheetsEntry );

    StepInjectionMetaEntry sheetEntry = new StepInjectionMetaEntry( Entry.DATA_LINE.name(),
      Entry.DATA_LINE.getValueType(), Entry.DATA_LINE.getDescription() );
    sheetsEntry.getDetails().add( sheetEntry );

    for ( Entry entry : Entry.values() ) {
      if ( entry.getParent() == Entry.DATA_LINE ) {
        StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
        sheetEntry.getDetails().add( metaEntry );
      }
    }

    return all;
  }

  private class GridMetaEntry {
    String fieldName;
    String fieldType;
    String fieldFormat;
    String currency;
    String decimal;
    String group;
    int fieldLength;
    int fieldPrecision;
    boolean setEmptyString;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<GridMetaEntry> gridMetaEntries = new ArrayList<GridMetaEntry>();
    List<List<String>> dataLines = new ArrayList<List<String>>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        if ( fieldsEntry == Entry.FIELDS ) {
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry != null ) {
              if ( fieldEntry == Entry.FIELD ) {

                GridMetaEntry gridMetaEntry = new GridMetaEntry();

                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for ( StepInjectionMetaEntry entry : entries ) {
                  Entry metaEntry = Entry.findEntry( entry.getKey() );
                  if ( metaEntry != null ) {
                    String value = (String) entry.getValue();
                    switch ( metaEntry ) {
                      case NAME:
                        gridMetaEntry.fieldName = value;
                        break;
                      case TYPE:
                        gridMetaEntry.fieldType = value;
                        break;
                      case LENGTH:
                        gridMetaEntry.fieldLength = Const.toInt( value, -1 );
                        break;
                      case PRECISION:
                        gridMetaEntry.fieldPrecision = Const.toInt( value, -1 );
                        break;
                      case CURRENCY:
                        gridMetaEntry.currency = value;
                        break;
                      case GROUP:
                        gridMetaEntry.group = value;
                        break;
                      case DECIMAL:
                        gridMetaEntry.decimal = value;
                        break;
                      case FORMAT:
                        gridMetaEntry.fieldFormat = value;
                        break;
                      case EMPTY_STRING:
                        gridMetaEntry.setEmptyString = "Y".equalsIgnoreCase( value )
                          || "TRUE".equalsIgnoreCase( value );
                        break;
                      default:
                        break;
                    }
                  }
                }

                gridMetaEntries.add( gridMetaEntry );
              }
            }
          }
        }
      }
    }

    // Now that we know how many fields we have we can simply read all the data fields...
    //
    List<String> line = new ArrayList<String>();

    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        if ( fieldsEntry == Entry.DATA_LINES ) {
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry != null ) {
              if ( fieldEntry == Entry.DATA_LINE ) {
                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for ( StepInjectionMetaEntry entry : entries ) {
                  Entry metaEntry = Entry.findEntry( entry.getKey() );
                  if ( metaEntry != null ) {
                    String value = (String) entry.getValue();
                    switch ( metaEntry ) {
                      case DATA_VALUE:
                        line.add( value );
                        if ( line.size() >= gridMetaEntries.size() ) {
                          dataLines.add( line );
                          line = new ArrayList<String>();
                        }
                        break;
                      default:
                        break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Pass the grid to the step metadata
    //
    if ( gridMetaEntries.size() > 0 ) {
      meta.allocate( gridMetaEntries.size() );
      for ( int i = 0; i < gridMetaEntries.size(); i++ ) {
        GridMetaEntry entry = gridMetaEntries.get( i );
        //CHECKSTYLE:Indentation:OFF
        meta.getFieldName()[i] = entry.fieldName;
        meta.getFieldType()[i] = entry.fieldType;
        meta.getFieldFormat()[i] = entry.fieldFormat;
        meta.getFieldLength()[i] = entry.fieldLength;
        meta.getFieldPrecision()[i] = entry.fieldPrecision;
        meta.getCurrency()[i] = entry.currency;
        meta.getGroup()[i] = entry.group;
        meta.getDecimal()[i] = entry.decimal;
        meta.isSetEmptyString()[i] = entry.setEmptyString;
      }
    }

    if ( dataLines.size() > 0 ) {
      // Set the data ...
      //
      meta.setDataLines( dataLines );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public DataGridMeta getMeta() {
    return meta;
  }

  private enum Entry {

    FIELDS( ValueMetaInterface.TYPE_NONE, "All the fields" ),
      FIELD( ValueMetaInterface.TYPE_NONE, "One field" ),

      NAME( FIELD, ValueMetaInterface.TYPE_STRING, "Field name" ),
      TYPE( FIELD, ValueMetaInterface.TYPE_STRING, "Field data type" ),
      FORMAT( FIELD, ValueMetaInterface.TYPE_STRING, "Field conversion format" ),
      CURRENCY( FIELD, ValueMetaInterface.TYPE_STRING, "Field currency symbol" ),
      DECIMAL( FIELD, ValueMetaInterface.TYPE_STRING, "Field decimal symbol" ),
      GROUP( FIELD, ValueMetaInterface.TYPE_STRING, "Field group symbol" ),
      LENGTH( FIELD, ValueMetaInterface.TYPE_STRING, "Field length" ),
      PRECISION( FIELD, ValueMetaInterface.TYPE_STRING, "Field precision" ),
      EMPTY_STRING( FIELD, ValueMetaInterface.TYPE_STRING, "Set field to empty string?" ),

      DATA_LINES( ValueMetaInterface.TYPE_NONE, "Nr Rows x Nr Columns values" ),
      DATA_LINE( DATA_LINES, ValueMetaInterface.TYPE_NONE, "One data value" ),
      DATA_VALUE( DATA_LINE, ValueMetaInterface.TYPE_STRING, "One value" );

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
