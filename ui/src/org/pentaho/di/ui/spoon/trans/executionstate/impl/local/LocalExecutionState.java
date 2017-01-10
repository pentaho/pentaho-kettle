package org.pentaho.di.ui.spoon.trans.executionstate.impl.local;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionState.StepState.StepStateField.*;

/**
 * Represents trans execution state for a trans running locally to the process, allowing metrics to be retrieved
 * directly from the executing trans.
 */
public class LocalExecutionState implements ExecutionState {
  private final List<StepInterface> steps;

  public LocalExecutionState( final List<StepInterface> steps ) {
    this.steps = steps;
  }

  @Override public List<StepState> getStepStates() {
    return steps.stream()
      .map( LocalStepState::new )
      .collect( Collectors.toList() );
  }

  /**
   * Adapts org.pentaho.di.trans.step.StepStatus to a generic interface not tied to a Trans or particulars of UI
   * layout.
   */
  class LocalStepState implements StepState {

    final Map<StepStateField, String> fieldRetrievalMap;
    private final boolean running;
    private final BaseStepData.StepExecutionStatus status;

    LocalStepState( StepInterface step ) {
      this.running = step.isRunning();
      this.status = step.getStatus();

      StepStatus stepStatus = new StepStatus( step );

      fieldRetrievalMap = new ImmutableMap.Builder<StepStateField, String>()
        .put( NAME, stepStatus.getStepname() )
        .put( COPY, Integer.toString( stepStatus.getCopy() ) )
        .put( READ, Long.toString( stepStatus.getLinesRead() ) )
        .put( WRITTEN, Long.toString( stepStatus.getLinesWritten() ) )
        .put( INPUT, Long.toString( stepStatus.getLinesInput() ) )
        .put( OUTPUT, Long.toString( stepStatus.getLinesOutput() ) )
        .put( UPDATED, Long.toString( stepStatus.getLinesUpdated() ) )
        .put( REJECTED, Long.toString( stepStatus.getLinesRejected() ) )
        .put( ERRORS, Long.toString( stepStatus.getErrors() ) )
        .put( DESC, stepStatus.getStatusDescription() )
        .put( SECONDS, Double.toString( stepStatus.getSeconds() ) )
        .put( SPEED, stepStatus.getSpeed() )
        .put( PRIORITY, stepStatus.getPriority() )
        .build();
    }

    @Override public List<String> getStringFieldValues( StepStateField... fields ) {
      return Arrays.asList( fields ).stream()
        .map( fieldRetrievalMap::get )
        .collect( Collectors.toList() );
    }

    @Override public BaseStepData.StepExecutionStatus getExecStatus() {
      return status;
    }
  }
}
