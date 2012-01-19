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

import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.hadoop.mapreduce.PentahoMapRunnable.Counter;
import org.pentaho.hadoop.mapreduce.PentahoMapRunnable.OutKeyValueOrdinals;

/**
 * Row Listener that forwards rows along to an {@link OutputCollector}.
 * 
 */
public class OutputCollectorRowListener<K, V> extends RowAdapter {
  private boolean debug;

  private Reporter reporter;

  private Class<K> outClassK;

  private Class<V> outClassV;

  private OutputCollector<K, V> output;

  private Exception exception;
  
  private OutKeyValueOrdinals outOrdinals;

  public OutputCollectorRowListener(OutputCollector<K, V> output, Class<K> outClassK, Class<V> outClassV,
      Reporter reporter, boolean debug) {
    this.output = output;
    this.outClassK = outClassK;
    this.outClassV = outClassV;
    this.reporter = reporter;
    this.debug = debug;
    
    outOrdinals = null;
  }

  @Override
  public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
    try {
      /*
       * Operation: 
       * Column 1: Key (convert to outClassK) 
       * Column 2: Value (convert to outClassV)
       */
      if (row != null && !rowMeta.isEmpty() && rowMeta.size() >= 2) {
        if (outOrdinals==null) {
          outOrdinals = new OutKeyValueOrdinals(rowMeta);

          if (outOrdinals.getKeyOrdinal() < 0 || outOrdinals.getValueOrdinal() < 0) {
            throw new KettleException("outKey or outValue is not defined in transformation output stream"); //$NON-NLS-1$
          }
        }

        // TODO Implement type safe converters
        
        if (debug) {
          setDebugStatus( reporter,
            "Begin conversion of output key [from:" + (row[outOrdinals.getKeyOrdinal()] == null ? null : row[outOrdinals.getKeyOrdinal()].getClass()) + "] [to:" + outClassK + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        Object outKey = TypeConverterFactory
            .getInstance()
            .getConverter(
                row[outOrdinals.getKeyOrdinal()] == null ? null : row[outOrdinals.getKeyOrdinal()].getClass(),
                outClassK).convert(rowMeta.getValueMeta(outOrdinals.getKeyOrdinal()), row[outOrdinals.getKeyOrdinal()]);

       if (debug) {
         setDebugStatus(reporter,
            "Begin conversion of output value [from:" + (row[outOrdinals.getValueOrdinal()] == null ? null //$NON-NLS-1$
                : row[outOrdinals.getValueOrdinal()].getClass()) + "] [to:" + outClassV + "]")  ; //$NON-NLS-1$ //$NON-NLS-2$
       }
        Object outVal = TypeConverterFactory
            .getInstance()
            .getConverter(
                row[outOrdinals.getValueOrdinal()] == null ? null : row[outOrdinals.getValueOrdinal()].getClass(),
                outClassV)
            .convert(rowMeta.getValueMeta(outOrdinals.getValueOrdinal()), row[outOrdinals.getValueOrdinal()]);

        if (outKey != null && outVal != null) {
          if (debug) setDebugStatus(reporter, "Collecting output record [" + outKey + "] - [" + outVal + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          // TODO Implement type safe converters
          @SuppressWarnings("unchecked")
          K k = (K) outKey;
          // TODO Implement type safe converters
          @SuppressWarnings("unchecked")
          V v = (V) outVal;
          output.collect(k, v);
        } else {
          if (outKey == null) {
            if (debug) setDebugStatus(reporter, "Transformation returned a null key"); //$NON-NLS-1$
            reporter.incrCounter(Counter.OUT_RECORD_WITH_NULL_KEY, 1);
          }
          if (outVal == null) {
            if (debug) setDebugStatus(reporter, "Transformation returned a null value"); //$NON-NLS-1$
            reporter.incrCounter(Counter.OUT_RECORD_WITH_NULL_VALUE, 1);
          }
        }
      } else {
        if (row == null || rowMeta.isEmpty()) {
          if (debug) setDebugStatus(reporter, "Invalid row received from transformation"); //$NON-NLS-1$
        } else if (rowMeta.size() < 2) {
          if (debug) setDebugStatus(reporter, "Invalid row format. Expected key/value columns, but received " + rowMeta.size() //$NON-NLS-1$
              + " columns"); //$NON-NLS-1$
        } else {
          OutKeyValueOrdinals outOrdinals = new OutKeyValueOrdinals(rowMeta);
          if (outOrdinals.getKeyOrdinal() < 0 || outOrdinals.getValueOrdinal() < 0) {
            if (debug) setDebugStatus(reporter, "outKey or outValue is missing from the transformation output step"); //$NON-NLS-1$
          }
          if (debug) setDebugStatus(reporter, "Unknown issue with received data from transformation"); //$NON-NLS-1$
        }
      }
    } catch (Exception ex) {
      setDebugStatus(reporter, "Unexpected exception recieved: " + ex.getMessage()); //$NON-NLS-1$
      exception = ex;
      throw new RuntimeException(ex);
    }
  }

  /**
   * Set the reporter status if {@code debug == true}.
   */
  public void setDebugStatus(Reporter reporter, String message) {
    if (debug) {
      System.out.println(message);
      reporter.setStatus(message);
    }
  }

  /**
   * @return The exception thrown from {@link #rowWrittenEvent(RowMetaInterface, Object[])}. 
   */
  public Exception getException() {
    return exception;
  }
}
