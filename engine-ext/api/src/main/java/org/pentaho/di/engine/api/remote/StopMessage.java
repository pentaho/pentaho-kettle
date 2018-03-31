/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

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

  /**
   * Constructor used by the clients when sending the stop request for the daemon server
   *
   * @param reasonPhrase reason for the Stop request
   */
  public StopMessage( String reasonPhrase ) {
    this.reasonPhrase = reasonPhrase;
    this.result = Status.SUCCESS;
  }

  /**
   * Constructor used by the daemon server to send the stop request for the driver
   *
   * @param requestUUID  request/execution ID to stop
   * @param reasonPhrase reason for the Stop request
   */
  public StopMessage( String requestUUID, String reasonPhrase ) {
    this.requestUUID = requestUUID;
    this.reasonPhrase = reasonPhrase;
    this.result = Status.SUCCESS;
  }

  /**
   * Constructor used by driver to send back to daemon server the sessionKilled of the stop operation
   *
   * @param reasonPhrase returns back the reason presented by daemon server for the stop request
   * @param result       stop operation result: SUCCESS, FAILED, SESSION_KILLED;
   */
  public StopMessage( String reasonPhrase, Status result ) {
    this.reasonPhrase = reasonPhrase;
    this.result = result;
  }

  /**
   * Constructor used by driver to send back to daemon server the sessionKilled of the stop operation
   *
   * @param requestUUID  request/execution ID to stop or stopped
   * @param reasonPhrase returns back the reason presented by daemon server for the stop request
   * @param result       stop operation result: SUCCESS, FAILED, SESSION_KILLED;
   */
  public StopMessage( String requestUUID, String reasonPhrase, Status result ) {
    this.requestUUID = requestUUID;
    this.reasonPhrase = reasonPhrase;
    this.result = result;
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
}
