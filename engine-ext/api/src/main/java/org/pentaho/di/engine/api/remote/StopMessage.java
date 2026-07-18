/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.engine.api.remote;

import java.util.Objects;

/**
 * Used for requesting to stop execution and for informing the sessionKilled of the stop operation.
 * Also used to inform the client of server failure
 * <p>
 * Since the CloseReason is not serializable collect the necessary parts and reconstruct the object when needed.
 * <p>
 * NOTE: There was an attempt to extend CloseReason and implement Message to make it serializable; but this did not
 * work.
 * <p>
 * Created by ccaspanello on
 * 7/25/17.
 */
@SuppressWarnings( "unused" )
public class StopMessage implements Message {


  /**
   * The result of stop operation.
   */
  public enum Status {
    /**
     * The session was killed by the stop request.
     */
    SESSION_KILLED,
    /**
     * The stop request was successful.
     */
    SUCCESS,
    /**
     * The stop request failed.
     */
    FAILED
  }

  private static final long serialVersionUID = 1641758282148544010L;
  private final String reasonPhrase;
  private final String requestUUID;
  private final Status status;
  private final boolean safeStop;


  /**
   * Constructor used by the clients when sending the stop request for the daemon server
   *
   * @param reasonPhrase reason for the Stop request
   */
  public StopMessage( String reasonPhrase ) {
    this( null, reasonPhrase, Status.SUCCESS, false );
  }

  /**
   * Constructor used by the daemon server to send the stop request for the driver
   *
   * @param requestUUID  request/execution ID to stop
   * @param reasonPhrase reason for the Stop request
   */
  public StopMessage( String requestUUID, String reasonPhrase ) {
    this( requestUUID, reasonPhrase, Status.SUCCESS, false );
  }

  /**
   * Constructor used by driver to send back to daemon server the sessionKilled of the stop operation
   *
   * @param reasonPhrase returns back the reason presented by daemon server for the stop request
   * @param status       stop operation status: {@link StopMessage.Status}
   */
  public StopMessage( String reasonPhrase, Status status ) {
    this( null, reasonPhrase, status, false );
  }

  /**
   * Constructor used by driver to send back to daemon server the sessionKilled of the stop operation
   *
   * @param requestUUID  request/execution ID to stop or stopped
   * @param reasonPhrase returns back the reason presented by daemon server for the stop request
   * @param status       stop operation status: {@link StopMessage.Status}
   */
  public StopMessage( String requestUUID, String reasonPhrase, Status status ) {
    this( requestUUID, reasonPhrase, status, false );
  }

  /**
   * Constructor used by driver to send back to daemon server the sessionKilled of the stop operation
   *
   * @param requestUUID  request/execution ID to stop or stopped
   * @param reasonPhrase returns back the reason presented by daemon server for the stop request
   * @param status       stop operation status: {@link StopMessage.Status}
   * @param safeStop     true if the stop should be "graceful".
   */
  private StopMessage( String requestUUID, String reasonPhrase, Status status, boolean safeStop ) {
    this.requestUUID = requestUUID;
    this.reasonPhrase = reasonPhrase;
    this.status = status;
    this.safeStop = safeStop;
  }

  /**
   * Returns the request unique identifier
   *
   * @return request unique identifier
   */
  public String getRequestUUID() {
    return requestUUID;
  }

  /**
   * Returns the stop reason.
   *
   * @return stop reason;
   */
  public String getReasonPhrase() {
    return reasonPhrase;
  }

  /**
   * Returns the status.
   *
   * @return
   */
  public Status getStatus() {
    return status;
  }

  /**
   * True if the execution was stop with success, false otherwise.
   *
   * @return true if failed to stop the execution, false otherwise;
   * @deprecated As of 11.1.0.0, use {@link #getStatus()} instead.
   */
  @Deprecated( since = "11.1.0.0", forRemoval = true )
  public boolean operationSuccessful() {
    return this.status == Status.SUCCESS;
  }

  /**
   * True if failed to stop the execution, false otherwise.
   *
   * @return true if failed to stop the execution, false otherwise;
   * @deprecated As of 11.1.0.0, use {@link #getStatus()} instead.
   */
  @Deprecated( since = "11.1.0.0", forRemoval = true )
  public boolean operationFailed() {
    return this.status == Status.FAILED;
  }

  /**
   * True if the session was killed, false otherwise.
   *
   * @return true if the session was killed, false otherwise;
   * @deprecated As of 11.1.0.0, use {@link #getStatus()} instead.
   */
  @Deprecated( since = "11.1.0.0", forRemoval = true )
  public boolean sessionWasKilled() {
    return this.status == Status.SESSION_KILLED;
  }

  /**
   * True if the stop should be "graceful".  I.e. should finish any work currently
   * in progress.
   */
  public boolean isSafeStop() {
    return safeStop;
  }

  @Override
  public String toString() {
    return "StopMessage{"
      + "reasonPhrase='" + reasonPhrase + '\''
      + ", requestUUID='" + requestUUID + '\''
      + ", status=" + status
      + ", safeStop=" + safeStop
      + '}';
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    StopMessage that = (StopMessage) o;

    return Objects.equals( requestUUID, that.requestUUID )
      && Objects.equals( reasonPhrase, that.reasonPhrase )
      && status == that.status
      && safeStop == that.safeStop;
  }

  @Override
  public int hashCode() {
    return Objects.hash( requestUUID, reasonPhrase, status, safeStop );
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String reasonPhrase;
    private String requestUUID;
    private Status status;
    private boolean safeStop = false;

    public Builder reasonPhrase( String reasonPhrase ) {
      this.reasonPhrase = reasonPhrase;
      return this;
    }

    public Builder requestUUID( String requestUUID ) {
      this.requestUUID = requestUUID;
      return this;
    }

    public Builder status( Status status ) {
      this.status = status;
      return this;
    }

    public Builder safeStop( boolean safeStop ) {
      this.safeStop = safeStop;
      return this;
    }

    public StopMessage build() {
      return new StopMessage( requestUUID, reasonPhrase, status, safeStop );
    }
  }

}
