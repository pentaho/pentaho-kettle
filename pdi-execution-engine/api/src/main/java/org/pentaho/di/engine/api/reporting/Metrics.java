package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.IPDIEvent;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by hudak on 1/5/17.
 */
public class Metrics implements Wrapper, Serializable {
  private final long in, out, dropped, inFlight;

  public static Metrics empty() {
    return new Metrics( 0, 0, 0, 0 );
  }

  public Metrics( long in, long out, long dropped, long inFlight ) {
    this.in = in;
    this.out = out;
    this.dropped = dropped;
    this.inFlight = inFlight;
  }

  /**
   * Get number of {@link IPDIEvent}s into this component
   *
   * @return
   */
  public long getIn() {
    return in;
  }

  /**
   * Get number of {@link IPDIEvent}s out from this component
   *
   * @return
   */
  public long getOut() {
    return out;
  }

  /**
   * Get number of {@link IPDIEvent}s dropped (errorred)
   *
   * @return
   */
  public long getDropped() {
    return dropped;
  }

  /**
   * Get number of {@link IPDIEvent}s currently in-flight
   *
   * @return
   */
  public long getInFlight() {
    return inFlight;
  }

  public Metrics add( Metrics right ) {
    return new Metrics(
      getIn() + right.getIn(),
      getOut() + right.getOut(),
      getDropped() + right.getDropped(),
      getInFlight() + right.getInFlight()
    );
  }

  @Override public <T> Optional<T> unwrap( Class<T> clazz ) {
    if ( clazz.isAssignableFrom( getClass() ) ) {
      return Optional.of( clazz.cast( this ) );
    }
    return Optional.empty();
  }

  @Override public String toString() {
    return "Metrics{" +
      "in=" + in +
      ", out=" + out +
      ", dropped=" + dropped +
      ", inFlight=" + inFlight +
      '}';
  }
}
