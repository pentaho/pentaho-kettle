/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.UISession;


/**
 * This class provides application-wide access to the context of the currently processed request.
 * <p>
 * It is possible to register a context to a thread other than the request thread (i.e. not the
 * current thread). This is useful to enable background processes to access data stored in a UI
 * session in the same way as in the request thread.
 * </p>
 * <p>
 * Note: In case that a context was already added using the <code>setContext(ServiceContext)</code>
 * method and it's tried to add another context using <code>setContext(ServiceContext,Thread)</code>
 * no exception will be thrown. That's because a check is not possible due to implementation
 * details. In such a case the context added with <code>setContext(ServiceContext)</code> will be
 * preferred.
 * </p>
 */
public class ContextProvider {

  // The context mapping mechanism used in standard UI requests from the client
  private final static ThreadLocal<ServiceContext> CONTEXT_HOLDER = new ThreadLocal<>();

  // Used to map contexts to background threads from another thread
  // Note: this map could also be used to replace the CONTEXT_HOLDER, but we prefer the thread
  // local for the common use case because of its smaller synchronization impact.
  private final static Map<Thread, ServiceContext> CONTEXT_HOLDER_FOR_BG_THREADS
    = new WeakHashMap<>();

  /**
   * Maps the given service context to the currently processed request.
   * <p>
   * Note: to dispose of contexts that are added with this method use {@link #disposeContext()}.
   * </p>
   */
  public static void setContext( ServiceContext context ) {
    ParamCheck.notNull( context, "context" );
    if( getContextInternal() != null ) {
      String msg = "Current thread has already a context instance buffered.";
      throw new IllegalStateException( msg );
    }
    CONTEXT_HOLDER.set( context );
  }

  /**
   * Maps the given service context to the specified thread. This is used to allow background
   * processes to access data stored in the UI session.
   * <p>
   * Note: to dispose of contexts mapped with this method use {@link #disposeContext(Thread)}. To
   * map the context to the current thread use <code>setContext(ServiceContext)</code> instead.
   * </p>
   */
  public static void setContext( ServiceContext context, Thread thread ) {
    ParamCheck.notNull( context, "context" );
    ParamCheck.notNull( thread, "thread" );
    synchronized( CONTEXT_HOLDER_FOR_BG_THREADS ){
      if( CONTEXT_HOLDER_FOR_BG_THREADS.containsKey( thread ) ) {
        String msg = "The given thread has already a context instance mapped.";
        throw new IllegalStateException( msg );
      }
      CONTEXT_HOLDER_FOR_BG_THREADS.put( thread, context );
    }
  }

  /**
   * Returns the service context mapped to the currently processed request.
   */
  public static ServiceContext getContext() {
    ServiceContext result = getContextInternal();
    if( result == null ) {
      String msg = "No context available outside of the request processing.";
      throw new IllegalStateException( msg );
    }
    return result;
  }

  /**
   * Returns whether the current thread has a mapped service context.
   */
  public static boolean hasContext() {
    return getContextInternal() != null;
  }

  /**
   * Releases the currently buffered context instance. This is automatically called by the framework
   * to end the context's lifecycle. A premature call will cause failure of the currently processed
   * request lifecycle.
   * <p>
   * Note: only <code>ServiceContext</code> instances that where mapped by calling
   * <code>setContext()</code> from the running thread can be disposed of using this method.
   * Contexts that were registered by <code>setContext(Thread,ServiceContext)</code> must be
   * disposed of by using <code>disposeContext(Thread)</code>.
   * </p>
   */
  public static void disposeContext() {
    ServiceContext context = CONTEXT_HOLDER.get();
    if( context != null ) {
      if( !context.isDisposed() ) {
        context.dispose();
      }
    }
    // DO NOT MOVE THIS LINE INTO THE IF BLOCK
    // This would cause a memory leak as disposeContext() is used to dispose
    // of a context *and* disassociate the context from the thread
    releaseContextHolder();
  }

  /**
   * Releases the association between a thread and its context. This is used for background
   * processing that needs access to data stored in a session context.
   * <p>
   * Note: only <code>ServiceContext</code> instances that were mapped with the
   * <code>setContext(Thread,ServiceContext)</code> method can be disposed by this method. Contexts
   * that were registered by <code>setContext(ServiceContext)</code> by the running thread must be
   * disposed of by calling <code>disposeContext()</code> from the same thread.
   * </p>
   */
  public static void disposeContext( Thread thread ) {
    ParamCheck.notNull( thread, "thread" );
    synchronized( CONTEXT_HOLDER_FOR_BG_THREADS ) {
      ServiceContext removed = CONTEXT_HOLDER_FOR_BG_THREADS.remove( thread );
      if( removed != null ) {
        removed.dispose();
      }
    }
  }

  public static boolean releaseContextHolder() {
    if( CONTEXT_HOLDER.get() != null ) {
      CONTEXT_HOLDER.set( null );
      return false;
    }
    synchronized( CONTEXT_HOLDER_FOR_BG_THREADS ) {
      CONTEXT_HOLDER_FOR_BG_THREADS.remove( Thread.currentThread() );
    }
    return true;
  }

  private static ServiceContext getContextInternal() {
    ServiceContext result = CONTEXT_HOLDER.get();
    if( result == null ) {
      synchronized( CONTEXT_HOLDER_FOR_BG_THREADS ) {
        result = CONTEXT_HOLDER_FOR_BG_THREADS.get( Thread.currentThread() );
      }
    }
    return result;
  }

  /**
   * Returns the UI session that is associated with the currently processed request.
   */
  public static UISession getUISession() {
    return getContext().getUISession();
  }

  /**
   * Returns the application context that is associated with the currently processed request.
   */
  public static ApplicationContextImpl getApplicationContext() {
    return getContext().getApplicationContext();
  }

  /**
   * Returns the <code>HttpServletRequest</code> that is currently processed. This is a convenience
   * method that delegates to <code>ContextProvider.getContext().getRequest()</code>.
   */
  public static HttpServletRequest getRequest() {
    return getContext().getRequest();
  }

  /**
   * Returns the <code>HttpServletResponse</code> that is mapped to the currently processed request.
   * This is a convenience method that delegates to
   * <code>ContextProvider.getContext().getResponse()</code>.
   */
  public static HttpServletResponse getResponse() {
    return getContext().getResponse();
  }

  /**
   * Returns the service store that is mapped to the currently processed request. This is a
   * convenience method that delegates to
   * <code>ContextProvider.getContext().getServiceStore()</code>.
   */
  public static ServiceStore getServiceStore() {
    return getContext().getServiceStore();
  }

  /**
   * Returns the protocol writer for the current request. This is a convenience method that
   * delegates to <code>ContextProvider.getContext().getProtocolWriter()</code>.
   */
  public static ProtocolMessageWriter getProtocolWriter() {
    return getContext().getProtocolWriter();
  }

}
