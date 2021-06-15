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
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 *    Rüdiger Herrmann - exception handler (bug 367773)
 ******************************************************************************/
package org.eclipse.swt.widgets;

import static org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.readPropertyValue;
import static org.eclipse.rap.rwt.remote.JsonMapping.readPoint;
import static org.eclipse.rap.rwt.remote.JsonMapping.readRectangle;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.Adaptable;
import org.eclipse.rap.rwt.application.ExceptionHandler;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.lifecycle.IUIThreadHolder;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycle;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.lifecycle.PhaseId;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServletLog;
import org.eclipse.rap.rwt.internal.theme.CssColor;
import org.eclipse.rap.rwt.internal.theme.CssImage;
import org.eclipse.rap.rwt.internal.theme.CssValue;
import org.eclipse.rap.rwt.internal.theme.SimpleSelector;
import org.eclipse.rap.rwt.internal.theme.ThemeUtil;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.events.EventList;
import org.eclipse.swt.internal.events.EventUtil;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.IdGenerator;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.internal.widgets.WidgetTreeUtil;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;


/**
 * Instances of this class are responsible for managing the
 * connection between SWT and the underlying operating
 * system. Their most important function is to implement
 * the SWT event loop in terms of the platform event model.
 * They also provide various methods for accessing information
 * about the operating system, and have overall control over
 * the operating system resources which SWT allocates.
 * <p>
 * Applications which are built with SWT will <em>almost always</em>
 * require only a single display. In particular, some platforms
 * which SWT supports will not allow more than one <em>active</em>
 * display. In other words, some platforms do not support
 * creating a new display if one already exists that has not been
 * sent the <code>dispose()</code> message.
 * <p>
 * In SWT, the thread which creates a <code>Display</code>
 * instance is distinguished as the <em>user-interface thread</em>
 * for that display.
 * </p>
 * The user-interface thread for a particular display has the
 * following special attributes:
 * <ul>
 * <li>
 * The event loop for that display must be run from the thread.
 * </li>
 * <li>
 * Some SWT API methods (notably, most of the public methods in
 * <code>Widget</code> and its subclasses), may only be called
 * from the thread. (To support multi-threaded user-interface
 * applications, class <code>Display</code> provides inter-thread
 * communication methods which allow threads other than the
 * user-interface thread to request that it perform operations
 * on their behalf.)
 * </li>
 * <li>
 * The thread is not allowed to construct other
 * <code>Display</code>s until that display has been disposed.
 * (Note that, this is in addition to the restriction mentioned
 * above concerning platform support for multiple displays. Thus,
 * the only way to have multiple simultaneously active displays,
 * even on platforms which support it, is to have multiple threads.)
 * </li>
 * </ul>
 * Enforcing these attributes allows SWT to be implemented directly
 * on the underlying operating system's event model. This has
 * numerous benefits including smaller footprint, better use of
 * resources, safer memory management, clearer program logic,
 * better performance, and fewer overall operating system threads
 * required. The down side however, is that care must be taken
 * (only) when constructing multi-threaded applications to use the
 * inter-thread communication mechanisms which this class provides
 * when required.
 * </p><p>
 * All SWT API methods which may only be called from the user-interface
 * thread are distinguished in their documentation by indicating that
 * they throw the "<code>ERROR_THREAD_INVALID_ACCESS</code>"
 * SWT exception.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Close, Dispose, Skin</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 *
 * @see #readAndDispatch
 * @see #sleep
 * @see Device#dispose
 *
 * @since 1.0
 */
@SuppressWarnings( "deprecation" )
public class Display extends Device implements Adaptable {

  private final static String BOUNDS = "bounds";
  private final static String DPI = "dpi";
  private final static String COLOR_DEPTH = "colorDepth";
  private static final String ATTR_INVALIDATE_FOCUS
    = DisplayAdapter.class.getName() + "#invalidateFocus";
  private static final String APP_NAME = Display.class.getName() + "#appName";
  private static final String APP_VERSION = Display.class.getName() + "#appVersion";
  private static final int DOUBLE_CLICK_TIME = 500; // Keep in sync with client-side (EventUtil.js)
  private static final int GROW_SIZE = 1024;

  static final String PACKAGE_PREFIX = "org.eclipse.swt.widgets.";

  private static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new ExceptionHandler() {
    @Override
    public void handleException( Throwable throwable ) {
      if( throwable instanceof RuntimeException ) {
        throw ( RuntimeException )throwable;
      }
      if( throwable instanceof Error ) {
        throw ( Error )throwable;
      }
    }
  };

  /**
   * Returns the display which the currently running thread is
   * the user-interface thread for, or null if the currently
   * running thread is not a user-interface thread for any display.
   *
   * @return the current display
   */
  public static Display getCurrent() {
    Display result = LifeCycleUtil.getSessionDisplay();
    if( result != null ) {
      if( result.isDisposed() || result.getThread() != Thread.currentThread() ) {
        result = null;
      }
    }
    return result;
  }

  /**
   * Returns the default display. One is created if it did not already exist.
   *
   * <p><strong>Note:</strong> In RWT, a new display is only created if the
   * calling thread is the user-interface thread.
   * </p>
   *
   * @return the default display
   */
  public static Display getDefault() {
    Display display = LifeCycleUtil.getSessionDisplay();
    if( display == null || display.isDisposed() ) {
      if( isUIThread() ) {
        display = new Display();
      }
    }
    return display;
  }

  private static boolean isUIThread() {
    boolean result = false;
    if( ContextProvider.hasContext() ) {
      IUIThreadHolder uiThreadHolder = LifeCycleUtil.getUIThread( ContextProvider.getUISession() );
      Thread uiThread = uiThreadHolder == null ? null : uiThreadHolder.getThread();
      result = uiThread == Thread.currentThread();
    }
    return result;
  }

  private final List<Shell> shells;
  private transient Thread thread;
  private final UISession uiSession;
  private final Rectangle bounds;
  private final Point dpi;
  private final int depth;
  private final Point cursorLocation;
  private Shell activeShell;
  private Collection<Control> redrawControls;
  private Control focusControl;
  private EventTable filterTable;
  private EventTable eventTable;
  private transient Monitor monitor;
  private transient IDisplayAdapter displayAdapter;
  private WidgetRemoteAdapter remoteAdapter;
  private Runnable[] disposeList;
  private Composite[] layoutDeferred;
  private int layoutDeferredCount;
  private Widget[] skinList;
  private int skinCount;
  private boolean beep;

  /* Display Data */
  private Object data;
  private String[] keys;
  private Object[] values;

  private Synchronizer synchronizer;
  private TimerExecScheduler scheduler;

  /**
   * Constructs a new instance of this class.
   * <p>
   * Note: The resulting display is marked as the <em>current</em>
   * display. If this is the first display which has been
   * constructed since the application started, it is also
   * marked as the <em>default</em> display.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if called from a thread that already created an existing display</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see #getCurrent
   * @see #getDefault
   * @see Widget#checkSubclass
   * @see Shell
   */
  public Display() {
    if( getCurrent() != null ) {
      SWT.error( SWT.ERROR_NOT_IMPLEMENTED, null, " [multiple displays]" );
    }
    LifeCycleUtil.setSessionDisplay( this );
    attachThread();
    uiSession = ContextProvider.getUISession();
    shells = new ArrayList<>();
    monitor = new Monitor( this );
    cursorLocation = new Point( 0, 0 );
    bounds = readInitialBounds();
    dpi = readDPI();
    depth = readDepth();
    synchronizer = new Synchronizer( this );
    register();
  }

  /**
   * Returns a rectangle describing the receiver's size and location.
   *
   * @return the bounding rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  @Override
  public Rectangle getBounds() {
    checkDevice();
    return new Rectangle( bounds.x, bounds.y, bounds.width, bounds.height );
  }

  @Override
  public Point getDPI() {
    checkDevice();
    return new Point( dpi.x, dpi.y );
  }

  @Override
  public int getDepth() {
    checkDevice();
    return depth;
  }

  /**
   * Returns the control which currently has keyboard focus,
   * or null if keyboard events are not currently going to
   * any of the controls built by the currently running
   * application.
   *
   * @return the control under the cursor
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Control getFocusControl() {
    checkDevice();
    return focusControl;
  }

  private void setFocusControl( Control focusControl, boolean fireEvents ) {
    if( this.focusControl != focusControl ) {
      if( this.focusControl != null && !this.focusControl.isInDispose() ) {
        Control currentFocusControl = this.focusControl;
        Shell shell = currentFocusControl.getShell();
        if( fireEvents ) {
          currentFocusControl.notifyListeners( SWT.FocusOut, new Event() );
        }
        shell.updateDefaultButton( currentFocusControl, false );
      }
      this.focusControl = focusControl;
      if( this.focusControl != null ) {
        Control currentFocusControl = this.focusControl;
        Shell shell = currentFocusControl.getShell();
        if( fireEvents ) {
          currentFocusControl.notifyListeners( SWT.FocusIn, new Event() );
        }
        shell.updateDefaultButton( currentFocusControl, true );
      }
    }
  }

  /////////////////////
  // Coordinate mapping

  /**
   * Maps a point from one coordinate system to another.
   * When the control is null, coordinates are mapped to
   * the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate
   * systems are mirrored, special care needs to be taken
   * when mapping coordinates from one control to another
   * to ensure the result is correctly mirrored.
   *
   * Mapping a point that is the origin of a rectangle and
   * then adding the width and height is not equivalent to
   * mapping the rectangle.  When one control is mirrored
   * and the other is not, adding the width and height to a
   * point that was mapped causes the rectangle to extend
   * in the wrong direction.  Mapping the entire rectangle
   * instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   *
   * @param from the source <code>Control</code> or <code>null</code>
   * @param to the destination <code>Control</code> or <code>null</code>
   * @param point to be mapped
   * @return point with mapped coordinates
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Point map( Control from, Control to, Point point ) {
    checkDevice();
    if( point == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return map( from, to, point.x, point.y );
  }

  /**
   * Maps a point from one coordinate system to another.
   * When the control is null, coordinates are mapped to
   * the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate
   * systems are mirrored, special care needs to be taken
   * when mapping coordinates from one control to another
   * to ensure the result is correctly mirrored.
   *
   * Mapping a point that is the origin of a rectangle and
   * then adding the width and height is not equivalent to
   * mapping the rectangle.  When one control is mirrored
   * and the other is not, adding the width and height to a
   * point that was mapped causes the rectangle to extend
   * in the wrong direction.  Mapping the entire rectangle
   * instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   *
   * @param from the source <code>Control</code> or <code>null</code>
   * @param to the destination <code>Control</code> or <code>null</code>
   * @param x coordinates to be mapped
   * @param y coordinates to be mapped
   * @return point with mapped coordinates
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Point map( Control from, Control to, int x, int y ) {
    checkDevice();
    Rectangle rectangle = map( from, to, x, y, 0, 0 );
    return new Point( rectangle.x, rectangle.y );
  }

  /**
   * Maps a point from one coordinate system to another.
   * When the control is null, coordinates are mapped to
   * the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate
   * systems are mirrored, special care needs to be taken
   * when mapping coordinates from one control to another
   * to ensure the result is correctly mirrored.
   *
   * Mapping a point that is the origin of a rectangle and
   * then adding the width and height is not equivalent to
   * mapping the rectangle.  When one control is mirrored
   * and the other is not, adding the width and height to a
   * point that was mapped causes the rectangle to extend
   * in the wrong direction.  Mapping the entire rectangle
   * instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   *
   * @param from the source <code>Control</code> or <code>null</code>
   * @param to the destination <code>Control</code> or <code>null</code>
   * @param rectangle to be mapped
   * @return rectangle with mapped coordinates
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Rectangle map( Control from, Control to, Rectangle rectangle ) {
    checkDevice();
    if( rectangle == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return map( from, to, rectangle.x, rectangle.y, rectangle.width, rectangle.height );
  }

  /**
   * Maps a point from one coordinate system to another.
   * When the control is null, coordinates are mapped to
   * the display.
   * <p>
   * NOTE: On right-to-left platforms where the coordinate
   * systems are mirrored, special care needs to be taken
   * when mapping coordinates from one control to another
   * to ensure the result is correctly mirrored.
   *
   * Mapping a point that is the origin of a rectangle and
   * then adding the width and height is not equivalent to
   * mapping the rectangle.  When one control is mirrored
   * and the other is not, adding the width and height to a
   * point that was mapped causes the rectangle to extend
   * in the wrong direction.  Mapping the entire rectangle
   * instead of just one point causes both the origin and
   * the corner of the rectangle to be mapped.
   * </p>
   *
   * @param from the source <code>Control</code> or <code>null</code>
   * @param to the destination <code>Control</code> or <code>null</code>
   * @param x coordinates to be mapped
   * @param y coordinates to be mapped
   * @param width coordinates to be mapped
   * @param height coordinates to be mapped
   * @return rectangle with mapped coordinates
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Rectangle map( Control from, Control to, int x, int y, int width, int height ) {
    checkDevice();
    int newX = x;
    int newY = y;
    if( from != null ) {
      Point fromOrigin = getAbsoluteOrigin( from );
      newX = fromOrigin.x + applyMirror( from, newX, width );
      newY = fromOrigin.y + newY;
    }
    if( to != null ) {
      Point toOrigin = getAbsoluteOrigin( to );
      newX = applyMirror( to, newX - toOrigin.x, width );
      newY = newY - toOrigin.y;
    }
    return new Rectangle( newX, newY, width, height );
  }

  /*
   * Returns the origin of the coordinate system of a given control in absolute
   * coordinates, i.e. relative to the display.
   */
  private static Point getAbsoluteOrigin( Control control ) {
    Control currentControl = control;
    Point absolute = new Point( 0, 0 );
    while( currentControl != null ) {
      Point origin = getOrigin( currentControl );
      absolute.x += applyMirror( currentControl.getParent(), origin.x, currentControl.getSize().x );
      absolute.y += origin.y;
      if( currentControl instanceof Shell ) {
        currentControl = null;
      } else {
        currentControl = currentControl.getParent();
      }
    }
    return new Point( absolute.x, absolute.y );
  }

  /*
   * Returns the origin of the coordinate system of a given control, relative to
   * it's parent or, if it does not have a parent, relative to the display.
   */
  private static Point getOrigin( Control control ) {
    Point result = control.getLocation();
    // Due the way that the qx client implementation works, the coordinate
    // system of composites starts at the inner edge of their border and thus
    // need to be offset by the border width.
    // Since only composites can contain child widgets, only they need this
    // correction. This implementation seems to be a good fit with SWT.
    if( control instanceof Composite ) {
      boolean rtl = control.getOrientation() == SWT.RIGHT_TO_LEFT;
      BoxDimensions borderWidth = control.getBorder();
      result.x += rtl ? borderWidth.right : borderWidth.left;
      result.y += borderWidth.top;
    }
    return result;
  }

  private static int applyMirror( Control control, int x, int width ) {
    if( control != null && control.getOrientation() == SWT.RIGHT_TO_LEFT ) {
      BoxDimensions border = control.getBorder();
      int innerWidth = control.getSize().x - border.left - border.right;
      return innerWidth - x - width;
    }
    return x;
  }

  ///////////
  // Listener

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when an event of the given type occurs. The event
   * type is one of the event constants defined in class <code>SWT</code>.
   * When the event does occur in the display, the listener is notified by
   * sending it the <code>handleEvent()</code> message.
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should be notified when the event occurs
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see Listener
   * @see SWT
   * @see #removeListener
   *
   * @since 1.3
   */
  public void addListener( int eventType, Listener listener ) {
    checkDevice();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( eventTable == null ) {
      eventTable = new EventTable();
    }
    eventTable.hook( eventType, listener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when an event of the given type occurs. The event type
   * is one of the event constants defined in class <code>SWT</code>.
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see Listener
   * @see SWT
   * @see #addListener
   *
   * @since 1.3
   */
  public void removeListener( int eventType, Listener listener ) {
    checkDevice();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( eventTable != null ) {
      eventTable.unhook( eventType, listener );
    }
  }

  void sendEvent( int eventType, Event event ) {
    event.display = this;
    event.type = eventType;
    if( event.time == 0 ) {
      event.time = EventUtil.getLastEventTime();
    }
    filterEvent( event );
    if( eventTable != null ) {
      eventTable.sendEvent( event );
    }
  }

  private void notifyListeners( final int eventType, final Event event ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        sendEvent( eventType, event );
      }
    } );
  }

  private boolean isListening( int eventType ) {
    return eventTable == null ? false : eventTable.hooks( eventType );
  }

  //////////
  // Dispose

  /**
   * Causes the <code>run()</code> method of the runnable to
   * be invoked by the user-interface thread just before the
   * receiver is disposed.  Specifying a <code>null</code> runnable
   * is ignored.
   *
   * @param runnable code to run at dispose time.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void disposeExec( Runnable runnable ) {
    checkDevice();
    if (disposeList == null) {
      disposeList = new Runnable [4];
    }
    for (int i=0; i<disposeList.length; i++) {
      if (disposeList [i] == null) {
        disposeList [i] = runnable;
        return;
      }
    }
    Runnable [] newDisposeList = new Runnable [disposeList.length + 4];
    System.arraycopy (disposeList, 0, newDisposeList, 0, disposeList.length);
    newDisposeList [disposeList.length] = runnable;
    disposeList = newDisposeList;
  }

  /**
   * Requests that the connection between SWT and the underlying
   * operating system be closed.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see Device#dispose
   *
   * @since 1.3
   */
  public void close() {
    checkDevice();
    Event event = new Event();
    sendEvent( SWT.Close, event );
    if( event.doit ) {
      dispose();
    }
  }

  @Override
  protected void release() {
    sendDisposeEvent();
    disposeShells();
    runDisposeExecs();
    synchronizer.releaseSynchronizer();
    if( scheduler != null ) {
      scheduler.dispose();
    }
    filterTable = null;
    eventTable = null;
  }

  @Override
  protected void destroy() {
    deregister();
  }

  private void sendDisposeEvent() {
    sendEvent( SWT.Dispose, new Event() );
  }

  private void disposeShells() {
    Shell[] shells = getShells();
    for( int i = 0; i < shells.length; i++ ) {
      Shell shell = shells[ i ];
      try {
        shell.dispose();
      } catch( Throwable thr ) {
        ServletLog.log( "Exception while disposing shell: " + shell, thr );
      }
    }
    // TODO [rh] consider dispatching pending messages (e.g. asyncExec)
    //      while( readAndDispatch() ) {}
  }

  private void runDisposeExecs() {
    checkDevice();
    if( disposeList != null ) {
      for( int i = 0; i < disposeList.length; i++ ) {
        if( disposeList[ i ] != null ) {
          try {
            disposeList[ i ].run();
          } catch( Throwable thr ) {
            String msg = "Exception while executing dispose-runnable.";
            ServletLog.log( msg, thr );
          }
        }
      }
    }
  }

  /////////////////////
  // Adaptable override

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IDisplayAdapter.class ) {
      if( displayAdapter == null ) {
        displayAdapter = new DisplayAdapter();
      }
      return ( T )displayAdapter;
    }
    if( adapter == RemoteAdapter.class ) {
      if( remoteAdapter == null ) {
        String id = IdGenerator.getInstance( uiSession ).createId( this );
        remoteAdapter = new WidgetRemoteAdapter( id );
      }
      return ( T )remoteAdapter;
    }
    return null;
  }

  private ApplicationContextImpl getApplicationContext() {
    return ( ApplicationContextImpl )uiSession.getApplicationContext();
  }

  ///////////////////
  // Shell management

  /**
   * Returns a (possibly empty) array containing all shells which have
   * not been disposed and have the receiver as their display.
   *
   * @return the receiver's shells
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Shell[] getShells() {
    checkDevice();
    Shell[] result = new Shell[ shells.size() ];
    shells.toArray( result );
    return result;
  }

  /**
   * Returns the currently active <code>Shell</code>, or null
   * if no shell belonging to the currently running application
   * is active.
   *
   * @return the active shell or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Shell getActiveShell() {
    checkDevice();
    return activeShell;
  }

  final void setActiveShell( Shell activeShell ) {
    checkDevice();
    if( this.activeShell != activeShell ) {
      Shell lastActiveShell = this.activeShell;
      if( this.activeShell != null ) {
        this.activeShell.saveFocus();
      }
      // Move active shell to end of list to maintain correct z-order
      if( activeShell != null ) {
        shells.remove( activeShell );
        shells.add( activeShell );
      }
      if( lastActiveShell != null && !lastActiveShell.hasState( Widget.DISPOSE_SENT ) ) {
        lastActiveShell.notifyListeners( SWT.Deactivate, new Event() );
      }
      this.activeShell = activeShell;
      if( activeShell != null ) {
        activeShell.notifyListeners( SWT.Activate, new Event() );
      }
      if( this.activeShell != null ) {
        this.activeShell.restoreFocus();
      }
    }
  }

  final void addShell( Shell shell ) {
    shells.add( shell );
  }

  final void removeShell( Shell shell ) {
    shells.remove( shell );
    if( shell == activeShell ) {
      int counter = 0;
      Shell lastVisibleShell = null;
      while( lastVisibleShell == null && counter < shells.size() ) {
        counter++;
        Shell currentShell = shells.get( shells.size() - counter );
        if( currentShell.isVisible() ) {
          lastVisibleShell = currentShell;
        }
      }
      setActiveShell( lastVisibleShell );
    }
  }

  ////////////////////
  // Thread management

  /**
   * Returns the user-interface thread for the receiver. Note that the
   * user-interface thread may change per user-request.
   *
   * @return the receiver's user-interface thread or null if there's no
   *         current user-request executed that belongs to the display.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Thread getThread() {
    synchronized( deviceLock ) {
      if( isDisposed() ) {
        error( SWT.ERROR_DEVICE_DISPOSED );
      }
      return thread;
    }
  }

  private void attachThread() {
    thread = Thread.currentThread();
  }

  private void detachThread() {
    thread = null;
  }

  //////////////////////
  // Information methods

  /**
   * Returns the display which the given thread is the
   * user-interface thread for, or null if the given thread
   * is not a user-interface thread for any display.  Specifying
   * <code>null</code> as the thread will return <code>null</code>
   * for the display.
   *
   * @param thread the user-interface thread
   * @return the display for the given thread
   */
  public static Display findDisplay( Thread thread ) {
    synchronized( Device.class ) {
      for( WeakReference<Display> displayRef : getDisplays() ) {
        if( displayRef != null ) {
          Display display = displayRef.get();
          if( display != null && !display.isDisposed() && display.thread == thread ) {
            return display;
          }
        }
      }
      return null;
    }
  }

  /**
   * Sets the synchronizer used by the display to be
   * the argument, which can not be null.
   *
   * @param synchronizer the new synchronizer for the display (must not be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the synchronizer is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread message</li>
   * </ul>
   *
   * @since 1.3
   */
  // verbatim copy of SWT code
  public void setSynchronizer (Synchronizer synchronizer) {
    checkDevice ();
    if (synchronizer == null) {
      error (SWT.ERROR_NULL_ARGUMENT);
    }
    if (synchronizer == this.synchronizer) {
      return;
    }
    Synchronizer oldSynchronizer;
    synchronized (deviceLock) {
      oldSynchronizer = this.synchronizer;
      this.synchronizer = synchronizer;
    }
    if (oldSynchronizer != null) {
      oldSynchronizer.runAsyncMessages(true);
    }
  }

  /**
   * Gets the synchronizer used by the display.
   *
   * @return the receiver's synchronizer
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public Synchronizer getSynchronizer() {
    checkDevice ();
    return synchronizer;
  }

  /**
   * Returns the thread that has invoked <code>syncExec</code>
   * or null if no such runnable is currently being invoked by
   * the user-interface thread.
   * <p>
   * Note: If a runnable invoked by asyncExec is currently
   * running, this method will return null.
   * </p>
   *
   * @return the receiver's sync-interface thread
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public Thread getSyncThread () {
    synchronized( deviceLock ) {
      if( isDisposed() ) {
        error( SWT.ERROR_DEVICE_DISPOSED );
      }
      return synchronizer.syncThread;
    }
  }

  /**
   * Causes the <code>run()</code> method of the runnable to
   * be invoked by the user-interface thread at the next
   * reasonable opportunity. Note that the user-interface thread may change
   * per user-request. The caller of this method continues
   * to run in parallel, and is not notified when the
   * runnable has completed.  Specifying <code>null</code> as the
   * runnable simply wakes the user-interface thread when run.
   * <p>
   * Note that at the time the runnable is invoked, widgets
   * that have the receiver as their display may have been
   * disposed. Therefore, it is necessary to check for this
   * case inside the runnable before accessing the widget.
   * </p>
   *
   * @param runnable code to run on the user-interface thread or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #syncExec
   */
  public void asyncExec( Runnable runnable ) {
    synchronized( deviceLock ) {
      if( isDisposed() ) {
        error( SWT.ERROR_DEVICE_DISPOSED );
      }
      synchronizer.asyncExec( runnable );
    }
  }

  /**
   * Causes the <code>run()</code> method of the runnable to
   * be invoked by the user-interface thread at the next
   * reasonable opportunity. Note that the user-interface thread may change
   * per user-request. The thread which calls this method
   * is suspended until the runnable completes.  Specifying <code>null</code>
   * as the runnable simply wakes the user-interface thread.
   * <p>
   * Note that at the time the runnable is invoked, widgets
   * that have the receiver as their display may have been
   * disposed. Therefore, it is necessary to check for this
   * case inside the runnable before accessing the widget.
   * </p>
   *
   * @param runnable code to run on the user-interface thread or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_FAILED_EXEC - if an exception occured when executing the runnable</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #asyncExec
   */
  public void syncExec( Runnable runnable ) {
    Synchronizer synchronizer;
    synchronized( deviceLock ) {
      if( isDisposed() ) {
        error( SWT.ERROR_DEVICE_DISPOSED );
      }
      synchronizer = this.synchronizer;
    }
    synchronizer.syncExec( runnable );
  }

  /**
   * Causes the <code>run()</code> method of the runnable to
   * be invoked by the user-interface thread after the specified
   * number of milliseconds have elapsed. If milliseconds is less
   * than zero, the runnable is not executed.
   * <p>
   * Note that at the time the runnable is invoked, widgets
   * that have the receiver as their display may have been
   * disposed. Therefore, it is necessary to check for this
   * case inside the runnable before accessing the widget.
   * </p>
   *
   * @param milliseconds the delay before running the runnable
   * @param runnable code to run on the user-interface thread
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #asyncExec
   * @since 1.2
   */
  public void timerExec( int milliseconds, Runnable runnable ) {
    checkDevice();
    if( runnable == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( scheduler == null ) {
      scheduler = createTimerExecScheduler();
    }
    if( milliseconds < 0 ) {
      scheduler.cancel( runnable );
    } else {
      scheduler.schedule( milliseconds, runnable );
    }
  }

  TimerExecScheduler createTimerExecScheduler() {
    return new TimerExecScheduler( this );
  }

  /**
   * Reads an event from the <!-- operating system's --> event queue,
   * dispatches it appropriately, and returns <code>true</code>
   * if there is potentially more work to do, or <code>false</code>
   * if the caller can sleep until another event is placed on
   * the event queue.
   * <p>
   * In addition to checking the system event queue, this method also
   * checks if any inter-thread messages (created by <code>syncExec()</code>
   * or <code>asyncExec()</code>) are waiting to be processed, and if
   * so handles them before returning.
   * </p>
   *
   * @return <code>false</code> if the caller can sleep upon return from this method
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread message</li>
   * </ul>
   *
   * @see #sleep
   * @see #wake
   *
   * @since 1.1
   */
  public boolean readAndDispatch() {
    checkDevice();
    return safeReadAndDispatch();
  }

  private boolean safeReadAndDispatch() {
    boolean result = false;
    try {
      runSkin();
      runDeferredLayouts();
      result = runPendingMessages();
    } catch( RuntimeException runtimeException ) {
      handleException( runtimeException );
    } catch( Error error ) {
      handleException( error );
      throw error;
    }
    return result;
  }

  private boolean runPendingMessages() {
    boolean result = false;
    if(    PhaseId.PREPARE_UI_ROOT.equals( CurrentPhase.get() )
        || PhaseId.PROCESS_ACTION.equals( CurrentPhase.get() ) )
    {
      result = ProcessActionRunner.executeNext();
      if( !result ) {
        result = executeNextEvent();
      }
      if( !result ) {
        result = synchronizer.runAsyncMessages( false );
      }
      if( !result ) {
        result = executeNextRedraw();
      }
    }
    return result;
  }

  private static boolean executeNextEvent() {
    boolean result = false;
    Event[] events = EventList.getInstance().getAll();
    while( !result && events.length > 0 ) {
      Event event = events[ 0 ];
      EventList.getInstance().remove( event );
      if( EventUtil.allowProcessing( event ) ) {
        event.widget.notifyListeners( event.type, event );
        result = true;
      } else {
        events = EventList.getInstance().getAll();
      }
    }
    return result;
  }

  private void handleException( Throwable throwable ) {
    ExceptionHandler exceptionHandler = getExceptionHandler();
    exceptionHandler.handleException( throwable );
  }

  private ExceptionHandler getExceptionHandler() {
    ExceptionHandler result = getApplicationContext().getExceptionHandler();
    if( result == null ) {
      result = DEFAULT_EXCEPTION_HANDLER;
    }
    return result;
  }

  /**
   * Causes the user-interface thread to <em>sleep</em> (that is,
   * to be put in a state where it does not consume CPU cycles)
   * until an event is received or it is otherwise awakened.
   *
   * @return <code>true</code> if an event requiring dispatching was placed on the queue.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #wake
   *
   * @since 1.1
   */
  public boolean sleep() {
    checkDevice();
    LifeCycle lifeCycle = getApplicationContext().getLifeCycleFactory().getLifeCycle();
    lifeCycle.sleep();
    // return true as we cannot reliably determinate what actually caused
    // lifeCycle#sleep() to return
    return true;
  }

  /**
   * Notifies the client to send a request in order to wake up a sleeping UI thread and to perform
   * pending UI updates. Note that this method may be called from any thread.
   * <p>
   * Note that this method has no effect unless a {@link ServerPushSession} is active.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void wake() {
    synchronized( deviceLock ) {
      if( isDisposed() ) {
        error( SWT.ERROR_DEVICE_DISPOSED );
      }
      if( thread != Thread.currentThread() ) {
        uiSession.exec( new Runnable() {
          @Override
          public void run() {
            synchronizer.asyncExec( new WakeRunnable() );
          }
        } );
      }
    }
  }

  protected void wakeThread() {
    uiSession.exec( new Runnable() {
      @Override
      public void run() {
        ServerPushManager.getInstance().wakeClient();
      }
    } );
  }

  Object getDeviceLock() {
    return deviceLock;
  }

  //////////
  // Redraw

  void redrawControl( Control control, boolean redraw ) {
    if( redraw ) {
      if( redrawControls == null ) {
        redrawControls = new LinkedList<>();
      }
      if( !redrawControls.contains( control ) ) {
        redrawControls.add( control );
      }
    } else {
      if( redrawControls != null ) {
        redrawControls.remove( control );
      }
    }
  }

  boolean needsRedraw( Control control ) {
    return redrawControls != null && redrawControls.contains( control );
  }

  private boolean executeNextRedraw() {
    boolean result = false;
    if( redrawControls != null ) {
      Iterator<Control> iterator = redrawControls.iterator();
      if( iterator.hasNext() ) {
        Control control = iterator.next();
        WidgetUtil.getLCA( control ).doRedrawFake( control );
        redrawControls.remove( control );
        result = true;
      }
    }
    return result;
  }

  //////////////////////
  // Information methods

  /**
   * Returns the single instance of the system tray or null when there is no
   * system tray available for the platform.
   *
   * @return the system tray or <code>null</code>
   * @exception SWTException <ul>
   *              <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * @since 1.4
   */
  public Tray getSystemTray() {
    checkDevice();
    return null;
  }

  /**
   * Returns the single instance of the application menu bar or null
   * when there is no application menu bar for the platform.
   *
   * @return the application menu bar or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.4
   */
  public Menu getMenuBar() {
    checkDevice();
    return null;
  }

  /**
   * Returns the single instance of the system taskBar or null
   * when there is no system taskBar available for the platform.
   *
   * @return the system taskBar or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.4
   */
  public TaskBar getSystemTaskBar() {
    checkDevice();
    return null;
  }

  /**
   * Returns the single instance of the system-provided menu for the application.
   * On platforms where no menu is provided for the application this method returns null.
   *
   * @return the system menu or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.4
   */
  public Menu getSystemMenu() {
    checkDevice();
    return null;
  }

  @Override
  public Color getSystemColor( int id ) {
    checkDevice();
    Color result = null;
    CssValue value = null;
    switch( id ) {
      case SWT.COLOR_WIDGET_DARK_SHADOW:
        value = ThemeUtil.getCssValue( "Display",
                                       "rwt-darkshadow-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_WIDGET_NORMAL_SHADOW:
        value = ThemeUtil.getCssValue( "Display",
                                       "rwt-shadow-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_WIDGET_LIGHT_SHADOW:
        value = ThemeUtil.getCssValue( "Display",
                                       "rwt-lightshadow-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW:
        value = ThemeUtil.getCssValue( "Display",
                                       "rwt-highlight-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_WIDGET_BORDER:
        value = ThemeUtil.getCssValue( "Display",
                                       "rwt-thinborder-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_WIDGET_BACKGROUND:
        // TODO [rst] Revise element name
        value = ThemeUtil.getCssValue( "NONE",
                                       "background-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_WIDGET_FOREGROUND:
        // TODO [rst] Revise element name
        value = ThemeUtil.getCssValue( "NONE", "color", SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_LIST_FOREGROUND:
        value = ThemeUtil.getCssValue( "List", "color", SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_LIST_BACKGROUND:
        value = ThemeUtil.getCssValue( "List",
                                       "background-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_LIST_SELECTION:
        value = ThemeUtil.getCssValue( "List-Item",
                                       "background-color",
                                       SimpleSelector.SELECTED );
      break;
      case SWT.COLOR_LIST_SELECTION_TEXT:
        value = ThemeUtil.getCssValue( "List-Item",
                                       "color",
                                       SimpleSelector.SELECTED );
      break;
      case SWT.COLOR_INFO_FOREGROUND:
        value = ThemeUtil.getCssValue( "Widget-ToolTip",
                                       "color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_INFO_BACKGROUND:
        value = ThemeUtil.getCssValue( "Display",
                                       "rwt-infobackground-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_TITLE_FOREGROUND:
        value = ThemeUtil.getCssValue( "Shell-Titlebar",
                                       "color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_TITLE_INACTIVE_FOREGROUND:
        value = ThemeUtil.getCssValue( "Shell-Titlebar",
                                       "color",
                                       SimpleSelector.INACTIVE );
      break;
      case SWT.COLOR_TITLE_BACKGROUND:
        value = ThemeUtil.getCssValue( "Shell-Titlebar",
                                       "background-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_TITLE_INACTIVE_BACKGROUND:
        value = ThemeUtil.getCssValue( "Shell-Titlebar",
                                       "background-color",
                                       SimpleSelector.INACTIVE );
      break;
      case SWT.COLOR_TITLE_BACKGROUND_GRADIENT:
        value = ThemeUtil.getCssValue( "Shell-Titlebar",
                                       "background-gradient-color",
                                       SimpleSelector.DEFAULT );
      break;
      case SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT:
        value = ThemeUtil.getCssValue( "Shell-Titlebar",
                                       "background-gradient-color",
                                       SimpleSelector.INACTIVE );
      break;
      default:
        result = super.getSystemColor( id );
    }
    if( value != null ) {
      result = CssColor.createColor( ( CssColor )value );
      if( result == null ) {
        // TODO [rst] Revise: theming must prevent transparency for system colors
        throw new IllegalArgumentException( "Transparent system color" );
      }
    }
    return result;
  }

  /**
   * Returns the matching standard platform image for the given
   * constant, which should be one of the icon constants
   * specified in class <code>SWT</code>. This image should
   * not be free'd because it was allocated by the system,
   * not the application.  A value of <code>null</code> will
   * be returned either if the supplied constant is not an
   * SWT icon constant or if the platform does not define an
   * image that corresponds to the constant.
   *
   * @param id the SWT icon constant
   * @return the corresponding image or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see SWT#ICON_ERROR
   * @see SWT#ICON_INFORMATION
   * @see SWT#ICON_QUESTION
   * @see SWT#ICON_WARNING
   * @see SWT#ICON_WORKING
   */
  public Image getSystemImage( int id ) {
    checkDevice();
    Image result = null;
    switch( id ) {
      case SWT.ICON_ERROR:
        result = createSystemImage( "rwt-error-image" );
      break;
      case SWT.ICON_INFORMATION:
        result = createSystemImage( "rwt-information-image" );
      break;
      case SWT.ICON_QUESTION:
        result = createSystemImage( "rwt-question-image" );
      break;
      case SWT.ICON_WARNING:
        result = createSystemImage( "rwt-warning-image" );
      break;
      case SWT.ICON_WORKING:
        result = createSystemImage( "rwt-working-image" );
      break;
    }
    return result;
  }

  private static Image createSystemImage( String cssProperty ) {
    Image result = null;
    CssValue cssValue = ThemeUtil.getCssValue( "Display", cssProperty, SimpleSelector.DEFAULT );
    if( cssValue != null ) {
      try {
        result = CssImage.createSwtImage( ( CssImage )cssValue );
      } catch( IOException ioe ) {
        throw new RuntimeException( "Could not read system image", ioe );
      }
    }
    return result;
  }

  /**
   * Returns the matching standard platform cursor for the given
   * constant, which should be one of the cursor constants
   * specified in class <code>SWT</code>. This cursor should
   * not be free'd because it was allocated by the system,
   * not the application.  A value of <code>null</code> will
   * be returned if the supplied constant is not an SWT cursor
   * constant.
   *
   * @param id the SWT cursor constant
   * @return the corresponding cursor or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see SWT#CURSOR_ARROW
   * @see SWT#CURSOR_WAIT
   * @see SWT#CURSOR_CROSS
   * @see SWT#CURSOR_HELP
   * @see SWT#CURSOR_SIZEALL
   * @see SWT#CURSOR_SIZENS
   * @see SWT#CURSOR_SIZEWE
   * @see SWT#CURSOR_SIZEN
   * @see SWT#CURSOR_SIZES
   * @see SWT#CURSOR_SIZEE
   * @see SWT#CURSOR_SIZEW
   * @see SWT#CURSOR_SIZENE
   * @see SWT#CURSOR_SIZESE
   * @see SWT#CURSOR_SIZESW
   * @see SWT#CURSOR_SIZENW
   * @see SWT#CURSOR_IBEAM
   * @see SWT#CURSOR_HAND
   *
   * @since 1.3
   */
  public Cursor getSystemCursor( int id ) {
    checkDevice();
    return getApplicationContext().getResourceFactory().getCursor( id );
  }

  /**
   * Returns the longest duration, in milliseconds, between
   * two mouse button clicks that will be considered a
   * <em>double click</em> <!-- by the underlying operating system -->.
   *
   * @return the double click time
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.2
   */
  public int getDoubleClickTime() {
    checkDevice();
    return DOUBLE_CLICK_TIME;
  }

  /**
   * Returns the control which the on-screen pointer is currently
   * over top of, or null if it is not currently over one of the
   * controls built by the currently running application.
   *
 * @return the control under the cursor or <code>null</code>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public Control getCursorControl () {
    checkDevice();
    ControlFinder finder = new ControlFinder( this, getCursorLocation() );
    return finder.getControl();
  }

  /**
   * Returns the button dismissal alignment, one of <code>LEFT</code> or <code>RIGHT</code>.
   * The button dismissal alignment is the ordering that should be used when positioning the
   * default dismissal button for a dialog.  For example, in a dialog that contains an OK and
   * CANCEL button, on platforms where the button dismissal alignment is <code>LEFT</code>, the
   * button ordering should be OK/CANCEL.  When button dismissal alignment is <code>RIGHT</code>,
   * the button ordering should be CANCEL/OK.
   *
   * @return the button dismissal order
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public int getDismissalAlignment() {
    checkDevice();
    return SWT.LEFT;
  }

  /**
   * Returns true when the high contrast mode is enabled.
   * Otherwise, false is returned.
   * <p>
   * Note: This operation is a hint and is not supported on
   * platforms that do not have this concept.
   * </p>
   *
   * @return the high contrast mode
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public boolean getHighContrast() {
    checkDevice();
    return false;
  }

  /**
   * Returns the location of the on-screen pointer relative
   * to the top left corner of the screen.
   *
   * @return the cursor location
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public Point getCursorLocation() {
    checkDevice();
    return new Point( cursorLocation.x, cursorLocation.y );
  }

  //////////
  // Filters

  /**
   * Adds the listener to the collection of listeners who will be notified when
   * an event of the given type occurs anywhere in a widget. The event type is
   * one of the event constants defined in class <code>SWT</code>. When the
   * event does occur, the listener is notified by sending it the
   * <code>handleEvent()</code> message.
   * <p>
   * Setting the type of an event to <code>SWT.None</code> from within the
   * <code>handleEvent()</code> method can be used to change the event type
   * and stop subsequent Java listeners from running. Because event filters run
   * before other listeners, event filters can both block other listeners and
   * set arbitrary fields within an event. For this reason, event filters are
   * both powerful and dangerous. They should generally be avoided for
   * performance, debugging and code maintenance reasons.
   * </p>
   * <p>
   * <strong>NOTE:</strong> In RAP, only events that have a matching listener registered on the
   * event's source widget are processed on the server. One exception are key events, for which a
   * whitelist can be define using {@link org.eclipse.rap.rwt.RWT#ACTIVE_KEYS RWT#ACTIVE_KEYS}.
   * </p>
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should be notified when the event occurs
   * @exception IllegalArgumentException
   *                <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *                </ul>
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
   *                disposed</li>
   *                </ul>
   * @see Listener
   * @see SWT
   * @see #removeFilter
   * @see #removeListener
   */
  public void addFilter( int eventType, Listener listener ) {
    checkDevice();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( filterTable == null ) {
      filterTable = new EventTable();
    }
    filterTable.hook( eventType, listener );
  }

  /**
   * Removes the listener from the collection of listeners who will be notified
   * when an event of the given type occurs anywhere in a widget. The event type
   * is one of the event constants defined in class <code>SWT</code>.
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should no longer be notified when the
   *            event occurs
   * @exception IllegalArgumentException
   *                <ul>
   *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *                </ul>
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   * @see Listener
   * @see SWT
   * @see #addFilter
   * @see #addListener
   */
  public void removeFilter( int eventType, Listener listener ) {
    checkDevice ();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( filterTable == null ) {
      return;
    }
    filterTable.unhook( eventType, listener );
    if( filterTable.size() == 0 ) {
      filterTable = null;
    }
  }

  boolean filters( int eventType ) {
    return filterTable != null ? filterTable.hooks( eventType ) : false;
  }

  void filterEvent( Event event ) {
    if( filterTable != null ) {
      filterTable.sendEvent( event );
    }
  }

  void addLayoutDeferred( Composite comp ) {
    if( layoutDeferred == null ) {
      layoutDeferred = new Composite[ 64 ];
    }
    if( layoutDeferredCount == layoutDeferred.length ) {
      Composite[] temp = new Composite[ layoutDeferred.length + 64 ];
      System.arraycopy( layoutDeferred, 0, temp, 0, layoutDeferred.length );
      layoutDeferred = temp;
    }
    layoutDeferred[ layoutDeferredCount++ ] = comp;
  }

  boolean runDeferredLayouts() {
    boolean result = false;
    if( layoutDeferredCount != 0 ) {
      Composite[] temp = layoutDeferred;
      int count = layoutDeferredCount;
      layoutDeferred = null;
      layoutDeferredCount = 0;
      for( int i = 0; i < count; i++ ) {
        Composite comp = temp[ i ];
        if( !comp.isDisposed() ) {
          comp.setLayoutDeferred( false );
        }
      }
      result = true;
    }
    return result;
  }

  ///////////////////
  // Skinning support

  void addSkinnableWidget( Widget widget ) {
    if( skinList == null ) {
      skinList = new Widget[ GROW_SIZE ];
    }
    if( skinCount >= skinList.length ) {
      Widget[] newSkinWidgets = new Widget[ skinList.length + GROW_SIZE ];
      System.arraycopy( skinList, 0, newSkinWidgets, 0, skinList.length );
      skinList = newSkinWidgets;
    }
    skinList[ skinCount++ ] = widget;
  }

  boolean runSkin() {
    boolean result = false;
    if( skinCount > 0 ) {
      Widget[] oldSkinWidgets = skinList;
      int count = skinCount;
      skinList = new Widget[ GROW_SIZE ];
      skinCount = 0;
      for( int i = 0; i < count; i++ ) {
        Widget widget = oldSkinWidgets[ i ];
        if( widget != null && !widget.isDisposed() ) {
          widget.removeState( Widget.SKIN_NEEDED );
          oldSkinWidgets[ i ] = null;
          sendSkinEvent( widget );
        }
      }
      result = true;
    }
    return result;
  }

  private void sendSkinEvent( Widget widget ) {
    Event event = new Event();
    event.widget = widget;
    sendEvent( SWT.Skin, event );
  }

  ///////////////
  // Data methods

  /**
   * Returns the application defined, display specific data
   * associated with the receiver, or null if it has not been
   * set. The <em>display specific data</em> is a single,
   * unnamed field that is stored with every display.
   * <p>
   * Applications may put arbitrary objects in this field. If
   * the object stored in the display specific data needs to
   * be notified when the display is disposed of, it is the
   * application's responsibility to provide a
   * <code>disposeExec()</code> handler which does so.
   * </p>
   *
   * @return the display specific data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *    created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #setData(Object)
   * @see #disposeExec(Runnable)
   *
   * @since 1.2
   */
  public Object getData() {
    checkDevice();
    return data;
  }

  /**
   * Sets the application defined, display specific data
   * associated with the receiver, to the argument.
   * The <em>display specific data</em> is a single,
   * unnamed field that is stored with every display.
   * <p>
   * Applications may put arbitrary objects in this field. If
   * the object stored in the display specific data needs to
   * be notified when the display is disposed of, it is the
   * application's responsibility provide a
   * <code>disposeExec()</code> handler which does so.
   * </p>
   *
   * @param data the new display specific data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *    created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #getData()
   * @see #disposeExec(Runnable)
   *
   * @since 1.2
   */
  public void setData( Object data ) {
    checkDevice();
    this.data = data;
  }

  /**
   * Sets the application defined property of the receiver
   * with the specified name to the given argument.
   * <p>
   * Applications may have associated arbitrary objects with the
   * receiver in this fashion. If the objects stored in the
   * properties need to be notified when the display is disposed
   * of, it is the application's responsibility provide a
   * <code>disposeExec()</code> handler which does so.
   * </p>
   *
   * @param key the name of the property
   * @param value the new value for the property
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *    created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #getData(String)
   * @see #disposeExec(Runnable)
   *
   * @since 1.2
   */
  // [bm]: This is a verbatim copy of SWT, thus no reformatting was done.
  public void setData( String key, Object value ) {
    checkDevice ();
    if (key == null) {
      error (SWT.ERROR_NULL_ARGUMENT);
    }

    /* Remove the key/value pair */
    if (value == null) {
      if (keys == null) {
        return;
      }
      int index = 0;
      while (index < keys.length && !keys [index].equals (key)) {
        index++;
      }
      if (index == keys.length) {
        return;
      }
      if (keys.length == 1) {
        keys = null;
        values = null;
      } else {
        String [] newKeys = new String [keys.length - 1];
        Object [] newValues = new Object [values.length - 1];
        System.arraycopy (keys, 0, newKeys, 0, index);
        System.arraycopy (keys, index + 1, newKeys, index, newKeys.length - index);
        System.arraycopy (values, 0, newValues, 0, index);
        System.arraycopy (values, index + 1, newValues, index, newValues.length - index);
        keys = newKeys;
        values = newValues;
      }
      return;
    }

    /* Add the key/value pair */
    if (keys == null) {
      keys = new String [] {key};
      values = new Object [] {value};
      return;
    }
    for (int i=0; i<keys.length; i++) {
      if (keys [i].equals (key)) {
        values [i] = value;
        return;
      }
    }
    String [] newKeys = new String [keys.length + 1];
    Object [] newValues = new Object [values.length + 1];
    System.arraycopy (keys, 0, newKeys, 0, keys.length);
    System.arraycopy (values, 0, newValues, 0, values.length);
    newKeys [keys.length] = key;
    newValues [values.length] = value;
    keys = newKeys;
    values = newValues;
  }

  /**
   * Returns the application defined property of the receiver
   * with the specified name, or null if it has not been set.
   * <p>
   * Applications may have associated arbitrary objects with the
   * receiver in this fashion. If the objects stored in the
   * properties need to be notified when the display is disposed
   * of, it is the application's responsibility to provide a
   * <code>disposeExec()</code> handler which does so.
   * </p>
   *
   * @param key the name of the property
   * @return the value of the property or null if it has not been set
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *    created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see #setData(String, Object)
   * @see #disposeExec(Runnable)
   *
   * @since 1.2
   */
  // [bm] This is a verbatim copy of SWT, thus no reformatting was done.
  public Object getData( String key ) {
    checkDevice ();
    if( key == null ) {
      error (SWT.ERROR_NULL_ARGUMENT);
    }
    if (keys == null) {
      return null;
    }
    for (int i=0; i<keys.length; i++) {
      if (keys [i].equals (key)) {
        return values [i];
      }
    }
    return null;
  }

  ///////////
  // Monitors

  /**
   * Returns an array of monitors attached to the device.
   *
   * @return the array of monitors
   *
   * @since 1.2
   */
  public Monitor[] getMonitors() {
    checkDevice();
    if( monitor == null ) {
      monitor = new Monitor( this );
    }
    return new Monitor[] { monitor };
  }

  /**
   * Returns the primary monitor for that device.
   *
   * @return the primary monitor
   *
   * @since 1.2
   */
  public Monitor getPrimaryMonitor() {
    return getMonitors()[ 0 ];
  }

  ////////////////////////
  // AppName- and version

  /**
   * Returns the application name.
   *
   * @return the application name
   *
   * @see #setAppName(String)
   *
   * @since 1.3
   */
  public static String getAppName() {
    UISession session = ContextProvider.getUISession();
    return ( String )session.getAttribute( APP_NAME );
  }

  /**
   * Returns the application version.
   *
   * @return the application version
   *
   * @see #setAppVersion(String)
   *
   * @since 1.3
   */
  public static String getAppVersion() {
    UISession session = ContextProvider.getUISession();
    return ( String )session.getAttribute( APP_VERSION );
  }

  /**
   * Sets the application name to the argument.
   * <p>
   * Specifying <code>null</code> for the name clears it.
   * </p>
   *
   * @param name the new app name or <code>null</code>
   *
   * @since 1.3
   */
  public static void setAppName( String name ) {
    UISession session = ContextProvider.getUISession();
    session.setAttribute( APP_NAME, name );
  }

  /**
   * Sets the application version to the argument.
   *
   * @param version the new app version
   *
   * @since 1.3
   */
  public static void setAppVersion( String version ) {
    UISession session = ContextProvider.getUISession();
    session.setAttribute( APP_VERSION, version );
  }

  /**
   * Forces all outstanding paint requests for the display
   * to be processed before this method returns.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see Control#update()
   *
   * @since 1.3
   */
  public void update() {
    checkDevice();
  }

  /**
   * Causes the system hardware to emit a short sound
   * (if it supports this capability).
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public void beep() {
    checkDevice();
    beep = true;
  }

  /**
   * Returns true if a touch-aware input device is attached to the system,
   * enabled, and ready for use.
   *
   * @since 1.4
   */
  public boolean getTouchEnabled() {
    checkDevice();
    return false;
  }

  @SuppressWarnings("unchecked")
  private void register() {
    synchronized( Device.class ) {
      boolean registered = false;
      WeakReference<Display>[] displays = getDisplays();
      for( int i = 0; !registered && i < displays.length; i++ ) {
        if( canDisplayRefBeReplaced( displays[ i ] ) ) {
          displays[ i ] = new WeakReference<>( this );
          registered = true;
        }
      }
      if( !registered ) {
        WeakReference<Display>[] newDisplays = new WeakReference[ displays.length + 4 ];
        System.arraycopy( displays, 0, newDisplays, 0, displays.length );
        newDisplays[ displays.length ] = new WeakReference<>( this );
        setDisplays( newDisplays );
      }
    }
  }

  private boolean canDisplayRefBeReplaced( WeakReference<Display> displayRef ) {
    boolean result = false;
    if( displayRef == null ) {
      result = true;
    } else {
      Display display = displayRef.get();
      if( display == null || display.thread == thread ) {
        result = true;
      }
    }
    return result;
  }

  private void deregister() {
    synchronized( Device.class ) {
      WeakReference<Display>[] displays = getDisplays();
      for( int i = 0; i < displays.length; i++ ) {
        WeakReference<Display> current = displays[ i ];
        if( current != null && this == current.get() ) {
          displays[ i ] = null;
        }
      }
    }
  }

  private static WeakReference<Display>[] getDisplays() {
    return ContextProvider.getApplicationContext().getDisplaysHolder().getDisplays();
  }

  private void setDisplays( WeakReference<Display>[] displays ) {
    getApplicationContext().getDisplaysHolder().setDisplays( displays );
  }

  /////////////////////
  // Consistency checks

  @SuppressWarnings( "unused" )
  static boolean isValidClass( Class<?> clazz ) {
//    String name = clazz.getName();
//    int index = name.lastIndexOf( '.' );
//    return name.substring( 0, index + 1 ).equals( PACKAGE_PREFIX );
    return true;
  }

  boolean isValidThread () {
    return thread == Thread.currentThread();
  }

  @Override
  protected void checkDevice() {
    if( !isValidThread() ) {
      error( SWT.ERROR_THREAD_INVALID_ACCESS );
    }
    if( isDisposed() ) {
      error( SWT.ERROR_DEVICE_DISPOSED );
    }
  }

  //////////////////
  // Helping methods

  /**
   * Does whatever display specific cleanup is required, and then uses the code
   * in <code>SWTError.error</code> to handle the error.
   *
   * @param code the descriptive error code
   * @see SWT#error(int)
   */
  void error( int code ) {
    SWT.error( code );
  }

  private Rectangle readInitialBounds() {
    JsonValue value = readPropertyValue( getId( this ), BOUNDS );
    return value != null ? readRectangle( value ) : new Rectangle( 0, 0, 1024, 768 );
  }

  private Point readDPI() {
    JsonValue value = readPropertyValue( getId( this ), DPI );
    return value != null ? readPoint( value ) : new Point( 0, 0 );
  }

  private int readDepth() {
    JsonValue value = readPropertyValue( getId( this ), COLOR_DEPTH );
    return value != null ? value.asInt() : 16;
  }

  /////////////////
  // Inner classes

  private static class WakeRunnable implements Runnable, SerializableCompatibility {

    @Override
    public void run() {
    }

  }

  private static final class ControlFinder {

    private final Display display;
    private final Point location;
    private final Set<Control> foundComponentInParent;
    private Control control;

    ControlFinder( Display display, Point location ) {
      this.display = display;
      this.location = new Point( location.x, location.y );
      foundComponentInParent = new HashSet<>();
      find();
    }

    Control getControl() {
      return control;
    }

    private void find() {
      Shell[] shells = display.getShells();
      for( int i = 0; control == null && i < shells.length; i++ ) {
        WidgetTreeUtil.accept( shells[ i ], new WidgetTreeVisitor() {
          @Override
          public boolean visit( Widget widget ) {
            if( widget instanceof Control ) {
              return visitControl( ( Control )widget );
            }
            return true;
          }
        } );
      }
    }

    private boolean visitControl( Control control ) {
      Rectangle bounds = getAbsoluteBounds( control );
      boolean result = false;
      if( control.isVisible() && bounds.contains( location ) ) {
        /*
         * only assign control to cursor location if there was no other control
         * already assigned within the same composite
         */
        result = foundComponentInParent.add( control.getParent() );
        if( result ) {
          this.control = control;
        }
      }
      return result;
    }

    private static Rectangle getAbsoluteBounds( Control control ) {
      Rectangle bounds = control.getBounds();
      Point origin = getAbsoluteOrigin( control );
      return new Rectangle( origin.x, origin.y, bounds.width, bounds.height );
    }

  }

  private final class DisplayAdapter implements IDisplayAdapter {

    @Override
    public void setBounds( Rectangle bounds ) {
      Display.this.bounds.x = bounds.x;
      Display.this.bounds.y = bounds.y;
      Display.this.bounds.width = bounds.width;
      Display.this.bounds.height = bounds.height;
    }

    @Override
    public void setCursorLocation( int x, int y ) {
      cursorLocation.x = x;
      cursorLocation.y = y;
    }

    @Override
    public void setActiveShell( Shell activeShell ) {
      Display.this.setActiveShell( activeShell );
    }

    @Override
    public void setFocusControl( Control focusControl, boolean fireEvents ) {
      Display.this.setFocusControl( focusControl, fireEvents );
    }

    @Override
    public void invalidateFocus() {
      ContextProvider.getServiceStore().setAttribute( ATTR_INVALIDATE_FOCUS, Boolean.TRUE );
    }

    @Override
    public boolean isFocusInvalidated() {
      Object value = ContextProvider.getServiceStore().getAttribute( ATTR_INVALIDATE_FOCUS );
      return value != null;
    }

    @Override
    public Shell[] getShells() {
      Shell[] result = new Shell[ shells.size() ];
      shells.toArray( result );
      return result;
    }

    @Override
    public UISession getUISession() {
      return uiSession;
    }

    @Override
    public void attachThread() {
      Display.this.attachThread();
    }

    @Override
    public void detachThread() {
      Display.this.detachThread();
    }

    @Override
    public boolean isValidThread() {
      return Display.this.isValidThread();
    }

    @Override
    public boolean isBeepCalled() {
      return beep;
    }

    @Override
    public void resetBeep() {
      beep = false;
    }

    @Override
    public void notifyListeners( int eventType, Event event ) {
      Display.this.notifyListeners( eventType, event );
    }

    @Override
    public boolean isListening( int eventType ) {
      return Display.this.isListening( eventType );
    }

  }

}
