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

import com.thoughtworks.xstream.XStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.*;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.TypeConverterFactory;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Map runner that uses the normal Kettle execution engine to process all input data during one single run.<p>
 * This relies on newly un-@Deprecated interfaces ({@link MapRunnable}, {@link JobConf}) in Hadoop 0.21.0.
 */
public class PentahoMapRunnable<K1, V1, K2, V2> implements MapRunnable<K1, V1, K2, V2> {

  protected static enum Counter {
    INPUT_RECORDS, OUTPUT_RECORDS, OUT_RECORD_WITH_NULL_KEY, OUT_RECORD_WITH_NULL_VALUE
  };

  protected String transMapXml;

  protected String transReduceXml;

  protected String mapInputStepName;

  protected String reduceInputStepName;

  protected String mapOutputStepName;

  protected String reduceOutputStepName;

  protected Class<K2> outClassK;

  protected Class<V2> outClassV;

  protected String id = UUID.randomUUID().toString();

  protected boolean debug = false;

  //  the transformation that will be used as a mapper or reducer
  protected Trans trans;
  
  protected VariableSpace variableSpace = null;
  
  protected LogLevel logLevel;
  
  protected OutputCollectorRowListener<K2, V2> rowCollector;

  public PentahoMapRunnable() throws KettleException {
  }

  public void configure(JobConf job) {
    debug = "true".equalsIgnoreCase(job.get("debug")); //$NON-NLS-1$

    transMapXml = job.get("transformation-map-xml");
    transReduceXml = job.get("transformation-reduce-xml");
    mapInputStepName = job.get("transformation-map-input-stepname");
    mapOutputStepName = job.get("transformation-map-output-stepname");
    reduceInputStepName = job.get("transformation-reduce-input-stepname");
    reduceOutputStepName = job.get("transformation-reduce-output-stepname");
    String xmlVariableSpace = job.get("variableSpace");
    
    outClassK = (Class<K2>) job.getMapOutputKeyClass();
    outClassV = (Class<V2>) job.getMapOutputValueClass();

    if (!Const.isEmpty(xmlVariableSpace)) {
       setDebugStatus("PentahoMapRunnable(): variableSpace was retrieved from the job.  The contents: ");
       setDebugStatus(xmlVariableSpace);
       
       //  deserialize from xml to variable space
       XStream xStream = new XStream();
       
       setDebugStatus("PentahoMapRunnable(): Setting classes variableSpace property.: ");
       variableSpace = (VariableSpace)xStream.fromXML(xmlVariableSpace);
    }
    else {
      setDebugStatus("PentahoMapRunnable(): The PDI Job's variable space was not sent.");
      variableSpace = new Variables();
   }

   // Pass some information to the transformation...
   //
   variableSpace.setVariable("Internal.Hadoop.NumMapTasks", Integer.toString(job.getNumMapTasks()));
   variableSpace.setVariable("Internal.Hadoop.NumReduceTasks", Integer.toString(job.getNumReduceTasks()));
   String taskId = job.get("mapred.task.id");
   variableSpace.setVariable("Internal.Hadoop.TaskId", taskId);
   // TODO: Verify if the string range holds true for all Hadoop distributions
   String nodeNumber = taskId==null || taskId.length()<34 ? "0" : taskId.substring(28, 34); 
   // get rid of zeroes.
   variableSpace.setVariable("Internal.Hadoop.NodeNumber", Integer.toString(Integer.valueOf(nodeNumber))); 
      
    setDebugStatus("Job configuration");
    setDebugStatus("Output key class: " + outClassK.getName());
    setDebugStatus("Output value class: " + outClassV.getName());
    
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

  public void injectValue(Object key, ITypeConverter inConverterK, Object value, ITypeConverter inConverterV,
      RowMeta injectorRowMeta, RowProducer rowProducer, Reporter reporter) throws Exception {

    injectValue(key, 0, inConverterK, value, 1, inConverterV, injectorRowMeta, rowProducer, reporter);
  }

  public void injectValue(Object key, int keyOrdinal, ITypeConverter inConverterK, Object value, int valueOrdinal,
      ITypeConverter inConverterV, RowMeta injectorRowMeta, RowProducer rowProducer, Reporter reporter)
      throws Exception {
    Object[] row = new Object[injectorRowMeta.size()];
    row[keyOrdinal] = inConverterK != null ? inConverterK.convert(injectorRowMeta.getValueMeta(keyOrdinal), key) : key;
    row[valueOrdinal] = inConverterV != null ? inConverterV.convert(injectorRowMeta.getValueMeta(valueOrdinal), value)
        : value;

    if (debug) {
      setDebugStatus(reporter, "Injecting input record [" + row[keyOrdinal] + "] - [" + row[valueOrdinal] + "]");
    }

    rowProducer.putRow(injectorRowMeta, row);
  }

  protected void createTrans(final Configuration conf) {

    try {
      setDebugStatus("Creating a transformation for a map.");
      trans = MRUtil.getTrans(conf, transMapXml, false);
    } catch (KettleException ke) {
      throw new RuntimeException("Error loading transformation", ke); //$NON-NLS-1$
    }
  }

  public String getTransMapXml() {
    return transMapXml;
  }

  public void setTransMapXml(String transMapXml) {
    this.transMapXml = transMapXml;
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

  public void setOutClassK(Class<K2> outClassK) {
    this.outClassK = outClassK;
  }

  public Class<?> getOutClassV() {
    return outClassV;
  }

  public void setOutClassV(Class<V2> outClassV) {
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
    if (debug) {
      System.out.println(message);
      reporter.setStatus(message);
    }
  }

  private void setDebugStatus(String message) {
    if (debug) {
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
      if (rowMeta != null) {
        String[] fieldNames = rowMeta.getFieldNames();

        for (int i = 0; i < fieldNames.length; i++) {
          if (fieldNames[i].equalsIgnoreCase(getKeyName())) {
            keyOrdinal = i;
            if (valueOrdinal >= 0) {
              break;
            }
          } else if (fieldNames[i].equalsIgnoreCase(getValueName())) {
            valueOrdinal = i;
            if (keyOrdinal >= 0) {
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

  public void run(RecordReader<K1, V1> input, final OutputCollector<K2, V2> output, final Reporter reporter)
      throws IOException {
    try {
      if (trans == null) {
        throw new RuntimeException("Error initializing transformation.  See error log."); //$NON-NLS-1$
      } else {
        // Clean up old logging
        CentralLogStore.discardLines(trans.getLogChannelId(), true);
      }
      
      // Create a copy of trans so we don't continue to add new TransListeners and run into a ConcurrentModificationException
      // when this mapper is reused "quickly"
      trans = MRUtil.recreateTrans(trans);
      
      String logLinePrefix = getClass().getName()+".run: ";
      setDebugStatus(logLinePrefix + " The transformation was just recreated.");
       
      //  share the variables from the PDI job.
      //  we do this here instead of in createTrans() as MRUtil.recreateTrans() wil not 
      //  copy "execution" trans information.
      if (variableSpace != null) {
         setDebugStatus("Sharing the VariableSpace from the PDI job.");
         trans.shareVariablesWith(variableSpace);
      
         if (debug) {
            
            //  list the variables
            List<String> variables = Arrays.asList(trans.listVariables());
            Collections.sort(variables);
        
            if (variables != null) {
               setDebugStatus("Variables: ");
               for(String variable: variables) {
                  setDebugStatus("     "+variable+" = "+trans.getVariable(variable));
               }
            }
         }
      }
      else {
         setDebugStatus(reporter, "variableSpace is null.  We are not going to share it with the trans.");
      }
            
      //  set the trans' log level if we have our's set
      if (logLevel != null) {
         setDebugStatus("Setting the trans.logLevel to "+logLevel.toString());
         trans.setLogLevel(logLevel);
      }
      else {
         setDebugStatus("logLevel is null.  The trans log level will not be set.");
      }
            
      // allocate key & value instances that are re-used for all entries
      K1 key = input.createKey();
      V1 value = input.createValue();

      setDebugStatus(reporter, "Preparing transformation for execution");
      trans.prepareExecution(null);

      try {
        setDebugStatus(reporter, "Locating output step: " + mapOutputStepName);
        StepInterface outputStep = trans.findRunThread(mapOutputStepName);
        if (outputStep != null) {
          rowCollector = new OutputCollectorRowListener(output, outClassK, outClassV, reporter, debug);
//          rowCollector = OutputCollectorRowListener.build(output, outputRowMeta, outClassK, outClassV, reporter, debug);
          outputStep.addRowListener(rowCollector);

          RowMeta injectorRowMeta = new RowMeta();
          RowProducer rowProducer = null;
          TypeConverterFactory typeConverterFactory = new TypeConverterFactory();
          ITypeConverter inConverterK = null;
          ITypeConverter inConverterV = null;

          setDebugStatus(reporter, "Locating input step: " + mapInputStepName);
          if (mapInputStepName != null) {
            // Setup row injection
            rowProducer = trans.addRowProducer(mapInputStepName, 0);
            StepInterface inputStep = rowProducer.getStepInterface();
            StepMetaInterface inputStepMeta = inputStep.getStepMeta().getStepMetaInterface();

            InKeyValueOrdinals inOrdinals = null;
            if (inputStepMeta instanceof BaseStepMeta) {
              setDebugStatus(reporter,
                  "Generating converters from RowMeta for injection into the mapper transformation");

              // Use getFields(...) to get the row meta and therefore the expected input types
              inputStepMeta.getFields(injectorRowMeta, null, null, null, null);

              inOrdinals = new InKeyValueOrdinals(injectorRowMeta);

              if (inOrdinals.getKeyOrdinal() < 0 || inOrdinals.getValueOrdinal() < 0) {
                throw new KettleException("key or value is not defined in transformation injector step");
              }

              // Get a converter for the Key if the value meta has a concrete Java class we can use. 
              // If no converter can be found here we wont do any type conversion.
              if (injectorRowMeta.getValueMeta(inOrdinals.getKeyOrdinal()) != null) {
                inConverterK = typeConverterFactory.getConverter(key.getClass(), injectorRowMeta.getValueMeta(inOrdinals.getKeyOrdinal()));
              }

              // Get a converter for the Value if the value meta has a concrete Java class we can use. 
              // If no converter can be found here we wont do any type conversion.
              if (injectorRowMeta.getValueMeta(inOrdinals.getValueOrdinal()) != null) {
                inConverterV = typeConverterFactory.getConverter(value.getClass(), injectorRowMeta.getValueMeta(inOrdinals.getValueOrdinal()));
              }
            }

            trans.startThreads();
            if (rowProducer != null) {

              while (input.next(key, value)) {
                if (inOrdinals != null) {
                  injectValue(key, inOrdinals.getKeyOrdinal(), inConverterK, value, inOrdinals.getValueOrdinal(),
                      inConverterV, injectorRowMeta, rowProducer, reporter);
                } else {
                  injectValue(key, inConverterK, value, inConverterV, injectorRowMeta, rowProducer, reporter);
                }
              }

              rowProducer.finished();
            }

            trans.waitUntilFinished();
            setDebugStatus(reporter, "Mapper transformation has finished");
            if (trans.getErrors() > 0) {
              setDebugStatus("Errors detected for mapper transformation");
              List<LoggingEvent> logList = CentralLogStore.getLogBufferFromTo(trans.getLogChannelId(), false, 0, CentralLogStore.getLastBufferLineNr());

              StringBuffer buff = new StringBuffer();
              for (LoggingEvent le : logList) {
                if (le.getLevel() == Level.ERROR) {
                  buff.append(le.getMessage().toString()).append("\n");
                }
              }
              throw new Exception("Errors were detected for mapper transformation:\n\n" 
                  + buff.toString());
            }

          } else {
            setDebugStatus(reporter, "No input stepname was defined");
          }
          if (getException() != null) {
            setDebugStatus(reporter, "An exception was generated by the mapper transformation");
            // Bubble the exception from within Kettle to Hadoop
            throw getException();
          }

        } else {
          if (mapOutputStepName != null) {
            setDebugStatus(reporter, "Output step [" + mapOutputStepName + "]could not be found");
            throw new KettleException("Output step not defined in transformation");
          } else {
            setDebugStatus(reporter, "Output step name not specified");
          }
        }
      } finally {
        try {
          trans.stopAll();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        try {
          trans.cleanup();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
      setDebugStatus(reporter, "An exception was generated by the mapper task");
      throw new IOException(e);
    }
    reporter.setStatus("Completed processing record");
  }

}
