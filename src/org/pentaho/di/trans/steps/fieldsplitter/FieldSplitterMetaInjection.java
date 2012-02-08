/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fieldsplitter;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.pms.util.Const;

/**
 * This takes care of the external metadata injection into the FieldSplitterMeta class
 * 
 * @author Matt
 */
public class FieldSplitterMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    FIELD_TO_SPLIT(ValueMetaInterface.TYPE_STRING, "The name of the field to split"),
    DELIMITER(ValueMetaInterface.TYPE_STRING, "The delimiter"),
    
    FIELDS(ValueMetaInterface.TYPE_NONE, "All the resulting fields"),
    FIELD(ValueMetaInterface.TYPE_NONE, "One result field"),

    NAME(ValueMetaInterface.TYPE_STRING, "Field name"),
    ID(ValueMetaInterface.TYPE_STRING, "The ID"),
    REMOVE_ID(ValueMetaInterface.TYPE_STRING, "Remove ID? (Y/N)"),
    DATA_TYPE(ValueMetaInterface.TYPE_STRING, "Data type (String, Number, ...)"),
    LENGTH(ValueMetaInterface.TYPE_STRING, "Length"),
    PRECISION(ValueMetaInterface.TYPE_STRING, "Precision"),
    FORMAT(ValueMetaInterface.TYPE_STRING, "The format (mask)"),
    GROUPING(ValueMetaInterface.TYPE_STRING, "The grouping symbol"),
    DECIMAL(ValueMetaInterface.TYPE_STRING, "The decimal symbol"),
    CURRENCY(ValueMetaInterface.TYPE_STRING, "The currency symbol"),
    NULL_IF(ValueMetaInterface.TYPE_STRING, "Value to convert to null"),
    DEFAULT(ValueMetaInterface.TYPE_STRING, "The default value in case of null"),
    TRIM_TYPE(ValueMetaInterface.TYPE_STRING, "The trim type (none, left, right, both)"),
    ;
    
    private int valueType;
    private String description;

    private Entry(int valueType, String description) {
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
    
    public static Entry findEntry(String key) {
      return Entry.valueOf(key);
    }
  }

  
  private FieldSplitterMeta meta;

  public FieldSplitterMetaInjection(FieldSplitterMeta meta) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();
    
    Entry[] topEntries = new Entry[] { Entry.FIELD_TO_SPLIT, Entry.DELIMITER, };
    for (Entry topEntry : topEntries) {
      all.add(new StepInjectionMetaEntry(topEntry.name(), topEntry.getValueType(), topEntry.getDescription()));
    }
    
    StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry("FIELDS", ValueMetaInterface.TYPE_NONE, Entry.FIELDS.description);
    all.add(fieldsEntry);

    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry("FIELD", ValueMetaInterface.TYPE_NONE, Entry.FIELD.description);
    fieldsEntry.getDetails().add(fieldEntry);
    
    Entry[] fieldsEntries = new Entry[] { Entry.NAME, Entry.ID, Entry.REMOVE_ID, Entry.DATA_TYPE, Entry.LENGTH, Entry.PRECISION,
        Entry.FORMAT, Entry.GROUPING, Entry.DECIMAL, Entry.CURRENCY, Entry.NULL_IF, Entry.DEFAULT, Entry.TRIM_TYPE, };
    for (Entry entry : fieldsEntries) {
      StepInjectionMetaEntry metaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
      fieldEntry.getDetails().add(metaEntry);
    }
    
    return all;
  }
  
  private class Split {
    String name;
    String id;
    boolean removeId;
    int dataType;
    int length;
    int precision;
    String format;
    String grouping;
    String decimal;
    String currency;
    String nullIf;
    String ifNull;
    int trimType;
  }

  @Override
  public void injectStepMetadataEntries(List<StepInjectionMetaEntry> all) throws KettleException {
   
    List<Split> splits = new ArrayList<Split>();
    
    // Parse the fields, inject into the meta class..
    //
    for (StepInjectionMetaEntry lookFields : all) {
      Entry fieldsEntry = Entry.findEntry(lookFields.getKey());
      if (fieldsEntry!=null) {

        String lookValue = (String)lookFields.getValue();
        switch(fieldsEntry) {
        case FIELDS:
          {
            for (StepInjectionMetaEntry lookField : lookFields.getDetails()) {
              Entry fieldEntry = Entry.findEntry(lookField.getKey());
              if (fieldEntry!=null) {
                if (fieldEntry == Entry.FIELD) {
                  
                  Split split = new Split();
                  
                  List<StepInjectionMetaEntry> entries = lookField.getDetails();
                  for (StepInjectionMetaEntry entry : entries) {
                    Entry metaEntry = Entry.findEntry(entry.getKey());
                    if (metaEntry!=null) {
                      String value = (String)entry.getValue();
                      switch(metaEntry) {
                      case NAME:  split.name = value; break;
                      case ID:  split.id= value; break;
                      case REMOVE_ID:  split.removeId = "Y".equalsIgnoreCase(value); break;
                      case DATA_TYPE: split.dataType= ValueMeta.getType(value); break;
                      case LENGTH:  split.length = Const.toInt(value, -1); break;
                      case PRECISION:  split.precision = Const.toInt(value, -1); break;
                      case FORMAT:  split.format = value; break;
                      case GROUPING:  split.grouping = value; break;
                      case DECIMAL:  split.decimal = value; break;
                      case CURRENCY:  split.currency = value; break;
                      case NULL_IF:  split.nullIf = value; break;
                      case DEFAULT:  split.ifNull = value; break;
                      case TRIM_TYPE:  split.trimType = ValueMeta.getTrimTypeByCode(value); break;
                      }
                    }
                  }
                  splits.add(split);
                }
              }
            }
          }
          break;
       
        case FIELD_TO_SPLIT: meta.setSplitField(lookValue); break;
        case DELIMITER: meta.setDelimiter(lookValue); break;
        default:
          break;
        }
      }
    }

    // Pass the grid to the step metadata
    //
    meta.allocate(splits.size());
    for (int i = 0; i < splits.size(); i++) {
      Split split = splits.get(i);

      meta.getFieldName()[i] = split.name;
      meta.getFieldID()[i] = split.id;
      meta.getFieldRemoveID()[i] = split.removeId;
      meta.getFieldType()[i] = split.dataType;
      meta.getFieldFormat()[i] = split.format;
      meta.getFieldGroup()[i] = split.grouping;
      meta.getFieldDecimal()[i] = split.decimal;
      meta.getFieldCurrency()[i] = split.currency;
      meta.getFieldLength()[i] = split.length;
      meta.getFieldPrecision()[i] = split.precision;
      meta.getFieldNullIf()[i] = split.nullIf;
      meta.getFieldIfNull()[i] = split.ifNull;
      meta.getFieldTrimType()[i] = split.trimType;
    }
  }

  public FieldSplitterMeta getMeta() {
    return meta;
  }

  

}
