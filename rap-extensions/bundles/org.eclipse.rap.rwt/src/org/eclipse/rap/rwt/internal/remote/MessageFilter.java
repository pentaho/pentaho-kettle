/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.remote;

import org.eclipse.rap.rwt.internal.protocol.RequestMessage;
import org.eclipse.rap.rwt.internal.protocol.ResponseMessage;


/**
 * A MessageFilter intercepts the processing of an incoming message in order to add additional
 * processing steps. All message filters registered in the system build up a filter chain that all
 * messages pass through. Every message filter must delegate to the message to the chain, but it can
 * perform actions before and after message processing.
 * <pre>
 * request message  --> |message| --> |message| --> |RWT|
 * response message <-- |filter | <-- |filter | <-- |   |
 * </pre>
 * <p>
 * Message filters can be used for tasks such as logging, tracking, measuring or setting up some
 * kind of environment required during message processing. They <strong>should not modify</strong>
 * the incoming or outgoing messages.
 * </p>
 */
public interface MessageFilter {

  /**
   * Processes an incoming message and delegates to the filter chain. Implementations must delegate
   * to the filter chain, but they can perform actions before and after this call. Example:
   * <pre>
   * &#064;Override
   * public ResponseMessage handleMessage( RequestMessage request, MessageFilterChain chain ) {
   *   // code to run before message processing
   *   ResponseMessage response = chain.handleMessage( request );
   *   // code to run after message processing
   *   return response;
   * }
   * </pre>
   *
   * @param request the incoming message from the client
   * @param chain the filter chain to delegate to
   * @return the message to send to the client in response
   */
  ResponseMessage handleMessage( RequestMessage request, MessageFilterChain chain );

}
