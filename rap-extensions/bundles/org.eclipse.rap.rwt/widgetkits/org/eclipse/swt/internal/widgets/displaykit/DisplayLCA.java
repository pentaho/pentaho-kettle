/*******************************************************************************
 * Copyright (c) 2002, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.displaykit;

import static org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getLCA;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_RESIZE;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.handleOperation;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rap.rwt.internal.lifecycle.EntryPointManager;
import org.eclipse.rap.rwt.internal.lifecycle.EntryPointRegistration;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.ReparentedControls;
import org.eclipse.rap.rwt.internal.lifecycle.UITestUtil;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectLifeCycleAdapter;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.textsize.MeasurementUtil;
import org.eclipse.rap.rwt.internal.util.ActiveKeysUtil;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.ControlRemoteAdapter;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.internal.widgets.WidgetTreeUtil;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public class DisplayLCA {

  static final String PROP_FOCUS_CONTROL = "focusControl";
  static final String PROP_EXIT_CONFIRMATION = "exitConfirmation";
  private static final String METHOD_BEEP = "beep";
  private static final String PROP_RESIZE_LISTENER = "listener_Resize";

  public void readData( Display display ) {
    handleOperations( display );
    visitWidgets( display );
    DNDSupport.handleOperations();
    RemoteObjectLifeCycleAdapter.readData( ProtocolUtil.getClientMessage() );
  }

  public void preserveValues( Display display ) {
    RemoteAdapter adapter = getAdapter( display );
    adapter.preserve( PROP_FOCUS_CONTROL, display.getFocusControl() );
    adapter.preserve( PROP_EXIT_CONFIRMATION, getExitConfirmation() );
    adapter.preserve( PROP_RESIZE_LISTENER, Boolean.valueOf( hasResizeListener( display ) ) );
    ActiveKeysUtil.preserveActiveKeys( display );
    ActiveKeysUtil.preserveCancelKeys( display );
    ActiveKeysUtil.preserveMnemonicActivator( display );
    if( adapter.isInitialized() ) {
      for( Shell shell : getShells( display ) ) {
        WidgetTreeUtil.accept( shell, new WidgetTreeVisitor() {
          @Override
          public boolean visit( Widget widget ) {
            getLCA( widget ).preserveValues( widget );
            return true;
          }
        } );
      }
    }
  }

  public void render( Display display ) throws IOException {
    renderOverflow( display );
    renderReparentControls();
    renderDisposeWidgets();
    renderExitConfirmation( display );
    renderEnableUiTests( display );
    renderShells( display );
    renderFocus( display );
    renderBeep( display );
    renderResizeListener( display );
    renderServerPush( display );
    ActiveKeysUtil.renderActiveKeys( display );
    ActiveKeysUtil.renderCancelKeys( display );
    ActiveKeysUtil.renderMnemonicActivator( display );
    RemoteObjectLifeCycleAdapter.render();
    MeasurementUtil.renderMeasurementItems();
    runRenderRunnables( display );
    markInitialized( display );
  }

  public void clearPreserved( Display display ) {
    ( ( WidgetRemoteAdapter )getAdapter( display ) ).clearPreserved();
    for( Shell shell : getShells( display ) ) {
      WidgetTreeUtil.accept( shell, new WidgetTreeVisitor() {
        @Override
        public boolean visit( Widget widget ) {
          ( ( WidgetRemoteAdapter )getAdapter( widget ) ).clearPreserved();
          return true;
        }
      } );
    }
  }

  private static void handleOperations( Display display ) {
    ClientMessage clientMessage = ProtocolUtil.getClientMessage();
    List<Operation> operations = clientMessage .getAllOperationsFor( getId( display ) );
    if( !operations.isEmpty() ) {
      OperationHandler handler = new DisplayOperationHandler( display );
      for( Operation operation : operations ) {
        handleOperation( handler, operation );
      }
    }
  }

  private static void visitWidgets( Display display ) {
    WidgetTreeVisitor visitor = new WidgetTreeVisitor() {
      @Override
      public boolean visit( Widget widget ) {
        getLCA( widget ).readData( widget );
        return true;
      }
    };
    for( Shell shell : getShells( display ) ) {
      WidgetTreeUtil.accept( shell, visitor );
    }
  }

  private static void renderOverflow( Display display ) {
    if( !getAdapter( display ).isInitialized() ) {
      String overflow = getEntryPointProperties().get( WebClient.PAGE_OVERFLOW );
      if( overflow != null ) {
        RemoteObjectFactory.getRemoteObject( display ).set( "overflow", overflow );
      }
    }
  }

  private static Map<String, String> getEntryPointProperties() {
    ApplicationContextImpl applicationContext = ContextProvider.getApplicationContext();
    EntryPointManager entryPointManager = applicationContext.getEntryPointManager();
    String servletPath = getRequest().getServletPath();
    EntryPointRegistration registration = entryPointManager.getRegistrationByPath( servletPath );
    if( registration != null ) {
      return registration.getProperties();
    }
    return Collections.emptyMap();
  }

  private static void renderShells( Display display ) throws IOException {
    RenderVisitor visitor = new RenderVisitor();
    for( Shell shell : getShells( display ) ) {
      WidgetTreeUtil.accept( shell, visitor );
      visitor.reThrowProblem();
    }
  }

  private static void renderExitConfirmation( Display display ) {
    String exitConfirmation = getExitConfirmation();
    RemoteAdapter adapter = getAdapter( display );
    Object oldExitConfirmation = adapter.getPreserved( PROP_EXIT_CONFIRMATION );
    boolean hasChanged = exitConfirmation == null
                       ? oldExitConfirmation != null
                       : !exitConfirmation.equals( oldExitConfirmation );
    if( hasChanged ) {
      getRemoteObject( display ).set( PROP_EXIT_CONFIRMATION, exitConfirmation );
    }
  }

  private static String getExitConfirmation() {
    ExitConfirmation exitConfirmation = RWT.getClient().getService( ExitConfirmation.class );
    return exitConfirmation == null ? null : exitConfirmation.getMessage();
  }

  private static void renderReparentControls() {
    for( Control control : ReparentedControls.getAll() ) {
      if( !control.isDisposed() ) {
        getRemoteAdapter( control ).renderParent( control );
      }
    }
  }

  private static ControlRemoteAdapter getRemoteAdapter( Control control ) {
    return ( ControlRemoteAdapter )control.getAdapter( RemoteAdapter.class );
  }

  private static void renderDisposeWidgets() throws IOException {
    for( Widget widget : DisposedWidgets.getAll() ) {
      getLCA( widget ).renderDispose( widget );
    }
  }

  private static void renderFocus( Display display ) {
    if( !display.isDisposed() ) {
      IDisplayAdapter displayAdapter = getDisplayAdapter( display );
      RemoteAdapter widgetAdapter = getAdapter( display );
      Object oldValue = widgetAdapter.getPreserved( PROP_FOCUS_CONTROL );
      if(    !widgetAdapter.isInitialized()
          || oldValue != display.getFocusControl()
          || displayAdapter.isFocusInvalidated() )
      {
        // TODO [rst] Added null check as a NPE occurred in some rare cases
        Control focusControl = display.getFocusControl();
        if( focusControl != null ) {
          getRemoteObject( display ).set( PROP_FOCUS_CONTROL, getId( display.getFocusControl() ) );
        }
      }
    }
  }

  private static void renderBeep( Display display ) {
    IDisplayAdapter displayAdapter = getDisplayAdapter( display );
    if( displayAdapter.isBeepCalled() ) {
      displayAdapter.resetBeep();
      getRemoteObject( display ).call( METHOD_BEEP, null );
    }
  }

  private static void renderResizeListener( Display display ) {
    RemoteAdapter adapter = getAdapter( display );
    Boolean oldValue = ( Boolean )adapter.getPreserved( PROP_RESIZE_LISTENER );
    if( oldValue == null ) {
      oldValue = Boolean.FALSE;
    }
    Boolean newValue = Boolean.valueOf( hasResizeListener( display ) );
    if( !oldValue.equals( newValue ) ) {
      getRemoteObject( display ).listen( EVENT_RESIZE, newValue.booleanValue() );
    }
  }

  private static void renderServerPush( Display display ) {
    ServerPushRenderer serverPushRenderer = new ServerPushRenderer();
    if( display.isDisposed() ) {
      serverPushRenderer.renderActivation( false );
    } else {
      serverPushRenderer.render();
    }
  }

  private static void renderEnableUiTests( Display display ) {
    if( UITestUtil.isEnabled() ) {
      if( !getAdapter( display ).isInitialized() ) {
        RemoteObjectFactory.getRemoteObject( display ).set( "enableUiTests", true );
      }
    }
  }

  private static void runRenderRunnables( Display display ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( display );
    for( Runnable runnable : adapter.getRenderRunnables() ) {
      runnable.run();
    }
    adapter.clearRenderRunnables();
  }

  private static void markInitialized( Display display ) {
    ( ( WidgetRemoteAdapter )getAdapter( display ) ).setInitialized( true );
  }

  private static boolean hasResizeListener( Display display ) {
    return getDisplayAdapter( display ).isListening( SWT.Resize );
  }

  private static IDisplayAdapter getDisplayAdapter( Display display ) {
    return display.getAdapter( IDisplayAdapter.class );
  }

  private static Shell[] getShells( Display display ) {
    return getDisplayAdapter( display ).getShells();
  }

  private static final class RenderVisitor implements WidgetTreeVisitor {

    private IOException ioProblem;

    @Override
    public boolean visit( Widget widget ) {
      ioProblem = null;
      try {
        render( widget );
        runRenderRunnables( widget );
      } catch( IOException ioe ) {
        ioProblem = ioe;
        return false;
      }
      return true;
    }

    private void reThrowProblem() throws IOException {
      if( ioProblem != null ) {
        throw ioProblem;
      }
    }

    private static void render( Widget widget ) throws IOException {
      getLCA( widget ).render( widget );
    }

    private static void runRenderRunnables( Widget widget ) {
      WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( widget );
      for( Runnable runnable : adapter.getRenderRunnables() ) {
        runnable.run();
      }
      adapter.clearRenderRunnables();
    }
  }

}
