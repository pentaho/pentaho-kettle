package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class SingleThreadedTransExecutor {

  private Trans trans;
  private List<StepMetaDataCombi> steps;
  private boolean[] done;
  private int nrDone;
  private List<List<StreamInterface>> stepInfoStreams;
  private List<List<RowSet>> stepInfoRowSets;
  
  public SingleThreadedTransExecutor(final Trans trans) {
    this.trans = trans;

    steps = trans.getSteps();

    // Always disable thread priority management, it will always slow us down...
    //
    for (StepMetaDataCombi combi : steps) {
        combi.step.setUsingThreadPriorityManagment(false);
    }

    sortSteps();
    
    done = new boolean[steps.size()];
    nrDone = 0;

    stepInfoStreams = new ArrayList<List<StreamInterface>>();
    stepInfoRowSets = new ArrayList<List<RowSet>>();
    for (StepMetaDataCombi combi : steps) {
      List<StreamInterface> infoStreams = combi.stepMeta.getStepMetaInterface().getStepIOMeta().getInfoStreams();
      stepInfoStreams.add( infoStreams );
      List<RowSet> infoRowSets = new ArrayList<RowSet>();
      for (StreamInterface infoStream : infoStreams) {
        RowSet infoRowSet = trans.findRowSet(infoStream.getStepname(), 0, combi.stepname, 0);
        if (infoRowSet!=null) {
          infoRowSets.add(infoRowSet);
        }
      }
      stepInfoRowSets.add(infoRowSets);
    }

  }
  
  private void sortSteps() {
    // Sort the steps from start to finish...
    //
    /*
    Collections.sort(steps, new Comparator<StepMetaDataCombi>() {
      public int compare(StepMetaDataCombi c1, StepMetaDataCombi c2) {

        boolean c1BeforeC2 = trans.getTransMeta().findPrevious(c2.stepMeta, c1.stepMeta);
        boolean c2BeforeC1 = trans.getTransMeta().findPrevious(c1.stepMeta, c2.stepMeta);

        if (!(c1BeforeC2 ^ c2BeforeC1)) {
          // In this situation there is no path from c1 to c2
          // This causes Collections.sort() great trouble.
          //
          return false; 
        }
        
        if (c1BeforeC2) {
          return -1;
        } else {
          return 1;
        }
      }
    });
    */
    
    // The bubble sort algorithm in contrast to the QuickSort or MergeSort algorithms 
    // does indeed cover all possibilities.
    // Sorting larger transformations with hundreds of steps might be too slow though.
    // We should consider caching TransMeta.findPrevious() results in that case.
    //
    for (int x=0;x<steps.size();x++) {
      for (int y=0;y<steps.size()-1;y++) {
        StepMetaDataCombi one = steps.get(y);
        StepMetaDataCombi two = steps.get(y+1);
        boolean before = trans.getTransMeta().findPrevious(one.stepMeta, two.stepMeta);
        if (before) {
          // two was found to be positioned BEFORE one so we need to switch them...
          //
          steps.set(y, two);
          steps.set(y+1, one);
        }
      }
    }
    
    //
    System.out.println("-------------------------------------------------------");
    System.out.println("Steps after sort: ");
    for (StepMetaDataCombi combi : steps) {
      System.out.println(combi.step.toString());
    }
    System.out.println("-------------------------------------------------------");
    //
  }

  public boolean init() throws KettleException {
    
    // See if the steps support the SingleThreaded transformation type...
    //
    for (StepMetaDataCombi combi : steps) {
      TransformationType[] types = combi.stepMeta.getStepMetaInterface().getSupportedTransformationTypes();
      boolean ok = false;
      for (TransformationType type : types) {
        if (type == TransformationType.SingleThreaded) ok = true; 
      }
      if (!ok) {
        throw new KettleException("Step '"+combi.stepname+"' of type '"+combi.stepMeta.getStepID()+"' is not yet supported in a Single Threaded transformation engine.");
      }
    }    
    // Initialize all the steps...
    //
    for (StepMetaDataCombi combi : steps) {
        boolean ok = combi.step.init(combi.meta, combi.data);
        if (!ok) {
          return false;
        }
    }
    return true;

  }
  
  /**
   * Give all steps in the transformation the chance to process all rows on input...
   * 
   * @return true if more iterations can be performed.  False if this is not the case.
   */
  public boolean oneIteration() throws KettleException {
    
    for (int s=0;s<steps.size() && !trans.isStopped();s++) {
      if (!done[s]) {
        
        StepMetaDataCombi combi = steps.get(s);

        // If this step is waiting for data (text, db, and so on), we simply read all the data
        // This means that it is impractical to use this transformation type to load large files.
        //
        boolean stepDone = false;
        // For every input row we call the processRow() method of the step.
        //
        List<RowSet> infoRowSets = stepInfoRowSets.get(s);
        
        // Loop over info-rowsets FIRST to make sure we support the "Stream Lookup" step and so on.
        //
        for (RowSet rowSet : infoRowSets) {
          boolean once = true;
          while(once || (rowSet.size()>0 && !stepDone)) {
            once=false;
            stepDone = !combi.step.processRow(combi.meta, combi.data);
          }
        }        

        // Do normal processing of input rows...
        //
        List<RowSet> rowSets = combi.step.getInputRowSets();
        
        // If there are no input row sets, we read all rows until finish.
        // This applies to steps like "Table Input", "Text File Input" and so on.
        // If they do have an input row set, to get filenames or other parameters, 
        // we need to handle this in the batchComplete() methods.
        //
        if (rowSets.size()==0) {
          while (!stepDone && !trans.isStopped()) {
            stepDone = !combi.step.processRow(combi.meta, combi.data);
          }
        } else {
          // Since we can't be sure that the step actually reads from the row sets where we measure rows, 
          // we simply count the total nr of rows on input.  The steps will find the rows in either row set.
          // 
          int nrRows=0;
          for (RowSet rowSet : rowSets) {
            nrRows+=rowSet.size();
          }
          
          // Now do the number of processRows() calls.
          //
          for (int i=0;i<nrRows;i++) {
              stepDone = !combi.step.processRow(combi.meta, combi.data);
          }
        }
        
        // Signal the step that a batch of rows has passed for this iteration (sort rows and all)
        //
        combi.step.batchComplete();
        
        // System.out.println(combi.step.toString()+" : input="+getTotalRows(combi.step.getInputRowSets())+", output="+getTotalRows(combi.step.getOutputRowSets()));

        if (stepDone) {
          nrDone++;
        }
        
        done[s] = stepDone;
      }
    }
    
    return nrDone<steps.size() && !trans.isStopped();
  }
  
  protected int getTotalRows(List<RowSet> rowSets) {
    int total=0;
    for (RowSet rowSet : rowSets) total+=rowSet.size();
    return total;
  }

  public void dispose() throws KettleException {
    // Finalize all the steps...
    //
    for (StepMetaDataCombi combi : steps) {
        combi.step.dispose(combi.meta, combi.data);
    }
  }
}
