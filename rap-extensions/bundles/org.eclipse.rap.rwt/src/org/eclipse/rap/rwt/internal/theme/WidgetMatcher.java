/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.theme.css.ConditionalValue;
import org.eclipse.swt.widgets.Widget;


public final class WidgetMatcher implements ValueSelector {

  public static interface Constraint {
    boolean matches( Widget widget );
  }

  private final Map<String, Constraint> constraintMap;

  public WidgetMatcher() {
    // This map is accessed by all UI sessions simultaneously. However, We don't need to
    // synchronize get and put since constraints are deterministic, i.e. in case of concurrent
    // insertions one constraint overwriting the other is not critical.
    constraintMap = new ConcurrentHashMap<>();
  }

  public void addStyle( String string, int style ) {
    constraintMap.put( "[" + string, createStyleConstraint( style ) );
  }

  public void addState( String string, Constraint constraint ) {
    constraintMap.put( ":" + string, constraint );
  }

  @Override
  public CssValue select( Widget widget, ConditionalValue... values ) {
    CssValue result = null;
    for( int i = 0; i < values.length && result == null; i++ ) {
      ConditionalValue condValue = values[ i ];
      String[] constraints = condValue.constraints;
      if( matches( widget, constraints ) ) {
        result = condValue.value;
      }
    }
    return result;
  }

  private boolean matches( Widget widget, String[] constraints ) {
    for( String string : constraints ) {
      Constraint constraint = getConstraint( string );
      if( constraint == null || !constraint.matches( widget ) ) {
        return false;
      }
    }
    return true;
  }

  private Constraint getConstraint( String string ) {
    Constraint constraint = constraintMap.get( string );
    if( constraint == null && string.startsWith( "." ) ) {
      constraint = createVariantConstraint( string.substring( 1 ) );
      constraintMap.put( string, constraint );
    }
    return constraint;
  }

  private static Constraint createStyleConstraint( final int style ) {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        return ( widget.getStyle() & style ) != 0;
      }
    };
  }

  private static Constraint createVariantConstraint( final String variant ) {
    return new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        return hasVariant( widget, variant );
      }
    };
  }

  private static boolean hasVariant( Widget widget, String variant ) {
    String actualVariant = WidgetUtil.getVariant( widget );
    return actualVariant != null && actualVariant.equals( variant );
  }

}
