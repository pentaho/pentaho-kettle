/*******************************************************************************
 * Copyright (c) 2007, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;


/**
 * Utility class to open and close an external browser window.
 *
 * @deprecated Use {@link UrlLauncher} instead
 * @since 2.0
 */
@Deprecated
public final class ExternalBrowser {

  /**
   * Style parameter (value 1&lt;&lt;1) indicating that the address combo and
   * 'Go' button will be created for the browser.
   * <p>Note: This style parameter is a hint and might be ignored by some
   * browsers.</p>
   */
  public static final int LOCATION_BAR = 1 << 1;

  /**
   * Style parameter (value 1&lt;&lt;2) indicating that the navigation bar for
   * navigating web pages will be created for the web browser.
   * <p>Note: This style parameter is a hint and might be ignored by some
   * browsers.</p>
   */
  public static final int NAVIGATION_BAR = 1 << 2;

  /**
   * Style constant (value 1&lt;&lt;3) indicating that status will be tracked
   * and shown for the browser (page loading progress, text messages etc.).
   * <p>Note: This style parameter is a hint and might be ignored by some
   * browsers.</p>
   */
  public static final int STATUS = 1 << 3;

  private static final String EXTERNAL_BROWSER_ID = "eb";
  private static final String EXTERNAL_BROWSER_TYPE = "rwt.widgets.ExternalBrowser";
  private static final String METHOD_OPEN = "open";
  private static final String METHOD_CLOSE = "close";
  private static final String PROPERTY_ID = "id";
  private static final String PROPERTY_URL = "url";
  private static final String PROPERTY_STYLE = "style";

  /**
   * Opens the given <code>url</code> in an external browser.
   *
   * <p>The method will reuse an existing browser window if the same
   * <code>id</code> value is passed to it.</p>
   *
   * @param id if an instance of a browser with the same id is already
   *   opened, it will be reused instead of opening a new one. The id
   *   must neither be <code>null</code> nor empty.
   * @param url the URL to display, must not be <code>null</code>
   * @param style the style display constants. Style constants should be
   *   bitwise-ORed together.
   *
   * @throws SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the <code>id</code> or <code>url</code>
   *      is <code>null</code></li>
   *    <li>ERROR_INVALID_ARGUMENT - if the <code>id</code> is empty</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *      created the receiver</li>
   * </ul>
   *
   * @deprecated Use {@link UrlLauncher#openURL(String)} instead
   */
  @Deprecated
  public static void open( String id, String url, int style ) {
    checkWidget();
    ensureInstance();
    if( id == null || url == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( id.length() == 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    renderOpen( id, url, style );
  }

  /**
   * Closes the browser window denoted by the given <code>id</code>. The
   * method does nothing if there is no browser window with the given id.
   *
   * @param id if an instance of a browser with the same id is opened,
   *   it will be close. The id must neither be <code>null</code> nor empty.
   *
   * @throws SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the <code>id</code> is
   *      <code>null</code></li>
   *    <li>ERROR_INVALID_ARGUMENT - if the <code>id</code> is empty</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *      created the receiver</li>
   * </ul>
   *
   * @deprecated
   */
  @Deprecated
  public static void close( String id ) {
    checkWidget();
    ensureInstance();
    if( id == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( id.length() == 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    renderClose( id );
  }

  //////////////////
  // Helping methods

  private static void ensureInstance() {
    SingletonUtil.getSessionInstance( ExternalBrowser.class );
  }

  private static void renderOpen( String id, String url, int style ) {
    ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
    JsonObject parameters = new JsonObject()
      .add( PROPERTY_ID, JsonValue.valueOf( id ) )
      .add( PROPERTY_URL, JsonValue.valueOf( url ) )
      .add( PROPERTY_STYLE, getFeatures( style ) );
    protocolWriter.appendCall( EXTERNAL_BROWSER_ID, METHOD_OPEN, parameters );
  }

  private static void renderClose( String id ) {
    ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
    JsonObject parameters = new JsonObject().add( PROPERTY_ID, JsonValue.valueOf( id ) );
    protocolWriter.appendCall( EXTERNAL_BROWSER_ID, METHOD_CLOSE, parameters );
  }

  private static JsonArray getFeatures( int style ) {
    JsonArray features = new JsonArray();
    if( ( style & STATUS ) != 0 ) {
      features.add( "STATUS" );
    }
    if( ( style & LOCATION_BAR ) != 0 ) {
      features.add( "LOCATION_BAR" );
    }
    if( ( style & NAVIGATION_BAR ) != 0 ) {
      features.add( "NAVIGATION_BAR" );
    }
    return features;
  }

  private static void checkWidget() {
    if( Display.getCurrent().getThread() != Thread.currentThread() ) {
      SWT.error( SWT.ERROR_THREAD_INVALID_ACCESS );
    }
  }

  private ExternalBrowser() {
    ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
    protocolWriter.appendCreate( EXTERNAL_BROWSER_ID, EXTERNAL_BROWSER_TYPE );
  }
}
