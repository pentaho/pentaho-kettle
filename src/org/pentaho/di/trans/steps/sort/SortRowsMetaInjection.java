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

package org.pentaho.di.trans.steps.sort;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * This takes care of the external metadata injection into the SortRowsMeta class
 * 
 * @author Matt
 */
public class SortRowsMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    SORT_SIZE_ROWS(ValueMetaInterface.TYPE_STRING, "In memory sort size (in rows)"),
    SORT_DIRECTORY(ValueMetaInterface.TYPE_STRING, "The sort directory"),
    SORT_FILE_PREFIX(ValueMetaInterface.TYPE_STRING, "The sort file prefix"),
    FREE_MEMORY_TRESHOLD(ValueMetaInterface.TYPE_STRING, "The free memory treshold (in %)"),
    ONLY_PASS_UNIQUE_ROWS(ValueMetaInterface.TYPE_STRING, "Only pass unique rows? (Y/N)"),
    COMPRESS_TEMP_FILES(ValueMetaInterface.TYPE_STRING, "Compress temporary files? (Y/N)"),

    FIELDS(ValueMetaInterface.TYPE_NONE, "All the fields to sort"),
    FIELD(ValueMetaInterface.TYPE_NONE, "One field to sort"),

    NAME(ValueMetaInterface.TYPE_STRING, "Field name"),
    SORT_ASCENDING(ValueMetaInterface.TYPE_STRING, "Sort ascending? (Y/N)"),
    IGNORE_CASE(ValueMetaInterface.TYPE_STRING, "Ignore case? (Y/N)"),
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

  
  private SortRowsMeta meta;

  public SortRowsMetaInjection(SortRowsMeta meta) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();
    
    Entry[] topEntries = new Entry[] { Entry.SORT_SIZE_ROWS, Entry.ONLY_PASS_UNIQUE_ROWS, Entry.COMPRESS_TEMP_FILES, 
        Entry.SORT_DIRECTORY, Entry.SORT_FILE_PREFIX, Entry.FREE_MEMORY_TRESHOLD, };
    for (Entry topEntry : topEntries) {
      all.add(new StepInjectionMetaEntry(topEntry.name(), topEntry.getValueType(), topEntry.getDescription()));
    }
    
    StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry("FIELDS", ValueMetaInterface.TYPE_NONE, Entry.FIELDS.description);
    all.add(fieldsEntry);

    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry("FIELD", ValueMetaInterface.TYPE_NONE, Entry.FIELD.description);
    fieldsEntry.getDetails().add(fieldEntry);
    
    Entry[] fieldsEntries = new Entry[] { Entry.NAME, Entry.SORT_ASCENDING, Entry.IGNORE_CASE, };
    for (Entry entry : fieldsEntries) {
      StepInjectionMetaEntry metaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
      fieldEntry.getDetails().add(metaEntry);
    }
    
    return all;
  }

  @Override
  public void injectStepMetadataEntries(List<StepInjectionMetaEntry> all) throws KettleException {
    
    List<String> sortNames = new ArrayList<String>();
    List<Boolean> sortAscs = new ArrayList<Boolean>();
    List<Boolean> sortCases = new ArrayList<Boolean>();
    
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
                  
                  String sortName = null;
                  boolean sortAsc = false;
                  boolean sortCase = false;
                  
                  List<StepInjectionMetaEntry> entries = lookField.getDetails();
                  for (StepInjectionMetaEntry entry : entries) {
                    Entry metaEntry = Entry.findEntry(entry.getKey());
                    if (metaEntry!=null) {
                      String value = (String)entry.getValue();
                      switch(metaEntry) {
                      case NAME:               sortName = value; break;
                      case SORT_ASCENDING:     sortAsc = "Y".equalsIgnoreCase(value); break;
                      case IGNORE_CASE:        sortCase = "Y".equalsIgnoreCase(value); break;
                      }
                    }
                  }
                  sortNames.add(sortName);
                  sortAscs.add(sortAsc);
                  sortCases.add(sortCase);
                }
              }
            }
          }
          break;
       
        case COMPRESS_TEMP_FILES : meta.setCompressFiles("Y".equalsIgnoreCase(lookValue)); break;
        case ONLY_PASS_UNIQUE_ROWS : meta.setOnlyPassingUniqueRows("Y".equalsIgnoreCase(lookValue)); break;
        case SORT_SIZE_ROWS: meta.setSortSize(lookValue); break;
        case SORT_DIRECTORY: meta.setDirectory(lookValue); break;
        case SORT_FILE_PREFIX: meta.setPrefix(lookValue); break;
        case FREE_MEMORY_TRESHOLD: meta.setFreeMemoryLimit(lookValue); break;
        default:
          break;
        }
      }
    }

    // Pass the grid to the step metadata
    //
    meta.setFieldName(sortNames.toArray(new String[sortNames.size()]));
    boolean ascending[] = new boolean[sortAscs.size()];
    boolean cases[] = new boolean[sortCases.size()];
    for (int i=0;i<ascending.length;i++) {
      ascending[i] = sortAscs.get(i);
      cases[i] = sortCases.get(i);
    }
    meta.setAscending(ascending);
    meta.setCaseSensitive(cases);
  }

  public SortRowsMeta getMeta() {
    return meta;
  }

  

}
