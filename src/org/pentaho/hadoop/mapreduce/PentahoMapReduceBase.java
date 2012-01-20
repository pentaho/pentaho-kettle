/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.hadoop.mapreduce;

import java.io.IOException;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Reporter;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;

import com.thoughtworks.xstream.XStream;

@SuppressWarnings("deprecation")
public class PentahoMapReduceBase<K, V> extends MapReduceBase {
   
  protected static enum Counter {
    INPUT_RECORDS,
    OUTPUT_RECORDS,
    OUT_RECORD_WITH_NULL_KEY,
    OUT_RECORD_WITH_NULL_VALUE
  }

  public static final String STRING_COMBINE_SINGLE_THREADED = "transformation-combine-single-threaded";
  public static final String STRING_REDUCE_SINGLE_THREADED = "transformation-reduce-single-threaded";

  protected String transMapXml;
  protected String transCombinerXml;
  protected String transReduceXml;

  protected String mapInputStepName;
  protected String combinerInputStepName;
  protected String reduceInputStepName;

  protected String mapOutputStepName;
  protected String combinerOutputStepName;
  protected String reduceOutputStepName;
  
  protected VariableSpace variableSpace = null;

  protected Class<K> outClassK;
  protected Class<V> outClassV;

  protected String id = UUID.randomUUID().toString();

  protected boolean debug = false;
  
  protected LogLevel logLevel;
  
  //  the transformation that will be used as a mapper or reducer
  protected Trans trans;
  
  //  One of these is what trans is to be used as
  public static enum MROperations {Map, Combine, Reduce};
  
  //  we set this to what this object is being used for - map or reduce
  protected MROperations mrOperation;

  protected OutputCollectorRowListener<K, V> rowCollector;
  protected boolean combineSingleThreaded;
  protected boolean reduceSingleThreaded;
  
  public PentahoMapReduceBase() throws KettleException {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void configure(JobConf job) {
    super.configure(job);
    
    debug = "true".equalsIgnoreCase(job.get("debug")); //$NON-NLS-1$
    
    transMapXml = job.get("transformation-map-xml");
    transCombinerXml = job.get("transformation-combiner-xml");
    transReduceXml = job.get("transformation-reduce-xml");
    mapInputStepName = job.get("transformation-map-input-stepname");
    mapOutputStepName = job.get("transformation-map-output-stepname");
    combinerInputStepName = job.get("transformation-combiner-input-stepname");
    combinerOutputStepName = job.get("transformation-combiner-output-stepname");
    combineSingleThreaded = isCombinerSingleThreaded(job);
    reduceInputStepName = job.get("transformation-reduce-input-stepname");
    reduceOutputStepName = job.get("transformation-reduce-output-stepname");
    reduceSingleThreaded = isReducerSingleThreaded(job);
    String xmlVariableSpace = job.get("variableSpace");
    
    if (!Const.isEmpty(xmlVariableSpace)) {
       setDebugStatus("PentahoMapReduceBase. variableSpace was retrieved from the job.  The contents: ");
       
       //  deserialize from xml to variable space
       XStream xStream = new XStream();
          
       if (xStream != null) {
           setDebugStatus("PentahoMapReduceBase: Setting classes variableSpace property.: ");
           variableSpace = (VariableSpace)xStream.fromXML(xmlVariableSpace);
       }
    }
    else {
      setDebugStatus("PentahoMapReduceBase: The PDI Job's variable space was not found in the job configuration.");
    }
    
    outClassK = (Class<K>) job.getOutputKeyClass();
    outClassV = (Class<V>) job.getOutputValueClass();
    
    if(debug) {
      System.out.println("Job configuration>");
      System.out.println("Output key class: " + outClassK.getName());
      System.out.println("Output value class: " + outClassV.getName());   
    }
    
    //  set the log level to what the level of the job is
    String stringLogLevel = job.get("logLevel");
    if (!Const.isEmpty(stringLogLevel)) {
       logLevel = LogLevel.valueOf(stringLogLevel);
       setDebugStatus("Log level set to "+stringLogLevel);
    }
    else {
       System.out.println("Could not retrieve the log level from the job configuration.  logLevel will not be set.");
    }
    
    createTrans(job);
  }

  @Override
  public void close() throws IOException {
    super.close();
  }

  @Deprecated
  /**
   * Use the other injectValue method - The paramters have been arranged to be more uniform
   */
  public void injectValue(Object key, ITypeConverter inConverterK, ITypeConverter inConverterV, RowMeta injectorRowMeta, RowProducer rowProducer, Object value, Reporter reporter) throws Exception {
    injectValue(key, inConverterK, value, inConverterV, injectorRowMeta, rowProducer, reporter);
  }
  
  public void injectValue(Object key, ITypeConverter inConverterK, 
      Object value, ITypeConverter inConverterV,
      RowMetaInterface injectorRowMeta, RowProducer rowProducer, Reporter reporter)
      throws Exception {
    
    injectValue(key, 0, inConverterK, value, 1, inConverterV, injectorRowMeta, rowProducer, reporter);
  }
  
  public void injectValue(Object key, int keyOrdinal, ITypeConverter inConverterK, 
                          Object value, int valueOrdinal, ITypeConverter inConverterV,
                          RowMetaInterface injectorRowMeta, RowProducer rowProducer, Reporter reporter)
                          throws Exception {
    Object[] row = new Object[injectorRowMeta.size()];
    row[keyOrdinal] = inConverterK != null ? inConverterK.convert(injectorRowMeta.getValueMeta(keyOrdinal), key) : key;
    row[valueOrdinal] = inConverterV != null ? inConverterV.convert(injectorRowMeta.getValueMeta(valueOrdinal), value) : value;

    if (debug) {
      setDebugStatus(reporter, "Injecting input record [" + row[keyOrdinal] + "] - [" + row[valueOrdinal] + "]");
    }
    
    rowProducer.putRow(injectorRowMeta, row);
  }

  protected void createTrans(final Configuration conf) {
      
      if (mrOperation == null) {
          throw new RuntimeException("Map or reduce operation has not been specified.  Call setMRType from implementing classes constructor.");
      }
      
      try {
          if (mrOperation.equals(MROperations.Map)) {
              setDebugStatus("Creating a transformation for a map.");
              trans = MRUtil.getTrans(conf, transMapXml, false);
          }
          else if (mrOperation.equals(MROperations.Combine)) {
              setDebugStatus("Creating a transformation for a combiner.");
              trans = MRUtil.getTrans(conf, transCombinerXml, isCombinerSingleThreaded(conf));
        }
          else if (mrOperation.equals(MROperations.Reduce)) {
              setDebugStatus("Creating a transformation for a reduce.");
              trans = MRUtil.getTrans(conf, transReduceXml, isReducerSingleThreaded(conf));
          }
      }      
      catch (KettleException ke) {
          throw new RuntimeException("Error loading transformation for " + mrOperation, ke); //$NON-NLS-1$
      }
      
  }

  private boolean isCombinerSingleThreaded(final Configuration conf) {
    return "true".equalsIgnoreCase(conf.get(STRING_COMBINE_SINGLE_THREADED));
  }
  
  private boolean isReducerSingleThreaded(final Configuration conf) {
    return "true".equalsIgnoreCase(conf.get(STRING_REDUCE_SINGLE_THREADED));
  }

  public void setMRType(MROperations mrOperation) {
      this.mrOperation = mrOperation;   
  }
  
  public String getTransMapXml() {
    return transMapXml;
  }

  public void setTransMapXml(String transMapXml) {
    this.transMapXml = transMapXml;
  }

  public String getTransCombinerXml() {
    return transCombinerXml;
  }

  public void setCombinerMapXml(String transCombinerXml) {
    this.transCombinerXml = transCombinerXml;
  }
  
  public String getTransReduceXml() {
    return transReduceXml;
  }

  public void setTransReduceXml(String transReduceXml) {
    this.transReduceXml = transReduceXml;
  }

  public String getMapInputStepName() {
    return mapInputStepName;
  }

  public void setMapInputStepName(String mapInputStepName) {
    this.mapInputStepName = mapInputStepName;
  }

  public String getMapOutputStepName() {
    return mapOutputStepName;
  }

  public void setMapOutputStepName(String mapOutputStepName) {
    this.mapOutputStepName = mapOutputStepName;
  }

  public String getCombinerInputStepName() {
    return combinerInputStepName;
  }

  public void setCombinerInputStepName(String combinerInputStepName) {
    this.combinerInputStepName = combinerInputStepName;
  }

  public String getCombinerOutputStepName() {
    return combinerOutputStepName;
  }

  public void setCombinerOutputStepName(String combinerOutputStepName) {
    this.combinerOutputStepName = combinerOutputStepName;
  }
  
  public String getReduceInputStepName() {
    return reduceInputStepName;
  }

  public void setReduceInputStepName(String reduceInputStepName) {
    this.reduceInputStepName = reduceInputStepName;
  }

  public String getReduceOutputStepName() {
    return reduceOutputStepName;
  }

  public void setReduceOutputStepName(String reduceOutputStepName) {
    this.reduceOutputStepName = reduceOutputStepName;
  }

  public Class<?> getOutClassK() {
    return outClassK;
  }

  public void setOutClassK(Class<K> outClassK) {
    this.outClassK = outClassK;
  }

  public Class<?> getOutClassV() {
    return outClassV;
  }

  public void setOutClassV(Class<V> outClassV) {
    this.outClassV = outClassV;
  }

  public Trans getTrans() {
    return trans;
  }

  public void setTrans(Trans trans) {
    this.trans = trans;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Exception getException() {
    return rowCollector != null ? rowCollector.getException() : null;
  }
  
  public void setDebugStatus(Reporter reporter, String message) {
    if(debug) {
      System.out.println(message);
      reporter.setStatus(message);
    }
  }

  private void setDebugStatus(String message) {
      if(debug) {
          System.out.println(message);
        }     
  }
  
  protected static class OutKeyValueOrdinals extends BaseKeyValueOrdinals {
    public OutKeyValueOrdinals(RowMetaInterface rowMeta) {
      super(rowMeta);
    }

    @Override
    protected final String getKeyName() {
      return "outKey"; //$NON-NLS-1$
    }

    @Override
    protected final String getValueName() {
      return "outValue"; //$NON-NLS-1$
    }
  }
  
  protected static class InKeyValueOrdinals extends BaseKeyValueOrdinals {
    public InKeyValueOrdinals(RowMetaInterface rowMeta) {
      super(rowMeta);
    }

    @Override
    protected final String getKeyName() {
      return "key"; //$NON-NLS-1$
    }

    @Override
    protected final String getValueName() {
      return "value"; //$NON-NLS-1$
    }
  }
  
  protected static abstract class BaseKeyValueOrdinals {
    private int keyOrdinal = -1;
    private int valueOrdinal = -1;
    
    public BaseKeyValueOrdinals(RowMetaInterface rowMeta) {
      if(rowMeta != null) {
        String[] fieldNames = rowMeta.getFieldNames();    
        
        for(int i = 0; i < fieldNames.length; i++) {
          if(fieldNames[i].equalsIgnoreCase(getKeyName())) {
            keyOrdinal = i;
            if(valueOrdinal >= 0) {
              break;
            }
          } else if(fieldNames[i].equalsIgnoreCase(getValueName())) {
            valueOrdinal= i;
            if(keyOrdinal >= 0) {
              break;
            }
          }
        }
      }
    }
    
    protected abstract String getKeyName();
    protected abstract String getValueName();
    
    public int getKeyOrdinal() {
      return keyOrdinal;
    }
    
    public int getValueOrdinal() {
      return valueOrdinal;
    }
  }  
}
