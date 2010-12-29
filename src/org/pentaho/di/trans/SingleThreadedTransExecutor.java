package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepIOMetaInterface;
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

    // Sort the steps from start to finish...
    //
    Collections.sort(steps, new Comparator<StepMetaDataCombi>() {
        public int compare(StepMetaDataCombi c1, StepMetaDataCombi c2) {
            
            boolean c1BeforeC2 = trans.getTransMeta().findPrevious(c2.stepMeta, c1.stepMeta);
            if (c1BeforeC2) {
                return -1;
            } else {
                return 1;
            }
        }
    });
    
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
  
  public boolean init() throws KettleException {
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
        if (combi.step.isWaitingForData()) {
          
          while (!trans.isStopped() && combi.step.processRow(combi.meta, combi.data));
          stepDone = true;
          
        } else {
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
  
          // Now go back to the regular program.
          //
          List<RowSet> rowSets = combi.step.getInputRowSets();
          for (RowSet rowSet : rowSets) {
            int nrRows = rowSet.size();
            for (int i=0;i<nrRows;i++) {
              stepDone = !combi.step.processRow(combi.meta, combi.data);
            }
          }
        }
        if (stepDone) {
          nrDone++;
        }
        
        done[s] = stepDone;
      }
    }
    
    return nrDone<steps.size() && !trans.isStopped();
  }
  
  public void dispose() throws KettleException {
    // Finalize all the steps...
    //
    for (StepMetaDataCombi combi : steps) {
        combi.step.dispose(combi.meta, combi.data);
    }
  }
}
