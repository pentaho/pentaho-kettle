/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2021 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core;

import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class WebSpoonUtils {

  private static final InheritableThreadLocal<UISession> uiSession = new InheritableThreadLocal<UISession>();
  private static final HashMap<String, UISession> uiSessionMap = new HashMap<String, UISession>();
  /**
   * This is a map between connectionId (cid) and user.
   */
  private static final HashMap<String, String> userMap = new HashMap<String, String>();

  public static void setTestId( Widget widget, String value ) {
    if ( !widget.isDisposed() ) {
      String $el = widget instanceof Text ? "$input" : "$el";
      String id = WidgetUtil.getId( widget );
      value = StringEscapeUtils.escapeJavaScript( value );
      exec( "rap.getObject( '", id, "' ).", $el, ".attr( 'test-id', '", value + "' );" );
    }
  }

  public static void exec( String... strings ) {
    StringBuilder builder = new StringBuilder();
    builder.append( "try{" );
    for ( String str : strings ) {
      builder.append( str );
    }
    builder.append( "}catch(e){}" );
    JavaScriptExecutor executor = RWT.getClient().getService( JavaScriptExecutor.class );
    executor.execute( builder.toString() );
  }

  /**
   * Set UISession to InheritableThreadLocal.
   * The current or any child thread can get an UISession.
   * This UISession will be GCed when no thread has a reference to it.
   * @see java.lang.ThreadLocal
   * @param uiSession
   */
  public static void setUISession( UISession uiSession ) {
    WebSpoonUtils.uiSession.set( uiSession );
  }

  /**
   * Get UISession from InheritableThreadLocal.
   * The current or any parent thread should set one before getting.
   * @return UISession
   */
  public static UISession getUISession() {
    return WebSpoonUtils.uiSession.get();
  }

  /**
   * Set UISession with cid (Connection Id) as a key.
   * Unlike {@link #setUISession(UISession)}, this UISession should explicitly be removed when a UIThread dies.
   * @param cid
   * @param uiSession
   */
  public static void setUISession( String cid, UISession uiSession ) {
    uiSessionMap.put( cid, uiSession );
  }

  /**
   * Get UISession by cid (Connection Id).
   * @param cid
   * @return UISession
   */
  public static UISession getUISession( String cid ) {
    return uiSessionMap.get( cid );
  }

  public static void removeUISession( String cid ) {
    uiSessionMap.remove( cid );
  }

  public static void setUser( String cid, String user ) {
    userMap.put( cid, user );
  }

  public static String getUser( String cid ) {
    return userMap.get( cid );
  }

  public static void removeUser( String cid ) {
    userMap.remove( cid );
  }

  public static String getConnectionId() {
    return ( (UISessionImpl) getUISession() ).getConnectionId();
  }

}
