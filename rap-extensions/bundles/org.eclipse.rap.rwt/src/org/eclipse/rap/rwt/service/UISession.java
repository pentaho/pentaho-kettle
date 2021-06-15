/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
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

import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.client.service.ClientInfo;
import org.eclipse.rap.rwt.remote.Connection;


/**
 * The <code>UISession</code> represents the current instance of the UI.
 * <p>
 * In contrast to the <code>HttpSession</code> it is possible to register a listener that is
 * notified <em>before</em> the session is destroyed. This listener can be used to cleanup on
 * session shutdown with the UI session still intact.
 * </p>
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface UISession {

  /**
   * Binds an object to this UI Session, using the name specified. If an object of the same name is
   * already bound to the UI session, the object is replaced.
   * <p>
   * If the value is null, this has the same effect as calling <code>removeAttribute()<code>.
   * </p>
   *
   * @param name the name to which the object is bound; cannot be <code>null</code>
   * @param value the object to be bound
   * @return <code>true</code> if the attribute was set or <code>false</code> if the attribute could
   *         not be set because the session was invalidated.
   */
  boolean setAttribute( String name, Object value );

  /**
   * Returns the object bound with the specified name in this UI session, or <code>null</code> if no
   * object is bound under the name.
   *
   * @param name a string specifying the name of the object; cannot be null
   * @return the object bound with the specified name or <code>null</code> if no object is bound
   *         with this name
   */
  Object getAttribute( String name );

  /**
   * Removes the object bound with the specified name from this UI session. If no object is bound
   * with the specified name, this method does nothing.
   *
   * @param name The name of the object to remove from this UI session, must not be
   *          <code>null</code>
   * @return <code>true</code> if the attribute was removed or <code>false</code> if the attribute
   *         could not be removed because the session was invalidated
   * @see #isBound()
   */
  boolean removeAttribute( String name );

  /**
   * Returns an <code>Enumeration</code> of the names of all objects bound to this UI session.
   *
   * @return An <code>Enumeration</code> of strings that contains the names of all objects bound to
   *         this UI session or an empty enumeration if the underlying session was invalidated
   * @see #isBound()
   */
  Enumeration<String> getAttributeNames();

  /**
   * Returns a unique identifier for this UI session.
   *
   * @return the unique identifier for this UI session
   */
  String getId();

  /**
   * Adds a <code>UISessionListener</code> to this UI session. UISessionListeners are used to
   * receive a notification before the UI session is destroyed. If the given listener was already
   * added the method has no effect.
   * <p>
   * If the UI session is already destroyed or is about to be destroyed the listener will not be
   * added. In this case, this method returns <code>false</code>. A return value of
   * <code>true</code> ensures that the listener is registered and will be called.
   * </p>
   *
   * @param listener the listener to be added
   * @return <code>true</code> if the listener was added and will be called, or <code>false</code>
   *         if the listener could not be added
   * @see #isBound()
   */
  boolean addUISessionListener( UISessionListener listener );

  /**
   * Removes a <code>UISessionListener</code> from this UI session. UISessionListeners are used to
   * receive notifications before the UI session is destroyed. If the given listener was not added
   * to the session store this method has no effect.
   * <p>
   * If the UI session is already destroyed or is about to be destroyed the listener will not be
   * removed. In this case, this method returns <code>false</code>. A return value of
   * <code>true</code> ensures that the listener is removed and will not be called anymore.
   * </p>
   *
   * @param listener the listener to be removed
   * @return <code>true</code> if the listener was removed and will not be called anymore, or
   *         <code>false</code> if the listener could not be removed
   * @see #isBound()
   */
  boolean removeUISessionListener( UISessionListener listener );

  /**
   * Returns the ApplicationContext this UISession belongs to.
   *
   * @return the application context for this UI session
   * @since 2.1
   */
  ApplicationContext getApplicationContext();

  /**
   * Returns the underlying HttpSession instance.
   *
   * @return the HttpSession instance
   */
  HttpSession getHttpSession();

  /**
   * Returns whether this UI session is bound to the underlying <code>HttpSession</code> or not. If
   * the session store is unbound it behaves as if the HTTP session it belonged to was invalidated.
   *
   * @return true if the session store is bound, false otherwise.
   */
  boolean isBound();

  /**
   * Returns a representation of the client that is connected with the server in this UI session.
   *
   * @return The client for this UI session, never <code>null</code>
   */
  public Client getClient();

  /**
   * Returns the connection used to communicate with the client that is connected to this UI
   * session.
   *
   * @return the connection for this UI session, never <code>null</code>
   */
  public Connection getConnection();

  /**
   * Returns the preferred <code>Locale</code> for this UI session. The result reflects the locale
   * that has been set using {@link #setLocale(Locale)}. If no locale has been set on this
   * UISession, the locale will be taken from client using the {@link ClientInfo} service. If the
   * client request doesn't provide a preferred locale, then this method returns the default locale
   * for the server.
   *
   * @return the preferred locale for the client, or the default system locale, never
   *         <code>null</code>
   * @see #setLocale(Locale)
   */
  public Locale getLocale();

  /**
   * Sets the preferred <code>Locale</code> for this UI session. The value set can be retrieved
   * using {@link #getLocale()}.
   *
   * @param locale the locale to set, or <code>null</code> to reset
   * @see #getLocale()
   */
  public void setLocale( Locale locale );

  /**
   * Executes the given runnable in the context of this UI session. The runnable will be executed in
   * the calling thread but with a simulated context so that calls to <code>RWT.getUISession</code>
   * will return this UISession even in a background thread.
   * <p>
   * <strong>Note:</strong> this method is <strong>discouraged</strong>. It is only kept for
   * compatibility. New code should access the UISession directly.
   * </p>
   *
   * @param runnable the runnable to execute in the context of this UI session
   * @see org.eclipse.rap.rwt.SingletonUtil
   */
  void exec( Runnable runnable );
}
