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

package org.pentaho.di.trans.steps.tableinput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * This takes care of the external metadata injection into the TableInputMeta class
 *
 * @author Chris
 */
public class TableInputMetaInjection implements StepMetaInjectionInterface {

  public enum Entry implements StepMetaInjectionEntryInterface {

    SQL( ValueMetaInterface.TYPE_STRING, "The SQL for the table input step" ),

      LAZY_CONVERSION( ValueMetaInterface.TYPE_STRING, "Enable lazy conversion? (Y/N)" ),
      REPLACE_VARIABLES( ValueMetaInterface.TYPE_STRING, "Replace variables in script? (Y/N)" ),
      EXECUTE_FOR_EACH_ROW( ValueMetaInterface.TYPE_STRING, "Execute for each row?? (Y/N)" ),
      LIMIT( ValueMetaInterface.TYPE_STRING, "Limit size" );

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

  private TableInputMeta meta;

  public TableInputMetaInjection( TableInputMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    Entry[] topEntries =
      new Entry[] {
        Entry.SQL, Entry.LAZY_CONVERSION, Entry.REPLACE_VARIABLES, Entry.EXECUTE_FOR_EACH_ROW, Entry.LIMIT, };
    for ( Entry topEntry : topEntries ) {
      all.add( new StepInjectionMetaEntry( topEntry.name(), topEntry.getValueType(), topEntry.getDescription() ) );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      String lookValue = (String) lookFields.getValue();
      switch ( fieldsEntry ) {
        case SQL:
          meta.setSQL( lookValue );
          break;
        case LAZY_CONVERSION:
          meta.setLazyConversionActive( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case REPLACE_VARIABLES:
          meta.setVariableReplacementActive( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case EXECUTE_FOR_EACH_ROW:
          meta.setExecuteEachInputRow( "Y".equalsIgnoreCase( lookValue ) );
          break;
        case LIMIT:
          meta.setRowLimit( lookValue );
          break;
        default:
          break;
      }
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {

    List<StepInjectionMetaEntry> list = new ArrayList<StepInjectionMetaEntry>();
    list.add( StepInjectionUtil.getEntry( Entry.SQL, meta.getSQL() ) );
    list.add( StepInjectionUtil.getEntry( Entry.LAZY_CONVERSION, meta.isLazyConversionActive() ) );
    list.add( StepInjectionUtil.getEntry( Entry.REPLACE_VARIABLES, meta.isVariableReplacementActive() ) );
    list.add( StepInjectionUtil.getEntry( Entry.EXECUTE_FOR_EACH_ROW, meta.isExecuteEachInputRow() ) );
    list.add( StepInjectionUtil.getEntry( Entry.LIMIT, meta.getRowLimit() ) );

    return list;
  }

  public TableInputMeta getMeta() {
    return meta;
  }
}
