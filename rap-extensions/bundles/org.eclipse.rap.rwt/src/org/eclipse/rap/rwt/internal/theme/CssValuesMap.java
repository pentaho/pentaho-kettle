/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rap.rwt.internal.theme.css.ConditionalValue;
import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;


/**
 * Contains the values defined in a CSS style sheet in an optimized structure
 * for providing quick access to the values for a given element and property.
 */
public final class CssValuesMap {

  private final Map<String, Map<String, ConditionalValue[]>> elementsMap;

  public CssValuesMap( StyleSheet styleSheet, ThemeableWidget[] themeableWidgets ) {
    elementsMap = new HashMap<>();
    extractValues( styleSheet, themeableWidgets );
  }

  public ConditionalValue[] getValues( String elementName, String propertyName ) {
    ConditionalValue[] result = null;
    Map<String, ConditionalValue[]> valuesMap = elementsMap.get( elementName );
    if( valuesMap != null ) {
      result = valuesMap.get( propertyName );
    }
    if( result == null ) {
      result = elementsMap.get( "*" ).get( propertyName );
    }
    return result;
  }

  public CssValue[] getAllValues() {
    Set<CssValue> resultSet = new LinkedHashSet<>();
    for( Map<String, ConditionalValue[]> valuesMap : elementsMap.values() ) {
      for( ConditionalValue[] condValues : valuesMap.values() ) {
        for( ConditionalValue condValue : condValues ) {
          resultSet.add( condValue.value );
        }
      }
    }
    return resultSet.toArray( new CssValue[ resultSet.size() ] );
  }

  private void extractValues( StyleSheet styleSheet, ThemeableWidget[] themeableWidgets ) {
    for( ThemeableWidget themeableWidget : themeableWidgets ) {
      extractValuesForWidget( styleSheet, themeableWidget );
    }
  }

  private void extractValuesForWidget( StyleSheet styleSheet, ThemeableWidget themeableWidget ) {
    if( themeableWidget.elements != null ) {
      for( CssElement element : themeableWidget.elements ) {
        extractValuesForElement( styleSheet, element );
      }
    }
  }

  private void extractValuesForElement( StyleSheet styleSheet, CssElement element ) {
    String elementName = element.getName();
    String[] properties = element.getProperties();
    Map<String, ConditionalValue[]> valuesMap = new LinkedHashMap<>();
    elementsMap.put( elementName, valuesMap );
    for( String propertyName : properties ) {
      ConditionalValue[] values = styleSheet.getValues( elementName, propertyName );
      valuesMap.put( propertyName, filterValues( values, element ) );
    }
  }

  private static ConditionalValue[] filterValues( ConditionalValue[] values, CssElement element ) {
    Collection<ConditionalValue> resultList = new ArrayList<>();
    String[] latestConstraint = null;
    for( ConditionalValue value : values ) {
      if( !Arrays.equals( latestConstraint, value.constraints ) ) {
        if( matches( element, value.constraints ) ) {
          resultList.add( value );
          latestConstraint = value.constraints;
        }
      }
    }
    return resultList.toArray( new ConditionalValue[ resultList.size() ] );
  }

  private static boolean matches( CssElement element, String[] constraints ) {
    boolean passed = true;
    // TODO [rst] Revise: no restrictions for * rules
    if( !"*".equals( element.getName() ) ) {
      for( int k = 0; k < constraints.length && passed; k++ ) {
        String constraint = constraints[ k ];
        if( constraint.charAt( 0 ) == ':' ) {
          passed &= contains( element.getStates(), constraint.substring( 1 ) );
        } else if( constraint.charAt( 0 ) == '[' ) {
          passed &= contains( element.getStyles(), constraint.substring( 1 ) );
        }
      }
    }
    return passed;
  }

  private static boolean contains( String[] elements, String string ) {
    for( int i = 0; i < elements.length; i++ ) {
      if( string.equals( elements[ i ] ) ) {
        return true;
      }
    }
    return false;
  }

}
