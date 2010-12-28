package org.pentaho.di.trans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

public class TransIdleTest extends TestCase {
  
  /**
   * Test case for injector step... also a show case on how
   * to use injector.
   */
  public void testListeners() throws Exception
  {
      KettleEnvironment.init();

      //
      // Create a new transformation...
      //
      TransMeta transMeta = new TransMeta();
      transMeta.setName("injectortest");
      transMeta.setTransformationType(TransformationType.Monitored);
      
      PluginRegistry registry = PluginRegistry.getInstance();            

      // 
      // create an injector step...
      //
      String injectorStepname = "injector step";
      InjectorMeta im = new InjectorMeta();
      
      // Set the information of the injector.
      //
      String injectorPid = registry.getPluginId(StepPluginType.class, im);
      StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
      injectorStep.setLocation(50,50);
      injectorStep.setDraw(true);
      transMeta.addStep(injectorStep);

      // 
      // Add a few dummy steps
      //
      StepMeta previous = injectorStep;
      String dummyStepname=null;
      
      int nrDummySteps = 1;
      for (int s=0;s<nrDummySteps;s++) {
        dummyStepname = "dummy step "+(s+1);            
        DummyTransMeta dm = new DummyTransMeta();
  
        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        dummyStep.setLocation(100+50*s, 50);
        dummyStep.setDraw(true);
        transMeta.addStep(dummyStep);                              
  
        TransHopMeta hi = new TransHopMeta(previous, dummyStep);
        transMeta.addTransHop(hi);
        
        previous=dummyStep;
      }
      
      transMeta.writeXML("/tmp/idleTest.ktr");
      
      long transStart = System.currentTimeMillis();
              
      // Now execute the transformation...
      Trans trans = new Trans(transMeta);
      trans.setLogLevel(LogLevel.MINIMAL);

      trans.prepareExecution(null);
              
      StepInterface si = trans.getStepInterface(dummyStepname, 0);
      RowStepCollector rc = new RowStepCollector();
      si.addRowListener(rc);
      
      RowProducer rp = trans.addRowProducer(injectorStepname, 0);
      trans.startThreads();
      
      int iterations=10000;
      long totalWait=0;

      for (int i=0;i<iterations;i++) {
        // add rows
        List<RowMetaAndData> inputList = createData();
        for (RowMetaAndData rm : inputList ) {
          rp.putRow(rm.getRowMeta(), rm.getData());
        }
        
        // Wait for the rows to be processed and the xform to become idle.
        //
        long start = System.currentTimeMillis();
        while (!trans.isIdle()) {
          /*
          for (StepMetaDataCombi combi : trans.getSteps()) {
            int inputSize=0;
            int blocking=0;
            for (RowSet rowSet : combi.step.getInputRowSets()) {
              inputSize+=rowSet.size();
              blocking+=rowSet.isBlocking()?1:0;
            }
            System.out.println(combi.stepname+" : idle="+combi.step.isIdle()+", input="+inputSize+", blocking rowsets="+blocking);
          }
          System.out.println("-----------------------");
          */
          Thread.sleep(0,1);
        }
        long end = System.currentTimeMillis();
        long delay = end-start;
        totalWait+=delay;
        System.out.println("#"+i+" : detected idle transformation in "+delay+"ms, average is: "+Const.round(((double)totalWait/(i+1)), 1));
        
        List<RowMetaAndData> resultRows = rc.getRowsWritten();
        assertEquals(inputList.size(), resultRows.size());
        resultRows.clear();
      }
      
      rp.finished();
      
      trans.waitUntilFinished();   
      long transEnd =System.currentTimeMillis();
      long transTime = transEnd-transStart;
      System.out.println("Average delay before idle : "+Const.round(((double)totalWait/iterations), 1));
      double transTimeSeconds = Const.round(((double)transTime/1000), 1);
      System.out.println("Total transformation runtime for "+iterations+" iterations :"+transTimeSeconds+" seconds");
      double transTimePerIteration = Const.round(((double)transTime/iterations), 2);
      System.out.println("Runtime per iteration: "+transTimePerIteration+" miliseconds");
      double rowsPerSecond = (double)iterations*createData().size() / ((double)transTime/1000);
      System.out.println("Average speed: "+rowsPerSecond+" rows/second");
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  public RowMetaInterface createRowMetaInterface()
  {
      RowMetaInterface rm = new RowMeta();
      
      ValueMetaInterface valuesMeta[] = {
              new ValueMeta("field1", ValueMeta.TYPE_STRING),
              new ValueMeta("field2", ValueMeta.TYPE_INTEGER),
              new ValueMeta("field3", ValueMeta.TYPE_NUMBER),
              new ValueMeta("field4", ValueMeta.TYPE_DATE),
              new ValueMeta("field5", ValueMeta.TYPE_BOOLEAN),
              new ValueMeta("field6", ValueMeta.TYPE_BIGNUMBER),
              new ValueMeta("field7", ValueMeta.TYPE_BIGNUMBER) 
      };

      for (int i=0; i < valuesMeta.length; i++ )
      {
          rm.addValueMeta(valuesMeta[i]);
      }
      
      return rm;
  }
  
  public List<RowMetaAndData> createData()
  {
      List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
      
      RowMetaInterface rm = createRowMetaInterface();
      
      Object[] r1 = new Object[] { "KETTLE1", new Long(123L), 
                                   new Double(10.5D), new Date(),
                                   Boolean.TRUE, BigDecimal.valueOf(123.45),
                                   BigDecimal.valueOf(123.60) };
      Object[] r2 = new Object[] { "KETTLE2", new Long(500L), 
                                   new Double(20.0D), new Date(),
                                   Boolean.FALSE, BigDecimal.valueOf(123.45),
                                   BigDecimal.valueOf(123.60) };
      Object[] r3 = new Object[] { "KETTLE3", new Long(501L), 
                                   new Double(21.0D), new Date(),
                                   Boolean.FALSE, BigDecimal.valueOf(123.45),
                                   BigDecimal.valueOf(123.70) };
      
      list.add(new RowMetaAndData(rm, r1));
      list.add(new RowMetaAndData(rm, r2));
      list.add(new RowMetaAndData(rm, r3));
      
      return list;
  }

  /**
   *  Check the 2 lists comparing the rows in order.
   *  If they are not the same fail the test. 
   */
  public void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2)
  {
      int idx = 1;
      if ( rows1.size() != rows2.size() )
      {
          fail("Number of rows is not the same: " + 
               rows1.size() + " and " + rows2.size());
      }
      Iterator<RowMetaAndData> it1 = rows1.iterator();
      Iterator<RowMetaAndData> it2 = rows2.iterator();
      
      while ( it1.hasNext() && it2.hasNext() )
      {
          RowMetaAndData rm1 = (RowMetaAndData)it1.next();
          RowMetaAndData rm2 = (RowMetaAndData)it2.next();
          
          Object[] r1 = rm1.getData();
          Object[] r2 = rm2.getData();
          
          if ( r1.length != r2.length )
          {
              fail("row nr " + idx + "is not equal");
          }
          int fields[] = new int[r1.length];
          for ( int ydx = 0; ydx < r1.length; ydx++ )
          {
              fields[ydx] = ydx;
          }
          try {
              if ( rm1.getRowMeta().compare(r1, r2, fields) != 0 )
              {
                  fail("row nr " + idx + "is not equal");
              }
          } catch (KettleValueException e) {
              fail("row nr " + idx + "is not equal");
          }
              
          idx++;
      }
  }
  

}
