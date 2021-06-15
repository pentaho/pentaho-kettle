/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.theme.ThemePropertyAdapterRegistry.ThemePropertyAdapter;
import org.eclipse.rap.rwt.internal.theme.css.ConditionalValue;
import org.eclipse.rap.rwt.service.ApplicationContext;


public final class ThemeStoreWriter {

  private final CssElement[] allThemeableWidgetElements;
  private final Theme theme;
  private final ApplicationContext applicationContext;

  public ThemeStoreWriter( ApplicationContext applicationContext,
                           Theme theme,
                           CssElement[] elements )
  {
    this.applicationContext = applicationContext;
    this.theme = theme;
    allThemeableWidgetElements = elements;
  }

  public String createJson() {
    CssValue[] allValues = theme.getValuesMap().getAllValues();
    Map<String, JsonObject> valuesMap = createValuesMap( allValues );
    JsonObject json = new JsonObject();
    json.add( "values", createJsonFromValuesMap( valuesMap ) );
    json.add( "theme", createThemeJson() );
    return json.toString();
  }

  private JsonObject createThemeJson() {
    JsonObject result = new JsonObject();
    CssValuesMap valuesMap = theme.getValuesMap();
    for( int i = 0; i < allThemeableWidgetElements.length; i++ ) {
      CssElement element = allThemeableWidgetElements[ i ];
      String elementName = element.getName();
      JsonObject elementObj = createThemeJsonForElement( valuesMap, element );
      result.add( elementName, elementObj );
    }
    return result;
  }

  private JsonObject createThemeJsonForElement( CssValuesMap valuesMap, CssElement element ) {
    JsonObject result = new JsonObject();
    ThemePropertyAdapterRegistry registry
      = ThemePropertyAdapterRegistry.getInstance( applicationContext );
    for( String propertyName : element.getProperties() ) {
      JsonArray valuesArray = new JsonArray();
      String elementName = element.getName();
      for( ConditionalValue conditionalValue : valuesMap.getValues( elementName, propertyName ) ) {
        JsonArray array = new JsonArray();
        array.add( createJsonArray( conditionalValue.constraints ) );
        CssValue value = conditionalValue.value;
        ThemePropertyAdapter adapter = registry.getPropertyAdapter( value.getClass() );
        String cssKey = adapter.getKey( value );
        array.add( cssKey );
        valuesArray.add( array );
      }
      result.add( propertyName, valuesArray );
    }
    return result;
  }

  private Map<String, JsonObject> createValuesMap( CssValue[] values ) {
    Map<String, JsonObject> result = new LinkedHashMap<>();
    for( CssValue value : values ) {
      appendValueToMap( value, result );
    }
    return result;
  }

  private void appendValueToMap( CssValue propertyValue, Map<String,JsonObject> valuesMap ) {
    ThemePropertyAdapterRegistry registry
      = ThemePropertyAdapterRegistry.getInstance( applicationContext );
    ThemePropertyAdapter adapter = registry.getPropertyAdapter( propertyValue.getClass() );
    if( adapter != null ) {
      String slot = adapter.getSlot( propertyValue );
      if( slot != null ) {
        String key = adapter.getKey( propertyValue );
        JsonValue value = adapter.getValue( propertyValue );
        if( value != null ) {
          JsonObject slotObject = getSlot( valuesMap, slot );
          slotObject.add( key, value );
        }
      }
    }
  }

  private static JsonValue createJsonFromValuesMap( Map<String, JsonObject> valuesMap ) {
    JsonObject result = new JsonObject();
    for( Entry<String, JsonObject> entry : valuesMap.entrySet() ) {
      result.add( entry.getKey(), entry.getValue() );
    }
    return result;
  }

  private static JsonObject getSlot( Map<String,JsonObject> valuesMap, String name ) {
    JsonObject result = valuesMap.get( name );
    if( result == null ) {
      result = new JsonObject();
      valuesMap.put( name, result );
    }
    return result;
  }

}
