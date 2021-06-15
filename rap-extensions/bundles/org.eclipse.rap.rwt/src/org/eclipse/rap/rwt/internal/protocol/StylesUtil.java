/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Widget;


public final class StylesUtil {

  private static final Map<String, Integer> availableStyles;

  static {
    availableStyles = new LinkedHashMap<>();
    initializeAvailableStyles();
  }

  private static void initializeAvailableStyles() {
    Field[] constants = SWT.class.getDeclaredFields();
    for( Field constant : constants ) {
      if( constant.getType() == int.class ) {
        addStyleToMap( constant );
      }
    }
  }

  private static void addStyleToMap( Field constant ) {
    String styleName = constant.getName();
    try {
      constant.setAccessible( true );
      Integer value = Integer.valueOf( constant.getInt( null ) ); // use null because we access statics
      availableStyles.put( styleName, value );
    } catch( Exception e ) {
      String message = "Could not initialize SWT styles map with constant " + styleName;
      throw new RuntimeException( message, e );
    }
  }

  public static String[] filterStyles( Widget widget, String... allowedStyles ) {
    return filterStyles( widget.getStyle(), allowedStyles );
  }

  public static String[] filterStyles( int styles, String... allowedStyles ) {
    List<String> containedStyles = findContainedStyles( styles, allowedStyles );
    if( containedStyles.isEmpty() ) {
      containedStyles.add( "NONE" );
    }
    String[] result = new String[ containedStyles.size() ];
    return containedStyles.toArray( result );
  }

  private static List<String> findContainedStyles( int styles, String... allowedStyles ) {
    List<String> containedStyles = new ArrayList<>();
    for( String allowedStyle : allowedStyles ) {
      Integer object = availableStyles.get( allowedStyle );
      if( object == null ) {
        throw new IllegalArgumentException( allowedStyle + " is not an existing SWT style" );
      }
      if( ( styles & object.intValue() ) != 0 ) {
        containedStyles.add( allowedStyle );
      }
    }
    return containedStyles;
  }

  private StylesUtil() {
    // prevent instantiation
  }

}
