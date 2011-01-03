package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaDataCombi;

public class WordCountSingleThreadTest extends TestCase {

  public void testWordCountMapper() throws Exception {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta("testfiles/wordcount-mapper.ktr");
    transMeta.setTransformationType(TransformationType.SingleThreaded);
    
    long transStart = System.currentTimeMillis();

    // Now execute the transformation...
    Trans trans = new Trans(transMeta);
    trans.setLogLevel(LogLevel.MINIMAL);

    trans.prepareExecution(null);

    StepInterface si = trans.getStepInterface("Output", 0);
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener(rc);

    RowProducer rp = trans.addRowProducer("Injector", 0);
    trans.startThreads();
    
    String metricsStep = "Remove garbage";

    // The single threaded transformation type expects us to run the steps
    // ourselves.
    //
    SingleThreadedTransExecutor executor = new SingleThreadedTransExecutor(trans);

    // Initialize all steps
    //
    executor.init();

    int iterations = 1000000;
    long totalWait = 0;
    List<RowMetaAndData> inputList = createMapperData();

    for (int i = 0; i < iterations; i++) {
      // add rows
      for (RowMetaAndData rm : inputList) {
        Object[] copy = rm.getRowMeta().cloneRow(rm.getData());
        rp.putRow(rm.getRowMeta(), copy);
      }

      long start = System.currentTimeMillis();

      boolean cont = executor.oneIteration();
      if (!cont) {
        fail("We don't expect any step or the transformation to be done before the end of all iterations.");
      }

      long end = System.currentTimeMillis();
      long delay = end - start;
      totalWait += delay;
      if (i > 0 && (i % 100000) == 0) {
        long rowsProcessed = trans.findRunThread(metricsStep).getLinesRead();
        double speed = Const.round(((double)rowsProcessed) / ((double) (end - transStart) / 1000), 1);
        int totalRows = 0;
        for (StepMetaDataCombi combi : trans.getSteps()) {
          for (RowSet rowSet : combi.step.getInputRowSets())
            totalRows += rowSet.size();
          for (RowSet rowSet : combi.step.getOutputRowSets())
            totalRows += rowSet.size();
        }
        System.out.println("#" + i + " : Finished processing one iteration in " + delay + "ms, average is: " + Const.round(((double) totalWait / (i + 1)), 1) + ", speed=" + speed + " row/s, total rows buffered: " + totalRows);
      }

      List<RowMetaAndData> resultRows = rc.getRowsWritten();

      // Result has one row less because we filter out one.
      // We also join with 3 identical rows in a data grid, giving 9 rows of which 3 are filtered out
      //
      assertEquals("Error found in iteration "+i+" : not the expected amount of output rows.", 9, resultRows.size());
      rc.clear();
    }

    rp.finished();

    // Dispose all steps.
    //
    executor.dispose();
    
    long rowsProcessed = trans.findRunThread(metricsStep).getLinesRead();

    long transEnd = System.currentTimeMillis();
    long transTime = transEnd - transStart;
    System.out.println("Average delay before idle : " + Const.round(((double) totalWait / iterations), 1));
    double transTimeSeconds = Const.round(((double) transTime / 1000), 1);
    System.out.println("Total transformation runtime for " + iterations + " iterations :" + transTimeSeconds + " seconds");
    double transTimePerIteration = Const.round(((double) transTime / iterations), 2);
    System.out.println("Runtime per iteration: " + transTimePerIteration + " miliseconds");
    double rowsPerSecond = Const.round(((double) rowsProcessed) / ((double) transTime / 1000), 1);
    System.out.println("Average speed: " + rowsPerSecond + " rows/second");
  }
  
  public void testWordCountReducer() throws Exception {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta("testfiles/wordcount-reducer.ktr");
    transMeta.setTransformationType(TransformationType.SingleThreaded);
    
    long transStart = System.currentTimeMillis();

    // Now execute the transformation...
    Trans trans = new Trans(transMeta);
    trans.setLogLevel(LogLevel.MINIMAL);

    trans.prepareExecution(null);

    StepInterface si = trans.getStepInterface("Output", 0);
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener(rc);

    RowProducer rp = trans.addRowProducer("Injector", 0);
    trans.startThreads();
    
    String metricsStep = "Injector";

    // The single threaded transformation type expects us to run the steps
    // ourselves.
    //
    SingleThreadedTransExecutor executor = new SingleThreadedTransExecutor(trans);

    // Initialize all steps
    //
    executor.init();

    int iterations = 1000000;
    long totalWait = 0;
    List<RowMetaAndData> inputList = createReducerData();

    for (int i = 0; i < iterations; i++) {
      // add rows
      for (RowMetaAndData rm : inputList) {
        Object[] copy = rm.getRowMeta().cloneRow(rm.getData());
        rp.putRow(rm.getRowMeta(), copy);
      }

      long start = System.currentTimeMillis();

      boolean cont = executor.oneIteration();
      if (!cont) {
        fail("We don't expect any step or the transformation to be done before the end of all iterations.");
      }

      long end = System.currentTimeMillis();
      long delay = end - start;
      totalWait += delay;
      if (i > 0 && (i % 100000) == 0) {
        long rowsProcessed = trans.findRunThread(metricsStep).getLinesRead();
        double speed = Const.round(((double)rowsProcessed) / ((double) (end - transStart) / 1000), 1);
        int totalRows = 0;
        for (StepMetaDataCombi combi : trans.getSteps()) {
          for (RowSet rowSet : combi.step.getInputRowSets())
            totalRows += rowSet.size();
          for (RowSet rowSet : combi.step.getOutputRowSets())
            totalRows += rowSet.size();
        }
        System.out.println("#" + i + " : Finished processing one iteration in " + delay + "ms, average is: " + Const.round(((double) totalWait / (i + 1)), 1) + ", speed=" + speed + " row/s, total rows buffered: " + totalRows);
      }

      List<RowMetaAndData> resultRows = rc.getRowsWritten();

      // The "group by" step reduces the amount of rows from 6 to 4. 
      //
      assertEquals("Error found in iteration "+i+" : not the expected amount of output rows.", 4, resultRows.size());
      rc.clear();
    }

    rp.finished();

    // Dispose all steps.
    //
    executor.dispose();
    
    long rowsProcessed = trans.findRunThread(metricsStep).getLinesRead();

    long transEnd = System.currentTimeMillis();
    long transTime = transEnd - transStart;
    System.out.println("Average delay before idle : " + Const.round(((double) totalWait / iterations), 1));
    double transTimeSeconds = Const.round(((double) transTime / 1000), 1);
    System.out.println("Total transformation runtime for " + iterations + " iterations :" + transTimeSeconds + " seconds");
    double transTimePerIteration = Const.round(((double) transTime / iterations), 2);
    System.out.println("Runtime per iteration: " + transTimePerIteration + " miliseconds");
    double rowsPerSecond = Const.round(((double) rowsProcessed) / ((double) transTime / 1000), 1);
    System.out.println("Average speed: " + rowsPerSecond + " rows/second");
  }


  public RowMetaInterface createMapperRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface valuesMeta[] = { 
        new ValueMeta("key", ValueMeta.TYPE_STRING), 
        new ValueMeta("value", ValueMeta.TYPE_STRING), 
      };

    for (int i = 0; i < valuesMeta.length; i++) {
      rm.addValueMeta(valuesMeta[i]);
    }

    return rm;
  }

  public List<RowMetaAndData> createMapperData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createMapperRowMetaInterface();

    Object[] r1 = new Object[] { "12345", "The quick brown fox jumped over the lazy dog",  };
    list.add(new RowMetaAndData(rm, r1));

    return list;
  }

  
  public RowMetaInterface createReducerRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface valuesMeta[] = { 
        new ValueMeta("key", ValueMeta.TYPE_STRING), 
        new ValueMeta("value", ValueMeta.TYPE_INTEGER), 
      };

    for (int i = 0; i < valuesMeta.length; i++) {
      rm.addValueMeta(valuesMeta[i]);
    }

    return rm;
  }

  public List<RowMetaAndData> createReducerData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createReducerRowMetaInterface();

    Object[] r1 = new Object[] { "A", Long.valueOf(100),  };
    list.add(new RowMetaAndData(rm, r1));
    
    r1 = new Object[] { "A", Long.valueOf(200),  };
    list.add(new RowMetaAndData(rm, r1));

    r1 = new Object[] { "B", Long.valueOf(300),  };
    list.add(new RowMetaAndData(rm, r1));

    r1 = new Object[] { "C", Long.valueOf(400),  };
    list.add(new RowMetaAndData(rm, r1));

    r1 = new Object[] { "C", Long.valueOf(500),  };
    list.add(new RowMetaAndData(rm, r1));

    r1 = new Object[] { "D", Long.valueOf(600),  };
    list.add(new RowMetaAndData(rm, r1));

    return list;
  }

}
