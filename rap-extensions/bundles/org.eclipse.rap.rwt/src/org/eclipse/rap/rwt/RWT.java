/*******************************************************************************
 * Copyright (c) 2007, 2019 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing implementation
 *    Ralf Zahn (ARS) - browser history support (Bug 283291)
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.rap.rwt;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycle;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServletLog;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.service.ServiceManager;
import org.eclipse.rap.rwt.service.SettingStore;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;


/**
 * This class provides access to those parts of RWT which are not covered by the SWT API. For
 * example, it provides access to the current UI session and the request.
 *
 * @since 2.0
 * @see UISession
 * @see ApplicationContext
 * @see ResourceManager
 * @see HttpServletRequest
 * @see HttpServletResponse
 */
public final class RWT {

  /**
   * <p>This utility class helps to provide a similar approach for compile safe
   * native language support than {@link org.eclipse.osgi.util.NLS NLS} does.
   * We can not use the original approach though, due to the nature of
   * server side environments, that have to deal with different locales
   * per user session or even requests.</p>
   *
   * <p>
   * Usage:
   * <pre>
   *  public class FooMessages {
   *    private static final String BUNDLE_NAME = "foo.bar.messages";
   *
   *    public String MyMessage;
   *
   *    public static FooMessages get() {
   *      return ( FootMessages )RWT.NLS.getISO8859_1Encoded( BUNDLE_NAME, FooMessages.class );
   *    }
   *  }
   * </pre>
   *
   * BUNDLE_NAME contains the name of a properties file (without file extension)
   * that follows the conventions of standard {@link ResourceBundle} property
   * files. For each field (in the example 'MyMessage') there has to be a
   * key entry in the localization property file. Use the
   * <code>FooMessages</code> like this in the application code:
   *
   * <pre>
   *   Label label = ...;
   *   label.setText( FooMessages.get().MyMessage );
   * </pre>
   * </p>
   */
  @SuppressWarnings("javadoc")
  public static final class NLS {

    private final static Map<ResourceBundle,Object> map = new HashMap<>();

    /**
     * Returns a NLS object for the given resource bundle and type. See
     * class description for usage information.
     * The resource bundles read by this method have to be ISO 8859-1 encoded.
     * This is according to the {@link java.util.Properties Properties} file
     * specification.
     *
     * @param bundleName the resource bundle to load.
     * @param clazz the class of the NLS object to load.
     */
    public static <T> T getISO8859_1Encoded( String bundleName, Class<T> clazz ) {
      ClassLoader loader = clazz.getClassLoader();
      ResourceBundle bundle = ResourceBundle.getBundle( bundleName, getLocale(), loader );
      return internalGet( bundle, clazz );
    }

    /**
     * Returns a NLS object for the given resource bundle and type. See
     * class description for usage information.
     * The resource bundles read by this method have to be UTF-8 encoded. Note
     * that this is not according to the {@link java.util.Properties Properties}
     * file specification and meant for a more convenient use.
     *
     * @param bundleName the resource bundle to load.
     * @param clazz the class of the NLS object to load.
     */
    public static <T> T getUTF8Encoded( String bundleName, Class<T> clazz ) {
      ClassLoader loader = clazz.getClassLoader();
      ResourceBundle bundle = Utf8ResourceBundle.getBundle( bundleName, getLocale(), loader );
      return internalGet( bundle, clazz );
    }

    @SuppressWarnings( "unchecked" )
    private static <T> T internalGet( ResourceBundle bundle, Class<T> clazz ) {
      T result;
      synchronized( map ) {
        result = ( T )map.get( bundle );
        if( result == null ) {
          result = ClassUtil.newInstance( clazz );
          Field[] fields = clazz.getDeclaredFields();
          for( int i = 0; i < fields.length; i++ ) {
            String fieldName = fields[ i ].getName();
            try {
              if(    String.class.isAssignableFrom( fields[ i ].getType() )
                  && Modifier.isPublic( fields[ i ].getModifiers() )
                  && !Modifier.isStatic( fields[ i ].getModifiers() ) )
              {
                try {
                  String value = bundle.getString( fieldName );
                  if( value != null ) {
                    fields[ i ].setAccessible( true );
                    fields[ i ].set( result, value );
                  }
                } catch( MissingResourceException mre ) {
                  fields[ i ].setAccessible( true );
                  fields[ i ].set( result, "" );
                  throw mre;
                }
              }
            } catch( Exception ex ) {
              String qualifiedName = clazz.getName() + "#" + fieldName;
              ServletLog.log( "Failed to load localized message for: " + qualifiedName, ex );
            }
          }
          map.put( bundle, result );
        }
      }
      return result;
    }
  }

  /**
   * The property to use in <code>Display.setData()</code> in order to activate global key events
   * for certain key sequences. The value for this property has to be an array of Strings, each
   * representing a key sequence. When this property is set on the display, the client will be
   * instructed to issue events for the given key sequences. These key events can be captured using
   * <code>Display.addFilter()</code>.
   * <p>
   * The property can also be used in <code>Control.setData()</code>. In this case, a key listener
   * that is attached to that control will only receive events for the specified key sequences.
   * Control without active keys set will issue events for all key strokes.
   * </p>
   * <p>
   * Valid strings for key sequences consist of one key and any number of modifier keys,
   * separated by <code>+</code>. Keys can be identified by their upper case character or by any
   * of the keywords below. Special characters (not a letter or digit) should not be combined with
   * any modifiers, and will issue events regardless of pressed modifiers.
   * </p>
   * <p>
   * The following keywords can be used to refer to special keys:
   * <code>BACKSPACE</code>, <code>TAB</code>, <code>RETURN</code>,
   * <code>ENTER</code>, <code>ESCAPE</code>, <code>SPACE</code>, <code>PAGE_UP</code>,
   * <code>PAGE_DOWN</code>, <code>END</code>, <code>HOME</code>, <code>ARROW_LEFT</code>,
   * <code>ARROW_UP</code>, <code>ARROW_RIGHT</code>, <code>ARROW_DOWN</code>, <code>INSERT</code>,
   * <code>DELETE</code>, <code>F1</code>, <code>F2</code>, <code>F3</code>, <code>F4</code>,
   * <code>F5</code>, <code>F6</code>, <code>F7</code>, <code>F8</code>, <code>F9</code>,
   * <code>F10</code>, <code>F11</code>, <code>F12</code>, Valid modifier keys are
   * <code>SHIFT</code>, <code>ALT</code>, and <code>CTRL</code>.
   * </p>
   * Examples: <code>&quot;A&quot;</code>, <code>&quot;#&quot;</code>, <code>&quot;F12&quot;</code>,
   * <code>&quot;CTRL+1&quot;</code>, <code>&quot;ALT+ARROW_DOWN&quot;</code>,
   * <code>&quot;ALT+SHIFT+X&quot;</code>.
   * <p>
   * </p>
   * <p>
   * Example code for implementing a key binding: <code><pre>
   * display.setData( RWT.ACTIVE_KEYS, new String[] { &quot;CTRL+1&quot;, &quot;CTRL+2&quot; } );
   * display.addFilter( SWT.KeyDown, new Listener() {
   *   public void handleEvent( Event event ) {
   *     boolean ctrlPressed = ( event.stateMask &amp; SWT.Ctrl ) != 0;
   *     if( ctrlPressed &amp;&amp; event.character == '1' ) {
   *       // handle Ctrl+1
   *     }
   *   }
   * } );
   * </pre></code>
   * </p>
   *
   * @see Display#setData(String,Object)
   * @see Display#addFilter(int, Listener)
   * @see RWT#CANCEL_KEYS
   */
  public static final String ACTIVE_KEYS = "org.eclipse.rap.rwt.activeKeys";

  /**
   * The property to use in <code>Display.setData()</code> in order to always cancel the client's
   * default operation associated with certain key sequences. It allows the same values as
   * {@link RWT#ACTIVE_KEYS}. If a key sequences is given in {@link RWT#CANCEL_KEYS} as well as
   * in {@link RWT#ACTIVE_KEYS}, it will cancel its default operation, but still issue the event.
   * <p>
   * The property can also be used in <code>Control.setData()</code>. In this case, the associated
   * default operation will only be cancelled if the control is focused.
   * </p>
   *
   * <p>
   * Depending on the client, there may be certain keys that cannot be cancelled.
   * </p>
   *
   * @see Display#setData(String,Object)
   * @see RWT#ACTIVE_KEYS
   */
  public static final String CANCEL_KEYS = "org.eclipse.rap.rwt.cancelKeys";

  /**
   * The property to use in <code>Display.setData()</code> in order to set the key combination for
   * mnemonics activation. The value for this property has to be a String.
   * <p>
   * Valid string for key sequence consist of any number of modifier keys, separated by
   * <code>+</code>.
   * </p>
   * <p>
   * Mnemonics are currently supported by <code>MenuItem</code>, <code>Button</code>,
   * <code>Label</code>, <code>CLabel</code>, <code>Group</code>, <code>ToolItem</code>,
   * <code>TabItem</code> and <code>CTabItem</code>. Mnemonics are not supported on a widgets
   * with enabled markup.
   * </p>
   * <p>
   * Example code:<code><pre>
   * display.setData( RWT.MNEMONIC_ACTIVATOR, &quot;ALT+CTRL&quot; );
   * </pre></code>
   * </p>
   *
   * @see Display#setData(String,Object)
   * @since 2.1
   */
  public static final String MNEMONIC_ACTIVATOR = "org.eclipse.rap.rwt.mnemonicActivator";

  /**
   * The property to use in <code>Control.setData()</code> in order to set a custom item height.
   * The custom item height must be specified as an <code>Integer</code> and passed to
   * <code>setData()</code> with this constant as the key.
   * <p>
   * For example:
   * <code>table.setData( RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf( 45 ) );</code>
   * </p>
   * <p><b>Used By:</b><ul>
   * <li><code>Table</code></li>
   * <li><code>Tree</code></li>
   * <li><code>List</code></li>
   * </ul></p>
   *
   * @see Control#setData(String,Object)
   */
  public static final String CUSTOM_ITEM_HEIGHT = "org.eclipse.rap.rwt.customItemHeight";

  /**
   * Controls the number of preloaded items outside (above and below) visible area of virtual
   * <code>Tree</code> or <code>Table</code>. The preloaded items must be specified as an
   * <code>Integer</code> and passed to <code>setData()</code> with this constant as the key.
   * <p>
   * For example: <code>table.setData( RWT.PRELOADED_ITEMS, Integer.valueOf( 10 ) );</code>
   * </p>
   * <p>
   * <b>Used By:</b>
   * <ul>
   * <li><code>Table</code></li>
   * <li><code>Tree</code></li>
   * </ul>
   * </p>
   *
   * @see Control#setData(String,Object)
   * @since 2.2
   */
  public static final String PRELOADED_ITEMS = "org.eclipse.rap.rwt.preloadedItems";

  /**
   * Controls whether the use of <em>markup</em> in text is enabled. To enable
   * markup in text, this constant must be passed to <code>setData()</code> with
   * a value of <code>Boolean.TRUE</code>. The call to <code>setData()</code>
   * must be placed directly after the control is created. Once the markup in text
   * is enabled it's not possible to disable it.
   * <p>
   * For example:
   * <code><pre>
   *   Table table = new Table( parent, SWT.NONE );
   *   table.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
   * </pre></code>
   * </p>
   * <p>
   * When markup is enabled, certain XHTML tags can be used in the text property
   * of the respective widget. Specifying an unsupported element will lead to an
   * {@link IllegalArgumentException} when setting the text. The following table
   * lists the currently supported tags:
   * <dl>
   * <dt>{@literal <b>text</b>}</dt>
   * <dd>renders its content in bold font style</dd>
   * <dt>{@literal <i>text</i>}</dt>
   * <dd>renders its content in italic font style</dd>
   * <dt>{@literal <br/>}</dt>
   * <dd>inserts a line break</dd>
   * <dt>{@literal <sub>}</dt>
   * <dd>renders its content as subscript</dd>
   * <dt>{@literal <sup>}</dt>
   * <dd>renders its content as superscript</dd>
   * <dt>{@literal <big>}</dt>
   * <dd>renders its content with bigger font size</dd>
   * <dt>{@literal <small>}</dt>
   * <dd>renders its content with smaller font size</dd>
   * <dt>{@literal <del>}</dt>
   * <dd>renders its content as deleted text</dd>
   * <dt>{@literal <ins>}</dt>
   * <dd>renders its content as inserted text</dd>
   * <dt>{@literal <em>}</dt>
   * <dd>renders its content as emphasized text</dd>
   * <dt>{@literal <strong>}</dt>
   * <dd>renders its content as strong emphasized text</dd>
   * <dt>{@literal <dfn>}</dt>
   * <dd>renders its content as instance definition</dd>
   * <dt>{@literal <code>}</dt>
   * <dd>renders its content as computer code fragment</dd>
   * <dt>{@literal <samp>}</dt>
   * <dd>renders its content as sample program output</dd>
   * <dt>{@literal <kbd>}</dt>
   * <dd>renders its content as text to be entered by the user</dd>
   * <dt>{@literal <var>}</dt>
   * <dd>renders its content as instance of a variable or program argument</dd>
   * <dt>{@literal <cite>}</dt>
   * <dd>renders its content as citation</dd>
   * <dt>{@literal <q>}</dt>
   * <dd>renders its content as short inline quotation</dd>
   * <dt>{@literal <abbr>}</dt>
   * <dd>renders its content as abbreviation</dd>
   * <dt>{@literal <span>}</dt>
   * <dd>generic style container</dd>
   * <dt>{@literal <img>}</dt>
   * <dd>renders an image</dd>
   * <dt>{@literal <a>}</dt>
   * <dd>renders a hyperlink</dd>
   * </dl>
   * The visual representation of the above tags can be specified in a <code>style</code>
   * attribute.
   * </p>
   * <p>
   * <b>Used By:</b>
   * <ul>
   * <li><code>Table</code></li>
   * <li><code>Tree</code></li>
   * <li><code>Grid</code></li>
   * <li><code>List</code></li>
   * <li><code>Label</code></li>
   * <li><code>CLabel</code></li>
   * <li><code>ToolTip</code></li>
   * <li><code>Button</code></li>
   * </ul>
   * </p>
   *
   * @see Control#setData(String,Object)
   * @see RWT#HYPERLINK
   */
  public static final String MARKUP_ENABLED = "org.eclipse.rap.rwt.markupEnabled";

  /**
   * Controls whether the use of <em>markup</em> in tooltip text is enabled. To enable
   * markup in tooltip text, this constant must be passed to <code>setData()</code> with
   * a value of <code>Boolean.TRUE</code>. The call to <code>setData()</code>
   * must be placed directly after the control is created. Once, the markup in tooltip text
   * is enabled it's not possible to disable it.
   *
   * @see Control#setData(String,Object)
   * @see RWT#MARKUP_ENABLED
   * @since 2.2
   */
  public static final String TOOLTIP_MARKUP_ENABLED = "org.eclipse.rap.rwt.tooltipMarkupEnabled";

  /**
   * Controls the number of fixed columns. This constant must be passed to <code>setData()</code>
   * together with an <code>Integer</code> object. The given number of columns, starting
   * with the current leftmost one, will not scroll horizontally. The call to <code>setData()</code>
   * must be placed directly after the control is created.
   * <p>
   * For example:
   * <code><pre>
   *   Table table = new Table( parent, SWT.NONE );
   *   table.setData( RWT.FIXED_COLUMNS, Integer.valueOf( 2 ) );
   * </pre></code>
   * </p>
   * <b>Used By:</b>
   * <ul>
   * <li><code>Table</code></li>
   * <li><code>Tree</code></li>
   * </ul>
   * </p>
   *
   * @see Control#setData(String,Object)
   */
  public static final String FIXED_COLUMNS = "org.eclipse.rap.rwt.fixedColumns";

  /**
   * Controls the text shown as a badge. This constant must be passed to <code>setData()</code>
   * together with an <code>String</code> object.
   * <p>
   * For example:
   * <code><pre>
   *   TabItem item = new TabItem( folder, SWT.NONE );
   *   item.setData( RWT.BADGE, "23" );
   * </pre></code>
   * </p>
   * <b>Used By:</b>
   * <ul>
   * <li><code>TabItem</code></li>
   * </ul>
   * </p>
   *
   * @see Control#setData(String,Object)
   * @since 3.0
   */
  public static final String BADGE = "org.eclipse.rap.rwt.badge";

  /**
   * The ID of the default theme. The default theme is the active theme if no
   * custom theme has been specified. This ID can be used to register theme
   * contributions to the default theme.
   *
   * @see Application#addStyleSheet(String, String)
   */
  public static final String DEFAULT_THEME_ID = "org.eclipse.rap.rwt.theme.Default";

  /**
   * Used to mark a widget as belonging to a custom variant in order to apply a
   * different theming to it. A custom variant can be applied to any widget like this:
   *
   * <pre>
   * button.setData( RWT.CUSTOM_VARIANT, "mybutton" );
   * </pre>
   *
   * For more information on custom variants, see the RAP help on theming.
   *
   * @see Widget#setData(String,Object)
   * @since 2.0
   */
  public static final String CUSTOM_VARIANT = "org.eclipse.rap.rwt.customVariant";

  /**
   * Used to apply a row template to a control. Row templates replace the column layout model of a
   * Tree or a Table with a custom presentation defined by an instance of
   * {@link org.eclipse.rap.rwt.template.Template}. A template cell will display the content of an
   * item's column when its <em>bindingIndex</em> is set to the corresponding column index.
   * <p>
   * To apply a row template on a control, use the control's <code>setData()</code> method with this
   * constant as key:
   * </p>
   *
   * <pre>
   * Template template = new Template();
   * // add cells to this template
   * new TextCell(template).setBindingIndex(0).setTop(10).setLeft(20) ...;
   * ...
   * Table table = new Table(parent, SWT.FULL_SELECTION);
   * // Add as many columns as needed to add multiple texts/images to items
   * new TableColumn();
   * ...
   * table.setData(RWT.ROW_TEMPLATE, template);
   * </pre>
   * <p>
   * The call to <code>setData()</code> must be placed directly after the control's creation. Once a
   * template is applied to a control, the control will not be affected by changes to the template.
   * </p>
   * <p>
   * Note that TableColumn/TreeColumn instances must be created in order to support multiple item
   * texts/images. If the <code>SWT.FULL_SELECTION</code> style flag is not set, no selection will
   * be displayed.
   * </p>
   * <p>
   * <b>Supported by:</b>
   * <ul>
   * <li><code>Table</code></li>
   * <li><code>Tree</code></li>
   * <li><code>Grid</code></li>
   * </ul>
   * </p>
   *
   * @see org.eclipse.swt.widgets.Control#setData(String,Object)
   * @see org.eclipse.rap.rwt.template.Template
   * @since 2.2
   */
  public static final String ROW_TEMPLATE = "org.eclipse.rap.rwt.rowTemplate";

  /**
   * Used as <em>detail</em> information on a selection event to indicate that a hyperlink (anchor
   * tag) in a markup text was selected instead of the widget that contains the markup. To enable
   * selection events on markup hyperlinks, the <code>a</code> element must have it's
   * <code>target</code> property set to “<code>_rwt</code>”.
   *
   * <p>
   * <b>Supported by:</b>
   * <ul>
   * <li><code>Table</code></li>
   * <li><code>Tree</code></li>
   * <li><code>Grid</code></li>
   * <li><code>List</code></li>
   * </ul>
   * </p>
   *
   * @see RWT#MARKUP_ENABLED
   * @see org.eclipse.swt.events.SelectionEvent#detail
   * @since 2.1
   */
  public static final int HYPERLINK = 1 << 26;

  /**
   * Used as as <em>detail</em> information on a selection event to indicate that a selectable
   * template cell was selected instead of the widget that contains the cell.
   *
   * @see org.eclipse.rap.rwt.template.Cell#setSelectable(boolean)
   * @see org.eclipse.swt.events.SelectionEvent#detail
   * @since 2.2
   */
  public static final int CELL = 1 << 27;

  /**
   * Returns the instance of the resource manager for the current application context. This is a
   * shortcut for <code>RWT.getApplicationContext().getResourceManager()</code>.
   *
   * @return the resource manager for the current application context
   * @see ApplicationContext#getResourceManager()
   */
  public static ResourceManager getResourceManager() {
    return getApplicationContext().getResourceManager();
  }

  /**
   * Returns the instance of the service manager for the current application context. This is a
   * shortcut for <code>RWT.getApplicationContext().getServiceManager()</code>.
   *
   * @return the service manager instance for the current application context
   * @see ApplicationContext#getServiceManager()
   */
  public static ServiceManager getServiceManager() {
    return getApplicationContext().getServiceManager();
  }

  /**
   * Returns the setting store instance for the current UI session. The setting store is a
   * persistent store for user-specific data.
   *
   * @return the setting store for the current session, never <code>null</code>
   */
  public static SettingStore getSettingStore() {
    return ContextProvider.getApplicationContext().getSettingStoreManager().getStore();
  }

  /**
   * Returns the current UI session. This method must be executed from the UI thread.
   *
   * @return the current UI session instance, never <code>null</code>
   * @throws IllegalStateException when called outside of the UI thread
   */
  public static UISession getUISession() {
    checkContext();
    return ContextProvider.getUISession();
  }

  /**
   * Returns the UI session that is associated with the given display.
   *
   * @return the UI session instance for the given display, never <code>null</code>
   */
  public static UISession getUISession( Display display ) {
    ParamCheck.notNull( display, "display" );
    return display.getAdapter( IDisplayAdapter.class ).getUISession();
  }

  /**
   * Returns the <code>ApplicationContext</code> instance that represents the web context's
   * global data storage area.
   *
   * @return instance of {@link ApplicationContext}
   */
  public static ApplicationContext getApplicationContext() {
    checkContext();
    return ContextProvider.getApplicationContext();
  }

  /**
   * Returns the <code>HttpServletRequest</code> that is currently processed.
   * <p>
   * <strong>Note:</strong> This method is <strong>not recommended</strong>. Typical application
   * code should not need to call this method. Processing requests from the client is up to the
   * framework. In rare cases, an application may be wish to access request details such as certain
   * HTTP headers.
   * </p>
   *
   * @return the currently processed request
   */
  public static HttpServletRequest getRequest() {
    checkContext();
    return ContextProvider.getRequest();
  }

  /**
   * Returns the <code>HttpServletResponse</code> that will be sent to the client after processing
   * the current request.
   * <p>
   * <strong>Note:</strong> This method is <strong>not recommended</strong>. Typical application
   * code should not need to call this method. The response should only be written and modified by
   * the framework. In rare cases, an application may wish to access the response, e.g. to add a
   * Cookie.
   * </p>
   *
   * @return the response object that will be sent to the client
   */
  public static HttpServletResponse getResponse() {
    checkContext();
    return ContextProvider.getResponse();
  }

  /**
   * Returns the preferred <code>Locale</code> for the current UI session.
   * This method is a shortcut for <code>RWT.getUISession().getLocale()</code>.
   *
   * @return the preferred <code>Locale</code> for the current UI session.
   *
   * @see UISession#getLocale()
   */
  public static Locale getLocale() {
    return getUISession().getLocale();
  }

  /**
   * Sets the preferred <code>Locale</code> for the current UI session. This method is a shortcut
   * for <code>RWT.getUISession().setLocale( locale )</code>.
   *
   * @param locale the locale to set, or <code>null</code> to reset
   * @see UISession#setLocale(Locale)
   */
  public static void setLocale( Locale locale ) {
    getUISession().setLocale( locale );
  }

  /**
   * Executes the run method of the given <code>runnable</code> on the
   * request thread. This method may only be called from the UI thread.
   * <p>
   * <strong>NOTE:</strong> This API is provisional and may change without
   * further notice.
   * </p>
   * @param runnable the code to be executed on the request thread
   * @throws IllegalStateException when called from a non-UI thread
   */
  public static void requestThreadExec( Runnable runnable ) {
    ParamCheck.notNull( runnable, "runnable" );
    checkContext();
    checkPhase();
    Display display = LifeCycleUtil.getSessionDisplay();
    if( display == null || display.isDisposed() ) {
      SWT.error( SWT.ERROR_DEVICE_DISPOSED );
    }
    LifeCycle lifeCycle = ContextProvider.getApplicationContext().getLifeCycleFactory().getLifeCycle();
    lifeCycle.requestThreadExec( runnable );
  }

  /**
   * Returns a representation of the client that is connected with the server in the current UI
   * session. This is a shortcut for <code>RWT.getUISession().getClient()</code>.
   *
   * @return The client for the current UI session
   * @throws IllegalStateException when called outside of the request context
   */
  public static Client getClient() {
    return getUISession().getClient();
  }

  private static void checkContext() {
    if( !ContextProvider.hasContext() ) {
      throw new IllegalStateException( "Invalid thread access" );
    }
  }

  private static void checkPhase() {
    if( CurrentPhase.get() == null ) {
      throw new IllegalStateException( "Invalid thread access" );
    }
  }

  private RWT() {
    // prevent instantiation
  }

}
