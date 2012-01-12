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
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

@Deprecated
public class GenericTransMap<K extends WritableComparable, V extends Writable, K2, V2> extends PentahoMapReduceBase<K2, V2> implements Mapper<K, V, K2, V2> {

  
  public GenericTransMap() throws KettleException {
      super();
      this.setMRType(MROperations.Map);
  } 
    
  public void map(final K key, final V value, final OutputCollector<K2, V2> output, final Reporter reporter) throws IOException {
    try {
      reporter.setStatus("Begin processing record");

      if (trans == null) {
        throw new RuntimeException("Error initializing transformation.  See error log."); //$NON-NLS-1$
      }

      setDebugStatus(reporter, "Preparing transformation for execution");
      trans.prepareExecution(null);

      setDebugStatus(reporter, "Locating output step: " + mapOutputStepName);
      StepInterface outputStep = trans.findRunThread(mapOutputStepName);
      if (outputStep != null) {
        RowMeta injectorRowMeta = new RowMeta();

        rowCollector = new OutputCollectorRowListener<K2, V2>(output, outClassK, outClassV, reporter, debug);
        outputStep.addRowListener(rowCollector);

        RowProducer rowProducer = null;
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
            setDebugStatus(reporter, "Generating converters from RowMeta for injection into the mapper transformation");

            // Convert to BaseStepMeta and use getFields(...) to get the row meta and therefore the expected input types
            ((BaseStepMeta) inputStepMeta).getFields(injectorRowMeta, null, null, null, null);

            inOrdinals = new InKeyValueOrdinals(injectorRowMeta);
            
            if(inOrdinals.getKeyOrdinal() < 0 || inOrdinals.getValueOrdinal() < 0) {
              throw new KettleException("key or value is not defined in transformation injector step");
            }
            
            // Get a converter for the Key
            if (injectorRowMeta.getValueMeta(inOrdinals.getKeyOrdinal()) != null) {
              Class<?> metaClass = null;

              // Get the concrete java class that corresponds to a given Kettle meta data type
              metaClass = MRUtil.getJavaClass(injectorRowMeta.getValueMeta(inOrdinals.getKeyOrdinal()));

              if (metaClass != null) {
                // If a KettleType with a concrete conversion was found then use it
                inConverterK = TypeConverterFactory.getInstance().getConverter(key.getClass(), metaClass);
              }
            }

            // Get a converter for the Value
            if (injectorRowMeta.getValueMeta(inOrdinals.getValueOrdinal()) != null) {
              Class<?> metaClass = null;

              // Get the concrete java class that corresponds to a given Kettle meta data type
              metaClass = MRUtil.getJavaClass(injectorRowMeta.getValueMeta(inOrdinals.getValueOrdinal()));

              if (metaClass != null) {
                // If a KettleType with a concrete conversion was found then use it
                inConverterV = TypeConverterFactory.getInstance().getConverter(value.getClass(), metaClass);
              }
            }
          }

          trans.startThreads();
  
          if (rowProducer != null) {
            if(inOrdinals != null) {
              injectValue(key, inOrdinals.getKeyOrdinal(), inConverterK, value, inOrdinals.getValueOrdinal(), inConverterV, injectorRowMeta, rowProducer, reporter);
            } else {
              injectValue(key, inConverterK, value, inConverterV, injectorRowMeta, rowProducer, reporter);
            }
            rowProducer.finished();
          }
  
          trans.waitUntilFinished();
          setDebugStatus(reporter, "Mapper transformation has finished");

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
    } catch (Exception e) {
      e.printStackTrace(System.err);
      setDebugStatus(reporter, "An exception was generated by the mapper task");
      throw new IOException(e);
    }
    reporter.setStatus("Completed processing record");
  }

}