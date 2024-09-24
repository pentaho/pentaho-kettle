/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class SingleThreadedTransExecutor {

  private Trans trans;
  private List<StepMetaDataCombi> steps;
  private boolean[] done;
  private int nrDone;
  private List<List<StreamInterface>> stepInfoStreams;
  private List<List<RowSet>> stepInfoRowSets;
  private LogChannelInterface log;

  public SingleThreadedTransExecutor( final Trans trans ) {
    this.trans = trans;
    this.log = trans.getLogChannel();

    steps = trans.getSteps();

    // Always disable thread priority management, it will always slow us down...
    //
    for ( StepMetaDataCombi combi : steps ) {
      combi.step.setUsingThreadPriorityManagment( false );
    }

    sortSteps();

    done = new boolean[steps.size()];
    nrDone = 0;

    stepInfoStreams = new ArrayList<List<StreamInterface>>();
    stepInfoRowSets = new ArrayList<List<RowSet>>();
    for ( StepMetaDataCombi combi : steps ) {
      List<StreamInterface> infoStreams = combi.stepMeta.getStepMetaInterface().getStepIOMeta().getInfoStreams();
      stepInfoStreams.add( infoStreams );
      List<RowSet> infoRowSets = new ArrayList<RowSet>();
      for ( StreamInterface infoStream : infoStreams ) {
        RowSet infoRowSet = trans.findRowSet( infoStream.getStepname(), 0, combi.stepname, 0 );
        if ( infoRowSet != null ) {
          infoRowSets.add( infoRowSet );
        }
      }
      stepInfoRowSets.add( infoRowSets );
    }

  }

  /**
   * Sort the steps from start to finish...
   */
  private void sortSteps() {

    // The bubble sort algorithm in contrast to the QuickSort or MergeSort
    // algorithms
    // does indeed cover all possibilities.
    // Sorting larger transformations with hundreds of steps might be too slow
    // though.
    // We should consider caching TransMeta.findPrevious() results in that case.
    //
    trans.getTransMeta().clearCaches();

    //
    // Cocktail sort (bi-directional bubble sort)
    //
    // Original sort was taking 3ms for 30 steps
    // cocktail sort takes about 8ms for the same 30, but it works :)

    // set these to true if you are working on this algorithm and don't like
    // flying blind.
    //
    boolean testing = true; // log sort details

    int stepsMinSize = 0;
    int stepsSize = steps.size();

    // Noticed a problem with an immediate shrinking iteration window
    // trapping rows that need to be sorted.
    // This threshold buys us some time to get the sorting close before
    // starting to decrease the window size.
    //
    // TODO: this could become much smarter by tracking row movement
    // and reacting to that each outer iteration verses
    // using a threshold.
    //
    // After this many iterations enable trimming inner iteration
    // window on no change being detected.
    //
    int windowShrinkThreshold = (int) Math.round( stepsSize * 0.75 );

    // give ourselves some room to sort big lists. the window threshold should
    // stop us before reaching this anyway.
    //
    int totalIterations = stepsSize * 2;
    int actualIterations = 0;

    boolean isBefore = false;
    boolean forwardChange = false;
    boolean backwardChange = false;

    boolean lastForwardChange = true;
    boolean keepSortingForward = true;

    StepMetaDataCombi one = null;
    StepMetaDataCombi two = null;

    StringBuilder tLogString = new StringBuilder(); // this helps group our
                                                    // output so other threads
                                                    // don't get logs in our
                                                    // output.
    tLogString.append( "-------------------------------------------------------" ).append( "\n" );
    tLogString.append( "--SingleThreadedTransExecutor.sortSteps(cocktail)" ).append( "\n" );
    tLogString.append( "--Trans: " ).append( trans.getName() ).append( "\n" );
    tLogString.append( "-" ).append( "\n" );

    long startTime = System.currentTimeMillis();

    for ( int x = 0; x < totalIterations; x++ ) {

      // Go forward through the list
      //
      if ( keepSortingForward ) {
        for ( int y = stepsMinSize; y < stepsSize - 1; y++ ) {
          one = steps.get( y );
          two = steps.get( y + 1 );
          isBefore = trans.getTransMeta().findPrevious( one.stepMeta, two.stepMeta );
          if ( isBefore ) {
            // two was found to be positioned BEFORE one so we need to
            // switch them...
            //
            steps.set( y, two );
            steps.set( y + 1, one );
            forwardChange = true;

          }
        }
      }

      // Go backward through the list
      //
      for ( int z = stepsSize - 1; z > stepsMinSize; z-- ) {
        one = steps.get( z );
        two = steps.get( z - 1 );

        isBefore = trans.getTransMeta().findPrevious( one.stepMeta, two.stepMeta );
        if ( !isBefore ) {
          // two was found NOT to be positioned BEFORE one so we need to
          // switch them...
          //
          steps.set( z, two );
          steps.set( z - 1, one );
          backwardChange = true;
        }
      }

      // Shrink stepsSize(max) if there was no forward change
      //
      if ( x > windowShrinkThreshold && !forwardChange ) {

        // should we keep going? check the window size
        //
        stepsSize--;
        if ( stepsSize <= stepsMinSize ) {
          if ( testing ) {
            tLogString.append( String.format( "stepsMinSize:%s  stepsSize:%s", stepsMinSize, stepsSize ) );
            tLogString
              .append( "stepsSize is <= stepsMinSize.. exiting outer sort loop. index:" + x ).append( "\n" );
          }
          break;
        }
      }

      // shrink stepsMinSize(min) if there was no backward change
      //
      if ( x > windowShrinkThreshold && !backwardChange ) {

        // should we keep going? check the window size
        //
        stepsMinSize++;
        if ( stepsMinSize >= stepsSize ) {
          if ( testing ) {
            tLogString.append( String.format( "stepsMinSize:%s  stepsSize:%s", stepsMinSize, stepsSize ) ).append(
              "\n" );
            tLogString
              .append( "stepsMinSize is >= stepsSize.. exiting outer sort loop. index:" + x ).append( "\n" );
          }
          break;
        }
      }

      // End of both forward and backward traversal.
      // Time to see if we should keep going.
      //
      actualIterations++;

      if ( !forwardChange && !backwardChange ) {
        if ( testing ) {
          tLogString.append( String.format( "existing outer loop because no "
            + "change was detected going forward or backward. index:%s  min:%s  max:%s",
            x, stepsMinSize, stepsSize ) ).append( "\n" );
        }
        break;
      }

      //
      // if we are past the first iteration and there has been no change twice,
      // quit doing it!
      //
      if ( keepSortingForward && x > 0 && !lastForwardChange && !forwardChange ) {
        keepSortingForward = false;
      }
      lastForwardChange = forwardChange;
      forwardChange = false;
      backwardChange = false;

    } // finished sorting

    long endTime = System.currentTimeMillis();
    long totalTime = ( endTime - startTime );

    tLogString.append( "-------------------------------------------------------" ).append( "\n" );
    tLogString.append( "Steps sort time: " + totalTime + "ms" ).append( "\n" );
    tLogString.append( "Total iterations: " + actualIterations ).append( "\n" );
    tLogString.append( "Step count: " + steps.size() ).append( "\n" );
    tLogString.append( "Steps after sort: " ).append( "\n" );
    for ( StepMetaDataCombi combi : steps ) {
      tLogString.append( combi.step.getStepname() ).append( "\n" );
    }
    tLogString.append( "-------------------------------------------------------" ).append( "\n" );

    if ( log.isDetailed() ) {
      log.logDetailed( tLogString.toString() );
    }
  }

  public boolean init() throws KettleException {

    // See if the steps support the SingleThreaded transformation type...
    //
    log.logBasic( "Single Threaded Executor initializing Trans: [" + trans.getName( ) + "]" );
    for ( StepMetaDataCombi combi : steps ) {
      TransformationType[] types = combi.stepMeta.getStepMetaInterface().getSupportedTransformationTypes();
      boolean ok = false;
      for ( TransformationType type : types ) {
        if ( type == TransformationType.SingleThreaded ) {
          ok = true;
        }
      }
      if ( !ok ) {
        throw new KettleException( "Step '"
          + combi.stepname + "' of type '" + combi.stepMeta.getStepID()
          + "' is not yet supported in a Single Threaded transformation engine." );
      }
    }
    // Initialize all the steps...
    //
    for ( StepMetaDataCombi combi : steps ) {
      boolean ok = combi.step.init( combi.meta, combi.data );
      if ( !ok ) {
        return false;
      }
    }
    return true;

  }

  /**
   * Give all steps in the transformation the chance to process all rows on input...
   *
   * @return true if more iterations can be performed. False if this is not the case.
   */
  public boolean oneIteration() throws KettleException {

    for ( int s = 0; s < steps.size() && !trans.isStopped(); s++ ) {
      if ( !done[s] ) {

        StepMetaDataCombi combi = steps.get( s );

        // If this step is waiting for data (text, db, and so on), we simply read all the data
        // This means that it is impractical to use this transformation type to load large files.
        //
        boolean stepDone = false;
        // For every input row we call the processRow() method of the step.
        //
        List<RowSet> infoRowSets = stepInfoRowSets.get( s );

        // Loop over info-rowsets FIRST to make sure we support the "Stream Lookup" step and so on.
        //
        for ( RowSet rowSet : infoRowSets ) {
          boolean once = true;
          while ( once || ( rowSet.size() > 0 && !stepDone ) ) {
            once = false;
            stepDone = !combi.step.processRow( combi.meta, combi.data );
            if ( combi.step.getErrors() > 0 ) {
              return false;
            }
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
        if ( rowSets.size() == 0 ) {
          while ( !stepDone && !trans.isStopped() ) {
            stepDone = !combi.step.processRow( combi.meta, combi.data );
            if ( combi.step.getErrors() > 0 ) {
              return false;
            }
          }
        } else {
          // Since we can't be sure that the step actually reads from the row sets where we measure rows,
          // we simply count the total nr of rows on input. The steps will find the rows in either row set.
          //
          int nrRows = 0;
          for ( RowSet rowSet : rowSets ) {
            nrRows += rowSet.size();
          }

          // Now do the number of processRows() calls.
          //
          for ( int i = 0; i < nrRows; i++ ) {
            stepDone = !combi.step.processRow( combi.meta, combi.data );
            if ( combi.step.getErrors() > 0 ) {
              return false;
            }
          }
        }

        // Signal the step that a batch of rows has passed for this iteration (sort rows and all)
        //
        combi.step.batchComplete();

        // System.out.println(combi.step.toString()+" : input="+getTotalRows(combi.step.getInputRowSets())+",
        // output="+getTotalRows(combi.step.getOutputRowSets()));

        if ( stepDone ) {
          nrDone++;
        }

        done[s] = stepDone;
      }
    }

    return nrDone < steps.size() && !trans.isStopped();
  }

  protected int getTotalRows( List<RowSet> rowSets ) {
    int total = 0;
    for ( RowSet rowSet : rowSets ) {
      total += rowSet.size();
    }
    return total;
  }

  public long getErrors() {
    return trans.getErrors();
  }

  public Result getResult() {
    return trans.getResult();
  }

  public boolean isStopped() {
    return trans.isStopped();
  }

  public boolean beforeStartProcessing( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    log.logBasic( "Single Threaded Executor Before Trans Start Processing: [" + trans.getName( ) + "]" );
    // Call beforeStartProcessing
    boolean result = false;
    for ( StepMetaDataCombi combi : trans.getSteps() ) {
      result = combi.step.beforeStartProcessing( combi.meta, combi.data ) || result;
    }

    return result;
  }

  public boolean afterFinishProcessing( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    log.logBasic( "Single Threaded Executor After Trans Finish Processing: [" + trans.getName( ) + "]" );
    // Call afterFinishProcessing
    boolean result = false;
    for ( StepMetaDataCombi combi : trans.getSteps() ) {
      result = combi.step.afterFinishProcessing( combi.meta, combi.data ) || result;
    }

    return result;
  }

  public void dispose() throws KettleException {

    log.logBasic( "Single Threaded Executor Disposing Trans: [" + trans.getName( ) + "]" );
    // Call output done.
    //
    for ( StepMetaDataCombi combi : trans.getSteps() ) {
      combi.step.setOutputDone();
    }

    // Finalize all the steps...
    //
    for ( StepMetaDataCombi combi : steps ) {
      combi.step.dispose( combi.meta, combi.data );
      combi.step.markStop();
    }

  }

  public Trans getTrans() {
    return trans;
  }

  /**
   * Clear the error in the transformation, clear all the rows from all the row sets...
   */
  public void clearError() {
    trans.clearError();
  }
}
