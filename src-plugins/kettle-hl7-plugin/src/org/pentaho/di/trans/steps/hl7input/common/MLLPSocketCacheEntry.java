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
  public MLLPSocketCacheEntry(ServerSocket serverSocket, StreamSource streamSource, MLLPTransport transport) {
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
  public void setServerSocket(ServerSocket serverSocket) {
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
  public void setStreamSource(StreamSource streamSource) {
    this.streamSource = streamSource;
  }

  /**
   * @return the transport
   */
  public MLLPTransport getTransport() {
    return transport;
  }

  /**
   * @param transport the transport to set
   */
  public void setTransport(MLLPTransport transport) {
    this.transport = transport;
  }

  /**
   * @return the jobListener
   */
  public JobListener getJobListener() {
    return jobListener;
  }

  /**
   * @param jobListener the jobListener to set
   */
  public void setJobListener(JobListener jobListener) {
    this.jobListener = jobListener;
  }
}