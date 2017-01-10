package org.pentaho.di.ui.spoon.trans.executionstate.impl.local;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionStateEvent;
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionStatePublisher;
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionStateSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalExecutionStatePublisher implements ExecutionStatePublisher {
  private final Trans trans;

  private static final int POLL_FREQUENCY = 250;

  List<ExecutionStateSubscriber> subscribers = new ArrayList<>();
  private final Timer timer;

  public LocalExecutionStatePublisher( Spoon spoon, Trans trans ) {
    this.trans = trans;
    timer = new Timer( trans.getName() );
    startPolling( spoon );
  }

  private void startPolling( final Spoon spoon ) {
    TimerTask task = new TimerTask() {
      public void run() {
        if ( !spoon.getDisplay().isDisposed() ) {
          spoon.getDisplay().asyncExec( () -> sendUpdate() );
        }
      }
    };
    timer.schedule( task, 0, POLL_FREQUENCY );
  }

  private void sendUpdate() {
    List<StepInterface> steps = IntStream.range( 0, trans.nrSteps() )
      .mapToObj( trans::getRunThread )
      .collect( Collectors.toList() );
    maybeCancelTimer( steps );

    subscribers.stream()
      .forEach( sub -> sub.execStateChanged(
        new ExecutionStateEvent( new LocalExecutionState( steps ) )
      ) );
  }

  private void maybeCancelTimer( List<StepInterface> steps ) {
    Optional<StepInterface> runningSteps = steps.stream()
      .filter( StepInterface::isRunning )
      .findFirst();
    if ( steps.size() > 0 && !runningSteps.isPresent() ) {
      // nothing left running, cancel the timer
      timer.cancel();
    }
  }

  @Override public synchronized void subscribe( ExecutionStateSubscriber subscriber ) {
    subscribers.add( subscriber );
  }
}
