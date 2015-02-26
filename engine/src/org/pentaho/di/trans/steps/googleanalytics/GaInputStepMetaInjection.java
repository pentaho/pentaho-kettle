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

package org.pentaho.di.trans.steps.googleanalytics;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * This takes care of the external metadata injection into the GaInputStepMeta class
 * 
 * @author Matt
 */
public class GaInputStepMetaInjection implements StepMetaInjectionInterface {

  private enum Entry {

    APPLICATION_NAME(ValueMetaInterface.TYPE_STRING, "Application Name"),
    EMAIL(ValueMetaInterface.TYPE_STRING, "Email"),
    PASSWORD(ValueMetaInterface.TYPE_STRING, "Password"),
    SIMPLE_API_KEY(ValueMetaInterface.TYPE_STRING, "Simple API Key"),
    SPECIFY_TABLEID(ValueMetaInterface.TYPE_STRING, "Specify tableId"),

    START_DATE(ValueMetaInterface.TYPE_STRING, "Start Date (YYYY-MM-DD)"),
    END_DATE(ValueMetaInterface.TYPE_STRING, "End Date (YYYY-MM-DD)"),
    DIMENSIONS(ValueMetaInterface.TYPE_STRING, "Dimensions"),
    METRICS(ValueMetaInterface.TYPE_STRING, "Metrics"),
    FILTERS(ValueMetaInterface.TYPE_STRING, "Filters"),
    SORT(ValueMetaInterface.TYPE_STRING, "Sort"),
    USE_CUSTOM_SEGMENT(ValueMetaInterface.TYPE_STRING, "Use custom segment"),
    SEGMENT_ID(ValueMetaInterface.TYPE_STRING, "Segment Id"),
    SEGMENT_NAME(ValueMetaInterface.TYPE_STRING, "Use predefined segment"),

    OUTPUT_FIELDS(ValueMetaInterface.TYPE_NONE, "The output fields"),
    OUTPUT_FIELD(ValueMetaInterface.TYPE_NONE, "One output field"),
    OUTPUT_FIELDTYPE(ValueMetaInterface.TYPE_STRING, "Feed Field Type"),
    OUTPUT_FEED_FIELD(ValueMetaInterface.TYPE_STRING, "Feed Field"),
    OUTPUT_FIELDNAME(ValueMetaInterface.TYPE_STRING, "Output Field"),
    OUTPUT_TYPE(ValueMetaInterface.TYPE_STRING, "Output Type"),
    OUTPUT_FORMAT(ValueMetaInterface.TYPE_STRING, "Input Format"),

    LIMIT_SIZE(ValueMetaInterface.TYPE_STRING, "Limit Size"),
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

  
  private GaInputStepMeta meta;

  public GaInputStepMetaInjection(GaInputStepMeta meta) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();
    
    Entry[] topEntries = new Entry[] { Entry.APPLICATION_NAME, Entry.EMAIL, Entry.PASSWORD, 
      Entry.SIMPLE_API_KEY, Entry.SPECIFY_TABLEID, Entry.START_DATE, Entry.END_DATE, Entry.DIMENSIONS, 
      Entry.METRICS, Entry.FILTERS, Entry.SORT, Entry.USE_CUSTOM_SEGMENT, Entry.SEGMENT_ID, Entry.SEGMENT_NAME, Entry.LIMIT_SIZE, };
    for (Entry topEntry : topEntries) {
      all.add(new StepInjectionMetaEntry(topEntry.name(), topEntry.getValueType(), topEntry.getDescription()));
    }
    
    // The outputs
    //
    StepInjectionMetaEntry outputsEntry = new StepInjectionMetaEntry(Entry.OUTPUT_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.OUTPUT_FIELDS.description);
    all.add(outputsEntry);
    StepInjectionMetaEntry outputEntry = new StepInjectionMetaEntry(Entry.OUTPUT_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.OUTPUT_FIELD.description);
    outputsEntry.getDetails().add(outputEntry);
    
    Entry[] outputEntries = new Entry[] { Entry.OUTPUT_FIELDTYPE, Entry.OUTPUT_FEED_FIELD, Entry.OUTPUT_FIELDNAME, Entry.OUTPUT_TYPE, Entry.OUTPUT_FORMAT, };
    for (Entry entry : outputEntries) {
      StepInjectionMetaEntry metaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
      outputEntry.getDetails().add(metaEntry);
    }
    
    return all;
  }

  @Override
  public void injectStepMetadataEntries(List<StepInjectionMetaEntry> all) throws KettleException {
    
    List<String> outputFeedtypes = new ArrayList<String>();
    List<String> outputFeeds = new ArrayList<String>();
    List<String> outputFields = new ArrayList<String>();
    List<Integer> outputTypes = new ArrayList<Integer>();
    List<String> outputFormats = new ArrayList<String>();
    
    // Parse the fields, inject into the meta class..
    //
    for (StepInjectionMetaEntry lookFields : all) {
      Entry fieldsEntry = Entry.findEntry(lookFields.getKey());
      if (fieldsEntry==null) continue;

      String lookValue = (String)lookFields.getValue();
      switch(fieldsEntry) {
      case OUTPUT_FIELDS:
        {
          for (StepInjectionMetaEntry lookField : lookFields.getDetails()) {
            Entry fieldEntry = Entry.findEntry(lookField.getKey());
            if (fieldEntry == Entry.OUTPUT_FIELD) {

              String outputFeedtype = null;
              String outputFeed = null;
              String outputField = null;
              int outputType = -1;
              String outputFormat = null;
              
              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for (StepInjectionMetaEntry entry : entries) {
                Entry metaEntry = Entry.findEntry(entry.getKey());
                if (metaEntry!=null) {
                  String value = (String)entry.getValue();
                  switch(metaEntry) {
                  case OUTPUT_FIELDTYPE: outputFeedtype= value; break;
                  case OUTPUT_FEED_FIELD: outputFeed= value; break;
                  case OUTPUT_FIELDNAME: outputField= value; break;
                  case OUTPUT_TYPE: outputType= ValueMeta.getType(value); break;
                  case OUTPUT_FORMAT: outputFormat= value; break;
                  }
                }
              }
              outputFeedtypes.add(outputFeedtype);
              outputFeeds.add(outputFeed);
              outputFields.add(outputField);
              outputTypes.add(outputType);
              outputFormats.add(outputFormat);
            }
          }
        }
        break;
        
      case APPLICATION_NAME: meta.setGaAppName(lookValue); break;
      case EMAIL: meta.setGaEmail(lookValue); break;
      case PASSWORD: meta.setGaPassword(lookValue); break;
      case SIMPLE_API_KEY: meta.setGaApiKey(lookValue); break;
      case SPECIFY_TABLEID: meta.setGaCustomTableId(lookValue); break;
      case START_DATE: meta.setStartDate(lookValue); break;
      case END_DATE: meta.setEndDate(lookValue); break;
      case DIMENSIONS: meta.setDimensions(lookValue); break;
      case METRICS: meta.setMetrics(lookValue); break;
      case FILTERS: meta.setFilters(lookValue); break;
      case SORT: meta.setSort(lookValue); break;
      case USE_CUSTOM_SEGMENT: meta.setUseCustomSegment("Y".equalsIgnoreCase(lookValue)); break;
      case SEGMENT_ID: meta.setSegmentId(lookValue); break;
      case SEGMENT_NAME: meta.setSegmentName(lookValue); break;
      case LIMIT_SIZE: meta.setRowLimit(Integer.parseInt(lookValue)); break;
      }
    }
    

    // Pass the grid to the step metadata
    //
    meta.setFeedFieldType(outputFeedtypes.toArray(new String[outputFeedtypes.size()]));
    meta.setFeedField(outputFeeds.toArray(new String[outputFeeds.size()]));
    meta.setOutputField(outputFields.toArray(new String[outputFields.size()]));
    int[] types = new int[outputTypes.size()];
    for (int i=0;i<types.length;i++) {
      types[i] = outputTypes.get(i);
    }
    meta.setOutputType(types);
    meta.setConversionMask(outputFormats.toArray(new String[outputFormats.size()]));
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public GaInputStepMeta getMeta() {
    return meta;
  }
}
