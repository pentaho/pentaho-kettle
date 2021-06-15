/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.util;


/**
 * Constant utility class which provides commonly used strings for HTTP.
 */
public final class HTTP {

  public static final String CONTENT_TYPE_HTML = "text/html";
  public static final String CONTENT_TYPE_JAVASCRIPT = "text/javascript";
  public static final String CONTENT_TYPE_JSON = "application/json"; // RFC 4627

  public final static String CHARSET_UTF_8 = "UTF-8";
  public static final String METHOD_GET = "GET";
  public static final String METHOD_POST = "POST";
  public static final String HEADER_ACCEPT = "Accept";

  private HTTP() {
    // prevent instantiation
  }

}
