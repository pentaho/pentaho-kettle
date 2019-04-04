/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.di.trans.steps.hl7input.common;

import java.net.ServerSocket;

import org.pentaho.di.job.JobListener;

import ca.uhn.hl7v2.protocol.StreamSource;
import ca.uhn.hl7v2.protocol.impl.MLLPTransport;

public class MLLPSocketCacheEntry {

  private ServerSocket serverSocket;
  private StreamSource streamSource;
  private MLLPTransport transport;
  private JobListener jobListener;

  /**
   * @param serverSocket
   * @param streamSource
   */
  public MLLPSocketCacheEntry( ServerSocket serverSocket, StreamSource streamSource, MLLPTransport transport ) {
    this.serverSocket = serverSocket;
    this.streamSource = streamSource;
    this.transport = transport;
  }

  /**
   * @return the serverSocket
   */
  public ServerSocket getServerSocket() {
    return serverSocket;
  }

  /**
   * @param serverSocket
   *          the serverSocket to set
   */
  public void setServerSocket( ServerSocket serverSocket ) {
    this.serverSocket = serverSocket;
  }

  /**
   * @return the streamSource
   */
  public StreamSource getStreamSource() {
    return streamSource;
  }

  /**
   * @param streamSource
   *          the streamSource to set
   */
  public void setStreamSource( StreamSource streamSource ) {
    this.streamSource = streamSource;
  }

  /**
   * @return the transport
   */
  public MLLPTransport getTransport() {
    return transport;
  }

  /**
   * @param transport
   *          the transport to set
   */
  public void setTransport( MLLPTransport transport ) {
    this.transport = transport;
  }

  /**
   * @return the jobListener
   */
  public JobListener getJobListener() {
    return jobListener;
  }

  /**
   * @param jobListener
   *          the jobListener to set
   */
  public void setJobListener( JobListener jobListener ) {
    this.jobListener = jobListener;
  }
}
