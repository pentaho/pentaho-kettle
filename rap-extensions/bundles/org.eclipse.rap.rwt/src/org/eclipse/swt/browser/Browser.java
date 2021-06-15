/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * Copyright (c) 2017 Hitachi America, Ltd., R&D.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.browser;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.lifecycle.SimpleLifeCycle;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.widgets.BrowserCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.internal.browser.browserkit.BrowserLCA;
import org.eclipse.swt.internal.events.EventTypes;
import org.eclipse.swt.internal.widgets.IBrowserAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;


/**
 * Instances of this class implement the browser user interface
 * metaphor.  It allows the user to visualize and navigate through
 * HTML documents.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @since 1.0
 *
 * <hr/>
 * <p>Currently implemented</p>
 * <ul><li>text and url property</li></ul>
 * <p>The enabled property in not (yet) evaluated.</p>
 * <p>Focus events are not yet implemented</p>
 *
 */
// TODO [rh] implement refresh method
// TODO [rh] bring focus events to work
public class Browser extends Composite {

  private static final String FUNCTIONS_TO_CREATE
    = Browser.class.getName() + "#functionsToCreate.";
  private static final String FUNCTIONS_TO_DESTROY
    = Browser.class.getName() + "#functionsToDestroy.";

  static final String ABOUT_BLANK = "about:blank";

  private String url;
  private String html;
  private boolean urlChanged;
  private String executeScript;
  private Boolean executeResult;
  private boolean executePending;
  private Object evaluateResult;
  private BrowserCallback browserCallback;
  private transient IBrowserAdapter browserAdapter;
  private final List<BrowserFunction> functions;

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
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for browser creation</li>
   * </ul>
   *
   * @see org.eclipse.swt.widgets.Widget#getStyle
   */
  public Browser( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    html = "";
    url = "";
    functions = new ArrayList<>();
    addDisposeListener( new BrowserDisposeListener() );
  }

  /**
   * Loads a URL.
   *
   * @param url the URL to be loaded
   *
   * @return true if the operation was successful and false otherwise.
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the url is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @see #getUrl
   */
  public boolean setUrl( String url ) {
    checkWidget();
    if( url == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    boolean result = sendLocationChangingEvent( url );
    if( result ) {
      this.url = url;
      urlChanged = true;
      html = "";
      sendLocationChangedEvent( url );
      sendProgressChangedEvent();
    }
    return result;
  }

  /**
   * Returns the current URL.
   *
   * @return the current URL or an empty <code>String</code> if there is no current URL
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @see #setUrl
   */
  public String getUrl() {
    checkWidget();
    return url;
  }

  /**
   * Renders HTML.
   *
   * <p>
   * The html parameter is Unicode encoded since it is a java <code>String</code>.
   * As a result, the HTML meta tag charset should not be set. The charset is implied
   * by the <code>String</code> itself.
   *
   * @param html the HTML content to be rendered
   *
   * @return true if the operation was successful and false otherwise.
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the html is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @see #setUrl
   */
  public boolean setText( String html ) {
    checkWidget();
    if( html == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    boolean result = sendLocationChangingEvent( ABOUT_BLANK );
    if( result ) {
      this.html = html;
      url = "";
      urlChanged = true;
      sendLocationChangedEvent( ABOUT_BLANK );
      sendProgressChangedEvent();
    }
    return result;
  }

  /**
   * Execute the specified script.
   *
   * <p>Execute a script containing javascript commands in the context of the
   * current document.</p>
   *
   * <!-- Begin RAP specific -->
   * <p><strong>RAP Note:</strong> Care should be taken when using this method.
   * The given <code>script</code> is executed in an <code>IFRAME</code>
   * inside the document that represents the client-side application.
   * Since the execution context of an <code>IFRAME</code> is not fully
   * isolated from the surrounding document it may break the client-side
   * application.</p>
   * <p>This method is not supported when running the application in JEE_COMPATIBILITY mode.
   * Use <code>evaluate(String, BrowserCallBack)</code> instead.</p>
   * <p>This method will throw an IllegalStateException if called while another script is still
   * pending or executed. This can happen if called within a BrowserFunction, or if an SWT event
   * is pending to be executed. (E.g. clicking a Button twice very fast.)
   * </p>
   * <!-- End RAP specific -->
   *
   * @param script the script with javascript commands
   *
   * @return <code>true</code> if the operation was successful and
   * <code>false</code> otherwise
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the script is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @exception UnsupportedOperationException when running the application in JEE_COMPATIBILITY mode
   * @exception IllegalStateException when another script is already being executed.
   *
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   *
   * @since 1.1
   */
  public boolean execute( String script ) {
    checkOperationMode();
    checkWidget();
    if( script == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( executeScript != null ) {
      throw new IllegalStateException( "Another script is already pending" );
    }
    executeScript = script;
    executeResult = null;
    while( executeResult == null ) {
      Display display = getDisplay();
      if( !display.readAndDispatch() )  {
        display.sleep();
      }
    }
    executeScript = null;
    executePending = false;
    return executeResult.booleanValue();
  }

  /**
   * Attempts to dispose the receiver, but allows the dispose to be vetoed
   * by the user in response to an <code>onbeforeunload</code> listener
   * in the Browser's current page.
   *
   * @return <code>true</code> if the receiver was disposed, and <code>false</code> otherwise
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #dispose()
   *
   * @since 3.6
   */
  public boolean close () {
    checkWidget();
    dispose ();
    return true;
  }

  /**
   * Returns the result, if any, of executing the specified script.
   * <p>
   * Evaluates a script containing javascript commands in the context of
   * the current document.  If document-defined functions or properties
   * are accessed by the script then this method should not be invoked
   * until the document has finished loading (<code>ProgressListener.completed()</code>
   * gives notification of this).
   * </p><p>
   * If the script returns a value with a supported type then a java
   * representation of the value is returned.  The supported
   * javascript -> java mappings are:
   * <ul>
   * <li>javascript null or undefined -> <code>null</code></li>
   * <li>javascript number -> <code>java.lang.Double</code></li>
   * <li>javascript string -> <code>java.lang.String</code></li>
   * <li>javascript boolean -> <code>java.lang.Boolean</code></li>
   * <li>javascript array whose elements are all of supported types -> <code>java.lang.Object[]</code></li>
   * </ul>
   *
   * An <code>SWTException</code> is thrown if the return value has an
   * unsupported type, or if evaluating the script causes a javascript
   * error to be thrown.
   *
   * <!-- Begin RAP specific -->
   * <p><strong>RAP Note:</strong> Care should be taken when using this method.
   * The given <code>script</code> is executed in an <code>IFRAME</code>
   * inside the document that represents the client-side application.
   * Since the execution context of an <code>IFRAME</code> is not fully
   * isolated from the surrounding document it may break the client-side
   * application.</p>
   * <p>This method is not supported when running the application in JEE_COMPATIBILITY mode.
   * Use <code>evaluate(String, BrowserCallback)</code> instead.</p>
   * <p>This method will throw an IllegalStateException if called while another script is still
   * pending or executed. This can happen if called within a BrowserFunction, or if an SWT
   * event is pending to be executed. (E.g. clicking a Button twice very fast.)
   * </p>
   * <!-- End RAP specific -->
   *
   * @param script the script with javascript commands
   *
   * @return the return value, if any, of executing the script
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the script is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_FAILED_EVALUATE when the script evaluation causes a javascript error to be thrown</li>
   *    <li>ERROR_INVALID_RETURN_VALUE when the script returns a value of unsupported type</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @exception UnsupportedOperationException when running the application in JEE_COMPATIBILITY mode
   * @exception IllegalStateException when another script is already being executed.

   * @see ProgressListener#completed(ProgressEvent)
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   *
   * @since 1.4
   */
  public Object evaluate( String script ) throws SWTException {
    checkOperationMode();
    if( script == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    boolean success = execute( prepareScript( script ) );
    if( !success ) {
      throw createException();
    }
    return evaluateResult;
  }

  /**
   * Executes the given script in a non-blocking way. The <code>browserCallback</code> is notified
   * when the result from the operation is available.
   * <p>
   * Use this method instead of the <code>execute()</code> or <code>evaluate()</code> methods when
   * running in <em>JEE_COMPATIBILITY</em> mode.
   * </p>
   *
   * <p>
   * This method will throw an IllegalStateException if called while another script is
   * still pending to be executed.
   * </p>

   * @param script the script to execute, must not be <code>null</code>.
   * @param browserCallback the callback to be notified when the result from the script execution is
   * available, must not be <code>null</code>.
   *
   * @exception IllegalStateException when another script is already being executed.
   *
   * @see BrowserCallback
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   * @rwtextension This method is not available in SWT.
   * @since 3.1
   */
  public void evaluate( String script, BrowserCallback browserCallback ) {
    ParamCheck.notNull( script, "script" );
    ParamCheck.notNull( browserCallback, "browserCallback" );
    evaluateNonBlocking( script, browserCallback );
  }

  public void addOpenWindowListener( OpenWindowListener listener ) {  }

  public void addCloseWindowListener( CloseWindowListener listener ) {  }

  /**
   * Adds the listener to the collection of listeners who will be
   * notified when the current location has changed or is about to change.
   * <p>
   * This notification typically occurs when the application navigates
   * to a new location with {@link #setUrl(String)} or when the user
   * activates a hyperlink.
   * </p>
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   */
  public void addLocationListener( LocationListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedBrowserListener browserListener = new TypedBrowserListener( listener );
    addListener( EventTypes.LOCALTION_CHANGED, browserListener );
    addListener( EventTypes.LOCALTION_CHANGING, browserListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the current location is changed or about to be changed.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   */
  public void removeLocationListener( LocationListener listener ) {
    checkWidget();
    removeListener( EventTypes.LOCALTION_CHANGED, listener );
    removeListener( EventTypes.LOCALTION_CHANGING, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will be
   * notified when a progress is made during the loading of the current
   * URL or when the loading of the current URL has been completed.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @since 1.4
   */
  public void addProgressListener( ProgressListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedBrowserListener browserListener = new TypedBrowserListener( listener );
    addListener( EventTypes.PROGRESS_CHANGED, browserListener );
    addListener( EventTypes.PROGRESS_COMPLETED, browserListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when a progress is made during the loading of the current
   * URL or when the loading of the current URL has been completed.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
   *    <li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
   * </ul>
   *
   * @since 1.4
   */
  public void removeProgressListener( ProgressListener listener ) {
    checkWidget();
    removeListener( EventTypes.PROGRESS_CHANGED, listener );
    removeListener( EventTypes.PROGRESS_COMPLETED, listener );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IBrowserAdapter.class ) {
      if( browserAdapter == null ) {
        browserAdapter = new BrowserAdapter();
      }
      return ( T )browserAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )BrowserLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  /**
   * Returns the JavaXPCOM <code>nsIWebBrowser</code> for the receiver, or <code>null</code>
   * if it is not available.  In order for an <code>nsIWebBrowser</code> to be returned all
   * of the following must be true: <ul>
   *    <li>the receiver's style must be <code>SWT.MOZILLA</code></li>
   *    <li>the classes from JavaXPCOM &gt;= 1.8.1.2 must be resolvable at runtime</li>
   *    <li>the version of the underlying XULRunner must be &gt;= 1.8.1.2</li>
   * </ul>
   *
   * @return the receiver's JavaXPCOM <code>nsIWebBrowser</code> or <code>null</code>
   *
   * @since 1.4
   */
  public Object getWebBrowser() {
    checkWidget();
    return null;
  }

  private static int checkStyle( int style ) {
    int result = style;
    if( ( style & ( SWT.MOZILLA | SWT.WEBKIT ) ) != 0 ) {
      throw new SWTError( SWT.ERROR_NO_HANDLES, "Unsupported Browser type" );
    }
    if( ( result & SWT.H_SCROLL ) != 0 ) {
      result &= ~SWT.H_SCROLL;
    }
    if( ( result & SWT.V_SCROLL ) != 0 ) {
      result &= ~SWT.V_SCROLL;
    }
    return result;
  }

  //////////////////////////////////////////
  // BrowserFunction support helping methods

  private BrowserFunction[] getBrowserFunctions() {
    return functions.toArray( new BrowserFunction[ functions.size() ] );
  }

  void createFunction( BrowserFunction function ) {
    boolean removed = false;
    for( int i = 0; !removed && i < functions.size(); i++ ) {
      BrowserFunction current = functions.get( i );
      if( current.name.equals( function.name ) ) {
        functions.remove( current );
        removed = true;
      }
    }
    functions.add( function );
    if( !removed ) {
      updateBrowserFunctions( function.getName(), true );
    }
  }

  void destroyFunction( BrowserFunction function ) {
    functions.remove( function );
    updateBrowserFunctions( function.getName(), false );
  }

  private void updateBrowserFunctions( String function, boolean create ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    String id = WidgetUtil.getId( this );
    String key = create ? FUNCTIONS_TO_CREATE + id : FUNCTIONS_TO_DESTROY + id;
    String[] funcList = ( String[] )serviceStore.getAttribute( key );
    String[] newList;
    if( funcList == null ) {
      newList = new String[ 1 ];
      newList[ 0 ] = function;
    } else {
      newList = new String[ funcList.length + 1 ];
      System.arraycopy( funcList, 0, newList, 0, funcList.length );
      newList[ funcList.length ] = function;
    }
    serviceStore.setAttribute( key, newList );
  }

  @Override
  protected void checkWidget() {
    super.checkWidget();
  }

  private static void checkOperationMode() {
    if( getApplicationContext().getLifeCycleFactory().getLifeCycle() instanceof SimpleLifeCycle ) {
      throw new UnsupportedOperationException( "Method not supported in JEE_COMPATIBILITY mode." );
    }
  }

  private void onDispose() {
    executeResult = Boolean.FALSE;
    evaluateResult = null;
    executeScript = null;
    executePending = false;
  }

  //////////////////
  // Helping methods

  private boolean sendLocationChangingEvent( String location ) {
    Event event = new Event();
    event.text = location;
    notifyListeners( EventTypes.LOCALTION_CHANGING, event );
    return event.doit;
  }

  private void sendLocationChangedEvent( String location ) {
    Event event = new Event();
    event.text = location;
    event.detail = SWT.TOP;
    notifyListeners( EventTypes.LOCALTION_CHANGED, event );
  }

  private void sendProgressChangedEvent() {
    notifyListeners( EventTypes.PROGRESS_CHANGED, new Event() );
  }

  private static String prepareScript( String script ) {
    StringBuilder buffer = new StringBuilder( "(function(){" );
    buffer.append( script );
    buffer.append( "})();" );
    return buffer.toString();
  }

  private void setExecuteResult( final boolean success, final Object result ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        executeResult = Boolean.valueOf( success );
        evaluateResult = result;
        if( browserCallback != null ) {
          if( success ) {
            browserCallback.evaluationSucceeded( result );
          } else {
            browserCallback.evaluationFailed( createException() );
          }
          browserCallback = null;
          executeScript = null;
          executePending = false;
        }
      }
    } );
  }

  private void evaluateNonBlocking( String script, BrowserCallback browserCallback ) {
    checkWidget();
    if( executeScript != null ) {
      throw new IllegalStateException( "Another script is already pending" );
    }
    this.browserCallback = browserCallback;
    executeScript = prepareScript( script );
  }

  private static SWTException createException() {
    // TODO: Get the error message from the client
    String errorString = "Failed to evaluate Javascript expression";
    return new SWTException( SWT.ERROR_FAILED_EVALUATE, errorString );
  }

  ////////////////
  // Inner classes

  private class BrowserDisposeListener implements DisposeListener  {
    @Override
    public void widgetDisposed( DisposeEvent event ) {
      onDispose();
    }
  }

  private final class BrowserAdapter implements IBrowserAdapter {

    @Override
    public String getText() {
      return html;
    }

    @Override
    public String getExecuteScript() {
      return executeScript;
    }

    @Override
    public void setExecuteResult( boolean success, Object result ) {
      Browser.this.setExecuteResult( success, result );
    }

    @Override
    public void setExecutePending( boolean executePending ) {
      Browser.this.executePending = executePending;
    }

    @Override
    public boolean getExecutePending() {
      return executePending;
    }

    @Override
    public BrowserFunction[] getBrowserFunctions() {
      return Browser.this.getBrowserFunctions();
    }

    @Override
    public boolean hasUrlChanged() {
      return urlChanged;
    }

    @Override
    public void resetUrlChanged() {
      urlChanged = false;
    }

  }

  static class TypedBrowserListener extends TypedListener {

    TypedBrowserListener( SWTEventListener listener ) {
      super( listener );
    }

    @Override
    public void handleEvent( Event event ) {
      switch( event.type ) {
        case EventTypes.LOCALTION_CHANGING: {
          LocationListener locationListener = ( LocationListener )getEventListener();
          LocationEvent locationEvent = new LocationEvent( event );
          locationListener.changing( locationEvent );
          event.doit = locationEvent.doit;
          break;
        }
        case EventTypes.LOCALTION_CHANGED: {
          LocationListener locationListener = ( LocationListener )getEventListener();
          LocationEvent locationEvent = new LocationEvent( event );
          locationListener.changed( locationEvent );
          break;
        }
        case EventTypes.PROGRESS_CHANGED: {
          ProgressListener progressListener = ( ProgressListener )getEventListener();
          ProgressEvent progressEvent = new ProgressEvent( event );
          progressListener.changed( progressEvent );
          break;
        }
        case EventTypes.PROGRESS_COMPLETED: {
          ProgressListener progressListener = ( ProgressListener )getEventListener();
          ProgressEvent progressEvent = new ProgressEvent( event );
          progressListener.completed( progressEvent );
          break;
        }
      }
    }
  }

  public boolean isBackEnabled () {
    return false;
  }

  public boolean isForwardEnabled () {
    return false;
  }

  public boolean back () {
    return false;
  }

  public boolean forward () {
    return false;
  }

  public void refresh () {
  }
}