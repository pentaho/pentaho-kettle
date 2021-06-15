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
package org.eclipse.swt.widgets;

import static org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil.clientListenerAdded;
import static org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil.clientListenerRemoved;

import org.eclipse.rap.rwt.Adaptable;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.lifecycle.PhaseId;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetDataUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.events.EventList;
import org.eclipse.swt.internal.events.EventUtil;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.IdGenerator;
import org.eclipse.swt.internal.widgets.ParentHolderRemoteAdapter;
import org.eclipse.swt.internal.widgets.WidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;


/**
 * This class is the abstract superclass of all user interface objects.
 * Widgets are created, disposed and issue notification to listeners
 * when events occur which affect them.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Dispose</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation. However, it has not been marked
 * final to allow those outside of the SWT development team to implement
 * patched versions of the class in order to get around specific
 * limitations in advance of when those limitations can be addressed
 * by the team.  Any class built using subclassing to access the internals
 * of this class will likely fail to compile or run between releases and
 * may be strongly platform specific. Subclassing should not be attempted
 * without an intimate and detailed understanding of the workings of the
 * hierarchy. No support is provided for user-written classes which are
 * implemented as subclasses of this class.
 * </p>
 * <p>Even though this class implements <code>Adaptable</code> this interface
 * is <em>not</em> part of the RWT public API. It is only meant to be shared
 * within the packages provided by RWT and should never be accessed from
 * application code.
 * </p>
 *
 * @see #checkSubclass
 * @since 1.0
 */
@SuppressWarnings( "deprecation" )
public abstract class Widget implements Adaptable, SerializableCompatibility {

  private static final Listener[] EMPTY_LISTENERS = new Listener[ 0 ];

  /* Default size for widgets */
  static final int DEFAULT_WIDTH = 64;
  static final int DEFAULT_HEIGHT = 64;

  /* Global state flags */
  static final int DISPOSED = 1 << 0;
//  static final int CANVAS = 1 << 1;
  static final int KEYED_DATA = 1 << 2;
  static final int DISABLED = 1 << 3;
  static final int HIDDEN = 1 << 4;

  /* A layout was requested on this widget */
  static final int LAYOUT_NEEDED  = 1 << 5;

  /* The preferred size of a child has changed */
  static final int LAYOUT_CHANGED = 1 << 6;

  /* A layout was requested in this widget hierarchy */
  static final int LAYOUT_CHILD = 1 << 7;

  /* Background flags */
  static final int THEME_BACKGROUND = 1 << 8;
  static final int PARENT_BACKGROUND = 1 << 10;

  /* Dispose and release flags */
  static final int RELEASED = 1 << 11;
  static final int DISPOSE_SENT = 1 << 12;

  /* Notify of the opportunity to skin this widget */
  static final int SKIN_NEEDED = 1 << 21;

  int style;
  private int state;
  Display display;
  private Object data;
  private EventTable eventTable;
  private RemoteAdapter remoteAdapter;
  private IWidgetGraphicsAdapter widgetGraphicsAdapter;

  Widget() {
    // prevent instantiation from outside this package
  }

  /**
   * Constructs a new instance of this class given its parent
   * and a style value describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a widget which will be the parent of the new instance (cannot be null)
   * @param style the style of widget to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see SWT
   * @see #checkSubclass
   * @see #getStyle
   */
  public Widget( Widget parent, int style ) {
    checkSubclass();
    if( parent == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    this.style = style;
    display = parent.display;
    reskinWidget();
    remoteAdapter = new ParentHolderRemoteAdapter( parent );
  }

  /**
   * Implementation of the <code>Adaptable</code> interface.
   * <p><strong>IMPORTANT:</strong> This method is <em>not</em> part of the RWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by RWT. It should never be accessed
   * from application code.
   * </p>
   * @noreference This method is not intended to be referenced by clients.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    // The adapters returned here are buffered for performance reasons. Don't change this without
    // good reason
    if( adapter == RemoteAdapter.class ) {
      return (T) ensureRemoteAdapter();
    }
    if( adapter == ThemeAdapter.class ) {
      ThemeManager themeManager = getApplicationContext().getThemeManager();
      return (T) themeManager.getThemeAdapterManager().getThemeAdapter( this );
    }
    if( adapter == IWidgetGraphicsAdapter.class ) {
      if( widgetGraphicsAdapter == null ) {
        widgetGraphicsAdapter = new WidgetGraphicsAdapter();
      }
      return (T) widgetGraphicsAdapter;
    }
    return null;
  }

  /**
   * Returns the application defined widget data associated
   * with the receiver, or null if it has not been set. The
   * <em>widget data</em> is a single, unnamed field that is
   * stored with every widget.
   * <p>
   * Applications may put arbitrary objects in this field. If
   * the object stored in the widget data needs to be notified
   * when the widget is disposed of, it is the application's
   * responsibility to hook the Dispose event on the widget and
   * do so.
   * </p>
   *
   * @return the widget data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - when the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - when called from the wrong thread</li>
   * </ul>
   *
   * @see #setData(Object)
   */
  public Object getData() {
    checkWidget();
    return hasState( KEYED_DATA ) ? ( ( Object[] )data )[ 0 ] : data;
  }

  /**
   * Sets the application defined widget data associated
   * with the receiver to be the argument. The <em>widget
   * data</em> is a single, unnamed field that is stored
   * with every widget.
   * <p>
   * Applications may put arbitrary objects in this field. If
   * the object stored in the widget data needs to be notified
   * when the widget is disposed of, it is the application's
   * responsibility to hook the Dispose event on the widget and
   * do so.
   * </p>
   *
   * @param data the widget data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - when the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - when called from the wrong thread</li>
   * </ul>
   *
   * @see #getData()
   */
  public void setData( Object data ) {
    checkWidget();
    if( hasState( KEYED_DATA ) ) {
      ( ( Object[] )this.data )[ 0 ] = data;
    } else {
      this.data = data;
    }
  }

  /**
   * Returns the application defined property of the receiver
   * with the specified name, or null if it has not been set.
   * <p>
   * Applications may have associated arbitrary objects with the
   * receiver in this fashion. If the objects stored in the
   * properties need to be notified when the widget is disposed
   * of, it is the application's responsibility to hook the
   * Dispose event on the widget and do so.
   * </p>
   *
   * @param key the name of the property
   * @return the value of the property or null if it has not been set
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setData(String, Object)
   */
  public Object getData( String key ) {
    checkWidget();
    if( key == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    Object result = null;
    if( hasState( KEYED_DATA ) ) {
      Object[] table = ( Object[] )data;
      for( int i = 1; result == null && i < table.length; i += 2 ) {
        if( key.equals( table[ i ] ) ) {
          result = table[ i + 1 ];
        }
      }
    }
    return result;
  }

  /**
   * Sets the application defined property of the receiver
   * with the specified name to the given value.
   * <p>
   * Applications may associate arbitrary objects with the
   * receiver in this fashion. If the objects stored in the
   * properties need to be notified when the widget is disposed
   * of, it is the application's responsibility to hook the
   * Dispose event on the widget and do so.
   * </p>
   *
   * @param key the name of the property
   * @param value the new value for the property
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #getData(String)
   */
  public void setData( String key, Object value ) {
    checkWidget();
    if( key == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( RWT.CUSTOM_VARIANT.equals( key ) ) {
      if( value != null ) {
        checkCustomVariant( value );
      }
      WidgetLCAUtil.preserveCustomVariant( this );
    }
    if( WidgetDataUtil.getDataKeys().contains( key ) ) {
      WidgetLCAUtil.preserveData( this );
    }
    int index = 1;
    Object[] table = null;
    if( hasState( KEYED_DATA ) ) {
      table = ( Object[] )data;
      while( index < table.length ) {
        if( key.equals( table[ index ] ) ) {
          break;
        }
        index += 2;
      }
    }
    if( value != null ) {
      if( hasState( KEYED_DATA ) ) {
        if( index == table.length ) {
          Object[] newTable = new Object[ table.length + 2 ];
          System.arraycopy( table, 0, newTable, 0, table.length );
          data = table = newTable;
        }
      } else {
        table = new Object[ 3 ];
        table[ 0 ] = data;
        data = table;
        addState( KEYED_DATA );
      }
      table[ index ] = key;
      table[ index + 1 ] = value;
    } else {
      if( hasState( KEYED_DATA ) ) {
        if( index != table.length ) {
          int length = table.length - 2;
          if( length == 1 ) {
            data = table[ 0 ];
            removeState( KEYED_DATA );
          } else {
            Object[] newTable = new Object[ length ];
            System.arraycopy( table, 0, newTable, 0, index );
            System.arraycopy( table, index + 2, newTable, index, length - index );
            data = newTable;
          }
        }
      }
    }
    if( key.equals( SWT.SKIN_CLASS ) || key.equals( SWT.SKIN_ID ) ) {
      reskin( SWT.ALL );
    }
  }

  ///////////////////////////////////////////
  // Methods to get/set single and keyed data

  /**
   * Returns the <code>Display</code> that is associated with
   * the receiver.
   * <p>
   * A widget's display is either provided when it is created
   * (for example, top level <code>Shell</code>s) or is the
   * same as its parent's display.
   * </p>
   *
   * @return the receiver's display
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Display getDisplay() {
    if( hasState( DISPOSED ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    return display;
  }

  /**
   * Returns the receiver's style information.
   * <p>
   * Note that the value which is returned by this method <em>may
   * not match</em> the value which was provided to the constructor
   * when the receiver was created. This can occur when the underlying
   * operating system does not support a particular combination of
   * requested styles. For example, if the platform widget used to
   * implement a particular SWT widget always has scroll bars, the
   * result of calling this method would always have the
   * <code>SWT.H_SCROLL</code> and <code>SWT.V_SCROLL</code> bits set.
   * </p>
   *
   * @return the style bits
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getStyle() {
    checkWidget();
    return style;
  }


  ///////////////////////////////////////////////
  // Registration and deregistration of listeners

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the widget is disposed. When the widget is
   * disposed, the listener is notified by sending it the
   * <code>widgetDisposed()</code> message.
   *
   * @param listener the listener which should be notified when the receiver is disposed
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see DisposeListener
   * @see #removeDisposeListener
   */
  public void addDisposeListener( DisposeListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Dispose, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the widget is disposed.
   *
   * @param listener the listener which should no longer be notified when the receiver is disposed
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see DisposeListener
   * @see #addDisposeListener
   */
  public void removeDisposeListener( DisposeListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    preserveListeners();
    if( eventTable != null ) {
      eventTable.unhook( SWT.Dispose, listener );
    }
  }

  ////////////////////////////////////////
  // Methods for untyped listener handling

  /**
   * Adds the listener to the collection of listeners who will be notified when
   * an event of the given type occurs. When the event does occur in the widget,
   * the listener is notified by sending it the <code>handleEvent()</code>
   * message. The event type is one of the event constants defined in class
   * <code>SWT</code>.
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should be notified when the event occurs
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see Listener
   * @see SWT
   * @see #removeListener
   * @see #notifyListeners
   */
  public void addListener( int eventType, Listener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    ensureEventTable();
    preserveListeners();
    eventTable.hook( eventType, listener );
    if( listener instanceof ClientListener ) {
      clientListenerAdded( this, eventType, ( ClientListener )listener );
    }
  }

  /**
   * Removes the listener from the collection of listeners who will be notified
   * when an event of the given type occurs. The event type is one of the event
   * constants defined in class <code>SWT</code>.
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should no longer be notified when the
   *          event occurs
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see Listener
   * @see SWT
   * @see #addListener
   * @see #notifyListeners
   */
  public void removeListener( int eventType, Listener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    preserveListeners();
    if( eventTable != null ) {
      eventTable.unhook( eventType, listener );
    }
    if( listener instanceof ClientListener ) {
      clientListenerRemoved( this, eventType, ( ClientListener )listener );
    }
  }

  /**
   * Notifies all of the receiver's listeners for events
   * of the given type that one such event has occurred by
   * invoking their <code>handleEvent()</code> method.  The
   * event type is one of the event constants defined in class
   * <code>SWT</code>.
   *
   * @param eventType the type of event which has occurred
   * @param event the event data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT
   * @see #addListener
   * @see #getListeners(int)
   * @see #removeListener(int, Listener)
   * @since 1.2
   */
  public void notifyListeners( int eventType, Event event ) {
    checkWidget();
    Event newEvent = event == null ? new Event() : event;
    newEvent.widget = this;
    newEvent.type = eventType;
    newEvent.display = display;
    if( newEvent.time == 0 ) {
      newEvent.time = EventUtil.getLastEventTime();
    }
    sendEvent( newEvent );
  }

  /**
   * Returns <code>true</code> if there are any listeners
   * for the specified event type associated with the receiver,
   * and <code>false</code> otherwise. The event type is one of
   * the event constants defined in class <code>SWT</code>.
   *
   * @param eventType the type of event
   * @return true if the event is hooked
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT
   * @since 1.3
   */
  public boolean isListening( int eventType ) {
    checkWidget();
    return eventTable == null ? false : eventTable.hooks( eventType );
  }

  /**
   * Returns an array of listeners who will be notified when an event
   * of the given type occurs. The event type is one of the event constants
   * defined in class <code>SWT</code>.
   *
   * @param eventType the type of event to listen for
   * @return an array of listeners that will be notified when the event occurs
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Listener
   * @see SWT
   * @see #addListener(int, Listener)
   * @see #removeListener(int, Listener)
   * @see #notifyListeners
   *
   * @since 1.3
   */
  public Listener[] getListeners( int eventType ) {
    checkWidget();
    return eventTable == null ? EMPTY_LISTENERS : eventTable.getListeners( eventType );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when an event of the given type occurs.
   * <p>
   * <b>IMPORTANT:</b> This method is <em>not</em> part of the SWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by SWT. It should never be
   * referenced from application code.
   * </p>
   *
   * @param eventType the type of event to listen for
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see Listener
   * @see #addListener
   *
   * @noreference This method is not intended to be referenced by clients.
   * @since 2.0
   */
  protected void removeListener( int eventType, SWTEventListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    preserveListeners();
    if( eventTable != null ) {
      eventTable.unhook( eventType, listener );
    }
  }

  private void sendEvent( Event event ) {
    if( isEventProcessingPhase() ) {
      event.display.filterEvent( event );
      if( eventTable != null ) {
        eventTable.sendEvent( event );
      }
    } else {
      EventList.getInstance().add( event );
    }
  }

  private static boolean isEventProcessingPhase() {
    PhaseId currentPhase = CurrentPhase.get();
    return PhaseId.PREPARE_UI_ROOT.equals( currentPhase )
        || PhaseId.PROCESS_ACTION.equals( currentPhase );
  }

  private void ensureEventTable() {
    if( eventTable == null ) {
      eventTable = new EventTable();
    }
  }

  ///////////////////
  // Skinning support

  /**
   * Marks the widget to be skinned.
   * <p>
   * The skin event is sent to the receiver's display when appropriate (usually before the next event
   * is handled). Widgets are automatically marked for skinning upon creation as well as when its skin
   * id or class changes. The skin id and/or class can be changed by calling <code>Display.setData(String, Object)</code>
   * with the keys SWT.SKIN_ID and/or SWT.SKIN_CLASS. Once the skin event is sent to a widget, it
   * will not be sent again unless <code>reskin(int)</code> is called on the widget or on an ancestor
   * while specifying the <code>SWT.ALL</code> flag.
   * </p>
   * <p>
   * The parameter <code>flags</code> may be either:
   * <dl>
   * <dt><b>SWT.ALL</b></dt>
   * <dd>all children in the receiver's widget tree should be skinned</dd>
   * <dt><b>SWT.NONE</b></dt>
   * <dd>only the receiver should be skinned</dd>
   * </dl>
   * </p>
   * @param flags the flags specifying how to reskin
   *
   * @exception SWTException
   * <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.3
   */
  public void reskin( int flags ) {
    checkWidget();
    reskinWidget();
    if( ( flags & SWT.ALL ) != 0 ) {
      reskinChildren( flags );
    }
  }

  @SuppressWarnings( "unused" )
  void reskinChildren( int flags ) {
  }

  void reskinWidget() {
    if( !hasState( SKIN_NEEDED ) ) {
      addState( SKIN_NEEDED );
      display.addSkinnableWidget( this );
    }
  }

  ///////////////////////
  // toString and helpers

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the receiver
   */
  @Override
  public String toString() {
    String string = "*Disposed*";
    if( !isDisposed() ) {
      string = "*Wrong Thread*";
      if( isValidThread() ) {
        string = getNameText();
      }
    }
    return getName() + " {" + string + "}";
  }

  /**
   * Returns the name of the widget. This is the name of
   * the class without the package name.
   *
   * @return the name of the widget
   */
  String getName() {
    String string = getClass().getName();
    int index = string.lastIndexOf( '.' );
    if( index != -1 ) {
      string = string.substring( index + 1, string.length() );
    }
    return string;
  }

  /*
   * Returns a short printable representation for the contents
   * of a widget. For example, a button may answer the label
   * text. This is used by <code>toString</code> to provide a
   * more meaningful description of the widget.
   *
   * @return the contents string for the widget
   *
   * @see #toString
   */
  String getNameText() {
    return "";
  }

  ///////////////////////////////////
  // Methods to dispose of the widget

  /**
   * Disposes of the operating system resources associated with
   * the receiver and all its descendents. After this method has
   * been invoked, the receiver and all descendents will answer
   * <code>true</code> when sent the message <code>isDisposed()</code>.
   * Any internal connections between the widgets in the tree will
   * have been removed to facilitate garbage collection.
   * <p>
   * NOTE: This method is not called recursively on the descendents
   * of the receiver. This means that, widget implementers can not
   * detect when a widget is being disposed of by re-implementing
   * this method, but should instead listen for the <code>Dispose</code>
   * event.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #addDisposeListener
   * @see #removeDisposeListener
   * @see #checkWidget
   */
  // TODO [rh] ensure that this implementation aligns with SWT rules for
  //      disposing (see The Standard Widget Toolkit, p 13)
  public void dispose() {
    if( !isDisposed() ) {
      if( !isValidThread() ) {
        error( SWT.ERROR_THREAD_INVALID_ACCESS );
      }
      if( !hasState( DISPOSE_SENT ) ) {
        addState( DISPOSE_SENT );
        notifyListeners( SWT.Dispose, new Event() );
      }
      if( !hasState( DISPOSED ) ) {
        releaseChildren();
      }
      if( !hasState( RELEASED ) ) {
        addState( RELEASED );
        releaseParent();
        releaseWidget();
        getAdapter( RemoteAdapter.class ).markDisposed( this );
      }
    }
  }

  /**
   * Returns <code>true</code> if the widget has been disposed,
   * and <code>false</code> otherwise.
   * <p>
   * This method gets the dispose state for the widget.
   * When a widget has been disposed, it is an error to
   * invoke any other method using the widget.
   * </p>
   *
   * @return <code>true</code> when the widget is disposed and <code>false</code> otherwise
   */
  public boolean isDisposed() {
    return hasState( DISPOSED );
  }

  boolean isInDispose() {
    return hasState( DISPOSE_SENT );
  }

  void releaseChildren() {
    // do nothing - derived classes may override
  }

  void releaseParent() {
    // do nothing - derived classes may override
  }

  void releaseWidget() {
    eventTable = null;
    addState( DISPOSED );
  }

  /**
   * Checks that this class can be subclassed.
   * <p>
   * The SWT class library is intended to be subclassed
   * only at specific, controlled points (most notably,
   * <code>Composite</code> and <code>Canvas</code> when
   * implementing new widgets). This method enforces this
   * rule unless it is overridden.
   * </p><p>
   * <em>IMPORTANT:</em> By providing an implementation of this
   * method that allows a subclass of a class which does not
   * normally allow subclassing to be created, the implementer
   * agrees to be fully responsible for the fact that any such
   * subclass will likely fail between SWT releases and will be
   * strongly platform specific. No support is provided for
   * user-written classes which are implemented in this fashion.
   * </p><p>
   * The ability to subclass outside of the allowed SWT classes
   * is intended purely to enable those not on the SWT development
   * team to implement patches in order to get around specific
   * limitations in advance of when those limitations can be
   * addressed by the team. Subclassing should not be attempted
   * without an intimate and detailed understanding of the hierarchy.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  protected void checkSubclass() {
    if( !isValidSubclass() ) {
      error( SWT.ERROR_INVALID_SUBCLASS );
    }
  }

  /*
   * Returns <code>true</code> when subclassing is
   * allowed and <code>false</code> otherwise
   *
   * @return <code>true</code> when subclassing is allowed and <code>false</code> otherwise
   */
  boolean isValidSubclass() {
    return Display.isValidClass( getClass() );
  }

  /**
   * Throws an <code>SWTException</code> if the receiver can not
   * be accessed by the caller. This may include both checks on
   * the state of the receiver and more generally on the entire
   * execution context. This method <em>should</em> be called by
   * widget implementors to enforce the standard SWT invariants.
   * <p>
   * Currently, it is an error to invoke any method (other than
   * <code>isDisposed()</code>) on a widget that has had its
   * <code>dispose()</code> method called. It is also an error
   * to call widget methods from any thread that is different
   * from the thread that created the widget.
   * </p><p>
   * In future releases of SWT, there may be more or fewer error
   * checks and exceptions may be thrown for different reasons.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  protected void checkWidget() {
    if( !isValidThread() ) {
      error( SWT.ERROR_THREAD_INVALID_ACCESS );
    }
    if( hasState( DISPOSED ) ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
  }

  /*
   * Returns <code>true</code> when the current thread is
   * the thread that created the widget and <code>false</code>
   * otherwise.
   *
   * @return <code>true</code> when the current thread is the thread that
   * created the widget and <code>false</code> otherwise
   */
  boolean isValidThread() {
    return getDisplay().isValidThread();
  }

  static int checkBits( int style, int int0, int int1, int int2, int int3, int int4, int int5 ) {
    int mask = int0 | int1 | int2 | int3 | int4 | int5;
    int result = style;
    if( ( result & mask ) == 0 ) {
      result |= int0;
    }
    if( ( result & int0 ) != 0 ) {
      result = ( result & ~mask ) | int0;
    }
    if( ( result & int1 ) != 0 ) {
      result = ( result & ~mask ) | int1;
    }
    if( ( result & int2 ) != 0 ) {
      result = ( result & ~mask ) | int2;
    }
    if( ( result & int3 ) != 0 ) {
      result = ( result & ~mask ) | int3;
    }
    if( ( result & int4 ) != 0 ) {
      result = ( result & ~mask ) | int4;
    }
    if( ( result & int5 ) != 0 ) {
      result = ( result & ~mask ) | int5;
    }
    return result;
  }

  void checkOrientation( Widget parent ) {
    style &= ~SWT.MIRRORED;
    if( ( style & ( SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT ) ) == 0 ) {
      if( parent != null ) {
        if( ( parent.style & SWT.LEFT_TO_RIGHT ) != 0 ) {
          style |= SWT.LEFT_TO_RIGHT;
        }
        if( ( parent.style & SWT.RIGHT_TO_LEFT ) != 0 ) {
          style |= SWT.RIGHT_TO_LEFT;
        }
      }
    }
    style = checkBits( style, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, 0, 0, 0, 0 );
  }

  void error( int code ) {
    SWT.error( code );
  }

  boolean hasState( int flag ) {
    return ( state & flag ) != 0;
  }

  void addState( int flag ) {
    state |= flag;
  }

  void removeState( int flag ) {
    state &= ~flag;
  }

  private void preserveListeners() {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )ensureRemoteAdapter();
    if( !( adapter ).hasPreservedListeners() ) {
      WidgetLCAUtil.preserveListeners( this, eventTable != null ? eventTable.getEventList() : 0 );
    }
  }

  private RemoteAdapter ensureRemoteAdapter() {
    if( remoteAdapter == null ) {
      remoteAdapter = createRemoteAdapter( null );
    } else if( remoteAdapter instanceof ParentHolderRemoteAdapter ) {
      remoteAdapter = createRemoteAdapter( remoteAdapter.getParent() );
    }
    return remoteAdapter;
  }

  private RemoteAdapter createRemoteAdapter( Widget parent ) {
    String id = IdGenerator.getInstance( RWT.getUISession( display ) ).createId( this );
    return createRemoteAdapter( parent, id );
  }

  RemoteAdapter createRemoteAdapter( Widget parent, String id ) {
    WidgetRemoteAdapter remoteAdapter = new WidgetRemoteAdapter( id );
    remoteAdapter.setParent( parent );
    return remoteAdapter;
  }

  private ApplicationContextImpl getApplicationContext() {
    IDisplayAdapter displayAdapter = display.getAdapter( IDisplayAdapter.class );
    return ( ApplicationContextImpl )displayAdapter.getUISession().getApplicationContext();
  }

  private static void checkCustomVariant( Object value ) {
    if( !( value instanceof String ) ) {
      throw new IllegalArgumentException( "Custom variant is not a string" );
    }
    if( !validateVariantString( ( String )value ) ) {
      throw new IllegalArgumentException( "Invalid custom variant: " + value );
    }
  }

  private static boolean validateVariantString( String variant ) {
    int start = 0;
    int length = variant.length();
    if( variant.startsWith( "-" ) ) {
      start++ ;
    }
    if( length == 0 ) {
      return false;
    }
    if( !isValidStart( variant.charAt( start ) ) ) {
      return false;
    }
    for( int i = start + 1; i < length; i++ ) {
      if( !isValidPart( variant.charAt( i ) ) ) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidStart( char ch ) {
    return ch == '_'
      || ( ch >= 'a' && ch <= 'z' )
      || ( ch >= 'A' && ch <= 'Z' )
      || ( ch >= 128 && ch <= 255 );
  }

  private static boolean isValidPart( char ch ) {
    return isValidStart( ch )
      || ( ch >= '0' && ch <= '9' )
      || ch == '-';
  }

}
