package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.ReportingEvent;

import java.io.Serializable;
import java.util.Objects;

/**
 * {@link ReportingEvent} data holder for remote execution. These objects should be passed to {@link
 * ExecutionRequest#update(Notification)} to signal when a request state should change.
 * <p>
 * These events can also be subscribed to through the execution context.
 * Use the {@link Transformation} as the event source.
 * <p>
 * Created by hudak on 1/26/17.
 */
public class Notification implements Serializable {
  private static final long serialVersionUID = -6768653457353051556L;
  private final String serviceId;
  private final Type type;

  public Notification( String serviceId, Type type ) {
    this.serviceId = Objects.requireNonNull( serviceId, "serviceId == null" );
    this.type = Objects.requireNonNull( type, "type == null" );
  }

  /**
   * @return unique identifier for the processing service
   */
  public String getServiceId() {
    return serviceId;
  }

  public Type getType() {
    return type;
  }

  public enum Type {
    /**
     * Submit a bid to process this request. If {@link ExecutionRequest#update(Notification)} returns true, the request
     * will expect more updates from this service.
     */
    CLAIM,

    /**
     * Signal to the client that no more updates should be expected. All local event streams should close.
     */
    CLOSE
  }

  static class Event implements ReportingEvent<Transformation, Notification> {
    private final Transformation source;
    private final Notification notification;

    Event( Transformation source, Notification notification ) {
      this.source = source;
      this.notification = notification;
    }

    @Override public Transformation getSource() {
      return source;
    }

    @Override public Notification getData() {
      return notification;
    }
  }
}
