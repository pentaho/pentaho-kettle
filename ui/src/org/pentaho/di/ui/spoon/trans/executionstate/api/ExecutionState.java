package org.pentaho.di.ui.spoon.trans.executionstate.api;

import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;

import java.util.List;

/**
 * Container for trans execution info.  Currently only holds
 * step metrics and StepExecutionStatus.
 */
public interface ExecutionState {

  List<StepState> getStepStates();

  interface StepState {
    enum StepStateField {
      NAME, COPY, READ, WRITTEN, INPUT, OUTPUT, UPDATED,
      REJECTED, ERRORS, DESC, SECONDS, SPEED, PRIORITY
    }

    List<String> getStringFieldValues( StepStateField... fields );

    StepExecutionStatus getExecStatus();
  }
}
