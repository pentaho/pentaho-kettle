/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;


/**
 * A service handler can be used to process custom requests, e.g. to deliver files, images or other
 * content to the client. Service handlers are called in the user's session scope, i.e. they can access session
 * information. They can be implemented like servlets, i.e. they can access headers and parameters
 * from the request object and write the result into the response writer or output stream.
 * <p>
 * Implementations can be registered with the {@link ServiceManager} or in an
 * {@link ApplicationConfiguration}. Once registered, a service handler can be accessed by a URL
 * with a specific parameter. This URL can be obtained by
 * {@link ServiceManager#getServiceHandlerUrl(String)}. Example:
 * </p>
 *
 * <pre>
 * RWT.getServiceManager().registerServiceHandler( &quot;download&quot;, new MyServiceHandler() );
 * String url = RWT.getServiceManager().getServiceHandlerUrl( &quot;download&quot; );
 * </pre>
 *
 * @see ServiceManager
 * @since 2.0
 */
public interface ServiceHandler {

  /**
   * This method is called by the framework when a request for a service handler is received by the
   * client. Implementations can access information of the request and write their output to the
   * response object. Before writing content, the content type and encoding should be set.
   *
   * @param request the request that has been received from the client
   * @param response the response that will be sent to the client
   * @throws IOException
   * @throws ServletException
   */
  void service( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException;

}
