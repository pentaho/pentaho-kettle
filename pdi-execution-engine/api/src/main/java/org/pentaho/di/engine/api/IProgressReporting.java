package org.pentaho.di.engine.api;

/**
 * Implementations report on their status along with metrics associated with execution progress.
 * <p>
 * Created by nbaker on 5/30/16.
 */
public interface IProgressReporting<T extends IPDIEvent> extends IPDIEventSource<T> {
  enum Status {
    STOPPED,
    RUNNING,
    PAUSED,
    FINISHED
  }

  /**
   * Get number of {@link IPDIEvent}s into this component
   *
   * @return
   */
  long getIn();

  /**
   * Get number of {@link IPDIEvent}s out from this component
   *
   * @return
   */
  long getOut();

  /**
   * Get number of {@link IPDIEvent}s dropped (errorred)
   *
   * @return
   */
  long getDropped();

  /**
   * Get number of {@link IPDIEvent}s currently in-flight
   *
   * @return
   */
  int getInFlight();

  /**
   * Return the current status of this component
   *
   * @return
   */
  Status getStatus();

}
