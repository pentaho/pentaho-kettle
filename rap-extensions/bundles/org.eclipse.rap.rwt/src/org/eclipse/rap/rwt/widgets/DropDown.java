/*******************************************************************************
 * Copyright (c) 2013, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetDataUtil;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.events.EventLCAUtil;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * Instances of this class represent a list of items that are attached to a control (parent).
 * If made visible (<code>setVisibility(true)</code>), the list
 * appears attached to the control. The user can select an item from the list
 * using the arrows keys and Enter, or the mouse.
 *
 * The list disappears automatically if the control looses focus, if an item is clicked,
 * or if the escape key is pressed.
 *
 * This class supports the {@link RWT#MARKUP_ENABLED} property the same way Table and Tree do.
 *
 * <p>
 * <dl>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Selection</dd>
 * </dl>
 * <p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @since 2.3
 *
 */

@SuppressWarnings( "deprecation" )
public class DropDown extends Widget {

  private static final String COLUMNS = "columns";
  private static final String REMOTE_TYPE = "rwt.widgets.DropDown";
  private static final String SELECTION = "Selection";
  private static final String DEFAULT_SELECTION = "DefaultSelection";
  private static final String[] ALLOWED_STYLES = {
    "V_SCROLL", "H_SCROLL"
  };
  private final java.util.List<String> items;

  private RemoteObject remoteObject;
  private Object widgetAdapter;
  private final Control parent;
  private final Listener disposeListener;
  private boolean visibility = false;
  private int selectionIndex = -1;
  private int visibleItemCount = 5;

  /**
   * Constructs a new instance of this class given its parent
   * and a style value describing its behavior.
   *
   * @param parent a control, usually <code>Text</code>,
   *        which will be the parent of the new instance (cannot be null)
   * @param style the style of widget to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see Text
   * @see Widget#checkSubclass
   *
   * @since 3.2
   */
  public DropDown( Control parent, int style ) {
    super( parent, checkStyle( style ) );
    this.parent = parent;
    items = new ArrayList<>();
    getRemoteObject().set( "parent", WidgetUtil.getId( parent ) );
    getRemoteObject().set( "style", createJsonArray( getStyles( this, ALLOWED_STYLES ) ) );
    getRemoteObject().setHandler( new InternalOperationHandler() );
    disposeListener = new Listener() {
      @Override
      public void handleEvent( Event event ) {
        DropDown.this.dispose();
      }
    };
    parent.addListener( SWT.Dispose, disposeListener );
  }

  /**
   * Constructs a new instance of this class given its parent.
   *
   * @param parent a control, usually <code>Text</code>,
   *        which will be the parent of the new instance (cannot be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   *
   * @see Text
   * @see Widget#checkSubclass
   */
  public DropDown( Control parent ) {
    this( parent, SWT.V_SCROLL );
  }

  /**
   * Sets the receiver's items to be the given array of items.
   *
   * @param items the array of items
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if an item in the items array is null</li>
   * </ul>
   */
  public void setItems( String[] items ) {
    checkWidget();
    if( items == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < items.length; i++ ) {
      if( items[ i ] == null ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
    this.items.clear();
    this.items.addAll( Arrays.asList( items ) );
    remoteObject.set( "items", createJsonArray( items ) );
    setSelectionIndexImpl( -1 );
  }


  /**
   * Returns a (possibly empty) array of <code>String</code>s which
   * are the items in the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver
   * to maintain its list of items, so modifying the array will
   * not affect the receiver.
   * </p>
   *
   * @return the items in the receiver's list
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String[] getItems() {
    checkWidget();
    return items.toArray( new String[ items.size() ] );
  }

  /**
   * Selects the item at the given zero-relative index in the receiver.
   * If the item at the index was already selected, it remains selected.
   * Indices that are out of range are ignored.
   *
   * @param selection the index of the item to select
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelectionIndex( int selection ) {
    checkWidget();
    if( selectionIndex != selection && selection >= -1 && selection < items.size() ) {
      setSelectionIndexImpl( selection );
      remoteObject.set( "selectionIndex", selection );
    }
  }

  /**
   * Returns the zero-relative index of the item which is currently
   * selected in the receiver, or -1 if no item is selected.
   *
   * @return the index of the selected item or -1
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getSelectionIndex() {
    checkWidget();
    return selectionIndex;
  }

  /**
   * Sets the maximum number of items that are visible in the receiver's list.
   *
   * @param itemCount the new number of items to be visible (default is 5)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setVisibleItemCount( int itemCount ) {
    checkWidget();
    if( visibleItemCount != itemCount ) {
      visibleItemCount = itemCount;
      remoteObject.set( "visibleItemCount", itemCount );
    }
  }

  /**
   * Gets the number of items that are visible in the receiver's list.
   *
   * @return the number of items that are visible
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getVisibleItemCount() {
    checkWidget();
    return visibleItemCount;
  }

  /**
   * Marks the receiver as visible if the argument is <code>true</code>,
   * and marks it invisible otherwise.
   *
   * @param visible the new visibility state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setVisible( boolean visible ) {
    checkWidget();
    if( visibility != visible ) {
      setVisibleImpl( visible );
      remoteObject.set( "visible", visible );
    }
  }

  /**
   * Returns <code>true</code> if the receiver is visible, and
   * <code>false</code> otherwise.
   * <p>
   * The initial value is false.
   * </p>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @return the receiver's visibility state
   *
   */
  public boolean getVisible() {
    checkWidget();
    return visibility;
  }

  /**
   * Returns the receiver's parent, which must be a <code>Control</code>.
   *
   * @return the receiver's parent
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Control getParent() {
    checkWidget();
    return parent;
  }

  @Override
  public void dispose() {
    if( !isDisposed() ) {
      super.dispose();
      parent.removeListener( SWT.Dispose, disposeListener );
      remoteObject.destroy();
    }
  }

  @Override
  public void setData( String key, Object value ) {
    super.setData( key, value );
    renderData( key, value );
    if( RWT.MARKUP_ENABLED.equals( key ) && value instanceof Boolean ) {
      remoteObject.set( "markupEnabled", ( ( Boolean )value ).booleanValue() );
    } else if( COLUMNS.equals( key ) ) {
      if( value instanceof int[] ) {
        int arr[] = ( int[] )value;
        remoteObject.set( COLUMNS, createJsonArray( arr ) );
      } else {
        remoteObject.set( COLUMNS, JsonValue.NULL );
      }
    }
  }

  @Override
  public void addListener( int eventType, Listener listener ) {
    boolean wasListening = EventLCAUtil.isListening( this, eventType );
    super.addListener( eventType, listener );
    boolean isListening = EventLCAUtil.isListening( this, eventType );
    String remoteType = eventTypeToString( eventType );
    if( listener instanceof ClientListener ) {
      JsonObject parameters = new JsonObject()
        .add( "eventType", ClientListenerUtil.getEventType( eventType ) )
        .add( "listenerId", ClientListenerUtil.getRemoteId( ( ClientListener )listener ) );
      remoteObject.call( "addListener", parameters );
    } else if( remoteType != null && !wasListening && isListening ) {
      remoteObject.listen( remoteType, true );
    }
  }

  @Override
  public void removeListener( int eventType, Listener listener ) {
    boolean wasListening = EventLCAUtil.isListening( this, eventType );
    super.removeListener( eventType, listener );
    boolean isListening = EventLCAUtil.isListening( this, eventType );
    String remoteType = eventTypeToString( eventType );
    if( listener instanceof ClientListener ) {
      JsonObject parameters = new JsonObject()
        .add( "eventType", ClientListenerUtil.getEventType( eventType ) )
        .add( "listenerId", ClientListenerUtil.getRemoteId( ( ClientListener )listener ) );
      remoteObject.call( "removeListener", parameters );
    } else if( remoteType != null && wasListening && !isListening ) {
      remoteObject.listen( remoteType, false );
    }
  }

  //////////////
  // overwritten

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    T result;
    if( adapter == RemoteAdapter.class ) {
      // TODO [tb] : This way of getting the right id into the WidgetAdapter is obviously
      //             not ideal. Revise once Bug 397602 (Render operations in the order of their
      //             occurrence) is fixed.
      if( widgetAdapter == null ) {
        widgetAdapter = new WidgetRemoteAdapter( getProtocolId() );
      }
      result = ( T )widgetAdapter;
    } else {
      result = super.getAdapter( adapter );
    }
    return result;
  }

  //////////
  // private


  private class InternalOperationHandler extends AbstractOperationHandler {

    @Override
    public void handleSet( JsonObject properties ) {
      if( properties.get( "visible" ) != null ) {
        setVisibleImpl( properties.get( "visible" ).asBoolean() );
      }
      if( properties.get( "selectionIndex" ) != null ) {
        setSelectionIndexImpl( properties.get( "selectionIndex" ).asInt() );
      }
    }

    @Override
    public void handleNotify( String type, JsonObject properties ) {
      if( SELECTION.equals( type ) || DEFAULT_SELECTION.equals( type )) {
        Event event = new Event();
        event.index = properties.get( "index" ).asInt();
        event.text = event.index >= 0 ? items.get( event.index ) : "";
        notifyListeners( stringToEventType( type ), event );
      }
    }

  }

  private void setVisibleImpl( boolean value ) {
    visibility = value;
  }

  private void setSelectionIndexImpl( int value ) {
    selectionIndex = value;
  }

  private String getProtocolId() {
    return getRemoteObject().getId();
  }

  private RemoteObject getRemoteObject() {
    if( remoteObject == null ) {
      remoteObject = RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    }
    return remoteObject;
  }

  private void renderData( String key, Object value ) {
    // TODO [tb] : could be optimized using a PhaseListener
    //             This implementation assumes the client merges the new values with the existing
    //             ones, which is the case in the WebClient
    if( WidgetDataUtil.getDataKeys().contains( key ) ) {
      JsonObject data = new JsonObject().add( key, JsonUtil.createJsonValue( value ) );
      remoteObject.set( "data", data );
    }
  }

  private static String eventTypeToString( int type ) {
    String result;
    switch( type ) {
      case SWT.Selection:
        result = SELECTION;
      break;
      case SWT.DefaultSelection:
        result = DEFAULT_SELECTION;
      break;
      default:
        result = null;
      break;
    }
    return result;
  }

  private static int stringToEventType( String str ) {
    int result = -1;
    if( SELECTION.equals( str ) ) {
      result = SWT.Selection;
    } else if( DEFAULT_SELECTION.equals( str ) ) {
      result = SWT.DefaultSelection;
    }
    return result;
  }

  private static int checkStyle( int style ) {
    return style | SWT.V_SCROLL;
  }

}
