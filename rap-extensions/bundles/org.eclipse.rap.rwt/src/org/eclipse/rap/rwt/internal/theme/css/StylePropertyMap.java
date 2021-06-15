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
package org.eclipse.rap.rwt.internal.theme.css;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.rap.rwt.internal.theme.CssValue;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


public class StylePropertyMap {

  private final Map<String, CssValue> properties;

  public StylePropertyMap() {
    properties = new HashMap<>();
  }

  public void setProperty( String key, CssValue value ) {
    ParamCheck.notNull( key, "key" );
    ParamCheck.notNull( value, "value" );
    properties.put( key, value );
  }

  public String[] getProperties() {
    Set<String> keySet = properties.keySet();
    return keySet.toArray( new String[ keySet.size() ] );
  }

  public CssValue getValue( String propertyName ) {
    return properties.get( propertyName );
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append( "{\n" );
    String[] properties = getProperties();
    for( int i = 0; i < properties.length; i++ ) {
      String property = properties[ i ];
      CssValue value = getValue( property );
      result.append( "  " );
      result.append( property );
      result.append( ": " );
      result.append( value );
      result.append( ";\n" );
    }
    result.append( "}" );
    return result.toString();
  }

  @Override
  public boolean equals( Object obj ) {
    boolean result = false;
    if( obj == this ) {
      result = true;
    } else if( obj.getClass() == this.getClass() ) {
      StylePropertyMap other = ( StylePropertyMap )obj;
      result = properties.equals( other.properties );
    }
    return result;
  }

  @Override
  public int hashCode() {
    return properties.hashCode();
  }

}
