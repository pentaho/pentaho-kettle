/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.engine.api.remote;

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
@SuppressWarnings ( "unused" )
public class StopMessage implements Message {

  public enum Status {
    SESSION_KILLED, // spark session was killed
    SUCCESS, //Stop execution was successful
    FAILED //failed to stop execution
  }

  private static final long serialVersionUID = 8842623444691045346L;
  private String reasonPhrase;
  private String requestUUID;
  private Status result;
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
   * @param result       stop operation result: SUCCESS, FAILED, SESSION_KILLED;
   */
  public StopMessage( String reasonPhrase, Status result ) {
    this( null, reasonPhrase, result, false );
  }

  /**
   * Constructor used by driver to send back to daemon server the sessionKilled of the stop operation
   *
   * @param requestUUID  request/execution ID to stop or stopped
   * @param reasonPhrase returns back the reason presented by daemon server for the stop request
   * @param result       stop operation result: SUCCESS, FAILED, SESSION_KILLED;
   */
  public StopMessage( String requestUUID, String reasonPhrase, Status result ) {
    this( requestUUID, reasonPhrase, result, false );
  }

  private StopMessage( String requestUUID, String reasonPhrase, Status result, boolean safeStop ) {
    this.requestUUID = requestUUID;
    this.reasonPhrase = reasonPhrase;
    this.result = result;
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
   * Sets the stop reason phrase
   *
   * @param reasonPhrase stop reason phrase
   */
  public void setReasonPhrase( String reasonPhrase ) {
    this.reasonPhrase = reasonPhrase;
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
   * True if the execution was stop with success, false otherwise.
   *
   * @return true if failed to stop the execution, false otherwise;
   */
  public boolean operationSuccessful() {
    return this.result == Status.SUCCESS;
  }

  /**
   * True if failed to stop the execution, false otherwise.
   *
   * @return true if failed to stop the execution, false otherwise;
   */
  public boolean operationFailed() {
    return this.result == Status.FAILED;
  }

  /**
   * True if the session was killed, false otherwise.
   *
   * @return true if the session was killed, false otherwise;
   */
  public boolean sessionWasKilled() {
    return this.result == Status.SESSION_KILLED;
  }


  /**
   * True if the stop should be "graceful".  I.e. should finish any work currently
   * in progress.
   */
  public boolean isSafeStop() {
    return safeStop;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String reasonPhrase;
    private String requestUUID;
    private Status result;
    private boolean safeStop = false;

    public Builder reasonPhrase( String reasonPhrase ) {
      this.reasonPhrase = reasonPhrase;
      return this;
    }

    public Builder requestUUID( String requestUUID ) {
      this.requestUUID = requestUUID;
      return this;
    }

    public Builder result( Status result ) {
      this.result = result;
      return this;
    }

    public Builder safeStop( boolean safeStop ) {
      this.safeStop = safeStop;
      return this;
    }

    public StopMessage build() {
      return new StopMessage( requestUUID, reasonPhrase, result, safeStop );
    }
  }

}
