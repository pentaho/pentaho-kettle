/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Display;


public final class ContextUtil {

  private static final ClassLoader CLASS_LOADER = ContextUtil.class.getClassLoader();
  private static final HttpServletResponse FAKE_RESPONSE = createFakeResponse();
  private static final Class<?> FAKE_REQUEST_CLASS = getRequestProxyClass();

  private ContextUtil() {
    // prevent instantiation
  }

  public static void runNonUIThreadWithFakeContext( Display display, Runnable runnable ) {
    UISession uiSession = display.getAdapter( IDisplayAdapter.class ).getUISession();
    runNonUIThreadWithFakeContext( uiSession, runnable );
  }

  public static void runNonUIThreadWithFakeContext( UISession uiSession, Runnable runnable ) {
    // Don't replace local variables by method calls, since the context may
    // change during the methods execution.
    boolean useDifferentContext = ContextProvider.hasContext()
                                  && ContextProvider.getUISession() != uiSession;
    ServiceContext contextBuffer = null;
    // TODO [fappel]: The context handling's getting very awkward in case of
    //                having the context mapped instead of stored it in
    //                the ContextProvider's ThreadLocal (see ContextProvider).
    //                Because of this the wasMapped variable is used to
    //                use the correct way to restore the buffered context.
    //                See whether this can be done more elegantly and supplement
    //                the test cases...
    boolean wasMapped = false;
    if( useDifferentContext ) {
      contextBuffer = ContextProvider.getContext();
      wasMapped = ContextProvider.releaseContextHolder();
    }
    boolean useFakeContext = !ContextProvider.hasContext();
    if( useFakeContext ) {
      ContextProvider.setContext( createFakeContext( uiSession ) );
    }
    try {
      runnable.run();
    } finally {
      if( useFakeContext ) {
        ContextProvider.disposeContext();
      }
      if( useDifferentContext ) {
        if( wasMapped ) {
          ContextProvider.setContext( contextBuffer, Thread.currentThread() );
        } else {
          ContextProvider.setContext( contextBuffer );
        }
      }
    }
  }

  public static ServiceContext createFakeContext( UISession uiSession ) {
    HttpServletRequest request = createFakeRequest( uiSession );
    ServiceContext result = new ServiceContext( request, FAKE_RESPONSE, uiSession );
    result.setServiceStore( new ServiceStore() );
    return result;
  }

  private static HttpServletRequest createFakeRequest( UISession uiSession ) {
    InvocationHandler invocationHandler = new RequestInvocationHandler( uiSession );
    Class<?>[] paramTypes = { InvocationHandler.class };
    Object[] paramValues = { invocationHandler };
    Object fakeRequest = ClassUtil.newInstance( FAKE_REQUEST_CLASS, paramTypes, paramValues );
    return ( HttpServletRequest )fakeRequest;
  }

  private static Class<?> getRequestProxyClass() {
    return Proxy.getProxyClass( CLASS_LOADER, new Class<?>[] { HttpServletRequest.class } );
  }

  private static HttpServletResponse createFakeResponse() {
    Class<?>[] interfaces = { HttpServletResponse.class };
    ResponseInvocationHandler invocationHandler = new ResponseInvocationHandler();
    Object proxy = Proxy.newProxyInstance( CLASS_LOADER, interfaces, invocationHandler );
    return ( HttpServletResponse )proxy;
  }

  private static final class ResponseInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      throw new UnsupportedOperationException();
    }
  }

  private static class RequestInvocationHandler implements InvocationHandler {
    private final UISession uiSession;

    RequestInvocationHandler( UISession uiSession ) {
      this.uiSession = uiSession;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      Object result;
      if( "getSession".equals( method.getName() ) ) {
        result = uiSession.getHttpSession();
      } else if( "getLocale".equals( method.getName() ) ) {
        result = null;
      } else {
        throw new UnsupportedOperationException();
      }
      return result;
    }
  }

}
