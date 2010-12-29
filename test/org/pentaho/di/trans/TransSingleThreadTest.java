package org.pentaho.di.trans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

public class TransSingleThreadTest extends TestCase {

  public static void main(String[] args) throws Exception {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta("testfiles/SingleThreadedTest - Stream Lookup.ktr");
    transMeta.setTransformationType(TransformationType.SingleThreaded);
    
    long transStart = System.currentTimeMillis();

    // Now execute the transformation...
    Trans trans = new Trans(transMeta);
    trans.setLogLevel(LogLevel.MINIMAL);

    trans.prepareExecution(null);

    StepInterface si = trans.getStepInterface("OUTPUT", 0);
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener(rc);

    RowProducer rp = trans.addRowProducer("INPUT", 0);
    trans.startThreads();

    // The single threaded transformation type expects us to run the steps
    // ourselves.
    //
    SingleThreadedTransExecutor executor = new SingleThreadedTransExecutor(trans);

    // Initialize all steps
    //
    executor.init();

    int iterations = 1000000;
    long totalWait = 0;
    List<RowMetaAndData> inputList = createData();

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
      if (i > 0 && (i % 5000) == 0) {
        double speed = Const.round(((double) i*inputList.size()) / ((double) (end - transStart) / 1000), 1);
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
      //
      assertEquals(inputList.size() - 1, resultRows.size());
      rc.clear();
    }

    rp.finished();

    // Dispose all steps.
    //
    executor.dispose();

    long transEnd = System.currentTimeMillis();
    long transTime = transEnd - transStart;
    System.out.println("Average delay before idle : " + Const.round(((double) totalWait / iterations), 1));
    double transTimeSeconds = Const.round(((double) transTime / 1000), 1);
    System.out.println("Total transformation runtime for " + iterations + " iterations :" + transTimeSeconds + " seconds");
    double transTimePerIteration = Const.round(((double) transTime / iterations), 2);
    System.out.println("Runtime per iteration: " + transTimePerIteration + " miliseconds");
    double rowsPerSecond = Const.round(((double) iterations * inputList.size()) / ((double) transTime / 1000), 1);
    System.out.println("Average speed: " + rowsPerSecond + " rows/second");
  }

  public static RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface valuesMeta[] = { 
        new ValueMeta("field1", ValueMeta.TYPE_STRING), 
        new ValueMeta("field2", ValueMeta.TYPE_INTEGER), 
        new ValueMeta("field3", ValueMeta.TYPE_NUMBER), 
        new ValueMeta("field4", ValueMeta.TYPE_DATE),
        new ValueMeta("field5", ValueMeta.TYPE_BOOLEAN), 
        new ValueMeta("field6", ValueMeta.TYPE_BIGNUMBER), 
        new ValueMeta("field7", ValueMeta.TYPE_BIGNUMBER),
      };

    for (int i = 0; i < valuesMeta.length; i++) {
      rm.addValueMeta(valuesMeta[i]);
    }

    return rm;
  }

  public static List<RowMetaAndData> createData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "KETTLE1", new Long(123L), new Double(10.5D), new Date(), Boolean.TRUE, BigDecimal.valueOf(123.45), BigDecimal.valueOf(123.60) };
    Object[] r2 = new Object[] { "KETTLE2", new Long(500L), new Double(20.0D), new Date(), Boolean.FALSE, BigDecimal.valueOf(123.45), BigDecimal.valueOf(123.60) };
    Object[] r3 = new Object[] { "KETTLE3", new Long(501L), new Double(21.0D), new Date(), Boolean.FALSE, BigDecimal.valueOf(123.45), BigDecimal.valueOf(123.70) };

    list.add(new RowMetaAndData(rm, r1));
    list.add(new RowMetaAndData(rm, r2));
    list.add(new RowMetaAndData(rm, r3));

    return list;
  }
}
