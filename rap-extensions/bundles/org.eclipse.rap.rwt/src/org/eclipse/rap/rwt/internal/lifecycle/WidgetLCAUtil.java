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
package org.eclipse.rap.rwt.internal.lifecycle;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getVariant;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonValue;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil.getClientListenerOperations;
import static org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil.getRemoteId;
import static org.eclipse.rap.rwt.internal.util.MnemonicUtil.removeAmpersandControlCharacters;
import static org.eclipse.rap.rwt.remote.JsonMapping.toJson;
import static org.eclipse.swt.internal.events.EventLCAUtil.containsEvent;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isToolTipMarkupEnabledFor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.protocol.StylesUtil;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation.AddListener;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation.RemoveListener;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;


public final class WidgetLCAUtil {

  private static final String PROP_TOOLTIP = "toolTip";
  private static final String PROP_TOOLTIP_MARKUP_ENABLED = "toolTipMarkupEnabled";
  private static final String PROP_FONT = "font";
  private static final String PROP_FOREGROUND = "foreground";
  private static final String PROP_BACKGROUND = "background";
  private static final String PROP_BACKGROUND_TRANSPARENCY = "backgroundTrans";
  private static final String PROP_BACKGROUND_GRADIENT_COLORS = "backgroundGradientColors";
  private static final String PROP_BACKGROUND_GRADIENT_PERCENTS = "backgroundGradientPercents";
  private static final String PROP_BACKGROUND_GRADIENT_VERTICAL = "backgroundGradientVertical";
  private static final String PROP_ROUNDED_BORDER_WIDTH = "roundedBorderWidth";
  private static final String PROP_ROUNDED_BORDER_COLOR = "roundedBorderColor";
  private static final String PROP_ROUNDED_BORDER_RADIUS = "roundedBorderRadius";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_DATA = "data";
  private static final String PROP_VARIANT = "customVariant";
  private static final String PROP_HELP_LISTENER = "Help";
  private static final String PROP_SELECTION_LISTENER = "Selection";
  private static final String PROP_DEFAULT_SELECTION_LISTENER = "DefaultSelection";
  private static final String PROP_MODIFY_LISTENER = "Modify";
  private static final String PROP_KEY_LISTENER = "KeyDown";

  private static final Rectangle DEF_ROUNDED_BORDER_RADIUS = new Rectangle( 0, 0, 0, 0 );

  private WidgetLCAUtil() {
    // prevent instantiation
  }

  public static void preserveBounds( Widget widget, Rectangle bounds ) {
    getAdapter( widget ).preserve( Props.BOUNDS, bounds );
  }

  public static void preserveEnabled( Widget widget, boolean enabled ) {
    getAdapter( widget ).preserve( PROP_ENABLED, Boolean.valueOf( enabled ) );
  }

  public static void preserveToolTipText( Widget widget, String toolTip ) {
    String text = toolTip == null ? "" : toolTip;
    getAdapter( widget ).preserve( PROP_TOOLTIP, text );
  }

  public static void preserveFont( Widget widget, Font font ) {
    getAdapter( widget ).preserve( PROP_FONT, font );
  }

  public static void preserveForeground( Widget widget, Color foreground ) {
    getAdapter( widget ).preserve( PROP_FOREGROUND, foreground );
  }

  public static void preserveBackground( Widget widget, Color background ) {
    preserveBackground( widget, background, false );
  }

  public static void preserveBackground( Widget widget, Color background, boolean transparency ) {
    RemoteAdapter adapter = getAdapter( widget );
    adapter.preserve( PROP_BACKGROUND, background );
    adapter.preserve( PROP_BACKGROUND_TRANSPARENCY, Boolean.valueOf( transparency ) );
  }

  public static void preserveBackgroundGradient( Widget widget ) {
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    if( adapter != null ) {
      IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
      Color[] bgGradientColors = gfxAdapter.getBackgroundGradientColors();
      int[] bgGradientPercents = gfxAdapter.getBackgroundGradientPercents();
      boolean bgGradientVertical = gfxAdapter.isBackgroundGradientVertical();
      RemoteAdapter widgetAdapter = getAdapter( widget );
      widgetAdapter.preserve( PROP_BACKGROUND_GRADIENT_COLORS, bgGradientColors );
      widgetAdapter.preserve( PROP_BACKGROUND_GRADIENT_PERCENTS, bgGradientPercents );
      widgetAdapter.preserve( PROP_BACKGROUND_GRADIENT_VERTICAL,
                              Boolean.valueOf( bgGradientVertical ) );
    }
  }

  public static void preserveRoundedBorder( Widget widget ) {
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    if( adapter != null ) {
      IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
      int width = gfxAdapter.getRoundedBorderWidth();
      Color color = gfxAdapter.getRoundedBorderColor();
      Rectangle radius = gfxAdapter.getRoundedBorderRadius();
      RemoteAdapter widgetAdapter = getAdapter( widget );
      widgetAdapter.preserve( PROP_ROUNDED_BORDER_WIDTH, Integer.valueOf( width ) );
      widgetAdapter.preserve( PROP_ROUNDED_BORDER_COLOR, color );
      widgetAdapter.preserve( PROP_ROUNDED_BORDER_RADIUS, radius );
    }
  }

  public static void preserveData( Widget widget ) {
    WidgetRemoteAdapter remoteAdapter = getRemoteAdapter( widget );
    if( !remoteAdapter.hasPreservedData() ) {
      remoteAdapter.preserveData( getData( widget ) );
    }
  }

  public static void renderData( Widget widget ) {
    WidgetRemoteAdapter remoteAdapter = getRemoteAdapter( widget );
    if( remoteAdapter.hasPreservedData() ) {
      Object[] actual = getData( widget );
      Object[] preserved = remoteAdapter.getPreservedData();
      if( changed( widget, actual, preserved, null ) ) {
        getRemoteObject( widget ).set( PROP_DATA, getJsonForData( actual ) );
      }
    }
  }

  public static void preserveCustomVariant( Widget widget ) {
    WidgetRemoteAdapter remoteAdapter = getRemoteAdapter( widget );
    if( !remoteAdapter.hasPreservedVariant() ) {
      remoteAdapter.preserveVariant( getVariant( widget ) );
    }
  }

  public static void renderCustomVariant( Widget widget ) {
    WidgetRemoteAdapter remoteAdapter = getRemoteAdapter( widget );
    if( remoteAdapter.hasPreservedVariant() ) {
      String actual = getVariant( widget );
      String preserved = remoteAdapter.getPreservedVariant();
      if( changed( widget, actual, preserved, null ) ) {
        getRemoteObject( widget ).set( PROP_VARIANT, actual == null ? null : "variant_" + actual );
      }
    }
  }

  public static void renderBounds( Widget widget, Rectangle bounds ) {
    renderProperty( widget, Props.BOUNDS, bounds, null );
  }

  public static void renderEnabled( Widget widget, boolean enabled ) {
    renderProperty( widget, Props.ENABLED, enabled, true );
  }

  public static void renderMenu( Widget widget, Menu menu ) {
    renderProperty( widget, Props.MENU, menu, null );
  }

  public static void renderToolTip( Widget widget, String toolTip ) {
    renderToolTipMarkupEnabled( widget );
    String text = toolTip == null ? "" : toolTip;
    if( hasChanged( widget, PROP_TOOLTIP, text, "" ) ) {
      if( !isToolTipMarkupEnabledFor( widget ) ) {
        text = removeAmpersandControlCharacters( text );
      }
      getRemoteObject( widget ).set( PROP_TOOLTIP, text );
    }
  }

  static void renderToolTipMarkupEnabled( Widget widget ) {
    RemoteAdapter adapter = getAdapter( widget );
    if( !adapter.isInitialized() && isToolTipMarkupEnabledFor( widget ) ) {
      getRemoteObject( widget ).set( PROP_TOOLTIP_MARKUP_ENABLED, true );
    }
  }

  public static void renderFont( Widget widget, Font font ) {
    if( hasChanged( widget, PROP_FONT, font, null ) ) {
      getRemoteObject( widget ).set( PROP_FONT, toJson( font ) );
    }
  }

  public static void renderForeground( Widget widget, Color newColor ) {
    if( hasChanged( widget, PROP_FOREGROUND, newColor, null ) ) {
      getRemoteObject( widget ).set( PROP_FOREGROUND, toJson( newColor ) );
    }
  }

  public static void renderBackground( Widget widget, Color newColor ) {
    renderBackground( widget, newColor, false );
  }

  public static void renderBackground( Widget widget, Color background, boolean transparency ) {
    boolean transparencyChanged = hasChanged( widget,
                                              PROP_BACKGROUND_TRANSPARENCY,
                                              Boolean.valueOf( transparency ),
                                              Boolean.FALSE );
    boolean colorChanged = hasChanged( widget, PROP_BACKGROUND, background, null );
    if( transparencyChanged || colorChanged ) {
      JsonValue color = transparency && background == null
                      ? toJson( new RGB( 0, 0, 0 ), 0 )
                      : toJson( background, transparency ? 0 : 255 );
      getRemoteObject( widget ).set( PROP_BACKGROUND, color );
    }
  }

  public static void renderBackgroundGradient( Widget widget ) {
    if( hasBackgroundGradientChanged( widget ) ) {
      Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
      IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
      Color[] bgGradientColors = graphicsAdapter.getBackgroundGradientColors();
      JsonValue args = JsonValue.NULL;
      if( bgGradientColors!= null ) {
        JsonArray colors = new JsonArray();
        for( int i = 0; i < bgGradientColors.length; i++ ) {
          colors.add( toJson( bgGradientColors[ i ] ) );
        }
        int[] bgGradientPercents = graphicsAdapter.getBackgroundGradientPercents();
        JsonValue percents = createJsonArray( bgGradientPercents );
        boolean bgGradientVertical = graphicsAdapter.isBackgroundGradientVertical();
        args = new JsonArray()
          .add( colors )
          .add( percents )
          .add( bgGradientVertical );
      }
      getRemoteObject( widget ).set( "backgroundGradient", args );
    }
  }

  private static boolean hasBackgroundGradientChanged( Widget widget ) {
    IWidgetGraphicsAdapter graphicsAdapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    Color[] bgGradientColors = graphicsAdapter.getBackgroundGradientColors();
    int[] bgGradientPercents = graphicsAdapter.getBackgroundGradientPercents();
    boolean bgGradientVertical = graphicsAdapter.isBackgroundGradientVertical();
    return    hasChanged( widget,
                          PROP_BACKGROUND_GRADIENT_COLORS,
                          bgGradientColors,
                          null )
           || hasChanged( widget,
                          PROP_BACKGROUND_GRADIENT_PERCENTS,
                          bgGradientPercents,
                          null )
           || hasChanged( widget,
                          PROP_BACKGROUND_GRADIENT_VERTICAL,
                          Boolean.valueOf( bgGradientVertical ),
                          Boolean.FALSE );
  }

  public static void renderRoundedBorder( Widget widget ) {
    if( hasRoundedBorderChanged( widget ) ) {
      Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
      IWidgetGraphicsAdapter graphicAdapter = ( IWidgetGraphicsAdapter )adapter;
      JsonValue args = JsonValue.NULL;
      int width = graphicAdapter.getRoundedBorderWidth();
      Color color = graphicAdapter.getRoundedBorderColor();
      if( width > 0 && color != null ) {
        Rectangle radius = graphicAdapter.getRoundedBorderRadius();
        args = new JsonArray()
          .add( width )
          .add( toJson( color ) )
          .add( radius.x )
          .add( radius.y )
          .add( radius.width )
          .add( radius.height );
      }
      getRemoteObject( widget ).set( "roundedBorder", args );
    }
  }

  private static boolean hasRoundedBorderChanged( Widget widget ) {
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    int width = graphicsAdapter.getRoundedBorderWidth();
    Color color = graphicsAdapter.getRoundedBorderColor();
    Rectangle radius = graphicsAdapter.getRoundedBorderRadius();
    return
         hasChanged( widget,
                     PROP_ROUNDED_BORDER_WIDTH,
                     Integer.valueOf( width ),
                     Integer.valueOf( 0 ) )
      || hasChanged( widget,
                     PROP_ROUNDED_BORDER_COLOR,
                     color,
                     null )
      || hasChanged( widget,
                     PROP_ROUNDED_BORDER_RADIUS,
                     radius,
                     DEF_ROUNDED_BORDER_RADIUS );
  }

  public static boolean wasEventSent( Widget widget, String eventName ) {
    return ProtocolUtil.wasEventSent( getId( widget ), eventName );
  }

  public static void preserveProperty( Widget widget, String property, Object value ) {
    getAdapter( widget ).preserve( property, value );
  }

  public static void preserveProperty( Widget widget, String property, int value ) {
    preserveProperty( widget, property, Integer.valueOf( value ) );
  }

  public static void preserveProperty( Widget widget, String property, boolean value ) {
    preserveProperty( widget, property, Boolean.valueOf( value ) );
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     String newValue,
                                     String defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      getRemoteObject( widget ).set( property, newValue );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Integer newValue,
                                     Integer defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : JsonValue.valueOf( newValue.intValue() );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     String[] newValue,
                                     String[] defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : createJsonArray( newValue );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     boolean[] newValue,
                                     boolean[] defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : createJsonArray( newValue );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     int[] newValue,
                                     int[] defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : createJsonArray( newValue );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     int newValue,
                                     int defaultValue )
  {
    Integer newValueObject = Integer.valueOf( newValue );
    Integer defaultValueObject = Integer.valueOf( defaultValue );
    if( hasChanged( widget, property, newValueObject, defaultValueObject ) ) {
      getRemoteObject( widget ).set( property, newValue );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     boolean newValue,
                                     boolean defaultValue )
  {
    Boolean newValueObject = Boolean.valueOf( newValue );
    Boolean defaultValueObject = Boolean.valueOf( defaultValue );
    if( hasChanged( widget, property, newValueObject, defaultValueObject ) ) {
      getRemoteObject( widget ).set( property, newValue );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Image newValue,
                                     Image defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      getRemoteObject( widget ).set( property, toJson( newValue ) );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Image[] newValue,
                                     Image[] defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : createJsonArray( newValue );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Color newValue,
                                     Color defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      getRemoteObject( widget ).set( property, toJson( newValue ) );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Color[] newValue,
                                     Color[] defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : createJsonArray( newValue );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Font[] newValue,
                                     Font[] defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      JsonValue value = newValue == null ? JsonValue.NULL : createJsonArray( newValue );
      getRemoteObject( widget ).set( property, value );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Point newValue,
                                     Point defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      getRemoteObject( widget ).set( property, toJson( newValue ) );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Rectangle newValue,
                                     Rectangle defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      getRemoteObject( widget ).set( property, toJson( newValue ) );
    }
  }

  public static void renderProperty( Widget widget,
                                     String property,
                                     Widget newValue,
                                     Widget defaultValue )
  {
    if( hasChanged( widget, property, newValue, defaultValue ) ) {
      String widgetId = newValue == null ? null : getId( newValue );
      getRemoteObject( widget ).set( property, widgetId );
    }
  }

  public static void renderListenHelp( Widget widget ) {
    renderListener( widget, SWT.Help, PROP_HELP_LISTENER );
  }

  public static void renderListenSelection( Widget widget ) {
    renderListener( widget, SWT.Selection, PROP_SELECTION_LISTENER );
  }

  public static void renderListenDefaultSelection( Widget widget ) {
    renderListener( widget, SWT.DefaultSelection, PROP_DEFAULT_SELECTION_LISTENER );
  }

  // NOTE: Client does not support Verify, it is created server-side from Modify
  public static void renderListenModifyVerify( Widget widget ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( widget );
    if( adapter.hasPreservedListeners() ) {
      boolean actual = isListening( widget, SWT.Modify ) || isListening( widget, SWT.Verify );
      boolean preserved = containsEvent( adapter.getPreservedListeners(), SWT.Modify )
                       || containsEvent( adapter.getPreservedListeners(), SWT.Verify );
      if( changed( widget, actual, preserved, false ) ) {
        getRemoteObject( widget ).listen( PROP_MODIFY_LISTENER, actual );
      }
    }
  }

  public static void renderListenKey( Widget widget ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( widget );
    if( adapter.hasPreservedListeners() ) {
      boolean actual = isListening( widget, SWT.KeyUp ) || isListening( widget, SWT.KeyDown );
      boolean preserved = containsEvent( adapter.getPreservedListeners(), SWT.KeyUp )
                       || containsEvent( adapter.getPreservedListeners(), SWT.KeyDown );
      if( changed( widget, actual, preserved, false ) ) {
        getRemoteObject( widget ).listen( WidgetLCAUtil.PROP_KEY_LISTENER, actual );
      }
    }
  }

  public static void preserveListener( Widget widget, String listener, boolean value ) {
    getAdapter( widget ).preserve( listener, Boolean.valueOf( value ) );
  }

  public static void renderListener( Widget widget,
                                     String listener,
                                     boolean newValue,
                                     boolean defaultValue )
  {
    Boolean newValueObject = Boolean.valueOf( newValue );
    Boolean defaultValueObject = Boolean.valueOf( defaultValue );
    if( hasChanged( widget, listener, newValueObject, defaultValueObject ) ) {
      getRemoteObject( widget ).listen( listener, newValue );
    }
  }

  public static void preserveListeners( Widget widget, long eventList ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( widget );
    if( !adapter.hasPreservedListeners() ) {
      adapter.preserveListeners( eventList );
    }
  }

  public static void renderListener( Widget widget, int eventType, String eventName ) {
    renderListener( widget, eventType, eventName, isListening( widget, eventType ) );
  }

  private static void renderListener( Widget widget,
                                      int eventType,
                                      String eventName,
                                      boolean isListening )
  {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( widget );
    if( adapter.hasPreservedListeners() ) {
      boolean preserved = containsEvent( adapter.getPreservedListeners(), eventType );
      if( changed( widget, isListening, preserved, false ) ) {
        getRemoteObject( widget ).listen( eventName, isListening );
      }
    }
  }

  public static void renderClientListeners( Widget widget ) {
    List<ClientListenerOperation> operations = getClientListenerOperations( widget );
    if( operations != null ) {
      for( ClientListenerOperation operation : operations ) {
        JsonObject parameters = new JsonObject();
        parameters.add( "listenerId", getRemoteId( operation.getListener() ) );
        parameters.add( "eventType", ClientListenerUtil.getEventType( operation.getEventType() ) );
        if( operation instanceof AddListener ) {
          getRemoteObject( widget ).call( "addListener", parameters );
        } else if( operation instanceof RemoveListener ) {
          getRemoteObject( widget ).call( "removeListener", parameters );
        }
      }
    }
    ClientListenerUtil.clearClientListenerOperations( widget );
  }

  public static boolean hasChanged( Widget widget, String property, Object actualValue ) {
    return !equals( actualValue, getAdapter( widget ).getPreserved( property ) );
  }

  public static boolean hasChanged( Widget widget,
                                    String property,
                                    Object actualValue,
                                    Object defaultValue )
  {
    Object preservedValue = getAdapter( widget ).getPreserved( property );
    return changed( widget, actualValue, preservedValue, defaultValue );
  }

  static boolean changed( Widget widget,
                          Object actualValue,
                          Object preservedValue,
                          Object defaultValue )
  {
    if( getAdapter( widget ).isInitialized() ) {
      return !equals( actualValue, preservedValue );
    }
    return !equals( actualValue, defaultValue );
  }

  static boolean changed( Widget widget,
                          boolean actualValue,
                          boolean preservedValue,
                          boolean defaultValue )
  {
    if( getAdapter( widget ).isInitialized() ) {
      return actualValue != preservedValue;
    }
    return actualValue != defaultValue;
  }

  static boolean changed( Widget widget,
                          int actualValue,
                          int preservedValue,
                          int defaultValue )
  {
    if( getAdapter( widget ).isInitialized() ) {
      return actualValue != preservedValue;
    }
    return actualValue != defaultValue;
  }

  public static String[] getStyles( Widget widget, String[] styles ) {
    return StylesUtil.filterStyles( widget, styles );
  }

  static boolean equals( Object object1, Object object2 ) {
    boolean result;
    if( object1 == object2 ) {
      result = true;
    } else if( object1 == null ) {
      result = false;
    } else if( object1 instanceof boolean[] && object2 instanceof boolean[] ) {
      result = Arrays.equals( ( boolean[] )object1, ( boolean[] )object2 );
    } else if( object1 instanceof int[] && object2 instanceof int[] ) {
      result = Arrays.equals( ( int[] )object1, ( int[] )object2 );
    } else if( object1 instanceof long[] && object2 instanceof long[] ) {
      result = Arrays.equals( ( long[] )object1, ( long[] )object2 );
    } else if( object1 instanceof float[] && object2 instanceof float[] ) {
      result = Arrays.equals( ( float[] )object1, ( float[] )object2 );
    } else if( object1 instanceof double[] && object2 instanceof double[] ) {
      result = Arrays.equals( ( double[] )object1, ( double[] )object2 );
    } else if( object1 instanceof Object[] && object2 instanceof Object[] ) {
      result = Arrays.equals( ( Object[] )object1, ( Object[] )object2 );
    } else {
      result = object1.equals( object2 );
    }
    return result;
  }

  private static Object[] getData( Widget widget ) {
    List<Object> result = null;
    for( String key : WidgetDataUtil.getDataKeys() ) {
      Object value = widget.getData( key );
      if( value != null ) {
        if( result == null ) {
          result = new ArrayList<>();
        }
        result.add( key );
        result.add( value );
      }
    }
    return result == null ? null : result.toArray();
  }

  @SuppressWarnings( "deprecation" )
  private static JsonObject getJsonForData( Object[] data ) {
    JsonObject jsonObject = new JsonObject();
    if( data != null ) {
      for( int i = 0; i < data.length; i++ ) {
        jsonObject.add( (String)data[ i ], createJsonValue( data[ ++i ] ) );
      }
    }
    return jsonObject;
  }

  private static WidgetRemoteAdapter getRemoteAdapter( Widget widget ) {
    return ( WidgetRemoteAdapter )widget.getAdapter( RemoteAdapter.class );
  }

}
