/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt;

import org.eclipse.rap.rwt.internal.SingletonManager;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.UISession;


/**
 * Creates and maintains a unique instance of a given type for the given scope. The scope is either
 * a UI session or an application context. Within the context of this scope,
 * <code>getUniqueInstance(...)</code> will always return the same object, but for different scopes
 * the returned instances will be different.
 * <p>
 * This utility class can be used to adjust classic singletons to the appropriate scope in RAP.
 * Example:
 * </p>
 * <pre>
 * public class FooSingleton {
 *
 *   private FooSingleton() {
 *   }
 *
 *   public static FooSingleton getInstance() {
 *     return SingletonUtil.getUniqueInstance( FooSingleton.class, RWT.getUISession() );
 *   }
 * }
 * </pre>
 *
 * @since 2.0
 */
public final class SingletonUtil {

  /**
   * Returns an instance of the specified type that is unique within the current UI session. If no
   * such instance exists yet, a new one will be created. The specified type must have a
   * parameterless constructor.
   * <p>
   * This method is a shortcut for
   * <code>getUniqueInstance( type, RWT.getUISession() )</code>.
   * </p>
   *
   * @param type the type to obtain a singleton instance for
   * @return the unique instance of the specified type that is associated with the current UI
   *         session
   */
  public static <T> T getSessionInstance( Class<T> type ) {
    return getUniqueInstance( type, RWT.getUISession() );
  }

  /**
   * Returns an instance of the specified type that is unique within the given UI session. If no
   * such instance exists yet, a new one will be created. The specified type must have a
   * parameterless constructor.
   *
   * @param type the type to obtain a singleton instance for
   * @param uiSession the UI session to store the singleton instance in
   * @return the unique instance of the specified type that is associated with the given UI session
   * @since 2.3
   */
  public static <T> T getUniqueInstance( Class<T> type, UISession uiSession ) {
    ParamCheck.notNull( type, "type" );
    ParamCheck.notNull( uiSession, "uiSession" );
    return SingletonManager.getInstance( uiSession ).getSingleton( type );
  }

  /**
   * Returns an instance of the specified type that is unique within the given UI session. If no
   * such instance exists yet, a new one will be created. The specified type must have a
   * parameterless constructor.
   *
   * @param type the type to obtain a singleton instance for
   * @param applicationContext the application context to store the singleton instance in
   * @return the unique instance of the specified type that is associated with the given application
   *         context
   * @since 2.3
   */
  public static <T> T getUniqueInstance( Class<T> type, ApplicationContext applicationContext ) {
    ParamCheck.notNull( type, "type" );
    ParamCheck.notNull( applicationContext, "applicationContext" );
    return SingletonManager.getInstance( applicationContext ).getSingleton( type );
  }

}
