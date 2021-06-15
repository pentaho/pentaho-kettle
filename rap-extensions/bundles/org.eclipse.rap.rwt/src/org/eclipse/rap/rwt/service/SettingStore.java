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

import java.io.IOException;
import java.util.Enumeration;


/**
 * A setting store is a persistent store for user-specific settings (i.e. key/value pairs). The data
 * in this store is kept beyond the scope of a single session.
 * <p>
 * A setting store instance is always associated with the current user session. Any settings being
 * put into the store (via {@link #setAttribute(String, String)}) are considered persisted from that
 * point on.
 * </p>
 * <p>
 * To retrieve data stored in a previous session, a client can invoke the method
 * {@link #loadById(String)} with the ID of the session to load into the store. This will load any
 * data stored under that id into the current setting store.
 * </p>
 * <p>
 * The framework will assign a new setting store to each new UI session, based on the current
 * session id. After the user has authenticated, application developers can use the
 * {@link #loadById(String)} method to initialize the store with persisted data stored under that id
 * during a previous session. Obviously, any data that is set into the store from that point on,
 * will also be persisted under the current id and will also be available in the future.
 * </p>
 * <p>
 * Custom setting store implementations are supported, those must provide a corresponding
 * {@link SettingStoreFactory}.
 * </p>
 *
 * @see FileSettingStore
 * @since 2.0
 */
public interface SettingStore {

  /**
   * Returns the attribute stored under the specified name in this setting store.
   *
   * @param name a non-null String specifying the name of the attribute
   * @return the attribute stored under the given name or <code>null</code> if no attribute is
   *         stored under that name
   * @throw {@link NullPointerException} if name is <code>null</code>
   */
  String getAttribute( String name );

  /**
   * Returns an {@link Enumeration} of strings with the names of all attributes in this setting
   * store.
   *
   * @return an enumeration with the attribute names in this setting store, never <code>null</code>
   */
  Enumeration<String> getAttributeNames();

  /**
   * Stores a given attribute in this setting store, using the name specified. If an attribute with
   * the same name is already stored in this setting store, the previous value is replaced. The
   * attribute is considered persisted when after this method completes.
   * <p>
   * If the value is <code>null</code>, this has the same effect as calling
   * {@link #removeAttribute(String)}.
   * </p>
   * <p>
   * {@link SettingStoreListener}s attached to this instance will be notified after an attribute has
   * been stored.
   * </p>
   *
   * @param name the name of the attribute, must not be <code>null</code> or empty
   * @param value the attribute to store, may be <code>null</code>
   * @throws IOException if the load operation failed to complete normally
   */
  void setAttribute( String name, String value ) throws IOException;

  /**
   * Removes the attribute stored under the specified name from this setting store. If no attribute
   * is stored under the specified name, this method does nothing.
   * <p>
   * {@link SettingStoreListener}s attached to this instance will be notified after an attribute has
   * been removed.
   * </p>
   *
   * @param name the name of the attribute to remove, must not be <code>null</code>
   * @throws IOException if the remove operation failed to complete normally
   */
  void removeAttribute( String name ) throws IOException;

  /**
   * Replaces the contents of this setting store with the persisted contents associated with the
   * given ID.
   * <p>
   * The attributes of this setting store will remain associated with the old id, but will be
   * removed from this store instance. {@link SettingStoreListener}s attached to this store
   * will receive a notification for each removed attribute.
   * </p>
   * <p>
   * During the load operation this store will be filled with the attributes associated with the new
   * ID. {@link SettingStoreListener}s attached to this store will receive a notification for each
   * added attribute.
   * </p>
   * <p>
   * After the load operation this store will only hold attributes associated with the new id value.
   * </p>
   *
   * @param id the ID of the settings to load, must not be <code>null</code> or empty
   * @throws IOException if the load operation failed to complete normally
   * @throws IllegalArgumentException if the given id is empty
   */
  void loadById( String id ) throws IOException;

  /**
   * Returns the unique identifier of this setting store.
   *
   * @return a non-empty string, never <code>null</code>
   */
  String getId();

  /**
   * Attaches the given listener to this setting store. Listeners will be notified of changes in the
   * store. If the listener has already been added to this store, this method does nothing.
   *
   * @param listener the listener to add, must not be <code>null</code>
   */
  void addSettingStoreListener( SettingStoreListener listener );

  /**
   * Removes the given listener from this setting store. If the listener has not been added, this
   * method does nothing.
   *
   * @param listener the listener to remove, must not be <code>null</code>
   */
  void removeSettingStoreListener( SettingStoreListener listener );

}
